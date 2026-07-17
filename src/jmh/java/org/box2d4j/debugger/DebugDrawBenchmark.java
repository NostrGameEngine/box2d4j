package org.box2d4j.debugger;

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

/** Measures simulation-thread debug capture without a GPU or render-thread dependency. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class DebugDrawBenchmark {
    @Param({"shapes", "all"})
    public String detail;

    private b2WorldId worldId;
    private WorldDrawBatch batch;
    private WorldDrawBatch.DrawOptions options;

    @Setup(Level.Iteration)
    public void createWorld() {
        worldId = DebuggerBenchmarkWorld.createLargePyramid(b2DefaultWorldDef());
        for (int i = 0; i < 120; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }
        batch = new WorldDrawBatch();
        options = new WorldDrawBatch.DrawOptions();
        if ("all".equals(detail)) {
            options.jointExtras = true;
            options.islands = true;
            options.bounds = true;
            options.contacts = true;
            options.graphColors = true;
            options.contactNormals = true;
            options.contactImpulses = true;
            options.frictionImpulses = true;
        }
    }

    @TearDown(Level.Iteration)
    public void destroyWorld() {
        b2DestroyWorld(worldId);
    }

    @Benchmark
    public void capture() {
        batch.captureInto(worldId, options, 0.02f);
    }
}
