package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ShapeDef;

import static org.box2d4j.B2.*;

public final class BenchmarkShapeDistance {
    private BenchmarkShapeDistance() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.ShapeDistanceScene sample = BenchmarkScenes.createShapeDistance();
        if (SampleRuntime.isActive()) {
            int[] drawIndex = {0};
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_kinematicBody;
            b2BodyId bodyA = sample.scene.createBody(bodyDef);
            b2BodyId bodyB = sample.scene.createBody(bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.filter.maskBits = 0L;
            b2CreatePolygonShape(bodyA, shapeDef, sample.polygonA);
            b2CreatePolygonShape(bodyB, shapeDef, sample.polygonB);
            Runnable updateDisplay = () -> {
                int index = drawIndex[0];
                b2Body_SetTransform(bodyA, sample.transformsA[index].p, sample.transformsA[index].q);
                b2Body_SetTransform(bodyB, sample.transformsB[index].p, sample.transformsB[index].q);
            };
            SampleRuntime.integer("benchmarkShapeDistance.drawIndex", "Draw Index", 0, 0,
                sample.transformsA.length - 1, 1, value -> {
                    drawIndex[0] = value;
                    updateDisplay.run();
                });
            updateDisplay.run();
        }
        return BenchmarkSampleResult.simulate("benchmarkShapeDistance", sample.scene, 1, sample::beforeStep, null,
            sample.hash::value);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
