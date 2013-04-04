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

import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.NotSupportedException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.LoginService;
import uk.ac.stfc.topcat.gwt.client.LoginServiceAsync;
import uk.ac.stfc.topcat.gwt.client.UploadService;
import uk.ac.stfc.topcat.gwt.client.UploadServiceAsync;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.UploadAuthorisationEvent;
import uk.ac.stfc.topcat.gwt.client.model.DatafileFormatModel;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

public class UploadDatasetPanel extends Composite {
    private final UploadServiceAsync uploadService = GWT.create(UploadService.class);
    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
    private final LoginServiceAsync loginService = GWT.create(LoginService.class);
    private FormPanel datasetPanel;
    private String facility = "";
    private String investigationId;
    private ListStore<DatafileModel> uploadFileList = new ListStore<DatafileModel>();
    private List<DatafileFormatModel> datafileFormats = new ArrayList<DatafileFormatModel>();
    private LayoutContainer datafilePanel = new LayoutContainer();
    private Text errorMessage = new Text("");
    private Integer localId = 0;
    private String SOURCE;
    private String idsUrl = "";

    /**
     * Constructor
     */
    public UploadDatasetPanel(String source) {
        SOURCE = source;

        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        datafilePanel.setLayout(new FormLayout());

        // Data set panel
        datasetPanel = getDSPanel();
        mainContainer.add(datasetPanel);

        // Data file panel
        datafilePanel.add(getDFPanel());
        mainContainer.add(datafilePanel);

        // Buttons
        ButtonBar buttons = getButtonBar();
        mainContainer.add(buttons, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(20, 0, 10, 0)));

        // Message box
        mainContainer.add(errorMessage);
        errorMessage.hide();

