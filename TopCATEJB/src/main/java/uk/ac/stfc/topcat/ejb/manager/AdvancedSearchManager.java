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
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.NotSupportedException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;

/**
 * This class implements advanced search TODO: implement advancedsearch
 * pagination
 * <p>
 *
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class AdvancedSearchManager {

    private final static Logger logger = Logger.getLogger(AdvancedSearchManager.class.getName());

    /**
     * This method returns all investigations in a given facility that meets the
     * AdvancedSearch details.
     *
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param searchDetails
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> searchAdvancedInvestigationInServer(EntityManager manager, String topcatSessionId,
            String facilityName, TAdvancedSearchDetails searchDetails) throws TopcatException {
        logger.info("searchAdvancedInvestigationInServer: topcatSessionId (" + topcatSessionId + "), facilityName ("
                + facilityName + ")");
        // Get the valid session using topcatSessionId for a facilityName
        // send request to icat server and gather the investigation results.
        ArrayList<TInvestigation> resultInvestigations = null;
        TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                topcatSessionId, facilityName);
        if (userSession != null) {
            resultInvestigations = searchAdvancedInvestigationUsingICATSession(userSession, searchDetails);
        }
        return resultInvestigations;
    }

    /**
     * This method searches *ALL* the servers to get the investigations that
     * meet the advanced search details.
     *
     * @param manager
     * @param topcatSessionId
     * @param searchDetails
     * @return
     * @throws TopcatException
     */
    public ArrayList<TInvestigation> searchAdvancedInvestigation(EntityManager manager, String topcatSessionId,
            TAdvancedSearchDetails searchDetails) throws TopcatException {
        logger.info("searchAdvancedInvestigation: topcatSessionId (" + topcatSessionId + ")");
        // Get the list of valid sessions using topcatSessionId
        // Go through each icat session and gather the results.
        ArrayList<TInvestigation> resultInvestigations = null;
        List<TopcatUserSession> userSessions = null;
        if (searchDetails.getFacilityList().size() == 0) {
            userSessions = UserManager.getValidUserSessionByTopcatSession(manager, topcatSessionId);
        } else {
            userSessions = new ArrayList<TopcatUserSession>();
            for (String facility : searchDetails.getFacilityList()) {
                TopcatUserSession facilitySession = UserManager.getValidUserSessionByTopcatSessionAndServerName(
                        manager, topcatSessionId, facility);
                if (facilitySession != null) {
                    userSessions.add(facilitySession);
                }
            }
        }

        for (int i = 0; i < userSessions.size(); i++) {
            ArrayList<TInvestigation> tmpList = searchAdvancedInvestigationUsingICATSession(userSessions.get(i),
                    searchDetails);
            if (tmpList == null) {
                continue;
            }
            if (resultInvestigations != null) {
                resultInvestigations.addAll(tmpList);
            } else {
                resultInvestigations = tmpList;
            }
        }
        return resultInvestigations;
    }


    /**
     * This method calls the icat instance of the server requested to search of
     * the investigations that meet the input search details.
     *
     * @param session
     * @param searchDetails
     * @return
     * @throws TopcatException
     */
    private ArrayList<TInvestigation> searchAdvancedInvestigationUsingICATSession(TopcatUserSession session,
            TAdvancedSearchDetails searchDetails) throws TopcatException {
        logger.debug("searchAdvancedInvestigationUsingICATSession: Searching server "
                + session.getUserId().getServerId().getServerUrl() + "  with icat session id "
                + session.getIcatSessionId());
        // Get the ICAT Service url
        // call the search using keyword method
        ArrayList<TInvestigation> returnTInvestigations = new ArrayList<TInvestigation>();
        try {
            logger.trace(session.getUserId().getServerId().getName() + " Version:"
                    + session.getUserId().getServerId().getVersion() + "  URL: "
                    + session.getUserId().getServerId().getServerUrl());
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(
                    session.getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(),
                    session.getUserId().getServerId().getServerUrl());
            return service.searchByAdvancedPagination(session.getIcatSessionId(), searchDetails, 0, 200);
        } catch (TopcatException e) {
            throw e;
        } catch (MalformedURLException ex) {
            logger.error("searchAdvancedInvestigationUsingICATSession:" + ex.getMessage());
        } catch (Exception ex) {
            logger.error("searchAdvancedInvestigationUsingICATSession: (Unknown expetion)" + ex.getMessage());
        }
        return returnTInvestigations;
    }


    /**
     * This method returns all datafiles that match the parameter if given else
     * the instrument and Run number range.
     *
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param searchDetails
     * @return
     * @throws TopcatException
     */
    public ArrayList<TDatafile> searchAdvancedDatafilesInServer(EntityManager manager, String topcatSessionId,
            String facilityName, TAdvancedSearchDetails searchDetails) throws TopcatException {
        logger.info("searchAdvancedDatafilesInServer: topcatSessionId (" + topcatSessionId + "), facilityName ("
                + facilityName + ")");
        // Get the valid session using topcatSessionId for a facilityName
        // send request to icat server and gather the investigation results.
        ArrayList<TDatafile> resultDatafiles = null;
        TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager,
                topcatSessionId, facilityName);
        if (userSession != null) {
            if (searchDetails.getParameterName() != null) {
                resultDatafiles = searchDatafilesByParameterUsingICATSession(userSession, searchDetails);
            } else {
                resultDatafiles = searchDatafilesByRunnoUsingICATSession(userSession, searchDetails);
            }
        }
        return resultDatafiles;
    }

    /**
     * This method calls the icat instance of the server requested to search for
     * datafiles that correspond to given parameter
     *
     * @param session
     * @param searchDetails
     * @return
     * @throws TopcatException
     */
    private ArrayList<TDatafile> searchDatafilesByParameterUsingICATSession(TopcatUserSession session,
            TAdvancedSearchDetails searchDetails) throws TopcatException {
        logger.debug("searchDatafilesByParameterUsingICATSession: Searching server "
                + session.getUserId().getServerId().getServerUrl() + "  with icat session id "
                + session.getIcatSessionId());
        logger.trace("Search Details: Parameter name" + searchDetails.getParameterName() + "  Parameter vale:"
                + searchDetails.getParameterValue() + "  Parameter units:" + searchDetails.getParameterUnits());
        ArrayList<TDatafile> returnTDatafiles = new ArrayList<TDatafile>();
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(
                    session.getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(),
                    session.getUserId().getServerId().getServerUrl());
            return service.searchDatafilesByParameter(session.getIcatSessionId(), searchDetails);
        } catch (TopcatException e) {
            throw e;
        } catch (MalformedURLException ex) {
            logger.error("searchDatafilesByParameterUsingICATSession:" + ex.getMessage());
        } catch (Exception ex) {
            logger.error("searchDatafilesByParameterUsingICATSession: (Unknown expetion)" + ex.getMessage());
            ex.printStackTrace();
        }
        return returnTDatafiles;
    }

    /**
     * This method calls the icat instance of the server requested to search for
     * datafiles that correspond to given instrument and run number range
     *
     * @param session
     * @param searchDetails
     * @return
     * @throws TopcatException
     */
    private ArrayList<TDatafile> searchDatafilesByRunnoUsingICATSession(TopcatUserSession session,
            TAdvancedSearchDetails searchDetails) throws TopcatException {
        logger.debug("searchDatafilesByRunnoUsingICATSession: Searching server "
                + session.getUserId().getServerId().getServerUrl() + "  with icat session id "
                + session.getIcatSessionId());
        logger.trace("Search Details: RunNumber Start" + searchDetails.getRbNumberStart() + "  RunNumber End:"
                + searchDetails.getRbNumberEnd());
        ArrayList<TDatafile> returnTDatafiles = new ArrayList<TDatafile>();
        try {
            float startRun = 0;
            float endRun = 0;
            if (searchDetails.getRbNumberStart() != null && (!searchDetails.getRbNumberStart().isEmpty())) {
                startRun = Float.parseFloat(searchDetails.getRbNumberStart());
            }
            if (searchDetails.getRbNumberEnd() != null && (!searchDetails.getRbNumberEnd().isEmpty())) {
                endRun = Float.parseFloat(searchDetails.getRbNumberEnd());
            }
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(
                    session.getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(),
                    session.getUserId().getServerId().getServerUrl());
            return service.searchDatafilesByRunNumber(session.getIcatSessionId(), searchDetails.getInstrumentList(),
                    startRun, endRun);
        } catch (TopcatException e) {
            throw e;
        } catch (MalformedURLException ex) {
            logger.error("searchDatafilesByRunnoUsingICATSession:" + ex.getMessage());
        } catch (Exception ex) {
            logger.error("searchDatafilesByRunnoUsingICATSession: (Unknown expetion)" + ex.getMessage());
            ex.printStackTrace();
        }
        return returnTDatafiles;
    }

    public ArrayList<TDataset> searchForDatasetsByParameter(EntityManager manager, String topcatSessionId,
            String facilityName, TAdvancedSearchDetails searchDetails) throws TopcatException {
        return null; // TODO
    }

}
