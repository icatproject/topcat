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

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddDatafileEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddDatasetEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationDetailsEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddDatafileEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddDatasetEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInvestigationDetailsEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.manager.DownloadManager;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;
import uk.ac.stfc.topcat.gwt.shared.DataSelectionType;
import uk.ac.stfc.topcat.gwt.shared.IdsFlag;
import uk.ac.stfc.topcat.gwt.shared.Utils;
import uk.ac.stfc.topcat.gwt.shared.model.TopcatDataSelection;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.CheckCascade;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

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
    private final UtilityServiceAsync utilityService = UtilityService.Util.getInstance();
    private BaseTreeLoader<ICATNode> loader;
    private TreePanel<ICATNode> tree;    private HashMap<String, ArrayList<ICATNode>> logfilesMap = new HashMap<String, ArrayList<ICATNode>>();
    private InvestigationPanel investigationPanel;
    private static final String SOURCE = "BrowsePanel";
    private ICATNode node;
    private EventPipeLine eventPipline;

    public BrowsePanel() {
        eventPipline = EventPipeLine.getInstance();

        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        // Add Treepanel
        RpcProxy<ArrayList<ICATNode>> proxy = getProxy();

        loader = getLoader(proxy);
        TreeStore<ICATNode> store = new TreeStore<ICATNode>(loader);
        tree = new TreePanel<ICATNode>(store);

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.setCollapsible(true);
        contentPanel.setLayout(new RowLayout(Orientation.VERTICAL));
        contentPanel.setTopComponent(getToolBar());

        // Add tree listeners and menu
        addExpandListener();
        addBeforeCheckChangeListener();
        addSingleClickListener();
        addDoubleClickListener();
        addChangeListener(); //TODO
        addContextMenu();

        tree.setView(new CheckboxTreePanelView<ICATNode>());
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

        mainContainer.add(contentPanel);

        // Investigation detail
        investigationPanel = new InvestigationPanel(SOURCE);
        investigationPanel.hide();
        mainContainer.add(investigationPanel);

        initComponent(mainContainer);
        setMonitorWindowResize(true);

        // Add listeners
        createAddInvestigationDetailsHandler();
        createLoginHandler();
        createLogoutHandler();
        CreateAddDatafileHandler();
        CreateAddDatasetHandler();

        loader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent event) {
                tree.setExpanded(node, true);
            }
        });
    }

    /**
     * Get a base tree loader that uses the given proxy.
     *
     * @param proxy
     * @return a BaseTreeLoader
     */
    private BaseTreeLoader<ICATNode> getLoader(RpcProxy<ArrayList<ICATNode>> proxy) {
        BaseTreeLoader<ICATNode> loader = new BaseTreeLoader<ICATNode>(proxy) {
            @Override
            public boolean hasChildren(ICATNode parent) {
                // treeGrid.getStore().getChildCount(parent) !=0
                return logfilesMap.get(parent.getFacility() + parent.getDatafileId()) != null
                        || parent.getNodeType() != ICATNodeType.DATAFILE
                        && parent.getNodeType() != ICATNodeType.UNKNOWN;
            }
        };
        return loader;
    }

    /**
     * Get a ToolBar with a ButtonBar with a download button.
     *
     * @return a ToolBar
     */
    private ToolBar getToolBar() {
        ToolBar toolBar = new ToolBar();

        DownloadButton btnDownload = new DownloadButton("Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload()), tree);
        btnDownload.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                String downloadName = ((DownloadButtonEvent) ce).getDownloadName();

                download(downloadName);
            }
        });
        toolBar.add(btnDownload);

        Button btnCheckSelectedSize = new Button("Check Selected Size", AbstractImagePrototype.create(Resource.ICONS.iconFileSize()));
        btnCheckSelectedSize.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<ICATNode> selectedItems = tree.getCheckedSelection();
                Map<String, TopcatDataSelection> dataSelectionMap = reduceToDataSelection(selectedItems);

                if (dataSelectionMap.size() > 0) {
                    for(Map.Entry<String, TopcatDataSelection> entry : dataSelectionMap.entrySet()) {
                        EventPipeLine.getInstance().showDataSelectionSizeDialog(entry.getKey(), entry.getValue(), DataSelectionType.MIXED);
                    }
                } else {
                    EventPipeLine.getInstance().showMessageDialog("Nothing selected to check.");
                }
            }
        });

        toolBar.add(btnCheckSelectedSize);

        toolBar.add(new SeparatorToolItem());

        Button clearAllButton = new Button("Clear All Selected", AbstractImagePrototype.create(Resource.ICONS.clearAll()));
        clearAllButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                tree.setCheckedSelection(new ArrayList<ICATNode>());
            }
        });

        toolBar.add(clearAllButton);

        Button collapseAllButton = new Button("Collapse All", AbstractImagePrototype.create(Resource.ICONS.collapseAll()));
        collapseAllButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
              tree.collapseAll();
            }
        });
        toolBar.add(collapseAllButton);

        return toolBar;
    }

    /**
     * Get the RPC proxy which will get the information from the server using
     * GWT-RPC AJAX calls each time the user expands the tree to browse.
     *
     * @return a RpcProxy
     */
    private RpcProxy<ArrayList<ICATNode>> getProxy() {
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
                                if (caught instanceof SessionException) {
                                    eventPipline.checkStillLoggedIn();
                                } else if (caught instanceof InternalException) {
                                    EventPipeLine
                                            .getInstance()
                                            .showErrorDialog(
                                                    "An internal error occured on the server, please see the server logs for more details.");
                                } else {
                                    eventPipline.showErrorDialog(
                                            "Error retrieving data from server @browse panel. " + caught.getMessage());
                                }
                                callback.onFailure(caught);
                                // TODO This is not working properly, the circle
                                // over the node does not disappear
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
        return proxy;
    }

    /**
     * Add an Expand Listener to the tree.
     */
    private void addExpandListener() {
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
    }

    /**
     * Add a BeforeCheckChange Listener to the tree.
     */
    private void addBeforeCheckChangeListener() {
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
            }
        });
    }

    /**
     * Add an OnClick Listener to the tree.
     */
    private void addSingleClickListener() {
        // On click show investigation details
        tree.addListener(Events.OnClick, new Listener<TreePanelEvent<ICATNode>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (node.getModel().getNodeType() == ICATNodeType.INVESTIGATION) {
                    ICATNode icatnode = node.getModel();
                    eventPipline.getInvestigationDetails(icatnode.getFacility(),
                            icatnode.getInvestigationId(), SOURCE);
                }
            }
        });
    }

    /**
     * Add an OnDoubleClick Listener to the tree.
     */
    private void addDoubleClickListener() {
        // On double click on datafile node show a parameter window
        tree.sinkEvents(Events.OnDoubleClick.getEventCode());
        tree.addListener(Events.OnDoubleClick, new Listener<TreePanelEvent<ICATNode>>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (node.getModel().getNodeType() == ICATNodeType.INVESTIGATION) {
                    ICATNode icatnode = node.getModel();
                    eventPipline.getInvestigationDetails(icatnode.getFacility(),
                            icatnode.getInvestigationId(), SOURCE);
                } else if (node.getModel().getNodeType() == ICATNodeType.DATASET) {
                    ICATNode icatnode = node.getModel();
                    eventPipline.showParameterWindowWithHistory(icatnode.getFacility(),
                            Constants.DATA_SET, icatnode.getDatasetId(), icatnode.getDatafileName());
                } else if (node.getModel().getNodeType() == ICATNodeType.DATAFILE) {
                    ICATNode icatnode = node.getModel();
                    eventPipline.showParameterWindowWithHistory(icatnode.getFacility(),
                            Constants.DATA_FILE, icatnode.getDatafileId(), icatnode.getDatafileName());
                }
            }
        });
    }


    private void addChangeListener() {
        tree.addListener(Events.CheckChange, new Listener<TreePanelEvent<ICATNode>>(){

            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                // TODO Auto-generated method stub
                //ICATNode node = be.getItem();
            }

        });
    }


    /**
     * Add a ContextMenu to the tree.
     */
    private void addContextMenu() {
        // Context Menu
        tree.setContextMenu(new Menu());
        tree.addListener(Events.ContextMenu, new Listener<TreePanelEvent<ICATNode>>() {
            @Override
            public void handleEvent(TreePanelEvent<ICATNode> be) {
                @SuppressWarnings("unchecked")
                TreePanel<ICATNode>.TreeNode node = be.getNode();
                if (node.getModel().getNodeType() == ICATNodeType.INVESTIGATION) {
                    tree.setContextMenu(getInvestigationMenu());
                } else if (node.getModel().getNodeType() == ICATNodeType.DATASET) {
                    tree.setContextMenu(getDatasetMenu());
                } else if (node.getModel().getNodeType() == ICATNodeType.DATAFILE) {
                    tree.setContextMenu(getDatafileMenu());
                } else {
                    be.setCancelled(true);
                }
            }
        });
    }

    /**
     * Get a context menu specific to an investigation node.
     *
     * @return a context menu
     */
    private Menu getInvestigationMenu() {
        Menu contextMenu = new Menu();
        contextMenu.setWidth(180);
        contextMenu.addStyleName("context-menu");
        MenuItem showInvestigation = new MenuItem();
        showInvestigation.setText("Show Investigation Details");
        showInvestigation.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconShowInvestigationDetails()));
        contextMenu.add(showInvestigation);
        showInvestigation.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                eventPipline.getInvestigationDetails(
                        tree.getSelectionModel().getSelectedItem().getFacility(),
                        tree.getSelectionModel().getSelectedItem().getInvestigationId(), SOURCE);
            }
        });

        MenuItem showSize = new MenuItem();
        showSize.setText("Show Investigation Size");
        showSize.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconFileSize()));

        contextMenu.add(showSize);
        showSize.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                if (tree.getSelectionModel().getSelectedItem() != null) {
                    TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                    topcatDataSelection.addInvestigation(new Long(tree.getSelectionModel().getSelectedItem().getInvestigationId()));

                    EventPipeLine.getInstance().showDataSelectionSizeDialog(
                            tree.getSelectionModel().getSelectedItem().getFacility(),
                            topcatDataSelection,
                            DataSelectionType.INVESTIGATION);
                }
            }
        });

        MenuItem showDataSet = new MenuItem();
        showDataSet.setText("Show Data Sets");
        showDataSet.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconOpenDataset()));
        contextMenu.add(showDataSet);
        showDataSet.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                eventPipline.showDatasetWindowWithHistory(
                        tree.getSelectionModel().getSelectedItem().getFacility(),
                        tree.getSelectionModel().getSelectedItem().getInvestigationId(),
                        tree.getSelectionModel().getSelectedItem().getInvestigationName());
            }
        });

        MenuItem downloadInvestigation = new MenuItem();
        downloadInvestigation.setText("Download this Investigation");
        downloadInvestigation.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconDownloadInvestigation()));
        contextMenu.add(downloadInvestigation);
        downloadInvestigation.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                topcatDataSelection.addInvestigation(new Long(tree.getSelectionModel().getSelectedItem().getInvestigationId()));

                DownloadManager.getInstance().downloadData(tree.getSelectionModel().getSelectedItem().getFacility(),
                        topcatDataSelection,
                        Utils.normaliseFileName(tree.getSelectionModel().getSelectedItem().getTitle()),
                        IdsFlag.ZIP_AND_COMPRESS);
            }
        });


        if(eventPipline.hasCreateDatasetSupport(tree.getSelectionModel().getSelectedItem().getFacility())) {
            MenuItem addDataset = new MenuItem();
            addDataset.setText("Add Data Set");
            addDataset.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconAddDataset()));
            contextMenu.add(addDataset);
            addDataset.addSelectionListener(new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent ce) {
                    eventPipline.showAddNewDatasetWindow(
                            tree.getSelectionModel().getSelectedItem().getFacility(),
                            tree.getSelectionModel().getSelectedItem().getInvestigationId(),
                            SOURCE,
                            tree.getSelectionModel().getSelectedItem());

                }
            });
        }

        return contextMenu;
    }

    /**
     * Get a context menu specific to an data set node.
     *
     * @return a context menu
     */
    private Menu getDatasetMenu() {
        Menu contextMenu = new Menu();
        contextMenu.setWidth(190);
        contextMenu.addStyleName("context-menu");
        MenuItem showDataset = new MenuItem();
        showDataset.setText("Show Data Set Parameters");
        showDataset.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconShowDatasetParameter()));
        contextMenu.add(showDataset);
        showDataset.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                eventPipline.showParameterWindowWithHistory(
                        tree.getSelectionModel().getSelectedItem().getFacility(), Constants.DATA_SET,
                        tree.getSelectionModel().getSelectedItem().getDatasetId(),
                        tree.getSelectionModel().getSelectedItem().getDatasetName());
            }
        });

        MenuItem showSize = new MenuItem();
        showSize.setText("Show Data Set Size");
        showSize.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconFileSize()));

        contextMenu.add(showSize);
        showSize.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                if (tree.getSelectionModel().getSelectedItem() != null) {
                    TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                    topcatDataSelection.addDataset(new Long(tree.getSelectionModel().getSelectedItem().getDatasetId()));

                    EventPipeLine.getInstance().showDataSelectionSizeDialog(
                            tree.getSelectionModel().getSelectedItem().getFacility(),
                            topcatDataSelection,
                            DataSelectionType.DATASET);
                }
            }
        });

        MenuItem downloadDataset = new MenuItem();
        downloadDataset.setText("Download this Data Set");
        downloadDataset.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconDownloadDataset()));
        contextMenu.add(downloadDataset);
        downloadDataset.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                topcatDataSelection.addDataset(new Long(tree.getSelectionModel().getSelectedItem().getDatasetId()));
                DownloadManager.getInstance().downloadData(tree.getSelectionModel().getSelectedItem().getFacility(),
                        topcatDataSelection,
                        Utils.normaliseFileName(tree.getSelectionModel().getSelectedItem().getDatasetName()),
                        IdsFlag.ZIP_AND_COMPRESS);

            }
        });

        //add data file if enabled and supported by ids
        if (eventPipline.hasUploadSupport(tree.getSelectionModel().getSelectedItem().getFacility())) {
            MenuItem addDatafile = new MenuItem();
            addDatafile.setText("Add Data File");
            addDatafile.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconAddDatafile()));
            contextMenu.add(addDatafile);
            addDatafile.addSelectionListener(new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent ce) {
                    eventPipline.showUploadDatasetWindow(tree.getSelectionModel().getSelectedItem().getFacility(),
                            tree.getSelectionModel().getSelectedItem().getDatasetId(),
                            tree.getSelectionModel().getSelectedItem(),
                            SOURCE);
                }
            });
        }

        return contextMenu;
    }

    /**
     * Get a context menu specific to an data file node.
     *
     * @return a context menu
     */
    private Menu getDatafileMenu() {
        Menu contextMenu = new Menu();
        contextMenu.setWidth(190);
        contextMenu.addStyleName("context-menu");
        MenuItem showDatafile = new MenuItem();
        showDatafile.setText("Show Data File Parameters");
        showDatafile.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconShowDatafileParameter()));
        contextMenu.add(showDatafile);
        showDatafile.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                eventPipline.showParameterWindowWithHistory(
                        tree.getSelectionModel().getSelectedItem().getFacility(), Constants.DATA_FILE,
                        tree.getSelectionModel().getSelectedItem().getDatafileId(),
                        tree.getSelectionModel().getSelectedItem().getDatafileName());
            }
        });

        MenuItem showSize = new MenuItem();
        showSize.setText("Show Data File Size");
        showSize.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconFileSize()));

        contextMenu.add(showSize);
        showSize.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                if (tree.getSelectionModel().getSelectedItem() != null) {
                    TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
                    topcatDataSelection.addDatafile(new Long(tree.getSelectionModel().getSelectedItem().getDatafileId()));

                    EventPipeLine.getInstance().showDataSelectionSizeDialog(
                            tree.getSelectionModel().getSelectedItem().getFacility(),
                            topcatDataSelection,
                            DataSelectionType.DATAFILE);
                }
            }
        });

        MenuItem showDatafileDownload = new MenuItem();
        showDatafileDownload.setText("Download Data File");
        showDatafileDownload.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconDownloadDatafile()));
        contextMenu.add(showDatafileDownload);
        showDatafileDownload.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                downloadSingleFile(tree.getSelectionModel().getSelectedItem());
            }
        });

        return contextMenu;
    }

    /**
     * Download a single datafile
     * The download name will be the filename and the file will be uncompressed
     *
     * @param node
     */
    private void downloadSingleFile(ICATNode node) {
        //make sure it a data file node type
        if (node.getNodeType() == ICATNodeType.DATAFILE) {
            TopcatDataSelection topcatDataSelection = new TopcatDataSelection();
            topcatDataSelection.addDatafile(new Long(node.getDatafileId()));

            DownloadManager.getInstance().downloadData(node.getFacility(), topcatDataSelection, node.getDatafileName(), IdsFlag.NONE);
        }
    }

    /**
     * Download selected datasets and datafiles.
     *
     * USer will be promtped for download name and files will be a compressed zip
     */
    private void download(String downloadName) {
        List<ICATNode> selectedItems = tree.getCheckedSelection();

        Map<String, TopcatDataSelection> dataSelectionMap = reduceToDataSelection(selectedItems);

        if (dataSelectionMap.size() == 0) {
            eventPipline.showMessageDialog("Nothing selected for download");
        } else if (dataSelectionMap.size() == 1) {
            for (String facility : dataSelectionMap.keySet()) {
                DownloadManager.getInstance().downloadData(facility, dataSelectionMap.get(facility), downloadName, IdsFlag.ZIP_AND_COMPRESS);
                break;
            }
        } else {
            for (String facility : dataSelectionMap.keySet()) {
                DownloadManager.getInstance().downloadData(facility, dataSelectionMap.get(facility), downloadName + "-" + facility, IdsFlag.ZIP_AND_COMPRESS);
            }
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

    /**
     * Setup a handler to react to add investigation details events.
     */
    private void createAddInvestigationDetailsHandler() {
        AddInvestigationDetailsEvent.registerToSource(EventPipeLine.getEventBus(), SOURCE,
                new AddInvestigationDetailsEventHandler() {
                    @Override
                    public void addInvestigationDetails(AddInvestigationDetailsEvent event) {
                        investigationPanel.show();
                        investigationPanel.setInvestigation(event.getInvestigation());
                    }
                });
    }

    /**
     * Setup a handler to react to Login events.
     */
    private void createLoginHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                List<ICATNode> nodes = tree.getStore().findModels("type", ICATNodeType.FACILITY);
                for (ICATNode node : nodes) {
                    if (node.getFacility().equalsIgnoreCase(event.getFacilityName())) {
                        loader.loadChildren(node);
                    }
                }
            }
        });
    }

    /**
     * Setup a handler to react to Logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                // Close the tree and remove the children for the logged out
                // facility
                List<ICATNode> nodes = tree.getStore().findModels("type", ICATNodeType.FACILITY);
                for (ICATNode node : nodes) {
                    if (node.getFacility().equalsIgnoreCase(event.getFacilityName())) {
                        tree.setExpanded(node, false);
                        tree.getStore().removeAll(node);
                    }
                }

                // Hide the investigation details if it is an investigation of
                // the logged out facility
                String invDetailsFacility = investigationPanel.getFacilityName();
                if (!(invDetailsFacility == null) && invDetailsFacility.equalsIgnoreCase(event.getFacilityName())) {
                    investigationPanel.hide();
                    investigationPanel.reset();
                }
            }
        });
    }


    /**
     *  Setup handler for addd data file
     */
    private void CreateAddDatafileHandler() {
        AddDatafileEvent.registerToSource(EventPipeLine.getEventBus(), SOURCE, new AddDatafileEventHandler() {

            @Override
            public void addDatafile(AddDatafileEvent event) {
                node = (ICATNode) event.getNode();
                loader.loadChildren(node);
            }
        });

    }


    /**
     *  Setup handler for addd data file
     */
    private void CreateAddDatasetHandler() {
        AddDatasetEvent.registerToSource(EventPipeLine.getEventBus(), SOURCE, new AddDatasetEventHandler() {

            @Override
            public void addDataset(AddDatasetEvent event) {
                node = (ICATNode) event.getNode();
                loader.loadChildren(node);
            }
        });

    }


    /**
     * This method reduced an ICATNode list to a map of the selected investigation, dataset and datafiles.
     * Child dataset and datafiles are removed
     *
     * @param selectedItems
     * @return
     */
    private Map<String, TopcatDataSelection> reduceToDataSelection(List<ICATNode> selectedItems) {

        Map<String, TopcatDataSelection> dataSelectionMap = new HashMap<String, TopcatDataSelection>();

        //create dataSelectionMap for each facility
        List<TFacility> facilities = eventPipline.getFacilities();
        for(TFacility facility : facilities) {
            dataSelectionMap.put(facility.getName(), new TopcatDataSelection());
        }

        HashMap<String, ArrayList<Long>> invMap = new HashMap<String, ArrayList<Long>>();

        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.INVESTIGATION) {
                ArrayList<Long> idList = invMap.get(node.getFacility());

                if (idList == null) {
                    idList = new ArrayList<Long>();
                }

                idList.add(new Long(node.getInvestigationId()));
                invMap.put(node.getFacility(), idList);

                dataSelectionMap.get(node.getFacility()).addInvestigation(new Long(node.getInvestigationId()));
            }
        }

        // Create map of selected datasets
        // map: key = facility name, value = list of dataset ids
        HashMap<String, ArrayList<Long>> dsMap = new HashMap<String, ArrayList<Long>>();

        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.DATASET) {
                //check if dataset parent is already selected
                ArrayList<Long> invList = invMap.get(node.getFacility());
                if (invList != null) {
                    String parentInvestigationId = tree.getStore().getParent(node).getInvestigationId();
                    if (parentInvestigationId != null) {
                        if (invList.contains(new Long(parentInvestigationId))) {
                            continue;
                        }
                    }
                }

                ArrayList<Long> idList = dsMap.get(node.getFacility());

                if (idList == null) {
                    idList = new ArrayList<Long>();
                }

                idList.add(new Long(node.getDatasetId()));

                dataSelectionMap.get(node.getFacility()).addDataset(new Long(node.getDatasetId()));
                dsMap.put(node.getFacility(), idList);
            }
        }

        // Create map of selected datafiles
        // map: key = facility name, value = list of datafile ids
        HashMap<String, ArrayList<Long>> dfMap = new HashMap<String, ArrayList<Long>>();


        for (ICATNode node : selectedItems) {
            if (node.getNodeType() == ICATNodeType.DATAFILE) {
                ArrayList<Long> dsList = dsMap.get(node.getFacility());
                if (dsList != null) {
                    String parentDataSetId = tree.getStore().getParent(node).getDatasetId();

                    if (parentDataSetId != null) {
                        if (dsList.contains(new Long(parentDataSetId))) {
                            continue;
                        }
                    }
                }

                ArrayList<Long> invList = invMap.get(node.getFacility());
                if (invList != null) {
                    ICATNode investigationNode = null;

                    //get parent node
                    ICATNode parentNode = tree.getStore().getParent(node);
                    //check if node is a dataset
                    if (parentNode.getNodeType() == ICATNodeType.DATAFILE) {
                        //again get the parent to get the investigation
                        ICATNode tempNode = tree.getStore().getParent(parentNode);

                        if (tempNode.getNodeType() == ICATNodeType.DATASET) {
                        	ICATNode tempNode1 = tree.getStore().getParent(tempNode);

                        	if (tempNode1.getNodeType() == ICATNodeType.INVESTIGATION) {
                                investigationNode = tempNode1;
                            }
                        }

                        if (tempNode.getNodeType() == ICATNodeType.INVESTIGATION) {
                            investigationNode = tempNode;
                        }
                    } else if (parentNode.getNodeType() == ICATNodeType.DATASET) {
                        //again get the parent to get the investigation
                        ICATNode tempNode = tree.getStore().getParent(parentNode);

                        if (tempNode.getNodeType() == ICATNodeType.INVESTIGATION) {
                            investigationNode = tempNode;
                        }
                    } else if(parentNode.getNodeType() == ICATNodeType.INVESTIGATION){
                        investigationNode = parentNode;
                    }

                    if (investigationNode != null) {
                        String parentInvestigationId = investigationNode.getInvestigationId();

                        if (parentInvestigationId != null) {
                            if (invList.contains(new Long(parentInvestigationId))) {
                                continue;
                            }
                        }
                    }
                }

                ArrayList<Long> idList = dfMap.get(node.getFacility());
                if (idList == null) {
                    idList = new ArrayList<Long>();
                }

                idList.add(new Long(node.getDatafileId()));

                dataSelectionMap.get(node.getFacility()).addDatafile(new Long(node.getDatafileId()));
                dfMap.put(node.getFacility(), idList);
            }
        }

        //remove facilities without items
        for(Map.Entry<String, TopcatDataSelection> entry : dataSelectionMap.entrySet()) {
            if (entry.getValue().getParameters().size() == 0) {
                dataSelectionMap.remove(entry.getKey());
            }
        }


        //debug
        /*
        for(Map.Entry<String, TopcatDataSelection> entry : dataSelectionMap.entrySet()) {
            rootLogger.log(Level.SEVERE, "DataSelection:" + Arrays.toString(entry.getValue().getParameters().entrySet().toArray()));
        }
        */

        return dataSelectionMap;
    }

}
