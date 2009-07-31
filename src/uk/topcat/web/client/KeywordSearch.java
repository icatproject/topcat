package uk.topcat.web.client;

import java.util.ArrayList;

import uk.topcat.web.client.model.TInvestigation;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author d.w.flannery@gmail.com (Damian Flannery)
 * 
 */
@RemoteServiceRelativePath("KeywordSearch")
public interface KeywordSearch extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static KeywordSearchAsync instance;
		public static KeywordSearchAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(KeywordSearch.class);
			}
			return instance;
		}
	}
	
	PagingLoadResult<TInvestigation> searchByKeyword(ArrayList<String> keywords, PagingLoadConfig config);
}
