/**
 * 
 * Copyright (c) 2009-2013
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
package uk.ac.stfc.topcat.gwt.client.authentication;

/**
 * Imports
 */
import java.util.HashMap;
import java.util.Map;

import uk.ac.stfc.topcat.gwt.client.LoginInterface;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.model.AuthenticationModel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * This is an authentication widget that gets the user name and password.
 */
public class DefaultAuthenticationWidget extends Composite {
    private LoginInterface loginHandler = null;
    private TextField<String> txtFldPassword;
    private TextField<String> txtFldUsername;
    private AuthenticationModel authenticationModel;    

    public DefaultAuthenticationWidget() {
        LayoutContainer mainContainer = new LayoutContainer();
        VerticalPanel panel = new VerticalPanel();
        FlexTable flexTable = new FlexTable();
        flexTable.setSize("304px", "100px");
        
        final Button btnLogin = new Button("Login");

        LabelField lblfldUsername = new LabelField("Username");
        flexTable.setWidget(1, 0, lblfldUsername);

        txtFldUsername = new TextField<String>();
        flexTable.setWidget(1, 1, txtFldUsername);
        txtFldUsername.setFieldLabel("New TextField");

        LabelField lblfldPassword = new LabelField("Password");
        flexTable.setWidget(2, 0, lblfldPassword);

        txtFldPassword = new TextField<String>();
        // On enter key in password box. fire click on login button.
        txtFldPassword.addListener(Events.SpecialKey, new Listener<FieldEvent>() {
            @Override
            public void handleEvent(FieldEvent e) {
                if (e.getKeyCode() == KeyCodes.KEY_ENTER) {
                    btnLogin.fireEvent(Events.Select);
                }
            }
        });
        txtFldPassword.setPassword(true);
        flexTable.setWidget(2, 1, txtFldPassword);
        txtFldPassword.setFieldLabel("New TextField");
        
        FlexTable buttonTable = new FlexTable();
        buttonTable.setSize("304px", "70px");

        btnLogin.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (loginHandler != null) {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("username", txtFldUsername.getValue());
                    parameters.put("password", txtFldPassword.getValue());                    
                    loginHandler.onLoginOk(authenticationModel.getFacilityName(), authenticationModel.getType(),
                            parameters);
                }
            }
        });
        buttonTable.setWidget(0, 0, btnLogin);
        btnLogin.setSize("50", "25");

        Button btnCancel = new Button("Cancel");
        btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (loginHandler != null)
                    loginHandler.onLoginCancel();
                EventPipeLine.getEventBus().fireEventFromSource(new LogoutEvent(authenticationModel.getFacilityName()),
                        authenticationModel.getFacilityName());
            }
        });

        buttonTable.setWidget(0, 1, btnCancel);
        btnCancel.setSize("50", "25");
        buttonTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
        buttonTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);               
        panel.add(flexTable);
        panel.add(buttonTable);
        

        mainContainer.add(panel);
        mainContainer.setHeight("175px");
        initComponent(mainContainer);
        setAutoHeight(true);
    }

    public void setAuthenticationModel(AuthenticationModel authenticationModel) {
        this.authenticationModel = authenticationModel;
    }

    public void setLoginHandler(LoginInterface loginHandler) {
        this.loginHandler = loginHandler;
    }

    /**
     * Show Login Widget. First clear out password. If user name exists focus on
     * password.
     */
    @Override
    public void focus() {
        txtFldPassword.clear();
        if (txtFldUsername.isDirty()) {
            txtFldPassword.focus();
        } else {
            txtFldUsername.focus();
        }
        super.focus();
    }
}
