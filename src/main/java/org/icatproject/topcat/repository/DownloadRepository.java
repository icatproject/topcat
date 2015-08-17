package org.icatproject.topcat.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.validator.routines.EmailValidator;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.utils.MailBean;
import org.icatproject.topcat.utils.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Singleton
public class DownloadRepository {
    @PersistenceContext(unitName="topcatv2")
    EntityManager em;

    @EJB
    MailBean mailBean;

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
                sb.append(" AND d.status = :status");
            }

            if (transport != null) {
                sb.append(" AND d.transport = :transport");
            }

            if (preparedId != null) {
                sb.append(" AND d.preparedId = :preparedId");
            }

            sb.append(" ORDER BY d.createdAt DESC");

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

                EmailValidator emailValidator = EmailValidator.getInstance();
                PropertyHandler properties = PropertyHandler.getInstance();

                if (properties.isMailEnable() == true) {
                    if (downloads.get(0).getEmail() != null) {
                        if (emailValidator.isValid(downloads.get(0).getEmail())) {

                            Map<String, String> valuesMap = new HashMap<String, String>();
                            valuesMap.put("email", downloads.get(0).getEmail());
                            valuesMap.put("userName", downloads.get(0).getUserName());
                            valuesMap.put("facilityName", downloads.get(0).getFacilityName());
                            valuesMap.put("preparedId", downloads.get(0).getPreparedId());
                            valuesMap.put("downloadUrl", downloads.get(0).getTransportUrl() + "/ids/getData?preparedId=" + downloads.get(0).getPreparedId() + "&outName=" + downloads.get(0).getFacilityName());
                            valuesMap.put("fileName", downloads.get(0).getFileName());

                            StrSubstitutor sub = new StrSubstitutor(valuesMap);

                            if (downloads.get(0).getTransport().equals("https")) {
                                mailBean.send(downloads.get(0).getEmail(), sub.replace(properties.getMailSubject()), sub.replace(properties.getMailBodyHttps()));
                            }

                            if (downloads.get(0).getTransport().equals("globus")) {
                                mailBean.send(downloads.get(0).getEmail(), sub.replace(properties.getMailSubject()), sub.replace(properties.getMailBodyGlobus()));
                            }
                        } else {
                            logger.debug("Email not sent. Invalid email " + downloads.get(0).getEmail());
                        }
                    }
                } else {
                    logger.debug("Email not sent. Email not enabled");
                }

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
