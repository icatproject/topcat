package org.icatproject.topcat;

import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;

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
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.exceptions.TopcatException;
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
    
	// StatusCheck treats TopcatExceptions differently to all other Exceptions,
	// so need to test both cases.  However, the only IdsClient method used by StatusCheck
	// that can throw anything other than a TopcatException is isPrepared;
	// so it will not make sense to use FailMode.EXCEPTION for prepareData or getSize.
	
	public enum FailMode { OK, EXCEPTION, TOPCAT_EXCEPTION };
	
    private class MockIdsClient extends IdsClient {
    	
    	// Make size and preparedId available for tests
    	
    	public Long size = 26L;
    	public String preparedId = "DummyPreparedId";
    	
    	private boolean isPreparedValue;
    	private FailMode failMode;
    	private boolean prepareDataCalledFlag;
    	private boolean isPreparedCalledFlag;
    	
    	public MockIdsClient(String url) {
    		// We are forced to do this as IdsClient has no no-args constructor;
    		// This forces us to have the Properties defined, even though we won't use them.
    		super(url);
    		isPreparedValue = false;
    		failMode = FailMode.OK;
    		prepareDataCalledFlag = false;
    		isPreparedCalledFlag = false;
    	}
    	
    	// Mock overrides
    	
        public String prepareData(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) throws TopcatException {
        	prepareDataCalledFlag = true;
        	if( failMode == FailMode.TOPCAT_EXCEPTION ) {
        		throw new TopcatException(500,"Deliberate TopcatException for testing");
        	}
        	return preparedId;
        }
        
        public boolean isPrepared(String preparedId) throws TopcatException, IOException {
        	// This is the only IdsClient method used by StatusCheck that can throw anything other than a TopcatException
        	isPreparedCalledFlag = true;
        	if( failMode == FailMode.TOPCAT_EXCEPTION ) {
        		throw new TopcatException(500,"Deliberate TopcatException for testing");
        	} else if( failMode == FailMode.EXCEPTION ) {
        		throw new IOException("Deliberate exception for testing");
        	}
        	return isPreparedValue;
        }

        public Long getSize(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) throws TopcatException {
        	if( failMode == FailMode.TOPCAT_EXCEPTION ) {
        		throw new TopcatException(500,"Deliberate TopcatException for testing");
        	}
        	return size;
        }
        
        // Mock utility methods
        
        public void setIsPrepared(Boolean aBool) {
        	isPreparedValue = aBool;
        }
        
        public void resetPrepareDataCalledFlag() {
        	prepareDataCalledFlag = false;
        }
        
        public void resetIsPreparedCalledFlag() {
        	isPreparedCalledFlag = false;
        }
        
        public void setFailMode(FailMode aFailMode) {
        	failMode = aFailMode;
        }
        
        public boolean prepareDataWasCalled() {
        	return prepareDataCalledFlag;
        }

        public boolean isPreparedWasCalled() {
        	return isPreparedCalledFlag;
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
	
	@Test
	@Transactional
	public void testTwoTierNonHttpDownload() throws Exception {
		
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId2";
		String transport = "globus";
		
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
		
		// But as it's not an http[s] download, updateStatuses won't test this
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download still be RESTORING, and email still not sent
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Mock pollcat setting the status to COMPLETE
		// It does this using the PUT <topcat>/admin/download/{id}/status API,
		// which uses the DownloadRepository
		
		postDownload = downloadRepository.getDownload(downloadId);
		postDownload.setStatus(DownloadStatus.COMPLETE);
        postDownload.setCompletedAt(new Date());

        downloadRepository.save(postDownload);

        statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download still be RESTORING, but download.email is null, so isEmailSent should still be false
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.COMPLETE, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// clean up
		deleteDummyDownload(postDownload);
	}
	
	@Test
	@Transactional
	public void testPrepareDataFailure() throws Exception {
		
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId3";
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
		
		// In this test, have the prepareData call fail.
		// Note: IdsClient.prepareData() can only throw TopcatException;
		// we cannot test handling of other exceptions using the mock.
		
		// A TopcatException - should expire the download
		
		mockIdsClient.setFailMode(FailMode.TOPCAT_EXCEPTION);

		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download status should now be EXPIRED, no email sent.
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.EXPIRED, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// clean up
		deleteDummyDownload(postDownload);
	}

	@Test
	@Transactional
	public void testIsPreparedFailure() throws Exception {
		
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId3";
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
		
		// In this test, have the prepareData call succeed
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download status should now be RESTORING, no email sent.
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Now mock the IDS failing
		// First, with an arbitrary exception - download status should not change
		
		mockIdsClient.setFailMode(FailMode.EXCEPTION);
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download status should not have changed
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Now fail with a TopcatException - download should be Expired
		
		mockIdsClient.setFailMode(FailMode.TOPCAT_EXCEPTION);
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download should now be EXPIRED
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.EXPIRED, postDownload.getStatus());
		
		// clean up
		deleteDummyDownload(postDownload);
	}

	@Test
	@Transactional
	public void testDelays() throws Exception {
		
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId4";
		String transport = "http";
		
		// Create a two-tier download; initial status should be PREPARING
		Download dummyDownload = createDummyDownload(preparedId, transport, true);
		Long downloadId = dummyDownload.getId();
		
		assertEquals(DownloadStatus.PREPARING, dummyDownload.getStatus());

		/*
		 * We assume that the scheduled poll() is not doing any work!
		 */
		
		int pollDelay = 1;
		int pollIntervalWait = 3;
		
		// FIRST mock-scheduled call - expect prepareData to be called, status set to RESTORING
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download status should now be RESTORING, no email sent.
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// SECOND mock-scheduled call - too early: expect isPrepared NOT to be called, and nothing changed

		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		assertFalse(mockIdsClient.isPreparedWasCalled());
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Now sleep for at least pollDelay seconds, and try again
		
		TimeUnit.SECONDS.sleep(pollDelay+1);

		// THIRD mock-scheduled call, after pollDelay seconds - expect isPrepared called, but no changes

		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		assertTrue(mockIdsClient.isPreparedWasCalled());
		mockIdsClient.resetIsPreparedCalledFlag();
		
		// But the status should not have changed, as isPrepared will have returned false

		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// FOURTH  mock-scheduled call, before pollIntervalWait seconds have passed: isPrepared should NOT be called
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);

		assertFalse(mockIdsClient.isPreparedWasCalled());

		// Now mock the IDS having prepared the data
		
		mockIdsClient.setIsPrepared(true);
		
		// Now wait for at least pollIntervalWaitSeconds, and try again
		
		TimeUnit.SECONDS.sleep(pollIntervalWait+1);
		
		// FIFTH  mock-scheduled call - expect isPrepared called, status changed to COMPLETE etc.

		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		assertTrue(mockIdsClient.isPreparedWasCalled());
		mockIdsClient.resetIsPreparedCalledFlag();
		
		// Download should now be COMPLETE, and email flagged as sent (though it wasn't!)
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.COMPLETE, postDownload.getStatus());
		assertTrue(postDownload.getIsEmailSent());
		
		// clean up
		deleteDummyDownload(postDownload);
	}
	
	@Test
	@Transactional
	public void testExpiredDownloadsIgnored() throws Exception {

		DownloadStatus status = DownloadStatus.EXPIRED;
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId";
		String transport = "http";
		
		// Create a single-tier download; initial status should be COMPLETE
		Download dummyDownload = createDummyDownload(preparedId, transport, false);
		Long downloadId = dummyDownload.getId();
		
		// Set the status and persist it
		
		dummyDownload.setStatus(status);
        em.persist(dummyDownload);
        em.flush();
        
        // Not testing delays, so set to zero

		int pollDelay = 0;
		int pollIntervalWait = 0;
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// This download should have been ignored - no status change, no email sent.
		
		assertFalse(mockIdsClient.prepareDataWasCalled());
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertEquals(status, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// clean up
		deleteDummyDownload(postDownload);
	}
	
	@Test
	@Transactional
	public void testDeletedDownloadsIgnored() throws Exception {

		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId";
		String transport = "http";
		
		// Create a single-tier download; initial status should be COMPLETE
		Download dummyDownload = createDummyDownload(preparedId, transport, false);
		Long downloadId = dummyDownload.getId();
		
		// Set download deleted and persist it
		
		dummyDownload.setIsDeleted(true);
        em.persist(dummyDownload);
        em.flush();
        
        // Not testing delays, so set to zero

		int pollDelay = 0;
		int pollIntervalWait = 0;
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// This download should have been ignored - still deleted, no email sent.
		
		assertFalse(mockIdsClient.prepareDataWasCalled());
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertTrue(postDownload.getIsDeleted());
		assertFalse(postDownload.getIsEmailSent());
		
		// clean up
		deleteDummyDownload(postDownload);
	}
	
	@Test
	@Transactional
	public void testExceptionDelays() throws Exception {
		
		// Similar to testDelays, but set MockIdsClient to throw an (IO)Exception when used by performCheck
		
		String dummyUrl = "DummyUrl";
		MockIdsClient mockIdsClient = new MockIdsClient(dummyUrl);
		
		String preparedId = "InitialPreparedId4";
		String transport = "http";
		
		// Create a two-tier download; initial status should be PREPARING
		Download dummyDownload = createDummyDownload(preparedId, transport, true);
		Long downloadId = dummyDownload.getId();
		
		assertEquals(DownloadStatus.PREPARING, dummyDownload.getStatus());

		/*
		 * We assume that the scheduled poll() is not doing any work!
		 */
		
		int pollDelay = 1;
		int pollIntervalWait = 3;
		
		// FIRST mock-scheduled call - expect prepareData to be called, status set to RESTORING
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		// Download status should now be RESTORING, no email sent.
		
		Download postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Now set mockIdsClient to throw an (IO)Exception when isPrepared is called
		
		mockIdsClient.setFailMode(FailMode.EXCEPTION);
		
		// SECOND mock-scheduled call - too early: expect isPrepared NOT to be called, and nothing changed

		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		assertFalse(mockIdsClient.isPreparedWasCalled());
		
		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Now sleep for at least pollDelay seconds, and try again
		
		TimeUnit.SECONDS.sleep(pollDelay+1);

		// THIRD mock-scheduled call, after pollDelay seconds - expect isPrepared called, but no changes

		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		assertTrue(mockIdsClient.isPreparedWasCalled());
		mockIdsClient.resetIsPreparedCalledFlag();
		
		// But the status should not have changed, as isPrepared will have thrown an exception

		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// FOURTH  mock-scheduled call, before pollIntervalWait seconds have passed: isPrepared should NOT be called
		// (the exception handling should have set the timestamp)
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);

		assertFalse(mockIdsClient.isPreparedWasCalled());

		// Now mock the IDS having prepared the data - but will still throw an exception
		
		mockIdsClient.setIsPrepared(true);
		
		// Now wait for at least pollIntervalWaitSeconds, and try again
		
		TimeUnit.SECONDS.sleep(pollIntervalWait+1);
		
		// FIFTH mock-scheduled call. Nothing should have changed, because an IOException was thrown
		
		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		assertTrue(mockIdsClient.isPreparedWasCalled());
		mockIdsClient.resetIsPreparedCalledFlag();
		
		// But the status should not have changed, as isPrepared will have thrown an exception

		postDownload = getDummyDownload(downloadId);
		
		assertEquals(DownloadStatus.RESTORING, postDownload.getStatus());
		assertFalse(postDownload.getIsEmailSent());
		
		// Now tell the client to stop throwing exceptions
		
		mockIdsClient.setFailMode(FailMode.OK);
		
		// Now wait for at least pollIntervalWaitSeconds, and try again
		
		TimeUnit.SECONDS.sleep(pollIntervalWait+1);
		
		// SIXTH  mock-scheduled call - expect isPrepared called, status changed to COMPLETE etc.

		statusCheck.updateStatuses(pollDelay, pollIntervalWait, mockIdsClient);
		
		assertTrue(mockIdsClient.isPreparedWasCalled());
		mockIdsClient.resetIsPreparedCalledFlag();
		
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
