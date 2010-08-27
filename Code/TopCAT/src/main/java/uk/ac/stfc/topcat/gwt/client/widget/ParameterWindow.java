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
package uk.ac.stfc.topcat.gwt.client.widget;
/**
 * Imports
 */
import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.model.ParameterModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
/**
 * This class implements the gxt floating window which shows the list of parameters.
 * 
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class ParameterWindow extends Window {
	private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);

	private ListStore<ParameterModel> parameterList;
	private boolean historyVerified;
	private String facilityName;
	private String datafileId;
	private String datafileName;
	public ParameterWindow() {
		//Listener called when the parameter window is closed.
		addWindowListener(new WindowListener() {
			public void windowHide(WindowEvent we) {
				//Update the history to notify the close of parameter window
				EventPipeLine.getInstance().getHistoryManager().updateHistory();
			}
		});		
		parameterList = new ListStore<ParameterModel>();
		setHeading("");
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		ToolBar toolBar = new ToolBar();
		
		Button btnExport = new Button("Export");
		btnExport.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				EventPipeLine.getInstance().downloadParametersData(facilityName,datafileId);
			}
		});
		toolBar.add(btnExport);
		
		SeparatorToolItem separatorToolItem = new SeparatorToolItem();
		toolBar.add(separatorToolItem);
		add(toolBar);
		
		ColumnConfig clmncnfgName = new ColumnConfig("name", "Name", 150);
		configs.add(clmncnfgName);
		
		ColumnConfig clmncnfgUnits = new ColumnConfig("units", "Units", 178);
		configs.add(clmncnfgUnits);
		
		ColumnConfig clmncnfgValue = new ColumnConfig("value", "Value", 150);
		configs.add(clmncnfgValue);
		
		Grid<ParameterModel> grid = new Grid<ParameterModel>(parameterList, new ColumnModel(configs));
		add(grid);
		grid.setSize("661px", "430px");
		grid.setAutoWidth(true);
		grid.setBorders(true);
		setSize("670px","430px");
	}

	/**
	 * @return the facility name to which the parameter window is displaying
	 */
	public String getFacilityName() {
		return facilityName;
	}

	/**
	 * @return the datafile id of the parameters that parameter window is displaying
	 */
	public String getDatafileId() {
		return datafileId;
	}

	/**
	 * @return the datafile name of the parameters that parameter window is displaying
	 */
	public String getDatafileName() {
		return datafileName;
	}

	/**
	 * This method sets the datafile name.
	 * @param datafileName
	 */
	public void setDatafileName(String datafileName) {
		this.datafileName = datafileName;
		setHeading("Datafile: "+datafileName);
	}

	/**
	 * This method sets the datafile information of the parameter window, this will call
	 * the AJAX method to get the parameters information from the server and displayed in
	 * this window.
	 * @param facilityName iCAT instance name
	 * @param datafileId   Datafile id
	 */
	public void setDatafileInfo(String facilityName,String datafileId) {
		this.facilityName=facilityName;
		this.datafileId=datafileId;
		utilityService.getDatafileParameters(facilityName, datafileId, new AsyncCallback<ArrayList<ParameterModel>>() {
			@Override
			public void onSuccess(ArrayList<ParameterModel> result) {
				setParameterList(result);
				if(result.size()==0){ //If there are no parameters then information message dialog is displayed 
					EventPipeLine.getInstance().showErrorDialog("No Parameters");
					hide();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				parameterList.removeAll();
				hide();
			}
		});
	}
	
	/**
	 * This method sets the parameters that will be displayed in the window.
	 * @param parameterList list of parameters
	 */
	public void setParameterList(ArrayList<ParameterModel> parameterList) {
		this.parameterList.removeAll();
		this.parameterList.add(parameterList);
	}
	
	/**
	 * This method sets the parameters that will be displayed in the window.
	 * @param parameterList list of parameters
	 */
	public void setParameterList(ListStore<ParameterModel> parameterList) {
		this.parameterList.removeAll();
		this.parameterList.add(parameterList.getModels());
	}
	
	/**
	 * this method returns all the parameters displayed in the window 
	 * @return
	 */
	public ListStore<ParameterModel> getParameterList() {
		return parameterList;
	}

	/**
	 * @return the history string corresponding to current window.
	 */
	public String getHistoryString() {
		String history="";
		history+=HistoryManager.seperatorModel+HistoryManager.seperatorToken+"Model"+HistoryManager.seperatorKeyValues+"Parameter";
		history+=HistoryManager.seperatorToken+"SN"+HistoryManager.seperatorKeyValues+facilityName;		
		history+=HistoryManager.seperatorToken+"DFId"+HistoryManager.seperatorKeyValues+datafileId;
		history+=HistoryManager.seperatorToken+"DFN"+HistoryManager.seperatorKeyValues+datafileName;
		return history;
	}

	/**
	 * This method compares the input information with the current window information (such as datafile id and server name).
	 * if the match then they return true otherwise false 
	 */
	public boolean isSameModel(String ServerName,String datafileId){
		if(facilityName.compareTo(ServerName)==0&&this.datafileId.compareTo(datafileId)==0)return true;
		return false;		
	}
	
	/**
	 * @return the historyVerified flag
	 */
	public boolean isHistoryVerified() {
		return historyVerified;
	}

	/**
	 * Sets the history verified flag
	 * @param historyVerified
	 */
	public void setHistoryVerified(boolean historyVerified) {
		this.historyVerified = historyVerified;
	}
}
