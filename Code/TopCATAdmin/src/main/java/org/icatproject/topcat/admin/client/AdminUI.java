package org.icatproject.topcat.admin.client;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.topcat.admin.shared.Constants;
import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.client.service.DataServiceAsync;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Button;

public class AdminUI extends Composite {

	interface adminUIUiBinder extends UiBinder<Widget, AdminUI> {
	}


	private static adminUIUiBinder uiBinder = GWT.create(adminUIUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	private static final String MENU_ADD = "ADD";
	private static final String MENU_EDIT = "EDIT";
	private static int row;
	private static int column;
	
	ArrayList<Long> idArray  = new ArrayList<Long>();
	Constants headerNames = new Constants();
	
	@UiField
	FlexTable table, editMenu;
	@UiField
	VerticalPanel vPanel;
	@UiField
	Button btnSave, btnCancel, btnYes, btnNo, btnAdd;
	@UiField
	DialogBox dialogWindow, alertDialogBox;
	@UiField
	TextBox txtName, txtServerUrl, txtDownloadServiceUrl;
	@UiField
	ListBox txtPluginName, txtDownloadPluginName, txtVersion;
	@UiField
	HorizontalPanel hPanel0;
	
	
	
	
	public AdminUI() {

		initWidget(uiBinder.createAndBindUi(this));
		TableCall();
	}

	private void DisplayTable(List<TFacility> result) {
		int c, r = 1;
		
		
		// header section, columns width are equal to the second flextable
		table.getColumnFormatter().setWidth(0, "150px");
		table.getColumnFormatter().setWidth(1, "120px");
		table.getColumnFormatter().setWidth(2, "400px");
		table.getColumnFormatter().setWidth(3, "220px");
		table.getColumnFormatter().setWidth(4, "150px");
		table.getColumnFormatter().setWidth(5, "190px");

		table.setText(0, 0, Constants.NAME);
		table.setText(0, 1, Constants.VERSION);
		table.setText(0, 2, Constants.SERVER_URL);
		table.setText(0, 3, Constants.PLUGIN_NAME);
		table.setText(0, 4, Constants.DOWNLOAD_PLUGIN_NAME);
		table.setText(0, 5, Constants.DOWNLOAD_SERVICE_URL);

		// with the use of a second flextable the for loop display the content
		// of the TOPCAT_ICAT_SERVER
		for (TFacility facility : result) {
			c = 0;	

			table.setText(r, c++ , facility.getName());
			table.setText(r, c++, facility.getVersion());
			table.setText(r, c++, facility.getUrl());
			table.setText(r, c++, facility.getSearchPluginName());
			table.setText(r, c++, facility.getDownloadPluginName());
			table.setText(r++, c++, facility.getDownloadServiceUrl());
		}
		
		idArray.clear();
		idArray.add(null); // The first elementis populated with a null so that the index correspond with the rows
		
		for (TFacility facility : result) {
			idArray.add(facility.getId()); 
		}
		
		// counts the numbers of columns available and adds a delete and a edit
		// button
		Button[] deleteBtn = new Button[r];
		Button[] editBtn = new Button[r];

		for (int i = 1; i < r; i++) {

			deleteBtn[i] = new Button("delete");
			table.setWidget(i, 8, deleteBtn[i]);
			editBtn[i] = new Button("edit");
			table.setWidget(i, 7, editBtn[i]);
		}
	}

	private void handleAddNEditButton(String menu) {
	// EVERYTING IN HERE IS IN THE DIALOG BOX

		// LABELS FOR EACH ROW IN THE DIALOG BOX
		editMenu.setText(0, 0, Constants.NAME + ":");
		editMenu.setText(1, 0, Constants.VERSION + ":");
		editMenu.setText(2, 0, Constants.SERVER_URL + ":");
		editMenu.setText(3, 0, Constants.PLUGIN_NAME + ":");
		editMenu.setText(4, 0, Constants.DOWNLOAD_PLUGIN_NAME + ":");
		editMenu.setText(5, 0, Constants.DOWNLOAD_SERVICE_URL + ":");

		// CREATES A SPACE BETTWEEN THE LABELS AND THE WIDGETS
		editMenu.getColumnFormatter().setWidth(1, "5px");

		// ADDING WIDGETS TO THE FLEXTABLE
		editMenu.setWidget(0, 2, txtName);
		editMenu.setWidget(1, 2, txtVersion);
		editMenu.setWidget(2, 2, txtServerUrl);
		editMenu.setWidget(3, 2, txtPluginName);
		editMenu.setWidget(4, 2, txtDownloadPluginName);
		editMenu.setWidget(5, 2, txtDownloadServiceUrl);
		editMenu.setWidget(7, 0, hPanel0);

		// SETTING THE TEXT IN THE
		if (menu.equals(MENU_EDIT)) {
			txtName.setText(table.getText(row, 0));
			txtServerUrl.setText(table.getText(row, 2));
			txtDownloadServiceUrl.setText(table.getText(row, 5));
		}

		// THESE ARE THE ITEMS IN THE VERSION LISTBOX
		txtVersion.insertItem("v420", "v420", 0);
		if (menu.equals(MENU_ADD) || table.getText(row, 1).equals("v420")) {
			txtVersion.setItemSelected(0, true);
		}

		// THESE ARE THE ITEMS IN THE PLUGIN_NAME LISTBOX
		txtPluginName.insertItem("", "", 0);
		txtPluginName.insertItem("uk.ac.stfc.topcat.gwt.client.facility.ISISPlugin", 1);
		txtPluginName.insertItem("uk.ac.stfc.topcat.gwt.client.facility.DiamondFacilityPlugin",	2);
		

		if (menu.equals(MENU_ADD) || table.getText(row, 3).equals(null)) {
			txtPluginName.setItemSelected(0, true);
		} 
		else if (table.getText(row, 3).equals(txtPluginName.getItemText(1))) {
			txtPluginName.setItemSelected(1, true);
		} 
		else if (table.getText(row, 3).equals(txtPluginName.getItemText(2))) {
			txtPluginName.setItemSelected(2, true);
		}

		// THESE ARE THE ITEMS IN THE DOWNLOAD_PLUGIN_NAME LISTBOX
		txtDownloadPluginName.insertItem("", "", 0);
		txtDownloadPluginName.insertItem("IDS", 1);

		if (menu.equals(MENU_ADD) || table.getText(row, 4).equals(null)) {
			txtDownloadPluginName.setItemSelected(0, true);
		} 
		else if (table.getText(row, 4).equals(txtDownloadPluginName.getItemText(1))) {
			txtDownloadPluginName.setItemSelected(1, true);
		}
		
		if(menu.equals(MENU_EDIT)){
			btnSave.setText("update");
		}
		else{
			btnSave.setText("save");
		}
		
		dialogWindow.setText(menu + " MENU");
		dialogWindow.center();
		dialogWindow.setVisible(true);
	}

	private void handleDeleteButton() {
		alertDialogBox.setVisible(true);
		alertDialogBox.center();
	}

	private void TableCall() {
		
		AsyncCallback<List<TFacility>> callback = new AsyncCallback<List<TFacility>>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(List<TFacility> result) {
				DisplayTable(result);
				
			}
		};
		// make the call to the server
		System.out.println("LoginPanel: making call to DataService");
		dataService.getAllFacilities(callback);
	}

