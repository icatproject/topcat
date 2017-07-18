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
import org.icatproject.topcat.IcatClient;

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
  
  @EJB
  private CacheRepository cacheRepository;

  @Schedule(hour="*", minute="*", second="*")
  private void poll() {
    if(!busy.compareAndSet(false, true)){
      return;
    }

    try {
      Properties properties = Properties.getInstance();
      int pollDelay = Integer.valueOf(properties.getProperty("poll.delay", "600"));
      int pollIntervalWait = Integer.valueOf(properties.getProperty("poll.interval.wait", "600"));

      TypedQuery<Download> query = em.createQuery("select download from Download download where download.isDeleted != true and download.status != org.icatproject.topcat.domain.DownloadStatus.EXPIRED and (download.status = org.icatproject.topcat.domain.DownloadStatus.PREPARING or (download.status = org.icatproject.topcat.domain.DownloadStatus.RESTORING and download.transport = 'https') or (download.email != null and download.isEmailSent = false))", Download.class);
      List<Download> downloads = query.getResultList();

      for(Download download : downloads){
        Date lastCheck = lastChecks.get(download.getId());
        Date now = new Date();
        long createdSecondsAgo = (now.getTime() - download.getCreatedAt().getTime()) / 1000;
        if(download.getStatus() == DownloadStatus.PREPARING){
          prepareDownload(download);
        } else if(createdSecondsAgo >= pollDelay){
          if(lastCheck == null){
            performCheck(download);
          } else {
            long lastCheckSecondsAgo = (now.getTime() - lastCheck.getTime()) / 1000;
            if(lastCheckSecondsAgo >= pollIntervalWait){
              performCheck(download);
            }
          }
        }
      }
    } catch(Exception e){
      logger.error(e.getMessage());
    } finally {
      busy.set(false);
    }
  }

  private void performCheck(Download download) {
    try {
      IdsClient idsClient = new IdsClient(download.getTransportUrl());
      if(!download.getIsEmailSent() && download.getStatus() == DownloadStatus.COMPLETE){
        download.setIsEmailSent(true);
        em.persist(download);
        em.flush();
        lastChecks.remove(download.getId());
        sendDownloadReadyEmail(download);
      } else if(download.getTransport().equals("https") && idsClient.isPrepared(download.getPreparedId())){
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
      logger.error("performCheck IOException: " + e.toString());
    } catch(TopcatException e) {
      logger.error("performCheck TopcatException: marking download as expired (preparedId=" + download.getPreparedId() + "): " + e.toString());
      download.setStatus(DownloadStatus.EXPIRED);
      em.persist(download);
      em.flush();
      lastChecks.remove(download.getId());
    } catch(Exception e){
      logger.error("performCheck Exception: " + e.toString());
    }
  }

  private void sendDownloadReadyEmail(Download download){
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

        String downloadUrl = download.getTransportUrl();
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

  private void prepareDownload(Download download) throws Exception {

    try {
      IdsClient idsClient = new IdsClient(download.getTransportUrl());
      String preparedId = idsClient.prepareData(download.getSessionId(), download.getInvestigationIds(), download.getDatasetIds(), download.getDatafileIds());
      download.setPreparedId(preparedId);

      IcatClient icatClient = new IcatClient(download.getIcatUrl(), download.getSessionId());
      try {
        Long size = icatClient.getSize(cacheRepository, download.getInvestigationIds(), download.getDatasetIds(), download.getDatafileIds());
        download.setSize(size);
      } catch(Exception e) {
        download.setSize(-1);
      }

      if (download.getIsTwoLevel() || !download.getTransport().equals("https")) {
        download.setStatus(DownloadStatus.RESTORING);
      } else {
        download.setStatus(DownloadStatus.COMPLETE);
        download.setCompletedAt(new Date());
      }

      downloadRepository.save(download);
    } catch(TopcatException e) {
      logger.error("prepareDownload TopcatException: marking download as expired (preparedId=" + download.getPreparedId() + "): " + e.toString());
      download.setStatus(DownloadStatus.EXPIRED);
      em.persist(download);
      em.flush();
      lastChecks.remove(download.getId());
    } catch(Exception e){
      logger.error("prepareDownload Exception: " + e.toString());
    }

  }

}