package com.github.onsdigital.fanoutcascade.exceptions;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sullid (David Sullivan) on 24/12/2017
 * @project dp-fanout-cascade
 */
public class PurgingExceptionHandler implements ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgingExceptionHandler.class);

    // Logs the exception and purges the task from the layer
    @Override
    public void handleLayerException(HandlerTask task, FanoutCascadeLayer layer, Exception e) {
        LOGGER.error(String.format("Exception caught while handling task %s in layer %s, purging.", task, layer), e);
        layer.popFuture(task);
    }
}
