package org.icatproject.topcat.web.rest;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import javax.json.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.DefaultValue;

import org.icatproject.topcat.domain.*;
import org.icatproject.topcat.exceptions.*;
import org.icatproject.topcat.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcat.IdsClient;
import org.icatproject.topcat.FacilityMap;
import org.icatproject.topcat.IcatClient;
import org.icatproject.topcat.Properties;

@Stateless
@LocalBean
@Path("user")
public class UserResource {

	private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
	

	@EJB
	private DownloadRepository downloadRepository;

	@EJB
	private DownloadTypeRepository downloadTypeRepository;

	@EJB
	private CartRepository cartRepository;

	@EJB
	private CacheRepository cacheRepository;

	private String anonUserName;

	@PersistenceContext(unitName = "topcat")
	EntityManager em;

	public UserResource() {
		Properties properties = Properties.getInstance();
		this.anonUserName = properties.getProperty("anonUserName", "");
    }

	/**
	 * Returns the cart userName, which is either the ICAT userName if the user isn't anonUserName,
	 * or it's the ICAT userName plus the sessionId if it is the anon user name
	 */
	private String getCartUserName(String userName, String sessionId) {
		if (userName.equals(this.anonUserName)) {
			return userName + "/" + sessionId;
		} else {
			return userName;
		}
	}

	/**
	 * Returns a list of downloads associated with a particular sessionId
	 * filtered by a partial JPQL expression.
	 *
	 * @summary getDownloads
	 *
	 * @param facilityName
	 *            a facility name - properties must map this to a url to a valid ICAT REST api.
	 * 
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @param queryOffset
	 *            any JPQL expression (*) that can be appended to
	 *            "SELECT download from Download download", e.g.
	 *            "where download.isDeleted = false". Note that like ICAT the
	 *            syntax has been extended allowing (sql like) limit clauses in
	 *            the form "limit [offset], [row count]" e.g. "limit 10, 20".
	 *            Note the "status" attribute is an enum (not a string) i.e.
	 *            org.icatproject.topcat.domain.Status with the following
	 *            possible states: 'ONLINE', 'ARCHIVE' or 'RESTORING'. So an
	 *            example query involving the status attribute could be
	 *            "where download.status = org.icatproject.topcat.domain.Status.ARCHIVE limit 0, 10"
	 *            (*) Note: the expression must not contain closing brackets (")").
	 *
	 * @return returns an array of downloads in the form
	 *         [{"completedAt":"2016-03-18T16:02:36","createdAt":
	 *         "2016-03-18T16:02:36","deletedAt":"2016-03-18T16:02:47",
	 *         "downloadItems":[{"entityId":18064,"entityType":"datafile","id":2
	 *         },{"entityId":18061,"entityType":"datafile","id":3}],"email":"",
	 *         "facilityName":"test","fileName":"test_2016-3-18_16-05-59",
	 *         "id":2,"isDeleted":false,
	 *         "isTwoLevel":false,"preparedId":
	 *         "6d3aaca5-da9f-4e6a-922d-eceeefcc07e0","status":"COMPLETE",
	 *         "size":324675,"transport":"https",
	 *         "userName":"simple/root"}]
	 *
	 * @throws MalformedURLException
	 *             if facilityName is invalid.
	 *
	 * @throws ParseException
	 *             if a JPQL query is malformed.
	 * 
	 * @throws TopcatException
	 *             if anything else goes wrong.
	 */
	@GET
	@Path("/downloads")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDownloads(@QueryParam("facilityName") String facilityName, @QueryParam("sessionId") String sessionId,
			@QueryParam("queryOffset") String queryOffset)
			throws TopcatException, MalformedURLException, ParseException {

		String icatUrl = getIcatUrl( facilityName );
		IcatClient icatClient = new IcatClient(icatUrl, sessionId);

		Map<String, Object> params = new HashMap<String, Object>();
		// Note: we believe that userName can never be null/empty
		String cartUserName = getCartUserName(icatClient.getUserName(), sessionId);
		params.put("userName", cartUserName);
		params.put("queryOffset", queryOffset);

		List<Download> downloads = new ArrayList<Download>();
		downloads = downloadRepository.getDownloads(params);

		return Response.ok().entity(new GenericEntity<List<Download>>(downloads) {
		}).build();
	}

