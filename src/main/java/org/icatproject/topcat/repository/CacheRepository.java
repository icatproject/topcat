package org.icatproject.topcat.repository;

import java.util.*;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.Schedule;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.icatproject.topcat.domain.Cache;
import org.icatproject.topcat.utils.PropertyHandler;

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
		Cache cache = getCache(key);
		if(cache != null){
			cache.setLastAccessTime(new Date());
			em.persist(cache);
			em.flush();
			return cache.getValue();
		} else {
			return null;
		}
	}

	public void put(String key, Serializable value){
		Cache cache = getCache(key);
		if(cache == null){
			cache = new Cache();
			cache.setKey(key);
		}
		cache.setValue(value);
		em.persist(cache);
		em.flush();
	}

	@Schedule(hour="*", minute="0")
	public void prune(){
		PropertyHandler properties = PropertyHandler.getInstance();
      	int maxCacheSize = properties.getMaxCacheSize();

		TypedQuery<Cache> query = em.createQuery("select cache from Cache cache order by cache.lastAccessTime desc", Cache.class);
		query.setMaxResults(maxCacheSize);

		List<Cache> caches;
		int page = 1;
		while(true){
			query.setFirstResult(maxCacheSize * page);
			caches = query.getResultList();
			if(caches.size() > 0){
				for(Cache cache : caches){
					em.remove(cache);
				}
			} else {
				break;
			}
		}

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
