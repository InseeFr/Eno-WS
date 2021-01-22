package fr.insee.eno.ws.transform ;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.insee.eno.postprocessing.lunaticxml.LunaticXMLVTLParserPostprocessor;

public class TestXpath2VTLParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestXpath2VTLParser.class);

    @Test
    public void parseToVTL(){
        LunaticXMLVTLParserPostprocessor parser = new LunaticXMLVTLParserPostprocessor();
        String xpath = "concat(A,B,C)";
        String expectedVTL = "A || B || C";
        String vtl = parser.parseToVTL(xpath);
        LOGGER.info("Xpath parse to " + vtl);
        Assert.assertEquals(expectedVTL,vtl);
    }



}
