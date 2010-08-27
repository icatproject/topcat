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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserInfo;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatServer;
import uk.ac.stfc.topcat.ejb.entity.TopcatUser;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;

/**
 * This class manages the user login and logouts etc. for TopCAT
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class UserManager {

    private final static Logger logger = Logger.getLogger(UserManager.class.getName());

    public UserManager() {
    }

    /**
     * This method returns the default user / Anonymous user. this will be used for
     * login to resoures where user doen't have a login id.
     * @param manager
     * @return Anonymous user
     */
    public TopcatUserInfo getDefaultUser(EntityManager manager) {
        TopcatUserInfo defaultUser = (TopcatUserInfo) manager.createNamedQuery("TopcatUserInfo.findAnonymousUser").getSingleResult();
        return defaultUser;
    }

    /**
     * This method returns a Session ID, the Session ID is a random unique string
     * generated.
     * @param manager
     * @return a Unique string.
     */
    public String login(EntityManager manager) {
        //create UUID for session
        String sid = UUID.randomUUID().toString();
        return sid;
    }

    /**
     * This method login to a requested ICAT Server.
     * @param manager
     * @param topcatSessionId
     * @param serverName
     * @param username
     * @param password
     * @param hours
     * @throws MalformedURLException
     * @throws SessionException_Exception
     */
    public void login(EntityManager manager, String topcatSessionId, String serverName, String username,
            String password, long hours) throws AuthenticationException {
        //Process
        //1) Get the icat server
        //2) Login to the ICAT Server using user name and password
        //3) if the server login failes then throw exception and return.
        //4) if the server login is success then
        //4a) get the user surname from the server
        //5) find the user by searching the user session table using topcatSessionId
        //5a) if found then the user is old one else
        //5b) find the user using the user name and server id in TopcatUser table.
        //5c) If not found in TopcatUser table then create a new user.
        //6) if a session doesn't exist then create a usersession entry for the user and topcatSessionId otherwise update the exisiting session.


        //TODO: don't allow default user to login


        //Get the icat server
        TopcatIcatServer icatServer = findTopcatIcatServerByName(manager, serverName);
        logger.finest("login:" + icatServer.getServerUrl() + " username: " + username);
        //Login to the icat server
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(icatServer.getName(), icatServer.getVersion(), icatServer.getServerUrl());
            String icatSessionId = service.loginLifetime(username, password, (int) hours);
            String userSurname = service.getUserSurname(icatSessionId, username);
            //Get all the topcat user with session id
            TopcatUser user = null;
            TopcatUserSession session = null;
            TopcatUserInfo userInfo = null;

            List<TopcatUserSession> resultSessions = (List<TopcatUserSession>) manager.createNamedQuery("TopcatUserSession.findByTopcatSessionId").setParameter("topcatSessionId", topcatSessionId).getResultList();
            if (resultSessions != null && resultSessions.size() != 0) {
                userInfo = resultSessions.get(0).getUserId().getTopcatUserId();
                //search through the results to get the relavent session
                for (TopcatUserSession us : resultSessions) {
                    if (us.getUserId().getServerId().getId() == icatServer.getId() && us.getUserId().getName().equals(username)) { // found the relavent session
                        session = us;
                        user = session.getUserId();
                        break;
                    }
                }
                if (user == null) { 
                    user = findUserFromUsernameAndServer(userInfo, username, icatServer.getName());
                    if (user == null) {
                        user = new TopcatUser();
                        user.setName(username);
                        user.setServerId(icatServer);
                        user.setTopcatUserId(userInfo);
                        user.setUserSurname(userSurname);
                        manager.persist(user);
                    }
                }
            } else { //This is a Anonymous user session trying to login to first server with session id
                //Check if the user is already a logged in user.
                user = findUserFromUsernameAndServer(manager, username, serverName);
                if (user == null) {//Its a new user
                    //Create a new user
                    userInfo = new TopcatUserInfo();
                    userInfo.setDisplayName(username);
                    userInfo.setHomeServer(icatServer);
                    manager.persist(userInfo); //persist the new user info
                    user = new TopcatUser(username, null, icatServer, userInfo, null);
                    user.setUserSurname(userSurname);
                    manager.persist(user);
                } else { // Its old user
                    userInfo = user.getTopcatUserId();
                }
            }

            //Check if the user is relogin again with same session id then replace this rather than create a new one
            if (session != null && session.getUserId().getServerId().getId() == icatServer.getId()) {
                if(icatSessionId==null) throw new AuthenticationException("ICAT returned a null");
                session.setIcatSessionId(icatSessionId);
                manager.merge(session);
            } else {
                TopcatUserSession userSession = new TopcatUserSession();
                userSession.setUserId(user);
                userSession.setIcatSessionId(icatSessionId);
                userSession.setTopcatSessionId(topcatSessionId);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, (int) hours);
                userSession.setExpiryDate(calendar.getTime());
                manager.persist(userSession);
            }
            user.setUserSurname(userSurname);
            manager.merge(user);
 
            manager.flush();
        } catch (MalformedURLException ex) {
            logger.warning("UserManager (login): " + ex.getMessage());
            throw new AuthenticationException("ICAT URL is not valid");
        } catch (javax.xml.ws.WebServiceException ex) {
            logger.warning("UserManager (login): " + ex.getMessage());
            throw new AuthenticationException("ICAT Server not available");
        }
    }

    /**
     * This method logout of all the ICAT servers that the user has logged in using
     * the topcat session id.
     * @param manager
     * @param topcatSessionId
     */
    public void logout(EntityManager manager, String topcatSessionId) {
        logger.finest("logout: topcat session id" + topcatSessionId);
        if (topcatSessionId == null) {
            return;
        }
        List<TopcatUserSession> sessionList = manager.createNamedQuery("TopcatUserSession.findByTopcatSessionId").setParameter("topcatSessionId", topcatSessionId).getResultList();
        if (sessionList == null) {
            return;
        }
        Iterator it = sessionList.iterator();
        while (it.hasNext()) {
            TopcatUserSession tus = (TopcatUserSession) it.next();
            manager.remove(tus);
        }
        manager.flush();
    }

    /**
     * This method removes the ICAT login session of user for a given server.
     * @param manager
     * @param topcatSessionId
     * @param serverName
     */
    public void logout(EntityManager manager, String topcatSessionId, String serverName) {
        logger.finest("logout: topcat session Id" + topcatSessionId + " serverName:" + serverName);
        if (topcatSessionId == null) {
            return;
        }
        List<TopcatUserSession> sessionList = manager.createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName").setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", serverName).getResultList();
        if (sessionList == null) {
            return;
        }
        Iterator it = sessionList.iterator();
        while (it.hasNext()) {
            TopcatUserSession tus = (TopcatUserSession) it.next();
            manager.remove(tus);
        }
        manager.flush();
    }

    /**
     * This method finds the user corresponding to the username used for loggging
     * to given ICAT Server.
     * @param manager
     * @param username ICAT Server username
     * @param serverName ICAT Server 
     * @return: User 
     */
    private TopcatUser findUserFromUsernameAndServer(EntityManager manager, String username, String serverName) {
        //Find the User with the user name and server id
        try {
            TopcatUser user = (TopcatUser) manager.createNamedQuery("TopcatUser.findByNameAndServerAndHomeNotAnonymous").setParameter("userName", username).setParameter("serverName", serverName).getSingleResult();
            return user;
        } catch (NoResultException ex) {
        }
        return null;
    }

    /**
     * This gets the user corresponding to username and server in the userInfo.
     * @param userInfo
     * @param username
     * @param serverName
     * @return
     */
    private TopcatUser findUserFromUsernameAndServer(TopcatUserInfo userInfo, String username, String serverName) {
        List<TopcatUser> userList = userInfo.getTopcatUserList();
        for (TopcatUser user : userList) {
            if (user.getServerId().getName().equals(serverName) && user.getName().equals(username)) {
                return user; //found the user
            }
        }
        return null; //no user found
    }

    /**
     * This method finds the ICAT server using the server name.
     * @param manager
     * @param serverName
     * @return
     */
    private TopcatIcatServer findTopcatIcatServerByName(EntityManager manager, String serverName) {
        //Find the ICAT Server from serverName
        TopcatIcatServer icatServer = (TopcatIcatServer) manager.createNamedQuery("TopcatIcatServer.findByName").setParameter("name", serverName).getSingleResult();
        if (icatServer == null) {
            return null; // TODO: Instead throw server not found exception
        }
        return icatServer;
    }

    /**
     * This method get all the valid user icat sessions for the given topcat session.
     * @param manager
     * @param topcatSessionId
     * @return
     */
    public static List<TopcatUserSession> getValidUserSessionByTopcatSession(EntityManager manager, String topcatSessionId) {
        //First find all the user sessions corresponding to the TopcatSessionId
        //Get all the Anonymous user sessions for the servers that user doesn't have logins
        //This is done in one query.
        List<TopcatUserSession> userSessionsWithValidAuth = (List<TopcatUserSession>) manager.createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndAnonymous").setParameter("topcatSessionId", topcatSessionId).getResultList();
        return userSessionsWithValidAuth;
    }

    /**
     * This method gets valid icat user session for the given server and topcat session.
     * @param manager
     * @param topcatSessionId
     * @param serverName
     * @return
     */
    public static TopcatUserSession getValidUserSessionByTopcatSessionAndServerName(EntityManager manager, String topcatSessionId, String serverName) {
        //Find the TopcatUserSession corresponding to topcatSessionId and serverName
        //Check whether the session has expired, if expired then get the Anonymous user session for that server
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager.createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName").setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", serverName).getSingleResult();
        } catch (NoResultException ex) {
        }
        if (userSession == null || userSession.getExpiryDate().before(new Date())) { // Invalid session
            try {
                userSession = (TopcatUserSession) manager.createNamedQuery("TopcatUserSession.findByAnonymousAndServerName").setParameter("serverName", serverName).getSingleResult();
            } catch (NoResultException ex) {
            }
        }
        return userSession;
    }

    /**
     * This does anonymouse login update for all the icat servers
     * @param manager
     */
    public void anonymousUserLogin(EntityManager manager) {
        //Get all the servers
        //for each server do anonymous login

        //TODO:If any of the servers fail then create a time to invoke anonymous login
        //after 5mins.
        try {
            List<TopcatIcatServer> servers = (List<TopcatIcatServer>) manager.createNamedQuery("TopcatIcatServer.findAll").getResultList();
            for (TopcatIcatServer server : servers) {
                try {
                    anonymousUserLoginToServer(manager, server.getName());
                } catch (AuthenticationException ex) {
                    Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
                    //TODO: create a timer to login to this server again.
                }
            }
        } catch (NoResultException ex) {
        }
    }

    /**
     * This method does a anonymous user login for a given server and update the
     * icat session information to be used in anonymous topcat logins.
     * @param manager
     * @param serverName
     * @throws AuthenticationException
     */
    public void anonymousUserLoginToServer(EntityManager manager, String serverName) throws AuthenticationException {
        //Get the anonymous User session for the server
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager.createNamedQuery("TopcatUserSession.findByAnonymousAndServerName").setParameter("serverName", serverName).getSingleResult();
        } catch (NoResultException ex) {
        }
        if (userSession != null) {
            TopcatIcatServer server = userSession.getUserId().getServerId();
            try {
                ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(), server.getVersion(), server.getServerUrl());
                service.logout(userSession.getIcatSessionId());
                String icatSessionId = service.loginLifetime(server.getDefaultUser(), server.getDefaultPassword(), 24);
                userSession.setIcatSessionId(icatSessionId);
                manager.persist(userSession);
            } catch (MalformedURLException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new AuthenticationException("ICAT Login to " + server.getName() + " Failed");
            }
        }
    }
}
