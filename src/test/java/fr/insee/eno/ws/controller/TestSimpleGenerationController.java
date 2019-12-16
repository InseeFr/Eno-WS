package fr.insee.eno.ws.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.insee.eno.parameters.StudyUnit;
import fr.insee.eno.ws.EnoWS;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=EnoWS.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:enows.properties")
public class TestSimpleGenerationController {
	
	private static String TEMP_FOLDER_PATH = System.getProperty("java.io.tmpdir")+"/"+"enows-test";
	
	@BeforeClass
	public static void cleaningFiles() throws IOException {
		File directory = new File(TEMP_FOLDER_PATH);
		if(directory.exists()) {
			FileUtils.deleteDirectory(directory);
		}
	}
	
	
	@Test
	public void testXforms() throws ClientProtocolException, IOException, URISyntaxException {
		String basePath = "src/test/resources/transforms";
		File in = new File(String.format("%s/ddi.xml", basePath));
		
		CloseableHttpClient httpclient = HttpClients.createDefault();		
		HttpPost post = new HttpPost("http://localhost:8080/questionnaire/"+StudyUnit.DEFAULT+"/xforms");;
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();         
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addBinaryBody("in", in, ContentType.DEFAULT_BINARY, "ddi.xml");
		HttpEntity entity = builder.build();
		post.setEntity(entity);
		HttpResponse response = httpclient.execute(post);
		Header content=response.getFirstHeader("Content-Disposition");
		
		Matcher matcher = Pattern.compile("filename=\"(.*)\"").matcher(content.getValue());
		String fileName = null;
		while(matcher.find()){
			fileName = matcher.group(1);
		}
		HttpEntity entityResponse = response.getEntity();
		File fileOutput = new File(TEMP_FOLDER_PATH+"/"+fileName);
		FileUtils.write(fileOutput, EntityUtils.toString(entityResponse, "UTF-8"),StandardCharsets.UTF_8);
		System.out.println("Path to generated file : "+fileOutput.getAbsolutePath());
	}
	
	
	
	@Test
	public void testJsonLunatic() throws ClientProtocolException, IOException, URISyntaxException {
		String basePath = "src/test/resources/transforms";
		File in = new File(String.format("%s/ddi.xml", basePath));
		
		CloseableHttpClient httpclient = HttpClients.createDefault();		
		HttpPost post = new HttpPost("http://localhost:8080/questionnaire/"+StudyUnit.DEFAULT+"/json-lunatic");;
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();         
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addBinaryBody("in", in, ContentType.DEFAULT_BINARY, "ddi.xml");
		HttpEntity entity = builder.build();
		post.setEntity(entity);
		HttpResponse response = httpclient.execute(post);
		Header content=response.getFirstHeader("Content-Disposition");
		
		Matcher matcher = Pattern.compile("filename=\"(.*)\"").matcher(content.getValue());
		String fileName = null;
		while(matcher.find()){
			fileName = matcher.group(1);
		}
		HttpEntity entityResponse = response.getEntity();
		File fileOutput = new File(TEMP_FOLDER_PATH+"/"+fileName);
		FileUtils.write(fileOutput, EntityUtils.toString(entityResponse, "UTF-8"),StandardCharsets.UTF_8);
		System.out.println("Path to generated file : "+fileOutput.getAbsolutePath());
	}

}
