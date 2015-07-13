package org.icatproject.topcat.web.rest;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.icatproject.topcat.domain.BooleanValue;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadItem;
import org.icatproject.topcat.domain.EntityType;
import org.icatproject.topcat.domain.LongValue;
import org.icatproject.topcat.domain.StringValue;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.repository.DownloadRepository;

import com.github.javafaker.Faker;


@Stateless
@LocalBean
@Path("v1")
public class AdminResource {
    static final Logger logger = Logger.getLogger(AdminResource.class);

    @EJB
    private DownloadRepository downloadRepository;

    @EJB
    private ICATClientBean icatClientService;


    @GET
    @Path("/downloads/facility/{facilityName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDownloadsByFacilityName(
            @PathParam("facilityName") String facilityName,
            @QueryParam("status") String status,
            @QueryParam("transport") String transport,
            @QueryParam("preparedId") String preparedId) {
        logger.info("getDownloadsByFacilityName() called");

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", facilityName);
        params.put("status", status);
        params.put("transport", transport);
        params.put("preparedId", preparedId);

        List<Download> downloads = new ArrayList<Download>();
        downloads = downloadRepository.getDownloadsByFacilityName(params);

        return Response.ok().entity(new GenericEntity<List<Download>>(downloads){}).build();
    }


    @GET
    @Path("/generate-fixture")
    @Produces({MediaType.APPLICATION_JSON})
    public Response loadFixtures() {
        logger.info("loadFixture() called");
        Faker faker = new Faker();
        Long count = 0L;

        String[] facilitites = {"dls", "sig"};
        String[] users = {"wayne", "rachel", "jane", "dave"};
        String[] transports = {"https", "globus"};
        String[] statuses = {"ONLINE", "ARCHIVE", "COMPLETE"};

        for(int x = 0; x < 20; x++) {
            logger.info("loadFixture() called");
            int facilityIdx = new Random().nextInt(facilitites.length);
            String facility = (facilitites[facilityIdx]);

            int userIdx = new Random().nextInt(users.length);
            String user = (users[userIdx]);

            int transportIdx = new Random().nextInt(transports.length);
            String transport = (transports[transportIdx]);

            int statusIdx = new Random().nextInt(statuses.length);
            String status = (statuses[statusIdx]);

            List<DownloadItem> downloadItems = new ArrayList<DownloadItem>();

            Download download = new Download();
            download.setFacilityName(facility);
            download.setFileName(faker.lorem().fixedString(12).toLowerCase().replace(" ", ""));
            download.setPreparedId(UUID.randomUUID().toString());
            download.setUserName(user);
            download.setTransport(transport);
            download.setStatus(status);
            download.setEmail(user + "@stfc.ac.uk");


            int numItems = new Random().nextInt(10 - 2) + 2;

            for(int i = 0; i < numItems; i++) {

                DownloadItem item = new DownloadItem();
                item.setEntityType(EntityType.investigation);

                int entityId = new Random().nextInt(200000 - 1) + 1;

                item.setEntityId(new Long(entityId));
                item.setDownload(download);

                downloadItems.add(item);
                download.setDownloadItems(downloadItems);
            }

            downloadRepository.save(download);
            count++;
        }

        LongValue id = new LongValue(count);

        return Response.ok().entity(id).build();
    }


    @GET
    @Path("/ping")
    @Produces({MediaType.APPLICATION_JSON})
    public Response ping() {
        logger.info("ping() called");
        return Response.ok().entity("ok").build();

    }


    @POST
    @Path("/login")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @FormParam("serverName") String serverName,
            @FormParam("authenticationType") String authenticationType,
            @FormParam("username") String username,
            @FormParam("password") String password) throws MalformedURLException, AuthenticationException, InternalException {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);

        String icatSessionId = icatClientService.login(serverName, authenticationType, parameters);
        StringValue topcatIcatSession = new StringValue(icatSessionId);

        return Response.ok().entity(topcatIcatSession).build();
    }


    @GET
    @Path("/session/{serverName}/{icatSessionId}/valid")
    @Produces({MediaType.APPLICATION_JSON})
    public Response isSessionValid(@PathParam("serverName") String serverName, @QueryParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        Boolean result = icatClientService.isSessionValid(serverName, icatSessionId);
        BooleanValue value = new BooleanValue(result);

        return Response.ok().entity(value).build();
    }


    @GET
    @Path("/session/{serverName}/{icatSessionId}/remain")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTimeRemaining(@PathParam("serverName") String serverName, @QueryParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        Long result = icatClientService.getRemainingMinutes(serverName, icatSessionId);
        LongValue value = new LongValue(result);

        return Response.ok().entity(value).build();
    }

    @GET
    @Path("/exception/{status}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exceptionTest(@PathParam("status") int status) throws IcatException {
        if (status == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            throw new IcatException("Internal Exeption");
        }

        if (status == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new IcatException("Bad Request Exeption");
        }

        if (status == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new IcatException("Insufficient Privileges Exception");
        }

        if (status == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new IcatException("Bad Request Exception");
        }

        //throw internal for all others
        throw new IcatException("Internal Exeption");

    }



}
