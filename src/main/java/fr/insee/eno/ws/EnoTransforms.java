package fr.insee.eno.ws;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.eno.Constants;
import fr.insee.eno.transforms.DDIToFO;
import fr.insee.eno.transforms.DDIToFOWithPlugin;
import fr.insee.eno.transforms.DDIToPDF;
import fr.insee.eno.transforms.DDIToXForm;
import fr.insee.eno.transforms.Transformer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/eno")
@Api(value = "Eno Transforms")
public class EnoTransforms {

	@Autowired
	DDIToXForm ddiToXForm;

	@Autowired
	DDIToFO ddiToFO;

	@Autowired
	DDIToPDF ddiToPDF;
	
	@Autowired
	DDIToFOWithPlugin ddiToFOWithPlugin;

	Logger logger = LogManager.getLogger(EnoTransforms.class);

	
	
	@GET
	@Path("/pdf")
	@Produces("application/pdf")
	@ApiOperation(value = "Get PDF")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK",response=byte.class), @ApiResponse(code = 500, message = "Error") })

	public Response getPdf() throws Exception
	{
	    
		File file = null;
		URL url = Constants.class.getResource("/pdf/out.pdf");
	    try {
	        file = new File(url.toURI());
	    } catch (URISyntaxException e) {
	        file = new File(url.getPath());
	    } 
	    FileInputStream fileInputStream = new FileInputStream(file);
	    javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.ok((Object) fileInputStream);
	    responseBuilder.type("application/pdf");
	    
	    responseBuilder.header("Content-Disposition", "attachment ; filename=test.pdf");
	    return responseBuilder.build();
	}
	
	
	@POST
	@Path("ddi2xforms")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@ApiOperation(value = "Get XForm From DDI metadata", notes = "Get Transformed XForm document from DDI metadata representation")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Error") })
	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "DDI", value = "DDI representation of the Questionnaire", paramType = "body", dataType = "string") })
	public Response ddi2XForm(@Context final HttpServletRequest request) throws Exception {
		try {
			return transform(request, ddiToXForm);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new EnoException(500, e.getMessage(), null);
		}
	}

	@POST
	@Path("ddi2fo")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@ApiOperation(value = "Get Fo From DDI metadata", notes = "Get Transformed XSL-FO document from DDI metadata representation")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Error") })
	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "DDI", value = "DDI representation of the Questionnaire", paramType = "body", dataType = "string") })
	public Response ddi2FO(@Context final HttpServletRequest request) throws Exception {
		try {
			return transform(request, ddiToFO);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new EnoException(500, e.getMessage(), null);
		}
	}

	@POST
	@Path("ddi2pdf")
	@Produces("application/pdf")
	@Consumes(MediaType.APPLICATION_XML)
	@ApiOperation(value = "Get PDF From DDI metadata", notes = "Get Transformed PDF document from DDI metadata representation")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Error") })
	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "DDI", value = "DDI representation of the Questionnaire", paramType = "body", dataType = "string") })
	public Response ddi2PDF(@Context final HttpServletRequest request) throws Exception {

		try {
			
			String stream = ddiToPDF.transform(request.getInputStream(), null);
			return Response.ok(stream,"application/pdf")
					.header("Content-Disposition", "filename=form.pdf").build();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw e;
		}

	}
	
	@POST
	@Path("ddi2fo-with-plugin")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@ApiOperation(value = "Get FO (with plugin) From DDI metadata", notes = "Get Transformed XSL FO document from DDI metadata representation")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Error") })
	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "DDI", value = "DDI representation of the Questionnaire", paramType = "body", dataType = "string") })
	public Response ddi2FOWithPlugin(@Context final HttpServletRequest request) throws Exception {

		try {
			return transform(request, ddiToFOWithPlugin);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new EnoException(500, e.getMessage(), null);
		}

	}

	private Response transform(HttpServletRequest request, Transformer transformer) throws Exception {
		try {
			StreamingOutput stream = output -> {
				try {
					transformer.transform(request.getInputStream(), output, null);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					throw new EnoException(500, e.getMessage(), null);
				}
			};
			return Response.ok(stream).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
