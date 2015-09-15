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

import java.util.List;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.exception.InsufficientPrivilegesException;
import uk.ac.stfc.topcat.core.gwt.module.exception.NoSuchObjectException;
import uk.ac.stfc.topcat.core.gwt.module.exception.NotSupportedException;
import uk.ac.stfc.topcat.core.gwt.module.exception.ObjectAlreadyExistsException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.UploadService;
import uk.ac.stfc.topcat.gwt.client.UploadServiceAsync;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddDatasetEvent;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Imports
 */


/**
 * Window widget to add a new dataset
 * 
 * <p>
 * 
 * @author Wayne Chung
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class AddNewDatasetWindow extends Window {
    private final UtilityServiceAsync utilityService = UtilityService.Util.getInstance();
    private final UploadServiceAsync uploadService = UploadService.Util.getInstance();
    
    private FormPanel form = new FormPanel();
    private TopcatInvestigation investigation;
    private String SOURCE;
    private BaseModelData node;

    public AddNewDatasetWindow(TopcatInvestigation topcatInvestigation, String source, BaseModelData node) {
        investigation = topcatInvestigation;
        SOURCE = source;
        this.node = node;
        
        // Listener called when the datafile window is closed.
        addWindowListener(new WindowListener() {
            @Override
            public void windowHide(WindowEvent we) {
                
            }
        });
        
        setModal(true);
        setBlinkModal(true); 
        setHeadingText("Add Data Set to " + investigation.getInvestigationTitle());
        setLayout(new RowLayout(Orientation.VERTICAL));        
        setSize(400, 200);
        
        form.setHeadingText("New Data Set Parameters");
        form.setWidth(370);
        form.setLabelWidth(120);
        form.setStyleAttribute("margin", "10px");

        // Data set name
        TextField<String> dsName = new TextField<String>();
        dsName.setFieldLabel("Data Set Name");
        dsName.setItemId("datasetName");
        dsName.setAllowBlank(false);
        form.add(dsName);

        // Data set description
        TextField<String> dsDescription = new TextField<String>();
        dsDescription.setFieldLabel("Data Set Description");
        dsDescription.setItemId("datasetDescription");
        form.add(dsDescription);

        // Data set type
        ComboBox<DatasetModel> comboBoxDSType = getDSTypeBox();
        form.add(comboBoxDSType);
        
        form.add(getSubmitBar());
        
        add(form);
    }
    
    
    
    /**
     * Get a combo box containing the list of possible data set formats
     * 
     * @return a combo box containing the list of possible data set formats
     */
    private ComboBox<DatasetModel> getDSTypeBox() {        
        ComboBox<DatasetModel> combo = new ComboBox<DatasetModel>();
        
        combo.setStore(new ListStore<DatasetModel>());
        combo.setFieldLabel("Data Set Type");
        combo.setDisplayField("datasetType");
        combo.setItemId("datasetType");
        combo.setForceSelection(true);
        combo.setAllowBlank(false);
        combo.setTypeAhead(true);
        combo.setTriggerAction(TriggerAction.ALL);
        combo.setEditable(false);
        combo.setForceSelection(true);        
        
        setDSTypes(investigation.getFacilityName());
        
        return combo;
    }
    
    
    /**
     * Get the list of dataset types for the given facility from the server and
     * populate the combo box with the results.
     * 
     * @param facility
     */    
    private void setDSTypes(final String facility) {
        try {
        
            EventPipeLine.getInstance().showRetrievingData();
            utilityService.getDatasetTypes(facility, new AsyncCallback<List<String>>() {
                @SuppressWarnings("unchecked")
                @Override
                public void onSuccess(List<String> result) {                    
                    EventPipeLine.getInstance().hideRetrievingData();
                    if (result.size() > 0) {
                        for (String type : result) {                        
                            ((ComboBox<DatasetModel>) form.getItemByItemId("datasetType")).getStore().add(
                                    new DatasetModel(facility, "", "", "", type, ""));
                        }
                    }
                    //EventPipeLine.getEventBus().fireEventFromSource(new UploadAuthorisationEvent(facility, true), SOURCE);
                }
    
                @Override
                public void onFailure(Throwable caught) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    if (caught instanceof SessionException) {
                        EventPipeLine.getInstance().checkStillLoggedIn();
                    } else if (caught instanceof NotSupportedException) {
                        //EventPipeLine.getEventBus().fireEventFromSource(new UploadAuthorisationEvent(facility, false), SOURCE);
                    } else {
                        EventPipeLine.getInstance().showErrorDialog("Error retrieving data set types from " + facility);
                    }
                }
            });
        
        } catch (Exception e) {
            EventPipeLine.getInstance().showErrorDialog("Error retrieving data set types from " + facility);
        }
    }
    
    
    /**
     * Get the button bar with add and submit buttons.
     * 
     * @return a button bar
     */
    private ButtonBar getSubmitBar() {
        ButtonBar buttons = new ButtonBar();
        
        buttons.setAlignment(HorizontalAlignment.CENTER);        
        buttons.add(getSubmitButton());
        buttons.add(getResetButton());
        
        return buttons;
    }

    /**
     * Get the Submit button
     * 
     * @return a Submit button
     */
    private Button getSubmitButton() {
        Button btn = new Button("Submit");
        
        btn.setToolTip("Click to create a data set");
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void componentSelected(ButtonEvent ce) {
               if (form.isValid()) {
                   TextField<String> datasetName = (TextField<String>) form.getItemByItemId("datasetName");
                   TextField<String> datasetDescription = (TextField<String>) form.getItemByItemId("datasetDescription");
                   ComboBox<DatasetModel> datasetType = (ComboBox<DatasetModel>) form.getItemByItemId("datasetType");
                   
                   TDataset dataset = new TDataset();
                   dataset.setInvestigationId(investigation.getInvestigationId());
                   dataset.setName(datasetName.getValue());
                   dataset.setDescription(datasetDescription.getValue());
                   dataset.setType(datasetType.getSelection().get(0).getType());
                   dataset.setFacilityName(investigation.getFacilityName());
                   
                   //EventPipeLine.getEventBus().fireEventFromSource(new AddDatasetEvent(investigation.getFacilityName(), node), SOURCE);
                   //closeWindow();
                   
                   
                   uploadService.createDataSet(dataset, new AsyncCallback<Long>() {
                       @Override
                       public void onSuccess(Long result) {                           
                           //need to fire event to update the investigation in the browse all data
                           EventPipeLine.getEventBus().fireEventFromSource(new AddDatasetEvent(investigation.getFacilityName(), node), SOURCE);
                           
                           //need to fire event to update any opened investigation windows
                           
                           closeWindow();
                       }
    
                       @Override
                       public void onFailure(Throwable caught) {
                           if (caught instanceof SessionException) {
                               closeWindow();
                               EventPipeLine.getInstance().checkStillLoggedIn();
                           } else if (caught instanceof NotSupportedException) {
                               //EventPipeLine.getEventBus().fireEventFromSource(new UploadAuthorisationEvent(facility, false), SOURCE);
                               closeWindow();
                           } else if (caught instanceof InsufficientPrivilegesException){
                               closeWindow();
                               EventPipeLine.getInstance().showErrorDialog("You do not have sufficient privileges to create a dataset in this investigation");    
                           } else if (caught instanceof ObjectAlreadyExistsException){
                               EventPipeLine.getInstance().showErrorDialog("The dataset name already exists in the investigation. Please choose a different name.");
                           } else if (caught instanceof NoSuchObjectException){
                               closeWindow();
                               EventPipeLine.getInstance().showErrorDialog("Investigation not found");
                           } else {
                               closeWindow();
                               EventPipeLine.getInstance().showErrorDialog("Failed to add data set");
                           }
                       }
                   });
               } else {
                   EventPipeLine.getInstance().showErrorDialog("Form invalid");
               }
               
            }
        });
        
        
        return btn;
    }

    /**
     * Get the Reset button.
     * 
     * @return an Reset button
     */
    private Button getResetButton() {        
        Button btn = new Button("Reset");        
        
        btn.setToolTip("Click to clear all fields");
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {                
                reset();
            }
        });
        
        return btn;
    }
    
    
    /**
     * reset form    
     */
    @SuppressWarnings("unchecked")
    private void reset() {
        ((TextField<String>) form.getItemByItemId("datasetName")).reset();
        ((TextField<String>) form.getItemByItemId("datasetDescription")).reset();
        ((ComboBox<DatasetModel>) form.getItemByItemId("datasetType")).reset();
        
    }
    
    
    private void closeWindow(){
        this.hide();
    }
    

}
