package fr.insee.eno.ws.controller;

import fr.insee.eno.Constants;
import fr.insee.eno.parameters.*;
import fr.insee.eno.service.ParameterizedGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import java.nio.file.Files;

@Tag(name="Generation from Pogues")
@RestController
@RequestMapping("/questionnaire")
public class GenerationPoguesController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationPoguesController.class);

	// Eno core service
	private final ParameterizedGenerationService parametrizedGenerationService = new ParameterizedGenerationService();

	@Operation(
			summary = "Generation DDI from Pogues XML questionnaire.",
			description = "Generation of a DDI from the given Pogues XML questionnaire."
	)
	@PostMapping(value="poguesxml-2-ddi", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateDDIQuestionnaire(
			@RequestPart(value="in") MultipartFile in) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);
		ENOParameters enoParameters = new ENOParameters();
		Pipeline pipeline = new Pipeline();
		pipeline.setInFormat(InFormat.POGUES_XML);
		pipeline.setOutFormat(OutFormat.DDI);
		pipeline.getPreProcessing().add(PreProcessing.POGUES_XML_INSERT_FILTER_LOOP_INTO_QUESTION_TREE);
		pipeline.getPreProcessing().add(PreProcessing.POGUES_XML_GOTO_2_ITE);

		enoParameters.setPipeline(pipeline);
		
		File enoOutput = parametrizedGenerationService.generateQuestionnaire(
				enoInput, enoParameters, null, null, null);

		// Fix: deleting temp files created in PoguesXMLPreprocessorGoToTreatment and PoguesXmlInsertFilterLoopIntoQuestionTree
		String tempSup = FilenameUtils.removeExtension(enoInput.getAbsolutePath()) + Constants.TEMP_EXTENSION;
		String tempTempSup = FilenameUtils.removeExtension(new File(tempSup).getAbsolutePath()) + Constants.TEMP_EXTENSION;
		
		FileUtils.forceDelete(new File(tempSup));
		FileUtils.forceDelete(new File(tempTempSup));
		
		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno DDI generation processing");
		LOGGER.info("Output DDI file: {}", enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}

}
