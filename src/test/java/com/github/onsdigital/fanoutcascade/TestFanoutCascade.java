package com.github.onsdigital.fanoutcascade;

import com.github.onsdigital.fanoutcascade.handlers.TestConsoleHandler;
import com.github.onsdigital.fanoutcascade.handlers.TestHandler;
import com.github.onsdigital.fanoutcascade.handlertasks.TestConsoleTask;
import com.github.onsdigital.fanoutcascade.handlertasks.TestHandlerTask;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascade;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author sullid (David Sullivan) on 21/12/2017
 * @project dp-fanout-cascade
 */
public class TestFanoutCascade {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHandlerTask.class);

    @Test
    public void testRegister() {
        FanoutCascadeRegistry cascadeRegistry = FanoutCascadeRegistry.getInstance();
        cascadeRegistry.register(TestHandlerTask.class, TestHandler.class, 8);
        cascadeRegistry.register(TestConsoleTask.class, TestConsoleHandler.class, 8);

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

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testFailWithoutRegister() {
        // Tests that the cascade fails without registration of task with the correct message

        TestHandlerTask task = new TestHandlerTask();

        assertFalse(FanoutCascadeRegistry.getInstance().handlerRegisteredForTask(TestHandlerTask.class));

        expectedEx.expect(RuntimeException.class);
        String expectedMessage = String.format("No layer registered for class " + TestHandlerTask.class.getName());
        expectedEx.expectMessage(expectedMessage);

        // Should fail
        LOGGER.info("Note: RuntimeException is expected.");
        FanoutCascade.getInstance().getLayerForTask(TestHandlerTask.class).submit(task);
    }

}
