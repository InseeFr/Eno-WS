package fr.insee.eno.ws.service;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.eno.parameters.Context;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.Mode;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.service.ParameterizedGenerationService;
import fr.insee.eno.ws.controller.GenerationController;

@Service
public class QuestionnaireGenerateService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerationController.class);
	
	private ParameterizedGenerationService generationService = new ParameterizedGenerationService();

	@Autowired
	private ParameterService parameterService;
	


	
	public File generateQuestionnaireFile(Context context, OutFormat outFormat,Mode mode, MultipartFile in, MultipartFile specificTreatment)throws Exception {

	    File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context, outFormat, mode);
		
		InputStream specificTreatmentIS = specificTreatment!=null ? specificTreatment.getInputStream():null;

		File enoOutput= generationService.generateQuestionnaire(enoInput, enoParameters, null, specificTreatmentIS, null);
	
		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of eno processing");
		LOGGER.info("OutPut File :"+enoOutput.getName());
		
		return enoOutput;
	}
}
