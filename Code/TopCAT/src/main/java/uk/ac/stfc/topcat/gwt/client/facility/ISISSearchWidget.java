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

import com.extjs.gxt.ui.client.Style.Orientation;
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
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
/**
 * This is custom search widget for ISIS used in plugin.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class ISISSearchWidget extends Composite {

	protected EventPipeLine eventBus;
	private ListField<Instrument> lstInstrumentDatafile;
	String facilityName;
	private ListField<Instrument> lstInstrument;
	private NumberField datafileStartRunNumber;
	private NumberField datafileEndRunNumber;
	public ISISSearchWidget(EventPipeLine eventBusInput) {
		LayoutContainer topContainer = new LayoutContainer();
		
		LayoutContainer investigationContainer = new LayoutContainer();
		TableLayout tl_investigationContainer = new TableLayout(4);
		tl_investigationContainer.setCellSpacing(3);
		investigationContainer.setLayout(tl_investigationContainer);
		
		LabelField lblfldExperimentProposal = new LabelField("Experiment / Proposal Search");
		investigationContainer.add(lblfldExperimentProposal);
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		
		LabelField lblfldStartDate = new LabelField("Start Date");
		investigationContainer.add(lblfldStartDate);
		
		final DateField startDate = new DateField();
		startDate.setName("startDate");
		investigationContainer.add(startDate);
		startDate.setFieldLabel("New DateField");
		startDate.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));
		
		LabelField lblfldEndDate = new LabelField("End Date");
		investigationContainer.add(lblfldEndDate);
		
		final DateField endDate = new DateField();
		investigationContainer.add(endDate);
		endDate.setFieldLabel("New DateField");
		endDate.getPropertyEditor().setFormat(DateTimeFormat.getFormat("dd/MM/yyyy"));
		
		LabelField lblfldRunNumberStart = new LabelField("Run Number Start");
		investigationContainer.add(lblfldRunNumberStart);
		
		final NumberField runNumberStart = new NumberField();
		investigationContainer.add(runNumberStart);
		runNumberStart.setFieldLabel("New NumberField");
		
		LabelField lblfldRunNumberEnd = new LabelField("Run Number End");
		investigationContainer.add(lblfldRunNumberEnd);
		
		final NumberField runNumberEnd = new NumberField();
		investigationContainer.add(runNumberEnd);
		runNumberEnd.setFieldLabel("New NumberField");
		
		LabelField lblfldInstrument = new LabelField("Instrument");
		investigationContainer.add(lblfldInstrument);
		
		lstInstrument = new ListField<Instrument>();
		investigationContainer.add(lstInstrument);
		lstInstrument.setFieldLabel("New ListField");
		lstInstrument.setDisplayField("name");
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		investigationContainer.add(new Text());
		
		Button btnSearch = new Button("Search");
		btnSearch.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				//Create TAdvancedSearchDetails and call the eventbus
				TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
				searchDetails.setStartDate(startDate.getValue());
				searchDetails.setEndDate(endDate.getValue());
				searchDetails.getFacilityList().add(facilityName);
				if(runNumberStart.getValue()!=null)
					searchDetails.setRbNumberStart(runNumberStart.getValue().toString());
				if(runNumberEnd.getValue()!=null)
					searchDetails.setRbNumberEnd(runNumberEnd.getValue().toString());
				List<Instrument> selectedIns = lstInstrument.getSelection();
				for(Instrument ins : selectedIns) {
					searchDetails.getInstrumentList().add(ins.getName());
				}
				eventBus.searchForInvestigation(searchDetails);
			}
		});
		investigationContainer.add(btnSearch);
		
		Button btnReset = new Button("Reset");
		btnReset.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				startDate.clear();
				endDate.clear();
				runNumberStart.clear();
				runNumberEnd.clear();
				lstInstrument.getListView().getSelectionModel().deselectAll();
			}
		});
		investigationContainer.add(btnReset);
		investigationContainer.add(new Text());
		topContainer.setLayout(new FillLayout(Orientation.VERTICAL));
		
		topContainer.add(investigationContainer);
		LayoutContainer datafileContainer = new LayoutContainer();
		TableLayout tl_datafileContainer = new TableLayout(4);
		tl_datafileContainer.setCellSpacing(3);
		datafileContainer.setLayout(tl_datafileContainer);
		
		LabelField lblfldDatafileSearch = new LabelField("Datafile Search");
		datafileContainer.add(lblfldDatafileSearch);
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
				datafileContainer.add(new Text());
		
				
				LabelField lblfldRunNumberStart_1 = new LabelField("Run Number Start");
				datafileContainer.add(lblfldRunNumberStart_1);
		
		datafileStartRunNumber = new NumberField();
		datafileContainer.add(datafileStartRunNumber);
		LabelField lblfldRunNumberEnd_1 = new LabelField("Run Number End");
		datafileContainer.add(lblfldRunNumberEnd_1);
		datafileEndRunNumber = new NumberField();
		datafileContainer.add(datafileEndRunNumber);
		datafileEndRunNumber.setFieldLabel("New NumberField");
		datafileContainer.setBorders(true);
		
			LabelField lblfldInstrument_1 = new LabelField("Instrument");
			datafileContainer.add(lblfldInstrument_1);
		
		lstInstrumentDatafile = new ListField<Instrument>();
		datafileContainer.add(lstInstrumentDatafile);
		lstInstrumentDatafile.setFieldLabel("New ListField");
		lstInstrumentDatafile.setDisplayField("name");
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		datafileContainer.add(new Text());
		Button btnSearchDatafile = new Button("Search");
		btnSearchDatafile.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				//Create TAdvancedSearchDetails and call the eventbus
				TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
				searchDetails.getFacilityList().add(facilityName);
				if(datafileStartRunNumber.getValue()!=null)
					searchDetails.setRbNumberStart(datafileStartRunNumber.getValue().toString());
				if(datafileEndRunNumber.getValue()!=null)
					searchDetails.setRbNumberEnd(datafileEndRunNumber.getValue().toString());
				List<Instrument> selectedIns = lstInstrumentDatafile.getSelection();
				for(Instrument ins : selectedIns) {
					searchDetails.getInstrumentList().add(ins.getName());
				}				
				eventBus.searchForDatafiles(facilityName,searchDetails);
			}
		});
		datafileContainer.add(btnSearchDatafile);
		
		Button btnResetDatafile = new Button("Reset");
		btnResetDatafile.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				datafileStartRunNumber.clear();
				datafileEndRunNumber.clear();
				lstInstrumentDatafile.getListView().getSelectionModel().deselectAll();
			}
		});		
		datafileContainer.add(btnResetDatafile);
		datafileContainer.add(new Text());
		topContainer.add(datafileContainer);
		datafileContainer.setSize("625px", "250px");
		
		initComponent(topContainer);
		topContainer.setSize("661px", "543px");
		investigationContainer.setSize("625px", "250px");
		investigationContainer.setBorders(true);
		this.eventBus = eventBusInput;
	}

	public void setFacilityName(String facilityName){
		this.facilityName=facilityName;
		ListStore<Instrument> instruments = eventBus.getFacilityInstruments(facilityName);
		lstInstrument.setStore(instruments);
		lstInstrumentDatafile.setStore(instruments);			
	}
}
