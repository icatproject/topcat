package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginInfoPanelUpdateEventHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class LoginInfoPanelUpdateEvent extends GwtEvent<LoginInfoPanelUpdateEventHandler> {

    public static Type<LoginInfoPanelUpdateEventHandler> TYPE = new Type<LoginInfoPanelUpdateEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final LoginInfoPanelUpdateEventHandler handler) {
        return eventBus.addHandler(LoginInfoPanelUpdateEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final LoginInfoPanelUpdateEventHandler handler) {
        return eventBus.addHandlerToSource(LoginInfoPanelUpdateEvent.TYPE, source, handler);
    }

    private final String facilityName;

    public LoginInfoPanelUpdateEvent(final String facility) {
        this.facilityName = facility;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    @Override
    public Type<LoginInfoPanelUpdateEventHandler> getAssociatedType() {
        return LoginInfoPanelUpdateEvent.TYPE;
    }

    @Override
    protected void dispatch(final LoginInfoPanelUpdateEventHandler handler) {
        handler.update(this);
    }
}
