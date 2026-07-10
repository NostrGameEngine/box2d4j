package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class RecreateStatic {
    private RecreateStatic() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeBox(1.0f, 1.0f);
        b2CreatePolygonShape(bodyId, shapeDef, box);

        b2BodyId groundId = new b2BodyId();
        for (int step = 0; step < 120; ++step) {
            if (b2Body_IsValid(groundId)) {
                b2DestroyBody(groundId);
                groundId = new b2BodyId();
            }

            b2BodyDef groundDef = b2DefaultBodyDef();
            groundId = b2CreateBody(worldId, groundDef);

            b2ShapeDef groundShapeDef = b2DefaultShapeDef();
            groundShapeDef.invokeContactCreation = true;
            b2Segment segment = new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(10.0f, 0.0f));
            b2CreateSegmentShape(groundId, groundShapeDef, segment);

            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        b2ContactData[] contactData = new b2ContactData[4];
        int bodyContactCount = b2Body_GetContactData(bodyId, contactData, contactData.length);
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)), v.x, v.y,
            b2Body_GetAngularVelocity(bodyId), counters.bodyCount, counters.shapeCount, counters.contactCount,
            bodyContactCount, b2World_GetAwakeBodyCount(worldId));
        b2DestroyWorld(worldId);
        return result;
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int bodyContactCount;
        public final int awakeBodyCount;

        public Result(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity,
                      int bodyCount, int shapeCount, int contactCount, int bodyContactCount, int awakeBodyCount) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.bodyContactCount = bodyContactCount;
            this.awakeBodyCount = awakeBodyCount;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "recreateStatic %s %s %s %s %s %s %d %d %d %d %d",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), bodyCount, shapeCount, contactCount, bodyContactCount, awakeBodyCount);
        }
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.9g", value);
        if (text.indexOf('e') < 0 && text.indexOf('E') < 0 && text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text;
    }
}
