package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2CosSin;
import org.box2d4j.b2Counters;
import org.box2d4j.b2ExplosionDef;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WeldJointDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Explosion {
    private static final int STEP_COUNT = 120;
    private static final int BODY_COUNT = 12;

    private Explosion() {
    }

    public static Result run() {
        return run(STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        bodyDef.type = b2_dynamicBody;
        bodyDef.gravityScale = 0.0f;
        b2ShapeDef shapeDef = b2DefaultShapeDef();

        float referenceAngle = 0.0f;

        b2WeldJointDef weldDef = b2DefaultWeldJointDef();
        weldDef.referenceAngle = referenceAngle;
        weldDef.angularHertz = 0.5f;
        weldDef.angularDampingRatio = 0.7f;
        weldDef.linearHertz = 0.5f;
        weldDef.linearDampingRatio = 0.7f;
        weldDef.bodyIdA = groundId;
        weldDef.localAnchorB = b2Vec2_zero;

        b2BodyId[] bodies = new b2BodyId[BODY_COUNT];
        b2JointId[] joints = new b2JointId[BODY_COUNT];
        float r = 8.0f;
        int index = 0;
        for (float angle = 0.0f; angle < 360.0f; angle += 30.0f) {
            b2CosSin cosSin = b2ComputeCosSin(angle * B2_PI / 180.0f);
            bodyDef.position = new b2Vec2(r * cosSin.cosine, r * cosSin.sine);
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(1.0f, 0.1f);
            b2CreatePolygonShape(bodyId, shapeDef, box);

            weldDef.localAnchorA = bodyDef.position;
            weldDef.bodyIdB = bodyId;
            b2JointId jointId = b2CreateWeldJoint(worldId, weldDef);
            bodies[index] = bodyId;
            joints[index] = jointId;
            index += 1;
        }

        float[] parameters = {7.0f, 3.0f, 10.0f};
        Runnable explode = () -> explode(worldId, parameters);
        SampleRuntime.action("explosion.explode", "Explode", explode);
        SampleRuntime.slider("explosion.radius", "Radius", parameters[0], 0.0f, 20.0f, 0.1f,
            value -> parameters[0] = value);
        SampleRuntime.slider("explosion.falloff", "Falloff", parameters[1], 0.0f, 20.0f, 0.1f,
            value -> parameters[1] = value);
        SampleRuntime.slider("explosion.impulse", "Impulse", parameters[2], -20.0f, 20.0f, 0.1f,
            value -> parameters[2] = value);
        boolean interactive = SampleRuntime.isActive();
        if (!interactive) {
            explode.run();
        }
        float[] referenceAngleState = {referenceAngle};
        SampleRuntime.beforeStep(() -> updateReferenceAngle(joints, referenceAngleState));

        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                updateReferenceAngle(joints, referenceAngleState);
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] states = new BodyState[bodies.length];
        for (int i = 0; i < bodies.length; ++i) {
            states[i] = bodyState(bodies[i]);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(referenceAngleState[0], counters.bodyCount, counters.shapeCount, counters.jointCount,
            counters.contactCount, b2World_GetAwakeBodyCount(worldId), states);
        b2DestroyWorld(worldId);
        return result;
    }

    private static void explode(b2WorldId worldId, float[] parameters) {
        b2ExplosionDef def = b2DefaultExplosionDef();
        def.position = b2Vec2_zero;
        def.radius = parameters[0];
        def.falloff = parameters[1];
        def.impulsePerLength = parameters[2];
        b2World_Explode(worldId, def);
    }

    private static void updateReferenceAngle(b2JointId[] joints, float[] referenceAngle) {
        referenceAngle[0] = b2UnwindAngle(referenceAngle[0] + 60.0f * B2_PI / 180.0f / 60.0f);
        for (b2JointId joint : joints) {
            b2Joint_SetReferenceAngle(joint, referenceAngle[0]);
        }
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length > 0 ? Integer.parseInt(args[0]) : STEP_COUNT;
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final float referenceAngle;
        public final int bodyCount;
        public final int shapeCount;
        public final int jointCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState[] states;

        public Result(float referenceAngle, int bodyCount, int shapeCount, int jointCount, int contactCount,
                      int awakeBodyCount, BodyState[] states) {
            this.referenceAngle = referenceAngle;
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.jointCount = jointCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("explosion ")
                .append(formatFloat(referenceAngle)).append(' ')
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(jointCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(states.length);
            for (BodyState state : states) {
                builder.append(state.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;

        BodyState(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity));
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
