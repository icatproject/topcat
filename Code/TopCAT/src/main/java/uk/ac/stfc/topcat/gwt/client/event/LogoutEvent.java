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
    private final boolean statusCheck;

    /**
     * @param facilityName
     *            the name of the facility
     */
    public LogoutEvent(final String facilityName) {
        this.facilityName = facilityName;
        this.statusCheck = false;
    }

    /**
     * @param facilityName
     *            the name of the facility
     * @param statusCheck
     *            true if we are just doing a check on status
     */
    public LogoutEvent(final String facilityName, boolean statusCheck) {
        this.facilityName = facilityName;
        this.statusCheck = statusCheck;
    }

    /**
     * @return the name of the facility
     */
    public String getFacilityName() {
        return this.facilityName;
    }

    /**
     * @return true if we are just doing a check on status
     */
    public boolean isStatusCheck() {
        return statusCheck;
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
