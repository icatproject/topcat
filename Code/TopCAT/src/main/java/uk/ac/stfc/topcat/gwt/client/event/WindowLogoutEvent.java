package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.WindowLogoutEventHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class WindowLogoutEvent extends GwtEvent<WindowLogoutEventHandler> {

    public static Type<WindowLogoutEventHandler> TYPE = new Type<WindowLogoutEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final WindowLogoutEventHandler handler) {
        return eventBus.addHandler(WindowLogoutEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final WindowLogoutEventHandler handler) {
        return eventBus.addHandlerToSource(WindowLogoutEvent.TYPE, source, handler);
    }

    private final String facilityName;

    public WindowLogoutEvent(final String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    @Override
    public Type<WindowLogoutEventHandler> getAssociatedType() {
        return WindowLogoutEvent.TYPE;
    }

    @Override
    protected void dispatch(final WindowLogoutEventHandler handler) {
        handler.logout(this);
    }
}
