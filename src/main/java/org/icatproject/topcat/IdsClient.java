package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.icatproject.topcat.httpclient.*;
import org.icatproject.topcat.exceptions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdsClient {

    private Logger logger = LoggerFactory.getLogger(IdsClient.class);

    private HttpClient httpClient;
   
    public IdsClient(String url){
        this.httpClient = new HttpClient(url + "/ids");
    }

    public String prepareData(String sessionId, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) throws TopcatException {
        try {
            StringBuffer investigationIdsBuffer = new StringBuffer();
            StringBuffer datasetIdsBuffer = new StringBuffer();
            StringBuffer datafileIdsBuffer = new StringBuffer();
            
            if(investigationIds != null){
                for(Long investigationId : investigationIds){
                    if(investigationIdsBuffer.length() != 0){
                        investigationIdsBuffer.append(",");
                    }
                    investigationIdsBuffer.append(investigationId);
                }
            }

            if(datasetIds != null){
                for(Long datasetId : datasetIds){
                    if(datasetIdsBuffer.length() != 0){
                        datasetIdsBuffer.append(",");
                    }
                    datasetIdsBuffer.append(datasetId);
                }
            }

            if(datafileIds != null){
                for(Long datafileId : datafileIds){
                    if(datafileIdsBuffer.length() != 0){
                        datafileIdsBuffer.append(",");
                    }
                    datafileIdsBuffer.append(datafileId);
                }
            }

            StringBuffer data = new StringBuffer();
            data.append("sessionId=" + sessionId);
            if(investigationIdsBuffer.length() > 0){
                data.append("&investigationIds=" + investigationIdsBuffer);
            }
            if(datasetIdsBuffer.length() > 0){
                data.append("&datasetIds=" + datasetIdsBuffer);
            }
            if(datafileIdsBuffer.length() > 0){
                data.append("&datafileIds=" + datafileIdsBuffer);
            }

            return httpClient.post("prepareData", new HashMap<String, String>(), data.toString()).toString();
        } catch (Exception e){
            throw new BadRequestException(e.getMessage());
        }
    }

    public boolean isPrepared(String preparedId) throws TopcatException {
        try {
            Response response = httpClient.get("isPrepared?zip=true&preparedId=" + preparedId, new HashMap<String, String>());
            if(response.getCode() >= 400){
                throw new NotFoundException(parseJson(response.toString()).getString("message"));
            }
            return response.toString().equals("true");
        } catch (TopcatException e){
            throw e;
        } catch (Exception e){
            throw new BadRequestException(e.getMessage());
        }
    }

    public boolean isTwoLevel() throws TopcatException {
        try {
            return httpClient.get("isTwoLevel", new HashMap<String, String>()).toString().equals("true");
        } catch (Exception e){
            throw new BadRequestException(e.getMessage());
        }
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
                newDatasetId = datasetIds.get(0);
            } else if(datafileIds.size() > 0){
                newDatafileId = datafileIds.get(0);
            } else {
                break;
            }

            String offset = generateDataSelectionOffset(offsetPrefix, currentInvestigationIds, newInvestigationId, currentDatasetIds, newDatasetId, currentDatafileIds, newDatafileId);

            if(offset.length() > 1024){
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

            if(newInvestigationId != null){
                investigationIds.remove(0);
                newInvestigationId = null;
            } else if(newDatasetId != null){
                datasetIds.remove(0);
                newDatasetId = null;
            } else if(newDatafileId != null){
                datafileIds.remove(0);
                newDatafileId = null;
            }

        }

        if(currentInvestigationIds.size() > 0 || currentDatasetIds.size() > 0 || currentDatafileIds.size() > 0){
            out.add(generateDataSelectionOffset(offsetPrefix, currentInvestigationIds, null, currentDatasetIds, null, currentDatafileIds, null));
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

    //todo: merge into Util methods in 2.3.0
    private JsonObject parseJson(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonObject out = jsonReader.readObject();
        jsonReader.close();
        return out;
    }

}
