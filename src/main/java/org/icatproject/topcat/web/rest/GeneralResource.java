package org.icatproject.topcat.web.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.icatproject.topcat.Constants;
import org.icatproject.topcat.domain.ConfVar;
import org.icatproject.topcat.repository.ConfVarRepository;
import org.icatproject.topcat.exceptions.TopcatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Path("")
public class GeneralResource {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

    @EJB
    private ConfVarRepository confVarRepository;

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


        return Response.ok().entity("\"ok\"").build();
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
        return Response.ok().entity("\"" + Constants.API_VERSION + "\"").build();
    }

    /**
     * Retrieves a configuration variable.
     *
     * @summary getConfVar
     *
     *
     * @throws TopcatException if anything else goes wrong.
     */
    @GET
    @Path("/confVars/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getConfVar(@PathParam("name") String name)
        throws TopcatException {

        ConfVar confVar = confVarRepository.getConfVar(name);

        if(confVar != null){
            return Response.ok().entity(confVar).build();
        } else {
            return Response.ok().entity("\"\"").build();
        }
    }

}
