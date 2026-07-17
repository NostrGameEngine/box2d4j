package org.box2d4j.debugger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

final class LatestFrameExchangeTest {
    @Test
    void producerReplacesUnconsumedFramesWithoutBlocking() {
        LatestFrameExchange exchange = new LatestFrameExchange();
        for (int version = 1; version <= 100; ++version) {
            DebugFrame frame = exchange.acquireWritable();
            assertNotNull(frame);
            frame.version = version;
            exchange.publish(frame);
        }

        DebugFrame latest = exchange.pollLatest();
        assertNotNull(latest);
        assertEquals(100L, latest.version);
        assertNull(exchange.pollLatest());
        exchange.release(latest);
    }
}
