package fr.insee.eno.ws.controller;

import fr.insee.eno.parameters.*;
import fr.insee.eno.params.ValorizatorParameters;
import fr.insee.eno.params.ValorizatorParametersImpl;
import fr.insee.eno.service.MultiModelService;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.service.ParameterService;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@Tag(name="Generation from DDI (custom parameters)")
@RestController
@RequestMapping("/questionnaire")
public class GenerationCustomController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationCustomController.class);

	// Eno-WS services
	private final ParameterService parameterService;
	private final TransformService transformService;

	// Eno core service
	private final MultiModelService multiModelService = new MultiModelService();
	private final ParameterizedGenerationService parametrizedGenerationService = new ParameterizedGenerationService();
	private final ValorizatorParameters valorizatorParameters = new ValorizatorParametersImpl();

    public GenerationCustomController(ParameterService parameterService, TransformService transformService) {
        this.parameterService = parameterService;
        this.transformService = transformService;
    }

	private static String headersAttachment(File enoOutput) {
		return "attachment;filename=\"" + enoOutput.getName() + "\"";
	}

    @Operation(
			summary="Integration of business questionnaire according to params, metadata and specificTreatment (business default pipeline is used).",
			description="It generates a questionnaire for integration with default business pipeline: using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
	)
	@PostMapping(value= {"ddi-2-xforms"}, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXforms(
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="params") MultipartFile params,
			@RequestPart(value="metadata") MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a Xforms questionnaire (business context).");

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params != null ? params.getInputStream() : null;
		InputStream metadataIS = metadata != null ? metadata.getInputStream() : null;
		InputStream specificTreatmentIS = specificTreatment != null ? specificTreatment.getInputStream() : null;

		ENOParameters currentEnoParams = valorizatorParameters.getParameters(paramsIS);
		Context currentContext = currentEnoParams.getParameters().getContext() != null ?
				currentEnoParams.getParameters().getContext() :
				Context.BUSINESS; // TODO: throw an exception here if the context is not business instead

		ENOParameters defaultEnoParamsDDI2Xforms =  parameterService.getDefaultCustomParameters(
				currentContext, OutFormat.XFORMS, null);
		
		Pipeline defaultPipeline = defaultEnoParamsDDI2Xforms.getPipeline();
		currentEnoParams.setPipeline(defaultPipeline);
		
		File enoOutput = multiModelService.generateQuestionnaire(
				enoInput, currentEnoParams, metadataIS, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno Xforms generation processing");
		LOGGER.info("Output Xforms questionnaire file: {}", enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, headersAttachment(enoOutput))
				.body(stream);
	}

	@Operation(
			summary="Integration of questionnaire according to params, metadata and specificTreatment.",
			description="It generates a questionnaire for integration with default pipeline: using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
	)
	@PostMapping(value= {"ddi-2-fo"}, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFo(
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="params") MultipartFile params,
			@RequestPart(value="metadata") MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a FO questionnaire (business context).");

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params!=null ? params.getInputStream():null;
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		ENOParameters currentEnoParams = valorizatorParameters.getParameters(paramsIS);
		Context currentContext = currentEnoParams.getParameters().getContext();

		ENOParameters defaultEnoParamsDDI2Fo =  parameterService.getDefaultCustomParameters(
				currentContext, OutFormat.FO, null);
		
		Pipeline defaultPipeline = defaultEnoParamsDDI2Fo.getPipeline();
		currentEnoParams.setPipeline(defaultPipeline);
		
		File enoOutput = multiModelService.generateQuestionnaire(enoInput, currentEnoParams, metadataIS, specificTreatmentIS, null);
		
		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno FO generation processing");
		LOGGER.info("Output file: {}", enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, headersAttachment(enoOutput))
				.body(stream);
	}

	@Operation(
			summary="Integration of questionnaire according to params, metadata and specificTreatment.",
			description="It generates a questionnaire for integration with default pipeline: using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
	)
	@PostMapping(value= {"ddi-2-lunatic-json"}, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateLunatic(
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="params") MultipartFile params,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a Lunatic questionnaire.");

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params != null ? params.getInputStream() : null;
		InputStream specificTreatmentIS = specificTreatment != null ? specificTreatment.getInputStream() : null;

		ENOParameters currentEnoParams = valorizatorParameters.getParameters(paramsIS);
		Mode mode = currentEnoParams.getMode();
		Context context = currentEnoParams.getParameters().getContext();

		LOGGER.info("Mode defined in parameters file: {}", mode);

		ENOParameters defaultEnoParamsDDI2Lunatic = parameterService.getDefaultCustomParameters(
				context, OutFormat.LUNATIC_XML, mode);

		Pipeline defaultPipeline = defaultEnoParamsDDI2Lunatic.getPipeline();
		currentEnoParams.setPipeline(defaultPipeline);

		File enoTemp = parametrizedGenerationService.generateQuestionnaire(
				enoInput, currentEnoParams, null, specificTreatmentIS, null);
		File enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("Output file: {}", enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, headersAttachment(enoOutput))
				.body(stream);
	}

}
