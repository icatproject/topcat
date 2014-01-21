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
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

/**
 * This is a widget, Customised for Diamond Facility
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class DiamondSearchWidget extends Composite {

    EventPipeLine eventBus;
    private DateField startDate;
    private DateField endDate;
    private ListField<Instrument> beamLine;
    private String facilityName;
    private TextField<String> visitId;

    public DiamondSearchWidget(EventPipeLine eventBusPipeLine) {
        this.eventBus = eventBusPipeLine;

        LayoutContainer layoutContainer = new LayoutContainer();
        TableLayout tl_layoutContainer = new TableLayout(2);
        tl_layoutContainer.setCellSpacing(3);
        layoutContainer.setLayout(tl_layoutContainer);

        LabelField lblfldInvestigationSearch = new LabelField("Investigation Search");
        layoutContainer.add(lblfldInvestigationSearch);
        layoutContainer.add(new Text());

        LabelField lblfldStartDate = new LabelField("Start Date");
        layoutContainer.add(lblfldStartDate);

        startDate = new DateField();
        layoutContainer.add(startDate);
        startDate.setFieldLabel("New DateField");

        LabelField lblfldEndDate = new LabelField("End Date");
        layoutContainer.add(lblfldEndDate);

        endDate = new DateField();
        layoutContainer.add(endDate);
        endDate.setFieldLabel("New DateField");

        LabelField lblfldVisitId = new LabelField("Visit Id");
        layoutContainer.add(lblfldVisitId);

        visitId = new TextField<String>();
        layoutContainer.add(visitId);
        visitId.setFieldLabel("New TextField");

        LabelField lblfldBeamline = new LabelField("BeamLine");
        layoutContainer.add(lblfldBeamline);

        beamLine = new ListField<Instrument>();
        layoutContainer.add(beamLine);
        beamLine.setFieldLabel("New ListField");
        beamLine.setDisplayField("name");

        layoutContainer.add(new Text());
        layoutContainer.add(new Text());

        Button btnSearch = new Button("Search");
        btnSearch.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {                
                if (isInputValid() == true) {
                    TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
                    searchDetails.setStartDate(startDate.getValue());
                    searchDetails.setEndDate(endDate.getValue());
                    searchDetails.setVisitId(visitId.getValue());
                    searchDetails.getFacilityList().add(facilityName);
                    List<Instrument> selectedIns = beamLine.getSelection();
                    for (Instrument ins : selectedIns) {
                        searchDetails.getInstrumentList().add(ins.getName());
                    }
                    
                    eventBus.searchForInvestigation(searchDetails);
                }
            }
        });
        layoutContainer.add(btnSearch);

        Button btnReset = new Button("Reset");
        btnReset.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                startDate.clear();
                endDate.clear();
                beamLine.getListView().getSelectionModel().deselectAll();
                visitId.clear();
            }
        });
        layoutContainer.add(btnReset);

        layoutContainer.add(new Text());

        initComponent(layoutContainer);
        layoutContainer.setHeight("275px");
        setBorders(true);
        setAutoHeight(true);
    }
    
    /**
     * validate form input
     * 
     * @return
     */
    private boolean isInputValid(){        
        if (!startDate.isValid() || !endDate.isValid()) {
            return false;
        }
        
        if (endDate.getValue() != null && startDate.getValue() == null) {
            startDate.markInvalid("Please enter a 'Start Date'");
            startDate.focus();
            return false;
        }
        
        if (startDate.getValue() != null && endDate.getValue() != null) {
            if (startDate.getValue().compareTo(endDate.getValue()) > 0) {
                endDate.markInvalid("'End Date' must be equal or greater than 'Start Date'");
                endDate.focus();
                
                return false;
            }
        }
        
        if (startDate.getValue() != null && endDate.getValue() == null) {
            endDate.setValue(startDate.getValue());
        }
        
            
        return true;
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
        beamLine.setStore(instruments);
        createLogoutHandler();
    }

    /**
     * Setup a handler to react to Logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.registerToSource(EventPipeLine.getEventBus(), facilityName, new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                ListStore<Instrument> instruments = new ListStore<Instrument>();
                beamLine.setStore(instruments);
                startDate.clear();
                endDate.clear();
                visitId.clear();
            }
        });
    }
}
