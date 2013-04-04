/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.stfc.topcat.icatclient.v341;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigator;
import uk.ac.stfc.topcat.core.gwt.module.TParameter;
import uk.ac.stfc.topcat.core.gwt.module.TPublication;
import uk.ac.stfc.topcat.core.gwt.module.TShift;
import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;

/**
 * 
 * @author sn65
 */
public class ICATInterfacev341 extends ICATWebInterfaceBase {
    private ICAT service;
    private String serverName;

    public ICATInterfacev341(String serverURL, String serverName) throws MalformedURLException {
        service = new ICATService(new URL(serverURL), new QName("client.icat3.uk", "ICATService")).getICATPort();
        this.serverName = serverName;
    }

    @Override
    public String loginLifetime(String authenticationType, Map<String, String> parameters, int hours)
            throws AuthenticationException {
        String result = new String();
        try {
            result = service.loginLifetime(parameters.get("username"), parameters.get("password"), hours);
        } catch (SessionException_Exception ex) {
            throw new AuthenticationException("ICAT Server not available");
        } catch (javax.xml.ws.WebServiceException ex) {
            throw new AuthenticationException("ICAT Server not available");
        }
        return result;
    }

    @Override
    public String loginWithTicket(String authenticationServiceUrl, String ticket) throws AuthenticationException {
        String result = new String();
        try {
            result = service.loginWithCredentials(ticket);
        } catch (javax.xml.ws.WebServiceException e) {
            throw new AuthenticationException("ICAT Server not available");
        } catch (SessionException_Exception e) {
            throw new AuthenticationException("ICAT Server not available");
        }
        return result;
    }

    @Override
    public void logout(String sessionId) throws AuthenticationException {
        try {
            service.logout(sessionId);
        } catch (javax.xml.ws.WebServiceException ex) {
            throw new AuthenticationException("ICAT Server not available");
        }
    }

    @Override
    public Boolean isSessionValid(String sessionId) {
        try {
            return new Boolean(service.isSessionValid(sessionId));
        } catch (javax.xml.ws.WebServiceException ex) {
        }
        return Boolean.FALSE;
    }

