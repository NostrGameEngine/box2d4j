package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class CardHouse {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int BODY_COUNT = 40;

    private CardHouse() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId[] bodies = new b2BodyId[BODY_COUNT];
        int bodyIndex = 0;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(0.0f, -2.0f);
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.7f;

        b2Polygon groundBox = b2MakeBox(40.0f, 2.0f);
        b2CreatePolygonShape(groundId, shapeDef, groundBox);

        float cardHeight = 0.2f;
        float cardThickness = 0.001f;

        float angle0 = 25.0f * B2_PI / 180.0f;
        float angle1 = -25.0f * B2_PI / 180.0f;
        float angle2 = 0.5f * B2_PI;

        b2Polygon cardBox = b2MakeBox(cardThickness, cardHeight);
        bodyDef.type = b2_dynamicBody;

        int nb = 5;
        float z0 = 0.0f;
        float y = cardHeight - 0.02f;
        while (nb != 0) {
            float z = z0;
            for (int i = 0; i < nb; ++i) {
                if (i != nb - 1) {
                    bodyDef.position = new b2Vec2(z + 0.25f, y + cardHeight - 0.015f);
                    bodyDef.rotation = b2MakeRot(angle2);
                    b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                    b2CreatePolygonShape(bodyId, shapeDef, cardBox);
                    bodies[bodyIndex++] = bodyId;
                }

                bodyDef.position = new b2Vec2(z, y);
                bodyDef.rotation = b2MakeRot(angle1);
                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, cardBox);
                bodies[bodyIndex++] = bodyId;

                z += 0.175f;

                bodyDef.position = new b2Vec2(z, y);
                bodyDef.rotation = b2MakeRot(angle0);
                bodyId = b2CreateBody(worldId, bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, cardBox);
                bodies[bodyIndex++] = bodyId;

                z += 0.175f;
            }
            y += cardHeight * 2.0f - 0.03f;
            z0 += 0.175f;
            nb -= 1;
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
            builder.append("cardHouse ")
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
