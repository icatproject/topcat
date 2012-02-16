package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;

import com.google.gwt.event.shared.EventHandler;

public interface LoginEventHandler extends EventHandler {

    void login(LoginEvent event);

}
