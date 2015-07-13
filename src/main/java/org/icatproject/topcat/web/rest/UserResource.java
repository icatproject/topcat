package org.icatproject.topcat.web.rest;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.repository.DownloadRepository;

@Stateless
@LocalBean
@Path("v1")
public class UserResource {
    static final Logger logger = Logger.getLogger(UserResource.class);

    @EJB
    private DownloadRepository downloadRepository;

    @EJB
    private ICATClientBean icatClientService;


    @GET
    @Path("/downloads/facility/{facilityName}/user/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDownloadsByFacilityNameAndUser(
            @PathParam("facilityName") String facilityName,
            @PathParam("userName") String userName,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("icatUrl") String icatUrl,
            @QueryParam("status") String status,
            @QueryParam("transport") String transport,
            @QueryParam("preparedId") String preparedId) throws MalformedURLException, TopcatException {
        logger.info("getDownloadsByFacilityNameAndUser() called");

        if (sessionId == null) {
            throw new BadRequestException("sessionId query parameter is required");
        }

        if (icatUrl == null) {
            throw new BadRequestException("icatUrl query parameter is required");
        }

        //check user is authorised
        boolean auth = icatClientService.isSessionValid(icatUrl, sessionId);

        if (! auth) {
            throw new AuthenticationException("sessionId not valid");
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", facilityName);
        params.put("userName", userName);
        params.put("sessionId", sessionId);
        params.put("icatUrl", icatUrl);
        params.put("status", status);
        params.put("transport", transport);
        params.put("preparedId", preparedId);

        List<Download> downloads = new ArrayList<Download>();
        downloads = downloadRepository.getDownloadsByFacilityNameAndUser(params);

        return Response.ok().entity(new GenericEntity<List<Download>>(downloads){}).build();
    }

}
