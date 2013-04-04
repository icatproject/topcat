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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.session.UploadManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;
import uk.ac.stfc.topcat.gwt.client.UploadService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This is servlet implementation of Upload methods.
 * 
 */
@SuppressWarnings("serial")
public class UploadServiceImpl extends RemoteServiceServlet implements UploadService {
    private UploadManagementBeanLocal uploadManager = null;
    private UserManagementBeanLocal userManager = null;

    /**
     * Servlet Init method.
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        try {
            // create initial context
            Context ctx = new InitialContext();
            uploadManager = (UploadManagementBeanLocal) ctx
                    .lookup("java:global/TopCAT/UploadManagementBean!uk.ac.stfc.topcat.ejb.session.UploadManagementBeanLocal");
            userManager = (UserManagementBeanLocal) ctx
                    .lookup("java:global/TopCAT/UserManagementBean!uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal");
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Long createDataSet(TDataset dataset) throws TopcatException {
        return uploadManager.createDataSet(getSessionId(), dataset);
    }

    /**
     * This method returns session id from the session information.
     * 
     * @return user session id
     * @throws SessionException
     */
    private String getSessionId() throws SessionException {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String sessionId = null;
        if (session.getAttribute("SESSION_ID") == null) { // First time login
            try {
                sessionId = userManager.login();
                session.setAttribute("SESSION_ID", sessionId);
            } catch (AuthenticationException e) {
                throw new SessionException("Invalid topcat session id");
            }
        } else {
            sessionId = (String) session.getAttribute("SESSION_ID");
        }
        return sessionId;
    }
}
