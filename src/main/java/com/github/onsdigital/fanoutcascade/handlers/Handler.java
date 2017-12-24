package com.github.onsdigital.fanoutcascade.handlers;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public interface Handler {

    Object handleTask(HandlerTask handlerTask) throws Exception;

}
