package org.icatproject.topcat.web.rest;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.ParseException;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.LongValue;
import org.icatproject.topcat.domain.StringValue;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.exceptions.ForbiddenException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.repository.CartRepository;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.statuscheck.ExecutePoll;
import org.icatproject.topcat.statuscheck.PollBean;
import org.icatproject.topcat.statuscheck.PollFutureBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Stateless
@LocalBean
@Path("v1/admin")
public class AdminResource {
    private static final Logger logger = LoggerFactory.getLogger(AdminResource.class);

    @EJB
    private DownloadRepository downloadRepository;

    @EJB
    private CartRepository cartRepository;

    @EJB
    private ICATClientBean icatClientService;

    @EJB
    private ExecutePoll executePoll;

    @EJB
    private PollBean pollBean;

    @EJB
    private PollFutureBean pollFutureBean;

    @GET
    @Path("/isValidSession")
    @Produces({MediaType.APPLICATION_JSON})
    public Response isValidSession(
            @QueryParam("icatUrl") String icatUrl,
            @QueryParam("sessionId") String sessionId)
            throws MalformedURLException, TopcatException {
        logger.info("isValidSession() called");
        String isAdmin = icatClientService.isAdmin(icatUrl, sessionId) ? "true" : "false";

        return Response.ok().entity(isAdmin).build();
    }

    @GET
    @Path("/downloads/facility/{facilityName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDownloadsByFacilityName(
            @PathParam("facilityName") String facilityName,
            @QueryParam("icatUrl") String icatUrl,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("userName") String userName,
            @QueryParam("status") String status,
            @QueryParam("transport") String transport,
            @QueryParam("preparedId") String preparedId,
            @QueryParam("createdAtFrom") String createdAtFrom,
            @QueryParam("createdAtTo") String createdAtTo,
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("page") Integer page)
            throws TopcatException, MalformedURLException, ParseException {

        onlyAllowAdmin(icatUrl, sessionId);

        logger.info("getDownloadsByFacilityName() called");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("facilityName", facilityName);
        params.put("userName", userName);
        params.put("status", status);
        params.put("transport", transport);
        params.put("preparedId", preparedId);
        params.put("createdAtFrom", createdAtFrom);
        params.put("createdAtTo", createdAtTo);
        params.put("pageSize", pageSize);
        params.put("page", page);

        List<Download> downloads = new ArrayList<Download>();
        downloads = downloadRepository.getDownloadsByFacilityName(params);

        return Response.ok().entity(new GenericEntity<List<Download>>(downloads){}).build();
    }

    @PUT
    @Path("/download/{preparedId}/status")
    @Produces({MediaType.APPLICATION_JSON})
    public Response setDownloadStatus(
        @PathParam("preparedId") String preparedId,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("value") String value)
        throws TopcatException, MalformedURLException, ParseException {

        onlyAllowAdmin(icatUrl, sessionId);

        Download download = downloadRepository.getDownloadByPreparedId(preparedId);

        if(download == null){
            throw new NotFoundException("could not find download");
        }

        download.setStatus(DownloadStatus.valueOf(value));

        downloadRepository.save(download);

        return Response.ok().build();
    }
    
    @PUT
    @Path("/download/{preparedId}/isDeleted")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteDownload(
        @PathParam("preparedId") String preparedId,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("value") Boolean value)
        throws TopcatException, MalformedURLException, ParseException {

        onlyAllowAdmin(icatUrl, sessionId);

        Download download = downloadRepository.getDownloadByPreparedId(preparedId);

        if(download == null){
            throw new NotFoundException("could not find download");
        }

        download.setIsDeleted(value);
        if(value){
            download.setDeletedAt(new Date());
        }

        downloadRepository.save(download);

        return Response.ok().build();
    }
    
    /*
    @PUT
    @Path("/downloads/facility/{facilityName}/complete")
    @Produces({MediaType.APPLICATION_JSON})
    public Response setCompleteByPreparedId(
            @PathParam("facilityName") String facilityName,
            @QueryParam("preparedId") String preparedId) throws BadRequestException {
        logger.info("setCompleteByPreparedId() called");

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", facilityName);
        params.put("preparedId", preparedId);


        String result = downloadRepository.setCompleteByPreparedId(params);

        if (result == null) {
            throw new BadRequestException("PreparedId " + preparedId + " not found");
        }

        StringValue id = new StringValue(preparedId);
        return Response.ok().entity(id).build();
    }


    @GET
    @Path("/poll/run")
    @Produces({MediaType.APPLICATION_JSON})
    public Response runAllPoll() {
        logger.info("runAllPoll() called");

        int count = executePoll.run();

        LongValue value = new LongValue(new Long(count));

        return Response.ok().entity(value).build();
    }

    @GET
    @Path("/poll/run/{preparedId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response runOnePoll(@PathParam("preparedId") String preparedId) {
        logger.info("runOnePoll() called");

        int count = executePoll.runByPreparedId(preparedId);

        LongValue value = new LongValue(new Long(count));

        return Response.ok().entity(value).build();
    }

    @GET
    @Path("/poll/list")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCurrentPoll(
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId)
        throws TopcatException, MalformedURLException, ParseException {

        onlyAllowAdmin(icatUrl, sessionId);

        logger.info("getCurrentPoll() called");

        List<StringValue> values = new ArrayList<StringValue>();

        List<String> preparedIds = pollBean.getAll();

        for (String preparedId : preparedIds) {
            logger.info("getCurrentPoll() preparedId: " + preparedId);
            StringValue value = new StringValue(preparedId);

            values.add(value);
        }

        return Response.ok().entity(new GenericEntity<List<StringValue>>(values){}).build();
    }

    @DELETE
    @Path("/poll/run")
    @Produces({MediaType.APPLICATION_JSON})
    public Response removeAllPoll() {
        logger.info("removeAllPoll() called");

        int count = pollFutureBean.cancelAll();

        logger.info(count + " poll task cancelled");

        LongValue value = new LongValue(new Long(count));

        return Response.ok().entity(value).build();
    }


    @DELETE
    @Path("/poll/run/{preparedId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response removeOnePoll(@PathParam("preparedId") String preparedId) {
        logger.info("removeOnePoll() called");

        int count = pollFutureBean.cancelByPreparedId(preparedId);

        logger.info(count + " poll task cancelled for preparedId: " + preparedId);

        LongValue value = new LongValue(new Long(count));

        return Response.ok().entity(value).build();
    }

    */

    private void onlyAllowAdmin(String icatUrl, String sessionId) throws TopcatException, MalformedURLException {
        if(icatUrl == null || sessionId == null || !icatClientService.isAdmin(icatUrl, sessionId)){
            throw new ForbiddenException("please provide a valid icatUrl and sessionId");
        }
    }

}
