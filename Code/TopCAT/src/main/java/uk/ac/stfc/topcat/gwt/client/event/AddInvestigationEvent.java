package uk.ac.stfc.topcat.gwt.client.event;

import java.util.ArrayList;

import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInvestigationEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.InvestigationType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddInvestigationEvent extends GwtEvent<AddInvestigationEventHandler> {

    public static Type<AddInvestigationEventHandler> TYPE = new Type<AddInvestigationEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddInvestigationEventHandler handler) {
        return eventBus.addHandler(AddInvestigationEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddInvestigationEventHandler handler) {
        return eventBus.addHandlerToSource(AddInvestigationEvent.TYPE, source, handler);
    }

    private final String facilityName;
    private final ArrayList<InvestigationType> investigations;

    public AddInvestigationEvent(final String facilityName, final ArrayList<InvestigationType> investigations) {
        this.facilityName = facilityName;
        this.investigations = investigations;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    public ArrayList<InvestigationType> getInvestigations() {
        return this.investigations;
    }

    @Override
    public Type<AddInvestigationEventHandler> getAssociatedType() {
        return AddInvestigationEvent.TYPE;
    }

    @Override
    protected void dispatch(final AddInvestigationEventHandler handler) {
        handler.addInvestigations(this);
    }
}
