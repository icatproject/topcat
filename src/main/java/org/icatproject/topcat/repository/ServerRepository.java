package org.icatproject.topcat.repository;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.icatproject.topcat.domain.TopcatIcatServer;


@Stateless
@LocalBean
@Singleton
public class ServerRepository {
    @PersistenceContext(unitName="topcatservices")
    EntityManager em;

    static final Logger logger = Logger.getLogger(ServerRepository.class);

    @SuppressWarnings("unchecked")
    public List<TopcatIcatServer> getAllServers() {

        List<TopcatIcatServer> servers = new ArrayList<TopcatIcatServer>();

        if (em != null) {
            Query query = em.createNamedQuery("TopcatIcatServer.findAll", TopcatIcatServer.class);
            servers = query.getResultList();
        }

        return servers;

    }

    @SuppressWarnings("unchecked")
    public TopcatIcatServer getServerByName(String serverName) {
        //TODO get single row instead of list
        List<TopcatIcatServer> servers = new ArrayList<TopcatIcatServer>();

        if (em != null) {
            Query query = em.createNamedQuery("TopcatIcatServer.findByName", TopcatIcatServer.class);
            servers = query.setParameter("name", serverName).getResultList();
        }

        if (servers.size() > 0) {
            return servers.get(0);
        }

        return null;
    }






    public TopcatIcatServer save(TopcatIcatServer server) {
        //em.getTransaction().begin();
        em.persist(server);
        //em.getTransaction().commit();
        em.flush();

        return server;
    }


}
