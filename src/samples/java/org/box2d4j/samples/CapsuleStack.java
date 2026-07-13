package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class CapsuleStack {
    private static final int DEFAULT_STEP_COUNT = 180;

    private CapsuleStack() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(0.0f, -1.0f);
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Polygon polygon = b2MakeBox(10.0f, 1.0f);
            b2CreatePolygonShape(groundId, shapeDef, polygon);
        }

        b2BodyId[] bodies = new b2BodyId[20];
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;

        float a = 0.25f;
        b2Capsule capsule = new b2Capsule(new b2Vec2(-4.0f * a, 0.0f), new b2Vec2(4.0f * a, 0.0f), a);

        b2ShapeDef shapeDef = b2DefaultShapeDef();

        float y = 2.0f * a;
        for (int i = 0; i < 20; ++i) {
            bodyDef.position.y = y;
            bodies[i] = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(bodies[i], shapeDef, capsule);
            y += 3.0f * a;
        }

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        BodyState bottom = bodyState(bodies[0]);
        BodyState middle = bodyState(bodies[10]);
        BodyState top = bodyState(bodies[19]);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(counters.contactCount, awakeBodyCount, bottom, middle, top);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)), v.x, v.y);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState bottom;
        public final BodyState middle;
        public final BodyState top;

        public Result(int contactCount, int awakeBodyCount, BodyState bottom, BodyState middle, BodyState top) {
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.bottom = bottom;
            this.middle = middle;
            this.top = top;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "capsuleStack %d %d%s%s%s",
                contactCount, awakeBodyCount, bottom.toLinePart(), middle.toLinePart(), top.toLinePart());
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;

        BodyState(float x, float y, float angle, float velocityX, float velocityY) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY));
        }
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        return String.format(Locale.ROOT, "%.9g", value);
    }
}
