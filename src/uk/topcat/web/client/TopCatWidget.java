package uk.topcat.web.client;

import java.util.List;

import uk.topcat.web.client.autocomplete.AutoSuggestForm;
import uk.topcat.web.client.language.LanguageConstants;
import uk.topcat.web.client.ui.SampleView;
import uk.topcat.web.client.ui.header.HeaderPanel;
import uk.topcat.web.client.ui.results.PagingBeanModelGrid;
import uk.topcat.web.client.ui.results.VerticalTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * @author d.w.flannery@gmail.com (Damian Flannery)
 * 
 */
public class TopCatWidget extends Composite implements TopCatPresenter.View {

	//private final DockPanel panel;
	private final DockPanel dock;	
	private Widget content;
	public static final int PAGE_WIDTH = 950;
	public static final int VERTICAL_TAB_LABEL_MAX_WIDTH = 105;
	Button search;
	AutoSuggestForm autoForm;
	public PagingBeanModelGrid pbmg;
	
	
	public PagingBeanModelGrid getPagingModelGrid() {
		return pbmg;
	}
	
	public HasClickHandlers getSearchClickHandlers() {
		return search;
	}
	
	public List<String> getKeywordSearchTerms() {
		return autoForm.getItemsSelected();
	}

	public TopCatWidget() {

		LanguageConstants lang = GWT.create(LanguageConstants.class);
		
		//create master dock panel
		dock = new DockPanel();
		dock.setWidth("100%");
		dock.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		dock.setVerticalAlignment(HasAlignment.ALIGN_TOP);
		dock.setSpacing(0);

		//create tab panel for keyword, facility, advanced search
		final DecoratedTabPanel tabPanel = new DecoratedTabPanel();
		tabPanel.setWidth((PAGE_WIDTH - VERTICAL_TAB_LABEL_MAX_WIDTH) + "px");
		tabPanel.setAnimationEnabled(false);

		
		//add a keyword tab
		//-----------------
		HTML keywordInfo = new HTML(lang.keywordTabLabel());
		HTML keywordText = new HTML(lang.keywordSearchInstructions());

		//create autocomplete panel for keyword search
		HorizontalPanel autoPanel = new HorizontalPanel();
		autoForm = new AutoSuggestForm();
		autoPanel.add(autoForm);
		autoPanel.add(new HTML("&nbsp;"));
		search = new Button(lang.cbtn_search());
		autoPanel.add(search);
		
		VerticalPanel keywordPanel = new VerticalPanel();
		keywordPanel.add(new HTML("&nbsp;"));
		keywordPanel.add(keywordText);
		keywordPanel.add(autoPanel);
		keywordPanel.add(new HTML("&nbsp;"));

		//Hack to enable switching of tabs onHover - add handler to HTML keywordInfo element
		keywordInfo.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				tabPanel.selectTab(0);
			}
		});
		tabPanel.add(keywordPanel, keywordInfo);
		
		// Add an Advanced tab
		// -------------------
		HTML advancedInfo = new HTML(lang.htab_advancedLabel());
		HTML advancedText = new HTML(lang.htab_advancedDescription());
		advancedInfo.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				tabPanel.selectTab(1);
			}
		});
		tabPanel.add(advancedText, advancedInfo);
		
		// Add a tag tab 
		//-----------------
		HTML tagInfo = new HTML(lang.htab_tagLabel());
		VerticalPanel vPanel = new VerticalPanel();
		
		HTML tagText = new HTML(lang.htab_tagDescription());
		vPanel.add(tagText);

		Image browseImg = new Image("img/TagCloud.jpg");
		// Image("http://www.wordle.net/thumb/wrdl/1010547/neutrons");
		vPanel.add(browseImg);
		
		tagInfo.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				tabPanel.selectTab(2);
			}
		});
		tabPanel.add(vPanel, tagInfo);

		// Add a browse tab 
		//-----------------
		HTML browseInfo = new HTML(lang.htab_browseLabel());
		
		HorizontalPanel browseHPanel = new HorizontalPanel();
		
		
		VerticalPanel browseVPanel = new VerticalPanel();
		HTML browseText = new HTML(lang.htab_browseDescription());
		browseVPanel.add(browseText);

		Tree browseTree = new Tree();
		
		TreeItem root = new TreeItem("/");
		browseTree.addItem(root);
		
		TreeItem facility_ISIS = new TreeItem("ISIS/");
		facility_ISIS.addItem("Cycle 2008-06/");
		facility_ISIS.addItem("Cycle 2009-01/");
		facility_ISIS.addItem("Cycle 2009-01/");
		root.addItem(facility_ISIS);
		
		TreeItem facility_DLS = new TreeItem("DLS/");
		facility_DLS.addItem("2007/");
		facility_DLS.addItem("2008/");
		facility_DLS.addItem("2009/");
		root.addItem(facility_DLS);
		
		TreeItem facility_ILL = new TreeItem("ILL/");	
		facility_ILL.addItem("2002/");
		facility_ILL.addItem("2003/");
		facility_ILL.addItem("2004/");
		
		TreeItem ill_2005 = new TreeItem("2005/");	
		ill_2005.addItem("D1A/");
		ill_2005.addItem("D11/");
		ill_2005.addItem("IN4/");

		TreeItem ill_instr_IN3 = new TreeItem("IN3/");	
		ill_2005.addItem(ill_instr_IN3);
		ill_2005.addItem("IN20/");
		ill_2005.addItem("Cryo-EDM/");
		facility_ILL.addItem(ill_2005);
		facility_ILL.addItem("2006/");
		facility_ILL.addItem("2007/");
		facility_ILL.addItem("2008/");
		facility_ILL.addItem("2009/");		
		root.addItem(facility_ILL);
		browseTree.addItem(root);
		
		browseVPanel.add(browseTree);
		browseHPanel.add(browseVPanel);
		
		VerticalPanel buttonPanel = new VerticalPanel();
	
		Button addToSel = new Button("Add to Selection");
		Button removeFromSel = new Button("Remove from Selection");
		buttonPanel.add(addToSel);
		buttonPanel.add(new HTML("&nbsp;"));
		buttonPanel.add(removeFromSel);
		browseHPanel.add(buttonPanel);

		
		browseInfo.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				tabPanel.selectTab(3);
			}
		});
		tabPanel.add(browseHPanel, browseInfo);

		// Add an My Data tab
		// -------------------
		HTML myDataInfo = new HTML(lang.htab_myDataLabel());
		HTML myDataText = new HTML(lang.htab_myDataDescription());
		myDataInfo.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				tabPanel.selectTab(4);
			}
		});
		tabPanel.add(myDataText, myDataInfo);
		
		// Add an Bookmarks tab
		// -------------------
		HTML bookmarksInfo = new HTML(lang.htab_bookmarksLabel());
		HTML bookmarksText = new HTML(lang.htab_bookmarksDescription());
		bookmarksInfo.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				tabPanel.selectTab(5);
			}
		});
		tabPanel.add(bookmarksText, bookmarksInfo);
		
		
		// Add an Preferences tab
		// -----------------------
		HTML prefInfo = new HTML(lang.htab_preferenceLabel());
		HTML prefText = new HTML(lang.htab_preferenceDescription());
		prefInfo.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				tabPanel.selectTab(6);
			}
		});
		tabPanel.add(prefText, prefInfo);
		
		// Return the content
		// ------------------
		tabPanel.selectTab(0);
		tabPanel.ensureDebugId("cwTabPanel");

		
		
		
		
		//Create a Vertical Tab Panel for Search Results
		final VerticalTabPanel vtp = new VerticalTabPanel();
		vtp.setWidth(PAGE_WIDTH + "px");
		
		//Create PagingBeanModelGrid to house search Results
		pbmg = new PagingBeanModelGrid();
		
		//Create Tabs (including PagingBeanModelGrid on first tab)
		
		// Create Vertical Panels
		// ----------------------
		
		// Welcome
		VerticalPanel welcomePanel = new VerticalPanel();
		HTML welcomeText = new HTML(lang.vtab_welcomeDescription());
		welcomePanel.add(welcomeText);
		Image pandataLogo = new Image("img/Logos_Pandata.jpg");
		welcomePanel.add(pandataLogo);
		vtp.add(welcomePanel, new HTML(lang.vtab_welcomeLabel()));
		
		// Investigation
		VerticalPanel investigationPanel = new VerticalPanel();
		HTML investigationText = new HTML(lang.vtab_investigationDescription());
		investigationPanel.add(investigationText);
		investigationPanel.add(pbmg);
		vtp.add(investigationPanel,new HTML(lang.vtab_investigationLabel())); 

		// Sample
		SampleView sampleV = SampleView.getView();			
		vtp.add(sampleV,sampleV.toString() );  
	
		// Dataset
		VerticalPanel datasetPanel = new VerticalPanel();
		HTML datasetText = new HTML(lang.vtab_datasetDescription());
		datasetPanel.add(datasetText);
		datasetPanel.add(new PagingBeanModelGrid());	
		vtp.add(datasetPanel,new HTML(lang.vtab_datasetLabel()) );  
	
		// DataFiles
		VerticalPanel datafilePanel = new VerticalPanel();
		HTML datafileText = new HTML(lang.vtab_datafileDescription());
		datafilePanel.add(datafileText);
		datafilePanel.add(new PagingBeanModelGrid());		
		vtp.add(datafilePanel,new HTML(lang.vtab_datafileLabel()) );  
		
		// Download
		vtp.add(new HTML(lang.vtab_downloadDescription()), new HTML(lang.vtab_downloadLabel()));
		vtp.add(new HTML(lang.vtab_permissionDescription()), new HTML(lang.vtab_permissionLabel()));
		vtp.add(new HTML(lang.vtab_applicationDescription()), new HTML(lang.vtab_applicationLabel()));
		vtp.selectTab(0);
		
		
		/*
		search.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				//vtp.insert(new PagingGrid(), tabText, beforeIndex)
				
				vtp.remove(0);
				//vtp.insert(new PagingGrid(), "Search Results", 0);
				//new PagingGrid();
				
				vtp.insert(new PagingBeanModelGrid(), "Search Results", 0);
				
				vtp.selectTab(0);
			}
		});
		*/

		// Create a table to layout the login form options
		VerticalPanel loginPanel = new VerticalPanel();
		FlexTable layout = new FlexTable();
		layout.setCellSpacing(0);
		layout.setWidth("300px");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// Add a title to the form
		layout.setHTML(0, 0, "..");
		cellFormatter.setColSpan(0, 0, 2);
		cellFormatter.setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);

		Grid advancedOptions = new Grid(3, 5);
		advancedOptions.setCellSpacing(0);
		advancedOptions.setHTML(0, 2,
				lang.usernameLabel());
		advancedOptions.setWidget(0, 3, new TextBox());
		advancedOptions.setHTML(1, 2, lang.passwordLabel());
		advancedOptions.setWidget(1, 3, new PasswordTextBox());
		advancedOptions.setWidget(2, 3, new Button(lang.loginButton()));
		advancedOptions.setHTML(0, 0, "<a href='#'><i><font color='orange'>DLS</font></i></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		advancedOptions.setHTML(1, 0, "<a href='#'><i><font color='orange'>ILL</font></i></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		advancedOptions.setHTML(2, 0, "<a href='#'><i><font color='orange'>ISIS</font></i></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		
		// Add advanced options to form in a disclosure panel
		HTML loginHeader = new HTML(lang.welcomeMessage());
		loginHeader.setWordWrap(false);
		
		//DisclosurePanel is a GWT widget that expands on click to reveal more UI
		final DisclosurePanel advancedDisclosure = new DisclosurePanel(
				loginHeader);
		loginHeader.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				advancedDisclosure.setOpen(true);
			}
		});
		
		advancedDisclosure.setAnimationEnabled(true);
		advancedDisclosure.ensureDebugId("cwDisclosurePanel");
		advancedDisclosure.setContent(advancedOptions);
		layout.setWidget(3, 0, advancedDisclosure);
		cellFormatter.setColSpan(3, 0, 2);

		// Wrap the contents in a DecoratorPanel
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setWidget(advancedDisclosure);

		loginPanel.add(decPanel);

		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		mainPanel.setSpacing(10);

		HeaderPanel myPanel = new HeaderPanel();
		myPanel.setSize("100%", "100%");
		myPanel.addStyleName("demo-panel");

		HorizontalPanel logoPanel = new HorizontalPanel();
		Image logo = new Image("img/TopCatLogo.gif");
		logoPanel.add(new Image("img/Spacer.gif"));
		logoPanel.add(new Image("img/Spacer.gif"));
		
		logoPanel.add(logo);

		myPanel.add(logoPanel, loginPanel);
		myPanel
				.setCellHorizontalAlignment(loginPanel,
						HasAlignment.ALIGN_RIGHT);
		myPanel.setCellVerticalAlignment(loginPanel, HasAlignment.ALIGN_TOP);
		myPanel.setCellVerticalAlignment(logoPanel, HasAlignment.ALIGN_TOP);
		myPanel.setCellHorizontalAlignment(logoPanel, HasAlignment.ALIGN_LEFT);

		
		mainPanel.add(myPanel);

		mainPanel.add(tabPanel);

		mainPanel.add(vtp);

		// dock.add(headerGrid, DockPanel.NORTH);
		dock.add(mainPanel, DockPanel.CENTER);
		
		
		initWidget(dock);

	}

	public Widget getWidget() {
		return this;
	}

	public void addHeaderPanel(BaseView view) {
		//panel.add(view.getWidget(), DockPanel.NORTH);
	}
	
	public void addSearchPanel(BaseView view) {
		//panel.add(view.getWidget(), DockPanel.NORTH);
	}
	
	public void addMainPanel(BaseView view) {
		//panel.add(view.getWidget(), DockPanel.NORTH);
	}
	


	public void addContent(BaseView view) {
		removeContent();
		content = view.getWidget();
		//panel.add(view.getWidget(), DockPanel.CENTER);
	}

	public void removeContent() {
		if (content != null) {
			//panel.remove(content);
		}
	}

	public void addMenu(BaseView view) {
		// TODO Auto-generated method stub
		
	}

}
