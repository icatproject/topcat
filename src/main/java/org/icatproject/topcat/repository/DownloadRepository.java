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

import org.apache.log4j.Logger;
import org.icatproject.topcat.domain.Download;


@Stateless
@LocalBean
@Singleton
public class DownloadRepository {
    @PersistenceContext(unitName="topcatv2")
    EntityManager em;

    static final Logger logger = Logger.getLogger(DownloadRepository.class);

    public List<Download> getDownloadsByFacilityName(Map<String, String> params) {
        List<Download> downloads = new ArrayList<Download>();

        String facilityName = params.get("facilityName");
        String status = params.get("status");
        String transport = params.get("transport");
        String preparedId = params.get("preparedId");

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

            if (status != null) {
                query.setParameter("status", status);
            }

            if (transport != null) {
                query.setParameter("transport", transport);
            }

            if (preparedId != null) {
                query.setParameter("preparedId", preparedId);
            }

            logger.debug(query.toString());

            downloads = query.getResultList();

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


    public Download save(Download store) {
        em.persist(store);
        em.flush();

        return store;
    }



}
