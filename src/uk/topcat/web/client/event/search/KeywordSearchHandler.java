package uk.topcat.web.client.event.search;

import uk.topcat.web.client.event.search.KeywordSearchEvent;
import com.google.gwt.event.shared.EventHandler;

public interface KeywordSearchHandler extends EventHandler {

	void onKeywordSearch(KeywordSearchEvent event);

}
