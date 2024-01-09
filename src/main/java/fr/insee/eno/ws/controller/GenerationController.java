package fr.insee.eno.ws.controller;

import fr.insee.eno.Constants;
import fr.insee.eno.parameters.*;
import fr.insee.eno.service.MultiModelService;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.service.ParameterService;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Tag(name="Generation of questionnaire")
@RestController
@RequestMapping("/questionnaire")
public class GenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationController.class);

	private final ParameterizedGenerationService parametrizedGenerationService = new ParameterizedGenerationService();
	
	private final MultiModelService multiModelService =  new MultiModelService();


	@Operation(
			summary="Generation of questionnaire according to params, metadata and specificTreatment.",
			description="It generates a questionnaire : using the parameters file (required), metadata file (optional) and the specificTreatment file (optional). To use it, you have to upload all necessary files."
			)
	@PostMapping(value="in-2-out", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generate(
			@RequestPart(value="in",required=true) MultipartFile in, 
			@RequestPart(value="params",required=true) MultipartFile params,
			@RequestPart(value="metadata",required=false) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
			@RequestPart(value="mapping",required=false) MultipartFile mapping,
			
			@RequestParam(value="multi-model",required=false,defaultValue="false") boolean multiModel) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params!=null ? params.getInputStream():null;
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;
		InputStream mappingIS = mapping!=null ? mapping.getInputStream():null;

		File enoOutput;
		if(multiModel) {
			enoOutput = multiModelService.generateQuestionnaire(enoInput, paramsIS, metadataIS, specificTreatmentIS, mappingIS);
		}
		else {
			enoOutput = parametrizedGenerationService.generateQuestionnaire(enoInput, paramsIS, metadataIS, specificTreatmentIS, mappingIS);
		}
		
		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	@Operation(
			summary="Generation of ddi questionnaire from pogues-xml questionnaire.",
			description="It generates a ddi questionnaire from a pogues-xml questionnaire. You can choose if the tranformation uses markdown to xhtml post processor."
			)
	@PostMapping(value="poguesxml-2-ddi", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateDDIQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);
		ENOParameters enoParameters = new ENOParameters();
		Pipeline pipeline = new Pipeline();
		pipeline.setInFormat(InFormat.POGUES_XML);
		pipeline.setOutFormat(OutFormat.DDI);
		pipeline.getPreProcessing().add(PreProcessing.POGUES_XML_INSERT_FILTER_LOOP_INTO_QUESTION_TREE);
		pipeline.getPreProcessing().add(PreProcessing.POGUES_XML_GOTO_2_ITE);

		enoParameters.setPipeline(pipeline);
		
		File enoOutput = parametrizedGenerationService.generateQuestionnaire(enoInput, enoParameters, null, null, null);

		// Fix : deleting temp files created in PoguesXMLPreprocessorGoToTreatment and PoguesXmlInsertFilterLoopIntoQuestionTree
		String tempSup = FilenameUtils.removeExtension(enoInput.getAbsolutePath()) + Constants.TEMP_EXTENSION;
		String temptempSup = FilenameUtils.removeExtension(new File(tempSup).getAbsolutePath()) + Constants.TEMP_EXTENSION;
		
		FileUtils.forceDelete(new File(tempSup));
		FileUtils.forceDelete(new File(temptempSup));	
		
		FileUtils.forceDelete(enoInput);

		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}


}