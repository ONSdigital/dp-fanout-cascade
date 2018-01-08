package com.github.onsdigital.fanoutcascade.handlertasks;

import com.github.onsdigital.fanoutcascade.handlers.Handler;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascade;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeLayer;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public abstract class HandlerTask implements Callable<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerTask.class);
    private Class<? extends HandlerTask> handlerTask;

    public HandlerTask(Class<? extends HandlerTask> handlerTask) {
        this.handlerTask = handlerTask;
    }

    // Submit a task back into the cascade
    private void submitTask(HandlerTask task) {
        Class<? extends HandlerTask> taskClazz = task.getClass();
        if (FanoutCascade.getInstance().hasLayer(taskClazz)) {
            FanoutCascade.getInstance().getLayerForTask(taskClazz).submit(task);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object call() throws Exception {
        Class<? extends Handler> clazz = FanoutCascadeRegistry.getInstance().getHandlerForTask(this.handlerTask);
        try {
            Handler handler = clazz.newInstance();
            Object obj = null;
            try {
                obj = handler.handleTask(this);
            } catch (Exception e) {
                // Handle any exception raised in the handler
                FanoutCascadeLayer layer = FanoutCascade.getInstance().getLayerForTask(this.handlerTask);
                FanoutCascadeRegistry.getInstance().getExceptionHandler().handleLayerException(this, layer, e);
            }

            if (obj instanceof HandlerTask) {
                // Submit back into the cascade
                HandlerTask task = (HandlerTask) obj;
                this.submitTask(task);
            } else if (obj instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) obj;
                for (Object o : collection) {
                    if (o instanceof HandlerTask) {
                        HandlerTask task = (HandlerTask) o;
                        this.submitTask(task);
                    }
                }
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Error instantiating class", e);
        }
        // Nothing to return
        return null;
    }
}
