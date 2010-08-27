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
package uk.ac.stfc.topcat.gwt.client.facility;
/**
 * Imports
 */
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.Instrument;

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
 * This is a widget, Customized for Diamond Facility
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class DiamondSearchWidget extends Composite {

	EventPipeLine eventBus;
	private DateField startDate;
	private DateField endDate;
	private ListField<Instrument> beamLine;
	private String facilityName;
	@SuppressWarnings("unchecked")
	private TextField visitId;
	@SuppressWarnings("unchecked")
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
		
		visitId = new TextField();
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
			public void componentSelected(ButtonEvent ce) {
				TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
				searchDetails.setStartDate(startDate.getValue());
				searchDetails.setEndDate(endDate.getValue());
				searchDetails.getFacilityList().add(facilityName);
				List<Instrument> selectedIns = beamLine.getSelection();
				for(Instrument ins : selectedIns) {
					searchDetails.getInstrumentList().add(ins.getName());
				}		
				eventBus.searchForInvestigation(searchDetails);				
			}
		});
		layoutContainer.add(btnSearch);
		
		Button btnReset = new Button("Reset");
		btnReset.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				startDate.clear();
				endDate.clear();
				beamLine.getListView().getSelectionModel().deselectAll();
				visitId.clear();
			}
		});
		layoutContainer.add(btnReset);
		initComponent(layoutContainer);
		layoutContainer.setSize("389px", "274px");
		layoutContainer.setBorders(true);
		setBorders(true);
		setAutoHeight(true);
	}
	
	public void setFacilityName(String facilityName){
		this.facilityName=facilityName;
		ListStore<Instrument> instruments = eventBus.getFacilityInstruments(facilityName);
		beamLine.setStore(instruments);
	}	

}
