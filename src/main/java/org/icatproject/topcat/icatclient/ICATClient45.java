package org.icatproject.topcat.icatclient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.icatproject.topcat.domain.CartItem;
import org.icatproject.topcat.domain.EntityType;
import org.icatproject.topcat.domain.ParentEntity;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.ForbiddenException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.utils.PropertyHandler;
import org.icatproject_4_5_0.Datafile;
import org.icatproject_4_5_0.Dataset;
import org.icatproject_4_5_0.ICAT;
import org.icatproject_4_5_0.ICATService;
import org.icatproject_4_5_0.IcatException_Exception;
import org.icatproject_4_5_0.Investigation;
import org.icatproject_4_5_0.Login.Credentials;
import org.icatproject_4_5_0.Login.Credentials.Entry;
import org.icatproject_4_5_0.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ICAT client for ICAT 4.5
 */
public class ICATClient45 implements ICATClientInterface {
	private ICAT service;

	private static final Logger logger = LoggerFactory.getLogger(ICATClient45.class);

	public ICATClient45(String serverURL) throws MalformedURLException {
		logger.info("ICATInterfacev45: serverURL (" + serverURL + ")");

		if (!serverURL.matches(".*/ICATService/ICAT\\?wsdl$")) {
			if (serverURL.matches(".*/$")) {
				serverURL = serverURL + "ICATService/ICAT?wsdl";
			} else {
				serverURL = serverURL + "/ICATService/ICAT?wsdl";
			}
		}
		URL url = new URL(serverURL);

		service = new ICATService(url, new QName("http://icatproject.org", "ICATService")).getICATPort();
	}

	/**
	 * Authenticate against an ICAT service and return an icat session id
	 *
	 * @param authenticationType
	 *            the authentication type name
	 * @param parameters
	 *            the map of parameters (username and password)
	 * @return String the icat session id
	 *
	 * @throws AuthenticationException
	 *             the authentication exception
	 * @throws InternalException
	 *             the internal exception
	 *
	 */
	@Override
	public String login(String authenticationType, Map<String, String> parameters)
			throws AuthenticationException, InternalException {
		logger.info(
				"login: authenticationType (" + authenticationType + "), number of parameters " + parameters.size());

		String result = new String();

		try {
			Credentials credentials = new Credentials();
			List<Entry> entries = credentials.getEntry();
			for (String key : parameters.keySet()) {
				Entry entry = new Entry();
				entry.setKey(key);
				entry.setValue(parameters.get(key));
				entries.add(entry);
			}
			result = service.login(authenticationType, credentials);
		} catch (IcatException_Exception ex) {
			logger.debug("IcatException_Exception:" + ex.getMessage());
			throw new AuthenticationException(ex.getMessage());
		} catch (javax.xml.ws.WebServiceException ex) {
			logger.debug("login: WebServiceException:" + ex.getMessage());
			throw new InternalException("ICAT Server not available");
		}

		logger.info("login: result(" + result + ")");

		return result;
	}

	/**
	 * Returns the user name as used by icat
	 *
	 * @param icatSessionId
	 *            the icat session id
	 * @return the icat user name
	 * @throws TopcatException
	 */
	@Override
	public String getUserName(String icatSessionId) throws TopcatException {
		String result = null;

		try {
			result = service.getUserName(icatSessionId);
		} catch (IcatException_Exception e) {
			throwNewICATException(e);
		}

		return result;

	}

