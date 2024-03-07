package fr.insee.eno.ws.service;

import fr.insee.lunatic.conversion.JSONCleaner;
import fr.insee.lunatic.conversion.XMLLunaticFlatToJSONLunaticFlatTranslator;
import fr.insee.lunatic.conversion.XMLLunaticToXMLLunaticFlatTranslator;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Service
public class TransformService {
	

	private static final Logger LOGGER = LoggerFactory.getLogger(TransformService.class);
	
	private XMLLunaticToXMLLunaticFlatTranslator translatorXML2XMLF = new XMLLunaticToXMLLunaticFlatTranslator();
	private XMLLunaticFlatToJSONLunaticFlatTranslator translatorXMLF2JSONF = new XMLLunaticFlatToJSONLunaticFlatTranslator();
	private JSONCleaner jsonCleaner = new JSONCleaner();

	public String XMLLunaticToJSONLunaticFlat(String xmlLunatic) throws Exception {
		return jsonCleaner.clean(translatorXMLF2JSONF.translate(translatorXML2XMLF.generate(xmlLunatic)));
	}

	public ByteArrayOutputStream XMLLunaticToJSONLunaticFlat(InputStream xmlLunatic) throws Exception {
		String xmlLunaticString = IOUtils.toString(xmlLunatic, StandardCharsets.UTF_8);
		xmlLunatic.close();
		String jsonLunaticString = XMLLunaticToJSONLunaticFlat(xmlLunaticString);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(jsonLunaticString.getBytes(StandardCharsets.UTF_8));
		return outputStream;
	}

}
