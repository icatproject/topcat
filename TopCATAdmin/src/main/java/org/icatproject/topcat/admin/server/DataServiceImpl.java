package org.icatproject.topcat.admin.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.UnavailableException;

import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.server.ejb.session.AdminEJB;
import org.icatproject.topcat.admin.shared.ServerException;
import org.icatproject.topcat.admin.shared.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TMessages;
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
		return "New row has been added to TopcatIcatServer Table";
	}

	@Override
	public String updateIcatServer(TFacility facility) throws TopcatException {
		adminEJB.updateIcatServer(facility);
		return "Row has been updated to TopcatIcatServer Table";
	}

	@Override
	public String removeIcatServer(Long id, String facilityName)
			throws TopcatException {
		if (!adminEJB.authCall(facilityName).isEmpty()) {
		}
		adminEJB.removeIcatServer(id, facilityName);
		return "The Row with the ID: " + id
				+ " has been Removed from the to TopcatIcatServer Table";
	}

	@Override
	public List<TAuthentication> authDetailsCall(String serverName) {
		return adminEJB.authCall(serverName);
	}

	@Override
	public String updateAuthDetails(TAuthentication authentication, long authID) {
		logger.debug("Update");
		adminEJB.updateAuthDetails(authentication, authID);
		return "Authentication Details have been updated";
	}

	@Override
	public String ping(String url, String urlSelection) {
	    
	    if (url == null || url.trim().isEmpty()) {
	        return "No url set";
	    }

		int code = 0;
		HttpURLConnection connection = null;
		URL address = null;
		logger.debug("Selection = " +urlSelection);
		logger.debug("URL: " + url);
		
		if(urlSelection.equals("Download Service")){
            if (url.matches(".*/$")) {
                url = url + "ids/ping";
            } else {
                url = url + "/";
            }
		}
		else if (urlSelection.equals("ICAT")){
			if (!url.matches(".*/ICATService/ICAT\\?wsdl$")) {
				if (url.matches(".*/$")) {
					url = url + "ICATService/ICAT?wsdl";
				} else {
		           	url = url + "/ICATService/ICAT?wsdl";
				}
			}
		}
		
		try {
			address = new URL(url);
		        logger.debug("Connecting to URL: " + address);
			if (address.getProtocol().equalsIgnoreCase("http")) {
				connection = (HttpURLConnection) address.openConnection();
			}else if (address.getProtocol().equalsIgnoreCase("https")) {
				connection = (HttpsURLConnection) address.openConnection();
			}
			code = connection.getResponseCode();
                        logger.debug("ResponseCode: " + code);

		}catch (MalformedURLException e) {
			String msg = "The URL '" + address.toString() + "' is invalid";
			e.printStackTrace();
			return "unsuccessfully: " + msg;

		}catch (UnknownHostException e) {
			String msg = "Server '"+ address.getHost() + "' does not exist";
			logger.debug(msg);
			return "unsuccessfully: " + msg;
			
		}
		catch (IOException e) {
			return e.getMessage();
		}		
		switch (code) {			case 401:
			return "successfully, but URL requires login";
		case 404:
			return "unsuccessfully. The requested resource does not exist"; 
		case 200:
			return "successfully";
                case 501:
                case 400:	
                    return "successfully";
		default:
			return "unsuccessful";
		}
	}
	

	public String removeAuthenticationDetails(Long id) {
		adminEJB.removeRowFromAuthTable(id);
		return "The Row with the ID: " + id
				+ "  has been Removed from the IcatAuthetication Table";
	}

	public String addAuthDetails(TAuthentication authentication) {
		adminEJB.addRowToAuthTable(authentication);
		return "New row has been added to IcatAuthetication Table";

	}

	public ArrayList<Integer> authCount() {
		List<TFacility> result = adminEJB.getAllFacilities();
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(null);

		for (TFacility facility : result) {
			facility.getName();
			list.add(adminEJB.icatAssociatedAuthCount(facility.getName()));
		}

		return list;
	}
	
	public List<TMessages> getAllMessages() {
		return adminEJB.messageCall();
	}

	@Override
	public String addMessages(TMessages message) throws TopcatException {
		adminEJB.addMessage(message);
		return "Successfull";
	}

	@Override
	public String deleteMessage(TMessages message) throws TopcatException {
		adminEJB.deleteMessage(message);
		return "Successful";
		
	}

    @Override
    public List<TMessages> getMessageByDateRange(Date fromDateTime,
            Date toDateTime) {
        return adminEJB.getMessagesByDateRange(fromDateTime, toDateTime);        
    }
}
