package uk.ac.stfc.topcat.gwt.client.eventHandler;

import uk.ac.stfc.topcat.gwt.client.event.AddMyDownloadEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddMyDownloadEventHandler extends EventHandler {

    void addMyDownloads(AddMyDownloadEvent event);

}
