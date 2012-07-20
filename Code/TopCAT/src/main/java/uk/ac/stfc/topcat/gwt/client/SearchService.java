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
package uk.ac.stfc.topcat.gwt.client;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The <code>SearchService</code> interface is used to perform searches to
 * retrieve lists of keywords, investigations or data files. Searches are done
 * using keywords and advanced search details.
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@RemoteServiceRelativePath("search")
public interface SearchService extends RemoteService {

    /**
     * Get a list of *public* keywords that match the partial key. Limit the
     * number of keywords returned to the given maximum number of keywords.
     * 
     * @param sessionId
     *            a string containing the session id
     * @param serverName
     *            a string containing the server name
     * @param partialKey
     *            a string containing the partial key to search on
     * @param numberOfKeywords
     *            the maximum number of keywords to return
     * @return a list of strings containing keywords
     * 
     */
    List<String> getKeywordsFromServer(String sessionId, String serverName, String partialKey, int numberOfKeywords);

    /**
     * Get a list of investigations that have the given input keywords and are
     * the user's investigations. Search all the iCat servers. <b>NB</b>: the
     * search is case sensitive and return maximum of 200 results.
     * 
     * @param sessionId
     *            a string containing the session id
     * @param keywords
     *            a list of strings containing keywords to search on
     * @return a list of <code>TInvestigation</code> containing investigations
     */
    List<TInvestigation> getSearchResultsMyInvestigationFromKeywords(String sessionId, ArrayList<String> keywords);

    /**
     * Get a list of investigations matching the criteria given in the search
     * details. Search all the iCat servers.
     * 
     * @param sessionId
     *            a string containing the session id
     * @param searchDetails
     *            a <code>TAdvancedSearchDetails</code> containing the search
     *            details
     * @return a list of <code>TInvestigation</code> containing investigations
     */
    List<TInvestigation> getAdvancedSearchResultsInvestigation(String sessionId, TAdvancedSearchDetails searchDetails);

    /**
     * Get a list of data files matching the criteria given in the search
     * details. <b>NB</b>: only instruments and run number start and end used.
     * 
     * @param sessionId
     *            a string containing the session id
     * @param serverName
     *            a string containing the server name
     * @param searchDetails
     *            a <code>TAdvancedSearchDetails</code> containing the search
     *            details
     * @return a list of <code>DatafileModel</code> containing data files
     */
    ArrayList<DatafileModel> getAdvancedSearchResultsDatafile(String sessionId, String serverName,
            TAdvancedSearchDetails searchDetails);
}
