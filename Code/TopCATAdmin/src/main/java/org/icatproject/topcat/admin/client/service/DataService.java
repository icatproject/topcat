package org.icatproject.topcat.admin.client.service;

import java.util.List;

import org.icatproject.topcat.admin.shared.ServerException;
import org.icatproject.topcat.admin.shared.SessionException;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("DataService")
public interface DataService extends RemoteService {

	String login(String username, String password) throws SessionException,
			ServerException;

	List<TFacility> getAllFacilities();

	TFacility rowCall(Long id);

	String addIcatServer(TFacility facility) throws TopcatException;

	String updateIcatServer(TFacility facility) throws TopcatException;

	String removeIcatServer(Long id) throws TopcatException;

	String updateAuthDetails(TFacility facility, long authID)
			throws TopcatException;

	String ping(String url, String urlSelection) throws TopcatException;

}