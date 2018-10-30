package fr.insee.eno.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@ApplicationPath("/api")
public class SwaggerConfig extends ResourceConfig {

	public SwaggerConfig(@Context ServletConfig servletConfig) throws IOException {
		super();

		Properties props = getEnvironmentProperties();
		OpenAPI oas = new OpenAPI();

		Info info = new Info().title("Eno API").version("1.1.0")
				.description("Rest Endpoints and services Integration used by Eno");
		oas.info(info);
		Server server = new Server();
		server.setDescription("API server");

		String url = String.format("%s://%s%s", props.get("fr.insee.eno.api.scheme"),
				props.get("fr.insee.eno.api.host"), props.get("fr.insee.eno.api.name"));

		server.setUrl(url);
		List<Server> servers = new ArrayList<Server>();
		servers.add(server);
		oas.setServers(servers);
		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas).prettyPrint(true)
				.resourcePackages(Stream.of("fr.insee.eno.ws").collect(Collectors.toSet()));

		OpenApiResource openApiResource = new OpenApiResource();

		openApiResource.setOpenApiConfiguration(oasConfig);
		register(openApiResource);

	}

	private Properties getEnvironmentProperties() throws IOException {
		Properties props = new Properties();

		String propsPath = "eno.properties";
		props.load(getClass().getClassLoader().getResourceAsStream(propsPath));
		File f = new File(String.format("%s/webapps/%s", System.getProperty("catalina.base"), "eno.properties"));
		if (f.exists() && !f.isDirectory()) {
			FileReader r = new FileReader(f);
			props.load(r);
			r.close();
		}
		return props;
	}

}
