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

import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.Facility;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

public class ParameterSearchSubPanel extends Composite {
    private EventPipeLine eventBus;
    private ListField<Facility> listFieldFacility;
    private TextField<String> paramName;
    private TextField<String> paramUnits;
    private TextField<String> paramValue;
    private Text errorMessage;

    public ParameterSearchSubPanel() {
        eventBus = EventPipeLine.getInstance();
        LayoutContainer topContainer = new LayoutContainer();
        LayoutContainer layoutContainer = new LayoutContainer();
        TableLayout tl_layoutContainer = new TableLayout(2);
        tl_layoutContainer.setCellSpacing(5);
        layoutContainer.setLayout(tl_layoutContainer);

        LabelField lblfldFacility = new LabelField("Facility");
        layoutContainer.add(lblfldFacility);

        listFieldFacility = new ListField<Facility>();
        listFieldFacility.setDisplayField("name");
        listFieldFacility.setStore(new ListStore<Facility>());
        layoutContainer.add(listFieldFacility);

        LabelField lblfldParamName = new LabelField("Parameter Name");
        layoutContainer.add(lblfldParamName);

        paramName = new TextField<String>();
        layoutContainer.add(paramName);
        paramName.setFieldLabel("New TextField");

        LabelField lblfldParamUnits = new LabelField("Parameter Units");
        layoutContainer.add(lblfldParamUnits);

        paramUnits = new TextField<String>();
        layoutContainer.add(paramUnits);
        paramUnits.setFieldLabel("New TextField");

        LabelField lblfldParamValue = new LabelField("Parameter Value");
        layoutContainer.add(lblfldParamValue);

        paramValue = new TextField<String>();
        layoutContainer.add(paramValue);
        paramValue.setFieldLabel("New TextField");

        layoutContainer.add(new Text());
        layoutContainer.add(new Text());

        // Experiment Search Button
        Button btnSearchExp = new Button("Search Experiments");
        btnSearchExp.setToolTip("Get a list of experiments");
        btnSearchExp.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                errorMessage.setText("");
                if (validateInput()) {
                    TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
                    searchDetails.setFacilityList(getFacilitySelectedList());
                    searchDetails.setParameterName(paramName.getValue());
                    searchDetails.setParameterUnits(paramUnits.getValue());
                    searchDetails.setParameterValue(paramValue.getValue());
                    eventBus.searchForInvestigation(searchDetails);
                }
            }
        });
        layoutContainer.add(btnSearchExp);
        layoutContainer.add(new Text());

        // Dataset Search Button TODO

        // Datafile Search Button
        Button btnSearchFile = new Button("Search Data Files");
        btnSearchFile.setToolTip("Get a list of data files");
        btnSearchFile.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                errorMessage.setText("");
                if (validateInput()) {
                    TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
                    searchDetails.setFacilityList(getFacilitySelectedList());
                    searchDetails.setParameterName(paramName.getValue());
                    searchDetails.setParameterUnits(paramUnits.getValue());
                    searchDetails.setParameterValue(paramValue.getValue());
                    eventBus.searchForDatafilesByParameter(searchDetails);
                }
            }
        });
        layoutContainer.add(btnSearchFile);

        // Reset Button
        Button btnReset = new Button("Reset");
        btnReset.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                reset();
            }
        });

        layoutContainer.add(btnReset);

        topContainer.add(layoutContainer);
        topContainer.add(new Text());
        errorMessage = new Text();
        errorMessage.setText("");
        topContainer.add(errorMessage);
        topContainer.setHeight("310px");
        initComponent(topContainer);
        setBorders(true);
        setAutoHeight(true);

        createLoginHandler();
        createLogoutHandler();
    }

    private ArrayList<String> getFacilitySelectedList() {
        List<Facility> facilityList = listFieldFacility.getSelection();
        ArrayList<String> resultFacility = new ArrayList<String>();
        for (Facility facility : facilityList) {
            resultFacility.add(facility.getFacilityName());
        }
        return resultFacility;
    }

    private boolean validateInput() {
        if (listFieldFacility.getSelection().size() == 0) {
            errorMessage.setText("Please select at least one Facility");
            listFieldFacility.focus();
            return false;
        }
        if (paramName.getValue() == null) {
            errorMessage.setText("Please enter a 'Parameter Name'");
            paramName.focus();
            return false;
        }
        if (paramUnits.getValue() == null) {
            errorMessage.setText("Please enter the 'Parameter Units'");
            paramUnits.focus();
            return false;
        }
        if (paramValue.getValue() == null) {
            errorMessage.setText("Please enter a 'Parameter Value'");
            paramValue.focus();
            return false;
        }
        return true;
    }

    private void reset() {
        listFieldFacility.clear();
        errorMessage.setText("");
        paramName.clear();
        paramUnits.clear();
        paramValue.clear();
        listFieldFacility.focus();
    }

    /**
     * Setup a handler to react to Login events.
     */
    private void createLoginHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                listFieldFacility.getStore().add(new Facility(event.getFacilityName(), null));
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
                Facility facility = listFieldFacility.getStore().findModel("name", event.getFacilityName());
                listFieldFacility.getStore().remove(facility);
            }
        });
    }

}
