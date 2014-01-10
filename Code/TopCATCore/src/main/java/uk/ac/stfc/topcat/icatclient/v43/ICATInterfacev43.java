/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.icatclient.v43;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TConstants;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileFormat;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigator;
import uk.ac.stfc.topcat.core.gwt.module.TParameter;
import uk.ac.stfc.topcat.core.gwt.module.TPublication;
import uk.ac.stfc.topcat.core.gwt.module.TShift;
import uk.ac.stfc.topcat.core.gwt.module.exception.BadParameterException;
import uk.ac.stfc.topcat.core.gwt.module.exception.InsufficientPrivilegesException;
import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.NoSuchObjectException;
import uk.ac.stfc.topcat.core.gwt.module.exception.ObjectAlreadyExistsException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.core.gwt.module.exception.ValidationException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;

import org.icatproject_4_3_0.Datafile;
import org.icatproject_4_3_0.DatafileFormat;
import org.icatproject_4_3_0.DatafileParameter;
import org.icatproject_4_3_0.Dataset;
import org.icatproject_4_3_0.DatasetParameter;
import org.icatproject_4_3_0.DatasetType;
import org.icatproject_4_3_0.Facility;
import org.icatproject_4_3_0.FacilityCycle;
import org.icatproject_4_3_0.ICAT;
import org.icatproject_4_3_0.ICATService;
import org.icatproject_4_3_0.IcatException;
import org.icatproject_4_3_0.IcatExceptionType;
import org.icatproject_4_3_0.IcatException_Exception;
import org.icatproject_4_3_0.Investigation;
import org.icatproject_4_3_0.InvestigationInstrument;
import org.icatproject_4_3_0.InvestigationParameter;
import org.icatproject_4_3_0.InvestigationUser;
import org.icatproject_4_3_0.Keyword;
import org.icatproject_4_3_0.Login.Credentials;
import org.icatproject_4_3_0.Login.Credentials.Entry;
import org.icatproject_4_3_0.ParameterValueType;
import org.icatproject_4_3_0.Publication;
import org.icatproject_4_3_0.Shift;

/**
 *
 */
public class ICATInterfacev43 extends ICATWebInterfaceBase {
    private ICAT service;
    private String serverName;
    private final static Logger logger = Logger.getLogger(ICATInterfacev43.class.getName());

    public ICATInterfacev43(String serverURL, String serverName) throws MalformedURLException {
        logger.info("ICATInterfacev43: serverURL (" + serverURL + "), serverName (" + serverName + ")");
        if (!serverURL.matches(".*/ICATService/ICAT\\?wsdl$")) {
            if (serverURL.matches(".*/$")) {
                serverURL = serverURL + "ICATService/ICAT?wsdl";
            } else {
                serverURL = serverURL + "/ICATService/ICAT?wsdl";
            }
        }
        URL url = new URL(serverURL);
        logger.trace("ICATInterfacev43: Using URL:" + url.toString());
        service = new ICATService(url, new QName("http://icatproject.org", "ICATService")).getICATPort();
        this.serverName = serverName;
    }

    @Override
    public String loginLifetime(String authenticationType, Map<String, String> parameters, int hours)
            throws AuthenticationException {
        logger.info("loginLifetime: authenticationType (" + authenticationType + "), number of parameters "
                + parameters.size());
        String result = new String();
        try {
            // TODO no longer uses hours
            Credentials credentials = new Credentials();
            List<Entry> entries = credentials.getEntry();
            for (String key : parameters.keySet()) {
                Entry entry = new Entry();
                entry.setKey(key);
                entry.setValue(parameters.get(key));
                entries.add(entry);
            }
            result = service.login(authenticationType, credentials);
        } catch (IcatException_Exception ex) {
            // TODO check type
            logger.debug("loginLifetime: IcatException_Exception:" + ex.getMessage());

            throw new AuthenticationException("ICAT Server not available");
        } catch (javax.xml.ws.WebServiceException ex) {
            logger.debug("loginLifetime: WebServiceException:" + ex.getMessage());
            throw new AuthenticationException("ICAT Server not available");
        }
        logger.debug("loginLifetime: result(" + result + ")");
        return result;
    }

    @Override
    public void logout(String sessionId) throws AuthenticationException {
        logger.info("logout: sessionId (" + sessionId + ")");
        try {
            service.logout(sessionId);
        } catch (IcatException_Exception e) {
            // TODO check type
            throw new AuthenticationException("ICAT Server not available");
        } catch (javax.xml.ws.WebServiceException ex) {
            throw new AuthenticationException("ICAT Server not available");
        }
    }

    @Override
    public Boolean isSessionValid(String sessionId) {
        logger.info("isSessionValid: sessionId (" + sessionId + ")");
        try {
            Boolean result = service.getRemainingMinutes(sessionId) > 0;
            if (logger.isTraceEnabled()) {
                logger.trace("isSessionValid: " + result);
            }
            return result;
        } catch (javax.xml.ws.WebServiceException ex) {
        } catch (IcatException_Exception e) {
            IcatException ue = e.getFaultInfo();
            if (ue.getType().equals(IcatExceptionType.SESSION)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("isSessionValid: FALSE");
                }
                return Boolean.FALSE;
            } else {
                // TODO handle other types
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("isSessionValid: FALSE");
        }
        return Boolean.FALSE;
    }

    @Override
    public String getUserSurname(String sessionId, String userId) {
        logger.info("getUserSurname: sessionId (" + sessionId + "), userId (" + userId + ")");
        String name;
        try {
            name = service.getUserName(sessionId);
        } catch (IcatException_Exception e) {
            return userId;
        }
        return name;
    }

