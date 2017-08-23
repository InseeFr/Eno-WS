package fr.insee.eno.config;

import io.swagger.jaxrs.config.BeanConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

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
        super.init(config);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setTitle("Eno Transform");
        beanConfig.setVersion("0.1");
        beanConfig.setDescription("D.D.I. to XForm Transformations As A Service");
        beanConfig.setSchemes(new String[]{"http"});
        //TODO Externalize the parameter
        beanConfig.setBasePath("/api");
        beanConfig.setHost("dvstromaeldb01.ad.insee.intra:8080");
        beanConfig.setResourcePackage("fr.insee.eno.ws");
        beanConfig.setScan(true);
    }
}
