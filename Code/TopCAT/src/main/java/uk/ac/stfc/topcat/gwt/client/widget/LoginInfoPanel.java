/**
 * 
 * Copyright (c) 2009-2012
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
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginCheckCompleteEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * This is a widget that holds a facility information and has a button to
 * login/logout of a facility.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class LoginInfoPanel extends Composite {
    Button btnLogin;
    private LabelField lblFieldFacility;
    private HorizontalPanel horizontalPanel;
    WaitDialog waitDialog;
    EventPipeLine eventPipeLine;
    boolean validLogin;
    TFacility facility;

    public LoginInfoPanel(EventPipeLine epl, TFacility name) {
        eventPipeLine = epl;
        facility = name;

        horizontalPanel = new HorizontalPanel();
        horizontalPanel.setHorizontalAlign(HorizontalAlignment.RIGHT);
        horizontalPanel.setSize("100%", "30px");

        lblFieldFacility = new LabelField("New LabelField");
        TableData td_lblFieldFacility = new TableData();
        td_lblFieldFacility.setHorizontalAlign(HorizontalAlignment.RIGHT);
        horizontalPanel.add(lblFieldFacility, td_lblFieldFacility);
        lblFieldFacility.setText(facility.getName());

        btnLogin = new Button("Login");
        btnLogin.setIconAlign(IconAlign.RIGHT);
        btnLogin.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconLogin()));
        TableData td_btnLogin = new TableData();
        td_btnLogin.setHorizontalAlign(HorizontalAlignment.RIGHT);
        horizontalPanel.add(btnLogin, td_btnLogin);
        waitDialog = new WaitDialog();
        waitDialog.hide();

        btnLogin.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (validLogin) {
                    eventPipeLine.facilityLogout(facility.getName());
                } else {
                    eventPipeLine.showLoginWidget(facility.getName());
                }
            }
        });

        initComponent(horizontalPanel);
        validLogin = false;

        LoginEvent.registerToSource(EventPipeLine.getEventBus(), facility.getName(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                validLogin = true;
                btnLogin.setText("Logout");
                if (event.isStatusCheck()) {
                    EventPipeLine.getEventBus().fireEventFromSource(
                            new LoginCheckCompleteEvent(event.getFacilityName(), true), event.getFacilityName());
                }
            }
        });

        LogoutEvent.registerToSource(EventPipeLine.getEventBus(), facility.getName(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                validLogin = false;
                btnLogin.setText("Login");
                if (event.isStatusCheck()) {
                    EventPipeLine.getEventBus().fireEventFromSource(
                            new LoginCheckCompleteEvent(event.getFacilityName(), false), event.getFacilityName());
                }
            }
        });

        horizontalPanel.layout();
    }

    public boolean isValidLogin() {
        return validLogin;
    }

}
