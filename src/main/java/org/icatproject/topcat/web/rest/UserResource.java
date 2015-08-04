package org.icatproject.topcat.web.rest;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient;
import org.icatproject.topcat.domain.Cart;
import org.icatproject.topcat.domain.CartDTO;
import org.icatproject.topcat.domain.CartItem;
import org.icatproject.topcat.domain.CartSubmitDTO;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadItem;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.domain.LongValue;
import org.icatproject.topcat.domain.ParentEntity;
import org.icatproject.topcat.domain.StringValue;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.idsclient.IdsClientBean;
import org.icatproject.topcat.repository.CartRepository;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.utils.CartUtils;

@Stateless
@LocalBean
@Path("v1")
public class UserResource {
    static final Logger logger = Logger.getLogger(UserResource.class);

    @EJB
    private DownloadRepository downloadRepository;

    @EJB
    private CartRepository cartRepository;

    @EJB
    private ICATClientBean icatClientService;

    @EJB
    private IdsClientBean idsClientService;


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


    @GET
    @Path("/cart/facility/{facilityName}/user/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCartByFacilityNameAndUser(
            @PathParam("facilityName") String facilityName,
            @PathParam("userName") String userName,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("icatUrl") String icatUrl) throws MalformedURLException, TopcatException {
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

        Cart cart = cartRepository.getCartByFacilityNameAndUser(params);

        if (cart == null) {
            logger.debug("cart is null");
            return Response.ok().entity("{}").build();
        }

        logger.debug("cart:" + cart.toString());

        return Response.ok().entity(cart).build();
    }

    @DELETE
    @Path("/cart/facility/{facilityName}/user/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response removeCartByFacilityNameAndUser(
            @PathParam("facilityName") String facilityName,
            @PathParam("userName") String userName,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("icatUrl") String icatUrl) throws MalformedURLException, TopcatException {
        logger.info("removeCartByFacilityNameAndUser() called");

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

        int deletedCount = cartRepository.removeCartByFacilityNameAndUser(params);

        LongValue result = new LongValue(new Long(deletedCount));

        logger.debug(deletedCount + " cart belonging to " + userName + " on facility " + facilityName + " was removed");

        return Response.ok().entity(result).build();
    }


