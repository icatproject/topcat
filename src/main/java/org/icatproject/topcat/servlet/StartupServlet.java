package org.icatproject.topcat.servlet;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.servlet.http.HttpServlet;

import org.icatproject.topcat.statuscheck.ExecuteCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(StartupServlet.class);

    @EJB
    private ExecuteCheck executeCheck;

    @PostConstruct
    public void init() {
        logger.debug("Startup init....");
        executeCheck.run();
    }

}
