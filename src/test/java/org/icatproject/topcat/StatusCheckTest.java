package org.icatproject.topcat;

import java.util.*;
import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.jboss.arquillian.transaction.api.annotation.Transactional;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ejb.EJB;

import org.icatproject.topcat.domain.*;

import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.StatusCheck;

import java.sql.*;

@RunWith(Arquillian.class)
public class StatusCheckTest {

	@PersistenceContext(unitName = "topcat")
	EntityManager em;

	@Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(StatusCheck.class, DownloadRepository.class, IdsClient.class)
            .addPackages(true,"org.icatproject.topcat.domain","org.icatproject.topcat.exceptions")
            .addAsResource("META-INF/persistence.xml")
            // .addAsResource("topcat.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    private class MockIdsClient extends IdsClient {
    	
    	// Make size and preparedId available for tests
    	
    	public Long size = 26L;
    	public String preparedId = "DummyPreparedId";
    	
    	private boolean isPreparedValue;
    	
    	public MockIdsClient(String url) {
    		// We are forced to do this as IdsClient has no no-args constructor;
    		// This forces us to have the Properties defined, even though we won't use them.
    		super(url);
    		isPreparedValue = false;
    	}
    	
    	// Mock overrides
    	
        public String prepareData(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) {
        	return preparedId;
        }
        
        public boolean isPrepared(String preparedId) {
        	return isPreparedValue;
        }

        public Long getSize(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) {
        	return size;
        }
        
        // Mock utility methods
        
        public void setIsPrepared(Boolean aBool) {
        	isPreparedValue = aBool;
        }
    }

	@EJB
	private DownloadRepository downloadRepository;

	@Inject
	private StatusCheck statusCheck;
	
	@Test
	@Transactional
	public void testSimpleDownload() throws Exception {
		
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId";
		String transport = "http";
		
		// Create a single-tier download; initial status should be COMPLETE
		Download dummyDownload = createDummyDownload(preparedId, transport, false);
		Long downloadId = dummyDownload.getId();
		
		assertEquals(DownloadStatus.COMPLETE, dummyDownload.getStatus());

		/*
		 * If (as I suspect) the scheduled poll() is running, it might add a lastCheck timestamp for our test download,
		 * which could prevent the test call below from doing any useful work.
		 * We are not (yet) testing the delay behaviour, so together these imply that we should set very short wait times.
		 * Of course, even 1 second is too long!
		 */
		
		int pollDelay = 0;
		int pollIntervalWait = 0;
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// This download should have been ignored - no status change, no email sent.
		// REMEMBER: dummyDownload.email is null, so it should be excluded by the query in updateStatuses()		
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.COMPLETE, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// clean up
		deleteDummyDownload(postDownload);
	}
	
	@Test
	@Transactional
	public void testTwoTierDownload() throws Exception {
		
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId2";
		String transport = "http";
		
		// Create a two-tier download; initial status should be PREPARING
		Download dummyDownload = createDummyDownload(preparedId, transport, true);
		Long downloadId = dummyDownload.getId();
		
		assertEquals(DownloadStatus.PREPARING, dummyDownload.getStatus());

		/*
		 * If (as I suspect) the scheduled poll() is running, it might add a lastCheck timestamp for our test download,
		 * which could prevent the test call below from doing any useful work.
		 * We are not (yet) testing the delay behaviour, so together these imply that we should set very short wait times.
		 * Of course, even 1 second is too long!
		 * TODO: consider adding sleeps to test more realistic behaviour.
		 */
		
		int pollDelay = 0;
		int pollIntervalWait = 0;
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download status should now be RESTORING, no email sent.
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Now mock the IDS having prepared the data
		
		mockIdsClient.setIsPrepared(true);
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download should now be COMPLETE, and email flagged as sent (though it wasn't!)
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.COMPLETE, postDownload.getStatus());
		assertTrue(postDownload.getIsEmailSent());
		
		// clean up
		deleteDummyDownload(postDownload);
	}
	
	private Download createDummyDownload(String preparedId, String transport, Boolean isTwoLevel) {
		
		// This mocks what UserResource.submitCart() might do.
		
		String facilityName = "LILS";
		String sessionId = "DummySessionId";
		String fileName = "DummyFilename";
		String userName = "DummyUsername";
		String fullName = "Dummy Full Name";
		// Note: setting email to null means we won't exercise (or test!) the mail-sending code
		String email = null;
		
		Download download = new Download();
		download.setSessionId(sessionId);
		download.setFacilityName(facilityName);
		download.setFileName(fileName);
		download.setUserName(userName);
		download.setFullName(fullName);
		download.setTransport(transport);
		download.setEmail(email);
		download.setIsEmailSent(false);
		download.setSize(0);

	        // Create one or more dummy DownloadItems

		List<DownloadItem> downloadItems = new ArrayList<DownloadItem>();

		for (int i=0; i <= 2; i++) {
			DownloadItem downloadItem = new DownloadItem();
			downloadItem.setEntityId( 10L + i );
			downloadItem.setEntityType(EntityType.dataset);
			downloadItem.setDownload(download);
			downloadItems.add(downloadItem);
		}

		download.setDownloadItems(downloadItems);

		download.setIsTwoLevel(isTwoLevel);

		if(isTwoLevel){
			download.setStatus(DownloadStatus.PREPARING);
		} else {
	   		download.setPreparedId(preparedId);
			download.setStatus(DownloadStatus.COMPLETE);
		}

		em.persist(download);
		em.flush();
		em.refresh(download);
		em.flush();

	    return download;
	}
	
	private Download getDummyDownload(Long downloadId) {
		
		Download download;
		
	    TypedQuery<Download> query = em.createQuery("select download from Download download where download.id = :id", Download.class);
	    query.setParameter("id",downloadId);
	    try {
	    	download = query.getSingleResult();
	    } catch (Exception e) {
	    	download = null;
	    }
	    return download;
	}
	
	private void deleteDummyDownload(Download download) {
		em.remove(download);;
		em.flush();
	}
}
