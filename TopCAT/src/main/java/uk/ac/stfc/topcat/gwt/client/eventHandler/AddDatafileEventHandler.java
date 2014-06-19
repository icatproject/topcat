package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.AddDatafileEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddDatafileEventHandler extends EventHandler {
    void addDatafile(AddDatafileEvent event);

}
