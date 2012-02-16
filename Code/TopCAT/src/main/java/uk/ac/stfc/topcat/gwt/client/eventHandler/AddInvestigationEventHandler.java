package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddInvestigationEventHandler extends EventHandler {

    void addInvestigations(AddInvestigationEvent event);

}
