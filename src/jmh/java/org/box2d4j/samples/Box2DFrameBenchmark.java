package org.box2d4j.samples;

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
import java.util.function.IntConsumer;

import static org.box2d4j.B2.b2DestroyWorld;
import static org.box2d4j.B2.b2World_Step;

/** Measures one complete headless sample frame across the upstream benchmark catalog. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Box2DFrameBenchmark {
    @Param({
        "empty", "barrel", "tumbler", "large-pyramid", "many-pyramids", "joint-grid", "smash",
        "spinner", "many-tumblers", "create-destroy", "sleep", "compound", "kinematic", "cast",
        "rain", "shape-distance", "sensor", "large-world"
    })
    public String sceneName;

    private BenchmarkScenes.Scene scene;
    private IntConsumer beforeStep;
    private IntConsumer afterStep;
    private boolean stepWorld;
    private int stepCount;

    @Setup(Level.Iteration)
    public void createScene() {
        stepWorld = true;
        stepCount = 0;
        switch (sceneName) {
            case "empty":
                scene = new BenchmarkScenes.Scene();
                break;
            case "barrel":
                scene = BenchmarkScenes.createBarrel();
                break;
            case "tumbler":
                scene = BenchmarkScenes.createTumbler();
                break;
            case "large-pyramid":
                scene = BenchmarkScenes.createLargePyramid();
                break;
            case "many-pyramids":
                scene = BenchmarkScenes.createManyPyramids();
                break;
            case "joint-grid":
                scene = BenchmarkScenes.createJointGrid();
                break;
            case "smash":
                scene = BenchmarkScenes.createSmash();
                break;
            case "spinner":
                scene = BenchmarkScenes.createSpinner();
                break;
            case "many-tumblers": {
                BenchmarkScenes.ManyTumblersScene sample = BenchmarkScenes.createManyTumblers();
                scene = sample.scene;
                afterStep = sample::afterStep;
                break;
            }
            case "create-destroy": {
                BenchmarkScenes.CreateDestroyScene sample = BenchmarkScenes.createCreateDestroy();
                scene = sample.scene;
                beforeStep = sample::beforeStep;
                stepWorld = false;
                break;
            }
            case "sleep": {
                BenchmarkScenes.SleepScene sample = BenchmarkScenes.createSleep();
                scene = sample.scene;
                beforeStep = sample::beforeStep;
                break;
            }
            case "compound":
                scene = BenchmarkScenes.createCompound();
                break;
            case "kinematic":
                scene = BenchmarkScenes.createKinematic();
                break;
            case "cast": {
                BenchmarkScenes.CastScene sample = BenchmarkScenes.createCast();
                scene = sample.scene;
                afterStep = sample::afterStep;
                break;
            }
            case "rain": {
                BenchmarkScenes.RainScene sample = BenchmarkScenes.createRain();
                scene = sample.scene;
                beforeStep = sample::beforeStep;
                break;
            }
            case "shape-distance": {
                BenchmarkScenes.ShapeDistanceScene sample = BenchmarkScenes.createShapeDistance();
                scene = sample.scene;
                beforeStep = sample::beforeStep;
                break;
            }
            case "sensor": {
                BenchmarkScenes.SensorScene sample = BenchmarkScenes.createSensor();
                scene = sample.scene;
                afterStep = sample::afterStep;
                break;
            }
            case "large-world": {
                BenchmarkScenes.LargeWorldScene sample = BenchmarkScenes.createLargeWorld();
                scene = sample.scene;
                beforeStep = sample::beforeStep;
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown benchmark scene: " + sceneName);
        }
    }

    @TearDown(Level.Iteration)
    public void destroyScene() {
        if (scene != null) {
            b2DestroyWorld(scene.worldId);
        }
        scene = null;
        beforeStep = null;
        afterStep = null;
    }

    @Benchmark
    public void frame() {
        int currentStep = stepCount++;
        if (beforeStep != null) {
            beforeStep.accept(currentStep);
        }
        if (stepWorld) {
            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
        }
        if (afterStep != null) {
            afterStep.accept(currentStep);
        }
    }
}