	private void addRowToTable() {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}
			public void onSuccess(String result) {
				TableCall();
				dialogWindow.hide();
			}
		};
			
		
		TFacility facility = new TFacility();
		EntitiySetter(facility, MENU_ADD);
		
		//make the call to the server
		System.out.println("LoginPanel: making call to DataService");
		dataService.addIcatServer(facility, callback);
	}

	private void updateRowInTable(){
		
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				TableCall();
				dialogWindow.hide();
			}
		};
			
		
		TFacility facility = new TFacility();
		EntitiySetter(facility, MENU_EDIT);
	
		//make the call to the server
		System.out.println("LoginPanel: making call to DataService");
		dataService.updateIcatServer(facility, callback);
	}
	
	private TFacility EntitiySetter(TFacility facility, String action){
		
		facility.setName(txtName.getText());
		facility.setVersion(txtVersion.getItemText(txtVersion.getSelectedIndex()));
		facility.setUrl(txtServerUrl.getText());
		facility.setSearchPluginName(txtPluginName.getItemText(txtPluginName.getSelectedIndex()));
		facility.setDownloadPluginName((txtDownloadPluginName.getItemText(txtDownloadPluginName.getSelectedIndex())));
		facility.setDownloadServiceUrl(txtDownloadServiceUrl.getText());
		
		if(action.equals(MENU_EDIT))
			facility.setId(idArray.get(row));
		
		return facility;
		
	}
	
	@UiHandler("table")
 	void HandleEditeNDelteButtonClick(ClickEvent e) {

		Cell cell = table.getCellForEvent(e);
		row = cell.getRowIndex();
		column = cell.getCellIndex();

		if (column == 7) {
			handleAddNEditButton(MENU_EDIT);
		}

		else if (column == 8)					
			handleDeleteButton();
	}

	@UiHandler("btnAdd")
	void HandleAddButtonClick(ClickEvent e) {

		handleAddNEditButton(MENU_ADD);
	}

	@UiHandler("btnCancel")
	void HandleCloseButtonClick(ClickEvent e) {
		ClearDialogBoxFields();
	}

	@UiHandler("btnNo")
	void HandleNoButton(ClickEvent e) {
		ClearDialogBoxFields();
	}

	@UiHandler("btnSave")
	void HandleSaveButton(ClickEvent e) {	

		if(btnSave.getText() == "save"){
			addRowToTable();
		}
		else if (btnSave.getText() == "update"){
			updateRowInTable();	
		}
		
		ClearDialogBoxFields();
	}
	
	@UiHandler("btnYes")
	void HandleYesButton(ClickEvent e){
//		DelteRowInTable();
	}
	
	private void ClearDialogBoxFields(){
		
		txtDownloadPluginName.clear();
		txtPluginName.clear();
		txtVersion.clear();
		btnSave.setText("save");
		txtDownloadServiceUrl.setText(null);
		txtName.setText(null);
		txtServerUrl.setText(null);
		dialogWindow.setVisible(false);
		dialogWindow.setModal(false);
		alertDialogBox.setVisible(false);
		alertDialogBox.setModal(false);
	}
}
