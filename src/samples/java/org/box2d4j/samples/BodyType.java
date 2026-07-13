package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2PrismaticJointDef;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class BodyType {
    private static final int DEFAULT_STEP_COUNT = 0;
    private static final float SPEED = 3.0f;

    private BodyType() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        return runScript(stepCount, 5);
    }

    public static Result runScript(int stepCount, int scriptPhaseCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = createScene(worldId);
        boolean interactive = SampleRuntime.isActive();
        if (interactive) {
            SampleRuntime.choice("bodyType.type", "Body Type", scene.type,
                new String[] {"Static", "Kinematic", "Dynamic"}, scene::setType);
            SampleRuntime.toggle("bodyType.enabled", "Enable", scene.enabled, scene::setEnabled);
            SampleRuntime.beforeStep(scene::step);
        } else {
            scene.applyGuiScript(scriptPhaseCount);
        }

        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                scene.step();
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        BodyState[] states = new BodyState[scene.bodies.length];
        for (int i = 0; i < scene.bodies.length; ++i) {
            states[i] = bodyState(scene.bodies[i]);
        }
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), scene.type, scene.enabled, states);
        b2DestroyWorld(worldId);
        return result;
    }

    private static Scene createScene(b2WorldId worldId) {
        int type = b2_dynamicBody;
        boolean enabled = true;
        b2BodyId groundId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            groundId = b2CreateBody(worldId, bodyDef);

            b2Segment segment = new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyId attachmentId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-2.0f, 3.0f);
            attachmentId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(0.5f, 2.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2CreatePolygonShape(attachmentId, shapeDef, box);
        }

        b2BodyId secondAttachmentId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = type;
            bodyDef.isEnabled = enabled;
            bodyDef.position = new b2Vec2(3.0f, 3.0f);
            secondAttachmentId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(0.5f, 2.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2CreatePolygonShape(secondAttachmentId, shapeDef, box);
        }

        b2BodyId platformId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = type;
            bodyDef.isEnabled = enabled;
            bodyDef.position = new b2Vec2(-4.0f, 5.0f);
            platformId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeOffsetBox(0.5f, 4.0f, new b2Vec2(4.0f, 0.0f), b2MakeRot(0.5f * B2_PI));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 2.0f;
            b2CreatePolygonShape(platformId, shapeDef, box);

            b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
            b2Vec2 pivot = new b2Vec2(-2.0f, 5.0f);
            revoluteDef.bodyIdA = attachmentId;
            revoluteDef.bodyIdB = platformId;
            revoluteDef.localAnchorA = b2Body_GetLocalPoint(attachmentId, pivot);
            revoluteDef.localAnchorB = b2Body_GetLocalPoint(platformId, pivot);
            revoluteDef.maxMotorTorque = 50.0f;
            revoluteDef.enableMotor = true;
            b2CreateRevoluteJoint(worldId, revoluteDef);

            pivot = new b2Vec2(3.0f, 5.0f);
            revoluteDef.bodyIdA = secondAttachmentId;
            revoluteDef.bodyIdB = platformId;
            revoluteDef.localAnchorA = b2Body_GetLocalPoint(secondAttachmentId, pivot);
            revoluteDef.localAnchorB = b2Body_GetLocalPoint(platformId, pivot);
            revoluteDef.maxMotorTorque = 50.0f;
            revoluteDef.enableMotor = true;
            b2CreateRevoluteJoint(worldId, revoluteDef);

            b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
            b2Vec2 anchor = new b2Vec2(0.0f, 5.0f);
            prismaticDef.bodyIdA = groundId;
            prismaticDef.bodyIdB = platformId;
            prismaticDef.localAnchorA = b2Body_GetLocalPoint(groundId, anchor);
            prismaticDef.localAnchorB = b2Body_GetLocalPoint(platformId, anchor);
            prismaticDef.localAxisA = new b2Vec2(1.0f, 0.0f);
            prismaticDef.maxMotorForce = 1000.0f;
            prismaticDef.motorSpeed = 0.0f;
            prismaticDef.enableMotor = true;
            prismaticDef.lowerTranslation = -10.0f;
            prismaticDef.upperTranslation = 10.0f;
            prismaticDef.enableLimit = true;
            b2CreatePrismaticJoint(worldId, prismaticDef);
        }

        b2BodyId payloadId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-3.0f, 8.0f);
            payloadId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(0.75f, 0.75f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 2.0f;
            b2CreatePolygonShape(payloadId, shapeDef, box);
        }

        b2BodyId secondPayloadId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = type;
            bodyDef.isEnabled = enabled;
            bodyDef.position = new b2Vec2(2.0f, 8.0f);
            secondPayloadId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(0.75f, 0.75f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 2.0f;
            b2CreatePolygonShape(secondPayloadId, shapeDef, box);
        }

        b2BodyId touchingBodyId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = type;
            bodyDef.isEnabled = enabled;
            bodyDef.position = new b2Vec2(8.0f, 0.2f);
            touchingBodyId = b2CreateBody(worldId, bodyDef);

            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.25f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 2.0f;
            b2CreateCapsuleShape(touchingBodyId, shapeDef, capsule);
        }

        b2BodyId floatingBodyId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = type;
            bodyDef.isEnabled = enabled;
            bodyDef.position = new b2Vec2(-8.0f, 12.0f);
            bodyDef.gravityScale = 0.0f;
            floatingBodyId = b2CreateBody(worldId, bodyDef);

            b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.5f), 0.25f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 2.0f;
            b2CreateCircleShape(floatingBodyId, shapeDef, circle);
        }

        return new Scene(type, enabled, platformId,
            new b2BodyId[] {attachmentId, secondAttachmentId, platformId, payloadId, secondPayloadId, touchingBodyId,
                floatingBodyId});
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(b2Body_GetType(bodyId), b2Body_IsEnabled(bodyId), b2Body_IsAwake(bodyId),
            b2Body_GetContactCapacity(bodyId), p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)), v.x, v.y,
            b2Body_GetAngularVelocity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        int scriptPhaseCount = args.length < 2 ? 5 : Integer.parseInt(args[1]);
        System.out.println(runScript(stepCount, scriptPhaseCount).toLine());
    }

    private static final class Scene {
        int type;
        boolean enabled;
        final b2BodyId platformId;
        final b2BodyId[] bodies;

        Scene(int type, boolean enabled, b2BodyId platformId, b2BodyId[] bodies) {
            this.type = type;
            this.enabled = enabled;
            this.platformId = platformId;
            this.bodies = bodies;
        }

        void applyGuiScript(int phaseCount) {
            if (phaseCount >= 1) {
                setType(b2_kinematicBody);
            }
            if (phaseCount >= 2) {
                setEnabled(false);
            }
            if (phaseCount >= 3) {
                setEnabled(true);
            }
            if (phaseCount >= 4) {
                setType(b2_staticBody);
            }
            if (phaseCount >= 5) {
                setType(b2_dynamicBody);
            }
        }

        void setType(int nextType) {
            type = nextType;
            b2Body_SetType(bodies[2], nextType);
            if (nextType == b2_kinematicBody) {
                b2Body_SetLinearVelocity(bodies[2], new b2Vec2(-SPEED, 0.0f));
                b2Body_SetAngularVelocity(bodies[2], 0.0f);
            }
            b2Body_SetType(bodies[1], nextType);
            b2Body_SetType(bodies[4], nextType);
            b2Body_SetType(bodies[5], nextType);
            b2Body_SetType(bodies[6], nextType);
        }

        void setEnabled(boolean nextEnabled) {
            enabled = nextEnabled;
            if (nextEnabled) {
                for (int i : new int[] {2, 1, 4, 5, 6}) {
                    b2Body_Enable(bodies[i]);
                }
                if (type == b2_kinematicBody) {
                    b2Body_SetLinearVelocity(platformId, new b2Vec2(-SPEED, 0.0f));
                    b2Body_SetAngularVelocity(platformId, 0.0f);
                }
            } else {
                for (int i : new int[] {2, 1, 4, 5, 6}) {
                    b2Body_Disable(bodies[i]);
                }
            }
        }

        void step() {
            if (type == b2_kinematicBody) {
                b2Vec2 p = b2Body_GetPosition(platformId);
                b2Vec2 v = b2Body_GetLinearVelocity(platformId);
                if ((p.x < -14.0f && v.x < 0.0f) || (p.x > 6.0f && v.x > 0.0f)) {
                    v.x = -v.x;
                    b2Body_SetLinearVelocity(platformId, v);
                }
            }
        }
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int type;
        public final boolean enabled;
        public final BodyState[] states;

        public Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, int type,
            boolean enabled, BodyState[] states) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.type = type;
            this.enabled = enabled;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("bodyType ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(type).append(' ')
                .append(enabled ? 1 : 0).append(' ')
                .append(states.length);
            for (BodyState state : states) {
                builder.append(state.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final int type;
        public final boolean enabled;
        public final boolean awake;
        public final int contactCapacity;
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;

        BodyState(int type, boolean enabled, boolean awake, int contactCapacity, float x, float y, float angle,
            float velocityX, float velocityY, float angularVelocity) {
            this.type = type;
            this.enabled = enabled;
            this.awake = awake;
            this.contactCapacity = contactCapacity;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %d %d %d %d %s %s %s %s %s %s", type, enabled ? 1 : 0,
                awake ? 1 : 0, contactCapacity, formatFloat(x), formatFloat(y), formatFloat(angle),
                formatFloat(velocityX), formatFloat(velocityY), formatFloat(angularVelocity));
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
