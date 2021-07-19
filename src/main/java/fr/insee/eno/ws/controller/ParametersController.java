package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import fr.insee.eno.parameters.OutFormat;
import fr.insee.eno.parameters.Context;
import fr.insee.eno.parameters.Mode;
import fr.insee.eno.ws.service.ParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Parameters")
@RestController
@RequestMapping("/parameter")
public class ParametersController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParametersController.class);

	@Autowired
	private ParameterService parameterService;
	
	@Operation(
			summary="Get all default out format parameters.", 
			description="It returns the default parameters file without Pipeline which is overloaded. This file don't be used directly : you have to fill Pipeline.")
	@GetMapping(value="default", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<StreamingResponseBody> getDefaultParam() throws Exception {

		InputStream paramsInputStream = parameterService.getDefaultParametersIS();

		StreamingResponseBody stream = out -> out.write(IOUtils.toByteArray(paramsInputStream));

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"default-params.xml\"")
				.body(stream);
	}

	@Operation(
			summary="Get default xml parameters file for the given context according to the outFormat",
			description="It returns parameters used by default according to the studyunit and the outFormat.")
	@GetMapping(value="{context}/default", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<StreamingResponseBody> getDefaultOutParam(
			@PathVariable Context context,
			@RequestParam OutFormat outFormat,
			@RequestParam Mode mode) throws Exception {
		File fileParam;

		switch (outFormat) {
		case XFORMS:
			fileParam=parameterService.getDefaultCustomParametersFile(context, OutFormat.XFORMS,null);
			break;
		case FO:
			fileParam=parameterService.getDefaultCustomParametersFile(context, OutFormat.FO,null);
			break;
		case LUNATIC_XML:
			fileParam=parameterService.getDefaultCustomParametersFile(context, OutFormat.LUNATIC_XML,mode);
			break;
		case DDI:
			fileParam=parameterService.getDefaultCustomParametersFile(Context.DEFAULT, OutFormat.DDI,null);
			break;
		case FODT:
			fileParam=parameterService.getDefaultCustomParametersFile(context, OutFormat.FODT,null);
			break;
		default:
			fileParam = File.createTempFile("default-param-"+ context +"-"+ (outFormat==OutFormat.LUNATIC_XML?mode.value():""), ".xml");
			
			FileUtils.copyInputStreamToFile(parameterService.getDefaultParametersIS(), fileParam);
			break;
		}
		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(fileParam.toPath()));

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+context+"-"+outFormat+"-default-params.xml\"")
				.body(stream);
	}



}