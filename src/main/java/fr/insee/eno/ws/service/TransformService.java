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
	
	private XMLLunaticToXMLLunaticFlatTranslator translatorXML2XMLF = new XMLLunaticToXMLLunaticFlatTranslator();
	private XMLLunaticFlatToJSONLunaticFlatTranslator translatorXMLF2JSONF = new XMLLunaticFlatToJSONLunaticFlatTranslator();
	private JSONCleaner jsonCleaner = new JSONCleaner();
	
	
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
