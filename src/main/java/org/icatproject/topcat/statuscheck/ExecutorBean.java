package org.icatproject.topcat.statuscheck;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;

import org.icatproject.topcat.repository.DownloadRepository;


@Stateless
public class ExecutorBean {
    @Resource
    private ManagedExecutorService executorService;

    @EJB
    private DownloadRepository downloadRepository;

    public ExecutorBean() {
    }

    public void executeAsync(String preparedId) {
        this.executorService.submit(new CheckStatusTask(preparedId, downloadRepository));
    }
}

