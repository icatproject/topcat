package uk.topcat.web.client;

import java.util.List;
import uk.topcat.web.client.ui.results.PagingBeanModelGrid;
import com.google.gwt.event.dom.client.HasClickHandlers;

/**
 * @author d.w.flannery@gmail.com (Damian Flannery)
 * 
 */
public interface TopCatPresenter extends BasePresenter {
	
	interface View extends BaseView {

		void addMenu(BaseView view);

		void addContent(BaseView view);

		void removeContent();
		
		HasClickHandlers getSearchClickHandlers();
		
		List<String> getKeywordSearchTerms();
		
		PagingBeanModelGrid getPagingModelGrid();

	}

	View go();

}
