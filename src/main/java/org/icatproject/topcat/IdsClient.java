package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.icatproject.topcat.httpclient.*;

public class IdsClient {

    private HttpClient httpClient;
   
    public void IdsClient(String url){
        this.httpClient = new HttpClient(url + "/ids");
    }

    //public String prepareData(String sessionId, DataSelection dataSelection, Flag flags)

    public boolean isPrepared(String preparedId) throws Exception {
        return httpClient.get("isPrepared?preparedId=" + preparedId, new HashMap<String, String>()).toString().equals("true");
    }

    public String getStatus(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) throws Exception {
        String out = "ONLINE";

        for(String offset : chunkOffsets("getStatus?sessionId=" + sessionId + "&", investigationIds, datasetIds, datafileIds)){
            String result = httpClient.get(offset, new HashMap<String, String>()).toString();
            if(result.equals("RESTORING")){
                return "RESTORING";
            } else if(result.equals("ARCHIVED")){
                out = "ARCHIVED";
            }
        }

        return out;
    }

    public Long getSize(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) throws Exception {
        Long out = 0L;

        for(String offset : chunkOffsets("getSize?sessionId=" + sessionId + "&", investigationIds, datasetIds, datafileIds)){
            out += Long.parseLong(httpClient.get(offset, new HashMap<String, String>()).toString());
        }

        return out;
    }

    public boolean isTwoLevel() throws Exception {
        return httpClient.get("isTwoLevel", new HashMap<String, String>()).toString().equals("true");
    }

    private List<String> chunkOffsets(String offsetPrefix, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds){
        List<String> out = new ArrayList<String>();
        StringBuilder buffer = null;

        List<Long> currentInvestigationIds = new ArrayList<Long>();
        List<Long> currentDatasetIds = new ArrayList<Long>();
        List<Long> currentDatafileIds = new ArrayList<Long>();

        Long newInvestigationId;
        Long newDatasetId;
        Long newDatafileId;

        while(true){
            newInvestigationId = null;
            newDatasetId = null;
            newDatafileId = null;


            if(investigationIds.size() > 0){
                newInvestigationId = investigationIds.get(0);
            } else if(datasetIds.size() > 0){
                newInvestigationId = datasetIds.get(0);
            } else if(investigationIds.size() > 0){
                newDatafileId = datafileIds.get(0);
            } else {
                break;
            }

            String offset = generateDataSelectionOffset(offsetPrefix, currentInvestigationIds, newInvestigationId, currentDatasetIds, newDatasetId, currentDatafileIds, newDatafileId);

            if(offset.length() > 1024){
                out.add(generateDataSelectionOffset(offsetPrefix, currentInvestigationIds, null, currentDatasetIds, null, currentDatafileIds, null));
                currentInvestigationIds = new ArrayList<Long>();
                currentDatasetIds = new ArrayList<Long>();
                currentDatafileIds = new ArrayList<Long>();

            } else if(newInvestigationId != null){
                currentInvestigationIds.add(newInvestigationId);
            } else if(newDatasetId != null){
                currentDatasetIds.add(newDatasetId);
            } else if(newDatafileId != null){
                currentDatafileIds.add(newDatafileId);
            }

        }

        return out;
    }

    private String generateDataSelectionOffset(
        String offsetPrefix,
        List<Long> investigationIds, Long newInvestigationId,
        List<Long> datasetIds, Long newDatasetId,
        List<Long> datafileIds, Long newDatafileId){

        StringBuffer investigationIdsBuffer = new StringBuffer();
        StringBuffer datasetIdsBuffer = new StringBuffer();
        StringBuffer datafileIdsBuffer = new StringBuffer();

        if(newInvestigationId != null){
            investigationIdsBuffer.append(newInvestigationId);
        }
        if(investigationIds != null){
            for(Long investigationId : investigationIds){
                if(investigationIdsBuffer.length() != 0){
                    investigationIdsBuffer.append(",");
                }
                investigationIdsBuffer.append(investigationId);
            }
        }

        if(newDatasetId != null){
            datasetIdsBuffer.append(newDatasetId);
        }
        if(datasetIds != null){
            for(Long datasetId : datasetIds){
                if(datasetIdsBuffer.length() != 0){
                    datasetIdsBuffer.append(",");
                }
                datasetIdsBuffer.append(datasetId);
            }
        }

        if(newDatafileId != null){
            datafileIdsBuffer.append(newDatafileId);
        }
        if(datafileIds != null){
            for(Long datafileId : datafileIds){
                if(datafileIdsBuffer.length() != 0){
                    datafileIdsBuffer.append(",");
                }
                datafileIdsBuffer.append(datafileId);
            }
        }

        StringBuffer idsBuffer = new StringBuffer();
        if(investigationIdsBuffer.length() > 0){
            idsBuffer.append("investigationIds=" + investigationIdsBuffer);
        }
        if(datasetIdsBuffer.length() > 0){
            if(idsBuffer.length() > 0){
                idsBuffer.append("&");
            }
            idsBuffer.append("datasetIds=" + datasetIdsBuffer);
        }
        if(datafileIdsBuffer.length() > 0){
            if(idsBuffer.length() > 0){
                idsBuffer.append("&");
            }
            idsBuffer.append("datafileIds=" + datafileIdsBuffer);
        }

        return offsetPrefix + idsBuffer;
    }

}
