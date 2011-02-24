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

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.ejb.manager.UserManager;

/**
 * Session Bean implementation class UserManagementBean which includes user login, logout etc.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@Stateless
public class UserManagementBean implements UserManagementBeanLocal {

    @PersistenceContext(unitName = "TopCATEJBPU")
    protected EntityManager manager;
    private UserManager userManager;

    /**
     * Default constructor. 
     */
    public UserManagementBean() {
        userManager = new UserManager();
    }

    protected UserManagementBean(EntityManager manager) {
        this.manager = manager;
        userManager = new UserManager();
    }

    @Override
    /**
     * This method logs out user
     * @param sessionId : Session id used for communicating with TopCAT
     */
    public void logout(String sessionId) throws AuthenticationException {
        userManager.logout(manager,sessionId);
    }

    @Override
    /**
     * This method logs out user from a server
     * @param sessionId : Session id used for communicating with TopCAT
     * @param serverName : iCAT Server name
     */
    public void logout(String sessionId,String serverName) throws AuthenticationException {
        userManager.logout(manager,sessionId,serverName);
    }


    @Override
    /**
     * This method login to TopCAT and retun a session id string. this string should be used for
     * communicating with the TopCAT System.
     * @return Session id string value
     */
    public String login() throws AuthenticationException {
        return userManager.login(manager);
    }

    @Override
    /**
     * This method authenticates and authorises a user with session id to an ICAT server so that the
     * further opertaion with TopCAT will uses this new credential rather than anonymous user login to icat server.
     * @param sessionId : sessionId used to communicate with TopCAT.
     * @param serverName: ICAT Server name to authenticate with.
     * @param username: username for ICAT server.
     * @param password: password for ICAT server.
     * @param hours: number of hours the authentication to be valid.
     */
    public void login(String sessionId, String serverName, String username,
            String password, long hours) throws AuthenticationException {
            userManager.login(manager,sessionId, serverName, username, password, hours);
    }

    /**
     * This method checks whether the login session is valid 
     * @param sessionId sessionId used to communicate with TopCAT
     * @param serverName ICAT Server name to check with
     */
    public Boolean isSessionValid(String sessionId, String serverName){
            return userManager.isSessionValid(manager, sessionId, serverName);
    }
    /**
     * This method is a timer service to automatically update the anonymous login
     * to icat servers.
     */
    @Schedule(hour="*/6")
    public void anonymousLogin(){
        userManager.anonymousUserLogin(manager);
    }
}
