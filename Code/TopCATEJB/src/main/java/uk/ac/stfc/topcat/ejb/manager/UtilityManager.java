/**
 *
 * Copyright (c) 2009-2012
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.exception.ICATMethodNotFoundException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TopcatException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
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
        ArrayList<String> facilityNames = new ArrayList<String>();
        List<TopcatIcatServer> servers = manager.createNamedQuery("TopcatIcatServer.findAll").getResultList();
        for (TopcatIcatServer icatServer : servers) {
            facilityNames.add(icatServer.getName());
        }
        return facilityNames;
    }

    public ArrayList<TFacility> getAllFacilities(EntityManager manager) {
        ArrayList<TFacility> facilityNames = new ArrayList<TFacility>();
        List<TopcatIcatServer> servers = manager.createNamedQuery("TopcatIcatServer.findAll").getResultList();
        for (TopcatIcatServer icatServer : servers) {
            facilityNames.add(new TFacility(icatServer.getName(), icatServer.getServerUrl(),
                    icatServer.getPluginName(), icatServer.getDownloadPluginName(), icatServer
                            .getAuthenticationServiceUrl(), icatServer.getAuthenticationServiceType()));
        }
        return facilityNames;
    }

    /**
     * This method returns all the instrument names from a given server
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @return
     */
    public ArrayList<String> getInstrumentNames(EntityManager manager, String sessionId, String serverName) {
        ArrayList<String> instrumentNames = new ArrayList<String>();
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", sessionId).setParameter("serverName", serverName)
                    .getSingleResult();
            instrumentNames.addAll(getInstrumentNames(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId()));
        } catch (javax.persistence.NoResultException ex) {
            try {
                userSession = (TopcatUserSession) manager
                        .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                        .setParameter("serverName", serverName).getSingleResult();
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
     */
    private List<String> getInstrumentNames(String sessionId, TopcatIcatServer server) {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listInstruments(sessionId);
        } catch (MalformedURLException ex) {
            logger.warning("getInstrumentNames: " + ex.getMessage());
        }
        return new ArrayList<String>();
    }

    /**
     * This method returns all the investigation types from a given server
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @return
     */
    public ArrayList<String> getInvestigationTypes(EntityManager manager, String sessionId, String serverName) {
        ArrayList<String> investigationTypes = new ArrayList<String>();
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", sessionId).setParameter("serverName", serverName)
                    .getSingleResult();
            investigationTypes.addAll(getInvestigationTypes(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId()));
        } catch (javax.persistence.NoResultException ex) {
            try {
                userSession = (TopcatUserSession) manager
                        .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                        .setParameter("serverName", serverName).getSingleResult();
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
     */
    private List<String> getInvestigationTypes(String sessionId, TopcatIcatServer server) {
        try {
            // Get the ICAT webservice client and call get investigation types
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listInvestigationTypes(sessionId);
        } catch (MalformedURLException ex) {
            logger.warning("getInvestigationTypes: " + ex.getMessage());
        } catch (java.lang.NullPointerException ex) {
            logger.warning("getInvestigationTypes: " + ex.getMessage());
        }
        return new ArrayList<String>();
    }

    /**
     * This method implements the return of all the facility cycles from a given
     * server
     * 
     * @param sessionId
     * @param server
     * @return
     */
    private ArrayList<TFacilityCycle> getFacilityCycles(String sessionId, TopcatIcatServer server)
            throws ICATMethodNotFoundException {
        try {
            // Get the ICAT webservice client and call get facility cycles
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listFacilityCycles(sessionId);
        } catch (MalformedURLException ex) {
            logger.warning("getInvestigationTypes: " + ex.getMessage());
        } catch (java.lang.NullPointerException ex) {
            logger.warning("getInvestigationTypes: " + ex.getMessage());
        } catch (Exception ex) {
            throw new ICATMethodNotFoundException(ex.getMessage());
        }
        return new ArrayList<TFacilityCycle>();
    }

    /**
     * This method returns all the investigation types from a given server
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @return
     */
    public ArrayList<TFacilityCycle> getFacilityCyclesWithInstrument(EntityManager manager, String sessionId,
            String serverName, String instrument) throws ICATMethodNotFoundException {
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", sessionId).setParameter("serverName", serverName)
                    .getSingleResult();
            return getFacilityCyclesWithInstrument(userSession.getIcatSessionId(), userSession.getUserId()
                    .getServerId(), instrument);
        } catch (javax.persistence.NoResultException ex) {
            try {
                userSession = (TopcatUserSession) manager
                        .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                        .setParameter("serverName", serverName).getSingleResult();
                return getFacilityCycles(userSession.getIcatSessionId(), userSession.getUserId().getServerId());
            } catch (javax.persistence.NoResultException exinnex) {
            }
        }
        return new ArrayList<TFacilityCycle>();
    }

    /**
     * This method implements the return of all the facility cycles from a given
     * server
     * 
     * @param sessionId
     * @param server
     * @return
     */
    private ArrayList<TFacilityCycle> getFacilityCyclesWithInstrument(String sessionId, TopcatIcatServer server,
            String instrument) throws ICATMethodNotFoundException {
        try {
            // Get the ICAT webservice client and call get facility cycles
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.listFacilityCyclesForInstrument(sessionId, instrument);
        } catch (MalformedURLException ex) {
            logger.warning("getInvestigationTypes: " + ex.getMessage());
        } catch (java.lang.NullPointerException ex) {
            logger.warning("getInvestigationTypes: " + ex.getMessage());
        } catch (Exception ex) {
            throw new ICATMethodNotFoundException(ex.getMessage());
        }
        return new ArrayList<TFacilityCycle>();
    }

    /**
     * This method returns all the investigations of the user as the
     * investigator from a given server.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @return
     */
    public ArrayList<TInvestigation> getMyInvestigationsInServer(EntityManager manager, String sessionId,
            String serverName) {
        try {
            TopcatUserSession userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", sessionId).setParameter("serverName", serverName)
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
     */
    public ArrayList<TInvestigation> getMyInvestigationsInServer(String sessionId, TopcatIcatServer server) {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getMyInvestigations(sessionId);
        } catch (MalformedURLException ex) {
            logger.warning("getMyInvestigationsInServer: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all user investigations of which the user is
     * investigator from a given server and instrument name.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param instrumentName
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getMyInvestigationsInServerAndInstrument(EntityManager manager, String sessionId,
            String serverName, String instrumentName) throws TopcatException {
        try {
            TopcatUserSession userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", sessionId).setParameter("serverName", serverName)
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
    public ArrayList<TInvestigation> getMyInvestigationsInServerAndInstrument(String sessionId,
            TopcatIcatServer server, String userName, String userSurname, String instrumentName) throws TopcatException {
        logger.finest("getMyInvestigationsInServerAndInstrument: Searching for investigation in server:"
                + server.getName() + " username:" + userName + " instrument:" + instrumentName);
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            details.getInstrumentList().add(instrumentName);
            details.getInvestigatorNameList().add(userSurname);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.searchByAdvancedPagination(sessionId, details, 0, 200);
        } catch (MalformedURLException ex) {
            logger.warning("getMyInvestigationsInServerAndInstrument: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigation for the user in a given server
     * and the given instrument.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param instrumentName
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getAllInvestigationsInServerAndInstrument(EntityManager manager, String sessionId,
            String serverName, String instrumentName) throws TopcatException {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
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
    public ArrayList<TInvestigation> getAllInvestigationsInServerAndInstrument(String sessionId,
            TopcatIcatServer server, String userName, String instrumentName) throws TopcatException {
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            details.getInstrumentList().add(instrumentName);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.searchByAdvancedPagination(sessionId, details, 0, 200);
        } catch (MalformedURLException ex) {
            logger.warning("getAllInvestigationsInServerAndInstrument: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all user investigations of which the user is
     * investigator from a given server instrument name and facility cycle.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param instrumentName
     * @param facilityCycle
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getMyInvestigationsInServerInstrumentAndCycle(EntityManager manager,
            String sessionId, String serverName, String instrumentName, TFacilityCycle facilityCycle)
            throws TopcatException {
        try {
            TopcatUserSession userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", sessionId).setParameter("serverName", serverName)
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
    public ArrayList<TInvestigation> getMyInvestigationsInServeInstrumentAndCycle(String sessionId,
            TopcatIcatServer server, String userName, String userSurname, String instrumentName,
            TFacilityCycle facilityCycle) throws TopcatException {
        logger.finest("getMyInvestigationsInServerAndInstrument: Searching for investigation in server:"
                + server.getName() + " username:" + userName + " instrument:" + instrumentName + " facilityCycle:"
                + facilityCycle);
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
            logger.warning("getMyInvestigationsInServeInstrumentAndCycle: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns all the investigation for the user in a given server,
     * the given instrument and facility cycle.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param instrumentName
     * @param facilityCycle
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> getAllInvestigationsInServerInstrumentAndCycle(EntityManager manager,
            String sessionId, String serverName, String instrumentName, TFacilityCycle facilityCycle)
            throws TopcatException {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
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
    public ArrayList<TInvestigation> getAllInvestigationsInServerInstrumentAndCycle(String sessionId,
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
            logger.warning("getAllInvestigationsInServerInstrumentAndCycle: " + ex.getMessage());
        }

        return new ArrayList<TInvestigation>();
    }

    /**
     * This method returns the investigation details from the given server for
     * the given investigation id.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param investigationId
     * @return
     * @throws AuthenticationException
     */
    public TInvestigation getInvestigationDetails(EntityManager manager, String sessionId, String serverName,
            String investigationId) throws AuthenticationException {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
            return getInvestigationDetails(userSession.getIcatSessionId(), userSession.getUserId().getServerId(),
                    investigationId);
        } catch (javax.persistence.NoResultException ex) {
            throw new AuthenticationException("Could not find session information");
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
     */
    public TInvestigation getInvestigationDetails(String sessionId, TopcatIcatServer server, String investigationId)
            throws AuthenticationException {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getInvestigationDetails(sessionId, Long.valueOf(investigationId));
        } catch (MalformedURLException ex) {
            logger.warning("getInvestigationDetails: " + ex.getMessage());
        }
        return new TInvestigation();
    }

    /**
     * This method returns datasets for a given investigation number on given
     * input server
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param investigationNumber
     * @return
     * @throws AuthenticationException
     */
    public ArrayList<TDataset> getDatasetsInServer(EntityManager manager, String sessionId, String serverName,
            String investigationNumber) throws AuthenticationException {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
            return getDatasetsInServer(userSession.getIcatSessionId(), userSession.getUserId().getServerId(),
                    investigationNumber);
        } catch (javax.persistence.NoResultException ex) {
            throw new AuthenticationException("Could not find session information");
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
     */
    public ArrayList<TDataset> getDatasetsInServer(String sessionId, TopcatIcatServer server, String investigationNumber) {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getDatasetsInInvestigation(sessionId, Long.valueOf(investigationNumber));
        } catch (MalformedURLException ex) {
            logger.warning("getDatasetsInServer: " + ex.getMessage());

        } catch (NullPointerException ex) {
            logger.warning("getDatasetsInServer: " + ex.getMessage());
        }
        return new ArrayList<TDataset>();
    }

    /**
     * This method get the parameters corresponding to the input datasetid on
     * the given server.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param datasetId
     * @return
     */
    public ArrayList<TDatasetParameter> getDatasetInfo(EntityManager manager, String sessionId, String serverName,
            String datasetId) {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
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
     */
    public ArrayList<TDatasetParameter> getDatasetInfo(String sessionId, TopcatIcatServer server, String datasetId) {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getParametersInDataset(sessionId, Long.valueOf(datasetId));
        } catch (MalformedURLException ex) {
            logger.warning("getDatasetInfo: " + ex.getMessage());
        }
        return new ArrayList<TDatasetParameter>();
    }

    /**
     * This method returns the datafile name corresponding to a given datasetid
     * in a given input server
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param datasetId
     * @return
     */
    public String getDatasetName(EntityManager manager, String sessionId, String serverName, String datasetId) {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
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
     */
    public String getDatasetName(String sessionId, TopcatIcatServer server, String datasetId) {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getDatasetName(sessionId, Long.valueOf(datasetId));
        } catch (MalformedURLException ex) {
            logger.warning("getDatasetName: " + ex.getMessage());
        }
        return "";
    }

    /**
     * This method returns datafiles corresponding to a given datasetid in a
     * given input server
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param datasetId
     * @return
     */
    public ArrayList<TDatafile> getDatafilesInServer(EntityManager manager, String sessionId, String serverName,
            String datasetId) {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
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
     */
    public ArrayList<TDatafile> getDatafilesInServer(String sessionId, TopcatIcatServer server, String datasetId) {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getDatafilesInDataset(sessionId, Long.valueOf(datasetId));
        } catch (MalformedURLException ex) {
            logger.warning("getDatafilesInServer: " + ex.getMessage());
        }
        return new ArrayList<TDatafile>();
    }

    /**
     * This method get the parameters corresponding to the input datafileid on
     * the given server.
     * 
     * @param manager
     * @param sessionId
     * @param serverName
     * @param datafileId
     * @return
     */
    public ArrayList<TDatafileParameter> getDatafileInfo(EntityManager manager, String sessionId, String serverName,
            String datafileId) {
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
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
     */
    public ArrayList<TDatafileParameter> getDatafileInfo(String sessionId, TopcatIcatServer server, String datafileId) {
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                    server.getVersion(), server.getServerUrl());
            return service.getParametersInDatafile(sessionId, Long.valueOf(datafileId));
        } catch (MalformedURLException ex) {
            logger.warning("getDatafileInfo: " + ex.getMessage());
        }
        return new ArrayList<TDatafileParameter>();
    }

    public String getDatafilesDownloadURL(EntityManager manager, String sessionId, String serverName,
            ArrayList<Long> datafileIds) {
        String result = "";
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, serverName);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(serverName,
                    userSession.getUserId().getServerId().getVersion(),
                    userSession.getUserId().getServerId().getServerUrl());
            return service.downloadDatafiles(userSession.getIcatSessionId(), datafileIds);
        } catch (MalformedURLException ex) {
            Logger.getLogger(UtilityManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Get the URL of a file that contains the requested data set for the given
     * facility.
     * 
     * @param manager
     * @param sessionId
     *            a string containing the session id
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            the data set id
     * @return a string containing a URL
     */
    public String getDatasetDownloadURL(EntityManager manager, String sessionId, String facilityName, Long datasetId) {
        String result = "";
        try {
            TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                    sessionId, facilityName);
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(facilityName,
                    userSession.getUserId().getServerId().getVersion(),
                    userSession.getUserId().getServerId().getServerUrl());
            return service.downloadDataset(userSession.getIcatSessionId(), datasetId);
        } catch (MalformedURLException ex) {
            Logger.getLogger(UtilityManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public List<TopcatUserDownload> getMyDownloadList(EntityManager manager, String sessionId, String facilityName) {
        TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager, sessionId,
                facilityName);
        manager.createNamedQuery("TopcatUserDownload.cleanup").executeUpdate();
        List<TopcatUserDownload> userDownloads = manager.createNamedQuery("TopcatUserDownload.findByUserId")
                .setParameter("userId", userSession.getUserId()).getResultList();
        return userDownloads;
    }

    public void addMyDownload(EntityManager manager, String sessionId, String facilityName, Date submitTime,
            String downloadName, String status, Date expiryTime, String url) {
        TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager, sessionId,
                facilityName);
        TopcatUserDownload download = new TopcatUserDownload();
        download.setName(downloadName);
        download.setUrl(url);
        download.setStatus(status);
        download.setSubmitTime(submitTime);
        download.setUserId(userSession.getUserId());
        download.setExpiryTime(expiryTime);
        manager.persist(download);
    }

    public void updateDownloadStatus(EntityManager manager, String sessionId, String facilityName, String url,
            String updatedUrl, String status) {
        manager.createNamedQuery("TopcatUserDownload.updateStatus").setParameter("url", url)
                .setParameter("updatedUrl", updatedUrl).setParameter("status", status).executeUpdate();
        manager.flush();
    }

}
