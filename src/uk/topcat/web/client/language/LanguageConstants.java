package uk.topcat.web.client.language;

import com.google.gwt.i18n.client.Constants;

public interface LanguageConstants extends Constants {

	//login	
	String welcomeMessage();
	
	String usernameLabel();
	String passwordLabel();
	String loginButton();
	
	//search
	String keywordTabLabel();
	String keywordSearchInstructions();
	String searchButton();
	
	String browseTabLabel();
	String advancedTabLabel();
	
	//search results
	String searchResultsTabLabel();
	String myDataTabLabel();
	
	//Search results grid
	String searchResultsGridTitle();
	String invNumberColumnTitle();
	String titleColumnTitle();
	String investigatorsColumnTitle();
	String invTypeColumnTitle();
	String instrumentColumnTitle();
	String facilityColumnTitle();
	String yearColumnTitle();
	
	//Horizontal Tabs labels : Accessing / Selecting the data. 
	String htab_keywordLabel(); 
	String htab_advancedLabel(); 
	String htab_tagLabel(); 
	String htab_browseLabel(); 
	String htab_myDataLabel();
	String htab_bookmarksLabel();
	String htab_preferenceLabel();
	String htab_keywordDescription(); 
	String htab_advancedDescription(); 
	String htab_tagDescription(); 
	String htab_browseDescription(); 
	String htab_myDataDescription();
	String htab_bookmarksDescription();
	String htab_preferenceDescription();
	
	//Vertical Tabs labels : Display or Action on the data 
	String vtab_welcomeLabel();
	String vtab_investigationLabel();
	String vtab_sampleLabel();
	String vtab_datasetLabel();
	String vtab_datafileLabel ();
	String vtab_downloadLabel();
	String vtab_permissionLabel();
	String vtab_applicationLabel();
	
	String vtab_welcomeDescription();
	String vtab_investigationDescription();
	String vtab_sampleDescription();
	String vtab_datasetDescription();
	String vtab_datafileDescription();
	String vtab_downloadDescription();
	String vtab_permissionDescription();
	String vtab_applicationDescription();	 

	//Common Button Labels
	String cbtn_search();
	String cbtn_reset();
	String cbtn_keep();
	String cbtn_remove();
	String cbtn_reverse();
		
	//Sample List View Information
	String slst_sampleIdCol();
	String slst_nameCol();
	String slst_invIdCol();
	String slst_invTitleCol();
	String slst_invNumCol();
	String slst_invVisitCol();
	String slst_instanceCol();
	String slst_chemFormCol();
	String slst_propSampleIdCol();
	String slst_safetyInfoCol();
}
