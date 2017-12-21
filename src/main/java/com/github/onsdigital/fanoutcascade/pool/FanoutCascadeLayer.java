package com.github.onsdigital.fanoutcascade.pool;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class FanoutCascadeLayer {

    private ExecutorService executorService;
    private Map<HandlerTask, Future<Object>> tasks;

    public FanoutCascadeLayer(int numThreads) {
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.tasks = new ConcurrentHashMap<HandlerTask, Future<Object>>();
    }

    public void submit(HandlerTask handlerTask) {
        if (!FanoutCascadeRegistry.getInstance().handlerRegisteredForTask(handlerTask.getClass())) {
            throw new RuntimeException("No Handler registered for HandlerTask " + handlerTask.getClass());
        }
        this.tasks.put(handlerTask, this.executorService.submit(handlerTask));
    }

    public Set<HandlerTask> getKeySet() {
        return this.tasks.keySet();
    }

    public Future<Object> getFuture(HandlerTask task) {
        return this.tasks.get(task);
    }

}
