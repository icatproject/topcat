package org.icatproject.topcat.admin.server.ejb.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TMessages;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatAuthentication;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatServer;
import uk.ac.stfc.topcat.ejb.entity.TopcatMessages;
import uk.ac.stfc.topcat.ejb.entity.TopcatUser;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;
import uk.ac.stfc.topcat.ejb.manager.UtilityManager;

@Stateless
public class AdminEJB {

	final static Logger logger = LoggerFactory.getLogger(AdminEJB.class);

	@PersistenceContext(unitName = "TopCATEJBPU")
	private EntityManager entityManager;

	@PostConstruct
	private void init() {
		try {
			logger.debug("Initialised AdminEJB");
		} catch (Exception e) {
			String msg = e.getClass().getName() + " reports " + e.getMessage();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	public void printTopcatIcatServerDetails() {
		@SuppressWarnings("unchecked")
		List<TopcatIcatServer> servers = entityManager.createNamedQuery(
				"TopcatIcatServer.findAll").getResultList();
		for (TopcatIcatServer icatServer : servers) {
			logger.debug(icatServer.getId() + " " + icatServer.getName() + " "
					+ icatServer.getServerUrl() + " " + icatServer.getVersion());
		}
	}

	public ArrayList<TFacility> getAllFacilities() {
		UtilityManager utilityManager = new UtilityManager();
		ArrayList<TFacility> allFacilities = utilityManager
				.getAllFacilities(entityManager);
		for (TFacility facility : allFacilities) {
			logger.debug("getAllFacility :" + " Name: " + facility.getName()
					+ " PluginName:  " + facility.getUrl() + " Version: "
					+ facility.getVersion());

		}
		return allFacilities;
	}

	public void addIcatServer(TFacility facility) throws TopcatException {

		TopcatIcatServer tiServer = new TopcatIcatServer();
		tiServer.setName(facility.getName());
		tiServer.setVersion(facility.getVersion());
		tiServer.setServerUrl(facility.getUrl());
		tiServer.setPluginName(facility.getSearchPluginName());
		tiServer.setDownloadPluginName(facility.getDownloadPluginName());
		tiServer.setDownloadType(facility.getDownloadTypeName());
		tiServer.setDownloadServiceUrl(facility.getDownloadServiceUrl());		
		entityManager.persist(tiServer);

		logger.debug("A new row has been added");
	}

	public void updateIcatServer(TFacility facility) {

		TopcatIcatServer tiServer = new TopcatIcatServer();
		tiServer = entityManager.find(TopcatIcatServer.class, facility.getId());
		tiServer.setName(facility.getName());
		tiServer.setVersion(facility.getVersion());
		tiServer.setServerUrl(facility.getUrl());
		tiServer.setPluginName(facility.getSearchPluginName());
		tiServer.setDownloadPluginName(facility.getDownloadPluginName());
		tiServer.setDownloadType(facility.getDownloadTypeName());
		tiServer.setDownloadServiceUrl(facility.getDownloadServiceUrl());
		entityManager.merge(tiServer);

		logger.debug("The Row with the ID: " + facility.getId()
				+ " has been Removed");
	}

	public void removeIcatServer(Long id, String facilityName) {
		@SuppressWarnings("unchecked")
		//get list of users that belongs to the server
		List<TopcatUser> topcatUsers = entityManager.createNamedQuery("TopcatUser.findByServerId").setParameter("serverId", id).getResultList();
		//loop user
		for (TopcatUser topcatUser : topcatUsers) {
		    //remove sessions belonging to the user
	        entityManager.createNamedQuery("TopcatUserSession.deleteSessionByUserId").setParameter("userId", topcatUser.getId()).executeUpdate();
	        
	        //remove user downloads
	        entityManager.createNamedQuery("TopcatUserDownload.deleteByUserId").setParameter("userId", topcatUser.getId()).executeUpdate();
	        
		    //remove the user
            entityManager.remove(topcatUser);            
        }
		
		//remove user info
        entityManager.createNamedQuery("TopcatUserInfo.deleteByServerId").setParameter("serverId", id).executeUpdate();
		
		//remove authentication types for the server
		entityManager.createNamedQuery("TopcatIcatAuthentication.deleteByServerId").setParameter("serverId", id).executeUpdate();
		
		//Delete server 
		TopcatIcatServer tiServer = entityManager.find(TopcatIcatServer.class,id);		
		entityManager.remove(tiServer);
		entityManager.flush();
	}

	public List<TAuthentication> authCall(String serverName) {
		ArrayList<TAuthentication> authenticationDetails = new ArrayList<TAuthentication>();
		@SuppressWarnings("unchecked")
                List<TopcatIcatAuthentication> authenDetails = entityManager
				.createNamedQuery("TopcatIcatAuthentication.findByServerName")
				.setParameter("serverName", serverName).getResultList();
		for (TopcatIcatAuthentication authentication : authenDetails) {
			TAuthentication tAuthentication = new TAuthentication();
			tAuthentication.setType(authentication.getAuthenticationType());
			tAuthentication.setPluginName(authentication.getPluginName());
			tAuthentication
					.setUrl(authentication.getAuthenticationServiceUrl());
			tAuthentication.setId(authentication.getId());
			tAuthentication.setDisplayName(authentication.getDisplayName());
			authenticationDetails.add(tAuthentication);
		}

		return authenticationDetails;
	}

	public void updateAuthDetails(TAuthentication authentication, long id) {
		logger.debug("its updating");
		TopcatIcatAuthentication tiServer = new TopcatIcatAuthentication();
		tiServer = entityManager.find(TopcatIcatAuthentication.class, id);
		tiServer.setAuthenticationType(authentication.getType());
		tiServer.setAuthenticationServiceUrl(authentication.getUrl());
		tiServer.setPluginName(authentication.getPluginName());
		tiServer.setDisplayName(authentication.getDisplayName());
		entityManager.merge(tiServer);
	}

	public void removeRowFromAuthTable(Long id) {
		TopcatIcatAuthentication authonticationDetails = entityManager.find(
				TopcatIcatAuthentication.class, id);
		entityManager.remove(authonticationDetails);
	}

	public void addRowToAuthTable(TAuthentication authentication) {
		TopcatIcatAuthentication authenticationDetails = new TopcatIcatAuthentication();
		authenticationDetails.setAuthenticationType((authentication.getType()));
		authenticationDetails.setAuthenticationServiceUrl((authentication
				.getUrl()));
		authenticationDetails.setPluginName((authentication.getPluginName()));
		authenticationDetails.setDisplayName(authentication.getDisplayName());
		logger.debug(" " + authentication.getId());
		authenticationDetails.setServerId(entityManager.find(
				TopcatIcatServer.class, authentication.getId()));
		entityManager.persist(authenticationDetails);

	}

	public int icatAssociatedAuthCount(String serverName) {
		int count = entityManager
				.createNamedQuery("TopcatIcatAuthentication.findByServerName")
				.setParameter("serverName", serverName).getResultList().size();
		return count;
	}

	public List<TMessages> messageCall() {
		
		 List<TMessages> tMessages = new ArrayList<TMessages>();
		 List<?> messages = entityManager.createNamedQuery("TopcatMessages.findAll").getResultList();
		
		 for (Object message: messages){
			 TMessages tMessage = new TMessages();
			 tMessage.setId(((TopcatMessages) message).getId());
			 tMessage.setMessage(((TopcatMessages) message).getMessage());
			 tMessage.setStartTime(((TopcatMessages) message).getStartTime());
			 tMessage.setStopTime(((TopcatMessages) message).getStopTime());
			 tMessages.add(tMessage);
		 }
		return tMessages;
		 
	}

	public void addMessage(TMessages message) {
		TopcatMessages entity = new TopcatMessages();
		entity.setMessage(message.getMessage());
		entity.setStartTime(message.getStartTime());
		entity.setStopTime(message.getStopTime());
		logger.debug(entity.getMessage());
		logger.debug(entity.getStartTime().toString());
		logger.debug(entity.getStopTime().toString());
		entityManager.persist(entity);
		
	}
	
	public void deleteMessage(TMessages message) {
	    logger.debug(message.getId().toString());
		TopcatMessages topcatMessage =  (TopcatMessages) entityManager.
		        createNamedQuery("TopcatMessages.findById").
		        setParameter("id", message.getId()).getSingleResult();				
		entityManager.remove(topcatMessage);
		entityManager.flush();
		
	}

    public List<TMessages> getMessagesByDateRange(Date fromDateTime,
            Date toDateTime) {
        List<TMessages> tMessages = new ArrayList<TMessages>();
        List<?> messages = entityManager.
                createNamedQuery("TopcatMessages.findMessagesByDateRange").
                setParameter("fromDateTime", fromDateTime).
                setParameter("toDateTime", toDateTime).
                getResultList();
       
        for (Object message: messages){
            TMessages tMessage = new TMessages();
            tMessage.setId(((TopcatMessages) message).getId());
            tMessage.setMessage(((TopcatMessages) message).getMessage());
            tMessage.setStartTime(((TopcatMessages) message).getStartTime());
            tMessage.setStopTime(((TopcatMessages) message).getStopTime());
            tMessages.add(tMessage);
        }
        
        return tMessages;
    }
	
	

}
