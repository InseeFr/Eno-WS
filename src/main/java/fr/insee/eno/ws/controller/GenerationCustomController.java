package fr.insee.eno.ws.controller;

import fr.insee.eno.exception.EnoParametersException;
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
@SuppressWarnings("unused")
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

    @Operation(
			summary="Integration of business questionnaire according to params, metadata and specificTreatment (business default pipeline is used).",
			description="It generates a questionnaire for integration with default business pipeline: using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
	)
	@PostMapping(value = "ddi-2-xforms",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXforms(
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="params") MultipartFile params,
			@RequestPart(value="metadata") MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a Xforms questionnaire.");

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params != null ? params.getInputStream() : null;
		InputStream metadataIS = metadata != null ? metadata.getInputStream() : null;
		InputStream specificTreatmentIS = specificTreatment != null ? specificTreatment.getInputStream() : null;

		ENOParameters enoParameters = valorizatorParameters.getParameters(paramsIS);
		Context context = enoParameters.getParameters().getContext();
		Mode mode = enoParameters.getMode();

		nonNullContextCheck(context);
		if (Context.HOUSEHOLD.equals(context))
			throw new EnoParametersException("Context 'HOUSEHOLD' is not compatible with Xforms format.");
		nonNullModeCheck(mode);
		if (Mode.CAPI.equals(mode) || Mode.CATI.equals(mode) || Mode.PAPI.equals(mode))
			throw new EnoParametersException("Mode '" + mode + "' is not compatible with Xforms format.");

		logModeAndContext(context, mode);

		ENOParameters defaultEnoParamsDDI2Xforms =  parameterService.getDefaultCustomParameters(
				context, OutFormat.XFORMS, null);
		
		Pipeline defaultPipeline = defaultEnoParamsDDI2Xforms.getPipeline();
		enoParameters.setPipeline(defaultPipeline);
		
		File enoOutput = multiModelService.generateQuestionnaire(
				enoInput, enoParameters, metadataIS, specificTreatmentIS, null);

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
	@PostMapping(value = "ddi-2-fo",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFo(
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="params") MultipartFile params,
			@RequestPart(value="metadata") MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		LOGGER.info("Received request to transform DDI to a FO questionnaire.");

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params != null ? params.getInputStream() : null;
		InputStream metadataIS = metadata != null ? metadata.getInputStream() : null;
		InputStream specificTreatmentIS = specificTreatment != null ? specificTreatment.getInputStream() : null;

		ENOParameters enoParameters = valorizatorParameters.getParameters(paramsIS);
		Context context = enoParameters.getParameters().getContext();
		Mode mode = enoParameters.getMode();

		nonNullContextCheck(context);
		nonNullModeCheck(mode);
		if (Mode.CAPI.equals(mode) || Mode.CATI.equals(mode) || Mode.CAWI.equals(mode))
			throw new EnoParametersException("Mode '" + mode + "' is not compatible with FO format.");

		logModeAndContext(context, mode);

		ENOParameters defaultEnoParamsDDI2Fo =  parameterService.getDefaultCustomParameters(
				context, OutFormat.FO, null);
		
		Pipeline defaultPipeline = defaultEnoParamsDDI2Fo.getPipeline();
		enoParameters.setPipeline(defaultPipeline);
		
		File enoOutput = multiModelService.generateQuestionnaire(
				enoInput, enoParameters, metadataIS, specificTreatmentIS, null);
		
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
	@PostMapping(value = "ddi-2-lunatic-json",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
		Context context = currentEnoParams.getParameters().getContext();
		Mode mode = currentEnoParams.getMode();

		nonNullContextCheck(context);
		nonNullModeCheck(mode);
		if (Mode.PAPI.equals(mode))
			throw new EnoParametersException("Mode 'PAPI' is not compatible with Lunatic format.");

		logModeAndContext(context, mode);

		ENOParameters defaultEnoParamsDDI2Lunatic = parameterService.getDefaultCustomParameters(
				context, OutFormat.LUNATIC_XML, mode);

		Pipeline defaultPipeline = defaultEnoParamsDDI2Lunatic.getPipeline();
		currentEnoParams.setPipeline(defaultPipeline);

		File enoTemp = parametrizedGenerationService.generateQuestionnaire(
				enoInput, currentEnoParams, null, specificTreatmentIS, null);
		File enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno Lunatic generation processing");
		LOGGER.info("Output file: {}", enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, headersAttachment(enoOutput))
				.body(stream);
	}

	private static String headersAttachment(File enoOutput) {
		return "attachment;filename=\"" + enoOutput.getName() + "\"";
	}

	private static void nonNullContextCheck(Context context) {
		if (context == null)
			throw new EnoParametersException("No context defined in Eno parameters file given.");
	}

	private static void nonNullModeCheck(Mode mode) {
		if (mode == null)
			throw new EnoParametersException("No mode defined in Eno parameters file given.");
	}

	private static void logModeAndContext(Context context, Mode mode) {
		LOGGER.info("Context={}, Mode={} defined in parameters file", context, mode);
	}

}
