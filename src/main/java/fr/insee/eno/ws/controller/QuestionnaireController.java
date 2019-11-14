package fr.insee.eno.ws.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.eno.ParameterizedGenerationService;
import fr.insee.eno.parameters.Capture;
import fr.insee.eno.parameters.CaptureEnum;
import fr.insee.eno.parameters.DecimalSeparator;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.InFormat;
import fr.insee.eno.parameters.Orientation;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.StudyUnit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Generation of questionnaire")
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionnaireController.class);
	
	private ParameterizedGenerationService generationService = new ParameterizedGenerationService();
	
	
	@Operation(description="Generate questionnaire according to the parameters")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "I'm alive")
    })
    @PostMapping(value="generate", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
   	public ResponseEntity<?> generate(
			@RequestPart(value="in",required=true) MultipartFile in, 
			@RequestPart(value="params",required=true) MultipartFile params,
			@RequestPart(value="metadata",required=false) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) {
		
		String inFileName = in.getOriginalFilename();
		String paramsFileName = params.getOriginalFilename();
		
   		return new ResponseEntity<>("FIchier in :"+inFileName+" - Fichier params :"+paramsFileName,HttpStatus.OK);
   	}
	
	@Operation(description="Generate questionnaire according to the parameters")
	@ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "I'm alive")
    })
    @PostMapping(value="generateEnoJson", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes= {MediaType.MULTIPART_FORM_DATA_VALUE,MediaType.APPLICATION_JSON_VALUE})
   	public ResponseEntity<?> generateEnoParams(
   			// Params
   			@RequestParam InFormat inFormat,
   			@RequestParam OutFormat outFormat,
   			@RequestParam StudyUnit studyUnit,
   			
   			@RequestParam(value="IdentificationQuestion") boolean beginQuestionIdentification,
   			@RequestParam(value="ResponseTimeQuestion") boolean EndQuestionResponseTime,
   			@RequestParam(value="CommentQuestion") boolean EndQuestionCommentQuestion,
   			
   			@RequestParam(value="NumericExample") boolean numericExample,
   			@RequestParam(value="DecimalSperator") DecimalSeparator decimalSeparator,
   			@RequestParam(value="Campagne",defaultValue="campagne-test") String campagne,
   			
   			@RequestParam(value="Orientation") Orientation orientation,
   			@RequestParam(value="Capture") CaptureEnum capture,
   			
   			// Files
			@RequestPart(value="in",required=true) MultipartFile in,			
			@RequestPart(value="metadata",required=false) MultipartFile metadata,
			@RequestPart(value="specificTreatment",required=false) MultipartFile specificTreatment) {
		
		String inFileName = in.getOriginalFilename();
		
		ENOParameters enoParameters = new ENOParameters();
		
   		return new ResponseEntity<>("FIchier in :"+inFileName+" - InFormat params :"+inFormat,HttpStatus.OK);
   	}
}