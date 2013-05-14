package org.icatproject.topcat.admin.client;


import java.util.List;

import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.client.service.DataServiceAsync;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginPanel extends Composite {
	// Annotation can be used to change the name of the associated xml file
	// @UiTemplate("LoginPanel.ui.xml")
	interface MyUiBinder extends UiBinder<Widget, LoginPanel> {
	}

	@UiField
	TextBox username;

	@UiField
	PasswordTextBox password;

	@UiField
	Label messageLabel;

	@UiField 
	Button login;

	@UiField 
	Button test;

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	public LoginPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		
	}

	@UiHandler("login")
	void handleLoginButtonClick(ClickEvent e) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

	    	public void onSuccess(String result) {
	    		messageLabel.setText(result);
	    	}
		};

		// make the call to the server
		System.out.println("LoginPanel: making call to DataService");
		dataService.login(username.getText(), password.getText(), callback);
	}

	@UiHandler("test")
	void handleTestButtonClick(ClickEvent e) {
		AsyncCallback<List<TFacility>> callback = new AsyncCallback<List<TFacility>>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

	    	public void onSuccess(List<TFacility> result) {
	    		String message = "";
	    		for ( TFacility facility : result ) {
	    			message +=	facility.getAuthenticationServiceType() + " " +
	    						facility.getAuthenticationServiceUrl() + " " +
	    						facility.getDownloadPluginName() + " " +
	    						facility.getName()	+ " " +
	    						facility.getSearchPluginName() ;
	    		}
	    		message += " \n";
	    		Window.alert(message);
	    	}
		};

		// make the call to the server
		System.out.println("LoginPanel: making call to DataService");
		dataService.getAllFacilities(callback);
	}

	@UiHandler("password")
	void onKeyPress(KeyPressEvent event)
    {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
        {
            login.click();
        }
    }
	
}
