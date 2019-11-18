package fr.insee.eno.ws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
@ConditionalOnExpression("'${spring.profiles.active}'!='prod'")
public class OpenApiConfiguration{
	
	@Value("${fr.insee.enows.enocore.version}")
	private String enoVersion;
	
	@Value("${fr.insee.enows.version}")
	private String projectVersion;
	
	
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(
						new Info()
						.title("Eno Web Services")
						.description("## Generator using Eno version : <span style=\"color:darkred;\">"+enoVersion+"</span> ")
						.version(projectVersion)
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}
}
