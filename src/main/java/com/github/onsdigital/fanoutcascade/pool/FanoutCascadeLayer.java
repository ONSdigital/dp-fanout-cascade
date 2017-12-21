package com.github.onsdigital.fanoutcascade.pool;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FanoutCascadeLayer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FanoutCascadeLayer.class);

    private ExecutorService executorService;
    private Map<HandlerTask, Future<Object>> tasks;

    protected FanoutCascadeLayer(int numThreads) {
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.tasks = new ConcurrentHashMap<HandlerTask, Future<Object>>();
    }

    public void submit(HandlerTask handlerTask) {
        if (!FanoutCascadeRegistry.getInstance().handlerRegisteredForTask(handlerTask.getClass())) {
            throw new RuntimeException("No Handler registered for HandlerTask " + handlerTask.getClass());
        }
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Submitting task %s to layer.", handlerTask.getClass().getName()));
        this.tasks.put(handlerTask, this.executorService.submit(handlerTask));
    }

    public Set<HandlerTask> getKeySet() {
        return this.tasks.keySet();
    }

    /**
     *
     * @param task
     * @return Future<Object>, which is removed from the task list
     */
    public synchronized Future<Object> popFuture(HandlerTask task) {
        return this.tasks.remove(task);
    }

    public synchronized boolean isIdle() {
        for (HandlerTask task : this.getKeySet()) {
            if (!this.tasks.get(task).isDone()) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean isShutdown() {
        return this.executorService.isShutdown();
    }

    @Override
    public void close() throws Exception {
        if (!this.executorService.isShutdown()) {
            this.executorService.shutdown();
        }
    }
}
