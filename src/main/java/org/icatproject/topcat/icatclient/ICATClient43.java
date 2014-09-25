package org.icatproject.topcat.icatclient;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.icatproject.topcat.Constants;
import org.icatproject.topcat.domain.SortOrder;
import org.icatproject.topcat.domain.TDatafileFormat;
import org.icatproject.topcat.domain.TDataset;
import org.icatproject.topcat.domain.TDatasetType;
import org.icatproject.topcat.domain.TFacility;
import org.icatproject.topcat.domain.TFacilityCycle;
import org.icatproject.topcat.domain.TInstrument;
import org.icatproject.topcat.domain.TInvestigation;
import org.icatproject.topcat.domain.TInvestigationType;
import org.icatproject.topcat.domain.TParameterType;
import org.icatproject.topcat.domain.TParameterValueType;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.ForbiddenException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientInterface;
import org.icatproject.topcat.utils.XMLGregorianCalendarConversionUtil;
import org.icatproject_4_3_0.DatafileFormat;
import org.icatproject_4_3_0.Dataset;
import org.icatproject_4_3_0.DatasetType;
import org.icatproject_4_3_0.Facility;
import org.icatproject_4_3_0.FacilityCycle;
import org.icatproject_4_3_0.ICAT;
import org.icatproject_4_3_0.ICATService;
import org.icatproject_4_3_0.IcatExceptionType;
import org.icatproject_4_3_0.IcatException_Exception;
import org.icatproject_4_3_0.Instrument;
import org.icatproject_4_3_0.Investigation;
import org.icatproject_4_3_0.InvestigationType;
import org.icatproject_4_3_0.Login.Credentials;
import org.icatproject_4_3_0.Login.Credentials.Entry;
import org.icatproject_4_3_0.ParameterType;
import org.icatproject_4_3_0.ParameterValueType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * ICAT client for ICAT 4.3
 */
public class ICATClient43 implements ICATClientInterface {
    private ICAT service;

    private final static Logger logger = Logger.getLogger(ICATClient43.class);

    public ICATClient43(String serverURL, String serverName) throws MalformedURLException {
        logger.info("ICATInterfacev43: serverURL (" + serverURL + "), serverName (" + serverName + ")");

        if (!serverURL.matches(".*/ICATService/ICAT\\?wsdl$")) {
            if (serverURL.matches(".*/$")) {
                serverURL = serverURL + "ICATService/ICAT?wsdl";
            } else {
                serverURL = serverURL + "/ICATService/ICAT?wsdl";
            }
        }
        URL url = new URL(serverURL);

        service = new ICATService(url, new QName("http://icatproject.org", "ICATService")).getICATPort();
    }

    /**
     * Authenticate against an ICAT service and return an icat session id
     *
     * @param authenticationType the authentication type name
     * @param parameters the map of parameters (username and password)
     * @return String the icat session id
     *
     * @throws AuthenticationException  the authentication exception
     * @throws InternalException the internal exception
     *
     */
    @Override
    public String login(String authenticationType, Map<String, String> parameters) throws AuthenticationException, InternalException {
        logger.info("login: authenticationType (" + authenticationType + "), number of parameters " + parameters.size());

        String result = new String();

        try {
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
            logger.debug("IcatException_Exception:" + ex.getMessage());
            throw new AuthenticationException(ex.getMessage());
        } catch (javax.xml.ws.WebServiceException ex) {
            logger.debug("login: WebServiceException:" + ex.getMessage());
            throw new InternalException("ICAT Server not available");
        }

        logger.info("login: result(" + result + ")");

        return result;
    }

