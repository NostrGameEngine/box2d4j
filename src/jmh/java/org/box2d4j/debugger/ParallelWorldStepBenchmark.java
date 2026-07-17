package org.box2d4j.debugger;

import org.box2d4j.b2WorldDef;
import org.box2d4j.b2WorldId;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.concurrent.TimeUnit;

import static org.box2d4j.B2.*;

/** Includes the viewer's ExecutorService adapter in the measured physics step. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ParallelWorldStepBenchmark {
    @Param({"1", "2", "4", "8"})
    public int workerCount;

    private DebuggerTaskScheduler scheduler;
    private b2WorldId worldId;

    @Setup(Level.Iteration)
    public void createWorld() {
        scheduler = new DebuggerTaskScheduler(workerCount);
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.setTaskScheduler(scheduler);
        worldId = DebuggerBenchmarkWorld.createLargePyramid(worldDef);
    }

    @TearDown(Level.Iteration)
    public void destroyWorld() {
        if (worldId != null) {
            b2DestroyWorld(worldId);
            worldId = null;
        }
        scheduler.close();
        scheduler = null;
    }

    @Benchmark
    public void step() {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }
}
