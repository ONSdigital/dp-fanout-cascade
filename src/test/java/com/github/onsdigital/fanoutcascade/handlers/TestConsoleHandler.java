package com.github.onsdigital.fanoutcascade.handlers;

import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import com.github.onsdigital.fanoutcascade.handlertasks.TestConsoleTask;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class TestConsoleHandler implements Handler {
    @Override
    public Object handleTask(HandlerTask handlerTask) {
        // The FanoutCascade will ensure we always get the correct HandlerTask, so this cast should always be safe
        TestConsoleTask testHandlerTask = (TestConsoleTask) handlerTask;
        String message = testHandlerTask.getMessage();
        System.out.println(message);
        return null;
    }
}
