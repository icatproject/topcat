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
package uk.ac.stfc.topcat.gwt.server;

/**
 * Imports
 */
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;
import uk.ac.stfc.topcat.gwt.client.LoginService;
import uk.ac.stfc.topcat.gwt.client.exception.LoginException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This class is a servlet implementation providing the login service to the gwt
 * AJAX client
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
    @EJB
    private UserManagementBeanLocal userManager;
    private final static Logger logger = Logger.getLogger(LoginServiceImpl.class.getName());

    /**
     * This method initializes the servlet, creates a usermanagementbean. this
     * will be used by the servlet to perform usermanagement operations.
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);        
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.stfc.topcat.gwt.client.LoginService#login(java.util.Map,
     * java.lang.String, java.lang.String)
     */
    @Override
    public String login(Map<String, String> parameters, String authenticationType, String facilityName)
            throws LoginException {
        String sessionId = getTopcatSessionId();
        
        logger.info("login as map method: authenticationType: " + authenticationType + ", facilityName: " + facilityName);
        
        try {
            userManager.login(sessionId, facilityName, authenticationType, parameters);
        } catch (AuthenticationException e) {
            throw (new LoginException(e.getMessage()));
        }

        return sessionId;
    }
    
    @Override
    public Boolean login(String icatSessionId, String authenticationType, String facilityName)
            throws LoginException {        
        String sessionId = getTopcatSessionId();
        
        logger.info("login as icatSessionId: " +  icatSessionId + ", authenticationType: " + authenticationType + ", facilityName: " + facilityName);
        
        Boolean loginSuccess = false;
        
        try {
            loginSuccess = userManager.login(sessionId, facilityName, authenticationType, icatSessionId);
        } catch (AuthenticationException e) {
            throw (new LoginException(e.getMessage()));
        }
        
        return loginSuccess;
    }
    

    /*
     * This method performs logout operation from an iCAT server. (non-Javadoc)
     * 
     * @param facilityName name of iCAT instance
     * 
     * @see uk.ac.stfc.topcat.gwt.client.LoginService#logout(java.lang.String)
     */
    @Override
    public void logout(String facilityName) throws LoginException {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String sessionId = null;
        if (session.getAttribute("SESSION_ID") == null) { // First time login
            throw new LoginException("Session not valid");
        } else {
            sessionId = (String) session.getAttribute("SESSION_ID");
        }
        try {
            userManager.logout(sessionId, facilityName);
        } catch (AuthenticationException e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public Boolean isUserLoggedIn(String facilityName) {
        String topcatSessionId = getTopcatSessionId();
        return userManager.isSessionValid(topcatSessionId, facilityName);
    }

    @Override
    public String getSessionId(String facilityName) throws TopcatException {
        return userManager.getIcatSessionId(getTopcatSessionId(), facilityName);
    }

    /**
     * This method returns the session id from the Servlet SESSION variable
     * ***WARNING: only works if the user browser cookies are enable ***
     * 
     * @return
     */
    private String getTopcatSessionId() {
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
