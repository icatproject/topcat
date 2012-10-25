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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TConstants;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.Facility;
import uk.ac.stfc.topcat.gwt.client.model.ParameterModel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ParameterSearchSubPanel extends Composite {
    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
    private EventPipeLine eventBus;
    private ListField<Facility> listFieldFacility;
    private ComboBox<ParameterModel> comboBoxName;
    private ComboBox<ParameterModel> comboBoxUnits;
    private Set<String> types = new HashSet<String>();
    private CheckBox valueRange;
    private LabelField lblfldValue;
    private TextField<String> value;
    private TextField<String> valueMax;
    private LabelField lblfldValueDate;
    private DateField valueDate;
    private DateField valueDateMax;
    private Text errorMessage;
    private boolean dateValueSelected = false;

    public ParameterSearchSubPanel() {
        eventBus = EventPipeLine.getInstance();
        LayoutContainer topContainer = new LayoutContainer();
        LayoutContainer layoutContainer = new LayoutContainer();
        TableLayout tl_layoutContainer = new TableLayout(3);
        tl_layoutContainer.setCellSpacing(5);
        layoutContainer.setLayout(tl_layoutContainer);

        // Facility
        LabelField lblfldFacility = new LabelField("Facility");
        layoutContainer.add(lblfldFacility);

        listFieldFacility = new ListField<Facility>();
        listFieldFacility.setDisplayField("name");
        listFieldFacility.setStore(new ListStore<Facility>());
        listFieldFacility.addSelectionChangedListener(new SelectionChangedListener<Facility>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<Facility> se) {
                addNames(se.getSelection());
            }
        });
        layoutContainer.add(listFieldFacility);
        layoutContainer.add(new Text());

        // Name
        LabelField lblfldParamName = new LabelField("Parameter Name");
        layoutContainer.add(lblfldParamName);

        comboBoxName = new ComboBox<ParameterModel>();
        comboBoxName.addSelectionChangedListener(new SelectionChangedListener<ParameterModel>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<ParameterModel> se) {
                addUnits(se.getSelectedItem());
            }
        });
        comboBoxName.setStore(new ListStore<ParameterModel>());
        comboBoxName.setDisplayField("name");
        comboBoxName.setTypeAhead(true);
        comboBoxName.setTriggerAction(TriggerAction.ALL);
        layoutContainer.add(comboBoxName);

        comboBoxName.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        comboBoxName.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        layoutContainer.add(new Text());

        // Units
        LabelField lblfldParamUnits = new LabelField("Parameter Units");
        layoutContainer.add(lblfldParamUnits);

        comboBoxUnits = new ComboBox<ParameterModel>();
        comboBoxUnits.addSelectionChangedListener(new SelectionChangedListener<ParameterModel>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<ParameterModel> se) {
                getType(se.getSelectedItem());
            }
        });
        comboBoxUnits.setStore(new ListStore<ParameterModel>());
        comboBoxUnits.setDisplayField("units");
        comboBoxUnits.setTypeAhead(true);
        comboBoxUnits.setTriggerAction(TriggerAction.ALL);
        layoutContainer.add(comboBoxUnits);

        comboBoxUnits.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        comboBoxUnits.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        layoutContainer.add(new Text());

        // Parameter range selection box
        LabelField lblfldRange = new LabelField("Parameter Range");
        layoutContainer.add(lblfldRange);
        valueRange = new CheckBox();
        valueRange.setValue(false);
        layoutContainer.add(valueRange);

        valueRange.addListener(Events.Change, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                showParameterValueBoxes();
            }
        });
        layoutContainer.add(new Text());

        // Value
        lblfldValue = new LabelField("Parameter Value");
        layoutContainer.add(lblfldValue);

        value = new TextField<String>();
        layoutContainer.add(value);

        valueMax = new TextField<String>();
        layoutContainer.add(valueMax);
        valueMax.hide();

        // Date
        lblfldValueDate = new LabelField("Parameter Value");
        layoutContainer.add(lblfldValueDate);
        lblfldValueDate.hide();

        valueDate = new DateField();
        layoutContainer.add(valueDate);
        valueDate.hide();

        valueDateMax = new DateField();
        layoutContainer.add(valueDateMax);
        valueDateMax.hide();

        layoutContainer.add(new Text());
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());

        // Experiment Search Button
        Button btnSearchExp = new Button("Search Experiment Parameters");
        btnSearchExp.setToolTip("Get a list of experiments");
        btnSearchExp.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                errorMessage.setText("");
                if (validateInput()) {
                    TAdvancedSearchDetails searchDetails = getSearchDetails();
                    eventBus.searchForInvestigation(searchDetails);
                }
            }
        });
        layoutContainer.add(btnSearchExp);
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());

        // Dataset Search Button TODO

        // Datafile Search Button
        Button btnSearchFile = new Button("Search Data File Parameters");
        btnSearchFile.setToolTip("Get a list of data files");
        btnSearchFile.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                errorMessage.setText("");
                if (validateInput()) {
                    TAdvancedSearchDetails searchDetails = getSearchDetails();
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

        // Other bits
        topContainer.add(layoutContainer);
        topContainer.add(new Text());
        errorMessage = new Text();
        errorMessage.setText("");
        topContainer.add(errorMessage);
        topContainer.setHeight("310px");
        topContainer.setHeight("330px");
        initComponent(topContainer);
        setBorders(true);
        setAutoHeight(true);

        createLoginHandler();
        createLogoutHandler();
    }

    private TAdvancedSearchDetails getSearchDetails() {
        TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
        searchDetails.setFacilityList((ArrayList<String>) comboBoxUnits.getSelection().get(0).getFacilityNames());
        searchDetails.setParameterName(comboBoxUnits.getSelection().get(0).getName());
        searchDetails.setParameterUnits(comboBoxUnits.getSelection().get(0).getUnits());
        if (dateValueSelected) {
            searchDetails.setParameterValue(getDate(valueDate.getValue()));
        } else {
            searchDetails.setParameterValue(value.getValue());
        }
        if (valueRange.getValue()) {
            if (dateValueSelected) {
                searchDetails.setParameterValueMax(getDate(valueDateMax.getValue()));
            } else {
                searchDetails.setParameterValueMax(valueMax.getValue());
            }
        }
        return searchDetails;
    }

    private String getDate(Date date) {
        StringBuilder retDate = new StringBuilder();
        retDate.append("{ts ");
        DateTimeFormat f = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
        retDate.append(f.format(date));
        retDate.append("}");
        return retDate.toString();
    }

    private void addNames(final List<Facility> facilities) {
        comboBoxName.getStore().removeAll();
        comboBoxName.clear();
        if (facilities == null) {
            // this is a result of reset
            return;
        }
        for (final Facility facility : facilities) {
            EventPipeLine.getInstance().showRetrievingData();
            utilityService.getParameterNames(facility.getFacilityName(), new AsyncCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> result) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    if (result.size() > 0) {
                        for (String name : result) {
                            if (comboBoxName.getStore().findModel("name", name) != null) {
                                comboBoxName.getStore().findModel("name", name)
                                        .addFacilityName(facility.getFacilityName());
                            } else {
                                comboBoxName.getStore().add(new ParameterModel(facility.getFacilityName(), name));
                            }
                        }
                        if (facilities.size() == 1 && comboBoxName.getStore().getCount() == 1) {
                            // We are only checking one facility so we are not
                            // waiting for more data to come back
                            comboBoxName.setValue(comboBoxName.getStore().getAt(0));
                        } else {
                            comboBoxName.focus();
                            comboBoxName.expand();
                        }
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    EventPipeLine.getInstance().showErrorDialog(
                            "Error retrieving parameter names from " + facility.getFacilityName());
                }
            });
        }
    }

    private void addUnits(final ParameterModel parameter) {
        comboBoxUnits.getStore().removeAll();
        comboBoxUnits.clear();
        if (parameter == null) {
            // this is a result of reset
            return;
        }

        // Create an all units parameter
        ParameterModel allUnits = new ParameterModel(parameter.getName(), TConstants.ALL_UNITS, "");
        for (final String facilityName : parameter.getFacilityNames()) {
            allUnits.addFacilityName(facilityName);
        }
        comboBoxUnits.getStore().add(allUnits);

        // Get the possible units from each facility
        for (final String facilityName : parameter.getFacilityNames()) {
            EventPipeLine.getInstance().showRetrievingData();
            utilityService.getParameterUnits(facilityName, parameter.getName(), new AsyncCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> result) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    if (result.size() > 0) {
                        for (String units : result) {
                            List<ParameterModel> models = comboBoxUnits.getStore().findModels("name",
                                    parameter.getName());
                            if (models.size() > 0) {
                                // found possible matches
                                boolean found = false;
                                for (ParameterModel model : models) {
                                    if (model.getUnits().equals(units)) {
                                        // found match on name and units
                                        model.addFacilityName(facilityName);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    comboBoxUnits.getStore().add(
                                            new ParameterModel(facilityName, parameter.getName(), units, ""));
                                }
                            } else {
                                comboBoxUnits.getStore().add(
                                        new ParameterModel(facilityName, parameter.getName(), units, ""));
                            }
                        }
                        comboBoxUnits.focus();
                        if (parameter.getFacilityNames().size() == 1 && comboBoxUnits.getStore().getCount() == 2) {
                            // We are only checking one facility so we are not
                            // waiting for more data to come back
                            comboBoxUnits.setValue(comboBoxUnits.getStore().getAt(1));
                        }
                        if (comboBoxUnits.getStore().getCount() > 2) {
                            comboBoxUnits.expand();
                        }
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    EventPipeLine.getInstance()
                            .showErrorDialog("Error retrieving parameter units from " + facilityName);
                }
            });
        }
    }

    /**
     * Ask all of the selected facilities for the parameter types for the given
     * name and unit.
     * 
     * @param selectedModel
     */
    private void getType(ParameterModel selectedModel) {
        types.clear();
        if (selectedModel == null) {
            // this is a result of reset
            return;
        }
        if (selectedModel.getUnits().equals(TConstants.ALL_UNITS)) {
            if (comboBoxUnits.getStore().getCount() > 2) {
                // As there are more than 1 set of units we will not bother
                // getting the types, the server can do the type checking
                dateValueSelected = false;
                showParameterValueBoxes();
                return;
            } else {
                // even though --ALL-- is selected there is only one unit so we
                // will do type checking
                selectedModel = comboBoxUnits.getStore().getAt(1);
            }
        }
        for (final String facilityName : selectedModel.getFacilityNames()) {
            EventPipeLine.getInstance().showRetrievingData();
            utilityService.getParameterTypes(facilityName, selectedModel.getName(), selectedModel.getUnits(),
                    new AsyncCallback<ArrayList<String>>() {
                        @Override
                        public void onSuccess(ArrayList<String> results) {
                            EventPipeLine.getInstance().hideRetrievingData();
                            for (String result : results) {
                                types.add(result);
                            }
                            if (types.size() == 1 && types.contains("DATE_AND_TIME")) {
                                dateValueSelected = true;
                                showParameterValueBoxes();
                                valueDate.focus();
                            } else {
                                dateValueSelected = false;
                                showParameterValueBoxes();
                                value.focus();
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            EventPipeLine.getInstance().hideRetrievingData();
                            EventPipeLine.getInstance().showErrorDialog(
                                    "Error retrieving parameter type from " + facilityName);
                        }
                    });
        }
    }

    private boolean validateInput() {
        if (listFieldFacility.getSelection().size() == 0) {
            errorMessage.setText("Please select at least one Facility");
            listFieldFacility.focus();
            return false;
        }
        if (comboBoxName.getSelection().size() == 0) {
            errorMessage.setText("Please select a 'Parameter Name'");
            comboBoxName.focus();
            return false;
        }
        if (comboBoxUnits.getSelection().size() == 0) {
            errorMessage.setText("Please select the 'Parameter Units'");
            comboBoxUnits.focus();
            return false;
        }
        if (dateValueSelected) {
            if (valueDate.getValue() == null) {
                errorMessage.setText("Please enter a 'Parameter Value'");
                valueDate.focus();
                return false;
            }
        } else {
            if (value.getValue() == null) {
                errorMessage.setText("Please enter a 'Parameter Value'");
                value.focus();
                return false;
            }
        }
        if (types.size() == 1) {
            // As there is only one type we can do type checking
            if (types.contains("NUMERIC")) {
                try {
                    new Double(value.getValue());
                } catch (NumberFormatException e) {
                    errorMessage.setText("'Parameter Value' must be numeric");
                    value.focus();
                    return false;
                }
            }
        }

        if (valueRange.getValue()) {
            if (dateValueSelected) {
                if (valueDateMax.getValue() == null) {
                    errorMessage.setText("Please enter a second 'Parameter Value'");
                    valueDateMax.focus();
                    return false;
                }
            } else {
                if (valueMax.getValue() == null) {
                    errorMessage.setText("Please enter a second 'Parameter Value'");
                    valueMax.focus();
                    return false;
                }
            }
            if (types.size() == 1) {
                // As there is only one type we can do type checking
                if (types.contains("NUMERIC")) {
                    try {
                        new Double(valueMax.getValue());
                    } catch (NumberFormatException e) {
                        errorMessage.setText("Second 'Parameter Value' must be numeric");
                        valueMax.focus();
                        return false;
                    }
                    if (new Double(value.getValue()) > new Double(valueMax.getValue())) {
                        errorMessage.setText("Second 'Parameter Value' must be equal or greater than the first");
                        valueMax.focus();
                        return false;
                    }
                } else if (types.contains("DATE_AND_TIME")) {
                    if (valueDate.getValue().compareTo(valueDateMax.getValue()) > 0) {
                        errorMessage.setText("Second 'Parameter Value' must be equal or greater than the first");
                        valueDateMax.focus();
                        return false;
                    }
                } else {
                    if (value.getValue().compareTo(valueMax.getValue()) > 0) {
                        errorMessage.setText("Second 'Parameter Value' must be equal or greater than the first");
                        valueMax.focus();
                        return false;
                    }
                }

            }
        }
        return true;
    }

    private void showParameterValueBoxes() {
        valueMax.clear();
        valueDateMax.clear();
        if (dateValueSelected) {
            // show date stuff
            lblfldValueDate.show();
            valueDate.show();
            if (valueRange.getValue()) {
                valueDateMax.show();
                if (valueDate.getValue() == null) {
                    valueDate.focus();
                } else {
                    valueDateMax.focus();
                }
            } else {
                valueDateMax.hide();
            }
            // hide numeric/string stuff
            lblfldValue.hide();
            value.hide();
            valueMax.hide();
        } else {
            // hide date stuff
            lblfldValueDate.hide();
            valueDate.hide();
            valueDateMax.hide();
            // show numeric/string stuff
            lblfldValue.show();
            value.show();
            if (valueRange.getValue()) {
                valueMax.show();
                if (value.getValue() == null || value.getValue().isEmpty()) {
                    value.focus();
                } else {
                    valueMax.focus();
                }
            } else {
                valueMax.hide();
            }
        }
    }

    private void reset() {
        errorMessage.setText("");
        types.clear();
        value.clear();
        valueMax.clear();
        valueDate.clear();
        valueDateMax.clear();
        dateValueSelected = false;
        comboBoxUnits.getStore().removeAll();
        comboBoxUnits.clear();
        comboBoxName.clear();
        comboBoxName.getStore().removeAll();
        listFieldFacility.clear();
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
