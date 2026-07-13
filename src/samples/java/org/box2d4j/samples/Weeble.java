package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2ExplosionDef;
import org.box2d4j.b2MassData;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Weeble {
    private static final int DEFAULT_STEP_COUNT = 240;

    private Weeble() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2World_SetFrictionCallback(worldId, (frictionA, materialA, frictionB, materialB) -> 0.1f);
        b2World_SetRestitutionCallback(worldId, (restitutionA, materialA, restitutionB, materialB) -> 1.0f);

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2Segment segment = new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 3.0f);
        bodyDef.rotation = b2MakeRot(0.25f * B2_PI);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -1.0f), new b2Vec2(0.0f, 1.0f), 1.0f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCapsuleShape(bodyId, shapeDef, capsule);

        float mass = b2Body_GetMass(bodyId);
        float inertiaTensor = b2Body_GetRotationalInertia(bodyId);
        float offset = 1.5f;
        inertiaTensor += mass * offset * offset;

        b2MassData massData = new b2MassData();
        massData.mass = mass;
        massData.center = new b2Vec2(0.0f, -offset);
        massData.rotationalInertia = inertiaTensor;
        b2Body_SetMassData(bodyId, massData);
        float[] magnitude = {8.0f};
        SampleRuntime.action("weeble.teleport", "Teleport", () ->
            b2Body_SetTransform(bodyId, new b2Vec2(0.0f, 5.0f), b2MakeRot(0.95f * B2_PI)));
        SampleRuntime.action("weeble.explode", "Explode", () -> {
            b2ExplosionDef def = b2DefaultExplosionDef();
            def.position = new b2Vec2();
            def.radius = 2.0f;
            def.falloff = 0.1f;
            def.impulsePerLength = magnitude[0];
            b2World_Explode(worldId, def);
        });
        SampleRuntime.slider("weeble.magnitude", "Magnitude", magnitude[0], -100.0f, 100.0f, 1.0f,
            value -> magnitude[0] = value);

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
        float angularVelocity = b2Body_GetAngularVelocity(bodyId);
        b2Vec2 localPoint = new b2Vec2(0.0f, 2.0f);
        b2Vec2 worldPoint = b2Body_GetWorldPoint(bodyId, localPoint);
        b2Vec2 localVelocity = b2Body_GetLocalPointVelocity(bodyId, localPoint);
        b2Vec2 worldVelocity = b2Body_GetWorldPointVelocity(bodyId, worldPoint);
        b2MassData currentMass = b2Body_GetMassData(bodyId);
        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        Result result = new Result(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            linearVelocity.x, linearVelocity.y, angularVelocity, worldPoint.x, worldPoint.y,
            localVelocity.x, localVelocity.y, worldVelocity.x, worldVelocity.y,
            currentMass.mass, currentMass.center.y, currentMass.rotationalInertia,
            counters.contactCount, awakeBodyCount);
        b2DestroyWorld(worldId);
        return result;
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
        public final float worldPointX;
        public final float worldPointY;
        public final float localVelocityX;
        public final float localVelocityY;
        public final float worldVelocityX;
        public final float worldVelocityY;
        public final float mass;
        public final float centerY;
        public final float rotationalInertia;
        public final int contactCount;
        public final int awakeBodyCount;

        public Result(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity,
                      float worldPointX, float worldPointY, float localVelocityX, float localVelocityY,
                      float worldVelocityX, float worldVelocityY, float mass, float centerY, float rotationalInertia,
                      int contactCount, int awakeBodyCount) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.worldPointX = worldPointX;
            this.worldPointY = worldPointY;
            this.localVelocityX = localVelocityX;
            this.localVelocityY = localVelocityY;
            this.worldVelocityX = worldVelocityX;
            this.worldVelocityY = worldVelocityY;
            this.mass = mass;
            this.centerY = centerY;
            this.rotationalInertia = rotationalInertia;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "weeble %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %d %d",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), formatFloat(worldPointX), formatFloat(worldPointY),
                formatFloat(localVelocityX), formatFloat(localVelocityY), formatFloat(worldVelocityX),
                formatFloat(worldVelocityY), formatFloat(mass), formatFloat(centerY), formatFloat(rotationalInertia),
                contactCount, awakeBodyCount);
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
