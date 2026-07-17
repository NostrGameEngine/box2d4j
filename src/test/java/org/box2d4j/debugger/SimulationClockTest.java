package org.box2d4j.debugger;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SimulationClockTest {
    @Test
    void pauseRefreshAndSingleStepWakeTheSimulationThread() throws Exception {
        SimulationClock clock = new SimulationClock();
        clock.setPlaying(false);
        CountDownLatch completed = new CountDownLatch(3);
        AtomicInteger actions = new AtomicInteger();
        Thread simulation = new Thread(() -> {
            try {
                for (int i = 0; i < 3; ++i) {
                    actions.set(10 * actions.get() + clock.awaitAction());
                    completed.countDown();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        simulation.start();
        try {
            assertFalse(completed.await(100, TimeUnit.MILLISECONDS));
            clock.requestRefresh();
            assertTrue(awaitCount(completed, 2));
            clock.stepOnce();
            assertTrue(awaitCount(completed, 1));
            clock.stepOnce();
            assertTrue(completed.await(2, TimeUnit.SECONDS));
            assertEquals(211, actions.get());
        } finally {
            clock.cancel();
            simulation.interrupt();
            simulation.join(2000L);
        }
        assertFalse(simulation.isAlive());
    }

    private static boolean awaitCount(CountDownLatch latch, long expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (latch.getCount() != expected && System.nanoTime() < deadline) {
            Thread.sleep(5L);
        }
        return latch.getCount() == expected;
    }
}
