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
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.ejb.entity.TopcatKeywords;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;
import uk.ac.stfc.topcat.ejb.utils.Configuration;

/**
 * This class manages the keywords updating from the servers and serving the
 * keywords.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class KeywordManager {

    /**
     * This is fixed number of keywords that can be returned.
     */
    private static int maxResultKeywords = 501;
    private final static Logger logger = Logger.getLogger(KeywordManager.class.getName());

    /**
     * This method gets all the keywords from the icat server using the session
     * id and the icat server url. NOTE: This mehtod may fail if there are lots
     * of keywords in icat server.
     * 
     * @param sessionId
     *            : Sesssion id for the icat server.
     * @param serverURL
     *            : icat server URL.
     * @return : list of keywords
     * @throws TopcatException
     */
    private List<String> getKeywordsFromServer(String sessionId, String serverName, String serverVersion,
            String serverURL) throws TopcatException {
        List<String> resultKeywords = null;
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(serverName,
                    serverVersion, serverURL);
            resultKeywords = service.getKeywordsForUser(sessionId);
        } catch (MalformedURLException ex) {
            logger.warning("getKeywordsFromServer: " + ex.getMessage());
        }
        return resultKeywords;
    }

    /**
     * This method gets the given number of investigations starting at the start
     * index from icat server.
     * 
     * @param sessionId
     *            : session id for the icat server
     * @param serverURL
     *            : url of the icat server
     * @param startIndex
     *            : start index of the result investigation to return
     * @param count
     *            : number of investigations to return
     * @return: list of investigations.
     */
    private List<TInvestigation> getInvestigationsFromServer(String sessionId, String serverName, String serverVersion,
            String serverURL, int startIndex, int count) {
        logger.finest("getInvestigationsFromServer: from Server " + serverURL + " iCAT sessionId " + sessionId
                + " startIndex: " + startIndex + " count:" + count);
        List<TInvestigation> resultInvestigations = null;
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(serverName,
                    serverVersion, serverURL);
            return service.searchByAdvancedPagination(sessionId, details, startIndex, count);
        } catch (MalformedURLException ex) {
            logger.warning("getInvestigationsFromServer: " + ex.getMessage());
        } catch (javax.xml.ws.WebServiceException ex) {
            logger.warning("getInvestigationsFromServer: " + ex.getMessage());
        } catch (TopcatException ex) {
            logger.warning("getInvestigationsFromServer: " + ex.getMessage());
        }
        return resultInvestigations;
    }

    /**
     * This method returns keywords for a given investigation from the icat
     * server.
     * 
     * @param sessionId
     *            : session id for the icat server
     * @param serverURL
     *            : icat server url
     * @param inv
     *            : investigation information
     * @return : keywords corresponding to input investigation
     * @throws TopcatException
     */
    private ArrayList<String> getKeywordsOfInvestigations(String sessionId, String serverName, String serverVersion,
            String serverURL, TInvestigation inv) throws TopcatException {
        logger.finest("getKeywordsOfInvestigations: from server " + serverURL + " with iCAT sessionId: " + sessionId);
        ArrayList<String> resultKeywords = null;
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(serverName,
                    serverVersion, serverURL);
            return service.getKeywordsInInvestigation(sessionId, Long.valueOf(inv.getInvestigationId()));
        } catch (MalformedURLException ex) {
            logger.warning("getKeywordsOfInvestigations: " + ex.getMessage());
        }
        return resultKeywords;
    }

    /**
     * This method is to update the local cache of keywords from different ICAT
     * servers TODO: Problem extracting all the keywords from ICAT server due to
     * session timeout TODO: and injesting each investigation keyword creates
     * out of memory issues.
     * 
     * @param manager
     * @param session
     * @throws TopcatException
     */
    private void UpdateKeywordsFromServerUsingInvestigations(EntityManager manager, TopcatUserSession session)
            throws TopcatException {
        int pageSize = 200;
        // This is a complex bit of getting the loop of investigation by pagning
        // through all public investigations
        for (int invCount = 0;; invCount += pageSize) {
            List<TInvestigation> allPublicInvestigations = getInvestigationsFromServer(session.getIcatSessionId(),
                    session.getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(),
                    session.getUserId().getServerId().getServerUrl(), invCount, pageSize);
            if (allPublicInvestigations == null) {
                break;
            }
            for (TInvestigation inv : allPublicInvestigations) {
                // Get all the keywords from the server for given investigation
                ArrayList<String> resultKeywords = getKeywordsOfInvestigations(session.getIcatSessionId(), session
                        .getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(), session
                        .getUserId().getServerId().getServerUrl(), inv);
                if (resultKeywords == null) {
                    return;
                }

                int count = 0;
                // Update the database
                for (String key : resultKeywords) {
                    TopcatKeywords keyword = new TopcatKeywords();
                    keyword.setKeyword(key);
                    try {
                        TopcatKeywords tKey = manager.find(TopcatKeywords.class, keyword.getKeyword());
                        if (tKey == null) {
                            manager.persist(keyword);

                        }
                        if (count == 100) {
                            manager.flush();
                            count = 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // System.out.println("Carry on with the exception of duplicate keys"+key.getKeywordPK().getName());
                    }
                    count++;
                }
                manager.flush();
            }
        }

    }

    /**
     * This method updates the local cache of keywords using a session to icat
     * server.
     * 
     * @param manager
     *            : entity manager
     * @param session
     *            : session to connect to icat server. NOTE: This method takes a
     *            days to update. need to figure out best way to updating the
     *            local cache
     * @throws TopcatException
     */
    private void UpdateKeywordsFromServer(EntityManager manager, TopcatUserSession session) throws TopcatException {
        // Get all the keywords from the server
        List<String> resultKeywords = getKeywordsFromServer(session.getIcatSessionId(), session.getUserId()
                .getServerId().getName(), session.getUserId().getServerId().getVersion(), session.getUserId()
                .getServerId().getServerUrl());
        if (resultKeywords == null) {
            return;
        }
        int count = 0;
        // Update the database
        for (String key : resultKeywords) {
            TopcatKeywords keyword = new TopcatKeywords();
            keyword.setKeyword(key);
            try {
                TopcatKeywords tKey = manager.find(TopcatKeywords.class, keyword.getKeyword());
                if (tKey == null) {
                    manager.persist(keyword);
                }
                if (count == 100) {
                    manager.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Carry on with the exception of duplicate keys");
            }
            count++;
        }
    }

    /**
     * This method gets all the anonymous login information to connect to
     * multiple icat servers and then updates all the keywords to local cache.
     * 
     * @param manager
     * @throws TopcatException
     */
    public void UpdateKeywordsFromAll(EntityManager manager) throws TopcatException {
        // Get all the server info
        List<TopcatUserSession> sessionList = manager.createNamedQuery("TopcatUserSession.findByAnonymous")
                .getResultList();
        if (sessionList == null) {
            return;
        }
        // update keywords from individual servers
        for (TopcatUserSession session : sessionList) {
            // UpdateKeywordsFromServer(manager,session);
            // TODO: Changing here to how the keywords are added
            UpdateKeywordsFromServerUsingInvestigations(manager, session);
        }
    }

    /**
     * This method returns the keywords with a given prefix. fetches from local
     * cache rather than individual icat servers.
     * 
     * @param manager
     *            : entity manager for the icat server.
     * @param prefix
     *            : prefix for the return keywords
     * @return: list of keywords at the maximum number given by the
     *          maxResultKeywords
     */
    private ArrayList<String> getKeywordsWithPrefixFromLocalCache(EntityManager manager, String prefix) {
        ArrayList<String> keywords = new ArrayList<String>();
        String prefixString = prefix.toLowerCase() + "%";
        manager.createNativeQuery("alter session set NLS_COMP=LINGUISTIC").executeUpdate();
        manager.createNativeQuery("alter session set NLS_SORT=BINARY_CI").executeUpdate();
        List<TopcatKeywords> keywordList = manager.createNamedQuery("TopcatKeywords.findByPrefix")
                .setParameter("prefix", prefixString).setMaxResults(maxResultKeywords).getResultList();
        if (keywordList == null) {
            return keywords;
        }
        for (TopcatKeywords key : keywordList) {
            keywords.add(key.getKeyword());
        }
        return keywords;
    }

    /**
     * This method returns the keywords with the partialkey match. depending on
     * the topcat configuration the keywords can be retrived from local cache or
     * via webservice.
     * 
     * @param manager
     *            : entity manager for the icat server.
     * @param sessionId
     *            : Topcat session id
     * @param ServerName
     *            : icat Server name
     * @param partialKey
     *            : partial keyword
     * @param numberOfKeywords
     *            : number of keywords to return
     * @return: list of keywords at the maximum number given by the
     *          maxResultKeywords
     * @throws TopcatException
     */
    public ArrayList<String> getKeywordsWithPrefix(EntityManager manager, String sessionId, String serverName,
            String partialKey, int numberOfKeywords) throws TopcatException {

        // Check the configuration setting for cache
        if (Configuration.INSTANCE.isKeywordsCached()) {
            return getKeywordsWithPrefixFromLocalCache(manager, partialKey);
        } else {
            return getKeywordsWithPrefixFromWebservice(manager, sessionId, serverName, partialKey, numberOfKeywords);
        }
    }

    /**
     * This method gets the keywords from the webservice.
     * 
     * @param sessionId
     *            : Topcat session id
     * @param ServerName
     *            : icat Server name
     * @param partialKey
     *            : partial keyword
     * @param numberOfKeywords
     *            : number of keywords to return
     * @return if there is valid session id then it will return all the keywords
     *         the user have access to.
     * @throws TopcatException
     */
    private ArrayList<String> getKeywordsWithPrefixFromWebservice(EntityManager manager, String sessionId,
            String serverName, String partialKey, int numberOfKeywords) throws TopcatException {
        ArrayList<String> keywords = new ArrayList<String>();
        // Get the user session id from the topcat session id
        logger.finest("getKeywordsWithPrefixFromWebservice: TopcatSessionId (" + sessionId + ") serverName ("
                + serverName + ")");
        TopcatUserSession userSession = UserManager.getValidUserSessionByTopcatSessionAndServerName(manager, sessionId,
                serverName);
        if (userSession != null) {
            try {
                ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(
                        userSession.getUserId().getServerId().getName(),
                        userSession.getUserId().getServerId().getVersion(),
                        userSession.getUserId().getServerId().getServerUrl());
                keywords = service.getKeywordsForUserWithStartMax(userSession.getIcatSessionId(), partialKey,
                        numberOfKeywords);
            } catch (MalformedURLException ex) {
                logger.warning("getKeywordsWithPrefixFromWebservice: " + ex.getMessage());
            }
        }
        return keywords;
    }
}
