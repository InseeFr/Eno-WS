package fr.insee.eno.ws.controller.utils;

import java.io.File;
import java.nio.file.Files;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class ResponseUtils {

	private ResponseUtils() {}
	
	public static ResponseEntity<StreamingResponseBody> generateResponseFromFile(File file) {

		StreamingResponseBody stream = out -> out.write(Files.readAllBytes(file.toPath()));

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+file.getName()+"\"")
				.body(stream);
	}

}
