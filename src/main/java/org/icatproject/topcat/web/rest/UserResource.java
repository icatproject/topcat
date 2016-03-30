package org.icatproject.topcat.web.rest;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.ParseException;

import javax.transaction.Transactional;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient.Flag;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.topcat.Constants;
import org.icatproject.topcat.domain.Cart;
import org.icatproject.topcat.domain.CartDTO;
import org.icatproject.topcat.domain.CartItem;
import org.icatproject.topcat.domain.CartSubmitDTO;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadItem;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.domain.EntityType;
import org.icatproject.topcat.domain.LongValue;
import org.icatproject.topcat.domain.ParentEntity;
import org.icatproject.topcat.domain.StringValue;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.ForbiddenException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.idsclient.IdsClientBean;
import org.icatproject.topcat.repository.CartRepository;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.utils.CartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Path("v1/user")
public class UserResource {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

    @EJB
    private DownloadRepository downloadRepository;

    @EJB
    private CartRepository cartRepository;

    @EJB
    private ICATClientBean icatClientService;

    @EJB
    private IdsClientBean idsClientService;

    @PersistenceContext(unitName="topcatv2")
    EntityManager em;

    /**
     * Returns a list of downloads associated with a particular sessionId filtered by a partial JPQL expression.
     *
     * @param icatUrl a url to a valid ICAT REST api.
     * 
     * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code> 
     *
     * @param queryOffset
     *  any JPQL expression that can be appended to "SELECT download from Download download",
     *  e.g. "where download.isDeleted = false". Note that like ICAT the syntax has been extended
     *  allowing (sql like) limit clauses in the form "limit [offset], [row count]" e.g. "limit 10, 20".
     *  Note the "status" attribute is an enum (not a string) i.e. org.icatproject.topcat.domain.Status
     *  with the following possible states: ONLINE, ARCHIVE or RESTORING. So an example query involving
     *  the status attribute could be "where download.status = org.icatproject.topcat.domain.Status.ARCHIVE limit 0, 10"
     *
     * @return returns an array of downloads in the form
     * [{"completedAt":"2016-03-18T16:02:36","createdAt":"2016-03-18T16:02:36","deletedAt":"2016-03-18T16:02:47","downloadItems":[{"entityId":18064,"entityType":"datafile","id":2},{"entityId":18061,"entityType":"datafile","id":3}],"email":"","facilityName":"test","fileName":"test_2016-3-18_16-05-59","icatUrl":"https://example.com","id":2,"isDeleted":false,"isTwoLevel":false,"preparedId":"6d3aaca5-da9f-4e6a-922d-eceeefcc07e0","status":"COMPLETE","transport":"https","transportUrl":"https://example.com","userName":"simple/root"}]
     */
    @GET
    @Path("/downloads")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDownloads(
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId,
        @QueryParam("queryOffset") String queryOffset)
        throws TopcatException, MalformedURLException, ParseException {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userName", icatClientService.getUserName(icatUrl, sessionId));
        params.put("queryOffset", queryOffset);

        List<Download> downloads = new ArrayList<Download>();
        downloads = downloadRepository.getDownloads(params);

        return Response.ok().entity(new GenericEntity<List<Download>>(downloads){}).build();
    }

    /**
     * Sets whether or not a download is deleted associated with a particular sessionId.
     *
     * @param icatUrl a url to a valid ICAT REST api.
     * 
     * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code> 
     *
     * @param id the download id in the database.
     *
     * @param value either true or false.
     */
    @PUT
    @Path("/download/{id}/isDeleted")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteDownload(
        @PathParam("id") Long id,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("value") Boolean value)
        throws TopcatException, MalformedURLException, ParseException {

        Download download = downloadRepository.getDownload(id);
        if(download == null){
            throw new NotFoundException("could not find download");
        }

        String userName = icatClientService.getUserName(icatUrl, sessionId);
        if(!download.getUserName().equals(userName)){
            throw new ForbiddenException("You do not have permission to delete this download");
        }

        download.setIsDeleted(value);
        if(value){
            download.setDeletedAt(new Date());
        }

        downloadRepository.save(download);

        return Response.ok().build();
    }

    /**
     * Returns the cart object associated with a particular sessionId and facility.
     *
     * @param icatUrl a url to a valid ICAT REST api.
     * 
     * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code> 
     *
     * @param facilityName the name of the facility e.g. 'dls'.
     *
     * @return returns the cart object in the form:
     * {"cartItems":[{"entityId":18178,"entityType":"datafile","id":1,"name":"tenenvironment.rhy","parentEntities":[{"entityId":182,"entityType":"investigation","id":1},{"entityId":1818,"entityType":"dataset","id":2}]},{"entityId":181,"entityType":"investigation","id":2,"name":"APPLIEDAHEAD","parentEntities":[]}],"createdAt":"2016-03-30T10:52:32","facilityName":"example","id":1,"updatedAt":"2016-03-30T10:52:32","userName":"simple/root"}
     */
    @GET
    @Path("/cart/{facilityName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCart(
        @PathParam("facilityName") String facilityName,
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId)
        throws TopcatException, MalformedURLException, ParseException {

        String userName = icatClientService.getUserName(icatUrl, sessionId);
        Cart cart = cartRepository.getCart(userName, facilityName);
        
        if(cart != null){
            em.refresh(cart);
            return Response.ok().entity(cart).build();
        } else {
            return emptyCart(facilityName, userName);
        }
    }

