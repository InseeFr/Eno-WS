package fr.insee.eno.ws.controller;

import java.io.File;
import java.nio.file.Files;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class ResponseUtil {
	
	public static ResponseEntity<StreamingResponseBody> generateResponseFromFile(File file)throws Exception {

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(file.toPath())) ;

		return  ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+file.getName()+"\"")
				.body(stream);
	}
}
