package uk.topcat.web.client.ui.results;

import java.util.ArrayList;
import java.util.List;

import uk.topcat.web.client.KeywordSearch;
import uk.topcat.web.client.KeywordSearchAsync;
import uk.topcat.web.client.language.LanguageConstants;
import uk.topcat.web.client.model.TInvestigation;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PagingBeanModelGrid extends LayoutContainer {

	private final KeywordSearchAsync keywordSearch = GWT
	.create(KeywordSearch.class);
	
	
	ContentPanel panel;
	Grid<BeanModel> grid;
	
	final BasePagingLoader<PagingLoadResult<ModelData>> loader;
	
	public void setVisible(boolean visible) {
		grid.setVisible(visible);
	}
	
	public BasePagingLoader<PagingLoadResult<ModelData>> getLoader() {
		return loader;
	}
	
	public void loadGrid() {
		
	}
	
	public PagingBeanModelGrid() {
		LanguageConstants lang = GWT.create(LanguageConstants.class);
		
		
	    FlowLayout layout = new FlowLayout(10);  
	    setLayout(layout);  
	  
	    RpcProxy<PagingLoadResult<TInvestigation>> proxy = new RpcProxy<PagingLoadResult<TInvestigation>>() {  
	      @Override  
	      public void load(Object loadConfig,  
	          AsyncCallback<PagingLoadResult<TInvestigation>> callback) {  
	    	  keywordSearch.searchByKeyword(new ArrayList<String>(), (PagingLoadConfig) loadConfig, callback);   	  
	      }  
	    };  
	  
	    // loader  
	    loader = new BasePagingLoader<PagingLoadResult<ModelData>>(  
	        proxy, new BeanModelReader());  
	    loader.setRemoteSort(true);  
	  
	    ListStore<BeanModel> store = new ListStore<BeanModel>(loader);  
	  
	    final PagingToolBar toolBar = new PagingToolBar(15);  
	    toolBar.bind(loader);  
	    
	    GridCellRenderer<TInvestigation> facility = new GridCellRenderer<TInvestigation>() {  
	        public String render(TInvestigation model, String property, ColumnData config, int rowIndex,  
	            int colIndex, ListStore<TInvestigation> store, Grid<TInvestigation> grid) {  
	          String fac = (String)model.get(property);  
	        	//String fac = model.getFacility();
	          //String style = val < 0 ? "red" : "green";  
	          //return "<span style='color:" + style + "'>" + number.format(val) + "</span>";  
	          	return "<img src='img/" + fac + ".jpg' />";
	        }  
	      };  
	     
	    XTemplate tpl = XTemplate.create("<p><b>Summary:</b> {invAbstract}</p>");  
	    RowExpander expander = new RowExpander();  
	    expander.setTemplate(tpl); 
	  
	    List<ColumnConfig> columns = new ArrayList<ColumnConfig>();  
	    columns.add(new RowNumberer());
	    columns.add(expander);
	    columns.add(new ColumnConfig("invNumber", lang.invNumberColumnTitle(), 60));  
	    columns.add(new ColumnConfig("title", lang.titleColumnTitle(), 220));  
	    columns.add(new ColumnConfig("investigators", lang.investigatorsColumnTitle(), 200));
	    
	    columns.add(new ColumnConfig("invType", lang.invTypeColumnTitle(), 50)); 
	    
	    columns.add(new ColumnConfig("instrument", lang.instrumentColumnTitle(), 70));  
	    
	    //ColumnConfig facilityColumn = new ColumnConfig("facility", "Facility", 50);
	    //facilityColumn.setRenderer(facility);	   
	    //columns.add(facilityColumn);  
	    
	    
	    columns.add(new ColumnConfig("facility", lang.facilityColumnTitle(), 50));  
	    
	    ColumnConfig date = new ColumnConfig("year", lang.yearColumnTitle(), 50);  
	    date.setDateTimeFormat(DateTimeFormat.getFormat("yyyy"));  
	    columns.add(date);  
	    
	   
	    ColumnModel cm = new ColumnModel(columns);  
	  
	    grid = new Grid<BeanModel>(store, cm);  
	    grid.addPlugin(expander);  
	    grid.addListener(Events.Attach, new Listener<GridEvent<BeanModel>>() {  
	      public void handleEvent(GridEvent<BeanModel> be) {  
	        //loader.load(0, 15);  
	      }  
	    }); 
	    grid.setLoadMask(true);  
	    grid.setBorders(false);  
	    //grid.setAutoExpandColumn("invNumber");  
	    grid.setStripeRows(true);
	    //grid.getView().setAutoFill(true); 
	    grid.setAutoHeight(true);
	    
	    grid.addListener(Events.RowDoubleClick, new Listener<GridEvent>(){
	        public void handleEvent(GridEvent ge)
	        {       	      	
	        	final Window window = new InvestigationDetailsWindow();  
	            window.show();            
	        }       
	    });
	    
	  
	    panel = new ContentPanel();  
	    panel.setFrame(false);  
	    panel.setCollapsible(false);  
	    panel.setAnimCollapse(false);  
	    panel.setButtonAlign(HorizontalAlignment.CENTER);  
	    panel.setHeading("Investigations");  
	    panel.setLayout(new FitLayout());  
	    panel.add(grid);  
	    panel.setBottomComponent(toolBar);  
	   
	    
	    //panel.setVisible(false);
	    add(panel);  
	    
	  
	  }  
}
