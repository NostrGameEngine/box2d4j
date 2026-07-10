package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;

public final class HighMassRatio1 {
    private HighMassRatio1() {
    }

    public static RobustnessSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(),
            b2MakeOffsetBox(50.0f, 1.0f, new b2Vec2(0.0f, -1.0f), b2Rot_identity));

        float extent = 1.0f;
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2Polygon box = b2MakeBox(extent, extent);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        List<b2BodyId> bodies = new ArrayList<>();
        for (int pyramidIndex = 0; pyramidIndex < 3; ++pyramidIndex) {
            int count = 10;
            float offset = -20.0f * extent + 2.0f * (count + 1.0f) * extent * pyramidIndex;
            float y = extent;
            while (count > 0) {
                for (int i = 0; i < count; ++i) {
                    float coefficient = i - 0.5f * count;
                    float bodyY = count == 1 ? y + 2.0f : y;
                    bodyDef.position = new b2Vec2(2.0f * coefficient * extent + offset, bodyY);
                    b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                    shapeDef.density = count == 1 ? (pyramidIndex + 1.0f) * 100.0f : 1.0f;
                    b2CreatePolygonShape(bodyId, shapeDef, box);
                    bodies.add(bodyId);
                }
                count -= 1;
                y += 2.0f * extent;
            }
        }
        return RobustnessSampleResult.simulate(
            "highMassRatio1", worldId, bodies.toArray(new b2BodyId[0]));
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
