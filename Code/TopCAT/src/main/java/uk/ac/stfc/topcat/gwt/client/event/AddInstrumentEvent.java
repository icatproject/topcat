package uk.ac.stfc.topcat.gwt.client.event;

import java.util.ArrayList;

import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInstrumentEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.Instrument;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddInstrumentEvent extends GwtEvent<AddInstrumentEventHandler> {

    public static Type<AddInstrumentEventHandler> TYPE = new Type<AddInstrumentEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddInstrumentEventHandler handler) {
        return eventBus.addHandler(AddInstrumentEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddInstrumentEventHandler handler) {
        return eventBus.addHandlerToSource(AddInstrumentEvent.TYPE, source, handler);
    }

    private final String facilityName;
    private final ArrayList<Instrument> instruments;

    public AddInstrumentEvent(final String facilityName, final ArrayList<Instrument> instruments) {
        this.facilityName = facilityName;
        this.instruments = instruments;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    public ArrayList<Instrument> getInstruments() {
        return this.instruments;
    }

    @Override
    public Type<AddInstrumentEventHandler> getAssociatedType() {
        return AddInstrumentEvent.TYPE;
    }

    @Override
    protected void dispatch(final AddInstrumentEventHandler handler) {
        handler.addInstruments(this);
    }
}
