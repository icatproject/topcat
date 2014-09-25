package org.icatproject.topcat.web.rest;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.icatproject.topcat.Constants;
import org.icatproject.topcat.domain.BooleanValue;
import org.icatproject.topcat.domain.LongValue;
import org.icatproject.topcat.domain.Pager;
import org.icatproject.topcat.domain.TDatafileFormat;
import org.icatproject.topcat.domain.TDataset;
import org.icatproject.topcat.domain.TDatasetType;
import org.icatproject.topcat.domain.TFacility;
import org.icatproject.topcat.domain.TFacilityCycle;
import org.icatproject.topcat.domain.TInstrument;
import org.icatproject.topcat.domain.TInvestigation;
import org.icatproject.topcat.domain.TInvestigationType;
import org.icatproject.topcat.domain.TParameterType;
import org.icatproject.topcat.domain.TopcatIcatServer;
import org.icatproject.topcat.domain.TopcatStringValue;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.repository.ServerRepository;
import org.icatproject.topcat.utils.PagerHelper;


@Stateless
@LocalBean
@Path("v1")
public class AdminResource {
    static final Logger logger = Logger.getLogger(AdminResource.class);

    @EJB
    private ServerRepository serverRepository;

    @EJB
    private ICATClientBean icatClientService;


    @GET
    @Path("/servers")
    //@JSONP(queryParam="callback")
    //@Produces({MediaType.APPLICATION_JSON, "application/x-javascript", "text/javascript"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getServers() {
        logger.info("getServers() called");
        List<TopcatIcatServer> servers = new ArrayList<TopcatIcatServer>();
        servers = serverRepository.getAllServers();

