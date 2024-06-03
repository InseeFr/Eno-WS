package fr.insee.eno.ws.service;

import fr.insee.lunatic.conversion.JSONCleaner;
import fr.insee.lunatic.conversion.XMLLunaticFlatToJSONLunaticFlatTranslator;
import fr.insee.lunatic.conversion.XMLLunaticToXMLLunaticFlatTranslator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Service
@Slf4j
public class TransformService {

	private XMLLunaticToXMLLunaticFlatTranslator translatorXML2XMLF = new XMLLunaticToXMLLunaticFlatTranslator();
	private XMLLunaticFlatToJSONLunaticFlatTranslator translatorXMLF2JSONF = new XMLLunaticFlatToJSONLunaticFlatTranslator();
	private JSONCleaner jsonCleaner = new JSONCleaner();

	public String XMLLunaticToJSONLunaticFlat(String xmlLunatic) throws Exception {
		return jsonCleaner.clean(translatorXMLF2JSONF.translate(translatorXML2XMLF.generate(xmlLunatic)));
	}

	public ByteArrayOutputStream XMLLunaticToJSONLunaticFlat(InputStream xmlLunatic) throws Exception {
		String xmlLunaticString = new String(xmlLunatic.readAllBytes(), StandardCharsets.UTF_8);
		xmlLunatic.close();
		String jsonLunaticString = XMLLunaticToJSONLunaticFlat(xmlLunaticString);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(jsonLunaticString.getBytes(StandardCharsets.UTF_8));
		return outputStream;
	}

}
