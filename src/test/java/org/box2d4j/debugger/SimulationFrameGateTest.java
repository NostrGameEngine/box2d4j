package org.box2d4j.debugger;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SimulationFrameGateTest {
    @Test
    void duplicateGrantCannotAdvanceTheFollowingFrame() throws Exception {
        SimulationFrameGate gate = new SimulationFrameGate();
        CountDownLatch firstPublished = new CountDownLatch(1);
        CountDownLatch publishSecond = new CountDownLatch(1);
        CountDownLatch secondPublished = new CountDownLatch(1);
        CountDownLatch secondCompleted = new CountDownLatch(1);

        Thread simulation = new Thread(() -> {
            try {
                gate.publishAndAwait(firstPublished::countDown);
                publishSecond.await();
                gate.publishAndAwait(secondPublished::countDown);
                secondCompleted.countDown();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        simulation.start();
        try {
            assertTrue(firstPublished.await(2, TimeUnit.SECONDS));
            assertTrue(gate.grant());
            assertFalse(gate.grant());

            publishSecond.countDown();
            assertTrue(secondPublished.await(2, TimeUnit.SECONDS));
            assertFalse(secondCompleted.await(100, TimeUnit.MILLISECONDS));

            assertTrue(gate.grant());
            assertTrue(secondCompleted.await(2, TimeUnit.SECONDS));
        } finally {
            gate.cancel();
            simulation.interrupt();
            simulation.join(2000L);
        }
        assertFalse(simulation.isAlive());
    }
}
