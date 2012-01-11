/**
 * 
 * Copyright (c) 2009-2010
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.gwt.client.widget;
/**
 * Imports
 */
import uk.ac.stfc.topcat.gwt.client.LoginInterface;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

/**
 * This class is a widget for login window.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class LoginWidget extends Window {

	private LoginInterface loginHandler = null;
	private TextField<String> txtFldPassword;
	private TextField<String> txtFldUsername;
	private Button btnLogin;
	private String facilityName;
	public LoginWidget() {
		//Auto focus to username text box
		addListener(Events.OnFocus, new Listener<ComponentEvent>() {
			public void handleEvent(ComponentEvent e) {
				txtFldUsername.focus();
			}
		});
		setBlinkModal(true);
		setModal(true);

		setHeading("New Window");
		RowLayout rowLayout=new RowLayout(Orientation.VERTICAL);		
		setLayout(rowLayout);
		
		FlexTable flexTable = new FlexTable();
		flexTable.setSize("304px", "170px");
		
		LabelField lblfldUsername = new LabelField("Username");
		flexTable.setWidget(0, 0, lblfldUsername);
		
		txtFldUsername = new TextField<String>();
		flexTable.setWidget(0, 1, txtFldUsername);
		txtFldUsername.setFieldLabel("New TextField");
		
		LabelField lblfldPassword = new LabelField("Password");
		flexTable.setWidget(1, 0, lblfldPassword);
		
		txtFldPassword = new TextField<String>();
		//On enter key in password box. fire click on login button.
		txtFldPassword.addListener(Events.SpecialKey, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent e) {
				if(e.getKeyCode() == KeyCodes.KEY_ENTER){
					btnLogin.fireEvent(Events.Select);
				}
			}
		});
		txtFldPassword.setPassword(true);
		flexTable.setWidget(1, 1, txtFldPassword);
		txtFldPassword.setFieldLabel("New TextField");
		
		btnLogin = new com.extjs.gxt.ui.client.widget.button.Button("Login");
		btnLogin.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if(loginHandler!=null)
					loginHandler.onLoginOk(facilityName,(String)txtFldUsername.getValue(), (String)txtFldPassword.getValue());				
			}
		});
		flexTable.setWidget(2, 0, btnLogin);
		btnLogin.setSize("50", "25");
		
		com.extjs.gxt.ui.client.widget.button.Button btnCancel = new com.extjs.gxt.ui.client.widget.button.Button("Cancel");
		btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if(loginHandler!=null)
					loginHandler.onLoginCancel();				
			}
		});

		flexTable.setWidget(2, 1, btnCancel);
		btnCancel.setSize("50", "25");
		flexTable.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		add(flexTable);
		flexTable.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_MIDDLE);
	}

	public void setLoginHandler(LoginInterface loginHandler){
		this.loginHandler = loginHandler;
	}
	
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
		setHeading("Login to "+facilityName);		
	}
	
	public String getFacilityName() {
		return this.facilityName;
	}
	
    /**
     * Show Login Widget. First clear out password. If user name exists focus on
     * password.
     */
    @Override
    public void show() {
        txtFldPassword.clear();
        if (txtFldUsername.isDirty()) {
            setFocusWidget(txtFldPassword);
        } else {
            setFocusWidget(txtFldUsername);
        }
        super.show();
    }
}
