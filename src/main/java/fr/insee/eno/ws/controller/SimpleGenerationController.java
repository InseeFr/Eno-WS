package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import fr.insee.eno.ParameterizedGenerationService;
import fr.insee.eno.parameters.AccompanyingMail;
import fr.insee.eno.parameters.Capture;
import fr.insee.eno.parameters.CaptureEnum;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.EndQuestion;
import fr.insee.eno.parameters.Format;
import fr.insee.eno.parameters.InFormat;
import fr.insee.eno.parameters.Level;
import fr.insee.eno.parameters.Orientation;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.PDFParameters;
import fr.insee.eno.parameters.Parameters;
import fr.insee.eno.parameters.Pipeline;
import fr.insee.eno.parameters.PreProcessing;
import fr.insee.eno.parameters.StudyUnit;
import fr.insee.eno.ws.service.TransformService;
import fr.insee.eno.ws.service.ParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
	
	@Operation(description="Generate pdf questionnaire according to the studyunit")
	@PostMapping(value="{studyUnit}/pdf", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generatePDFQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable StudyUnit studyUnit) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(studyUnit,OutFormat.PDF);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoTempFO = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS);
		File enoOutput = transformService.foToPDFtransform(enoTempFO);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	@Operation(description="Generate pdf questionnaire according to the studyunit")
	@PostMapping(value="{studyUnit}/fo", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFOQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable StudyUnit studyUnit) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(studyUnit, OutFormat.PDF);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	@Operation(description="Generate pdf questionnaire according to the studyunit")
	@PostMapping(value="{studyUnit}/xforms", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXformsQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable StudyUnit studyUnit) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(studyUnit,OutFormat.FR);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	@Operation(description="Generate specification of questionnaire")
	@PostMapping(value="odt", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateODTQuestionnaire(
			@RequestPart(value="in",required=true) MultipartFile in) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);
		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(StudyUnit.DEFAULT,OutFormat.ODT);
		File enoOutput = generationService.generateQuestionnaire(enoInput,enoParameters, null, null);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.ACCEPT, "*")
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	@Operation(description="Generate pdf questionnaire according to the studyunit")
	@PostMapping(value="{studyUnit}/xml-lunatic", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXMLLunaticQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable StudyUnit studyUnit) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(StudyUnit.DEFAULT,OutFormat.JS);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	
	
	@Operation(description="Generate json-lunatic questionnaire according to the studyunit and ")
	@PostMapping(value="{studyUnit}/json-lunatic", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateJSONLunaticQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@PathVariable StudyUnit studyUnit,
			@RequestParam(value="flatModel", defaultValue="true") boolean flatModel) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(StudyUnit.DEFAULT,OutFormat.JS);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoTemp = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS);
		File enoOutput;
		if(flatModel) {
			enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);
		}else {
			enoOutput = transformService.XMLLunaticToJSONLunatic(enoTemp);
		}
		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	
}