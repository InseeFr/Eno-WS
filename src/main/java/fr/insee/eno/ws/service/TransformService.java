package fr.insee.eno.ws.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import fr.insee.lunatic.conversion.JSONCleaner;
import fr.insee.lunatic.conversion.XMLLunaticFlatToJSONLunaticFlatTranslator;
import fr.insee.lunatic.conversion.XMLLunaticToJSONLunaticTranslator;
import fr.insee.lunatic.conversion.XMLLunaticToXMLLunaticFlatTranslator;


@Service
public class TransformService {
	

	private static final Logger LOGGER = LoggerFactory.getLogger(TransformService.class);
	
	private XMLLunaticToJSONLunaticTranslator translatorXML2JSON = new XMLLunaticToJSONLunaticTranslator();
	private XMLLunaticToXMLLunaticFlatTranslator translatorXML2XMLF = new XMLLunaticToXMLLunaticFlatTranslator();
	private XMLLunaticFlatToJSONLunaticFlatTranslator translatorXMLF2JSONF = new XMLLunaticFlatToJSONLunaticFlatTranslator();
	private JSONCleaner jsonCleaner = new JSONCleaner();
	
	
	public File foToPDFtransform(File foFile) throws TransformerException, IOException, SAXException, URISyntaxException {
		LOGGER.info("Converting FO file to PDF file");
		File conf = new File(TransformService.class.getResource("/pdf/fop.xconf").toURI());
		InputStream isXconf = new FileInputStream(conf);
		URI imgFolderUri = TransformService.class.getResource("/pdf/img/").toURI();
		LOGGER.info("FO  file : " + foFile.getAbsolutePath());

		// Step 1: Construct a FopFactory by specifying a reference to the
		// configuration file
		// (reuse if you plan to render multiple documents!)
		FopFactory fopFactory = FopFactory.newInstance(imgFolderUri, isXconf);

		String outFilePath = FilenameUtils.removeExtension(foFile.getPath()) + ".pdf";
		File outFilePDF = new File(outFilePath);

		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons
		// (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outFilePDF));

		// Step 3: Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		// Step 4: Setup JAXP using identity transformer
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(); // identity
															// transformer

		// Step 5: Setup input and output for XSLT transformation
		// Setup input stream
		Source src = new StreamSource(foFile);
		// Resulting SAX events (the generated FO) must be piped through
		// to FOP
		Result res = new SAXResult(fop.getDefaultHandler());

		// Step 6: Start XSLT transformation and FOP processing
		transformer.transform(src, res);

		// Clean-up
		out.close();
		return outFilePDF;
	}
	
	public File XMLLunaticToJSONLunatic(File xmlLunatic) throws Exception   {
		Path outPathJSON = Paths.get(FilenameUtils.removeExtension(xmlLunatic.getPath()) + ".json");
		Files.deleteIfExists(outPathJSON);
		Path outputFile = Files.createFile(outPathJSON);
		Files.write(outPathJSON,
				jsonCleaner.clean(translatorXML2JSON.translate(xmlLunatic))
				.getBytes(StandardCharsets.UTF_8));
		return outputFile.toFile();
	}
	
	public File XMLLunaticToJSONLunaticFlat(File xmlLunatic) throws Exception {
		Path outPathJSON = Paths.get(FilenameUtils.removeExtension(xmlLunatic.getPath()) + ".json");
		Files.deleteIfExists(outPathJSON);
		Path outputFile = Files.createFile(outPathJSON);
		Files.write(outPathJSON,
				jsonCleaner.clean(translatorXMLF2JSONF.translate(translatorXML2XMLF.generate(xmlLunatic)))
				.getBytes(StandardCharsets.UTF_8));
		return outputFile.toFile();
	}

}