    /*
     * Adds items to the cart.
     *
     * @param icatUrl a url to a valid ICAT REST api.
     * 
     * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code> 
     *
     * @param facilityName the name of the facility e.g. 'dls'.
     *
     * @param items a list of entity type (i.e. datafile, dataset or investigation) and id pairs in the form:
     * investigation 2, datafile 1
     *
     * @return returns the cart object in the form:
     * {"cartItems":[{"entityId":18178,"entityType":"datafile","id":1,"name":"tenenvironment.rhy","parentEntities":[{"entityId":182,"entityType":"investigation","id":1},{"entityId":1818,"entityType":"dataset","id":2}]},{"entityId":181,"entityType":"investigation","id":2,"name":"APPLIEDAHEAD","parentEntities":[]}],"createdAt":"2016-03-30T10:52:32","facilityName":"example","id":1,"updatedAt":"2016-03-30T10:52:32","userName":"simple/root"}
     */
    @POST
    @Path("/cart/{facilityName}/cartItems")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addCartItems(
        @PathParam("facilityName") String facilityName,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("items") String items)
        throws TopcatException, MalformedURLException, ParseException {

        String userName = icatClientService.getUserName(icatUrl, sessionId);
        Cart cart = cartRepository.getCart(userName, facilityName);

        if(cart == null){
            cart = new Cart();
            cart.setFacilityName(facilityName);
            cart.setUserName(userName);
            em.persist(cart);
            em.flush();
        }

        Map<String, List<Long>> entityTypeEntityIds = new HashMap<String, List<Long>>();
        entityTypeEntityIds.put("investigation", new ArrayList<Long>());
        entityTypeEntityIds.put("dataset", new ArrayList<Long>());
        entityTypeEntityIds.put("datafile", new ArrayList<Long>());

        for(String item : items.split("\\s*,\\s*")){
            String[] pair = item.split("\\s+");
            if(pair.length == 2){
                String entityType = pair[0];
                Long entityId = Long.parseLong(pair[1]);
                List<Long> entityIds = entityTypeEntityIds.get(entityType);
                boolean isAlreadyInCart = false;

                for(CartItem cartItem : cart.getCartItems()){
                    if(cartItem.getEntityType().equals(EntityType.valueOf(entityType)) && cartItem.getEntityId().equals(entityId)){
                        isAlreadyInCart = true;
                        break;
                    }
                }

                if(!isAlreadyInCart){
                    entityIds.add(entityId);
                }
            }
        }

        for(CartItem cartItem : icatClientService.getCartItems(icatUrl, sessionId, entityTypeEntityIds)){
            cartItem.setCart(cart);
            em.persist(cartItem);
            for(ParentEntity parentEntity : cartItem.getParentEntities()){
                parentEntity.setCartItem(cartItem);
                em.persist(parentEntity);
            }
        }

        em.flush();
        em.refresh(cart);
        
        return Response.ok().entity(cart).build();
    }


    @DELETE
    @Path("/cart/{facilityName}/cartItems")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteCartItems(
        @PathParam("facilityName") String facilityName,
        @QueryParam("icatUrl") String icatUrl,
        @QueryParam("sessionId") String sessionId,
        @QueryParam("items") String items)
        throws TopcatException, MalformedURLException, ParseException {

        String userName = icatClientService.getUserName(icatUrl, sessionId);
        Cart cart = cartRepository.getCart(userName, facilityName);
        if(cart == null){
            return emptyCart(facilityName, userName);
        }

        if(items.equals("*")){
            for(CartItem cartItem : cart.getCartItems()){
                em.remove(cartItem);
            }
        } else {
            for(String item : items.split("\\s*,\\s*")){
                String[] pair = item.split("\\s+");
                
                if(pair.length > 1){
                    String entityType = pair[0];
                    Long entityId = Long.parseLong(pair[1]);

                    for(CartItem cartItem : cart.getCartItems()){
                        boolean entityTypesMatch = cartItem.getEntityType().equals(EntityType.valueOf(entityType));
                        boolean entityIdsMatch = cartItem.getEntityId().equals(entityId);
                        if(entityTypesMatch && entityIdsMatch){
                            em.remove(cartItem);
                        }
                    }
                } else {
                    Long id = Long.parseLong(pair[0]);
                    for(CartItem cartItem : cart.getCartItems()){
                        if(cartItem.getId().equals(id)){
                            em.remove(cartItem);
                            break;
                        }
                    }
                }
            }
        }

        em.flush();
        em.refresh(cart);

        if(cart.getCartItems().size() == 0){
            em.remove(cart);
            em.flush();
            return emptyCart(facilityName, userName);
        }

        return Response.ok().entity(cart).build();
    }

