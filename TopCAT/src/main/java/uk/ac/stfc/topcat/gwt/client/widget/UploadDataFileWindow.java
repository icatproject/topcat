package uk.ac.stfc.topcat.gwt.client.widget;

import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.NotSupportedException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.LoginService;
import uk.ac.stfc.topcat.gwt.client.LoginServiceAsync;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddDatafileEvent;
import uk.ac.stfc.topcat.gwt.client.ids.IdsResponseCode;
import uk.ac.stfc.topcat.gwt.client.model.DatafileFormatModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
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
public class UploadDataFileWindow extends Window {
    private final UtilityServiceAsync utilityService = UtilityService.Util.getInstance();
    
    private FormPanel form = new FormPanel();
    String datasetId;
    String facilityName;
    String SOURCE;
    HiddenField<String> datafileFormatId;
    HiddenField<String> hiddenSessionId;
    HiddenField<String> hiddenDatasetId;    
    HiddenField<String> hiddenWindowNameTransport;
    HiddenField<String> hiddenAddPad;
    String target;
    Button btn;
    MessageBox progressBox; 
    boolean processing = false;
    ICATNode node;

    public UploadDataFileWindow(String id, final String facilityName,  String source, final BaseModelData node) {
        this.datasetId = id;
        this.facilityName = facilityName;
        SOURCE = source;
        
        
        // Listener called when the upload file window is closed.
        addWindowListener(new WindowListener() {
            @Override
            public void windowHide(WindowEvent we) {
                
            }
        });
        
        //setModal(true);
        setBlinkModal(true); 
        setHeadingText("Add File to Data Set");
        setLayout(new RowLayout(Orientation.VERTICAL));
        setSize(400, 200);
        
        form.setAction(getIdsUrl(facilityName));
        form.setEncoding(Encoding.MULTIPART);
        form.setMethod(Method.POST);
        
        form.setHeadingText("New File Parameters");
        form.setWidth(370);
        form.setLabelWidth(120);
        form.setStyleAttribute("margin", "10px");        

        // Data file description
        TextField<String> description = new TextField<String>();
        description.setFieldLabel("File Description");
        description.setItemId("description");
        description.setName("description");
        form.add(description);

        // Data file format box
        form.add(getDFFormatBox());
        
        // dataFileformatId id
        datafileFormatId = new HiddenField<String>();
        datafileFormatId.setItemId("datafileformatId");
        datafileFormatId.setName("datafileFormatId");
        form.add(datafileFormatId);        
        
        // Session id
        hiddenSessionId = new HiddenField<String>();
        hiddenSessionId.setItemId("sessionId");
        hiddenSessionId.setName("sessionId");
        form.add(hiddenSessionId);        
        
        hiddenDatasetId = new HiddenField<String>();
        hiddenDatasetId.setItemId("datasetId");
        hiddenDatasetId.setName("datasetId");
        form.add(hiddenDatasetId);
        
        hiddenWindowNameTransport = new HiddenField<String>();
        hiddenWindowNameTransport.setItemId("wrap");
        hiddenWindowNameTransport.setName("wrap");
        hiddenWindowNameTransport.setValue("true");
        form.add(hiddenWindowNameTransport);
        
        //field to padding for IE for error  response. See http://support.microsoft.com/kb/294807
        hiddenAddPad = new HiddenField<String>();
        hiddenAddPad.setItemId("padding");
        hiddenAddPad.setName("padding");
        if (GXT.isIE) {
            hiddenAddPad.setValue("true");
        }
        form.add(hiddenAddPad);
        
        // Data file file object
        FileUploadField file = new FileUploadField();
        file.setAllowBlank(false);
        file.setFieldLabel("File Name");
        file.setItemId("fileObject");
        file.setName("fileObject");
        form.add(file);
        
        //add submit/reset button bar
        form.add(getSubmitBar());
        
        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>(){
            @Override
            public void handleEvent(FormEvent be) {
                btn.disable();
                toBack();
                progressBox = MessageBox.wait("Progress",  "Uploading file, please wait...", "Uploading...");
            }
        });
        
