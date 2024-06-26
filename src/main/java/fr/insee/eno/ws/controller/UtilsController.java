package fr.insee.eno.ws.controller;

import fr.insee.eno.parameters.*;
import fr.insee.eno.postprocessing.lunaticxml.LunaticXMLVTLParserPostprocessor;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.controller.utils.ResponseUtils;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;

@Tag(name = "Utils")
@RestController
@RequestMapping("/utils")
@Slf4j
public class UtilsController {


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
			description = "**The version 3.2 of DDI format is now deprecated.** " +
					"It generates a ddi in 3.3 version questionnaire from a a ddi in 3.2 version questionnaire.")
	@PostMapping(value = "ddi32-2-ddi33",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Deprecated(since = "2.0.0")
	public ResponseEntity<StreamingResponseBody> convertDDI32ToDDI33(
			@RequestPart(value = "in") MultipartFile in) throws Exception {


		ENOParameters enoParameters = new ENOParameters();
		Pipeline pipeline = new Pipeline();
		pipeline.setInFormat(InFormat.DDI);
		pipeline.setOutFormat(OutFormat.DDI);
		pipeline.getPreProcessing().add(PreProcessing.DDI_32_TO_DDI_33);
		enoParameters.setPipeline(pipeline);

		ByteArrayOutputStream enoOutput = parameterizedGenerationService.generateQuestionnaire(in.getInputStream(), enoParameters, null, null, null);

		log.info("END of eno processing");
		return ResponseUtils.generateResponseFromOutputStream(enoOutput, "ddi33.xml");
	}

	/**
	 * Converts a Xpath 1.1 expression into a VTL 2.0 expression
	 * @param xpath A string XPath 1.1 expression
	 * @return A string VTL 2.0 expression
	 * @deprecated This endpoint and its logic class has been copied/pasted in the Eno Java web-service.
	 */
	@Operation(summary = "Generation of VTL formula from Xpath formula",
			description = "**This endpoint has been migrated in the Eno 'Java' web-service.** " +
					"It generates a VTL in 2.0 version from a Xpath in 1.1 version.")
	@PostMapping(value = "xpath-2-vtl")
	@Deprecated(since = "2.0.0")
	public ResponseEntity<String> convertXpathToVTL(
			@RequestParam(value = "xpath") String xpath) {

		String vtl = parser.parseToVTL(xpath);
		log.info("Xpath expression parsed to VTL: {}", vtl);
		return ResponseEntity.ok().body(vtl);
	}

	/**
	 * Converts the given Lunatic XML questionnaire file into a Lunatic JSON questionnaire file.
	 * @param in A Lunatic XML questionnaire file.
	 * @return A Lunatic JSON questionnaire file.
	 * @throws Exception if conversion fails.
	 * @deprecated The Lunatic XML format is deprecated.
	 */
	@Operation(summary = "Generation of Lunatic Json from Lunatic XML",
			description = "**The Lunatic XML format is now deprecated.** " +
					"It generates a Lunatic Json from a Lunatic XML, using the Lunatic-Model library.")
	@PostMapping(value = "lunatic-model/xml-2-json",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Deprecated(since = "2.0.0")
	public ResponseEntity<StreamingResponseBody> convertLunaticXmlToJson(
			@RequestPart(value = "in") MultipartFile in) throws Exception {
		
		log.info("START of Lunatic XML -> Lunatic Json transforming");

		ByteArrayOutputStream lunaticJsonOutput = transformService.XMLLunaticToJSONLunaticFlat(in.getInputStream());
		
		log.info("END of Lunatic XML -> Lunatic Json transforming");

		return ResponseUtils.generateResponseFromOutputStream(lunaticJsonOutput, "questionnaire.json");
	}

}
