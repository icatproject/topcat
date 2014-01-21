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

import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.UploadAuthorisationEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.UploadAuthorisationEventHandler;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class InvestigationPanel extends Composite {
    private ContentPanel mainPanel;
    private TabPanel tabPanel;
    private InvestigationDetailPanel invDetailPanel;
    private AssociatedSoftwarePanel associatedSoftwarePanel;
    private UploadDatasetPanel uploadDatasetPanel;
    private EventPipeLine eventBus;
    private TabItem tabAssSoft;
    private TabItem tabUpload;
    private String source;

    public InvestigationPanel(String source) {
        this.source = source;
        eventBus = EventPipeLine.getInstance();

        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));
        mainContainer.setBorders(true);
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(true);
        mainPanel.setHeadingText("Investigation");
        mainPanel.setBodyBorder(false);
        mainPanel.setCollapsible(true);
        mainPanel.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        mainPanel.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        mainContainer.add(mainPanel, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(20, 10, 10, 10)));

        mainPanel.add(getToolBar());

        tabPanel = new TabPanel();
        tabPanel.setMinTabWidth(60);

        // Investigation Details Tab
        addInvDetailsTab();

        // Associated Software Tab
        // TODO
        // addAssociatedSoftwareTab();

        // Upload Dataset Tab
        // TODO
        // addUploadDatasetTab();

        mainPanel.add(tabPanel);
        initComponent(mainContainer);
        tabPanel.addListener(Events.Select, new Listener<TabPanelEvent>() {
            @Override
            public void handleEvent(TabPanelEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        createUploadAthorisationHandler();
    }

    /**
     * Get the facility name.
     *
     * @return the facility name
     */
    protected String getFacilityName() {
        return invDetailPanel.getFacilityName();
    }

    /**
     * Erase all data and hide.
     */
    protected void reset() {
        invDetailPanel.reset();
        // TODO
        // uploadDatasetPanel.reset();
    }

    /**
     * Set the investigation.
     *
     * @param inv
     *            the investigation
     */
    protected void setInvestigation(TInvestigation inv) {
        mainPanel.setHeadingText("Investigation: " + inv.getTitle());
        invDetailPanel.setInvestigation(inv);
        // TODO
        // uploadDatasetPanel.setInvestigation(inv);
    }

    private ToolBar getToolBar() {
        ToolBar toolBar = new ToolBar();
        ButtonBar buttonBar = new ButtonBar();
        Button btnShowDataSets = new Button("Show Data Sets");
        btnShowDataSets
                .setToolTip("Click to open a window containing the list of data sets associated with this investigation");
        btnShowDataSets.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                eventBus.showDatasetWindowWithHistory(invDetailPanel.getInvestigationModel().getFacilityName(),
                        invDetailPanel.getInvestigationModel().getInvestigationId(), invDetailPanel
                                .getInvestigationModel().getInvestigationTitle());
            }
        });
        buttonBar.add(btnShowDataSets);
        Button btnExport = new Button("Download Investigation Summary");
        btnExport.setToolTip("Click to download the data shown in this window");
        btnExport.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                EventPipeLine.getInstance().downloadParametersData(
                        invDetailPanel.getInvestigationModel().getFacilityNameFromInvestigation(), Constants.INVESTIGATION,
                        invDetailPanel.getInvestigationModel().getInvestigationId());
            }
        });
        buttonBar.add(btnExport);
        toolBar.add(buttonBar);
        toolBar.setBorders(true);
        return toolBar;
    }

    private void addInvDetailsTab() {
        TabItem tabInvDetails = new TabItem("Investigation Details");
        tabInvDetails.setItemId("InvDetails");
        invDetailPanel = new InvestigationDetailPanel();
        invDetailPanel.setAutoWidth(true);
        invDetailPanel.setAutoHeight(true);
        tabInvDetails.add(invDetailPanel);
        tabPanel.add(tabInvDetails);
        tabInvDetails.setAutoHeight(true);
        tabInvDetails.setAutoWidth(true);
    }

    private void addAssociatedSoftwareTab() {
        tabAssSoft = new TabItem("Associated Software");
        tabAssSoft.setItemId("AssociatedSoftware");
        associatedSoftwarePanel = new AssociatedSoftwarePanel();
        associatedSoftwarePanel.setAutoWidth(true);
        associatedSoftwarePanel.setAutoHeight(true);
        tabAssSoft.add(associatedSoftwarePanel);
        tabPanel.add(tabAssSoft);
        tabAssSoft.setAutoHeight(true);
        tabAssSoft.setAutoWidth(true);
    }

    private void addUploadDatasetTab() {
        tabUpload = new TabItem("Upload Dataset");
        tabUpload.setItemId("UploadDataset");
        uploadDatasetPanel = new UploadDatasetPanel(source);
        uploadDatasetPanel.setAutoWidth(true);
        uploadDatasetPanel.setAutoHeight(true);
        tabUpload.add(uploadDatasetPanel);
        tabPanel.add(tabUpload);
        tabUpload.setAutoHeight(true);
        tabUpload.setAutoWidth(true);
        tabUpload.disable();
    }

    // Upload Dataset Tab

    /**
     * Setup a handler to react to Logout events.
     */
    private void createUploadAthorisationHandler() {
        // TODO we need a means of determining if this user is authorised to
        // upload data
        UploadAuthorisationEvent.registerToSource(EventPipeLine.getEventBus(), source,
                new UploadAuthorisationEventHandler() {
                    @Override
                    public void update(UploadAuthorisationEvent event) {
                        if (event.isAuthorised()) {
                            tabUpload.enable();
                        } else {
                            tabUpload.disable();
                            tabPanel.setSelection(tabPanel.getItemByItemId("InvDetails"));
                        }
                    }
                });
    }

}