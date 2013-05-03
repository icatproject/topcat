package org.icatproject.topcat.admin.client.service;

import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataServiceAsync {

	void login(String username, String password, AsyncCallback<String> callback);

	void getAllFacilities(AsyncCallback<List<TFacility>> callback);

	/**
	 * Add an entry for the given facility.
	 * 
	 * @param facility
	 *            a <code>TFacility</code> object containing details about an ICAT server
	 * @param callback
	 */
	void addIcatServer(TFacility facility, AsyncCallback<String> callback);

}
