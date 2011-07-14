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
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import java.util.List;

/**
 * This widget displays the download requests.
 * 
 */
public class MyDownloadPanel extends Composite {

    private Grid<DownloadModel> grid;
    private ListStore<DownloadModel> downloadStore = new ListStore<DownloadModel>();
    private EventPipeLine eventBus;

    public MyDownloadPanel() {
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        verticalPanel.setBorders(true);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig clmncnfgServerName = new ColumnConfig("facilityName", "Facility Name", 150);
        configs.add(clmncnfgServerName);

        ColumnConfig clmncnfgInvestigationNumber = new ColumnConfig("id", "Reference", 150);
        configs.add(clmncnfgInvestigationNumber);

        ColumnConfig clmncnfgVisitId = new ColumnConfig("url", "URL", 250);
        configs.add(clmncnfgVisitId);

        grid = new Grid<DownloadModel>(downloadStore, new ColumnModel(configs));
        grid.setAutoExpandColumn("url");
        grid.setAutoExpandMin(200);
        grid.setMinColumnWidth(100);
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<DownloadModel>>() {
            public void handleEvent(GridEvent<DownloadModel> e) {
                DownloadModel download = (DownloadModel) e.getModel();
                eventBus.showDownloadWindowWithHistory(download.getUrl());
            }
        });
        grid.setSize("800px", "376px");
        verticalPanel.add(grid);
        grid.setBorders(true);

        verticalPanel.setAutoWidth(true);
        setMonitorWindowResize(true);

        initComponent(verticalPanel);
        show();
    }

    /**
     * Add details of a download.
     * 
     * @param facility
     * @param id
     * @param url
     */
    public void addDownload(String facility, Long id, String url) {
        // TODO nasty hack for Diamond
        if (facility.equalsIgnoreCase("DIAMOND")) {
            url = "https://srb.esc.rl.ac.uk/dataportal";
        }

        downloadStore.add(new DownloadModel(facility, id, url));
    }

    /**
     * Remove all downloads for the given facility.
     * 
     * @param facilityName
     */
    public void clearDownloadList(String facilityName) {
        List<DownloadModel> downloadList = downloadStore.getModels();

        for (Iterator<DownloadModel> it = downloadList.iterator(); it.hasNext();) {
            if (it.next().getFacilityName().equals(facilityName)) {
                it.remove();
            }
        }
        downloadStore.removeAll();
        downloadStore.add(downloadList);
    }

    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
    }
}
