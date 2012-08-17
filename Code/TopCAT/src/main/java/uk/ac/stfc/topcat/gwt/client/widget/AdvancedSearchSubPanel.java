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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.callback.InvestigationSearchCallback;
import uk.ac.stfc.topcat.gwt.client.event.AddInstrumentEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInstrumentEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInvestigationEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.Facility;
import uk.ac.stfc.topcat.gwt.client.model.Instrument;
import uk.ac.stfc.topcat.gwt.client.model.InvestigationType;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * This is a widget, used in search panel. It shows advanced search options to
 * be set by user.
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */

public class AdvancedSearchSubPanel extends Composite {
    private ListField<Facility> listFieldFacility;
    private ListField<Instrument> lstInstrument;
    private ListField<InvestigationType> lstInvestigationTypes;
    private TextField<String> txtFldProposalTitle;
    private TextField<String> txtFldProposalAbstract;
    private TextField<String> txtFldSampleName;
    private TextField<String> txtFldInvestigatorName;
    private TextField<String> txtFldDataFileName;
    private DateField dateFieldStart;
    private DateField dateFieldEnd;
    private TextField<String> txtFldRunNo;
    private InvestigationSearchCallback invSearchCallback;

    private HashMap<String, ArrayList<Instrument>> instrumentList;
    private HashMap<String, ArrayList<InvestigationType>> investigationTypeList;

    public AdvancedSearchSubPanel() {

        LayoutContainer layoutContainer = new LayoutContainer();

        FlexTable flexTable = new FlexTable();
        flexTable.setCellSpacing(5);
        layoutContainer.add(flexTable);

        LabelField lblfldProposalTitle = new LabelField("Proposal Title");
        flexTable.setWidget(0, 0, lblfldProposalTitle);

        txtFldProposalTitle = new TextField<String>();
        flexTable.setWidget(0, 1, txtFldProposalTitle);
        txtFldProposalTitle.setFieldLabel("New TextField");

        LabelField lblfldProposalAbstract = new LabelField("Proposal Abstract");
        flexTable.setWidget(1, 0, lblfldProposalAbstract);

        txtFldProposalAbstract = new TextField<String>();
        flexTable.setWidget(1, 1, txtFldProposalAbstract);
        txtFldProposalAbstract.setFieldLabel("New TextField");

        LabelField lblfldSample = new LabelField("Sample");
        flexTable.setWidget(2, 0, lblfldSample);

        txtFldSampleName = new TextField<String>();
        flexTable.setWidget(2, 1, txtFldSampleName);
        txtFldSampleName.setFieldLabel("New TextField");

        LabelField lblfldInvestigatorName = new LabelField("Investigator Name");
        lblfldInvestigatorName.setToolTip("Surname");
        flexTable.setWidget(3, 0, lblfldInvestigatorName);

        txtFldInvestigatorName = new TextField<String>();
        flexTable.setWidget(3, 1, txtFldInvestigatorName);
        txtFldInvestigatorName.setFieldLabel("New TextField");

        LabelField lblfldDatafileName = new LabelField("DataFile Name");
        flexTable.setWidget(4, 0, lblfldDatafileName);

        txtFldDataFileName = new TextField<String>();
        flexTable.setWidget(4, 1, txtFldDataFileName);
        txtFldDataFileName.setFieldLabel("New TextField");

        LabelField lblfldStartDate = new LabelField("Start Date");
        flexTable.setWidget(5, 0, lblfldStartDate);

        dateFieldStart = new DateField();
        flexTable.setWidget(5, 1, dateFieldStart);
        dateFieldStart.setFieldLabel("New DateField");
        dateFieldStart.getPropertyEditor().setFormat(
                DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));

        LabelField lblfldEndDate = new LabelField("End Date");
        flexTable.setWidget(5, 2, lblfldEndDate);

