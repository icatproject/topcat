package org.icatproject.topcat.admin.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataServiceAsync {

	void login(String username, String password, AsyncCallback<String> callback);


}
