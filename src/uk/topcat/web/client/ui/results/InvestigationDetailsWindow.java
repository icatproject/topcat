package uk.topcat.web.client.ui.results;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;

public class InvestigationDetailsWindow extends Window {

	public InvestigationDetailsWindow() {
		 
	    setSize(600, 400);  
	    setPlain(true);  
	    setModal(true);  
	    setBlinkModal(true);  
	    setHeading("Investigation Details");  
	    setLayout(new FitLayout());  
	    setCollapsible(true);
	    
	    GroupingStore<Stock> store = new GroupingStore<Stock>();  
	    store.add(InvestigationDetailsWindow.getCompanies());  
	    store.groupBy("industry");  
	  
	    ColumnConfig company = new ColumnConfig("name", "Company", 60);  
	    ColumnConfig price = new ColumnConfig("open", "Price", 20);  
	    ColumnConfig change = new ColumnConfig("change", "Change", 20);  
	    ColumnConfig industry = new ColumnConfig("industry", "Industry", 20);  
	    ColumnConfig last = new ColumnConfig("date", "Last Updated", 20);  
	    last.setDateTimeFormat(DateTimeFormat.getFormat("MM/dd/y"));  
	  
	    List<ColumnConfig> config = new ArrayList<ColumnConfig>();  
	    config.add(company);  
	    config.add(price);  
	    config.add(change);  
	    config.add(industry);  
	    config.add(last);  
	  
	    final ColumnModel cm = new ColumnModel(config);  
	  
	    GroupingView view = new GroupingView();  
	    view.setShowGroupedColumn(false);  
	    view.setForceFit(true);  
	    view.setGroupRenderer(new GridGroupRenderer() { 
	    	
	      public String render(GroupColumnData data) {  
	        
	    	  String f = cm.getColumnById(data.field).getHeader();  
	        String l = data.models.size() == 1 ? "Item" : "Items";  
	        return f + ": " + data.group + " (" + data.models.size() + " " + l + ")";  
	      }  
	    });  
	  
	    Grid<Stock> grid = new Grid<Stock>(store, cm);  
	    grid.setView(view);  
	    grid.setBorders(true);  
	  
	    ContentPanel panel = new ContentPanel();  
	    panel.setHeading("Grouping Example");  
	    panel.setHeaderVisible(false);
	    //panel.setIcon(Examples.ICONS.table());  
	      
	    panel.setFrame(false);  
	    panel.setSize(700, 450);  
	    panel.setLayout(new FitLayout());  
	    panel.add(grid); 
	    
	    
	    add(panel);
	    
	    
	}
	
	public static List<Stock> getCompanies() {
	    DateTimeFormat f = DateTimeFormat.getFormat("M/d h:mma");
	    List<Stock> stocks = new ArrayList<Stock>();
	    stocks.add(new Stock("3m Co", 71.72, 0.02, 0.03, f.parse("4/2 12:00am"),
	        "Manufacturing"));
	    stocks.add(new Stock("Alcoa Inc", 29.01, 0.42, 1.47, f.parse("4/1 12:00am"),
	        "Manufacturing"));
	    stocks.add(new Stock("Altria Group Inc", 83.81, 0.28, 0.34, f.parse("4/3 12:00am"),
	        "Manufacturing"));
	    stocks.add(new Stock("American Express Company", 52.55, 0.01, 0.02,
	        f.parse("4/8 12:00am"), "Finance"));
	    stocks.add(new Stock("American International Group, Inc.", 64.13, 0.31, 0.49,
	        f.parse("4/1 12:00am"), "Services"));
	    stocks.add(new Stock("AT&T Inc.", 31.61, -0.48, -1.54, f.parse("4/8 12:00am"),
	        "Services"));
	    stocks.add(new Stock("Boeing Co.", 75.43, 0.53, 0.71, f.parse("4/8 12:00am"),
	        "Manufacturing"));
	    stocks.add(new Stock("Caterpillar Inc.", 67.27, 0.92, 1.39, f.parse("4/1 12:00am"),
	        "Services"));
	    stocks.add(new Stock("Citigroup, Inc.", 49.37, 0.02, 0.04, f.parse("4/4 12:00am"),
	        "Finance"));
	    stocks.add(new Stock("E.I. du Pont de Nemours and Company", 40.48, 0.51, 1.28,
	        f.parse("4/1 12:00am"), "Manufacturing"));
	    stocks.add(new Stock("Exxon Mobil Corp", 68.1, -0.43, -0.64, f.parse("4/3 12:00am"),
	        "Manufacturing"));
	    stocks.add(new Stock("General Electric Company", 34.14, -0.08, -0.23,
	        f.parse("4/3 12:00am"), "Manufacturing"));
	    stocks.add(new Stock("General Motors Corporation", 30.27, 1.09, 3.74,
	        f.parse("4/3 12:00am"), "Automotive"));
	    stocks.add(new Stock("Hewlett-Packard Co.", 36.53, -0.03, -0.08,
	        f.parse("4/3 12:00am"), "Computer"));
	    stocks.add(new Stock("Honeywell Intl Inc", 38.77, 0.05, 0.13, f.parse("4/3 12:00am"),
	        "Manufacturing"));
	    stocks.add(new Stock("Intel Corporation", 19.88, 0.31, 1.58, f.parse("4/2 12:00am"),
	        "Computer"));
	    stocks.add(new Stock("International Business Machines", 81.41, 0.44, 0.54,
	        f.parse("4/1 12:00am"), "Computer"));
	    stocks.add(new Stock("Johnson & Johnson", 64.72, 0.06, 0.09, f.parse("4/2 12:00am"),
	        "Medical"));
	    stocks.add(new Stock("JP Morgan & Chase & Co", 45.73, 0.07, 0.15,
	        f.parse("4/2 12:00am"), "Finance"));
	    stocks.add(new Stock("McDonald\"s Corporation", 36.76, 0.86, 2.40,
	        f.parse("4/2 12:00am"), "Food"));
	    stocks.add(new Stock("Merck & Co., Inc.", 40.96, 0.41, 1.01, f.parse("4/2 12:00am"),
	        "Medical"));
	    stocks.add(new Stock("Microsoft Corporation", 25.84, 0.14, 0.54,
	        f.parse("4/2 12:00am"), "Computer"));
	    stocks.add(new Stock("Pfizer Inc", 27.96, 0.4, 1.45, f.parse("4/8 12:00am"),
	        "Services"));
	    stocks.add(new Stock("The Coca-Cola Company", 45.07, 0.26, 0.58,
	        f.parse("4/1 12:00am"), "Food"));
	    stocks.add(new Stock("The Home Depot, Inc.", 34.64, 0.35, 1.02,
	        f.parse("4/8 12:00am"), "Retail"));
	    stocks.add(new Stock("The Procter & Gamble Company", 61.91, 0.01, 0.02,
	        f.parse("4/1 12:00am"), "Manufacturing"));
	    stocks.add(new Stock("United Technologies Corporation", 63.26, 0.55, 0.88,
	        f.parse("4/1 12:00am"), "Computer"));
	    stocks.add(new Stock("Verizon Communications", 35.57, 0.39, 1.11,
	        f.parse("4/3 12:00am"), "Services"));
	    stocks.add(new Stock("Wal-Mart Stores, Inc.", 45.45, 0.73, 1.63,
	        f.parse("4/3 12:00am"), "Retail"));
	    stocks.add(new Stock("Walt Disney Company (The) (Holding Company)", 29.89, 0.24,
	        0.81, f.parse("4/1 12:00am"), "Services"));
	    return stocks;
	  }

}
