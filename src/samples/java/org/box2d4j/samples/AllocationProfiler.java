package org.box2d4j.samples;

import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.function.Supplier;

import static org.box2d4j.B2.b2DestroyWorld;
import static org.box2d4j.B2.b2World_Step;

/** Reports steady-state allocation and step time without requiring an external profiler. */
public final class AllocationProfiler {
    private AllocationProfiler() {
    }

    public static void main(String[] args) {
        int warmupSteps = args.length > 0 ? Integer.parseInt(args[0]) : 600;
        int measuredSteps = args.length > 1 ? Integer.parseInt(args[1]) : 1200;
        ThreadMXBean threadBean = threadBean();
        profile(threadBean, "empty", BenchmarkScenes.Scene::new, warmupSteps, measuredSteps);
        profile(threadBean, "tumbler", BenchmarkScenes::createTumbler, warmupSteps, measuredSteps);
        profile(threadBean, "large-pyramid", BenchmarkScenes::createLargePyramid, warmupSteps, measuredSteps);
    }

    private static void profile(ThreadMXBean threadBean, String name,
                                Supplier<BenchmarkScenes.Scene> sceneFactory,
                                int warmupSteps, int measuredSteps) {
        BenchmarkScenes.Scene scene = sceneFactory.get();
        try {
            for (int i = 0; i < warmupSteps; ++i) {
                b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            }
            long threadId = Thread.currentThread().getId();
            long allocatedBefore = threadBean.getThreadAllocatedBytes(threadId);
            long timeBefore = System.nanoTime();
            for (int i = 0; i < measuredSteps; ++i) {
                b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            }
            long elapsedNanos = System.nanoTime() - timeBefore;
            long allocatedBytes = threadBean.getThreadAllocatedBytes(threadId) - allocatedBefore;
            System.out.printf(Locale.ROOT,
                "%s: %,d bytes, %,.1f bytes/step, %.3f ms/step (%d warmup, %d measured)%n",
                name, allocatedBytes, (double) allocatedBytes / measuredSteps,
                elapsedNanos / 1_000_000.0 / measuredSteps, warmupSteps, measuredSteps);
        } finally {
            b2DestroyWorld(scene.worldId);
        }
    }

    private static ThreadMXBean threadBean() {
        java.lang.management.ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!(bean instanceof ThreadMXBean)) {
            throw new UnsupportedOperationException("Thread allocation profiling is not supported by this JVM");
        }
        ThreadMXBean threadBean = (ThreadMXBean) bean;
        if (!threadBean.isThreadAllocatedMemorySupported()) {
            throw new UnsupportedOperationException("Thread allocation profiling is not supported by this JVM");
        }
        if (!threadBean.isThreadAllocatedMemoryEnabled()) {
            threadBean.setThreadAllocatedMemoryEnabled(true);
        }
        return threadBean;
    }
}
