package org.icatproject.topcat.statuscheck;

import java.io.IOException;

import org.icatproject.ids.client.BadRequestException;
import org.icatproject.ids.client.InsufficientPrivilegesException;
import org.icatproject.ids.client.InternalException;
import org.icatproject.ids.client.NotFoundException;
import org.icatproject.ids.client.NotImplementedException;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.utils.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollStatusTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(PollStatusTask.class);

    private String preparedId;
    private DownloadRepository downloadRepository;
    private PollBean pollBean;

    public PollStatusTask() {

    }

    public PollStatusTask(String preparedId, DownloadRepository downloadRepository, PollBean pollBean) {
        this.preparedId = preparedId;
        this.downloadRepository = downloadRepository;
        this.pollBean = pollBean;
    }

    @Override
    public void run() {
        logger.info("New poll starting...");

        PropertyHandler properties = PropertyHandler.getInstance();

        int pollDelay = properties.getPollDelay();
        int pollIntervalWait = properties.getPollIntervalWait();

        pollBean.add(preparedId);

        boolean status = false;

        do {
            try {
                //wait a minute to give ids some time to process
                logger.info("Waiting " + pollDelay + " milliseconds before checking....");
                Thread.sleep(pollDelay);
                PollStatusWorker worker = new PollStatusWorker(preparedId, downloadRepository, pollBean);

                status = worker.checkStatus();
                logger.info(preparedId + " online status is " + status);

                //wait 10 minutes for the next try
                if (status == false) {
                    logger.info("Waiting " + pollIntervalWait + " milliseconds before rechecking....");
                    Thread.sleep(pollIntervalWait);
                }
            } catch (InterruptedException | BadRequestException | NotFoundException | InsufficientPrivilegesException | InternalException | NotImplementedException | IOException e) {
                logger.debug("Thread interrupted", e);

                Thread.currentThread().interrupt();
                return;
            }
        } while (status == false);
    }

    public String getPreparedId() {
        return preparedId;
    }

    public void setPreparedId(String preparedId) {
        this.preparedId = preparedId;
    }
}

