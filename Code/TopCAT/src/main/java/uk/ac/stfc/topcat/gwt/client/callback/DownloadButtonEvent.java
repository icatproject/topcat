package uk.ac.stfc.topcat.gwt.client.callback;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * Download button event type.
 * 
 * <p/>
 * Note: For a given event, only the fields which are appropriate will be filled
 * in. The appropriate fields for each event are documented by the event source.
 * 
 * @see DownloadButton
 */
public class DownloadButtonEvent extends ButtonEvent {
    private String downloadName;

    /**
     * Creates a new button event.
     * 
     * @param button
     *            the source button
     * @param downloadName
     *            the name of the download
     */
    public DownloadButtonEvent(Button button) {
        super(button);
    }

    /**
     * Returns the download name.
     * 
     * @return the download name
     */
    public String getDownloadName() {
        if (downloadName == null) {
            return "";
        }
        return downloadName;
    }

    /**
     * Sets the download name.
     * 
     * @param downloadName
     *            the name of the download
     */
    public void setDownloadName(String downloadName) {
        this.downloadName = downloadName;
    }

}
