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

    private static FanoutCascade INSTANCE = new FanoutCascade();

    public static FanoutCascade getInstance() {
        return INSTANCE;
    }

    private Map<Class<? extends HandlerTask>, FanoutCascadeLayer> layers;

    private FanoutCascade() {
        this.layers = emptyLayersMap();
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

    public void purgeShutdownLayers() {
        // Removes all layers which have shutdown
        for (Class<? extends HandlerTask> clazz : this.layers.keySet()) {
            if (this.layers.get(clazz).isShutdown()) {
                this.layers.remove(clazz);
            }
        }
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

    public void registerShutdownThread() {
        // Register a Runtime shutdown thread for the cascade
        Runtime.getRuntime().addShutdownHook(new ShutDownThread());
    }

    @Override
    public void close() throws Exception {
        // Triggers close on all layers
        for (Class<? extends HandlerTask> clazz : getInstance().getRegisteredTasks()) {
            getInstance().getLayerForTask(clazz).close();
        }

        boolean isShutdown = false;
        while (!isShutdown) {
            isShutdown = true;
            for (Class<? extends HandlerTask> clazz : getInstance().getRegisteredTasks()) {
                if (!getInstance().getLayerForTask(clazz).isShutdown()) {
                    isShutdown = false;
                }
            }
        }
    }

    private static Map<Class<? extends HandlerTask>, FanoutCascadeLayer> emptyLayersMap() {
        return new ConcurrentHashMap<>();
    }

    // Shutdown thread
    public static class ShutDownThread extends Thread {

        private final Logger LOGGER = LoggerFactory.getLogger(ShutDownThread.class);

        @Override
        public void run() {
            try {
                LOGGER.info("Triggering shutdown of all layers");
                FanoutCascade.getInstance().close();
            } catch (Exception e) {
                LOGGER.error("Unable to shutdown FanoutCascace", e);
            }
        }

    }
}
