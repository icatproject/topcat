package uk.topcat.web.client.event.search;

import com.google.gwt.event.shared.GwtEvent;

public class KeywordSearchEvent extends GwtEvent<KeywordSearchHandler> {

	private static Type<KeywordSearchHandler> TYPE;

	public static Type<KeywordSearchHandler> getType() {
		return TYPE != null ? TYPE
				: (TYPE = new Type<KeywordSearchHandler>());
	}
	
	@Override
	protected void dispatch(KeywordSearchHandler handler) {
		handler.onKeywordSearch(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<KeywordSearchHandler> getAssociatedType() {
		return getType();
	}

}
