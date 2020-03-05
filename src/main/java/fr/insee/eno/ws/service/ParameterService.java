package fr.insee.eno.ws.service;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.insee.eno.Constants;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.StudyUnit;
import fr.insee.eno.params.ValorizatorParameters;
import fr.insee.eno.params.ValorizatorParametersImpl;

@Service
public class ParameterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParameterService.class);
	
	private ValorizatorParameters valorizatorParameters = new ValorizatorParametersImpl();
	
	public ENOParameters getDefaultCustomParameters(StudyUnit studyUnit, OutFormat outFormat) throws Exception  {
		studyUnit=studyUnit!=null?studyUnit:StudyUnit.DEFAULT;
		String parametersPath = String.format("/params/%s/%s.xml", outFormat.value().toLowerCase(), studyUnit.value().toLowerCase());
		InputStream xmlParameters = getInputStreamFromPath(parametersPath);
		return valorizatorParameters.getParameters(xmlParameters);
	}
	
		
	public File getDefaultCustomParametersFile(StudyUnit studyUnit, OutFormat outFormat) throws Exception  {		
		studyUnit=studyUnit!=null?studyUnit:StudyUnit.DEFAULT;
		String parametersPath = String.format("/params/%s/%s.xml", outFormat.value().toLowerCase(), studyUnit.value().toLowerCase());
		File fileParam = new File(TransformService.class.getResource(parametersPath).toURI());
		return valorizatorParameters.mergeParameters(fileParam);
	}
	
	public InputStream getDefaultParametersIS() throws Exception  {
		InputStream xmlParameters = Constants.getInputStreamFromPath(Constants.PARAMETERS_DEFAULT_XML);
		return xmlParameters;
	}
	
	public static InputStream getInputStreamFromPath(String path) {
		LOGGER.debug("Loading " + path);
		try {
			return ParameterService.class.getResourceAsStream(path);
		} catch (Exception e) {
			LOGGER.error("Error when loading file");
			return null;
		}
	}



}
