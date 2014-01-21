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
package uk.ac.stfc.topcat.ejb.manager;

import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.ejb.entity.TopcatIcatServer;
import uk.ac.stfc.topcat.ejb.entity.TopcatUser;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserInfo;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserSession;

/**
 * This class manages the user login and logouts etc. for TopCAT
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class UserManager {

    private final static Logger logger = Logger.getLogger(UserManager.class.getName());

    public UserManager() {
    }

    /**
     * This method returns the default user / Anonymous user. this will be used
     * for login to resources where user doen't have a login id.
     * 
     * @param manager
     * @return Anonymous user
     */
    public TopcatUserInfo getDefaultUser(EntityManager manager) {
        logger.info("getDefaultUser");
        TopcatUserInfo defaultUser = (TopcatUserInfo) manager.createNamedQuery("TopcatUserInfo.findAnonymousUser")
                .getSingleResult();
        return defaultUser;
    }

    /**
     * This method returns a Session ID, the Session ID is a random unique
     * string generated.
     * 
     * @param manager
     * @return a Unique string.
     */
    public String login(EntityManager manager) {
        logger.info("login");
        // create UUID for session
        String sid = UUID.randomUUID().toString();
        return sid;
    }

    /**
     * This method login to a requested ICAT Server.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @param authenticationType
     * @throws AuthenticationException
     */
    public void login(EntityManager manager, String topcatSessionId, String facilityName, String authenticationType,
            Map<String, String> parameters) throws AuthenticationException {
        logger.info("login: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName
                + "), authenticationType (" + authenticationType + "), number of parameters " + parameters.size());
        long hours = 2;
        // Process
        // 1) Get the icat server
        // 2) Login to the ICAT Server using user name and password
        // 3) if the server login fails then throw exception and return.

        // Get the icat server
        TopcatIcatServer icatServer = findTopcatIcatServerByName(manager, facilityName);
        if (logger.isTraceEnabled()) {
            logger.trace("login: " + icatServer.getServerUrl());
        }
        // Login to the icat server
        try {
            ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(icatServer.getName(),
                    icatServer.getVersion(), icatServer.getServerUrl());
            String icatSessionId = service.loginLifetime(authenticationType, parameters, (int) hours);
            String username = parameters.get("username");
            if (username == null) {
                // currently only implemented in 3.4.1 and 4.2
                username = service.getUserName(icatSessionId);
            }

            loginUpdate(manager, service, topcatSessionId, icatServer, username, icatSessionId, hours);

        } catch (MalformedURLException ex) {
            logger.error("UserManager (login): " + ex.getMessage());
            throw new AuthenticationException("ICAT URL is not valid");
        } catch (javax.xml.ws.WebServiceException ex) {
            logger.warn("UserManager (login): " + ex.getMessage());
            throw new AuthenticationException("ICAT Server not available");
        } catch (TopcatException e) {
            throw new AuthenticationException("Unable to get surname");
        }
    }

    /**
     * This method logout of all the ICAT servers that the user has logged in
     * using the topcat session id.
     * 
     * @param manager
     * @param topcatSessionId
     */
    public void logout(EntityManager manager, String topcatSessionId) {
        logger.info("logout: topcatSessionId (" + topcatSessionId + ")");
        if (topcatSessionId == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<TopcatUserSession> sessionList = manager.createNamedQuery("TopcatUserSession.findByTopcatSessionId")
                .setParameter("topcatSessionId", topcatSessionId).getResultList();
        if (sessionList == null) {
            return;
        }
        Iterator<TopcatUserSession> it = sessionList.iterator();
        while (it.hasNext()) {
            TopcatUserSession tus = (TopcatUserSession) it.next();
            manager.remove(tus);
        }
        manager.flush();
    }

    /**
     * This method removes the ICAT login session of user for a given server.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     */
    public void logout(EntityManager manager, String topcatSessionId, String facilityName) {
        logger.info("logout: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName + ")");
        if (topcatSessionId == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<TopcatUserSession> sessionList = manager
                .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                .getResultList();
        if (sessionList == null) {
            return;
        }
        Iterator<TopcatUserSession> it = sessionList.iterator();
        while (it.hasNext()) {
            TopcatUserSession tus = (TopcatUserSession) it.next();
            manager.remove(tus);
        }
        manager.flush();
    }

    /**
     * This method get the icat session corresponding the topcat session id and
     * icat server and queries the icat server to check whether the session is
     * valid.
     * 
     * @param manager
     * @param sessionId
     * @param facilityName
     * @return
     */
    public Boolean isSessionValid(EntityManager manager, String topcatSessionId, String facilityName) {
        logger.info("isSessionValid: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName + ")");
        if (topcatSessionId == null) {
            return Boolean.FALSE;
        }
        @SuppressWarnings("unchecked")
        List<TopcatUserSession> sessionList = manager
                .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                .getResultList();
        if (sessionList == null) {
            return Boolean.FALSE;
        }
        Iterator<TopcatUserSession> it = sessionList.iterator();
        while (it.hasNext()) {
            TopcatUserSession tus = (TopcatUserSession) it.next();
            TopcatIcatServer icatServer = tus.getUserId().getServerId();
            try {
                ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(
                        icatServer.getName(), icatServer.getVersion(), icatServer.getServerUrl());
                return service.isSessionValid(tus.getIcatSessionId());
            } catch (MalformedURLException ex) {
                logger.error("isSessionValid:" + ex.getMessage());
            }
        }
        return Boolean.FALSE;
    }

    private void loginUpdate(EntityManager manager, ICATWebInterfaceBase service, String topcatSessionId,
            TopcatIcatServer icatServer, String username, String icatSessionId, long hours)
            throws AuthenticationException {
        // Process

        // 4) if the server login is success then
        // 4a) get the user surname from the server
        // 5) find the user by searching the user session table using
        // topcatSessionId
        // 5a) if found then the user is old one else
        // 5b) find the user using the user name and server id in TopcatUser
        // table.
        // 5c) If not found in TopcatUser table then create a new user.
        // 6) if a session doesn't exist then create a usersession entry for the
        // user and topcatSessionId otherwise update the existing session.

        // TODO: don't allow default user to login
        String userSurname = service.getUserSurname(icatSessionId, username);
        // Get all the topcat user with session id
        TopcatUser user = null;
        TopcatUserSession session = null;
        TopcatUserInfo userInfo = null;

        @SuppressWarnings("unchecked")
        List<TopcatUserSession> resultSessions = manager.createNamedQuery("TopcatUserSession.findByTopcatSessionId")
                .setParameter("topcatSessionId", topcatSessionId).getResultList();
        if (resultSessions != null && resultSessions.size() != 0) {
            userInfo = resultSessions.get(0).getUserId().getTopcatUserId();
            // search through the results to get the relevant session
            for (TopcatUserSession us : resultSessions) {
                if (us.getUserId().getServerId().getId() == icatServer.getId()
                        && us.getUserId().getName().equals(username)) {
                    // found the relevant session
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
        } else { // This is a Anonymous user session trying to login to first
                 // server with session id
            // Check if the user is already a logged in user.
            user = findUserFromUsernameAndServer(manager, username, icatServer.getName());
            if (user == null) {// Its a new user
                // Create a new user
                userInfo = new TopcatUserInfo();
                userInfo.setDisplayName(username);
                userInfo.setHomeServer(icatServer);
                manager.persist(userInfo); // persist the new user info
                user = new TopcatUser(username, null, icatServer, userInfo, null);
                user.setUserSurname(userSurname);
                manager.persist(user);
            } else { // Its old user
                userInfo = user.getTopcatUserId();
            }
        }

        // Check if the user is relogin again with same session id then replace
        // this rather than create a new one
        if (session != null && session.getUserId().getServerId().getId() == icatServer.getId()) {
            if (icatSessionId == null) {
                throw new AuthenticationException("ICAT returned a null");
            }
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
    }

    /**
     * This method finds the user corresponding to the username used for
     * loggging to given ICAT Server.
     * 
     * @param manager
     * @param username
     *            ICAT Server username
     * @param facilityName
     *            ICAT Server
     * @return: User
     */
    private TopcatUser findUserFromUsernameAndServer(EntityManager manager, String username, String facilityName) {
        // Find the User with the user name and server id
        try {
            TopcatUser user = (TopcatUser) manager
                    .createNamedQuery("TopcatUser.findByNameAndServerAndHomeNotAnonymous")
                    .setParameter("userName", username).setParameter("serverName", facilityName).getSingleResult();
            return user;
        } catch (NoResultException ex) {
        }
        return null;
    }

    /**
     * This gets the user corresponding to username and facility in the
     * userInfo.
     * 
     * @param userInfo
     * @param username
     * @param facilityName
     * @return
     */
    private TopcatUser findUserFromUsernameAndServer(TopcatUserInfo userInfo, String username, String facilityName) {
        List<TopcatUser> userList = userInfo.getTopcatUserList();
        for (TopcatUser user : userList) {
            if (user.getServerId().getName().equals(facilityName) && user.getName().equals(username)) {
                return user; // found the user
            }
        }
        return null; // no user found
    }

    /**
     * This method finds the ICAT server using the facility name.
     * 
     * @param manager
     * @param facilityName
     * @return
     */
    private TopcatIcatServer findTopcatIcatServerByName(EntityManager manager, String facilityName) {
        // Find the ICAT Server from facilityName
        TopcatIcatServer icatServer = (TopcatIcatServer) manager.createNamedQuery("TopcatIcatServer.findByName")
                .setParameter("name", facilityName).getSingleResult();
        if (icatServer == null) {
            return null; // TODO: Instead throw server not found exception
        }
        return icatServer;
    }

    /**
     * This method get all the valid user icat sessions for the given topcat
     * session.
     * 
     * @param manager
     * @param topcatSessionId
     * @return
     */
    public static List<TopcatUserSession> getValidUserSessionByTopcatSession(EntityManager manager,
            String topcatSessionId) {
        logger.info("getValidUserSessionByTopcatSession: topcatSessionId (" + topcatSessionId + ")");
        // First find all the user sessions corresponding to the TopcatSessionId
        // Get all the Anonymous user sessions for the servers that user doesn't
        // have logins
        // This is done in one query.
        @SuppressWarnings("unchecked")
        List<TopcatUserSession> userSessionsWithValidAuth = manager
                .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndAnonymous")
                .setParameter("topcatSessionId", topcatSessionId).getResultList();
        return userSessionsWithValidAuth;
    }

    /**
     * This method gets valid icat user session for the given facility and
     * topcat session.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return
     * @throws SessionException
     */
    public static TopcatUserSession getValidUserSessionByTopcatSessionAndServerName(EntityManager manager,
            String topcatSessionId, String facilityName) throws SessionException {
        logger.info("getValidUserSessionByTopcatSessionAndServerName: topcatSessionId (" + topcatSessionId
                + "), facilityName (" + facilityName + ")");
        // Find the TopcatUserSession corresponding to topcatSessionId and
        // facilityName
        // Check whether the session has expired, if expired then get the
        // Anonymous user session for that server
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
        } catch (NoResultException ex) {
        }
        if (userSession == null || userSession.getExpiryDate().before(new Date())) { // Invalid
                                                                                     // session
            try {
                userSession = (TopcatUserSession) manager
                        .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                        .setParameter("serverName", facilityName).getSingleResult();
            } catch (NoResultException ex) {
                throw new SessionException("Invalid topcat session id");
            }
        }
        return userSession;
    }

    /**
     * This does anonymous login update for all the icat servers
     * 
     * @param manager
     */
    public void anonymousUserLogin(EntityManager manager) {
        logger.info("anonymousUserLogin");
        // Get all the servers
        // for each server do anonymous login

        // TODO:If any of the servers fail then create a time to invoke
        // anonymous login
        // after 5mins.
        try {
            @SuppressWarnings("unchecked")
            List<TopcatIcatServer> servers = manager.createNamedQuery("TopcatIcatServer.findAll").getResultList();
            for (TopcatIcatServer server : servers) {
                try {
                    anonymousUserLoginToServer(manager, server.getName());
                } catch (AuthenticationException ex) {
                    logger.error("anonymousUserLogin:" + ex.getMessage());
                    // TODO: create a timer to login to this server again.
                }
            }
        } catch (NoResultException ex) {
        }
    }

    /**
     * This method does a anonymous user login for a given facility and update
     * the icat session information to be used in anonymous topcat logins.
     * 
     * @param manager
     * @param facilityName
     * @throws AuthenticationException
     */
    private void anonymousUserLoginToServer(EntityManager manager, String facilityName) throws AuthenticationException {
        // Get the anonymous User session for the server
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByAnonymousAndServerName")
                    .setParameter("serverName", facilityName).getSingleResult();
        } catch (NoResultException ex) {
        }
        if (userSession != null) {
            TopcatIcatServer server = userSession.getUserId().getServerId();
            try {
                ICATWebInterfaceBase service = ICATInterfaceFactory.getInstance().createICATInterface(server.getName(),
                        server.getVersion(), server.getServerUrl());
                try {
                    service.logout(userSession.getIcatSessionId());
                } catch (AuthenticationException ex) {
                }
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("username", server.getDefaultUser());
                parameters.put("password", server.getDefaultPassword());
                String icatSessionId = service.loginLifetime(null, parameters, 24);
                userSession.setIcatSessionId(icatSessionId);
                manager.persist(userSession);
            } catch (MalformedURLException ex) {
                logger.error("anonymousUserLoginToServer:" + ex.getMessage());
                throw new AuthenticationException("ICAT Login to " + server.getName() + " Failed");
            }
        }
    }

    /**
     * Get the current ICAT session id for the given TopCAT session and
     * facility.
     * 
     * @param manager
     * @param topcatSessionId
     * @param facilityName
     * @return ICAT session id
     * @throws TopcatException
     */
    public String getIcatSessionId(EntityManager manager, String topcatSessionId, String facilityName)
            throws TopcatException {
        logger.info("getIcatSessionId: topcatSessionId (" + topcatSessionId + "), facilityName (" + facilityName + ")");
        TopcatUserSession userSession = null;
        try {
            userSession = (TopcatUserSession) manager
                    .createNamedQuery("TopcatUserSession.findByTopcatSessionIdAndServerName")
                    .setParameter("topcatSessionId", topcatSessionId).setParameter("serverName", facilityName)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new SessionException("Invalid topcat session id");
        }
        return userSession.getIcatSessionId();
    }

}