	/**
	 * Sets whether or not a download is deleted associated with a particular
	 * sessionId.
	 *
	 * @summary deleteDownload
	 *
	 * @param facilityName
	 *            a facility name - properties must map this to a url to a valid ICAT REST api.
	 * 
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @param id
	 *            the download id in the database.
	 *
	 * @param value
	 *            either true or false.
	 *
	 * @return an empty Response
	 * 
	 * @throws MalformedURLException
	 *             if facilityName is invalid.
	 *
	 * @throws ParseException
	 *             if a JPQL query is malformed.
	 * 
	 * @throws TopcatException
	 *             if anything else goes wrong.
	 */
	@PUT
	@Path("/download/{id}/isDeleted")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteDownload(@PathParam("id") Long id, @FormParam("facilityName") String facilityName,
			@FormParam("sessionId") String sessionId, @FormParam("value") Boolean value)
			throws TopcatException, MalformedURLException, ParseException {

		Download download = downloadRepository.getDownload(id);
		if (download == null) {
			throw new NotFoundException("could not find download");
		}

		String icatUrl = getIcatUrl( facilityName );
		IcatClient icatClient = new IcatClient(icatUrl, sessionId);

		String userName = icatClient.getUserName();
		String cartUserName = getCartUserName(userName, sessionId);
		if (!download.getUserName().equals(cartUserName)) {
			throw new ForbiddenException("you do not have permission to delete this download");
		}

		download.setIsDeleted(value);
		if (value) {
			download.setDeletedAt(new Date());
		}

		downloadRepository.save(download);

		return Response.ok().build();
	}

	/**
     * Sets the download status.
     *
     * @summary setDownloadStatus
     *
	 * @param facilityName
	 *            a facility name - properties must map this to a url to a valid ICAT REST api.
     * 
     * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code> 
     *
     * @param id the download id in the database.
     *
     * @param value the status value i.e. 'ONLINE', 'ARCHIVE' or 'RESTORING'.
     *
     * @return an empty Response
     * 
     * @throws MalformedURLException if facilityName is invalid.
     *
     * @throws ParseException if a JPQL query is malformed.
     * 
     * @throws TopcatException if anything else goes wrong.
     */
    @PUT
    @Path("/download/{id}/status")
    @Produces({MediaType.APPLICATION_JSON})
    public Response setDownloadStatus(
        @PathParam("id") Long id,
        @FormParam("facilityName") String facilityName,
        @FormParam("sessionId") String sessionId,
        @FormParam("value") String value)
        throws TopcatException, MalformedURLException, ParseException {

        Download download = downloadRepository.getDownload(id);

        if(download == null){
            throw new NotFoundException("could not find download");
        }

        String icatUrl = getIcatUrl( facilityName );
        IcatClient icatClient = new IcatClient(icatUrl, sessionId);

		String userName = icatClient.getUserName();
		String cartUserName = getCartUserName(userName, sessionId);
		if (!download.getUserName().equals(cartUserName)) {
			throw new ForbiddenException("you do not have permission to delete this download");
		}

        download.setStatus(DownloadStatus.valueOf(value));
        if(value.equals("COMPLETE")){
            download.setCompletedAt(new Date());
        }

        downloadRepository.save(download);

        return Response.ok().build();
	}

