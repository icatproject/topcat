package org.icatproject.topcat.admin.server;

import javax.ejb.EJB;
import javax.servlet.UnavailableException;

import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.server.ejb.session.AdminEJB;
import org.icatproject.topcat.admin.shared.ServerException;
import org.icatproject.topcat.admin.shared.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements DataService {

	final static Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

	@EJB
	private AdminEJB adminEJB;

	@Override
	public void init() throws UnavailableException {
		try {
			// TODO - initialise any objects here
		} catch (Exception e) {
			logger.error("Fatal error " + e.getClass() + " reports " + e.getMessage());
			throw new UnavailableException(e.getMessage());
		}
	}

	public String login(String username, String password) throws SessionException, ServerException {
		logger.debug("In DataServiceImpl.login()");
		return "session-id-for-" + username;
	}


}
