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
import java.util.HashMap;
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.CheckCascade;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This widget shows a tree to browse All data. The hierarchy is:
 * <dl>
 * <dd>-- Facility</dd>
 * <dd>&nbsp;&nbsp;-- Instrument</dd>
 * <dd>&nbsp;&nbsp;&nbsp;&nbsp;-- Investigation</dd>
 * <dd>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-- Dataset or Datafile</dd>
 * <dd>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-- Datafile</dd>
 * </dl>
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class BrowsePanel extends Composite {

    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
    private BaseTreeLoader<ICATNode> loader;
    TreePanel<ICATNode> tree;
    HashMap<String, ArrayList<ICATNode>> logfilesMap = new HashMap<String, ArrayList<ICATNode>>();

    public BrowsePanel() {

        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.setCollapsible(true);
        contentPanel.setLayout(new RowLayout(Orientation.VERTICAL));

        // ToolBar with ButtonBar with download button
        ToolBar toolBar = new ToolBar();
        ButtonBar buttonBar = new ButtonBar();
        DownloadButton btnDownload = new DownloadButton();
        btnDownload.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                download(((DownloadButtonEvent) ce).getDownloadName());
            }
        });
        buttonBar.add(btnDownload);
        toolBar.add(buttonBar);
        contentPanel.setTopComponent(toolBar);

        // Add Treepanel
        // This is RPC proxy to get the information from the server using
        // GWT-RPC AJAX calls each time the user expands the tree to browse.
        RpcProxy<ArrayList<ICATNode>> proxy = new RpcProxy<ArrayList<ICATNode>>() {

            // Get the nodes from the server using GWT-RPC. For the datafiles
            // grouped under datafiles get it from the cache.
            @Override
            protected void load(Object loadConfig, final AsyncCallback<ArrayList<ICATNode>> callback) {
                // Retrieve the datafiles information from the cache instead of
                // going to the GWT-RPC for datafiles grouped under datafiles
                // (log files under RAW files)
                if (loadConfig != null && ((ICATNode) loadConfig).getNodeType() == ICATNodeType.DATAFILE) {
                    String key = ((ICATNode) loadConfig).getFacility() + ((ICATNode) loadConfig).getDatafileId();
                    callback.onSuccess(logfilesMap.get(key));
                    return;
                }
                utilityService.getAllICATNodeDatafiles((ICATNode) loadConfig,
                        new AsyncCallback<HashMap<String, ArrayList<ICATNode>>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                callback.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(HashMap<String, ArrayList<ICATNode>> result) {
                                ArrayList<ICATNode> rawFiles = result.get("");
                                // remove from the result list
                                result.remove("");
                                for (String key : result.keySet()) {
                                    logfilesMap.put(key, result.get(key));
                                }
                                callback.onSuccess(rawFiles);
                            }

                        });
            }
        };

        loader = new BaseTreeLoader<ICATNode>(proxy) {
            @Override
            public boolean hasChildren(ICATNode parent) {
                // treeGrid.getStore().getChildCount(parent) !=0
                return logfilesMap.get(parent.getFacility() + parent.getDatafileId()) != null
                        || parent.getNodeType() != ICATNodeType.DATAFILE
                        && parent.getNodeType() != ICATNodeType.UNKNOWN;
            }
        };

        TreeStore<ICATNode> store = new TreeStore<ICATNode>(loader);

        tree = new TreePanel<ICATNode>(store);

        // This handler calls the reloading of the tree if the children are
        // none. Useful when the session expires or user hasn't logged in.
        tree.addListener(Events.Expand, new Listener<TreePanelEvent<ICATNode>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                TreePanel<ICATNode>.TreeNode node = be.getNode();

                if (!node.isLeaf() && node.getItemCount() == 0)
                    loader.loadChildren(be.getItem());
            }
        });

        // This is to check the RAW Datafile checked and check all the children
        tree.addListener(Events.BeforeCheckChange, new Listener<TreePanelEvent<ICATNode>>() {
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                // If children of raw datafiles are not loaded then load them.
                ICATNode node = be.getItem();
                if ((node.getNodeType() == ICATNodeType.DATAFILE || node.getNodeType() == ICATNodeType.INVESTIGATION)
                        && loader.hasChildren(node) && tree.getStore().getChildCount(node) == 0) {
                    loader.loadChildren(node);
                }
                // Only allow selection of datafiles, datasets and
                // investigations
                if (node.getNodeType() != ICATNodeType.DATAFILE && node.getNodeType() != ICATNodeType.DATASET
                        && node.getNodeType() != ICATNodeType.INVESTIGATION) {
                    be.setCancelled(true);
                    return;
                }
            }
        });

        // On double click on datafile node show a parameter window
        tree.sinkEvents(Events.OnDoubleClick.getEventCode());
        tree.addListener(Events.OnDoubleClick, new Listener<TreePanelEvent<ICATNode>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (node.getModel().getNodeType() == ICATNodeType.DATAFILE) {
                    ICATNode icatnode = node.getModel();
                    EventPipeLine.getInstance().showParameterWindowWithHistory(icatnode.getFacility(),
                            icatnode.getDatafileId(), icatnode.getDatafileName());
                }
            }
        });

        tree.setCaching(true);
        tree.setDisplayProperty("name");
        tree.setCheckable(true);
        tree.setCheckStyle(CheckCascade.CHILDREN);
        tree.setAutoHeight(true);

        VerticalPanel bodyPanel = new VerticalPanel();
        bodyPanel.add(tree);
        bodyPanel.setLayoutOnChange(true);
        bodyPanel.setScrollMode(Scroll.AUTO);
        bodyPanel.setHeight("600px");
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(10));
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
        bodyPanel.setLayout(layout);
        contentPanel.add(bodyPanel);

        layoutContainer.add(contentPanel);
        initComponent(layoutContainer);
    }

    /**
     * Download selected datasets and datafiles.
     */
    private void download(String downloadName) {
        List<ICATNode> selectedItems = tree.getCheckedSelection();

        // Create map of selected datasets
        // map: key = facility name, value = list of dataset ids
        HashMap<String, ArrayList<Long>> dsMap = new HashMap<String, ArrayList<Long>>();
        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.DATASET) {
                ArrayList<Long> idList = dsMap.get(node.getFacility());
                if (idList == null) {
                    idList = new ArrayList<Long>();
                    dsMap.put(node.getFacility(), idList);
                }
                idList.add(new Long(node.getDatasetId()));
            }
        }

        // Create map of selected datafiles
        // map: key = facility name, value = list of datafile ids
        HashMap<String, ArrayList<Long>> dfMap = new HashMap<String, ArrayList<Long>>();
        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.DATAFILE) {
                ArrayList<Long> dsList = dsMap.get(node.getFacility());
                if (dsList != null) {
                    if (dsList.contains(new Long(tree.getStore().getParent(node).getDatasetId()))) {
                        // we have already selected the whole datset so ignore
                        // the file
                        continue;
                    }
                }
                ArrayList<Long> idList = dfMap.get(node.getFacility());
                if (idList == null) {
                    idList = new ArrayList<Long>();
                    dfMap.put(node.getFacility(), idList);
                }
                idList.add(new Long(node.getDatafileId()));
            }
        }

        // Calculate how many batches the data will need to be split into.
        // Different batches are required for each dataset and for each
        // facility. Datafiles from the same facility can be grouped together.
        int requiredBatches = 0;
        for (String facility : dsMap.keySet()) {
            requiredBatches = requiredBatches + dsMap.get(facility).size();
        }
        requiredBatches = requiredBatches + dfMap.size();

        int batchCount = 0;
        // get a download frame for each data set
        for (String facility : dsMap.keySet()) {
            List<Long> idList = dsMap.get(facility);
            for (Long id : idList) {
                batchCount = batchCount + 1;
                if (requiredBatches == 1) {
                    EventPipeLine.getInstance().downloadDatasets(facility, id, downloadName);
                } else {
                    if (batchCount < 10) {
                        EventPipeLine.getInstance().downloadDatasets(facility, id, downloadName + "-0" + batchCount);
                    } else {
                        EventPipeLine.getInstance().downloadDatasets(facility, id, downloadName + "-" + batchCount);
                    }
                }
            }
        }

        // get a download frame for each batch of data files, one batch per
        // facility
        for (String facility : dfMap.keySet()) {
            batchCount = batchCount + 1;
            List<Long> idList = dfMap.get(facility);
            if (requiredBatches == 1) {
                EventPipeLine.getInstance().downloadDatafiles(facility, idList, downloadName);
            } else {
                if (batchCount < 10) {
                    EventPipeLine.getInstance().downloadDatafiles(facility, idList, downloadName + "-0" + batchCount);
                } else {
                    EventPipeLine.getInstance().downloadDatafiles(facility, idList, downloadName + "-" + batchCount);
                }
            }
        }

        if (batchCount == 0) {
            EventPipeLine.getInstance().showMessageDialog("Nothing selected for download");
        } else if (batchCount == 1) {
            EventPipeLine.getInstance().showMessageDialog(
                    "Your data is being retrieved from tape and will automatically start downloading shortly "
                            + "as a single file. The status of your download can be seen from the ‘My Downloads’ tab.");
        } else {
            EventPipeLine.getInstance().showMessageDialog(
                    "Your data is being retrieved from tape and will automatically start downloading shortly " + "as "
                            + batchCount
                            + " files. The status of your download can be seen from the ‘My Downloads’ tab.");
        }
    }

    /**
     * This method sets the width of the tree.
     * 
     * @param width
     */
    public void setTreeWidth(int width) {
        tree.setWidth(width);
    }

}
