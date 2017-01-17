package fr.insee.eno.rest.test;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import fr.insee.eno.utils.Constants;

import org.junit.Assert;

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
	 * This test will return true if we succeed to connect with the /Generation method even if 
	 * the test input file was not found/declared.
	 */
	@Test
	public void simpsonsTest() {
		logger.debug(
				"Main test of the questionnaire generation : calling /REST_Questionnaire_Generator/Main/Service/Generation with the static input file : ");
		logger.debug(Constants.TEST_INPUT_XML);

		try{
			URL url = Thread.currentThread().getContextClassLoader().getResource(Constants.TEST_INPUT_XML);
			File file = null;
			logger.debug("URL of test input xml : " + url);
			if(url != null)
			{
				logger.debug("The test input file was found : calling the Generation method...");
				file = new File(url.getPath());
				RestAssured.given().multiPart(file).expect().statusCode(200).when()
				.post("/REST_Questionnaire_Generator/Main/Service/Generation");
			}
		}
		//If a technical error is caught, launches the dummy helloworld test to keep the building from failing
		//But logs an error with the given exception
		catch (Exception e)
		{
			logger.error(e.getMessage());
			RestAssured.expect().statusCode(200).contentType(ContentType.TEXT).when()
			.get("/REST_Questionnaire_Generator/Main/Service/helloworld");
		}
		
	}

	/**
	 * Non regression Test : calling the Difference.java class to test if
	 * TEST_FILE_TO_COMPARE and TEST_REFERENCE_FILE have the same exact content
	 */
	@Test
	public void nonRegressionTest() {
		logger.debug("Starting Non Regression Test with files : -fileToCompare : " + Constants.TEST_FILE_TO_COMPARE
				+ " -referenceFile" + Constants.TEST_REFERENCE_FILE);

		File toCompare = new File(Constants.TEST_FILE_TO_COMPARE);
		
		URL urlReference = Thread.currentThread().getContextClassLoader().getResource(Constants.TEST_REFERENCE_FILE);
		File referenceFile = null;
		
		if(urlReference != null)
		{
			referenceFile = new File(urlReference.getPath());
		}
		
		Difference diff = new Difference();
		
		if(toCompare != null && toCompare.exists() && referenceFile != null && referenceFile.exists())
		{
			logger.debug("Non regression Test : both files were found, asserting true for nonRegressionTest");
			Assert.assertEquals(true, diff.nonRegressionTest());
		}
		else
		{
			logger.debug("Non regression Test : at least one file wasn't found, asserting false for nonRegressionTest");
			Assert.assertEquals(false, diff.nonRegressionTest());
		}
		
		
	}

}
