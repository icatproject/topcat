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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
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
	private static int table0Row, table0Column, table1Column, table1Row;
	private static long authID;

	public enum validationMessages {
		// TODO Come up with better messages
		facilityName(
				"* Please provide a valid Facility name to proceed ! e.g. ISIS"), icatURL(
				"* Please provide a valid ICat URL to proceed ! e.g. ....."), downloadServicURL(
				"* Please provide a valid Download Service URL to proceed ! e.g. ....");

		private validationMessages(final String text) {
			this.text = text;
		}

		private final String text;

		@Override
		public String toString() {
			return text;
		}
	}

	ArrayList<Long> idArray = new ArrayList<Long>();
	Constants headerNames = new Constants();

	@UiField
	FlexTable table0, table1, editMenu, editMenu1;
	@UiField
	VerticalPanel vPanel;
	@UiField
	Button btnSave, btnCancel, btnYes, btnNo, btnAdd, btnSave1, btnCancel1;
	@UiField
	DialogBox dialogWindow, alertDialogBox, authEditWindow;
	@UiField
	TextBox txtName, txtServerUrl, txtDownloadServiceUrl, txtAuthURL;
	@UiField
	ListBox txtPluginName, txtDownloadPluginName, txtVersion, txtAuthType,
			txtAuthPluginName;
	@UiField
	HorizontalPanel hPanel0, hPanel1;
	@UiField
	Label lbl1, lblAuth;

	public AdminUI() {
		initWidget(uiBinder.createAndBindUi(this));
		TableCall();
		// Window.alert(" "+ table0.getOffsetHeight( ));
	}

	private void DisplayTable(List<TFacility> result) {

		table0.removeAllRows();
		int c, r = 1;

		// header section, columns width are equal to the second flextable
		table0.getColumnFormatter().setWidth(0, "150px");
		table0.getColumnFormatter().setWidth(1, "80px");
		table0.getColumnFormatter().setWidth(2, "220px");
		table0.getColumnFormatter().setWidth(3, "220px");
		table0.getColumnFormatter().setWidth(4, "150px");
		table0.getColumnFormatter().setWidth(5, "190px");

		table0.setText(0, 0, Constants.NAME);
		table0.setText(0, 1, Constants.VERSION);
		table0.setText(0, 2, Constants.SERVER_URL);
		table0.setText(0, 3, Constants.PLUGIN_NAME);
		table0.setText(0, 4, Constants.DOWNLOAD_PLUGIN_NAME);
		table0.setText(0, 5, Constants.DOWNLOAD_SERVICE_URL);

		// with the use of a second flextable the for loop display the content
		// of the TOPCAT_ICAT_SERVER
		for (TFacility facility : result) {
			c = 0;

			table0.setText(r, c++, facility.getName());
			table0.setText(r, c++, facility.getVersion());
			table0.setText(r, c++, facility.getUrl());
			table0.setText(r, c++, facility.getSearchPluginName());
			table0.setText(r, c++, facility.getDownloadPluginName());
			table0.setText(r++, c++, facility.getDownloadServiceUrl());
		}

		idArray.clear();
		idArray.add(null); // The first elementis populated with a null so that
							// the index correspond with the rows

		for (TFacility facility : result) {
			idArray.add(facility.getId());
		}

		// counts the numbers of columns available and adds a delete and a edit
		// button
		Button[] deleteBtn = new Button[r];
		Button[] editBtn = new Button[r];
		Button[] pingICatBtn = new Button[r];
		Button[] pingDSBtn = new Button[r];
		Button[] authbtn = new Button[r];

		for (int i = 1; i < r; i++) {

			editBtn[i] = new Button("edit");
			table0.setWidget(i, 7, editBtn[i]);
			deleteBtn[i] = new Button("delete");
			table0.setWidget(i, 8, deleteBtn[i]);
			pingICatBtn[i] = new Button("ping Icat");
			table0.setWidget(i, 9, pingICatBtn[i]);
			pingDSBtn[i] = new Button("ping Download Service");
			table0.setWidget(i, 10, pingDSBtn[i]);
			authbtn[i] = new Button("Authentication Details");
			table0.setWidget(i, 11, authbtn[i]);
		}
	}

	private void DisplayAuthTable(TFacility result) {

		table1.getColumnFormatter().setWidth(0, "200px");
		table1.getColumnFormatter().setWidth(1, "200px");
		table1.getColumnFormatter().setWidth(2, "220px");

		table1.setText(0, 0, Constants.AUTHENTICATION_SERVICE_TYPE);
		table1.setText(0, 1, "Plugin Name");
		table1.setText(0, 2, Constants.AUTHENTICATION_URL);

		table1.setText(1, 0, result.getAuthenticationServiceType());
		table1.setText(1, 1, "Empty");
		table1.setText(1, 2, result.getAuthenticationServiceUrl());

		Button authEditButton = new Button("edit");
		Button authDeleteButton = new Button("delete");
		Button authPingButton = new Button("ping");
		table1.setWidget(1, 3, authEditButton);
		table1.setWidget(1, 4, authDeleteButton);
		table1.setWidget(1, 5, authPingButton);
		authID = result.getId();
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

		editMenu.getColumnFormatter().setWidth(0, "170px");
		editMenu.getColumnFormatter().setWidth(2, "355px");
		// CREATES A SPACE BETTWEEN THE LABELS AND THE WIDGETS
		editMenu.getColumnFormatter().setWidth(1, "5px");

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
		if (menu.equals(MENU_EDIT)) {
			txtName.setText(table0.getText(table0Row, 0));
			txtServerUrl.setText(table0.getText(table0Row, 2));
			txtDownloadServiceUrl.setText(table0.getText(table0Row, 5));
		}

		// THESE ARE THE ITEMS IN THE VERSION LISTBOX
		txtVersion.insertItem("v420", "v420", 0);
		if (menu.equals(MENU_ADD)
				|| table0.getText(table0Row, 1).trim().equals("v420")) {
			txtVersion.setItemSelected(0, true);
		}

		// THESE ARE THE ITEMS IN THE PLUGIN_NAME LISTBOX
		txtPluginName.insertItem("", "", 0);
		txtPluginName.insertItem(
				"uk.ac.stfc.topcat.gwt.client.facility.ISISPlugin", 1);
		txtPluginName.insertItem(
				"uk.ac.stfc.topcat.gwt.client.facility.DiamondFacilityPlugin",
				2);

		if (menu.equals(MENU_ADD)
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
		txtDownloadPluginName.insertItem("IDS", 1);

		if (menu.equals(MENU_ADD)
				|| table0.getText(table0Row, 4).trim().trim().equals(null)) {
			txtDownloadPluginName.setItemSelected(0, true);
		} else if (table0.getText(table0Row, 4).trim().trim()
				.equals(txtDownloadPluginName.getItemText(1))) {
			txtDownloadPluginName.setItemSelected(1, true);
		}

		if (menu.equals(MENU_EDIT)) {
			btnSave.setText("update");
		} else {
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
		dataService.getAllFacilities(callback);
	}

	private void addRowToTable(TFacility facility) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				TableCall();
				dialogWindow.hide();
			}
		};

		entitiySetter(facility, MENU_ADD);

		// make the call to the server
		dataService.addIcatServer(facility, callback);
	}

	private void updateRowInTable(TFacility facility, String edit) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				TableCall();
				dialogWindow.hide();
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
				TableCall();
				alertDialogBox.hide();
			}
		};

		// make the call to the server
		dataService.removeIcatServer(idArray.get(table0Row), callback);
	}

	private void rowCall() {
		AsyncCallback<TFacility> callback = new AsyncCallback<TFacility>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(TFacility result) {

				lblAuth.setText(table0.getText(table0Row, 0)
						+ " Authentication Details");
				lblAuth.setVisible(true);
				DisplayAuthTable(result);
			}
		};

		// make the call to the server
		dataService.rowCall(idArray.get(table0Row), callback);
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
			facility.setId(idArray.get(table0Row));

		return facility;

	}

	private void ClearDialogBoxFields() {

		editMenu.clearCell(0, 1);
		editMenu.clearCell(2, 1);
		editMenu.clearCell(5, 1);
		txtDownloadPluginName.clear();
		txtPluginName.clear();
		txtVersion.clear();
		txtAuthPluginName.clear();
		txtAuthType.clear();
		btnSave.setText("save");
		txtDownloadServiceUrl.setText(null);
		txtName.setText(null);
		txtServerUrl.setText(null);
		authEditWindow.setVisible(false);
		dialogWindow.setVisible(false);
		alertDialogBox.setVisible(false);
		authEditWindow.setModal(false);
		dialogWindow.setModal(false);
		alertDialogBox.setModal(false);
		lbl1.setText(null);
	}

	private boolean validationCheck() {
		boolean invalid = false;

		Image img = new Image();
		img.setUrl("/TopCATAdmin/src/main/resources/img/exclamation-icon.png");

		// TODO Find a way of showing the images

		if (txtName.getText().trim().isEmpty()) {
			lbl1.setText(validationMessages.facilityName.toString());
			editMenu.setWidget(0, 1, img);
			img.setVisibleRect(0, 0, 16, 16);
			invalid = true;
		}
		if (txtServerUrl.getText().trim().isEmpty()) {
			lbl1.setText(validationMessages.icatURL.toString());
			editMenu.setWidget(0, 1, img);
			img.setVisibleRect(0, 0, 16, 16);
			invalid = true;
		}
		if (txtDownloadServiceUrl.getText().trim().isEmpty()) {
			lbl1.setText(validationMessages.downloadServicURL.toString());
			editMenu.setWidget(0, 1, img);
			img.setVisibleRect(0, 0, 16, 16);
			invalid = true;
		}
		if (invalid == false)
			return true;
		else
			return false;
	}

	@UiHandler("table0")
	void handleTable0ButtonsClick(ClickEvent e) {

		Cell cell = table0.getCellForEvent(e);
		table0Row = cell.getRowIndex();
		table0Column = cell.getCellIndex();
		String url;

		switch (table0Column) {
		case 7:
			handleAddNEditButton(MENU_EDIT);
			break;
		case 8:
			handleDeleteButton();
			break;
		case 9:
			url = table0.getText(table0Row, 2);
			handlePingButtons(url, "Icat");
			break;
		case 10:
			url = table0.getText(table0Row, 5);
			handlePingButtons(url, "Downlaod Service");
			break;
		case 11:
			rowCall();
			break;
		}
	}

	@UiHandler("table1")
	void handleTable1Butonsclick(ClickEvent e) {
		Cell cell = table1.getCellForEvent(e);
		table1Row = cell.getRowIndex();
		table1Column = cell.getCellIndex();
		String url = table1.getText(table1Row, 2);

		switch (table1Column) {
		case 3:
			handleAuthEditButton();
			break;
		case 4:
			// handleAuthDeleteButton()
			Window.alert("Delete");
			break;
		case 5:
			handlePingButtons(url, "Authentication");
			break;
		}
	}

	private void handleAuthEditButton() {
		editMenu1.setText(0, 0, Constants.AUTHENTICATION_SERVICE_TYPE);
		editMenu1.setText(1, 0, "Plugin Name");
		editMenu1.setText(2, 0, Constants.AUTHENTICATION_URL);

		editMenu1.setWidget(0, 1, txtAuthType);
		editMenu1.setWidget(1, 1, txtAuthPluginName);
		editMenu1.setWidget(2, 1, txtAuthURL);

		// Initialising Plugin Name ListBox
		txtAuthPluginName.insertItem("User/Password", 0);
		txtAuthPluginName.insertItem("CAS", 1);
		txtAuthPluginName.insertItem("Anonymous", 2);

		if (table1.getText(table1Row, 1).trim()
				.equals(txtAuthPluginName.getItemText(0))) {
			txtAuthPluginName.setItemSelected(0, true);
		} else if (table1.getText(table1Row, 1).trim()
				.equals(txtAuthPluginName.getItemText(1))) {
			txtAuthPluginName.setItemSelected(1, true);
		} else if (table1.getText(table1Row, 1).trim()
				.equals(txtAuthPluginName.getItemText(2))) {
			txtAuthPluginName.setItemSelected(2, true);
		}

		// Initialising Plugin Name ListBox
		txtAuthType.insertItem("LDUP", 0);
		txtAuthType.insertItem("DB", 1);

		if (table1.getText(table1Row, 0).trim()
				.equals(txtAuthType.getItemText(0))) {
			txtAuthType.setItemSelected(0, true);
		} else if (table1.getText(table1Row, 0).trim()
				.equals(txtAuthType.getItemText(1))) {
			txtAuthType.setItemSelected(1, true);
		}

		txtAuthURL.setText(table1.getText(table1Row, 2).trim());

		authEditWindow.center();
		authEditWindow.setVisible(true);

	}

	@UiHandler("btnAdd")
	void handleAddButtonClick(ClickEvent e) {

		handleAddNEditButton(MENU_ADD);
	}

	@UiHandler(value = { "btnCancel", "btnCancel1" })
	void handleCancelButtonClick(ClickEvent e) {
		ClearDialogBoxFields();
	}

	@UiHandler("btnNo")
	void handleNoButton(ClickEvent e) {
		ClearDialogBoxFields();
	}

	@UiHandler("btnSave")
	void handleSaveButton(ClickEvent e) {
		TFacility facility = new TFacility();

		if (validationCheck() == true) {

			if (btnSave.getText() == "save") {
				addRowToTable(facility);
			} else if (btnSave.getText() == "update") {
				updateRowInTable(facility, MENU_EDIT);
			}

			ClearDialogBoxFields();
		}
	}

	@UiHandler("btnSave1")
	void handleSaveButton2(ClickEvent e) {
		TFacility facility = new TFacility();

		facility.setAuthenticationServiceType(txtAuthType
				.getItemText(txtAuthType.getSelectedIndex()));
		facility.setAuthenticationServiceUrl(txtAuthURL.getText().trim());
		facility.setId(authID);
		// TODO FOUND OUT ABOUT THE AUTHENTICATION PLUGIN NAME COLUMN IN THE
		// TOPCAT-ICAT-SERVER TABLE

		updateAuth(facility);
		rowCall();

		ClearDialogBoxFields();
	}

	private void updateAuth(TFacility facility) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				rowCall();
			}
		};

		authID = idArray.get(table0Row);
		// make the call to the server
		dataService.updateAuthDetails(facility, authID, callback);
	}

	@UiHandler("btnYes")
	void handleYesButton(ClickEvent e) {
		removeRowFromTable();
	}

	void handlePingButtons(String url, String urlSelection) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			public void onSuccess(String result) {
				Window.alert(result);
			}
		};
		// make the call to the server
		dataService.ping(url, urlSelection, callback);

	}
}
