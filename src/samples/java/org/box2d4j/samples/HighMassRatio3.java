package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class HighMassRatio3 {
    private HighMassRatio3() {
    }

    public static RobustnessSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(),
            b2MakeOffsetBox(50.0f, 1.0f, new b2Vec2(0.0f, -1.0f), b2Rot_identity));

        float extent = 1.0f;
        b2Vec2[] points = {
            new b2Vec2(-0.5f * extent, 0.0f),
            new b2Vec2(0.5f * extent, 0.0f),
            new b2Vec2(0.0f, extent)
        };
        b2Hull hull = b2ComputeHull(points, points.length);
        b2Polygon triangle = b2MakePolygon(hull, 0.0f);
        b2Polygon bigBox = b2MakeBox(10.0f * extent, 10.0f * extent);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId[] bodies = new b2BodyId[3];

        bodyDef.position = new b2Vec2(-9.0f * extent, 0.5f * extent);
        bodies[0] = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodies[0], shapeDef, triangle);
        bodyDef.position = new b2Vec2(9.0f * extent, 0.5f * extent);
        bodies[1] = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodies[1], shapeDef, triangle);
        bodyDef.position = new b2Vec2(0.0f, (10.0f + 4.0f) * extent);
        bodies[2] = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodies[2], shapeDef, bigBox);

        return RobustnessSampleResult.simulate("highMassRatio3", worldId, bodies);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
