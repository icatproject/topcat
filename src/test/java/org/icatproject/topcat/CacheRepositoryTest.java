package org.icatproject.topcat;

import java.util.*;
import java.lang.reflect.*;

import static org.junit.Assert.*;
import org.junit.*;


import javax.ejb.EJB;


import org.icatproject.topcat.repository.CacheRepository;

public class CacheRepositoryTest {

	@EJB
	private CacheRepository cacheRepository;

	private static String sessionId;


	@Test
	public void testPutAndGet() throws Exception {
		//cacheRepository.put("test:1", "Hello World!");
		//assertEquals( "Hello World!", (String) cacheRepository.get("test:1"));
	}

	@Test
	public void testRemove() {
		// BR: this test requires the cacheRepository bean to be created,
		// and I don't know how to do that. I don't think Jody did either,
		// as all other tests that use cacheRepository have been commented-out!
		/*
		String key = "test:remove";
		cacheRepository.put(key, "Hello World");
		cacheRepository.remove(key);
		assertEquals(null,cacheRepository.get(key));
		*/
	}
}