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
    private final boolean statusCheck;

    /**
     * @param facilityName
     *            the name of the facility
     */
    public LoginEvent(final String facilityName) {
        this.facilityName = facilityName;
        this.statusCheck = false;
    }

    /**
     * @param facilityName
     *            the name of the facility
     * @param statusCheck
     *            true if we are just doing a check on status
     */
    public LoginEvent(final String facilityName, boolean statusCheck) {
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
    public Type<LoginEventHandler> getAssociatedType() {
        return LoginEvent.TYPE;
    }

    @Override
    protected void dispatch(final LoginEventHandler handler) {
        handler.login(this);
    }
}
