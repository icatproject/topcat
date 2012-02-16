package uk.ac.stfc.topcat.gwt.client.event;

import java.util.ArrayList;

import uk.ac.stfc.topcat.gwt.client.eventHandler.AddMyInvestigationEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddMyInvestigationEvent extends GwtEvent<AddMyInvestigationEventHandler> {

    public static Type<AddMyInvestigationEventHandler> TYPE = new Type<AddMyInvestigationEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddMyInvestigationEventHandler handler) {
        return eventBus.addHandler(AddMyInvestigationEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddMyInvestigationEventHandler handler) {
        return eventBus.addHandlerToSource(AddMyInvestigationEvent.TYPE, source, handler);
    }

    private final String facilityName;
    private final ArrayList<TopcatInvestigation> myInvestigations;

    public AddMyInvestigationEvent(final String facility, final ArrayList<TopcatInvestigation> myInvestigations) {
        this.facilityName = facility;
        this.myInvestigations = myInvestigations;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    public ArrayList<TopcatInvestigation> getMyInvestigations() {
        return this.myInvestigations;
    }

    @Override
    public Type<AddMyInvestigationEventHandler> getAssociatedType() {
        return AddMyInvestigationEvent.TYPE;
    }

    @Override
    protected void dispatch(final AddMyInvestigationEventHandler handler) {
        handler.addMyInvestigations(this);
    }
}
