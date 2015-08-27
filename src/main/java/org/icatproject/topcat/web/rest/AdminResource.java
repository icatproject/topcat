package org.icatproject.topcat.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.LongValue;
import org.icatproject.topcat.domain.StringValue;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.repository.CartRepository;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.statuscheck.ExecuteCheck;
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
    private ExecuteCheck executeCheck;


    @GET
    @Path("/downloads/facility/{facilityName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDownloadsByFacilityName(
            @PathParam("facilityName") String facilityName,
            @QueryParam("status") String status,
            @QueryParam("transport") String transport,
            @QueryParam("preparedId") String preparedId) throws BadRequestException {
        logger.info("getDownloadsByFacilityName() called");

        //validate status
        if (status != null) {
            DownloadStatus downloadStatus = DownloadStatus.valueOf(status);

            if (downloadStatus == null) {
                throw new BadRequestException("Status must be RESTORING or COMPLETE");
            }
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", facilityName);
        params.put("status", status);
        params.put("transport", transport);
        params.put("preparedId", preparedId);



        List<Download> downloads = new ArrayList<Download>();
        downloads = downloadRepository.getDownloadsByFacilityName(params);

        return Response.ok().entity(new GenericEntity<List<Download>>(downloads){}).build();
    }


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
    @Path("/checkStatus")
    @Produces({MediaType.APPLICATION_JSON})
    public Response checkStatus(
            @QueryParam("facilityName") String facilityName,
            @QueryParam("transport") String transport,
            @QueryParam("preparedId") String preparedId) {
        logger.info("checkStatus() called");

        int count = executeCheck.run();

        LongValue value = new LongValue(new Long(count));

        return Response.ok().entity(value).build();
    }
}
