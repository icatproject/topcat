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
			for(String chunk : (List<String>) chunkOffsets.invoke(idsClient, "preparedData?sessionIds=312313-21312-312&", generateIds(i * 100, 1000), generateIds(i * 100, 1000), generateIds(i * 100, 1000))){
				assertTrue(chunk.length() <= 2048);
			}
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

}