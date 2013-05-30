package org.icatproject.topcat.admin.server;

import java.util.List;

import javax.ejb.EJB;
import javax.servlet.UnavailableException;

import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.server.ejb.session.AdminEJB;
import org.icatproject.topcat.admin.shared.ServerException;
import org.icatproject.topcat.admin.shared.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements
		DataService {

	final static Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

	@EJB
	private AdminEJB adminEJB;

	@Override
	public void init() throws UnavailableException {
		try {
			// TODO - initialise any objects here
		} catch (Exception e) {
			logger.error("Fatal error " + e.getClass() + " reports "
					+ e.getMessage());
			throw new UnavailableException(e.getMessage());
		}
	}

	@Override
	public String login(String username, String password)
			throws SessionException, ServerException {
		logger.debug("In DataServiceImpl.login()");
		adminEJB.printTopcatIcatServerDetails();
		return "session-id-for-" + username;
	}

	@Override
	public List<TFacility> getAllFacilities() {
		return adminEJB.getAllFacilities();
	}

	@Override
	public String addIcatServer(TFacility facility) throws TopcatException {
		adminEJB.addIcatServer(facility);
		return "New row has been added ";
	}

	@Override
	public String updateIcatServer(TFacility facility) throws TopcatException {
		adminEJB.updateIcatServer(facility);
		return "Row has been Updated";
	}

	@Override
	public String removeIcatServer(Long id) throws TopcatException {
		adminEJB.removeIcatServer(id);
		return "The Row with the ID: " + id + " has been Removed";
	}

	@Override
	public TFacility rowCall(Long id) {
		return adminEJB.rowCall(id);
	}

	@Override
	public String updateAuthDetails(TFacility facility, long authID) {
		adminEJB.updateAuthDetails(facility, authID);
		return "Authentication Details have been updated";
	}

	@Override
	public String ping(String url, String urlSelection) {
		String status = urlSelection + " URL pinged ";
		try {
			Runtime r = Runtime.getRuntime();
			Process ping = r.exec("ping -c1 " + url);

			ping.waitFor();
			int val = ping.exitValue();

			if (val == 0)
				status += "successfully";

			else
				status += "unsuccessful";

		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;

	}
}
