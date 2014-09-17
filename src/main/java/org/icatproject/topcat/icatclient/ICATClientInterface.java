package org.icatproject.topcat.icatclient;

import java.util.List;
import java.util.Map;

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
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;

public interface ICATClientInterface {
    public String login(String authenticationType, Map<String, String> parameters) throws AuthenticationException, InternalException;
    public String getUserName(String icatSessionId) throws IcatException;
    public Boolean isSessionValid(String icatSessionId) throws IcatException;
    public Long getRemainingMinutes(String icatSessionId) throws IcatException;
    public void refresh(String icatSessionId) throws IcatException;
    public void logout(String icatSessionId) throws IcatException;
    public List<TFacility> getFacilities(String icatSessionId) throws IcatException;
    public TFacility getFacilityById(String icatSessionId, Long id) throws IcatException;
    public List<TFacilityCycle> getFacilityCyclesByFacilityId(String icatSessionId, Long facilityId) throws IcatException;
    public List<TDatasetType> getDatasetTypesByFacilityId(String icatSessionId, Long facilityId) throws IcatException;
    public List<TDatafileFormat> getDatafileFormatsByFacilityId(String icatSessionId, Long facilityId) throws IcatException;
    public List<TParameterType> getParameterTypesByFacilityId(String icatSessionId, Long facilityId) throws IcatException;
    public List<TInvestigationType> getInvestigationTypesByFacilityId(String icatSessionId, Long facilityId) throws IcatException;
    public List<TInstrument> getInstrumentsByfacilityId(String icatSessionId, Long facilityId) throws IcatException;
    public List<TInvestigation> getInvestigationsByInstrumentId(String icatSessionId, Long instrumentId) throws IcatException;

    public Long getInvestigationsByInstrumentIdCount(String icatSessionId, Long instrumentId) throws IcatException;
    public List<TInvestigation> getInvestigationsByInstrumentIdPaginated(String icatSessionId, Long instrumentId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException;
    public TInvestigation getInvestigationById(String icatSessionId, Long investigationId) throws IcatException;

    public List<TDataset> getDatasetsByInvestigationId(String icatSessionId, Long investigationId) throws IcatException;
    public Long getDatasetsByInvestigationIdCount(String icatSessionId, Long instrumentId) throws IcatException;
    public List<TDataset> getDatasetsByInvestigationIdPaginated(String icatSessionId, Long investigationId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException;

    public List<TInvestigation> getInvestigationsByFacilityCycleId(String icatSessionId, Long facilityCycleId) throws IcatException;
    public Long getInvestigationsByFacilityCycleIdCount(String icatSessionId, Long facilityCycleId) throws IcatException;
    public List<TInvestigation> getInvestigationsByFacilityCycleIdPaginated(String icatSessionId, Long facilityCycleId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException;

    public List<TFacilityCycle> getFacilityCycleByInstrumentId(String icatSessionId, Long instrumentId) throws IcatException;
    public Long getFacilityCycleByInstrumentIdCount(String icatSessionId, Long instrumentId) throws IcatException;
    public List<TFacilityCycle> getFacilityCycleByInstrumentIdPaginated(String icatSessionId, Long instrumentId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws IcatException;


}
