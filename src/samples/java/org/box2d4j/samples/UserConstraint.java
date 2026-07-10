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

public final class UserConstraint {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final float SAMPLE_HERTZ = 60.0f;
    private static final float CONSTRAINT_HERTZ = 3.0f;
    private static final float DAMPING_RATIO = 0.7f;
    private static final float MAX_FORCE = 1000.0f;

    private UserConstraint() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2Polygon box = b2MakeBox(1.0f, 0.5f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 20.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.gravityScale = 1.0f;
        bodyDef.angularDamping = 0.5f;
        bodyDef.linearDamping = 0.2f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyId, shapeDef, box);

        float[] impulses = {0.0f, 0.0f};
        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / SAMPLE_HERTZ, 4);
            applyUserConstraint(bodyId, impulses);
        }

        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        b2Vec2 center = b2Body_GetWorldCenterOfMass(bodyId);
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), transform.p.x, transform.p.y, transform.q.c, transform.q.s,
            velocity.x, velocity.y, b2Body_GetAngularVelocity(bodyId), b2Body_GetMass(bodyId),
            b2Body_GetRotationalInertia(bodyId), center.x, center.y, impulses[0], impulses[1],
            impulses[0] * SAMPLE_HERTZ, impulses[1] * SAMPLE_HERTZ);
        b2DestroyWorld(worldId);
        return result;
    }

    private static void applyUserConstraint(b2BodyId bodyId, float[] impulses) {
        float timeStep = 1.0f / SAMPLE_HERTZ;
        float omega = 2.0f * B2_PI * CONSTRAINT_HERTZ;
        float sigma = 2.0f * DAMPING_RATIO + timeStep * omega;
        float s = timeStep * omega * sigma;
        float impulseCoefficient = 1.0f / (1.0f + s);
        float massCoefficient = s * impulseCoefficient;
        float biasCoefficient = omega / sigma;

        b2Vec2[] localAnchors = {new b2Vec2(1.0f, -0.5f), new b2Vec2(1.0f, 0.5f)};
        float mass = b2Body_GetMass(bodyId);
        float invMass = mass < 0.0001f ? 0.0f : 1.0f / mass;
        float inertiaTensor = b2Body_GetRotationalInertia(bodyId);
        float invI = inertiaTensor < 0.0001f ? 0.0f : 1.0f / inertiaTensor;

        b2Vec2 vB = b2Body_GetLinearVelocity(bodyId);
        float omegaB = b2Body_GetAngularVelocity(bodyId);
        b2Vec2 pB = b2Body_GetWorldCenterOfMass(bodyId);

        for (int i = 0; i < 2; ++i) {
            b2Vec2 anchorA = new b2Vec2(3.0f, 0.0f);
            b2Vec2 anchorB = b2Body_GetWorldPoint(bodyId, localAnchors[i]);
            b2Vec2 deltaAnchor = b2Sub(anchorB, anchorA);

            float slackLength = 1.0f;
            float length = b2Length(deltaAnchor);
            float c = length - slackLength;
            if (c < 0.0f || length < 0.001f) {
                impulses[i] = 0.0f;
                continue;
            }

            b2Vec2 axis = b2Normalize(deltaAnchor);
            b2Vec2 rB = b2Sub(anchorB, pB);
            float jb = b2Cross(rB, axis);
            float k = invMass + jb * invI * jb;
            float invK = k < 0.0001f ? 0.0f : 1.0f / k;

            float cDot = b2Dot(vB, axis) + jb * omegaB;
            float impulse = -massCoefficient * invK * (cDot + biasCoefficient * c);
            float appliedImpulse = b2ClampFloat(impulse, -MAX_FORCE * timeStep, 0.0f);

            vB = b2MulAdd(vB, invMass * appliedImpulse, axis);
            omegaB += appliedImpulse * invI * jb;
            impulses[i] = appliedImpulse;
        }

        b2Body_SetLinearVelocity(bodyId, vB);
        b2Body_SetAngularVelocity(bodyId, omegaB);
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
        public final float x;
        public final float y;
        public final float cos;
        public final float sin;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final float mass;
        public final float rotationalInertia;
        public final float centerX;
        public final float centerY;
        public final float impulse0;
        public final float impulse1;
        public final float force0;
        public final float force1;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, float x, float y,
            float cos, float sin, float velocityX, float velocityY, float angularVelocity, float mass,
            float rotationalInertia, float centerX, float centerY, float impulse0, float impulse1, float force0,
            float force1) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.mass = mass;
            this.rotationalInertia = rotationalInertia;
            this.centerX = centerX;
            this.centerY = centerY;
            this.impulse0 = impulse0;
            this.impulse1 = impulse1;
            this.force0 = force0;
            this.force1 = force1;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "userConstraint %d %d %d %d %d %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s",
                bodyCount, shapeCount, contactCount, jointCount, awakeBodyCount, formatFloat(x), formatFloat(y),
                formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), formatFloat(mass), formatFloat(rotationalInertia), formatFloat(centerX),
                formatFloat(centerY), formatFloat(impulse0), formatFloat(impulse1), formatFloat(force0),
                formatFloat(force1));
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
