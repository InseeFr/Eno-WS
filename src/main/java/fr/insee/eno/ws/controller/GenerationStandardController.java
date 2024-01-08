package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import fr.insee.eno.ws.controller.utils.ResponseUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import fr.insee.eno.parameters.Capture;
import fr.insee.eno.parameters.CaptureEnum;
import fr.insee.eno.parameters.Context;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.FOParameters;
import fr.insee.eno.parameters.Format;
import fr.insee.eno.parameters.Mode;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.service.ParameterService;
import fr.insee.eno.ws.service.QuestionnaireGenerateService;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Generation from DDI (standard parameters)")
@RestController
@RequestMapping("/questionnaire")
public class GenerationStandardController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationStandardController.class);


	private ParameterizedGenerationService parametrizedGenerationService = new ParameterizedGenerationService();


	@Autowired
	private ParameterService parameterService;


	@Autowired
	private TransformService transformService;

	@Autowired
	private QuestionnaireGenerateService generateQuestionnaireService;

	@Operation(
			summary="Generation of fo questionnaire according to the context.",
			description="It generates a fo questionnaire from a ddi questionnaire using the default fo parameters according to the study unit. "
					+ "See it using the end point : */parameter/{context}/default*"
			)
	@PostMapping(value="{context}/fo", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFOQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
			@RequestParam(value="Format-column",required=false) Integer nbColumn,
			@RequestParam(value="Capture",required=false) CaptureEnum capture,
			@PathVariable Context context) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a FO questionnaire with context '{}' using standard parameters.",
				context);
		
		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context,OutFormat.FO,null);
		
		FOParameters foParameters = enoParameters.getParameters().getFoParameters();
	    if(capture!=null) {		
	    	Capture capture2 = foParameters.getCapture();
	    	capture2.setNumeric(capture);
	    	foParameters.setCapture(capture2);;};
	    		
	    if(nbColumn!=null) {
	    Format format = foParameters.getFormat();
		format.setColumns(nbColumn);}
	    
	    InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = parametrizedGenerationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;
		
		return ResponseUtils.generateResponseFromFile(enoOutput);
	}
	

	
	@Operation(
			summary="Generation of xforms questionnaire according to the context.",
			description="It generates a xforms questionnaire from a ddi questionnaire using the default xforms parameters according to the study unit. "
					+ "See it using the end point : */parameter/{context}/default*"
			)
	@PostMapping(value="{context}/xforms", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXformsQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable Context context) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a Xforms questionnaire with context '{}' using standard parameters.",
				context);

		File enoOutput = generateQuestionnaireService.generateQuestionnaireFile(context, OutFormat.XFORMS,null,in, specificTreatment);
		
		return ResponseUtils.generateResponseFromFile(enoOutput);
	}
	
	
	@Operation(
			summary="Generation of lunatic-xml questionnaire according  to the context.",
			description="It generates a lunatic-xml questionnaire from a ddi questionnaire using the default js parameters according to the study unit. "
					+ "See it using the end point : */parameter/{context}/default*"
			)
	@PostMapping(value="{context}/lunatic-xml/{mode}", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXMLLunaticQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable Context context,
			@PathVariable Mode mode) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a Lunatic XML questionnaire with context '{}' and mode '{}' " +
						"using standard parameters.",
				context, mode);

		File enoOutput = generateQuestionnaireService.generateQuestionnaireFile(context, OutFormat.LUNATIC_XML,mode,in, specificTreatment);
		
		return ResponseUtils.generateResponseFromFile(enoOutput);
	}
	
	
	
	@Operation(
			summary="Generation of pdf questionnaire according  to the context.",
			description="It generates a lunatic-json-flat questionnaire from a ddi questionnaire using the default js parameters according to the study unit. "
					+ "See it using the end point : */parameter/{context}/default*"
					+ "The params *parsingXpathVTL* must be 'true' (default value) if controls language is pseudo-xpath."
			)
	@PostMapping(value="{context}/lunatic-json/{mode}", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	
	public ResponseEntity<StreamingResponseBody> generateJSONLunaticQuestionnaire(
			

			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable Context context,
			@PathVariable Mode mode) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a Lunatic (json) questionnaire with context '{}' and mode '{}' " +
						"using standard parameters.",
				context, mode);

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context,OutFormat.LUNATIC_XML,mode);
		
	    InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoTemp = parametrizedGenerationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);
		File enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;
		
		return ResponseUtils.generateResponseFromFile(enoOutput);
	}
	

	@Operation(
			summary="Generation of fodt questionnaire according  to the context.",
			description="It generates a odt questionnaire from a ddi questionnaire using the default js parameters according to the study unit. "
					+ "See it using the end point : */parameter/{context}/default*"
			)
	@PostMapping(value="{context}/fodt", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFodtQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
		
			@PathVariable Context context) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a fodt specification file with context '{}' using standard parameters.",
				context);

		File enoOutput = generateQuestionnaireService.generateQuestionnaireFile(context, OutFormat.FODT,null,in,null);

		return ResponseUtils.generateResponseFromFile(enoOutput);
	}

}