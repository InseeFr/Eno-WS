package fr.insee.eno.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {
        "classpath:eno.properties",
        "file:${catalina.base}/webapps/eno.properties"
}, ignoreResourceNotFound = true)
public class ApplicationContext {
}
