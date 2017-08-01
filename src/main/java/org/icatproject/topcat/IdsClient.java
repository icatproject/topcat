package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.icatproject.topcat.httpclient.*;
import org.icatproject.topcat.exceptions.*;
import org.icatproject.topcat.Properties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdsClient {

    private Logger logger = LoggerFactory.getLogger(IdsClient.class);

    private HttpClient httpClient;

    private int timeout;
   
    public IdsClient(String url){
        this.httpClient = new HttpClient(url + "/ids");
        Properties properties = Properties.getInstance();
        this.timeout = Integer.valueOf(properties.getProperty("ids.timeout", "-1"));
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
            data.append("&zip=true");
            if(investigationIdsBuffer.length() > 0){
                data.append("&investigationIds=" + investigationIdsBuffer);
            }
            if(datasetIdsBuffer.length() > 0){
                data.append("&datasetIds=" + datasetIdsBuffer);
            }
            if(datafileIdsBuffer.length() > 0){
                data.append("&datafileIds=" + datafileIdsBuffer);
            }

            Response out = httpClient.post("prepareData", new HashMap<String, String>(), data.toString(), timeout);
            if(out.getCode() == 404){
                throw new NotFoundException("Could not prepareData got a 404 response");
            } else if(out.getCode() >= 400){
                throw new BadRequestException("Could not prepareData got " + out.getCode() + " response: " + out.toString());
            }

            return out.toString();
        } catch(TopcatException e){
            throw e;
        } catch (Exception e){
            throw new BadRequestException(e.getMessage());
        }
    }

    public boolean isPrepared(String preparedId) throws TopcatException, IOException {
        try {
            Response response = httpClient.get("isPrepared?zip=true&preparedId=" + preparedId, new HashMap<String, String>(), timeout);
            if(response.getCode() == 404){
                throw new NotFoundException("Could not run isPrepared got a 404 response");
            } else if(response.getCode() >= 400){
                throw new BadRequestException(Utils.parseJsonObject(response.toString()).getString("message"));
            }
            return response.toString().equals("true");
        } catch(IOException e){
            throw e;
        } catch (TopcatException e){
            throw e;
        } catch (Exception e){
            throw new BadRequestException(e.getMessage());
        }
    }

    public boolean isTwoLevel() throws TopcatException {
        try {
            Response response = httpClient.get("isTwoLevel", new HashMap<String, String>());

            if(response.getCode() == 404){
                throw new NotFoundException("Could not run isTwoLevel got a 404 response");
            } else if(response.getCode() >= 400){
                throw new BadRequestException(Utils.parseJsonObject(response.toString()).getString("message"));
            }

            return response.toString().equals("true");
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

}
