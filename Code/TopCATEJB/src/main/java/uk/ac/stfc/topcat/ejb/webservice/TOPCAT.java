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
package uk.ac.stfc.topcat.ejb.webservice;

import java.util.ArrayList;
import java.util.Map;

import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.xml.ws.RequestWrapper;
import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.session.SearchManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UploadManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UtilityLocal;

/**
 * Webservice interface to the TopCAT
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@WebService()
@Stateless()
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class TOPCAT {
    @EJB
    private UserManagementBeanLocal userManagement;
    @EJB
    private SearchManagementBeanLocal searchManagement;
    @EJB
    private UploadManagementBeanLocal uploadManagement;
    @EJB
    private UtilityLocal utility;

    @WebMethod(operationName = "login")
    public String login() throws AuthenticationException {
        return userManagement.login();
    }

    @WebMethod(operationName = "ICATLogin")
    @RequestWrapper(className = "ICATLogin")
    public void ICATLogin(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "serverName") String serverName,
            @WebParam(name = "authenticationType") String authenticationType,
            @WebParam(name = "credentials") Map<String, String> credentials) throws AuthenticationException {
        userManagement.login(sessionId, serverName, authenticationType, credentials);
    }

    @WebMethod(operationName = "logout")
    public void logout(@WebParam(name = "sessionId") String sessionId) throws AuthenticationException {
        userManagement.logout(sessionId);
    }

    @WebMethod(operationName = "searchBasicInvestigationByKeywords")
    public java.util.ArrayList<uk.ac.stfc.topcat.core.gwt.module.TInvestigation> searchBasicInvestigationByKeywords(
            @WebParam(name = "topcatSessionId") String topcatSessionId,
            @WebParam(name = "keywords") java.util.ArrayList<java.lang.String> keywords) {
        return searchManagement.searchBasicInvestigationByKeywords(topcatSessionId, keywords);
    }

    /**
     * Web service operation
     * 
     * @throws TopcatException
     */
    @WebMethod(operationName = "searchBasicInvestigationByKeywordsInServer")
    public java.util.ArrayList<uk.ac.stfc.topcat.core.gwt.module.TInvestigation> searchBasicInvestigationByKeywordsInServer(
            @WebParam(name = "topcatSessionId") String topcatSessionId,
            @WebParam(name = "serverName") String serverName,
            @WebParam(name = "keywords") java.util.ArrayList<java.lang.String> keywords) throws TopcatException {
        // TODO write your implementation code here:
        return searchManagement.searchBasicInvestigationByKeywordsInServer(topcatSessionId, serverName, keywords);
    }

    /**
     * Web service operation
     * 
     * @throws TopcatException
     */
    @WebMethod(operationName = "getMyInvestigationsInServer")
    public java.util.ArrayList<uk.ac.stfc.topcat.core.gwt.module.TInvestigation> getMyInvestigationsInServer(
            @WebParam(name = "topcatSessionId") String topcatSessionId, @WebParam(name = "serverName") String serverName)
            throws TopcatException {
        return (ArrayList<TInvestigation>) utility.getMyInvestigationsInServer(topcatSessionId, serverName);
    }

    /**
     * Web service operation
     * 
     * @throws TopcatException
     */
    @WebMethod(operationName = "getDatafilesDownloadURL")
    public String getDatafilesDownloadURL(@WebParam(name = "topcatSessionId") String topcatSessionId,
            @WebParam(name = "serverName") String serverName,
            @WebParam(name = "datafileIds") java.util.ArrayList<java.lang.Long> datafileIds) throws TopcatException {
        return utility.getDatafilesDownloadURL(topcatSessionId, serverName, datafileIds);
    }

}
