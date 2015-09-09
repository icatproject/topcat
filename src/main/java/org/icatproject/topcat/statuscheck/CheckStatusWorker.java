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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.icatproject.ids.client.BadRequestException;
import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.ids.client.InsufficientPrivilegesException;
import org.icatproject.ids.client.InternalException;
import org.icatproject.ids.client.NotFoundException;
import org.icatproject.ids.client.NotImplementedException;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.IdsReader;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.utils.PropertyHandler;
import org.icatproject_4_5_0.ICAT;
import org.icatproject_4_5_0.ICATService;
import org.icatproject_4_5_0.IcatException_Exception;
import org.icatproject_4_5_0.Login.Credentials;
import org.icatproject_4_5_0.Login.Credentials.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckStatusWorker {
    private static final Logger logger = LoggerFactory.getLogger(CheckStatusWorker.class);

    private String preparedId;
    private String filePath;
    private File file;
    private IdsClient ids;
    private String sessionId;
    private ICAT service;
    private DownloadRepository downloadRepository;
    private List<Long> fileIds;

    public CheckStatusWorker(String preparedId, DownloadRepository downloadRepository) throws IOException, IcatException_Exception, InternalException, BadRequestException, NotFoundException, NotImplementedException {
        this.preparedId = preparedId;
        this.downloadRepository = downloadRepository;
        setFileIds(new ArrayList<Long>());

        Download download = downloadRepository.getDownloadsByPreparedId(preparedId);

        logger.info("CheckStatusWorker called");

        PropertyHandler properties = PropertyHandler.getInstance();

        String icatUrl = download.getIcatUrl();
        String idsUrl = download.getTransportUrl();
        IdsReader idsReader = properties.getIdsReaders().get(download.getFacilityName());
        String prepared_file_directory = properties.getPath();

        logger.info("CheckStatusWorker params: " + icatUrl + " " + idsUrl + " " + prepared_file_directory +  " " + idsReader.getAuthenticatorType() + " " +
                idsReader.getUserName() + " " + idsReader.getPassword());


        if (prepared_file_directory.matches("/$")) {
            this.filePath = prepared_file_directory + preparedId;
        } else {
            this.filePath = prepared_file_directory + "/" + preparedId;
        }

        file = new File(filePath);


        this.sessionId = getSessionId(idsReader, icatUrl);
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
        logger.info("getting datafiles for " + preparedId);

        List<Long> fileIds = ids.getDatafileIds(preparedId);

        if (this.fileIds == null || this.fileIds.isEmpty()) {
            logger.info("returned list is empty");
        } else {
            for(Long id : fileIds) {
                logger.info("id:" + id);
            }
        }

        return fileIds;
    }

    public boolean checkStatus() throws IOException, BadRequestException, NotFoundException, InsufficientPrivilegesException, InternalException, NotImplementedException, IcatException_Exception, InterruptedException {
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

        //iterate fileids and check it status using getStatus from the ids
        Iterator<Long> i = fileIds.iterator();
        while (i.hasNext()) {
            Long fileId = i.next();

            DataSelection dataSelection = new DataSelection();
            dataSelection.addDatafile(fileId);

            Status status = ids.getStatus(null, dataSelection);

            if (status.equals(Status.ONLINE)) {
                logger.info(fileId + " is online, removing");
                i.remove();
            } else if (status.equals(Status.ARCHIVED)) {
                logger.info("Is archive, calling isPrepared in 5 minutes for preparedId " + this.preparedId);
                Thread.sleep(300000);
                ids.isPrepared(this.preparedId);
                logger.info("isPrepared called sleeping for 5 minutes");
                Thread.sleep(300000);
                break;
            } else {
                //if not online as false and exit the loop
                logger.info(fileId + " not online, breaking from loop");
                break;
            }
        }

        if (fileIds.isEmpty()) {
            logger.info("is empty, marking download as available");

            Map<String, String> params = new HashMap<String, String>();
            params.put("preparedId", this.preparedId);

            downloadRepository.setCompleteByPreparedId(params);

            File file = new File(this.filePath);
            boolean result = false;

            if (file.exists()) {
                logger.info("preparedId " + this.preparedId + " is available, deleting file" + this.filePath);
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
            //refresh session
            service.refresh(sessionId);

            return false;
        }
    }


    private String getSessionId(IdsReader idsReader, String icatUrl) throws IcatException_Exception, MalformedURLException {
        logger.info(idsReader.getFacilityName() + " " + idsReader.getUserName() + " " + icatUrl);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(idsReader.getUserNameKey(), idsReader.getUserName());
        parameters.put(idsReader.getPasswordKey(), idsReader.getPassword());


        if (!icatUrl.matches(".*/ICATService/ICAT\\?wsdl$")) {
            if (icatUrl.matches(".*/$")) {
                icatUrl = icatUrl + "ICATService/ICAT?wsdl";
            } else {
                icatUrl = icatUrl + "/ICATService/ICAT?wsdl";
            }
        }
        URL url = new URL(icatUrl);

        service = new ICATService(url, new QName("http://icatproject.org", "ICATService")).getICATPort();

        Credentials credentials = new Credentials();
        List<Entry> entries = credentials.getEntry();
        for (String key : parameters.keySet()) {
            Entry entry = new Entry();
            entry.setKey(key);
            entry.setValue(parameters.get(key));
            entries.add(entry);
        }
        String sessionId = service.login(idsReader.getAuthenticatorType(), credentials);

        logger.info("User: "+ idsReader.getUserName() + " sessionId: " + sessionId);

        return sessionId;

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

}

