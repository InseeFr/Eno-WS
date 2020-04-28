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
	
	@Value("${fr.insee.enows.lunatic.model.version}")
	private String lunaticModelVersion;
	
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
								"<h2>Generator using :</h2>"
							  + "<style>.cell{border: black 2px solid; text-align: center; font-weight: bold; font-size: 1.5em;} .version{color:darkred}</style>"
							  + "<table style=\"width:40%\">"
							  + "	<tr><td class=\"cell\">Eno version</td><td class=\"cell version\">"+enoVersion+"</td></tr>"
							  + "	<tr><td class=\"cell\">Lunatic Model version</td><td class=\"cell version\">"+lunaticModelVersion+"</td></tr>"
							  + "</table>")
						.version(projectVersion)
						.license(new License().name("Apache 2.0").url("http://springdoc.org"))
						);
		return openAPI;
	}
}
