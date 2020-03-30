package fr.insee.eno.ws.service;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import fr.insee.eno.Constants;
import fr.insee.eno.parameters.Context;
import fr.insee.eno.parameters.ENOParameters;
import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.params.ValorizatorParameters;
import fr.insee.eno.params.ValorizatorParametersImpl;

@Service
public class ParameterService {


	
	private ValorizatorParameters valorizatorParameters = new ValorizatorParametersImpl();
	
	public ENOParameters getDefaultCustomParameters(Context context, OutFormat outFormat) throws Exception  {
		File mergedParams = getDefaultCustomParametersFile(context, outFormat);
		InputStream mergedParamsInputStream = FileUtils.openInputStream(mergedParams);
		ENOParameters finalParams = valorizatorParameters.getParameters(mergedParamsInputStream);
		mergedParamsInputStream.close();
		return finalParams;
	}
	
		
	public File getDefaultCustomParametersFile(Context context, OutFormat outFormat) throws Exception  {		
		context=context!=null?context:Context.DEFAULT;
		String parametersPath = String.format("/params/%s/%s.xml", outFormat.value().toLowerCase(), context.value().toLowerCase());
		File fileParam = new File(TransformService.class.getResource(parametersPath).toURI());
		return valorizatorParameters.mergeParameters(fileParam);
	}
	
	public InputStream getDefaultParametersIS() throws Exception  {
		InputStream xmlParameters = Constants.getInputStreamFromPath(Constants.PARAMETERS_DEFAULT_XML);
		return xmlParameters;
	}
	

}
