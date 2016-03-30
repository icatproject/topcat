package org.icatproject.topcat.web.rest;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Path;

import org.icatproject.topcat.Constants;
import org.icatproject.topcat.domain.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Path("v1")
public class GeneralResource {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

    /**
     * Used to detect whether Topcat is running or not.
     *
     * @summary ping
     *
     * @return a string "ok" if all is well
    */
    @GET
    @Path("/ping")
    @Produces({MediaType.APPLICATION_JSON})
    public Response ping() {
        logger.info("ping() called");

        StringValue value = new StringValue("ok");

        return Response.ok().entity(value).build();
    }

    /**
     * Provides the current version of the Topcat instance.
     *
     * @summary getVersion
     *
     * @return a version number as a string e.g. "2.0.0"
    */
    @GET
    @Path("/version")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getVersion() {
        logger.info("getVersion() called");

        StringValue value = new StringValue(Constants.API_VERSION);

        return Response.ok().entity(value).build();
    }

}