	/**
	 * Returns the cart object associated with a particular sessionId and
	 * facility.
	 *
	 * @summary getCart
	 *
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @param facilityName
	 *            the name of the facility e.g. 'dls'.
	 *            properties must map this to a url to a valid ICAT REST api.
	 *
	 * @return returns the cart object in the form:
	 *         {"cartItems":[{"entityId":18178,"entityType":"datafile","id":1,
	 *         "name":"tenenvironment.rhy","parentEntities":[{"entityId":182,
	 *         "entityType":"investigation","id":1},{"entityId":1818,
	 *         "entityType":"dataset","id":2}]},{"entityId":181,"entityType":
	 *         "investigation","id":2,"name":"APPLIEDAHEAD","parentEntities":[]}
	 *         ],"createdAt":"2016-03-30T10:52:32","facilityName":"example","id"
	 *         :1,"updatedAt":"2016-03-30T10:52:32","userName":"simple/root"}
	 *
	 * @throws MalformedURLException
	 *             if facilityName is invalid.
	 *
	 * @throws ParseException
	 *             if a JPQL query is malformed.
	 * 
	 * @throws TopcatException
	 *             if anything else goes wrong.
	 */
	@GET
	@Path("/cart/{facilityName}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getCart(@PathParam("facilityName") String facilityName, 
			@QueryParam("sessionId") String sessionId) throws TopcatException, MalformedURLException, ParseException {

        String icatUrl = getIcatUrl( facilityName );
		IcatClient icatClient = new IcatClient(icatUrl, sessionId);

		String userName = icatClient.getUserName();
		String cartUserName = getCartUserName(userName, sessionId);
		Cart cart = cartRepository.getCart(cartUserName, facilityName);

		if (cart != null) {
			em.refresh(cart);
			return Response.ok().entity(cart).build();
		} else {
			return emptyCart(facilityName, cartUserName);
		}
	}

	/**
	 * Adds items to the cart associated with a particular sessionId and
	 * facility.
	 *
	 * @summary addCartItems
	 *
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @param facilityName
	 *            the name of the facility e.g. 'dls'.
	 *            Properties must map this to a url to a valid ICAT REST api.
	 *
	 * @param items
	 *            a list of entity type (i.e. datafile, dataset or
	 *            investigation) and entity id pairs in the form: investigation
	 *            2, datafile 1
	 *
	 * @param remove
	 *            flag to determine whether the request should be used to remove
	 *            items from the cart or not. If set to true, the items given in
	 *            the request will be removed from the cart (equivalent to the
	 *            DELETE endpoint). The default is to add to the cart (i.e.
	 *            false)
	 *
	 * @return returns the cart object in the form:
	 *         {"cartItems":[{"entityId":18178,"entityType":"datafile","id":1,
	 *         "name":"tenenvironment.rhy","parentEntities":[{"entityId":182,
	 *         "entityType":"investigation","id":1},{"entityId":1818,
	 *         "entityType":"dataset","id":2}]},{"entityId":181,"entityType":
	 *         "investigation","id":2,"name":"APPLIEDAHEAD","parentEntities":[]}
	 *         ],"createdAt":"2016-03-30T10:52:32","facilityName":"example","id"
	 *         :1,"updatedAt":"2016-03-30T10:52:32","userName":"simple/root"}
	 *
	 * @throws MalformedURLException
	 *             if facilityName is invalid.
	 *
	 * @throws ParseException
	 *             if a JPQL query is malformed.
	 * 
	 * @throws TopcatException
	 *             if anything else goes wrong.
	 */
	@POST
	@Path("/cart/{facilityName}/cartItems")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response addCartItems(@PathParam("facilityName") String facilityName, 
			@FormParam("sessionId") String sessionId, @FormParam("items") String items, @DefaultValue("false") @FormParam("remove") Boolean remove)
			throws TopcatException, MalformedURLException, ParseException {

		logger.info("addCartItems() called");

		if (remove == true) {
			logger.info("Calling deleteCartItems() from addCartItems()");
			Response deleteCartResponse = this.deleteCartItems(facilityName, sessionId, items);
			return deleteCartResponse;
		}

		String icatUrl = getIcatUrl( facilityName );
		IcatClient icatClient = new IcatClient(icatUrl, sessionId);

		String userName = icatClient.getUserName();
		String cartUserName = getCartUserName(userName, sessionId);
		Cart cart = cartRepository.getCart(cartUserName, facilityName);

		if (cart == null) {
			cart = new Cart();
			cart.setFacilityName(facilityName);
			cart.setUserName(cartUserName);
			em.persist(cart);
			em.flush();
		}

		Map<Long, Boolean> isInvestigationIdIndex = new HashMap<Long, Boolean>();
        Map<Long, Boolean> isDatasetIdIndex = new HashMap<Long, Boolean>();
        Map<Long, Boolean> isDatafileIdIndex = new HashMap<Long, Boolean>();

        for(CartItem cartItem : cart.getCartItems()){
            if(cartItem.getEntityType().equals(EntityType.valueOf("investigation"))){
                isInvestigationIdIndex.put(cartItem.getEntityId(), true);
            } else if(cartItem.getEntityType().equals(EntityType.valueOf("dataset"))){
                isDatasetIdIndex.put(cartItem.getEntityId(), true);
            } else {
            	isDatafileIdIndex.put(cartItem.getEntityId(), true);
            }
        }

		List<Long> investigationIdsToAdd = new ArrayList<Long>();
		List<Long> datasetIdsToAdd = new ArrayList<Long>();
		List<Long> datafileIdsToAdd = new ArrayList<Long>();


		for (String item : items.split("\\s*,\\s*")) {
			String[] pair = item.split("\\s+");
			if (pair.length == 2) {
				String entityType = pair[0];
				Long entityId = Long.parseLong(pair[1]);
				if(entityType.equals("investigation") && isInvestigationIdIndex.get(entityId) != null){
					continue;
				} else if(entityType.equals("dataset") && isDatasetIdIndex.get(entityId) != null){
					continue;
				} else if(entityType.equals("datafile") && isDatafileIdIndex.get(entityId) != null){
					continue;
				}

				if(entityType.equals("investigation")){
					investigationIdsToAdd.add(entityId);
					isInvestigationIdIndex.put(entityId, true);
				} else if(entityType.equals("dataset")){
					datasetIdsToAdd.add(entityId);
					isDatasetIdIndex.put(entityId, true);
				} else {
					datafileIdsToAdd.add(entityId);
				}
			}
		}

		addEntitiesToCart(icatClient, cart, "investigation", investigationIdsToAdd);
		addEntitiesToCart(icatClient, cart, "dataset", datasetIdsToAdd);
		addEntitiesToCart(icatClient, cart, "datafile", datafileIdsToAdd);

		em.flush();
		em.refresh(cart);

		//remove any entities that have a parent added to the cart

        for(CartItem cartItem : cart.getCartItems()){
            for(ParentEntity parentEntity : cartItem.getParentEntities()){
                if(parentEntity.getEntityType().equals(EntityType.valueOf("investigation")) && isInvestigationIdIndex.get(parentEntity.getEntityId()) != null){
                    em.remove(cartItem);
                    break;
                } else if(parentEntity.getEntityType().equals(EntityType.valueOf("dataset")) && isDatasetIdIndex.get(parentEntity.getEntityId()) != null){
                    em.remove(cartItem);
                    break;
                }
            }
        }

		em.flush();
		em.refresh(cart);

		return Response.ok().entity(cart).build();
	}


