package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;

import com.google.gwt.event.shared.EventHandler;

public interface LogoutEventHandler extends EventHandler {

    void logout(LogoutEvent event);

}
