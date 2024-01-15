package fr.insee.eno.ws.service;

import fr.insee.eno.parameters.Context;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.Mode;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.service.MultiModelService;
import fr.insee.eno.service.ParameterizedGenerationService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

@Service
public class QuestionnaireGenerateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionnaireGenerateService.class);

	// Eno core service
	private final ParameterizedGenerationService generationService = new ParameterizedGenerationService();
	private final MultiModelService multiModelService = new MultiModelService();

	// Eno-WS service
	private final ParameterService parameterService;

	public QuestionnaireGenerateService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	public File generateQuestionnaireFile(Context context, OutFormat outFormat, Mode mode,
										  MultipartFile in,
										  MultipartFile metadata,
										  MultipartFile specificTreatment) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context, outFormat, mode);

		InputStream metadataIS = metadata != null ? metadata.getInputStream() : null;
		InputStream specificTreatmentIS = specificTreatment != null ? specificTreatment.getInputStream() : null;

		File enoOutput= generationService.generateQuestionnaire(
				enoInput, enoParameters, metadataIS, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno questionnaire processing");
		LOGGER.info("Output file: {}", enoOutput.getName());

		return enoOutput;
	}

	public File generateMultiModelQuestionnaires(Context context, OutFormat outFormat, Mode mode,
												 MultipartFile in,
												 MultipartFile metadata,
												 MultipartFile specificTreatment) throws Exception {

		File enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(in.getInputStream(), enoInput);

		ENOParameters enoParameters = parameterService.getDefaultCustomParameters(context, outFormat, mode);

		InputStream metadataIS = metadata != null ? metadata.getInputStream() : null;
		InputStream specificTreatmentIS = specificTreatment != null ? specificTreatment.getInputStream() : null;

		File enoOutput= multiModelService.generateQuestionnaire(
				enoInput, enoParameters, metadataIS, specificTreatmentIS, null);

		FileUtils.forceDelete(enoInput);

		LOGGER.info("END of Eno multi-model questionnaires processing");
		LOGGER.info("Output file: {}", enoOutput.getName());

		return enoOutput;
	}

}
