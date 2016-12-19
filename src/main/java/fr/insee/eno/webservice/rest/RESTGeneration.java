package fr.insee.eno.webservice.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import fr.insee.eno.targets.DDI2FR;
import fr.insee.eno.targets.DDIPreprocessing;
import fr.insee.eno.utils.CleanFolder;
import fr.insee.eno.utils.Constants;

/**
 * Main WebService class of the Questionnaire Generator
 * 
 * @author gerose
 *
 */
@Path("/Service")
public class RESTGeneration {

	final static Logger logger = Logger.getLogger(RESTGeneration.class);

	/**
	 * Dummy GET Helloworld used in unit tests
	 * 
	 * @return "Hello world" as a String
	 */
	@GET
	@Path("helloworld")
	public String helloworld() {
		return "Hello world";
	}

	/**
	 * Main WS method called to generate a questionnaire from an input xml DDI
	 * file
	 * 
	 * @param uploadedInputStream
	 *            The inputStream that will be used to write the file locally
	 * @param fileDetail
	 *            The proper file that was sent to the method
	 * @return a Response Object (.ok) with the created generator if everything
	 *         went as expected OR a Response Object (.ok) with the error that
	 *         occured during the generation if something wrong happened
	 */
	@POST
	@Path("Generation")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response generation(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("parameters") InputStream parametersInputStream,
			@FormDataParam("parameters") FormDataContentDisposition parametersDetail) {

		// Services used to process the DDIPreprocessing and DDI2FR targets
		DDIPreprocessing ddiPreprocessingService = new DDIPreprocessing();
		DDI2FR ddi2frService = new DDI2FR();

		logger.debug("WebService called with parameter file : " + fileDetail);
		logger.debug("WebService called with parameter parameters : " + parametersDetail);
		try {
			// Gets the input file's name without extension : used to create to
			// folder (ex : simpsons)
			String questionnaireNameFolder = null;
			if(uploadedInputStream != null && fileDetail != null && fileDetail.getFileName() != null)
			{
				questionnaireNameFolder = FilenameUtils.getBaseName(fileDetail.getFileName());
			}
			else
			{
				throw new Exception("You must provide an input file for the 'file' parameter.");
			}
			
			// Getting the path to the questionnaireFolder
			String questionnaireFolder = Constants.TARGET_FOLDER + "/" + questionnaireNameFolder;
			logger.debug("Questionnaire folder to be created : " + questionnaireFolder);

			// Cleaning this questionnaireFolder : deleting all files within
			CleanFolder cleaner = new CleanFolder();
			logger.debug("Cleaning the questionnaire folder");
			cleaner.cleanTarget(questionnaireFolder);
			
			//Setting the parameters file path if provided
			String parametersFilePath = null;
			if(parametersInputStream != null && parametersDetail != null && parametersDetail.getFileName() != null)
			{
				parametersFilePath = questionnaireFolder + "/" + Constants.PARAMETERS_FILE;
				writeToFile(parametersInputStream, parametersFilePath);
			}

			// Save the input ddi questionnaire in the /questionnaire folder
			String xmlInput = Constants.QUESTIONNAIRE_FOLDER + "/" + questionnaireNameFolder + "/ddi/"
					+ fileDetail.getFileName();
			logger.debug("Saving the file locally : " + xmlInput);
			writeToFile(uploadedInputStream, xmlInput);

			// DDIPreprocessing Target : returns the path to the -final file.
			logger.debug("Calling DDIPreprocessing Target");
			String titledFile = ddiPreprocessingService.ddiPreprocessingTarget(xmlInput, parametersFilePath);

			// DDI2FR Target : returns the path to the created form
			logger.debug("Calling DDI2FR Target");
			String outputForm = ddi2frService.ddi2frTarget(titledFile, questionnaireNameFolder);

			// Returning the created file in the Response Object
			logger.debug("Setting up response with : " + outputForm);
			File file = new File(outputForm);

			return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
					.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();

		} catch (Exception e) {

			logger.error("Error during the generation :" + e.getMessage());
			logger.error(e, e);

			return Response.ok("Error during the generation : " + e.toString()).build();
		}
	}

	/**
	 * Writes an uploaded file to the local machine
	 * 
	 * @param uploadedInputStream
	 *            The inputStream to write the file
	 * @param uploadedFileLocation
	 *            The location where the file will be written
	 * @throws Exception
	 *             : mainly if the uploadedFileLocation is inexistant
	 */
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) throws Exception {

		File outputFile = new File(uploadedFileLocation);
		String pathToOutputFile = outputFile.getAbsolutePath();
		pathToOutputFile = pathToOutputFile.substring(0, pathToOutputFile.lastIndexOf(File.separator));

		Files.createDirectories(Paths.get(pathToOutputFile));
		OutputStream out = new FileOutputStream(outputFile);
		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = uploadedInputStream.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
		out.flush();
		out.close();
	}

}