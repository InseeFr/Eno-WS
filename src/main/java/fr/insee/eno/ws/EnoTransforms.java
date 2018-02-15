package fr.insee.eno.ws;

import io.swagger.annotations.*;
import fr.insee.eno.transforms.DDIToXForm;
import fr.insee.eno.transforms.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/api")
@Api(value = "Eno Transforms")
public class EnoTransforms {

    @Autowired
    DDIToXForm ddiToXForm;

    Logger logger = LogManager.getLogger(EnoTransforms.class);

    @POST
    @Path("eno")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @ApiOperation(
            value = "Get XForm From DDI metadata",
            notes = "Get Transformed XForm document from DDI metadata representation"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Error")
    })
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "DDI", value = "DDI representation of the Questionnaire", paramType = "body", dataType = "string")
    })
    public Response ddi2XForm(@Context final HttpServletRequest request) throws Exception {
        try {
            return transform(request, ddiToXForm);
        } catch(EnoException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    private Response transform(HttpServletRequest request, Transformer transformer) throws Exception {
        try {
            StreamingOutput stream = output -> {
                try {
                    transformer.transform(request.getInputStream(), output, null);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    throw new EnoException(500, e.getMessage(), null);
                }
            };
            return Response.ok(stream).build();
        } catch (Exception e) {
            throw e;
        }
    }
}
