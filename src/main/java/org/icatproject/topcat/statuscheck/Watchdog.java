import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.collections4.ListUtils;

import javax.ejb.Singleton;
import javax.ejb.Schedule;

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

@Singleton
public class Watchdog {
  private static final Logger logger = LoggerFactory.getLogger(Watchdog.class);
  private Map<Long, Date> lastChecks = new HashMap<Long, Date>();
  private AtomicBoolean busy = new AtomicBoolean(false);

  @PersistenceContext(unitName="topcatv2")
  EntityManager em;

  @Schedule(hour="*", minute="*")
  private void poll() {
    if(!busy.compareAndSet(false, true)){
      return;
    }

    logger.debug("poll");

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

    busy.set(false);
  }

  private void performCheck(Download download) {
    logger.debug("performCheck ");

    try {
      IdsClient ids = new IdsClient(new URL(download.getTransportUrl()));
      PropertyHandler properties = PropertyHandler.getInstance();
      int maxPerGetStatus = properties.getMaxPerGetStatus();
      
      List<List<Long>> partitionedFileIds = ListUtils.partition(ids.getDatafileIds(download.getPreparedId()), maxPerGetStatus);

      boolean isComplete = true;
      for (List<Long> currentFileIds : partitionedFileIds) {
        DataSelection dataSelection = new DataSelection();
        dataSelection.addDatafiles(currentFileIds);

        Status status = ids.getStatus(null, dataSelection);
        if(status.equals(Status.ARCHIVED)){
          download.setStatus(DownloadStatus.FAILED);
          em.persist(download);
          em.flush();
          lastChecks.remove(download.getId());
        lastChecks.remove(download.getId());
        } else if(!status.equals(Status.ONLINE)){
          logger.debug("Status: " + status.toString());
          isComplete = false;
          break;
        }
      }

      if(isComplete){
        download.setStatus(DownloadStatus.COMPLETE);
        download.setCompletedAt(new Date());
        em.persist(download);
        em.flush();
        lastChecks.remove(download.getId());
      } else {
        lastChecks.put(download.getId(), new Date());
      }
    } catch(Exception e){
      logger.debug(e.toString());
    }
  }

}