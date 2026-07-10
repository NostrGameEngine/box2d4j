package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Pivot {
    private Pivot() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2Segment segment = new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2Vec2 v = new b2Vec2(5.0f, 0.0f);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 3.0f);
        bodyDef.gravityScale = 1.0f;
        bodyDef.linearVelocity = v;

        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        float lever = 3.0f;
        b2Vec2 r = new b2Vec2(0.0f, -lever);
        float omega = b2Cross(v, r) / b2Dot(r, r);
        b2Body_SetAngularVelocity(bodyId, omega);

        b2Polygon box = b2MakeBox(0.1f, lever);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(bodyId, shapeDef, box);

        for (int step = 0; step < 120; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
        float angularVelocity = b2Body_GetAngularVelocity(bodyId);
        b2Vec2 worldR = b2Body_GetWorldVector(bodyId, new b2Vec2(0.0f, -lever));
        b2Vec2 pivotVelocity = b2Add(linearVelocity, b2CrossSV(angularVelocity, worldR));
        b2Vec2 localVelocity = b2Body_GetLocalPointVelocity(bodyId, new b2Vec2(0.0f, -lever));
        b2Vec2 worldPoint = b2Body_GetWorldPoint(bodyId, new b2Vec2(0.0f, -lever));
        b2Vec2 worldVelocity = b2Body_GetWorldPointVelocity(bodyId, worldPoint);
        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        Result result = new Result(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            linearVelocity.x, linearVelocity.y, angularVelocity, pivotVelocity.x, pivotVelocity.y,
            localVelocity.x, localVelocity.y, worldVelocity.x, worldVelocity.y,
            counters.contactCount, awakeBodyCount);
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
        public final float pivotVelocityX;
        public final float pivotVelocityY;
        public final float localVelocityX;
        public final float localVelocityY;
        public final float worldVelocityX;
        public final float worldVelocityY;
        public final int contactCount;
        public final int awakeBodyCount;

        public Result(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity,
                      float pivotVelocityX, float pivotVelocityY, float localVelocityX, float localVelocityY,
                      float worldVelocityX, float worldVelocityY, int contactCount, int awakeBodyCount) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.pivotVelocityX = pivotVelocityX;
            this.pivotVelocityY = pivotVelocityY;
            this.localVelocityX = localVelocityX;
            this.localVelocityY = localVelocityY;
            this.worldVelocityX = worldVelocityX;
            this.worldVelocityY = worldVelocityY;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "pivot %s %s %s %s %s %s %s %s %s %s %s %s %d %d",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), formatFloat(pivotVelocityX), formatFloat(pivotVelocityY),
                formatFloat(localVelocityX), formatFloat(localVelocityY), formatFloat(worldVelocityX),
                formatFloat(worldVelocityY), contactCount, awakeBodyCount);
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
