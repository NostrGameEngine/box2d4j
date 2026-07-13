package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class KinematicBody {
    private static final int DEFAULT_STEP_COUNT = 120;

    private KinematicBody() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        float amplitude = 2.0f;
        float time = 0.0f;
        float timeStep = 1.0f / 60.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_kinematicBody;
        bodyDef.position.x = 2.0f * amplitude;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2Polygon box = b2MakeBox(0.1f, 1.0f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(bodyId, shapeDef, box);

        for (int i = 0; i < stepCount; ++i) {
            b2Vec2 point = new b2Vec2(
                2.0f * amplitude * (float) Math.cos(time),
                amplitude * (float) Math.sin(2.0f * time));
            b2Body_SetTargetTransform(bodyId, new b2Transform(point, b2MakeRot(2.0f * time)), timeStep);

            b2World_Step(worldId, timeStep, 4);
            time += timeStep;
        }

        b2Vec2 p = b2Body_GetPosition(bodyId);
        float angle = b2Rot_GetAngle(b2Body_GetRotation(bodyId));
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        float angularVelocity = b2Body_GetAngularVelocity(bodyId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(p.x, p.y, angle, v.x, v.y, angularVelocity, time, awakeBodyCount);
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
        public final float time;
        public final int awakeBodyCount;

        public Result(float x, float y, float angle, float velocityX, float velocityY,
                      float angularVelocity, float time, int awakeBodyCount) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.time = time;
            this.awakeBodyCount = awakeBodyCount;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "kinematic %s %s %s %s %s %s %s %d",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), formatFloat(time), awakeBodyCount);
        }

        private static String formatFloat(float value) {
            if (value == 0.0f) {
                return "0";
            }
            return trimTrailingZeros(String.format(Locale.ROOT, "%.9g", value));
        }

        private static String trimTrailingZeros(String text) {
            int exponent = Math.max(text.indexOf('e'), text.indexOf('E'));
            String suffix = exponent >= 0 ? text.substring(exponent) : "";
            String mantissa = exponent >= 0 ? text.substring(0, exponent) : text;
            if (mantissa.indexOf('.') >= 0) {
                while (mantissa.endsWith("0")) {
                    mantissa = mantissa.substring(0, mantissa.length() - 1);
                }
                if (mantissa.endsWith(".")) {
                    mantissa = mantissa.substring(0, mantissa.length() - 1);
                }
            }
            return mantissa + suffix;
        }
    }
}
