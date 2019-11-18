package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

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

import fr.insee.eno.ParameterizedGenerationService;
import fr.insee.eno.parameters.AccompanyingMail;
import fr.insee.eno.parameters.BeginQuestion;
import fr.insee.eno.parameters.Capture;
import fr.insee.eno.parameters.CaptureEnum;
import fr.insee.eno.parameters.DecimalSeparator;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.EndQuestion;
import fr.insee.eno.parameters.FRParameters;
import fr.insee.eno.parameters.Format;
import fr.insee.eno.parameters.Level;
import fr.insee.eno.parameters.Orientation;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.PDFParameters;
import fr.insee.eno.parameters.Parameters;
import fr.insee.eno.parameters.StudyUnit;
import fr.insee.eno.ws.service.TransformService;
import fr.insee.eno.ws.service.ParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Generation of questionnaire")
@RestController
@RequestMapping("/generation")
public class GenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationController.class);

	private ParameterizedGenerationService generationService = new ParameterizedGenerationService();

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private TransformService transformService;


	@Operation(description="Generation questionnaire according to params, metadata and specificTreatment")
	@PostMapping(value="full-param", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generate(
			@RequestPart(value="in",required=true) MultipartFile in, 
			@RequestPart(value="params",required=true) MultipartFile params,
			@RequestPart(value="metadata",required=false) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		InputStream paramsIS = params!=null ? params.getInputStream():null;
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, paramsIS, metadataIS, specificTreatmentIS);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}


	@Operation(description="Generate fo questionnaire according to the parameters")
	@PostMapping(value="fo-param", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateFOQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@RequestParam StudyUnit studyUnit,

			@RequestParam(value="ResponseTimeQuestion") boolean EndQuestionResponseTime,
			@RequestParam(value="CommentQuestion") boolean EndQuestionCommentQuestion,

			@RequestParam(value="Format-orientation") Orientation orientation,
			@RequestParam(value="Format-column",defaultValue="1") int nbColumn,
			@RequestParam(value="AccompanyingMail") AccompanyingMail accompanyingMail,
			@RequestParam(value="PageBreakBetween") Level pageBreakBetween, 
			@RequestParam(value="Capture") CaptureEnum capture) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters =  parameterService.getDefaultCustomParameters(studyUnit,OutFormat.PDF);
		Parameters parameters = enoParameters.getParameters();
		parameters.setStudyUnit(studyUnit);
		EndQuestion endQuestion = parameters.getEndQuestion();
		endQuestion.setResponseTimeQuestion(EndQuestionResponseTime);
		endQuestion.setCommentQuestion(EndQuestionCommentQuestion);        
		PDFParameters pdfParameters = parameters.getPdfParameters();
		Format format = pdfParameters.getFormat();
		format.setOrientation(orientation);
		format.setColumns(nbColumn);
		pdfParameters.setAccompanyingMail(accompanyingMail);
		Capture capture2 = pdfParameters.getCapture();
		capture2.setNumeric(capture);
		pdfParameters.setCapture(capture2);

		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}

	@Operation(description="Generate pdf questionnaire according to the parameters")
	@PostMapping(value="pdf-param", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generatePDFQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@RequestParam StudyUnit studyUnit,

			@RequestParam(value="ResponseTimeQuestion") boolean EndQuestionResponseTime,
			@RequestParam(value="CommentQuestion") boolean EndQuestionCommentQuestion,

			@RequestParam(value="Format-orientation") Orientation orientation,
			@RequestParam(value="Format-column",defaultValue="1") int nbColumn,
			@RequestParam(value="AccompanyingMail") AccompanyingMail accompanyingMail,
			@RequestParam(value="PageBreakBetween") Level pageBreakBetween, 
			@RequestParam(value="Capture") CaptureEnum capture) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters =  parameterService.getDefaultCustomParameters(studyUnit,OutFormat.PDF);
		Parameters parameters = enoParameters.getParameters();
		parameters.setStudyUnit(studyUnit);
		EndQuestion endQuestion = parameters.getEndQuestion();
		endQuestion.setResponseTimeQuestion(EndQuestionResponseTime);
		endQuestion.setCommentQuestion(EndQuestionCommentQuestion);        
		PDFParameters pdfParameters = parameters.getPdfParameters();
		Format format = pdfParameters.getFormat();
		format.setOrientation(orientation);
		format.setColumns(nbColumn);
		pdfParameters.setAccompanyingMail(accompanyingMail);
		Capture capture2 = pdfParameters.getCapture();
		capture2.setNumeric(capture);
		pdfParameters.setCapture(capture2);

		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoTempFO = generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS);
		File enoOutput = transformService.foToPDFtransform(enoTempFO);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}

	@Operation(description="Generate xforms questionnaire according to the parameters \n For css, sperate style sheet by ','")
	@PostMapping(value="xforms-param", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<StreamingResponseBody> generateXformsQuestionnaire(

			// Files
			@RequestPart(value="in",required=true) MultipartFile in,			
			@RequestPart(value="metadata",required=false) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment,

			@RequestParam StudyUnit studyUnit,


			@RequestParam(value="IdentificationQuestion") boolean IdentificationQuestion,
			@RequestParam(value="ResponseTimeQuestion") boolean EndQuestionResponseTime,
			@RequestParam(value="CommentQuestion") boolean EndQuestionCommentQuestion,

			@RequestParam(value="NumericExample") boolean numericExample,
			@RequestParam(value="Deblocage") boolean deblocage,
			@RequestParam(value="Satisfaction") boolean satisfaction,
			@RequestParam(value="LengthOfLongTable", defaultValue="7") int lengthOfLongTable, 
			@RequestParam(value="DecimalSeparator") DecimalSeparator decimalSeparator,
			@RequestParam(value="css") String css) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters =  parameterService.getDefaultCustomParameters(studyUnit,OutFormat.FR);
		Parameters parameters = enoParameters.getParameters();
		parameters.setStudyUnit(studyUnit);
		BeginQuestion beginQuestion = parameters.getBeginQuestion();
		if(beginQuestion!=null) {beginQuestion.setIdentification(IdentificationQuestion);}
		EndQuestion endQuestion = parameters.getEndQuestion();
		if(endQuestion!=null) {
			endQuestion.setResponseTimeQuestion(EndQuestionResponseTime);
			endQuestion.setCommentQuestion(EndQuestionCommentQuestion);
		}
		FRParameters frParameters = parameters.getFrParameters();
		if(frParameters!=null) {
			frParameters.setNumericExample(numericExample);
			frParameters.setDeblocage(deblocage);
			frParameters.setSatisfaction(satisfaction);
			frParameters.setLengthOfLongTable(lengthOfLongTable);
			frParameters.setDecimalSeparator(decimalSeparator);
			frParameters.getCss().addAll(Arrays.asList(css.split(",")));		
		}
		InputStream metadataIS = metadata!=null ? metadata.getInputStream():null;
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput = generationService.generateQuestionnaire(enoInput, enoParameters, metadataIS, specificTreatmentIS);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(enoOutput.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+enoOutput.getName()+"\"")
				.body(stream);
	}




}