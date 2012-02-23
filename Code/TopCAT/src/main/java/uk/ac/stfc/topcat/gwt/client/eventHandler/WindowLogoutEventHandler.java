package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.WindowLogoutEvent;

import com.google.gwt.event.shared.EventHandler;

public interface WindowLogoutEventHandler extends EventHandler {

    void logout(WindowLogoutEvent event);

}
