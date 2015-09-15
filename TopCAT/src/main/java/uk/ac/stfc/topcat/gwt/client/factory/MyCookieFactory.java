package uk.ac.stfc.topcat.gwt.client.factory;

import uk.ac.stfc.topcat.gwt.client.model.TopcatCookie;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;


public interface MyCookieFactory extends AutoBeanFactory{
    AutoBean<TopcatCookie> topcatCookie();
}
