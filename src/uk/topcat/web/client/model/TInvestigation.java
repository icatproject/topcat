package uk.topcat.web.client.model;

import java.io.Serializable;
import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BeanModelTag;

public class TInvestigation extends BaseModel implements BeanModelTag, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String title;
	String instrument;
	String facility;
	String invNumber;
	String investigators;
	String invType;
	Date year;
	String invAbstract;
	
	public TInvestigation() {
	}
	
	public String getInvType() {
		return get("invType");
		//return invType;
	}
	public void setInvType(String invType) {
		set("invType", invType);
		//this.invType = invType;
	}
	public String getInvAbstract() {
		return get("invAbstract");
		//return invAbstract;
	}
	public void setInvAbstract(String invAbstract) {
		set("invAbstract", invAbstract);
		//this.invAbstract = invAbstract;
	}
	public String getInvNumber() {
		return get("invNumber");
		//return invNumber;
	}
	public void setInvNumber(String invNumber) {
		set("invNumber", invNumber);
		//this.invNumber = invNumber;
	}
	public String getInvestigators() {
		return get("investigators");
		//return investigators;
	}
	public void setInvestigators(String investigators) {
		set("investigators", investigators);
		//this.investigators = investigators;
	}
	public Date getYear() {
		return get("year");
		//return year;
	}
	public void setYear(Date year) {
		set("year", year);
		//this.year = year;
	}
	public String getTitle() {
		return get("title");
		//return title;
	}
	public void setTitle(String title) {
		set("title", title);
		//this.title = title;
	}
	public String getInstrument() {
		return get("instrument");
		//return instrument;
	}
	public void setInstrument(String instrument) {
		set("instrument", instrument);
		//this.instrument = instrument;
	}
	public String getFacility() {
		return get("facility");
		//return facility;
	}
	public void setFacility(String facility) {
		set("facility", facility);
		//this.facility = facility;
	}
	
}
