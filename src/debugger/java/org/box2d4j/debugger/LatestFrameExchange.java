package org.box2d4j.debugger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

final class LatestFrameExchange {
    private static final int FRAME_COUNT = 3;

    private final ConcurrentLinkedQueue<DebugFrame> available = new ConcurrentLinkedQueue<>();
    private final AtomicReference<DebugFrame> latest = new AtomicReference<>();

    LatestFrameExchange() {
        for (int i = 0; i < FRAME_COUNT; ++i) {
            available.add(new DebugFrame());
        }
    }

    DebugFrame acquireWritable() {
        return available.poll();
    }

    void publish(DebugFrame frame) {
        DebugFrame displaced = latest.getAndSet(frame);
        if (displaced != null) {
            available.offer(displaced);
        }
    }

    DebugFrame pollLatest() {
        return latest.getAndSet(null);
    }

    void release(DebugFrame frame) {
        if (frame != null) {
            available.offer(frame);
        }
    }
}
