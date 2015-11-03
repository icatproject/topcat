package org.icatproject.topcat.statuscheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.icatproject.ids.client.BadRequestException;
import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.ids.client.InsufficientPrivilegesException;
import org.icatproject.ids.client.InternalException;
import org.icatproject.ids.client.NotFoundException;
import org.icatproject.ids.client.NotImplementedException;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.utils.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollStatusWorker {
    private static final Logger logger = LoggerFactory.getLogger(PollStatusWorker.class);

    private String preparedId;
    private String filePath;
    private File file;
    private IdsClient ids;
    private DownloadRepository downloadRepository;
    private PollBean pollBean;
    private List<Long> fileIds;
    private int maxPerStatus;
    private int pollIsPreparedWait;

    public PollStatusWorker(String preparedId, DownloadRepository downloadRepository, PollBean pollBean) throws IOException, InternalException, BadRequestException, NotFoundException, NotImplementedException {
        this.preparedId = preparedId;
        this.downloadRepository = downloadRepository;
        this.pollBean = pollBean;

        setFileIds(new ArrayList<Long>());

        Download download = downloadRepository.getDownloadsByPreparedId(preparedId);

        logger.info("CheckStatusWorker called");

        PropertyHandler properties = PropertyHandler.getInstance();

        String icatUrl = download.getIcatUrl();
        String idsUrl = download.getTransportUrl();
        String prepared_file_directory = properties.getPath();

        this.maxPerStatus = properties.getMaxPerGetStatus();
        this.pollIsPreparedWait = properties.getPollDelay();

        logger.info("CheckStatusWorker params: " + icatUrl + " " + idsUrl + " " + prepared_file_directory);


        if (prepared_file_directory.matches("/$")) {
            this.filePath = prepared_file_directory + preparedId;
        } else {
            this.filePath = prepared_file_directory + "/" + preparedId;
        }

        file = new File(filePath);

        this.ids = getIdsClient(idsUrl);

        //create file if it doesn't already exists
        if (! file.exists()) {
            this.fileIds = getFileIdsFromIDS();
            writePreparedFile(file, this.fileIds);
        }
    }



    public void writePreparedFile(File file, List<Long> fileIds) throws IOException {
        logger.info("writing file");

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        for (Long fileId : fileIds) {
            bw.write(fileId.toString());
            bw.newLine();
        }
        bw.close();
    }



    public List<Long> getFileIdsFromIDS() throws InternalException, BadRequestException, NotFoundException, NotImplementedException {
        logger.info("getting datafileIds for " + preparedId);

        List<Long> fileIds = ids.getDatafileIds(preparedId);

        if (fileIds == null || fileIds.isEmpty()) {
            logger.info("returned list is empty");
        } else {
            logger.info(preparedId + " has " + fileIds.size() + " files");
        }

        return fileIds;
    }

    public boolean checkStatus() throws IOException, BadRequestException, NotFoundException, InsufficientPrivilegesException, InternalException, NotImplementedException, InterruptedException {
        if (! file.exists()) {
            return true;
        }

        BufferedReader br = new BufferedReader(new FileReader(filePath));

        String line;
        List<Long> fileIds = new ArrayList<Long>();

        //read file into linkhashmap
        while ((line = br.readLine()) != null) {
           fileIds.add(new Long(line));
        }
        br.close();

        //make copy of array
        List<Long> fileIdsCopy = new ArrayList<Long>(fileIds);

        List<List<Long>> s = ListUtils.partition(fileIdsCopy, maxPerStatus);

        for (List<Long> datafiles : s) {
            DataSelection dataSelection = new DataSelection();
            dataSelection.addDatafiles(datafiles);

            Status status = ids.getStatus(null, dataSelection);

            if (status.equals(Status.ONLINE)) {
                logger.info(datafiles.toString() + " is ONLINE for " + this.preparedId);
                //remove the ids from fileIds list
                fileIds.removeAll(datafiles);
            } else if (status.equals(Status.ARCHIVED)) {
                ids.isPrepared(this.preparedId);
                logger.info("isPrepared called for " + this.preparedId + ". Sleeping for " + pollIsPreparedWait + " milliseconds");
                Thread.sleep(pollIsPreparedWait);
                break;
            } else if (status.equals(Status.RESTORING)){
                logger.info(datafiles.toString() + " is RESTORING for " + this.preparedId + ". Breaking from loop");
                break;
            } else {
                logger.info(datafiles.toString() + " is UNKNOWN for " + this.preparedId + ". Breaking from loop");
                break;
            }
        }

        if (fileIds.isEmpty()) {
            logger.info(this.preparedId + " file is empty, marking download as available");

            Map<String, String> params = new HashMap<String, String>();
            params.put("preparedId", this.preparedId);

            downloadRepository.setCompleteByPreparedId(params);
            pollBean.remove(this.preparedId);

            File file = new File(this.filePath);
            boolean result = false;

            if (file.exists()) {
                logger.info(this.preparedId + " is available, deleting file" + this.filePath);
                result = file.delete();
            }

            if (result) {
                logger.info("delete successful");
            } else {
                logger.info("delete failed");
            }

            return true;
        } else {
            logger.info("not empty, writing file");
            writePreparedFile(file, fileIds);

            return false;
        }




        //iterate fileids and check it status using getStatus from the ids
        /*
        Iterator<Long> i = fileIds.iterator();

        while (i.hasNext()) {
            Long fileId = i.next();

            DataSelection dataSelection = new DataSelection();
            dataSelection.addDatafile(fileId);

            Status status = ids.getStatus(null, dataSelection);

            if (status.equals(Status.ONLINE)) {
                logger.info(fileId + " is ONLINE for " + this.preparedId);
                i.remove();
            } else if (status.equals(Status.ARCHIVED)) {
                logger.info(fileId + " is ARCHIVED for  "+ this.preparedId + " calling isPrepared in 5 minutes");
                Thread.sleep(300000);
                ids.isPrepared(this.preparedId);
                logger.info("isPrepared called for " + this.preparedId + ". Sleeping for 5 minutes");
                Thread.sleep(300000);
                break;
            } else if (status.equals(Status.RESTORING)){
                logger.info(fileId + " is RESTORING for " + this.preparedId + ". Breaking from loop");
                break;
            } else {
                logger.info(fileId + " is UNKNOWN for " + this.preparedId + ". Breaking from loop");
                break;
            }
        }

        if (fileIds.isEmpty()) {
            logger.info(this.preparedId + " file is empty, marking download as available");

            Map<String, String> params = new HashMap<String, String>();
            params.put("preparedId", this.preparedId);

            downloadRepository.setCompleteByPreparedId(params);
            pollBean.remove(this.preparedId);

            File file = new File(this.filePath);
            boolean result = false;

            if (file.exists()) {
                logger.info(this.preparedId + " is available, deleting file" + this.filePath);
                result = file.delete();
            }

            if (result) {
                logger.info("delete successful");
            } else {
                logger.info("delete failed");
            }

            return true;
        } else {
            logger.info("not empty, writing file");
            writePreparedFile(file, fileIds);

            return false;
        }
        */
    }

    private IdsClient getIdsClient(String idsUrl) throws MalformedURLException {
        URL idsURL = new URL(idsUrl);
        ids = new IdsClient(idsURL);

        return ids;
    }

    public String getPreparedId() {
        return preparedId;
    }


    public void setPreparedId(String preparedId) {
        this.preparedId = preparedId;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }



    public int getMaxPerStatus() {
        return maxPerStatus;
    }



    public void setMaxPerStatus(int maxPerStatus) {
        this.maxPerStatus = maxPerStatus;
    }



    public int getPollIsPreparedWait() {
        return pollIsPreparedWait;
    }



    public void setPollIsPreparedWait(int pollIsPreparedWait) {
        this.pollIsPreparedWait = pollIsPreparedWait;
    }

}

