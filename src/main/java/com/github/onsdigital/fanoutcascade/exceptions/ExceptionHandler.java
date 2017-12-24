package com.github.onsdigital.fanoutcascade.exceptions;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeLayer;

/**
 * @author sullid (David Sullivan) on 24/12/2017
 * @project dp-fanout-cascade
 */
public interface ExceptionHandler {

    void handleLayerException(HandlerTask task, FanoutCascadeLayer layer, Exception e);

}