    @Override
    public String getUserSurname(String sessionId, String userId) {
        try {
            FacilityUser user = service.getFacilityUserByFederalId(sessionId, userId);
            String surname = user.getLastName();
            if (surname == null)
                return userId;
            return surname;
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (javax.xml.ws.WebServiceException ex) {
        } catch (SessionException_Exception ex) {
        }
        return userId;
    }

    @Override
    public String getUserName(String sessionId) {
        try {
            String surname = service.getUserDetailsFromSessionId(sessionId).getFederalId();
            if (surname == null) {
                return "";
            }
            return surname;
        } catch (NoSuchUserException_Exception e) {
        } catch (SessionException_Exception e) {
        }
        return "";
    }

    @Override
    public ArrayList<String> listInstruments(String sessionId) {
        ArrayList<String> instruments = new ArrayList<String>();
        try {
            instruments.addAll(service.listInstruments(sessionId));
        } catch (java.lang.NullPointerException ex) {
        } catch (SessionException_Exception ex) {
        }
        return instruments;
    }

    @Override
    public ArrayList<String> listInvestigationTypes(String sessionId) {
        ArrayList<String> investigationTypes = new ArrayList<String>();
        try {
            investigationTypes.addAll(service.listInvestigationTypes(sessionId));
        } catch (SessionException_Exception ex) {
        } catch (java.lang.NullPointerException ex) {
        }
        return investigationTypes;
    }

    @Override
    public ArrayList<TFacilityCycle> listFacilityCycles(String sessionId) throws TopcatException {
        ArrayList<TFacilityCycle> facilityCycles = new ArrayList<TFacilityCycle>();
        try {
            // Get the ICAT webservice client and call get investigation types
            List<FacilityCycle> fcList = service.listFacilityCycles(sessionId);
            for (FacilityCycle fc : fcList) {
                Date start = new Date();
                Date end = new Date();
                if (fc.getStartDate() != null)
                    start = fc.getStartDate().toGregorianCalendar().getTime();
                if (fc.getFinishDate() != null)
                    end = fc.getFinishDate().toGregorianCalendar().getTime();
                facilityCycles.add(new TFacilityCycle(fc.getDescription(), fc.getName(), start, end));
            }
        } catch (SessionException_Exception ex) {
        } catch (java.lang.NullPointerException ex) {
        } catch (Exception ex) {
            throw new InternalException(ex.getMessage());
        }
        return facilityCycles;
    }

    @Override
    public ArrayList<TInvestigation> getMyInvestigations(String sessionId) {
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        try {
            List<Investigation> resultInv = service.getMyInvestigations(sessionId);
            for (Investigation inv : resultInv) {
                investigationList.add(copyInvestigationToTInvestigation(serverName, inv));
            }
        } catch (SessionException_Exception ex) {
        }
        Collections.sort(investigationList);
        return investigationList;
    }

    @Override
    public TInvestigation getInvestigationDetails(String sessionId, Long investigationId)
            throws AuthenticationException {
        TInvestigation ti = new TInvestigation();
        try {
            Investigation resultInv = service.getInvestigationIncludes(sessionId, investigationId,
                    InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES);
            ti = copyInvestigationToTInvestigation(serverName, resultInv);
            ti.setInstrument(resultInv.getInstrument());
            ti.setProposal(resultInv.getInvAbstract());
            ArrayList<TPublication> publicationList = new ArrayList<TPublication>();
            List<Publication> pubs = resultInv.getPublicationCollection();
            for (Publication pub : pubs) {
                publicationList.add(copyPublicationToTPublication(pub));
            }
            ti.setPublications(publicationList);

            ArrayList<TInvestigator> investigatorList = new ArrayList<TInvestigator>();
            List<Investigator> investigators = resultInv.getInvestigatorCollection();
            for (Investigator investigator : investigators) {
                investigatorList.add(copyInvestigatorToTInvestigator(investigator));
            }
            Collections.sort(investigatorList);
            ti.setInvestigators(investigatorList);

            ArrayList<TShift> shiftList = new ArrayList<TShift>();
            List<Shift> shifts = resultInv.getShiftCollection();
            for (Shift shift : shifts) {
                shiftList.add(copyShiftToTShift(shift));
            }
            ti.setShifts(shiftList);

            ArrayList<TParameter> parameterList = new ArrayList<TParameter>();
            if (resultInv.getInvParamName() != null) {
                TParameter param = new TParameter();
                param.setName(resultInv.getInvParamName());
                param.setValue(resultInv.getInvParamValue());
                parameterList.add(param);
            }
            ti.setParameters(parameterList);
        } catch (SessionException_Exception ex) {
            throw new AuthenticationException(ex.getMessage());
        } catch (InsufficientPrivilegesException_Exception e) {
        } catch (NoSuchObjectFoundException_Exception e) {
        }
        return ti;
    }

    @Override
    public ArrayList<TInvestigation> searchByAdvancedPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) {
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        AdvancedSearchDetails inputParams = convertToAdvancedSearchDetails(details);

        try {
            List<Investigation> resultInv = service.searchByAdvancedPagination(sessionId, inputParams, 0, 200);
            for (Investigation inv : resultInv) {
                investigationList.add(copyInvestigationToTInvestigation(serverName, inv));
            }
        } catch (SessionException_Exception ex) {
        }
        Collections.sort(investigationList);
        return investigationList;
    }

    @Override
    public ArrayList<TDataset> getDatasetsInInvestigation(String sessionId, Long investigationId) {
        ArrayList<TDataset> datasetList = new ArrayList<TDataset>();
        try {
            Investigation resultInv = service.getInvestigationIncludes(sessionId, Long.valueOf(investigationId),
                    InvestigationInclude.DATASETS_ONLY);
            List<Dataset> dList = resultInv.getDatasetCollection();
            for (Dataset dataset : dList) {
                datasetList.add(new TDataset(serverName, null, dataset.getId().toString(), dataset.getName(), dataset
                        .getDescription(), dataset.getDatasetType(), dataset.getDatasetStatus()));
            }
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        }
        return datasetList;
    }

    @Override
    public ArrayList<TDatasetParameter> getParametersInDataset(String sessionId, Long datasetId) {
        ArrayList<TDatasetParameter> result = new ArrayList<TDatasetParameter>();
        try {
            Dataset ds = service.getDatasetIncludes(sessionId, Long.valueOf(datasetId),
                    DatasetInclude.DATASET_PARAMETERS_ONLY);
            List<DatasetParameter> dsList = ds.getDatasetParameterCollection();
            for (DatasetParameter dsParam : dsList) {
                if (dsParam.getValueType() == ParameterValueType.NUMERIC) {
                    result.add(new TDatasetParameter(dsParam.getDatasetParameterPK().getName(), dsParam
                            .getDatasetParameterPK().getUnits(), dsParam.getNumericValue().toString()));
                } else if (dsParam.getValueType() == ParameterValueType.STRING) {
                    result.add(new TDatasetParameter(dsParam.getDatasetParameterPK().getName(), dsParam
                            .getDatasetParameterPK().getUnits(), dsParam.getStringValue()));
                } else if (dsParam.getValueType() == ParameterValueType.DATE_AND_TIME) {
                    result.add(new TDatasetParameter(dsParam.getDatasetParameterPK().getName(), dsParam
                            .getDatasetParameterPK().getUnits(), dsParam.getDateTimeValue().toString()));
                }
            }
        } catch (SessionException_Exception ex) {
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        }
        return result;
    }

