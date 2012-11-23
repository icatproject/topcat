package uk.ac.stfc.topcat.gwt.client.widget;

import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;

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

    public InvestigationPanel() {
        eventBus = EventPipeLine.getInstance();

        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));
        mainContainer.setBorders(true);
        mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(true);
        mainPanel.setHeading("Investigation");
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

        ToolBar toolMenuBar = new ToolBar();
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
                        invDetailPanel.getInvestigationModel().getFacilityName(), Constants.INVESTIGATION,
                        invDetailPanel.getInvestigationModel().getInvestigationId());
            }
        });
        buttonBar.add(btnExport);
        toolMenuBar.add(buttonBar);
        toolMenuBar.setBorders(true);
        mainPanel.add(toolMenuBar);
        tabPanel = new TabPanel();
        tabPanel.setMinTabWidth(60);

        // Investigation Details Tab
        TabItem tabInvDetails = new TabItem("Investigation Details");
        tabInvDetails.setItemId("InvDetails");
        invDetailPanel = new InvestigationDetailPanel();
        invDetailPanel.setAutoWidth(true);
        invDetailPanel.setAutoHeight(true);
        tabInvDetails.add(invDetailPanel);
        tabPanel.add(tabInvDetails);
        tabInvDetails.setAutoHeight(true);
        tabInvDetails.setAutoWidth(true);

        // Associated Software Tab
        TabItem tabAssSoft = new TabItem("Associated Software");
        tabAssSoft.setItemId("AssociatedSoftware");
        associatedSoftwarePanel = new AssociatedSoftwarePanel();
        associatedSoftwarePanel.setAutoWidth(true);
        associatedSoftwarePanel.setAutoHeight(true);
        tabAssSoft.add(associatedSoftwarePanel);
        tabPanel.add(tabAssSoft);
        tabAssSoft.setAutoHeight(true);
        tabAssSoft.setAutoWidth(true);

        // Upload Dataset Tab
        TabItem tabUpload = new TabItem("Upload Dataset");
        tabUpload.setItemId("UploadDataset");
        uploadDatasetPanel = new UploadDatasetPanel();
        uploadDatasetPanel.setAutoWidth(true);
        uploadDatasetPanel.setAutoHeight(true);
        tabUpload.add(uploadDatasetPanel);
        tabPanel.add(tabUpload);
        tabUpload.setAutoHeight(true);
        tabUpload.setAutoWidth(true);

        mainPanel.add(tabPanel);
        initComponent(mainContainer);
        tabPanel.addListener(Events.Select, new Listener<TabPanelEvent>() {
            @Override
            public void handleEvent(TabPanelEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
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

    }

    /**
     * Set the investigation.
     * 
     * @param inv
     *            the investigation
     */
    protected void setInvestigation(TInvestigation inv) {
        mainPanel.setHeading("Investigation: " + inv.getTitle());
        invDetailPanel.setInvestigation(inv);
        associatedSoftwarePanel.setInstrument(inv.getInstrument());
    }

}