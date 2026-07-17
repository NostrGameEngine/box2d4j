package org.box2d4j.debugger;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldDef;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

final class DebuggerBenchmarkWorld {
    private DebuggerBenchmarkWorld() {
    }

    static b2WorldId createLargePyramid(b2WorldDef worldDef) {
        b2WorldId worldId = b2CreateWorld(worldDef);
        b2World_EnableSleeping(worldId, false);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(100.0f, 1.0f));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        float halfExtent = 0.5f;
        b2Polygon box = b2MakeSquare(halfExtent);
        int baseCount = 20;
        for (int row = 0; row < baseCount; ++row) {
            float y = (2.0f * row + 1.0f) * halfExtent;
            for (int column = row; column < baseCount; ++column) {
                float x = (row + 1.0f) * halfExtent
                    + 2.0f * (column - row) * halfExtent - halfExtent * baseCount;
                bodyDef.position = new b2Vec2(x, y);
                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, box);
            }
        }
        return worldId;
    }
}
