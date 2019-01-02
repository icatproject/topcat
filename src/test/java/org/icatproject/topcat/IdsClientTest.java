package org.icatproject.topcat;

import java.util.*;
import java.lang.reflect.*;

import static org.junit.Assert.*;
import org.junit.*;

public class IdsClientTest {


	@Test
	public void testChunkOffsets() throws Exception {
		IdsClient idsClient = new IdsClient("https://localhost:8181");
		Method chunkOffsets = idsClient.getClass().getDeclaredMethod("chunkOffsets", String.class, List.class, List.class, List.class);
		chunkOffsets.setAccessible(true);

		for(int i = 0; i < 10; i++){
			// Use the same IDs for each list of investigation/dataset/datafileIds
			List<Long> investigationIds = generateIds(i * 100, 1000);
			List<Long> datasetIds = generateIds(i * 100, 1000);
			List<Long> datafileIds = generateIds(i * 100, 1000);
			List<String> chunks = (List<String>) chunkOffsets.invoke(idsClient, "preparedData?sessionIds=312313-21312-312&", investigationIds, datasetIds, datafileIds);
			for(String chunk : chunks){
				assertTrue(chunk.length() <= 2048);
			}
			// Each ID ought to appear *somewhere* as an investigation/dataset/datafileId
			// Careful: build process may have emptied the original entityIds lists
			List<Long> entityIds = generateIds(i * 100, 1000);
			boolean allFound = true;
			for( Long id : entityIds ) {
				boolean foundInvestigation = false;
				boolean foundDataset = false;
				boolean foundDatafile = false;
				for( String chunk : chunks ) {
					// Only look if we haven't found it in a previous chunk
					// TODO: worry about duplicates?
					if( ! foundInvestigation ) foundInvestigation = chunkContains(chunk, "investigation",id);
					if( ! foundDataset )       foundDataset       = chunkContains(chunk, "dataset",id);
					if( ! foundDatafile )      foundDatafile      = chunkContains(chunk, "datafile",id);
				}
				allFound = allFound && foundInvestigation && foundDataset && foundDatafile;
			}
			assertTrue("Not all IDs found in chunks", allFound);
		}

		String expected = "test?investigationIds=1,2,3&datasetIds=4,5,6&datafileIds=7,8,9";
		List<String> offsets = (List<String>) chunkOffsets.invoke(idsClient, "test?", generateIds(1, 3), generateIds(4, 3), generateIds(7, 3));
		String actual = offsets.get(0);

		assertTrue("expected: " + expected + " actual: " + actual, expected.equals(actual));

	}

	private List<Long> generateIds(int offset, int count){
		List<Long> out = new ArrayList<Long>();

		for(int i = 0; i < count; i++){
			out.add(Long.valueOf(offset + i));
		}

		return out;
	}
	
	private boolean chunkContains(String chunk, String entityType, Long id) {
		// return true if chunk contains a substring of the form "<entity>Ids=m1,m2,...,mN" with some mi == id
		return chunk.matches(".*" + entityType + "Ids=[\\d+,]*" + id.toString() + "[,\\d+]*.*");
	}

}