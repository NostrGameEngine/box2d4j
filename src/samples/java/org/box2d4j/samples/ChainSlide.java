package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class ChainSlide {
    private ChainSlide() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());

        b2Vec2[] points = new b2Vec2[80];
        float width = 2.0f;
        float height = 1.0f;
        float x = 20.0f;
        float y = 0.0f;
        for (int i = 0; i < 20; ++i) {
            points[i] = new b2Vec2(x, y);
            x -= width;
        }
        for (int i = 20; i < 40; ++i) {
            points[i] = new b2Vec2(x, y);
            y += height;
        }
        for (int i = 40; i < 60; ++i) {
            points[i] = new b2Vec2(x, y);
            x += width;
        }
        for (int i = 60; i < 80; ++i) {
            points[i] = new b2Vec2(x, y);
            y -= height;
        }
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = points.length;
        chainDef.isLoop = true;
        b2CreateChain(groundId, chainDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.linearVelocity = new b2Vec2(100.0f, 0.0f);
        bodyDef.position = new b2Vec2(-19.5f, 0.5f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.0f;
        b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(), 0.5f));

        return ContinuousSampleResult.simulate("chainSlide", worldId, bodyId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
