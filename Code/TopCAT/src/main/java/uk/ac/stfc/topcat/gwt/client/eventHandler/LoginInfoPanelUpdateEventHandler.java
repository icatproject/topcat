package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.LoginInfoPanelUpdateEvent;

import com.google.gwt.event.shared.EventHandler;

public interface LoginInfoPanelUpdateEventHandler extends EventHandler {

    void update(LoginInfoPanelUpdateEvent event);

}
