package org.icatproject.topcat.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
//import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class AdminEntryPoint implements EntryPoint {
	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("AdminEntryPoint.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, AdminEntryPoint> {
	}
	
	@UiField
	TabLayoutPanel tabLayoutPanel;


	AdminUI adminUI = new AdminUI();
	AdminMessenger adminMsg = new AdminMessenger();

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public void onModuleLoad() {
		RootLayoutPanel rp = RootLayoutPanel.get();
		rp.add(uiBinder.createAndBindUi(this));
		
		tabLayoutPanel.setAnimationDuration(1000);
		tabLayoutPanel.add(adminUI, "DB Managment");
		tabLayoutPanel.add(adminMsg, "TopCat Message Generater");

	}
}
