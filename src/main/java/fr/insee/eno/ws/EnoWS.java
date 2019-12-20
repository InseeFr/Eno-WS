package fr.insee.eno.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication(scanBasePackages = "fr.insee.eno.ws")
public class EnoWS extends SpringBootServletInitializer{
	
	
	public static final String APP_NAME = "enows";
	
	public static void main(String[] args) {
		System.setProperty("spring.config.name", APP_NAME);
		SpringApplication.run(EnoWS.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		System.setProperty("spring.config.name", APP_NAME);		
		setProperty();
		return application.sources(EnoWS.class);
	}
	
	@ConditionalOnExpression("'${spring.profiles.active}'!='local')")
	public static void setProperty() {
		System.setProperty("spring.config.location",
				"classpath:"+APP_NAME+".properties,"
				+ "file:///${catalina.base}/webapps/"+APP_NAME+".properties");
	}
}
