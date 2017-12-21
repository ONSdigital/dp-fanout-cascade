package com.github.onsdigital.fanoutcascade.pool;

import com.github.onsdigital.fanoutcascade.handlers.Handler;
import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class FanoutCascadeRegistry {

    private Map<Class<? extends HandlerTask>, Class<? extends Handler>> taskRegistry;

    private static FanoutCascadeRegistry INSTANCE = new FanoutCascadeRegistry();

    private FanoutCascadeRegistry() {
        this.taskRegistry = new ConcurrentHashMap<Class<? extends HandlerTask>, Class<? extends Handler>>();
    }

    public static FanoutCascadeRegistry getInstance() {
        return INSTANCE;
    }

    public boolean handlerRegisteredForTask(Class<? extends HandlerTask> clazz) {
        return taskRegistry.containsKey(clazz);
    }

    public Class<? extends Handler> getHandlerForTask(Class<? extends HandlerTask> clazz) {
        return taskRegistry.get(clazz);
    }

    public void register(Class<? extends HandlerTask> task, Class<? extends Handler> handler, int numThreads) {
        taskRegistry.put(task, handler);
        FanoutCascade.getInstance().registerLayer(task, numThreads);
    }

}
