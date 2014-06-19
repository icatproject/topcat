package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.AddDatafileEventHandler;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddDatafileEvent extends GwtEvent<AddDatafileEventHandler> {

    public static Type<AddDatafileEventHandler> TYPE = new Type<AddDatafileEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddDatafileEventHandler handler) {
        return eventBus.addHandler(AddDatafileEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddDatafileEventHandler handler) {
        return eventBus.addHandlerToSource(AddDatafileEvent.TYPE, source, handler);
    }

    private final String facilityName;
    private BaseModelData node;

    /**
     * @param facilityName
     *            the name of the facility
     */
    public AddDatafileEvent(final String facilityName,  BaseModelData node) {        
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
    public Type<AddDatafileEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final AddDatafileEventHandler handler) {
        handler.addDatafile(this);
    }

    public BaseModelData getNode() {
        return node;
    }

}
