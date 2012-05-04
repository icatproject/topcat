/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.icatclient.v401;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.exception.ICATMethodNotFoundException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigator;
import uk.ac.stfc.topcat.core.gwt.module.TPublication;
import uk.ac.stfc.topcat.core.gwt.module.TShift;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;

/**
 * 
 */
public class ICATInterfacev401 extends ICATWebInterfaceBase {
    private ICAT service;
    private String serverName;

    public ICATInterfacev401(String serverURL, String serverName) throws MalformedURLException {
        service = new ICATService(new URL(serverURL), new QName("client.icat3.uk", "ICATService")).getICATPort();
        this.serverName = serverName;
    }

    public String loginLifetime(String username, String password, int hours) throws AuthenticationException {
        String result = new String();
        try {
            result = service.loginLifetime(username, password, hours);
        } catch (SessionException_Exception ex) {
            throw new AuthenticationException("ICAT Server not available");
        } catch (javax.xml.ws.WebServiceException ex) {
            throw new AuthenticationException("ICAT Server not available");
        } catch (IcatInternalException_Exception e) {
        }
        return result;
    }

    public void logout(String sessionId) throws AuthenticationException {
        try {
            service.logout(sessionId);
        } catch (javax.xml.ws.WebServiceException ex) {
            throw new AuthenticationException("ICAT Server not available");
        }
    }

    public Boolean isSessionValid(String sessionId) {
        try {
            return new Boolean(service.isSessionValid(sessionId));
        } catch (javax.xml.ws.WebServiceException ex) {
        }
        return Boolean.FALSE;
    }

    public String getUserSurname(String sessionId, String userId) {
        // try {
        // FacilityUser user = service.getFacilityUserByFederalId(sessionId,
        // userId);
        // String surname = user.getLastName();
        // if (surname == null)
        // return userId;
        // return surname;
        // } catch (NoSuchObjectFoundException_Exception ex) {
        // } catch (javax.xml.ws.WebServiceException ex) {
        // } catch (SessionException_Exception ex) {
        // } TODO
        return userId;
    }

    public ArrayList<String> listInstruments(String sessionId) {
        return searchList(sessionId, "DISTINCT Instrument.fullName");
    }

    public ArrayList<String> listInvestigationTypes(String sessionId) {
        return searchList(sessionId, "DISTINCT InvestigationType.description");
    }

    public ArrayList<TFacilityCycle> listFacilityCycles(String sessionId) throws ICATMethodNotFoundException {
        ArrayList<TFacilityCycle> facilityCycles = new ArrayList<TFacilityCycle>();
        // try {
        // // Get the ICAT webservice client and call get investigation types
        // List<FacilityCycle> fcList = service.listFacilityCycles(sessionId);
        // for (FacilityCycle fc : fcList) {
        // Date start = new Date();
        // Date end = new Date();
        // if (fc.getStartDate() != null)
        // start = fc.getStartDate().toGregorianCalendar().getTime();
        // if (fc.getFinishDate() != null)
        // end = fc.getFinishDate().toGregorianCalendar().getTime();
        // facilityCycles.add(new TFacilityCycle(fc.getDescription(),
        // fc.getName(), start, end));
        // }
        // } catch (SessionException_Exception ex) {
        // } catch (java.lang.NullPointerException ex) {
        // } catch (Exception ex) {
        // throw new ICATMethodNotFoundException(ex.getMessage());
        // }
        return facilityCycles;
    }

    public ArrayList<TInvestigation> getMyInvestigationsIncludesPagination(String sessionId, int start, int end) {
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        try {
            // TODO use start and end
            List<Object> resultInv = service.search(sessionId, "0, 200 Investigation");
            // List<Investigation> resultInv =
            // service.getMyInvestigationsIncludesPagination(sessionId,
            // InvestigationInclude.NONE, 0, 200);
            for (Object inv : resultInv) {
                investigationList.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
            }
        } catch (SessionException_Exception ex) {
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        } catch (InsufficientPrivilegesException_Exception e) {
        }
        Collections.sort(investigationList);
        return investigationList;
    }

