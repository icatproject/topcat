package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.AddInstrumentEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddInstrumentEventHandler extends EventHandler {

    void addInstruments(AddInstrumentEvent event);

}
