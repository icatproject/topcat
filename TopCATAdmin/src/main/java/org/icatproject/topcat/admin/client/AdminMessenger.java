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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
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
	@UiField FlexTable messageForm;
	@UiField TextArea txtMessage;
	@UiField CheckBox allDayCheck;
	@UiField ScrollPanel scrollPanel;
	@UiField Button addBtn; 
	@UiField CellTable<TMessages> messageListTable;
	@UiField FlowPanel errorPanel;
	
	String[] timeValue = new String[] {"00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30","05:00","05:30","06:00","06:30","07:00","07:30","08:00","08:30"
			,"09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30"
			,"19:00","19:30","20:00","20:30","21:00","21:30","22:00","22:30","23:00","23:30", "23:59"};
	
	
	//create a data provider.
    ListDataProvider<TMessages> dataProvider;
    
    //validation
    public final static int MAX_MESSAGE_LENGTH = 255;
    private TMessages tMessage = new TMessages();
    
    public final class validationMessage {
        public static final String START_TIME_REQUIRED = "Start time is required";
        public static final String END_TIME_REQUIRED = "End time is required"; 
        public static final String START_BEFORE_END_TIME = "Start time must be before the end time"; 
        public static final String MESSAGE_LENGTH = "Message must be less than " + MAX_MESSAGE_LENGTH + " characters "; 
        public static final String MESSAGE_REQUIRED = "Message is required"; 
        public static final String TIME_OVERLAP_EXISTING = "New message overlaps an existing message";
    }
    
	
	public AdminMessenger() {
		initWidget(uiBinder.createAndBindUi(this));
		
		//add input widgets
		createMessageForm();
		
		//create the message list
		createMessagesTable();
		
		//load messages
		loadMessages();
	}


    public void createMessageForm(){		
		DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd/MM/yyyy"); 
		
		fromDate.setFormat(new DateBox.DefaultFormat(dateFormat));
		toDate.setFormat(new DateBox.DefaultFormat(dateFormat));		
		
		messageForm.setWidget(0, 2, errorPanel);
		
		messageForm.setText(1, 0, "Start Time: ");
		messageForm.setText(1, 1, "");
		messageForm.setWidget(1, 2, fromDate);
		messageForm.setWidget(1, 3, fromTime);
		messageForm.setWidget(1, 4, allDayCheck);
		messageForm.setText  (1, 5, "All day event");
		
		messageForm.setText(2, 0, "End Time: ");
		messageForm.setText(2, 1, "");
		messageForm.setWidget(2, 2, toDate);
		messageForm.setWidget(2, 3, toTime);
		
		messageForm.setText(3, 0, "Message:");
		messageForm.setText(3, 1, "");
		messageForm.setWidget(3, 2, txtMessage);
		
		messageForm.setWidget(4, 0, addBtn);		
		
		//set colspan
		messageForm.getFlexCellFormatter().setColSpan(0, 2, 4);
		messageForm.getFlexCellFormatter().setColSpan(2, 4, 2);
		messageForm.getFlexCellFormatter().setColSpan(3, 2, 4);				
        messageForm.getFlexCellFormatter().setColSpan(4, 0, 6);
		
		for(String values : timeValue){
			fromTime.addItem(values);
			toTime.addItem(values);
		}
	}
    
    
    
    
    private void createMessagesTable() {
        //date time format for date cells
        DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd MMM yyyy HH:mm");
        
        
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
            return message.getStopTime();
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
        /*
        cells.add(new ActionHasCell("Edit", new ActionCell.Delegate<TMessages>(){
            @Override
            public void execute(TMessages message) {
                Window.alert("To be implemented");
            }
        }));
        */
        
        cells.add(new ActionHasCell("Delete", new ActionCell.Delegate<TMessages>() {
            @Override
            public void execute(TMessages message) {
                deleteMessage(message);
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
        messageListTable.addColumn(actionColumn, "Action");
        
        //have to set a width for setColumnWidth to work!!!
        messageListTable.setWidth("100%", true);        
        messageListTable.setColumnWidth(startTimeColumn, 15, Unit.PCT);
        messageListTable.setColumnWidth(endTimeColumn, 15, Unit.PCT);
        messageListTable.setColumnWidth(messageColumn, 60, Unit.PCT);
        messageListTable.setColumnWidth(actionColumn, 10, Unit.PCT);
        
        //create data provider
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
				
		//get date object from datepicker
	    Date fromDateObj = fromDate.getValue();
	    Date toDateObj = toDate.getValue();
	    
	    String fromTimeString = fromTime.getValue(fromTime.getSelectedIndex());
        String toTimeString = toTime.getValue(toTime.getSelectedIndex());
        
        if (allDayCheck.getValue() == true) {
            int toTimeItemCount = toTime.getItemCount();
            
            fromTimeString = fromTime.getValue(0);
            toTimeString = toTime.getValue((toTimeItemCount == 0) ? 0 : toTimeItemCount - 1); 
        }
	    
	    if (fromDateObj != null) {
	        fromDateObj = createDateTimeObj(fromDateObj, fromTimeString);
	    }	    
	    
	    if (toDateObj != null) {
	        toDateObj = createDateTimeObj(toDateObj, toTimeString);
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
	
	/**
	 * Add a new message
	 *  
	 * @param message
	 */
	private void addNewMessage(){
		AsyncCallback<String> callback = new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Server error: " + caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
			    resetForm();
			    loadMessages();
			}			
		};
		// make the call to the server
		dataService.addMessages(tMessage, callback);
	}
	
	/**
	 * Delete the selected message
	 * @param message
	 */
	private void deleteMessage(final TMessages message){
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
		dataService.deleteMessage(message, callback);
	}
	
	
	private void validateOverlappingDateRange(Date fromDateTimeObj, Date toDateTimeObj){
        AsyncCallback<List<TMessages>> callback = new AsyncCallback<List<TMessages>>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Server error: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<TMessages> results) {
                if (results.size() > 0) {                    
                    messageForm.setWidget(1, 1, new Image("images/exclamation-icon.png"));
                    messageForm.setWidget(2, 1, new Image("images/exclamation-icon.png"));
                    errorPanel.add(new HTML("&bull; " + validationMessage.TIME_OVERLAP_EXISTING));
                } else {
                    errorPanel.clear();
                    addNewMessage();
                }
            }
        };        
        
        dataService.getMessageByDateRange(fromDateTimeObj, toDateTimeObj, callback);
    }
	
	
	
	/**
	 * Reset the form values
	 */
	private void resetForm() {
	    toDate.setValue(null); 
	    fromDate.setValue(null);
	    fromTime.setSelectedIndex(0);
	    toTime.setSelectedIndex(0);
	    txtMessage.setValue(null);
	    allDayCheck.setValue(false);
	}	
	
	/**
	 * Validate whether the message form is correctly filled
	 * 
	 * @return boolean
	 */
	private boolean validateMessageForm() {
	    boolean isValidFromDate = false;
	    boolean isValidToDate = false;        
        boolean isValidFromTime = false;
        boolean isValidToTime = false;
        boolean isValidRequiredMessage = false;
        boolean isValidMessageLength = false;
        boolean isValidDateOrder = false;
        
        Date fromDateTimeObj = new Date();
        Date toDateTimeObj = new Date();
        String fromTimeString = fromTime.getItemText(fromTime.getSelectedIndex());
        String toTimeString = toTime.getItemText(toTime.getSelectedIndex());
        
        //clear validation errors
        errorPanel.clear();
        
        //clear error ! image
        messageForm.setText(1, 1, "");
        messageForm.setText(2, 1, "");
        messageForm.setText(3, 1, "");
                
        //set time if all day checked
        if (allDayCheck.getValue() == true) {
            int toTimeItemCount = toTime.getItemCount();
            
            fromTimeString = fromTime.getValue(0);
            toTimeString = toTime.getValue((toTimeItemCount == 0) ? 0 : toTimeItemCount - 1);            
        }
	    
        // validate from date is set
	    if (fromDate.getValue() != null) {
	        isValidFromDate = true;
        } else {
            messageForm.setWidget(1, 1, new Image("images/exclamation-icon.png"));
            errorPanel.add(new HTML("&bull; " + validationMessage.START_TIME_REQUIRED));
        }
	    
	    // validate to date is set 
	    if (toDate.getValue() != null) {
            isValidToDate = true;
        } else {
            messageForm.setWidget(2, 1, new Image("images/exclamation-icon.png"));
            errorPanel.add(new HTML("&bull; " + validationMessage.END_TIME_REQUIRED));
        }
	    
	    // validate from time has a value
	    if (! fromTimeString.isEmpty()) {
            isValidFromTime = true;
        } else {
            messageForm.setWidget(1, 1, new Image("images/exclamation-icon.png"));
            errorPanel.add(new HTML("&bull; " + validationMessage.START_TIME_REQUIRED));
        }
	    
	    // validate to time has a value
	    if (! toTimeString.isEmpty()) {
            isValidToTime = true;
        } else {
            messageForm.setWidget(2, 1, new Image("images/exclamation-icon.png"));
            errorPanel.add(new HTML("&bull; " + validationMessage.END_TIME_REQUIRED));
        }
	    
	    //validate from time is before to time for non all day message    
	    if (isValidFromDate && isValidFromTime && isValidToDate && isValidToTime && allDayCheck.getValue() == false) {	        
	        fromDateTimeObj = createDateTimeObj(fromDate.getValue(), fromTimeString);
	        toDateTimeObj = createDateTimeObj(toDate.getValue(), toTimeString);
            
            if (toDateTimeObj.compareTo(fromDateTimeObj) > 0) {
                isValidDateOrder = true;
            } else {
                messageForm.setWidget(1, 1, new Image("images/exclamation-icon.png"));
                messageForm.setWidget(2, 1, new Image("images/exclamation-icon.png"));
                errorPanel.add(new HTML("&bull; " + validationMessage.START_BEFORE_END_TIME));
            }
	    }
	    
	    //validate from time is before to time for all day message	    
	    if (isValidFromDate && isValidFromTime && isValidToDate && isValidToTime && allDayCheck.getValue() == true) {
            Date fromDateObj = fromDate.getValue();
            Date toDateObj = toDate.getValue();
            
            if (toDateObj.compareTo(fromDateObj) >= 0) {
                isValidDateOrder = true;
            } else {
                messageForm.setWidget(1, 1, new Image("images/exclamation-icon.png"));
                messageForm.setWidget(2, 1, new Image("images/exclamation-icon.png"));
                errorPanel.add(new HTML("&bull; " + validationMessage.START_BEFORE_END_TIME));
            }
        }
	    
	    // validate message has content and is less that 255 characters
	    if (!txtMessage.getValue().isEmpty()) {
            isValidRequiredMessage = true;
             
            if (isValidRequiredMessage && txtMessage.getValue().length() <= MAX_MESSAGE_LENGTH) {
                isValidMessageLength = true;
            } else {
                messageForm.setWidget(3, 1, new Image("images/exclamation-icon.png"));
                errorPanel.add(new HTML("&bull; " + validationMessage.MESSAGE_LENGTH));
            }
        } else {
            messageForm.setWidget(3, 1, new Image("images/exclamation-icon.png"));
            errorPanel.add(new HTML("&bull; " + validationMessage.MESSAGE_REQUIRED));
        }
	    
	    return isValidFromDate && isValidToDate && isValidFromTime && isValidToTime && isValidDateOrder && isValidRequiredMessage && isValidMessageLength;
	}
	
	/**
	 * Returns the date object with time
	 * 
	 * @param date
	 * @param time
	 * @return Date object
	 */
	private Date createDateTimeObj(Date date, String timeString) {
    	//get the from date string in dd/MM/yyyy format
        String dateString = DateTimeFormat.getFormat("dd/MM/yyyy").format(date);
        //append hours:minutes to date string 
        dateString = dateString + " " + timeString;
        //get the from date object
        return DateTimeFormat.getFormat("dd/MM/yyyy HH:mm").parse(dateString);
	}    
	
	
	@UiHandler("addBtn")
	void handleAddButton(ClickEvent e){
	    //validate the form
	    if (validateMessageForm()) {
	        //create tMessage
            tMessage = createQuery(tMessage);            
            //validate date range doesn't overlap via gwt-rpc and add the message
	        validateOverlappingDateRange(tMessage.getStartTime(), tMessage.getStopTime());    		
	    }
	}
	
	
	@UiHandler("allDayCheck")
    void handleAllDayCheckButton(ClickEvent e){
	    boolean checked = ((CheckBox) e.getSource()).getValue();
	    if(checked) {
	        fromTime.setVisible(false);
	        toTime.setVisible(false);
	    } else {	        
	        fromTime.setVisible(true);
            toTime.setVisible(true);
	    }
    }
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

