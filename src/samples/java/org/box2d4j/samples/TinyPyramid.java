package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class TinyPyramid {
    private TinyPyramid() {
    }

    public static RobustnessSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(),
            b2MakeOffsetBox(5.0f, 1.0f, new b2Vec2(0.0f, -1.0f), b2Rot_identity));

        float extent = 0.025f;
        int baseCount = 30;
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeSquare(extent);
        b2BodyId[] bodies = new b2BodyId[baseCount * (baseCount + 1) / 2];
        int bodyIndex = 0;
        for (int i = 0; i < baseCount; ++i) {
            float y = (2.0f * i + 1.0f) * extent;
            for (int j = i; j < baseCount; ++j) {
                float x = (i + 1.0f) * extent + 2.0f * (j - i) * extent - baseCount * extent;
                bodyDef.position = new b2Vec2(x, y);
                bodies[bodyIndex] = b2CreateBody(worldId, bodyDef);
                b2CreatePolygonShape(bodies[bodyIndex], shapeDef, box);
                bodyIndex += 1;
            }
        }

        return RobustnessSampleResult.simulate("tinyPyramid", worldId, bodies);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
