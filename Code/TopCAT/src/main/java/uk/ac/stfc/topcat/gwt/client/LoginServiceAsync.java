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
package uk.ac.stfc.topcat.gwt.client;

/**
 * Imports
 */
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The <code>LoginServiceAsync</code> interface is used to perform asynchronous
 * login and logout of iCat servers.
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public interface LoginServiceAsync {

    /**
     * Login to an iCat server using the given username and password.
     * 
     * @param username
     *            a string containing the user name for logging into iCat
     * @param password
     *            a string containing the password for logging into iCat
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void login(String username, String password, String facilityName, AsyncCallback<String> callback);

    /**
     * Logout of an iCat server.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void logout(String facilityName, AsyncCallback<Void> callback);

    /**
     * Check if a user is logged in to a iCat server.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void isUserLoggedIn(String facilityName, AsyncCallback<Boolean> callback);
}