    @POST
    @Path("/cart/{facilityName}/submit")
    @Produces({MediaType.APPLICATION_JSON})
    public Response submitCart(
        @PathParam("facilityName") String facilityName,
        @FormParam("icatUrl") String icatUrl,
        @FormParam("sessionId") String sessionId,
        @FormParam("transport") String transport,
        @FormParam("transportUrl") String transportUrl,
        @FormParam("email") String email,
        @FormParam("fileName") String fileName,
        @FormParam("zipType") String zipType)
        throws TopcatException, MalformedURLException, ParseException {

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BadRequestException("fileName is required");
        }

        if (transport == null || transport.trim().isEmpty()) {
            throw new BadRequestException("transport is required");
        }

        String userName = icatClientService.getUserName(icatUrl, sessionId);
        Cart cart = cartRepository.getCart(userName, facilityName);
        String fullName = icatClientService.getFullName(icatUrl, sessionId);
        Long downloadId = null;
        
        if(cart != null){
            em.refresh(cart);
            DataSelection dataSelection = cartToDataSelection(cart);
            String preparedId = idsClientService.prepareData(transportUrl, sessionId, dataSelection, getZipFlag(zipType));

            if (preparedId != null) {
                Download download = new Download();
                download.setPreparedId(preparedId);
                download.setFacilityName(cart.getFacilityName());
                download.setFileName(fileName);
                download.setUserName(cart.getUserName());
                download.setFullName(fullName);
                download.setTransport(transport);
                download.setTransportUrl(transportUrl);
                download.setIcatUrl(icatUrl);
                download.setEmail(email);
                boolean isTwoLevel = idsClientService.isTwoLevel(transportUrl);
                download.setIsTwoLevel(isTwoLevel);

                Status status = idsClientService.getStatus(transportUrl, sessionId, dataSelection);
                if(status != Status.ONLINE){
                    download.setStatus(DownloadStatus.RESTORING);
                } else {
                    download.setStatus(DownloadStatus.COMPLETE);
                    download.setCompletedAt(new Date());
                }

                List<DownloadItem> downloadItems = new ArrayList<DownloadItem>();

                for(CartItem cartItem : cart.getCartItems()) {
                    DownloadItem downloadItem = new DownloadItem();
                    downloadItem.setEntityId(cartItem.getEntityId());
                    downloadItem.setEntityType(cartItem.getEntityType());
                    downloadItem.setDownload(download);
                    downloadItems.add(downloadItem);
                }

                download.setDownloadItems(downloadItems);

                try {
                    em.persist(download);
                    em.flush();
                    em.refresh(download);
                    downloadId = download.getId();
                    em.remove(cart);
                    em.flush();
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                    throw new BadRequestException("Unable to submit for cart for download");
                }

            } else {
                throw new BadRequestException("Unable to submit for cart for download");
            }
           
        }

        return emptyCart(facilityName, userName, downloadId);
    }

    private Response emptyCart(String facilityName, String userName, Long downloadId){
        JsonObjectBuilder emptyCart = Json.createObjectBuilder()
            .add("facilityName", facilityName)
            .add("userName", userName)
            .add("cartItems", Json.createArrayBuilder().build());
            
            if(downloadId != null){
                emptyCart.add("downloadId", downloadId);
            }

        return Response.ok().entity(emptyCart.build().toString()).build();
    }

    private Response emptyCart(String facilityName, String userName){
        return emptyCart(facilityName, userName, null);
    }



    private Flag getZipFlag(String zip) {
        if (zip == null ) {
            return Flag.ZIP;
        }
        zip = zip.toUpperCase();
        if(zip.equals("ZIP_AND_COMPRESS")) {
            return Flag.ZIP_AND_COMPRESS;
        } else {
            return Flag.ZIP;
        }
    }

    private DataSelection cartToDataSelection(Cart cart) {
        DataSelection dataSelection = new DataSelection();

        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getEntityType() == EntityType.investigation) {
                dataSelection.addInvestigation(cartItem.getEntityId());
            }

            if (cartItem.getEntityType() == EntityType.dataset) {
                dataSelection.addDataset(cartItem.getEntityId());
            }

            if (cartItem.getEntityType() == EntityType.datafile) {
                dataSelection.addDatafile(cartItem.getEntityId());
            }
        }

        return dataSelection;
    }
    

}
