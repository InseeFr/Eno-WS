package fr.insee.eno.ws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@ConditionalOnExpression("'${spring.profiles.active}'!='prod'")
public class OpenApiConfiguration {
	

	@Value("${enows.server.url}")
	private String baseUrl;
		
	@Value("${enows.enocore.version}")
	private String enoVersion;
	
	@Value("${enows.version}")
	private String projectVersion;
	
	@Bean
	public OpenAPI customOpenAPI() {
		Server server = new Server();
		server.setUrl(baseUrl);		
		OpenAPI openAPI = new OpenAPI()
				.addServersItem(server)
				.info(
						new Info()
						.title("Eno Web Services")
						.description("## Generator using Eno version : <span style=\"color:darkred;\">"+enoVersion+"</span> ")
						.version(projectVersion)
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
		return openAPI;
	}
}
