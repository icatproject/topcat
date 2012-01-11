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
package uk.ac.stfc.topcat.ejb.session;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.ejb.manager.AdvancedSearchManager;
import uk.ac.stfc.topcat.ejb.manager.BasicSearchManager;

/**
 * This is stateless bean implementation for searching icat servers.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */

@Stateless
public class SearchManagementBean implements SearchManagementBeanLocal {
    @PersistenceContext(unitName = "TopCATEJBPU")
    protected EntityManager manager;
    
    private BasicSearchManager basicSearchManager;
    private AdvancedSearchManager advancedSearchManager;

    public SearchManagementBean() {
        basicSearchManager=new BasicSearchManager();
        advancedSearchManager = new AdvancedSearchManager();
    }
    
    public ArrayList<TInvestigation> searchBasicInvestigationByKeywords(String topcatSessionId,ArrayList<String> keywords) {
        return basicSearchManager.searchBasicInvestigationByKeywords(manager,topcatSessionId, keywords);
    }

    public ArrayList<TInvestigation> searchBasicInvestigationByKeywordsInServer(String topcatSessionId, String serverName, ArrayList<String> keywords) {
        return basicSearchManager.searchBasicInvestigationByKeywordsInServer(manager,topcatSessionId,serverName,keywords);
    }

    public ArrayList<TInvestigation> searchBasicMyInvestigationByKeywordsInServer(String topcatSessionId, String serverName, ArrayList<String> keywords) {
        return basicSearchManager.searchBasicMyInvestigationByKeywordsInServer(manager,topcatSessionId,serverName,keywords);
    }

    public ArrayList<TInvestigation> searchBasicMyInvestigationByKeywords(String topcatSessionId, List<String> keywords) {
       return basicSearchManager.searchBasicMyInvestigationByKeywords(manager,topcatSessionId,keywords);
    }

    public ArrayList<TInvestigation> searchAdvancedInvestigation(String topcatSessionId,TAdvancedSearchDetails searchDetails) {
        return advancedSearchManager.searchAdvancedInvestigation(manager,topcatSessionId, searchDetails);
    }

    public ArrayList<TInvestigation> searchAdvancedInvestigationInServer(String topcatSessionId, String serverName,TAdvancedSearchDetails searchDetails) {
        return advancedSearchManager.searchAdvancedInvestigationInServer(manager,topcatSessionId,serverName,searchDetails);
    }

    public ArrayList<TDatafile> searchAdvancedDatafileInServer(String topcatSessionId,String serverName,TAdvancedSearchDetails searchDetails){
        return advancedSearchManager.searchAdvancedDatafilesInServer(manager, topcatSessionId, serverName, searchDetails);
    }
}
