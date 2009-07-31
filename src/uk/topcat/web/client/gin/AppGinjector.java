/**
 * 
 */
package uk.topcat.web.client.gin;

import uk.topcat.web.client.TopCatPresenter;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

/**
 * @author d.w.flannery@gmail.com (Damian Flannery)
 * 
 */
@GinModules(AppModule.class)
public interface AppGinjector extends Ginjector {

	
	TopCatPresenter getTopCatPresenter();
	
	HandlerManager getEventBus();

}
