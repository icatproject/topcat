package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.AddDatasetEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddDatasetEventHandler extends EventHandler {
    void addDataset(AddDatasetEvent event);

}
