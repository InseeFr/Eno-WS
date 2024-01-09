package fr.insee.eno.ws.controller;

import fr.insee.eno.parameters.*;
import fr.insee.eno.postprocessing.lunaticxml.LunaticXMLVTLParserPostprocessor;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.service.TransformService;
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
import java.nio.file.Files;

@Tag(name = "Utils")
@RestController
@RequestMapping("/utils")
public class UtilsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtilsController.class);

	// Eno-WS service
	private final TransformService transformService;

	// Eno core services
	private final ParameterizedGenerationService parameterizedGenerationService;
	private final LunaticXMLVTLParserPostprocessor parser;

    public UtilsController(TransformService transformService) {
        this.transformService = transformService;
		this.parameterizedGenerationService = new ParameterizedGenerationService();
		this.parser = new LunaticXMLVTLParserPostprocessor();
    }

	/**
	 * Converts the given DDI in version 3.2 in a DDI in version 3.3.
	 * @param in A DDI 3.2 file.
	 * @return A DDI 3.3 file.
	 * @throws Exception if conversion fails.
	 * @deprecated DDI 3.2 is deprecated.
	 */
    @Operation(summary = "Generation of ddi33 questionnaire from ddi32 questionnaire.",
			description = "It generates a ddi in 3.3 version questionnaire from a a ddi in 3.2 version questionnaire.")
	@PostMapping(value = "ddi32-2-ddi33",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Deprecated(since = "2.0.0")
	public ResponseEntity<StreamingResponseBody> generateDDI33Questionnaire(
			@RequestPart(value = "in") MultipartFile in) throws Exception {

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
		LOGGER.info("OutPut File: {}", enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath()));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + enoOutput.getName() + "\"")
				.body(stream);
	}

	/**
	 * Converts a Xpath 1.1 expression into a VTL 2.0 expression
	 * @param xpath A string XPath 1.1 expression
	 * @return A string VTL 2.0 expression
	 * @deprecated This endpoint and its logic class has been copied/pasted in the Eno Java web-service.
	 */
	@Operation(summary = "Generation of VTL formula from Xpath formula",
			description = "It generates a VTL in 2.0 version from a Xpath in 1.1 version.")
	@PostMapping(value = "xpath-2-vtl")
	@Deprecated(since = "2.0.0")
	public ResponseEntity<String> generateVTLFormula(
			@RequestParam(value = "xpath") String xpath) {

		String vtl = parser.parseToVTL(xpath);
		LOGGER.info("Xpath expression parsed to VTL: {}", vtl);
		return ResponseEntity.ok()
				.body(vtl);
	}

	/**
	 * Converts the given Lunatic XML questionnaire file into a Lunatic JSON questionnaire file.
	 * @param in A Lunatic XML questionnaire file.
	 * @return A Lunatic JSON questionnaire file.
	 * @throws Exception if conversion fails.
	 * @deprecated The Lunatic XML format is deprecated.
	 */
	@Operation(summary = "Generation of Lunatic Json from Lunatic XML",
			description = "It generates a Lunatic Json from a Lunatic XML, using the Lunatic-Model library.")
	@PostMapping(value = "lunatic-model/xml-2-json",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Deprecated(since = "2.0.0")
	public ResponseEntity<StreamingResponseBody> generateLunaticJson(
			@RequestPart(value = "in") MultipartFile in) throws Exception {
		
		LOGGER.info("START of Lunatic XML -> Lunatic Json transforming");
		File lunaticXMLInput = File.createTempFile("lunatic", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), lunaticXMLInput);

		File lunaticJsonOutput = transformService.XMLLunaticToJSONLunaticFlat(lunaticXMLInput);

		FileUtils.forceDelete(lunaticXMLInput);
		
		LOGGER.info("END of Lunatic XML -> Lunatic Json transforming");
		LOGGER.info("OutPut File: {}", lunaticJsonOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(lunaticJsonOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+lunaticJsonOutput.getName()+"\"")
				.body(stream);
	}

}
