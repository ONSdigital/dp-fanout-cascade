package com.github.onsdigital.fanoutcascade.pool;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class FanoutCascade {

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

    public FanoutCascadeLayer getLayerForTask(Class<? extends HandlerTask> clazz) {
        if (!this.hasLayer(clazz)) {
            throw new RuntimeException("No layer registered for class " + clazz.getName());
        }
        return layers.get(clazz);
    }
}
