package fr.insee.eno.ws;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.eno.Constants;
import fr.insee.eno.transforms.DDIToPDF;
import fr.insee.eno.transforms.PipeLine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/eno")
@Tag(name = "Eno", description = "Eno transform")
@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"),
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class EnoTransforms {

	@Autowired
	DDIToPDF ddiToPDF;

	Logger logger = LogManager.getLogger(EnoTransforms.class);

	@GET
	@Path("/pdf")
	@Produces("application/pdf")
	@Operation(operationId = "getPDF", summary = "Get PDF", responses = {
			@ApiResponse(content = @Content(mediaType = "application/pdf")) })
	public Response getPdf() throws Exception {

		File file = null;
		URL url = Constants.class.getResource("/pdf/out.pdf");
		try {
			file = new File(url.toURI());
		} catch (URISyntaxException e) {
			file = new File(url.getPath());
		}
		FileInputStream fileInputStream = new FileInputStream(file);
		javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response
				.ok((Object) fileInputStream);
		responseBuilder.type("application/pdf");

		responseBuilder.header("Content-Disposition", "attachment ; filename=test.pdf");
		return responseBuilder.build();
	}

	@POST
	@Path("ddi2pdf")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Operation(operationId = "getPDF", summary = "Get PDF", responses = {
			@ApiResponse(content = @Content(mediaType = "application/pdf")) })
	public Response ddi2pdf(@Context final HttpServletRequest request) throws Exception {
		PipeLine pipeline = new PipeLine();
		Map<String, Object> params = new HashMap<>();
		String filePath = null;
		String questionnaireName = "pdf";
		try {
			filePath = pipeline.from(request.getInputStream()).map(ddiToPDF::transform, params, questionnaireName)
					.transform();
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new EnoException(500, e.getMessage(), null);
		}

		File file = new File(filePath);
		byte[] output = Files.readAllBytes(file.toPath());
		return Response.ok(output, MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();

	}

}
