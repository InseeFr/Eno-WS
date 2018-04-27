package fr.insee.eno.transforms;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.eno.GenerationService;
import fr.insee.eno.generation.DDI2PDFGenerator;
import fr.insee.eno.postprocessing.PDFPostprocessor;
import fr.insee.eno.preprocessing.DDIPreprocessor;

@Service
public class DDIToFOWithPluginImpl implements DDIToFOWithPlugin {

    private static final Logger logger = LogManager.getLogger(DDIToFOWithPluginImpl.class);

    @Override
    public void transform(InputStream input, OutputStream output, Map<String, Object> params) throws Exception {
        if (null == input) {
            throw new NullPointerException("Null input");
        }
        if (null == output) {
            throw new NullPointerException("Null output");
        }
        try {
            String xform =  transform(input, params);
            output.write(xform.getBytes(StandardCharsets.UTF_8));
        } catch(Exception e) {
            throw e;
        }
    }

    @Override
    public String transform(InputStream input, Map<String, Object> params) throws Exception {
        if (null == input) {
            throw new NullPointerException("Null input");
        }
        File enoInput = null;
        try {
            enoInput = File.createTempFile("eno", ".xml");
            FileUtils.copyInputStreamToFile(input, enoInput);
            return transform(enoInput, params);
        } catch(Exception e) {
            throw e;
        }
    }

    @Override
    public String transform(String input, Map<String, Object> params) throws Exception {
        File enoInput = null;
        if (null == input) {
            throw new NullPointerException("Null input");
        }
        try {
            enoInput = File.createTempFile("eno", ".xml");
            FileUtils.writeStringToFile(enoInput, input, StandardCharsets.UTF_8);
            return transform(enoInput, params);
        } catch(Exception e) {
            throw e;
        }
    }

    private String transform(File file, Map<String, Object> params) throws Exception {
        File output = null;
        try {
            logger.debug("Transform File");
            GenerationService genService = new GenerationService(new DDIPreprocessor(), new DDI2PDFGenerator(), new PDFPostprocessor());
            output = genService.generateQuestionnaire(file,null);
            String response = FileUtils.readFileToString(output, Charset.forName("UTF-8"));
            return response;
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
