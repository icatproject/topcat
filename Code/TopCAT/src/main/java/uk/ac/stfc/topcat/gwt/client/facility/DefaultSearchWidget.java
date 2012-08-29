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
package uk.ac.stfc.topcat.gwt.client.facility;

/**
 * Imports
 */
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.Instrument;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * This is a widget, a default search widget collects the user search info and
 * sends to EventPipeLine to handle the search operation.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class DefaultSearchWidget extends Composite {

    private EventPipeLine eventBus;
    private DateField startDate;
    private DateField endDate;
    private NumberField runNumberStart;
    private NumberField runNumberEnd;
    private ListField<Instrument> lstInstrument;
    private String facilityName;
    private Text errorMessage;

    public DefaultSearchWidget(EventPipeLine eventBusPipeLine) {
        this.eventBus = eventBusPipeLine;

        LayoutContainer topContainer = new LayoutContainer();
        LayoutContainer layoutContainer = new LayoutContainer();
        TableLayout tl_layoutContainer = new TableLayout(4);
        tl_layoutContainer.setCellSpacing(5);
        layoutContainer.setLayout(tl_layoutContainer);

        LabelField lblfldExperimentProposal = new LabelField("Experiment / Proposal Search");
        topContainer.add(lblfldExperimentProposal);

        LabelField lblfldStartDate = new LabelField("Start Date");
        layoutContainer.add(lblfldStartDate);

        startDate = new DateField();
        layoutContainer.add(startDate);
        startDate.setFieldLabel("New DateField");
        startDate.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));

        LabelField lblfldEndDate = new LabelField("End Date");
        layoutContainer.add(lblfldEndDate);
        lblfldEndDate.setWidth("85px");

        endDate = new DateField();
        layoutContainer.add(endDate);
        endDate.setFieldLabel("New DateField");
        endDate.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));

        LabelField lblfldRunNumberStart = new LabelField("Run Number Start");
        layoutContainer.add(lblfldRunNumberStart);

        runNumberStart = new NumberField();
        layoutContainer.add(runNumberStart);
        runNumberStart.setFieldLabel("New NumberField");

        LabelField lblfldRunNumberEnd = new LabelField("Run Number End");
        layoutContainer.add(lblfldRunNumberEnd);

        runNumberEnd = new NumberField();
        layoutContainer.add(runNumberEnd);
        runNumberEnd.setFieldLabel("New NumberField");

        LabelField lblfldInstrument = new LabelField("Instrument");
        layoutContainer.add(lblfldInstrument);

        lstInstrument = new ListField<Instrument>();
        layoutContainer.add(lstInstrument);
        lstInstrument.setFieldLabel("New ListField");
        lstInstrument.setDisplayField("name");
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());
        layoutContainer.add(new Text());

        Button btnSearch = new Button("Search");
        btnSearch.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                TAdvancedSearchDetails searchDetails = validateInput();
                if (searchDetails != null) {
                    eventBus.searchForInvestigation(searchDetails);
                }
            }
        });
        layoutContainer.add(btnSearch);

        Button btnReset = new Button("Reset");
        btnReset.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                reset();
            }
        });
        layoutContainer.add(btnReset);
        layoutContainer.add(new Text());

        topContainer.add(layoutContainer);
        topContainer.add(new Text());
        errorMessage = new Text();
        errorMessage.setText("");
        topContainer.add(errorMessage);
        topContainer.setHeight("275px");
        initComponent(topContainer);
        setBorders(true);
        setAutoHeight(true);
    }

    /**
     * Set the facility name.
     * 
     * @param facilityName
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
        ListStore<Instrument> instruments = eventBus.getFacilityInstruments(facilityName);
        instruments.sort("name", Style.SortDir.ASC);
        lstInstrument.setStore(instruments);
        createLogoutHandler();
    }

    private TAdvancedSearchDetails validateInput() {
        errorMessage.setText("");
        TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
        if (endDate.getValue() == null) {
            if (startDate.getValue() != null) {
                searchDetails.setEndDate(startDate.getValue());
            }
        } else {
            if (startDate.getValue() == null) {
                errorMessage.setText("Please enter a 'Start Date'");
                startDate.focus();
                return null;
            } else {
                if (startDate.getValue().compareTo(endDate.getValue()) > 0) {
                    errorMessage.setText("'End Date' must be equal or greater than 'Start Date'");
                    endDate.focus();
                    return null;
                }
            }
            searchDetails.setEndDate(endDate.getValue());
        }
        searchDetails.setStartDate(startDate.getValue());
        searchDetails.getFacilityList().add(facilityName);
        if (runNumberEnd.getValue() == null) {
            if (runNumberStart.getValue() != null) {
                searchDetails.setRbNumberEnd(runNumberStart.getValue().toString());
            }
        } else {
            if (runNumberStart.getValue() == null) {
                errorMessage.setText("Please enter a 'Run Number Start'");
                runNumberStart.focus();
                return null;
            } else {
                if (runNumberStart.getValue().intValue() > runNumberEnd.getValue().intValue()) {
                    errorMessage.setText("'Run Number End' must be equal or greater than 'Run Number Start'");
                    runNumberEnd.focus();
                    return null;
                }
            }
            searchDetails.setRbNumberEnd(runNumberEnd.getValue().toString());
        }
        if (runNumberStart.getValue() != null) {
            searchDetails.setRbNumberStart(runNumberStart.getValue().toString());
        }
        List<Instrument> selectedIns = lstInstrument.getSelection();
        for (Instrument ins : selectedIns) {
            searchDetails.getInstrumentList().add(ins.getName());
        }
        return searchDetails;
    }

    private void reset() {
        errorMessage.setText("");
        startDate.clear();
        endDate.clear();
        runNumberStart.clear();
        runNumberEnd.clear();
        lstInstrument.getListView().getSelectionModel().deselectAll();
    }

    /**
     * Setup a handler to react to Logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                reset();
                lstInstrument = new ListField<Instrument>();
            }
        });
    }
}
