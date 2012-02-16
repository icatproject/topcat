package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class LogoutEvent extends GwtEvent<LogoutEventHandler> {

    public static Type<LogoutEventHandler> TYPE = new Type<LogoutEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final LogoutEventHandler handler) {
        return eventBus.addHandler(LogoutEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final LogoutEventHandler handler) {
        return eventBus.addHandlerToSource(LogoutEvent.TYPE, source, handler);
    }

    private final String facilityName;

    public LogoutEvent(final String facility) {
        this.facilityName = facility;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    @Override
    public Type<LogoutEventHandler> getAssociatedType() {
        return LogoutEvent.TYPE;
    }

    @Override
    protected void dispatch(final LogoutEventHandler handler) {
        handler.logout(this);
    }
}
