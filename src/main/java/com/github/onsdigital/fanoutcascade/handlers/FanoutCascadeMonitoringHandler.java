package com.github.onsdigital.fanoutcascade.handlers;

import com.github.onsdigital.fanoutcascade.handlertasks.FanoutCascadeMonitoringTask;
import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascade;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author sullid (David Sullivan) on 22/12/2017
 * @project dp-fanout-cascade
 */
public class FanoutCascadeMonitoringHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FanoutCascadeMonitoringHandler.class);

    @Override
    public Object handleTask(HandlerTask handlerTask) {
        FanoutCascadeMonitoringTask fanoutCascadeMonitoringTask = (FanoutCascadeMonitoringTask) handlerTask;

        while (!FanoutCascade.getInstance().isShutdown()) {
            Set<Class<? extends HandlerTask>> classSet = FanoutCascade.getInstance().getRegisteredTasks();
            Iterator<Class<? extends HandlerTask>> it = classSet.iterator();

            while (it.hasNext()) {
                Class<? extends HandlerTask> clazz = it.next();
                FanoutCascadeLayer layer = FanoutCascade.getInstance().getLayerForTask(clazz);

                Set<HandlerTask> handlerTasks = layer.getKeySet();
                Iterator<HandlerTask> handlerTaskIterator = handlerTasks.iterator();

                while (handlerTaskIterator.hasNext()) {
                    HandlerTask task = handlerTaskIterator.next();
                    Future<Object> future = layer.getFuture(task);

                    if (future.isDone()) {
                        try {
                            Object result = future.get();
                            if (null == result) {
                                // Purge this task
                                String message = String.format("FanoutCascadeMonitoringHandler found null result in layer %s, purging.", layer);
                                if (LOGGER.isDebugEnabled()) LOGGER.debug(message);
                                layer.popFuture(task);
                            }
                        } catch (InterruptedException e) {
                            LOGGER.error("Interrupted while getting result of future", e);
                        } catch (ExecutionException e) {
                            LOGGER.error("Error getting result of future", e);
                        }

                    }
                }
            }
            try {
                // Sleep
                sleepThread(fanoutCascadeMonitoringTask);
            } catch (InterruptedException e) {
                LOGGER.error("Error sleeping monitoring thread", e);
            }
        }
        return null;
    }

    private static void sleepThread(FanoutCascadeMonitoringTask fanoutCascadeMonitoringTask) throws InterruptedException {
        Thread.sleep(fanoutCascadeMonitoringTask.getTimeUnit().toMillis(fanoutCascadeMonitoringTask.getSleepTime()));
    }
}