        //add submit handler
        form.addListener(Events.Submit, new Listener<FormEvent>(){
            @Override
            public void handleEvent(FormEvent be) {
                progressBox.close();
                //close window
                closeWindow();
                
                if (be == null) {
                    EventPipeLine.getInstance().showErrorDialog("Retriving upload response from ids unsupported by your browser");
                } else {                    
                    String result = be.getResultHtml();
                    
                    if (result == null) {
                        EventPipeLine.getInstance().showErrorDialog("Error retriving upload response from ids");                                            
                    } else {
                        JSONObject jSONObject = null;
                        
                        try {                            
                            jSONObject = (JSONObject) JSONParser.parseStrict(be.getResultHtml());
                        
                            //check if response contain an id (for success)                            
                            JSONValue idValue =  jSONObject.get("id");
                            //check property exist
                            if (idValue != null) {
                                //success if id property has a number id
                                if (idValue.isNumber() != null) {
                                    EventPipeLine.getInstance().showMessageDialog("File successfully uploaded");
                                    EventPipeLine.getEventBus().fireEventFromSource(new AddDatafileEvent(facilityName, node), SOURCE);
                                } else {
                                    //shouldn't reach here as a success always returns an id but add error just in case                                    
                                    EventPipeLine.getInstance().showErrorDialog("File successfully uploaded but no file id in response");
                                }                                
                            } else {
                                //upload has failed here
                                
                                //get json code string
                                JSONValue responseCodeValue; 
                                JSONString responseCodeString = null;
                                responseCodeValue = jSONObject.get("code");
                                
                                if (responseCodeValue != null) {
                                    if (responseCodeValue.isString() != null) {
                                        responseCodeString = responseCodeValue.isString();
                                    }
                                }    
                                
                                JSONValue responseMessageValue; 
                                JSONString responseMessageString = null;
                                responseMessageValue = jSONObject.get("message");
                                
                                if (responseMessageValue != null) {
                                    if (responseMessageValue.isString() != null) {
                                        responseMessageString = responseMessageValue.isString();
                                    }
                                }                                
                                
                                if (responseCodeString != null && responseMessageValue != null) {                                
                                    if (responseCodeString.stringValue() == IdsResponseCode.BADREQUESTEXCEPTION) {
                                        EventPipeLine.getInstance().showErrorDialog("Bad request error: "+ responseMessageString.stringValue());
                                    } else if(responseCodeString.stringValue() == IdsResponseCode.INSUFFICIENTPRIVILEGESEXCEPTION) {
                                        EventPipeLine.getInstance().showErrorDialog("Insufficent privilege error: "+ responseMessageString.stringValue());
                                    } else if(responseCodeString.stringValue() == IdsResponseCode.INSUFFICIENTSTORAGEEXCEPTION) {
                                        EventPipeLine.getInstance().showErrorDialog("Insufficent storage error: "+ responseMessageString.stringValue());
                                    } else if(responseCodeString.stringValue() == IdsResponseCode.INTERNALEXCEPTION) {
                                        EventPipeLine.getInstance().showErrorDialog("IDS server error: "+ responseMessageString.stringValue());                                    
                                    } else {
                                        EventPipeLine.getInstance().showErrorDialog("Error: "+ responseMessageString.stringValue());
                                    }
                                
                                } else {                                    
                                    EventPipeLine.getInstance().showErrorDialog("Error occurred retriving error message");
                                }
                            }
                                              
                        } catch (Exception e) {
                            EventPipeLine.getInstance().showErrorDialog("Error occurred parsing response");
                        }
                    }
                }
            }
        });        
        
