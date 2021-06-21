package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

import fr.insee.eno.parameters.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import fr.insee.eno.service.MultiModelService;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.service.ParameterService;
import fr.insee.eno.ws.service.TransformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Generation of questionnaire")
@RestController
@RequestMapping("/questionnaire")
public class GenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationController.class);

	private ParameterizedGenerationService parametrizedGenerationService = new ParameterizedGenerationService();
	
	private MultiModelService multiModelService =  new MultiModelService();

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private TransformService transformService;


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
			summary="Generation of fo questionnaire according to the given fo parameters.",
			description="It generates a fo questionnaire from a ddi questionnaire using the fo parameters given."
			)
	@PostMapping(value="ddi-2-fo", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFOQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
						
			@RequestParam(value="multi-model",required=false,defaultValue="false") boolean multiModel,
			
			@RequestParam Context context,

			@RequestParam(value="ResponseTimeQuestion") boolean EndQuestionResponseTime,
			@RequestParam(value="CommentQuestion") boolean EndQuestionCommentQuestion,

			
			@RequestParam(value="Format-orientation") Orientation orientation,
			@RequestParam(value="Format-column",defaultValue="1") int nbColumn,
			@RequestParam(value="AccompanyingMail") AccompanyingMail accompanyingMail,
			@RequestParam(value="PageBreakBetween") Level pageBreakBetween, 
			@RequestParam(value="Capture") CaptureEnum capture,
			@RequestParam(value="QuestNum") BrowsingEnum questNum,
			@RequestParam(value="SeqNum") boolean seqNum,
			@RequestParam(value="PreQuestSymbol") boolean preQuestSymbol
			) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters =  parameterService.getDefaultCustomParameters(context,OutFormat.FO);
		
		Parameters parameters = enoParameters.getParameters();
		
		parameters.setContext(context);
		
		GlobalNumbering numerotation = parameters.getNumerotation();
		numerotation.setQuestNum(questNum);
		numerotation.setSeqNum(seqNum);
		numerotation.setPreQuestSymbol(preQuestSymbol);
		parameters.setNumerotation(numerotation);
		
		EndQuestion endQuestion = parameters.getEndQuestion();
			endQuestion.setResponseTimeQuestion(EndQuestionResponseTime);
			endQuestion.setCommentQuestion(EndQuestionCommentQuestion);
		
		FOParameters foParameters = parameters.getFoParameters();
		
		Format format = foParameters.getFormat();
		format.setOrientation(orientation);
		format.setColumns(nbColumn);
		

		
		foParameters.setAccompanyingMail(accompanyingMail);
		
		PageBreakBetween pageBreakbetweenFo = foParameters.getPageBreakBetween();
		pageBreakbetweenFo.setPdf(pageBreakBetween);
		foParameters.setPageBreakBetween(pageBreakbetweenFo);
		
		Capture capture2 = foParameters.getCapture();
		capture2.setNumeric(capture);
		foParameters.setCapture(capture2);
		
	    
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		
		File enoOutput;
		if(multiModel) {
			enoOutput = multiModelService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);
		}
		else {
			enoOutput = parametrizedGenerationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);
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
			summary="Generation of xforms questionnaire according to the given xforms parameters, metadata and specificTreatment.",
			description="It generates a xforms questionnaire from a ddi questionnaire using the xforms parameters given. For css parameters, sperate style sheet by ','"
			)
	@PostMapping(value="ddi-2-xforms", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXformsQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,			
			@RequestPart(value="metadata",required=false) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,
								
			@RequestParam(value="multi-model",required=false,defaultValue="false") boolean multiModel,

			@RequestParam Context context,


			@RequestParam(value="IdentificationQuestion") boolean IdentificationQuestion,
			@RequestParam(value="ResponseTimeQuestion") boolean EndQuestionResponseTime,
			@RequestParam(value="CommentQuestion") boolean EndQuestionCommentQuestion,

			@RequestParam(value="NumericExample") boolean numericExample,
			@RequestParam(value="Deblocage", defaultValue="false") boolean deblocage,
			@RequestParam(value="Satisfaction", defaultValue="false") boolean satisfaction,
			@RequestParam(value="LengthOfLongTable", defaultValue="7") int lengthOfLongTable, 
			@RequestParam(value="DecimalSeparator") DecimalSeparator decimalSeparator,
			@RequestParam(value="css", required=false) String css,
			@RequestParam(value="QuestNum") BrowsingEnum questNum,
			@RequestParam(value="SeqNum") boolean seqNum,
			@RequestParam(value="PreQuestSymbol") boolean preQuestSymbol
			) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters =  parameterService.getDefaultCustomParameters(context,OutFormat.XFORMS);

		Parameters parameters = enoParameters.getParameters();
		parameters.setContext(context);
		
		GlobalNumbering numerotation = parameters.getNumerotation();
		numerotation.setQuestNum(questNum);
		numerotation.setSeqNum(seqNum);
		numerotation.setPreQuestSymbol(preQuestSymbol);
		parameters.setNumerotation(numerotation);
		
		BeginQuestion beginQuestion = parameters.getBeginQuestion();
		if(beginQuestion!=null) {beginQuestion.setIdentification(IdentificationQuestion);}
		EndQuestion endQuestion = parameters.getEndQuestion();
		if(endQuestion!=null) {
			endQuestion.setResponseTimeQuestion(EndQuestionResponseTime);
			endQuestion.setCommentQuestion(EndQuestionCommentQuestion);
		}
		XFORMSParameters xformsParameters = parameters.getXformsParameters();
		if(xformsParameters!=null) {
			xformsParameters.setNumericExample(numericExample);
			xformsParameters.setDeblocage(deblocage);
			xformsParameters.setSatisfaction(satisfaction);
			xformsParameters.setLengthOfLongTable(lengthOfLongTable);
			xformsParameters.setDecimalSeparator(decimalSeparator);
			if(css!=null) {
			xformsParameters.getCss().addAll(Arrays.asList(css.split(",")));	}	
		}
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		
		File enoOutput;
		if(multiModel) {
			enoOutput = multiModelService.generateQuestionnaire(enoInput, enoParameters, metadataIS, specificTreatmentIS, null);
		}
		else {
			enoOutput = parametrizedGenerationService.generateQuestionnaire(enoInput, enoParameters, metadataIS, specificTreatmentIS, null);
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
			summary="Generation of lunatic-json questionnaire according to the given js parameters and specificTreatment.",
			description="It generates a lunatic-json (flat) questionnaire from a ddi questionnaire using the js parameters given."
			)
	@PostMapping(value="ddi-2-lunatic-json", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateJSQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@RequestParam Context context,

			@RequestParam(value="IdentificationQuestion", required=false, defaultValue = "false") boolean IdentificationQuestion,
			@RequestParam(value="ResponseTimeQuestion", required=false, defaultValue = "false") boolean EndQuestionResponseTime,
			@RequestParam(value="CommentQuestion", required=false, defaultValue = "false") boolean EndQuestionCommentQuestion,
			@RequestParam(value="parsingXpathVTL",required=false, defaultValue="true")  boolean parsingXpathVTL,
			@RequestParam(value="filterDescription", defaultValue="false") boolean filterDescription,
			@RequestParam(value="QuestNum") BrowsingEnum questNum,
			@RequestParam(value="SeqNum") boolean seqNum,
			@RequestParam(value="PreQuestSymbol") boolean preQuestSymbol,
			@RequestParam(value="Pagination",required = false, defaultValue = "NONE" ) Pagination pagination
			) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(Context.DEFAULT,OutFormat.LUNATIC_XML);
		
		//If input files contains VTL language control --> it's not necessary to parse xpath into vtl (post-processing)
		if(!parsingXpathVTL) {
		Pipeline pipeline = enoParameters.getPipeline();
		pipeline.getPostProcessing().remove(PostProcessing.LUNATIC_XML_VTL_PARSER);
		enoParameters.setPipeline(pipeline);
		}
	
		Parameters parameters = enoParameters.getParameters();
		parameters.setContext(context);
		
		GlobalNumbering numerotation = parameters.getNumerotation();
		numerotation.setQuestNum(questNum);
		numerotation.setSeqNum(seqNum);
		numerotation.setPreQuestSymbol(preQuestSymbol);
		parameters.setNumerotation(numerotation);
		
		BeginQuestion beginQuestion = parameters.getBeginQuestion();
		if(beginQuestion!=null) {beginQuestion.setIdentification(IdentificationQuestion);}
		EndQuestion endQuestion = parameters.getEndQuestion();
		if(endQuestion!=null) {
			endQuestion.setResponseTimeQuestion(EndQuestionResponseTime);
			endQuestion.setCommentQuestion(EndQuestionCommentQuestion);
		}
		LunaticXMLParameters lunaticXMLParameters = parameters.getLunaticXmlParameters();
		if(lunaticXMLParameters!=null) {
			lunaticXMLParameters.setFilterDescription(filterDescription);
			lunaticXMLParameters.setPagination(pagination);
		}
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoTemp = parametrizedGenerationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);
		File enoOutput = transformService.XMLLunaticToJSONLunaticFlat(enoTemp);

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

		FileUtils.forceDelete(enoInput);
		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}
	
	
	


	@Operation(
			summary="Generation of the specifications of the questionnaire according .",
			description="It generates a \".fodt\" questionnaire from a ddi questionnaire."
			)
	@PostMapping(value="ddi-2-fodt", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateODTQuestionnaire(
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestParam(value="QuestNum") BrowsingEnum questNum,
			@RequestParam(value="SeqNum") boolean seqNum,
			@RequestParam(value="PreQuestSymbol") boolean preQuestSymbol
			) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);
		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(Context.DEFAULT,OutFormat.FODT);
		
		Parameters parameters = enoParameters.getParameters();
		
		GlobalNumbering numerotation = parameters.getNumerotation();
		numerotation.setQuestNum(questNum);
		numerotation.setSeqNum(seqNum);
		numerotation.setPreQuestSymbol(preQuestSymbol);
		parameters.setNumerotation(numerotation);
		
		File enoOutput = parametrizedGenerationService.generateQuestionnaire(enoInput,enoParameters, null, null, null);

		FileUtils.forceDelete(enoInput);
		
		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.ACCEPT, "*")
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}


}