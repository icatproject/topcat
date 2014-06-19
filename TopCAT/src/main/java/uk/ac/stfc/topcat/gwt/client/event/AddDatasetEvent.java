package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.AddDatasetEventHandler;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddDatasetEvent extends GwtEvent<AddDatasetEventHandler> {

    public static Type<AddDatasetEventHandler> TYPE = new Type<AddDatasetEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddDatasetEventHandler handler) {
        return eventBus.addHandler(AddDatasetEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddDatasetEventHandler handler) {
        return eventBus.addHandlerToSource(AddDatasetEvent.TYPE, source, handler);
    }

    private final String facilityName;
    private BaseModelData node;

    /**
     * @param facilityName
     *            the name of the facility
     */
    public AddDatasetEvent(final String facilityName, BaseModelData node) {                
        this.facilityName = facilityName;
        this.node = node;
    }    

    /**
     * @return the name of the facility
     */
    public String getFacilityName() {
        return this.facilityName;
    }    

    @Override
    public Type<AddDatasetEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final AddDatasetEventHandler handler) {
        handler.addDataset(this);
    }

    public BaseModelData getNode() {
        return node;
    }

}
