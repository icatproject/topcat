/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.core.icat;

import java.util.ArrayList;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.exception.ICATMethodNotFoundException;
import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TopcatException;
import uk.ac.stfc.topcat.core.gwt.module.TopcatExceptionType;

/**
 * 
 * @author sn65
 */
public class ICATWebInterfaceBase {
    public String loginLifetime(String username, String password, int hours) throws AuthenticationException {
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

    public ArrayList<String> listInstruments(String sessionId) {
        return new ArrayList<String>();
    }

    public ArrayList<String> listInvestigationTypes(String sessionId) {
        return new ArrayList<String>();
    }

    public ArrayList<TFacilityCycle> listFacilityCycles(String sessionId) throws ICATMethodNotFoundException {
        return new ArrayList<TFacilityCycle>();
    }

    public ArrayList<TFacilityCycle> listFacilityCyclesForInstrument(String sessionId, String instrument)
            throws ICATMethodNotFoundException {
        return new ArrayList<TFacilityCycle>();
    }

    public ArrayList<TInvestigation> getMyInvestigations(String sessionId) {
        return new ArrayList<TInvestigation>();
    }

    public ArrayList<TInvestigation> searchByAdvancedPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) throws TopcatException {
        return new ArrayList<TInvestigation>();
    }

    public ArrayList<TDatafile> searchDatafilesByParameter(String sessionId, TAdvancedSearchDetails details)
            throws TopcatException {
        throw new TopcatException("Parameter searching is not currently supported by this server",
                TopcatExceptionType.NOT_SUPPORTED);
    }

    public TInvestigation getInvestigationDetails(String sessionId, Long investigationId)
            throws AuthenticationException {
        return new TInvestigation();
    }

    public ArrayList<TDataset> getDatasetsInInvestigation(String sessionId, Long investigationId) {
        return new ArrayList<TDataset>();
    }

    public ArrayList<TDatasetParameter> getParametersInDataset(String sessionId, Long datasetId) {
        return new ArrayList<TDatasetParameter>();
    }

    public String getDatasetName(String sessionId, Long datasetId) {
        return "";
    }

    public ArrayList<TDatafile> getDatafilesInDataset(String sessionId, Long datasetId) {
        return new ArrayList<TDatafile>();
    }

    public ArrayList<TDatafileParameter> getParametersInDatafile(String sessionId, Long datafileId) {
        return new ArrayList<TDatafileParameter>();
    }

    public ArrayList<String> getParameterNames(String sessionId) throws TopcatException {
        throw new TopcatException("getParameterNames is not supported", TopcatExceptionType.NOT_SUPPORTED);
    }

    public ArrayList<String> getParameterUnits(String sessionId, String name) throws TopcatException {
        throw new TopcatException("getParameterUnits is not supported", TopcatExceptionType.NOT_SUPPORTED);
    }

    public String getParameterType(String sessionId, String name, String units) throws TopcatException {
        throw new TopcatException("getParameterType is not supported", TopcatExceptionType.NOT_SUPPORTED);
    }

    public String downloadDatafiles(String sessionId, ArrayList<Long> datafileIds) {
        return null;
    }

    public String downloadDataset(String sessionId, Long datasetIds) {
        return null;
    }

    public ArrayList<String> getKeywordsForUser(String sessionId) {
        return new ArrayList<String>();
    }

    public ArrayList<String> getKeywordsInInvestigation(String sessionId, Long investigationId) {
        return new ArrayList<String>();
    }

    public ArrayList<TInvestigation> searchByKeywords(String sessionId, ArrayList<String> keywords) {
        return new ArrayList<TInvestigation>();
    }

    public ArrayList<TDatafile> searchDatafilesByRunNumber(String sessionId, ArrayList<String> instruments,
            float startRunNumber, float endRunNumber) {
        return new ArrayList<TDatafile>();
    }

    public ArrayList<String> getKeywordsForUserWithStartMax(String sessionId, String partialKey, int numberOfKeywords) {
        return new ArrayList<String>();
    }

    public String loginWithTicket(String authenticationServiceUrl, String ticket) throws AuthenticationException {
        throw new UnsupportedOperationException();
    }

    public String getUserNameFromSessionId(String icatSessionId) {
        throw new UnsupportedOperationException();
    }

}
