package uk.ac.stfc.topcat.gwt.client.widget;

import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A download button component. When the download button is pressed the user is
 * presented with a prompt box into which they can enter a name for the
 * download.
 * 
 * Fires event Select(downloadName)
 * 
 * <dl>
 * <dt><b>Events:</b></dt>
 * 
 * <dd><b>Select</b> : DownloadEvent(button, item)<br>
 * <div>Fires after a download name has been entered.</div>
 * <ul>
 * <li>button : this</li>
 * <li>downloadName : the name of the download</li>
 * </ul>
 * </dd>
 * 
 * </dl>
 * 
 */
public class DownloadButton extends Button {

    public DownloadButton() {
        super(" Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload()));

        this.addListener(Events.BeforeSelect, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                // prevent recursion
                if (be instanceof DownloadButtonEvent) {
                    return;
                }
                be.setCancelled(true);
                final MessageBox box = MessageBox.prompt("Download ", "Please enter a name for your download:");
                // listen for a button being pressed
                box.addCallback(new Listener<MessageBoxEvent>() {
                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked() != null
                                && be.getButtonClicked().getText().equalsIgnoreCase(MessageBox.OK)) {
                            DownloadButtonEvent de = createDownloadButtonEvent();
                            de.setDownloadName(be.getValue());
                            fireEvent(Events.Select, de);
                        }
                    }
                });
                // listen for the enter key in the text box
                box.getTextBox().addKeyListener(new KeyListener() {
                    @Override
                    public void componentKeyPress(ComponentEvent ce) {
                        if (ce.getKeyCode() == KeyCodes.KEY_ENTER) {
                            DownloadButtonEvent de = createDownloadButtonEvent();
                            de.setDownloadName(box.getTextBox().getValue());
                            fireEvent(Events.Select, de);
                            box.close();
                        }
                    }
                });
            }
        });
    }

    private DownloadButtonEvent createDownloadButtonEvent() {
        return new DownloadButtonEvent(this);
    }

}
