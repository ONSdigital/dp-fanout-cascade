package com.github.onsdigital.fanoutcascade.handlertasks;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class TestHandlerTask extends HandlerTask {
    public TestHandlerTask() {
        super(TestHandlerTask.class);
    }

    public TestConsoleTask getConsoleTask() {
        return new TestConsoleTask();
    }
}
