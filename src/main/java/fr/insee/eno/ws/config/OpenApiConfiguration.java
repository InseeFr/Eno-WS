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
	

	@Value("${fr.insee.enows.api.scheme}")
	private String apiScheme;
	
	@Value("${fr.insee.enows.api.host}")
	private String apiHost;
		
	@Value("${fr.insee.enows.enocore.version}")
	private String enoVersion;
	
	@Value("${fr.insee.enows.version}")
	private String projectVersion;
	
	@Bean
	public OpenAPI customOpenAPI() {
		Server server = new Server();
		server.setUrl(apiScheme+"://"+apiHost);		
		OpenAPI openAPI = new OpenAPI()
				.addServersItem(server)
				.info(
						new Info()
						.title("Eno Web Services")
						.description(
								"<p><h2>Generator using Eno version : <span style=\"color:darkred;\">"+enoVersion+"</span></h2></p>")
						.version(projectVersion)
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
		return openAPI;
	}
}
