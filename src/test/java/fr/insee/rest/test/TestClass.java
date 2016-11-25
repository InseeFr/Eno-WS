package fr.insee.rest.test;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import fr.insee.utils.Constants;

/**
 * Test Class used to test the REST Web Service
 * 
 * @author gerose
 *
 */
public class TestClass {

	final static Logger logger = Logger.getLogger(TestClass.class);

	/**
	 * Setting up the RestAssured default URI
	 */
	@Before
	public void setUp() {
		logger.debug("Setting the RestAssured base Uri to http://localhost:8080 : for local tests");
		RestAssured.baseURI = "http://localhost:8080";
		// RestAssured.basePath="http://localhost:8080";
	}

	/**
	 * Dummy helloworld test, should return "Hello world"
	 */
	@Test
	public void helloworldTest() {
		logger.debug(
				"Dummy helloworld test : trying to reach /REST_Questionnaire_Generator/Main/Service/helloworld with Status = 200");
		RestAssured.expect().statusCode(200).contentType(ContentType.TEXT).when()
				.get("/REST_Questionnaire_Generator/Main/Service/helloworld");

	}

	/**
	 * Testing the main method with a static input file
	 */
	@Test
	public void simpsonsTest() {
		logger.debug(
				"Main test of the questionnaire generation : calling /REST_Questionnaire_Generator/Main/Service/Generation with the static input file : ");
		logger.debug(Constants.TEST_INPUT_XML);

		
		URL url = Thread.currentThread().getContextClassLoader().getResource(Constants.TEST_INPUT_XML);
		File file = new File(url.getPath());
		
		
		RestAssured.given().multiPart(file).expect().statusCode(200).when()
				.post("/REST_Questionnaire_Generator/Main/Service/Generation");
	}

}
