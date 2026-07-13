package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class SingleBox {
    private static final int DEFAULT_STEP_COUNT = 120;

    private SingleBox() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        float extent = 1.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        float groundWidth = 66.0f * extent;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Segment segment = new b2Segment(
            new b2Vec2(-0.5f * 2.0f * groundWidth, 0.0f),
            new b2Vec2(0.5f * 2.0f * groundWidth, 0.0f));
        b2CreateSegmentShape(groundId, shapeDef, segment);

        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.0f);
        bodyDef.linearVelocity = new b2Vec2(5.0f, 0.0f);

        b2Polygon box = b2MakeBox(extent, extent);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyId, shapeDef, box);

        for (int i = 0; i < stepCount; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Vec2 p = b2Body_GetPosition(bodyId);
        float angle = b2Rot_GetAngle(b2Body_GetRotation(bodyId));
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        float angularVelocity = b2Body_GetAngularVelocity(bodyId);
        b2ContactData[] contactData = new b2ContactData[4];
        int contactCount = b2Body_GetContactData(bodyId, contactData, contactData.length);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(p.x, p.y, angle, v.x, v.y, angularVelocity, contactCount, awakeBodyCount);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int contactCount;
        public final int awakeBodyCount;

        public Result(float x, float y, float angle, float velocityX, float velocityY,
                      float angularVelocity, int contactCount, int awakeBodyCount) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "singleBox %s %s %s %s %s %s %d %d",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), contactCount, awakeBodyCount);
        }

        private static String formatFloat(float value) {
            if (value == 0.0f) {
                return "0";
            }
            return String.format(Locale.ROOT, "%.9g", value);
        }
    }
}
