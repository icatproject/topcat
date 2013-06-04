/**
 *
 * Copyright (c) 2009-2013
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.ejb.manager;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileFormat;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatAuthentication;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatServer;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserDownload;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;

/**
 * This has utilties such as getting list of facilities etc.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */

public class UtilityManager {

    private final static Logger logger = Logger.getLogger(UtilityManager.class.getName());

    /**
     * This method returns all the facility names currently used by the Topcat.
     * 
     * @param manager
     * @return
     */
    public ArrayList<String> getAllFacilityNames(EntityManager manager) {
        logger.info("getAllFacilityNames");
        ArrayList<String> facilityNames = new ArrayList<String>();
        List<TopcatIcatServer> servers = manager.createNamedQuery("TopcatIcatServer.findAll").getResultList();
        for (TopcatIcatServer icatServer : servers) {
            facilityNames.add(icatServer.getName());
        }
        return facilityNames;
    }

    public ArrayList<TFacility> getAllFacilities(EntityManager manager) {
        logger.info("getAllFacilities");
        ArrayList<TFacility> facilities = new ArrayList<TFacility>();
        List<TopcatIcatServer> servers = manager.createNamedQuery("TopcatIcatServer.findAll").getResultList();

        for (TopcatIcatServer icatServer : servers) {
            TFacility tFacility = new TFacility();
            tFacility.setName(icatServer.getName());
            tFacility.setUrl(icatServer.getServerUrl());
            tFacility.setVersion(icatServer.getVersion());
            tFacility.setSearchPluginName(icatServer.getPluginName());
            tFacility.setDownloadPluginName(icatServer.getDownloadPluginName());
            tFacility.setDownloadServiceUrl(icatServer.getDownloadServiceUrl());
            tFacility.setId(icatServer.getId());
            facilities.add(tFacility);
            if (logger.isTraceEnabled()) {
                logger.trace(tFacility.toString());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getAllFacilities facilities.size (" + facilities.size() + ")");
        }
        return facilities;
    }

    /**
     * This method returns all the instrument names from a given server
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws TopcatException
     */
    public ArrayList<String> getInstrumentNames(EntityManager manager, String topcatSessionId, String facilityName)
            throws TopcatException {
        logger.info("getInstrumentNames: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + ")");
        ArrayList<String> instrumentNames = new ArrayList<String>();
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
            instrumentNames.addAll(getInstrumentNames(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId()));
        } catch (javax.persistence.NoResultException ex) {
            try {
                userSession = (TopcatUserSession) manager
                        .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                        .setParameter("serverName", facilityName).getSingleResult();
                instrumentNames.addAll(getInstrumentNames(userSession.getIcatSessionId(), userSession.getUserId()
                        .getServerId()));
            } catch (javax.persistence.NoResultException exinnex) {
            }
        }
        return instrumentNames;
    }

