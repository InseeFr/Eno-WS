package fr.insee.eno.config;

import io.swagger.jaxrs.config.BeanConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by acordier on 24/07/17.
 */
public class SwaggerConfig extends HttpServlet {

    @Autowired
    private Environment env;

    public void init(ServletConfig config) throws ServletException {
    	
    	try {
			super.init(config);
			Properties props = getEnvironmentProperties();
			BeanConfig beanConfig = new BeanConfig();
			beanConfig.setTitle("Eno");
			beanConfig.setVersion("0.9.0");
			beanConfig.setDescription("D.D.I. to XForm Transformations as a Web Service");
			beanConfig.setSchemes(new String[] { "http" });
			// TODO Externalize the parameter
			beanConfig.setBasePath("/api");
			beanConfig.setHost(props.getProperty("fr.insee.eno.api.host"));
			beanConfig.setResourcePackage("fr.insee.eno.ws");
			beanConfig.setScan(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
    
    }
    
    
    private Properties getEnvironmentProperties() throws IOException {
		Properties props = new Properties();
		String env = System.getProperty("fr.insee.eno.env");
		if (null == env) {
			env = "dv";
		}
		String propsPath = String.format("%s/eno.properties", env);
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
