package org.icatproject.topcat.statuscheck;

import java.net.URL;
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

import org.icatproject.ids.client.IdsClient;
import org.icatproject.ids.client.NotFoundException;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.utils.PropertyHandler;
import org.icatproject.topcat.utils.MailBean;
import org.icatproject.topcat.utils.ConvertUtils;
import org.icatproject.topcat.repository.DownloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.validator.routines.EmailValidator;

@Singleton
public class Watchdog {

  private static final Logger logger = LoggerFactory.getLogger(Watchdog.class);
  private Map<Long, Date> lastChecks = new HashMap<Long, Date>();
  private AtomicBoolean busy = new AtomicBoolean(false);

  @PersistenceContext(unitName="topcat")
  EntityManager em;

  @EJB
  private DownloadRepository downloadRepository;

  @EJB
  MailBean mailBean;

  @Schedule(hour="*", minute="*")
  private void poll() {
    if(!busy.compareAndSet(false, true)){
      return;
    }

    PropertyHandler properties = PropertyHandler.getInstance();
    int pollDelay = properties.getPollDelay();
    int pollIntervalWait = properties.getPollIntervalWait();

    TypedQuery<Download> query = em.createQuery("select download from Download download where (download.status = org.icatproject.topcat.domain.DownloadStatus.RESTORING and download.transport = 'https') or (download.email != null and download.isEmailSent = false)", Download.class);
    List<Download> downloads = query.getResultList();

    for(Download download : downloads){
      Date lastCheck = lastChecks.get(download.getId());
      Date now = new Date();
      long createdSecondsAgo = (now.getTime() - download.getCreatedAt().getTime()) / 1000;

      if(createdSecondsAgo >= pollDelay){
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

    busy.set(false);
  }

  private void performCheck(Download download) {
    try {
      IdsClient ids = new IdsClient(new URL(download.getTransportUrl()));
      if(!download.getTransport().equals("https") && download.getStatus() == DownloadStatus.COMPLETE){
        download.setIsEmailSent(true);
        em.persist(download);
        em.flush();
        lastChecks.remove(download.getId());
        sendDownloadReadyEmail(download);
      } else if(download.getTransport().equals("https") && ids.isPrepared(download.getPreparedId())){
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
    } catch(NotFoundException e) {
      download.setStatus(DownloadStatus.EXPIRED);
      em.persist(download);
      em.flush();
      lastChecks.remove(download.getId());
    } catch(Exception e){
      logger.debug(e.toString());
    }
  }

  private void sendDownloadReadyEmail(Download download){
    EmailValidator emailValidator = EmailValidator.getInstance();
    PropertyHandler properties = PropertyHandler.getInstance();

    if (properties.isMailEnable() == true) {
      if (download.getEmail() != null) {
        if (emailValidator.isValid(download.getEmail())) {
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
          valuesMap.put("size", ConvertUtils.bytesToHumanReadable(download.getSize()));

          StrSubstitutor sub = new StrSubstitutor(valuesMap);
          String subject = sub.replace(properties.getMailSubject());
          String message = "";

          if (download.getTransport().equals("https")) {
            message = sub.replace(properties.getMailBodyHttps());
          }

          if (download.getTransport().equals("globus")) {
            message = sub.replace(properties.getMailBodyGlobus());
          }

          if (download.getTransport().equals("smartclient")) {
            message = sub.replace(properties.getMailBodySmartClient());
          }

          if (download.getTransport().equals("scarf")) {
            message = sub.replace(properties.getMailBodyScarf());
          }

          mailBean.send(download.getEmail(), subject, message);

        } else {
          logger.debug("Email not sent. Invalid email " + download.getEmail());
        }
      }
    } else {
      logger.debug("Email not sent. Email not enabled");
    }
  }

}