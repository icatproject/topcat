package uk.ac.stfc.topcat.gwt.client.widget;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.impl.FormPanelImpl;

public class FixedFormPanel extends FormPanel {
    
    @Override
    protected void onAttach() {
        super.onAttach();
        Element iFrame = getIFrameElement();
        Element fixedIFrame = createFixedFrame();
        
        FormPanelImpl impl = getImpl();
        impl.unhookEvents(iFrame, getLayoutTarget().dom);
        XDOM.getBody().removeChild(iFrame);
        
        XDOM.getBody().appendChild(fixedIFrame);
        impl.hookEvents(fixedIFrame, getLayoutTarget().dom, this);
        
        setIFrameElement(fixedIFrame);
        this.setVisible(false);
    }
    
    private Element createFixedFrame() {
        Element dummy = DOM.createDiv();
        DOM.setInnerHTML(dummy, "<iframe src=\"javascript:''\" name='" + getFrameName()
            + "' style='position:absolute;width:0;height:0;border:0'>");

        return DOM.getFirstChild(dummy);
      }
    
    private native String getFrameName() /*-{
        return this.@com.extjs.gxt.ui.client.widget.form.FormPanel::frameName;
    }-*/;
    
    private native Element getIFrameElement() /*-{
        return this.@com.extjs.gxt.ui.client.widget.form.FormPanel::iframe;
    }-*/;
    
    private native void setIFrameElement(Element fixedIFrame) /*-{
        this.@com.extjs.gxt.ui.client.widget.form.FormPanel::iframe = fixedIFrame;
    }-*/;
    
    private native FormPanelImpl getImpl() /*-{
        return @com.extjs.gxt.ui.client.widget.form.FormPanel::impl;
    }-*/;

}