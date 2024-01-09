package fr.insee.eno.ws.controller;

import fr.insee.eno.parameters.*;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.controller.utils.ResponseUtils;
import fr.insee.eno.ws.service.ParameterService;
import fr.insee.eno.ws.service.QuestionnaireGenerateService;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.InputStream;

@Tag(name="Generation from DDI (standard parameters)")
@RestController
@RequestMapping("/questionnaire")
public class GenerationStandardController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationStandardController.class);

	// Eno-WS services
	private final ParameterService parameterService;
	private final TransformService transformService;
	private final QuestionnaireGenerateService generateQuestionnaireService;

	// Eno core service
	private final ParameterizedGenerationService parametrizedGenerationService;

	public GenerationStandardController(ParameterService parameterService,
										TransformService transformService,
										QuestionnaireGenerateService generateQuestionnaireService) {
		this.parameterService = parameterService;
		this.transformService = transformService;
		this.generateQuestionnaireService = generateQuestionnaireService;
		this.parametrizedGenerationService = new ParameterizedGenerationService();
	}

	@Operation(
			summary="Generation of FO questionnaire from DDI.",
			description="Generation of a FO questionnaire from the given DDI with standard parameters. " +
					"Custom values can be passed for format of columns and capture mode."
			)
	@PostMapping(value="{context}/fo", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFOQuestionnaire(
			//
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
			//
			@RequestParam(value="Format-column",required=false) Integer nbColumn,
			@RequestParam(value="Capture",required=false) CaptureEnum capture,
			//
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
	    	foParameters.setCapture(capture2);
		}
	    		
	    if(nbColumn!=null) {
	    Format format = foParameters.getFormat();
		format.setColumns(nbColumn);}
	    
	    InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = parametrizedGenerationService.generateQuestionnaire(
				enoInput, enoParameters, null, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno FO questionnaire processing");
		LOGGER.info("Output file: {}", enoOutput.getName());
		
		return ResponseUtils.generateResponseFromFile(enoOutput);
	}

	@Operation(
			summary="Generation of Xforms questionnaire from DDI.",
			description="Generation of one or multiple Xforms questionnaires from given DDI with standard parameters. " +
					"If the multi-model option is set to true, the output questionnaire(s) are put in a zip file."
			)
	@PostMapping(value="{context}/xforms", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXformsQuestionnaire(
			//
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
			//
			@PathVariable Context context,
			//
			@RequestParam(value="multi-model",required=false,defaultValue="false") boolean multiModel) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a Xforms questionnaire with context '{}' using standard parameters.",
				context);

		File enoOutput;
		if (! multiModel)
			enoOutput = generateQuestionnaireService.generateQuestionnaireFile(
					context, OutFormat.XFORMS, null, in, specificTreatment);
		else
			enoOutput = generateQuestionnaireService.generateMultiModelQuestionnaires(
					context, OutFormat.XFORMS, null, in, specificTreatment);

		return ResponseUtils.generateResponseFromFile(enoOutput);
	}

	/**
	 * Endpoint to generate a Lunatic XML hierarchical questionnaire from a DDI.
	 * @param in DDI file.
	 * @param specificTreatment Specific treatment file.
	 * @param context Context.
	 * @param mode Collection mode.
	 * @return A response entity to download the output questionnaire.
	 * @throws Exception if something wrong happens...
	 * @deprecated Lunatic questionnaire generation is now supported by Eno Java
	 * (which also makes the Lunatic XML format obsolete).
	 */
	@Operation(
			summary = "Generation of Lunatic XML questionnaire from DDI.",
			description = "**The Lunatic XML format is now deprecated.** " +
					"Generation of a Lunatic XML hierarchical questionnaire from the given DDI " +
					"with standard parameters."
			)
	@PostMapping(value="{context}/lunatic-xml/{mode}", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	@Deprecated(since = "2.0.0")
	public ResponseEntity<StreamingResponseBody> generateXMLLunaticQuestionnaire(
			//
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
			//
			@PathVariable Context context,
			@PathVariable Mode mode) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a Lunatic XML questionnaire with context '{}' and mode '{}' " +
						"using standard parameters.",
				context, mode);

		File enoOutput = generateQuestionnaireService.generateQuestionnaireFile(
				context, OutFormat.LUNATIC_XML, mode, in, specificTreatment);
		
		return ResponseUtils.generateResponseFromFile(enoOutput);
	}

	/**
	 * Endpoint to generate a Lunatic JSON flat questionnaire from a DDI.
	 * @param in DDI file.
	 * @param specificTreatment Specific treatment file.
	 * @param context Context.
	 * @param mode Collection mode.
	 * @return A response entity to download the output questionnaire.
	 * @throws Exception if something wrong happens...
	 * @deprecated Lunatic questionnaire generation is now supported by Eno Java.
	 */
	@Operation(
			summary = "Generation of Lunatic questionnaire from DDI.",
			description = "**This endpoint has been migrated in the Eno 'Java' web-service.** " +
					"Generation of a Lunatic questionnaire from the given DDI with standard parameters. " +
					"The parameter `parsingXpathVTL` must be `true` if expressions are written in Xpath in the DDI."
			)
	@PostMapping(value="{context}/lunatic-json/{mode}", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	@Deprecated(since = "2.0.0")
	public ResponseEntity<StreamingResponseBody> generateJSONLunaticQuestionnaire(
			//
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
			//
			@PathVariable Context context,
			@PathVariable Mode mode) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a Lunatic (json) questionnaire with context '{}' and mode '{}' " +
						"using standard parameters.",
				context, mode);

		File enoTemp = generateQuestionnaireService.generateQuestionnaireFile(
				context, OutFormat.LUNATIC_XML, mode, in, specificTreatment);

		LOGGER.info("Transform Lunatic XML hierarchical to Lunatic JSON flat");
		File enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);

		FileUtils.forceDelete(enoTemp);

		LOGGER.info("Output Lunatic questionnaire json file: {}", enoOutput.getName());
		
		return ResponseUtils.generateResponseFromFile(enoOutput);
	}

	@Operation(
			summary="Generation of FODT specifications from DDI.",
			description="Generation of a FODT description of the questionnaire from the given DDI."
			)
	@PostMapping(value="{context}/fodt", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFodtQuestionnaire(
			//
			@RequestPart(value="in") MultipartFile in,
			//
			@PathVariable Context context) throws Exception {

		LOGGER.info(
				"Received request to transform DDI to a fodt specification file with context '{}' using standard parameters.",
				context);

		File enoOutput = generateQuestionnaireService.generateQuestionnaireFile(
				context, OutFormat.FODT, null, in, null);

		return ResponseUtils.generateResponseFromFile(enoOutput);
	}

}