	private void addEntitiesToCart(IcatClient icatClient, Cart cart, String entityType, List<Long> entityIds) throws TopcatException {
		if(entityIds.size() == 0){
			return;
		}	

		for (JsonObject entity : icatClient.getEntities(entityType, entityIds)) {
			String name = entity.getString("name");
			Long entityId = Long.valueOf(entity.getJsonNumber("id").longValue());

			CartItem cartItem = new CartItem();
			cartItem.setCart(cart);
			cartItem.setEntityType(EntityType.valueOf(entityType));
			cartItem.setEntityId(entityId);
			cartItem.setName(name);
			em.persist(cartItem);


			if (entityType.equals("datafile")) {
				ParentEntity parentEntity = new ParentEntity();
				parentEntity.setCartItem(cartItem);
				parentEntity.setEntityType(EntityType.valueOf("dataset"));
				parentEntity.setEntityId(Long.valueOf(entity.getJsonObject("dataset").getJsonNumber("id").longValue()));
				cartItem.getParentEntities().add(parentEntity);
				em.persist(parentEntity);

				parentEntity = new ParentEntity();
				parentEntity.setEntityType(EntityType.valueOf("investigation"));
				parentEntity.setEntityId(Long.valueOf(entity.getJsonObject("dataset").getJsonObject("investigation").getJsonNumber("id").longValue()));
				cartItem.getParentEntities().add(parentEntity);
				em.persist(parentEntity);

			} else if (entityType.equals("dataset")) {
				ParentEntity parentEntity = new ParentEntity();
				parentEntity.setEntityType(EntityType.valueOf("investigation"));
				parentEntity.setEntityId(Long.valueOf(entity.getJsonObject("investigation").getJsonNumber("id").longValue()));
				cartItem.getParentEntities().add(parentEntity);
				em.persist(parentEntity);
			}
		}
	}

