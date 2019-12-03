package org.icatproject.topcat;

import java.net.URL;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.annotation.Resource;

import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.Properties;
import org.icatproject.topcat.Utils;
import org.icatproject.topcat.repository.*;
import org.icatproject.topcat.IdsClient;
import org.icatproject.topcat.FacilityMap;

import org.icatproject.topcat.exceptions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.validator.routines.EmailValidator;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


@Singleton
public class StatusCheck {

  private static final Logger logger = LoggerFactory.getLogger(StatusCheck.class);
  private Map<Long, Date> lastChecks = new HashMap<Long, Date>();
  private AtomicBoolean busy = new AtomicBoolean(false);

  @PersistenceContext(unitName="topcat")
  EntityManager em;

  @EJB
  private DownloadRepository downloadRepository;

  @Resource(name = "mail/topcat")
  private Session mailSession;
  
  @Schedule(hour="*", minute="*", second="*")
  private void poll() {
	  
	  // Observation: glassfish may already prevent multiple executions, and may even count the attempt as an error,
	  // so it is possible that the use of a semaphore here is redundant.
	  
    if(!busy.compareAndSet(false, true)){
      return;
    }

    try {
      Properties properties = Properties.getInstance();
      int pollDelay = Integer.valueOf(properties.getProperty("poll.delay", "600"));
      int pollIntervalWait = Integer.valueOf(properties.getProperty("poll.interval.wait", "600"));

      // For testing, separate out the poll body into its own method
      // And allow test configurations to disable scheduled status checks
      if( ! Boolean.valueOf(properties.getProperty("test.disableDownloadStatusChecks","false"))) {
          updateStatuses(pollDelay, pollIntervalWait, null);   	  
      }
      
    } catch(Exception e){
      logger.error(e.getMessage());
    } finally {
      busy.set(false);
    }
  }
  
  /**
   * Update the status of each relevant download.
   * 
   * @param pollDelay minimum time to wait before initial preparation/check
   * @param pollIntervalWait minimum time between checks
   * @param injectedIdsClient optional (possibly mock) IdsClient
   * @throws Exception
   */
  public void updateStatuses(int pollDelay, int pollIntervalWait, IdsClient injectedIdsClient) throws Exception {
	  
	  // This method is intended for testing, but we are forced to make it public rather than protected.

	  TypedQuery<Download> query = em.createQuery("select download from Download download where download.isDeleted != true and download.status != org.icatproject.topcat.domain.DownloadStatus.EXPIRED and (download.status = org.icatproject.topcat.domain.DownloadStatus.PREPARING or (download.status = org.icatproject.topcat.domain.DownloadStatus.RESTORING and download.transport in ('https','http')) or (download.email != null and download.isEmailSent = false))", Download.class);
      List<Download> downloads = query.getResultList();

      for(Download download : downloads){
        Date lastCheck = lastChecks.get(download.getId());
        Date now = new Date();
        long createdSecondsAgo = (now.getTime() - download.getCreatedAt().getTime()) / 1000;
        if(download.getStatus() == DownloadStatus.PREPARING){
        	// If prepareDownload was called previously but caught an exception (other than TopcatException),
        	// we should not call it again immediately, but should impose a delay. See issue #462.          
          if(lastCheck == null){
        	  prepareDownload(download, injectedIdsClient);
            } else {
              long lastCheckSecondsAgo = (now.getTime() - lastCheck.getTime()) / 1000;
              if(lastCheckSecondsAgo >= pollIntervalWait){
            	  prepareDownload(download, injectedIdsClient);
              }
            }
        } else if(createdSecondsAgo >= pollDelay){
          if(lastCheck == null){
            performCheck(download, injectedIdsClient);
          } else {
            long lastCheckSecondsAgo = (now.getTime() - lastCheck.getTime()) / 1000;
            if(lastCheckSecondsAgo >= pollIntervalWait){
              performCheck(download, injectedIdsClient);
            }
          }
        }
      }	  
  }

  private void performCheck(Download download, IdsClient injectedIdsClient) {
    try {
      IdsClient idsClient = injectedIdsClient;
      if( idsClient == null ) {
    	  idsClient = new IdsClient(getDownloadUrl(download.getFacilityName(),download.getTransport()));
      }
      if(!download.getIsEmailSent() && download.getStatus() == DownloadStatus.COMPLETE){
    	  logger.info("Download COMPLETE for " + download.getFileName() + "; checking whether to send email...");
        download.setIsEmailSent(true);
        em.persist(download);
        em.flush();
        lastChecks.remove(download.getId());
        sendDownloadReadyEmail(download);
      } else if(download.getTransport().matches("https|http") && idsClient.isPrepared(download.getPreparedId())){
    	  logger.info("Download (http[s]) for " + download.getFileName() + " is Prepared, so setting COMPLETE and checking email...");
        download.setStatus(DownloadStatus.COMPLETE);
        download.setCompletedAt(new Date());
        download.setIsEmailSent(true);
        em.persist(download);
        em.flush();
        lastChecks.remove(download.getId());
        sendDownloadReadyEmail(download);
      } else {
        lastChecks.put(download.getId(), new Date());
      }
    } catch (IOException e){
    	handleException(download,"performCheck IOException: " + e.toString());
    } catch(NotFoundException e){
    	handleException(download,"performCheck NotFoundException: " + e.getMessage());
    } catch(TopcatException e) {
    	// Note: only expire downloads for TopcatExceptions. See issue #462
    	handleException(download,"performCheck TopcatException: " + e.toString(), true);
    } catch(Exception e){
    	handleException(download,"performCheck Exception: " + e.toString());
    }
  }