    @Override
    public String getUserName(String sessionId) throws TopcatException {
        logger.info("getUserName: sessionId (" + sessionId + ")");
        String name = "";
        try {
            name = service.getUserName(sessionId);
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getUserName");
        } catch (Throwable e) {
            logger.error("getUserName caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getUserName threw an unexpected exception, see server logs for details");
        }
        return name;
    }

    @Override
    public List<TDatafileFormat> listDatafileFormats(String sessionId) throws TopcatException {
        logger.info("listDatafileFormats: sessionId (" + sessionId + ")");
        ArrayList<TDatafileFormat> formatList = new ArrayList<TDatafileFormat>();
        try {
            List<Object> result = (List<Object>) service.search(sessionId, "DISTINCT DatafileFormat");
            for (Object dfFormat : result) {
                formatList.add(new TDatafileFormat(serverName, ((DatafileFormat) dfFormat).getId().toString(),
                        ((DatafileFormat) dfFormat).getName(), ((DatafileFormat) dfFormat).getDescription(),
                        ((DatafileFormat) dfFormat).getVersion(), ((DatafileFormat) dfFormat).getType()));
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "listDatafileFormats");
        } catch (Throwable e) {
            logger.error("listDatafileFormats caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, listDatafileFormats caught an unexpected exception, see server logs for details");
        }
        return formatList;
    }

    @Override
    public List<String> listDatasetTypes(String sessionId) throws TopcatException {
        logger.info("listDatasetTypes: sessionId (" + sessionId + ")");
        return searchList(sessionId, "DISTINCT DatasetType.name", "listDatasetTypes");
    }

    @Override
    public ArrayList<String> listInstruments(String sessionId) throws TopcatException {
        logger.info("listInstruments: sessionId (" + sessionId + ")");
        return searchList(sessionId, "DISTINCT Instrument.fullName", "listInstruments");
    }

    @Override
    public ArrayList<String> listInvestigationTypes(String sessionId) throws TopcatException {
        logger.info("listInvestigationTypes: sessionId (" + sessionId + ")");
        return searchList(sessionId, "DISTINCT InvestigationType.name", "listInvestigationTypes");
    }

    @Override
    public ArrayList<TFacilityCycle> listFacilityCycles(String sessionId) throws TopcatException {
        logger.info("listFacilityCycles: sessionId (" + sessionId + ")");
        ArrayList<TFacilityCycle> facilityCycles = new ArrayList<TFacilityCycle>();
        try {
            List<Object> resultCycle = service.search(sessionId, "FacilityCycle");
            for (Object fc : resultCycle) {
                facilityCycles.add(copyFacilityCycleToTFacilityCycle((FacilityCycle) fc));
            }
        } catch (IcatException_Exception ex) {
            convertToTopcatException(ex, "listFacilityCycles");
        } catch (Throwable e) {
            logger.error("listFacilityCycles: " + e.getMessage());
            throw new InternalException(
                    "Internal error, listFacilityCycles threw an unexpected exception, see server logs for details");
        }
        return facilityCycles;
    }

    @Override
    public ArrayList<TFacilityCycle> listFacilityCyclesForInstrument(String sessionId, String instrument)
            throws TopcatException {
        logger.info("listFacilityCyclesForInstrument: sessionId (" + sessionId + "), instrument (" + instrument + ")");
        ArrayList<TFacilityCycle> facilityCycles = new ArrayList<TFacilityCycle>();
        try {
            String invStartDate = getDate((XMLGregorianCalendar) service.search(sessionId,
                    "MIN (Investigation.startDate) <-> InvestigationInstrument <-> Instrument [fullName='" + instrument + "']").get(0));
            String invEndDate = getDate((XMLGregorianCalendar) service.search(sessionId,
                    "MAX (Investigation.startDate) <-> InvestigationInstrument <-> Instrument [fullName='" + instrument + "']").get(0));
            List<Object> resultCycle = service.search(sessionId, "FacilityCycle [(startDate >= " + invStartDate
                    + " AND startDate <= " + invEndDate + ") OR (endDate >= " + invStartDate + " AND endDate <= "
                    + invEndDate + ")]");
            for (Object fc : resultCycle) {
                facilityCycles.add(copyFacilityCycleToTFacilityCycle((FacilityCycle) fc));
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "listFacilityCyclesForInstrument");
        } catch (IndexOutOfBoundsException e) {
            // There are no investigations accessible to this user for this
            // instrument
        } catch (Throwable e) {
            logger.error("listFacilityCyclesForInstrument caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, listFacilityCyclesForInstrument threw an unexpected exception, see server logs for details");
        }
        Collections.sort(facilityCycles);
        return facilityCycles;
    }

    private TFacilityCycle copyFacilityCycleToTFacilityCycle(FacilityCycle fc) {
        Date start = new Date();
        Date end = new Date();
        if (fc.getStartDate() != null)
            start = fc.getStartDate().toGregorianCalendar().getTime();
        if (fc.getEndDate() != null)
            end = fc.getEndDate().toGregorianCalendar().getTime();
        return new TFacilityCycle(fc.getDescription(), ((FacilityCycle) fc).getName(), start, end);
    }

    @Override
    public ArrayList<TInvestigation> getMyInvestigations(String sessionId) throws TopcatException {
        logger.info("getMyInvestigations: sessionId (" + sessionId + ")");
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        try {
            String name = service.getUserName(sessionId);
            List<Object> resultInv = service.search(sessionId, "Investigation <-> InvestigationUser <-> User[name='"
                    + name + "']");
            for (Object inv : resultInv) {
                investigationList.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
            }
        } catch (IcatException_Exception ex) {
            convertToTopcatException(ex, "getMyInvestigations");
        } catch (Throwable e) {
            logger.error("getMyInvestigations: " + e.getMessage());
            throw new InternalException(
                    "Internal error, getMyInvestigations threw an unexpected exception, see server logs for details");

        }
        Collections.sort(investigationList);
        return investigationList;
    }

