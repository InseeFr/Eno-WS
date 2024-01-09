package fr.insee.eno.ws.controller;

import fr.insee.eno.service.MultiModelService;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.controller.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@Tag(name="Generation with custom mapping")
@RestController
@RequestMapping("/questionnaire")
public class GenerationWithMappingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationWithMappingController.class);

	// Eno core services
	private final ParameterizedGenerationService parametrizedGenerationService = new ParameterizedGenerationService();
	private final MultiModelService multiModelService =  new MultiModelService();

	// Weird endpoint to do weird things
	@Operation(
			summary = "Questionnaire generation according to params, metadata, specific treatment and mapping.",
			description = "Generation of one or multiple questionnaires from the input file given, " +
					"using a parameters file _(required)_, a metadata file _(optional)_, a specificTreatment file " +
					"_(optional)_ and a mapping file _(optional)_. " +
					"If the multi-model option is set to true, the output questionnaire(s) are put in a zip file."
	)
	@PostMapping(value = "in-2-out",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generate(
			//
			@RequestPart(value="in") MultipartFile in,
			@RequestPart(value="params") MultipartFile params,
			@RequestPart(value="metadata",required=false) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
			@RequestPart(value="mapping",required=false) MultipartFile mapping,
			//
			@RequestParam(value="multi-model",required=false,defaultValue="false") boolean multiModel) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params!=null ? params.getInputStream():null;
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;
		InputStream mappingIS = mapping!=null ? mapping.getInputStream():null;

		File enoOutput;
		if(multiModel) {
			enoOutput = multiModelService.generateQuestionnaire(
					enoInput, paramsIS, metadataIS, specificTreatmentIS, mappingIS);
		}
		else {
			enoOutput = parametrizedGenerationService.generateQuestionnaire(
					enoInput, paramsIS, metadataIS, specificTreatmentIS, mappingIS);
		}

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno 'in to out' processing");
		LOGGER.info("Output questionnaire file: {}", enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, HeaderUtils.headersAttachment(enoOutput))
				.body(stream);
	}

}
