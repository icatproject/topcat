/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.core.icat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileFormat;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.NotSupportedException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;

/**
 * 
 * @author sn65
 */
public class ICATWebInterfaceBase {
    public String loginLifetime(String authenticationType, Map<String, String> parameters, int hours)
            throws AuthenticationException {
        return null;
    }

    public void logout(String sessionId) throws AuthenticationException {
    }

    public Boolean isSessionValid(String sessionId) {
        return new Boolean(false);
    }

    public String getUserSurname(String sessionId, String userId) {
        return userId; // If not implemented just return the userid
    }

    public ArrayList<String> listInstruments(String sessionId) throws TopcatException {
        return new ArrayList<String>();
    }

    public ArrayList<String> listInvestigationTypes(String sessionId) throws TopcatException {
        return new ArrayList<String>();
    }

    public List<TDatafileFormat> listDatafileFormats(String sessionId) throws TopcatException {
        throw new NotSupportedException("listDatafileFormats is not supported");
    }

    public List<String> listDatasetTypes(String sessionId) throws TopcatException {
        throw new NotSupportedException("listDatasetTypes is not supported");
    }

    public List<TFacilityCycle> listFacilityCycles(String sessionId) throws TopcatException {
        throw new TopcatException("listFacilityCycles is not supported by this server");
    }

    public List<TFacilityCycle> listFacilityCyclesForInstrument(String sessionId, String instrument)
            throws TopcatException {
        throw new NotSupportedException("listFacilityCyclesForInstrument is not supported by this server");
    }

    public List<TInvestigation> getMyInvestigations(String sessionId) throws TopcatException {
        return new ArrayList<TInvestigation>();
    }

    public List<TInvestigation> getAllInvestigations(String sessionId) throws TopcatException {
        return new ArrayList<TInvestigation>();
    }

    public ArrayList<TInvestigation> searchByAdvancedPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) throws TopcatException {
        return new ArrayList<TInvestigation>();
    }    
    
    public ArrayList<TInvestigation> searchByFreeTextPagination(String sessionId, TAdvancedSearchDetails searchDetails,
            int start, int end) throws TopcatException {
        throw new NotSupportedException("Free text search is not currently supported by this server.");
    }    

    public ArrayList<TDatafile> searchDatafilesByParameter(String sessionId, TAdvancedSearchDetails details)
            throws TopcatException {
        throw new NotSupportedException("Parameter searching is not currently supported by this server");
    }

    public TInvestigation getInvestigationDetails(String sessionId, Long investigationId)
            throws AuthenticationException, TopcatException {
        return new TInvestigation();
    }

    public ArrayList<TDataset> getDatasetsInInvestigation(String sessionId, Long investigationId)
            throws TopcatException {
        return new ArrayList<TDataset>();
    }

    public ArrayList<TDatasetParameter> getParametersInDataset(String sessionId, Long datasetId) throws TopcatException {
        return new ArrayList<TDatasetParameter>();
    }

    public String getDatasetName(String sessionId, Long datasetId) throws TopcatException {
        return "";
    }

    public ArrayList<TDatafile> getDatafilesInDataset(String sessionId, Long datasetId) throws TopcatException {
        return new ArrayList<TDatafile>();
    }

    public ArrayList<TDatafileParameter> getParametersInDatafile(String sessionId, Long datafileId)
            throws TopcatException {
        return new ArrayList<TDatafileParameter>();
    }

    public ArrayList<String> getParameterNames(String sessionId) throws TopcatException {
        throw new NotSupportedException("getParameterNames is not supported");
    }

    public ArrayList<String> getParameterUnits(String sessionId, String name) throws TopcatException {
        throw new NotSupportedException("getParameterUnits is not supported");
    }

    public ArrayList<String> getParameterTypes(String sessionId, String name, String units) throws TopcatException {
        throw new NotSupportedException("getParameterType is not supported");
    }

    public String downloadDatafiles(String sessionId, List<Long> datafileIds) {
        return null;
    }

    public String downloadDataset(String sessionId, Long datasetIds) {
        return null;
    }

    public ArrayList<String> getKeywordsForUser(String sessionId) throws TopcatException {
        return new ArrayList<String>();
    }

    public ArrayList<String> getKeywordsInInvestigation(String sessionId, Long investigationId) throws TopcatException {
        return new ArrayList<String>();
    }

    public ArrayList<TInvestigation> searchByKeywords(String sessionId, ArrayList<String> keywords)
            throws TopcatException {
        return new ArrayList<TInvestigation>();
    }

    public ArrayList<TDatafile> searchDatafilesByRunNumber(String sessionId, ArrayList<String> instruments,
            float startRunNumber, float endRunNumber) throws TopcatException {
        return new ArrayList<TDatafile>();
    }

    public ArrayList<String> getKeywordsForUserWithStartMax(String sessionId, String partialKey, int numberOfKeywords)
            throws TopcatException {
        return new ArrayList<String>();
    }

    public String getUserName(String icatSessionId) throws TopcatException {
        throw new UnsupportedOperationException();
    }

    public Long createDataSet(String sessionId, TDataset dataset) throws TopcatException {
        throw new NotSupportedException("createDataSet is not supported");
    }

}
