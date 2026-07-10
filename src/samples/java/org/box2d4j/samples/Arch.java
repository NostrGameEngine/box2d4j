package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Arch {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int BODY_COUNT = 21;

    private Arch() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId[] bodies = new b2BodyId[BODY_COUNT];
        int bodyIndex = 0;

        b2Vec2[] ps1 = {
            new b2Vec2(16.0f, 0.0f),
            new b2Vec2(14.93803712795643f, 5.133601056842984f),
            new b2Vec2(13.79871746027416f, 10.24928069555078f),
            new b2Vec2(12.56252963284711f, 15.34107019122473f),
            new b2Vec2(11.20040987372525f, 20.39856541571217f),
            new b2Vec2(9.66521217819836f, 25.40369899225096f),
            new b2Vec2(7.87179930638133f, 30.3179337000085f),
            new b2Vec2(5.635199558196225f, 35.03820717801641f),
            new b2Vec2(2.405937953536585f, 39.09554102558315f)
        };
        b2Vec2[] ps2 = {
            new b2Vec2(24.0f, 0.0f),
            new b2Vec2(22.33619528222415f, 6.02299846205841f),
            new b2Vec2(20.54936888969905f, 12.00964361211476f),
            new b2Vec2(18.60854610798073f, 17.9470321677465f),
            new b2Vec2(16.46769273811807f, 23.81367936585418f),
            new b2Vec2(14.05325025774858f, 29.57079353071012f),
            new b2Vec2(11.23551045834022f, 35.13775818285372f),
            new b2Vec2(7.752568160730571f, 40.30450679009583f),
            new b2Vec2(3.016931552701656f, 44.28891593799322f)
        };

        float scale = 0.25f;
        for (int i = 0; i < ps1.length; ++i) {
            ps1[i] = b2MulSV(scale, ps1[i]);
            ps2[i] = b2MulSV(scale, ps2[i]);
        }

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.6f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2Segment segment = new b2Segment(new b2Vec2(-100.0f, 0.0f), new b2Vec2(100.0f, 0.0f));
        b2CreateSegmentShape(groundId, shapeDef, segment);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        for (int i = 0; i < 8; ++i) {
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2Vec2[] ps = {ps1[i], ps2[i], ps2[i + 1], ps1[i + 1]};
            b2Hull hull = b2ComputeHull(ps, 4);
            b2Polygon polygon = b2MakePolygon(hull, 0.0f);
            b2CreatePolygonShape(bodyId, shapeDef, polygon);
            bodies[bodyIndex++] = bodyId;
        }

        for (int i = 0; i < 8; ++i) {
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2Vec2[] ps = {
                new b2Vec2(-ps2[i].x, ps2[i].y),
                new b2Vec2(-ps1[i].x, ps1[i].y),
                new b2Vec2(-ps1[i + 1].x, ps1[i + 1].y),
                new b2Vec2(-ps2[i + 1].x, ps2[i + 1].y)
            };
            b2Hull hull = b2ComputeHull(ps, 4);
            b2Polygon polygon = b2MakePolygon(hull, 0.0f);
            b2CreatePolygonShape(bodyId, shapeDef, polygon);
            bodies[bodyIndex++] = bodyId;
        }

        {
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2Vec2[] ps = {
                ps1[8],
                ps2[8],
                new b2Vec2(-ps2[8].x, ps2[8].y),
                new b2Vec2(-ps1[8].x, ps1[8].y)
            };
            b2Hull hull = b2ComputeHull(ps, 4);
            b2Polygon polygon = b2MakePolygon(hull, 0.0f);
            b2CreatePolygonShape(bodyId, shapeDef, polygon);
            bodies[bodyIndex++] = bodyId;
        }

        for (int i = 0; i < 4; ++i) {
            b2Polygon box = b2MakeBox(2.0f, 0.5f);
            bodyDef.position = new b2Vec2(0.0f, 0.5f + ps2[8].y + 1.0f * i);
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(bodyId, shapeDef, box);
            bodies[bodyIndex++] = bodyId;
        }

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] states = new BodyState[BODY_COUNT];
        for (int i = 0; i < BODY_COUNT; ++i) {
            states[i] = bodyState(bodies[i]);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(worldId), states);
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final BodyState[] bodies;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount,
               BodyState[] bodies) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.bodies = bodies;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("arch ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(bodies.length);
            for (BodyState body : bodies) {
                builder.append(body.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float cos;
        public final float sin;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int shapeCount;
        public final int contactCapacity;

        BodyState(float x, float y, float cos, float sin, float velocityX, float velocityY, float angularVelocity,
                  int shapeCount, int contactCapacity) {
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.shapeCount = shapeCount;
            this.contactCapacity = contactCapacity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %d %d", formatFloat(x), formatFloat(y),
                formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), shapeCount, contactCapacity);
        }
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.9g", value);
        int exponent = Math.max(text.indexOf('e'), text.indexOf('E'));
        String suffix = "";
        if (exponent >= 0) {
            suffix = text.substring(exponent);
            text = text.substring(0, exponent);
        }
        if (text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text + suffix;
    }
}
