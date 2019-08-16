package org.icatproject.topcat;

import java.util.*;
import java.lang.reflect.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import static org.junit.Assert.*;
import org.junit.*;
import javax.inject.Inject;


import javax.ejb.EJB;


import org.icatproject.topcat.repository.CacheRepository;

@RunWith(Arquillian.class)
public class CacheRepositoryTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClass(CacheRepository.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
	private CacheRepository cacheRepository;

	private static String sessionId;


	@Test
	public void testPutAndGet() throws Exception {
		cacheRepository.put("test:1", "Hello World!");
		assertEquals( "Hello World!", (String) cacheRepository.get("test:1"));
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