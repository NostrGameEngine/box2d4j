package org.box2d4j.debugger;

import java.util.concurrent.Semaphore;

final class SimulationFrameGate {
    private final Semaphore permit = new Semaphore(0);

    private boolean awaiting;
    private boolean granted;
    private boolean cancelled;

    void publishAndAwait(Runnable publish) throws InterruptedException {
        synchronized (this) {
            if (cancelled) {
                return;
            }
            awaiting = true;
            granted = false;
            publish.run();
        }

        try {
            permit.acquire();
        } finally {
            synchronized (this) {
                awaiting = false;
                granted = false;
            }
        }
    }

    synchronized boolean isAwaiting() {
        return awaiting;
    }

    synchronized boolean grant() {
        if (!awaiting || granted || cancelled) {
            return false;
        }
        granted = true;
        permit.release();
        return true;
    }

    synchronized void cancel() {
        cancelled = true;
        if (awaiting && !granted) {
            granted = true;
            permit.release();
        }
    }
}
