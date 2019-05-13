package org.icatproject.topcat.repository;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.icatproject.topcat.domain.DownloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class DownloadTypeRepository {
	@PersistenceContext(unitName = "topcat")
	EntityManager em;

	private static final Logger logger = LoggerFactory.getLogger(DownloadTypeRepository.class);

	public DownloadType getDownloadType(String facilityName, String downloadType) {
		TypedQuery<DownloadType> query = em.createQuery("select downloadType from DownloadType downloadType where downloadType.facilityName = :facilityName and downloadType.downloadType = :downloadType", DownloadType.class);
		query.setParameter("facilityName", facilityName);
		query.setParameter("downloadType", downloadType);
		List<DownloadType> resultList = query.getResultList();
		if(resultList.size() > 0){
			return resultList.get(0);
		} else {
			return null;
		}
	}

	public DownloadType save(DownloadType store) {
		em.persist(store);
		em.flush();

		return store;
	}
}
