package com.github.onsdigital.fanoutcascade.handlertasks;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class TestHandlerTask extends HandlerTask {
    public TestHandlerTask() {
        super(TestHandlerTask.class);
    }

    public String getMessage() {
        long threadId = Thread.currentThread().getId();
        String message = "Hello, world from thread " + threadId;
        return message;
    }
}
