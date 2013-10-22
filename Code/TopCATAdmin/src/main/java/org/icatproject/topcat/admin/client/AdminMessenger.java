package org.icatproject.topcat.admin.client;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.icatproject.topcat.admin.client.service.DataService;
import org.icatproject.topcat.admin.client.service.DataServiceAsync;

import uk.ac.stfc.topcat.core.gwt.module.TMessages;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.ListDataProvider;

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
	@UiField FlexTable messageInputTable;
	@UiField TextArea txtMessage;
	@UiField CheckBox allDayCheck;
	@UiField ScrollPanel scrollPanel;
	@UiField Button addBtn; 
	@UiField CellTable<TMessages> messageListTable;	
	
	String[] timeValue = new String[] {"00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30","05:00","05:30","06:00","06:30","07:00","07:30","08:00","08:30"
			,"09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30"
			,"19:00","19:30","20:00","20:30","21:00","21:30","22:00","22:30","23:00","23:30"};
	
	
	//create a data provider.
    ListDataProvider<TMessages> dataProvider;
	
	public AdminMessenger() {
		initWidget(uiBinder.createAndBindUi(this));
		
		//add input widgets
		tableCall();
		
		//create the message list
		createMessagesTable();
		
		//load messages
		loadMessages();
	}


    public void tableCall(){		
		DateTimeFormat dateFormat= DateTimeFormat.getFormat("dd/MM/yyyy"); 
		
		fromDate.setFormat(new DateBox.DefaultFormat(dateFormat));
		toDate.setFormat(new DateBox.DefaultFormat(dateFormat));		
		messageInputTable.setText(0, 0, "From: ");
		messageInputTable.setText(0, 1, "");
		messageInputTable.setWidget(0, 2, fromDate);
		messageInputTable.setWidget(0, 3, fromTime);
		messageInputTable.setWidget(0, 4, allDayCheck);
		messageInputTable.setText  (0, 5, "All day event");
		
		
		messageInputTable.setText(1, 0, "To: ");
		messageInputTable.setText(0, 1, "");
		messageInputTable.setWidget(1, 2, toDate);
		messageInputTable.setWidget(1, 3, toTime);
		
		messageInputTable.setText(2, 0, "Message:");
		messageInputTable.setText(2, 1, "");
		messageInputTable.setWidget(2, 2, txtMessage);
		messageInputTable.setWidget(3, 0, addBtn);
		
		//set colspan
		messageInputTable.getFlexCellFormatter().setColSpan(1, 4, 2);
		messageInputTable.getFlexCellFormatter().setColSpan(2, 2, 4);
		messageInputTable.getFlexCellFormatter().setColSpan(3, 0, 6);
		
		for(String values : timeValue){
			fromTime.addItem(values);
			toTime.addItem(values);
		}
			
		//format the text area
        setupTextArea();
	}
    
    //set initial size of text area
    private void setupTextArea() {      
        txtMessage.setCharacterWidth(80);
        txtMessage.setVisibleLines(5);
    }
    
    
    private void createMessagesTable() {
        //date time format for date cells
        DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd MMM yyyy HH:mm");
        
        //add id column
        TextColumn<TMessages> idColumn = new TextColumn<TMessages>() {
          @Override
          public String getValue(TMessages message) {
            return message.getId().toString();
          }
        };
        messageListTable.addColumn(idColumn, "Id");
        
        
        //add a start time column
        DateCell startTimeCell = new DateCell(dateFormat);
        Column<TMessages, Date> startTimeColumn = new Column<TMessages, Date>(startTimeCell) {
          @Override
          public Date getValue(TMessages message) {
            return message.getStartTime();
          }
        };
        messageListTable.addColumn(startTimeColumn, "Start Time");
        
        
        //add a end time column
        DateCell endTimeCell = new DateCell(dateFormat);
        Column<TMessages, Date> endTimeColumn = new Column<TMessages, Date>(endTimeCell) {
          @Override
          public Date getValue(TMessages message) {
            return message.getStartTime();
          }
        };
        messageListTable.addColumn(endTimeColumn, "End Time");
        
        //add message column
        TextColumn<TMessages> messageColumn = new TextColumn<TMessages>() {
          @Override
          public String getValue(TMessages message) {
            return message.getMessage();
          }
        };
        messageListTable.addColumn(messageColumn, "Message");
        
        List<HasCell<TMessages, ?>> cells = new LinkedList<HasCell<TMessages, ?>>();
        
        //create 2 action cells for edit and delete button
        cells.add(new ActionHasCell("Edit", new ActionCell.Delegate<TMessages>(){
            @Override
            public void execute(TMessages message) {
                Window.alert("You clicked " + message.getId());
            }
        }));
        
        cells.add(new ActionHasCell("Delete", new ActionCell.Delegate<TMessages>() {
            @Override
            public void execute(TMessages message) {
                Window.alert("You clicked " + message.getId());
            }
        }));
        
        
        //create the actions composite cell
        CompositeCell<TMessages> actionCell = new CompositeCell<TMessages>(cells);
        
        //add actions column
        Column<TMessages, TMessages> actionColumn = new Column<TMessages, TMessages>(actionCell) {
            @Override
            public TMessages getValue(TMessages message) {
              return message;
            }
        };
        messageListTable.addColumn(actionColumn, "Actions");
        
        //have to set to fixedlayout for setColumnWidth to work!!!
        messageListTable.setWidth("100%", true);
        messageListTable.setColumnWidth(idColumn, 5, Unit.PCT);
        messageListTable.setColumnWidth(startTimeColumn, 10, Unit.PCT);
        messageListTable.setColumnWidth(endTimeColumn, 10, Unit.PCT);
        messageListTable.setColumnWidth(messageColumn, 65, Unit.PCT);
        messageListTable.setColumnWidth(actionColumn, 10, Unit.PCT);
        
        
        //messageListTable.addColumnStyleName(1, "messageTableTimeColumn");
        //messageListTable.addColumnStyleName(2, "messageTableTimeColumn");
        //messageListTable.addColumnStyleName(3, "messageTableMessageColumn");
        //messageListTable.addColumnStyleName(4, "messageTableActionColumn");        
        
        //create data privider
        dataProvider = new ListDataProvider<TMessages>();
        
        //connect the table to the data provider.
        dataProvider.addDataDisplay(messageListTable);
    }
    
    /**
     * Refresh the celltable with the list of messages
     * 
     * @param result
     */
    private void refreshCellTable(List<TMessages> result) {
        List<TMessages> list = dataProvider.getList();
        list.clear();
        list.addAll(result);
        dataProvider.refresh();
    }    


	private TMessages createQuery(TMessages message){
		String fromDateString = "";
		String toDateString = "";
		
		//get date object from datepicker
	    Date fromDateObj = fromDate.getDatePicker().getValue();
	    Date toDateObj = toDate.getDatePicker().getValue();
	    
	    String fromTimeString = fromTime.getValue(fromTime.getSelectedIndex());
        String toTimeString = toTime.getValue(toTime.getSelectedIndex());
	    
	    if (fromDateObj != null) {
	        //get the date in yyyy-MM-dd format
	        fromDateString = DateTimeFormat.getFormat("dd/MM/yyyy").format(fromDateObj);	        	        
	        //append hours:minutes to date string 
	        fromDateString = fromDateString + " " + fromTimeString;
	        //get the date object
	        fromDateObj = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").parse(fromDateString);	        
	    }	    
	    
	    if (toDateObj != null) {
            //get the date in yyyy-MM-dd format
	        toDateString = DateTimeFormat.getFormat("dd/MM/yyyy").format(toDateObj);            
            //append hours:minutes to date string 
	        toDateString = toDateString + " " + toTimeString;
            //get the date object
            toDateObj = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").parse(toDateString);
        }
	    
	    //set message start time
		message.setStartTime(fromDateObj);
		//set message stop time
		message.setStopTime(toDateObj);
		//set message text			
		message.setMessage(txtMessage.getText());
		
		return message;
	}
	
	private void loadMessages(){
		AsyncCallback<List<TMessages>> callback = new AsyncCallback<List<TMessages>>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(List<TMessages> result) {				
			    refreshCellTable(result);
			}
		};
		// make the call to the server
		dataService.getAllMessages(callback);
	}	
	
	private void addNewMessage(final TMessages message){
		AsyncCallback<String> callback = new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
			    loadMessages();
			}
		};
		// make the call to the server
		dataService.addMessages(message, callback);
	}
	
	/**
	 * Validate whethere the message form is correctly filled
	 * 
	 * @return boolean
	 */
	private boolean validateMessageForm() {
	    boolean isValidFromDate = false;
	    boolean isValidToDate = false;        
        boolean isValidFromTime = false;
        boolean isValidToTime = false;
        boolean isValidMessage = false;
        
        //clear error ! image
        messageInputTable.setText(0, 1, "");
        messageInputTable.setText(1, 1, "");
        messageInputTable.setText(2, 1, "");
	    
        // validate from date is set
	    if (fromDate.getValue() != null) {
	        isValidFromDate = true;
        } else {
            messageInputTable.setWidget(0, 1, new Image("images/exclamation-icon.png"));
        }
	    
	    // validate to date is set 
	    if (toDate.getValue() != null) {
            isValidToDate = true;
        } else {
            messageInputTable.setWidget(1, 1, new Image("images/exclamation-icon.png"));
        }
	    
	    // validate from time has a value
	    if (! fromTime.getItemText(fromTime.getSelectedIndex()).isEmpty()) {
            isValidFromTime = true;
        } else {
            messageInputTable.setWidget(0, 1, new Image("images/exclamation-icon.png"));
        }
	    
	    // validate to time has a value
	    if (! toTime.getItemText(toTime.getSelectedIndex()).isEmpty()) {
            isValidToTime = true;
        } else {
            messageInputTable.setWidget(1, 1, new Image("images/exclamation-icon.png"));
        }
	    
	    // validate message has content and is less that 4000 characters
	    if (! txtMessage.getValue().isEmpty() && txtMessage.getValue().length() <= 4000) {
            isValidMessage = true;
        } else {
            messageInputTable.setWidget(2, 1, new Image("images/exclamation-icon.png"));
        }	    
	    
	    return isValidFromDate && isValidToDate && isValidFromTime && isValidToTime && isValidMessage;
	}
	
	
	@UiHandler("addBtn")
	void handleAddButton(ClickEvent e){
	    //validate the form
	    if (validateMessageForm()) {
    		//create new message
	        TMessages message = new TMessages();
	        
	        //set the message values from the form widgets
    		message = createQuery(message);
    		
    		//save the message to the server
    		addNewMessage(message);
	    }	
	}

//	@UiHandler("fromDate")
//	void handleDateClick(ClickEvent e){
//		//new
//		
//	}

	
}


class ActionHasCell implements HasCell<TMessages, TMessages> {
    private ActionCell<TMessages> cell;

    public ActionHasCell(String text, ActionCell.Delegate<TMessages> delegate) {
        cell = new ActionCell<TMessages>(text, delegate);
    }

    @Override
    public Cell<TMessages> getCell() {
        return cell;
    }

    @Override
    public FieldUpdater<TMessages, TMessages> getFieldUpdater() {
        return null;
    }

    @Override
    public TMessages getValue(TMessages object) {
        return object;
    }
}

