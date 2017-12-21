package com.github.onsdigital.fanoutcascade;

import com.github.onsdigital.fanoutcascade.handlers.TestHandler;
import com.github.onsdigital.fanoutcascade.handlertasks.TestHandlerTask;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascade;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeRegistry;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class TestFanoutCascade {

    @Test
    public void test() {
        FanoutCascadeRegistry cascadeRegistry = FanoutCascadeRegistry.getInstance();
        cascadeRegistry.register(TestHandlerTask.class, TestHandler.class, 8);

        assertTrue(cascadeRegistry.handlerRegisteredForTask(TestHandlerTask.class));

        TestHandlerTask task = new TestHandlerTask();
        assertTrue(FanoutCascade.getInstance().hasLayer(TestHandlerTask.class));

        // Submit this task to the cascade 10 times
        int count = 10;
        while (count > 0) {
            FanoutCascade.getInstance().getLayerForTask(TestHandlerTask.class).submit(task);
            count--;
        }
    }

}
