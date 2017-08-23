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

@Path("/v1")
@Api(value = "Eno Transforms")
public class EnoTransformsV1 {

    @Autowired
    DDIToXForm ddiToXForm;

    Logger logger = LogManager.getLogger(EnoTransformsV1.class);

    @POST
    @Path("transform")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @ApiOperation(
            value = "Get Pogues XForm From Pogues DDI metadata",
            notes = "Get Transformed XForm document from Pogues DDI metadata representation"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Error")
    })
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "poguesDDI", value = "DDI representation of the Pogues Model", paramType = "body", dataType = "string")
    })
    public Response ddi2XForm(@Context final HttpServletRequest request) throws Exception {
        try {
            return transform(request, ddiToXForm);
        } catch(PoguesException e) {
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
                    throw new PoguesException(500, e.getMessage(), null);
                }
            };
            return Response.ok(stream).build();
        } catch (Exception e) {
            throw e;
        }
    }
}
