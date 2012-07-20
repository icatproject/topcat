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
package uk.ac.stfc.topcat.gwt.server;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.ejb.session.KeywordManagementLocal;
import uk.ac.stfc.topcat.ejb.session.SearchManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;
import uk.ac.stfc.topcat.gwt.client.SearchService;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This is Search Service servlet implementation, it provides basic and advanced
 * search options on ICAT for AJAX
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@SuppressWarnings("serial")
public class SearchServiceImpl extends RemoteServiceServlet implements SearchService {

    private SearchManagementBeanLocal searchManager = null;
    private KeywordManagementLocal keywordManager = null;
    private UserManagementBeanLocal userManager = null;

    /*
     * This is servlet initialisation code. creates search, keyword and user
     * managers (local interface for EJB session beans's) (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);

        try {
            // create initial context
            Context ctx = new InitialContext();
            searchManager = (SearchManagementBeanLocal) ctx
                    .lookup("java:global/TopCAT/SearchManagementBean!uk.ac.stfc.topcat.ejb.session.SearchManagementBeanLocal");
            keywordManager = (KeywordManagementLocal) ctx
                    .lookup("java:global/TopCAT/KeywordManagementBean!uk.ac.stfc.topcat.ejb.session.KeywordManagementLocal");
            userManager = (UserManagementBeanLocal) ctx
                    .lookup("java:global/TopCAT/UserManagementBean!uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal");

        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * This method returns the *public* keywords from the server that matches
     * the partial keys and given maximum number of keywords. (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.SearchService#getKeywordsFromServer(java
     * .lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public List<String> getKeywordsFromServer(String sessionId, String ServerName, String partialKey,
            int numberOfKeywords) {
        if (sessionId == null)
            sessionId = getSessionId();
        List<String> results = keywordManager
                .getKeywordsWithPrefix(sessionId, ServerName, partialKey, numberOfKeywords);
        return results;
    }

    /*
     * This method searches all the icat servers for user investigations(User as
     * Investigator) that has given input keywords ***WARNING: Search is CASE
     * Sensitive and return maximum of 200 results*** (non-Javadoc)
     * 
     * @see uk.ac.stfc.topcat.gwt.client.SearchService#
     * getSearchResultsMyInvestigationFromKeywords(java.lang.String,
     * java.util.List)
     */
    @Override
    public List<TInvestigation> getSearchResultsMyInvestigationFromKeywords(String sessionId, ArrayList<String> keywords) {
        if (sessionId == null)
            sessionId = getSessionId();
        List<TInvestigation> investigationList = searchManager
                .searchBasicMyInvestigationByKeywords(sessionId, keywords);
        return investigationList;
    }

    /*
     * This method searches all the icat servers for investigations with the
     * input search parameters. (non-Javadoc)
     * 
     * @see uk.ac.stfc.topcat.gwt.client.SearchService#
     * getAdvancedSearchResultsInvestigation(java.lang.String,
     * uk.ac.stfc.topcat.ejb.gwt.module.TAdvancedSearchDetails)
     */
    @Override
    public List<TInvestigation> getAdvancedSearchResultsInvestigation(String sessionId,
            TAdvancedSearchDetails searchDetails) {
        if (sessionId == null)
            sessionId = getSessionId();
        List<TInvestigation> investigationList = searchManager.searchAdvancedInvestigation(sessionId, searchDetails);
        return investigationList;
    }

    /**
     * This methods searches for the datafiles that meet the criteria given in
     * the searchDetails NOTE: only instruments and run number start and end
     * used. (non-Javadoc)
     * 
     * @see uk.ac.stfc.topcat.gwt.client.SearchService#getAdvancedSearchResultsDatafile(java.lang.String,
     *      uk.ac.stfc.topcat.ejb.gwt.module.TAdvancedSearchDetails)
     */
    @Override
    public ArrayList<DatafileModel> getAdvancedSearchResultsDatafile(String sessionId, String serverName,
            TAdvancedSearchDetails searchDetails) {
        if (sessionId == null)
            sessionId = getSessionId();
        List<TDatafile> datafileList = searchManager.searchAdvancedDatafileInServer(sessionId, serverName,
                searchDetails);
        // Convert TDatafiles to DatafileModel
        ArrayList<DatafileModel> datafileModel = new ArrayList<DatafileModel>();
        for (TDatafile datafile : datafileList) {
            datafileModel.add(new DatafileModel(serverName, null, datafile.getId(), datafile.getName(), datafile
                    .getSize().toString(), datafile.getFormat(), datafile.getFormatVersion(), datafile.getFormatType(),
                    datafile.getCreateTime(), datafile.getLocation()));
        }
        return datafileModel;
    }

    /**
     * This method returns the session id from the Servlet SESSION variable
     * ***WARNING: only works if the user browser cookies are enable ***
     * 
     * @return
     */
    private String getSessionId() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String sessionId = null;
        if (session.getAttribute("SESSION_ID") == null) { // First time login
            try {
                sessionId = userManager.login();
                session.setAttribute("SESSION_ID", sessionId);
            } catch (AuthenticationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sessionId = (String) session.getAttribute("SESSION_ID");
        }
        return sessionId;
    }

}
