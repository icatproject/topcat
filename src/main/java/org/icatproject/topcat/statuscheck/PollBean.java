package org.icatproject.topcat.statuscheck;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class PollBean {
    private List<String> preparedIds;

    @EJB
    private PollFutureBean pollFutureBean;

    @PostConstruct
    public void init() {
        preparedIds = new ArrayList<String>();
    }

    public List<String> getAll() {
        return preparedIds;
    }

    public boolean has(String preparedId) {
        return preparedIds.contains(preparedId);
    }

    public void add(String preparedId) {
        preparedIds.add(preparedId);
    }

    public void remove(String preparedId) {
        preparedIds.remove(preparedId);
        pollFutureBean.remove(preparedId);
    }

    public void removeAll() {
        preparedIds.clear();
    }

}
