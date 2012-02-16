package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class LoginEvent extends GwtEvent<LoginEventHandler> {

    public static Type<LoginEventHandler> TYPE = new Type<LoginEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final LoginEventHandler handler) {
        return eventBus.addHandler(LoginEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final LoginEventHandler handler) {
        return eventBus.addHandlerToSource(LoginEvent.TYPE, source, handler);
    }

    private final String facilityName;

    public LoginEvent(final String facility) {
        this.facilityName = facility;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    @Override
    public Type<LoginEventHandler> getAssociatedType() {
        return LoginEvent.TYPE;
    }

    @Override
    protected void dispatch(final LoginEventHandler handler) {
        handler.login(this);
    }
}
