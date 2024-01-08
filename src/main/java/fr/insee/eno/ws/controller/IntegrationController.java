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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import fr.insee.eno.parameters.Context;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.Pipeline;
import fr.insee.eno.parameters.Mode;
import fr.insee.eno.params.ValorizatorParameters;
import fr.insee.eno.params.ValorizatorParametersImpl;
import fr.insee.eno.service.MultiModelService;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.service.ParameterService;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name="Integration of questionnaire")
@RestController
@RequestMapping("/integration-business")
public class IntegrationController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationController.class);

	private MultiModelService multiModelService =  new MultiModelService();
	
	private ParameterizedGenerationService parametrizedGenerationService = new ParameterizedGenerationService();
	
	private ValorizatorParameters valorizatorParameters= new ValorizatorParametersImpl();
	
	@Autowired
	private ParameterService parameterService;
	
	@Autowired
	private TransformService transformService;

	@Operation(
			summary="Integration of business questionnaire according to params, metadata and specificTreatment (business default pipeline is used).",
			description="It generates a questionnaire for intregation with default business pipeline  : using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
			)
	@PostMapping(value= {"ddi-2-xforms"}, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXforms(
			@RequestPart(value="in",required=true) MultipartFile in, 
			@RequestPart(value="params",required=true) MultipartFile params,
			@RequestPart(value="metadata",required=true) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a Xforms questionnaire (business context).");

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params!=null ? params.getInputStream():null;
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		
		ENOParameters currentEnoParams = valorizatorParameters.getParameters(paramsIS);
		Context currentContext = currentEnoParams.getParameters().getContext()!=null ?currentEnoParams.getParameters().getContext():Context.BUSINESS;

		ENOParameters defaultEnoParamsddi2Xforms =  parameterService.getDefaultCustomParameters(currentContext,OutFormat.XFORMS,null);
		
		Pipeline defaultPipeline = defaultEnoParamsddi2Xforms.getPipeline();
		currentEnoParams.setPipeline(defaultPipeline);
		
		File enoOutput = multiModelService.generateQuestionnaire(enoInput, currentEnoParams, metadataIS, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	
	@Operation(
			summary="Integration of questionnaire according to params, metadata and specificTreatment.",
			description="It generates a questionnaire for intregation with default pipeline  : using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
			)
	@PostMapping(value= {"ddi-2-fo"}, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFo(
			@RequestPart(value="in",required=true) MultipartFile in, 
			@RequestPart(value="params",required=true) MultipartFile params,
			@RequestPart(value="metadata",required=true) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a FO questionnaire (business context).");

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params!=null ? params.getInputStream():null;
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		
		ENOParameters currentEnoParams = valorizatorParameters.getParameters(paramsIS);
		Context currentContext = currentEnoParams.getParameters().getContext();

		ENOParameters defaultEnoParamsddi2Fo =  parameterService.getDefaultCustomParameters(currentContext,OutFormat.FO,null);
		
		Pipeline defaultPipeline = defaultEnoParamsddi2Fo.getPipeline();
		currentEnoParams.setPipeline(defaultPipeline);
		
		File enoOutput = multiModelService.generateQuestionnaire(enoInput, currentEnoParams, metadataIS, specificTreatmentIS, null);
		
		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	@Operation(
			summary="Integration of questionnaire according to params, metadata and specificTreatment.",
			description="It generates a questionnaire for intregation with default pipeline  : using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
			)
	@PostMapping(value= {"ddi-2-lunatic-json/{mode}"}, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateLunaticBusiness(
			@PathVariable Mode mode,
			@RequestPart(value="in",required=true) MultipartFile in, 
			@RequestPart(value="params",required=true) MultipartFile params,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a Lunatic questionnaire (business context). Mode={}", mode);
 
		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params!=null ? params.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		
		ENOParameters currentEnoParams = valorizatorParameters.getParameters(paramsIS);
		

		ENOParameters defaultEnoParamsddi2Lunatic = parameterService.getDefaultCustomParameters(Context.BUSINESS,OutFormat.LUNATIC_XML,mode);
		
		Pipeline defaultPipeline = defaultEnoParamsddi2Lunatic.getPipeline();
		currentEnoParams.setPipeline(defaultPipeline);
		currentEnoParams.setMode(mode);
		currentEnoParams.getParameters().setContext(Context.BUSINESS);

		
		File enoTemp = parametrizedGenerationService.generateQuestionnaire(enoInput, currentEnoParams, null, specificTreatmentIS, null);
	    File enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);
		
		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}



}
