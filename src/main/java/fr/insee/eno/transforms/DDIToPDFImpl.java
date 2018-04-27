package fr.insee.eno.transforms;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.eno.GenerationService;
import fr.insee.eno.generation.DDI2PDFGenerator;
import fr.insee.eno.postprocessing.NoopPostprocessor;
import fr.insee.eno.preprocessing.DDIPreprocessor;

@Service
public class DDIToPDFImpl implements DDIToPDF {

	final static Logger logger = LogManager.getLogger(DDIToPDFImpl.class);

	@Override
	public void transform(InputStream input, OutputStream output, Map<String, Object> params) throws Exception {
		logger.debug("Eno transformation");
		if (null == input) {
			throw new NullPointerException("Null input");
		}
		if (null == output) {
			throw new NullPointerException("Null output");
		}
		String odt = transform(input, params);
		logger.debug("Eno transformation finished");
		output.write(odt.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String transform(InputStream input, Map<String, Object> params) throws Exception {
		if (null == input) {
			throw new NullPointerException("Null input");
		}
		File enoInput;
		enoInput = File.createTempFile("eno", ".xml");
		FileUtils.copyInputStreamToFile(input, enoInput);
		return transform(enoInput, params);
	}

	@Override
	public String transform(String input, Map<String, Object> params) throws Exception {
		File enoInput;
		if (null == input) {
			throw new NullPointerException("Null input");
		}
		enoInput = File.createTempFile("eno", ".xml");
		FileUtils.writeStringToFile(enoInput, input, StandardCharsets.UTF_8);
		return transform(enoInput, params);
	}

	private String transform(File file, Map<String, Object> params) throws Exception {
		try {
			File output;
			GenerationService genService = new GenerationService(new DDIPreprocessor(), new DDI2PDFGenerator(),
					new NoopPostprocessor());
			output = genService.generateQuestionnaire(file, null);
			File conf = new File(DDIToPDFImpl.class.getResource("/pdf/fop.xconf").toURI());
			FopFactory fopFactory = FopFactory.newInstance(conf);
			String outFilePath = FilenameUtils.removeExtension(output.getPath()) + ".pdf";
			File outFile = new File(outFilePath);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(); 
			Source src = new StreamSource(output);
			Result res = new SAXResult(fop.getDefaultHandler());
			transformer.transform(src, res);
			out.close();
			return FileUtils.readFileToString(outFile, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new Exception(String.format("%s:%s", getClass().getName(), e.getMessage()));
		}
	}
}
