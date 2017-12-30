package com.github.onsdigital.fanoutcascade.handlertasks;

import java.util.concurrent.TimeUnit;

/**
 * This class just triggers the execution of the FanoutCascadeMonitoringHandler thread
 * @author sullid (David Sullivan) on 22/12/2017
 * @project dp-fanout-cascade
 */
public class FanoutCascadeMonitoringTask extends HandlerTask {

    private TimeUnit timeUnit;
    private int sleepTime;

    public FanoutCascadeMonitoringTask() {
        this(TimeUnit.SECONDS, 1);
    }

    public FanoutCascadeMonitoringTask(TimeUnit timeUnit, int sleepTime) {
        super(FanoutCascadeMonitoringTask.class);
        this.timeUnit = timeUnit;
        this.sleepTime = sleepTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void sleepThread() throws InterruptedException {
        Thread.sleep(this.timeUnit.toMillis(this.sleepTime));
    }
}