        // Selected files grid
        Grid<DatafileModel> grid = getFileGrid();
        mainContainer.add(grid, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 0, 10, 0)));

        initComponent(mainContainer);
    }

    /**
     * Set the investigation to the current investigation.
     * 
     * @param investigation
     */
    protected void setInvestigation(TInvestigation inv) {
        clear();
        if (!facility.equals(inv.getServerName())) {
            facility = inv.getServerName();
            setDSTypes(facility);
            setDFFormats(facility);
            // TODO get URL from database
            idsUrl = "https://hostname.domain.example";
            setIdsUrl(idsUrl);
        }
        investigationId = inv.getInvestigationId();
    }

    /**
     * Create a form panel for creating a new dataset
     * 
     * @return a form panel
     */
    private FormPanel getDSPanel() {
        FormPanel datasetContainer = new FormPanel();
        datasetContainer.setHeading("New Data Set Parameters");
        datasetContainer.setWidth(370);
        datasetContainer.setLabelWidth(120);
        datasetContainer.setStyleAttribute("margin", "10px");

        // Data set name
        TextField<String> dsName = new TextField<String>();
        dsName.setFieldLabel("Data Set Name");
        dsName.setItemId("datasetName");
        dsName.setAllowBlank(false);
        datasetContainer.add(dsName);

        // Data set description
        TextField<String> dsDescription = new TextField<String>();
        dsDescription.setFieldLabel("Data Set Description");
        dsDescription.setItemId("datasetDescription");
        datasetContainer.add(dsDescription);

        // Data set type
        ComboBox<DatasetModel> comboBoxDSType = getDSTypeBox();
        datasetContainer.add(comboBoxDSType);

        // DOI selection box
        final CheckBox DOI = new CheckBox();
        DOI.setFieldLabel("Generate a DOI for the Data Set");
        DOI.setItemId("doi");
        DOI.setValue(false);
        datasetContainer.add(DOI);
        DOI.hide(); // TODO

        // DOI link
        FlexTable link = new FlexTable();
        link.setHTML(
                0,
                0,
                "<a href=\"http://www.doi.org/\">Digital Object Identifiers (DOI)</a> are issued by <a href=\"http://datacite.org/\">DataCite</a>");
        // TODO datasetContainer.add(link);

        // DOI message
        HorizontalPanel messagePanel = new HorizontalPanel();
        final LabelField lblfldDOIWarning = new LabelField("DOI generation will make the data set publicly available");
        messagePanel.add(lblfldDOIWarning);
        datasetContainer.add(messagePanel);
        lblfldDOIWarning.hide();

        // DOI selection box listener
        DOI.addListener(Events.Change, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                if (DOI.getValue()) {
                    lblfldDOIWarning.show();
                } else {
                    lblfldDOIWarning.hide();
                }
            }
        });
        return datasetContainer;
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
        return combo;
    }

    /**
     * Create a form panel for adding a file to the data set
     * 
     * @return a form panel
     */
    private FormPanel getDFPanel() {
        FormPanel form = new FormPanel();
        form.setItemId("datafileForm");
        form.setHeading("Add A File");
        form.setAction(idsUrl);
        form.setEncoding(Encoding.MULTIPART);
        form.setMethod(Method.POST);
        form.setWidth(370);
        form.setLabelWidth(120);

        // Data file file object
        FileUploadField file = new FileUploadField();
        file.setAllowBlank(false);
        file.setFieldLabel("File Name");
        file.setItemId("fileObject");
        file.setName("fileObject");
        form.add(file);

        // Data file description
        TextField<String> description = new TextField<String>();
        description.setFieldLabel("File Description");
        description.setItemId("description");
        description.setName("description");
        form.add(description);

        // Data file format box
        form.add(getDFFormatBox());

        // Data file name
        HiddenField<String> hiddenName = new HiddenField<String>();
        hiddenName.setItemId("name");
        hiddenName.setName("name");
        form.add(hiddenName);

        // Data file format id
        HiddenField<String> hiddenFormatId = new HiddenField<String>();
        hiddenFormatId.setItemId("datafileFormatId");
        hiddenFormatId.setName("datafileFormatId");
        form.add(hiddenFormatId);

        // Session id
        HiddenField<String> hiddenSessionId = new HiddenField<String>();
        hiddenSessionId.setItemId("sessionId");
        hiddenSessionId.setName("sessionId");
        form.add(hiddenSessionId);

        // Data set id
        HiddenField<String> hiddenDatasetId = new HiddenField<String>();
        hiddenDatasetId.setItemId("datasetId");
        hiddenDatasetId.setName("datasetId");
        form.add(hiddenDatasetId);
        return form;
    }

    /**
     * Get a combo box containing the list of possible data file formats
     * 
     * @return a combo box containing the list of possible data file formats
     */
    private ComboBox<DatafileFormatModel> getDFFormatBox() {
        ComboBox<DatafileFormatModel> combo = new ComboBox<DatafileFormatModel>();
        combo.setFieldLabel("File Format");
        combo.setDisplayField("datafileFormat");
        combo.setName("datafileFormat");
        combo.setItemId("datafileFormat");
        combo.setForceSelection(true);
        combo.setAllowBlank(false);
        combo.setTypeAhead(true);
        combo.setTriggerAction(TriggerAction.ALL);
        ListStore<DatafileFormatModel> store = new ListStore<DatafileFormatModel>();
        store.add(datafileFormats);
        combo.setStore(store);
        return combo;
    }

    /**
     * Get the button bar with add and submit buttons.
     * 
     * @return a button bar
     */
    private ButtonBar getButtonBar() {
        ButtonBar buttons = new ButtonBar();
        buttons.setAlignment(HorizontalAlignment.CENTER);
        buttons.add(getAddButton());
        buttons.add(getSubmitButton());
        buttons.add(getResetButton());
        return buttons;
    }

    /**
     * Get the Add File button.
     * 
     * @return an Add File button
     */
    private Button getAddButton() {
        Button btn = new Button("Add File");
        btn.setToolTip("Click to add file to the data set");
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                addFile();
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
        Button btn = new Button("reset");
        btn.setToolTip("Click to clear all fields");
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                clear();
            }
        });
        return btn;
    }

    /**
     * Get a grid to hold data about selected files.
     * 
     * @return a DatafileModel grid
     */
    private Grid<DatafileModel> getFileGrid() {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig clmncnfgFileName = new ColumnConfig("datafileName", "File", 150);
        configs.add(clmncnfgFileName);

        ColumnConfig clmncnfgDescription = new ColumnConfig("datafileDescription", "Description", 150);
        configs.add(clmncnfgDescription);

        ColumnConfig clmncnfgType = new ColumnConfig("datafileFormat", "Format", 150);
        configs.add(clmncnfgType);

        ColumnConfig clmncnfgButton = new ColumnConfig("", "", 70);
        clmncnfgButton.setRenderer(getButtonRenderer());
        configs.add(clmncnfgButton);

        Grid<DatafileModel> grid = new Grid<DatafileModel>(uploadFileList, new ColumnModel(configs));
        grid.setAutoExpandMin(200);
        grid.setMinColumnWidth(100);
        grid.setAutoHeight(true);
        return grid;
    }

    /**
     * Get a renderer that creates a remove button for the grid.
     * 
     * @return a remove button renderer
     */
    private GridCellRenderer<DatafileModel> getButtonRenderer() {
        GridCellRenderer<DatafileModel> buttonRenderer = new GridCellRenderer<DatafileModel>() {

            private boolean init;

            @Override
            public Object render(final DatafileModel model, String property, ColumnData config, final int rowIndex,
                    final int colIndex, ListStore<DatafileModel> store, Grid<DatafileModel> grid) {
                if (!init) {
                    init = true;
                    grid.addListener(Events.ColumnResize, new Listener<GridEvent<DatafileModel>>() {

                        @Override
                        public void handleEvent(GridEvent<DatafileModel> be) {
                            for (int i = 0; i < be.getGrid().getStore().getCount(); i++) {
                                if (be.getGrid().getView().getWidget(i, be.getColIndex()) != null
                                        && be.getGrid().getView().getWidget(i, be.getColIndex()) instanceof BoxComponent) {
                                    ((BoxComponent) be.getGrid().getView().getWidget(i, be.getColIndex())).setWidth(be
                                            .getWidth() - 10);
                                }
                            }
                        }
                    });
                }

                Button b = new Button((String) model.get(property), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        uploadFileList.remove(model);
                        Component component = null;
                        for (Component c : datafilePanel.getItems()) {
                            if (c.getItemId().equals(model.getId())) {
                                component = c;
                                break;
                            }
                        }
                        if (component != null) {
                            datafilePanel.remove(component);
                        }
                    }
                });
                b.setEnabled(true);
                b.setText("remove");
                b.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);
                b.setToolTip("Click to remove file from list");
                return b;
            }
        };
        return buttonRenderer;
    }

    /**
     * Add the file to the list.
     */
    @SuppressWarnings("unchecked")
    private void addFile() {
        errorMessage.hide();
        errorMessage.setText("");
        FormPanel currentForm = getForm();
        for (Field<?> f : currentForm.getFields()) {
            if (!f.isValid()) {
                errorMessage.setText("Please enter a value for " + f.getFieldLabel());
                errorMessage.show();
                f.focus();
                return;
            }
        }

        String name = ((FileUploadField) currentForm.getItemByItemId("fileObject")).getFileInput().getValue();
        String description = ((TextField<String>) currentForm.getItemByItemId("description")).getValue();
        String formatName = ((ComboBox<DatafileFormatModel>) currentForm.getItemByItemId("datafileFormat")).getValue()
                .getFormat();
        String datafileFormatId = ((ComboBox<DatafileFormatModel>) currentForm.getItemByItemId("datafileFormat"))
                .getValue().getFormatId();

        // add details to the grid
        uploadFileList.add(new DatafileModel(facility, "datasetId", "datasetName", localId.toString(), name,
                description, "fileSize", "doi", "", "formatId", formatName, "formatDescription", "formatVersion",
                "formatType", null, null));

        // Set the name and format id on the form
        ((HiddenField<String>) currentForm.getItemByItemId("name")).setValue(name);
        ((HiddenField<String>) currentForm.getItemByItemId("datafileFormatId")).setValue(datafileFormatId);

        // hide current form and attach a new one
        currentForm.setItemId(localId.toString());
        currentForm.hide();
        localId = localId + 1;

        FormPanel newForm = getDFPanel();
        datafilePanel.add(newForm);
        newForm.render(datafilePanel.getElement());
        // TODO need to get the FileUploadField and ComboBox to display properly
    }

    private FormPanel getForm() {
        return (FormPanel) datafilePanel.getItemByItemId("datafileForm");
    }

    /**
     * Get the Submit button
     * 
     * @return a Submit button
     */
    private Button getSubmitButton() {
        Button btn = new Button("Submit");
        btn.setToolTip("Click to create data set and upload files");
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                submit();
            }
        });
        return btn;
    }

    /**
     * Create a new data set and the upload the files.
     */
    private void submit() {
        errorMessage.hide();
        errorMessage.setText("");
        if (datafilePanel.getItemCount() < 2) {
            errorMessage.setText("Nothing to submit, please add one or more files");
            errorMessage.show();
            return;
        }
        for (Field<?> f : datasetPanel.getFields()) {
            if (!f.isValid()) {
                errorMessage.setText("Please enter a value for " + f.getFieldLabel());
                errorMessage.show();
                f.focus();
                return;
            }
        }

        @SuppressWarnings("unchecked")
        final String name = ((TextField<String>) datasetPanel.getItemByItemId("datasetName")).getValue();
        @SuppressWarnings("unchecked")
        String description = ((TextField<String>) datasetPanel.getItemByItemId("datasetDescription")).getValue();
        @SuppressWarnings("unchecked")
        String type = ((ComboBox<DatasetModel>) datasetPanel.getItemByItemId("datasetType")).getValue().getType();
        TDataset dataset = new TDataset(facility, investigationId, "", name, description, type, "");
        uploadService.createDataSet(dataset, new AsyncCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                uploadData(result, name);
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof SessionException) {
                    // session has probably expired, check all sessions to be
                    // safe
                    EventPipeLine.getInstance().checkStillLoggedIn();
                } else {
                    EventPipeLine.getInstance().showErrorDialog("Error creating data set. " + caught.getMessage());
                }
            }
        });
    }

    /**
     * Upload the files.
     */
    @SuppressWarnings("unchecked")
    private void uploadData(final Long datasetId, final String datasetName) {
        loginService.getSessionId(facility, new AsyncCallback<String>() {
            @Override
            public void onSuccess(String sessionId) {
                int fileCount = datafilePanel.getItemCount() - 1;
                for (Component form : datafilePanel.getItems()) {
                    if (form.getItemId().equals("datafileForm")) {
                        continue;
                    }
                    ((HiddenField<String>) ((FormPanel) form).getItemByItemId("datasetId")).setValue(String
                            .valueOf(datasetId));
                    ((HiddenField<String>) ((FormPanel) form).getItemByItemId("sessionId")).setValue(sessionId);
                    ((FormPanel) form).submit();
                    ((FormPanel) form).removeAll();
                }
                clear();
                if (fileCount == 1) {
                    MessageBox.info("Information", "Data set " + datasetName + " created and 1 file uploaded", null);
                } else {
                    MessageBox.info("Information", "Data set " + datasetName + " created and " + fileCount
                            + " files uploaded", null);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof SessionException) {
                    // session has probably expired, check all sessions to be
                    // safe
                    EventPipeLine.getInstance().checkStillLoggedIn();
                } else {
                    EventPipeLine.getInstance().showErrorDialog("Error getting session id. " + caught.getMessage());
                }
            }
        });
    }

    /**
     * Get the list of dataset types for the given facility from the server and
     * populate the combo box with the results.
     * 
     * @param facility
     */
    @SuppressWarnings("unchecked")
    private void setDSTypes(final String facility) {
        ((ComboBox<DatasetModel>) datasetPanel.getItemByItemId("datasetType")).getStore().removeAll();
        EventPipeLine.getInstance().showRetrievingData();
        utilityService.getDatasetTypes(facility, new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                EventPipeLine.getInstance().hideRetrievingData();
                if (result.size() > 0) {
                    for (String type : result) {
                        ((ComboBox<DatasetModel>) datasetPanel.getItemByItemId("datasetType")).getStore().add(
                                new DatasetModel(facility, "", "", "", type, ""));
                    }
                }
                EventPipeLine.getEventBus().fireEventFromSource(new UploadAuthorisationEvent(facility, true), SOURCE);
            }

            @Override
            public void onFailure(Throwable caught) {
                EventPipeLine.getInstance().hideRetrievingData();
                if (caught instanceof SessionException) {
                    EventPipeLine.getInstance().checkStillLoggedIn();
                } else if (caught instanceof NotSupportedException) {
                    EventPipeLine.getEventBus().fireEventFromSource(new UploadAuthorisationEvent(facility, false),
                            SOURCE);
                } else {
                    EventPipeLine.getInstance().showErrorDialog("Error retrieving data set types from " + facility);
                }
            }
        });
    }

    /**
     * Get the list of datafile formats for the given facility from the server
     * and populate the datafileFormats list with the results.
     * 
     * @param facility
     */
    @SuppressWarnings("unchecked")
    private void setDFFormats(final String facility) {
        datafileFormats.clear();
        ((ComboBox<DatafileFormatModel>) (getForm()).getItemByItemId("datafileFormat")).getStore().removeAll();

        EventPipeLine.getInstance().showRetrievingData();
        utilityService.getDatafileFormats(facility, new AsyncCallback<List<DatafileFormatModel>>() {
            @Override
            public void onSuccess(List<DatafileFormatModel> result) {
                EventPipeLine.getInstance().hideRetrievingData();
                if (result.size() > 0) {
                    datafileFormats.addAll(result);
                }
                ((ComboBox<DatafileFormatModel>) (getForm()).getItemByItemId("datafileFormat")).getStore().add(
                        datafileFormats);
            }

            @Override
            public void onFailure(Throwable caught) {
                EventPipeLine.getInstance().hideRetrievingData();
                if (caught instanceof SessionException) {
                    EventPipeLine.getInstance().checkStillLoggedIn();
                } else if (caught instanceof NotSupportedException) {
                    // skip, will be picked up by setDSTypes
                } else {
                    EventPipeLine.getInstance().showErrorDialog("Error retrieving data file formats from " + facility);
                }
            }
        });
    }

    /**
     * Set the action for the data file form to be the given url.
     * 
     * @param url
     *            the url of the ids
     */
    private void setIdsUrl(String url) {
        getForm().setAction(url);
    }

    /**
     * Clear out any non facility specific fields.
     */
    @SuppressWarnings("unchecked")
    private void clear() {
        errorMessage.hide();
        errorMessage.setText("");
        ((TextField<String>) datasetPanel.getItemByItemId("datasetName")).reset();
        ((TextField<String>) datasetPanel.getItemByItemId("datasetDescription")).reset();
        ((ComboBox<DatasetModel>) datasetPanel.getItemByItemId("datasetType")).reset();
        ((CheckBox) datasetPanel.getItemByItemId("doi")).reset();
        LayoutContainer form = getForm();
        ((FileUploadField) form.getItemByItemId("fileObject")).clear();
        ((TextField<String>) form.getItemByItemId("description")).reset();
        ((ComboBox<DatafileFormatModel>) form.getItemByItemId("datafileFormat")).reset();
        uploadFileList.removeAll();
        List<Component> components = new ArrayList<Component>();
        for (Component c : datafilePanel.getItems()) {
            if (!c.getItemId().equals("datafileForm")) {
                components.add(c);
            }
        }
        for (Component c : components) {
            datafilePanel.remove(c);
        }
    }

    /**
     * Clear out all fields.
     */
    @SuppressWarnings("unchecked")
    protected void reset() {
        clear();
        ((ComboBox<DatasetModel>) datasetPanel.getItemByItemId("datasetType")).getStore().removeAll();
        ((ComboBox<DatafileFormatModel>) (getForm()).getItemByItemId("datafileFormat")).getStore().removeAll();
        datafileFormats.clear();
        facility = "";
        idsUrl = "";
    }
}