    @POST
    @Path("/cart/submit")
    @Produces({MediaType.APPLICATION_JSON})
    public Response submitCart(CartSubmitDTO cartSubmitDTO) throws MalformedURLException, TopcatException {
        logger.info("submitCart() called");

        //validate. Sould use bean validation but don't know how to return json error messages
        if (cartSubmitDTO.getSessionId() == null) {
            throw new BadRequestException("sessionId is required");
        }

        if (cartSubmitDTO.getIcatUrl() == null) {
            throw new BadRequestException("icatUrl is required");
        }

        if (cartSubmitDTO.getFacilityName() == null || cartSubmitDTO.getFacilityName().isEmpty()) {
            throw new BadRequestException("facilityName is required");
        }

        if (cartSubmitDTO.getFileName() == null || cartSubmitDTO.getFileName().trim().isEmpty()) {
            throw new BadRequestException("fileName is required");
        }

        /*if (cartSubmitDTO.getStatus() == null) {
            throw new BadRequestException("status is required and must be ONLINE, ARCHIVED");
        }*/

        if (cartSubmitDTO.getTransport() == null || cartSubmitDTO.getTransport().trim().isEmpty()) {
            throw new BadRequestException("transport is required");
        }

        //check user is authorised
        logger.info("check user is authorised");
        boolean auth = icatClientService.isSessionValid(cartSubmitDTO.getIcatUrl(), cartSubmitDTO.getSessionId());

        if (! auth) {
            throw new AuthenticationException("sessionId not valid");
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", cartSubmitDTO.getFacilityName());
        params.put("userName", cartSubmitDTO.getUserName());
        params.put("sessionId", cartSubmitDTO.getSessionId());
        params.put("icatUrl", cartSubmitDTO.getIcatUrl());

        Cart cart = cartRepository.getCartByFacilityNameAndUser(params);

        if (cart == null) {
            throw new BadRequestException("There is no cart found for " + cartSubmitDTO.getUserName() + " on the facility " + cartSubmitDTO.getFacilityName());
        }

        Download download = new Download();
        download.setFacilityName(cartSubmitDTO.getFacilityName());
        download.setFileName(cartSubmitDTO.getFileName());
        download.setUserName(cartSubmitDTO.getUserName());
        download.setTransport(cartSubmitDTO.getTransport());
        download.setTransportUrl(cartSubmitDTO.getTransportUrl());
        download.setEmail(cartSubmitDTO.getEmail());

        List<DownloadItem> downloadItems = new ArrayList<DownloadItem>();

        for(CartItem cartItem : cart.getCartItems()) {
            DownloadItem downloadItem = new DownloadItem();
            downloadItem.setEntityId(cartItem.getEntityId());
            downloadItem.setEntityType(cartItem.getEntityType());
            downloadItem.setDownload(download);

            downloadItems.add(downloadItem);
        }

        download.setDownloadItems(downloadItems);

        DataSelection dataSelection =  CartUtils.cartToDataSelection(cart);

        logger.info("Send prepareData request to " + cartSubmitDTO.getTransportUrl());
        String preparedId = idsClientService.prepareData(cartSubmitDTO.getTransportUrl(), cartSubmitDTO.getSessionId(), dataSelection, IdsClient.Flag.ZIP_AND_COMPRESS);
        logger.info("Returned prepareId " + preparedId);

        if (preparedId != null) {
            download.setPreparedId(preparedId);
            download.setStatus(DownloadStatus.RESTORING);

            try {
                downloadRepository.save(download);
            } catch (Exception e) {
                logger.debug(e.getMessage());

                throw new BadRequestException("Unable to submit for cart for download");
            }
        } else {
            throw new BadRequestException("Unable to submit for cart for download");
        }

        StringValue value = new StringValue(preparedId);

        return Response.ok().entity(value).build();
    }




    @POST
    @Path("/cart")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public Response createCart(CartDTO cartDTO) throws MalformedURLException, TopcatException {
        logger.info("getDownloadsByFacilityNameAndUser() called");

        if (cartDTO.getSessionId() == null) {
            throw new BadRequestException("sessionId query parameter is required");
        }

        if (cartDTO.getIcatUrl() == null) {
            throw new BadRequestException("icatUrl query parameter is required");
        }

        //check user is authorised
        boolean auth = icatClientService.isSessionValid(cartDTO.getIcatUrl(), cartDTO.getSessionId());

        if (! auth) {
            throw new AuthenticationException("sessionId not valid");
        }

        Cart cart = new Cart();
        cart.setFacilityName(cartDTO.getFacilityName());
        cart.setUserName(cartDTO.getUserName());
        cart.setSize(cartDTO.getSize());
        cart.setAvailability(cartDTO.getAvailability());

        List<CartItem> cartItems = new ArrayList<CartItem>();

        for(CartItem cartItem : cartDTO.getCartItems()) {
            CartItem newCartItem = new CartItem();
            newCartItem.setEntityId(cartItem.getEntityId());
            newCartItem.setName(cartItem.getName());
            newCartItem.setEntityType(cartItem.getEntityType());
            newCartItem.setCart(cart);

            List<ParentEntity> newParentEntities = new ArrayList<ParentEntity>();
            //parent entities
            for(ParentEntity parentEntity : cartItem.getParentEntities()) {
                ParentEntity newParentEnity = new ParentEntity();
                newParentEnity.setEntityId(parentEntity.getEntityId());
                newParentEnity.setEntityType(parentEntity.getEntityType());
                newParentEnity.setCartItem(newCartItem);

                newParentEntities.add(newParentEnity);
            }

            newCartItem.setParentEntities(newParentEntities);

            cartItems.add(newCartItem);
        }

        cart.setCartItems(cartItems);

        cart = cartRepository.save(cart);

        LongValue preparedId = new LongValue(cart.getId());

        return Response.ok().entity(preparedId).build();
    }



}
