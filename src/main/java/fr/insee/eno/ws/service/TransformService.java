package fr.insee.eno.ws.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.insee.lunatic.conversion.JSONCleaner;
import fr.insee.lunatic.conversion.XMLLunaticFlatToJSONLunaticFlatTranslator;
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
