package fr.insee.eno.targets;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import fr.insee.eno.transform.xsl.XslParameters;
import fr.insee.eno.transform.xsl.XslTransformation;
import fr.insee.eno.utils.Constants;

/**
 * Service representing the ddi2fr ant target Creates the form.xhtml from the
 * -final.tmp file
 * 
 * @author gerose
 *
 */
public class DDI2FR {

	final static Logger logger = Logger.getLogger(DDI2FR.class);

	private static XslTransformation saxonService = new XslTransformation();

	/**
	 * Main method of the ddi2fr target
	 * 
	 * @param finalInput
	 *            : the -final tmp file used in input
	 * @param surveyName
	 *            : the name of the survey used to create the proper folder to
	 *            store the created outputs
	 * @return : the output form to be added in the WS Response
	 * @throws Exception
	 *             : XSL related exceptions
	 */
	public String ddi2frTarget(String finalInput, String surveyName) throws Exception {

		logger.debug("DDI2FR Target : START");
		logger.debug("Arguments : finalInput : " + finalInput + " surveyName " + surveyName);
		String formNameFolder = null;
		String outputBasicForm = null;

		File f = new File(finalInput);

		formNameFolder = FilenameUtils.getBaseName(f.getAbsolutePath());
		formNameFolder = FilenameUtils.removeExtension(formNameFolder);
		formNameFolder = formNameFolder.replace(XslParameters.TITLED_EXTENSION, "");

		logger.debug("formNameFolder : " + formNameFolder);

		outputBasicForm = Constants.TEMP_XFORMS_FOLDER + "/" + formNameFolder + "/" + Constants.BASIC_FORM_TMP_FILENAME;
		logger.debug("Output folder for basic-form : " + outputBasicForm);

		logger.debug("Ddi2fr part 1 : from -final to basic-form");
		logger.debug("-Input : " + finalInput + " -Output : " + outputBasicForm + " -Stylesheet : "
				+ Constants.TRANSFORMATIONS_DDI2FR_DDI2FR_XSL);
		logger.debug("-Parameters : " + surveyName + " | " + formNameFolder + " | " + Constants.PROPERTIES_FILE);
		saxonService.transformDdi2frBasicForm(finalInput, Constants.TRANSFORMATIONS_DDI2FR_DDI2FR_XSL, outputBasicForm,
				surveyName, formNameFolder, Constants.PROPERTIES_FILE);

		String outputForm = Constants.TARGET_FOLDER + "/" + surveyName + "/" + formNameFolder + "/form/form.xhtml";

		logger.debug("Ddi2fr part 2 : from basic-form to form.xhtml");
		logger.debug("-Input : " + outputBasicForm + " -Output : " + outputForm + " -Stylesheet : "
				+ Constants.BROWSING_TEMPLATE_XSL);
		logger.debug("-Parameters : " + surveyName + " | " + formNameFolder + " | " + Constants.PROPERTIES_FILE);
		saxonService.transformDdi2frBasicForm(outputBasicForm, Constants.BROWSING_TEMPLATE_XSL, outputForm, surveyName,
				formNameFolder, Constants.PROPERTIES_FILE);

		return outputForm;
	}
}
