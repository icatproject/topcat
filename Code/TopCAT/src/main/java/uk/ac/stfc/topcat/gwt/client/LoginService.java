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
import java.util.Map;

import uk.ac.stfc.topcat.gwt.client.exception.LoginException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The <code>LoginService</code> interface is used to perform login and logout
 * of iCat servers.
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@RemoteServiceRelativePath("LoginService")
public interface LoginService extends RemoteService {
    /**
     * Utility class for simplifying access to the instance of async service.
     */
    public static class Util {
        private static LoginServiceAsync instance;

        /**
         * Returns the instance value.
         * 
         * @return the <code>LoginServiceAsync</code> instance
         */
        public static LoginServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(LoginService.class);
            }
            return instance;
        }
    }

    /**
     * Login to an iCat server using the given parameters.
     * 
     * @param parameters
     *            a map of parameters that are specific to the
     *            authentication type
     * @param authenticationType
     *            a string containing the authentication type
     * @param facilityName
     *            a string containing the facility name
     * 
     * @return a string containing the session id
     * @throws LoginException
     */
    public String login(Map<String, String> parameters, String authenticationType, String facilityName)
            throws LoginException;

    /**
     * Logout of an iCat server.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @throws LoginException
     */
    public void logout(String facilityName) throws LoginException;

    /**
     * Check if a user is logged in to a iCat server.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @return <code>true</code> if the user is logged in
     */
    public Boolean isUserLoggedIn(String facilityName);

    /**
     * Use the CAS ticket to log in to a iCat server.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param authenticationServiceUrl
     *            a string containing the authentication service url
     * @param ticket
     *            a string containing the authentication ticket
     * @return <code>true</code> if the user is logged in
     * @throws LoginException
     */
    public String loginWithTicket(String facilityName, String authenticationServiceUrl, String ticket)
            throws LoginException;

}