	/**
	 * Deletes items from the cart associated with a particular sessionId and
	 * facility.
	 *
	 * @summary deleteCartItems
	 *
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @param facilityName
	 *            the name of the facility e.g. 'dls'.
	 *            Properties must map this to a url to a valid ICAT REST api.
	 *
	 * @param items
	 *            a list of entity type (i.e. datafile, dataset or
	 *            investigation) and entity id pairs, in the form: investigation
	 *            2, datafile 1. Or a list cart item ids in the form: 45, 56.
	 * 
	 * @return returns the cart object in the form:
	 *         {"cartItems":[{"entityId":18178,"entityType":"datafile","id":1,
	 *         "name":"tenenvironment.rhy","parentEntities":[{"entityId":182,
	 *         "entityType":"investigation","id":1},{"entityId":1818,
	 *         "entityType":"dataset","id":2}]},{"entityId":181,"entityType":
	 *         "investigation","id":2,"name":"APPLIEDAHEAD","parentEntities":[]}
	 *         ],"createdAt":"2016-03-30T10:52:32","facilityName":"example","id"
	 *         :1,"updatedAt":"2016-03-30T10:52:32","userName":"simple/root"}
	 *
	 * @throws MalformedURLException
	 *             if facilityName is invalid.
	 *
	 * @throws ParseException
	 *             if a JPQL query is malformed.
	 * 
	 * @throws TopcatException
	 *             if anything else goes wrong.
	 */
	@DELETE
	@Path("/cart/{facilityName}/cartItems")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteCartItems(@PathParam("facilityName") String facilityName,
			@QueryParam("sessionId") String sessionId,
			@QueryParam("items") String items) throws TopcatException, MalformedURLException, ParseException {

		String icatUrl = getIcatUrl( facilityName );
		IcatClient icatClient = new IcatClient(icatUrl, sessionId);

		String userName = icatClient.getUserName();
		String cartUserName = getCartUserName(userName, sessionId);
		Cart cart = cartRepository.getCart(cartUserName, facilityName);
		if (cart == null) {
			return emptyCart(facilityName, cartUserName);
		}

		if (items.equals("*")) {
			for (CartItem cartItem : cart.getCartItems()) {
				em.remove(cartItem);
			}
		} else {
			for (String item : items.split("\\s*,\\s*")) {
				String[] pair = item.split("\\s+");

				if (pair.length > 1) {
					String entityType = pair[0];
					Long entityId = Long.parseLong(pair[1]);

					for (CartItem cartItem : cart.getCartItems()) {
						boolean entityTypesMatch = cartItem.getEntityType().equals(EntityType.valueOf(entityType));
						boolean entityIdsMatch = cartItem.getEntityId().equals(entityId);
						if (entityTypesMatch && entityIdsMatch) {
							em.remove(cartItem);
						}
					}
				} else {
					Long id = Long.parseLong(pair[0]);
					for (CartItem cartItem : cart.getCartItems()) {
						if (cartItem.getId().equals(id)) {
							em.remove(cartItem);
							break;
						}
					}
				}
			}
		}

		em.flush();
		em.refresh(cart);

		if (cart.getCartItems().size() == 0) {
			em.remove(cart);
			em.flush();
			return emptyCart(facilityName, cartUserName);
		}

		return Response.ok().entity(cart).build();
	}

