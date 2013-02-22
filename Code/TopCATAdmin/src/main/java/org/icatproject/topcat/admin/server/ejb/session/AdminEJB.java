package org.icatproject.topcat.admin.server.ejb.session;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class AdminEJB {

	final static Logger logger = LoggerFactory.getLogger(AdminEJB.class);

	@PersistenceContext(unitName = "topcat_admin")
	private EntityManager entityManager;

	@PostConstruct
	private void init() {
		try {
			logger.debug("Initialised AdminEJB");
		} catch (Exception e) {
			String msg = e.getClass().getName() + " reports " + e.getMessage();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

}