    @Override
    public String getDatasetName(String sessionId, Long datasetId) {
        try {
            Dataset ds = service.getDataset(sessionId, datasetId);
            return ds.getName();
        } catch (SessionException_Exception ex) {
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        }
        return "";
    }

    @Override
    public ArrayList<TDatafile> getDatafilesInDataset(String sessionId, Long datasetId) {
        ArrayList<TDatafile> datafileList = new ArrayList<TDatafile>();
        try {
            Dataset resultInv;
            resultInv = service.getDatasetIncludes(sessionId, Long.valueOf(datasetId),
                    DatasetInclude.DATASET_AND_DATAFILES_ONLY);
            List<Datafile> dList = resultInv.getDatafileCollection();
            for (Datafile datafile : dList) {
                datafileList.add(copyDatafileToTDatafile(serverName, datafile));
            }
        } catch (SessionException_Exception ex) {
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        }
        return datafileList;
    }

    @Override
    public ArrayList<TDatafileParameter> getParametersInDatafile(String sessionId, Long datafileId) {
        ArrayList<TDatafileParameter> result = new ArrayList<TDatafileParameter>();
        try {
            Datafile df = service.getDatafile(sessionId, Long.valueOf(datafileId));
            List<DatafileParameter> dfList = df.getDatafileParameterCollection();
            for (DatafileParameter dfParam : dfList) {
                if (dfParam.getValueType() == ParameterValueType.NUMERIC) {
                    result.add(new TDatafileParameter(dfParam.getDatafileParameterPK().getName(), dfParam
                            .getDatafileParameterPK().getUnits(), dfParam.getNumericValue().toString()));
                } else if (dfParam.getValueType() == ParameterValueType.STRING) {
                    result.add(new TDatafileParameter(dfParam.getDatafileParameterPK().getName(), dfParam
                            .getDatafileParameterPK().getUnits(), dfParam.getStringValue()));
                } else if (dfParam.getValueType() == ParameterValueType.DATE_AND_TIME) {
                    result.add(new TDatafileParameter(dfParam.getDatafileParameterPK().getName(), dfParam
                            .getDatafileParameterPK().getUnits(), dfParam.getDateTimeValue().toString()));
                }
            }
        } catch (SessionException_Exception ex) {
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        }
        return result;
    }

    @Override
    public String downloadDatafiles(String sessionId, ArrayList<Long> datafileIds) {
        String result = "";
        try {
            result = service.downloadDatafiles(sessionId, datafileIds);
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        }
        return result;
    }

    @Override
    public String downloadDataset(String sessionId, Long datasetId) {
        String result = "";
        try {
            result = service.downloadDataset(sessionId, datasetId);
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        }
        return result;
    }

    @Override
    public ArrayList<String> getKeywordsForUser(String sessionId) {
        ArrayList<String> resultKeywords = new ArrayList<String>();
        try {
            resultKeywords.addAll(service.getKeywordsForUser(sessionId));
        } catch (SessionException_Exception ex) {
        }
        return resultKeywords;
    }

    @Override
    public ArrayList<String> getKeywordsInInvestigation(String sessionId, Long investigationId) {
        ArrayList<String> keywords = new ArrayList<String>();
        try {
            Investigation resultInvestigation = service.getInvestigationIncludes(sessionId, investigationId,
                    InvestigationInclude.KEYWORDS_ONLY);
            List<Keyword> resultKeywords = resultInvestigation.getKeywordCollection();
            for (Keyword key : resultKeywords) {
                keywords.add(key.getKeywordPK().getName());
            }
        } catch (InsufficientPrivilegesException_Exception ex) {
        } catch (NoSuchObjectFoundException_Exception ex) {
        } catch (SessionException_Exception ex) {
        }
        return keywords;
    }

