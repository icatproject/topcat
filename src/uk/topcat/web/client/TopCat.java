package uk.topcat.web.client;

import uk.topcat.web.client.event.search.KeywordSearchEvent;
import uk.topcat.web.client.event.search.KeywordSearchHandler;
import uk.topcat.web.client.gin.AppGinjector;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author d.w.flannery@gmail.com (Damian Flannery)
 * 
 */
public class TopCat implements EntryPoint {

	public static final int PAGE_WIDTH = 850;
	public static final int VERTICAL_TAB_LABEL_MAX_WIDTH = 105;

	public void onModuleLoad() {

		final AppGinjector ginjector = GWT.create(AppGinjector.class);
		final TopCatPresenter topCatPresenter = ginjector.getTopCatPresenter();
		RootPanel.get().add(topCatPresenter.go().getWidget());
		logEvent(ginjector.getEventBus());
	}

	private void logEvent(final HandlerManager eventBus) {
		eventBus.addHandler(KeywordSearchEvent.getType(),
				new KeywordSearchHandler() {
					public void onKeywordSearch(KeywordSearchEvent event) {
						GWT.log(event.toDebugString(), null);
					}
				});
	}

}