	/**
	 * Submits a cart which creates a download.
	 *
	 * @summary submitCart
	 *
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @param facilityName
	 *            the name of the facility e.g. 'dls'.
	 *            Properties must map this to a url to a valid ICAT REST api.
	 *
	 * @param transport
	 *            the type of delivery method e.g. 'https' or 'globus' etc...
	 *
	 * @param email
	 *            an optional email to send download status messages to e.g. if
	 *            the download is prepared
	 *
	 * @param fileName
	 *            the name of the zip file containing the downloads.
	 *
	 * @param zipType
	 *            zip compressing options can be 'ZIP' (default) or
	 *            'ZIP_AND_COMPRESS'
	 *
	 * @return returns the (empty) cart object (with downloadId) in the form:
	 *         {"facilityName":"test","userName":"simple/root","cartItems":[],
	 *         "downloadId":3}
	 *
	 * @throws MalformedURLException
	 *             if facilityName is invalid.
	 *
	 * @throws ParseException
	 *             if a JPQL query is malformed.
	 * 
	 * @throws TopcatException
	 *             if anything else goes wrong.
	 */
	@POST
	@Path("/cart/{facilityName}/submit")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response submitCart(@PathParam("facilityName") String facilityName,
			@FormParam("sessionId") String sessionId,
			@FormParam("transport") String transport,
			@FormParam("email") String email,
			@FormParam("fileName") String fileName,
			@FormParam("zipType") String zipType)
			throws TopcatException, MalformedURLException, ParseException {

		logger.info("submitCart called");

		if (fileName == null || fileName.trim().isEmpty()) {
			throw new BadRequestException("fileName is required");
		}

		if (transport == null || transport.trim().isEmpty()) {
			throw new BadRequestException("transport is required");
		}

		String icatUrl = getIcatUrl( facilityName );
		IcatClient icatClient = new IcatClient(icatUrl, sessionId);
		String userName = icatClient.getUserName();
		String cartUserName = getCartUserName(userName, sessionId);

		logger.info("submitCart: get cart for user: " + cartUserName + ", facility: " + facilityName + "...");

		Cart cart = cartRepository.getCart(cartUserName, facilityName);
		String fullName = icatClient.getFullName();
		Long downloadId = null;
		String transportUrl = getDownloadUrl(facilityName, transport);
		IdsClient idsClient = new IdsClient(transportUrl);

		if(email != null && email.equals("")){
			email = null;
		}
		

		if (cart != null) {
			em.refresh(cart);
			
			Download download = new Download();
			download.setSessionId(sessionId);
			download.setFacilityName(cart.getFacilityName());
			download.setFileName(fileName);
			download.setUserName(cart.getUserName());
			download.setFullName(fullName);
			download.setTransport(transport);
			download.setEmail(email);
			download.setIsEmailSent(false);
			download.setSize(0);

			List<DownloadItem> downloadItems = new ArrayList<DownloadItem>();

			for (CartItem cartItem : cart.getCartItems()) {
				DownloadItem downloadItem = new DownloadItem();
				downloadItem.setEntityId(cartItem.getEntityId());
				downloadItem.setEntityType(cartItem.getEntityType());
				downloadItem.setDownload(download);
				downloadItems.add(downloadItem);
			}

			download.setDownloadItems(downloadItems);

			Boolean isTwoLevel = idsClient.isTwoLevel();
			download.setIsTwoLevel(isTwoLevel);

			if(isTwoLevel){
				download.setStatus(DownloadStatus.PREPARING);
			} else {
				String preparedId = idsClient.prepareData(download.getSessionId(), download.getInvestigationIds(), download.getDatasetIds(), download.getDatafileIds());
      			download.setPreparedId(preparedId);
				download.setStatus(DownloadStatus.COMPLETE);
			}

			try {
				em.persist(download);
				em.flush();
				em.refresh(download);
				downloadId = download.getId();
				em.remove(cart);
				em.flush();
			} catch (Exception e) {
				logger.info("submitCart: exception during EntityManager operations: " + e.getMessage());
				throw new BadRequestException("Unable to submit for cart for download");
			}
		}

		return emptyCart(facilityName, cartUserName, downloadId);
	}



