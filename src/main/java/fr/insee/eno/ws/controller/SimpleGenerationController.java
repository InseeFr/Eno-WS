package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.Context;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.service.ParameterService;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Simple Generation of questionnaire")
@RestController
@RequestMapping("/questionnaire")
public class SimpleGenerationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGenerationController.class);
	
	private ParameterizedGenerationService generationService = new ParameterizedGenerationService();

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private TransformService transformService;
	
	
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

			@PathVariable Context context) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context, OutFormat.FO);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);
		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
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

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context,OutFormat.XFORMS);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);
		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	
	@Operation(
			summary="Generation of pdf questionnaire according  to the context.",
			description="It generates a lunatic-xml questionnaire from a ddi questionnaire using the default js parameters according to the study unit. "
					+ "See it using the end point : */parameter/{context}/default*"
			)
	@PostMapping(value="{context}/lunatic-xml", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXMLLunaticQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable Context context) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context, OutFormat.LUNATIC_XML);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);
		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	
	
	@Operation(
			summary="Generation of pdf questionnaire according  to the context.",
			description="It generates a lunatic-json-flat questionnaire from a ddi questionnaire using the default js parameters according to the study unit. "
					+ "See it using the end point : */parameter/{context}/default*"
			)
	@PostMapping(value="{context}/lunatic-json-flat", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateJSONLunaticQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable Context context,
			@RequestParam(value="flatModel", defaultValue="true") boolean flatModel) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context, OutFormat.LUNATIC_XML);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoTemp = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);
		File enoOutput;
		if(flatModel) {
			enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);
		}else {
			enoOutput = transformService.XMLLunaticToJSONLunatic(enoTemp);
		}
		
		FileUtils.forceDelete(enoInput);
		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	
}