    /**
     * Returns the user name as used by icat
     *
     * @param icatSessionId the icat session id
     * @return the icat user name
     * @throws TopcatException
     */
    @Override
    public String getUserName(String icatSessionId) throws TopcatException {
        String result = null;

        try {
            result = service.getUserName(icatSessionId);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        return result;

    }


    /**
     * Returns whether a session is valid or not
     *
     * @param icatSessionId the icat session id
     * @return whether the session is valid or not
     */
    @Override
    public Boolean isSessionValid(String icatSessionId) throws IcatException {
        Double result = null;

        try {
            result = service.getRemainingMinutes(icatSessionId);
        } catch (IcatException_Exception e) {
            logger.info("isSessionValid: " + e.getMessage());

            return false;
            //throw new IcatException(e.getMessage());
        }

        if (result > 0) {
            return true;
        }

        return false;
    }


    /**
     * Returns the number of minutes remaining for a session
     *
     * @param icatSessionId the icat session id
     * @return the remaining minutes of the session. -1 if session is not valid
     *          or if other icat server error is thrown
     */
    @Override
    public Long getRemainingMinutes(String icatSessionId) throws IcatException {
        Double result = null;

        try {
            result = service.getRemainingMinutes(icatSessionId);
        } catch (IcatException_Exception e) {
            return -1L;
        }

        return result.longValue();
    }

    /**
     * Resets the time-to-live of the icat session
     *
     * @param icatSessionId the icat session id
     * @throws TopcatException
     *
     */
    @Override
    public void refresh(String icatSessionId) throws TopcatException {
        try {
            service.refresh(icatSessionId);
        } catch (IcatException_Exception e) {
            logger.info("logout: " + e.getMessage());
            throwNewICATException(e);
        }

    }

    /**
     * This invalidates the sessionId.
     *
     * @param icatSessionId the icat session id
     * @throws TopcatException
     *
     */
    @Override
    public void logout(String icatSessionId) throws TopcatException {
        try {
            service.logout(icatSessionId);
        } catch (IcatException_Exception e) {
            logger.info("logout: " + e.getMessage());
            throwNewICATException(e);
        }

    }


    /**
     * Get list of facilities for a server
     *
     * @param icatSessionId the icat session id
     * @return list of facilities
     * @throws TopcatException
     *
     */
    @Override
    public List<TFacility> getFacilities(String icatSessionId) throws TopcatException {
        List<TFacility> facilities = new ArrayList<TFacility>();
        List<Object> results = new ArrayList<Object>();

        String query = "SELECT f FROM Facility f ORDER BY f.name";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            facilities.add(facilityToTFacility((Facility) result));
        }

        return facilities;
    }


    /**
     * Get list of facilities for a server
     *
     * @param icatSessionId the icat session id
     * @return list of facilities
     * @throws TopcatException
     *
     */
    @Override
    public TFacility getFacilityById(String icatSessionId, Long id) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        TFacility facility = null;

        String query = "SELECT f FROM Facility f WHERE f.id = '" + id + "'";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        if (results.size() > 0) {
            Object result = results.get(0);
            facility = facilityToTFacility((Facility) result);
        }

