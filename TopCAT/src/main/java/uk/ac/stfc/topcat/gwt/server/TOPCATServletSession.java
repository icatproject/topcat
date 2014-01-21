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
import javax.ejb.EJB;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;

/**
 * This Class is used to manage sessions for TOPCAT servlets
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class TOPCATServletSession implements HttpSessionListener {
    // Session bean for managing user session
    @EJB
    private UserManagementBeanLocal userManager;
    private final static Logger logger = Logger.getLogger(TOPCATServletSession.class.getName());

    /**
     * Constructor for initialising user sessions.
     */
    public TOPCATServletSession() {

    }

    /**
     * This method is override method called when a new session need to be
     * created.
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("sessionCreated: id (" + event.getSession().getId() + ")");
        }
    }

    /**
     * This method is override method called when a session expires and needs
     * destroying
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("sessionDestroyed: id (" + event.getSession().getId() + ")");
        }
        // logout of TOPCAT
        if (event.getSession().getAttribute("SESSION_ID") != null) {
            try {
                userManager.logout((String) event.getSession().getAttribute("SESSION_ID"));
            } catch (AuthenticationException e) {
            }
        }
    }

}
