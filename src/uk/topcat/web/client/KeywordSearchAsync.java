package uk.topcat.web.client;

import java.util.ArrayList;

import uk.topcat.web.client.model.TInvestigation;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author d.w.flannery@gmail.com (Damian Flannery)
 * 
 */
public interface KeywordSearchAsync {
	void searchByKeyword(ArrayList<String> keywords, PagingLoadConfig config, AsyncCallback<PagingLoadResult<TInvestigation>> callback);
}
