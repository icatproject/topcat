package uk.topcat.web.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.topcat.web.client.KeywordSearch;
import uk.topcat.web.client.model.TInvestigation;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class KeywordSearchImpl extends RemoteServiceServlet implements KeywordSearch {
	
	List<TInvestigation> investigationList;
	
	public PagingLoadResult<TInvestigation> searchByKeyword(ArrayList<String> keywords, PagingLoadConfig config) {
		
		loadInvestigations();
		
		String terms = "";
		for (String s : keywords) {
			terms += s + ", ";
		}
		
		if (config.getSortInfo().getSortField() != null) {
	      final String sortField = config.getSortInfo().getSortField();
	      
	      if (sortField != null) {
	        Collections.sort(investigationList, config.getSortInfo().getSortDir().comparator(
	            new Comparator<TInvestigation>() {
	              public int compare(TInvestigation p1, TInvestigation p2) {		              		            	  
	            	if (sortField.equals("facility")) {
	                  return p1.getFacility().compareTo(p2.getFacility());
	                } else if (sortField.equals("year")) {
	                  return p1.getYear().compareTo(p2.getYear());
	                } else if (sortField.equals("instrument")) {
	                  return p1.getInstrument().compareTo(p2.getInstrument());
	                } else if (sortField.equals("invNumber")) {
	                  return p1.getInvNumber().compareTo(p2.getInvNumber());
	                } else if (sortField.equals("investigators")) {
	                  return p1.getInvestigators().compareTo(p2.getInvestigators());
	                } else if (sortField.equals("invType")) {
	                  return p1.getInvType().compareTo(p2.getInvType());
	                } else if (sortField.equals("invAbstract")) {
	                  return p1.getInvAbstract().compareTo(p2.getInvAbstract());
	                } else if (sortField.equals("title")) {
	                  return p1.getTitle().compareTo(p2.getTitle());
	                }   
	                return 0;
	               
	              }
	            }));
		      }
		    }

		    ArrayList<TInvestigation> sublist = new ArrayList<TInvestigation>();
		    int start = config.getOffset();
		    int limit = investigationList.size();
		    if (config.getLimit() > 0) {
		      limit = Math.min(start + config.getLimit(), limit);
		    }
		    for (int i = config.getOffset(); i < limit; i++) {
		      sublist.add(investigationList.get(i));
		    }
		    return new BasePagingLoadResult<TInvestigation>(sublist, config.getOffset(), investigationList.size());
		    
	}

	private void loadInvestigations() {
	    investigationList = new ArrayList<TInvestigation>();

	    SimpleDateFormat sf = new SimpleDateFormat("yyyy");
	    try {

	      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	      DocumentBuilder db = dbf.newDocumentBuilder();
	      Document doc = db.parse(getClass().getResourceAsStream("investigations.xml"));
	      doc.getDocumentElement().normalize();

	      NodeList nodeList = doc.getElementsByTagName("row");

	      for (int s = 0; s < nodeList.getLength(); s++) {
	        Node fstNode = nodeList.item(s);
	        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
	          Element fstElmnt = (Element) fstNode;
	          NodeList fields = fstElmnt.getElementsByTagName("field");

	          TInvestigation inv = new TInvestigation();
	          inv.setTitle(getValue(fields, 0));
	          inv.setFacility(getValue(fields, 1));
	          inv.setYear(sf.parse(getValue(fields,2)));
	          inv.setInstrument(getValue(fields,3));
	          inv.setInvNumber(getValue(fields,4));
	          inv.setInvestigators(getValue(fields,5));
	          inv.setInvType(getValue(fields,6));
	          inv.setInvAbstract(getValue(fields,7));
	          
	          investigationList.add(inv);
	        }
	      }

	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }
	
	private String getValue(NodeList fields, int index) {
	    NodeList list = fields.item(index).getChildNodes();
	    if (list.getLength() > 0) {
	      return list.item(0).getNodeValue();
	    } else {
	      return "";
	    }
	  }
	
}