  private void sendDownloadReadyEmail(Download download) throws InternalException{
    EmailValidator emailValidator = EmailValidator.getInstance();
    Properties properties = Properties.getInstance();

    if (properties.getProperty("mail.enable", "false").equals("true")) {
      if (download.getEmail() != null && emailValidator.isValid(download.getEmail())) {
        // get fullName if exists
        String userName = download.getUserName();
        String fullName = download.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
          userName = fullName;
        }

        String downloadUrl = getDownloadUrl(download.getFacilityName(),download.getTransport());
        downloadUrl += "/ids/getData?preparedId=" + download.getPreparedId();
        downloadUrl += "&outname=" + download.getFileName();

        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("email", download.getEmail());
        valuesMap.put("userName", userName);
        valuesMap.put("facilityName", download.getFacilityName());
        valuesMap.put("preparedId", download.getPreparedId());
        valuesMap.put("downloadUrl", downloadUrl);
        valuesMap.put("fileName", download.getFileName());
        valuesMap.put("size", Utils.bytesToHumanReadable(download.getSize()));

        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String subject = sub.replace(properties.getProperty("mail.subject", "mail.subject not set in topcat.properties"));
        String bodyProperty = "mail.body." + download.getTransport();
        String body = sub.replace(properties.getProperty(bodyProperty, bodyProperty + " not set in topcat.properties"));


        Message message = new MimeMessage(mailSession);
        try {
          message.setSubject(subject);
          message.setText(body);
          message.setRecipients(RecipientType.TO, InternetAddress.parse(download.getEmail()));

          Transport.send(message);

          logger.debug("Email sent to " + download.getEmail());
        } catch (MessagingException e) {
          logger.debug(e.getMessage());
        }

      } else {
        logger.debug("Email not sent. Invalid email " + download.getEmail());
      }
    } else {
      logger.debug("Email not sent. Email not enabled");
    }
  }

  private void prepareDownload(Download download, IdsClient injectedIdsClient) throws Exception {

    try {
      IdsClient idsClient = injectedIdsClient;
      if( idsClient == null ) {
    	  idsClient = new IdsClient(getDownloadUrl(download.getFacilityName(),download.getTransport()));
      }
      logger.info("Requesting prepareData for Download " + download.getFileName());
      String preparedId = idsClient.prepareData(download.getSessionId(), download.getInvestigationIds(), download.getDatasetIds(), download.getDatafileIds());
      download.setPreparedId(preparedId);

      try {
        Long size = idsClient.getSize(download.getSessionId(), download.getInvestigationIds(), download.getDatasetIds(), download.getDatafileIds());
        download.setSize(size);
      } catch(Exception e) {
    	logger.error("prepareDownload: setting size to -1 as getSize threw exception: " + e.getMessage());
        download.setSize(-1);
      }

      if (download.getIsTwoLevel() || !download.getTransport().matches("https|http")) {
    	  logger.info("Setting Download status RESTORING for " + download.getFileName());
        download.setStatus(DownloadStatus.RESTORING);
      } else {
    	  logger.info("Setting Download status COMPLETE for " + download.getFileName());
        download.setStatus(DownloadStatus.COMPLETE);
        download.setCompletedAt(new Date());
      }

      downloadRepository.save(download);
    } catch(NotFoundException e){
    	handleException(download, "prepareDownload NotFoundException: " + e.getMessage());
    } catch(TopcatException e) {
    	// Note: only expire downloads for TopcatExceptions. See issue #462
    	handleException(download, "prepareDownload TopcatException: " + e.toString(), true);
    } catch(Exception e){
    	handleException(download, "prepareDownload Exception: " + e.toString());
    }

  }
  
  private void handleException( Download download, String reason, boolean doExpire ) {
	  if( doExpire ) {
	      logger.error("Marking download " + download.getId() + " as expired. Reason: " + reason);
	      download.setStatus(DownloadStatus.EXPIRED);
	      em.persist(download);
	      em.flush();
	      lastChecks.remove(download.getId());
	  } else {
		  // Record that we have tried to check (or prepare) this download,
		  // so that updateStatuses should not try again immediately.
		  logger.warn( "Ignoring: " + reason);
		  lastChecks.put(download.getId(), new Date());
	  }
  }
  
  private void handleException( Download download, String reason ) {
	  handleException( download, reason, false );
  }

  private String getDownloadUrl( String facilityName, String downloadType ) throws InternalException{
      return FacilityMap.getInstance().getDownloadUrl(facilityName, downloadType);
  }
}