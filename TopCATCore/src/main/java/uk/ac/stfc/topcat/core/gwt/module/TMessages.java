package uk.ac.stfc.topcat.core.gwt.module;

import java.io.Serializable;
import java.util.Date;

/**
 * This is shared with GWT for Messages information.
 * <p>
 * 
 * @author Mr. Noris Nyamekye
 * @version 1.0, 14-Aug-2014
 */
public class TMessages implements Serializable {
	private static final long serialVersionUID = 1L;
    private Long id;
	private String message;
	private Date startTime;
	private Date stopTime;


    public TMessages() {
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getStopTime(){
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }   
    

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("TMessages\n");
        message.append("id:").append(id).append("\n");
        message.append("name:").append(message).append("\n");
        return message.toString();
    }

}
