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
package uk.ac.stfc.topcat.gwt.client.widget;

/**
 * Imports
 */
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * This is a widget, used in search panel. It shows advanced search options to
 * be set by user.
 * 
 * <p>
 * 
 * @author Mr. wayne Chung
 * @version 1.0, &nbsp; 21-Nov-2013
 * @since iCAT Version 4.3
 */

public class FreeTextSearchSubPanel extends Composite {    
    private EventPipeLine eventBus;    
    private TextField<String> txtFldQuery;

    public FreeTextSearchSubPanel() {
        eventBus = EventPipeLine.getInstance();

        LayoutContainer layoutContainer = new LayoutContainer();

        FlexTable flexTable = new FlexTable();
        flexTable.setCellSpacing(5);
        layoutContainer.add(flexTable);

        LabelField lblfldQuery = new LabelField("Search");
        flexTable.setWidget(0, 0, lblfldQuery);

        txtFldQuery = new TextField<String>();
        flexTable.setWidget(0, 1, txtFldQuery);        
        
        flexTable.setWidget(1, 0, new Text());

        Button btnSearch = new Button("Search");
        btnSearch.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent e) {
                
                if (validateInput()) {                    
                    TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
                    searchDetails.setFreeTextQuery(txtFldQuery.getValue().trim());
                    searchDetails.setSearchAllData(true);
                    
                    eventBus.searchForInvestigationByFreeText(searchDetails);                    
                }
                
            }
        });
        flexTable.setWidget(2, 1, btnSearch);
        flexTable.setWidget(3, 0, new Text());

        initComponent(layoutContainer);
        layoutContainer.setBorders(true);        
        
        createLoginHandler();        
        createLogoutHandler();
    }
    
    
    private boolean validateInput() {
        if (txtFldQuery.getValue() == null || txtFldQuery.getValue().trim().isEmpty()) {
            return false;            
        }
           
        return true;
    }    
    
    
    /**
     * Setup a handler to react to Login events.
     */
    private void createLoginHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                
            }
        });
    }
    
    
    /**
     * Setup a handler to react to Logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {                
                
            }
        });
    }
    
    
    

}