        add(form);
    }
    
    /**
     * Get a combo box containing the list of possible data file formats
     * 
     * @return a combo box containing the list of possible data file formats
     */
    private ComboBox<DatafileFormatModel> getDFFormatBox() {
        ComboBox<DatafileFormatModel> combo = new ComboBox<DatafileFormatModel>();
        
        combo.setStore(new ListStore<DatafileFormatModel>());
        combo.setFieldLabel("File Format");
        combo.setDisplayField("datafileFormat");
        combo.setName("dataFileFormats");
        combo.setItemId("dataFileFormats");
        combo.setForceSelection(true);
        combo.setAllowBlank(false);
        combo.setTypeAhead(true);
        combo.setTriggerAction(TriggerAction.ALL);
        
        setDFFormat(facilityName);
        
        return combo;
    }
    
    
    
    /**
     * Get the list of dataset types for the given facility from the server and
     * populate the combo box with the results.
     * 
     * @param facility
     */    
    private void setDFFormat(final String facility) {
        try {
            EventPipeLine.getInstance().showRetrievingData();
            utilityService.getDatafileFormats(facility, new AsyncCallback<List<DatafileFormatModel>>() {
                @SuppressWarnings("unchecked")
                @Override
                public void onSuccess(List<DatafileFormatModel> result) {                    
                    EventPipeLine.getInstance().hideRetrievingData();
                    if (result.size() > 0) {
                        for (DatafileFormatModel format : result) {                        
                            ((ComboBox<DatafileFormatModel>) form.getItemByItemId("dataFileFormats")).getStore().add(format);
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
                        EventPipeLine.getInstance().showErrorDialog("Error retrieving data file formats from " + facility);
                    }
                }
            });
        
        } catch (Exception e) {
            EventPipeLine.getInstance().showErrorDialog("Error retrieving data file formats from " + facility);
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
        btn = new Button("Submit");
        
        btn.setToolTip("Click to create a data set");
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void componentSelected(ButtonEvent ce) {
               if (form.isValid()) {
                   //get the select data format id from the combo box
                   String fileformatid = ((ComboBox<DatafileFormatModel>) form.getItemByItemId("dataFileFormats")).getValue().getFormatId();
                   //set value for datafileformatid hidden field 
                   datafileFormatId.setRawValue(fileformatid);
                   hiddenDatasetId.setRawValue(datasetId);
                   
                   //We need to session id. Get it via LoginServiceAsync
                   LoginServiceAsync loginService = LoginService.Util.getInstance();
                   loginService.getSessionId(facilityName, new AsyncCallback<String>() {

                        @Override
                        public void onFailure(Throwable caught) {                            
                            EventPipeLine.getInstance().showErrorDialog("Error, failed to retrieve session id");
                        }
    
                        @Override
                        public void onSuccess(String result) {
                            //set the session id
                            hiddenSessionId.setRawValue(result);
                            //hiddenSessionId.setRawValue("deff2f8c-a286-4d5a-9af3-6d185aff1159");
                            
                            //make sure window.name transport is used
                            hiddenWindowNameTransport.setRawValue("true");
                            //we want to disable the combo box so the value isn't posted in the form-data
                            form.getItemByItemId("dataFileFormats").disable();
                            
                            //submit the form                            
                            form.submit();
                        }
                   
                   });
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
        FileUploadField fileUploadField = (FileUploadField) form.getItemByItemId("fileObject");
    
        if (fileUploadField != null && fileUploadField.isVisible()) {
            ((FileUploadField) form.getItemByItemId("fileObject")).reset();
        }
        
        ((TextField<String>) form.getItemByItemId("description")).reset();
        ((ComboBox<DatasetModel>) form.getItemByItemId("dataFileFormats")).reset();
    }   
    
    
    private void closeWindow(){
        this.hide();
    }
    
    private String getIdsUrl(String facilityName) {
        TFacility facility = EventPipeLine.getInstance().getFacility(facilityName);
        
        String idsUrl = facility.getDownloadServiceUrl();
        //deal with ending / and add /ids/put to end of url
        if (idsUrl.endsWith("/")) {
            idsUrl = idsUrl + "/ids/put"; 
        } else {
            idsUrl = idsUrl + "/ids/put";
        }
        
        return idsUrl;
    } 

}
