package org.icatproject.topcat.statuscheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;

import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadStatus;
import org.icatproject.topcat.repository.DownloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Singleton
public class ExecutePoll {
    private static final Logger logger = LoggerFactory.getLogger(ExecutePoll.class);

    @EJB
    private ExecutorBean executorBean;

    @EJB
    private DownloadRepository downloadRepository;

    @EJB
    private PollBean pollBean;

    public ExecutePoll(){

    }

    public int run() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("status", "RESTORING");
        params.put("transport", "https");
        params.put("isTwoLevel", "true");

        List<Download> downloads = new ArrayList<Download>();

        int count = 0;

        downloads = downloadRepository.getCheckDownloads(params);

        if (downloads != null) {
            for(Download download : downloads) {
                //only execute if not already in poll list
                if (! pollBean.has(download.getPreparedId())) {
                    logger.info("Run status check for preparedId " + download.getPreparedId() + " for user " + download.getUserName() + " with the download name of " + download.getFileName());
                    executorBean.executeAsync(download.getPreparedId());
                    count++;
                } else {
                    logger.info("preparedId " + download.getPreparedId() + " is already polling... skip polling");
                }

            }
        }

        return count;
    }

    public int runByPreparedId(String preparedId) {
        int count = 0;

        Download download = downloadRepository.getDownloadsByPreparedId(preparedId);

        if (download == null) {
            return count;
        }

        if (! download.getStatus().equals(DownloadStatus.RESTORING) ) {
            return count;
        }

        if (! download.getTransport().equals("https") ) {
            return count;
        }

        if (! download.getIsTwoLevel().equals(true) ) {
            return count;
        }

        //only execute if not already in poll list
        if (! pollBean.has(download.getPreparedId())) {
            logger.info("Run status poll for preparedId " + download.getPreparedId() + " for user " + download.getUserName() + " with the download name of " + download.getFileName());
            executorBean.executeAsync(download.getPreparedId());
            count++;
        } else {
            logger.info("preparedId " + download.getPreparedId() + " is already polling... skip polling");
        }

        return count;
    }


}
