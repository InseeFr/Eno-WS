package fr.insee.eno.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.swagger.jaxrs.config.BeanConfig;


public class SwaggerConfig extends HttpServlet {


    private final static Logger logger = LogManager.getLogger(SwaggerConfig.class);
    
    public void init(ServletConfig config) throws ServletException {
    	
    	try {
    		super.init(config);
            Properties props = getEnvironmentProperties();
            BeanConfig beanConfig = new BeanConfig();
            beanConfig.setTitle("Eno");
            beanConfig.setVersion("1.0.0");
            beanConfig.setDescription("Eno API endpoints");
            beanConfig.setSchemes(new String[]{props.getProperty("fr.insee.eno.api.scheme")});
            beanConfig.setBasePath(props.getProperty("fr.insee.eno.api.name"));
            beanConfig.setHost(props.getProperty("fr.insee.eno.api.host"));
            beanConfig.setResourcePackage("fr.insee.eno.ws");
            beanConfig.setScan(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
    
    }
    
    
    private Properties getEnvironmentProperties() throws IOException {
		Properties props = new Properties();
		
		String propsPath = "eno.properties";
		props.load(getClass().getClassLoader().getResourceAsStream(propsPath));
		File f = new File(
				String.format("%s/webapps/%s", System.getProperty("catalina.base"), "eno.properties"));
		if (f.exists() && !f.isDirectory()) {
			FileReader r = new FileReader(f);
			props.load(r);
			r.close();
		}
		return props;
	}
    
}
