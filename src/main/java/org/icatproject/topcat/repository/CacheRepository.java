package org.icatproject.topcat.repository;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.icatproject.topcat.domain.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcat.IcatClient;

import java.io.Serializable;


@LocalBean
@Singleton
public class CacheRepository {
	@PersistenceContext(unitName = "topcat")
	EntityManager em;

	private static final Logger logger = LoggerFactory.getLogger(ConfVarRepository.class);

	public Object get(String key){
		System.out.println("get");
		Cache cache = getCache(key);
		System.out.println("Cache cache = getCache(key);");
		if(cache != null){
			return cache.getValue();
		} else {
			return null;
		}
	}

	public void put(String key, Serializable value){
		Cache cache = getCache(key);
		if(cache == null){
			System.out.println("cache is null");
			cache = new Cache();
			cache.setKey(key);
		}
		System.out.println("cache.setValue(value)");
		cache.setValue(value);
		System.out.println("em.persist(cache);");
		em.persist(cache);
		em.flush();
	}

	private Cache getCache(String key){
		TypedQuery<Cache> query = em.createQuery("select cache from Cache cache where cache.key = :key", Cache.class);
		query.setParameter("key", key);
		List<Cache> resultList = query.getResultList();
		if(resultList.size() > 0){
			return resultList.get(0);
		} else {
			return null;
		}
	}

}
