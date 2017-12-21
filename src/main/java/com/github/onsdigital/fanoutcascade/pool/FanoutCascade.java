package com.github.onsdigital.fanoutcascade.pool;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class FanoutCascade implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FanoutCascade.class);

    private Map<Class<? extends HandlerTask>, FanoutCascadeLayer> layers;

    private static FanoutCascade INSTANCE = new FanoutCascade();

    private FanoutCascade() {
        this.layers = new ConcurrentHashMap<Class<? extends HandlerTask>, FanoutCascadeLayer>();
    }

    public static FanoutCascade getInstance() {
        return INSTANCE;
    }

    public boolean hasLayer(Class<? extends HandlerTask> clazz) {
        return this.layers.containsKey(clazz);
    }

    public void registerLayer(Class<? extends HandlerTask> clazz, int numThreads) {
        if (!this.hasLayer(clazz)) {
            this.layers.put(clazz, new FanoutCascadeLayer(numThreads));
        } else {
            throw new RuntimeException("FanoutCascadeLayer already exists for class " + clazz.getName());
        }
    }

    public Set<Class<? extends HandlerTask>> getRegisteredTasks() {
        return this.layers.keySet();
    }

    public FanoutCascadeLayer getLayerForTask(Class<? extends HandlerTask> clazz) {
        if (!this.hasLayer(clazz)) {
            String message = "No layer registered for class " + clazz.getName();
            RuntimeException e = new RuntimeException(message);
            LOGGER.error(message, e);
            throw e;
        }
        return layers.get(clazz);
    }

    public synchronized boolean isShutdown() {
        for (Class<? extends HandlerTask> clazz : getInstance().getRegisteredTasks()) {
            boolean isLayerShutdown = getInstance().getLayerForTask(clazz).isShutdown();
            if (!isLayerShutdown) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        // Triggers close on all layers
        for (Class<? extends HandlerTask> clazz : getInstance().getRegisteredTasks()) {
            getInstance().getLayerForTask(clazz).close();
        }
    }
}
