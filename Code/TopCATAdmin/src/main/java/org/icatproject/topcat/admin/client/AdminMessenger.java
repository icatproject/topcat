package org.icatproject.topcat.admin.client;



import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class AdminMessenger extends Composite {

	private static AdminMessengerUiBinder uiBinder = GWT
			.create(AdminMessengerUiBinder.class);

	interface AdminMessengerUiBinder extends UiBinder<Widget, AdminMessenger> {
	}
	
	@UiField DateBox fromDate, toDate;
	@UiField ListBox fromTime, toTime;
	@UiField FlexTable f_table;
	@UiField CheckBox AllDayCheck;
	
	
	String[] timeValue = new String[] {"00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30","05:00","05:30","06:00","06:30","07:00","07:30","08:00","08:30"
			,"09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30"
			,"19:00","19:30","20:00","20:30","21:00","21:30","22:00","22:30","23:00","23:30"};
	
	
	
	
	public AdminMessenger() {
		initWidget(uiBinder.createAndBindUi(this));
		onMudlueLoad();
	}


	public void onMudlueLoad(){
		
		DateTimeFormat dateFormat= DateTimeFormat.getFormat("dd/MM/yyyy"); 
		
		fromDate.setFormat(new DateBox.DefaultFormat(dateFormat));
		toDate.setFormat(new DateBox.DefaultFormat(dateFormat));
		
		f_table.setText  (0, 0, "From: ");
		f_table.setWidget(0, 1, fromDate);
		f_table.setWidget(0, 2, fromTime);
		f_table.setWidget(0, 3, AllDayCheck);
		f_table.setText  (0, 4, "All day event");
		
		f_table.setText  (1, 0, "To: ");
		f_table.setWidget(1, 1, toDate);
		f_table.setWidget(1, 2, toTime);

		
		for(String values : timeValue){
			fromTime.addItem(values);
			toTime.addItem(values);
		}
		
		

		
	}

}
	

