package uk.topcat.web.client.gin;

import uk.topcat.web.client.TopCatPresenter;
import uk.topcat.web.client.TopCatPresenterImpl;
import uk.topcat.web.client.TopCatWidget;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * @author esnunes@gmail.com (Eduardo S. Nunes)
 * 
 */
public class AppModule extends AbstractGinModule {

	@Override
	protected void configure() {

		bind(HandlerManager.class).toProvider(HandlerManagerProvider.class).in(
				Singleton.class);
 
		bind(TopCatPresenter.class).to(TopCatPresenterImpl.class);
		bind(TopCatPresenter.View.class).to(TopCatWidget.class);

	}

}
