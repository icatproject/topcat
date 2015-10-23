package org.icatproject.topcat.statuscheck;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class PollFutureBean {
    private Map<String, Future<?>> futures;

    @EJB
    private PollBean pollBean;

    @PostConstruct
    public void init() {
        futures = new HashMap<String, Future<?>>();
    }

    /**
     * add a task to map
     * @param preparedId
     * @param future
     */
    public void add(String preparedId, Future<?> future) {
        futures.put(preparedId, future);
    }

    public void remove(String preparedId) {
        futures.remove(preparedId);
    }


    /**
     * Cancel a task by preparedId
     *
     * @param preparedId
     * @return
     */
    public int cancelByPreparedId(String preparedId) {
        int count = 0;
        Future<?> future = futures.get(preparedId);

        if (future != null) {
            boolean result = future.cancel(true);

            if (result == true) {
                futures.remove(preparedId);
                pollBean.remove(preparedId);

                count++;
            }
        }

        return count;
    }


    /**
     * Cancel all polling tasks
     *
     * @return
     */
    public int cancelAll() {
        int count = 0;

        Iterator<Entry<String, Future<?>>> it = futures.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, Future<?>> entry = (Map.Entry<String, Future<?>>)it.next();

            Future<?> future = entry.getValue();

            if (future != null) {
                boolean result = future.cancel(true);

                if (result == true) {
                    futures.remove(entry.getKey());
                    pollBean.remove(entry.getKey());
                    count++;
                }
            }
        }

        return count;
    }
}
