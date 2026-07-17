package org.box2d4j.debugger;

final class SimulationClock {
    static final int CANCELLED = 0;
    static final int STEP = 1;
    static final int REFRESH = 2;

    private boolean playing = true;
    private boolean cancelled;
    private boolean refreshRequested;
    private int pendingSteps;
    private float targetHz = 60.0f;
    private long nextStepNanos;

    synchronized int awaitAction() throws InterruptedException {
        while (!cancelled) {
            if (refreshRequested) {
                refreshRequested = false;
                return REFRESH;
            }
            if (pendingSteps > 0) {
                pendingSteps -= 1;
                nextStepNanos = System.nanoTime() + periodNanos();
                return STEP;
            }
            if (!playing) {
                wait();
                continue;
            }

            long now = System.nanoTime();
            if (nextStepNanos == 0L || now >= nextStepNanos) {
                long period = periodNanos();
                nextStepNanos = nextStepNanos == 0L ? now + period : nextStepNanos + period;
                if (nextStepNanos < now - 4L * period) {
                    nextStepNanos = now + period;
                }
                return STEP;
            }
            long delay = nextStepNanos - now;
            wait(delay / 1_000_000L, (int) (delay % 1_000_000L));
        }
        return CANCELLED;
    }

    synchronized boolean isPlaying() {
        return playing;
    }

    synchronized void setPlaying(boolean playing) {
        this.playing = playing;
        pendingSteps = 0;
        nextStepNanos = playing ? System.nanoTime() : 0L;
        notifyAll();
    }

    synchronized void togglePlaying() {
        setPlaying(!playing);
    }

    synchronized void stepOnce() {
        playing = false;
        pendingSteps += 1;
        nextStepNanos = 0L;
        notifyAll();
    }

    synchronized float targetHz() {
        return targetHz;
    }

    synchronized void setTargetHz(float targetHz) {
        this.targetHz = Math.max(1.0f, Math.min(240.0f, targetHz));
        nextStepNanos = playing ? System.nanoTime() : 0L;
        notifyAll();
    }

    synchronized void requestRefresh() {
        refreshRequested = true;
        notifyAll();
    }

    synchronized void cancel() {
        cancelled = true;
        notifyAll();
    }

    private long periodNanos() {
        return (long) (1_000_000_000.0 / targetHz);
    }
}