    @Override
    public TInvestigation getInvestigationDetails(String sessionId, Long investigationId) throws TopcatException {
        logger.info("getInvestigationDetails: sessionId (" + sessionId + "), investigationId (" + investigationId + ")");
        TInvestigation ti = new TInvestigation();
        try {
            Investigation resultInv = (Investigation) service
                    .get(sessionId,
                            "Investigation inv INCLUDE inv.facility, inv.publications, inv.investigationInstruments.instrument, inv.investigationUsers.user, inv.shifts, inv.parameters.type",
                            investigationId);

            ti = copyInvestigationToTInvestigation(serverName, resultInv);

            List<String> instruments = new ArrayList<String>();
            List<InvestigationInstrument> investigationInstruments = resultInv.getInvestigationInstruments();
            for (InvestigationInstrument investigationInstrument :  investigationInstruments) {
                instruments.add(investigationInstrument.getInstrument().getFullName());
            }
            ti.setInstruments(instruments);

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

            ArrayList<TParameter> parameterList = new ArrayList<TParameter>();
            List<InvestigationParameter> params = resultInv.getParameters();
            for (InvestigationParameter param : params) {
                parameterList.add(copyParameterToTParameter(param));
            }
            ti.setParameters(parameterList);
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getInvestigationDetails");
        } catch (Throwable e) {
            logger.error("getInvestigationDetails caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getInvestigationDetails threw an unexpected exception, see server logs for details");
        }
        return ti;
    }

    private TParameter copyParameterToTParameter(InvestigationParameter param) {
        TParameter tp = new TParameter();
        tp.setName(param.getType().getName());
        if (param.getType().getValueType() == ParameterValueType.NUMERIC) {
            tp.setValue(param.getNumericValue().toString());
        } else if (param.getType().getValueType() == ParameterValueType.STRING) {
            tp.setValue(param.getStringValue());
        } else if (param.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
            tp.setValue(param.getDateTimeValue().toString());
        }
        tp.setUnits(param.getType().getUnits());
        return tp;
    }