        dateFieldEnd = new DateField();
        flexTable.setWidget(5, 3, dateFieldEnd);
        dateFieldEnd.setFieldLabel("New DateField");
        dateFieldEnd.getPropertyEditor().setFormat(
                DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));

        LabelField lblfldRbNumbervisitId = new LabelField("Rb Number/Visit Id");
        flexTable.setWidget(6, 0, lblfldRbNumbervisitId);

        txtFldRunNo = new TextField<String>();
        flexTable.setWidget(6, 1, txtFldRunNo);
        txtFldRunNo.setFieldLabel("New TextField");

        LabelField lblfldFacility = new LabelField("Facility");
        flexTable.setWidget(8, 0, lblfldFacility);

        listFieldFacility = new ListField<Facility>();
        listFieldFacility.addSelectionChangedListener(new SelectionChangedListener<Facility>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<Facility> se) {
                updateListWidgets();
            }
        });
        listFieldFacility.setDisplayField("name");
        listFieldFacility.setStore(new ListStore<Facility>());
        flexTable.setWidget(8, 1, listFieldFacility);

        LabelField lblfldInvestigationType = new LabelField("Investigation Type");
        flexTable.setWidget(9, 0, lblfldInvestigationType);

        lstInvestigationTypes = new ListField<InvestigationType>();
        lstInvestigationTypes.setSize("100%", "100px");
        lstInvestigationTypes.setDisplayField("displayName");
        lstInvestigationTypes.setStore(new ListStore<InvestigationType>());
        lstInvestigationTypes.setAutoWidth(true);
        lstInvestigationTypes.setFieldLabel("New ListField");
        lstInvestigationTypes.getListView().setAutoWidth(true);
        flexTable.setWidget(9, 1, lstInvestigationTypes);

        LabelField lblfldInstrumentbeamLine = new LabelField("Instrument/Beam Line");
        flexTable.setWidget(10, 0, lblfldInstrumentbeamLine);

        lstInstrument = new ListField<Instrument>();
        lstInstrument.setDisplayField("displayName");
        lstInstrument.setStore(new ListStore<Instrument>());
        flexTable.setWidget(10, 1, lstInstrument);

        flexTable.setWidget(11, 0, new Text());

        Button btnSearch = new Button("Search");
        btnSearch.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent e) {
                searchAdvanced();
            }
        });
        flexTable.setWidget(12, 1, btnSearch);

        Button btnReset = new Button("Reset");
        btnReset.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent e) {
                resetWidgetValues();
            }
        });
        flexTable.setWidget(12, 2, btnReset);
        flexTable.getCellFormatter().setHorizontalAlignment(12, 1, HasHorizontalAlignment.ALIGN_CENTER);
        flexTable.getCellFormatter().setHorizontalAlignment(12, 2, HasHorizontalAlignment.ALIGN_CENTER);

        flexTable.setWidget(13, 0, new Text());

        initComponent(layoutContainer);
        layoutContainer.setBorders(true);
        instrumentList = new HashMap<String, ArrayList<Instrument>>();
        investigationTypeList = new HashMap<String, ArrayList<InvestigationType>>();

        createLoginHandler();
        createAddInstrumentHandler();
        createAddInvestigationHandler();
        createLogoutHandler();
    }

    public ListField<InvestigationType> getListFieldInvestigationType() {
        return lstInvestigationTypes;
    }

    public ArrayList<String> getFacilitySelectedList() {
        List<Facility> facilityList = listFieldFacility.getSelection();
        ArrayList<String> resultFacility = new ArrayList<String>();
        for (Facility facility : facilityList) {
            resultFacility.add(facility.getFacilityName());
        }
        return resultFacility;
    }

    public ArrayList<String> getInstrumentSelectedList() {
        List<Instrument> instrumentList = lstInstrument.getSelection();
        ArrayList<String> resultInstrument = new ArrayList<String>();
        for (Instrument ins : instrumentList) {
            resultInstrument.add(ins.getName());
        }
        return resultInstrument;
    }

    public ArrayList<String> getInvestigationTypeSelectedList() {
        List<InvestigationType> invList = lstInvestigationTypes.getSelection();
        ArrayList<String> resultInvType = new ArrayList<String>();
        for (InvestigationType inv : invList) {
            resultInvType.add(inv.getName());
        }
        return resultInvType;
    }

    public InvestigationSearchCallback getInvSearchCallback() {
        return invSearchCallback;
    }

    public void setInvSearchCallback(InvestigationSearchCallback invSearchCallback) {
        this.invSearchCallback = invSearchCallback;
    }

    private TAdvancedSearchDetails createAdvancedSearchDetails() {
        TAdvancedSearchDetails result = new TAdvancedSearchDetails();
        result.setPropostaltitle(txtFldProposalTitle.getValue());
        result.setProposalAbstract(txtFldProposalAbstract.getValue());
        result.setSample(txtFldSampleName.getValue());
        if (txtFldInvestigatorName.getValue() != null && txtFldInvestigatorName.getValue().compareTo("") != 0)
            result.getInvestigatorNameList().add(txtFldInvestigatorName.getValue());
        result.setDatafileName(txtFldDataFileName.getValue());
        result.setStartDate(dateFieldStart.getValue());
        result.setEndDate(dateFieldEnd.getValue());
        result.setRbNumberStart(txtFldRunNo.getValue());
        result.setRbNumberEnd(txtFldRunNo.getValue());
        result.setFacilityList(getFacilitySelectedList());
        result.setInvestigationTypeList(getInvestigationTypeSelectedList());
        result.setInstrumentList(getInstrumentSelectedList());
        return result;
    }

    void searchAdvanced() {
        TAdvancedSearchDetails searchInputs = createAdvancedSearchDetails();
        if (invSearchCallback == null)
            return;
        invSearchCallback.searchForInvestigation(searchInputs);
    }

    /**
     * Update List Widgets. instrument list, investigation types.
     */
    public void updateListWidgets() {
        ArrayList<String> facilitySelectedList = getFacilitySelectedList();
        // Remove all the investigation types and instruments from widgets
        lstInstrument.getStore().removeAll();
        lstInvestigationTypes.getStore().removeAll();
        // Add new list
        for (String facilityName : facilitySelectedList) {
            lstInstrument.getStore().add(instrumentList.get(facilityName));
            if (investigationTypeList.get(facilityName) == null) {
                continue;
            }
            for (InvestigationType invType : investigationTypeList.get(facilityName)) {
                boolean invTypeExists = false;
                for (InvestigationType storeInvType : lstInvestigationTypes.getStore().getModels()) {
                    if (invType.getName().compareToIgnoreCase(storeInvType.getName()) == 0) {
                        lstInvestigationTypes.getStore().remove(storeInvType);
                        storeInvType.addServer(facilityName);
                        lstInvestigationTypes.getStore().add(storeInvType);
                        invTypeExists = true;
                        break;
                    }
                }
                if (!invTypeExists) {
                    lstInvestigationTypes.getStore().add(new InvestigationType(facilityName, invType.getName()));
                }
            }
        }
    }

    /**
     * Reset the widget values in the panel.
     */
    private void resetWidgetValues() {
        listFieldFacility.clear();
        lstInstrument.getListView().getSelectionModel().deselectAll();
        lstInvestigationTypes.clear();
        txtFldProposalTitle.clear();
        txtFldProposalAbstract.clear();
        txtFldSampleName.clear();
        txtFldInvestigatorName.clear();
        txtFldDataFileName.clear();
        dateFieldStart.clear();
        dateFieldEnd.clear();
        txtFldRunNo.clear();
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
     * Setup a handler to react to AddInstrument events.
     */
    private void createAddInstrumentHandler() {
        // react to a new set of instruments being added
        AddInstrumentEvent.register(EventPipeLine.getEventBus(), new AddInstrumentEventHandler() {
            @Override
            public void addInstruments(AddInstrumentEvent event) {
                instrumentList.put(event.getFacilityName(), event.getInstruments());
                updateListWidgets();
            }
        });
    }

    /**
     * Setup a handler to react to AddInvestigation events.
     */
    private void createAddInvestigationHandler() {
        // react to a new set of investigations being added
        AddInvestigationEvent.register(EventPipeLine.getEventBus(), new AddInvestigationEventHandler() {
            @Override
            public void addInvestigations(AddInvestigationEvent event) {
                investigationTypeList.put(event.getFacilityName(), event.getInvestigations());
                updateListWidgets();
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
                instrumentList.remove(event.getFacilityName());
                investigationTypeList.remove(event.getFacilityName());
                Facility facility = listFieldFacility.getStore().findModel("name", event.getFacilityName());
                listFieldFacility.getStore().remove(facility);
                updateListWidgets();
            }
        });
    }

}