    @Override
    public ArrayList<TInvestigation> searchByKeywords(String sessionId, ArrayList<String> keywords) {
        // call the search using keyword method
        List<Investigation> resultInvestigations = null;
        ArrayList<TInvestigation> returnTInvestigations = new ArrayList<TInvestigation>();
        try {
            resultInvestigations = service.searchByKeywords(sessionId, keywords);
        } catch (SessionException_Exception ex) {
        } catch (Exception ex) {
        }

        if (resultInvestigations != null) { // There are some result
                                            // investigations
            for (Investigation inv : resultInvestigations) {
                returnTInvestigations.add(copyInvestigationToTInvestigation(serverName, inv));
            }
        }
        Collections.sort(returnTInvestigations);
        return returnTInvestigations;
    }

    @Override
    public ArrayList<TDatafile> searchDatafilesByRunNumber(String sessionId, ArrayList<String> instruments,
            float startRunNumber, float endRunNumber) {
        List<Datafile> resultDatafiles = null;
        ArrayList<TDatafile> returnTDatafiles = new ArrayList<TDatafile>();
        try {
            resultDatafiles = service.searchByRunNumber(sessionId, instruments, startRunNumber, endRunNumber);
        } catch (SessionException_Exception ex) {
        }
        if (resultDatafiles != null) { // There are some result investigations
            for (Datafile datafile : resultDatafiles) {
                returnTDatafiles.add(copyDatafileToTDatafile(serverName, datafile));
            }
        }
        return returnTDatafiles;
    }

    @Override
    public ArrayList<String> getKeywordsForUserWithStartMax(String sessionId, String partialKey, int numberOfKeywords) {
        ArrayList<String> resultKeywords = new ArrayList<String>();
        try {
            resultKeywords.addAll(service.getKeywordsForUserStartWithMax(sessionId, partialKey, numberOfKeywords));
        } catch (SessionException_Exception ex) {
        }
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
            if (searchDetails.getGrantId() != null) {
                resultDetails.setGrantId(Long.parseLong(searchDetails.getGrantId()));
            }
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
        if (inv.getInvStartDate() != null) {
            invStartDate = inv.getInvStartDate().toGregorianCalendar().getTime();
        }
        if (inv.getInvEndDate() != null) {
            invEndDate = inv.getInvEndDate().toGregorianCalendar().getTime();
        }
        return new TInvestigation(id, inv.getInvNumber(), serverName, inv.getTitle(), invStartDate, invEndDate,
                inv.getVisitId());
    }

    private TDatafile copyDatafileToTDatafile(String serverName, Datafile datafile) {
        String format = "";
        String formatVersion = "";
        String formatType = "";
        Date createDate = null;
        if (datafile.getDatafileFormat() != null) {

            if (datafile.getDatafileFormat().getDatafileFormatPK() != null) {
                format = datafile.getDatafileFormat().getDatafileFormatPK().getName();
                formatVersion = datafile.getDatafileFormat().getDatafileFormatPK().getVersion();
            }
            formatType = datafile.getDatafileFormat().getFormatType();
        }
        if (datafile.getDatafileCreateTime() != null) {
            createDate = datafile.getDatafileCreateTime().toGregorianCalendar().getTime();
        }
        return new TDatafile(serverName, datafile.getId().toString(), datafile.getName(), null, datafile.getFileSize()
                .longValue(), null, format, null, formatVersion, formatType, createDate, null, datafile.getLocation(),
                null);
    }

    private TPublication copyPublicationToTPublication(Publication pub) {
        return new TPublication(pub.getFullReference(), pub.getId(), pub.getRepository(), pub.getRepositoryId(),
                pub.getUrl());
    }

    private TInvestigator copyInvestigatorToTInvestigator(Investigator investigator) {
        StringBuilder fullName = new StringBuilder();
        fullName.append(investigator.getFacilityUser().getTitle());
        fullName.append(" ");
        fullName.append(investigator.getFacilityUser().getFirstName());
        fullName.append(" ");
        fullName.append(investigator.getFacilityUser().getLastName());
        return new TInvestigator(investigator.getFacilityUser().getFacilityUserId(), investigator.getFacilityUser()
                .getFederalId(), fullName.toString(), investigator.getRole());
    }

    private TShift copyShiftToTShift(Shift shift) {
        return new TShift(shift.getShiftComment(), shift.getShiftPK().getStartDate().toGregorianCalendar().getTime(),
                shift.getShiftPK().getEndDate().toGregorianCalendar().getTime());
    }

}
