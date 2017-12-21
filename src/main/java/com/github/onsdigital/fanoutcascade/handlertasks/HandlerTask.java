package com.github.onsdigital.fanoutcascade.handlertasks;

import com.github.onsdigital.fanoutcascade.handlers.Handler;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public abstract class HandlerTask implements Callable<Object> {

    private static Logger LOGGER = LoggerFactory.getLogger(HandlerTask.class);
    private Class<? extends HandlerTask> handlerTask;

    public HandlerTask(Class<? extends HandlerTask> handlerTask) {
        this.handlerTask = handlerTask;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object call() throws Exception {
        Class<? extends Handler> clazz = FanoutCascadeRegistry.getInstance().getHandlerForTask(this.handlerTask);
        try {
            Handler handler = clazz.newInstance();
            Object obj = handler.handleTask(this);

            if (obj instanceof HandlerTask) {
                // Submit back into the cascade
//                FanoutCascade.getInstance().getLayer().submit((HandlerTask) obj);
            }
            return obj;
        } catch (InstantiationException e) {
            LOGGER.error("Error instantiating class", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Unable to instantiate class (illegal access)", e);
        }
        return null;
    }
}
