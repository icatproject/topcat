package org.icatproject.topcat.repository;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.text.ParseException;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.TemporalType;


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

    public List<Download> getDownloadsByFacilityName(Map<String, Object> params) throws ParseException{
        List<Download> downloads = new ArrayList<Download>();

        String facilityName = (String) params.get("facilityName");
        String userName = (String) params.get("userName");
        String status = (String) params.get("status");
        String transport = (String) params.get("transport");
        String preparedId = (String) params.get("preparedId");
        String createdAtFrom = (String) params.get("createdAtFrom");
        String createdAtTo = (String) params.get("createdAtTo");
        Integer pageSize = (Integer) params.get("pageSize");
        Integer page = (Integer) params.get("page");

        if(pageSize == null){
            pageSize = 10;
        }

        if (em != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT d FROM Download d WHERE d.isDeleted = false");

            if(facilityName != null) {
                sb.append( " AND d.facilityName = :facilityName");
            }

            if(userName != null) {
                sb.append( " AND d.userName like concat(:userName, '%')");
            }

            if(status != null) {
                sb.append( " AND d.status like concat(:status, '%')");
            }

            if(transport != null) {
                sb.append( " AND d.transport like concat(:transport, '%')");
            }

            if(preparedId != null) {
                sb.append( " AND d.preparedId like concat(:preparedId, '%')");
            }

            if(createdAtFrom != null && createdAtTo != null) {
                sb.append( " AND d.createdAt BETWEEN :createdAtFrom AND :createdAtTo");
            }

            logger.debug(sb.toString());

            TypedQuery<Download> query = em.createQuery(sb.toString(), Download.class);
            
            if(facilityName != null) {
                query.setParameter("facilityName", facilityName);
            }

            if(userName != null) {
                query.setParameter("userName", userName);
            }

            if(status != null) {
                query.setParameter("status", status);
            }

            if(transport != null) {
                query.setParameter("transport", transport);
            }

            if(preparedId != null) {
                query.setParameter("preparedId", preparedId);
            }

            if(createdAtFrom != null && createdAtTo != null) {
                DateFormat format = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
                query.setParameter("createdAtFrom", format.parse(createdAtFrom), TemporalType.DATE);
                query.setParameter("createdAtTo", format.parse(createdAtTo), TemporalType.DATE);
            }

            if(page != null) {
                query.setFirstResult((page - 1) * pageSize);
                query.setMaxResults(pageSize);
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
            sb.append("SELECT d FROM Download d WHERE d.isDeleted = false AND d.status = :status AND d.transport = :transport AND d.isTwoLevel = :isTwoLevel");

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

        DownloadStatus downloadStatus = null;

        if (status != null) {
            downloadStatus = DownloadStatus.valueOf(status);
        }

        if (em != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT d FROM Download d WHERE d.isDeleted = false AND d.facilityName = :facilityName AND d.userName = :userName" );

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
                query.setParameter("status", downloadStatus);
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

        if (em != null) {
            String jpql = "SELECT d FROM Download d WHERE d.isDeleted = false AND d.preparedId = :preparedId";

            TypedQuery<Download> query = em.createQuery(jpql, Download.class);
            query.setParameter("preparedId", params.get("preparedId"));

            downloads = query.getResultList();

            if (downloads.size() > 0) {
                downloads.get(0).setStatus(DownloadStatus.COMPLETE);
                downloads.get(0).setCompletedAt(new Date());
                em.flush();

                EmailValidator emailValidator = EmailValidator.getInstance();
                PropertyHandler properties = PropertyHandler.getInstance();

                if (properties.isMailEnable() == true) {
                    if (downloads.get(0).getEmail() != null) {
                        if (emailValidator.isValid(downloads.get(0).getEmail())) {
                            //get fullName if exists
                            String userName = downloads.get(0).getUserName();

                            String fullName = downloads.get(0).getFullName();

                            if (fullName != null && ! fullName.trim().isEmpty()) {
                                userName = fullName;
                            }

                            Map<String, String> valuesMap = new HashMap<String, String>();
                            valuesMap.put("email", downloads.get(0).getEmail());
                            valuesMap.put("userName", userName);
                            valuesMap.put("facilityName", downloads.get(0).getFacilityName());
                            valuesMap.put("preparedId", downloads.get(0).getPreparedId());
                            valuesMap.put("downloadUrl", downloads.get(0).getTransportUrl() + "/ids/getData?preparedId=" + downloads.get(0).getPreparedId() + "&outname=" + downloads.get(0).getFileName());
                            valuesMap.put("fileName", downloads.get(0).getFileName());

                            StrSubstitutor sub = new StrSubstitutor(valuesMap);

                            if (downloads.get(0).getTransport().equals("https")) {
                                mailBean.send(downloads.get(0).getEmail(), sub.replace(properties.getMailSubject()), sub.replace(properties.getMailBodyHttps()));
                            }

                            if (downloads.get(0).getTransport().equals("globus")) {
                                mailBean.send(downloads.get(0).getEmail(), sub.replace(properties.getMailSubject()), sub.replace(properties.getMailBodyGlobus()));
                            }

                            if (downloads.get(0).getTransport().equals("smartclient")) {
                                mailBean.send(downloads.get(0).getEmail(), sub.replace(properties.getMailSubject()), sub.replace(properties.getMailBodySmartClient()));
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


    public String deleteDownloadByPreparedIdAndUserName(Map<String, String> params) {
        List<Download> downloads = new ArrayList<Download>();

        if (em != null) {
            String jpql = "SELECT d FROM Download d WHERE d.isDeleted = false AND d.preparedId = :preparedId AND d.userName = :userName";

            TypedQuery<Download> query = em.createQuery(jpql, Download.class);
            query.setParameter("preparedId", params.get("preparedId"))
                .setParameter("userName", params.get("userName"));

            downloads = query.getResultList();

            if (downloads.size() > 0) {
                downloads.get(0).setIsDeleted(true);
                downloads.get(0).setDeletedAt(new Date());
                em.flush();

                return downloads.get(0).getPreparedId();
            }
        }

        return null;
    }

    public Download getDownloadByPreparedId(String preparedId) {
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

    //todo: try and remove - a preparedId can only correspond to a single download (not a List).
    public Download getDownloadsByPreparedId(String preparedId) {
        List<Download> downloads = new ArrayList<Download>();

        if (em != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT d FROM Download d WHERE d.isDeleted = false AND d.preparedId = :preparedId");


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
