package org.icatproject.topcat.admin.client.service;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.topcat.admin.shared.ServerException;
import org.icatproject.topcat.admin.shared.SessionException;

import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TMessages;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("DataService")
public interface DataService extends RemoteService {

	String login(String username, String password) throws SessionException,
			ServerException;

	List<TFacility> getAllFacilities();

	List<TAuthentication> authDetailsCall(String serverName);

	String addIcatServer(TFacility facility) throws TopcatException;

	String updateIcatServer(TFacility facility) throws TopcatException;

	String removeIcatServer(Long id, String facilityName) throws TopcatException;

	String updateAuthDetails(TAuthentication authentication, long authID)
			throws TopcatException;

	String ping(String url, String urlSelection) throws  Exception;

	String removeAuthenticationDetails(Long id) throws TopcatException;

	String addAuthDetails(TAuthentication authentication) throws TopcatException;

	ArrayList<Integer> authCount() throws TopcatException;

	List<TMessages> getAllMessages() throws TopcatException;

}