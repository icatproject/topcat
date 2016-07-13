package org.icatproject.topcat.repository;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.icatproject.topcat.domain.ConfVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Singleton
public class ConfVarRepository {
	@PersistenceContext(unitName = "topcat")
	EntityManager em;

	private static final Logger logger = LoggerFactory.getLogger(ConfVarRepository.class);

	public ConfVar getConfVar(String name){
		TypedQuery<ConfVar> query = em.createQuery("select confVar from ConfVar confVar where confVar.name = :name", ConfVar.class);
		query.setParameter("name", name);
		List<ConfVar> resultList = query.getResultList();
		if(resultList.size() > 0){
			return resultList.get(0);
		} else {
			return null;
		}
	}

	public ConfVar save(ConfVar store) {
		em.persist(store);
		em.flush();

		return store;
	}

}
