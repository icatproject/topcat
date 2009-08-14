/**
 * 
 */
package uk.topcat.web.client.ui;

import java.util.ArrayList;
import java.util.List;

import uk.topcat.web.client.language.LanguageConstants;
import uk.topcat.web.client.model.SampleListModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.GWT;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.Grid;

/**
 * @author ll56
 *
 */
public class SampleView extends LayoutContainer {

	private static SampleView samplev; 
	private LanguageConstants lang;
	VerticalPanel panel;
	
	// Singleton. 
	public static SampleView getView(){
		if(samplev == null){
			samplev = new SampleView();
		}
		return samplev;
	}
	
	public String toString()
	{
		return lang.vtab_sampleLabel();
	}
	
	/**
	 * 
	 */
	public SampleView() {
		
		 lang = GWT.create(LanguageConstants.class);
		  

		 
			 GroupingStore<SampleListModel> sampleStore = new GroupingStore<SampleListModel>();
			 sampleStore.groupBy("investigation_title");
			 List<ColumnConfig> config = new ArrayList<ColumnConfig>();  
			 
			 // Sample_id - Hidden  
			 ColumnConfig sample_id = new ColumnConfig("sample_id", lang.slst_sampleIdCol(), 50);
			 sample_id.setHidden(true);
			 config.add(sample_id);
			 
			 // Main info displayed in the table			 
			 config.add(new ColumnConfig("name", lang.slst_nameCol(), 100));
			 config.add(new ColumnConfig("instance", lang.slst_instanceCol(), 60));
			 config.add(new ColumnConfig("chemical_formula", lang.slst_chemFormCol(), 150));
			 config.add(new ColumnConfig("proposal_sample_id", lang.slst_propSampleIdCol(), 100));
			 
			 // Investigation Info - Hidden
			 ColumnConfig inv_title = new ColumnConfig("investigation_title", lang.slst_invTitleCol(), 50);
			 inv_title.setHidden(true);
			 config.add(inv_title);
			 ColumnConfig inv_num = new ColumnConfig("investigation_num", lang.slst_invNumCol(), 50);
			 inv_num.setHidden(true);
			 config.add(inv_num);
			 ColumnConfig inv_visit = new ColumnConfig("investigation_visit", lang.slst_invVisitCol(), 50);
			 inv_visit.setHidden(true);
			 config.add(inv_visit);
			 			 
			 ColumnConfig safety = new ColumnConfig("safety_information", lang.slst_safetyInfoCol(), 50);
			 safety.setHidden(true);
			 config.add(safety);
			 
			 final ColumnModel columnModel = new ColumnModel(config);  
			 GroupingView view = new GroupingView();  
			 view.setShowGroupedColumn(false);  
			 view.setForceFit(true); 
 
			      view.setGroupRenderer(new GridGroupRenderer() {  
			        public String render(GroupColumnData data) {  
			          String f = columnModel.getColumnById(data.field).getHeader();  
			          String l = data.models.size() == 1 ? "Item" : "Items";  
			          return f + ": " + data.group + " (" + data.models.size() + " " + l + ")";  
			        }  
			      });  
			      
			      Grid<SampleListModel> sampleList = new Grid<SampleListModel>(sampleStore, columnModel);
			      
			sampleList.setView(view);
			sampleList.setBorders(true);
					 
		 
		 
		 
		 // This is the series of Buttons at the bottom of the panel.
	        HorizontalPanel buttonBar = new HorizontalPanel();
	        buttonBar.setHorizontalAlign(HorizontalAlignment.RIGHT);
	        buttonBar.setWidth(900);
	        buttonBar.setLayout(new FlowLayout(10));
	        //buttonBar.setBorders(true);
	        buttonBar.setAutoHeight(true);
	        buttonBar.setSpacing(5);
		    buttonBar.add(new Button("Download"));
		    buttonBar.add(new Button("Select All"));
		    buttonBar.add(new Button("Reverse Selection"));
		    buttonBar.add(new Button("Do Something"));
		    buttonBar.add(new Button("And something else"));

		    
				    panel = new VerticalPanel();  
				    panel.setLayout(new FlowLayout(10));  
				    
//				    panel.setFrame(false);  
//				    panel.setCollapsible(false);  
//				    panel.setAnimCollapse(false);  
//				    panel.setButtonAlign(HorizontalAlignment.CENTER);  
				    panel.add(sampleList);
				    panel.add(buttonBar);  
				   

				    
				    //panel.setVisible(false);
				    add(panel);  
	}



}
