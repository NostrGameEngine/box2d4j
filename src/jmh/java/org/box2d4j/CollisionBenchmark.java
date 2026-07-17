package org.box2d4j;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

import static org.box2d4j.B2.*;

/** Covers every public primitive manifold entry point. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CollisionBenchmark {
    private final b2Transform transformA = new b2Transform(new b2Vec2(), b2Rot_identity);
    private final b2Transform transformB = new b2Transform(new b2Vec2(0.6f, 0.1f), b2MakeRot(0.1f));
    private final b2Circle circle = new b2Circle(new b2Vec2(), 0.5f);
    private final b2Capsule capsule = new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f);
    private final b2Segment segment = new b2Segment(new b2Vec2(-0.75f, 0.0f), new b2Vec2(0.75f, 0.0f));
    private final b2Polygon polygon = b2MakeRoundedBox(0.6f, 0.4f, 0.05f);
    private final b2ChainSegment chainSegment = makeChainSegment();
    private final b2SimplexCache chainCapsuleCache = new b2SimplexCache();
    private final b2SimplexCache chainPolygonCache = new b2SimplexCache();

    @Benchmark
    public b2Manifold circles() {
        return b2CollideCircles(circle, transformA, circle, transformB);
    }

    @Benchmark
    public b2Manifold capsuleAndCircle() {
        return b2CollideCapsuleAndCircle(capsule, transformA, circle, transformB);
    }

    @Benchmark
    public b2Manifold segmentAndCircle() {
        return b2CollideSegmentAndCircle(segment, transformA, circle, transformB);
    }

    @Benchmark
    public b2Manifold polygonAndCircle() {
        return b2CollidePolygonAndCircle(polygon, transformA, circle, transformB);
    }

    @Benchmark
    public b2Manifold capsules() {
        return b2CollideCapsules(capsule, transformA, capsule, transformB);
    }

    @Benchmark
    public b2Manifold segmentAndCapsule() {
        return b2CollideSegmentAndCapsule(segment, transformA, capsule, transformB);
    }

    @Benchmark
    public b2Manifold segmentAndPolygon() {
        return b2CollideSegmentAndPolygon(segment, transformA, polygon, transformB);
    }

    @Benchmark
    public b2Manifold polygonAndCapsule() {
        return b2CollidePolygonAndCapsule(polygon, transformA, capsule, transformB);
    }

    @Benchmark
    public b2Manifold chainSegmentAndCircle() {
        return b2CollideChainSegmentAndCircle(chainSegment, transformA, circle, transformB);
    }

    @Benchmark
    public b2Manifold chainSegmentAndCapsule() {
        return b2CollideChainSegmentAndCapsule(chainSegment, transformA, capsule, transformB, chainCapsuleCache);
    }

    @Benchmark
    public b2Manifold chainSegmentAndPolygon() {
        return b2CollideChainSegmentAndPolygon(chainSegment, transformA, polygon, transformB, chainPolygonCache);
    }

    @Benchmark
    public b2Manifold polygons() {
        return b2CollidePolygons(polygon, transformA, polygon, transformB);
    }

    private static b2ChainSegment makeChainSegment() {
        b2ChainSegment result = new b2ChainSegment();
        result.ghost1 = new b2Vec2(-1.5f, 0.0f);
        result.segment = new b2Segment(new b2Vec2(-0.75f, 0.0f), new b2Vec2(0.75f, 0.0f));
        result.ghost2 = new b2Vec2(1.5f, 0.0f);
        return result;
    }
}
