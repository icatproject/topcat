package uk.ac.stfc.topcat.gwt.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.impl.FormPanelImpl;
import com.google.gwt.user.client.ui.impl.FormPanelImplHost;

public class WindowNameFormPanelImpl extends FormPanelImpl {

    /**
     * Gets the response html from the loaded iframe's name property
     *
     * @param iframe the iframe from which the response html is to be extracted
     * @return the response html
     */
    public native String getContents(Element iframe) /*-{
      try {
        // Make sure the iframe's window & document are loaded.
        if (!iframe.contentWindow || !iframe.contentWindow.document)
          return null;

        // Get the response from window.name
        return iframe.contentWindow.name;
      } catch (e) {
        return null;
      }
    }-*/;

    /**
     * Hooks the iframe's onLoad event and the form's onSubmit event.
     *
     * @param iframe   the iframe whose onLoad event is to be hooked
     * @param form     the form whose onSubmit event is to be hooked
     * @param listener the listener to receive notification
     */
    public native void hookEvents(Element iframe, Element form,
        FormPanelImplHost listener) /*-{
      if (iframe) {
        iframe.onload = function() {
          // If there is no __formAction yet, this is a spurious onload
          // generated when the iframe is first added to the DOM.
          if (!iframe.__formAction)
            return;

          if(!iframe.__restoreSameDomain) {
            iframe.__restoreSameDomain = true;
            // restore same domain property of iframe to read window.name property
            iframe.contentWindow.location = 
               @com.google.gwt.core.client.GWT::getModuleBaseURL()() + 
                "clear.cache.gif";
            return;
          }
          listener.@com.google.gwt.user.client.ui.impl.FormPanelImplHost::onFrameLoad()();
        };
      }

      form.onsubmit = function() {
        // Hang on to the form's action url, needed in the
        // onload/onreadystatechange handler.
        if (iframe) {
          iframe.__formAction = form.action;
          iframe.__restoreSameDomain = false;
        }
        return listener.@com.google.gwt.user.client.ui.impl.FormPanelImplHost::onFormSubmit()();
      };
    }-*/;
  }