	/**
	 * Retrieves the total file size (in bytes) for any investigation, datasets or datafiles.
	 *
	 * @summary getSize
	 *
	 * @param facilityName
	 *            a facility name - properties must map this to a url to a valid ICAT REST api.
	 * 
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @param entityType
	 *            the type of entity 'investigation', 'dataset' or 'datafile'.
	 *
	 * @param entityId
	 *            a comma-separated-list of entity ids
	 *
	 * @return total size of the entities (as a Long)
	 * 
	 * @throws TopcatException
	 *             if anything else goes wrong.
	 */
	@GET
	@Path("/getSize")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSize(
		@QueryParam("facilityName") String facilityName,
		@QueryParam("sessionId") String sessionId,
		@QueryParam("entityType") String entityType,
		@QueryParam("entityId") Long entityId) throws TopcatException {

		String idsUrl = getIdsUrl( facilityName );
		IdsClient idsClient = new IdsClient(idsUrl);

		Long size = idsClient.getSize(cacheRepository, sessionId, entityType, entityId);

		return Response.ok().entity(size.toString()).build();
	}

	/**
	 * Query the enabled/disabled status of a download type. The default status is enabled.
	 * 
	 * @summary getDownloadTypeStatus
	 * 
	 * @param type
	 *            a download transport type name (as configured in topcat.json downloadTransportTypes[].type)
	 * @param facilityName
	 *            a facility name - properties must map this to a url to a valid ICAT REST api.
	 * 
	 * @param sessionId
	 *            a valid session id which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @return JSON object with disabled (boolean) and message (string) fields
	 * 
	 * @throws TopcatException
	 */
	@GET
	@Path("/downloadType/{type}/status")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getDownloadTypeStatus(
			@PathParam("type") String type,
			@QueryParam("facilityName") String facilityName,
			@QueryParam("sessionId") String sessionId)
					throws TopcatException {
		  
		Boolean disabled = false;
		String message = "";
		DownloadType downloadType = downloadTypeRepository.getDownloadType(facilityName, type);
		  
		if( downloadType != null ) {
			disabled = downloadType.getDisabled();
			message = downloadType.getMessage();
		}

		JsonObjectBuilder responseJson = Json.createObjectBuilder()
				.add("disabled", disabled)
				.add("message", message);

		return Response.ok().entity(responseJson.build().toString()).build();
	}

	private Response emptyCart(String facilityName, String userName, Long downloadId) {
		JsonObjectBuilder emptyCart = Json.createObjectBuilder().add("facilityName", facilityName)
				.add("userName", userName).add("cartItems", Json.createArrayBuilder().build());

		if (downloadId != null) {
			emptyCart.add("downloadId", downloadId);
		}

		return Response.ok().entity(emptyCart.build().toString()).build();
	}

	private Response emptyCart(String facilityName, String userName) {
		return emptyCart(facilityName, userName, null);
	}
	
	private String getIcatUrl( String facilityName ) throws BadRequestException{
		testFacilityName( facilityName, "getIcatUrl" );
		try {
			return FacilityMap.getInstance().getIcatUrl(facilityName);
		} catch (InternalException ie){
			throw new BadRequestException( ie.getMessage() );
		}
	}

	private String getIdsUrl( String facilityName ) throws BadRequestException{
		testFacilityName( facilityName, "getIdsUrl" );
		try {
			return FacilityMap.getInstance().getIdsUrl(facilityName);
		} catch (InternalException ie){
			throw new BadRequestException( ie.getMessage() );
		}
	}

	private String getDownloadUrl( String facilityName, String downloadType ) throws BadRequestException{
		testFacilityName( facilityName, "getDownloadUrl" );
		try {
			return FacilityMap.getInstance().getDownloadUrl(facilityName, downloadType);
		} catch (InternalException ie){
			throw new BadRequestException( ie.getMessage() );
		}
	}
	
	private void testFacilityName( String facilityName, String methodName ) throws BadRequestException{
		if( facilityName == null ){
			// Most likely an old-style API request using icat/idsUrl
			// rather than facilityName; so log and raise a specific error here.
			String message = "UserResource." + methodName + ": facilityName is null. Perhaps request is using old API?";
			logger.error( message );
			throw new BadRequestException( message );
		}
	}


}
