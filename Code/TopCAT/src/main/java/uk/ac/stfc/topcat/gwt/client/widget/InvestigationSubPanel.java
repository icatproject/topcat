/**
 * 
 * Copyright (c) 2009-2012
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
import uk.ac.stfc.topcat.core.gwt.module.TInvestigator;
import uk.ac.stfc.topcat.core.gwt.module.TPublication;
import uk.ac.stfc.topcat.core.gwt.module.TShift;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Text;

import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class InvestigationSubPanel extends Composite {
    private TopcatInvestigation investigationModel = new TopcatInvestigation();
    private ContentPanel contentPanel;
    private VerticalPanel contentlPanelBody;
    private LabelField lblfldTitle;
    private LabelField lblfldFacility;
    private LabelField lblfldInvestigationNumber;
    private Text txtFacility;
    private Text txtTitle;
    private Text txtInvestigationNumber;
    private Text txtVisitId;
    private LabelField lblfldVisitId;
    private Text txtInvestigators;
    private Text txtProposal;
    private LabelField lblfldProposal;
    private Button btnShowDataSets;
    private LayoutContainer layoutContainer;
    private Text txtShifts;
    private Text txtPublications;
    private FlexTable shiftsTable;
    private FlexTable namesTable;
    private FlexTable publicationsTable;
    private EventPipeLine eventBus;
    private Text txtStartDate;
    private LabelField lblfldStartDate;
    private Text txtEndDate;
    private LabelField lblfldEndDate;
    private HorizontalPanel buttonPanel;
    private Text txtProperties;
    private FlexTable propertiesTable;
    private LayoutContainer shiftsContainer;
    private LayoutContainer publicationsContainer;
    private LayoutContainer investigatorsContainer;
    private LayoutContainer proposalContainer;
    private LayoutContainer propertiesContainer;

    /**
     * Constructor
     */
    public InvestigationSubPanel() {
        contentPanel = new ContentPanel();
        contentPanel.setTitleCollapse(true);
        contentPanel.setFrame(true);
        contentPanel.setExpanded(false);
        contentPanel.setHeading("Investigation Details");
        contentPanel.setCollapsible(true);
        contentPanel.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        contentPanel.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });

        contentlPanelBody = new VerticalPanel();

        layoutContainer = new LayoutContainer();
        TableLayout tl_layoutContainer = new TableLayout(2);
        tl_layoutContainer.setWidth("705");
        tl_layoutContainer.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_layoutContainer.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        tl_layoutContainer.setCellSpacing(5);
        layoutContainer.setLayout(tl_layoutContainer);

        buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        TableData td_buttonPanel = new TableData();
        td_buttonPanel.setVerticalAlign(VerticalAlignment.TOP);
        td_buttonPanel.setHorizontalAlign(HorizontalAlignment.LEFT);
        td_buttonPanel.setColspan(2);

        btnShowDataSets = new Button("Show Data Sets");
        btnShowDataSets
                .setToolTip("Click to open a window containing the list of data sets associated with this investigation");
        btnShowDataSets.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                eventBus.showDatasetWindowWithHistory(investigationModel.getFacilityName(),
                        investigationModel.getInvestigationId(), investigationModel.getInvestigationTitle());
            }
        });
        buttonPanel.add(btnShowDataSets);

        layoutContainer.add(buttonPanel, td_buttonPanel);
        buttonPanel.setHeight("50px");

        txtFacility = new Text("Facility: ");
        layoutContainer.add(txtFacility);

        lblfldFacility = new LabelField("Facility");
        layoutContainer.add(lblfldFacility);

        txtTitle = new Text("Title: ");
        layoutContainer.add(txtTitle);

        lblfldTitle = new LabelField("Title");
        layoutContainer.add(lblfldTitle);

        txtInvestigationNumber = new Text("Investigation No: ");
        TableData td_txtInvestigationNumber = new TableData();
        td_txtInvestigationNumber.setWidth("105");
        layoutContainer.add(txtInvestigationNumber, td_txtInvestigationNumber);

        lblfldInvestigationNumber = new LabelField("Investigation Number");
        layoutContainer.add(lblfldInvestigationNumber);

        txtVisitId = new Text("Visit Id: ");
        layoutContainer.add(txtVisitId);

        lblfldVisitId = new LabelField("Visit Id");
        layoutContainer.add(lblfldVisitId);

        txtStartDate = new Text("Start Date:");
        layoutContainer.add(txtStartDate);

        lblfldStartDate = new LabelField("Start Date");
        layoutContainer.add(lblfldStartDate);

        txtEndDate = new Text("End Date:");
        layoutContainer.add(txtEndDate);

        lblfldEndDate = new LabelField("End Date");
        layoutContainer.add(lblfldEndDate);

        contentlPanelBody.add(layoutContainer);
        layoutContainer.setSize("100%", "100%");

        // Shifts
        shiftsContainer = new LayoutContainer();
        TableLayout tl_shiftsContainer = new TableLayout(2);
        tl_shiftsContainer.setCellSpacing(5);
        tl_shiftsContainer.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_shiftsContainer.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        shiftsContainer.setLayout(tl_shiftsContainer);

        txtShifts = new Text("Shifts:");
        TableData td_txtShifts = new TableData();
        td_txtShifts.setWidth("105");
        shiftsContainer.add(txtShifts, td_txtShifts);

        shiftsTable = new FlexTable();
        shiftsTable.setBorderWidth(1);
        shiftsContainer.add(shiftsTable);

        shiftsTable.getColumnFormatter().setWidth(0, "120px");
        shiftsTable.getColumnFormatter().setWidth(1, "120px");
        contentlPanelBody.add(shiftsContainer);

        // Investigators
        investigatorsContainer = new LayoutContainer();
        TableLayout tl_investigatorsContainer = new TableLayout(2);
        tl_investigatorsContainer.setCellSpacing(5);
        tl_investigatorsContainer.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_investigatorsContainer.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        investigatorsContainer.setLayout(tl_investigatorsContainer);

        txtInvestigators = new Text("Investigators:");
        TableData td_txtInvestigators = new TableData();
        td_txtInvestigators.setWidth("105");
        investigatorsContainer.add(txtInvestigators, td_txtInvestigators);

        namesTable = new FlexTable();
        investigatorsContainer.add(namesTable);
        contentlPanelBody.add(investigatorsContainer);

        // Proposals
        proposalContainer = new LayoutContainer();
        proposalContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        txtProposal = new Text("Proposal");
        proposalContainer.add(txtProposal, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        lblfldProposal = new LabelField("Proposal");
        proposalContainer.add(lblfldProposal, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));

        contentlPanelBody.add(proposalContainer);

        // Publications
        publicationsContainer = new LayoutContainer();
        publicationsContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        txtPublications = new Text("Publications:");
        publicationsContainer.add(txtPublications, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        publicationsTable = new FlexTable();
        publicationsContainer
                .add(publicationsTable, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));
        contentlPanelBody.add(publicationsContainer);

        // Properties
        propertiesContainer = new LayoutContainer();
        propertiesContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        txtProperties = new Text("Properties:");
        propertiesContainer.add(txtProperties, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        propertiesTable = new FlexTable();
        propertiesTable.setBorderWidth(1);
        propertiesContainer.add(propertiesTable, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));
        contentlPanelBody.add(propertiesContainer);

        contentPanel.add(contentlPanelBody);

        initComponent(contentPanel);
        contentlPanelBody.setWidth("705");
        initDataBindings();
    }

    /**
     * Get the facility name.
     * 
     * @return the facility name
     */
    public String getFacilityName() {
        return lblfldFacility.getText();
    }

    /**
     * Erase all data and hide.
     */
    public void reset() {
        investigationModel = new TopcatInvestigation();
        shiftsContainer.hide();
        shiftsTable.removeAllRows();
        investigatorsContainer.hide();
        namesTable.removeAllRows();
        proposalContainer.hide();
        publicationsContainer.hide();
        publicationsTable.removeAllRows();
        propertiesContainer.hide();
        propertiesTable.removeAllRows();
    }

    /**
     * Set the investigation.
     * 
     * @param inv
     *            the investigation
     */
    public void setInvestigation(TInvestigation inv) {
        reset();
        contentPanel.expand();
        investigationModel = new TopcatInvestigation(inv.getServerName(), inv.getInvestigationId(),
                inv.getInvestigationName(), inv.getTitle(), inv.getVisitId(), inv.getStartDate(), inv.getEndDate(),
                inv.getProposal());

        // Shifts
        if (inv.getShifts().size() > 0) {
            Text startDate = new Text("Start Date");
            startDate.setWidth(125);
            shiftsTable.setWidget(0, 0, startDate);
            shiftsTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

            Text endDate = new Text("End Date");
            endDate.setWidth(125);
            shiftsTable.setWidget(0, 1, endDate);

            shiftsTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);

            shiftsTable.setWidget(0, 2, new Text("Comment"));
            shiftsTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
            int i = 1;
            for (TShift shift : inv.getShifts()) {
                shiftsTable.setWidget(
                        i,
                        0,
                        new Text(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT).format(
                                shift.getStartDate())));
                shiftsTable.getCellFormatter().setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER);

                shiftsTable.setWidget(
                        i,
                        1,
                        new Text(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT).format(
                                shift.getEndDate())));
                shiftsTable.getCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
                shiftsTable.setWidget(i, 2, new Text(shift.getComment()));
                shiftsTable.getCellFormatter().setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_CENTER);
            }
            shiftsContainer.show();
        }

        // Investigators
        if (inv.getInvestigators().size() > 0) {
            int i = 0;
            for (TInvestigator tinvestigator : inv.getInvestigators()) {
                HorizontalPanel investigatorPanel = new HorizontalPanel();
                investigatorPanel.addText(tinvestigator.getRole());
                investigatorPanel.add(new Text("&nbsp;-&nbsp;"));
                investigatorPanel.addText(tinvestigator.getFullName());
                namesTable.setWidget(i, 0, investigatorPanel);
                i++;
            }
            investigatorsContainer.show();
        }

        // Proposal
        if (!(inv.getProposal() == null) && !(inv.getProposal().isEmpty())) {
            proposalContainer.show();
        }

        // Publications
        if (inv.getPublications().size() > 0) {
            int i = 0;
            for (TPublication publication : inv.getPublications()) {
                if (publication.getUrl() == null || publication.getUrl().isEmpty()) {
                    publicationsTable.setText(i, 0, publication.getFullReference());
                } else {
                    publicationsTable.setHTML(i, 0,
                            "<a href=\"" + publication.getUrl() + "\">" + publication.getFullReference() + "</a>");
                }
                i++;
            }
            publicationsContainer.show();
        }

        // Parameters
        if (!(inv.getParamName() == null) && !(inv.getParamName().isEmpty())) {
            propertiesTable.setWidget(0, 0, new Text(inv.getParamName()));
            propertiesTable.setWidget(0, 1, new Text(inv.getParamValue()));
            propertiesContainer.show();
        }

        initDataBindings();
    }

    /**
     * Set the event bus.
     * 
     * @param eventBus
     *            the event bus
     */
    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
    }

    protected void initDataBindings() {
        FieldBinding fieldBinding = new FieldBinding(lblfldFacility, "serverName");
        fieldBinding.bind(investigationModel);
        //
        FieldBinding fieldBinding_1 = new FieldBinding(lblfldTitle, "investigationName");
        fieldBinding_1.bind(investigationModel);
        //
        FieldBinding fieldBinding_2 = new FieldBinding(lblfldVisitId, "visitId");
        fieldBinding_2.bind(investigationModel);
        //
        FieldBinding fieldBinding_3 = new FieldBinding(lblfldProposal, "proposal");
        fieldBinding_3.bind(investigationModel);
        //
        FieldBinding fieldBinding_4 = new FieldBinding(lblfldTitle, "title");
        fieldBinding_4.bind(investigationModel);
        //
        FieldBinding fieldBinding_5 = new FieldBinding(lblfldInvestigationNumber, "investigationName");
        fieldBinding_5.bind(investigationModel);
        //
        FieldBinding fieldBinding_6 = new FieldBinding(lblfldStartDate, "formatedStartDate");
        fieldBinding_6.bind(investigationModel);
        //
        FieldBinding fieldBinding_7 = new FieldBinding(lblfldEndDate, "formatedEndDate");
        fieldBinding_7.bind(investigationModel);
    }
}
