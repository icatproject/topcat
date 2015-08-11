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
import org.icatproject.topcat.repository.DownloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
@Singleton
public class ExecuteCheck {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteCheck.class);

    @EJB
    private ExecutorBean executorBean;

    @EJB
    private DownloadRepository downloadRepository;

    public ExecuteCheck(){

    }

    public int run() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("status", "RESTORING");
        params.put("transport", "https");
        params.put("isTwoLevel", "true");

        List<Download> downloads = new ArrayList<Download>();

        downloads = downloadRepository.getCheckDownloads(params);

        if (downloads != null) {
            for(Download download : downloads) {
                logger.debug("Run status check for preparedId " + download.getPreparedId() + " for user " + download.getUserName() + " with the download name of " + download.getFileName());

                executorBean.executeAsync(download.getPreparedId());
            }

            return downloads.size();
        }

        logger.debug("No downloads found to intiate check");

        return 0;

    }


}
