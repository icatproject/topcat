package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.LoginCheckCompleteEvent;

import com.google.gwt.event.shared.EventHandler;

public interface LoginCheckCompleteEventHandler extends EventHandler {

    void update(LoginCheckCompleteEvent event);

}
