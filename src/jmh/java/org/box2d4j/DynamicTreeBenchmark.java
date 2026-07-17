package org.box2d4j;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.concurrent.TimeUnit;

import static org.box2d4j.B2.*;

/** Exercises broad-phase queries and the grow-only rebuild workspace. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class DynamicTreeBenchmark {
    private static final int SIDE = 64;

    private final b2AABB queryBounds = new b2AABB(new b2Vec2(20.0f, 20.0f), new b2Vec2(36.0f, 36.0f));
    private final b2RayCastInput rayInput = new b2RayCastInput(
        new b2Vec2(-1.0f, 31.5f), new b2Vec2(66.0f, 0.0f), 1.0f);
    private final b2ShapeCastInput shapeCastInput = new b2ShapeCastInput();
    private final b2AABB moveBounds = new b2AABB();
    private b2DynamicTree tree;
    private int[] proxies;
    private int moveIndex;

    @Setup(Level.Trial)
    public void createTree() {
        tree = b2DynamicTree_Create();
        proxies = new int[SIDE * SIDE];
        int index = 0;
        for (int y = 0; y < SIDE; ++y) {
            for (int x = 0; x < SIDE; ++x) {
                b2AABB bounds = new b2AABB(new b2Vec2(x, y), new b2Vec2(x + 0.8f, y + 0.8f));
                proxies[index] = b2DynamicTree_CreateProxy(tree, bounds, ~0L, index);
                index += 1;
            }
        }
        shapeCastInput.proxy = b2MakeProxy(new b2Vec2[] {new b2Vec2(-1.0f, 30.5f)}, 1, 0.5f);
        shapeCastInput.translation = new b2Vec2(66.0f, 0.0f);
        shapeCastInput.maxFraction = 1.0f;
    }

    @TearDown(Level.Trial)
    public void destroyTree() {
        b2DynamicTree_Destroy(tree);
    }

    @Benchmark
    public b2TreeStats query() {
        return b2DynamicTree_Query(tree, queryBounds, ~0L, (proxyId, userData) -> true);
    }

    @Benchmark
    public b2TreeStats rayCast() {
        return b2DynamicTree_RayCast(tree, rayInput, ~0L,
            (input, proxyId, userData) -> input.maxFraction);
    }

    @Benchmark
    public b2TreeStats shapeCast() {
        return b2DynamicTree_ShapeCast(tree, shapeCastInput, ~0L,
            (input, proxyId, userData) -> input.maxFraction);
    }

    @Benchmark
    public int moveAndRebuild() {
        int index = moveIndex++ & (proxies.length - 1);
        float x = index % SIDE;
        float y = index / SIDE;
        moveBounds.lowerBound.x = x + 0.05f;
        moveBounds.lowerBound.y = y + 0.05f;
        moveBounds.upperBound.x = x + 0.85f;
        moveBounds.upperBound.y = y + 0.85f;
        b2DynamicTree_MoveProxy(tree, proxies[index], moveBounds);
        return b2DynamicTree_Rebuild(tree, false);
    }
}
