package org.icatproject.topcat.admin.client;



import java.security.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.rmi.CORBA.Tie;


import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.client.service.DataServiceAsync;

import uk.ac.stfc.topcat.core.gwt.module.TMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * This is Topcat Admin Message Consol
 * <p>
 * 
 * @author Mr. Noris Nyamekye
 * @version 28-Jun-2013
 * @since 28-Jun-2013
 */

public class AdminMessenger extends Composite {

	private static AdminMessengerUiBinder uiBinder = GWT
			.create(AdminMessengerUiBinder.class);
	private DataServiceAsync dataService = GWT.create(DataService.class);
	
	interface AdminMessengerUiBinder extends UiBinder<Widget, AdminMessenger> {
	}
	
	@UiField DateBox toDate, fromDate;;
	@UiField ListBox fromTime, toTime;
	@UiField FlexTable table0, table1;
	@UiField TextBox txtMessage;
	@UiField CheckBox AllDayCheck;
	@UiField ScrollPanel scrollPanel;
	@UiField Button addBtn; 
	
	
	String[] timeValue = new String[] {"00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30","05:00","05:30","06:00","06:30","07:00","07:30","08:00","08:30"
			,"09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30"
			,"19:00","19:30","20:00","20:30","21:00","21:30","22:00","22:30","23:00","23:30"};
	
	
	public AdminMessenger() {
		initWidget(uiBinder.createAndBindUi(this));
		tableCall();
		
	}


	public void tableCall(){
		
		DateTimeFormat dateFormat= DateTimeFormat.getFormat("dd/MM/yyyy"); 
		
		fromDate.setFormat(new DateBox.DefaultFormat(dateFormat));
		toDate.setFormat(new DateBox.DefaultFormat(dateFormat));
		
		table0.setText  (0, 0, "From: ");
		table0.setWidget(0, 1, fromDate);
		table0.setWidget(0, 2, fromTime);
		table0.setWidget(0, 3, AllDayCheck);
		table0.setText  (0, 4, "All day event");
		
		
		table0.setText  (1, 0, "To: ");
		table0.setWidget(1, 1, toDate);
		table0.setWidget(1, 2, toTime);
		
		for(String values : timeValue){
			fromTime.addItem(values);
			toTime.addItem(values);
		}
		messageCall();	
	}

	private void generateCellTable(List<TMessages> result){
		int r = 1;
		
		table1.setText(0, 0, "ID");
		table1.getColumnFormatter().setWidth(0, "30px");
		table1.getColumnFormatter().setStyleName(0, "center");
		table1.setText(0, 1, "Message");
		table1.setText(0, 2, "Start Time");
		table1.setText(0, 3, "Stop Time");
		table1.getRowFormatter().setStyleName(0, "header");
		
		for (TMessages message : result){
			int c = 0;
			
			table1.setText(r, c++, message.getId().toString());
			table1.setText(r, c++, message.getmessage());
			table1.setText(r, c++, message.getStartTime().toString());
			table1.getRowFormatter().setStyleName(r, "table_style");
			table1.setText(r++, c++, message.getStopTime().toString());
			
		}
		
		Button[] deleteBtn = new Button[r];
		Button[] editBtn = new Button[r];
		Button[] enableBtn = new Button[r];

		for (int i = 1; i < r; i++) {

			editBtn[i] = new Button("edit");
			table1.setWidget(i, 4, editBtn[i]);
			editBtn[i].setTitle("Edit Message");
			editBtn[i].setWidth("50px");
			
			deleteBtn[i] = new Button("delete");
			table1.setWidget(i, 5, deleteBtn[i]);
			deleteBtn[i].setTitle("Delete Message");
			deleteBtn[i].setWidth("50px");
			
			enableBtn[i] = new Button("enable");
			table1.setWidget(i, 6, enableBtn[i]);
			enableBtn[i].setTitle("Edit the ICAT");
			enableBtn[i].setWidth("50px");
		}	
	}

//	private TMessages createQuery(TMessages message){
//		
//		message.setStartTime(fromDate.getDatePicker().getValue());
//		//Date addTime = message.getStartTime().setTime(toTime.getItemText(toTime.getSelectedIndex());
//		
//		message.setStopTime(toDate.getDatePicker().getValue());
//		
//		Window.alert("Hello");
//		txtMessage.setText(" " + message.getStartTime().toString()+"d");
//		Window.alert("Hello");
//

//		message.setStartTime(date);
		
//		string = toDate.getTextBox().getValue() + " " + toTime.getItemText(toTime.getSelectedIndex());
//		message.setStopTime(DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").parse(string));
				
//		message.setmessage(txtMessage.getText());

		
//		Window.alert(message.getmessage());
//		Window.alert(message.getStartTime().toString());
//		Window.alert(message.getStopTime().toString());
		
//		return message;
//	}
	
	private void messageCall(){
		AsyncCallback<List<TMessages>> callback = new AsyncCallback<List<TMessages>>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(List<TMessages> result) {
				generateCellTable(result);
			}
		};
		// make the call to the server
		dataService.getAllMessages(callback);
	}	
	
//	private void addNewMessage(TMessages message){
//		AsyncCallback<String> callback = new AsyncCallback<String>() {
//
//			@Override
//			public void onFailure(Throwable caught) {
//				Window.alert("Server error: " + caught.getMessage());
//			}
//
//			@Override
//			public void onSuccess(String result) {
//				tableCall();
//			}
//		};
//		// make the call to the server
//		dataService.addMessages(message, callback);
//	}	
	
	
	@UiHandler("addBtn")
	void handleAddButton(ClickEvent e){
		TMessages message = new TMessages();
//		createQuery(message);
//		addNewMessage(message);
	}

//	@UiHandler("fromDate")
//	void handleDateClick(ClickEvent e){
//		//new
//		
//	}
	
	
	

	
}