    public TInvestigation getInvestigationDetails(String sessionId, Long investigationId)
            throws AuthenticationException {
        TInvestigation ti = new TInvestigation();
        try {
            Investigation resultInv = (Investigation) service
                    .get(sessionId,
                            "Investigation INCLUDE Publication, InvestigationUser, Instrument, User, Shift, InvestigationParameter, ParameterType",
                            investigationId);
            // Investigation resultInv =
            // service.getInvestigationIncludes(sessionId, investigationId,
            // InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES);
            ti = copyInvestigationToTInvestigation(serverName, resultInv);
            ti.setInstrument((String) resultInv.getInstrument().getFullName()); // TODO
            ti.setProposal(resultInv.getSummary());
            ArrayList<TPublication> publicationList = new ArrayList<TPublication>();
            List<Publication> pubs = resultInv.getPublications();
            for (Publication pub : pubs) {
                publicationList.add(copyPublicationToTPublication(pub));
            }
            ti.setPublications(publicationList);

            ArrayList<TInvestigator> investigatorList = new ArrayList<TInvestigator>();
            List<InvestigationUser> users = resultInv.getInvestigationUsers();
            for (InvestigationUser user : users) {
                investigatorList.add(copyInvestigatorToTInvestigator(user));
            }
            Collections.sort(investigatorList);
            ti.setInvestigators(investigatorList);

            ArrayList<TShift> shiftList = new ArrayList<TShift>();
            List<Shift> shifts = resultInv.getShifts();
            for (Shift shift : shifts) {
                shiftList.add(copyShiftToTShift(shift));
            }
            ti.setShifts(shiftList);

            List<InvestigationParameter> params = resultInv.getInvestigationParameters();
            // TODO currently single parameter need to make into list
            for (InvestigationParameter param : params) {
                ti.setParamName(param.getType().getName());
                if (param.getType().getValueType() == ParameterValueType.NUMERIC) {
                    ti.setParamValue(param.getNumericValue().toString());
                } else if (param.getType().getValueType() == ParameterValueType.STRING) {
                    ti.setParamValue(param.getStringValue());
                } else if (param.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
                    ti.setParamValue(param.getDateTimeValue().toGregorianCalendar().getTime().toString());
                }
            }
        } catch (SessionException_Exception ex) {
            throw new AuthenticationException(ex.getMessage());
        } catch (InsufficientPrivilegesException_Exception e) {
        } catch (NoSuchObjectFoundException_Exception e) {
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        }
        return ti;
    }

