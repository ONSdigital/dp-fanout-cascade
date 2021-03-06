package com.github.onsdigital.fanoutcascade;

import com.github.onsdigital.fanoutcascade.handlers.TestConsoleHandler;
import com.github.onsdigital.fanoutcascade.handlers.TestHandler;
import com.github.onsdigital.fanoutcascade.handlertasks.FanoutCascadeMonitoringTask;
import com.github.onsdigital.fanoutcascade.handlertasks.HandlerTask;
import com.github.onsdigital.fanoutcascade.handlertasks.TestConsoleTask;
import com.github.onsdigital.fanoutcascade.handlertasks.TestHandlerTask;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascade;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeLayer;
import com.github.onsdigital.fanoutcascade.pool.FanoutCascadeRegistry;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
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
        cascadeRegistry.registerMonitoringThread();
        cascadeRegistry.register(TestHandlerTask.class, TestHandler.class, 8);
        cascadeRegistry.register(TestConsoleTask.class, TestConsoleHandler.class, 8);

        // Register shutdown hook
        FanoutCascade.getInstance().registerShutdownThread();

        // Submit the monitoring thread
        FanoutCascade.getInstance().getLayerForTask(FanoutCascadeMonitoringTask.class).submit(new FanoutCascadeMonitoringTask());

        assertTrue(cascadeRegistry.handlerRegisteredForTask(TestHandlerTask.class));

        TestHandlerTask task = new TestHandlerTask();
        assertTrue(FanoutCascade.getInstance().hasLayer(TestHandlerTask.class));

        // Submit this task to the cascade 10 times
        int count = 10;
        while (count > 0) {
            FanoutCascade.getInstance().getLayerForTask(TestHandlerTask.class).submit(task);
            count--;
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FanoutCascade.getInstance().close();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        while (!FanoutCascade.getInstance().isShutdown()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Assert.fail(e.getMessage());
            }
        }

        // Pop the result
        FanoutCascadeLayer layer = FanoutCascade.getInstance().getLayerForTask(TestHandlerTask.class);
        Future<Object> future = layer.getFuture(task);

        while (!future.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Assert.fail(e.getMessage());
            }
        }

        try {
            Object obj = future.get();

            // Assert it produced a TestConsoleTask
            assertTrue(obj instanceof TestConsoleTask);
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }

        // Assert all tasks were purged
        assertEquals(new HashSet<HandlerTask>(), FanoutCascade.getInstance().getLayerForTask(TestConsoleTask.class).getKeySet());

        // Purge shutdown layers
        FanoutCascade.getInstance().purgeShutdownLayers();

        // Assert no layers
        assertEquals(new HashSet<Class<? extends HandlerTask>>(), FanoutCascade.getInstance().getRegisteredTasks());
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
