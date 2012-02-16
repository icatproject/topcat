package uk.ac.stfc.topcat.gwt.client.event;

import java.util.ArrayList;

import uk.ac.stfc.topcat.gwt.client.eventHandler.AddMyDownloadEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddMyDownloadEvent extends GwtEvent<AddMyDownloadEventHandler> {

    public static Type<AddMyDownloadEventHandler> TYPE = new Type<AddMyDownloadEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddMyDownloadEventHandler handler) {
        return eventBus.addHandler(AddMyDownloadEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddMyDownloadEventHandler handler) {
        return eventBus.addHandlerToSource(AddMyDownloadEvent.TYPE, source, handler);
    }

    private final String facilityName;
    private final ArrayList<DownloadModel> myDownloads;

    public AddMyDownloadEvent(final String facility, final ArrayList<DownloadModel> myDownloads) {
        this.facilityName = facility;
        this.myDownloads = myDownloads;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    public ArrayList<DownloadModel> getMyDownloads() {
        return this.myDownloads;
    }

    @Override
    public Type<AddMyDownloadEventHandler> getAssociatedType() {
        return AddMyDownloadEvent.TYPE;
    }

    @Override
    protected void dispatch(final AddMyDownloadEventHandler handler) {
        handler.addMyDownloads(this);
    }
}
