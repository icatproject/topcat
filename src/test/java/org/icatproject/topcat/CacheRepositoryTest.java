package org.icatproject.topcat;

import java.util.*;
import java.lang.reflect.*;

import static org.junit.Assert.*;
import org.junit.*;


import javax.ejb.EJB;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.icatproject.topcat.repository.CacheRepository;

public class CacheRepositoryTest {

    private static Context  ctx;
    private static EJBContainer ejbContainer;

   @BeforeClass
   public  static void setUp() {
       ejbContainer = EJBContainer.createEJBContainer();
       System.out.println("Opening the container" );
       ctx = ejbContainer.getContext();
   }

   @AfterClass
   public  static void tearDown() {
       ejbContainer.close();
       System.out.println("Closing the container" );
   }

	@Test
	public void testPutAndGet() throws Exception {
        CacheRepository cacheRepository = (CacheRepository) ctx.lookup("java:global/classes/CacheRepository");
        assertNotNull(cacheRepository);
		cacheRepository.put("test:1", "Hello World!");
		assertEquals( "Hello World!", (String) cacheRepository.get("test:1"));
	}

	@Test
	public void testRemove() throws Exception {
		// BR: this test requires the cacheRepository bean to be created,
		// and I don't know how to do that. I don't think Jody did either,
		// as all other tests that use cacheRepository have been commented-out!
        CacheRepository cacheRepository = (CacheRepository) ctx.lookup("java:global/classes/CacheRepository");
        assertNotNull(cacheRepository);
		String key = "test:remove";
		cacheRepository.put(key, "Hello World");
		cacheRepository.remove(key);
		assertEquals(null,cacheRepository.get(key));
	}
}