        return Response.ok().entity(new GenericEntity<List<TopcatIcatServer>>(servers){}).build();

    }


    @GET
    @Path("/servers/{serverName}/facilities")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFacilities(@PathParam("serverName") String serverName, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TFacility> facilities = icatClientService.getFacilities(serverName, icatSessionId);

        return Response.ok().entity(new GenericEntity<List<TFacility>>(facilities){}).build();
    }

    @GET
    @Path("/servers/{serverName}/facilities/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFacilityById(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        TFacility facility = icatClientService.getFacilityById(serverName, icatSessionId, id);

        if (facility == null) {
            throw new NotFoundException("Investigation not found");
        }

        return Response.ok().entity(facility).build();
    }

    @GET
    @Path("/servers/{serverName}/facilities/{id}/facility-cycles")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFacilityCyclesByFacilityId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TFacilityCycle> facilityCycles = icatClientService.getFacilityCyclesByFacilityId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TFacilityCycle>>(facilityCycles){}).build();
    }


    @GET
    @Path("/servers/{serverName}/facilities/{id}/dataset-types")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatasetTypesByFacilityId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TDatasetType> datasetTypes = icatClientService.getDatasetTypesByFacilityId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TDatasetType>>(datasetTypes){}).build();
    }


    @GET
    @Path("/servers/{serverName}/facilities/{id}/datafile-formats")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatafielFormatsByFacilityId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TDatafileFormat> tDatafileFormats = icatClientService.getDatafileFormatsByFacilityId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TDatafileFormat>>(tDatafileFormats){}).build();
    }


    @GET
    @Path("/servers/{serverName}/facilities/{id}/parameter-types")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getParameterTypesByFacilityId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TParameterType> tParameterTypes = icatClientService.getParameterTypesByFacilityId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TParameterType>>(tParameterTypes){}).build();
    }


    @GET
    @Path("/servers/{serverName}/facilities/{id}/investigation-types")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInvestigationTypesByFacilityId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TInvestigationType> tInvestigationTypes = icatClientService.getInvestigationTypesByFacilityId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TInvestigationType>>(tInvestigationTypes){}).build();
    }


    @GET
    @Path("/servers/{serverName}/facilities/{id}/instruments")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInstrumentsByfacilityId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TInstrument> tInstruments = icatClientService.getInstrumentsByfacilityId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TInstrument>>(tInstruments){}).build();
    }


    @GET
    @Path("/servers/{serverName}/instruments/{id}/investigations")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInvestigationsByInstrumentId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TInvestigation> tInvestigations = icatClientService.getInvestigationsByInstrumentId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TInvestigation>>(tInvestigations){}).build();
    }

    @GET
    @Path("/servers/{serverName}/instruments/{id}/investigations/{page}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getInvestigationsByInstrumentIdPaginated(
            @Context UriInfo uriInfo,
            @PathParam("serverName") String serverName,
            @PathParam("id") Long id, @PathParam("page") Integer page,
            @HeaderParam("icatSessionId") String icatSessionId,
            @QueryParam("sort") String sort,
            @QueryParam("order") String order)
            throws TopcatException, MalformedURLException {
        //deal with negative or 0 page numbers
        if (page < 1) {
            throw new NotFoundException("Page not found");
        }

        PagerHelper pagerHelper;

        try {
            pagerHelper = new PagerHelper(page, Constants.MAX_INVESTIGATIONS_PER_PAGE, TInvestigation.class, sort, order);
        } catch(IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        List<TInvestigation> tInvestigations = icatClientService.getInvestigationsByInstrumentIdPaginated(
                serverName, icatSessionId, id, pagerHelper.getOffset(),
                pagerHelper.getMaxPerPage(),
                pagerHelper.getSortOption(),
                pagerHelper.getOrderOption());

        Long count = icatClientService.getInvestigationsByInstrumentIdCount(serverName, icatSessionId, id);
        Pager<TInvestigation> pager = new Pager<TInvestigation>(tInvestigations, count, page, Constants.MAX_INVESTIGATIONS_PER_PAGE);

        return Response.ok().entity(pager).build();
    }


    @GET
    @Path("/servers/{serverName}/investigations/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInvestigationById(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        TInvestigation investigation = icatClientService.getInvestigationById(serverName, icatSessionId, id);

        if (investigation == null) {
            throw new NotFoundException("Investigation id " + id + " not found");
        }

        return Response.ok().entity(investigation).build();
    }


    @GET
    @Path("/servers/{serverName}/investigations/{id}/datasets")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatasetByInvestigationId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        List<TDataset> tDatasets = icatClientService.getDatasetsByInvestigationId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TDataset>>(tDatasets){}).build();
    }

    @GET
    @Path("/servers/{serverName}/investigations/{id}/datasets/{page}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getDatasetByInvestigationIdPaginated(
            @Context UriInfo uriInfo,
            @PathParam("serverName") String serverName,
            @PathParam("id") Long id, @PathParam("page") Integer page,
            @HeaderParam("icatSessionId") String icatSessionId,
            @QueryParam("sort") String sort,
            @QueryParam("order") String order)
            throws TopcatException, MalformedURLException {
        //deal with negative or 0 page numbers
        if (page < 1) {
            throw new NotFoundException("Page not found");
        }

        PagerHelper pagerHelper;

        try {
            pagerHelper = new PagerHelper(page, Constants.MAX_DATASETS_PER_PAGE, TDataset.class, sort, order);
        } catch(IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        List<TDataset> tDatasets = icatClientService.getDatasetsByInvestigationIdPaginated(
                serverName, icatSessionId, id, pagerHelper.getOffset(),
                pagerHelper.getMaxPerPage(),
                pagerHelper.getSortOption(),
                pagerHelper.getOrderOption());

        Long count = icatClientService.getDatasetsByInvestigationIdCount(serverName, icatSessionId, id);

        Pager<TDataset> pager = new Pager<TDataset>(tDatasets, count, page, Constants.MAX_DATASETS_PER_PAGE);

        return Response.ok().entity(pager).build();
    }


    @GET
    @Path("/servers/{serverName}/facility-cycles/{id}/investigations")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInvestigationByFacilityCycleId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws MalformedURLException, TopcatException {
        List<TInvestigation> tInvestigations = icatClientService.getInvestigationsByFacilityCycleId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TInvestigation>>(tInvestigations){}).build();
    }

    @GET
    @Path("/servers/{serverName}/facility-cycles/{id}/investigations/{page}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getInvestigationByFacilityCycleIdPaginated(
            @Context UriInfo uriInfo,
            @PathParam("serverName") String serverName,
            @PathParam("id") Long id, @PathParam("page") Integer page,
            @HeaderParam("icatSessionId") String icatSessionId,
            @QueryParam("sort") String sort,
            @QueryParam("order") String order)
            throws MalformedURLException, TopcatException {
        //deal with negative or 0 page numbers
        if (page < 1) {
            throw new NotFoundException("Page not found");
        }

        PagerHelper pagerHelper;

        try {
            pagerHelper = new PagerHelper(page, Constants.MAX_INVESTIGATIONS_PER_PAGE, TInvestigation.class, sort, order);
        } catch(IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        List<TInvestigation> tInvestigations = icatClientService.getInvestigationsByFacilityCycleIdPaginated(
                serverName, icatSessionId, id, pagerHelper.getOffset(),
                pagerHelper.getMaxPerPage(),
                pagerHelper.getSortOption(),
                pagerHelper.getOrderOption());

        Long count = icatClientService.getInvestigationsByFacilityCycleIdCount(serverName, icatSessionId, id);
        Pager<TInvestigation> pager = new Pager<TInvestigation>(tInvestigations, count, page, Constants.MAX_INVESTIGATIONS_PER_PAGE);

        return Response.ok().entity(pager).build();
    }


    @GET
    @Path("/servers/{serverName}/instruments/{id}/facility-cycles")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFacilityCycleByInstrumentId(@PathParam("serverName") String serverName, @PathParam("id") Long id, @HeaderParam("icatSessionId") String icatSessionId) throws MalformedURLException, TopcatException {
        List<TFacilityCycle> tFacilityCycle = icatClientService.getFacilityCycleByInstrumentId(serverName, icatSessionId, id);

        return Response.ok().entity(new GenericEntity<List<TFacilityCycle>>(tFacilityCycle){}).build();
    }


    @GET
    @Path("/servers/{serverName}/instruments/{id}/facility-cycles/{page}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getFacilityCycleByInstrumentIdPaginated(
            @Context UriInfo uriInfo,
            @PathParam("serverName") String serverName,
            @PathParam("id") Long id, @PathParam("page") Integer page,
            @HeaderParam("icatSessionId") String icatSessionId,
            @QueryParam("sort") String sort,
            @QueryParam("order") String order)
            throws TopcatException, MalformedURLException {

        //deal with negative or 0 page numbers
        if (page < 1) {
            throw new NotFoundException("Page not found");
        }

        //set default sort
        if (sort == null) {
            sort = "name";
        }

        //set default order
        if (order == null) {
            order = "desc";
        }

        PagerHelper pagerHelper;

        try {
            pagerHelper = new PagerHelper(page, Constants.MAX_FACILITYCYCLE_PER_PAGE, TFacilityCycle.class, sort, order);
        } catch(IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

        List<TFacilityCycle> tFacilityCycles = icatClientService.getFacilityCycleByInstrumentIdPaginated(
                serverName, icatSessionId, id, pagerHelper.getOffset(),
                pagerHelper.getMaxPerPage(),
                pagerHelper.getSortOption(),
                pagerHelper.getOrderOption());

        Long count = icatClientService.getFacilityCycleByInstrumentIdCount(serverName, icatSessionId, id);

        int totalPages = (int) Math.ceil(count / (double) pagerHelper.getMaxPerPage());

        if (pagerHelper.getPage() > totalPages) {
            throw new NotFoundException("Page not found");
        }

        Pager<TFacilityCycle> pager = new Pager<TFacilityCycle>(tFacilityCycles, count, pagerHelper.getPage(), pagerHelper.getMaxPerPage());

        return Response.ok().entity(pager).build();
    }




    @POST
    @Path("/servers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createServer(@Valid TopcatIcatServer server) {
        server = serverRepository.save(server);

        return Response.ok().entity(server).build();
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
        TopcatStringValue topcatIcatSession = new TopcatStringValue(icatSessionId);

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
