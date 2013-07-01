package org.icatproject.topcat.admin.client;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.client.service.DataServiceAsync;
import org.icatproject.topcat.admin.shared.Constants;

import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdminUI extends Composite {

	interface adminUIUiBinder extends UiBinder<Widget, AdminUI> {
	}

	private static adminUIUiBinder uiBinder = GWT.create(adminUIUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);

	private static final String MENU_ADD = "ADD";
	private static final String MENU_EDIT = "EDIT";
	private static int table0Row, table0Column, table1Column, table1Row;
	private static boolean table0Flag = false;
	private static boolean table1Flag = false;

	public enum validationMessages {
		FACILITY_NAME(
				"Please provide a valid Facility Name to proceed e.g. ISIS"), ICAT_URL(
				"Please provide a valid ICAT URL to proceed e.g. https://example.com"), DOWNLOAD_SERVICE_URL(
				"Please provide a valid Download Service URL to proceed e.g. https://example.com/IDS"), DISPLAY_NAME(
				"Please provide a valid Display Name to proceed e.g. \"Facility ID\""), FACILITY_NAME_DUPLICATION(
				"Facility Name already exists, please use a different Facility Name"), DISPLAY_NAME_DUPLICATION(
				"Display Name already exists, please use a different Display Name");

		private validationMessages(final String text) {
			this.text = text;
		}

		private final String text;

		@Override
		public String toString() {
			return text;
		}
	}

	// The array lists beneath, will contain the id numbers for the ICAT Server and Authentication Table 
	ArrayList<Long> idArrayTable0 = new ArrayList<Long>();
	ArrayList<Long> idArrayTable1 = new ArrayList<Long>();
	
	// The array lists beneath, will contain the numbers of associated authentications, to the ICATs in the ICAT Server Table 
	ArrayList<Integer> numberOfAuthDetails = new ArrayList<Integer>();
	Constants headerNames = new Constants();

	@UiField
	FlexTable table0, table1, tableHeader, editMenu, editAuthMenu;
	@UiField
	VerticalPanel vPanel;
	@UiField
	Button btnMenu, btnCancel, btnYes, btnNo, btnAdd, btnAuthMenu, btnCancel1,
			btnOk, btnAddAuth;
	@UiField
	DialogBox tableMenu, alertDialogBox, authMenu, PingDialogBox;
	@UiField
	TextBox txtName, txtServerUrl, txtDownloadServiceUrl, txtAuthURL,
			txtDisplayName;
	@UiField
	ListBox txtPluginName, txtDownloadPluginName, txtVersion, txtAuthType,
			txtAuthPluginName;
	@UiField
	HorizontalPanel hPanel0, hPanel2, hPanel4;

	@UiField
	Label lbl1, lbl2, lbl3, lblAuth;

	@UiField
	ScrollPanel scrollPanel;

	@UiField
	SplitLayoutPanel sPanel;

	@UiField
	HTMLPanel htmlPanel;

	@UiField
	SimplePanel simplePanel0;

	@UiField
	FlowPanel flowPanel;

	public AdminUI() {
		initWidget(uiBinder.createAndBindUi(this));
		OnModuleLoad();
	}

	private void OnModuleLoad() {
		tableCall();
	}

	private void displayTable(List<TFacility> result) {

		/**
		 * Recreates/Creates a new ICAT server table and populates it with ICATs
		 * and their information.
		 * 
		 * @param a
		 *            list of <code>TFacilities</code>
		 * @return
		 * @throws
		 */
		table0.removeAllRows();
		int c, r = 1;

		// Header Section - Icat Server Table
		table0.setText(0, 0, Constants.NAME);
		table0.setText(0, 1, Constants.VERSION);
		table0.setText(0, 2, Constants.SERVER_URL);
		table0.setText(0, 3, Constants.PLUGIN_NAME);
		table0.setText(0, 4, Constants.DOWNLOAD_PLUGIN_NAME);
		table0.setText(0, 5, Constants.DOWNLOAD_SERVICE_URL);
		table0.getRowFormatter().setStyleName(0, "header");

		// Sets the width for each column 
		table0.getColumnFormatter().setWidth(0, "110px");
		table0.getColumnFormatter().setWidth(1, "90px");
		table0.getColumnFormatter().setWidth(2, "190px");
		table0.getColumnFormatter().setWidth(3, "190px");
		table0.getColumnFormatter().setWidth(4, "190px");
		table0.getColumnFormatter().setWidth(5, "190px");

		idArrayTable0.clear();
		idArrayTable0.add(null);

		//Populates the Table 
		for (TFacility facility : result) {
			c = 0;

			table0.setText(r, c++, facility.getName());
			table0.setText(r, c++, facility.getVersion());
			table0.setText(r, c++, facility.getUrl());
			table0.setText(r, c++, facility.getSearchPluginName());
			table0.setText(r, c++, facility.getDownloadPluginName());
			table0.getRowFormatter().setStyleName(r, "table_style");
			table0.setText(r++, c++, facility.getDownloadServiceUrl());
			idArrayTable0.add(facility.getId());

		}
		
		// Check if a row is selected 
		if (!(table0Row == 0) && table0Flag) {
			handleRowSelection("table0");
			table0Flag = false;
		}

		// Adding an edit, delete, ping and show Authentication button to each end of the row except the Header row  
		
		Button[] deleteBtn = new Button[r];
		Button[] editBtn = new Button[r];
		Button[] pingICatBtn = new Button[r];
		Button[] pingDSBtn = new Button[r];
		Button[] authbtn = new Button[r];

		for (int i = 1; i < r; i++) {

			editBtn[i] = new Button("edit");
			table0.setWidget(i, 6, editBtn[i]);
			editBtn[i].setTitle("Edit the ICAT");
			deleteBtn[i] = new Button("delete");
			table0.setWidget(i, 7, deleteBtn[i]);
			deleteBtn[i].setTitle("Remove the ICAT");
			pingICatBtn[i] = new Button("ping ICAT");
			table0.setWidget(i, 8, pingICatBtn[i]);
			pingICatBtn[i].setTitle("Ping the ICAT URL");
			pingDSBtn[i] = new Button("ping D.S.");
			table0.setWidget(i, 9, pingDSBtn[i]);
			pingDSBtn[i].setTitle("Ping the Download Service URL");
			authbtn[i] = new Button("Show Auth. Details");
			table0.setWidget(i, 10, authbtn[i]);
			authbtn[i]
					.setTitle("Show the Authetication Details associated with this ICAT");

		}
		
		setSplitterPosition();
		checkIncompleteIcat();
	}

	private void displayAuthTable(List<TAuthentication> result) {
		/**
		 * Recreates/Creates a new Authentication Details table and populates it
		 * with an ICAT's associated authentication details
		 * 
		 * @param a
		 *            list of <code>TAuthentication</code>
		 * @return
		 * @throws
		 */

		int c, r = 1;
		table1.removeAllRows();

		table1.getColumnFormatter().setWidth(0, "150px");
		table1.getColumnFormatter().setWidth(1, "150px");
		table1.getColumnFormatter().setWidth(2, "200px");
		table1.getColumnFormatter().setWidth(3, "220px");
		table1.getRowFormatter().setStyleName(0, "header");

		table1.setText(0, 0, Constants.DISPLAY_NAME);
		table1.setText(0, 1, "Type");
		table1.setText(0, 2, "Plugin Name");
		table1.setText(0, 3, "URL");

		idArrayTable1.clear();
		idArrayTable1.add(null);

		for (TAuthentication autheticationDetails : result) {
			c = 0;
			table1.setText(r, c++, autheticationDetails.getDisplayName());
			table1.setText(r, c++, autheticationDetails.getType());
			table1.setText(r, c++, autheticationDetails.getPluginName());
			table1.getRowFormatter().setStyleName(r, "table_style");
			table1.setText(r++, c++, autheticationDetails.getUrl());
			idArrayTable1.add(autheticationDetails.getId());
		}

		if (!(table1Row == 0) && table1Flag) {
			handleRowSelection("table1");
			table1Flag = false;
		}

		if (result.size() > 0) {
			Button[] authEditButton = new Button[r];
			new Button("edit");
			Button[] authDeleteButton = new Button[r];
			new Button("delete");
			Button[] authPingButton = new Button[r];
			new Button("ping");

			for (int i = 1; i < r; i++) {
				authEditButton[i] = new Button("edit");
				table1.setWidget(i, 4, authEditButton[i]);
				authDeleteButton[i] = new Button("delete");
				table1.setWidget(i, 5, authDeleteButton[i]);
				authPingButton[i] = new Button("ping");
				table1.setWidget(i, 6, authPingButton[i]);
			}
		}
		btnAddAuth.setVisible(true);

		lblAuth.setText("");
		if (table0.isCellPresent(table0Row, 0)) {
			lblAuth.setText(table0.getText(table0Row, 0)
					+ " Authentication Details");
			lblAuth.setVisible(true);
		} else {
			lblAuth.setText("Authentication Details");
		}

	}

	private void inititialiseMenu(String menuType) {
		/**
		 * Initialise the the Add/Edit Menu for the ICAT Server Table and sets
		 * it Visible
		 * 
		 * @param menuType
		 * @return
		 * @throws
		 */

		if (menuType.equals(MENU_EDIT)) {
			handleRowSelection("table0");
		}
		// EVERYTING IN HERE IS IN THE DIALOG BOX

		// LABELS FOR EACH ROW IN THE DIALOG BOX
		editMenu.setText(0, 0, Constants.NAME + ":");
		editMenu.setText(1, 0, Constants.VERSION + ":");
		editMenu.setText(2, 0, Constants.SERVER_URL + ":");
		editMenu.setText(3, 0, Constants.PLUGIN_NAME + ":");
		editMenu.setText(4, 0, Constants.DOWNLOAD_PLUGIN_NAME + ":");
		editMenu.setText(5, 0, Constants.DOWNLOAD_SERVICE_URL + ":");

		editMenu.getColumnFormatter().setWidth(0, "170px");
		editMenu.getColumnFormatter().setWidth(1, "5px");
		editMenu.getColumnFormatter().setWidth(2, "355px");
		// CREATES A SPACE BETTWEEN THE LABELS AND THE WIDGETS

		// ADDING WIDGETS TO THE FLEXTABLE
		editMenu.setWidget(0, 2, txtName);
		editMenu.setWidget(1, 2, txtVersion);
		editMenu.setWidget(2, 2, txtServerUrl);
		editMenu.setWidget(3, 2, txtPluginName);
		editMenu.setWidget(4, 2, txtDownloadPluginName);
		editMenu.setWidget(5, 2, txtDownloadServiceUrl);
		editMenu.setWidget(6, 2, lbl1);
		editMenu.setWidget(6, 0, hPanel0);

		// SETTING THE TEXT IN THE
		if (menuType.equals(MENU_EDIT)) {
			txtName.setText(table0.getText(table0Row, 0));
			txtServerUrl.setText(table0.getText(table0Row, 2));
			txtDownloadServiceUrl.setText(table0.getText(table0Row, 5));
		}

		// THESE ARE THE ITEMS IN THE VERSION LISTBOX
		txtVersion.insertItem("v42", "v42", 0);
		if (menuType.equals(MENU_ADD)
				|| table0.getText(table0Row, 1).trim().equals("v42")) {
			txtVersion.setItemSelected(0, true);
		}

		// THESE ARE THE ITEMS IN THE PLUGIN_NAME LISTBOX
		txtPluginName.insertItem("", "", 0);
		txtPluginName.insertItem(
				"uk.ac.stfc.topcat.gwt.client.facility.ISISPlugin", 1);
		txtPluginName.insertItem(
				"uk.ac.stfc.topcat.gwt.client.facility.DiamondFacilityPlugin",
				2);

		if (menuType.equals(MENU_ADD)
				|| table0.getText(table0Row, 3).trim().equals(null)) {
			txtPluginName.setItemSelected(0, true);
		} else if (table0.getText(table0Row, 3).trim()
				.equals(txtPluginName.getItemText(1))) {
			txtPluginName.setItemSelected(1, true);
		} else if (table0.getText(table0Row, 3).trim()
				.equals(txtPluginName.getItemText(2))) {
			txtPluginName.setItemSelected(2, true);
		}

		// THESE ARE THE ITEMS IN THE DOWNLOAD_PLUGIN_NAME LISTBOX
		txtDownloadPluginName.insertItem("", "", 0);
		txtDownloadPluginName.insertItem("ids", 1);

		if (menuType.equals(MENU_ADD)
				|| table0.getText(table0Row, 4).trim().equals(null)) {
			txtDownloadPluginName.setItemSelected(0, true);
		} else if (table0.getText(table0Row, 4).trim().trim()
				.equals(txtDownloadPluginName.getItemText(1))) {
			txtDownloadPluginName.setItemSelected(1, true);
		}

		if (menuType.equals(MENU_EDIT)) {
			btnMenu.setText("update");
		} else {
			btnMenu.setText("save");
		}

		tableMenu.setText(menuType + " MENU");
		tableMenu.center();
		tableMenu.setVisible(true);

	}

	private void initialiseAuthMenu(String menuType) {
		/**
		 * Initialise the the Add/Edit Menu for the Authentication Details Table
		 * and sets it Visible
		 * 
		 * @param menuType
		 * @return
		 * @throws
		 */

		if (menuType.equals(MENU_EDIT)) {
			handleRowSelection("table1");
		}

		editAuthMenu.setText(0, 0, Constants.DISPLAY_NAME);
		editAuthMenu.setText(1, 0, Constants.AUTHENTICATION_SERVICE_TYPE);
		editAuthMenu.setText(3, 0, Constants.AUTHENTICATION_URL);

		editAuthMenu.setWidget(0, 2, txtDisplayName);
		editAuthMenu.setWidget(1, 2, txtAuthType);
		editAuthMenu.setWidget(3, 2, txtAuthURL);
		editAuthMenu.setWidget(4, 2, lbl3);
		editAuthMenu.setWidget(4, 0, hPanel2);

		// Initialising Plugin Name ListBox
		txtAuthPluginName
				.insertItem(
						"uk.ac.stfc.topcat.gwt.client.authentication.AnonymousAuthenticationPlugin",
						0);
		txtAuthPluginName
				.insertItem(
						"uk.ac.stfc.topcat.gwt.client.authentication.CASAuthenticationPlugin",
						1);
		txtAuthPluginName
				.insertItem(
						"uk.ac.stfc.topcat.gwt.client.authentication.DefaultAuthenticationPlugin",
						2);

		// Initialising Typ ListBox
		txtAuthType.insertItem("anonymous", 0);
		txtAuthType.insertItem("cas", 1);
		txtAuthType.insertItem("db", 2);
		txtAuthType.insertItem("ldap", 3);
		txtAuthType.insertItem("uows", 4);

		if (menuType.equals(MENU_EDIT)) {

			txtDisplayName.setText(table1.getText(table1Row, 0).trim());

			if (table1.getText(table1Row, 1).trim()
					.equals(txtAuthType.getItemText(0))
					&& menuType.equals(MENU_EDIT)) {
				txtAuthType.setItemSelected(0, true);

			} else if (table1.getText(table1Row, 1).trim()
					.equals(txtAuthType.getItemText(1))
					&& menuType.equals(MENU_EDIT)) {
				txtAuthType.setItemSelected(1, true);

			} else if (table1.getText(table1Row, 1).trim()
					.equals(txtAuthType.getItemText(2))
					&& menuType.equals(MENU_EDIT)) {
				txtAuthType.setItemSelected(2, true);

			} else if (table1.getText(table1Row, 1).trim()
					.equals(txtAuthType.getItemText(3))
					&& menuType.equals(MENU_EDIT)) {
				txtAuthType.setItemSelected(3, true);

			} else if (table1.getText(table1Row, 1).trim()
					.equals(txtAuthType.getItemText(4))
					&& menuType.equals(MENU_EDIT)) {
				txtAuthType.setItemSelected(4, true);
			}

			if (menuType.equals(MENU_EDIT)) {
				txtAuthURL.setText(table1.getText(table1Row, 3).trim());
			}
		}

		if (menuType.equals(MENU_EDIT)) {
			btnAuthMenu.setText("update");
		} else {
			btnAuthMenu.setText("save");
		}

		authMenu.setText("AUTHENTICATION " + menuType + " MENU");
		authMenu.center();
		authMenu.setVisible(true);
	}

	private TFacility entitiySetter(TFacility facility, String action) {

		facility.setName(txtName.getText());
		facility.setVersion(txtVersion.getItemText(txtVersion
				.getSelectedIndex()));
		facility.setUrl(txtServerUrl.getText());
		facility.setSearchPluginName(txtPluginName.getItemText(txtPluginName
				.getSelectedIndex()));
		facility.setDownloadPluginName((txtDownloadPluginName
				.getItemText(txtDownloadPluginName.getSelectedIndex())));
		facility.setDownloadServiceUrl(txtDownloadServiceUrl.getText());

		if (action.equals(MENU_EDIT) && (facility.getId() == null))
			facility.setId(idArrayTable0.get(table0Row));

		return facility;

	}

	private void clearMenuBox() {

		editMenu.clearCell(0, 1);
		editMenu.clearCell(2, 1);
		editMenu.clearCell(5, 1);
		btnMenu.setText("save");
		txtName.setText(null);
		txtServerUrl.setText(null);
		txtDownloadServiceUrl.setText(null);
		txtPluginName.clear();
		txtDownloadPluginName.clear();
		txtVersion.clear();
		tableMenu.setModal(false);
		tableMenu.setVisible(false);
		lbl1.setText(null);

		alertDialogBox.setModal(false);
		alertDialogBox.setVisible(false);
	}

	private void clearAuthMenu() {
		editAuthMenu.clearCell(0, 1);
		btnAuthMenu.setText("save");
		txtAuthURL.setText(null);
		txtDisplayName.setText(null);
		txtAuthPluginName.clear();
		txtAuthType.clear();
		authMenu.setModal(false);
		authMenu.setVisible(false);
		lbl3.setText(null);
	}

	private boolean validationCheck() {
		boolean invalidName = false;
		boolean invalidSUrl = false;
		boolean invalidDSUrl = false;

		editMenu.clearCell(0, 1);
		editMenu.clearCell(2, 1);
		editMenu.clearCell(5, 1);

		if (txtName.getText().trim().isEmpty()) {
			lbl1.setText(validationMessages.FACILITY_NAME.toString());
			editMenu.setWidget(0, 1, new Image("images/exclamation-icon.png"));
			txtName.setFocus(true);
			invalidName = true;
		}

		if (btnMenu.getText() == "save") {
			for (int i = 1; i < table0.getRowCount(); i++) {
				if (table0.getText(i, 0).equals(txtName.getText())) {
					lbl1.setText(validationMessages.FACILITY_NAME_DUPLICATION
							.toString());
					editMenu.setWidget(0, 1, new Image(
							"images/exclamation-icon.png"));
					txtName.setFocus(true);
					invalidName = true;
				}
			}
		}

		if (txtServerUrl.getText().trim().isEmpty()) {
			if (invalidName != true) {
				lbl1.setText(validationMessages.ICAT_URL.toString());
				txtServerUrl.setFocus(true);
			}
			editMenu.setWidget(2, 1, new Image("images/exclamation-icon.png"));
			invalidSUrl = true;
		}
		if (txtDownloadServiceUrl.getText().trim().isEmpty()) {
			if ((invalidName || invalidSUrl) != true) {
				lbl1.setText(validationMessages.DOWNLOAD_SERVICE_URL.toString());
				txtDownloadServiceUrl.setFocus(true);
			}
			editMenu.setWidget(5, 1, new Image("images/exclamation-icon.png"));
			invalidDSUrl = true;
		}
		if ((invalidName || invalidSUrl || invalidDSUrl) == false)
			return true;
		else
			return false;
	}

	private boolean AuthMenuValidation() {

		editAuthMenu.clearCell(0, 1);

		if (txtDisplayName.getText().trim().isEmpty()) {
			lbl3.setText(validationMessages.DISPLAY_NAME.toString());
			editAuthMenu.setWidget(0, 1, new Image(
					"images/exclamation-icon.png"));
			return false;
		} else if (btnAuthMenu.getText() == "save") {
			for (int i = 1; i < table1.getRowCount(); i++) {
				if (table1.getText(i, 0).equals(txtDisplayName.getText())) {
					lbl3.setText(validationMessages.DISPLAY_NAME_DUPLICATION
							.toString());
					editAuthMenu.setWidget(0, 1, new Image(
							"images/exclamation-icon.png"));
					txtDisplayName.setFocus(true);
					return false;
				}
			}
		}

		return true;
	}

	void setSplitterPosition() {
		// Check the height of the panel that contains the ICAT Server Table and sets the Verticalsplitter right beneath it with a 25px space
		long height = htmlPanel.getOffsetHeight() + 25;
		sPanel.clear();
		sPanel.addEast(flowPanel, 270);
		sPanel.addNorth(scrollPanel, height);
		sPanel.add(simplePanel0);
	}

	private void handleInCompleteICat() {

		for (int i = 1; i < numberOfAuthDetails.size(); i++) {
			if (numberOfAuthDetails.get(i) == 0) {
				table0.getRowFormatter().removeStyleName(i, "table_style");
				table0.getRowFormatter().addStyleName(i, "incompleteSetting");

			}

		}

	}

	/*
	 * ################### Server Calls ##################F
	 */

	private void updateAuth(TAuthentication authentication) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				authTableCall();
				authMenu.hide();
			}
		};
		// make the call to the server
		dataService.updateAuthDetails(authentication,
				idArrayTable1.get(table1Row), callback);
	}

	private void tableCall() {
		AsyncCallback<List<TFacility>> callback = new AsyncCallback<List<TFacility>>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(List<TFacility> result) {
				displayTable(result);
				clearAuthMenu();
			}
		};
		// make the call to the server
		dataService.getAllFacilities(callback);
	}

	private void addRowToTable(TFacility facility) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				tableCall();

				tableMenu.hide();
			}
		};

		entitiySetter(facility, MENU_ADD);

		// make the call to the server
		dataService.addIcatServer(facility, callback);
	}

	private void addRowToAuthTable(TAuthentication authentication) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				authTableCall();
				authMenu.hide();
				checkIncompleteIcat();
			}
		};

		// make the call to the server
		dataService.addAuthDetails(authentication, callback);
	}

	private void updateRowInTable(TFacility facility, String edit) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				tableCall();
				tableMenu.hide();
			}
		};
		entitiySetter(facility, edit);

		// make the call to the server
		dataService.updateIcatServer(facility, callback);
	}

	private void removeRowFromTable() {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				tableCall();
				alertDialogBox.hide();
			}
		};

		// make the call to the server
		dataService.removeIcatServer(idArrayTable0.get(table0Row),
				table0.getText(table0Row, 0), callback);
	}

	private void removeRowFromAuthTable() {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				authTableCall();
				alertDialogBox.hide();
			}
		};

		// make the call to the server
		dataService.removeAuthenticationDetails(idArrayTable1.get(table1Row),
				callback);
	}

	private void authTableCall() {
		AsyncCallback<List<TAuthentication>> callback = new AsyncCallback<List<TAuthentication>>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(List<TAuthentication> result) {
				displayAuthTable(result);
			}
		};

		// make the call to the server
		dataService.authDetailsCall(table0.getText(table0Row, 0), callback);
	}

	private void handlePingButtonClick(String url, final String urlSelection) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				handlePingButtonEvent(result, urlSelection);
			}
		};
		// make the call to the server
		dataService.ping(url, urlSelection, callback);

	}

	private void checkIncompleteIcat() {
		// checks 
		AsyncCallback<ArrayList<Integer>> callback = new AsyncCallback<ArrayList<Integer>>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(ArrayList<Integer> result) {
				numberOfAuthDetails = result;
				handleInCompleteICat();
			}
		};

		// make the call to the server
		dataService.authCount(callback);
	}

	/*
	 * ################### Event handlers ##################
	 */

	@UiHandler("table0")
	void handleTable0ButtonsClick(ClickEvent e) {

		Cell cell = table0.getCellForEvent(e);
		table0Row = cell.getRowIndex();
		table0Column = cell.getCellIndex();
		String url;

		handleRowSelection("table0");
		authTableCall();

		switch (table0Column) {
		case 6:
			inititialiseMenu(MENU_EDIT);
			table0Flag = true;
			break;
		case 7:
			handleDeleteButtonEvent("ICAT_TABLE");
			break;
		case 8:
			handleRowSelection("table0");
			url = table0.getText(table0Row, 2);
			handlePingButtonClick(url, "ICAT");
			break;
		case 9:
			handleRowSelection("table0");
			url = table0.getText(table0Row, 5);
			handlePingButtonClick(url, "Download Service");
			break;
		case 10:
			handleRowSelection("table0");
			authTableCall();
			break;
		}
	}

	@UiHandler("table1")
	void handleTable1Butonsclick(ClickEvent e) {
		Cell cell = table1.getCellForEvent(e);
		table1Row = cell.getRowIndex();
		table1Column = cell.getCellIndex();

		String url = table1.getText(table1Row, 3);

		// Window.alert("Row: " + table1Row + " Column: " + table1Column);
		handleRowSelection("table1");

		switch (table1Column) {
		case 4:
			initialiseAuthMenu(MENU_EDIT);
			table1Flag = true;
			break;
		case 5:
			handleDeleteButtonEvent("AUTH_TABLE");
			break;
		case 6:
			handleRowSelection("table1");
			handlePingButtonClick(url, "Authentication Service");
			break;
		}
	}

	void handleRowUnselection(String table) {
		if (table == "table0") {
			for (int i = 1; i < table0.getRowCount(); i++) {

				if (numberOfAuthDetails.get(i) != 0) {
					table0.getRowFormatter().setStyleName(i, "table_style");
				} else {
					table0.getRowFormatter().setStyleName(i,
							"incompleteSetting");
				}
			}
		} else {
			for (int i = 1; i < table1.getRowCount(); i++) {
				table1.getRowFormatter().setStyleName(i, "table_style");
			}
		}
	}

	void handleRowSelection(String table) {
		if (table == "table0") {
			handleRowUnselection("table0");
			table0.getRowFormatter().setStyleName(table0Row, "selected");

		} else {
			handleRowUnselection("table1");
			table1.getRowFormatter().setStyleName(table1Row, "selected");
		}

	}

	@UiHandler("btnAdd")
	void handleAddButtonClick(ClickEvent e) {
		handleRowUnselection("table0");
		inititialiseMenu(MENU_ADD);
		txtName.setFocus(true);

	}

	@UiHandler("btnCancel")
	void handleCancelButtonClick(ClickEvent e) {
		clearMenuBox();
		handleRowUnselection("table0");
	}

	@UiHandler("btnCancel1")
	void handleAuthCancelButton(ClickEvent e) {
		clearAuthMenu();
		handleRowUnselection("table1");
	}

	@UiHandler("btnAuthMenu")
	void handleAuthSaveButton(ClickEvent e) {

		TAuthentication authentication = new TAuthentication();

		if (txtAuthType.getItemText(txtAuthType.getSelectedIndex()) == "anonymous") {
			authentication.setPluginName(txtAuthPluginName.getItemText(0));
		} else if (txtAuthType.getItemText(txtAuthType.getSelectedIndex()) == "cas") {
			authentication.setPluginName(txtAuthPluginName.getItemText(1));
		} else if (txtAuthType.getItemText(txtAuthType.getSelectedIndex()) == "db") {
			authentication.setPluginName(txtAuthPluginName.getItemText(2));
		} else if (txtAuthType.getItemText(txtAuthType.getSelectedIndex()) == "ldap") {
			authentication.setPluginName(txtAuthPluginName.getItemText(2));
		} else if (txtAuthType.getItemText(txtAuthType.getSelectedIndex()) == "uows") {
			authentication.setPluginName(txtAuthPluginName.getItemText(2));
		}

		authentication.setType(txtAuthType.getItemText(txtAuthType
				.getSelectedIndex()));
		authentication.setDisplayName(txtDisplayName.getText());
		authentication.setUrl(txtAuthURL.getText().trim());
		authentication.setId(idArrayTable0.get(table0Row));

		if (AuthMenuValidation() == true) {
			if (btnAuthMenu.getText() == "save") {
				addRowToAuthTable(authentication);
			} else if (btnAuthMenu.getText() == "update") {
				updateAuth(authentication);
			}
			clearAuthMenu();
		}
	}

	@UiHandler("btnYes")
	void handleYesButton(ClickEvent e) {
		String table = alertDialogBox.getTitle();

		if (table == "AUTH_TABLE") {
			removeRowFromAuthTable();
		} else if (table == "ICAT_TABLE") {
			removeRowFromTable();
		}
		checkIncompleteIcat();
	}

	@UiHandler("btnNo")
	void handleNoButton(ClickEvent e) {
		alertDialogBox.setVisible(false);
		alertDialogBox.setModal(false);

	}

	@UiHandler("btnMenu")
	void handleSaveUpdateButton(ClickEvent e) {
		TFacility facility = new TFacility();

		if (validationCheck() == true) {

			if (btnMenu.getText() == "save") {
				addRowToTable(facility);
			} else if (btnMenu.getText() == "update") {
				updateRowInTable(facility, MENU_EDIT);
			}
			clearMenuBox();
		}
	}

	@UiHandler("btnOk")
	void handleOkButtonClick(ClickEvent e) {
		PingDialogBox.setModal(false);
		PingDialogBox.setVisible(false);
	}

	@UiHandler("btnAddAuth")
	void handleAuthAddButtonClick(ClickEvent e) {
		handleRowUnselection("table1");
		initialiseAuthMenu(MENU_ADD);
		txtDisplayName.setFocus(true);
	}

	void handleDeleteButtonEvent(String table) {
		if (table == "AUTH_TABLE") {
			handleRowSelection("table1");
		} else {
			handleRowSelection("table0");
		}

		alertDialogBox.setTitle(table);
		alertDialogBox.setVisible(true);
		alertDialogBox.center();
	}

	void handlePingButtonEvent(String result, String urlSelection) {

		if (result == "successfully") {
			PingDialogBox.setText("Ping Status");
		} else {
			PingDialogBox.setText("Warning");
		}
		lbl2.setText(urlSelection + " pinged " + result);

		if (lbl2.getElement().getClientWidth() > 350) {
			PingDialogBox.setWidth("350px");
		} else {
			PingDialogBox.setWidth("");
		}

		PingDialogBox.center();
		PingDialogBox.setVisible(true);
	}

	void selectNewEntry() {
		table0Row = table0.getRowCount();
		table0.getRowFormatter().setStyleName(table0Row, "selected");
		handleRowSelection("table0");
		authTableCall();

		//
	}
}
