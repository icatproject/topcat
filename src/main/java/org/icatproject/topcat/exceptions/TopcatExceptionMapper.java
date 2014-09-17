package org.icatproject.topcat.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.icatproject.topcat.domain.ErrorMessage;

/**
 *
 */
@Provider
public class TopcatExceptionMapper implements ExceptionMapper<TopcatException> {

	@Override
	public Response toResponse(TopcatException e) {
	    ErrorMessage  error = new ErrorMessage ();
	    error.setStatus(e.getHttpStatusCode());
	    error.setCode(e.getClass().getSimpleName());
	    error.setMessage(e.getShortMessage());

		return Response.status(e.getHttpStatusCode()).entity(error)
        		.build();
	}
}