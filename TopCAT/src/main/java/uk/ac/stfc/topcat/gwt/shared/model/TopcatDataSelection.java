package uk.ac.stfc.topcat.gwt.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * To build set of data to be processed by the IDS.
 */
public class TopcatDataSelection implements Serializable{

    private static final long serialVersionUID = 1L;

    private Set<Long> datafileIds = new HashSet<Long>();

    private Set<Long> datasetIds = new HashSet<Long>();

    private Set<Long> investigationIds = new HashSet<Long>();

    /**
     * @param datafileId
     * @return itself to allow chaining of addXXX calls
     */
    public TopcatDataSelection addDatafile(long datafileId) {
        datafileIds.add(datafileId);
        return this;
    }

    /**
     * @param datafileIds
     * @return itself to allow chaining of addXXX calls
     */
    public TopcatDataSelection addDatafiles(List<Long> datafileIds) {
        this.datafileIds.addAll(datafileIds);
        return this;
    }

    /**
     * @param datasetId
     * @return itself to allow chaining of addXXX calls
     */
    public TopcatDataSelection addDataset(long datasetId) {
        datasetIds.add(datasetId);
        return this;
    }

    /**
     * @param datasetIds
     * @return itself to allow chaining of addXXX calls
     */
    public TopcatDataSelection addDatasets(List<Long> datasetIds) {
        this.datasetIds.addAll(datasetIds);
        return this;
    }

    /**
     * @param investigationId
     * @return itself to allow chaining of addXXX calls
     */
    public TopcatDataSelection addInvestigation(long investigationId) {
        investigationIds.add(investigationId);
        return this;
    }

    /**
     * @param investigationIds
     * @return itself to allow chaining of addXXX calls
     */
    public TopcatDataSelection addInvestigations(List<Long> investigationIds) {
        this.investigationIds.addAll(investigationIds);
        return this;
    }

    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        if (!investigationIds.isEmpty()) {
            parameters.put("investigationIds", setToString(investigationIds));
        }
        if (!datasetIds.isEmpty()) {
            parameters.put("datasetIds", setToString(datasetIds));
        }
        if (!datafileIds.isEmpty()) {
            parameters.put("datafileIds", setToString(datafileIds));
        }
        return parameters;
    }

    private String setToString(Set<Long> ids) {
        StringBuilder sb = new StringBuilder();
        for (long id : ids) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append(Long.toString(id));
        }
        return sb.toString();
    }

    public Set<Long> getDatafileIds() {
        return datafileIds;
    }

    public Set<Long> getDatasetIds() {
        return datasetIds;
    }

    public Set<Long> getInvestigationIds() {
        return investigationIds;
    }
}
