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

    public ArrayList<TInvestigation> getMyInvestigationsIncludesPagination(String sessionId, int start, int end) {
        return new ArrayList<TInvestigation>();
    }

    public ArrayList<TInvestigation> searchByAdvancedPagination(String sessionId, TAdvancedSearchDetails details,
            int start, int end) {
        return new ArrayList<TInvestigation>();
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

    public ArrayList<TDatafile> getDatafilesInDataset(String sessionId, Long datasetId) {
        return new ArrayList<TDatafile>();
    }

    public ArrayList<TDatafileParameter> getParametersInDatafile(String sessionId, Long datafileId) {
        return new ArrayList<TDatafileParameter>();
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

    public ArrayList<TDatafile> searchByRunNumber(String sessionId, ArrayList<String> instruments,
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