    @Override
    public ArrayList<TInvestigation> searchByAdvancedPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) throws TopcatException {
        logger.info("searchByAdvancedPagination: sessionId (" + sessionId + ", details, start (" + start + ", end ("
                + end + ")");
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();
        String query = getAdvancedQuery(sessionId, details);
        logger.info("advanced query: " + query);
        List<Object> resultInv = null;
        try {
            resultInv = service.search(sessionId, start + ", " + end + query);
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "searchByAdvancedPagination");
        } catch (Throwable e) {
            logger.error("searchByAdvancedPagination caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, searchByAdvancedPagination threw an unexpected exception, see server logs for details");
        }
        for (Object inv : resultInv) {
            logger.info("*********search result*********");            
            investigationList.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
        }
        Collections.sort(investigationList);
        return investigationList;
    }


    public ArrayList<TInvestigation> searchByFreeTextPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) throws TopcatException {
        logger.info("searchByFreeTextPagination: sessionId (" + sessionId + " query: " + details.getFreeTextQuery());
        ArrayList<TInvestigation> investigationList = new ArrayList<TInvestigation>();

        String query = details.getFreeTextQuery().trim();

        logger.info("paginate query:" + query);
        
        List<Object> resultInv = null;
        try {
            resultInv = service.searchText(sessionId, query, 200, "Investigation");
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "searchByFreeTextPagination");
        } catch (Throwable e) {
            logger.error("searchByFreeTextPagination caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, searchByFreeTextPagination threw an unexpected exception, see server logs for details");
        }
        
        //searchText() can only give the investigation table without the facility name information 
        //which is required for display. We need to perform another query to get all the 
        //investigations found form searchText()
        List<Long> investigationIds = new ArrayList<Long>();
        
        //get list of investigation ids
        for (Object inv : resultInv) {
            Investigation invTemp = (Investigation) inv;
            investigationIds.add(invTemp.getId());
        }
        
        //build in string for query
        String investigationIdList = "";
        investigationIdList = getINLong(investigationIds);
        
        List<Object> result = null;
        
        //search query 
        String jPQLQuery = "SELECT inv FROM Investigation inv WHERE inv.id IN " + investigationIdList + " INCLUDE inv.facility";
        
        //perform search
        try {
            result = service.search(sessionId, jPQLQuery);
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "searchByFreeTextPagination");
        } catch (Throwable e) {
            logger.error("searchByFreeTextPagination caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, searchByFreeTextPagination threw an unexpected exception, see server logs for details");
        }
        
        for (Object inv : result) {            
            investigationList.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
        }
        
        Collections.sort(investigationList);

        logger.info("result count:" + investigationList.size());

        return investigationList;
    }


    @Override
    public ArrayList<TDataset> getDatasetsInInvestigation(String sessionId, Long investigationId)
            throws TopcatException {
        logger.info("getDatasetsInInvestigation: sessionId (" + sessionId + "), " + investigationId + ")");
        ArrayList<TDataset> datasetList = new ArrayList<TDataset>();
        try {
            Investigation resultInv = (Investigation) service.get(sessionId,
                    "Investigation INCLUDE Dataset, DatasetType", investigationId);
            List<Dataset> dList = resultInv.getDatasets();
            for (Dataset dataset : dList) {
                String status;
                if (dataset.isComplete()) {
                    status = "complete";
                } else {
                    status = "in progress";
                }
                datasetList.add(new TDataset(serverName, null, dataset.getId().toString(), dataset.getName(), dataset
                        .getDescription(), dataset.getType().getName(), status));
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getDatasetsInInvestigation");
        } catch (Throwable e) {
            logger.error("getDatasetsInInvestigation caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getDatasetsInInvestigation threw an unexpected exception, see server logs for details");
        }
        return datasetList;
    }

    @Override
    public ArrayList<TDatasetParameter> getParametersInDataset(String sessionId, Long datasetId) throws TopcatException {
        logger.info("getParametersInDataset: sessionId (" + sessionId + "), datasetId (" + datasetId + ")");
        ArrayList<TDatasetParameter> result = new ArrayList<TDatasetParameter>();
        try {
            Dataset ds = (Dataset) service.get(sessionId, "Dataset INCLUDE DatasetParameter, ParameterType", datasetId);
            List<DatasetParameter> dsList = ds.getParameters();
            for (DatasetParameter dsParam : dsList) {
                if (dsParam.getType().getValueType() == ParameterValueType.NUMERIC) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getNumericValue().toString()));
                } else if (dsParam.getType().getValueType() == ParameterValueType.STRING) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getStringValue()));
                } else if (dsParam.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
                    result.add(new TDatasetParameter(dsParam.getType().getName(), dsParam.getType().getUnits(), dsParam
                            .getDateTimeValue().toString()));
                }
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getParametersInDataset");
        } catch (Throwable e) {
            logger.error("getParametersInDataset caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getParametersInDataset threw an unexpected exception, see server logs for details");
        }
        return result;
    }

    @Override
    public String getDatasetName(String sessionId, Long datasetId) throws TopcatException {
        logger.info("getDatasetName: sessionId (" + sessionId + "), datasetId (" + datasetId + ")");
        try {
            Dataset ds = (Dataset) service.get(sessionId, "Dataset", datasetId);
            return ds.getName();
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getDatasetName");
        } catch (Throwable e) {
            logger.error("getDatasetName caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getDatasetName threw an unexpected exception, see server logs for details");
        }
        return "";
    }

    @Override
    public ArrayList<TDatafile> getDatafilesInDataset(String sessionId, Long datasetId) throws TopcatException {
        logger.info("getDatafilesInDataset: sessionId (" + sessionId + "), datasetId (" + datasetId + ")");
        ArrayList<TDatafile> datafileList = new ArrayList<TDatafile>();
        try {
            Dataset resultInv = (Dataset) service.get(sessionId, "Dataset INCLUDE Datafile, DatafileFormat", datasetId);
            List<Datafile> dList = resultInv.getDatafiles();
            for (Datafile datafile : dList) {
                datafileList.add(copyDatafileToTDatafile(serverName, datafile));
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getDatafilesInDataset");
        } catch (Throwable e) {
            logger.error("getDatafilesInDataset caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getDatafilesInDataset threw an unexpected exception, see server logs for details");
        }
        return datafileList;
    }

    @Override
    public ArrayList<TDatafileParameter> getParametersInDatafile(String sessionId, Long datafileId)
            throws TopcatException {
        logger.info("getParametersInDatafile: sessionId (" + sessionId + "), datafileId (" + datafileId + ")");
        ArrayList<TDatafileParameter> result = new ArrayList<TDatafileParameter>();
        try {
            Datafile df = (Datafile) service.get(sessionId, "Datafile INCLUDE DatafileParameter, ParameterType",
                    datafileId);
            List<DatafileParameter> dfList = df.getParameters();
            for (DatafileParameter dfParam : dfList) {
                if (dfParam.getType().getValueType() == ParameterValueType.NUMERIC) {
                    result.add(new TDatafileParameter(dfParam.getType().getName(), dfParam.getType().getUnits(),
                            dfParam.getNumericValue().toString()));
                } else if (dfParam.getType().getValueType() == ParameterValueType.STRING) {
                    result.add(new TDatafileParameter(dfParam.getType().getName(), dfParam.getType().getUnits(),
                            dfParam.getStringValue()));
                } else if (dfParam.getType().getValueType() == ParameterValueType.DATE_AND_TIME) {
                    result.add(new TDatafileParameter(dfParam.getType().getName(), dfParam.getType().getUnits(),
                            dfParam.getDateTimeValue().toString()));
                }
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getParametersInDatafile");
        } catch (Throwable e) {
            logger.error("getParametersInDatafile caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getParametersInDatafile threw an unexpected exception, see server logs for details");
        }
        return result;
    }

    @Override
    public String downloadDatafiles(String sessionId, List<Long> datafileIds) {
        String result = "";
        return result;
    }

    @Override
    public String downloadDataset(String sessionId, Long datasetId) {
        String result = "";
        return result;
    }

    @Override
    public ArrayList<String> getKeywordsForUser(String sessionId) throws TopcatException {
        logger.info("getKeywordsForUser: sessionId (" + sessionId + ")");
        return searchList(sessionId, "DISTINCT Keyword.name", "getKeywordsForUser");
    }

    @Override
    public ArrayList<String> getKeywordsInInvestigation(String sessionId, Long investigationId) throws TopcatException {
        logger.info("getKeywordsInInvestigation: sessionId (" + sessionId + "), investigationId (" + investigationId
                + ")");
        ArrayList<String> keywords = new ArrayList<String>();
        try {
            Investigation inv = (Investigation) service
                    .get(sessionId, "Investigation INCLUDE Keyword", investigationId);
            List<Keyword> resultKeywords = inv.getKeywords();
            for (Keyword key : resultKeywords) {
                keywords.add(key.getName());
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getKeywordsInInvestigation");
        } catch (Throwable e) {
            logger.error("getKeywordsInInvestigation caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getKeywordsInInvestigation threw an unexpected exception, see server logs for details");
        }
        return keywords;
    }

    @Override
    public ArrayList<TInvestigation> searchByKeywords(String sessionId, ArrayList<String> keywords)
            throws TopcatException {
        logger.info("searchByKeywords: sessionId (" + sessionId + "), keywords)");
        // call the search using keyword method
        List<Object> resultInvestigations = null;
        ArrayList<TInvestigation> returnTInvestigations = new ArrayList<TInvestigation>();
        String query = "DISTINCT Investigation <-> Keyword[name IN " + getIN(keywords) + "]";
        try {
            resultInvestigations = service.search(sessionId, query);
            for (Object inv : resultInvestigations) {
                returnTInvestigations.add(copyInvestigationToTInvestigation(serverName, (Investigation) inv));
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "searchByKeywords");
        } catch (Throwable e) {
            logger.error("searchByKeywords caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, searchByKeywords threw an unexpected exception, see server logs for details");
        }
        Collections.sort(returnTInvestigations);
        return returnTInvestigations;
    }

    @Override
    public ArrayList<TDatafile> searchDatafilesByRunNumber(String sessionId, ArrayList<String> instruments,
            float startRunNumber, float endRunNumber) throws TopcatException {
        logger.info("searchDatafilesByRunNumber: sessionId (" + sessionId + "), instruments, startRunNumber("
                + startRunNumber + "), endRunNumber (" + endRunNumber + ")");
        List<Object> resultDatafiles = null;
        ArrayList<TDatafile> returnTDatafiles = new ArrayList<TDatafile>();
        try {
            resultDatafiles = service.search(sessionId,
                    "DISTINCT Datafile INCLUDE DatafileFormat <-> Dataset <-> Investigation <-> InvestigationInstrument <-> Instrument[fullName IN "
                            + getIN(instruments)
                            + "] <-> DatafileParameter[type.name='run_number' AND numericValue BETWEEN "
                            + startRunNumber + " AND " + endRunNumber + "]");
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "searchDatafilesByRunNumber");
        } catch (Throwable e) {
            logger.error("searchDatafilesByRunNumber caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, searchDatafilesByRunNumber threw an unexpected exception, see server logs for details");
        }
        for (Object datafile : resultDatafiles) {
            returnTDatafiles.add(copyDatafileToTDatafile(serverName, (Datafile) datafile));
        }
        return returnTDatafiles;
    }

    @Override
    public ArrayList<String> getKeywordsForUserWithStartMax(String sessionId, String partialKey, int numberOfKeywords)
            throws TopcatException {
        logger.info("getKeywordsForUserWithStartMax: sessionId (" + sessionId + "), partialKey (" + partialKey
                + "), numberOfKeywords (" + numberOfKeywords + ")");
        ArrayList<String> resultKeywords = new ArrayList<String>();
        try {
            List<Object> results = service.search(sessionId, "0," + numberOfKeywords + "DISTINCT Keyword.name LIKE "
                    + partialKey + "%");
            for (Object keyword : results) {
                resultKeywords.add((String) keyword);
            }
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "getKeywordsForUserWithStartMax");
        } catch (Throwable e) {
            logger.error("getKeywordsForUserWithStartMax caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getKeywordsForUserWithStartMax threw an unexpected exception, see server logs for details");
        }
        return resultKeywords;
    }

    @Override
    public ArrayList<TDatafile> searchDatafilesByParameter(String sessionId, TAdvancedSearchDetails details)
            throws TopcatException {
        logger.info("searchDatafilesByParameter: sessionId (" + sessionId + "), TAdvancedSearchDetails)");
        ArrayList<TDatafile> datafileList = new ArrayList<TDatafile>();
        String query = getParameterQuery(sessionId, details, "Datafile");
        List<Object> resultDf = null;
        try {
            resultDf = service.search(sessionId, query);
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "searchDatafilesByParameter");
        } catch (Throwable e) {
            logger.error("searchDatafilesByParameter caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, searchDatafilesByParameter threw an unexpected exception, see server logs for details");
        }
        for (Object df : resultDf) {
            datafileList.add(copyDatafileToTDatafile(serverName, (Datafile) df));
        }
        return datafileList;
    }

    @Override
    public ArrayList<String> getParameterNames(String sessionId) throws TopcatException {
        logger.info("getParameterNames: sessionId (" + sessionId + ")");
        ArrayList<String> names = searchList(sessionId, "DISTINCT ParameterType.name", "getParameterNames");
        Collections.sort(names);
        return names;
    }

    @Override
    public ArrayList<String> getParameterUnits(String sessionId, String name) throws TopcatException {
        logger.info("getParameterUnits: sessionId (" + sessionId + "), name (" + name + ")");
        ArrayList<String> units = searchList(sessionId, "DISTINCT ParameterType.units [name = '" + name + "']",
                "getParameterUnits");
        Collections.sort(units);
        return units;
    }

    @Override
    public ArrayList<String> getParameterTypes(String sessionId, String name, String units) throws TopcatException {
        logger.info("getParameterTypes: sessionId (" + sessionId + "), name (" + name + ", units (" + units + ")");
        ArrayList<String> unitsList = new ArrayList<String>();
        List<ParameterValueType> results = getParameterTypesFormService(sessionId, name, units);
        for (ParameterValueType result : results) {
            unitsList.add(result.toString());
        }
        return unitsList;
    }

    @Override
    public Long createDataSet(String sessionId, TDataset dataset) throws TopcatException {
        logger.info("createDataSet: sessionId (" + sessionId + "), dataset");
        Dataset ds = new Dataset();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        try {
            ds.setStartDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
        } catch (DatatypeConfigurationException e) {
            logger.warn("createDataSet, cannot get date: " + e.getMessage());
        }
        try {
            List<Object> datasetType = service.search(sessionId, "DatasetType [name='" + dataset.getType() + "']");
            ds.setType((DatasetType) datasetType.get(0));
        } catch (IcatException_Exception e) {
            logger.warn("createDataSet, cannot get DatasetType: " + e.getMessage());
            convertToTopcatException(e, "createDataSet");
        } catch (Throwable e) {
            logger.error("createDataSet caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, createDataSet threw an unexpected exception, see server logs for details");
        }
        try {
            ds.setInvestigation((Investigation) service.get(sessionId, "Investigation",
                    Long.valueOf(dataset.getInvestigationId())));
        } catch (IcatException_Exception e) {
            logger.warn("createDataSet, cannot get Investigation: " + e.getMessage());
            convertToTopcatException(e, "createDataSet");
        } catch (Throwable e) {
            logger.error("createDataSet caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, createDataSet threw an unexpected exception, see server logs for details");
        }
        ds.setDescription(dataset.getDescription());
        ds.setName(dataset.getName());
        ds.setComplete(false);
        try {
            return service.create(sessionId, ds);
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, "createDataSet");
        } catch (Throwable e) {
            logger.error("createDataSet caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, createDataSet threw an unexpected exception, see server logs for details");
        }
        return null;
    }

    private void convertToTopcatException(IcatException_Exception e, String callingMethod) throws TopcatException {
        IcatException ue = e.getFaultInfo();
        logger.warn(callingMethod + ": IcatException: " + ue.getType() + " ~ " + e.getMessage());
        if (ue.getType().equals(IcatExceptionType.BAD_PARAMETER)) {
            throw new BadParameterException(e.getMessage());
        } else if (ue.getType().equals(IcatExceptionType.INSUFFICIENT_PRIVILEGES)) {
            throw new InsufficientPrivilegesException(e.getMessage());
        } else if (ue.getType().equals(IcatExceptionType.INTERNAL)) {
            throw new InternalException(e.getMessage());
        } else if (ue.getType().equals(IcatExceptionType.NO_SUCH_OBJECT_FOUND)) {
            throw new NoSuchObjectException(e.getMessage());
        } else if (ue.getType().equals(IcatExceptionType.OBJECT_ALREADY_EXISTS)) {
            throw new ObjectAlreadyExistsException(e.getMessage());
        } else if (ue.getType().equals(IcatExceptionType.SESSION)) {
            throw new SessionException(e.getMessage());
        } else if (ue.getType().equals(IcatExceptionType.VALIDATION)) {
            throw new ValidationException(e.getMessage());
        }
    }

    private String getAdvancedQuery(String sessionId, TAdvancedSearchDetails details) throws TopcatException {
        // Parameter - if it is a parameter search then we do not use the other
        // search details
        if (details.getParameterName() != null) {
            return getParameterQuery(sessionId, details, "Investigation");
        }

        StringBuilder query = new StringBuilder(" DISTINCT Investigation INCLUDE Facility");
        boolean addAnd = false;
        boolean queryDataset = false;

        // Dates
        if (details.getStartDate() != null && details.getEndDate() != null) {
            addAnd = true;
            String startDate = getDate(details.getStartDate());
            String endDate = getDate(details.getEndDate());

            query.append(" [((startDate>=" + startDate + " AND startDate<=" + endDate + ") OR (endDate>=" + startDate
                    + " AND endDate<=" + endDate + "))");
        }

        // Proposal Abstract
        if (details.getProposalAbstract() != null) {
            if (addAnd) {
                query.append(" AND");
            } else {
                query.append(" [");
                addAnd = true;
            }
            query.append(" summary='" + details.getProposalAbstract() + "'");
        }

        // Proposal Title
        if (details.getPropostaltitle() != null) {
            if (addAnd) {
                query.append(" AND");
            } else {
                query.append(" [");
                addAnd = true;
            }
            query.append(" title LIKE '%" + details.getPropostaltitle() + "%'");
        }

        // Visit Id
        if (details.getVisitId() != null) {
            if (addAnd) {
                query.append(" AND");
            } else {
                query.append(" [");
                addAnd = true;
            }
            query.append(" visitId='" + details.getVisitId() + "'");
        }

        if (addAnd) {
            query.append("]");
        }

        // Data File Name
        if (details.getDatafileName() != null) {
            query.append(" <-> Dataset <-> Datafile[name='" + details.getDatafileName() + "']");
            queryDataset = true;
        }

        // Grant Id
        if (details.getGrantId() != null) {
            query.append(" <-> InvestigationParameter [type.name='grant_id' AND stringValue='" + details.getGrantId()
                    + "']");
        }

        // Instrument
        if (details.getInstrumentList().size() > 0) {
            query.append(" <-> InvestigationInstrument <-> Instrument[(name IN " + getIN(details.getInstrumentList()) + ") OR (fullName IN "
                    + getIN(details.getInstrumentList()) + ")]");
        }

        // Investigation Type
        if (details.getInvestigationTypeList().size() > 0) {
            query.append(" <-> InvestigationType[name IN " + getIN(details.getInvestigationTypeList()) + "]");
        }

        // Investigator Name
        if (details.getInvestigatorNameList().size() > 0) {
            query.append(" <-> InvestigationUser <-> User[");
            boolean firstLoop = true;
            for (String name : details.getInvestigatorNameList()) {
                if (firstLoop) {
                    firstLoop = false;
                } else {
                    query.append(" OR ");
                }
                query.append("name LIKE '%" + name + "%'");
            }
            query.append("]");
        }

        // Keywords
        if (details.getKeywords().size() > 0
                && !(details.getKeywords().size() == 1 && details.getKeywords().get(0).length() == 0)) {
            query.append(" <-> Keyword[name IN " + getIN(details.getKeywords()) + "]");
        }

        // Run Numbers
        if (details.getRbNumberStart() != null && details.getRbNumberEnd() != null) {
            query.append(" <-> Dataset <-> Datafile <-> DatafileParameter[type.name='run_number' AND numericValue BETWEEN "
                    + details.getRbNumberStart() + " AND " + details.getRbNumberEnd() + "]");
            queryDataset = true;
        }

        // Sample
        // TODO We cannot use dataset and sample in the same query
        if (details.getSample() != null && !queryDataset) {
            query.append(" <-> Sample[name='" + details.getSample() + "']");
        }
        return query.toString();
    }

    /**
     * Construct the query string to search for parameter(s).
     *
     * @param sessionId
     * @param details
     * @param entityName
     *            'Investigation', 'Dataset' or 'Datafile'
     * @return a query string
     * @throws TopcatException
     */
    private String getParameterQuery(String sessionId, TAdvancedSearchDetails details, String entityName)
            throws TopcatException {
        List<ParameterValueType> types = getParameterTypesFormService(sessionId, details.getParameterName(),
                details.getParameterUnits());
        StringBuilder query = new StringBuilder(" DISTINCT " + entityName);
        if (!details.getSearchAllData()) {
            String name = null;
            try {
                name = service.getUserName(sessionId);
            } catch (IcatException_Exception e) {
                convertToTopcatException(e, "getParameterQuery");
            } catch (Throwable e) {
                logger.error("getParameterQuery caught an unexpected exception: " + e.toString());
                throw new InternalException(
                        "Internal error, getParameterQuery threw an unexpected exception, see server logs for details");
            }
            if (entityName.equalsIgnoreCase("Investigation")) {
                query.append("<-> InvestigationUser <-> User[name='" + name + "']");
            } else if (entityName.equalsIgnoreCase("Dataset")) {
                query.append("<-> Investigation <-> InvestigationUser <-> User[name='" + name + "']");

            } else if (entityName.equalsIgnoreCase("Datafile")) {
                query.append("<-> Dataset <-> Investigation <-> InvestigationUser <-> User[name='" + name + "']");
            }
        }
        if (details.getParameterUnits().equals(TConstants.ALL_UNITS)) {
            // Search for all possible units for the parameter type
            query.append(" <-> " + entityName + "Parameter [");
            boolean first = true;
            for (ParameterValueType type : types) {
                if (!first) {
                    query.append(" OR ");
                }
                if (details.getParameterValueMax() == null || details.getParameterValueMax().isEmpty()) {
                    // Search for a single value
                    if (type == ParameterValueType.DATE_AND_TIME) {
                        query.append("(type.name='" + details.getParameterName() + "' AND dateTimeValue="
                                + details.getParameterValue() + ")");
                        first = false;
                    } else if (type == ParameterValueType.NUMERIC && isNumeric(details.getParameterValue())) {
                        // As this is checking all parameters only check numeric
                        // types if values are numeric
                        query.append("(type.name='" + details.getParameterName() + "' AND numericValue="
                                + details.getParameterValue() + ")");
                        first = false;
                    } else if (type == ParameterValueType.STRING) {
                        query.append("(type.name='" + details.getParameterName() + "' AND stringValue='"
                                + details.getParameterValue() + "')");
                        first = false;
                    }
                } else {
                    // Search for a range of single values
                    if (type == ParameterValueType.DATE_AND_TIME) {
                        query.append("(type.name='" + details.getParameterName() + "' AND dateTimeValue BETWEEN "
                                + details.getParameterValue() + " AND " + details.getParameterValueMax() + ")");
                        first = false;
                    } else if (type == ParameterValueType.NUMERIC && isNumeric(details.getParameterValue())
                            & isNumeric(details.getParameterValueMax())) {
                        // As this is checking all parameters only check numeric
                        // types if values are numeric
                        query.append("(type.name='" + details.getParameterName() + "' AND numericValue BETWEEN "
                                + details.getParameterValue() + " AND " + details.getParameterValueMax() + ")");
                        first = false;
                    } else if (type == ParameterValueType.STRING) {
                        query.append("(type.name='" + details.getParameterName() + "' AND stringValue BETWEEN '"
                                + details.getParameterValue() + "' AND '" + details.getParameterValueMax() + "')");
                        first = false;
                    }
                }
            }
            query.append("]");
        } else {
            // Search for specific parameter type and units
            query.append(" <-> " + entityName + "Parameter [type.name='" + details.getParameterName()
                    + "' AND type.units='" + details.getParameterUnits() + "' AND ");
            if (details.getParameterValueMax() == null || details.getParameterValueMax().isEmpty()) {
                // Search for a single value
                if (types.get(0) == ParameterValueType.DATE_AND_TIME) {
                    query.append("dateTimeValue=" + details.getParameterValue() + "]");
                } else if (types.get(0) == ParameterValueType.NUMERIC) {
                    query.append("numericValue=" + details.getParameterValue() + "]");
                } else if (types.get(0) == ParameterValueType.STRING) {
                    query.append("stringValue='" + details.getParameterValue() + "']");
                }
            } else {
                // Search for a range of values
                if (types.get(0) == ParameterValueType.DATE_AND_TIME) {
                    query.append("dateTimeValue BETWEEN " + details.getParameterValue() + " AND "
                            + details.getParameterValueMax() + "]");
                } else if (types.get(0) == ParameterValueType.NUMERIC) {
                    query.append("numericValue BETWEEN " + details.getParameterValue() + " AND "
                            + details.getParameterValueMax() + "]");
                } else if (types.get(0) == ParameterValueType.STRING) {
                    query.append("stringValue BETWEEN '" + details.getParameterValue() + "' AND '"
                            + details.getParameterValueMax() + "']");
                }
            }
        }
        return query.toString();
    }

    private boolean isNumeric(String value) {
        try {
            new Double(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private List<ParameterValueType> getParameterTypesFormService(String sessionId, String name, String units)
            throws TopcatException {
        String query;
        List<ParameterValueType> types = new ArrayList<ParameterValueType>();
        if (units.equals(TConstants.ALL_UNITS)) {
            query = "DISTINCT ParameterType.valueType [name='" + name + "']";
        } else {
            query = "DISTINCT ParameterType.valueType [name='" + name + "' and units='" + units + "']";
        }
        try {
            List<Object> results = service.search(sessionId, query);
            for (Object result : results) {
                types.add((ParameterValueType) result);
            }
        } catch (IcatException_Exception e) {
            IcatException ue = e.getFaultInfo();
            if (!ue.getType().equals(IcatExceptionType.BAD_PARAMETER)) {
                convertToTopcatException(e, "getParameterTypesFormService");
            } else {
                throw new BadParameterException("Parameter name/units not found");
            }
        } catch (Throwable e) {
            logger.error("getParameterTypesFormService caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, getParameterTypesFormService threw an unexpected exception, see server logs for details");
        }
        if (types.size() == 0) {
            // Parameter not found
            throw new BadParameterException("Parameter name/units not found");
        }
        return types;
    }

    /**
     * Get the date as a string from a <code>XMLGregorianCalendar</code> in a
     * format suitable for a icat query.
     *
     * @param date
     *            a <code>XMLGregorianCalendar</code> date
     * @return a string containing a date in a format suitable for a icat query
     */
    private String getDate(XMLGregorianCalendar date) {
        return getDate(date.toGregorianCalendar().getTime());
    }

    /**
     * Get the date as a string from a <code>Date</code> in a format suitable
     * for a icat query.
     *
     * @param date
     *            a <code>Date</code> date
     * @return a string containing a date in a format suitable for a icat query
     */
    private String getDate(Date date) {
        StringBuilder retDate = new StringBuilder();
        retDate.append("{ts ");
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        retDate.append(formater.format(date));
        retDate.append("}");
        return retDate.toString();
    }

    private TInvestigation copyInvestigationToTInvestigation(String serverName, Investigation inv) {
        String id = inv.getId().toString();
        Date invStartDate = null;
        Date invEndDate = null;
        String facilityName = "";

        if (inv.getStartDate() != null) {
            invStartDate = inv.getStartDate().toGregorianCalendar().getTime();
        }
        if (inv.getEndDate() != null) {
            invEndDate = inv.getEndDate().toGregorianCalendar().getTime();
        }

        Facility invFacility = inv.getFacility();
        if (invFacility != null) {
            facilityName = invFacility.getFullName();            
        } else {
            facilityName = serverName;            
        }

        return new TInvestigation(id, inv.getName(), serverName, facilityName, inv.getTitle(), invStartDate, invEndDate,
                inv.getVisitId());
    }

    private TDatafile copyDatafileToTDatafile(String serverName, Datafile datafile) {
        String formatId = "", formatName = "", formatDescription = "", formatVersion = "", formatType = "";
        Date createDate = null, modDate = null;
        if (datafile.getDatafileFormat() != null) {
            formatId = datafile.getDatafileFormat().getId().toString();
            formatName = datafile.getDatafileFormat().getName();
            formatDescription = datafile.getDatafileFormat().getDescription();
            formatVersion = datafile.getDatafileFormat().getVersion();
            formatType = datafile.getDatafileFormat().getType();
        }
        if (datafile.getDatafileCreateTime() != null) {
            createDate = datafile.getDatafileCreateTime().toGregorianCalendar().getTime();
        }
        if (datafile.getDatafileModTime() != null) {
            modDate = datafile.getDatafileModTime().toGregorianCalendar().getTime();
        }
        return new TDatafile(serverName, datafile.getId().toString(), datafile.getName(), datafile.getDescription(),
                datafile.getFileSize(), formatId, formatName, formatDescription, formatVersion, formatType, createDate,
                modDate, datafile.getLocation(), datafile.getDoi());
    }

    private TPublication copyPublicationToTPublication(Publication pub) {
        return new TPublication(pub.getFullReference(), pub.getId(), pub.getRepository(), pub.getRepositoryId(),
                pub.getUrl());
    }

    private TInvestigator copyInvestigatorToTInvestigator(InvestigationUser investigator) {        
        return new TInvestigator("", "", investigator.getUser().getFullName(), investigator.getRole());
    }

    private TShift copyShiftToTShift(Shift shift) {
        return new TShift(shift.getComment(), shift.getStartDate().toGregorianCalendar().getTime(), shift.getEndDate()
                .toGregorianCalendar().getTime());
    }

    /**
     * Call icat with the given query and then format the results as a list of
     * strings.
     *
     * @param sessionId
     * @param query
     *            a string containing the query to pass to icat
     * @param a
     *            string containing a message to be prefixed to TopcatException
     *            if it is thrown
     * @return a list of strings
     * @throws TopcatException
     */
    private ArrayList<String> searchList(String sessionId, String query, String message) throws TopcatException {
        ArrayList<String> returnList = new ArrayList<String>();
        try {
            List<Object> results = service.search(sessionId, query);
            for (Object item : results) {
                returnList.add((String) item);
            }
        } catch (java.lang.NullPointerException ex) {
        } catch (IcatException_Exception e) {
            convertToTopcatException(e, message);
        } catch (Throwable e) {
            logger.error("searchList caught an unexpected exception: " + e.toString());
            throw new InternalException(
                    "Internal error, searchList threw an unexpected exception, see server logs for details");
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
    
    private String getINLong(List<Long> ele) {
        final StringBuilder infield = new StringBuilder("(");
        for (final long t : ele) {
            if (infield.length() != 1) {
                infield.append(',');
            }
            infield.append(t);
        }
        infield.append(')');
        return infield.toString();
    }
    
}
