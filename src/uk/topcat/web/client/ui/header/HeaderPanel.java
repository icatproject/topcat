package uk.topcat.web.client.ui.header;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderPanel extends CellPanel {

	public void add(Widget child, Widget child2)
    {
		Element tr = DOM.createTR();
        Element td = DOM.createTD();
        Element td2 = DOM.createTD();
        
        DOM.appendChild(tr, td);       
        DOM.appendChild(tr, td2);
        
        
        DOM.appendChild(getBody(), tr);
        
        super.add(child, td);
        super.add(child2, td2);
    }
}
