package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcat.IdsClient;

@Singleton
public class DownloadPreparer {

	private static final Logger logger = LoggerFactory.getLogger(DownloadPreparer.class);

	private AtomicBoolean busy = new AtomicBoolean(false);

	@EJB
	private DownloadRepository downloadRepository;


    @Schedule(hour="*", minute="*", second="*")
    public void prepareDownloads(){
    	if(!busy.compareAndSet(false, true)){
            return;
        }

        try {

	        Map<String, Object> params = new HashMap<String, Object>();
	        params.put("queryOffset", "download.status = org.icatproject.topcat.domain.DownloadStatus.PREPARING");

	    	for(Download download : downloadRepository.getDownloads(params)){
	    		prepareDownload(download);
	    	}

    	} catch(Exception e){
    		logger.error(e.getMessage());
    	} finally {
    		busy.set(false);
    	}
    	
	}


	private void prepareDownload(Download download) throws Exception {
		IdsClient idsClient = new IdsClient(download.getTransportUrl());
		String preparedId = idsClient.prepareData(download.getPreparedId(), download.getInvestigationIds(), download.getDatasetIds(), download.getDatafileIds());
		long size = idsClient.getSize(download.getPreparedId(), download.getInvestigationIds(), download.getDatasetIds(), download.getDatafileIds());
		download.setPreparedId(preparedId);
		if (download.getIsTwoLevel() || !download.getTransport().equals("https")) {
			download.setStatus(DownloadStatus.RESTORING);
		} else {
			download.setStatus(DownloadStatus.COMPLETE);
			download.setCompletedAt(new Date());
		}

		downloadRepository.save(download);
	}

}