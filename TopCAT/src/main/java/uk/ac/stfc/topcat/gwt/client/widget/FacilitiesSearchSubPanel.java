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

import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddFacilityEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddFacilityEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.facility.FacilityPlugin;
import uk.ac.stfc.topcat.gwt.client.facility.FacilityPluginFactory;
import uk.ac.stfc.topcat.gwt.client.model.Facility;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * This is a widget, A sub panel in the SearchPanel. This provides custom
 * facility based search option. it has a combobox with the list of facilities
 * and upon selection of a facility in the list a custom search widget appears
 * in this panel. These search widgets are in
 * uk.ac.stfc.topcat.gwt.client.facility package.
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class FacilitiesSearchSubPanel extends Composite {
    private ComboBox<Facility> comboBoxFacility;
    private LayoutContainer facilityWidget;

    public FacilitiesSearchSubPanel() {

        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        layoutContainer.add(horizontalPanel);
        horizontalPanel.setSize("30%", "30px");
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        LabelField lblfldFacility = new LabelField("Facility");
        horizontalPanel.add(lblfldFacility);

        comboBoxFacility = new ComboBox<Facility>();
        comboBoxFacility.addSelectionChangedListener(new SelectionChangedListener<Facility>() {
            public void selectionChanged(SelectionChangedEvent<Facility> se) {
                facilityWidget.removeAll();
                FacilityPlugin plugin = FacilityPluginFactory.getInstance().getPlugin(
                        se.getSelectedItem().getFacilityPluginName());
                plugin.setFacilityName(se.getSelectedItem().getFacilityName());
                facilityWidget.add(plugin.getGUI());
                facilityWidget.layout(true);
            }
        });
        comboBoxFacility.setStore(new ListStore<Facility>());
        comboBoxFacility.setDisplayField("name");
        comboBoxFacility.setTypeAhead(true);
        comboBoxFacility.setTriggerAction(TriggerAction.ALL);
        horizontalPanel.add(comboBoxFacility);

        comboBoxFacility.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        comboBoxFacility.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });

        facilityWidget = new LayoutContainer();
        layoutContainer.add(facilityWidget);
        facilityWidget.setHeight("0px");
        facilityWidget.setLayout(new FitLayout());
        facilityWidget.setAutoHeight(true);
        initComponent(layoutContainer);
        setBorders(true);
        setAutoHeight(true);

        createAddFacilityHandler();
        createLogonHandler();
    }

    /**
     * Setup a handler to react to add facility events.
     */
    private void createAddFacilityHandler() {
        AddFacilityEvent.register(EventPipeLine.getEventBus(), new AddFacilityEventHandler() {
            @Override
            public void addFacilities(AddFacilityEvent event) {
                comboBoxFacility.getStore().add(event.getFacilities());
            }
        });
    }

    /**
     * Setup a handler to react to logon events.
     */
    private void createLogonHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                // if the facility search has not been set, set it
                if (facilityWidget.getItemCount() == 0) {
                    Facility facility = comboBoxFacility.getStore().findModel("name", event.getFacilityName());
                    if (facility != null) {
                        comboBoxFacility.setValue(facility);
                    }
                }
            }
        });
    }
}
