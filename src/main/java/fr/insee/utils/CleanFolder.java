package fr.insee.utils;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Service used to clean folders (deletes all files)
 * 
 * @author gerose
 *
 */
public class CleanFolder {

	final static Logger logger = Logger.getLogger(CleanFolder.class);

	/**
	 * Method representing the Clean ant target : Cleaning the current
	 * questionnaireFolder (with the created survey name) Cleaning the temp
	 * folder Cleaning the test folder
	 * 
	 * @param questionnaireFolder
	 *            : the folder that has to be either created or cleaned (having
	 *            the survey's name)
	 * @throws Exception
	 *             : FileNotfound / NoAccess mainly
	 */
	public void cleanTarget(String questionnaireFolder) throws Exception {

		String tempFolder = Constants.TARGET_FOLDER + "/temp";
		String testFolder = Constants.TARGET_FOLDER + "/test";

		logger.debug("Cleaning directories : " + tempFolder + " | " + testFolder + " | " + questionnaireFolder);

		FileUtils.forceMkdir(new File(tempFolder));
		FileUtils.cleanDirectory(new File(tempFolder));

		FileUtils.forceMkdir(new File(testFolder));
		FileUtils.cleanDirectory(new File(testFolder));

		FileUtils.forceMkdir(new File(questionnaireFolder));
		FileUtils.cleanDirectory(new File(questionnaireFolder));

	}

	/**
	 * Generic method to clean one folder
	 * 
	 * @param pathToFolder
	 *            : the folder to be cleaned
	 * @throws Exception
	 *             : FileNotfound / NoAccess mainly
	 */
	public void cleanOneFolder(String pathToFolder) throws Exception {
		logger.debug("Cleaning " + pathToFolder);
		FileUtils.forceMkdir(new File(pathToFolder));
		FileUtils.cleanDirectory(new File(pathToFolder));

	}
}