        return facility;
    }

    /**
     * Get list of facility cycles for a facility
     *
     * @param icatSessionId the icat session id
     * @param facilitId the id of the facility
     * @return list of facility cycles
     * @throws TopcatException
     *
     */
    @Override
    public List<TFacilityCycle> getFacilityCyclesByFacilityId(String icatSessionId, Long facilityId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TFacilityCycle> facilityCycles = new ArrayList<TFacilityCycle>();

        String query = "SELECT f FROM FacilityCycle f WHERE f.facility.id = '" + facilityId + "' ORDER BY f.name";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            facilityCycles.add(facilityCycleToTFacilityCycle((FacilityCycle) result));
        }

        return facilityCycles;
    }

    /**
     * Get list of data types for a facility
     *
     * @param icatSessionId the icat session id
     * @param facilitId the id of the facility
     * @return list of data types
     * @throws TopcatException
     *
     */
    @Override
    public List<TDatasetType> getDatasetTypesByFacilityId(String icatSessionId,
            Long facilityId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TDatasetType> tDatasetTypes = new ArrayList<TDatasetType>();

        String query = "SELECT d FROM DatasetType d WHERE d.facility.id = '" + facilityId + "' ORDER BY d.name";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tDatasetTypes.add(datasetTypeToTDatasetType((DatasetType) result));
        }

        return tDatasetTypes;
    }

    /**
     * Get list of datafile formats for a facility
     *
     * @param icatSessionId the icat session id
     * @param facilitId the id of the facility
     * @return list of datafile formats
     * @throws TopcatException
     *
     */
    @Override
    public List<TDatafileFormat> getDatafileFormatsByFacilityId(
            String icatSessionId, Long facilityId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TDatafileFormat> tDatafileFormats = new ArrayList<TDatafileFormat>();

        String query = "SELECT d FROM DatafileFormat d WHERE d.facility.id = '" + facilityId + "' ORDER BY d.name";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tDatafileFormats.add(datafileFormatToTDatafileFormat((DatafileFormat) result));
        }

        return tDatafileFormats;
    }

    /**
     * Get list of parameter types for a facility
     *
     * @param icatSessionId the icat session id
     * @param facilitId the id of the facility
     * @return list of parameter types
     * @throws TopcatException
     *
     */
    @Override
    public List<TParameterType> getParameterTypesByFacilityId(
            String icatSessionId, Long facilityId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TParameterType> tParameterTypes = new ArrayList<TParameterType>();

        String query = "SELECT p FROM ParameterType p WHERE p.facility.id = '" + facilityId + "' ORDER BY p.name";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tParameterTypes.add(parameterTypeToTParameterType((ParameterType) result));
        }

        return tParameterTypes;
    }


    /**
     * Get list of investigation types for a facility
     *
     * @param icatSessionId the icat session id
     * @param facilitId the id of the facility
     * @return list of investigation types
     * @throws TopcatException
     *
     */
    @Override
    public List<TInvestigationType> getInvestigationTypesByFacilityId(
            String icatSessionId, Long facilityId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TInvestigationType> tInvestigationTypes = new ArrayList<TInvestigationType>();

        String query = "SELECT i FROM InvestigationType i WHERE i.facility.id = '" + facilityId + "' ORDER BY i.name";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tInvestigationTypes.add(investigationTypeToTInvestigationType((InvestigationType) result));
        }

        return tInvestigationTypes;
    }

    /**
     * Get a list of instruments for a facility
     *
     * @param icatSessionId the icat session id
     * @param facilitId the id of the facility
     * @return list of instruments
     * @throws TopcatException
     *
     */
    @Override
    public List<TInstrument> getInstrumentsByfacilityId(String icatSessionId,
            Long facilityId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TInstrument> tInstruments = new ArrayList<TInstrument>();

        String query = "SELECT i FROM Instrument i WHERE i.facility.id = '" + facilityId + "' ORDER BY i.name";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tInstruments.add(instrumentToTInstrument((Instrument) result));
        }

        return tInstruments;
    }

    /**
     * Get a list of investigations for an instrument
     *
     * @param icatSessionId the icat session id
     * @param instrumentId the id of the instrument
     * @return list of investigations
     * @throws TopcatException
     *
     */
    @Override
    public List<TInvestigation> getInvestigationsByInstrumentId(
            String icatSessionId, Long instrumentId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TInvestigation> tInvestigations = new ArrayList<TInvestigation>();

        String query = "SELECT DISTINCT inv FROM Investigation inv, inv.investigationInstruments ins WHERE ins.instrument.id = '" + instrumentId + "' ORDER BY inv.id";

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        logger.debug("getInvestigationsByInstrumentId size: " + results.size());

        for(Object result : results) {
            tInvestigations.add(investigationToTInvestigation((Investigation) result));
        }

        return tInvestigations;
    }


    /**
     * Get the number of investigations for an instrument by the instrument id
     *
     * @param icatSessionId the icat session id
     * @param instrumentId the id of the instrument
     * @return an investigations
     * @throws TopcatException
     */
    @Override
    public Long getInvestigationsByInstrumentIdCount(String icatSessionId,
            Long instrumentId) throws TopcatException {
        Long count = 0L;
        List<Object> results = new ArrayList<Object>();

        String query = "SELECT COUNT(inv) FROM Investigation inv, inv.investigationInstruments ins "
                + "WHERE ins.instrument.id = '" + instrumentId + "'";

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        if (results.size() != 0) {
            count = (Long) results.get(0);
        }

        return count;
    }


    /**
     * Get a paginated list of investigations for an instrument
     *
     * @param icatSessionId the icat session id
     * @param instrumentId the id of the instrument
     * @param offset the starting row of result
     * @param numberOfRows the number of results to return
     * @param sortBy the field to sort by. see icat schema
     * @param order the order to sort by SortOrder.ASC or SortOrder.DESC
     * @return list of investigations
     * @throws TopcatException
     *
     */
    @Override
    public List<TInvestigation> getInvestigationsByInstrumentIdPaginated(
            String icatSessionId, Long instrumentId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TInvestigation> tInvestigations = new ArrayList<TInvestigation>();

        String query = "SELECT DISTINCT inv FROM Investigation inv, inv.investigationInstruments ins "
                + "WHERE ins.instrument.id = '" + instrumentId + "' ORDER BY inv." + sortBy + " " + order.getValue() + " LIMIT " + offset.toString() + ", " + numberOfRows.toString();

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tInvestigations.add(investigationToTInvestigation((Investigation) result));
        }

        return tInvestigations;
    }

    /**
     * Get an investigation by its id
     *
     * @param icatSessionId the icat session id
     * @param id the id of the investigation
     * @throws TopcatException
     */
    @Override
    public TInvestigation getInvestigationById(String icatSessionId, Long investigationId)
            throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        TInvestigation investigation = null;

        String query = "SELECT i FROM Investigation i WHERE i.id = '" + investigationId + "'";
        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        if (results.size() > 0) {
            Object result = results.get(0);
            investigation = investigationToTInvestigation((Investigation) result);
        }

        return investigation;
    }

    /**
     * Get list of datasets by investigation id
     *
     * @param icatSessionId the icat session id
     * @param id the id of the investigation
     * @throws TopcatException
     */
    @Override
    public List<TDataset> getDatasetsByInvestigationId(String icatSessionId,
            Long investigationId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TDataset> tDataset = new ArrayList<TDataset>();

        String query = "SELECT d FROM Dataset d WHERE d.investigation.id = '" + investigationId + "' ORDER BY d.id INCLUDE d.type";

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tDataset.add(datasetToTdataset((Dataset) result));
        }

        return tDataset;
    }


    /**
     * Get the number of dataset of an investigation by investigation id
     *
     * @param icatSessionId the icat session id
     * @param instrumentId the id of the instrument
     * @return an investigations
     * @throws TopcatException
     */
    @Override
    public Long getDatasetsByInvestigationIdCount(String icatSessionId,
            Long investigationId) throws TopcatException {
        Long count = 0L;
        List<Object> results = new ArrayList<Object>();

        String query = "SELECT COUNT(d) FROM Dataset d WHERE d.investigation.id = '" + investigationId + "'";

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        if (results.size() != 0) {
            count = (Long) results.get(0);
        }

        return count;
    }


    /**
     * Get a paginated list of investigations for an instrument
     *
     * @param icatSessionId the icat session id
     * @param instrumentId the id of the instrument
     * @param offset the starting row of result
     * @param numberOfRows the number of results to return
     * @param sortBy the field to sort by. see icat schema
     * @param order the order to sort by SortOrder.ASC or SortOrder.DESC
     * @return list of investigations
     * @throws TopcatException
     *
     */
    @Override
    public List<TDataset> getDatasetsByInvestigationIdPaginated(
            String icatSessionId, Long investigationId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TDataset> tDataset = new ArrayList<TDataset>();

        String query = "SELECT d FROM Dataset d WHERE d.investigation.id = '" + investigationId + "' ORDER BY d." + sortBy + " " + order.getValue() + " INCLUDE d.type LIMIT " + offset.toString() + ", " + numberOfRows.toString();

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        logger.debug("getDatasetByInvestigationIdPaginated size: " + results.size());

        for(Object result : results) {
            tDataset.add(datasetToTdataset((Dataset) result));
        }

        return tDataset;
    }




    /**
     * Converts a Facility to a TFacility object
     *
     * @param facility
     * @return
     */
    private TFacility facilityToTFacility(Facility facility) {
        return new TFacility(
                facility.getId(),
                facility.getName(),
                facility.getFullName(),
                facility.getDescription(),
                facility.getUrl());
    }


    /**
     * Converts a FacilityCycle to a TFacilityCycle object
     *
     * @param facility
     * @return
     */
    private TFacilityCycle facilityCycleToTFacilityCycle(FacilityCycle facilityCycle) {
        return new TFacilityCycle(
                facilityCycle.getId(),
                facilityCycle.getName(),
                facilityCycle.getDescription(),
                XMLGregorianCalendarConversionUtil.asDate(facilityCycle.getStartDate()),
                XMLGregorianCalendarConversionUtil.asDate(facilityCycle.getEndDate()));

    }

    /**
     * Converts a DatasetType to TDatesetType object
     *
     * @param datasetType
     * @return
     */
    private TDatasetType datasetTypeToTDatasetType(DatasetType datasetType) {
        return new TDatasetType(
                datasetType.getId(),
                datasetType.getName(),
                datasetType.getDescription());
    }

    /**
     * Converts a DatafileFormat to TDatafileFormat object
     *
     * @param datafileFormat
     * @return
     */
    private TDatafileFormat datafileFormatToTDatafileFormat(
            DatafileFormat datafileFormat) {
        return new TDatafileFormat(
                datafileFormat.getId(),
                datafileFormat.getName(),
                datafileFormat.getDescription(),
                datafileFormat.getVersion(),
                datafileFormat.getType());
    }

    /**
     * Converts a ParameterValueType to ParameterValueType enum
     *
     * @param parameterValueType
     * @return
     */
    private TParameterValueType parameterValueTypeToTParameterValueType(
            ParameterValueType parameterValueType) {
        if (parameterValueType == ParameterValueType.DATE_AND_TIME) {
            return TParameterValueType.DATE_AND_TIME;
        }

        if (parameterValueType == ParameterValueType.NUMERIC) {
            return TParameterValueType.NUMERIC;
        }

        if (parameterValueType == ParameterValueType.STRING) {
            return TParameterValueType.STRING;
        }

        return null;
    }

    /**
     * Converts a parameterType to a TparameterType object
     * @param parameterType
     * @return
     */
    private TParameterType parameterTypeToTParameterType(
            ParameterType parameterType) {
        return new TParameterType(
                parameterType.getId(),
                parameterType.getName(),
                parameterType.getDescription(),
                parameterType.getUnits(),
                parameterType.getUnitsFullName(),
                parameterValueTypeToTParameterValueType(parameterType.getValueType()));
    }


    private TInvestigationType investigationTypeToTInvestigationType(
            InvestigationType investigationType) {
        return new TInvestigationType(
                investigationType.getId(),
                investigationType.getName(),
                investigationType.getDescription());
    }


    private TInstrument instrumentToTInstrument(
            Instrument instrument) {
        return new TInstrument(
                instrument.getId(),
                instrument.getDescription(),
                instrument.getFullName(),
                instrument.getUrl(),
                instrument.getType());
    }


    private TInvestigation investigationToTInvestigation(Investigation investigation) {
        return new TInvestigation(
                investigation.getId(),
                investigation.getName(),
                investigation.getTitle(),
                investigation.getVisitId(),
                investigation.getSummary(),
                investigation.getDoi(),
                XMLGregorianCalendarConversionUtil.asDate(investigation.getStartDate()),
                XMLGregorianCalendarConversionUtil.asDate(investigation.getEndDate()),
                XMLGregorianCalendarConversionUtil.asDate(investigation.getReleaseDate()));
    }



    private TDataset datasetToTdataset(Dataset dataset) {
        return new TDataset(
                dataset.getId(),
                dataset.getName(),
                dataset.getDescription(),
                dataset.getDoi(),
                dataset.getLocation(),
                dataset.isComplete(),
                XMLGregorianCalendarConversionUtil.asDate(dataset.getStartDate()),
                XMLGregorianCalendarConversionUtil.asDate(dataset.getEndDate()),
                datasetTypeToTDatasetType(dataset.getType()));

    }

    @Override
    public List<TInvestigation> getInvestigationsByFacilityCycleId(
            String icatSessionId, Long facilityCycleId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TInvestigation> tInvestigations = new ArrayList<TInvestigation>();

        String query = "SELECT i FROM Investigation i, i.facility f, f.facilityCycles c WHERE "
                + "i.startDate >= c.startDate AND i.endDate <= c.endDate AND "
                + "c.id = '" + facilityCycleId + "' ORDER BY i.id";

        logger.debug(query);

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        logger.debug("getInvestigationByFacilityCycleId size: " + results.size());

        for(Object result : results) {
            tInvestigations.add(investigationToTInvestigation((Investigation) result));
        }

        return tInvestigations;
    }

    @Override
    public Long getInvestigationsByFacilityCycleIdCount(String icatSessionId,
            Long facilityCycleId) throws TopcatException {
        Long count = 0L;
        List<Object> results = new ArrayList<Object>();

        String query = "SELECT count(i) FROM Investigation i, i.facility f, f.facilityCycles c WHERE "
                + "i.startDate >= c.startDate AND i.endDate <= c.endDate AND "
                + "c.id = '" + facilityCycleId + "'";

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        if (results.size() != 0) {
            count = (Long) results.get(0);
        }

        return count;
    }

    @Override
    public List<TInvestigation> getInvestigationsByFacilityCycleIdPaginated(
            String icatSessionId, Long facilityCycleId, Integer offset,
            Integer numberOfRows, String sortBy, SortOrder order)
            throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TInvestigation> tInvestigations = new ArrayList<TInvestigation>();

        String query = "SELECT i FROM Investigation i, i.facility f, f.facilityCycles c WHERE "
                + "i.startDate >= c.startDate AND i.endDate <= c.endDate AND "
                + "c.id = '" + facilityCycleId + "' ORDER BY i." + sortBy
                + " " + order.getValue() + " LIMIT " + offset.toString() + ", " + numberOfRows.toString();

        try {
            results  = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        for(Object result : results) {
            tInvestigations.add(investigationToTInvestigation((Investigation) result));
        }

        return tInvestigations;
    }

    @Override
    public List<TFacilityCycle> getFacilityCycleByInstrumentId(
            String icatSessionId, Long instrumentId) throws TopcatException {
        List<Object> results = new ArrayList<Object>();
        List<TFacilityCycle> tFacilityCycle = new ArrayList<TFacilityCycle>();

        Object minInvestigationStartDate;
        Object maxInvestigationStartDate;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));


        //query to get the minimum start date of an investigation for an instrument
        String minInvestigationStartDateQuery = "SELECT MIN(inv.startDate) FROM Investigation inv, inv.investigationInstruments invins WHERE invins.instrument.id = '" + instrumentId + "'";

        //query to get the maximum start date of an investigation for an instrument
        String maxInvestigationStartDateQuery = "SELECT MAX(inv.startDate) FROM Investigation inv, inv.investigationInstruments invins WHERE invins.instrument.id = '" + instrumentId + "'";

        try {
            minInvestigationStartDate  = service.search(icatSessionId, minInvestigationStartDateQuery).get(0);
            maxInvestigationStartDate  = service.search(icatSessionId, maxInvestigationStartDateQuery).get(0);

            Date minDate = XMLGregorianCalendarConversionUtil.asDate((XMLGregorianCalendar) minInvestigationStartDate);
            Date maxDate = XMLGregorianCalendarConversionUtil.asDate((XMLGregorianCalendar) maxInvestigationStartDate);

            logger.debug(minDate.toString());
            logger.debug(maxDate.toString());

            //query to get the facilitycycles for when the instruments was used
            String query = "SELECT c FROM FacilityCycle c WHERE (c.startDate >= {ts " + df.format(minDate) + "} AND c.startDate <= {ts " + df.format(maxDate) + "}) "
                    + "OR (c.endDate >= {ts " + df.format(minDate) + "} AND c.endDate <= {ts " + df.format(maxDate) + "}) ORDER BY c.name DESC";

            logger.debug(query);

            results = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        } catch (IndexOutOfBoundsException e) {
            return tFacilityCycle;
        }

        logger.debug("getFacilityCycleByInstrumentId size: " + results.size());

        for(Object result : results) {
            tFacilityCycle.add(facilityCycleToTFacilityCycle((FacilityCycle) result));
        }

        return tFacilityCycle;
    }

    @Override
    public Long getFacilityCycleByInstrumentIdCount(String icatSessionId,
            Long instrumentId) throws TopcatException {
        Long count = 0L;
        List<Object> results = new ArrayList<Object>();

        Object minInvestigationStartDate;
        Object maxInvestigationStartDate;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));


        //query to get the minimum start date of an investigation for an instrument
        String minInvestigationStartDateQuery = "SELECT MIN(inv.startDate) FROM Investigation inv, inv.investigationInstruments invins WHERE invins.instrument.id = '" + instrumentId + "'";

        //query to get the maximum start date of an investigation for an instrument
        String maxInvestigationStartDateQuery = "SELECT MAX(inv.startDate) FROM Investigation inv, inv.investigationInstruments invins WHERE invins.instrument.id = '" + instrumentId + "'";

        try {
            minInvestigationStartDate  = service.search(icatSessionId, minInvestigationStartDateQuery).get(0);
            maxInvestigationStartDate  = service.search(icatSessionId, maxInvestigationStartDateQuery).get(0);

            Date minDate = XMLGregorianCalendarConversionUtil.asDate((XMLGregorianCalendar) minInvestigationStartDate);
            Date maxDate = XMLGregorianCalendarConversionUtil.asDate((XMLGregorianCalendar) maxInvestigationStartDate);

            //query to get the facilitycycles for when the instruments was used
            String query = "SELECT count(c) FROM FacilityCycle c WHERE (c.startDate >= {ts " + df.format(minDate) + "} AND c.startDate <= {ts " + df.format(maxDate) + "}) "
                    + "OR (c.endDate >= {ts " + df.format(minDate) + "} AND c.endDate <= {ts " + df.format(maxDate) + "})";

            results = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        if (results.size() != 0) {
            count = (Long) results.get(0);
        }

        return count;
    }

    @Override
    public List<TFacilityCycle> getFacilityCycleByInstrumentIdPaginated(
            String icatSessionId, Long instrumentId, Integer offset,
            Integer numberOfRows, String sortBy, SortOrder order)
            throws IcatException {
        List<Object> results = new ArrayList<Object>();
        List<TFacilityCycle> tFacilityCycle = new ArrayList<TFacilityCycle>();

        Object minInvestigationStartDate;
        Object maxInvestigationStartDate;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));


        //query to get the minimum start date of an investigation for an instrument
        String minInvestigationStartDateQuery = "SELECT MIN(inv.startDate) FROM Investigation inv, inv.investigationInstruments invins WHERE invins.instrument.id = '" + instrumentId + "'";

        //query to get the maximum start date of an investigation for an instrument
        String maxInvestigationStartDateQuery = "SELECT MAX(inv.startDate) FROM Investigation inv, inv.investigationInstruments invins WHERE invins.instrument.id = '" + instrumentId + "'";

        try {
            minInvestigationStartDate  = service.search(icatSessionId, minInvestigationStartDateQuery).get(0);
            maxInvestigationStartDate  = service.search(icatSessionId, maxInvestigationStartDateQuery).get(0);

            Date minDate = XMLGregorianCalendarConversionUtil.asDate((XMLGregorianCalendar) minInvestigationStartDate);
            Date maxDate = XMLGregorianCalendarConversionUtil.asDate((XMLGregorianCalendar) maxInvestigationStartDate);

            logger.debug(minDate.toString());
            logger.debug(maxDate.toString());

            //query to get the facilitycycles for when the instruments was used
            String query = "SELECT c FROM FacilityCycle c WHERE (c.startDate >= {ts " + df.format(minDate) + "} AND c.startDate <= {ts " + df.format(maxDate) + "}) "
                    + "OR (c.endDate >= {ts " + df.format(minDate) + "} AND c.endDate <= {ts " + df.format(maxDate) + "}) ORDER BY c." + sortBy
                + " " + order.getValue() + " LIMIT " + offset.toString() + ", " + numberOfRows.toString();

            logger.debug(query);

            results = service.search(icatSessionId, query);
        } catch (IcatException_Exception e) {
            throw new IcatException(e.getMessage());
        }

        logger.debug("getFacilityCycleByInstrumentIdPaginated size: " + results.size());

        for(Object result : results) {
            tFacilityCycle.add(facilityCycleToTFacilityCycle((FacilityCycle) result));
        }

        return tFacilityCycle;
    }


    //TODO
    /**
     * Determine if an IcatException_Exception is an INSUFFICIENT_PRIVILEGES or SESSION type
     * exception
     *
     * @param e
     * @throws AuthenticationException
     * @throws IcatException
     * @throws ForbiddenException
     * @throws BadRequestException
     * @throws NotFoundException
     */
    private void throwNewICATException(IcatException_Exception e) throws TopcatException {
        logger.debug("icatExceptionType: " + e.getFaultInfo().getType());

        switch(e.getFaultInfo().getType()) {
        case INSUFFICIENT_PRIVILEGES:
            throw new ForbiddenException(e.getMessage());
        case BAD_PARAMETER:
            throw new BadRequestException(e.getMessage());
        case INTERNAL:
            throw new IcatException(e.getMessage());
        case SESSION:
            throw new AuthenticationException(e.getMessage());
        case NO_SUCH_OBJECT_FOUND:
            throw new NotFoundException(e.getMessage());
        case VALIDATION:
            throw new IcatException(e.getMessage());
        default:
            throw new IcatException(e.getMessage());
        }

        /*
        if (e.getFaultInfo().getType() == IcatExceptionType.INSUFFICIENT_PRIVILEGES || e.getFaultInfo().getType() == IcatExceptionType.SESSION) {
            throw new AuthenticationException(e.getMessage());
        } else {
            throw new IcatException(e.getMessage());
        }
        */


    }


}
