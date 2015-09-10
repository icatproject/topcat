package org.icatproject.topcat.statuscheck;

import java.io.IOException;

import org.icatproject.ids.client.BadRequestException;
import org.icatproject.ids.client.InsufficientPrivilegesException;
import org.icatproject.ids.client.InternalException;
import org.icatproject.ids.client.NotFoundException;
import org.icatproject.ids.client.NotImplementedException;
import org.icatproject.topcat.repository.DownloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckStatusTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CheckStatusTask.class);

    private String preparedId;
    private DownloadRepository downloadRepository;

    public CheckStatusTask() {

    }

    public CheckStatusTask(String preparedId, DownloadRepository downloadRepository) {
        this.preparedId = preparedId;
        this.downloadRepository = downloadRepository;
    }

    @Override
    public void run() {
        logger.debug("New thread running...");

        boolean status = false;

        do {
            try {
                //wait a minute to give ids some time to process
                logger.debug("Waiting 60 seconds before checking....");
                Thread.sleep(60000);
                CheckStatusWorker worker = new CheckStatusWorker(preparedId, downloadRepository);

                status = worker.checkStatus();
                logger.info(preparedId + " online status is " + status);

                //wait 10 minutes for the next try
                if (status == false) {
                    logger.debug("Waiting 10 minutes before rechecking....");
                    Thread.sleep(600000);
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

