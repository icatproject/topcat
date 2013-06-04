package org.icatproject.topcat.admin.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
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
	
	
	LoginPanel loginPanel = new LoginPanel();
	AdminUI adminUI = new AdminUI();
	
	
	
	
	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	public void onModuleLoad() {

		RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));

	//	tabLayoutPanel.add(loginPanel, "Login");
		tabLayoutPanel.add(adminUI, "UI");

	}
}
