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

import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
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
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.CheckCascade;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
/**
 * This widget shows a tree to browse All data. the hierarchy is
 *  -- Facility
 *  	-- Instrument
 *  		-- Investigation
 *  			-- Dataset or Datafile
 *  				-- Datafile 
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class BrowsePanel extends Composite {

	private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
	private BaseTreeLoader<ICATNode> loader;
	TreePanel<ICATNode> treeGrid;
	HashMap<String, ArrayList<ICATNode>> logfilesMap = new HashMap<String, ArrayList<ICATNode>>();

	public BrowsePanel() {
		
		LayoutContainer layoutContainer = new LayoutContainer();
		layoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
		
		ContentPanel contentPanel = new ContentPanel();
		contentPanel.setHeaderVisible(false);
		contentPanel.setCollapsible(true);
		contentPanel.setLayout(new RowLayout(Orientation.VERTICAL));
		
		ToolBar toolBar = new ToolBar();
		contentPanel.add(toolBar);
		
				ButtonBar buttonBar = new ButtonBar();
				
						Button btnDownload = new Button("Download", AbstractImagePrototype
								.create(Resource.ICONS.iconDownload()));
						btnDownload.addSelectionListener(new SelectionListener<ButtonEvent>() {
							public void componentSelected(ButtonEvent ce) {
								// Collect list of Datafiles selected and download
								EventPipeLine.getInstance().downloadDatafilesICATNodes(
										treeGrid.getCheckedSelection());
							}
						});
						buttonBar.add(btnDownload);
						toolBar.add(buttonBar);

		//Add Treepanel
		//This is RPC proxy to get the information from the server using GWT-RPC AJAX calls
		//each time the user expands the tree to browse.
		RpcProxy<ArrayList<ICATNode>> proxy = new RpcProxy<ArrayList<ICATNode>>() {

			//Get the nodes from the server using GWT-RPC. For the datafiles grouped under datafiles get 
			//it from the cache.
			@Override
			protected void load(Object loadConfig,
					final AsyncCallback<ArrayList<ICATNode>> callback) {
				//Retrive the datafiles information from the cache instead of going to the GWT-RPC
				//for datafiles grouped under datafiles (log files under RAW files)
				if (loadConfig != null	&& ((ICATNode) loadConfig).getNodeType() == ICATNodeType.DATAFILE) {
					String key = ((ICATNode) loadConfig).getFacility()	+ ((ICATNode) loadConfig).getDatafileId();
					callback.onSuccess(logfilesMap.get(key));
					return;
				}
				utilityService.getAllICATNodeDatafiles((ICATNode)loadConfig, new AsyncCallback<HashMap<String,ArrayList<ICATNode>>>(){

					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					@Override
					public void onSuccess(
							HashMap<String, ArrayList<ICATNode>> result) {
						ArrayList<ICATNode> rawFiles=result.get("");
						result.remove(""); //remove from the result list
						for(String key:result.keySet()){
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
				
				return logfilesMap.get(parent.getFacility()+parent.getDatafileId())!=null//treeGrid.getStore().getChildCount(parent) != 0
				|| parent.getNodeType() != ICATNodeType.DATAFILE
				&& parent.getNodeType() != ICATNodeType.UNKNOWN;				
			}
		};

		TreeStore<ICATNode> store = new TreeStore<ICATNode>(loader);
		
		VerticalPanel contentPanel_1 = new VerticalPanel();
		contentPanel_1.setLayoutOnChange(true);
		contentPanel_1.setAutoWidth(true);
		//contentPanel_1.setAutoHeight(true); // This will make use of the browser bar
                contentPanel_1.setScrollMode(Scroll.AUTO); //This will set the Scroll bar
		contentPanel_1.setLayout(new RowLayout(Orientation.HORIZONTAL));
		contentPanel_1.setBorders(false);
		treeGrid = new TreePanel<ICATNode>(store);
		contentPanel_1.add(treeGrid);
		treeGrid.setAutoHeight(true);
		treeGrid.setAutoWidth(true);
		//This handler calls the reloading of the tree if the children are none. useful
		//when the session expires or user hasn't logged in.
		treeGrid.addListener(Events.Expand, new Listener<TreePanelEvent<ICATNode>>(){
			@SuppressWarnings("unchecked")
			@Override
			public void handleEvent(TreePanelEvent<ICATNode> be) {
				// TODO Auto-generated method stub
				TreePanel<ICATNode>.TreeNode node = be.getNode();

				if(!node.isLeaf()&&node.getItemCount()==0)
					loader.loadChildren(be.getItem());

			}

		});
		//This is to check the RAW Datafile checked and check all the children
		treeGrid.addListener(Events.BeforeCheckChange, new Listener<TreePanelEvent<ICATNode>>(){
			@Override
			public void handleEvent(TreePanelEvent<ICATNode> be) {
				ICATNode node = be.getItem(); // If children of raw datafiles are not loaded then load them.
				if(node.getNodeType()==ICATNodeType.DATAFILE&&loader.hasChildren(node)&&treeGrid.getStore().getChildCount(node)==0){
					loader.loadChildren(node);
				}
			}
			
		});
		//On double click on datafile node show a parameter window
		treeGrid.sinkEvents(Events.OnDoubleClick.getEventCode());
		treeGrid.addListener(Events.OnDoubleClick, new Listener<TreePanelEvent<ICATNode>>(){
			@SuppressWarnings("unchecked")
			@Override
			public void handleEvent(TreePanelEvent<ICATNode> be) {
				// TODO Auto-generated method stub
				TreePanel<ICATNode>.TreeNode node = be.getNode();
				if(node.getModel().getNodeType()==ICATNodeType.DATAFILE){
					ICATNode icatnode =node.getModel();
					EventPipeLine.getInstance().showParameterWindowWithHistory(icatnode.getFacility(), icatnode.getDatafileId(), icatnode.getDatafileName());
				}
			}

		});
		treeGrid.setCaching(true);
		treeGrid.setDisplayProperty("name");
		treeGrid.setCheckable(true);
		treeGrid.setCheckStyle(CheckCascade.CHILDREN);
		treeGrid.setSize("900px", "600px");
		treeGrid.setBorders(false);
		contentPanel.add(contentPanel_1);
		contentPanel_1.setSize("900px", "600px");
		layoutContainer.add(contentPanel);		
		
		initComponent(layoutContainer);
		layoutContainer.setBorders(true);
	}

}
