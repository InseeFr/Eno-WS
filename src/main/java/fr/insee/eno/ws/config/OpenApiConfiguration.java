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
	
	
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(
						new Info().title("Api for Eno").description("Generator").version("0.0.1")
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}
}
