package com.github.onsdigital.fanoutcascade.pool;

import com.github.onsdigital.fanoutcascade.exceptions.DefaultExceptionHandler;
import com.github.onsdigital.fanoutcascade.exceptions.ExceptionHandler;
import com.github.onsdigital.fanoutcascade.handlers.FanoutCascadeMonitoringHandler;
import com.github.onsdigital.fanoutcascade.handlers.Handler;
import com.github.onsdigital.fanoutcascade.handlertasks.FanoutCascadeMonitoringTask;
import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class FanoutCascadeRegistry {

    private Map<Class<? extends HandlerTask>, Class<? extends Handler>> taskRegistry;
    private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

    private static FanoutCascadeRegistry INSTANCE = new FanoutCascadeRegistry();

    private FanoutCascadeRegistry() {
        this.taskRegistry = new ConcurrentHashMap<>();
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

    public void registerMonitoringThread() {
        if (!taskRegistry.containsKey(FanoutCascadeMonitoringTask.class)) {
            register(FanoutCascadeMonitoringTask.class, FanoutCascadeMonitoringHandler.class, 1);
        }
    }

    // Registers a handler for this task and creates a dedicated FanoutCascade layer
    public void register(Class<? extends HandlerTask> task, Class<? extends Handler> handler, int numThreads) {
        taskRegistry.put(task, handler);
        FanoutCascade.getInstance().registerLayer(task, numThreads);
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
