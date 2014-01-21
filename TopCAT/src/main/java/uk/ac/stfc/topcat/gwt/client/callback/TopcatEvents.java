package uk.ac.stfc.topcat.gwt.client.callback;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.Component;

/**
 * This class fires a resize event.
 * 
 */
public class TopcatEvents extends Component {

    protected TopcatEvents() {
    }

    public void fireResize() {
        fireEvent(Events.Resize);
    }

}
