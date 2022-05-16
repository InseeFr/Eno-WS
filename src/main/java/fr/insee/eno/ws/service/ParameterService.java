package fr.insee.eno.ws.service;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.insee.eno.Constants;
import fr.insee.eno.exception.EnoParametersException;
import fr.insee.eno.parameters.Context;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.Mode;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.params.ValorizatorParameters;
import fr.insee.eno.params.ValorizatorParametersImpl;
import fr.insee.eno.params.validation.ValidationMessage;
import fr.insee.eno.params.validation.Validator;
import fr.insee.eno.params.validation.ValidatorImpl;

@Service
public class ParameterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParameterService.class);
	
	private ValorizatorParameters valorizatorParameters = new ValorizatorParametersImpl();
	private Validator validatorImp = new ValidatorImpl();
	
	public ENOParameters getDefaultCustomParameters(Context context, OutFormat outFormat, Mode mode) throws Exception  {
		File mergedParams = getDefaultCustomParametersFile(context, outFormat, mode);
		InputStream mergedParamsInputStream = FileUtils.openInputStream(mergedParams);
		ENOParameters finalParams = valorizatorParameters.getParameters(mergedParamsInputStream);
		mergedParamsInputStream.close();
		return finalParams;
	}
	
		
	public File getDefaultCustomParametersFile(Context context, OutFormat outFormat, Mode mode) throws Exception {
		context = context != null ? context : Context.DEFAULT;
		String parametersPath = "";

		ValidationMessage validation = validatorImp.validateMode(outFormat, mode);

		if (validation.isValid()) {

			if (mode != null && outFormat == OutFormat.LUNATIC_XML) {
				parametersPath = String.format("/params/%s/%s/%s.xml", outFormat.value().toLowerCase(),
						mode.value().toLowerCase(), context.value().toLowerCase());
			} else {
				parametersPath = String.format("/params/%s/%s.xml", outFormat.value().toLowerCase(),
						context.value().toLowerCase());
			}
			File fileParam = new File(TransformService.class.getResource(parametersPath).toURI());

			return valorizatorParameters.mergeParameters(fileParam);
		} else	{
			LOGGER.error(validation.getMessage());
			throw new EnoParametersException(validation.getMessage());
		}

	}


	
	public InputStream getDefaultParametersIS() throws Exception  {
		InputStream xmlParameters = Constants.getInputStreamFromPath(Constants.PARAMETERS_DEFAULT_XML);
		return xmlParameters;
	}
	

}
