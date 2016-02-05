package org.icatproject.topcat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.icatproject.topcat.domain.ErrorMessage;
import org.icatproject.topcat.exceptions.InternalException;

/**
 * This exception mapper handles runtime and exceptions thrown by jersey
 *
 *
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<RuntimeException> {

    static final Logger logger = Logger
            .getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException runtime) {
        // Check for any specific handling
        if (runtime instanceof WebApplicationException) {
            return handleWebApplicationException(runtime);
        }

        ErrorMessage error = new ErrorMessage();
        error.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        error.setCode(InternalException.class.getSimpleName());
        error.setMessage(runtime.getClass() + " " + runtime.getMessage());

        Response defaultResponse = Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .entity(error).type(MediaType.APPLICATION_JSON).build();

        return defaultResponse;
    }


    private Response handleWebApplicationException(RuntimeException exception) {
        WebApplicationException webAppException = (WebApplicationException) exception;

        int status = webAppException.getResponse().getStatus();
        ErrorMessage error = new ErrorMessage();

        if (status == 404) {
            error.setStatus(status);
            error.setCode("NotFoundException");
            error.setMessage(webAppException.getMessage());

            return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON).build();
        }

        if (status == 500) {
            //log error
            StringWriter errorStackTrace = new StringWriter();
            exception.printStackTrace(new PrintWriter(errorStackTrace));
            logger.error(exception.getClass() + " " + errorStackTrace.toString());

            error.setStatus(status);
            error.setCode("InternalException");
            error.setMessage(webAppException.getMessage());

            return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON).build();
        } else {
            error.setStatus(status);
            error.setCode(webAppException.getMessage());
            error.setMessage(webAppException.getMessage());

            return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
