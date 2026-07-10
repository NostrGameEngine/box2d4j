package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class OverlapRecovery {
    private OverlapRecovery() {
    }

    public static RobustnessSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-40.0f, 0.0f), new b2Vec2(40.0f, 0.0f)));

        Parameters parameters = new Parameters();
        b2BodyId[][] bodies = {createScene(worldId, null, parameters)};
        SampleRuntime.slider("overlapRecovery.extent", "Extent", parameters.extent, 0.1f, 1.0f, 0.1f,
            value -> {
                parameters.extent = value;
                bodies[0] = createScene(worldId, bodies[0], parameters);
            });
        SampleRuntime.integer("overlapRecovery.baseCount", "Base Count", parameters.baseCount, 1, 10, 1,
            value -> {
                parameters.baseCount = value;
                bodies[0] = createScene(worldId, bodies[0], parameters);
            });
        SampleRuntime.slider("overlapRecovery.overlap", "Overlap", parameters.overlap, 0.0f, 1.0f, 0.01f,
            value -> {
                parameters.overlap = value;
                bodies[0] = createScene(worldId, bodies[0], parameters);
            });
        SampleRuntime.slider("overlapRecovery.speed", "Speed", parameters.pushOut, 0.0f, 10.0f, 0.1f,
            value -> {
                parameters.pushOut = value;
                bodies[0] = createScene(worldId, bodies[0], parameters);
            });
        SampleRuntime.slider("overlapRecovery.hertz", "Hertz", parameters.hertz, 0.0f, 240.0f, 1.0f,
            value -> {
                parameters.hertz = value;
                bodies[0] = createScene(worldId, bodies[0], parameters);
            });
        SampleRuntime.slider("overlapRecovery.damping", "Damping Ratio", parameters.dampingRatio,
            0.0f, 20.0f, 0.1f, value -> {
                parameters.dampingRatio = value;
                bodies[0] = createScene(worldId, bodies[0], parameters);
            });
        SampleRuntime.action("overlapRecovery.reset", "Reset Scene",
            () -> bodies[0] = createScene(worldId, bodies[0], parameters));

        return RobustnessSampleResult.simulate("overlapRecovery", worldId, bodies[0]);
    }

    private static b2BodyId[] createScene(b2WorldId worldId, b2BodyId[] previousBodies, Parameters parameters) {
        if (previousBodies != null) {
            for (b2BodyId bodyId : previousBodies) {
                if (b2Body_IsValid(bodyId)) {
                    b2DestroyBody(bodyId);
                }
            }
        }
        b2World_SetContactTuning(worldId, parameters.hertz, parameters.dampingRatio, parameters.pushOut);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2Polygon box = b2MakeBox(parameters.extent, parameters.extent);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        int baseCount = parameters.baseCount;
        b2BodyId[] bodies = new b2BodyId[baseCount * (baseCount + 1) / 2];
        int bodyIndex = 0;
        float fraction = 1.0f - parameters.overlap;
        float y = parameters.extent;
        for (int i = 0; i < baseCount; ++i) {
            float x = fraction * parameters.extent * (i - baseCount);
            for (int j = i; j < baseCount; ++j) {
                bodyDef.position = new b2Vec2(x, y);
                bodies[bodyIndex] = b2CreateBody(worldId, bodyDef);
                b2CreatePolygonShape(bodies[bodyIndex], shapeDef, box);
                bodyIndex += 1;
                x += 2.0f * fraction * parameters.extent;
            }
            y += 2.0f * fraction * parameters.extent;
        }
        return bodies;
    }

    private static final class Parameters {
        int baseCount = 4;
        float overlap = 0.25f;
        float extent = 0.5f;
        float pushOut = 3.0f;
        float hertz = 30.0f;
        float dampingRatio = 10.0f;
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
