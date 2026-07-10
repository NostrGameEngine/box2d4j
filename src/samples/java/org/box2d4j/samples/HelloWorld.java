package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class HelloWorld {
    private HelloWorld() {
    }

    public static Result run() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundBodyDef = b2DefaultBodyDef();
        groundBodyDef.position = new b2Vec2(0.0f, -10.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundBodyDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(50.0f, 10.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 4.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.3f;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

        for (int i = 0; i < 90; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Vec2 p = b2Body_GetPosition(bodyId);
        float angle = b2Rot_GetAngle(b2Body_GetRotation(bodyId));
        b2DestroyWorld(worldId);
        return new Result(p.x, p.y, angle);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final float x;
        public final float y;
        public final float angle;

        public Result(float x, float y, float angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "hello %.9g %.9g %.9g", x, y, angle);
        }
    }
}
