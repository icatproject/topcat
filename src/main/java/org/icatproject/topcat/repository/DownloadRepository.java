package org.icatproject.topcat.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Singleton
public class DownloadRepository {
    @PersistenceContext(unitName="topcatv2")
    EntityManager em;

    private static final Logger logger = LoggerFactory.getLogger(DownloadRepository.class);

    public List<Download> getDownloadsByFacilityName(Map<String, String> params) {
        List<Download> downloads = new ArrayList<Download>();

        String facilityName = params.get("facilityName");
        String status = params.get("status");
        String transport = params.get("transport");
        String preparedId = params.get("preparedId");

        DownloadStatus downloadStatus = null;

        if (status != null) {
            downloadStatus = DownloadStatus.valueOf(status);
        }

        if (em != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT d FROM Download d WHERE d.facilityName = :facilityName");

            if (status != null) {
                sb.append( " AND d.status = :status");
            }

            if (transport != null) {
                sb.append( " AND d.transport = :transport");
            }

            if (preparedId != null) {
                sb.append( " AND d.preparedId = :preparedId");
            }

            //logger.debug(sb.toString());

            TypedQuery<Download> query = em.createQuery(sb.toString(), Download.class);
            query.setParameter("facilityName", facilityName);

            if (downloadStatus != null) {
                query.setParameter("status", downloadStatus);
            }

            if (transport != null) {
                query.setParameter("transport", transport);
            }

            if (preparedId != null) {
                query.setParameter("preparedId", preparedId);
            }

            logger.debug(query.toString());

            downloads = query.getResultList();

            if (downloads != null) {
                return downloads;
            }

        }

        return downloads;
    }


    public List<Download> getCheckDownloads(Map<String, String> params) {
        List<Download> downloads = new ArrayList<Download>();

        String status = params.get("status");
        String transport = params.get("transport");
        String isTwoLevel = params.get("isTwoLevel");

        DownloadStatus downloadStatus = null;

        if (status != null) {
            downloadStatus = DownloadStatus.valueOf(status);
        }

        boolean isTwoLevelBool = (isTwoLevel.equals("true")) ? true : false;

        if (em != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT d FROM Download d WHERE d.status = :status AND d.transport = :transport AND d.isTwoLevel = :isTwoLevel");

            TypedQuery<Download> query = em.createQuery(sb.toString(), Download.class);

            query.setParameter("status", downloadStatus)
                .setParameter("transport", transport)
                .setParameter("isTwoLevel", isTwoLevelBool);

            logger.debug(query.toString());

            downloads = query.getResultList();

            if (downloads != null) {
                return downloads;
            }
        }

        return downloads;
    }



    public List<Download> getDownloadsByFacilityNameAndUser(Map<String, String> params) {
        List<Download> downloads = new ArrayList<Download>();

        String facilityName = params.get("facilityName");
        String userName = params.get("userName");
        String status = params.get("status");
        String transport = params.get("transport");
        String preparedId = params.get("preparedId");

        if (em != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT d FROM Download d WHERE d.facilityName = :facilityName AND d.userName = :userName" );

            if (status != null) {
                sb.append( " AND d.status = :status");
            }

            if (transport != null) {
                sb.append( " AND d.transport = :transport");
            }

            if (preparedId != null) {
                sb.append( " AND d.preparedId = :preparedId");
            }

            logger.debug(sb.toString());

            TypedQuery<Download> query = em.createQuery(sb.toString(), Download.class);
            query.setParameter("facilityName", facilityName).
            setParameter("userName", userName);

            if (status != null) {
                query.setParameter("status", status);
            }

            if (transport != null) {
                query.setParameter("transport", transport);
            }

            if (preparedId != null) {
                query.setParameter("preparedId", preparedId);
            }

            downloads = query.getResultList();

        }

        return downloads;
    }


    public String setCompleteByPreparedId(Map<String, String> params) {
        List<Download> downloads = new ArrayList<Download>();

        String preparedId = params.get("preparedId");

        if (em != null) {
            String jpql = "SELECT d FROM Download d WHERE d.preparedId = :preparedId";

            TypedQuery<Download> query = em.createQuery(jpql, Download.class);
            query.setParameter("preparedId", preparedId);

            downloads = query.getResultList();

            if (downloads.size() > 0) {
                downloads.get(0).setStatus(DownloadStatus.COMPLETE);
                em.flush();

                return downloads.get(0).getPreparedId();
            }
        }

        return null;
    }


    public Download save(Download store) {
        em.persist(store);
        em.flush();

        return store;
    }

    public Download getDownloadsByPreparedId(String preparedId) {
        List<Download> downloads = new ArrayList<Download>();

        if (em != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT d FROM Download d WHERE d.preparedId = :preparedId");


            TypedQuery<Download> query = em.createQuery(sb.toString(), Download.class);
            query.setParameter("preparedId", preparedId);

            logger.debug(query.toString());

            downloads = query.getResultList();

            if (downloads.size() > 0) {
                return downloads.get(0);
            }

        }

        return null;
    }



}
