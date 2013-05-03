package org.icatproject.topcat.admin.client.service;

import java.util.List;

import org.icatproject.topcat.admin.shared.ServerException;
import org.icatproject.topcat.admin.shared.SessionException;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("DataService")
public interface DataService extends RemoteService {

	String login(String username, String password) throws SessionException,
			ServerException;

	List<TFacility> getAllFacilities();

	String addIcatServer(TFacility facility) throws TopcatException;

}