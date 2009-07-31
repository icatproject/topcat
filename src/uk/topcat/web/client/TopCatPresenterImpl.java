package uk.topcat.web.client;

import java.util.ArrayList;

import uk.topcat.web.client.event.search.KeywordSearchEvent;
import uk.topcat.web.client.event.search.KeywordSearchHandler;

import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.inject.Inject;

/**
 * @author d.w.flannery@gmail.com (Damian Flannery)
 * 
 */
public class TopCatPresenterImpl implements TopCatPresenter {

	private final View view;

	@Inject
	public TopCatPresenterImpl(final HandlerManager eventBus, View view) {

		this.view = view;

		view.getSearchClickHandlers().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				eventBus.fireEvent(new KeywordSearchEvent());
			}
		});

		bindEvents(eventBus);
	}

	private void bindEvents(HandlerManager eventBus) {

		eventBus.addHandler(KeywordSearchEvent.getType(),
				new KeywordSearchHandler() {
					public void onKeywordSearch(KeywordSearchEvent event) {
						doKeywordSearch();
					}
				});
	}

	public void doKeywordSearch() {

		ArrayList<String> list = (ArrayList<String>) view
				.getKeywordSearchTerms();
		String terms = "";
		for (String s : list) {
			terms += s + ", ";
		}

		Info.display("Search Terms", terms);
		view.getPagingModelGrid().getLoader().load();
	}

	public View go() {
		return view;
	}

	public BaseView getView() {
		return view;
	}

}
