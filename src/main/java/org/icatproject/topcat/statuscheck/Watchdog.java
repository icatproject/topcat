import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.collections4.ListUtils;

import java.text.ParseException;


import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import javax.persistence.Persistence;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.PersistenceContext;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.utils.PropertyHandler;
import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.ids.client.BadRequestException;
import org.icatproject.ids.client.InternalException;

@Singleton
public class Watchdog {
  private static final Logger logger = LoggerFactory.getLogger(Watchdog.class);
  private Map<Long, Date> lastChecks = new HashMap<Long, Date>();
  private Map<Long, List<Long>> fileIds = new HashMap<Long, List<Long>>();

  @PersistenceContext(unitName="topcatv2")
  EntityManager em;

  @Schedule(hour="*", minute="*")
  private void poll() throws MalformedURLException, InternalException, BadRequestException {
    logger.info("poll");
    PropertyHandler properties = PropertyHandler.getInstance();
    int pollDelay = properties.getPollDelay();
    int pollIntervalWait = properties.getPollIntervalWait();

    TypedQuery<Download> query = em.createQuery("select d from Download d where d.status = org.icatproject.topcat.domain.DownloadStatus.RESTORING", Download.class);
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
  }

  private void performCheck(Download download) throws MalformedURLException, InternalException, BadRequestException {
    IdsClient ids = new IdsClient(new URL(download.getTransportUrl()));
    PropertyHandler properties = PropertyHandler.getInstance();
    int maxPerGetStatus = properties.getMaxPerGetStatus();
    
    if(fileIds.get(download.getId()) == null){
      fileIds.put(download.getId(), ids.getDatafileIds(download.getPreparedId()));
    }

    List<List<Long>> partitionedFileIds = ListUtils.partition(fileIds.get(download.getId()), maxPerGetStatus);

    boolean isComplete = true;
    for (List<Long> currentFileIds : partitionedFileIds) {
      DataSelection dataSelection = new DataSelection();
      dataSelection.addDatafiles(currentFileIds);

      Status status = ids.getStatus(null, dataSelection);
      if (!status.equals(Status.ONLINE)){
        isComplete = false;
        break;
      }
    }

    if(isComplete){
      download.setStatus(DownloadStatus.COMPLETE);
      download.setCompletedAt(new Date());
      em.persist(download);
      em.flush();
    }

    lastChecks.put(download.getId(), new Date());
  }

}