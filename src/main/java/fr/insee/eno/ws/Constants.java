package fr.insee.eno.ws;

import java.io.IOException;
import java.util.Properties;

public class Constants {

    public static String getPropertiesFileName(){
        String propertiesFileName="enows";
        try {
            Properties properties = new Properties();
            properties.load(Constants.class.getResourceAsStream("/enows.properties"));
            propertiesFileName = properties.getProperty("fr.insee.enows.properties.file");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propertiesFileName;
    }
}
