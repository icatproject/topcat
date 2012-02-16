package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.AddFacilityEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddFacilityEventHandler extends EventHandler {

    void addFacilities(AddFacilityEvent event);

}
