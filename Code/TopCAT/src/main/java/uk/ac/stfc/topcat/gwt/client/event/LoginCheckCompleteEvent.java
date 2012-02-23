package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginCheckCompleteEventHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class LoginCheckCompleteEvent extends GwtEvent<LoginCheckCompleteEventHandler> {

    public static Type<LoginCheckCompleteEventHandler> TYPE = new Type<LoginCheckCompleteEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final LoginCheckCompleteEventHandler handler) {
        return eventBus.addHandler(LoginCheckCompleteEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final LoginCheckCompleteEventHandler handler) {
        return eventBus.addHandlerToSource(LoginCheckCompleteEvent.TYPE, source, handler);
    }

    private final String facilityName;

    public LoginCheckCompleteEvent(final String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    @Override
    public Type<LoginCheckCompleteEventHandler> getAssociatedType() {
        return LoginCheckCompleteEvent.TYPE;
    }

    @Override
    protected void dispatch(final LoginCheckCompleteEventHandler handler) {
        handler.update(this);
    }
}
