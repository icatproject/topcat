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
package uk.ac.stfc.topcat.gwt.client;

/**
 * Imports
 */
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.widget.HeaderPanel;
import uk.ac.stfc.topcat.gwt.client.widget.MainPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This is entry point for the TopCAT Application, Initializes all the widgets
 * and start up the service.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class TOPCATOnline implements EntryPoint {

    List<String> facilityNames;
    private Panel rootPanel;
    private MainPanel mainPanel;
    private HeaderPanel headerPanel;
    private EventPipeLine eventPipeLine;

    public void onModuleLoad() {
        rootPanel = RootPanel.get("one");
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        rootPanel.add(verticalPanel);

        headerPanel = new HeaderPanel();
        verticalPanel.add(headerPanel);
        verticalPanel.setCellHorizontalAlignment(headerPanel, HasHorizontalAlignment.ALIGN_CENTER);
        headerPanel.setAutoHeight(true);
        headerPanel.setAutoWidth(true);

        mainPanel = new MainPanel();
        verticalPanel.add(mainPanel);
        verticalPanel.setCellHorizontalAlignment(mainPanel, HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.setAutoHeight(true);
        mainPanel.setWidth("1260px");

        // Initialize event pipeline
        eventPipeLine = EventPipeLine.getInstance();
        eventPipeLine.setLoginPanel(headerPanel.getLoginPanel());
        eventPipeLine.setMainWindow(this);
        // Initialise
        eventPipeLine.getLogoURL();
        eventPipeLine.loadFacilityNames();
        // Set Event pipeline
        mainPanel.getSearchPanel().setEventBus(eventPipeLine);
        mainPanel.getMyDataPanel().setEventBus(eventPipeLine);
        mainPanel.getMyDownloadPanel().setEventBus(eventPipeLine);
        resizeMainPanel(Window.getClientWidth());
        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                // TODO Auto-generated method stub
                resizeMainPanel(event.getWidth());
            }

        });
        // process url
        eventPipeLine.getHistoryManager().processHistory(History.getToken());
        eventPipeLine.initDownloadParameter();
    }

    public void resizeMainPanel(int width) {
        int newWidth = width;
        if (newWidth < 800)
            newWidth = 800;
        mainPanel.setWidth(newWidth - 5);
        mainPanel.getSearchPanel().setGridWidth(newWidth - 5);
        mainPanel.getMyDataPanel().setGridWidth(newWidth - 5);
    }

    public RootPanel getRootPanel() {
        return (RootPanel) rootPanel;
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public HeaderPanel getHeaderPanel() {
        return headerPanel;
    }
}
