package fr.insee.eno.ws.controller;

import java.io.File;
import java.nio.file.Files;

import fr.insee.eno.postprocessing.lunaticxml.LunaticXMLVTLParserPostprocessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.InFormat;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.Pipeline;
import fr.insee.eno.parameters.PreProcessing;
import fr.insee.eno.service.ParameterizedGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Util")
@RestController
@RequestMapping("/util")
public class UtilController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtilController.class);
	
	private ParameterizedGenerationService parameterizedGenerationService = new ParameterizedGenerationService();

	private LunaticXMLVTLParserPostprocessor parser = new LunaticXMLVTLParserPostprocessor();

	@Operation(summary = "Generation of ddi33 questionnaire from ddi32 questionnaire.", description = "It generates a ddi in 3.3 version questionnaire from a a ddi in 3.2 version questionnaire.")
	@PostMapping(value = "ddi32-2-ddi33", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateDDI33Questionnaire(
			@RequestPart(value = "in", required = true) MultipartFile in) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = new ENOParameters();
		Pipeline pipeline = new Pipeline();
		pipeline.setInFormat(InFormat.DDI);
		pipeline.setOutFormat(OutFormat.DDI);
		pipeline.getPreProcessing().add(PreProcessing.DDI_32_TO_DDI_33);
		enoParameters.setPipeline(pipeline);

		File enoOutput = parameterizedGenerationService.generateQuestionnaire(enoInput, enoParameters, null, null, null);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :" + enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath()));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + enoOutput.getName() + "\"")
				.body(stream);
	}

	@Operation(summary = "Generation of VTL formula from Xpath formula", description = "It generates a VTL in 2.0 version from a Xpath in 1.1 version.")
	@PostMapping(value = "xpath-2-vtl")
	public ResponseEntity<String> generateVTLFormula(
			@RequestParam(value = "xpath", required = true) String xpath) throws Exception {

		String vtl = parser.parseToVTL(xpath);
		LOGGER.info("Xpath parse to" + vtl);
		return ResponseEntity.ok()
				.body(vtl);
	}

}
