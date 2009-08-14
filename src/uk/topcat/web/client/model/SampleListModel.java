package uk.topcat.web.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BeanModelTag;

import java.io.Serializable;
/**
 * This class is a data model for the data that will be displayed in the SampleView Grid. 
 * This is not completely satisfactory because it mixes data from the sample table and 
 * the Investigation table so that the data can be grouped by Investigation with the title displayed. 
 * 
 * The getter and setters has that strange form due to the BeanModelTag. 
 * Don't understand yet exactly why. I just mimic the model for Investigation written by Damian.  
 */
public class SampleListModel extends BaseModel implements BeanModelTag, Serializable{

	private static final long serialVersionUID = 1L;
	String sample_id;
	String investigation_id;
	String investigation_title; // From Investigation table
	String investigation_num; 	// From Investigation table
	String investigation_visit; // From Investigation table
	String name;
	String instance;
	String chemical_formula;
	String proposal_sample_id;
	String safety_information; // Can be long. Reserved for extra information. 

	public String getSample_id() {
		return get("sample_id");
	}
	public void setSample_id(String sample_id) {
		set("sample_id", sample_id);
	}
	
	public String getInvestigation_id() {
		return get("investigation_id");
	}
	public void setInvestigation_id(String investigation_id) {
		set("investigation_id", investigation_id);
	}
	
	public String getInvestigation_title() {
		return get("investigation_title");
	}
	public void setInvestigation_title(String investigation_title) {
		set("investigation_title", investigation_title);
	}
	
	public String getInvestigation_num() {
		return get("investigation_num");
	}
	public void setInvestigation_num(String investigation_num) {
		set("investigation_num", investigation_num);
	}
	
	public String getInvestigation_visit() {
		return get("investigation_visit");
	}
	public void setInvestigation_visit(String investigation_visit) {
		set("investigation_visit", investigation_visit);
	}
	
	public String getName() {
		return get("name");
	}
	public void setName(String name) {
		set("name", name);
	}
	public String getInstance() {
		return get("instance");
	}
	public void setInstance(String instance) {
		set("instance", instance);
	}
	public String getChemical_formula() {
		return get("chemical_formula");
	}
	public void setChemical_formula(String chemical_formula) {
		set("chemical_formula", chemical_formula);
	}
	public String getProposal_sample_id() {
		return get("proposal_sample_id");
	}
	public void setProposal_sample_id(String proposal_sample_id) {
		set("proposal_sample_id", proposal_sample_id);
	}
	public String getSafety_information() {
		return get("safety_information");
	}
	public void setSafety_information(String safety_information) {
		set("safety_information", safety_information);
	}

 
}
