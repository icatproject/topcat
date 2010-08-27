/**
 *
 * Copyright (c) 2009-2010
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
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;

/**
 * This class is the manager for basic search of mulitple icat's.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class BasicSearchManager {
    private final static Logger logger = Logger.getLogger(BasicSearchManager.class.getName());
    public BasicSearchManager() {
    }

    /**
     * This method searches all the ICAT server session for investigations having given list of keywords
     * @param manager
     * @param topcatSessionId
     * @param keywords
     * @return
     */
    public ArrayList<TInvestigation> searchBasicInvestigationByKeywords(EntityManager manager,String topcatSessionId, ArrayList<String> keywords) {
        //Get the list of valid sessions using topcatSessionId
        //Go through each icat session and gather the results.
        logger.finest("searchBasicInvestigationByKeywords: using topcatSessionId: ("+topcatSessionId+") Number of keywords:("+keywords.size()+")");
        ArrayList<TInvestigation> resultInvestigations=null;
        List<TopcatUserSession> userSessions= UserManager.getValidUserSessionByTopcatSession(manager, topcatSessionId);
        for(int i=0;i<userSessions.size();i++) {
            ArrayList<TInvestigation> tmpList = searchBasicInvestigationByKeywordsUsingICATSession(userSessions.get(i),keywords);
            if(tmpList==null) continue;
            if(resultInvestigations!=null)
                resultInvestigations.addAll(tmpList);
            else
                resultInvestigations = tmpList;
        }
        return resultInvestigations;
    }

    /**
     * This method searches for investigation using given ICAT Session for keywords.
     * @param session
     * @param keywords
     * @return
     */
    private ArrayList<TInvestigation> searchBasicInvestigationByKeywordsUsingICATSession(TopcatUserSession session, ArrayList<String> keywords) {
        logger.finest("searchBasicInvestigationByKeywordsUsingICATSession: Searching server "+session.getUserId().getServerId().getServerUrl()+"  with iCAT session id "+session.getIcatSessionId());
        // Get the ICAT Service url
        // call the search using keyword method
        ArrayList<TInvestigation> returnTInvestigations = new ArrayList<TInvestigation>();
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(session.getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(), session.getUserId().getServerId().getServerUrl());
            return service.searchByKeywords(session.getIcatSessionId(),keywords);
        } catch (MalformedURLException ex) {
            logger.warning("searchBasicInvestigationByKeywordsUsingICATSession: "+ex.getMessage());
        } catch (Exception ex) {
            logger.warning("searchBasicInvestigationByKeywordsUsingICATSession: (Unknown expetion)"+ex.getMessage());
        }
        return returnTInvestigations;
    }

    /**
     * This method returns all investigations in a given server having keywords.
     * @param manager
     * @param topcatSessionId
     * @param serverName
     * @param keywords
     * @return
     */
    public ArrayList<TInvestigation> searchBasicInvestigationByKeywordsInServer(EntityManager manager, String topcatSessionId, String serverName,ArrayList<String> keywords) {
        //Get the valid session using topcatSessionId for a serverName
        //send request to icat server and gather the investigation results.
        logger.finest("searchBasicInvestigationByKeywordsInServer: Searching server "+serverName+"  with topcat session id "+topcatSessionId+ "number of keywords "+keywords.size());
        ArrayList<TInvestigation> resultInvestigations=null;
        TopcatUserSession userSession= UserManager.getValidUserSessionByTopcatSessionAndServerName(manager, topcatSessionId,serverName);
        if(userSession!=null){
            resultInvestigations = searchBasicInvestigationByKeywordsUsingICATSession(userSession,keywords);
        }
        return resultInvestigations;
    }

    /**
     * This method searches all the ICAT server session for investigations having given list of keywords for investigations belonging to user
     * @param manager
     * @param topcatSessionId
     * @param keywords
     * @return
     */
    public ArrayList<TInvestigation> searchBasicMyInvestigationByKeywords(EntityManager manager,String topcatSessionId, List<String> keywords) {
        //Get the list of valid sessions using topcatSessionId
        //Go through each icat session and gather the results.
        logger.finest("searchBasicMyInvestigationByKeywords: with topcatsessionId"+topcatSessionId+" number of keywords "+keywords.size());
        ArrayList<TInvestigation> resultInvestigations=null;
        List<TopcatUserSession> userSessions= UserManager.getValidUserSessionByTopcatSession(manager, topcatSessionId);
        for(int i=0;i<userSessions.size();i++) {
            ArrayList<TInvestigation> tmpList = searchBasicMyInvestigationByKeywordsUsingICATSession(userSessions.get(i),keywords);
            if(tmpList==null) continue;
            if(resultInvestigations!=null)
                resultInvestigations.addAll(tmpList);
            else
                resultInvestigations = tmpList;
        }
        return resultInvestigations;
    }

    /**
     * This method returns all investigations in a given server having keywords. only gets the
     * investigation that belongs to user.
     * @param manager
     * @param topcatSessionId
     * @param serverName
     * @param keywords
     * @return
     */
    public ArrayList<TInvestigation> searchBasicMyInvestigationByKeywordsInServer(EntityManager manager, String topcatSessionId, String serverName,List<String> keywords) {
        //Get the valid session using topcatSessionId for a serverName
        //send request to icat server and gather the investigation results.
        logger.finest("searchBasicMyInvestigationByKeywordsInServer: Searching server "+ serverName+" with topcatSessionId ("+topcatSessionId+") number of keywords"+keywords.size());
        ArrayList<TInvestigation> resultInvestigations=null;
        TopcatUserSession userSession= UserManager.getValidUserSessionByTopcatSessionAndServerName(manager, topcatSessionId,serverName);
        if(userSession!=null){
            resultInvestigations = searchBasicMyInvestigationByKeywordsUsingICATSession(userSession,keywords);
        }
        return resultInvestigations;
    }

    /**
     * This method searches for investigation using given ICAT Session for keywords. only gets
     * investigations that belong to the user.
     * @param session
     * @param keywords
     * @return
     */
    private ArrayList<TInvestigation> searchBasicMyInvestigationByKeywordsUsingICATSession(TopcatUserSession session, List<String> keywords) {
        // Get the ICAT Service url
        // call the search using keyword method
        logger.finest("searchBasicMyInvestigationByKeywordsUsingICATSession: Searching server "+session.getUserId().getServerId().getServerUrl()+"  with session id "+session.getIcatSessionId());
        ArrayList<TInvestigation> returnTInvestigations = new ArrayList<TInvestigation>();
        try {
            TAdvancedSearchDetails details = new TAdvancedSearchDetails();
            details.getKeywords().addAll(keywords);
            details.getInvestigatorNameList().add(session.getUserId().getUserSurname());
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(session.getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(), session.getUserId().getServerId().getServerUrl());
            return service.searchByAdvancedPagination(session.getIcatSessionId(),details,0,200);
        } catch (MalformedURLException ex) {
            logger.warning("searchBasicMyInvestigationByKeywordsUsingICATSession: "+ex.getMessage());
        } catch (Exception ex) {
            logger.warning("searchBasicMyInvestigationByKeywordsUsingICATSession: (Unknown expetion)"+ex.getMessage());
        }
        return returnTInvestigations;
    }

    /**
     * This method returns keywords that mactch the partial key using iCAT webservice from each server
     * @param session
     * @param partialKey
     * @param numberOfKeywords
     * @return
     */
    private List<String> getKeywordsFromServerWithMatchedKey(TopcatUserSession session,String partialKey,int numberOfKeywords) {
        // Get the ICAT Service url
        // call the get keywords method.
        logger.finest("getKeywordsFromServerWithMatchedKey: getting keywords with the matched key "+partialKey);
        List<String> resultKeywords = null;
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(session.getUserId().getServerId().getName(), session.getUserId().getServerId().getVersion(), session.getUserId().getServerId().getServerUrl());
            return service.getKeywordsForUserWithStartMax(session.getIcatSessionId(),partialKey,numberOfKeywords);
        } catch (MalformedURLException ex) {
            logger.warning("getKeywordsFromServerWithMatchedKey: "+ex.getMessage());
        } 
        return resultKeywords;
    }

}
