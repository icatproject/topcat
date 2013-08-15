package org.icatproject.topcat.admin.client.service;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TMessages;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataServiceAsync {

	void login(String username, String password, AsyncCallback<String> callback);

	void getAllFacilities(AsyncCallback<List<TFacility>> callback);

	/**
	 * Add an entry for the given facility.
	 * 
	 * @param facility
	 *            a <code>TFacility</code> object containing details about an
	 *            ICAT server
	 * @param callback
	 */
	void addIcatServer(TFacility facility, AsyncCallback<String> callback);

	void updateIcatServer(TFacility facility, AsyncCallback<String> callback);

	void removeIcatServer(Long id, String facilityName, AsyncCallback<String> callback);

	void authDetailsCall(String serverName, AsyncCallback<List<TAuthentication>> callback);

	void updateAuthDetails(TAuthentication authentication, long authID,
			AsyncCallback<String> callback);

	void ping(String url, String urlSelection, AsyncCallback<String> callback);

	void removeAuthenticationDetails(Long id, AsyncCallback<String> callback);

	void addAuthDetails(TAuthentication authentication,
			AsyncCallback<String> callback);

	void authCount(AsyncCallback<ArrayList<Integer>> callback);

	void getAllMessages(AsyncCallback<List<TMessages>> callback);
}
