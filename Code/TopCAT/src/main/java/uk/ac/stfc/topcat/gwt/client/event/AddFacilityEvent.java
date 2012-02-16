package uk.ac.stfc.topcat.gwt.client.event;

import java.util.ArrayList;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddFacilityEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.Facility;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddFacilityEvent extends GwtEvent<AddFacilityEventHandler> {

    public static Type<AddFacilityEventHandler> TYPE = new Type<AddFacilityEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddFacilityEventHandler handler) {
        return eventBus.addHandler(AddFacilityEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddFacilityEventHandler handler) {
        return eventBus.addHandlerToSource(AddFacilityEvent.TYPE, source, handler);
    }

    private final ArrayList<Facility> facilities;
    private final ArrayList<TFacility> tFacilities;

    public AddFacilityEvent(final ArrayList<TFacility> tFacilities) {
        this.tFacilities = tFacilities;
        facilities = new ArrayList<Facility>();
        for (TFacility facility : tFacilities) {
            facilities.add(new Facility(facility.getName(), facility.getPluginName()));
        }
    }

    public ArrayList<Facility> getFacilities() {
        return this.facilities;
    }

    public ArrayList<TFacility> getTFacilities() {
        return this.tFacilities;
    }

    @Override
    public Type<AddFacilityEventHandler> getAssociatedType() {
        return AddFacilityEvent.TYPE;
    }

    @Override
    protected void dispatch(final AddFacilityEventHandler handler) {
        handler.addFacilities(this);
    }
}