    /**
     * This method implements the returns of all the instrument names from a
     * given server
     * 
     * @param manager
     * @param sessionId
     * @param server
     * @return
     * @throws TopcatException
     */
    private List<String> getInstrumentNames(String sessionId, TopcatIcatServer server) throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listInstruments(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getInstrumentNames: " + ex.getMessage());
        }
        return new ArrayList<String>();
    }

    /**
     * This method returns all the investigation types from a given server
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws TopcatException
     */
    public ArrayList<String> getInvestigationTypes(EntityManager manager, String topcatSessionId, String facilityName)
            throws TopcatException {
        logger.info("getInvestigationTypes: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + ")");
        ArrayList<String> investigationTypes = new ArrayList<String>();
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
            investigationTypes.addAll(getInvestigationTypes(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId()));
        } catch (javax.persistence.NoResultException ex) {
            try {
                userSession = (TopcatUserSession) manager
                        .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                        .setParameter("serverName", facilityName).getSingleResult();
                investigationTypes.addAll(getInvestigationTypes(userSession.getIcatSessionId(), userSession.getUserId()
                        .getServerId()));
            } catch (javax.persistence.NoResultException exinnex) {
            }
        }
        return investigationTypes;
    }

    /**
     * This method implements the return of all the investigation types from a
     * given server
     * 
     * @param sessionId
     * @param server
     * @return
     * @throws TopcatException
     */
    private List<String> getInvestigationTypes(String sessionId, TopcatIcatServer server) throws TopcatException {
        try {
            // Get the ICAT webservice client and call get investigation types
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listInvestigationTypes(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getInvestigationTypes: " + ex.getMessage());
        } catch (java.lang.NullPointerException ex) {
            logger.error("getInvestigationTypes: " + ex.getMessage());
        }
        return new ArrayList<String>();
    }

    /**
     * Get all of the facility cycles from a given facility and instrument.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param instrument
     * @return a list of <code>TFacilityCycle</code>
     * @throws TopcatException
     */
    public List<TFacilityCycle> getFacilityCyclesWithInstrument(EntityManager manager, String topcatSessionId,
            String facilityName, String instrument) throws TopcatException {
        logger.info("getFacilityCyclesWithInstrument: topcatSessionId (" + topcatSessionId + "), facilityName ("
                + facilityName + "), instrument (" + instrument + ")");
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
            return getFacilityCyclesWithInstrument(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId(), instrument);
        } catch (javax.persistence.NoResultException ex) {
            try {
                userSession = (TopcatUserSession) manager
                        .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                        .setParameter("serverName", facilityName).getSingleResult();
                return getFacilityCycles(userSession.getIcatSessionId(), userSession.getUserId().getServerId());
            } catch (javax.persistence.NoResultException exinnex) {
            }
        }
        return new ArrayList<TFacilityCycle>();
    }

    /**
     * Get all of the facility cycles from a given server and instrument.
     * 
     * @param sessionId
     * @param server
     * @param instrument
     * @return a list of <code>TFacilityCycle</code>
     * @throws TopcatException
     */
    private List<TFacilityCycle> getFacilityCyclesWithInstrument(String sessionId, TopcatIcatServer server,
            String instrument) throws TopcatException {
        try {
            // Get the ICAT webservice client and call get facility cycles
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listFacilityCyclesForInstrument(sessionId, instrument);
        } catch (MalformedURLException ex) {
            logger.error("getFacilityCyclesWithInstrument: " + ex.getMessage());
        } catch (java.lang.NullPointerException ex) {
            logger.error("getFacilityCyclesWithInstrument: " + ex.getMessage());
        }
        return new ArrayList<TFacilityCycle>();
    }

    /**
     * Get all of the facility cycles from a given server.
     * 
     * @param sessionId
     * @param server
     * @return a list of <code>TFacilityCycle</code>
     * @throws TopcatException
     */
    private List<TFacilityCycle> getFacilityCycles(String sessionId, TopcatIcatServer server) throws TopcatException {
        try {
            // Get the ICAT webservice client and call get facility cycles
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listFacilityCycles(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getFacilityCycles: " + ex.getMessage());
        } catch (java.lang.NullPointerException ex) {
            logger.error("getFacilityCycles: " + ex.getMessage());
        }
        return new ArrayList<TFacilityCycle>();
    }

    /**
     * This method returns all the investigations of the user as the
     * investigator from a given server.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws TopcatException
     */
    public List<TInvestigation> getMyInvestigationsInServer(EntityManager manager, String topcatSessionId,
            String facilityName) throws TopcatException {
        logger.info("getMyInvestigationsInServer: topcatSessionId (" + topcatSessionId + "), facilityName ("
                + facilityName + ")");
        try {
            TopcatUserSession userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
            return getMyInvestigationsInServer(userSession.getIcatSessionId(), userSession.getUserId().getServerId());
        } catch (javax.persistence.NoResultException ex) {
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigations for the user as the
     * investigator from given input server name.
     * 
     * @param sessionId
     * @param server
     * @return
     * @throws TopcatException
     */
    private List<TInvestigation> getMyInvestigationsInServer(String sessionId, TopcatIcatServer server)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getMyInvestigations(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getMyInvestigationsInServer: " + ex.getMessage());
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigations from a given server.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws TopcatException
     */
    public List<TInvestigation> getAllInvestigationsInServer(EntityManager manager, String topcatSessionId,
            String facilityName) throws TopcatException {
        logger.info("getAllInvestigationsInServer: topcatSessionId (" + topcatSessionId + "), facilityName ("
                + facilityName + ")");
        try {
            TopcatUserSession userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
            return getAllInvestigationsInServer(userSession.getIcatSessionId(), userSession.getUserId().getServerId());
        } catch (javax.persistence.NoResultException ex) {
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigations from given input server name.
     * 
     * @param sessionId
     * @param server
     * @return
     * @throws TopcatException
     */
    private List<TInvestigation> getAllInvestigationsInServer(String sessionId, TopcatIcatServer server)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getAllInvestigations(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getAllInvestigationsInServer: " + ex.getMessage());
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all user investigations of which the user is
     * investigator from a given facility and instrument name.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param instrumentName
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getMyInvestigationsInServerAndInstrument(EntityManager manager,
            String topcatSessionId, String facilityName, String instrumentName) throws TopcatException {
        logger.info("getMyInvestigationsInServerAndInstrument: topcatSessionId (" + topcatSessionId
                + "), facilityName (" + facilityName + "), instrumentName (" + instrumentName + ")");
        try {
            TopcatUserSession userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
            return getMyInvestigationsInServerAndInstrument(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId(), userSession.getUserId().getName(), userSession.getUserId().getUserSurname(),
                    instrumentName);
        } catch (javax.persistence.NoResultException ex) {
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all user investigations of which the user is
     * investigator from a given server and instrument name.
     * 
     * @param sessionId
     * @param server
     * @param userName
     * @param instrumentName
     * @return
     * @throws TopcatException
     */
    private ArrayList<TInvestigation> getMyInvestigationsInServerAndInstrument(String sessionId,
            TopcatIcatServer server, String userName, String userSurname, String instrumentName) throws TopcatException {
        if (logger.isDebugEnabled()) {
            logger.debug("getMyInvestigationsInServerAndInstrument: Searching for investigation in server:"
                    + server.getName() + " username:" + userName + " instrument:" + instrumentName);
        }
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            details.getInstrumentList().add(instrumentName);
            details.getInvestigatorNameList().add(userSurname);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.searchByAdvancedPagination(sessionId, details, 0, 200);
        } catch (MalformedURLException ex) {
            logger.error("getMyInvestigationsInServerAndInstrument: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigation for the user in a given server
     * and the given instrument.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param instrumentName
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getAllInvestigationsInServerAndInstrument(EntityManager manager,
            String topcatSessionId, String facilityName, String instrumentName) throws TopcatException {
        logger.info("getAllInvestigationsInServerAndInstrument: topcatSessionId (" + topcatSessionId
                + "), facilityName (" + facilityName + "), instrumentName (" + instrumentName + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getAllInvestigationsInServerAndInstrument(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId(), userSession.getUserId().getName(), instrumentName);
        } catch (javax.persistence.NoResultException ex) {
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigation for the user in a given server
     * and the given instrument.
     * 
     * @param sessionId
     * @param server
     * @param userName
     * @param instrumentName
     * @return
     * @throws TopcatException
     */
    private ArrayList<TInvestigation> getAllInvestigationsInServerAndInstrument(String sessionId,
            TopcatIcatServer server, String userName, String instrumentName) throws TopcatException {
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            details.getInstrumentList().add(instrumentName);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.searchByAdvancedPagination(sessionId, details, 0, 200);
        } catch (MalformedURLException ex) {
            logger.error("getAllInvestigationsInServerAndInstrument: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all user investigations of which the user is
     * investigator from a given facility instrument name and facility cycle.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param instrumentName
     * @param facilityCycle
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getMyInvestigationsInServerInstrumentAndCycle(EntityManager manager,
            String topcatSessionId, String facilityName, String instrumentName, TFacilityCycle facilityCycle)
            throws TopcatException {
        logger.info("getMyInvestigationsInServerInstrumentAndCycle: topcatSessionId (" + topcatSessionId
                + "), facilityName (" + facilityName + "), instrumentName (" + instrumentName + ")");
        try {
            TopcatUserSession userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
            return getMyInvestigationsInServeInstrumentAndCycle(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId(), userSession.getUserId().getName(), userSession.getUserId().getUserSurname(),
                    instrumentName, facilityCycle);
        } catch (javax.persistence.NoResultException ex) {
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all user investigations of which the user is
     * investigator from a given server , instrument name and facility cycle.
     * 
     * @param sessionId
     * @param server
     * @param userName
     * @param instrumentName
     * @param facilityCycle
     * @return
     * @throws TopcatException
     */
    private ArrayList<TInvestigation> getMyInvestigationsInServeInstrumentAndCycle(String sessionId,
            TopcatIcatServer server, String userName, String userSurname, String instrumentName,
            TFacilityCycle facilityCycle) throws TopcatException {
        if (logger.isDebugEnabled()) {
            logger.debug("getMyInvestigationsInServerAndInstrument: Searching for investigation in server:"
                    + server.getName() + " username:" + userName + " instrument:" + instrumentName + " facilityCycle:"
                    + facilityCycle);
        }
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            details.getInstrumentList().add(instrumentName);
            details.setStartDate(facilityCycle.getStartDate());
            details.setEndDate(facilityCycle.getFinishDate());
            details.getInvestigatorNameList().add(userSurname);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.searchByAdvancedPagination(sessionId, details, 0, 200);
        } catch (MalformedURLException ex) {
            logger.error("getMyInvestigationsInServeInstrumentAndCycle: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigation for the user in a given server,
     * the given instrument and facility cycle.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param instrumentName
     * @param facilityCycle
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getAllInvestigationsInServerInstrumentAndCycle(EntityManager manager,
            String topcatSessionId, String facilityName, String instrumentName, TFacilityCycle facilityCycle)
            throws TopcatException {
        logger.info("getAllInvestigationsInServerInstrumentAndCycle: topcatSessionId (" + topcatSessionId
                + "), facilityName (" + facilityName + "), instrumentName (" + instrumentName + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getAllInvestigationsInServerInstrumentAndCycle(userSession.getIcatSessionId(), userSession
                    .getUserId().getServerId(), userSession.getUserId().getName(), instrumentName, facilityCycle);
        } catch (javax.persistence.NoResultException ex) {
        }
        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigation for the user in a given server,
     * the given instrument and facility cycle.
     * 
     * @param sessionId
     * @param server
     * @param userName
     * @param instrumentName
     * @param facilityCycle
     * @return
     * @throws TopcatException
     */
    private ArrayList<TInvestigation> getAllInvestigationsInServerInstrumentAndCycle(String sessionId,
            TopcatIcatServer server, String userName, String instrumentName, TFacilityCycle facilityCycle)
            throws TopcatException {
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            details.getInstrumentList().add(instrumentName);
            details.setStartDate(facilityCycle.getStartDate());
            details.setEndDate(facilityCycle.getFinishDate());
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.searchByAdvancedPagination(sessionId, details, 0, 200);
        } catch (MalformedURLException ex) {
            logger.error("getAllInvestigationsInServerInstrumentAndCycle: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns the investigation details from the given facility for
     * the given investigation id.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param investigationId
     * @return
     * @throws TopcatException
     */
    public TInvestigation getInvestigationDetails(EntityManager manager, String topcatSessionId, String facilityName,
            String investigationId) throws TopcatException {
        logger.info("getInvestigationDetails: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), investigationId (" + investigationId + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getInvestigationDetails(userSession.getIcatSessionId(), userSession.getUserId().getServerId(),
                    investigationId);
        } catch (javax.persistence.NoResultException ex) {
            throw new SessionException("Invalid topcat session id");
        }
    }

    /**
     * This method returns the investigation details from the given server for
     * the given investigation id.
     * 
     * @param sessionId
     * @param server
     * @param investigationId
     * @return
     * @throws TopcatException
     */
    private TInvestigation getInvestigationDetails(String sessionId, TopcatIcatServer server, String investigationId)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getInvestigationDetails(sessionId, Long.valueOf(investigationId));
        } catch (MalformedURLException ex) {
            logger.error("getInvestigationDetails: " + ex.getMessage());
        } catch (AuthenticationException ex) {
            throw new SessionException("Invalid topcat session id");
        }
        return new TInvestigation();
    }

    /**
     * This method returns datasets for a given investigation number on given
     * input server
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param investigationNumber
     * @return
     * @throws TopcatException
     */
    public ArrayList<TDataset> getDatasetsInServer(EntityManager manager, String topcatSessionId, String facilityName,
            String investigationNumber) throws TopcatException {
        logger.info("getDatasetsInServer: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), investigationNumber (" + investigationNumber + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getDatasetsInServer(userSession.getIcatSessionId(), userSession.getUserId().getServerId(),
                    investigationNumber);
        } catch (javax.persistence.NoResultException ex) {
            throw new SessionException("Invalid topcat session id");
        }
    }

    /**
     * This method returns datasets for a given investigation number on given
     * input server
     * 
     * @param sessionId
     * @param server
     * @param investigationNumber
     * @return
     * @throws TopcatException
     */
    private ArrayList<TDataset> getDatasetsInServer(String sessionId, TopcatIcatServer server,
            String investigationNumber) throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getDatasetsInInvestigation(sessionId, Long.valueOf(investigationNumber));
        } catch (MalformedURLException ex) {
            logger.error("getDatasetsInServer: " + ex.getMessage());
        } catch (NullPointerException ex) {
            logger.error("getDatasetsInServer: " + ex.getMessage());
        }
        return new ArrayList<TDataset>();
    }

    /**
     * This method get the parameters corresponding to the input datasetid on
     * the given server.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param datasetId
     * @return
     * @throws TopcatException
     */
    public ArrayList<TDatasetParameter> getDatasetInfo(EntityManager manager, String topcatSessionId,
            String facilityName, String datasetId) throws TopcatException {
        logger.info("getDatasetInfo: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), datasetId (" + datasetId + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getDatasetInfo(userSession.getIcatSessionId(), userSession.getUserId().getServerId(), datasetId);
        } catch (javax.persistence.NoResultException ex) {
        }
        return null;
    }

    /**
     * This method get the parameters corresponding to the input datasetid on
     * the given server.
     * 
     * @param sessionId
     * @param server
     * @param datasetId
     * @return
     * @throws TopcatException
     */
    private ArrayList<TDatasetParameter> getDatasetInfo(String sessionId, TopcatIcatServer server, String datasetId)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getParametersInDataset(sessionId, Long.valueOf(datasetId));
        } catch (MalformedURLException ex) {
            logger.error("getDatasetInfo: " + ex.getMessage());
        }
        return new ArrayList<TDatasetParameter>();
    }

    /**
     * This method returns the datafile name corresponding to a given datasetid
     * in a given input server
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param datasetId
     * @return
     * @throws TopcatException
     */
    public String getDatasetName(EntityManager manager, String topcatSessionId, String facilityName, String datasetId)
            throws TopcatException {
        logger.info("getDatasetName: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), datasetId (" + datasetId + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getDatasetName(userSession.getIcatSessionId(), userSession.getUserId().getServerId(), datasetId);
        } catch (javax.persistence.NoResultException ex) {
        }
        return "";
    }

    /**
     * This method returns the datafile name corresponding to a given datasetid
     * in a given input server
     * 
     * @param sessionId
     * @param server
     * @param datasetId
     * @return
     * @throws TopcatException
     */
    private String getDatasetName(String sessionId, TopcatIcatServer server, String datasetId) throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getDatasetName(sessionId, Long.valueOf(datasetId));
        } catch (MalformedURLException ex) {
            logger.error("getDatasetName: " + ex.getMessage());
        }
        return "";
    }

    /**
     * This method returns datafiles corresponding to a given datasetid in a
     * given input server
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param datasetId
     * @return
     * @throws TopcatException
     */
    public ArrayList<TDatafile> getDatafilesInServer(EntityManager manager, String topcatSessionId,
            String facilityName, String datasetId) throws TopcatException {
        logger.info("getDatafilesInServer: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), datasetId (" + datasetId + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getDatafilesInServer(userSession.getIcatSessionId(), userSession.getUserId().getServerId(),
                    datasetId);
        } catch (javax.persistence.NoResultException ex) {
        }
        return new ArrayList<TDatafile>();
    }

    /**
     * This method returns datafiles corresponding to a given datasetid in a
     * given input server
     * 
     * @param sessionId
     * @param server
     * @param datasetId
     * @return
     * @throws TopcatException
     */
    private ArrayList<TDatafile> getDatafilesInServer(String sessionId, TopcatIcatServer server, String datasetId)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getDatafilesInDataset(sessionId, Long.valueOf(datasetId));
        } catch (MalformedURLException ex) {
            logger.error("getDatafilesInServer: " + ex.getMessage());
        }
        return new ArrayList<TDatafile>();
    }

    /**
     * This method get the parameters corresponding to the input datafileid on
     * the given server.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param datafileId
     * @return
     * @throws TopcatException
     */
    public ArrayList<TDatafileParameter> getDatafileInfo(EntityManager manager, String topcatSessionId,
            String facilityName, String datafileId) throws TopcatException {
        logger.info("getDatafileInfo: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), datafileId (" + datafileId + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getDatafileInfo(userSession.getIcatSessionId(), userSession.getUserId().getServerId(), datafileId);
        } catch (javax.persistence.NoResultException ex) {
        }
        return null;
    }

    /**
     * This method get the parameters corresponding to the input datafileid on
     * the given server.
     * 
     * @param sessionId
     * @param server
     * @param datafileId
     * @return
     * @throws TopcatException
     */
    private ArrayList<TDatafileParameter> getDatafileInfo(String sessionId, TopcatIcatServer server, String datafileId)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getParametersInDatafile(sessionId, Long.valueOf(datafileId));
        } catch (MalformedURLException ex) {
            logger.error("getDatafileInfo: " + ex.getMessage());
        }
        return new ArrayList<TDatafileParameter>();
    }

    /**
     * Get a list of parameter names known to a facility.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws TopcatException
     */
    public ArrayList<String> getParameterNames(EntityManager manager, String topcatSessionId, String facilityName)
            throws TopcatException {
        logger.info("getParameterNames: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getParameterNames(userSession.getIcatSessionId(), userSession.getUserId().getServerId());
        } catch (javax.persistence.NoResultException ex) {
        }
        return null;
    }

    private ArrayList<String> getParameterNames(String sessionId, TopcatIcatServer server) throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getParameterNames(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getParameterNames: " + ex.getMessage());
        }
        return new ArrayList<String>();
    }

    /**
     * Get a list of parameter units for the given facility and parameter name.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param name
     * @return
     * @throws TopcatException
     */
    public ArrayList<String> getParameterUnits(EntityManager manager, String topcatSessionId, String facilityName,
            String name) throws TopcatException {
        logger.info("getParameterUnits: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), name (" + name + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getParameterUnits(userSession.getIcatSessionId(), userSession.getUserId().getServerId(), name);
        } catch (javax.persistence.NoResultException ex) {
        }
        return null;
    }

    private ArrayList<String> getParameterUnits(String sessionId, TopcatIcatServer server, String name)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getParameterUnits(sessionId, name);
        } catch (MalformedURLException ex) {
            logger.error("getParameterUnits: " + ex.getMessage());
        }
        return new ArrayList<String>();
    }

    /**
     * Get the expected type of the parameter value for the given facility,
     * parameter name and parameter units. If the units are '--ALL--' then
     * return types for all units.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param name
     * @param units
     * @return
     * @throws TopcatException
     */
    public ArrayList<String> getParameterTypes(EntityManager manager, String topcatSessionId, String facilityName,
            String name, String units) throws TopcatException {
        logger.info("getParameterTypes: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), name (" + name + "), units (" + units + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getParameterTypes(userSession.getIcatSessionId(), userSession.getUserId().getServerId(), name, units);
        } catch (javax.persistence.NoResultException ex) {
        }
        return null;
    }

    private ArrayList<String> getParameterTypes(String sessionId, TopcatIcatServer server, String name, String units)
            throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getParameterTypes(sessionId, name, units);
        } catch (MalformedURLException ex) {
            logger.error("getParameterTypes: " + ex.getMessage());
        }
        return null;
    }

    public String getDatafilesDownloadURL(EntityManager manager, String topcatSessionId, String facilityName,
            ArrayList<Long> datafileIds) throws TopcatException {
        logger.info("getDatafilesDownloadURL: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), datafileIds.size (" + datafileIds.size() + ")");
        String result = "";
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(facilityName,
                    userSession.getUserId().getServerId().getVersion(),
                    userSession.getUserId().getServerId().getServerUrl());
            return service.downloadDatafiles(userSession.getIcatSessionId(), datafileIds);
        } catch (MalformedURLException ex) {
            logger.error("getDatafilesDownloadURL: " + ex.getMessage());
        }
        return result;
    }

    /**
     * Get the URL of a file that contains the requested data set for the given
     * facility.
     * 
     * @param manager
     * @param topcatSessionId
     *            a string containing the session id
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            the data set id
     * @return a string containing a URL
     * @throws TopcatException
     */
    public String getDatasetDownloadURL(EntityManager manager, String topcatSessionId, String facilityName,
            Long datasetId) throws TopcatException {
        logger.info("getDatasetDownloadURL: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), datasetId (" + datasetId + ")");
        String result = "";
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(facilityName,
                    userSession.getUserId().getServerId().getVersion(),
                    userSession.getUserId().getServerId().getServerUrl());
            return service.downloadDataset(userSession.getIcatSessionId(), datasetId);
        } catch (MalformedURLException ex) {
            logger.error("getDatasetDownloadURL: " + ex.getMessage());
        }
        return result;
    }

    public List<TopcatUserDownload> getMyDownloadList(EntityManager manager, String topcatSessionId, String facilityName)
            throws TopcatException {
        logger.info("getMyDownloadList: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName + ")");
        TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                topcatSessionId, facilityName);
        manager.createNamedQuery("TopcatUserDownload.cleanup").executeUpdate();
        List<TopcatUserDownload> userDownloads = manager.createNamedQuery("TopcatUserDownload.findByUserId")
                .setParameter("userId", userSession.getUserId()).getResultList();
        return userDownloads;
    }

    public void addMyDownload(EntityManager manager, String topcatSessionId, String facilityName, Date submitTime,
            String downloadName, String status, Date expiryTime, String url, String preparedId) throws TopcatException {
        logger.info("addMyDownload: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), downloadName (" + downloadName + "), status (" + status + "), url (" + url + "), preparedId ("
                + preparedId + ")");
        TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                topcatSessionId, facilityName);
        TopcatUserDownload download = new TopcatUserDownload();
        download.setName(downloadName);
        download.setUrl(url);
        download.setPreparedId(preparedId);
        download.setStatus(status);
        download.setSubmitTime(submitTime);
        download.setUserId(userSession.getUserId());
        download.setExpiryTime(expiryTime);
        manager.persist(download);
    }

    public void updateDownloadStatus(EntityManager manager, String topcatSessionId, String facilityName, String url,
            String updatedUrl, String status) {
        logger.info("updateDownloadStatus: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), url (" + url + "), updatedUrl (" + updatedUrl + "), status (" + status + ")");
        manager.createNamedQuery("TopcatUserDownload.updateStatus").setParameter("url", url)
                .setParameter("updatedUrl", updatedUrl).setParameter("status", status).executeUpdate();
        manager.flush();
    }

    /**
     * Get a list of authentication details for a facility.
     * 
     * @param manager
     * @param facilityName
     *            a string containing the facility name
     * @return a list of TAuthentication
     */
    public List<TAuthentication> getAuthenticationDetails(EntityManager manager, String facilityName) {
        logger.info("getAuthenticationDetails: facilityName (" + facilityName + ")");
        List<TAuthentication> authenticationDetails = new ArrayList<TAuthentication>();

        List<?> icatAuthentications = manager.createNamedQuery("TopcatIcatAuthentication.findByServerName")
                .setParameter("serverName", facilityName).getResultList();
        for (Object icatAuthentication : icatAuthentications) {
            TAuthentication tAuthentication = new TAuthentication();
            tAuthentication.setFacilityName(facilityName);
            tAuthentication.setDisplayName(((TopcatIcatAuthentication) icatAuthentication).getDisplayName());
            tAuthentication.setPluginName(((TopcatIcatAuthentication) icatAuthentication).getPluginName());
            tAuthentication.setType(((TopcatIcatAuthentication) icatAuthentication).getAuthenticationType());
            tAuthentication.setUrl(((TopcatIcatAuthentication) icatAuthentication).getAuthenticationServiceUrl());
            authenticationDetails.add(tAuthentication);
            if (logger.isTraceEnabled()) {
                logger.trace(tAuthentication.toString());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getAuthenticationDetails: returning " + authenticationDetails.size() + " results");
        }
        return authenticationDetails;
    }

    /**
     * Get the data file formats for the given facility.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws TopcatException
     */
    public List<TDatafileFormat> getDatafileFormats(EntityManager manager, String topcatSessionId, String facilityName)
            throws TopcatException {
        logger.info("getDatafileFormats: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getDatafileFormats(userSession.getIcatSessionId(), userSession.getUserId().getServerId());
        } catch (javax.persistence.NoResultException ex) {
            logger.error("getDatafileFormats: " + ex.getMessage());
        }
        return null;
    }

    private List<TDatafileFormat> getDatafileFormats(String sessionId, TopcatIcatServer server) throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listDatafileFormats(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getDatafileFormats: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Get the data set types for the given facility.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws TopcatException
     */
    public List<String> getDatasetTypes(EntityManager manager, String topcatSessionId, String facilityName)
            throws TopcatException {
        logger.info("getDatasetTypes: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName + ")");
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    topcatSessionId, facilityName);
            return getDatasetTypes(userSession.getIcatSessionId(), userSession.getUserId().getServerId());
        } catch (javax.persistence.NoResultException ex) {
            logger.error("getDatasetTypes: " + ex.getMessage());
        }
        return null;
    }

    private List<String> getDatasetTypes(String sessionId, TopcatIcatServer server) throws TopcatException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listDatasetTypes(sessionId);
        } catch (MalformedURLException ex) {
            logger.error("getDatasetTypes: " + ex.getMessage());
        }
        return null;
    }
}