	/**
	 * Checks to see if the user has admin access.
	 *
	 * @param icatSessionId
	 *            the icat session id
	 * @return whether the user has admin access or not
	 * @throws TopcatException
	 */
	@Override
	public Boolean isAdmin(String icatSessionId) throws TopcatException {
		String[] adminUserNames = PropertyHandler.getInstance().getAdminUserNames();
		String userName = getUserName(icatSessionId);
		int i;

		for (i = 0; i < adminUserNames.length; i++) {
			if (userName.equals(adminUserNames[i])) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getEntityName(String icatSessionId, String entityType, Long entityId) throws TopcatException {
		List<Object> result = new ArrayList<Object>();

		if (!(entityType.equals("investigation") || entityType.equals("dataset") || entityType.equals("datafile"))) {
			throw new BadRequestException(
					"The entity type can only be either 'investigation', 'dataset' or 'datafile' got instead '"
							+ entityType + "'");
		}

		String capitalizedEntityType = entityType.substring(0, 1).toUpperCase() + entityType.substring(1);

		String name = null;

		try {
			result = service.search(icatSessionId, "SELECT " + entityType + " FROM " + capitalizedEntityType + " "
					+ entityType + " WHERE " + entityType + ".id = " + entityId);
		} catch (IcatException_Exception e) {
			throwNewICATException(e);
		}

		if (!result.isEmpty()) {
			if (entityType.equals("investigation")) {
				name = ((Investigation) result.get(0)).getName();
			} else if (entityType.equals("dataset")) {
				name = ((Dataset) result.get(0)).getName();
			} else {
				name = ((Datafile) result.get(0)).getName();
			}

		} else {
			throw new BadRequestException("No such entity exists i.e. " + entityType + " with id " + entityId);
		}

		return name;
	}

	public List<Object> getEntities(String icatSessionId, String entityType, List<Long> entityIds)
			throws TopcatException {
		List<Object> out = new ArrayList<Object>();
		try {
			int i = 0;
			for (; i < entityIds.size();) {
				StringBuffer currentEntityIds = new StringBuffer();

				for (; i < entityIds.size() && currentEntityIds.length() <= 2000; i++) {
					if (currentEntityIds.length() != 0) {
						currentEntityIds.append(",");
					}
					currentEntityIds.append(entityIds.get(i));
				}

				String query;

				if (entityType.equals("datafile")) {
					query = "SELECT datafile from Datafile datafile where datafile.id in ("
							+ currentEntityIds.toString() + ") include datafile.dataset.investigation";
				} else if (entityType.equals("dataset")) {
					query = "SELECT dataset from Dataset dataset where dataset.id in (" + currentEntityIds.toString()
							+ ") include dataset.investigation";
				} else {
					query = "SELECT investigation from Investigation investigation where investigation.id in ("
							+ currentEntityIds.toString() + ")";
				}

				out.addAll(service.search(icatSessionId, query));
			}
		} catch (IcatException_Exception e) {
			throwNewICATException(e);
		}

		return out;
	}

	public List<CartItem> getCartItems(String icatSessionId, Map<String, List<Long>> entityTypeEntityIds)
			throws TopcatException {
		List<CartItem> out = new ArrayList<CartItem>();

		for (String entityType : entityTypeEntityIds.keySet()) {
			List<Long> entityIds = entityTypeEntityIds.get(entityType);
			List<Object> entities = getEntities(icatSessionId, entityType, entityIds);
			for (Object entity : entities) {
				String name;
				Long entityId;

				if (entityType.equals("investigation")) {
					Investigation investigation = (Investigation) entity;
					entityId = investigation.getId();
					name = investigation.getName();
				} else if (entityType.equals("dataset")) {
					Dataset dataset = (Dataset) entity;
					entityId = dataset.getId();
					name = dataset.getName();
				} else {
					Datafile datafile = (Datafile) entity;
					entityId = datafile.getId();
					name = datafile.getName();
				}

				CartItem cartItem = new CartItem();
				cartItem.setEntityType(EntityType.valueOf(entityType));
				cartItem.setEntityId(entityId);
				cartItem.setName(name);

				if (entityType.equals("datafile")) {
					ParentEntity parentEntity = new ParentEntity();
					parentEntity.setEntityType(EntityType.valueOf("dataset"));
					parentEntity.setEntityId(((Datafile) entity).getDataset().getId());
					cartItem.getParentEntities().add(parentEntity);

					parentEntity = new ParentEntity();
					parentEntity.setEntityType(EntityType.valueOf("investigation"));
					parentEntity.setEntityId(((Datafile) entity).getDataset().getInvestigation().getId());
					cartItem.getParentEntities().add(parentEntity);
				} else if (entityType.equals("dataset")) {
					ParentEntity parentEntity = new ParentEntity();
					parentEntity.setEntityType(EntityType.valueOf("investigation"));
					parentEntity.setEntityId(((Dataset) entity).getInvestigation().getId());
					cartItem.getParentEntities().add(parentEntity);
				}

				out.add(cartItem);

			}
		}

		return out;
	}

	@Override
	public String getFullName(String icatSessionId) throws TopcatException {
		List<Object> result = new ArrayList<Object>();

		String fullName = null;

		try {
			result = service.search(icatSessionId, "SELECT u FROM User u WHERE u.name = :user");
		} catch (IcatException_Exception e) {
			throwNewICATException(e);
		}

		if (result != null && !result.isEmpty()) {
			User user = (User) result.get(0);

			fullName = user.getFullName();
		}

		return fullName;
	}

	/**
	 * Returns whether a session is valid or not
	 *
	 * @param icatSessionId
	 *            the icat session id
	 * @return whether the session is valid or not
	 */
	@Override
	public Boolean isSessionValid(String icatSessionId) throws IcatException {
		Double result = null;

		try {
			result = service.getRemainingMinutes(icatSessionId);
		} catch (IcatException_Exception e) {
			logger.info("isSessionValid: " + e.getMessage());

			return false;
			// throw new IcatException(e.getMessage());
		}

		if (result > 0) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the number of minutes remaining for a session
	 *
	 * @param icatSessionId
	 *            the icat session id
	 * @return the remaining minutes of the session. -1 if session is not valid
	 *         or if other icat server error is thrown
	 */
	@Override
	public Long getRemainingMinutes(String icatSessionId) throws IcatException {
		Double result = null;

		try {
			result = service.getRemainingMinutes(icatSessionId);
		} catch (IcatException_Exception e) {
			return -1L;
		}

		return result.longValue();
	}

	/**
	 * Resets the time-to-live of the icat session
	 *
	 * @param icatSessionId
	 *            the icat session id
	 * @throws TopcatException
	 *
	 */
	@Override
	public void refresh(String icatSessionId) throws TopcatException {
		try {
			service.refresh(icatSessionId);
		} catch (IcatException_Exception e) {
			logger.info("logout: " + e.getMessage());
			throwNewICATException(e);
		}

	}

	/**
	 * This invalidates the sessionId.
	 *
	 * @param icatSessionId
	 *            the icat session id
	 * @throws TopcatException
	 *
	 */
	@Override
	public void logout(String icatSessionId) throws TopcatException {
		try {
			service.logout(icatSessionId);
		} catch (IcatException_Exception e) {
			logger.info("logout: " + e.getMessage());
			throwNewICATException(e);
		}

	}

	// TODO
	/**
	 * Determine if an IcatException_Exception is an INSUFFICIENT_PRIVILEGES or
	 * SESSION type exception
	 *
	 * @param e
	 * @throws AuthenticationException
	 * @throws IcatException
	 * @throws ForbiddenException
	 * @throws BadRequestException
	 * @throws NotFoundException
	 */
	private void throwNewICATException(IcatException_Exception e) throws TopcatException {
		logger.debug("icatExceptionType: " + e.getFaultInfo().getType());

		switch (e.getFaultInfo().getType()) {
		case INSUFFICIENT_PRIVILEGES:
			throw new ForbiddenException(e.getMessage());
		case BAD_PARAMETER:
			throw new BadRequestException(e.getMessage());
		case INTERNAL:
			throw new IcatException(e.getMessage());
		case SESSION:
			throw new AuthenticationException(e.getMessage());
		case NO_SUCH_OBJECT_FOUND:
			throw new NotFoundException(e.getMessage());
		case VALIDATION:
			throw new IcatException(e.getMessage());
		default:
			throw new IcatException(e.getMessage());
		}

		/*
		 * if (e.getFaultInfo().getType() ==
		 * IcatExceptionType.INSUFFICIENT_PRIVILEGES ||
		 * e.getFaultInfo().getType() == IcatExceptionType.SESSION) { throw new
		 * AuthenticationException(e.getMessage()); } else { throw new
		 * IcatException(e.getMessage()); }
		 */
	}

}
