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

import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.event.WindowEvent;

/**
 * This is a floating window widget, It shows list of datafiles for a given investigation. 
 * 
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3   
 */
public class DatafileWindow extends Window {
	private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);	
	ParameterWindow datafileInfoWindow;
	CheckBoxSelectionModel<DatafileModel> datafileSelectModel;
	GroupingStore<DatafileModel> dfmStore;
	ArrayList<DatasetModel> inputDatasetModels;
	boolean historyVerified;
	public DatafileWindow() {
		addWindowListener(new WindowListener() {
			public void windowHide(WindowEvent we) {
//				EventPipeLine.getInstance().removeWindowHistory(getHistoryString());
				EventPipeLine.getInstance().getHistoryManager().updateHistory();
			}
		});
		dfmStore = new GroupingStore<DatafileModel>();	
		datafileSelectModel = new CheckBoxSelectionModel<DatafileModel>();		
		dfmStore.groupBy("datasetName");		
		datafileInfoWindow = new ParameterWindow();				
		setHeading("Datafile Window");
		setLayout(new RowLayout(Orientation.VERTICAL));
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add(datafileSelectModel.getColumn());
		
		ColumnConfig clmncnfgDatasetName = new ColumnConfig("datasetName", "Dataset Name", 150);
		configs.add(clmncnfgDatasetName);
		
		ColumnConfig clmncnfgFileName = new ColumnConfig("datafileName", "File Name", 150);
		configs.add(clmncnfgFileName);
		
		ColumnConfig clmncnfgFileSizeb = new ColumnConfig("datafileSize", "File Size (MBytes)", 150);
		configs.add(clmncnfgFileSizeb);
		
		ColumnConfig clmncnfgFormat = new ColumnConfig("datafileFormat", "Format", 150);
		configs.add(clmncnfgFormat);
		
		ColumnConfig clmncnfgFormatVersion = new ColumnConfig("datafileFormatVersion", "Format Version", 150);
		configs.add(clmncnfgFormatVersion);
		
		ColumnConfig clmncnfgFormatType = new ColumnConfig("datafileFormatType", "Format Type", 150);
		configs.add(clmncnfgFormatType);
		
		ColumnConfig clmncnfgCreateTime = new ColumnConfig("datafileCreateTime", "Create Time", 150);
		clmncnfgCreateTime.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
		configs.add(clmncnfgCreateTime);
		
		final ColumnModel cm = new ColumnModel(configs);
		GroupingView view = new GroupingView();    
	    view.setShowGroupedColumn(false);		
		view.setForceFit(true);    
		view.setGroupRenderer(new GridGroupRenderer() {    
			@Override
			public String render(GroupColumnData data) {    
				String f = cm.getColumnById(data.field).getHeader();    
				String l = data.models.size() == 1 ? "Item" : "Items";    
				return f + ": " + data.group + " (" + data.models.size() + " " + l + ")";    
			}  
		});		
		Grid<DatafileModel> grid = new Grid<DatafileModel>(dfmStore, cm);
		grid.setHeight("398px");
		grid.setView(view);
		grid.setBorders(true);
		grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<DatafileModel>>() {
			public void handleEvent(GridEvent<DatafileModel> e) {
				DatafileModel datafile = (DatafileModel)e.getModel();
				EventPipeLine.getInstance().showParameterWindowWithHistory(datafile.getFacilityName(), datafile.getId(), datafile.getName());
			}
		});		
		grid.addPlugin(datafileSelectModel);
		grid.setSelectionModel(datafileSelectModel);
	    ToolBar toolBar = new ToolBar();
	    Button  btnView = new Button(" Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload())); 
	    btnView.addSelectionListener(new SelectionListener<ButtonEvent>() {
	    	public void componentSelected(ButtonEvent ce) {
	    		EventPipeLine.getInstance().downloadDatafiles(datafileSelectModel.getSelectedItems());
	    	}
	    });
	    toolBar.add(btnView);
	    toolBar.add(new SeparatorToolItem());
	    setTopComponent(toolBar);	   
	    
	    setLayout(new FitLayout());
	    setSize(700,500);
	    add(grid);
	}
	
	/**
	 * Set the datasets input which are used to get the datafiles corresponding to each datasets and are displayed
	 * in the window
	 * @param datasetList
	 */
	public void setDatasets(ArrayList<DatasetModel> datasetList) {
		inputDatasetModels=datasetList;
		//This is the list of datasets selected to be viewed for datafiles.
		utilityService.getDatafilesInDatasets(datasetList, new AsyncCallback<ArrayList<DatafileModel>>() {
			@Override
			public void onSuccess(ArrayList<DatafileModel> result) {
				setDatafileList(result);				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				dfmStore.removeAll();
			}
		});
	}
	
	/**
	 * Set the datafile list to be displayed in the window
	 * @param datafileList
	 */
	public void setDatafileList(ArrayList<DatafileModel> datafileList){
		dfmStore.removeAll();
		NumberFormat format = NumberFormat.getDecimalFormat();
		//convert Bytes to MegaBytes
		for(DatafileModel dfm :datafileList){
			Float size = Float.parseFloat(dfm.getFileSize());
			size=size/1048576.0f;
			dfm.setFileSize(format.format(size.doubleValue())+" MB");
		}
		dfmStore.add(datafileList);
	}

	/**
	 * @return the history string of this window
	 */
	public String getHistoryString() {
		String history="";
		history+=HistoryManager.seperatorModel+HistoryManager.seperatorToken+"Model"+HistoryManager.seperatorKeyValues+"Dataset";
		int count=0;
		for(DatasetModel dataset:inputDatasetModels){
			history+=HistoryManager.seperatorToken+"SN-"+count+HistoryManager.seperatorKeyValues+dataset.getFacilityName();		
			history+=HistoryManager.seperatorToken+"DSId-"+count+HistoryManager.seperatorKeyValues+dataset.getId();
			history+=HistoryManager.seperatorToken+"DSName-"+count+HistoryManager.seperatorKeyValues+dataset.getName();
			count++;
		}
		return history;
	}

	/**
	 * This method compares the input list of datasetmodes, If they match with current windows
	 * datasetmodels then it returns true otherwise false
	 * @param dsModelList
	 * @return
	 */
	public boolean isSameModel(ArrayList<DatasetModel> dsModelList) {
		int index=0;
		for(DatasetModel dsModel:dsModelList){			
			if(dsModel.getFacilityName().compareTo(inputDatasetModels.get(index).getFacilityName())!=0 || dsModel.getId().compareTo(inputDatasetModels.get(index).getId())!=0 ) return false;
			index++;
		}
		return true;
	}
	
	/**
	 * @return returns the history verified status
	 */
	public boolean isHistoryVerified() {
		return historyVerified;
	}
	
	/**
	 * This method sets the history verified status
	 * @param historyVerified
	 */
	public void setHistoryVerified(boolean historyVerified) {
		this.historyVerified = historyVerified;
	}

}
