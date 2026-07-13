package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Ragdoll {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int BONE_COUNT = 11;

    private Ragdoll() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreateSegmentShape(groundId, b2DefaultShapeDef(),
            new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f)));

        Human[] human = {Human.create(worldId, new b2Vec2(0.0f, 25.0f), 1.0f, 0.03f, 5.0f, 0.5f, 1)};
        float[] settings = {0.03f, 5.0f, 0.5f};
        SampleRuntime.slider("ragdoll.friction", "Friction", settings[0], 0.0f, 1.0f, 0.01f, value -> {
            settings[0] = value;
            human[0].setJointFrictionTorque(value);
        });
        SampleRuntime.slider("ragdoll.hertz", "Hertz", settings[1], 0.0f, 10.0f, 0.1f, value -> {
            settings[1] = value;
            human[0].setJointSpringHertz(value);
        });
        SampleRuntime.slider("ragdoll.damping", "Damping", settings[2], 0.0f, 4.0f, 0.1f, value -> {
            settings[2] = value;
            human[0].setJointDampingRatio(value);
        });
        SampleRuntime.action("ragdoll.respawn", "Respawn", () -> {
            human[0].destroy();
            human[0] = Human.create(worldId, new b2Vec2(0.0f, 25.0f), 1.0f,
                settings[0], settings[1], settings[2], 1);
        });
        b2World_SetContactTuning(worldId, 240.0f, 0.0f, 2.0f);

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] bodies = new BodyState[BONE_COUNT];
        JointState[] joints = new JointState[BONE_COUNT - 1];
        for (int i = 0; i < BONE_COUNT; ++i) {
            bodies[i] = bodyState(human[0].bodies[i]);
            if (i > 0) {
                joints[i - 1] = jointState(human[0].joints[i]);
            }
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodies, joints);
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    private static JointState jointState(b2JointId jointId) {
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new JointState(b2RevoluteJoint_GetAngle(jointId), b2RevoluteJoint_GetMotorTorque(jointId),
            b2RevoluteJoint_GetMaxMotorTorque(jointId), b2RevoluteJoint_GetSpringHertz(jointId),
            b2RevoluteJoint_GetSpringDampingRatio(jointId), force.x, force.y, b2Joint_GetConstraintTorque(jointId));
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
        public final JointState[] joints;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, BodyState[] bodies,
            JointState[] joints) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.bodies = bodies;
            this.joints = joints;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("ragdoll ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(bodies.length).append(' ')
                .append(joints.length);
            for (BodyState body : bodies) {
                builder.append(body.toLinePart());
            }
            for (JointState joint : joints) {
                builder.append(joint.toLinePart());
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

    public static final class JointState {
        public final float angle;
        public final float motorTorque;
        public final float maxMotorTorque;
        public final float springHertz;
        public final float dampingRatio;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(float angle, float motorTorque, float maxMotorTorque, float springHertz, float dampingRatio,
            float forceX, float forceY, float torque) {
            this.angle = angle;
            this.motorTorque = motorTorque;
            this.maxMotorTorque = maxMotorTorque;
            this.springHertz = springHertz;
            this.dampingRatio = dampingRatio;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %s", formatFloat(angle),
                formatFloat(motorTorque), formatFloat(maxMotorTorque), formatFloat(springHertz),
                formatFloat(dampingRatio), formatFloat(forceX), formatFloat(forceY), formatFloat(torque));
        }
    }

    static final class Human {
        private static final int RAND_LIMIT = 32767;
        private static final int RAND_SEED = 12345;
        final b2BodyId[] bodies = new b2BodyId[BONE_COUNT];
        final b2JointId[] joints = new b2JointId[BONE_COUNT];
        final float[] frictionScales = new float[BONE_COUNT];
        float frictionTorque;
        float originalScale;
        float scale;

        static Human create(b2WorldId worldId, b2Vec2 position, float scale, float frictionTorque, float hertz,
            float dampingRatio, int groupIndex) {
            Human human = new Human();
            for (int i = 0; i < BONE_COUNT; ++i) {
                human.frictionScales[i] = 1.0f;
            }
            human.originalScale = scale;
            human.scale = scale;
            human.frictionTorque = frictionTorque;

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.sleepThreshold = 0.1f;

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.friction = 0.2f;
            shapeDef.filter.groupIndex = -groupIndex;
            shapeDef.filter.categoryBits = 2;
            shapeDef.filter.maskBits = 3;

            b2ShapeDef footShapeDef = copyShapeDef(shapeDef);
            footShapeDef.material.friction = 0.05f;
            footShapeDef.filter.categoryBits = 2;
            footShapeDef.filter.maskBits = 1;

            float maxTorque = frictionTorque * scale;
            b2Polygon footPolygon = makeFootPolygon(scale);

            BoneDef[] defs = boneDefs(scale);
            for (int i = 0; i < defs.length; ++i) {
                BoneDef def = defs[i];
                human.frictionScales[i] = def.frictionScale;
                bodyDef.position = b2Add(new b2Vec2(0.0f, def.positionY), position);
                bodyDef.linearDamping = def.linearDamping;
                bodyDef.name = def.name;
                human.bodies[i] = b2CreateBody(worldId, bodyDef);
                b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, def.capsuleY1), new b2Vec2(0.0f, def.capsuleY2),
                    def.radius);
                b2CreateCapsuleShape(human.bodies[i], shapeDef, capsule);
                if (def.hasFoot) {
                    b2CreatePolygonShape(human.bodies[i], footShapeDef, footPolygon);
                }
                if (def.parentIndex >= 0) {
                    b2Vec2 pivot = b2Add(new b2Vec2(0.0f, def.pivotY), position);
                    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
                    jointDef.bodyIdA = human.bodies[def.parentIndex];
                    jointDef.bodyIdB = human.bodies[i];
                    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
                    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
                    jointDef.referenceAngle = def.referenceAngle;
                    jointDef.enableLimit = true;
                    jointDef.lowerAngle = def.lowerAngle;
                    jointDef.upperAngle = def.upperAngle;
                    jointDef.enableMotor = true;
                    jointDef.maxMotorTorque = def.frictionScale * maxTorque;
                    jointDef.enableSpring = hertz > 0.0f;
                    jointDef.hertz = hertz;
                    jointDef.dampingRatio = dampingRatio;
                    jointDef.drawSize = 0.05f;
                    human.joints[i] = b2CreateRevoluteJoint(worldId, jointDef);
                }
            }
            return human;
        }

        void applyRandomAngularImpulse(float magnitude) {
            float impulse = new RandomState(RAND_SEED).randomFloatRange(-magnitude, magnitude);
            b2Body_ApplyAngularImpulse(bodies[1], impulse, true);
        }

        void setJointFrictionTorque(float torque) {
            frictionTorque = torque;
            for (int i = 1; i < BONE_COUNT; ++i) {
                b2RevoluteJoint_EnableMotor(joints[i], torque != 0.0f);
                if (torque != 0.0f) {
                    b2RevoluteJoint_SetMaxMotorTorque(joints[i], scale * frictionScales[i] * torque);
                }
            }
        }

        void setJointSpringHertz(float hertz) {
            for (int i = 1; i < BONE_COUNT; ++i) {
                b2RevoluteJoint_EnableSpring(joints[i], hertz != 0.0f);
                if (hertz != 0.0f) {
                    b2RevoluteJoint_SetSpringHertz(joints[i], hertz);
                }
            }
        }

        void setJointDampingRatio(float dampingRatio) {
            for (int i = 1; i < BONE_COUNT; ++i) {
                b2RevoluteJoint_SetSpringDampingRatio(joints[i], dampingRatio);
            }
        }

        void enableSensorEvents(boolean enable) {
            b2ShapeId[] shapeIds = new b2ShapeId[1];
            int shapeCount = b2Body_GetShapes(bodies[1], shapeIds, shapeIds.length);
            if (shapeCount == 1) {
                b2Shape_EnableSensorEvents(shapeIds[0], enable);
            }
        }

        void destroy() {
            for (b2BodyId body : bodies) {
                if (b2Body_IsValid(body)) {
                    b2DestroyBody(body);
                }
            }
        }

        void setScale(float nextScale) {
            float ratio = nextScale / scale;
            float originalRatio = nextScale / originalScale;
            float scaledFrictionTorque = originalRatio * originalRatio * originalRatio * frictionTorque;
            b2Vec2 origin = b2Body_GetPosition(bodies[0]);

            for (int boneIndex = 0; boneIndex < BONE_COUNT; ++boneIndex) {
                b2BodyId bodyId = bodies[boneIndex];
                if (boneIndex > 0) {
                    b2Transform transform = b2Body_GetTransform(bodyId);
                    transform.p = b2MulAdd(origin, ratio, b2Sub(transform.p, origin));
                    b2Body_SetTransform(bodyId, transform.p, transform.q);

                    b2JointId jointId = joints[boneIndex];
                    b2Joint_SetLocalAnchorA(jointId, b2MulSV(ratio, b2Joint_GetLocalAnchorA(jointId)));
                    b2Joint_SetLocalAnchorB(jointId, b2MulSV(ratio, b2Joint_GetLocalAnchorB(jointId)));
                    if (b2Joint_GetType(jointId) == b2_revoluteJoint) {
                        b2RevoluteJoint_SetMaxMotorTorque(jointId, frictionScales[boneIndex] * scaledFrictionTorque);
                    }
                }

                b2ShapeId[] shapeIds = new b2ShapeId[2];
                int shapeCount = b2Body_GetShapes(bodyId, shapeIds, 2);
                for (int shapeIndex = 0; shapeIndex < shapeCount; ++shapeIndex) {
                    b2ShapeId shapeId = shapeIds[shapeIndex];
                    int type = b2Shape_GetType(shapeId);
                    if (type == b2_capsuleShape) {
                        b2Capsule capsule = b2Shape_GetCapsule(shapeId);
                        capsule.center1 = b2MulSV(ratio, capsule.center1);
                        capsule.center2 = b2MulSV(ratio, capsule.center2);
                        capsule.radius *= ratio;
                        b2Shape_SetCapsule(shapeId, capsule);
                    } else if (type == b2_polygonShape) {
                        b2Polygon polygon = b2Shape_GetPolygon(shapeId);
                        for (int pointIndex = 0; pointIndex < polygon.count; ++pointIndex) {
                            polygon.vertices[pointIndex] = b2MulSV(ratio, polygon.vertices[pointIndex]);
                        }
                        polygon.centroid = b2MulSV(ratio, polygon.centroid);
                        polygon.radius *= ratio;
                        b2Shape_SetPolygon(shapeId, polygon);
                    }
                }

                b2Body_ApplyMassFromShapes(bodyId);
            }

            scale = nextScale;
        }

        private static b2Polygon makeFootPolygon(float scale) {
            b2Vec2[] points = {
                new b2Vec2(-0.03f * scale, -0.185f * scale),
                new b2Vec2(0.11f * scale, -0.185f * scale),
                new b2Vec2(0.11f * scale, -0.16f * scale),
                new b2Vec2(-0.03f * scale, -0.14f * scale)
            };
            b2Hull hull = b2ComputeHull(points, 4);
            return b2MakePolygon(hull, 0.015f * scale);
        }

        private static b2ShapeDef copyShapeDef(b2ShapeDef source) {
            b2ShapeDef copy = b2DefaultShapeDef();
            copy.userData = source.userData;
            copy.density = source.density;
            copy.filter.categoryBits = source.filter.categoryBits;
            copy.filter.maskBits = source.filter.maskBits;
            copy.filter.groupIndex = source.filter.groupIndex;
            copy.material.friction = source.material.friction;
            copy.material.restitution = source.material.restitution;
            copy.material.rollingResistance = source.material.rollingResistance;
            copy.material.tangentSpeed = source.material.tangentSpeed;
            copy.material.userMaterialId = source.material.userMaterialId;
            copy.material.customColor = source.material.customColor;
            copy.isSensor = source.isSensor;
            copy.enableSensorEvents = source.enableSensorEvents;
            copy.enableContactEvents = source.enableContactEvents;
            copy.enableHitEvents = source.enableHitEvents;
            copy.enablePreSolveEvents = source.enablePreSolveEvents;
            copy.invokeContactCreation = source.invokeContactCreation;
            copy.updateBodyMass = source.updateBodyMass;
            copy.internalValue = source.internalValue;
            return copy;
        }

        private static final class RandomState {
            private int seed;

            RandomState(int seed) {
                this.seed = seed;
            }

            float randomFloatRange(float lo, float hi) {
                float r = randomInt() & RAND_LIMIT;
                r /= RAND_LIMIT;
                return (hi - lo) * r + lo;
            }

            private int randomInt() {
                int x = seed;
                x ^= x << 13;
                x ^= x >>> 17;
                x ^= x << 5;
                seed = x;
                return x & RAND_LIMIT;
            }
        }
    }

    private static BoneDef[] boneDefs(float s) {
        return new BoneDef[] {
            new BoneDef("hip", -1, 0.95f * s, 0.0f, -0.02f * s, 0.02f * s, 0.095f * s,
                1.0f, 0.0f, 0.0f, 0.0f, false),
            new BoneDef("torso", 0, 1.2f * s, 0.0f, -0.135f * s, 0.135f * s, 0.09f * s,
                0.5f, 1.0f * s, -0.25f * B2_PI, 0.0f, false),
            new BoneDef("head", 1, 1.475f * s, 0.1f, -0.038f * s, 0.039f * s, 0.075f * s,
                0.25f, 1.4f * s, -0.3f * B2_PI, 0.1f * B2_PI, false),
            new BoneDef("upper_left_leg", 0, 0.775f * s, 0.0f, -0.125f * s, 0.125f * s, 0.06f * s,
                1.0f, 0.9f * s, -0.05f * B2_PI, 0.4f * B2_PI, false),
            new BoneDef("lower_left_leg", 3, 0.475f * s, 0.0f, -0.155f * s, 0.125f * s, 0.045f * s,
                0.5f, 0.625f * s, -0.5f * B2_PI, -0.02f * B2_PI, true),
            new BoneDef("upper_right_leg", 0, 0.775f * s, 0.0f, -0.125f * s, 0.125f * s, 0.06f * s,
                1.0f, 0.9f * s, -0.05f * B2_PI, 0.4f * B2_PI, false),
            new BoneDef("lower_right_leg", 5, 0.475f * s, 0.0f, -0.155f * s, 0.125f * s, 0.045f * s,
                0.5f, 0.625f * s, -0.5f * B2_PI, -0.02f * B2_PI, true),
            new BoneDef("upper_left_arm", 1, 1.225f * s, 0.0f, -0.125f * s, 0.125f * s, 0.035f * s,
                0.5f, 1.35f * s, -0.1f * B2_PI, 0.8f * B2_PI, false),
            new BoneDef("lower_left_arm", 7, 0.975f * s, 0.1f, -0.125f * s, 0.125f * s, 0.03f * s,
                0.1f, 1.1f * s, -0.2f * B2_PI, 0.3f * B2_PI, false, 0.25f * B2_PI),
            new BoneDef("upper_right_arm", 1, 1.225f * s, 0.0f, -0.125f * s, 0.125f * s, 0.035f * s,
                0.5f, 1.35f * s, -0.1f * B2_PI, 0.8f * B2_PI, false),
            new BoneDef("lower_right_arm", 9, 0.975f * s, 0.1f, -0.125f * s, 0.125f * s, 0.03f * s,
                0.1f, 1.1f * s, -0.2f * B2_PI, 0.3f * B2_PI, false, 0.25f * B2_PI)
        };
    }

    private static final class BoneDef {
        final String name;
        final int parentIndex;
        final float positionY;
        final float linearDamping;
        final float capsuleY1;
        final float capsuleY2;
        final float radius;
        final float frictionScale;
        final float pivotY;
        final float lowerAngle;
        final float upperAngle;
        final boolean hasFoot;
        final float referenceAngle;

        BoneDef(String name, int parentIndex, float positionY, float linearDamping, float capsuleY1, float capsuleY2,
            float radius, float frictionScale, float pivotY, float lowerAngle, float upperAngle, boolean hasFoot) {
            this(name, parentIndex, positionY, linearDamping, capsuleY1, capsuleY2, radius, frictionScale, pivotY,
                lowerAngle, upperAngle, hasFoot, 0.0f);
        }

        BoneDef(String name, int parentIndex, float positionY, float linearDamping, float capsuleY1, float capsuleY2,
            float radius, float frictionScale, float pivotY, float lowerAngle, float upperAngle, boolean hasFoot,
            float referenceAngle) {
            this.name = name;
            this.parentIndex = parentIndex;
            this.positionY = positionY;
            this.linearDamping = linearDamping;
            this.capsuleY1 = capsuleY1;
            this.capsuleY2 = capsuleY2;
            this.radius = radius;
            this.frictionScale = frictionScale;
            this.pivotY = pivotY;
            this.lowerAngle = lowerAngle;
            this.upperAngle = upperAngle;
            this.hasFoot = hasFoot;
            this.referenceAngle = referenceAngle;
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
