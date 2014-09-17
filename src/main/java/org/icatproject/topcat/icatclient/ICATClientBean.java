package org.icatproject.topcat.icatclient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

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
import org.icatproject.topcat.domain.TopcatIcatServer;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.repository.ServerRepository;

@Stateless
public class ICATClientBean {
    @EJB
    private ServerRepository serverRepository;

    /**
     * login to an icat server
     *
     * @param serverName the server name
     * @param authenticationType the authentication type
     * @param parameters
     * @return the icat session id
     * @throws MalformedURLException
     * @throws AuthenticationException
     * @throws InternalException
     */
    public String login(String serverName, String authenticationType, Map<String, String> parameters) throws MalformedURLException, AuthenticationException, InternalException {
        ICATClientInterface service = getIcatService(serverName);

        return service.login(authenticationType, parameters);
    }

    /**
     * Check if icatSessionId is valid on a server
     *
     * @param serverName the server name
     * @param icatSessionId the icat session id
     * @return whether the session id valid or not
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public boolean isSessionValid(String serverName, String icatSessionId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.isSessionValid(icatSessionId);
    }


    /**
     * Returns the remaining minutes of a icat session
     *
     * @param serverName the server name
     * @param icatSessionId the icat session id
     * @return the remaining minutes or -1 if session is no valid
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public Long getRemainingMinutes(String serverName, String icatSessionId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getRemainingMinutes(icatSessionId);
    }


    /**
     * Returns a list of facilities from an icat server
     *
     * @param serverName
     * @param icatSessionId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TFacility> getFacilities(String serverName, String icatSessionId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getFacilities(icatSessionId);
    }

    /**
     * Return a facility by id
     *
     * @param serverName
     * @param icatSessionId
     * @param id
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public TFacility getFacilityById(String serverName, String icatSessionId, Long id) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getFacilityById(icatSessionId, id);
    }

    /**
     * Returns list of facility cycles for a facility
     *
     * @param serverName
     * @param icatSessionId
     * @param facilityId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TFacilityCycle> getFacilityCyclesByFacilityId(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getFacilityCyclesByFacilityId(icatSessionId, facilityId);
    }

    /**
     * Returns a list for dataset types for a facility
     *
     * @param serverName
     * @param icatSessionId
     * @param facilityId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TDatasetType> getDatasetTypesByFacilityId(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getDatasetTypesByFacilityId(icatSessionId, facilityId);
    }

    /**
     * Returns a list of data file format for a facility
     *
     * @param serverName
     * @param icatSessionId
     * @param facilityId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TDatafileFormat> getDatafileFormatsByFacilityId(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getDatafileFormatsByFacilityId(icatSessionId, facilityId);
    }



    /**
     * Returns a list of parameter types for a facility
     *
     * @param serverName
     * @param icatSessionId
     * @param facilityId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TParameterType> getParameterTypesByFacilityId(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getParameterTypesByFacilityId(icatSessionId, facilityId);
    }

    /**
     * Returns a list of investigation types for a facility
     *
     * @param serverName
     * @param icatSessionId
     * @param facilityId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TInvestigationType> getInvestigationTypesByFacilityId(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationTypesByFacilityId(icatSessionId, facilityId);
    }

    /**
     * Return a list of instruments for a facility
     *
     * @param serverName
     * @param icatSessionId
     * @param facilityId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TInstrument> getInstrumentsByfacilityId(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInstrumentsByfacilityId(icatSessionId, facilityId);
    }

    /**
     * Return a list of investigation for an instrument
     *
     * @param serverName
     * @param icatSessionId
     * @param facilityId
     * @return
     * @throws IcatException
     * @throws InternalException
     * @throws MalformedURLException
     */
    public List<TInvestigation> getInvestigationsByInstrumentId(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationsByInstrumentId(icatSessionId, facilityId);
    }


    public Long getInvestigationsByInstrumentIdCount(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationsByInstrumentIdCount(icatSessionId, facilityId);
    }


    public List<TInvestigation> getInvestigationsByInstrumentIdPaginated(String serverName, String icatSessionId, Long facilityId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationsByInstrumentIdPaginated(icatSessionId, facilityId, offset, numberOfRows, sortBy, order);
    }


    public TInvestigation getInvestigationById(String serverName, String icatSessionId, Long id) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationById(icatSessionId, id);
    }


    public List<TDataset> getDatasetsByInvestigationId(String serverName, String icatSessionId, Long id) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getDatasetsByInvestigationId(icatSessionId, id);
    }


    public Long getDatasetsByInvestigationIdCount(String serverName, String icatSessionId, Long facilityId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getDatasetsByInvestigationIdCount(icatSessionId, facilityId);
    }


    public List<TDataset> getDatasetsByInvestigationIdPaginated(String serverName, String icatSessionId, Long investigationId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getDatasetsByInvestigationIdPaginated(icatSessionId, investigationId, offset, numberOfRows, sortBy, order);
    }


    public List<TInvestigation> getInvestigationsByFacilityCycleId(String serverName, String icatSessionId, Long facilityCycleId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationsByFacilityCycleId(icatSessionId, facilityCycleId);
    }


    public Long getInvestigationsByFacilityCycleIdCount(String serverName, String icatSessionId, Long facilityCycleId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationsByFacilityCycleIdCount(icatSessionId, facilityCycleId);
    }


    public List<TInvestigation> getInvestigationsByFacilityCycleIdPaginated(String serverName, String icatSessionId, Long facilityCycleId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getInvestigationsByFacilityCycleIdPaginated(icatSessionId, facilityCycleId, offset, numberOfRows, sortBy, order);
    }


    public List<TFacilityCycle> getFacilityCycleByInstrumentId(String serverName, String icatSessionId, Long instrumentId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getFacilityCycleByInstrumentId(icatSessionId, instrumentId);
    }


    public Long getFacilityCycleByInstrumentIdCount(String serverName, String icatSessionId, Long instrumentId) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getFacilityCycleByInstrumentIdCount(icatSessionId, instrumentId);
    }


    public List<TFacilityCycle> getFacilityCycleByInstrumentIdPaginated(String serverName, String icatSessionId, Long instrumentId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException, InternalException, MalformedURLException {
        ICATClientInterface service = getIcatService(serverName);

        return service.getFacilityCycleByInstrumentIdPaginated(icatSessionId, instrumentId, offset, numberOfRows, sortBy, order);
    }



    /**
     * Returns an icat service for a given server name
     *
     * @param serverName
     * @return
     * @throws InternalException
     * @throws MalformedURLException
     */
    private ICATClientInterface getIcatService(String serverName) throws InternalException, MalformedURLException {
        TopcatIcatServer icatServer = serverRepository.getServerByName(serverName);

        if (icatServer == null) {
            throw new InternalException("No ICAT server named " + serverName);
        }

        ICATClientInterface service = ICATClientFactory.getInstance().createICATClient(
                icatServer.getName(), icatServer.getVersion(), icatServer.getServerUrl());

        if (service == null) {
            throw new InternalException("unable to retrieve service with url " + icatServer.getServerUrl());
        }

        return service;
    }



}
