package fr.insee.eno.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@SpringBootApplication(scanBasePackages = "fr.insee.eno.ws")
public class EnoWS extends SpringBootServletInitializer{
	
	
	public static final String APP_NAME = "enows";
	public static final String PROPERTIES_FILE_NAME = "enows.properties";

	private static final Logger LOGGER = LoggerFactory.getLogger(EnoWS.class);
	
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
	
	public static void setProperty() {
		System.setProperty("spring.config.location", "classpath:"+PROPERTIES_FILE_NAME);
	}
	
	@EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        final Environment env = event.getApplicationContext().getEnvironment();
        LOGGER.info("================================ Properties ================================");
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
                .filter(prop -> prop.startsWith("fr.insee") || prop.startsWith("logging") || prop.startsWith("spring"))
                .sorted()
                .forEach(prop -> LOGGER.info("{}: {}", prop, env.getProperty(prop)));
        LOGGER.info("===========================================================================");
        LOGGER.info("Available CPU : "+Runtime.getRuntime().availableProcessors());
        LOGGER.info(String.format("Max memory : %.2f GB",Runtime.getRuntime().maxMemory()/1e9d));
        LOGGER.info("===========================================================================");
    }
	
	@EventListener
	public void handleApplicationReady(ApplicationReadyEvent event) {
        LOGGER.info("=============== "+APP_NAME+"  has successfully started. ===============");
		
	}
}
