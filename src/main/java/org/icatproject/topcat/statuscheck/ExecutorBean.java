package org.icatproject.topcat.statuscheck;

import java.util.concurrent.Future;

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

    @EJB
    private PollBean pollBean;

    @EJB
    private PollFutureBean pollFutureBean;

    public ExecutorBean() {
    }

    public void executeAsync(String preparedId) {
        Future<?> future = this.executorService.submit(new PollStatusTask(preparedId, downloadRepository, pollBean));

        pollFutureBean.add(preparedId, future);
    }
}

