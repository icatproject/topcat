package uk.topcat.web.client.ui;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import uk.topcat.web.client.language.LanguageConstants;

public class BrowseView extends DockPanel{
	
	private static BrowseView browser; 
	private LanguageConstants lang;
	
	public static BrowseView getBrowseView(){
		if(browser == null){
			browser = new BrowseView();
		}
		return browser;
	}
		
	private BrowseView()
	{
	
		lang = GWT.create(LanguageConstants.class);
		
		// Add description	
		HTML browseText = new HTML(lang.htab_browseDescription());		
		this.add(browseText, DockPanel.NORTH);

		
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
		
		this.add(browseTree, DockPanel.CENTER);
				
		VerticalPanel buttonPanel = new VerticalPanel();
	
		Button addToSel = new Button("Add to Selection");
		Button removeFromSel = new Button("Remove from Selection");
		buttonPanel.add(addToSel);
		buttonPanel.add(new HTML("&nbsp;"));
		buttonPanel.add(removeFromSel);
		this.add(buttonPanel,DockPanel.EAST);
	}
	
	public String toString(){
		return lang.htab_browseLabel();
	}
	
	@Override
	public boolean remove(Widget child) {
		// TODO Auto-generated method stub
		return false;
	}

	public Iterator<Widget> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
