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

import java.io.*;
import java.util.*;


@LocalBean
@Singleton
public class CacheRepository {
	@PersistenceContext(unitName = "topcat")
	EntityManager em;

	private static final Logger logger = LoggerFactory.getLogger(ConfVarRepository.class);

	public Object get(String key){
		Cache cache = getCache(key);
		if(cache != null){
			try {
	            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(cache.getValue()));
	            Object object  = objectInputStream.readObject();
	            objectInputStream.close();
	            return object;
	        } catch(Exception e){
	            return null;
	        }
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
		try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(value);
            objectOutputStream.close();
            cache.setValue(byteArrayOutputStream.toByteArray());
        } catch(Exception e){}
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
