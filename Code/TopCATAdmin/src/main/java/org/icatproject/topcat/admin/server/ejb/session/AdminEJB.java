package org.icatproject.topcat.admin.server.ejb.session;

import java.util.ArrayList;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;

import org.icatproject.topcat.admin.shared.ServerException;
import org.icatproject.topcat.admin.shared.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.ssl.internal.ssl.Debug;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatServer;
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
		tiServer.setDownloadServiceUrl(facility.getDownloadServiceUrl());
		entityManager.merge(tiServer);

		logger.debug("The Row with the ID: " + facility.getId()
				+ " has been Removed");
	}

	public void removeIcatServer(Long id) {
		TopcatIcatServer tiServer = entityManager.find(TopcatIcatServer.class,
				id);
		entityManager.remove(tiServer);
	}

	public TFacility rowCall(Long id){
		TFacility facility = new TFacility();
		TopcatIcatServer tiServer = new TopcatIcatServer();
		tiServer = entityManager.find(TopcatIcatServer.class, id);
		facility.setName(tiServer.getName());
		facility.setVersion(tiServer.getVersion());
		facility.setUrl(tiServer.getServerUrl());
		facility.setSearchPluginName(tiServer.getPluginName());
		facility.setDownloadPluginName(tiServer.getDownloadPluginName());
		facility.setDownloadServiceUrl(tiServer.getDownloadServiceUrl());
		facility.setAuthenticationServiceType(tiServer.getAuthenticationServiceType());
		facility.setAuthenticationServiceUrl(tiServer.getAuthenticationServiceUrl());
		facility.setVersion(tiServer.getVersion());
		
		return facility;
		
	}
		
}