    public ArrayList<TInvestigation> searchByAdvancedPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) {
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        // AdvancedSearchDetails inputParams =
        // convertToAdvancedSearchDetails(details);
        //
        // try {
        // List<Investigation> resultInv =
        // service.searchByAdvancedPagination(sessionId, inputParams, 0, 200);
        // List<Investigation> resultInv = service.searchByAdvanced(sessionId,
        // inputParams); // TODO
        // for (Investigation inv : resultInv) {
        // investigationList.add(copyInvestigationToTInvestigation(serverName,
        // inv));
        // }
        // } catch (SessionException_Exception ex) {
        // } catch (BadParameterException_Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IcatInternalException_Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (InsufficientPrivilegesException_Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // Collections.sort(investigationList);
        return investigationList;
    }

    public ArrayList<TDataset> getDatasetsInInvestigation(String sessionId, Long investigationId) {
        ArrayList<TDataset> datasetList = new ArrayList<TDataset>();
        try {
            Investigation resultInv = (Investigation) service.get(sessionId,
                    "Investigation INCLUDE Dataset, DatasetType, DatasetStatus", investigationId);
            List<Dataset> dList = resultInv.getDatasets();
            for (Dataset dataset : dList) {

                datasetList.add(new TDataset(serverName, dataset.getId().toString(), dataset.getName(), dataset
                        .getDescription(), dataset.getType().getName(), "true"));
                // dataset.getStatus().getName())); //TODO
            }
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        }
        return datasetList;
    }

    public ArrayList<TDatasetParameter> getParametersInDataset(String sessionId, Long datasetId) {
        ArrayList<TDatasetParameter> result = new ArrayList<TDatasetParameter>();
        try {
            Dataset ds = (Dataset) service.get(sessionId, "Dataset INCLUDE DatasetParameter, ParameterType", datasetId);
            // service.getDatasetIncludes(sessionId, Long.valueOf(datasetId),
            // DatasetInclude.DATASET_PARAMETERS_ONLY);
            List<DatasetParameter> dsList = ds.getDatasetParameters();
            for (DatasetParameter dsParam : dsList) {
                System.out.println("parameter type: " + dsParam.getType());
                if (dsParam.getType().getValueType() == ParameterValueType.NUMERIC) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getNumericValue().toString()));
                } else if (dsParam.getType().getValueType() == ParameterValueType.STRING) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getStringValue()));
                } else if (dsParam.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getDateTimeValue().toString()));
                } else if (dsParam.getNumericValue() != null) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getNumericValue().toString()));
                } else if (dsParam.getStringValue() != null) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getStringValue()));
                } else if (dsParam.getDateTimeValue() != null) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getDateTimeValue().toString()));
                }
            }
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        } catch (InsufficientPrivilegesException_Exception e) {
        } catch (NoSuchObjectFoundException_Exception e) {
        } catch (SessionException_Exception e) {
        }
        return result;
    }

    public ArrayList<TDatafile> getDatafilesInDataset(String sessionId, Long datasetId) {
        ArrayList<TDatafile> datafileList = new ArrayList<TDatafile>();
        try {
            // resultInv = service.getDatasetIncludes(sessionId,
            // Long.valueOf(datasetId),
            // DatasetInclude.DATASET_AND_DATAFILES_ONLY);
            Dataset resultInv = (Dataset) service.get(sessionId, "Dataset INCLUDE Datafile, DatafileFormat", datasetId);
            List<Datafile> dList = resultInv.getDatafiles();
            for (Datafile datafile : dList) {
                datafileList.add(copyDatafileToTDatafile(serverName, datafile));
            }
        } catch (SessionException_Exception ex) {
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        }
        return datafileList;
    }

    public ArrayList<TDatafileParameter> getParametersInDatafile(String sessionId, Long datafileId) {
        ArrayList<TDatafileParameter> result = new ArrayList<TDatafileParameter>();
        // try {
        // Datafile df = service.getDatafile(sessionId,
        // Long.valueOf(datafileId));
        // List<DatafileParameter> dfList = df.getDatafileParameterCollection();
        // for (DatafileParameter dfParam : dfList) {
        // if (dfParam.getValueType() == ParameterValueType.NUMERIC) {
        // result.add(new
        // TDatafileParameter(dfParam.getDatafileParameterPK().getName(),
        // dfParam
        // .getDatafileParameterPK().getUnits(),
        // dfParam.getNumericValue().toString()));
        // } else if (dfParam.getValueType() == ParameterValueType.STRING) {
        // result.add(new
        // TDatafileParameter(dfParam.getDatafileParameterPK().getName(),
        // dfParam
        // .getDatafileParameterPK().getUnits(), dfParam.getStringValue()));
        // } else if (dfParam.getValueType() ==
        // ParameterValueType.DATE_AND_TIME) {
        // result.add(new
        // TDatafileParameter(dfParam.getDatafileParameterPK().getName(),
        // dfParam
        // .getDatafileParameterPK().getUnits(),
        // dfParam.getDateTimeValue().toString()));
        // }
        // }
        // } catch (SessionException_Exception ex) {
        // } catch (InsufficientPrivilegesException_Exception ex) {
        // } catch (NoSuchObjectFoundException_Exception ex) {
        // }
        return result;
    }

    public String downloadDatafiles(String sessionId, ArrayList<Long> datafileIds) {
        String result = "";
        try {
            result = service.downloadDatafiles(sessionId, datafileIds);
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        } catch (IcatInternalException_Exception e) {
        }
        return result;
    }

    public String downloadDataset(String sessionId, Long datasetId) {
        String result = "";
        try {
            result = service.downloadDataset(sessionId, datasetId);
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        } catch (IcatInternalException_Exception e) {
        }
        return result;
    }

    public ArrayList<String> getKeywordsForUser(String sessionId) {
        return searchList(sessionId, "DISTINCT Keyword.name");
    }

    public ArrayList<String> getKeywordsInInvestigation(String sessionId, Long investigationId) {
        ArrayList<String> keywords = new ArrayList<String>();
        try {
            Investigation inv = (Investigation) service
                    .get(sessionId, "Investigation INCLUDE Keyword", investigationId);
            List<Keyword> resultKeywords = inv.getKeywords();
            // Investigation resultInvestigation. =
            // service.getInvestigationIncludes(sessionId, investigationId,
            // InvestigationInclude.KEYWORDS_ONLY);
            for (Keyword key : resultKeywords) {
                keywords.add(key.getName());
            }
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        }
        return keywords;
    }

    public ArrayList<TInvestigation> searchByKeywords(String sessionId, ArrayList<String> keywords) {
        // call the search using keyword method
        List<Object> resultInvestigations = null;
        ArrayList<TInvestigation> returnTInvestigations = new ArrayList<TInvestigation>();
        String query = "DISTINCT Investigation <-> Keyword[name IN " + getIN(keywords) + "]";
        try {
            resultInvestigations = service.search(sessionId, query);
            for (Object inv : resultInvestigations) {
                returnTInvestigations.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
            }
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        } catch (InsufficientPrivilegesException_Exception e) {
        } catch (SessionException_Exception e) {
        }
        // if (resultInvestigations != null) {
        // // There are some result investigations
        // for (Object inv : resultInvestigations) {
        // returnTInvestigations.add(copyInvestigationToTInvestigation(serverName,
        // (Investigation) inv));
        // }
        // }
        Collections.sort(returnTInvestigations);
        return returnTInvestigations;
    }

    public ArrayList<TDatafile> searchByRunNumber(String sessionId, ArrayList<String> instruments,
            float startRunNumber, float endRunNumber) {
        List<Datafile> resultDatafiles = null;
        ArrayList<TDatafile> returnTDatafiles = new ArrayList<TDatafile>();
        // try {
        // resultDatafiles = service.searchByRunNumber(sessionId, instruments,
        // startRunNumber, endRunNumber);
        // } catch (SessionException_Exception ex) {
        // }
        // if (resultDatafiles != null) { // There are some result
        // investigations
        // for (Datafile datafile : resultDatafiles) {
        // returnTDatafiles.add(copyDatafileToTDatafile(serverName, datafile));
        // }
        // }
        return returnTDatafiles;
    }

    public ArrayList<String> getKeywordsForUserWithStartMax(String sessionId, String partialKey, int numberOfKeywords) {

        ArrayList<String> resultKeywords = new ArrayList<String>();
        // try {
        // resultKeywords.addAll(service.getKeywordsForUserStartWithMax(sessionId,
        // partialKey, numberOfKeywords)); // TODO
        // List<Object> results = service.search(sessionId, "0," +
        // numberOfKeywords + "DISTINCT Keyword.name"); // TODO
        //
        // for (Object keyword : results) {
        // resultKeywords.add((String) keyword);
        // }
        // } catch (BadParameterException_Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IcatInternalException_Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (InsufficientPrivilegesException_Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (SessionException_Exception e) {
        // // TODO Auto-generated catch block
        // }
        return resultKeywords;
    }

    private AdvancedSearchDetails convertToAdvancedSearchDetails(TAdvancedSearchDetails searchDetails) {
        AdvancedSearchDetails resultDetails = new AdvancedSearchDetails();
        try {
            resultDetails.setInvestigationName(searchDetails.getPropostaltitle());
            resultDetails.setInvestigationAbstract(searchDetails.getProposalAbstract());
            resultDetails.getInvestigators().addAll(searchDetails.getInvestigatorNameList());
            GregorianCalendar gc = new GregorianCalendar();
            if (searchDetails.getStartDate() != null) {
                gc.setTime(searchDetails.getStartDate());
                resultDetails.setDateRangeStart(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
            }
            if (searchDetails.getEndDate() != null) {
                gc.setTime(searchDetails.getEndDate());
                resultDetails.setDateRangeEnd(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
            }
            resultDetails.setDatafileName(searchDetails.getDatafileName());
            resultDetails.getInstruments().addAll(searchDetails.getInstrumentList());
            // if (searchDetails.getGrantId() != null) {
            // resultDetails.setGrantId(Long.parseLong(searchDetails.getGrantId()));
            // } TODO
            if (searchDetails.getInvestigationTypeList().size() != 0) {
                resultDetails.setInvestigationType(searchDetails.getInvestigationTypeList().get(0));
            }
            if (searchDetails.getRbNumberStart() != null) {
                resultDetails.setRunStart(Double.valueOf(searchDetails.getRbNumberStart()));
            }
            if (searchDetails.getRbNumberEnd() != null) {
                resultDetails.setRunEnd(Double.valueOf(searchDetails.getRbNumberEnd()));
            }

            resultDetails.setSampleName(searchDetails.getSample());
            resultDetails.getKeywords().addAll(searchDetails.getKeywords());
        } catch (DatatypeConfigurationException ex) {
        }
        return resultDetails;
    }

    private TInvestigation copyInvestigationToTInvestigation(String serverName, Investigation inv) {
        String id = inv.getId().toString();
        Date invStartDate = null;
        Date invEndDate = null;

        if (inv.getStartDate() != null) {
            invStartDate = inv.getStartDate().toGregorianCalendar().getTime();
        }
        if (inv.getEndDate() != null) {
            invEndDate = inv.getEndDate().toGregorianCalendar().getTime();
        }
        return new TInvestigation(id, inv.getName(), serverName, inv.getTitle(), invStartDate, invEndDate,
                inv.getVisitId());
    }

    private TDatafile copyDatafileToTDatafile(String serverName, Datafile datafile) {
        String format = "";
        String formatVersion = "";
        String formatType = "";
        Date createDate = null;
        if (datafile.getDatafileFormat() != null) {
            format = datafile.getDatafileFormat().getName();
            formatVersion = datafile.getDatafileFormat().getVersion();
            formatType = datafile.getDatafileFormat().getType();
        }
        if (datafile.getDatafileCreateTime() != null) {
            createDate = datafile.getDatafileCreateTime().toGregorianCalendar().getTime();
        }
        return new TDatafile(serverName, datafile.getId().toString(), datafile.getName(), Integer.getInteger(datafile
                .getFileSize().toString()), format, formatVersion, formatType, createDate, datafile.getLocation());
    }

    private TPublication copyPublicationToTPublication(Publication pub) {
        return new TPublication(pub.getFullReference(), pub.getId(), pub.getRepository(), pub.getRepositoryId(),
                pub.getUrl());
    }

    private TInvestigator copyInvestigatorToTInvestigator(InvestigationUser investigator) {
        // TODO add missing fields: facilityUser, federalId
        return new TInvestigator("", "", investigator.getUser().getFullName(), investigator.getRole());
    }

    private TShift copyShiftToTShift(Shift shift) {
        return new TShift(shift.getComment(), shift.getStartDate().toGregorianCalendar().getTime(), shift.getEndDate()
                .toGregorianCalendar().getTime());
    }

    public ArrayList<String> searchList(String sessionId, String query) {
        ArrayList<String> returnList = new ArrayList<String>();
        try {
            List<Object> results = service.search(sessionId, query);
            for (Object item : results) {
                returnList.add((String) item);
            }
        } catch (java.lang.NullPointerException ex) {
        } catch (BadParameterException_Exception e) {
        } catch (IcatInternalException_Exception e) {
        } catch (InsufficientPrivilegesException_Exception e) {
        } catch (SessionException_Exception e) {
        }
        return returnList;
    }

    private String getIN(List<String> ele) {
        final StringBuilder infield = new StringBuilder("(");
        for (final String t : ele) {
            if (infield.length() != 1) {
                infield.append(',');
            }
            infield.append('\'').append(t).append('\'');
        }
        infield.append(')');
        return infield.toString();
    }
}
