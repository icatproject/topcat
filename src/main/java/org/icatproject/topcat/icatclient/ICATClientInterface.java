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
import org.icatproject.topcat.exceptions.TopcatException;

public interface ICATClientInterface {
    public String login(String authenticationType, Map<String, String> parameters) throws AuthenticationException, InternalException;
    public String getUserName(String icatSessionId) throws TopcatException;
    public Boolean isSessionValid(String icatSessionId) throws TopcatException;
    public Long getRemainingMinutes(String icatSessionId) throws TopcatException;
    public void refresh(String icatSessionId) throws TopcatException;
    public void logout(String icatSessionId) throws TopcatException;
    public List<TFacility> getFacilities(String icatSessionId) throws TopcatException;
    public TFacility getFacilityById(String icatSessionId, Long id) throws TopcatException;
    public List<TFacilityCycle> getFacilityCyclesByFacilityId(String icatSessionId, Long facilityId) throws TopcatException;
    public List<TDatasetType> getDatasetTypesByFacilityId(String icatSessionId, Long facilityId) throws TopcatException;
    public List<TDatafileFormat> getDatafileFormatsByFacilityId(String icatSessionId, Long facilityId) throws TopcatException;
    public List<TParameterType> getParameterTypesByFacilityId(String icatSessionId, Long facilityId) throws TopcatException;
    public List<TInvestigationType> getInvestigationTypesByFacilityId(String icatSessionId, Long facilityId) throws TopcatException;
    public List<TInstrument> getInstrumentsByfacilityId(String icatSessionId, Long facilityId) throws TopcatException;
    public List<TInvestigation> getInvestigationsByInstrumentId(String icatSessionId, Long instrumentId) throws TopcatException;

    public Long getInvestigationsByInstrumentIdCount(String icatSessionId, Long instrumentId) throws TopcatException;
    public List<TInvestigation> getInvestigationsByInstrumentIdPaginated(String icatSessionId, Long instrumentId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws TopcatException;
    public TInvestigation getInvestigationById(String icatSessionId, Long investigationId) throws TopcatException;

    public List<TDataset> getDatasetsByInvestigationId(String icatSessionId, Long investigationId) throws TopcatException;
    public Long getDatasetsByInvestigationIdCount(String icatSessionId, Long instrumentId) throws TopcatException;
    public List<TDataset> getDatasetsByInvestigationIdPaginated(String icatSessionId, Long investigationId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws TopcatException;

    public List<TInvestigation> getInvestigationsByFacilityCycleId(String icatSessionId, Long facilityCycleId) throws TopcatException;
    public Long getInvestigationsByFacilityCycleIdCount(String icatSessionId, Long facilityCycleId) throws TopcatException;
    public List<TInvestigation> getInvestigationsByFacilityCycleIdPaginated(String icatSessionId, Long facilityCycleId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws TopcatException;

    public List<TFacilityCycle> getFacilityCycleByInstrumentId(String icatSessionId, Long instrumentId) throws TopcatException;
    public Long getFacilityCycleByInstrumentIdCount(String icatSessionId, Long instrumentId) throws TopcatException;
    public List<TFacilityCycle> getFacilityCycleByInstrumentIdPaginated(String icatSessionId, Long instrumentId, Integer offset, Integer numberOfRows, String sortBy, SortOrder order) throws TopcatException;


}
