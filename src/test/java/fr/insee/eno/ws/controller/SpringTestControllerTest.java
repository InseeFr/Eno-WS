package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;



@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class SpringTestControllerTest {
	
	@LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    public static Resource getUserFileResource(String fileName) throws IOException {
        //todo replace tempFile with a real file
        Path tempFile = Files.createTempFile(fileName, ".txt");
        Files.write(tempFile, "some test content...\nline1\nline2".getBytes());
        System.out.println("uploading: " + tempFile);
        File file = tempFile.toFile();
        //to upload in-memory bytes use ByteArrayResource instead
        return new FileSystemResource(file);
    }

    @Test
    public void greetingShouldReturnDefaultMessage() throws Exception {
        String message = this.restTemplate.getForObject("http://localhost:" + port + "/healthcheck",String.class);
        Assert.assertEquals("i'm alive", message);
    }
    
    @Test
    public void testSendFile() throws Exception {
    	
    	MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("in", getUserFileResource("in"));
        bodyMap.add("params", getUserFileResource("parameters"));
        bodyMap.add("metadata", getUserFileResource("metadata"));
        bodyMap.add("specificTreatment", getUserFileResource("specificTreatment"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/questionnaire/generate",
                HttpMethod.POST, requestEntity, String.class);
        System.out.println("response status: " + response.getStatusCode());
        System.out.println("response body: " + response.getBody());
    }

}
