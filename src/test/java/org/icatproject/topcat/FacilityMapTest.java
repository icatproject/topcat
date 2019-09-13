package org.icatproject.topcat;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.icatproject.topcat.exceptions.InternalException;
import org.junit.*;

public class FacilityMapTest {
	
	// A class to mock the Topcat Properties class
	// We will use it to construct both  good and bad property sets.
	
	private class MockProperties extends Properties {
		
		private Map<String,String> props;
		
		public MockProperties() {
			props = new HashMap<String,String>();
		}
		
		public void setMockProperty( String propName, String value ) {
			props.put(propName, value);
		}
		
		public String getProperty( String propertyName, String defaultValue ) {
			String value = props.get(propertyName);
			if( value == null ) {
				value = defaultValue;
			}
			return value;
		}
	}
	
	@Test
	public void testSimpleConstruction() throws InternalException {
		
		String facilityList = "LILS";
		String lilsIcatUrl = "DummyIcatUrl";
		String lilsIdsUrl = "DummyIdsUrl";
		
		MockProperties props = new MockProperties();
		
		props.setMockProperty("facility.list", facilityList);
		props.setMockProperty("facility.LILS.icatUrl", lilsIcatUrl);
		props.setMockProperty("facility.LILS.idsUrl", lilsIdsUrl);
		
		// The first test is that the FacilityMap should survive construction!
		
		FacilityMap facilityMap = new FacilityMap(props);
		
		assertEquals( lilsIcatUrl, facilityMap.getIcatUrl("LILS") );
		assertEquals( lilsIdsUrl, facilityMap.getIdsUrl("LILS") );
		
		// We haven't specified any download type URLs, so the IDS url should be returned
		
		assertEquals( lilsIdsUrl, facilityMap.getDownloadUrl("LILS", "http"));
		
		// If we do add a download type property, it should be retrieved properly
		
		String downloadType = "globus";
		String downloadUrl = "DummyGlobusUrl";
		
		props.setMockProperty("facility.LILS.downloadType." + downloadType, downloadUrl);
		
		assertEquals( downloadUrl, facilityMap.getDownloadUrl("LILS", downloadType));
	}
	
	@Test
	public void testComplexConstruction() throws InternalException {
		
		// Test loading of multiple facilities - should work with or without commas
		
		String facilityList1 = "Fac1, Fac2";
		String facilityList2 = "Fac1 Fac2";
		String fac1IcatUrl = "Fac1IcatUrl";
		String fac1IdsUrl = "Fac1IdsUrl";
		String fac1HttpUrl = "Fac1HttpUrl";
		String fac2IcatUrl = "Fac2IcatUrl";
		String fac2IdsUrl = "Fac2IdsUrl";
		String fac2HttpUrl = "Fac2HttpUrl";

		MockProperties props;
		
		String[] facilities = {facilityList1, facilityList2};
		for (String facilityList: facilities) {
			
			props = new MockProperties();
			props.setMockProperty("facility.list", facilityList1);
			
			props.setMockProperty("facility.Fac1.icatUrl", fac1IcatUrl);
			props.setMockProperty("facility.Fac1.idsUrl", fac1IdsUrl);
			props.setMockProperty("facility.Fac1.downloadType.http", fac1HttpUrl);

			props.setMockProperty("facility.Fac2.icatUrl", fac2IcatUrl);
			props.setMockProperty("facility.Fac2.idsUrl", fac2IdsUrl);
			props.setMockProperty("facility.Fac2.downloadType.http", fac2HttpUrl);
			
			FacilityMap facilityMap = new FacilityMap(props);

			assertEquals( fac1IcatUrl, facilityMap.getIcatUrl("Fac1") );
			assertEquals( fac1IdsUrl, facilityMap.getIdsUrl("Fac1") );
			assertEquals( fac1HttpUrl, facilityMap.getDownloadUrl("Fac1", "http"));
			assertEquals( fac1IdsUrl, facilityMap.getDownloadUrl("Fac1", "other"));
			
			assertEquals( fac2IcatUrl, facilityMap.getIcatUrl("Fac2") );
			assertEquals( fac2IdsUrl, facilityMap.getIdsUrl("Fac2") );
			assertEquals( fac2HttpUrl, facilityMap.getDownloadUrl("Fac2", "http"));
			assertEquals( fac2IdsUrl, facilityMap.getDownloadUrl("Fac2", "other"));
			
		}
	}

}
