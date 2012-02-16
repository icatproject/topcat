package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.AddMyInvestigationEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddMyInvestigationEventHandler extends EventHandler {

    void addMyInvestigations(AddMyInvestigationEvent event);

}
