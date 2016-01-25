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
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.exceptions.ForbiddenException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.repository.DownloadRepository;
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
    private ICATClientBean icatClientService;

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
    @Path("/downloads")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDownloads(
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId,
        @QueryParam("queryOffset") String queryOffset)
        throws TopcatException, MalformedURLException, ParseException {

        onlyAllowAdmin(icatUrl, sessionId);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("queryOffset", queryOffset);

        List<Download> downloads = new ArrayList<Download>();
        downloads = downloadRepository.getDownloads(params);

        return Response.ok().entity(new GenericEntity<List<Download>>(downloads){}).build();
    }

    @PUT
    @Path("/download/{id}/status")
    @Produces({MediaType.APPLICATION_JSON})
    public Response setDownloadStatus(
        @PathParam("id") Long id,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("value") String value)
        throws TopcatException, MalformedURLException, ParseException {

        onlyAllowAdmin(icatUrl, sessionId);

        Download download = downloadRepository.getDownload(id);

        if(download == null){
            throw new NotFoundException("could not find download");
        }

        download.setStatus(DownloadStatus.valueOf(value));

        downloadRepository.save(download);

        return Response.ok().build();
    }
    
    @PUT
    @Path("/download/{id}/isDeleted")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteDownload(
        @PathParam("id") Long id,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("value") Boolean value)
        throws TopcatException, MalformedURLException, ParseException {

        onlyAllowAdmin(icatUrl, sessionId);

        Download download = downloadRepository.getDownload(id);

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

    private void onlyAllowAdmin(String icatUrl, String sessionId) throws TopcatException, MalformedURLException {
        if(icatUrl == null || sessionId == null || !icatClientService.isAdmin(icatUrl, sessionId)){
            throw new ForbiddenException("please provide a valid icatUrl and sessionId");
        }
    }

}
