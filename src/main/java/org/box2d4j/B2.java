package org.box2d4j;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

/** C-style Box2D functions and constants. */
public final class B2 {
    public static final float B2_PI = 3.14159265359f;
    public static final int B2_HASH_INIT = 5381;
    public static final int B2_MAX_POLYGON_VERTICES = 8;
    public static final long B2_DEFAULT_CATEGORY_BITS = 1L;
    public static final long B2_DEFAULT_MASK_BITS = -1L;
    public static final int B2_MAX_WORLDS = 128;
    public static final int b2_staticBody = 0;
    public static final int b2_kinematicBody = 1;
    public static final int b2_dynamicBody = 2;
    public static final int b2_circleShape = 0;
    public static final int b2_capsuleShape = 1;
    public static final int b2_segmentShape = 2;
    public static final int b2_polygonShape = 3;
    public static final int b2_chainSegmentShape = 4;
    public static final int b2_shapeTypeCount = 5;
    public static final int b2_distanceJoint = 0;
    public static final int b2_filterJoint = 1;
    public static final int b2_motorJoint = 2;
    public static final int b2_mouseJoint = 3;
    public static final int b2_prismaticJoint = 4;
    public static final int b2_revoluteJoint = 5;
    public static final int b2_weldJoint = 6;
    public static final int b2_wheelJoint = 7;
    public static final int b2_toiStateUnknown = 0;
    public static final int b2_toiStateFailed = 1;
    public static final int b2_toiStateOverlapped = 2;
    public static final int b2_toiStateHit = 3;
    public static final int b2_toiStateSeparated = 4;
    private static final int B2_NULL_INDEX = -1;
    private static final int B2_TREE_STACK_SIZE = 1024;
    private static final int B2_ALLOCATED_NODE = 0x0001;
    private static final int B2_ENLARGED_NODE = 0x0002;
    private static final int B2_LEAF_NODE = 0x0004;
    private static final int B2_GRAPH_COLOR_COUNT = 12;
    private static final int B2_OVERFLOW_INDEX = B2_GRAPH_COLOR_COUNT - 1;
    private static final int B2_MAX_WORKERS = 64;
    public static final int b2_colorRed = 0xFF0000;
    public static final int b2_colorGreen = 0x008000;
    public static final int b2_colorBlue = 0x0000FF;
    public static final int b2_colorPink = 0xFFC0CB;
    public static final int b2_colorGold = 0xFFD700;
    public static final int b2_colorGray = 0x808080;
    public static final int b2_colorDimGray = 0x696969;
    public static final int b2_colorLightGray = 0xD3D3D3;
    public static final int b2_colorGainsboro = 0xDCDCDC;
    public static final int b2_colorSlateGray = 0x708090;
    public static final int b2_colorLightSlateGray = 0x778899;
    public static final int b2_colorSteelBlue = 0x4682B4;
    public static final int b2_colorLightSteelBlue = 0xB0C4DE;
    public static final int b2_colorDarkBlue = 0x00008B;
    public static final int b2_colorDarkCyan = 0x008B8B;
    public static final int b2_colorDarkSeaGreen = 0x8FBC8F;
    public static final int b2_colorDarkGoldenRod = 0xB8860B;
    public static final int b2_colorDarkGray = 0xA9A9A9;
    public static final int b2_colorDarkGreen = 0x006400;
    public static final int b2_colorDarkKhaki = 0xBDB76B;
    public static final int b2_colorDarkMagenta = 0x8B008B;
    public static final int b2_colorPaleGreen = 0x98FB98;
    public static final int b2_colorRoyalBlue = 0x4169E1;
    public static final int b2_colorTurquoise = 0x40E0D0;
    public static final int b2_colorWheat = 0xF5DEB3;
    public static final int b2_colorWhite = 0xFFFFFF;
    public static final int b2_colorBlueViolet = 0x8A2BE2;
    public static final int b2_colorCyan = 0x00FFFF;
    public static final int b2_colorViolet = 0xEE82EE;
    public static final int b2_colorChocolate = 0xD2691E;
    public static final int b2_colorGoldenRod = 0xDAA520;
    public static final int b2_colorSaddleBrown = 0x8B4513;
    public static final int b2_colorCoral = 0xFF7F50;
    public static final int b2_colorBox2DBlue = 0x30AEBF;
    public static final int b2_colorBox2DGreen = 0x8CC924;
    public static final int b2_colorBox2DYellow = 0xFFEE8C;
    public static final int b2_colorBlack = 0x000000;
    public static final int b2_colorYellow = 0xFFFF00;
    public static final int b2_colorLime = 0x00FF00;
    public static final int b2_colorMagenta = 0xFF00FF;
    public static final int b2_colorOrange = 0xFFA500;
    public static final int b2_colorSalmon = 0xFA8072;
    public static final int b2_colorOrangeRed = 0xFF4500;

    public static final b2Vec2 b2Vec2_zero = new b2Vec2(0.0f, 0.0f);
    public static final b2Rot b2Rot_identity = new b2Rot(1.0f, 0.0f);
    public static final b2Transform b2Transform_identity = new b2Transform(b2Vec2_zero, b2Rot_identity);
    public static final b2Mat22 b2Mat22_zero = new b2Mat22(new b2Vec2(), new b2Vec2());
    public static final b2WorldId b2_nullWorldId = new b2WorldId();
    public static final b2BodyId b2_nullBodyId = new b2BodyId();
    public static final b2ShapeId b2_nullShapeId = new b2ShapeId();
    public static final b2ChainId b2_nullChainId = new b2ChainId();
    public static final b2JointId b2_nullJointId = new b2JointId();

    private static final AtomicInteger BYTE_COUNT = new AtomicInteger();
    private static final WorldSlot[] WORLDS = new WorldSlot[B2_MAX_WORLDS];
    private static final int[] WORLD_GENERATIONS = new int[B2_MAX_WORLDS];
    private static volatile B2Allocator allocator = (size, alignment) -> ByteBuffer.allocateDirect(size);
    private static volatile B2AssertHandler assertHandler = (condition, fileName, lineNumber) -> {
        System.err.printf("BOX2D ASSERTION: %s, %s, line %d%n", condition, fileName, lineNumber);
        return 1;
    };
    private static volatile float lengthUnitsPerMeter = 1.0f;

    private static final class WorldSlot {
        final int index;
        int generation;
        b2WorldDef def;
        int workerCount;
        b2EnqueueTaskCallback enqueueTask;
        b2FinishTaskCallback finishTask;
        Object userTaskContext;
        final java.util.ArrayList<BodySlot> bodies = new java.util.ArrayList<>();
        final java.util.ArrayList<Integer> freeBodyIndices = new java.util.ArrayList<>();
        final java.util.ArrayList<BodySlot> solverBodyOrder = new java.util.ArrayList<>();
        final java.util.ArrayList<ShapeSlot> shapes = new java.util.ArrayList<>();
        final java.util.ArrayList<Integer> freeShapeIndices = new java.util.ArrayList<>();
        final java.util.ArrayList<ChainSlot> chains = new java.util.ArrayList<>();
        final java.util.ArrayList<Integer> freeChainIndices = new java.util.ArrayList<>();
        final java.util.ArrayList<JointSlot> joints = new java.util.ArrayList<>();
        final java.util.ArrayList<Integer> freeJointIndices = new java.util.ArrayList<>();
        final java.util.ArrayList<ContactSlot> contacts = new java.util.ArrayList<>();
        final java.util.ArrayList<Integer> freeContactIndices = new java.util.ArrayList<>();
        final java.util.ArrayList<ContactSlot> contactUpdateOrder = new java.util.ArrayList<>();
        final java.util.ArrayList<ContactSlot>[] contactGraphColors = b2CreateContactGraphColors();
        final java.util.ArrayList<JointSlot>[] jointGraphColors = b2CreateJointGraphColors();
        final java.util.HashSet<Integer>[] graphBodySets = b2CreateGraphBodySets();
        int nextContactIndex;
        int contactCount;
        boolean sleepEnabled;
        boolean continuousEnabled;
        boolean warmStartingEnabled = true;
        boolean speculativeEnabled = true;
        Object userData;
        b2Profile profile = new b2Profile();
        int stepCount;
        float invH;
        b2SensorEvents sensorEvents = new b2SensorEvents();
        b2ContactEvents contactEvents = new b2ContactEvents();
        b2BodyEvents bodyEvents = new b2BodyEvents();
        final java.util.HashMap<Integer, SensorOverlapState> sensorOverlaps = new java.util.HashMap<>();
        b2CustomFilterFcn customFilterFcn;
        Object customFilterContext;
        b2PreSolveFcn preSolveFcn;
        Object preSolveContext;
        final b2BroadPhase broadPhase = new b2BroadPhase();
        final java.util.HashMap<Integer, Integer> sleepIslandParents = new java.util.HashMap<>();
        final java.util.HashMap<Integer, java.util.ArrayList<ContactSlot>> sleepIslandContacts = new java.util.HashMap<>();
        final java.util.HashMap<Integer, java.util.ArrayList<JointSlot>> sleepIslandJoints = new java.util.HashMap<>();
        final java.util.HashSet<Integer> sleepingIslandRoots = new java.util.HashSet<>();
        final java.util.HashSet<Integer> sleepIslandConstraintRemoved = new java.util.HashSet<>();
        int nextSleepIslandId = 1;
        int splitSleepIslandId = B2_NULL_INDEX;

        WorldSlot(int index, int generation, b2WorldDef def) {
            this.index = index;
            this.generation = generation;
            this.def = def;
            this.sleepEnabled = def.enableSleep;
            this.continuousEnabled = def.enableContinuous;
            this.userData = def.userData;
            if (def.workerCount > 0 && def.enqueueTask != null && def.finishTask != null) {
                this.workerCount = b2MinInt(def.workerCount, B2_MAX_WORKERS);
                this.enqueueTask = def.enqueueTask;
                this.finishTask = def.finishTask;
                this.userTaskContext = def.userTaskContext;
            } else {
                this.workerCount = 1;
                this.enqueueTask = (task, itemCount, minRange, taskContext, userContext) -> {
                    if (itemCount > 0) {
                        task.invoke(0, itemCount, 0, taskContext);
                    }
                    return null;
                };
                this.finishTask = (taskHandle, userContext) -> {
                };
            }
            b2CreateBroadPhase(this.broadPhase);
        }
    }

    private static final class BodySlot {
        int index;
        int generation = 1;
        boolean alive = true;
        int worldIndex;
        int type;
        final java.util.ArrayList<ChainSlot> chains = new java.util.ArrayList<>();
        final java.util.ArrayList<JointSlot> joints = new java.util.ArrayList<>();
        final java.util.ArrayList<ContactSlot> contacts = new java.util.ArrayList<>();
        b2Vec2 position;
        b2Rot rotation;
        b2Vec2 linearVelocity;
        float angularVelocity;
        b2Vec2 force = new b2Vec2();
        float torque;
        float linearDamping;
        float angularDamping;
        float gravityScale;
        boolean enabled;
        boolean awake;
        boolean fixedRotation;
        boolean bullet;
        boolean allowFastRotation;
        boolean enableSleep;
        float sleepThreshold;
        float sleepTime;
        int sleepIslandId = B2_NULL_INDEX;
        String name = "";
        Object userData;
        float mass;
        float inertia;
        float invMass;
        float invInertia;
        b2Vec2 localCenter = new b2Vec2();
        b2Vec2 center = new b2Vec2();
        float minExtent = B2_HUGE();
        float maxExtent;
        final java.util.ArrayList<ShapeSlot> shapes = new java.util.ArrayList<>();
    }

    private static final class ShapeSlot {
        int index;
        int generation = 1;
        boolean alive = true;
        BodySlot body;
        b2ShapeDef def;
        int kind;
        b2Polygon polygon;
        b2Circle circle;
        b2Segment segment;
        b2Capsule capsule;
        b2ChainSegment chainSegment;
        b2Vec2 localCentroid = new b2Vec2();
        b2AABB aabb = new b2AABB();
        b2AABB fatAABB = new b2AABB();
        boolean enlargedAABB;
        int proxyKey = B2_NULL_INDEX;
    }

    private static final class ShapeRef {
        final int shapeIndex;
        final int generation;

        ShapeRef(int shapeIndex, int generation) {
            this.shapeIndex = shapeIndex;
            this.generation = generation;
        }
    }

    private static final class SensorOverlapState {
        final int sensorGeneration;
        final java.util.ArrayList<ShapeRef> refs;

        SensorOverlapState(int sensorGeneration, java.util.ArrayList<ShapeRef> refs) {
            this.sensorGeneration = sensorGeneration;
            this.refs = refs;
        }
    }

    private static final class ChainSlot {
        int index;
        int generation = 1;
        boolean alive = true;
        BodySlot body;
        int[] shapeIndices;
        b2SurfaceMaterial[] materials;
        int count;
    }

    private static final class JointSlot {
        int index;
        int generation = 1;
        boolean alive = true;
        int type;
        BodySlot bodyA;
        BodySlot bodyB;
        int colorIndex = B2_NULL_INDEX;
        int localIndex = B2_NULL_INDEX;
        int sleepIslandId = B2_NULL_INDEX;
        boolean collideConnected;
        Object userData;
        b2Vec2 localAnchorA = new b2Vec2();
        b2Vec2 localAnchorB = new b2Vec2();
        b2Vec2 localAxisA = new b2Vec2();
        float referenceAngle;
        float constraintHertz = 60.0f;
        float constraintDampingRatio = 2.0f;
        float distanceLength;
        boolean distanceEnableSpring;
        float distanceHertz;
        float distanceDampingRatio;
        boolean distanceEnableLimit;
        float distanceMinLength;
        float distanceMaxLength;
        boolean distanceEnableMotor;
        float distanceMaxMotorForce;
        float distanceMotorSpeed;
        float distanceImpulse;
        float distanceLowerImpulse;
        float distanceUpperImpulse;
        float distanceMotorImpulse;
        b2Vec2 motorLinearOffset = new b2Vec2();
        float motorAngularOffset;
        b2Vec2 motorLinearImpulse = new b2Vec2();
        float motorAngularImpulse;
        float motorMaxForce;
        float motorMaxTorque;
        float motorCorrectionFactor;
        b2Vec2 mouseTarget = new b2Vec2();
        float mouseHertz;
        float mouseDampingRatio;
        float mouseMaxForce;
        b2Vec2 mouseLinearImpulse = new b2Vec2();
        float mouseAngularImpulse;
        float weldLinearHertz;
        float weldLinearDampingRatio;
        float weldAngularHertz;
        float weldAngularDampingRatio;
        b2Vec2 weldLinearImpulse = new b2Vec2();
        float weldAngularImpulse;
        b2Vec2 revoluteLinearImpulse = new b2Vec2();
        float revoluteSpringImpulse;
        float revoluteMotorImpulse;
        float revoluteLowerImpulse;
        float revoluteUpperImpulse;
        float revoluteHertz;
        float revoluteDampingRatio;
        float revoluteTargetAngle;
        float revoluteMaxMotorTorque;
        float revoluteMotorSpeed;
        float revoluteLowerAngle;
        float revoluteUpperAngle;
        boolean revoluteEnableSpring;
        boolean revoluteEnableMotor;
        boolean revoluteEnableLimit;
        b2Vec2 prismaticImpulse = new b2Vec2();
        float prismaticSpringImpulse;
        float prismaticMotorImpulse;
        float prismaticLowerImpulse;
        float prismaticUpperImpulse;
        float prismaticHertz;
        float prismaticDampingRatio;
        float prismaticTargetTranslation;
        float prismaticMaxMotorForce;
        float prismaticMotorSpeed;
        float prismaticLowerTranslation;
        float prismaticUpperTranslation;
        boolean prismaticEnableSpring;
        boolean prismaticEnableLimit;
        boolean prismaticEnableMotor;
        float wheelPerpImpulse;
        float wheelMotorImpulse;
        float wheelSpringImpulse;
        float wheelLowerImpulse;
        float wheelUpperImpulse;
        float wheelMaxMotorTorque;
        float wheelMotorSpeed;
        float wheelLowerTranslation;
        float wheelUpperTranslation;
        float wheelHertz;
        float wheelDampingRatio;
        boolean wheelEnableSpring;
        boolean wheelEnableMotor;
        boolean wheelEnableLimit;
    }

    private static final class ContactSlot {
        int index;
        boolean alive = true;
        ShapeSlot shapeA;
        ShapeSlot shapeB;
        b2Manifold manifold = new b2Manifold();
        boolean touching;
        int sleepIslandId = B2_NULL_INDEX;
        int colorIndex = B2_NULL_INDEX;
        int localIndex = B2_NULL_INDEX;
    }

    private static final class SolverBodyState {
        BodySlot body;
        b2Vec2 linearVelocity = new b2Vec2();
        float angularVelocity;
        b2Vec2 deltaPosition = new b2Vec2();
        b2Rot deltaRotation = b2Rot_identity.copy();
    }

    private static final class BulletSweep {
        BodySlot body;
        b2Vec2 center1;
        b2Rot rotation1;
        b2Vec2 center2;
        b2Rot rotation2;
    }

    private static final class ContactConstraintPoint {
        b2Vec2 anchorA = new b2Vec2();
        b2Vec2 anchorB = new b2Vec2();
        float baseSeparation;
        float relativeVelocity;
        float normalImpulse;
        float tangentImpulse;
        float totalNormalImpulse;
        float normalMass;
        float tangentMass;
    }

    private static final class ContactConstraint {
        ContactSlot contact;
        int indexA = B2_NULL_INDEX;
        int indexB = B2_NULL_INDEX;
        ContactConstraintPoint[] points = {new ContactConstraintPoint(), new ContactConstraintPoint()};
        b2Vec2 normal = new b2Vec2();
        float invMassA;
        float invMassB;
        float invIA;
        float invIB;
        float friction;
        float restitution;
        float tangentSpeed;
        float rollingResistance;
        float rollingMass;
        float rollingImpulse;
        Softness softness;
        int pointCount;
    }

    private static final class ConstraintColor {
        final java.util.ArrayList<ContactConstraint> contacts = new java.util.ArrayList<>();
        final java.util.ArrayList<DistanceConstraint> distances = new java.util.ArrayList<>();
        final java.util.ArrayList<MotorConstraint> motors = new java.util.ArrayList<>();
        final java.util.ArrayList<MouseConstraint> mice = new java.util.ArrayList<>();
        final java.util.ArrayList<WeldConstraint> welds = new java.util.ArrayList<>();
        final java.util.ArrayList<RevoluteConstraint> revolutes = new java.util.ArrayList<>();
        final java.util.ArrayList<PrismaticConstraint> prismatics = new java.util.ArrayList<>();
        final java.util.ArrayList<WheelConstraint> wheels = new java.util.ArrayList<>();

        boolean active() {
            return !contacts.isEmpty() || !distances.isEmpty() || !motors.isEmpty() || !mice.isEmpty()
                || !welds.isEmpty() || !revolutes.isEmpty() || !prismatics.isEmpty() || !wheels.isEmpty();
        }
    }

    private static final class DistanceConstraint {
        JointSlot joint;
        int indexA = B2_NULL_INDEX;
        int indexB = B2_NULL_INDEX;
        float invMassA;
        float invMassB;
        float invIA;
        float invIB;
        b2Vec2 anchorA = new b2Vec2();
        b2Vec2 anchorB = new b2Vec2();
        b2Vec2 deltaCenter = new b2Vec2();
        Softness distanceSoftness;
        Softness constraintSoftness;
        float axialMass;
    }

    private static final class MotorConstraint {
        JointSlot joint;
        int indexA = B2_NULL_INDEX;
        int indexB = B2_NULL_INDEX;
        float invMassA;
        float invMassB;
        float invIA;
        float invIB;
        b2Vec2 anchorA = new b2Vec2();
        b2Vec2 anchorB = new b2Vec2();
        b2Vec2 deltaCenter = new b2Vec2();
        float deltaAngle;
        b2Mat22 linearMass = new b2Mat22();
        float angularMass;
    }

    private static final class MouseConstraint {
        JointSlot joint;
        int indexB = B2_NULL_INDEX;
        float invMassB;
        float invIB;
        b2Vec2 anchorB = new b2Vec2();
        b2Vec2 deltaCenter = new b2Vec2();
        b2Mat22 linearMass = new b2Mat22();
        Softness linearSoftness;
        Softness angularSoftness;
    }

    private static final class WeldConstraint {
        JointSlot joint;
        int indexA = B2_NULL_INDEX;
        int indexB = B2_NULL_INDEX;
        float invMassA;
        float invMassB;
        float invIA;
        float invIB;
        b2Vec2 anchorA = new b2Vec2();
        b2Vec2 anchorB = new b2Vec2();
        b2Vec2 deltaCenter = new b2Vec2();
        float deltaAngle;
        float axialMass;
        Softness linearSoftness;
        Softness angularSoftness;
    }

    private static final class RevoluteConstraint {
        JointSlot joint;
        int indexA = B2_NULL_INDEX;
        int indexB = B2_NULL_INDEX;
        float invMassA;
        float invMassB;
        float invIA;
        float invIB;
        b2Vec2 anchorA = new b2Vec2();
        b2Vec2 anchorB = new b2Vec2();
        b2Vec2 deltaCenter = new b2Vec2();
        float deltaAngle;
        float axialMass;
        Softness springSoftness;
        Softness constraintSoftness;
    }

    private static final class PrismaticConstraint {
        JointSlot joint;
        int indexA = B2_NULL_INDEX;
        int indexB = B2_NULL_INDEX;
        float invMassA;
        float invMassB;
        float invIA;
        float invIB;
        b2Vec2 anchorA = new b2Vec2();
        b2Vec2 anchorB = new b2Vec2();
        b2Vec2 axisA = new b2Vec2();
        b2Vec2 deltaCenter = new b2Vec2();
        float deltaAngle;
        float axialMass;
        Softness springSoftness;
        Softness constraintSoftness;
    }

    private static final class WheelConstraint {
        JointSlot joint;
        int indexA = B2_NULL_INDEX;
        int indexB = B2_NULL_INDEX;
        float invMassA;
        float invMassB;
        float invIA;
        float invIB;
        b2Vec2 anchorA = new b2Vec2();
        b2Vec2 anchorB = new b2Vec2();
        b2Vec2 axisA = new b2Vec2();
        b2Vec2 deltaCenter = new b2Vec2();
        float perpMass;
        float axialMass;
        float motorMass;
        Softness springSoftness;
        Softness constraintSoftness;
    }

    private static final class Softness {
        float biasRate;
        float massScale;
        float impulseScale;
    }

    private static final class b2SeparationFunction {
        static final int POINTS = 0;
        static final int FACE_A = 1;
        static final int FACE_B = 2;

        b2ShapeProxy proxyA;
        b2ShapeProxy proxyB;
        b2Sweep sweepA;
        b2Sweep sweepB;
        b2Vec2 localPoint = new b2Vec2();
        b2Vec2 axis = new b2Vec2();
        int type;
    }

    private B2() {
    }

    public static void b2SetAllocator(B2Allocator allocator) {
        B2.allocator = Objects.requireNonNull(allocator, "allocator");
    }

    public static void b2SetAllocator(IntFunction<ByteBuffer> allocator) {
        Objects.requireNonNull(allocator, "allocator");
        B2.allocator = (size, alignment) -> allocator.apply(size);
    }

    public static ByteBuffer b2Alloc(int size) {
        if (size == 0) {
            return null;
        }
        int size32 = ((size - 1) | 0x1F) + 1;
        BYTE_COUNT.addAndGet(size);
        ByteBuffer buffer = allocator.allocate(size32, 32);
        if (buffer == null) {
            throw new OutOfMemoryError("Box2D allocator returned null");
        }
        return buffer;
    }

    public static void b2Free(ByteBuffer buffer, int size) {
        if (buffer != null) {
            BYTE_COUNT.addAndGet(-size);
        }
    }

    public static int b2GetByteCount() {
        return BYTE_COUNT.get();
    }

    public static void b2SetAssertFcn(B2AssertHandler assertFcn) {
        assertHandler = Objects.requireNonNull(assertFcn, "assertFcn");
    }

    public static int b2InternalAssertFcn(String condition, String fileName, int lineNumber) {
        return assertHandler.assertFailed(condition, fileName, lineNumber);
    }

    public static void b2Assert(boolean condition, String expression) {
        if (!condition && b2InternalAssertFcn(expression, "", 0) != 0) {
            throw new AssertionError(expression);
        }
    }

    public static b2Version b2GetVersion() {
        return new b2Version(3, 1, 1);
    }

    public static b2WorldDef b2DefaultWorldDef() {
        b2WorldDef def = new b2WorldDef();
        def.gravity = new b2Vec2(0.0f, -10.0f);
        def.restitutionThreshold = 1.0f;
        def.hitEventThreshold = 1.0f;
        def.contactHertz = 30.0f;
        def.contactDampingRatio = 10.0f;
        def.maxContactPushSpeed = 3.0f;
        def.maximumLinearSpeed = 400.0f;
        def.enableSleep = true;
        def.enableContinuous = true;
        def.frictionCallback = null;
        def.restitutionCallback = null;
        def.workerCount = 1;
        def.internalValue = 0xFACEB00C;
        return def;
    }

    public static b2BodyDef b2DefaultBodyDef() {
        b2BodyDef def = new b2BodyDef();
        def.type = b2_staticBody;
        def.rotation = b2Rot_identity.copy();
        def.gravityScale = 1.0f;
        def.sleepThreshold = 0.05f;
        def.enableSleep = true;
        def.isAwake = true;
        def.isEnabled = true;
        def.internalValue = 0xB0D1DEF;
        return def;
    }

    public static b2Filter b2DefaultFilter() {
        b2Filter filter = new b2Filter();
        filter.categoryBits = B2_DEFAULT_CATEGORY_BITS;
        filter.maskBits = B2_DEFAULT_MASK_BITS;
        return filter;
    }

    public static b2QueryFilter b2DefaultQueryFilter() {
        b2QueryFilter filter = new b2QueryFilter();
        filter.categoryBits = B2_DEFAULT_CATEGORY_BITS;
        filter.maskBits = B2_DEFAULT_MASK_BITS;
        return filter;
    }

    public static b2SurfaceMaterial b2DefaultSurfaceMaterial() {
        b2SurfaceMaterial material = new b2SurfaceMaterial();
        material.friction = 0.6f;
        material.restitution = 0.0f;
        material.rollingResistance = 0.0f;
        material.tangentSpeed = 0.0f;
        return material;
    }

    public static b2ShapeDef b2DefaultShapeDef() {
        b2ShapeDef def = new b2ShapeDef();
        def.density = 1.0f;
        def.filter = b2DefaultFilter();
        def.material = b2DefaultSurfaceMaterial();
        def.invokeContactCreation = true;
        def.updateBodyMass = true;
        def.internalValue = 0x5A9EDEF;
        return def;
    }

    public static b2ChainDef b2DefaultChainDef() {
        b2ChainDef def = new b2ChainDef();
        def.materials = new b2SurfaceMaterial[] {b2DefaultSurfaceMaterial()};
        def.materialCount = 1;
        def.filter = b2DefaultFilter();
        def.internalValue = 0xC1A1DEF;
        return def;
    }

    public static b2DistanceJointDef b2DefaultDistanceJointDef() {
        b2DistanceJointDef def = new b2DistanceJointDef();
        def.length = 1.0f;
        def.maxLength = B2_HUGE();
        def.internalValue = 1152023;
        return def;
    }

    public static b2MotorJointDef b2DefaultMotorJointDef() {
        b2MotorJointDef def = new b2MotorJointDef();
        def.maxForce = 1.0f;
        def.maxTorque = 1.0f;
        def.correctionFactor = 0.3f;
        def.internalValue = 1152023;
        return def;
    }

    public static b2MouseJointDef b2DefaultMouseJointDef() {
        b2MouseJointDef def = new b2MouseJointDef();
        def.hertz = 4.0f;
        def.dampingRatio = 1.0f;
        def.maxForce = 1.0f;
        def.internalValue = 1152023;
        return def;
    }

    public static b2FilterJointDef b2DefaultFilterJointDef() {
        b2FilterJointDef def = new b2FilterJointDef();
        def.internalValue = 1152023;
        return def;
    }

    public static b2PrismaticJointDef b2DefaultPrismaticJointDef() {
        b2PrismaticJointDef def = new b2PrismaticJointDef();
        def.localAxisA = new b2Vec2(1.0f, 0.0f);
        def.internalValue = 1152023;
        return def;
    }

    public static b2RevoluteJointDef b2DefaultRevoluteJointDef() {
        b2RevoluteJointDef def = new b2RevoluteJointDef();
        def.drawSize = 0.25f;
        def.internalValue = 1152023;
        return def;
    }

    public static b2WeldJointDef b2DefaultWeldJointDef() {
        b2WeldJointDef def = new b2WeldJointDef();
        def.internalValue = 1152023;
        return def;
    }

    public static b2WheelJointDef b2DefaultWheelJointDef() {
        b2WheelJointDef def = new b2WheelJointDef();
        def.localAxisA = new b2Vec2(0.0f, 1.0f);
        def.enableSpring = true;
        def.hertz = 1.0f;
        def.dampingRatio = 0.7f;
        def.internalValue = 1152023;
        return def;
    }

    public static b2ExplosionDef b2DefaultExplosionDef() {
        b2ExplosionDef def = new b2ExplosionDef();
        def.maskBits = B2_DEFAULT_MASK_BITS;
        def.radius = 1.0f;
        def.falloff = 1.0f;
        return def;
    }

    public static b2DebugDraw b2DefaultDebugDraw() {
        return new b2DebugDraw();
    }

    public static boolean B2_IS_NULL(b2WorldId id) {
        return id.index1 == 0;
    }

    public static boolean B2_IS_NULL(b2BodyId id) {
        return id.index1 == 0;
    }

    public static boolean B2_IS_NULL(b2ShapeId id) {
        return id.index1 == 0;
    }

    public static boolean B2_IS_NULL(b2ChainId id) {
        return id.index1 == 0;
    }

    public static boolean B2_IS_NULL(b2JointId id) {
        return id.index1 == 0;
    }

    public static boolean B2_ID_EQUALS(b2BodyId id1, b2BodyId id2) {
        return id1.index1 == id2.index1 && id1.world0 == id2.world0 && id1.generation == id2.generation;
    }

    public static boolean B2_ID_EQUALS(b2ShapeId id1, b2ShapeId id2) {
        return id1.index1 == id2.index1 && id1.world0 == id2.world0 && id1.generation == id2.generation;
    }

    public static boolean B2_ID_EQUALS(b2ChainId id1, b2ChainId id2) {
        return id1.index1 == id2.index1 && id1.world0 == id2.world0 && id1.generation == id2.generation;
    }

    public static boolean B2_ID_EQUALS(b2JointId id1, b2JointId id2) {
        return id1.index1 == id2.index1 && id1.world0 == id2.world0 && id1.generation == id2.generation;
    }

    public static int b2StoreWorldId(b2WorldId id) {
        return ((id.index1 & 0xFFFF) << 16) | (id.generation & 0xFFFF);
    }

    public static b2WorldId b2LoadWorldId(int x) {
        return new b2WorldId((x >>> 16) & 0xFFFF, x & 0xFFFF);
    }

    public static long b2StoreBodyId(b2BodyId id) {
        return ((long) id.index1 << 32) | ((long) (id.world0 & 0xFFFF) << 16) | (long) (id.generation & 0xFFFF);
    }

    public static b2BodyId b2LoadBodyId(long x) {
        return new b2BodyId((int) (x >>> 32), (int) (x >>> 16) & 0xFFFF, (int) x & 0xFFFF);
    }

    public static long b2StoreShapeId(b2ShapeId id) {
        return ((long) id.index1 << 32) | ((long) (id.world0 & 0xFFFF) << 16) | (long) (id.generation & 0xFFFF);
    }

    public static b2ShapeId b2LoadShapeId(long x) {
        return new b2ShapeId((int) (x >>> 32), (int) (x >>> 16) & 0xFFFF, (int) x & 0xFFFF);
    }

    public static long b2StoreChainId(b2ChainId id) {
        return ((long) id.index1 << 32) | ((long) (id.world0 & 0xFFFF) << 16) | (long) (id.generation & 0xFFFF);
    }

    public static b2ChainId b2LoadChainId(long x) {
        return new b2ChainId((int) (x >>> 32), (int) (x >>> 16) & 0xFFFF, (int) x & 0xFFFF);
    }

    public static long b2StoreJointId(b2JointId id) {
        return ((long) id.index1 << 32) | ((long) (id.world0 & 0xFFFF) << 16) | (long) (id.generation & 0xFFFF);
    }

    public static b2JointId b2LoadJointId(long x) {
        return new b2JointId((int) (x >>> 32), (int) (x >>> 16) & 0xFFFF, (int) x & 0xFFFF);
    }

    private static int b2AllocSlot(java.util.ArrayList<Integer> freeIndices, int nextIndex) {
        int freeCount = freeIndices.size();
        return freeCount > 0 ? freeIndices.remove(freeCount - 1) : nextIndex;
    }

    private static <T> void b2StoreSlot(java.util.ArrayList<T> slots, int index, T value) {
        if (index == slots.size()) {
            slots.add(value);
        } else {
            slots.set(index, value);
        }
    }

    private static <T> void b2RemoveSwap(java.util.ArrayList<T> values, T value) {
        int index = values.indexOf(value);
        if (index == -1) {
            return;
        }
        T last = values.remove(values.size() - 1);
        if (index < values.size()) {
            values.set(index, last);
        }
    }

    public static long b2GetTicks() {
        return System.nanoTime();
    }

    public static float b2GetMilliseconds(long ticks) {
        return (System.nanoTime() - ticks) / 1_000_000.0f;
    }

    public static float b2GetMillisecondsAndReset(long[] ticks) {
        long now = System.nanoTime();
        float ms = (now - ticks[0]) / 1_000_000.0f;
        ticks[0] = now;
        return ms;
    }

    public static void b2Yield() {
        Thread.yield();
    }

    public static int b2Hash(int hash, byte[] data, int count) {
        for (int i = 0; i < count; ++i) {
            hash = ((hash << 5) + hash) + (data[i] & 0xFF);
        }
        return hash;
    }

    public static int b2CTZ32(int block) {
        return Integer.numberOfTrailingZeros(block);
    }

    public static int b2CLZ32(int value) {
        return Integer.numberOfLeadingZeros(value);
    }

    public static int b2CTZ64(long block) {
        return Long.numberOfTrailingZeros(block);
    }

    public static boolean b2IsPowerOf2(int x) {
        return (x & (x - 1)) == 0;
    }

    public static int b2BoundingPowerOf2(int x) {
        if (x <= 1) {
            return 1;
        }
        return 32 - b2CLZ32(x - 1);
    }

    public static int b2RoundUpPowerOf2(int x) {
        if (x <= 1) {
            return 1;
        }
        return 1 << (32 - b2CLZ32(x - 1));
    }

    public static b2BitSet b2CreateBitSet(int bitCapacity) {
        b2BitSet bitSet = new b2BitSet();
        bitSet.blockCapacity = (bitCapacity + Long.SIZE - 1) / Long.SIZE;
        bitSet.blockCount = 0;
        bitSet.bits = new long[bitSet.blockCapacity];
        return bitSet;
    }

    public static void b2DestroyBitSet(b2BitSet bitSet) {
        bitSet.bits = null;
        bitSet.blockCapacity = 0;
        bitSet.blockCount = 0;
    }

    public static void b2SetBitCountAndClear(b2BitSet bitSet, int bitCount) {
        int blockCount = (bitCount + Long.SIZE - 1) / Long.SIZE;
        if (bitSet.blockCapacity < blockCount) {
            int newBitCapacity = bitCount + (bitCount >> 1);
            b2BitSet newSet = b2CreateBitSet(newBitCapacity);
            bitSet.bits = newSet.bits;
            bitSet.blockCapacity = newSet.blockCapacity;
        }
        bitSet.blockCount = blockCount;
        for (int i = 0; i < blockCount; ++i) {
            bitSet.bits[i] = 0L;
        }
    }

    public static void b2GrowBitSet(b2BitSet bitSet, int blockCount) {
        b2Assert(blockCount > bitSet.blockCount, "blockCount > bitSet.blockCount");
        if (blockCount > bitSet.blockCapacity) {
            int oldCapacity = bitSet.blockCapacity;
            int newCapacity = blockCount + blockCount / 2;
            long[] newBits = new long[newCapacity];
            System.arraycopy(bitSet.bits, 0, newBits, 0, oldCapacity);
            bitSet.bits = newBits;
            bitSet.blockCapacity = newCapacity;
        }
        bitSet.blockCount = blockCount;
    }

    public static void b2InPlaceUnion(b2BitSet setA, b2BitSet setB) {
        b2Assert(setA.blockCount == setB.blockCount, "setA.blockCount == setB.blockCount");
        for (int i = 0; i < setA.blockCount; ++i) {
            setA.bits[i] |= setB.bits[i];
        }
    }

    public static void b2SetBit(b2BitSet bitSet, int bitIndex) {
        int blockIndex = bitIndex / Long.SIZE;
        b2Assert(blockIndex < bitSet.blockCount, "blockIndex < bitSet.blockCount");
        bitSet.bits[blockIndex] |= 1L << (bitIndex % Long.SIZE);
    }

    public static void b2SetBitGrow(b2BitSet bitSet, int bitIndex) {
        int blockIndex = bitIndex / Long.SIZE;
        if (blockIndex >= bitSet.blockCount) {
            b2GrowBitSet(bitSet, blockIndex + 1);
        }
        bitSet.bits[blockIndex] |= 1L << (bitIndex % Long.SIZE);
    }

    public static void b2ClearBit(b2BitSet bitSet, int bitIndex) {
        int blockIndex = bitIndex / Long.SIZE;
        if (blockIndex >= bitSet.blockCount) {
            return;
        }
        bitSet.bits[blockIndex] &= ~(1L << (bitIndex % Long.SIZE));
    }

    public static boolean b2GetBit(b2BitSet bitSet, int bitIndex) {
        int blockIndex = bitIndex / Long.SIZE;
        if (blockIndex >= bitSet.blockCount) {
            return false;
        }
        return (bitSet.bits[blockIndex] & (1L << (bitIndex % Long.SIZE))) != 0L;
    }

    public static int b2GetBitSetBytes(b2BitSet bitSet) {
        return bitSet.blockCapacity * Long.BYTES;
    }

    public static long B2_SHAPE_PAIR_KEY(int k1, int k2) {
        long a = k1 & 0xFFFFFFFFL;
        long b = k2 & 0xFFFFFFFFL;
        return k1 < k2 ? (a << 32) | b : (b << 32) | a;
    }

    public static int B2_PROXY_TYPE(int key) {
        return key & 3;
    }

    public static int B2_PROXY_ID(int key) {
        return key >> 2;
    }

    public static int B2_PROXY_KEY(int id, int type) {
        return (id << 2) | type;
    }

    public static b2HashSet b2CreateSet(int capacity) {
        b2HashSet set = new b2HashSet();
        set.capacity = capacity > 16 ? b2RoundUpPowerOf2(capacity) : 16;
        set.count = 0;
        set.items = new b2SetItem[set.capacity];
        for (int i = 0; i < set.items.length; ++i) {
            set.items[i] = new b2SetItem();
        }
        return set;
    }

    public static void b2DestroySet(b2HashSet set) {
        set.items = null;
        set.count = 0;
        set.capacity = 0;
    }

    public static void b2ClearSet(b2HashSet set) {
        set.count = 0;
        for (b2SetItem item : set.items) {
            item.key = 0L;
            item.hash = 0;
        }
    }

    private static int b2KeyHash(long key) {
        long h = key;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (int) h;
    }

    private static int b2FindSlot(b2HashSet set, long key, int hash) {
        int capacity = set.capacity;
        int index = hash & (capacity - 1);
        while (set.items[index].hash != 0 && set.items[index].key != key) {
            index = (index + 1) & (capacity - 1);
        }
        return index;
    }

    private static void b2AddKeyHaveCapacity(b2HashSet set, long key, int hash) {
        int index = b2FindSlot(set, key, hash);
        b2Assert(set.items[index].hash == 0, "items[index].hash == 0");
        set.items[index].key = key;
        set.items[index].hash = hash;
        set.count += 1;
    }

    private static void b2GrowTable(b2HashSet set) {
        int oldCount = set.count;
        int oldCapacity = set.capacity;
        b2SetItem[] oldItems = set.items;
        set.count = 0;
        set.capacity = 2 * oldCapacity;
        set.items = new b2SetItem[set.capacity];
        for (int i = 0; i < set.items.length; ++i) {
            set.items[i] = new b2SetItem();
        }
        for (int i = 0; i < oldCapacity; ++i) {
            b2SetItem item = oldItems[i];
            if (item.hash != 0) {
                b2AddKeyHaveCapacity(set, item.key, item.hash);
            }
        }
        b2Assert(set.count == oldCount, "set.count == oldCount");
    }

    public static boolean b2ContainsKey(b2HashSet set, long key) {
        b2Assert(key != 0L, "key != 0");
        int hash = b2KeyHash(key);
        int index = b2FindSlot(set, key, hash);
        return set.items[index].key == key;
    }

    public static int b2GetHashSetBytes(b2HashSet set) {
        return set.capacity * 16;
    }

    public static boolean b2AddKey(b2HashSet set, long key) {
        b2Assert(key != 0L, "key != 0");
        int hash = b2KeyHash(key);
        b2Assert(hash != 0, "hash != 0");
        int index = b2FindSlot(set, key, hash);
        if (set.items[index].hash != 0) {
            b2Assert(set.items[index].hash == hash && set.items[index].key == key, "key already present");
            return true;
        }
        if (2 * set.count >= set.capacity) {
            b2GrowTable(set);
        }
        b2AddKeyHaveCapacity(set, key, hash);
        return false;
    }

    public static boolean b2RemoveKey(b2HashSet set, long key) {
        int hash = b2KeyHash(key);
        int i = b2FindSlot(set, key, hash);
        if (set.items[i].hash == 0) {
            return false;
        }

        set.items[i].key = 0L;
        set.items[i].hash = 0;
        b2Assert(set.count > 0, "set.count > 0");
        set.count -= 1;

        int j = i;
        int capacity = set.capacity;
        for (;;) {
            j = (j + 1) & (capacity - 1);
            if (set.items[j].hash == 0) {
                break;
            }

            int k = set.items[j].hash & (capacity - 1);
            if (i <= j) {
                if (i < k && k <= j) {
                    continue;
                }
            } else if (i < k || k <= j) {
                continue;
            }

            set.items[i] = new b2SetItem(set.items[j]);
            set.items[j].key = 0L;
            set.items[j].hash = 0;
            i = j;
        }

        return true;
    }

    public static boolean b2IsValidFloat(float a) {
        return !Float.isNaN(a) && !Float.isInfinite(a);
    }

    public static boolean b2IsValidVec2(b2Vec2 v) {
        return b2IsValidFloat(v.x) && b2IsValidFloat(v.y);
    }

    public static boolean b2IsValidRotation(b2Rot q) {
        return b2IsValidFloat(q.s) && b2IsValidFloat(q.c) && b2IsNormalizedRot(q);
    }

    public static boolean b2IsValidAABB(b2AABB aabb) {
        return b2IsValidVec2(aabb.lowerBound)
            && b2IsValidVec2(aabb.upperBound)
            && aabb.upperBound.x >= aabb.lowerBound.x
            && aabb.upperBound.y >= aabb.lowerBound.y;
    }

    public static boolean b2IsValidPlane(b2Plane a) {
        return b2IsValidVec2(a.normal) && b2IsNormalized(a.normal) && b2IsValidFloat(a.offset);
    }

    public static int b2MinInt(int a, int b) {
        return a < b ? a : b;
    }

    public static int b2MaxInt(int a, int b) {
        return a > b ? a : b;
    }

    public static int b2AbsInt(int a) {
        return a < 0 ? -a : a;
    }

    public static int b2ClampInt(int a, int lower, int upper) {
        return a < lower ? lower : (a > upper ? upper : a);
    }

    public static float b2MinFloat(float a, float b) {
        return a < b ? a : b;
    }

    public static float b2MaxFloat(float a, float b) {
        return a > b ? a : b;
    }

    public static float b2AbsFloat(float a) {
        return a < 0.0f ? -a : a;
    }

    public static float b2ClampFloat(float a, float lower, float upper) {
        return a < lower ? lower : (a > upper ? upper : a);
    }

    public static float b2Atan2(float y, float x) {
        if (x == 0.0f && y == 0.0f) {
            return 0.0f;
        }

        float ax = b2AbsFloat(x);
        float ay = b2AbsFloat(y);
        float mx = b2MaxFloat(ay, ax);
        float mn = b2MinFloat(ay, ax);
        float a = mn / mx;

        float s = a * a;
        float c = s * a;
        float q = s * s;
        float r = 0.024840285f * q + 0.18681418f;
        float t = -0.094097948f * q - 0.33213072f;
        r = r * s + t;
        r = r * c + a;

        if (ay > ax) {
            r = 1.57079637f - r;
        }
        if (x < 0.0f) {
            r = 3.14159274f - r;
        }
        if (y < 0.0f) {
            r = -r;
        }
        return r;
    }

    public static b2CosSin b2ComputeCosSin(float radians) {
        float x = b2UnwindAngle(radians);
        float pi2 = B2_PI * B2_PI;

        float c;
        if (x < -0.5f * B2_PI) {
            float y = x + B2_PI;
            float y2 = y * y;
            c = -(pi2 - 4.0f * y2) / (pi2 + y2);
        } else if (x > 0.5f * B2_PI) {
            float y = x - B2_PI;
            float y2 = y * y;
            c = -(pi2 - 4.0f * y2) / (pi2 + y2);
        } else {
            float y2 = x * x;
            c = (pi2 - 4.0f * y2) / (pi2 + y2);
        }

        float s;
        if (x < 0.0f) {
            float y = x + B2_PI;
            s = -16.0f * y * (B2_PI - y) / (5.0f * pi2 - 4.0f * y * (B2_PI - y));
        } else {
            s = 16.0f * x * (B2_PI - x) / (5.0f * pi2 - 4.0f * x * (B2_PI - x));
        }

        float mag = (float) Math.sqrt(s * s + c * c);
        float invMag = mag > 0.0f ? 1.0f / mag : 0.0f;
        return new b2CosSin(c * invMag, s * invMag);
    }

    public static float b2Dot(b2Vec2 a, b2Vec2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static float b2Cross(b2Vec2 a, b2Vec2 b) {
        return a.x * b.y - a.y * b.x;
    }

    public static b2Vec2 b2CrossVS(b2Vec2 v, float s) {
        return new b2Vec2(s * v.y, -s * v.x);
    }

    public static b2Vec2 b2CrossSV(float s, b2Vec2 v) {
        return new b2Vec2(-s * v.y, s * v.x);
    }

    public static b2Vec2 b2LeftPerp(b2Vec2 v) {
        return new b2Vec2(-v.y, v.x);
    }

    public static b2Vec2 b2RightPerp(b2Vec2 v) {
        return new b2Vec2(v.y, -v.x);
    }

    public static b2Vec2 b2Add(b2Vec2 a, b2Vec2 b) {
        return new b2Vec2(a.x + b.x, a.y + b.y);
    }

    public static b2Vec2 b2Sub(b2Vec2 a, b2Vec2 b) {
        return new b2Vec2(a.x - b.x, a.y - b.y);
    }

    public static b2Vec2 b2Neg(b2Vec2 a) {
        return new b2Vec2(-a.x, -a.y);
    }

    public static b2Vec2 b2Lerp(b2Vec2 a, b2Vec2 b, float t) {
        return new b2Vec2((1.0f - t) * a.x + t * b.x, (1.0f - t) * a.y + t * b.y);
    }

    public static b2Vec2 b2Mul(b2Vec2 a, b2Vec2 b) {
        return new b2Vec2(a.x * b.x, a.y * b.y);
    }

    public static b2Vec2 b2MulSV(float s, b2Vec2 v) {
        return new b2Vec2(s * v.x, s * v.y);
    }

    public static b2Vec2 b2MulAdd(b2Vec2 a, float s, b2Vec2 b) {
        return new b2Vec2(a.x + s * b.x, a.y + s * b.y);
    }

    public static b2Vec2 b2MulSub(b2Vec2 a, float s, b2Vec2 b) {
        return new b2Vec2(a.x - s * b.x, a.y - s * b.y);
    }

    public static b2Vec2 b2Abs(b2Vec2 a) {
        return new b2Vec2(b2AbsFloat(a.x), b2AbsFloat(a.y));
    }

    public static b2Vec2 b2Min(b2Vec2 a, b2Vec2 b) {
        return new b2Vec2(b2MinFloat(a.x, b.x), b2MinFloat(a.y, b.y));
    }

    public static b2Vec2 b2Max(b2Vec2 a, b2Vec2 b) {
        return new b2Vec2(b2MaxFloat(a.x, b.x), b2MaxFloat(a.y, b.y));
    }

    public static b2Vec2 b2Clamp(b2Vec2 v, b2Vec2 a, b2Vec2 b) {
        return new b2Vec2(b2ClampFloat(v.x, a.x, b.x), b2ClampFloat(v.y, a.y, b.y));
    }

    public static float b2Length(b2Vec2 v) {
        return (float) Math.sqrt(v.x * v.x + v.y * v.y);
    }

    public static float b2Distance(b2Vec2 a, b2Vec2 b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static b2Vec2 b2Normalize(b2Vec2 v) {
        float length = (float) Math.sqrt(v.x * v.x + v.y * v.y);
        if (length < Math.ulp(1.0f)) {
            return new b2Vec2(0.0f, 0.0f);
        }
        float invLength = 1.0f / length;
        return new b2Vec2(invLength * v.x, invLength * v.y);
    }

    public static boolean b2IsNormalized(b2Vec2 a) {
        float aa = b2Dot(a, a);
        return b2AbsFloat(1.0f - aa) < 100.0f * Math.ulp(1.0f);
    }

    public static b2Vec2 b2GetLengthAndNormalize(float[] length, b2Vec2 v) {
        length[0] = (float) Math.sqrt(v.x * v.x + v.y * v.y);
        if (length[0] < Math.ulp(1.0f)) {
            return new b2Vec2(0.0f, 0.0f);
        }
        float invLength = 1.0f / length[0];
        return new b2Vec2(invLength * v.x, invLength * v.y);
    }

    public static b2Rot b2NormalizeRot(b2Rot q) {
        float mag = (float) Math.sqrt(q.s * q.s + q.c * q.c);
        float invMag = mag > 0.0f ? 1.0f / mag : 0.0f;
        return new b2Rot(q.c * invMag, q.s * invMag);
    }

    public static b2Rot b2IntegrateRotation(b2Rot q1, float deltaAngle) {
        b2Rot q2 = new b2Rot(q1.c - deltaAngle * q1.s, q1.s + deltaAngle * q1.c);
        float mag = (float) Math.sqrt(q2.s * q2.s + q2.c * q2.c);
        float invMag = mag > 0.0f ? 1.0f / mag : 0.0f;
        return new b2Rot(q2.c * invMag, q2.s * invMag);
    }

    public static float b2LengthSquared(b2Vec2 v) {
        return v.x * v.x + v.y * v.y;
    }

    public static float b2DistanceSquared(b2Vec2 a, b2Vec2 b) {
        float cx = b.x - a.x;
        float cy = b.y - a.y;
        return cx * cx + cy * cy;
    }

    public static b2Rot b2MakeRot(float radians) {
        b2CosSin cs = b2ComputeCosSin(radians);
        return new b2Rot(cs.cosine, cs.sine);
    }

    public static b2Rot b2ComputeRotationBetweenUnitVectors(b2Vec2 v1, b2Vec2 v2) {
        b2Assert(b2AbsFloat(1.0f - b2Length(v1)) < 100.0f * Math.ulp(1.0f), "v1 is unit");
        b2Assert(b2AbsFloat(1.0f - b2Length(v2)) < 100.0f * Math.ulp(1.0f), "v2 is unit");
        return b2NormalizeRot(new b2Rot(b2Dot(v1, v2), b2Cross(v1, v2)));
    }

    public static boolean b2IsNormalizedRot(b2Rot q) {
        float qq = q.s * q.s + q.c * q.c;
        return 1.0f - 0.0006f < qq && qq < 1.0f + 0.0006f;
    }

    public static b2Rot b2NLerp(b2Rot q1, b2Rot q2, float t) {
        float omt = 1.0f - t;
        b2Rot q = new b2Rot(omt * q1.c + t * q2.c, omt * q1.s + t * q2.s);
        float mag = (float) Math.sqrt(q.s * q.s + q.c * q.c);
        float invMag = mag > 0.0f ? 1.0f / mag : 0.0f;
        return new b2Rot(q.c * invMag, q.s * invMag);
    }

    public static float b2ComputeAngularVelocity(b2Rot q1, b2Rot q2, float inv_h) {
        return inv_h * (q2.s * q1.c - q2.c * q1.s);
    }

    public static float b2Rot_GetAngle(b2Rot q) {
        return b2Atan2(q.s, q.c);
    }

    public static b2Vec2 b2Rot_GetXAxis(b2Rot q) {
        return new b2Vec2(q.c, q.s);
    }

    public static b2Vec2 b2Rot_GetYAxis(b2Rot q) {
        return new b2Vec2(-q.s, q.c);
    }

    public static b2Rot b2MulRot(b2Rot q, b2Rot r) {
        return new b2Rot(q.c * r.c - q.s * r.s, q.s * r.c + q.c * r.s);
    }

    public static b2Rot b2InvMulRot(b2Rot q, b2Rot r) {
        return new b2Rot(q.c * r.c + q.s * r.s, q.c * r.s - q.s * r.c);
    }

    public static float b2RelativeAngle(b2Rot b, b2Rot a) {
        float s = b.s * a.c - b.c * a.s;
        float c = b.c * a.c + b.s * a.s;
        return b2Atan2(s, c);
    }

    public static float b2UnwindAngle(float radians) {
        return (float) Math.IEEEremainder(radians, 2.0f * B2_PI);
    }

    public static b2Vec2 b2RotateVector(b2Rot q, b2Vec2 v) {
        return new b2Vec2(q.c * v.x - q.s * v.y, q.s * v.x + q.c * v.y);
    }

    public static b2Vec2 b2InvRotateVector(b2Rot q, b2Vec2 v) {
        return new b2Vec2(q.c * v.x + q.s * v.y, -q.s * v.x + q.c * v.y);
    }

    public static b2Vec2 b2TransformPoint(b2Transform t, b2Vec2 p) {
        float x = (t.q.c * p.x - t.q.s * p.y) + t.p.x;
        float y = (t.q.s * p.x + t.q.c * p.y) + t.p.y;
        return new b2Vec2(x, y);
    }

    public static b2Vec2 b2InvTransformPoint(b2Transform t, b2Vec2 p) {
        float vx = p.x - t.p.x;
        float vy = p.y - t.p.y;
        return new b2Vec2(t.q.c * vx + t.q.s * vy, -t.q.s * vx + t.q.c * vy);
    }

    public static b2Transform b2MulTransforms(b2Transform A, b2Transform B) {
        b2Transform C = new b2Transform();
        C.q = b2MulRot(A.q, B.q);
        C.p = b2Add(b2RotateVector(A.q, B.p), A.p);
        return C;
    }

    public static b2Transform b2InvMulTransforms(b2Transform A, b2Transform B) {
        b2Transform C = new b2Transform();
        C.q = b2InvMulRot(A.q, B.q);
        C.p = b2InvRotateVector(A.q, b2Sub(B.p, A.p));
        return C;
    }

    public static b2Vec2 b2MulMV(b2Mat22 A, b2Vec2 v) {
        return new b2Vec2(A.cx.x * v.x + A.cy.x * v.y, A.cx.y * v.x + A.cy.y * v.y);
    }

    public static b2Mat22 b2GetInverse22(b2Mat22 A) {
        float a = A.cx.x;
        float b = A.cy.x;
        float c = A.cx.y;
        float d = A.cy.y;
        float det = a * d - b * c;
        if (det != 0.0f) {
            det = 1.0f / det;
        }
        return new b2Mat22(new b2Vec2(det * d, -det * c), new b2Vec2(-det * b, det * a));
    }

    public static b2Vec2 b2Solve22(b2Mat22 A, b2Vec2 b) {
        float a11 = A.cx.x;
        float a12 = A.cy.x;
        float a21 = A.cx.y;
        float a22 = A.cy.y;
        float det = a11 * a22 - a12 * a21;
        if (det != 0.0f) {
            det = 1.0f / det;
        }
        return new b2Vec2(det * (a22 * b.x - a12 * b.y), det * (a11 * b.y - a21 * b.x));
    }

    public static boolean b2AABB_Contains(b2AABB a, b2AABB b) {
        boolean s = true;
        s = s && a.lowerBound.x <= b.lowerBound.x;
        s = s && a.lowerBound.y <= b.lowerBound.y;
        s = s && b.upperBound.x <= a.upperBound.x;
        s = s && b.upperBound.y <= a.upperBound.y;
        return s;
    }

    public static b2Vec2 b2AABB_Center(b2AABB a) {
        return new b2Vec2(0.5f * (a.lowerBound.x + a.upperBound.x), 0.5f * (a.lowerBound.y + a.upperBound.y));
    }

    public static b2Vec2 b2AABB_Extents(b2AABB a) {
        return new b2Vec2(0.5f * (a.upperBound.x - a.lowerBound.x), 0.5f * (a.upperBound.y - a.lowerBound.y));
    }

    public static b2AABB b2AABB_Union(b2AABB a, b2AABB b) {
        b2AABB c = new b2AABB();
        c.lowerBound.x = b2MinFloat(a.lowerBound.x, b.lowerBound.x);
        c.lowerBound.y = b2MinFloat(a.lowerBound.y, b.lowerBound.y);
        c.upperBound.x = b2MaxFloat(a.upperBound.x, b.upperBound.x);
        c.upperBound.y = b2MaxFloat(a.upperBound.y, b.upperBound.y);
        return c;
    }

    public static boolean b2AABB_Overlaps(b2AABB a, b2AABB b) {
        return !(b.lowerBound.x > a.upperBound.x || b.lowerBound.y > a.upperBound.y
            || a.lowerBound.x > b.upperBound.x || a.lowerBound.y > b.upperBound.y);
    }

    public static b2AABB b2MakeAABB(b2Vec2[] points, int count, float radius) {
        b2Assert(count > 0, "count > 0");
        b2AABB a = new b2AABB(points[0], points[0]);
        for (int i = 1; i < count; ++i) {
            a.lowerBound = b2Min(a.lowerBound, points[i]);
            a.upperBound = b2Max(a.upperBound, points[i]);
        }
        b2Vec2 r = new b2Vec2(radius, radius);
        a.lowerBound = b2Sub(a.lowerBound, r);
        a.upperBound = b2Add(a.upperBound, r);
        return a;
    }

    public static float b2Perimeter(b2AABB a) {
        float wx = a.upperBound.x - a.lowerBound.x;
        float wy = a.upperBound.y - a.lowerBound.y;
        return 2.0f * (wx + wy);
    }

    public static boolean b2EnlargeAABB(b2AABB a, b2AABB b) {
        boolean changed = false;
        if (b.lowerBound.x < a.lowerBound.x) {
            a.lowerBound.x = b.lowerBound.x;
            changed = true;
        }
        if (b.lowerBound.y < a.lowerBound.y) {
            a.lowerBound.y = b.lowerBound.y;
            changed = true;
        }
        if (a.upperBound.x < b.upperBound.x) {
            a.upperBound.x = b.upperBound.x;
            changed = true;
        }
        if (a.upperBound.y < b.upperBound.y) {
            a.upperBound.y = b.upperBound.y;
            changed = true;
        }
        return changed;
    }

    public static float b2PlaneSeparation(b2Plane plane, b2Vec2 point) {
        return b2Dot(plane.normal, point) - plane.offset;
    }

    public static float b2SpringDamper(float hertz, float dampingRatio, float position, float velocity, float timeStep) {
        float omega = 2.0f * B2_PI * hertz;
        float omegaH = omega * timeStep;
        return (velocity - omega * omegaH * position)
            / (1.0f + 2.0f * dampingRatio * omegaH + omegaH * omegaH);
    }

    public static void b2SetLengthUnitsPerMeter(float lengthUnits) {
        b2Assert(b2IsValidFloat(lengthUnits) && lengthUnits > 0.0f, "lengthUnits is valid");
        lengthUnitsPerMeter = lengthUnits;
    }

    public static float b2GetLengthUnitsPerMeter() {
        return lengthUnitsPerMeter;
    }

    public static float B2_HUGE() {
        return 100000.0f * lengthUnitsPerMeter;
    }

    public static float B2_LINEAR_SLOP() {
        return 0.005f * lengthUnitsPerMeter;
    }

    public static float B2_SPECULATIVE_DISTANCE() {
        return 4.0f * B2_LINEAR_SLOP();
    }

    public static float B2_AABB_MARGIN() {
        return 0.05f * lengthUnitsPerMeter;
    }

    public static float B2_MAX_ROTATION() {
        return 0.25f * B2_PI;
    }

    public static float B2_TIME_TO_SLEEP() {
        return 0.5f;
    }

    private static int B2_MAKE_ID(int a, int b) {
        return ((a & 0xFF) << 8) | (b & 0xFF);
    }

    public static boolean b2IsValidRay(b2RayCastInput input) {
        return b2IsValidVec2(input.origin)
            && b2IsValidVec2(input.translation)
            && b2IsValidFloat(input.maxFraction)
            && 0.0f <= input.maxFraction
            && input.maxFraction < B2_HUGE();
    }

    public static b2CastOutput b2AABB_RayCast(b2AABB a, b2Vec2 p1, b2Vec2 p2) {
        b2CastOutput output = new b2CastOutput();
        float tmin = -Float.MAX_VALUE;
        float tmax = Float.MAX_VALUE;
        b2Vec2 p = p1;
        b2Vec2 d = b2Sub(p2, p1);
        b2Vec2 absD = b2Abs(d);
        b2Vec2 normal = new b2Vec2();

        if (absD.x < Math.ulp(1.0f)) {
            if (p.x < a.lowerBound.x || a.upperBound.x < p.x) {
                return output;
            }
        } else {
            float invD = 1.0f / d.x;
            float t1 = (a.lowerBound.x - p.x) * invD;
            float t2 = (a.upperBound.x - p.x) * invD;
            float s = -1.0f;
            if (t1 > t2) {
                float tmp = t1;
                t1 = t2;
                t2 = tmp;
                s = 1.0f;
            }
            if (t1 > tmin) {
                normal.y = 0.0f;
                normal.x = s;
                tmin = t1;
            }
            tmax = b2MinFloat(tmax, t2);
            if (tmin > tmax) {
                return output;
            }
        }

        if (absD.y < Math.ulp(1.0f)) {
            if (p.y < a.lowerBound.y || a.upperBound.y < p.y) {
                return output;
            }
        } else {
            float invD = 1.0f / d.y;
            float t1 = (a.lowerBound.y - p.y) * invD;
            float t2 = (a.upperBound.y - p.y) * invD;
            float s = -1.0f;
            if (t1 > t2) {
                float tmp = t1;
                t1 = t2;
                t2 = tmp;
                s = 1.0f;
            }
            if (t1 > tmin) {
                normal.x = 0.0f;
                normal.y = s;
                tmin = t1;
            }
            tmax = b2MinFloat(tmax, t2);
            if (tmin > tmax) {
                return output;
            }
        }

        if (tmin < 0.0f || 1.0f < tmin) {
            return output;
        }

        output.fraction = tmin;
        output.normal = normal;
        output.point = b2Lerp(p1, p2, tmin);
        output.hit = true;
        return output;
    }

    public static b2DynamicTree b2DynamicTree_Create() {
        b2DynamicTree tree = new b2DynamicTree();
        tree.root = B2_NULL_INDEX;
        tree.nodeCapacity = 16;
        tree.nodeCount = 0;
        tree.nodes = new b2TreeNode[tree.nodeCapacity];
        for (int i = 0; i < tree.nodeCapacity; ++i) {
            tree.nodes[i] = new b2TreeNode();
            tree.nodes[i].next = i + 1;
        }
        tree.nodes[tree.nodeCapacity - 1].next = B2_NULL_INDEX;
        tree.freeList = 0;
        return tree;
    }

    public static void b2DynamicTree_Destroy(b2DynamicTree tree) {
        tree.nodes = null;
        tree.root = 0;
        tree.nodeCount = 0;
        tree.nodeCapacity = 0;
        tree.freeList = 0;
        tree.proxyCount = 0;
        tree.leafIndices = null;
        tree.leafBoxes = null;
        tree.leafCenters = null;
        tree.binIndices = null;
        tree.rebuildCapacity = 0;
    }

    private static boolean b2IsLeaf(b2TreeNode node) {
        return (node.flags & B2_LEAF_NODE) != 0;
    }

    private static boolean b2IsAllocated(b2TreeNode node) {
        return (node.flags & B2_ALLOCATED_NODE) != 0;
    }

    private static void b2ResetTreeNode(b2TreeNode node) {
        node.aabb = new b2AABB();
        node.categoryBits = B2_DEFAULT_CATEGORY_BITS;
        node.child1 = B2_NULL_INDEX;
        node.child2 = B2_NULL_INDEX;
        node.userData = -1L;
        node.parent = B2_NULL_INDEX;
        node.next = B2_NULL_INDEX;
        node.height = 0;
        node.flags = B2_ALLOCATED_NODE;
    }

    private static int b2AllocateNode(b2DynamicTree tree) {
        if (tree.freeList == B2_NULL_INDEX) {
            int oldCapacity = tree.nodeCapacity;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            b2TreeNode[] oldNodes = tree.nodes;
            tree.nodes = java.util.Arrays.copyOf(oldNodes, newCapacity);
            for (int i = oldCapacity; i < newCapacity; ++i) {
                tree.nodes[i] = new b2TreeNode();
                tree.nodes[i].next = i + 1;
            }
            tree.nodes[newCapacity - 1].next = B2_NULL_INDEX;
            tree.freeList = oldCapacity;
            tree.nodeCapacity = newCapacity;
        }

        int nodeIndex = tree.freeList;
        b2TreeNode node = tree.nodes[nodeIndex];
        tree.freeList = node.next;
        b2ResetTreeNode(node);
        tree.nodeCount += 1;
        return nodeIndex;
    }

    private static void b2FreeNode(b2DynamicTree tree, int nodeId) {
        b2TreeNode node = tree.nodes[nodeId];
        node.next = tree.freeList;
        node.flags = 0;
        node.child1 = B2_NULL_INDEX;
        node.child2 = B2_NULL_INDEX;
        tree.freeList = nodeId;
        tree.nodeCount -= 1;
    }

    private static int b2FindBestSibling(b2DynamicTree tree, b2AABB boxD) {
        b2Vec2 centerD = b2AABB_Center(boxD);
        float areaD = b2Perimeter(boxD);
        b2TreeNode[] nodes = tree.nodes;
        int rootIndex = tree.root;
        b2AABB rootBox = nodes[rootIndex].aabb;
        float areaBase = b2Perimeter(rootBox);
        float directCost = b2Perimeter(b2AABB_Union(rootBox, boxD));
        float inheritedCost = 0.0f;
        int bestSibling = rootIndex;
        float bestCost = directCost;
        int index = rootIndex;

        while (nodes[index].height > 0) {
            int child1 = nodes[index].child1;
            int child2 = nodes[index].child2;
            float cost = directCost + inheritedCost;
            if (cost < bestCost) {
                bestSibling = index;
                bestCost = cost;
            }

            inheritedCost += directCost - areaBase;
            boolean leaf1 = nodes[child1].height == 0;
            boolean leaf2 = nodes[child2].height == 0;

            float lowerCost1 = Float.MAX_VALUE;
            b2AABB box1 = nodes[child1].aabb;
            float directCost1 = b2Perimeter(b2AABB_Union(box1, boxD));
            float area1 = 0.0f;
            if (leaf1) {
                float cost1 = directCost1 + inheritedCost;
                if (cost1 < bestCost) {
                    bestSibling = child1;
                    bestCost = cost1;
                }
            } else {
                area1 = b2Perimeter(box1);
                lowerCost1 = inheritedCost + directCost1 + b2MinFloat(areaD - area1, 0.0f);
            }

            float lowerCost2 = Float.MAX_VALUE;
            b2AABB box2 = nodes[child2].aabb;
            float directCost2 = b2Perimeter(b2AABB_Union(box2, boxD));
            float area2 = 0.0f;
            if (leaf2) {
                float cost2 = directCost2 + inheritedCost;
                if (cost2 < bestCost) {
                    bestSibling = child2;
                    bestCost = cost2;
                }
            } else {
                area2 = b2Perimeter(box2);
                lowerCost2 = inheritedCost + directCost2 + b2MinFloat(areaD - area2, 0.0f);
            }

            if (leaf1 && leaf2) {
                break;
            }
            if (bestCost <= lowerCost1 && bestCost <= lowerCost2) {
                break;
            }
            if (lowerCost1 == lowerCost2 && !leaf1) {
                b2Vec2 d1 = b2Sub(b2AABB_Center(box1), centerD);
                b2Vec2 d2 = b2Sub(b2AABB_Center(box2), centerD);
                lowerCost1 = b2LengthSquared(d1);
                lowerCost2 = b2LengthSquared(d2);
            }

            if (lowerCost1 < lowerCost2 && !leaf1) {
                index = child1;
                areaBase = area1;
                directCost = directCost1;
            } else {
                index = child2;
                areaBase = area2;
                directCost = directCost2;
            }
        }

        return bestSibling;
    }

    private static void b2RotateNodes(b2DynamicTree tree, int iA) {
        b2TreeNode[] nodes = tree.nodes;
        b2TreeNode A = nodes[iA];
        if (A.height < 2) {
            return;
        }

        int iB = A.child1;
        int iC = A.child2;
        b2TreeNode B = nodes[iB];
        b2TreeNode C = nodes[iC];

        if (B.height == 0) {
            int iF = C.child1;
            int iG = C.child2;
            b2TreeNode F = nodes[iF];
            b2TreeNode G = nodes[iG];

            float costBase = b2Perimeter(C.aabb);
            b2AABB aabbBG = b2AABB_Union(B.aabb, G.aabb);
            float costBF = b2Perimeter(aabbBG);
            b2AABB aabbBF = b2AABB_Union(B.aabb, F.aabb);
            float costBG = b2Perimeter(aabbBF);
            if (costBase < costBF && costBase < costBG) {
                return;
            }

            if (costBF < costBG) {
                A.child1 = iF;
                C.child1 = iB;
                B.parent = iC;
                F.parent = iA;
                C.aabb = aabbBG;
                C.height = 1 + Math.max(B.height, G.height);
                A.height = 1 + Math.max(C.height, F.height);
                C.categoryBits = B.categoryBits | G.categoryBits;
                A.categoryBits = C.categoryBits | F.categoryBits;
                C.flags |= (B.flags | G.flags) & B2_ENLARGED_NODE;
                A.flags |= (C.flags | F.flags) & B2_ENLARGED_NODE;
            } else {
                A.child1 = iG;
                C.child2 = iB;
                B.parent = iC;
                G.parent = iA;
                C.aabb = aabbBF;
                C.height = 1 + Math.max(B.height, F.height);
                A.height = 1 + Math.max(C.height, G.height);
                C.categoryBits = B.categoryBits | F.categoryBits;
                A.categoryBits = C.categoryBits | G.categoryBits;
                C.flags |= (B.flags | F.flags) & B2_ENLARGED_NODE;
                A.flags |= (C.flags | G.flags) & B2_ENLARGED_NODE;
            }
        } else if (C.height == 0) {
            int iD = B.child1;
            int iE = B.child2;
            b2TreeNode D = nodes[iD];
            b2TreeNode E = nodes[iE];

            float costBase = b2Perimeter(B.aabb);
            b2AABB aabbCE = b2AABB_Union(C.aabb, E.aabb);
            float costCD = b2Perimeter(aabbCE);
            b2AABB aabbCD = b2AABB_Union(C.aabb, D.aabb);
            float costCE = b2Perimeter(aabbCD);
            if (costBase < costCD && costBase < costCE) {
                return;
            }

            if (costCD < costCE) {
                A.child2 = iD;
                B.child1 = iC;
                C.parent = iB;
                D.parent = iA;
                B.aabb = aabbCE;
                B.height = 1 + Math.max(C.height, E.height);
                A.height = 1 + Math.max(B.height, D.height);
                B.categoryBits = C.categoryBits | E.categoryBits;
                A.categoryBits = B.categoryBits | D.categoryBits;
                B.flags |= (C.flags | E.flags) & B2_ENLARGED_NODE;
                A.flags |= (B.flags | D.flags) & B2_ENLARGED_NODE;
            } else {
                A.child2 = iE;
                B.child2 = iC;
                C.parent = iB;
                E.parent = iA;
                B.aabb = aabbCD;
                B.height = 1 + Math.max(C.height, D.height);
                A.height = 1 + Math.max(B.height, E.height);
                B.categoryBits = C.categoryBits | D.categoryBits;
                A.categoryBits = B.categoryBits | E.categoryBits;
                B.flags |= (C.flags | D.flags) & B2_ENLARGED_NODE;
                A.flags |= (B.flags | E.flags) & B2_ENLARGED_NODE;
            }
        } else {
            int iD = B.child1;
            int iE = B.child2;
            int iF = C.child1;
            int iG = C.child2;
            b2TreeNode D = nodes[iD];
            b2TreeNode E = nodes[iE];
            b2TreeNode F = nodes[iF];
            b2TreeNode G = nodes[iG];

            float areaB = b2Perimeter(B.aabb);
            float areaC = b2Perimeter(C.aabb);
            float bestCost = areaB + areaC;
            int bestRotation = 0;

            b2AABB aabbBG = b2AABB_Union(B.aabb, G.aabb);
            float costBF = areaB + b2Perimeter(aabbBG);
            if (costBF < bestCost) {
                bestRotation = 1;
                bestCost = costBF;
            }

            b2AABB aabbBF = b2AABB_Union(B.aabb, F.aabb);
            float costBG = areaB + b2Perimeter(aabbBF);
            if (costBG < bestCost) {
                bestRotation = 2;
                bestCost = costBG;
            }

            b2AABB aabbCE = b2AABB_Union(C.aabb, E.aabb);
            float costCD = areaC + b2Perimeter(aabbCE);
            if (costCD < bestCost) {
                bestRotation = 3;
                bestCost = costCD;
            }

            b2AABB aabbCD = b2AABB_Union(C.aabb, D.aabb);
            float costCE = areaC + b2Perimeter(aabbCD);
            if (costCE < bestCost) {
                bestRotation = 4;
            }

            switch (bestRotation) {
                case 1:
                    A.child1 = iF;
                    C.child1 = iB;
                    B.parent = iC;
                    F.parent = iA;
                    C.aabb = aabbBG;
                    C.height = 1 + Math.max(B.height, G.height);
                    A.height = 1 + Math.max(C.height, F.height);
                    C.categoryBits = B.categoryBits | G.categoryBits;
                    A.categoryBits = C.categoryBits | F.categoryBits;
                    C.flags |= (B.flags | G.flags) & B2_ENLARGED_NODE;
                    A.flags |= (C.flags | F.flags) & B2_ENLARGED_NODE;
                    break;
                case 2:
                    A.child1 = iG;
                    C.child2 = iB;
                    B.parent = iC;
                    G.parent = iA;
                    C.aabb = aabbBF;
                    C.height = 1 + Math.max(B.height, F.height);
                    A.height = 1 + Math.max(C.height, G.height);
                    C.categoryBits = B.categoryBits | F.categoryBits;
                    A.categoryBits = C.categoryBits | G.categoryBits;
                    C.flags |= (B.flags | F.flags) & B2_ENLARGED_NODE;
                    A.flags |= (C.flags | G.flags) & B2_ENLARGED_NODE;
                    break;
                case 3:
                    A.child2 = iD;
                    B.child1 = iC;
                    C.parent = iB;
                    D.parent = iA;
                    B.aabb = aabbCE;
                    B.height = 1 + Math.max(C.height, E.height);
                    A.height = 1 + Math.max(B.height, D.height);
                    B.categoryBits = C.categoryBits | E.categoryBits;
                    A.categoryBits = B.categoryBits | D.categoryBits;
                    B.flags |= (C.flags | E.flags) & B2_ENLARGED_NODE;
                    A.flags |= (B.flags | D.flags) & B2_ENLARGED_NODE;
                    break;
                case 4:
                    A.child2 = iE;
                    B.child2 = iC;
                    C.parent = iB;
                    E.parent = iA;
                    B.aabb = aabbCD;
                    B.height = 1 + Math.max(C.height, D.height);
                    A.height = 1 + Math.max(B.height, E.height);
                    B.categoryBits = C.categoryBits | D.categoryBits;
                    A.categoryBits = B.categoryBits | E.categoryBits;
                    B.flags |= (C.flags | D.flags) & B2_ENLARGED_NODE;
                    A.flags |= (B.flags | E.flags) & B2_ENLARGED_NODE;
                    break;
                default:
                    break;
            }
        }
    }

    private static void b2InsertLeaf(b2DynamicTree tree, int leaf, boolean shouldRotate) {
        if (tree.root == B2_NULL_INDEX) {
            tree.root = leaf;
            tree.nodes[tree.root].parent = B2_NULL_INDEX;
            return;
        }

        b2TreeNode[] nodes = tree.nodes;
        b2AABB leafAABB = nodes[leaf].aabb;
        int sibling = b2FindBestSibling(tree, leafAABB);
        int oldParent = nodes[sibling].parent;
        int newParent = b2AllocateNode(tree);
        nodes = tree.nodes;

        nodes[newParent].parent = oldParent;
        nodes[newParent].userData = -1L;
        nodes[newParent].aabb = b2AABB_Union(leafAABB, nodes[sibling].aabb);
        nodes[newParent].categoryBits = nodes[leaf].categoryBits | nodes[sibling].categoryBits;
        nodes[newParent].height = nodes[sibling].height + 1;

        if (oldParent != B2_NULL_INDEX) {
            if (nodes[oldParent].child1 == sibling) {
                nodes[oldParent].child1 = newParent;
            } else {
                nodes[oldParent].child2 = newParent;
            }
            nodes[newParent].child1 = sibling;
            nodes[newParent].child2 = leaf;
            nodes[sibling].parent = newParent;
            nodes[leaf].parent = newParent;
        } else {
            nodes[newParent].child1 = sibling;
            nodes[newParent].child2 = leaf;
            nodes[sibling].parent = newParent;
            nodes[leaf].parent = newParent;
            tree.root = newParent;
        }

        int index = nodes[leaf].parent;
        while (index != B2_NULL_INDEX) {
            int child1 = nodes[index].child1;
            int child2 = nodes[index].child2;
            nodes[index].aabb = b2AABB_Union(nodes[child1].aabb, nodes[child2].aabb);
            nodes[index].categoryBits = nodes[child1].categoryBits | nodes[child2].categoryBits;
            nodes[index].height = 1 + Math.max(nodes[child1].height, nodes[child2].height);
            nodes[index].flags |= (nodes[child1].flags | nodes[child2].flags) & B2_ENLARGED_NODE;
            if (shouldRotate) {
                b2RotateNodes(tree, index);
            }
            index = nodes[index].parent;
        }
    }

    private static void b2RemoveLeaf(b2DynamicTree tree, int leaf) {
        if (leaf == tree.root) {
            tree.root = B2_NULL_INDEX;
            return;
        }

        b2TreeNode[] nodes = tree.nodes;
        int parent = nodes[leaf].parent;
        int grandParent = nodes[parent].parent;
        int sibling = nodes[parent].child1 == leaf ? nodes[parent].child2 : nodes[parent].child1;

        if (grandParent != B2_NULL_INDEX) {
            if (nodes[grandParent].child1 == parent) {
                nodes[grandParent].child1 = sibling;
            } else {
                nodes[grandParent].child2 = sibling;
            }
            nodes[sibling].parent = grandParent;
            b2FreeNode(tree, parent);

            int index = grandParent;
            while (index != B2_NULL_INDEX) {
                b2TreeNode node = nodes[index];
                b2TreeNode child1 = nodes[node.child1];
                b2TreeNode child2 = nodes[node.child2];
                node.aabb = b2AABB_Union(child1.aabb, child2.aabb);
                node.categoryBits = child1.categoryBits | child2.categoryBits;
                node.height = 1 + Math.max(child1.height, child2.height);
                index = node.parent;
            }
        } else {
            tree.root = sibling;
            nodes[sibling].parent = B2_NULL_INDEX;
            b2FreeNode(tree, parent);
        }
    }

    public static int b2DynamicTree_CreateProxy(b2DynamicTree tree, b2AABB aabb, long categoryBits, long userData) {
        int proxyId = b2AllocateNode(tree);
        b2TreeNode node = tree.nodes[proxyId];
        node.aabb = aabb.copy();
        node.userData = userData;
        node.categoryBits = categoryBits;
        node.height = 0;
        node.flags = B2_ALLOCATED_NODE | B2_LEAF_NODE;
        b2InsertLeaf(tree, proxyId, true);
        tree.proxyCount += 1;
        return proxyId;
    }

    public static void b2DynamicTree_DestroyProxy(b2DynamicTree tree, int proxyId) {
        b2RemoveLeaf(tree, proxyId);
        b2FreeNode(tree, proxyId);
        tree.proxyCount -= 1;
    }

    public static int b2DynamicTree_GetProxyCount(b2DynamicTree tree) {
        return tree.proxyCount;
    }

    public static void b2DynamicTree_MoveProxy(b2DynamicTree tree, int proxyId, b2AABB aabb) {
        b2RemoveLeaf(tree, proxyId);
        tree.nodes[proxyId].aabb = aabb.copy();
        b2InsertLeaf(tree, proxyId, false);
    }

    public static void b2DynamicTree_EnlargeProxy(b2DynamicTree tree, int proxyId, b2AABB aabb) {
        b2TreeNode[] nodes = tree.nodes;
        nodes[proxyId].aabb = aabb.copy();
        int parentIndex = nodes[proxyId].parent;
        while (parentIndex != B2_NULL_INDEX) {
            boolean changed = b2EnlargeAABB(nodes[parentIndex].aabb, aabb);
            nodes[parentIndex].flags |= B2_ENLARGED_NODE;
            parentIndex = nodes[parentIndex].parent;
            if (!changed) {
                break;
            }
        }
        while (parentIndex != B2_NULL_INDEX) {
            if ((nodes[parentIndex].flags & B2_ENLARGED_NODE) != 0) {
                break;
            }
            nodes[parentIndex].flags |= B2_ENLARGED_NODE;
            parentIndex = nodes[parentIndex].parent;
        }
    }

    public static void b2DynamicTree_SetCategoryBits(b2DynamicTree tree, int proxyId, long categoryBits) {
        b2TreeNode[] nodes = tree.nodes;
        nodes[proxyId].categoryBits = categoryBits;
        int nodeIndex = nodes[proxyId].parent;
        while (nodeIndex != B2_NULL_INDEX) {
            b2TreeNode node = nodes[nodeIndex];
            node.categoryBits = nodes[node.child1].categoryBits | nodes[node.child2].categoryBits;
            nodeIndex = node.parent;
        }
    }

    public static long b2DynamicTree_GetCategoryBits(b2DynamicTree tree, int proxyId) {
        return tree.nodes[proxyId].categoryBits;
    }

    public static int b2DynamicTree_GetHeight(b2DynamicTree tree) {
        return tree.root == B2_NULL_INDEX ? 0 : tree.nodes[tree.root].height;
    }

    public static float b2DynamicTree_GetAreaRatio(b2DynamicTree tree) {
        if (tree.root == B2_NULL_INDEX) {
            return 0.0f;
        }
        float rootArea = b2Perimeter(tree.nodes[tree.root].aabb);
        float totalArea = 0.0f;
        for (int i = 0; i < tree.nodeCapacity; ++i) {
            b2TreeNode node = tree.nodes[i];
            if (!b2IsAllocated(node) || b2IsLeaf(node) || i == tree.root) {
                continue;
            }
            totalArea += b2Perimeter(node.aabb);
        }
        return totalArea / rootArea;
    }

    public static b2AABB b2DynamicTree_GetRootBounds(b2DynamicTree tree) {
        return tree.root != B2_NULL_INDEX ? tree.nodes[tree.root].aabb.copy() : new b2AABB(b2Vec2_zero, b2Vec2_zero);
    }

    public static long b2DynamicTree_GetUserData(b2DynamicTree tree, int proxyId) {
        return tree.nodes[proxyId].userData;
    }

    public static b2AABB b2DynamicTree_GetAABB(b2DynamicTree tree, int proxyId) {
        return tree.nodes[proxyId].aabb.copy();
    }

    public static int b2DynamicTree_GetByteCount(b2DynamicTree tree) {
        return 72 + 40 * tree.nodeCapacity + tree.rebuildCapacity * (4 + 16 + 8 + 4);
    }

    public static b2TreeStats b2DynamicTree_Query(b2DynamicTree tree, b2AABB aabb, long maskBits, b2TreeQueryCallback callback) {
        b2TreeStats result = new b2TreeStats();
        if (tree.nodeCount == 0) {
            return result;
        }

        int[] stack = new int[B2_TREE_STACK_SIZE];
        int stackCount = 0;
        stack[stackCount++] = tree.root;
        while (stackCount > 0) {
            int nodeId = stack[--stackCount];
            b2TreeNode node = tree.nodes[nodeId];
            result.nodeVisits += 1;
            if (b2AABB_Overlaps(node.aabb, aabb) && (node.categoryBits & maskBits) != 0) {
                if (b2IsLeaf(node)) {
                    result.leafVisits += 1;
                    if (!callback.invoke(nodeId, node.userData)) {
                        return result;
                    }
                } else if (stackCount < B2_TREE_STACK_SIZE - 1) {
                    stack[stackCount++] = node.child1;
                    stack[stackCount++] = node.child2;
                }
            }
        }
        return result;
    }

    public static b2TreeStats b2DynamicTree_RayCast(b2DynamicTree tree, b2RayCastInput input, long maskBits,
                                                     b2TreeRayCastCallback callback) {
        b2TreeStats result = new b2TreeStats();
        if (tree.nodeCount == 0) {
            return result;
        }

        b2Vec2 p1 = input.origin;
        b2Vec2 d = input.translation;
        b2Vec2 r = b2Normalize(d);
        b2Vec2 v = b2CrossSV(1.0f, r);
        b2Vec2 absV = b2Abs(v);
        float maxFraction = input.maxFraction;
        b2Vec2 p2 = b2MulAdd(p1, maxFraction, d);
        b2AABB segmentAABB = new b2AABB(b2Min(p1, p2), b2Max(p1, p2));
        int[] stack = new int[B2_TREE_STACK_SIZE];
        int stackCount = 0;
        stack[stackCount++] = tree.root;
        b2RayCastInput subInput = new b2RayCastInput();
        subInput.origin = input.origin.copy();
        subInput.translation = input.translation.copy();

        while (stackCount > 0) {
            int nodeId = stack[--stackCount];
            b2TreeNode node = tree.nodes[nodeId];
            result.nodeVisits += 1;
            if ((node.categoryBits & maskBits) == 0 || !b2AABB_Overlaps(node.aabb, segmentAABB)) {
                continue;
            }
            b2Vec2 c = b2AABB_Center(node.aabb);
            b2Vec2 h = b2AABB_Extents(node.aabb);
            float term1 = b2AbsFloat(b2Dot(v, b2Sub(p1, c)));
            float term2 = b2Dot(absV, h);
            if (term2 < term1) {
                continue;
            }
            if (b2IsLeaf(node)) {
                subInput.maxFraction = maxFraction;
                float value = callback.invoke(subInput, nodeId, node.userData);
                result.leafVisits += 1;
                if (value == 0.0f) {
                    return result;
                }
                if (0.0f < value && value <= maxFraction) {
                    maxFraction = value;
                    p2 = b2MulAdd(p1, maxFraction, d);
                    segmentAABB.lowerBound = b2Min(p1, p2);
                    segmentAABB.upperBound = b2Max(p1, p2);
                }
            } else if (stackCount < B2_TREE_STACK_SIZE - 1) {
                b2Vec2 c1 = b2AABB_Center(tree.nodes[node.child1].aabb);
                b2Vec2 c2 = b2AABB_Center(tree.nodes[node.child2].aabb);
                if (b2DistanceSquared(c1, p1) < b2DistanceSquared(c2, p1)) {
                    stack[stackCount++] = node.child2;
                    stack[stackCount++] = node.child1;
                } else {
                    stack[stackCount++] = node.child1;
                    stack[stackCount++] = node.child2;
                }
            }
        }
        return result;
    }

    public static b2TreeStats b2DynamicTree_ShapeCast(b2DynamicTree tree, b2ShapeCastInput input, long maskBits,
                                                       b2TreeShapeCastCallback callback) {
        b2TreeStats stats = new b2TreeStats();
        if (tree.nodeCount == 0 || input.proxy.count == 0) {
            return stats;
        }

        b2AABB originAABB = b2MakeAABB(input.proxy.points, input.proxy.count, input.proxy.radius);
        b2Vec2 p1 = b2AABB_Center(originAABB);
        b2Vec2 extension = b2AABB_Extents(originAABB);
        b2Vec2 r = input.translation;
        b2Vec2 v = b2CrossSV(1.0f, r);
        b2Vec2 absV = b2Abs(v);
        float maxFraction = input.maxFraction;
        b2Vec2 t = b2MulSV(maxFraction, input.translation);
        b2AABB totalAABB = new b2AABB(b2Min(originAABB.lowerBound, b2Add(originAABB.lowerBound, t)),
            b2Max(originAABB.upperBound, b2Add(originAABB.upperBound, t)));

        b2ShapeCastInput subInput = new b2ShapeCastInput();
        subInput.proxy = input.proxy;
        subInput.translation = input.translation.copy();
        subInput.canEncroach = input.canEncroach;

        int[] stack = new int[B2_TREE_STACK_SIZE];
        int stackCount = 0;
        stack[stackCount++] = tree.root;
        while (stackCount > 0) {
            int nodeId = stack[--stackCount];
            b2TreeNode node = tree.nodes[nodeId];
            stats.nodeVisits += 1;
            if ((node.categoryBits & maskBits) == 0 || !b2AABB_Overlaps(node.aabb, totalAABB)) {
                continue;
            }
            b2Vec2 c = b2AABB_Center(node.aabb);
            b2Vec2 h = b2Add(b2AABB_Extents(node.aabb), extension);
            float term1 = b2AbsFloat(b2Dot(v, b2Sub(p1, c)));
            float term2 = b2Dot(absV, h);
            if (term2 < term1) {
                continue;
            }
            if (b2IsLeaf(node)) {
                subInput.maxFraction = maxFraction;
                float value = callback.invoke(subInput, nodeId, node.userData);
                stats.leafVisits += 1;
                if (value == 0.0f) {
                    return stats;
                }
                if (0.0f < value && value < maxFraction) {
                    maxFraction = value;
                    t = b2MulSV(maxFraction, input.translation);
                    totalAABB.lowerBound = b2Min(originAABB.lowerBound, b2Add(originAABB.lowerBound, t));
                    totalAABB.upperBound = b2Max(originAABB.upperBound, b2Add(originAABB.upperBound, t));
                }
            } else if (stackCount < B2_TREE_STACK_SIZE - 1) {
                b2Vec2 c1 = b2AABB_Center(tree.nodes[node.child1].aabb);
                b2Vec2 c2 = b2AABB_Center(tree.nodes[node.child2].aabb);
                if (b2DistanceSquared(c1, p1) < b2DistanceSquared(c2, p1)) {
                    stack[stackCount++] = node.child2;
                    stack[stackCount++] = node.child1;
                } else {
                    stack[stackCount++] = node.child1;
                    stack[stackCount++] = node.child2;
                }
            }
        }
        return stats;
    }

    private static int b2PartitionMid(int[] indices, b2Vec2[] centers, int start, int count) {
        if (count <= 2) {
            return count / 2;
        }

        b2Vec2 lowerBound = centers[start];
        b2Vec2 upperBound = centers[start];
        for (int i = 1; i < count; ++i) {
            lowerBound = b2Min(lowerBound, centers[start + i]);
            upperBound = b2Max(upperBound, centers[start + i]);
        }

        b2Vec2 d = b2Sub(upperBound, lowerBound);
        b2Vec2 c = new b2Vec2(0.5f * (lowerBound.x + upperBound.x), 0.5f * (lowerBound.y + upperBound.y));
        int i1 = 0;
        int i2 = count;
        if (d.x > d.y) {
            float pivot = c.x;
            while (i1 < i2) {
                while (i1 < i2 && centers[start + i1].x < pivot) {
                    i1 += 1;
                }
                while (i1 < i2 && centers[start + i2 - 1].x >= pivot) {
                    i2 -= 1;
                }
                if (i1 < i2) {
                    b2SwapRebuildItems(indices, centers, start + i1, start + i2 - 1);
                    i1 += 1;
                    i2 -= 1;
                }
            }
        } else {
            float pivot = c.y;
            while (i1 < i2) {
                while (i1 < i2 && centers[start + i1].y < pivot) {
                    i1 += 1;
                }
                while (i1 < i2 && centers[start + i2 - 1].y >= pivot) {
                    i2 -= 1;
                }
                if (i1 < i2) {
                    b2SwapRebuildItems(indices, centers, start + i1, start + i2 - 1);
                    i1 += 1;
                    i2 -= 1;
                }
            }
        }

        if (i1 > 0 && i1 < count) {
            return i1;
        }
        return count / 2;
    }

    private static void b2SwapRebuildItems(int[] indices, b2Vec2[] centers, int a, int b) {
        int tempIndex = indices[a];
        indices[a] = indices[b];
        indices[b] = tempIndex;
        b2Vec2 tempCenter = centers[a];
        centers[a] = centers[b];
        centers[b] = tempCenter;
    }

    private static final class b2RebuildItem {
        int nodeIndex;
        int childCount;
        int startIndex;
        int splitIndex;
        int endIndex;
    }

    private static int b2BuildTree(b2DynamicTree tree, int leafCount) {
        b2TreeNode[] nodes = tree.nodes;
        int[] leafIndices = tree.leafIndices;
        if (leafCount == 1) {
            nodes[leafIndices[0]].parent = B2_NULL_INDEX;
            return leafIndices[0];
        }

        b2RebuildItem[] stack = new b2RebuildItem[B2_TREE_STACK_SIZE];
        for (int i = 0; i < stack.length; ++i) {
            stack[i] = new b2RebuildItem();
        }
        int top = 0;
        stack[0].nodeIndex = b2AllocateNode(tree);
        stack[0].childCount = -1;
        stack[0].startIndex = 0;
        stack[0].endIndex = leafCount;
        stack[0].splitIndex = b2PartitionMid(tree.leafIndices, tree.leafCenters, 0, leafCount);

        while (true) {
            b2RebuildItem item = stack[top];
            item.childCount += 1;

            if (item.childCount == 2) {
                if (top == 0) {
                    break;
                }

                b2RebuildItem parentItem = stack[top - 1];
                b2TreeNode parentNode = tree.nodes[parentItem.nodeIndex];
                if (parentItem.childCount == 0) {
                    parentNode.child1 = item.nodeIndex;
                } else {
                    parentNode.child2 = item.nodeIndex;
                }

                b2TreeNode node = tree.nodes[item.nodeIndex];
                node.parent = parentItem.nodeIndex;
                b2TreeNode child1 = tree.nodes[node.child1];
                b2TreeNode child2 = tree.nodes[node.child2];
                node.aabb = b2AABB_Union(child1.aabb, child2.aabb);
                node.height = 1 + Math.max(child1.height, child2.height);
                node.categoryBits = child1.categoryBits | child2.categoryBits;

                top -= 1;
            } else {
                int startIndex;
                int endIndex;
                if (item.childCount == 0) {
                    startIndex = item.startIndex;
                    endIndex = item.splitIndex;
                } else {
                    startIndex = item.splitIndex;
                    endIndex = item.endIndex;
                }

                int count = endIndex - startIndex;
                if (count == 1) {
                    int childIndex = leafIndices[startIndex];
                    b2TreeNode node = tree.nodes[item.nodeIndex];
                    if (item.childCount == 0) {
                        node.child1 = childIndex;
                    } else {
                        node.child2 = childIndex;
                    }
                    tree.nodes[childIndex].parent = item.nodeIndex;
                } else {
                    top += 1;
                    b2RebuildItem newItem = stack[top];
                    newItem.nodeIndex = b2AllocateNode(tree);
                    newItem.childCount = -1;
                    newItem.startIndex = startIndex;
                    newItem.endIndex = endIndex;
                    newItem.splitIndex = startIndex + b2PartitionMid(tree.leafIndices, tree.leafCenters, startIndex, count);
                }
            }
        }

        b2TreeNode rootNode = tree.nodes[stack[0].nodeIndex];
        b2TreeNode child1 = tree.nodes[rootNode.child1];
        b2TreeNode child2 = tree.nodes[rootNode.child2];
        rootNode.aabb = b2AABB_Union(child1.aabb, child2.aabb);
        rootNode.height = 1 + Math.max(child1.height, child2.height);
        rootNode.categoryBits = child1.categoryBits | child2.categoryBits;
        return stack[0].nodeIndex;
    }

    public static int b2DynamicTree_Rebuild(b2DynamicTree tree, boolean fullBuild) {
        int proxyCount = tree.proxyCount;
        if (proxyCount == 0) {
            return 0;
        }

        if (proxyCount > tree.rebuildCapacity) {
            int newCapacity = proxyCount + proxyCount / 2;
            tree.leafIndices = new int[newCapacity];
            tree.leafCenters = b2Vec2.array(newCapacity);
            tree.rebuildCapacity = newCapacity;
        }

        int leafCount = 0;
        int[] stack = new int[B2_TREE_STACK_SIZE];
        int stackCount = 0;
        int nodeIndex = tree.root;
        b2TreeNode[] nodes = tree.nodes;
        b2TreeNode node = nodes[nodeIndex];

        while (true) {
            if (node.height == 0 || (((node.flags & B2_ENLARGED_NODE) == 0) && !fullBuild)) {
                tree.leafIndices[leafCount] = nodeIndex;
                tree.leafCenters[leafCount] = b2AABB_Center(node.aabb);
                leafCount += 1;
                node.parent = B2_NULL_INDEX;
            } else {
                int doomedNodeIndex = nodeIndex;
                nodeIndex = node.child1;
                stack[stackCount++] = node.child2;
                node = nodes[nodeIndex];
                b2FreeNode(tree, doomedNodeIndex);
                continue;
            }

            if (stackCount == 0) {
                break;
            }

            nodeIndex = stack[--stackCount];
            node = nodes[nodeIndex];
        }

        tree.root = b2BuildTree(tree, leafCount);
        b2DynamicTree_Validate(tree);
        return leafCount;
    }

    public static void b2DynamicTree_Validate(b2DynamicTree tree) {
        if (tree.root == B2_NULL_INDEX) {
            return;
        }
        b2ValidateTreeStructure(tree, tree.root);
        b2ValidateTreeMetrics(tree, tree.root);
    }

    public static void b2DynamicTree_ValidateNoEnlarged(b2DynamicTree tree) {
        for (int i = 0; i < tree.nodeCapacity; ++i) {
            if (b2IsAllocated(tree.nodes[i])) {
                b2Assert((tree.nodes[i].flags & B2_ENLARGED_NODE) == 0, "dynamic tree node is not enlarged");
            }
        }
    }

    private static int b2ComputeTreeHeight(b2DynamicTree tree, int nodeId) {
        b2TreeNode node = tree.nodes[nodeId];
        if (b2IsLeaf(node)) {
            return 0;
        }
        return 1 + Math.max(b2ComputeTreeHeight(tree, node.child1), b2ComputeTreeHeight(tree, node.child2));
    }

    private static void b2ValidateTreeStructure(b2DynamicTree tree, int index) {
        if (index == B2_NULL_INDEX) {
            return;
        }
        if (index == tree.root) {
            b2Assert(tree.nodes[index].parent == B2_NULL_INDEX, "root parent is null");
        }
        b2TreeNode node = tree.nodes[index];
        b2Assert(node.flags == 0 || (node.flags & B2_ALLOCATED_NODE) != 0, "node allocated flag");
        if (b2IsLeaf(node)) {
            b2Assert(node.height == 0, "leaf height is zero");
            return;
        }
        b2Assert(tree.nodes[node.child1].parent == index, "child1 parent");
        b2Assert(tree.nodes[node.child2].parent == index, "child2 parent");
        b2ValidateTreeStructure(tree, node.child1);
        b2ValidateTreeStructure(tree, node.child2);
    }

    private static void b2ValidateTreeMetrics(b2DynamicTree tree, int index) {
        if (index == B2_NULL_INDEX) {
            return;
        }
        b2TreeNode node = tree.nodes[index];
        if (b2IsLeaf(node)) {
            b2Assert(node.height == 0, "leaf height is zero");
            return;
        }
        b2TreeNode child1 = tree.nodes[node.child1];
        b2TreeNode child2 = tree.nodes[node.child2];
        b2Assert(node.height == 1 + Math.max(child1.height, child2.height), "internal node height");
        b2Assert(b2AABB_Contains(node.aabb, child1.aabb), "node contains child1");
        b2Assert(b2AABB_Contains(node.aabb, child2.aabb), "node contains child2");
        b2Assert(node.categoryBits == (child1.categoryBits | child2.categoryBits), "node category bits");
        b2ValidateTreeMetrics(tree, node.child1);
        b2ValidateTreeMetrics(tree, node.child2);
    }

    public static void b2CreateBroadPhase(b2BroadPhase bp) {
        bp.moveSet = b2CreateSet(16);
        bp.moveArray = new java.util.ArrayList<>();
        bp.pairSet = b2CreateSet(32);
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            bp.trees[i] = b2DynamicTree_Create();
        }
    }

    public static void b2DestroyBroadPhase(b2BroadPhase bp) {
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            if (bp.trees[i] != null) {
                b2DynamicTree_Destroy(bp.trees[i]);
            }
        }
        b2DestroySet(bp.moveSet);
        b2DestroySet(bp.pairSet);
        bp.moveArray.clear();
    }

    public static void b2BufferMove(b2BroadPhase bp, int proxyKey) {
        boolean alreadyAdded = b2AddKey(bp.moveSet, proxyKey + 1L);
        if (!alreadyAdded) {
            bp.moveArray.add(proxyKey);
        }
    }

    private static void b2UnBufferMove(b2BroadPhase bp, int proxyKey) {
        boolean found = b2RemoveKey(bp.moveSet, proxyKey + 1L);
        if (found) {
            for (int i = 0; i < bp.moveArray.size(); ++i) {
                if (bp.moveArray.get(i) == proxyKey) {
                    int last = bp.moveArray.remove(bp.moveArray.size() - 1);
                    if (i < bp.moveArray.size()) {
                        bp.moveArray.set(i, last);
                    }
                    break;
                }
            }
        }
    }

    public static int b2BroadPhase_CreateProxy(b2BroadPhase bp, int proxyType, b2AABB aabb, long categoryBits, int shapeIndex,
                                               boolean forcePairCreation) {
        int proxyId = b2DynamicTree_CreateProxy(bp.trees[proxyType], aabb, categoryBits, shapeIndex);
        int proxyKey = B2_PROXY_KEY(proxyId, proxyType);
        if (proxyType != b2_staticBody || forcePairCreation) {
            b2BufferMove(bp, proxyKey);
        }
        return proxyKey;
    }

    public static void b2BroadPhase_DestroyProxy(b2BroadPhase bp, int proxyKey) {
        b2UnBufferMove(bp, proxyKey);
        int proxyType = B2_PROXY_TYPE(proxyKey);
        int proxyId = B2_PROXY_ID(proxyKey);
        b2DynamicTree_DestroyProxy(bp.trees[proxyType], proxyId);
    }

    public static void b2BroadPhase_MoveProxy(b2BroadPhase bp, int proxyKey, b2AABB aabb) {
        int proxyType = B2_PROXY_TYPE(proxyKey);
        int proxyId = B2_PROXY_ID(proxyKey);
        b2DynamicTree_MoveProxy(bp.trees[proxyType], proxyId, aabb);
        b2BufferMove(bp, proxyKey);
    }

    public static void b2BroadPhase_EnlargeProxy(b2BroadPhase bp, int proxyKey, b2AABB aabb) {
        int proxyType = B2_PROXY_TYPE(proxyKey);
        int proxyId = B2_PROXY_ID(proxyKey);
        b2DynamicTree_EnlargeProxy(bp.trees[proxyType], proxyId, aabb);
        b2BufferMove(bp, proxyKey);
    }

    public static void b2BroadPhase_RebuildTrees(b2BroadPhase bp) {
        b2DynamicTree_Rebuild(bp.trees[b2_dynamicBody], false);
        b2DynamicTree_Rebuild(bp.trees[b2_kinematicBody], false);
    }

    public static int b2BroadPhase_GetShapeIndex(b2BroadPhase bp, int proxyKey) {
        int proxyType = B2_PROXY_TYPE(proxyKey);
        int proxyId = B2_PROXY_ID(proxyKey);
        return (int) b2DynamicTree_GetUserData(bp.trees[proxyType], proxyId);
    }

    public static boolean b2BroadPhase_TestOverlap(b2BroadPhase bp, int proxyKeyA, int proxyKeyB) {
        int typeIndexA = B2_PROXY_TYPE(proxyKeyA);
        int proxyIdA = B2_PROXY_ID(proxyKeyA);
        int typeIndexB = B2_PROXY_TYPE(proxyKeyB);
        int proxyIdB = B2_PROXY_ID(proxyKeyB);
        b2AABB aabbA = b2DynamicTree_GetAABB(bp.trees[typeIndexA], proxyIdA);
        b2AABB aabbB = b2DynamicTree_GetAABB(bp.trees[typeIndexB], proxyIdB);
        return b2AABB_Overlaps(aabbA, aabbB);
    }

    public static void b2ValidateBroadphase(b2BroadPhase bp) {
        b2DynamicTree_Validate(bp.trees[b2_dynamicBody]);
        b2DynamicTree_Validate(bp.trees[b2_kinematicBody]);
    }

    public static void b2ValidateNoEnlarged(b2BroadPhase bp) {
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            b2DynamicTree_ValidateNoEnlarged(bp.trees[i]);
        }
    }

    private static int b2_bodyTypeCount() {
        return 3;
    }

    private static b2Vec2 b2ComputePolygonCentroid(b2Vec2[] vertices, int count) {
        b2Vec2 center = new b2Vec2();
        float area = 0.0f;
        b2Vec2 origin = vertices[0];
        final float inv3 = 1.0f / 3.0f;
        for (int i = 1; i < count - 1; ++i) {
            b2Vec2 e1 = b2Sub(vertices[i], origin);
            b2Vec2 e2 = b2Sub(vertices[i + 1], origin);
            float a = 0.5f * b2Cross(e1, e2);
            center = b2MulAdd(center, a * inv3, b2Add(e1, e2));
            area += a;
        }
        b2Assert(area > Math.ulp(1.0f), "area > FLT_EPSILON");
        float invArea = 1.0f / area;
        center.x *= invArea;
        center.y *= invArea;
        return b2Add(origin, center);
    }

    public static b2Polygon b2MakePolygon(b2Hull hull, float radius) {
        if (hull.count < 3) {
            return b2MakeSquare(0.5f);
        }
        b2Polygon shape = new b2Polygon();
        shape.count = hull.count;
        shape.radius = radius;
        for (int i = 0; i < shape.count; ++i) {
            shape.vertices[i].set(hull.points[i]);
        }
        for (int i = 0; i < shape.count; ++i) {
            int i2 = i + 1 < shape.count ? i + 1 : 0;
            b2Vec2 edge = b2Sub(shape.vertices[i2], shape.vertices[i]);
            shape.normals[i].set(b2Normalize(b2CrossVS(edge, 1.0f)));
        }
        shape.centroid = b2ComputePolygonCentroid(shape.vertices, shape.count);
        return shape;
    }

    public static b2Polygon b2MakeOffsetPolygon(b2Hull hull, b2Vec2 position, b2Rot rotation) {
        return b2MakeOffsetRoundedPolygon(hull, position, rotation, 0.0f);
    }

    public static b2Polygon b2MakeOffsetRoundedPolygon(b2Hull hull, b2Vec2 position, b2Rot rotation, float radius) {
        b2Polygon shape = b2MakePolygon(hull, radius);
        return b2TransformPolygon(new b2Transform(position, rotation), shape);
    }

    public static b2Polygon b2MakeSquare(float halfWidth) {
        return b2MakeBox(halfWidth, halfWidth);
    }

    public static b2Polygon b2MakeBox(float halfWidth, float halfHeight) {
        b2Polygon shape = new b2Polygon();
        shape.count = 4;
        shape.vertices[0].set(-halfWidth, -halfHeight);
        shape.vertices[1].set(halfWidth, -halfHeight);
        shape.vertices[2].set(halfWidth, halfHeight);
        shape.vertices[3].set(-halfWidth, halfHeight);
        shape.normals[0].set(0.0f, -1.0f);
        shape.normals[1].set(1.0f, 0.0f);
        shape.normals[2].set(0.0f, 1.0f);
        shape.normals[3].set(-1.0f, 0.0f);
        shape.radius = 0.0f;
        shape.centroid = new b2Vec2();
        return shape;
    }

    public static b2Polygon b2MakeRoundedBox(float halfWidth, float halfHeight, float radius) {
        b2Polygon shape = b2MakeBox(halfWidth, halfHeight);
        shape.radius = radius;
        return shape;
    }

    public static b2Polygon b2MakeOffsetBox(float halfWidth, float halfHeight, b2Vec2 center, b2Rot rotation) {
        return b2MakeOffsetRoundedBox(halfWidth, halfHeight, center, rotation, 0.0f);
    }

    public static b2Polygon b2MakeOffsetRoundedBox(float halfWidth, float halfHeight, b2Vec2 center, b2Rot rotation, float radius) {
        b2Transform xf = new b2Transform(center, rotation);
        b2Polygon shape = new b2Polygon();
        shape.count = 4;
        shape.vertices[0].set(b2TransformPoint(xf, new b2Vec2(-halfWidth, -halfHeight)));
        shape.vertices[1].set(b2TransformPoint(xf, new b2Vec2(halfWidth, -halfHeight)));
        shape.vertices[2].set(b2TransformPoint(xf, new b2Vec2(halfWidth, halfHeight)));
        shape.vertices[3].set(b2TransformPoint(xf, new b2Vec2(-halfWidth, halfHeight)));
        shape.normals[0].set(b2RotateVector(xf.q, new b2Vec2(0.0f, -1.0f)));
        shape.normals[1].set(b2RotateVector(xf.q, new b2Vec2(1.0f, 0.0f)));
        shape.normals[2].set(b2RotateVector(xf.q, new b2Vec2(0.0f, 1.0f)));
        shape.normals[3].set(b2RotateVector(xf.q, new b2Vec2(-1.0f, 0.0f)));
        shape.radius = radius;
        shape.centroid = xf.p.copy();
        return shape;
    }

    public static b2Polygon b2TransformPolygon(b2Transform transform, b2Polygon polygon) {
        b2Polygon p = new b2Polygon();
        p.count = polygon.count;
        p.radius = polygon.radius;
        for (int i = 0; i < p.count; ++i) {
            p.vertices[i].set(b2TransformPoint(transform, polygon.vertices[i]));
            p.normals[i].set(b2RotateVector(transform.q, polygon.normals[i]));
        }
        p.centroid = b2TransformPoint(transform, polygon.centroid);
        return p;
    }

    public static b2MassData b2ComputeCircleMass(b2Circle shape, float density) {
        float rr = shape.radius * shape.radius;
        b2MassData massData = new b2MassData();
        massData.mass = density * B2_PI * rr;
        massData.center = shape.center.copy();
        massData.rotationalInertia = massData.mass * (0.5f * rr + b2Dot(shape.center, shape.center));
        return massData;
    }

    public static b2MassData b2ComputeCapsuleMass(b2Capsule shape, float density) {
        float radius = shape.radius;
        float rr = radius * radius;
        b2Vec2 p1 = shape.center1;
        b2Vec2 p2 = shape.center2;
        float length = b2Length(b2Sub(p2, p1));
        float ll = length * length;
        float circleMass = density * (B2_PI * radius * radius);
        float boxMass = density * (2.0f * radius * length);
        b2MassData massData = new b2MassData();
        massData.mass = circleMass + boxMass;
        massData.center.x = 0.5f * (p1.x + p2.x);
        massData.center.y = 0.5f * (p1.y + p2.y);
        float lc = 4.0f * radius / (3.0f * B2_PI);
        float h = 0.5f * length;
        float circleInertia = circleMass * (0.5f * rr + h * h + 2.0f * h * lc);
        float boxInertia = boxMass * (4.0f * rr + ll) / 12.0f;
        massData.rotationalInertia = circleInertia + boxInertia;
        massData.rotationalInertia += massData.mass * b2Dot(massData.center, massData.center);
        return massData;
    }

    public static b2MassData b2ComputePolygonMass(b2Polygon shape, float density) {
        if (shape.count == 1) {
            return b2ComputeCircleMass(new b2Circle(shape.vertices[0], shape.radius), density);
        }
        if (shape.count == 2) {
            return b2ComputeCapsuleMass(new b2Capsule(shape.vertices[0], shape.vertices[1], shape.radius), density);
        }

        b2Vec2[] vertices = b2Vec2.array(B2_MAX_POLYGON_VERTICES);
        int count = shape.count;
        if (shape.radius > 0.0f) {
            float sqrt2 = 1.412f;
            for (int i = 0; i < count; ++i) {
                int j = i == 0 ? count - 1 : i - 1;
                b2Vec2 mid = b2Normalize(b2Add(shape.normals[j], shape.normals[i]));
                vertices[i].set(b2MulAdd(shape.vertices[i], sqrt2 * shape.radius, mid));
            }
        } else {
            for (int i = 0; i < count; ++i) {
                vertices[i].set(shape.vertices[i]);
            }
        }

        b2Vec2 center = new b2Vec2();
        float area = 0.0f;
        float rotationalInertia = 0.0f;
        b2Vec2 r = vertices[0];
        final float inv3 = 1.0f / 3.0f;
        for (int i = 1; i < count - 1; ++i) {
            b2Vec2 e1 = b2Sub(vertices[i], r);
            b2Vec2 e2 = b2Sub(vertices[i + 1], r);
            float d = b2Cross(e1, e2);
            float triangleArea = 0.5f * d;
            area += triangleArea;
            center = b2MulAdd(center, triangleArea * inv3, b2Add(e1, e2));
            float intx2 = e1.x * e1.x + e2.x * e1.x + e2.x * e2.x;
            float inty2 = e1.y * e1.y + e2.y * e1.y + e2.y * e2.y;
            rotationalInertia += (0.25f * inv3 * d) * (intx2 + inty2);
        }

        b2MassData massData = new b2MassData();
        massData.mass = density * area;
        float invArea = 1.0f / area;
        center.x *= invArea;
        center.y *= invArea;
        massData.center = b2Add(r, center);
        massData.rotationalInertia = density * rotationalInertia;
        massData.rotationalInertia += massData.mass * (b2Dot(massData.center, massData.center) - b2Dot(center, center));
        return massData;
    }

    public static b2AABB b2ComputeCircleAABB(b2Circle shape, b2Transform xf) {
        b2Vec2 p = b2TransformPoint(xf, shape.center);
        float r = shape.radius;
        return new b2AABB(new b2Vec2(p.x - r, p.y - r), new b2Vec2(p.x + r, p.y + r));
    }

    public static b2AABB b2ComputeCapsuleAABB(b2Capsule shape, b2Transform xf) {
        b2Vec2 v1 = b2TransformPoint(xf, shape.center1);
        b2Vec2 v2 = b2TransformPoint(xf, shape.center2);
        b2Vec2 r = new b2Vec2(shape.radius, shape.radius);
        return new b2AABB(b2Sub(b2Min(v1, v2), r), b2Add(b2Max(v1, v2), r));
    }

    public static b2AABB b2ComputePolygonAABB(b2Polygon shape, b2Transform xf) {
        b2Vec2 lower = b2TransformPoint(xf, shape.vertices[0]);
        b2Vec2 upper = lower.copy();
        for (int i = 1; i < shape.count; ++i) {
            b2Vec2 v = b2TransformPoint(xf, shape.vertices[i]);
            lower = b2Min(lower, v);
            upper = b2Max(upper, v);
        }
        b2Vec2 r = new b2Vec2(shape.radius, shape.radius);
        return new b2AABB(b2Sub(lower, r), b2Add(upper, r));
    }

    public static b2AABB b2ComputeSegmentAABB(b2Segment shape, b2Transform xf) {
        b2Vec2 v1 = b2TransformPoint(xf, shape.point1);
        b2Vec2 v2 = b2TransformPoint(xf, shape.point2);
        return new b2AABB(b2Min(v1, v2), b2Max(v1, v2));
    }

    public static boolean b2PointInCircle(b2Vec2 point, b2Circle shape) {
        return b2DistanceSquared(point, shape.center) <= shape.radius * shape.radius;
    }

    public static boolean b2PointInCapsule(b2Vec2 point, b2Capsule shape) {
        float rr = shape.radius * shape.radius;
        b2Vec2 d = b2Sub(shape.center2, shape.center1);
        float dd = b2Dot(d, d);
        if (dd == 0.0f) {
            return b2DistanceSquared(point, shape.center1) <= rr;
        }
        float t = b2Dot(b2Sub(point, shape.center1), d) / dd;
        t = b2ClampFloat(t, 0.0f, 1.0f);
        b2Vec2 c = b2MulAdd(shape.center1, t, d);
        return b2DistanceSquared(point, c) <= rr;
    }

    public static boolean b2PointInPolygon(b2Vec2 point, b2Polygon shape) {
        for (int i = 0; i < shape.count; ++i) {
            if (b2Dot(shape.normals[i], b2Sub(point, shape.vertices[i])) > shape.radius) {
                return false;
            }
        }
        return true;
    }

    public static b2Manifold b2CollideCircles(b2Circle circleA, b2Transform xfA, b2Circle circleB, b2Transform xfB) {
        b2Manifold manifold = new b2Manifold();
        b2Transform xf = b2InvMulTransforms(xfA, xfB);

        b2Vec2 pointA = circleA.center;
        b2Vec2 pointB = b2TransformPoint(xf, circleB.center);

        float[] distance = new float[1];
        b2Vec2 normal = b2GetLengthAndNormalize(distance, b2Sub(pointB, pointA));

        float separation = distance[0] - circleA.radius - circleB.radius;
        if (separation > B2_SPECULATIVE_DISTANCE()) {
            return manifold;
        }

        b2Vec2 cA = b2MulAdd(pointA, circleA.radius, normal);
        b2Vec2 cB = b2MulAdd(pointB, -circleB.radius, normal);
        b2Vec2 contactPointA = b2Lerp(cA, cB, 0.5f);

        manifold.normal = b2RotateVector(xfA.q, normal);
        b2ManifoldPoint mp = manifold.points[0];
        mp.anchorA = b2RotateVector(xfA.q, contactPointA);
        mp.anchorB = b2Add(mp.anchorA, b2Sub(xfA.p, xfB.p));
        mp.point = b2Add(mp.anchorA, xfA.p);
        mp.separation = separation;
        mp.id = 0;
        manifold.pointCount = 1;
        return manifold;
    }

    public static b2Manifold b2CollideCapsuleAndCircle(b2Capsule capsuleA, b2Transform xfA, b2Circle circleB, b2Transform xfB) {
        b2Manifold manifold = new b2Manifold();
        b2Transform xf = b2InvMulTransforms(xfA, xfB);

        b2Vec2 pB = b2TransformPoint(xf, circleB.center);
        b2Vec2 p1 = capsuleA.center1;
        b2Vec2 p2 = capsuleA.center2;
        b2Vec2 e = b2Sub(p2, p1);

        b2Vec2 pA;
        float s1 = b2Dot(b2Sub(pB, p1), e);
        float s2 = b2Dot(b2Sub(p2, pB), e);
        if (s1 < 0.0f) {
            pA = p1;
        } else if (s2 < 0.0f) {
            pA = p2;
        } else {
            float s = s1 / b2Dot(e, e);
            pA = b2MulAdd(p1, s, e);
        }

        float[] distance = new float[1];
        b2Vec2 normal = b2GetLengthAndNormalize(distance, b2Sub(pB, pA));

        float separation = distance[0] - capsuleA.radius - circleB.radius;
        if (separation > B2_SPECULATIVE_DISTANCE()) {
            return manifold;
        }

        b2Vec2 cA = b2MulAdd(pA, capsuleA.radius, normal);
        b2Vec2 cB = b2MulAdd(pB, -circleB.radius, normal);
        b2Vec2 contactPointA = b2Lerp(cA, cB, 0.5f);

        manifold.normal = b2RotateVector(xfA.q, normal);
        b2ManifoldPoint mp = manifold.points[0];
        mp.anchorA = b2RotateVector(xfA.q, contactPointA);
        mp.anchorB = b2Add(mp.anchorA, b2Sub(xfA.p, xfB.p));
        mp.point = b2Add(xfA.p, mp.anchorA);
        mp.separation = separation;
        mp.id = 0;
        manifold.pointCount = 1;
        return manifold;
    }

    public static b2Manifold b2CollideSegmentAndCircle(b2Segment segmentA, b2Transform xfA, b2Circle circleB, b2Transform xfB) {
        return b2CollideCapsuleAndCircle(new b2Capsule(segmentA.point1, segmentA.point2, 0.0f), xfA, circleB, xfB);
    }

    private static b2Polygon b2MakeCapsule(b2Vec2 p1, b2Vec2 p2, float radius) {
        b2Polygon shape = new b2Polygon();
        shape.vertices[0].set(p1);
        shape.vertices[1].set(p2);
        shape.centroid = b2Lerp(p1, p2, 0.5f);
        b2Vec2 axis = b2Normalize(b2Sub(p2, p1));
        b2Vec2 normal = b2RightPerp(axis);
        shape.normals[0].set(normal);
        shape.normals[1].set(b2Neg(normal));
        shape.count = 2;
        shape.radius = radius;
        return shape;
    }

    private static b2Polygon b2CopyPolygon(b2Polygon src) {
        b2Polygon dst = new b2Polygon();
        dst.count = src.count;
        dst.radius = src.radius;
        dst.centroid = src.centroid.copy();
        for (int i = 0; i < src.count; ++i) {
            dst.vertices[i].set(src.vertices[i]);
            dst.normals[i].set(src.normals[i]);
        }
        return dst;
    }

    private static void b2ConvertManifoldToWorld(b2Manifold manifold, b2Transform xfA, b2Transform xfB, b2Vec2 origin) {
        if (manifold.pointCount == 0) {
            return;
        }
        manifold.normal = b2RotateVector(xfA.q, manifold.normal);
        for (int i = 0; i < manifold.pointCount; ++i) {
            b2ManifoldPoint mp = manifold.points[i];
            mp.anchorA = b2RotateVector(xfA.q, b2Add(mp.anchorA, origin));
            mp.anchorB = b2Add(mp.anchorA, b2Sub(xfA.p, xfB.p));
            mp.point = b2Add(xfA.p, mp.anchorA);
        }
    }

    public static b2Manifold b2CollidePolygonAndCircle(b2Polygon polygonA, b2Transform xfA, b2Circle circleB, b2Transform xfB) {
        b2Manifold manifold = new b2Manifold();
        float speculativeDistance = B2_SPECULATIVE_DISTANCE();
        b2Transform xf = b2InvMulTransforms(xfA, xfB);

        b2Vec2 center = b2TransformPoint(xf, circleB.center);
        float radiusA = polygonA.radius;
        float radiusB = circleB.radius;
        float radius = radiusA + radiusB;

        int normalIndex = 0;
        float separation = -Float.MAX_VALUE;
        int vertexCount = polygonA.count;
        for (int i = 0; i < vertexCount; ++i) {
            float s = b2Dot(polygonA.normals[i], b2Sub(center, polygonA.vertices[i]));
            if (s > separation) {
                separation = s;
                normalIndex = i;
            }
        }

        if (separation > radius + speculativeDistance) {
            return manifold;
        }

        int vertIndex1 = normalIndex;
        int vertIndex2 = vertIndex1 + 1 < vertexCount ? vertIndex1 + 1 : 0;
        b2Vec2 v1 = polygonA.vertices[vertIndex1];
        b2Vec2 v2 = polygonA.vertices[vertIndex2];

        float u1 = b2Dot(b2Sub(center, v1), b2Sub(v2, v1));
        float u2 = b2Dot(b2Sub(center, v2), b2Sub(v1, v2));

        if (u1 < 0.0f && separation > Math.ulp(1.0f)) {
            b2Vec2 normal = b2Normalize(b2Sub(center, v1));
            separation = b2Dot(b2Sub(center, v1), normal);
            if (separation > radius + speculativeDistance) {
                return manifold;
            }

            b2Vec2 cA = b2MulAdd(v1, radiusA, normal);
            b2Vec2 cB = b2MulSub(center, radiusB, normal);
            b2Vec2 contactPointA = b2Lerp(cA, cB, 0.5f);

            manifold.normal = b2RotateVector(xfA.q, normal);
            b2ManifoldPoint mp = manifold.points[0];
            mp.anchorA = b2RotateVector(xfA.q, contactPointA);
            mp.anchorB = b2Add(mp.anchorA, b2Sub(xfA.p, xfB.p));
            mp.point = b2Add(xfA.p, mp.anchorA);
            mp.separation = b2Dot(b2Sub(cB, cA), normal);
            mp.id = 0;
            manifold.pointCount = 1;
        } else if (u2 < 0.0f && separation > Math.ulp(1.0f)) {
            b2Vec2 normal = b2Normalize(b2Sub(center, v2));
            separation = b2Dot(b2Sub(center, v2), normal);
            if (separation > radius + speculativeDistance) {
                return manifold;
            }

            b2Vec2 cA = b2MulAdd(v2, radiusA, normal);
            b2Vec2 cB = b2MulSub(center, radiusB, normal);
            b2Vec2 contactPointA = b2Lerp(cA, cB, 0.5f);

            manifold.normal = b2RotateVector(xfA.q, normal);
            b2ManifoldPoint mp = manifold.points[0];
            mp.anchorA = b2RotateVector(xfA.q, contactPointA);
            mp.anchorB = b2Add(mp.anchorA, b2Sub(xfA.p, xfB.p));
            mp.point = b2Add(xfA.p, mp.anchorA);
            mp.separation = b2Dot(b2Sub(cB, cA), normal);
            mp.id = 0;
            manifold.pointCount = 1;
        } else {
            b2Vec2 normal = polygonA.normals[normalIndex];
            manifold.normal = b2RotateVector(xfA.q, normal);

            b2Vec2 cA = b2MulAdd(center, radiusA - b2Dot(b2Sub(center, v1), normal), normal);
            b2Vec2 cB = b2MulSub(center, radiusB, normal);
            b2Vec2 contactPointA = b2Lerp(cA, cB, 0.5f);

            b2ManifoldPoint mp = manifold.points[0];
            mp.anchorA = b2RotateVector(xfA.q, contactPointA);
            mp.anchorB = b2Add(mp.anchorA, b2Sub(xfA.p, xfB.p));
            mp.point = b2Add(xfA.p, mp.anchorA);
            mp.separation = separation - radius;
            mp.id = 0;
            manifold.pointCount = 1;
        }

        return manifold;
    }

    public static b2Manifold b2CollideCapsules(b2Capsule capsuleA, b2Transform xfA, b2Capsule capsuleB, b2Transform xfB) {
        b2Vec2 origin = capsuleA.center1;
        b2Transform sfA = new b2Transform(b2Add(xfA.p, b2RotateVector(xfA.q, origin)), xfA.q);
        b2Transform xf = b2InvMulTransforms(sfA, xfB);

        b2Vec2 p1 = b2Vec2_zero.copy();
        b2Vec2 q1 = b2Sub(capsuleA.center2, origin);
        b2Vec2 p2 = b2TransformPoint(xf, capsuleB.center1);
        b2Vec2 q2 = b2TransformPoint(xf, capsuleB.center2);

        b2Vec2 d1 = b2Sub(q1, p1);
        b2Vec2 d2 = b2Sub(q2, p2);
        float dd1 = b2Dot(d1, d1);
        float dd2 = b2Dot(d2, d2);
        final float epsSqr = Math.ulp(1.0f) * Math.ulp(1.0f);

        b2Vec2 r = b2Sub(p1, p2);
        float rd1 = b2Dot(r, d1);
        float rd2 = b2Dot(r, d2);
        float d12 = b2Dot(d1, d2);
        float denom = dd1 * dd2 - d12 * d12;

        float f1 = 0.0f;
        if (denom != 0.0f) {
            f1 = b2ClampFloat((d12 * rd2 - rd1 * dd2) / denom, 0.0f, 1.0f);
        }

        float f2 = (d12 * f1 + rd2) / dd2;
        if (f2 < 0.0f) {
            f2 = 0.0f;
            f1 = b2ClampFloat(-rd1 / dd1, 0.0f, 1.0f);
        } else if (f2 > 1.0f) {
            f2 = 1.0f;
            f1 = b2ClampFloat((d12 - rd1) / dd1, 0.0f, 1.0f);
        }

        b2Vec2 closest1 = b2MulAdd(p1, f1, d1);
        b2Vec2 closest2 = b2MulAdd(p2, f2, d2);
        float distanceSquared = b2DistanceSquared(closest1, closest2);

        b2Manifold manifold = new b2Manifold();
        float radiusA = capsuleA.radius;
        float radiusB = capsuleB.radius;
        float radius = radiusA + radiusB;
        float maxDistance = radius + B2_SPECULATIVE_DISTANCE();
        if (distanceSquared > maxDistance * maxDistance) {
            return manifold;
        }

        float distance = (float) Math.sqrt(distanceSquared);
        float[] length1 = new float[1];
        float[] length2 = new float[1];
        b2Vec2 u1 = b2GetLengthAndNormalize(length1, d1);
        b2Vec2 u2 = b2GetLengthAndNormalize(length2, d2);

        float fp2 = b2Dot(b2Sub(p2, p1), u1);
        float fq2 = b2Dot(b2Sub(q2, p1), u1);
        boolean outsideA = (fp2 <= 0.0f && fq2 <= 0.0f) || (fp2 >= length1[0] && fq2 >= length1[0]);

        float fp1 = b2Dot(b2Sub(p1, p2), u2);
        float fq1 = b2Dot(b2Sub(q1, p2), u2);
        boolean outsideB = (fp1 <= 0.0f && fq1 <= 0.0f) || (fp1 >= length2[0] && fq1 >= length2[0]);

        if (!outsideA && !outsideB) {
            b2Vec2 normalA;
            float separationA;
            {
                normalA = b2LeftPerp(u1);
                float ss1 = b2Dot(b2Sub(p2, p1), normalA);
                float ss2 = b2Dot(b2Sub(q2, p1), normalA);
                float s1p = ss1 < ss2 ? ss1 : ss2;
                float s1n = -ss1 < -ss2 ? -ss1 : -ss2;
                if (s1p > s1n) {
                    separationA = s1p;
                } else {
                    separationA = s1n;
                    normalA = b2Neg(normalA);
                }
            }

            b2Vec2 normalB;
            float separationB;
            {
                normalB = b2LeftPerp(u2);
                float ss1 = b2Dot(b2Sub(p1, p2), normalB);
                float ss2 = b2Dot(b2Sub(q1, p2), normalB);
                float s1p = ss1 < ss2 ? ss1 : ss2;
                float s1n = -ss1 < -ss2 ? -ss1 : -ss2;
                if (s1p > s1n) {
                    separationB = s1p;
                } else {
                    separationB = s1n;
                    normalB = b2Neg(normalB);
                }
            }

            if (separationA + 0.1f * B2_LINEAR_SLOP() >= separationB) {
                manifold.normal = normalA;
                b2Vec2 cp = p2;
                b2Vec2 cq = q2;

                if (fp2 < 0.0f && fq2 > 0.0f) {
                    cp = b2Lerp(p2, q2, (0.0f - fp2) / (fq2 - fp2));
                } else if (fq2 < 0.0f && fp2 > 0.0f) {
                    cq = b2Lerp(q2, p2, (0.0f - fq2) / (fp2 - fq2));
                }

                if (fp2 > length1[0] && fq2 < length1[0]) {
                    cp = b2Lerp(p2, q2, (fp2 - length1[0]) / (fp2 - fq2));
                } else if (fq2 > length1[0] && fp2 < length1[0]) {
                    cq = b2Lerp(q2, p2, (fq2 - length1[0]) / (fq2 - fp2));
                }

                float sp = b2Dot(b2Sub(cp, p1), normalA);
                float sq = b2Dot(b2Sub(cq, p1), normalA);
                if (sp <= distance + B2_LINEAR_SLOP() || sq <= distance + B2_LINEAR_SLOP()) {
                    b2ManifoldPoint mp = manifold.points[0];
                    mp.anchorA = b2MulAdd(cp, 0.5f * (radiusA - radiusB - sp), normalA);
                    mp.separation = sp - radius;
                    mp.id = B2_MAKE_ID(0, 0);

                    mp = manifold.points[1];
                    mp.anchorA = b2MulAdd(cq, 0.5f * (radiusA - radiusB - sq), normalA);
                    mp.separation = sq - radius;
                    mp.id = B2_MAKE_ID(0, 1);
                    manifold.pointCount = 2;
                }
            } else {
                manifold.normal = b2Neg(normalB);
                b2Vec2 cp = p1;
                b2Vec2 cq = q1;

                if (fp1 < 0.0f && fq1 > 0.0f) {
                    cp = b2Lerp(p1, q1, (0.0f - fp1) / (fq1 - fp1));
                } else if (fq1 < 0.0f && fp1 > 0.0f) {
                    cq = b2Lerp(q1, p1, (0.0f - fq1) / (fp1 - fq1));
                }

                if (fp1 > length2[0] && fq1 < length2[0]) {
                    cp = b2Lerp(p1, q1, (fp1 - length2[0]) / (fp1 - fq1));
                } else if (fq1 > length2[0] && fp1 < length2[0]) {
                    cq = b2Lerp(q1, p1, (fq1 - length2[0]) / (fq1 - fp1));
                }

                float sp = b2Dot(b2Sub(cp, p2), normalB);
                float sq = b2Dot(b2Sub(cq, p2), normalB);
                if (sp <= distance + B2_LINEAR_SLOP() || sq <= distance + B2_LINEAR_SLOP()) {
                    b2ManifoldPoint mp = manifold.points[0];
                    mp.anchorA = b2MulAdd(cp, 0.5f * (radiusB - radiusA - sp), normalB);
                    mp.separation = sp - radius;
                    mp.id = B2_MAKE_ID(0, 0);

                    mp = manifold.points[1];
                    mp.anchorA = b2MulAdd(cq, 0.5f * (radiusB - radiusA - sq), normalB);
                    mp.separation = sq - radius;
                    mp.id = B2_MAKE_ID(1, 0);
                    manifold.pointCount = 2;
                }
            }
        }

        if (manifold.pointCount == 0) {
            b2Vec2 normal = b2Sub(closest2, closest1);
            if (b2Dot(normal, normal) > epsSqr) {
                normal = b2Normalize(normal);
            } else {
                normal = b2LeftPerp(u1);
            }

            b2Vec2 c1 = b2MulAdd(closest1, radiusA, normal);
            b2Vec2 c2 = b2MulAdd(closest2, -radiusB, normal);

            int i1 = f1 == 0.0f ? 0 : 1;
            int i2 = f2 == 0.0f ? 0 : 1;

            manifold.normal = normal;
            manifold.points[0].anchorA = b2Lerp(c1, c2, 0.5f);
            manifold.points[0].separation = (float) Math.sqrt(distanceSquared) - radius;
            manifold.points[0].id = B2_MAKE_ID(i1, i2);
            manifold.pointCount = 1;
        }

        b2ConvertManifoldToWorld(manifold, xfA, xfB, origin);
        return manifold;
    }

    public static b2Manifold b2CollideSegmentAndCapsule(b2Segment segmentA, b2Transform xfA, b2Capsule capsuleB, b2Transform xfB) {
        return b2CollideCapsules(new b2Capsule(segmentA.point1, segmentA.point2, 0.0f), xfA, capsuleB, xfB);
    }

    public static b2Manifold b2CollideSegmentAndPolygon(b2Segment segmentA, b2Transform xfA, b2Polygon polygonB, b2Transform xfB) {
        return b2CollidePolygons(b2MakeCapsule(segmentA.point1, segmentA.point2, 0.0f), xfA, polygonB, xfB);
    }

    public static b2Manifold b2CollidePolygonAndCapsule(b2Polygon polygonA, b2Transform xfA, b2Capsule capsuleB, b2Transform xfB) {
        return b2CollidePolygons(polygonA, xfA, b2MakeCapsule(capsuleB.center1, capsuleB.center2, capsuleB.radius), xfB);
    }

    public static b2Manifold b2CollideChainSegmentAndCircle(b2ChainSegment segmentA, b2Transform xfA, b2Circle circleB,
                                                             b2Transform xfB) {
        b2Manifold manifold = new b2Manifold();
        b2Transform xf = b2InvMulTransforms(xfA, xfB);
        b2Vec2 pB = b2TransformPoint(xf, circleB.center);

        b2Vec2 p1 = segmentA.segment.point1;
        b2Vec2 p2 = segmentA.segment.point2;
        b2Vec2 e = b2Sub(p2, p1);

        float offset = b2Dot(b2RightPerp(e), b2Sub(pB, p1));
        if (offset < 0.0f) {
            return manifold;
        }

        float u = b2Dot(e, b2Sub(p2, pB));
        float v = b2Dot(e, b2Sub(pB, p1));

        b2Vec2 pA;
        if (v <= 0.0f) {
            b2Vec2 prevEdge = b2Sub(p1, segmentA.ghost1);
            float uPrev = b2Dot(prevEdge, b2Sub(pB, p1));
            if (uPrev <= 0.0f) {
                return manifold;
            }
            pA = p1;
        } else if (u <= 0.0f) {
            b2Vec2 nextEdge = b2Sub(segmentA.ghost2, p2);
            float vNext = b2Dot(nextEdge, b2Sub(pB, p2));
            if (vNext > 0.0f) {
                return manifold;
            }
            pA = p2;
        } else {
            float ee = b2Dot(e, e);
            pA = new b2Vec2(u * p1.x + v * p2.x, u * p1.y + v * p2.y);
            pA = ee > 0.0f ? b2MulSV(1.0f / ee, pA) : p1;
        }

        float[] distanceRef = new float[1];
        b2Vec2 normal = b2GetLengthAndNormalize(distanceRef, b2Sub(pB, pA));
        float radius = circleB.radius;
        float separation = distanceRef[0] - radius;
        if (separation > B2_SPECULATIVE_DISTANCE()) {
            return manifold;
        }

        b2Vec2 cA = pA;
        b2Vec2 cB = b2MulAdd(pB, -radius, normal);
        b2Vec2 contactPointA = b2Lerp(cA, cB, 0.5f);

        manifold.normal = b2RotateVector(xfA.q, normal);
        b2ManifoldPoint mp = manifold.points[0];
        mp.anchorA = b2RotateVector(xfA.q, contactPointA);
        mp.anchorB = b2Add(mp.anchorA, b2Sub(xfA.p, xfB.p));
        mp.point = b2Add(xfA.p, mp.anchorA);
        mp.separation = separation;
        mp.id = 0;
        manifold.pointCount = 1;
        return manifold;
    }

    public static b2Manifold b2CollideChainSegmentAndCapsule(b2ChainSegment segmentA, b2Transform xfA, b2Capsule capsuleB,
                                                              b2Transform xfB, b2SimplexCache cache) {
        b2Polygon polyB = b2MakeCapsule(capsuleB.center1, capsuleB.center2, capsuleB.radius);
        return b2CollideChainSegmentAndPolygon(segmentA, xfA, polyB, xfB, cache);
    }

    private static b2Manifold b2ClipSegments(b2Vec2 a1, b2Vec2 a2, b2Vec2 b1, b2Vec2 b2, b2Vec2 normal,
                                             float ra, float rb, int id1, int id2) {
        b2Manifold manifold = new b2Manifold();
        b2Vec2 tangent = b2LeftPerp(normal);

        float lower1 = 0.0f;
        float upper1 = b2Dot(b2Sub(a2, a1), tangent);
        float upper2 = b2Dot(b2Sub(b1, a1), tangent);
        float lower2 = b2Dot(b2Sub(b2, a1), tangent);

        if (upper2 < lower1 || upper1 < lower2) {
            return manifold;
        }

        b2Vec2 vLower;
        if (lower2 < lower1 && upper2 - lower2 > Math.ulp(1.0f)) {
            vLower = b2Lerp(b2, b1, (lower1 - lower2) / (upper2 - lower2));
        } else {
            vLower = b2;
        }

        b2Vec2 vUpper;
        if (upper2 > upper1 && upper2 - lower2 > Math.ulp(1.0f)) {
            vUpper = b2Lerp(b2, b1, (upper1 - lower2) / (upper2 - lower2));
        } else {
            vUpper = b1;
        }

        float separationLower = b2Dot(b2Sub(vLower, a1), normal);
        float separationUpper = b2Dot(b2Sub(vUpper, a1), normal);

        vLower = b2MulAdd(vLower, 0.5f * (ra - rb - separationLower), normal);
        vUpper = b2MulAdd(vUpper, 0.5f * (ra - rb - separationUpper), normal);

        float radius = ra + rb;
        manifold.normal = normal;
        b2ManifoldPoint cp = manifold.points[0];
        cp.anchorA = vLower;
        cp.separation = separationLower - radius;
        cp.id = id1;

        cp = manifold.points[1];
        cp.anchorA = vUpper;
        cp.separation = separationUpper - radius;
        cp.id = id2;

        manifold.pointCount = 2;
        return manifold;
    }

    private static final int B2_NORMAL_SKIP = 0;
    private static final int B2_NORMAL_ADMIT = 1;
    private static final int B2_NORMAL_SNAP = 2;

    private static final class b2ChainSegmentParams {
        b2Vec2 edge1 = new b2Vec2();
        b2Vec2 normal0 = new b2Vec2();
        b2Vec2 normal2 = new b2Vec2();
        boolean convex1;
        boolean convex2;
    }

    private static int b2ClassifyNormal(b2ChainSegmentParams params, b2Vec2 normal) {
        final float sinTol = 0.01f;

        if (b2Dot(normal, params.edge1) <= 0.0f) {
            if (params.convex1) {
                if (b2Cross(normal, params.normal0) > sinTol) {
                    return B2_NORMAL_SKIP;
                }
                return B2_NORMAL_ADMIT;
            }
            return B2_NORMAL_SNAP;
        }

        if (params.convex2) {
            if (b2Cross(params.normal2, normal) > sinTol) {
                return B2_NORMAL_SKIP;
            }
            return B2_NORMAL_ADMIT;
        }
        return B2_NORMAL_SNAP;
    }

    public static b2Manifold b2CollideChainSegmentAndPolygon(b2ChainSegment segmentA, b2Transform xfA, b2Polygon polygonB,
                                                              b2Transform xfB, b2SimplexCache cache) {
        b2Manifold manifold = new b2Manifold();
        b2Transform xf = b2InvMulTransforms(xfA, xfB);

        b2Vec2 centroidB = b2TransformPoint(xf, polygonB.centroid);
        float radiusB = polygonB.radius;

        b2Vec2 p1 = segmentA.segment.point1;
        b2Vec2 p2 = segmentA.segment.point2;
        b2Vec2 edge1 = b2Normalize(b2Sub(p2, p1));

        b2ChainSegmentParams smoothParams = new b2ChainSegmentParams();
        smoothParams.edge1 = edge1;

        final float convexTol = 0.01f;
        b2Vec2 edge0 = b2Normalize(b2Sub(p1, segmentA.ghost1));
        smoothParams.normal0 = b2RightPerp(edge0);
        smoothParams.convex1 = b2Cross(edge0, edge1) >= convexTol;

        b2Vec2 edge2 = b2Normalize(b2Sub(segmentA.ghost2, p2));
        smoothParams.normal2 = b2RightPerp(edge2);
        smoothParams.convex2 = b2Cross(edge1, edge2) >= convexTol;

        b2Vec2 normal1 = b2RightPerp(edge1);
        boolean behind1 = b2Dot(normal1, b2Sub(centroidB, p1)) < 0.0f;
        boolean behind0 = true;
        boolean behind2 = true;
        if (smoothParams.convex1) {
            behind0 = b2Dot(smoothParams.normal0, b2Sub(centroidB, p1)) < 0.0f;
        }
        if (smoothParams.convex2) {
            behind2 = b2Dot(smoothParams.normal2, b2Sub(centroidB, p2)) < 0.0f;
        }
        if (behind1 && behind0 && behind2) {
            return manifold;
        }

        int count = polygonB.count;
        b2Vec2[] vertices = b2Vec2.array(B2_MAX_POLYGON_VERTICES);
        b2Vec2[] normals = b2Vec2.array(B2_MAX_POLYGON_VERTICES);
        for (int i = 0; i < count; ++i) {
            vertices[i].set(b2TransformPoint(xf, polygonB.vertices[i]));
            normals[i].set(b2RotateVector(xf.q, polygonB.normals[i]));
        }

        b2DistanceInput input = new b2DistanceInput();
        input.proxyA = b2MakeProxy(new b2Vec2[] {segmentA.segment.point1, segmentA.segment.point2}, 2, 0.0f);
        input.proxyB = b2MakeProxy(vertices, count, 0.0f);
        input.transformA = b2Transform_identity;
        input.transformB = b2Transform_identity;
        input.useRadii = false;

        b2DistanceOutput output = b2ShapeDistance(input, cache, null, 0);
        if (output.distance > radiusB + B2_SPECULATIVE_DISTANCE()) {
            return manifold;
        }

        b2Vec2 n0 = smoothParams.convex1 ? smoothParams.normal0 : normal1;
        b2Vec2 n2 = smoothParams.convex2 ? smoothParams.normal2 : normal1;

        int incidentIndex = -1;
        int incidentNormal = -1;

        if (!behind1 && output.distance > 0.1f * B2_LINEAR_SLOP()) {
            if (cache.count == 1) {
                b2Vec2 pA = output.pointA;
                b2Vec2 pB = output.pointB;
                b2Vec2 normal = b2Normalize(b2Sub(pB, pA));

                int type = b2ClassifyNormal(smoothParams, normal);
                if (type == B2_NORMAL_SKIP) {
                    return manifold;
                }

                if (type == B2_NORMAL_ADMIT) {
                    manifold.normal = b2RotateVector(xfA.q, normal);
                    b2ManifoldPoint cp = manifold.points[0];
                    cp.anchorA = b2RotateVector(xfA.q, pA);
                    cp.anchorB = b2Add(cp.anchorA, b2Sub(xfA.p, xfB.p));
                    cp.point = b2Add(xfA.p, cp.anchorA);
                    cp.separation = output.distance - radiusB;
                    cp.id = B2_MAKE_ID(cache.indexA[0] & 0xFF, cache.indexB[0] & 0xFF);
                    manifold.pointCount = 1;
                    return manifold;
                }

                incidentIndex = cache.indexB[0] & 0xFF;
            } else {
                int ia1 = cache.indexA[0] & 0xFF;
                int ia2 = cache.indexA[1] & 0xFF;
                int ib1 = cache.indexB[0] & 0xFF;
                int ib2 = cache.indexB[1] & 0xFF;

                if (ia1 == ia2) {
                    b2Vec2 normalB = b2Sub(output.pointA, output.pointB);
                    float dot1 = b2Dot(normalB, normals[ib1]);
                    float dot2 = b2Dot(normalB, normals[ib2]);
                    int ib = dot1 > dot2 ? ib1 : ib2;

                    normalB = normals[ib];
                    int type = b2ClassifyNormal(smoothParams, b2Neg(normalB));
                    if (type == B2_NORMAL_SKIP) {
                        return manifold;
                    }

                    if (type == B2_NORMAL_ADMIT) {
                        ib1 = ib;
                        ib2 = ib < count - 1 ? ib + 1 : 0;
                        b2Vec2 b1 = vertices[ib1];
                        b2Vec2 b2 = vertices[ib2];

                        dot1 = b2Dot(normalB, b2Sub(p1, b1));
                        dot2 = b2Dot(normalB, b2Sub(p2, b1));

                        if (dot1 < dot2) {
                            if (b2Dot(n0, normalB) < b2Dot(normal1, normalB)) {
                                return manifold;
                            }
                        } else if (b2Dot(n2, normalB) < b2Dot(normal1, normalB)) {
                            return manifold;
                        }

                        manifold = b2ClipSegments(b1, b2, p1, p2, normalB, radiusB, 0.0f,
                            B2_MAKE_ID(ib1, 1), B2_MAKE_ID(ib2, 0));
                        if (manifold.pointCount == 2) {
                            b2FinishChainManifold(manifold, xfA, xfB, b2Neg(normalB));
                        }
                        return manifold;
                    }

                    incidentNormal = ib;
                } else {
                    float dot1 = b2Dot(normal1, b2Sub(vertices[ib1], p1));
                    float dot2 = b2Dot(normal1, b2Sub(vertices[ib2], p2));
                    incidentIndex = dot1 < dot2 ? ib1 : ib2;
                }
            }
        } else {
            float edgeSeparation = Float.MAX_VALUE;
            for (int i = 0; i < count; ++i) {
                float s = b2Dot(normal1, b2Sub(vertices[i], p1));
                if (s < edgeSeparation) {
                    edgeSeparation = s;
                    incidentIndex = i;
                }
            }

            if (smoothParams.convex1) {
                float s0 = Float.MAX_VALUE;
                for (int i = 0; i < count; ++i) {
                    float s = b2Dot(smoothParams.normal0, b2Sub(vertices[i], p1));
                    if (s < s0) {
                        s0 = s;
                    }
                }
                if (s0 > edgeSeparation) {
                    edgeSeparation = s0;
                    incidentIndex = -1;
                }
            }

            if (smoothParams.convex2) {
                float s2 = Float.MAX_VALUE;
                for (int i = 0; i < count; ++i) {
                    float s = b2Dot(smoothParams.normal2, b2Sub(vertices[i], p2));
                    if (s < s2) {
                        s2 = s;
                    }
                }
                if (s2 > edgeSeparation) {
                    edgeSeparation = s2;
                    incidentIndex = -1;
                }
            }

            float polygonSeparation = -Float.MAX_VALUE;
            int referenceIndex = -1;
            for (int i = 0; i < count; ++i) {
                b2Vec2 n = normals[i];
                int type = b2ClassifyNormal(smoothParams, b2Neg(n));
                if (type != B2_NORMAL_ADMIT) {
                    continue;
                }

                b2Vec2 p = vertices[i];
                float s = b2MinFloat(b2Dot(n, b2Sub(p2, p)), b2Dot(n, b2Sub(p1, p)));
                if (s > polygonSeparation) {
                    polygonSeparation = s;
                    referenceIndex = i;
                }
            }

            if (polygonSeparation > edgeSeparation) {
                int ia1 = referenceIndex;
                int ia2 = ia1 < count - 1 ? ia1 + 1 : 0;
                b2Vec2 a1 = vertices[ia1];
                b2Vec2 a2 = vertices[ia2];
                b2Vec2 n = normals[ia1];

                float dot1 = b2Dot(n, b2Sub(p1, a1));
                float dot2 = b2Dot(n, b2Sub(p2, a1));

                if (dot1 < dot2) {
                    if (b2Dot(n0, n) < b2Dot(normal1, n)) {
                        return manifold;
                    }
                } else if (b2Dot(n2, n) < b2Dot(normal1, n)) {
                    return manifold;
                }

                manifold = b2ClipSegments(a1, a2, p1, p2, normals[ia1], radiusB, 0.0f,
                    B2_MAKE_ID(ia1, 1), B2_MAKE_ID(ia2, 0));
                if (manifold.pointCount == 2) {
                    b2FinishChainManifold(manifold, xfA, xfB, b2Neg(normals[ia1]));
                }
                return manifold;
            }

            if (incidentIndex == -1) {
                return manifold;
            }
        }

        b2Vec2 b1;
        b2Vec2 b2;
        int ib1;
        int ib2;

        if (incidentNormal != -1) {
            ib1 = incidentNormal;
            ib2 = ib1 < count - 1 ? ib1 + 1 : 0;
            b1 = vertices[ib1];
            b2 = vertices[ib2];
        } else {
            int i2 = incidentIndex;
            int i1 = i2 > 0 ? i2 - 1 : count - 1;
            float d1 = b2Dot(normal1, normals[i1]);
            float d2 = b2Dot(normal1, normals[i2]);
            if (d1 < d2) {
                ib1 = i1;
                ib2 = i2;
                b1 = vertices[ib1];
                b2 = vertices[ib2];
            } else {
                ib1 = i2;
                ib2 = i2 < count - 1 ? i2 + 1 : 0;
                b1 = vertices[ib1];
                b2 = vertices[ib2];
            }
        }

        manifold = b2ClipSegments(p1, p2, b1, b2, normal1, 0.0f, radiusB, B2_MAKE_ID(0, ib2), B2_MAKE_ID(1, ib1));
        if (manifold.pointCount == 2) {
            b2FinishChainManifold(manifold, xfA, xfB, manifold.normal);
        }
        return manifold;
    }

    private static void b2FinishChainManifold(b2Manifold manifold, b2Transform xfA, b2Transform xfB, b2Vec2 localNormal) {
        manifold.normal = b2RotateVector(xfA.q, localNormal);
        manifold.points[0].anchorA = b2RotateVector(xfA.q, manifold.points[0].anchorA);
        manifold.points[1].anchorA = b2RotateVector(xfA.q, manifold.points[1].anchorA);
        b2Vec2 pAB = b2Sub(xfA.p, xfB.p);
        manifold.points[0].anchorB = b2Add(manifold.points[0].anchorA, pAB);
        manifold.points[1].anchorB = b2Add(manifold.points[1].anchorA, pAB);
        manifold.points[0].point = b2Add(xfA.p, manifold.points[0].anchorA);
        manifold.points[1].point = b2Add(xfA.p, manifold.points[1].anchorA);
    }

    private static b2Manifold b2ClipPolygons(b2Polygon polyA, b2Polygon polyB, int edgeA, int edgeB, boolean flip) {
        b2Manifold manifold = new b2Manifold();

        b2Polygon poly1;
        b2Polygon poly2;
        int i11;
        int i12;
        int i21;
        int i22;

        if (flip) {
            poly1 = polyB;
            poly2 = polyA;
            i11 = edgeB;
            i12 = edgeB + 1 < polyB.count ? edgeB + 1 : 0;
            i21 = edgeA;
            i22 = edgeA + 1 < polyA.count ? edgeA + 1 : 0;
        } else {
            poly1 = polyA;
            poly2 = polyB;
            i11 = edgeA;
            i12 = edgeA + 1 < polyA.count ? edgeA + 1 : 0;
            i21 = edgeB;
            i22 = edgeB + 1 < polyB.count ? edgeB + 1 : 0;
        }

        b2Vec2 normal = poly1.normals[i11];
        b2Vec2 v11 = poly1.vertices[i11];
        b2Vec2 v12 = poly1.vertices[i12];
        b2Vec2 v21 = poly2.vertices[i21];
        b2Vec2 v22 = poly2.vertices[i22];
        b2Vec2 tangent = b2CrossSV(1.0f, normal);

        float lower1 = 0.0f;
        float upper1 = b2Dot(b2Sub(v12, v11), tangent);
        float upper2 = b2Dot(b2Sub(v21, v11), tangent);
        float lower2 = b2Dot(b2Sub(v22, v11), tangent);

        if (upper2 < lower1 || upper1 < lower2) {
            return manifold;
        }

        b2Vec2 vLower;
        if (lower2 < lower1 && upper2 - lower2 > Math.ulp(1.0f)) {
            vLower = b2Lerp(v22, v21, (lower1 - lower2) / (upper2 - lower2));
        } else {
            vLower = v22;
        }

        b2Vec2 vUpper;
        if (upper2 > upper1 && upper2 - lower2 > Math.ulp(1.0f)) {
            vUpper = b2Lerp(v22, v21, (upper1 - lower2) / (upper2 - lower2));
        } else {
            vUpper = v21;
        }

        float separationLower = b2Dot(b2Sub(vLower, v11), normal);
        float separationUpper = b2Dot(b2Sub(vUpper, v11), normal);

        float r1 = poly1.radius;
        float r2 = poly2.radius;

        vLower = b2MulAdd(vLower, 0.5f * (r1 - r2 - separationLower), normal);
        vUpper = b2MulAdd(vUpper, 0.5f * (r1 - r2 - separationUpper), normal);

        float radius = r1 + r2;
        if (!flip) {
            manifold.normal = normal.copy();
            b2ManifoldPoint cp = manifold.points[0];
            cp.anchorA = vLower;
            cp.separation = separationLower - radius;
            cp.id = B2_MAKE_ID(i11, i22);
            manifold.pointCount += 1;

            cp = manifold.points[1];
            cp.anchorA = vUpper;
            cp.separation = separationUpper - radius;
            cp.id = B2_MAKE_ID(i12, i21);
            manifold.pointCount += 1;
        } else {
            manifold.normal = b2Neg(normal);
            b2ManifoldPoint cp = manifold.points[0];
            cp.anchorA = vUpper;
            cp.separation = separationUpper - radius;
            cp.id = B2_MAKE_ID(i21, i12);
            manifold.pointCount += 1;

            cp = manifold.points[1];
            cp.anchorA = vLower;
            cp.separation = separationLower - radius;
            cp.id = B2_MAKE_ID(i22, i11);
            manifold.pointCount += 1;
        }

        return manifold;
    }

    private static float b2FindMaxSeparation(int[] edgeIndex, b2Polygon poly1, b2Polygon poly2) {
        int bestIndex = 0;
        float maxSeparation = -Float.MAX_VALUE;
        for (int i = 0; i < poly1.count; ++i) {
            b2Vec2 n = poly1.normals[i];
            b2Vec2 v1 = poly1.vertices[i];
            float si = Float.MAX_VALUE;
            for (int j = 0; j < poly2.count; ++j) {
                float sij = b2Dot(n, b2Sub(poly2.vertices[j], v1));
                if (sij < si) {
                    si = sij;
                }
            }
            if (si > maxSeparation) {
                maxSeparation = si;
                bestIndex = i;
            }
        }
        edgeIndex[0] = bestIndex;
        return maxSeparation;
    }

    public static b2Manifold b2CollidePolygons(b2Polygon polygonA, b2Transform xfA, b2Polygon polygonB, b2Transform xfB) {
        b2Vec2 origin = polygonA.vertices[0];
        float linearSlop = B2_LINEAR_SLOP();
        float speculativeDistance = B2_SPECULATIVE_DISTANCE();

        b2Transform sfA = new b2Transform(b2Add(xfA.p, b2RotateVector(xfA.q, origin)), xfA.q);
        b2Transform xf = b2InvMulTransforms(sfA, xfB);

        b2Polygon localPolyA = new b2Polygon();
        localPolyA.count = polygonA.count;
        localPolyA.radius = polygonA.radius;
        localPolyA.vertices[0].set(b2Vec2_zero);
        localPolyA.normals[0].set(polygonA.normals[0]);
        for (int i = 1; i < localPolyA.count; ++i) {
            localPolyA.vertices[i].set(b2Sub(polygonA.vertices[i], origin));
            localPolyA.normals[i].set(polygonA.normals[i]);
        }

        b2Polygon localPolyB = new b2Polygon();
        localPolyB.count = polygonB.count;
        localPolyB.radius = polygonB.radius;
        for (int i = 0; i < localPolyB.count; ++i) {
            localPolyB.vertices[i].set(b2TransformPoint(xf, polygonB.vertices[i]));
            localPolyB.normals[i].set(b2RotateVector(xf.q, polygonB.normals[i]));
        }

        int[] edgeARef = new int[1];
        float separationA = b2FindMaxSeparation(edgeARef, localPolyA, localPolyB);
        int edgeA = edgeARef[0];
        int[] edgeBRef = new int[1];
        float separationB = b2FindMaxSeparation(edgeBRef, localPolyB, localPolyA);
        int edgeB = edgeBRef[0];

        float radius = localPolyA.radius + localPolyB.radius;
        if (separationA > speculativeDistance + radius || separationB > speculativeDistance + radius) {
            return new b2Manifold();
        }

        boolean flip;
        if (separationA >= separationB) {
            flip = false;
            b2Vec2 searchDirection = localPolyA.normals[edgeA];
            edgeB = 0;
            float minDot = Float.MAX_VALUE;
            for (int i = 0; i < localPolyB.count; ++i) {
                float dot = b2Dot(searchDirection, localPolyB.normals[i]);
                if (dot < minDot) {
                    minDot = dot;
                    edgeB = i;
                }
            }
        } else {
            flip = true;
            b2Vec2 searchDirection = localPolyB.normals[edgeB];
            edgeA = 0;
            float minDot = Float.MAX_VALUE;
            for (int i = 0; i < localPolyA.count; ++i) {
                float dot = b2Dot(searchDirection, localPolyA.normals[i]);
                if (dot < minDot) {
                    minDot = dot;
                    edgeA = i;
                }
            }
        }

        b2Manifold manifold;
        if (separationA > 0.1f * linearSlop || separationB > 0.1f * linearSlop) {
            int i11 = edgeA;
            int i12 = edgeA + 1 < localPolyA.count ? edgeA + 1 : 0;
            int i21 = edgeB;
            int i22 = edgeB + 1 < localPolyB.count ? edgeB + 1 : 0;

            b2Vec2 v11 = localPolyA.vertices[i11];
            b2Vec2 v12 = localPolyA.vertices[i12];
            b2Vec2 v21 = localPolyB.vertices[i21];
            b2Vec2 v22 = localPolyB.vertices[i22];

            b2SegmentDistanceResult result = b2SegmentDistance(v11, v12, v21, v22);
            float distance = (float) Math.sqrt(result.distanceSquared);
            float separation = distance - radius;
            if (distance - radius > speculativeDistance) {
                return new b2Manifold();
            }

            manifold = b2ClipPolygons(localPolyA, localPolyB, edgeA, edgeB, flip);
            float minSeparation = Float.MAX_VALUE;
            for (int i = 0; i < manifold.pointCount; ++i) {
                minSeparation = b2MinFloat(minSeparation, manifold.points[i].separation);
            }

            if (separation + 0.1f * linearSlop < minSeparation) {
                if (result.fraction1 == 0.0f && result.fraction2 == 0.0f) {
                    manifold = vertexVertexManifold(v11, v21, localPolyA.radius, localPolyB.radius, distance, radius, i11, i21);
                } else if (result.fraction1 == 0.0f && result.fraction2 == 1.0f) {
                    manifold = vertexVertexManifold(v11, v22, localPolyA.radius, localPolyB.radius, distance, radius, i11, i22);
                } else if (result.fraction1 == 1.0f && result.fraction2 == 0.0f) {
                    manifold = vertexVertexManifold(v12, v21, localPolyA.radius, localPolyB.radius, distance, radius, i12, i21);
                } else if (result.fraction1 == 1.0f && result.fraction2 == 1.0f) {
                    manifold = vertexVertexManifold(v12, v22, localPolyA.radius, localPolyB.radius, distance, radius, i12, i22);
                }
            }
        } else {
            manifold = b2ClipPolygons(localPolyA, localPolyB, edgeA, edgeB, flip);
        }

        b2ConvertManifoldToWorld(manifold, xfA, xfB, origin);
        return manifold;
    }

    private static b2Manifold vertexVertexManifold(b2Vec2 vA, b2Vec2 vB, float radiusA, float radiusB, float distance, float radius, int iA, int iB) {
        b2Manifold manifold = new b2Manifold();
        b2Vec2 normal = b2Sub(vB, vA);
        float invDistance = 1.0f / distance;
        normal.x *= invDistance;
        normal.y *= invDistance;
        b2Vec2 c1 = b2MulAdd(vA, radiusA, normal);
        b2Vec2 c2 = b2MulAdd(vB, -radiusB, normal);
        manifold.normal = normal;
        manifold.points[0].anchorA = b2Lerp(c1, c2, 0.5f);
        manifold.points[0].separation = distance - radius;
        manifold.points[0].id = B2_MAKE_ID(iA, iB);
        manifold.pointCount = 1;
        return manifold;
    }

    public static b2CastOutput b2RayCastCircle(b2RayCastInput input, b2Circle shape) {
        b2CastOutput output = new b2CastOutput();
        b2Vec2 s = b2Sub(input.origin, shape.center);
        float rr = shape.radius * shape.radius;
        float[] length = new float[1];
        b2Vec2 d = b2GetLengthAndNormalize(length, input.translation);
        if (length[0] == 0.0f) {
            if (b2LengthSquared(s) < rr) {
                output.point = input.origin.copy();
                output.hit = true;
            }
            return output;
        }
        float t = -b2Dot(s, d);
        b2Vec2 c = b2MulAdd(s, t, d);
        float cc = b2Dot(c, c);
        if (cc > rr) {
            return output;
        }
        float h = (float) Math.sqrt(rr - cc);
        float fraction = t - h;
        if (fraction < 0.0f || input.maxFraction * length[0] < fraction) {
            if (b2LengthSquared(s) < rr) {
                output.point = input.origin.copy();
                output.hit = true;
            }
            return output;
        }
        b2Vec2 hitPoint = b2MulAdd(s, fraction, d);
        output.fraction = fraction / length[0];
        output.normal = b2Normalize(hitPoint);
        output.point = b2MulAdd(shape.center, shape.radius, output.normal);
        output.hit = true;
        return output;
    }

    public static b2CastOutput b2RayCastSegment(b2RayCastInput input, b2Segment shape, boolean oneSided) {
        if (oneSided) {
            float offset = b2Cross(b2Sub(input.origin, shape.point1), b2Sub(shape.point2, shape.point1));
            if (offset < 0.0f) {
                return new b2CastOutput();
            }
        }
        b2Vec2 p1 = input.origin;
        b2Vec2 d = input.translation;
        b2Vec2 e = b2Sub(shape.point2, shape.point1);
        b2CastOutput output = new b2CastOutput();
        float[] length = new float[1];
        b2Vec2 eUnit = b2GetLengthAndNormalize(length, e);
        if (length[0] == 0.0f) {
            return output;
        }
        b2Vec2 normal = b2RightPerp(eUnit);
        float numerator = b2Dot(normal, b2Sub(shape.point1, p1));
        float denominator = b2Dot(normal, d);
        if (denominator == 0.0f) {
            return output;
        }
        float t = numerator / denominator;
        if (t < 0.0f || input.maxFraction < t) {
            return output;
        }
        b2Vec2 p = b2MulAdd(p1, t, d);
        float s = b2Dot(b2Sub(p, shape.point1), eUnit);
        if (s < 0.0f || length[0] < s) {
            return output;
        }
        if (numerator > 0.0f) {
            normal = b2Neg(normal);
        }
        output.fraction = t;
        output.point = p;
        output.normal = normal;
        output.hit = true;
        return output;
    }

    public static b2CastOutput b2RayCastPolygon(b2RayCastInput input, b2Polygon shape) {
        if (shape.radius != 0.0f) {
            b2ShapeCastPairInput castInput = new b2ShapeCastPairInput();
            castInput.proxyA = b2MakeProxy(shape.vertices, shape.count, shape.radius);
            castInput.proxyB = b2MakeProxy(new b2Vec2[] {input.origin}, 1, 0.0f);
            castInput.transformA = b2Transform_identity.copy();
            castInput.transformB = b2Transform_identity.copy();
            castInput.translationB = input.translation.copy();
            castInput.maxFraction = input.maxFraction;
            castInput.canEncroach = false;
            return b2ShapeCast(castInput);
        }

        b2Vec2 base = shape.vertices[0];
        b2Vec2 p1 = b2Sub(input.origin, base);
        b2Vec2 d = input.translation;
        float lower = 0.0f;
        float upper = input.maxFraction;
        int index = -1;
        b2CastOutput output = new b2CastOutput();
        for (int i = 0; i < shape.count; ++i) {
            b2Vec2 vertex = b2Sub(shape.vertices[i], base);
            float numerator = b2Dot(shape.normals[i], b2Sub(vertex, p1));
            float denominator = b2Dot(shape.normals[i], d);
            if (denominator == 0.0f) {
                if (numerator < 0.0f) {
                    return output;
                }
            } else if (denominator < 0.0f && numerator < lower * denominator) {
                lower = numerator / denominator;
                index = i;
            } else if (denominator > 0.0f && numerator < upper * denominator) {
                upper = numerator / denominator;
            }
            if (upper < lower) {
                return output;
            }
        }
        if (index >= 0) {
            output.fraction = lower;
            output.normal = shape.normals[index].copy();
            output.point = b2MulAdd(input.origin, lower, d);
            output.hit = true;
        } else {
            output.point = input.origin.copy();
            output.hit = true;
        }
        return output;
    }

    public static b2CastOutput b2RayCastCapsule(b2RayCastInput input, b2Capsule shape) {
        b2CastOutput output = new b2CastOutput();

        b2Vec2 v1 = shape.center1;
        b2Vec2 v2 = shape.center2;
        b2Vec2 e = b2Sub(v2, v1);
        float[] capsuleLength = new float[1];
        b2Vec2 a = b2GetLengthAndNormalize(capsuleLength, e);
        if (capsuleLength[0] < Math.ulp(1.0f)) {
            return b2RayCastCircle(input, new b2Circle(v1, shape.radius));
        }

        b2Vec2 p1 = input.origin;
        b2Vec2 d = input.translation;
        b2Vec2 q = b2Sub(p1, v1);
        float qa = b2Dot(q, a);
        b2Vec2 qp = b2MulAdd(q, -qa, a);
        float radius = shape.radius;

        if (b2Dot(qp, qp) < radius * radius) {
            if (qa < 0.0f) {
                return b2RayCastCircle(input, new b2Circle(v1, shape.radius));
            }
            if (qa > capsuleLength[0]) {
                return b2RayCastCircle(input, new b2Circle(v2, shape.radius));
            }
            output.point = input.origin.copy();
            output.hit = true;
            return output;
        }

        b2Vec2 n = new b2Vec2(a.y, -a.x);
        float[] rayLength = new float[1];
        b2Vec2 u = b2GetLengthAndNormalize(rayLength, d);
        float den = -a.x * u.y + u.x * a.y;
        if (-Math.ulp(1.0f) < den && den < Math.ulp(1.0f)) {
            return output;
        }

        b2Vec2 b1 = b2MulSub(q, radius, n);
        b2Vec2 b2 = b2MulAdd(q, radius, n);
        float invDen = 1.0f / den;
        float s21 = (a.x * b1.y - b1.x * a.y) * invDen;
        float s22 = (a.x * b2.y - b2.x * a.y) * invDen;

        float s2;
        b2Vec2 b;
        if (s21 < s22) {
            s2 = s21;
            b = b1;
        } else {
            s2 = s22;
            b = b2;
            n = b2Neg(n);
        }

        if (s2 < 0.0f || input.maxFraction * rayLength[0] < s2) {
            return output;
        }

        float s1 = (-b.x * u.y + u.x * b.y) * invDen;
        if (s1 < 0.0f) {
            return b2RayCastCircle(input, new b2Circle(v1, shape.radius));
        }
        if (capsuleLength[0] < s1) {
            return b2RayCastCircle(input, new b2Circle(v2, shape.radius));
        }

        output.fraction = s2 / rayLength[0];
        output.point = b2Add(b2Lerp(v1, v2, s1 / capsuleLength[0]), b2MulSV(shape.radius, n));
        output.normal = n;
        output.hit = true;
        return output;
    }

    public static b2Hull b2ComputeHull(b2Vec2[] points, int count) {
        b2Hull hull = new b2Hull();
        if (count < 3 || count > B2_MAX_POLYGON_VERTICES) {
            return hull;
        }

        b2Vec2 lowerBound = new b2Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
        b2Vec2 upperBound = new b2Vec2(-Float.MAX_VALUE, -Float.MAX_VALUE);
        b2Vec2[] ps = b2Vec2.array(B2_MAX_POLYGON_VERTICES);
        int n = 0;
        float linearSlop = B2_LINEAR_SLOP();
        float tolSqr = 16.0f * linearSlop * linearSlop;
        for (int i = 0; i < count; ++i) {
            lowerBound = b2Min(lowerBound, points[i]);
            upperBound = b2Max(upperBound, points[i]);

            b2Vec2 vi = points[i];
            boolean unique = true;
            for (int j = 0; j < i; ++j) {
                if (b2DistanceSquared(vi, points[j]) < tolSqr) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                ps[n++].set(vi);
            }
        }
        if (n < 3) {
            return hull;
        }

        b2Vec2 c = b2MulSV(0.5f, b2Add(lowerBound, upperBound));
        int f1 = 0;
        float dsq1 = b2DistanceSquared(c, ps[f1]);
        for (int i = 1; i < n; ++i) {
            float dsq = b2DistanceSquared(c, ps[i]);
            if (dsq > dsq1) {
                f1 = i;
                dsq1 = dsq;
            }
        }

        b2Vec2 p1 = ps[f1].copy();
        ps[f1].set(ps[n - 1]);
        n -= 1;

        int f2 = 0;
        float dsq2 = b2DistanceSquared(p1, ps[f2]);
        for (int i = 1; i < n; ++i) {
            float dsq = b2DistanceSquared(p1, ps[i]);
            if (dsq > dsq2) {
                f2 = i;
                dsq2 = dsq;
            }
        }

        b2Vec2 p2 = ps[f2].copy();
        ps[f2].set(ps[n - 1]);
        n -= 1;

        b2Vec2[] rightPoints = b2Vec2.array(B2_MAX_POLYGON_VERTICES - 2);
        int rightCount = 0;
        b2Vec2[] leftPoints = b2Vec2.array(B2_MAX_POLYGON_VERTICES - 2);
        int leftCount = 0;

        b2Vec2 e = b2Normalize(b2Sub(p2, p1));
        for (int i = 0; i < n; ++i) {
            float d = b2Cross(b2Sub(ps[i], p1), e);
            if (d >= 2.0f * linearSlop) {
                rightPoints[rightCount++].set(ps[i]);
            } else if (d <= -2.0f * linearSlop) {
                leftPoints[leftCount++].set(ps[i]);
            }
        }

        b2Hull hull1 = b2RecurseHull(p1, p2, rightPoints, rightCount);
        b2Hull hull2 = b2RecurseHull(p2, p1, leftPoints, leftCount);
        if (hull1.count == 0 && hull2.count == 0) {
            return hull;
        }

        hull.points[hull.count++].set(p1);
        for (int i = 0; i < hull1.count; ++i) {
            hull.points[hull.count++].set(hull1.points[i]);
        }
        hull.points[hull.count++].set(p2);
        for (int i = 0; i < hull2.count; ++i) {
            hull.points[hull.count++].set(hull2.points[i]);
        }

        boolean searching = true;
        while (searching && hull.count > 2) {
            searching = false;
            for (int i = 0; i < hull.count; ++i) {
                int i1 = i;
                int i2 = (i + 1) % hull.count;
                int i3 = (i + 2) % hull.count;

                b2Vec2 s1 = hull.points[i1];
                b2Vec2 s2 = hull.points[i2];
                b2Vec2 s3 = hull.points[i3];
                b2Vec2 r = b2Normalize(b2Sub(s3, s1));
                float distance = b2Cross(b2Sub(s2, s1), r);
                if (distance <= 2.0f * linearSlop) {
                    for (int j = i2; j < hull.count - 1; ++j) {
                        hull.points[j].set(hull.points[j + 1]);
                    }
                    hull.count -= 1;
                    searching = true;
                    break;
                }
            }
        }

        if (hull.count < 3) {
            hull.count = 0;
        }
        return hull;
    }

    private static b2Hull b2RecurseHull(b2Vec2 p1, b2Vec2 p2, b2Vec2[] ps, int count) {
        b2Hull hull = new b2Hull();
        if (count == 0) {
            return hull;
        }

        b2Vec2 e = b2Normalize(b2Sub(p2, p1));
        b2Vec2[] rightPoints = b2Vec2.array(B2_MAX_POLYGON_VERTICES);
        int rightCount = 0;

        int bestIndex = 0;
        float bestDistance = b2Cross(b2Sub(ps[bestIndex], p1), e);
        if (bestDistance > 0.0f) {
            rightPoints[rightCount++].set(ps[bestIndex]);
        }

        for (int i = 1; i < count; ++i) {
            float distance = b2Cross(b2Sub(ps[i], p1), e);
            if (distance > bestDistance) {
                bestIndex = i;
                bestDistance = distance;
            }
            if (distance > 0.0f) {
                rightPoints[rightCount++].set(ps[i]);
            }
        }

        if (bestDistance < 2.0f * B2_LINEAR_SLOP()) {
            return hull;
        }

        b2Vec2 bestPoint = ps[bestIndex].copy();
        b2Hull hull1 = b2RecurseHull(p1, bestPoint, rightPoints, rightCount);
        b2Hull hull2 = b2RecurseHull(bestPoint, p2, rightPoints, rightCount);

        for (int i = 0; i < hull1.count; ++i) {
            hull.points[hull.count++].set(hull1.points[i]);
        }
        hull.points[hull.count++].set(bestPoint);
        for (int i = 0; i < hull2.count; ++i) {
            hull.points[hull.count++].set(hull2.points[i]);
        }

        return hull;
    }

    public static boolean b2ValidateHull(b2Hull hull) {
        if (hull.count < 3 || B2_MAX_POLYGON_VERTICES < hull.count) {
            return false;
        }
        for (int i = 0; i < hull.count; ++i) {
            int i2 = i < hull.count - 1 ? i + 1 : 0;
            b2Vec2 p = hull.points[i];
            b2Vec2 e = b2Normalize(b2Sub(hull.points[i2], p));
            for (int j = 0; j < hull.count; ++j) {
                if (j == i || j == i2) {
                    continue;
                }
                if (b2Cross(b2Sub(hull.points[j], p), e) >= 0.0f) {
                    return false;
                }
            }
        }
        return true;
    }

    public static b2SegmentDistanceResult b2SegmentDistance(b2Vec2 p1, b2Vec2 q1, b2Vec2 p2, b2Vec2 q2) {
        b2SegmentDistanceResult result = new b2SegmentDistanceResult();
        b2Vec2 d1 = b2Sub(q1, p1);
        b2Vec2 d2 = b2Sub(q2, p2);
        b2Vec2 r = b2Sub(p1, p2);
        float dd1 = b2Dot(d1, d1);
        float dd2 = b2Dot(d2, d2);
        float rd1 = b2Dot(r, d1);
        float rd2 = b2Dot(r, d2);

        final float epsSqr = Math.ulp(1.0f) * Math.ulp(1.0f);

        if (dd1 < epsSqr || dd2 < epsSqr) {
            if (dd1 >= epsSqr) {
                result.fraction1 = b2ClampFloat(-rd1 / dd1, 0.0f, 1.0f);
                result.fraction2 = 0.0f;
            } else if (dd2 >= epsSqr) {
                result.fraction1 = 0.0f;
                result.fraction2 = b2ClampFloat(rd2 / dd2, 0.0f, 1.0f);
            } else {
                result.fraction1 = 0.0f;
                result.fraction2 = 0.0f;
            }
        } else {
            float d12 = b2Dot(d1, d2);
            float denominator = dd1 * dd2 - d12 * d12;

            float f1 = 0.0f;
            if (denominator != 0.0f) {
                f1 = b2ClampFloat((d12 * rd2 - rd1 * dd2) / denominator, 0.0f, 1.0f);
            }

            float f2 = (d12 * f1 + rd2) / dd2;
            if (f2 < 0.0f) {
                f2 = 0.0f;
                f1 = b2ClampFloat(-rd1 / dd1, 0.0f, 1.0f);
            } else if (f2 > 1.0f) {
                f2 = 1.0f;
                f1 = b2ClampFloat((d12 - rd1) / dd1, 0.0f, 1.0f);
            }

            result.fraction1 = f1;
            result.fraction2 = f2;
        }

        result.closest1 = b2MulAdd(p1, result.fraction1, d1);
        result.closest2 = b2MulAdd(p2, result.fraction2, d2);
        result.distanceSquared = b2DistanceSquared(result.closest1, result.closest2);
        return result;
    }

    public static b2ShapeProxy b2MakeProxy(b2Vec2[] points, int count, float radius) {
        b2ShapeProxy proxy = new b2ShapeProxy();
        proxy.count = count;
        proxy.radius = radius;
        for (int i = 0; i < count; ++i) {
            proxy.points[i].set(points[i]);
        }
        return proxy;
    }

    public static b2ShapeProxy b2MakeOffsetProxy(b2Vec2[] points, int count, float radius, b2Vec2 position, b2Rot rotation) {
        b2ShapeProxy proxy = new b2ShapeProxy();
        proxy.count = count;
        proxy.radius = radius;
        b2Transform xf = new b2Transform(position, rotation);
        for (int i = 0; i < count; ++i) {
            proxy.points[i].set(b2TransformPoint(xf, points[i]));
        }
        return proxy;
    }

    public static b2CastOutput b2ShapeCastCircle(b2ShapeCastInput input, b2Circle shape) {
        b2ShapeCastPairInput pairInput = new b2ShapeCastPairInput();
        pairInput.proxyA = b2MakeProxy(new b2Vec2[] {shape.center}, 1, shape.radius);
        pairInput.proxyB = input.proxy;
        pairInput.transformA = b2Transform_identity;
        pairInput.transformB = b2Transform_identity;
        pairInput.translationB = input.translation;
        pairInput.maxFraction = input.maxFraction;
        pairInput.canEncroach = input.canEncroach;
        return b2ShapeCast(pairInput);
    }

    public static b2CastOutput b2ShapeCastCapsule(b2ShapeCastInput input, b2Capsule shape) {
        b2ShapeCastPairInput pairInput = new b2ShapeCastPairInput();
        pairInput.proxyA = b2MakeProxy(new b2Vec2[] {shape.center1, shape.center2}, 2, shape.radius);
        pairInput.proxyB = input.proxy;
        pairInput.transformA = b2Transform_identity;
        pairInput.transformB = b2Transform_identity;
        pairInput.translationB = input.translation;
        pairInput.maxFraction = input.maxFraction;
        pairInput.canEncroach = input.canEncroach;
        return b2ShapeCast(pairInput);
    }

    public static b2CastOutput b2ShapeCastSegment(b2ShapeCastInput input, b2Segment shape) {
        b2ShapeCastPairInput pairInput = new b2ShapeCastPairInput();
        pairInput.proxyA = b2MakeProxy(new b2Vec2[] {shape.point1, shape.point2}, 2, 0.0f);
        pairInput.proxyB = input.proxy;
        pairInput.transformA = b2Transform_identity;
        pairInput.transformB = b2Transform_identity;
        pairInput.translationB = input.translation;
        pairInput.maxFraction = input.maxFraction;
        pairInput.canEncroach = input.canEncroach;
        return b2ShapeCast(pairInput);
    }

    public static b2CastOutput b2ShapeCastPolygon(b2ShapeCastInput input, b2Polygon shape) {
        b2ShapeCastPairInput pairInput = new b2ShapeCastPairInput();
        pairInput.proxyA = b2MakeProxy(shape.vertices, shape.count, shape.radius);
        pairInput.proxyB = input.proxy;
        pairInput.transformA = b2Transform_identity;
        pairInput.transformB = b2Transform_identity;
        pairInput.translationB = input.translation;
        pairInput.maxFraction = input.maxFraction;
        pairInput.canEncroach = input.canEncroach;
        return b2ShapeCast(pairInput);
    }

    private static b2Vec2 b2Weight2(float a1, b2Vec2 w1, float a2, b2Vec2 w2) {
        return new b2Vec2(a1 * w1.x + a2 * w2.x, a1 * w1.y + a2 * w2.y);
    }

    private static b2Vec2 b2Weight3(float a1, b2Vec2 w1, float a2, b2Vec2 w2, float a3, b2Vec2 w3) {
        return new b2Vec2(a1 * w1.x + a2 * w2.x + a3 * w3.x, a1 * w1.y + a2 * w2.y + a3 * w3.y);
    }

    private static int b2FindSupport(b2ShapeProxy proxy, b2Vec2 direction) {
        int bestIndex = 0;
        float bestValue = b2Dot(proxy.points[0], direction);
        for (int i = 1; i < proxy.count; ++i) {
            float value = b2Dot(proxy.points[i], direction);
            if (value > bestValue) {
                bestIndex = i;
                bestValue = value;
            }
        }
        return bestIndex;
    }

    private static b2Simplex b2MakeSimplexFromCache(b2SimplexCache cache, b2ShapeProxy proxyA, b2ShapeProxy proxyB) {
        b2Simplex s = new b2Simplex();
        s.count = cache.count;
        b2SimplexVertex[] vertices = s.vertices();
        for (int i = 0; i < s.count; ++i) {
            b2SimplexVertex v = vertices[i];
            v.indexA = cache.indexA[i] & 0xFF;
            v.indexB = cache.indexB[i] & 0xFF;
            v.wA.set(proxyA.points[v.indexA]);
            v.wB.set(proxyB.points[v.indexB]);
            v.w.set(b2Sub(v.wA, v.wB));
            v.a = -1.0f;
        }

        if (s.count == 0) {
            b2SimplexVertex v = vertices[0];
            v.indexA = 0;
            v.indexB = 0;
            v.wA.set(proxyA.points[0]);
            v.wB.set(proxyB.points[0]);
            v.w.set(b2Sub(v.wA, v.wB));
            v.a = 1.0f;
            s.count = 1;
        }
        return s;
    }

    private static void b2MakeSimplexCache(b2SimplexCache cache, b2Simplex simplex) {
        cache.count = simplex.count;
        b2SimplexVertex[] vertices = simplex.vertices();
        for (int i = 0; i < simplex.count; ++i) {
            cache.indexA[i] = (byte) vertices[i].indexA;
            cache.indexB[i] = (byte) vertices[i].indexB;
        }
    }

    private static b2Vec2[] b2ComputeSimplexWitnessPoints(b2Simplex s) {
        b2Vec2 a;
        b2Vec2 b;
        switch (s.count) {
            case 1:
                a = s.v1.wA.copy();
                b = s.v1.wB.copy();
                break;
            case 2:
                a = b2Weight2(s.v1.a, s.v1.wA, s.v2.a, s.v2.wA);
                b = b2Weight2(s.v1.a, s.v1.wB, s.v2.a, s.v2.wB);
                break;
            case 3:
                a = b2Weight3(s.v1.a, s.v1.wA, s.v2.a, s.v2.wA, s.v3.a, s.v3.wA);
                b = a.copy();
                break;
            default:
                a = new b2Vec2();
                b = new b2Vec2();
                b2Assert(false, "invalid simplex count");
        }
        return new b2Vec2[] {a, b};
    }

    private static b2Vec2 b2SolveSimplex2(b2Simplex s) {
        b2Vec2 w1 = s.v1.w;
        b2Vec2 w2 = s.v2.w;
        b2Vec2 e12 = b2Sub(w2, w1);

        float d12_2 = -b2Dot(w1, e12);
        if (d12_2 <= 0.0f) {
            s.v1.a = 1.0f;
            s.count = 1;
            return b2Neg(w1);
        }

        float d12_1 = b2Dot(w2, e12);
        if (d12_1 <= 0.0f) {
            s.v2.a = 1.0f;
            s.count = 1;
            s.v1.set(s.v2);
            return b2Neg(w2);
        }

        float invD12 = 1.0f / (d12_1 + d12_2);
        s.v1.a = d12_1 * invD12;
        s.v2.a = d12_2 * invD12;
        s.count = 2;
        return b2CrossSV(b2Cross(b2Add(w1, w2), e12), e12);
    }

    private static b2Vec2 b2SolveSimplex3(b2Simplex s) {
        b2Vec2 w1 = s.v1.w;
        b2Vec2 w2 = s.v2.w;
        b2Vec2 w3 = s.v3.w;

        b2Vec2 e12 = b2Sub(w2, w1);
        float w1e12 = b2Dot(w1, e12);
        float w2e12 = b2Dot(w2, e12);
        float d12_1 = w2e12;
        float d12_2 = -w1e12;

        b2Vec2 e13 = b2Sub(w3, w1);
        float w1e13 = b2Dot(w1, e13);
        float w3e13 = b2Dot(w3, e13);
        float d13_1 = w3e13;
        float d13_2 = -w1e13;

        b2Vec2 e23 = b2Sub(w3, w2);
        float w2e23 = b2Dot(w2, e23);
        float w3e23 = b2Dot(w3, e23);
        float d23_1 = w3e23;
        float d23_2 = -w2e23;

        float n123 = b2Cross(e12, e13);
        float d123_1 = n123 * b2Cross(w2, w3);
        float d123_2 = n123 * b2Cross(w3, w1);
        float d123_3 = n123 * b2Cross(w1, w2);

        if (d12_2 <= 0.0f && d13_2 <= 0.0f) {
            s.v1.a = 1.0f;
            s.count = 1;
            return b2Neg(w1);
        }

        if (d12_1 > 0.0f && d12_2 > 0.0f && d123_3 <= 0.0f) {
            float invD12 = 1.0f / (d12_1 + d12_2);
            s.v1.a = d12_1 * invD12;
            s.v2.a = d12_2 * invD12;
            s.count = 2;
            return b2CrossSV(b2Cross(b2Add(w1, w2), e12), e12);
        }

        if (d13_1 > 0.0f && d13_2 > 0.0f && d123_2 <= 0.0f) {
            float invD13 = 1.0f / (d13_1 + d13_2);
            s.v1.a = d13_1 * invD13;
            s.v3.a = d13_2 * invD13;
            s.count = 2;
            s.v2.set(s.v3);
            return b2CrossSV(b2Cross(b2Add(w1, w3), e13), e13);
        }

        if (d12_1 <= 0.0f && d23_2 <= 0.0f) {
            s.v2.a = 1.0f;
            s.count = 1;
            s.v1.set(s.v2);
            return b2Neg(w2);
        }

        if (d13_1 <= 0.0f && d23_1 <= 0.0f) {
            s.v3.a = 1.0f;
            s.count = 1;
            s.v1.set(s.v3);
            return b2Neg(w3);
        }

        if (d23_1 > 0.0f && d23_2 > 0.0f && d123_1 <= 0.0f) {
            float invD23 = 1.0f / (d23_1 + d23_2);
            s.v2.a = d23_1 * invD23;
            s.v3.a = d23_2 * invD23;
            s.count = 2;
            s.v1.set(s.v3);
            return b2CrossSV(b2Cross(b2Add(w2, w3), e23), e23);
        }

        float invD123 = 1.0f / (d123_1 + d123_2 + d123_3);
        s.v1.a = d123_1 * invD123;
        s.v2.a = d123_2 * invD123;
        s.v3.a = d123_3 * invD123;
        s.count = 3;
        return b2Vec2_zero.copy();
    }

    public static b2DistanceOutput b2ShapeDistance(b2DistanceInput input, b2SimplexCache cache, Object simplexes, int simplexCapacity) {
        b2DistanceOutput output = new b2DistanceOutput();
        b2ShapeProxy proxyA = input.proxyA;

        b2ShapeProxy localProxyB = new b2ShapeProxy();
        b2Transform transform = b2InvMulTransforms(input.transformA, input.transformB);
        localProxyB.count = input.proxyB.count;
        localProxyB.radius = input.proxyB.radius;
        for (int i = 0; i < localProxyB.count; ++i) {
            localProxyB.points[i].set(b2TransformPoint(transform, input.proxyB.points[i]));
        }

        b2Simplex simplex = b2MakeSimplexFromCache(cache, proxyA, localProxyB);
        b2Simplex[] simplexArray = simplexes instanceof b2Simplex[] ? (b2Simplex[]) simplexes : null;
        int simplexIndex = 0;
        if (simplexArray != null && simplexIndex < simplexCapacity) {
            if (simplexArray[simplexIndex] == null) {
                simplexArray[simplexIndex] = new b2Simplex();
            }
            simplexArray[simplexIndex].set(simplex);
            simplexIndex += 1;
        }

        b2SimplexVertex[] vertices = simplex.vertices();
        b2Vec2 nonUnitNormal = b2Vec2_zero.copy();
        int[] saveA = new int[3];
        int[] saveB = new int[3];

        final int maxIterations = 20;
        int iteration = 0;
        while (iteration < maxIterations) {
            int saveCount = simplex.count;
            for (int i = 0; i < saveCount; ++i) {
                saveA[i] = vertices[i].indexA;
                saveB[i] = vertices[i].indexB;
            }

            b2Vec2 d = new b2Vec2();
            switch (simplex.count) {
                case 1:
                    d = b2Neg(simplex.v1.w);
                    break;
                case 2:
                    d = b2SolveSimplex2(simplex);
                    break;
                case 3:
                    d = b2SolveSimplex3(simplex);
                    break;
                default:
                    b2Assert(false, "invalid simplex count");
            }

            if (simplex.count == 3) {
                b2Vec2[] points = b2ComputeSimplexWitnessPoints(simplex);
                output.pointA = b2TransformPoint(input.transformA, points[0]);
                output.pointB = b2TransformPoint(input.transformA, points[1]);
                return output;
            }

            if (simplexArray != null && simplexIndex < simplexCapacity) {
                if (simplexArray[simplexIndex] == null) {
                    simplexArray[simplexIndex] = new b2Simplex();
                }
                simplexArray[simplexIndex].set(simplex);
                simplexIndex += 1;
            }

            if (b2Dot(d, d) < Math.ulp(1.0f) * Math.ulp(1.0f)) {
                b2Vec2[] points = b2ComputeSimplexWitnessPoints(simplex);
                output.pointA = b2TransformPoint(input.transformA, points[0]);
                output.pointB = b2TransformPoint(input.transformA, points[1]);
                return output;
            }

            nonUnitNormal = d;

            b2SimplexVertex vertex = vertices[simplex.count];
            vertex.indexA = b2FindSupport(proxyA, d);
            vertex.wA.set(proxyA.points[vertex.indexA]);
            vertex.indexB = b2FindSupport(localProxyB, b2Neg(d));
            vertex.wB.set(localProxyB.points[vertex.indexB]);
            vertex.w.set(b2Sub(vertex.wA, vertex.wB));

            ++iteration;

            boolean duplicate = false;
            for (int i = 0; i < saveCount; ++i) {
                if (vertex.indexA == saveA[i] && vertex.indexB == saveB[i]) {
                    duplicate = true;
                    break;
                }
            }

            if (duplicate) {
                break;
            }

            simplex.count += 1;
        }

        if (simplexArray != null && simplexIndex < simplexCapacity) {
            if (simplexArray[simplexIndex] == null) {
                simplexArray[simplexIndex] = new b2Simplex();
            }
            simplexArray[simplexIndex].set(simplex);
            simplexIndex += 1;
        }

        b2Vec2 normal = b2Normalize(nonUnitNormal);
        normal = b2RotateVector(input.transformA.q, normal);

        b2Vec2[] points = b2ComputeSimplexWitnessPoints(simplex);
        output.normal = normal;
        output.distance = b2Distance(points[0], points[1]);
        output.pointA = b2TransformPoint(input.transformA, points[0]);
        output.pointB = b2TransformPoint(input.transformA, points[1]);
        output.iterations = iteration;
        output.simplexCount = simplexIndex;

        b2MakeSimplexCache(cache, simplex);

        if (input.useRadii && output.distance > 0.1f * B2_LINEAR_SLOP()) {
            float radiusA = input.proxyA.radius;
            float radiusB = input.proxyB.radius;
            output.distance = b2MaxFloat(0.0f, output.distance - radiusA - radiusB);
            output.pointA = b2MulAdd(output.pointA, radiusA, normal);
            output.pointB = b2MulSub(output.pointB, radiusB, normal);
        }

        return output;
    }

    public static b2CastOutput b2ShapeCast(b2ShapeCastPairInput input) {
        float linearSlop = B2_LINEAR_SLOP();
        float totalRadius = input.proxyA.radius + input.proxyB.radius;
        float target = b2MaxFloat(linearSlop, totalRadius - linearSlop);
        float tolerance = 0.25f * linearSlop;

        b2SimplexCache cache = new b2SimplexCache();
        float fraction = 0.0f;

        b2DistanceInput distanceInput = new b2DistanceInput();
        distanceInput.proxyA = input.proxyA;
        distanceInput.proxyB = input.proxyB;
        distanceInput.transformA = input.transformA;
        distanceInput.transformB = input.transformB.copy();
        distanceInput.useRadii = false;

        b2Vec2 delta2 = input.translationB;
        b2CastOutput output = new b2CastOutput();
        int maxIterations = 20;
        for (int iteration = 0; iteration < maxIterations; ++iteration) {
            output.iterations += 1;
            b2DistanceOutput distanceOutput = b2ShapeDistance(distanceInput, cache, null, 0);

            if (distanceOutput.distance < target + tolerance) {
                if (iteration == 0) {
                    if (input.canEncroach && distanceOutput.distance > 2.0f * linearSlop) {
                        target = distanceOutput.distance - linearSlop;
                    } else {
                        output.hit = true;
                        b2Vec2 c1 = b2MulAdd(distanceOutput.pointA, input.proxyA.radius, distanceOutput.normal);
                        b2Vec2 c2 = b2MulAdd(distanceOutput.pointB, -input.proxyB.radius, distanceOutput.normal);
                        output.point = b2Lerp(c1, c2, 0.5f);
                        return output;
                    }
                } else {
                    output.fraction = fraction;
                    output.point = b2MulAdd(distanceOutput.pointA, input.proxyA.radius, distanceOutput.normal);
                    output.normal = distanceOutput.normal;
                    output.hit = true;
                    return output;
                }
            }

            float denominator = b2Dot(delta2, distanceOutput.normal);
            if (denominator >= 0.0f) {
                return output;
            }

            fraction += (target - distanceOutput.distance) / denominator;
            if (fraction >= input.maxFraction) {
                return output;
            }

            distanceInput.transformB.p = b2MulAdd(input.transformB.p, fraction, delta2);
        }

        return output;
    }

    public static b2Transform b2GetSweepTransform(b2Sweep sweep, float time) {
        b2Transform xf = new b2Transform();
        xf.p = b2Lerp(sweep.c1, sweep.c2, time);
        xf.q = b2NLerp(sweep.q1, sweep.q2, time);
        xf.p = b2MulSub(xf.p, 1.0f, b2RotateVector(xf.q, sweep.localCenter));
        return xf;
    }

    private static b2SeparationFunction b2MakeSeparationFunction(b2SimplexCache cache, b2ShapeProxy proxyA, b2Sweep sweepA,
                                                                 b2ShapeProxy proxyB, b2Sweep sweepB, float t1) {
        b2SeparationFunction f = new b2SeparationFunction();
        f.proxyA = proxyA;
        f.proxyB = proxyB;
        int count = cache.count;
        b2Assert(0 < count && count < 3, "0 < cache.count < 3");
        f.sweepA = copySweep(sweepA);
        f.sweepB = copySweep(sweepB);

        b2Transform xfA = b2GetSweepTransform(sweepA, t1);
        b2Transform xfB = b2GetSweepTransform(sweepB, t1);

        if (count == 1) {
            f.type = b2SeparationFunction.POINTS;
            b2Vec2 localPointA = proxyA.points[cache.indexA[0] & 0xFF];
            b2Vec2 localPointB = proxyB.points[cache.indexB[0] & 0xFF];
            b2Vec2 pointA = b2TransformPoint(xfA, localPointA);
            b2Vec2 pointB = b2TransformPoint(xfB, localPointB);
            f.axis = b2Normalize(b2Sub(pointB, pointA));
            f.localPoint = b2Vec2_zero.copy();
            return f;
        }

        if ((cache.indexA[0] & 0xFF) == (cache.indexA[1] & 0xFF)) {
            f.type = b2SeparationFunction.FACE_B;
            b2Vec2 localPointB1 = proxyB.points[cache.indexB[0] & 0xFF];
            b2Vec2 localPointB2 = proxyB.points[cache.indexB[1] & 0xFF];
            f.axis = b2Normalize(b2CrossVS(b2Sub(localPointB2, localPointB1), 1.0f));
            b2Vec2 normal = b2RotateVector(xfB.q, f.axis);
            f.localPoint = new b2Vec2(0.5f * (localPointB1.x + localPointB2.x), 0.5f * (localPointB1.y + localPointB2.y));
            b2Vec2 pointB = b2TransformPoint(xfB, f.localPoint);
            b2Vec2 localPointA = proxyA.points[cache.indexA[0] & 0xFF];
            b2Vec2 pointA = b2TransformPoint(xfA, localPointA);
            float s = b2Dot(b2Sub(pointA, pointB), normal);
            if (s < 0.0f) {
                f.axis = b2Neg(f.axis);
            }
            return f;
        }

        f.type = b2SeparationFunction.FACE_A;
        b2Vec2 localPointA1 = proxyA.points[cache.indexA[0] & 0xFF];
        b2Vec2 localPointA2 = proxyA.points[cache.indexA[1] & 0xFF];
        f.axis = b2Normalize(b2CrossVS(b2Sub(localPointA2, localPointA1), 1.0f));
        b2Vec2 normal = b2RotateVector(xfA.q, f.axis);
        f.localPoint = new b2Vec2(0.5f * (localPointA1.x + localPointA2.x), 0.5f * (localPointA1.y + localPointA2.y));
        b2Vec2 pointA = b2TransformPoint(xfA, f.localPoint);
        b2Vec2 localPointB = proxyB.points[cache.indexB[0] & 0xFF];
        b2Vec2 pointB = b2TransformPoint(xfB, localPointB);
        float s = b2Dot(b2Sub(pointB, pointA), normal);
        if (s < 0.0f) {
            f.axis = b2Neg(f.axis);
        }
        return f;
    }

    private static float b2FindMinSeparation(b2SeparationFunction f, int[] indexA, int[] indexB, float t) {
        b2Transform xfA = b2GetSweepTransform(f.sweepA, t);
        b2Transform xfB = b2GetSweepTransform(f.sweepB, t);

        switch (f.type) {
            case b2SeparationFunction.POINTS: {
                b2Vec2 axisA = b2InvRotateVector(xfA.q, f.axis);
                b2Vec2 axisB = b2InvRotateVector(xfB.q, b2Neg(f.axis));

                indexA[0] = b2FindSupport(f.proxyA, axisA);
                indexB[0] = b2FindSupport(f.proxyB, axisB);

                b2Vec2 pointA = b2TransformPoint(xfA, f.proxyA.points[indexA[0]]);
                b2Vec2 pointB = b2TransformPoint(xfB, f.proxyB.points[indexB[0]]);
                return b2Dot(b2Sub(pointB, pointA), f.axis);
            }

            case b2SeparationFunction.FACE_A: {
                b2Vec2 normal = b2RotateVector(xfA.q, f.axis);
                b2Vec2 pointA = b2TransformPoint(xfA, f.localPoint);
                b2Vec2 axisB = b2InvRotateVector(xfB.q, b2Neg(normal));

                indexA[0] = -1;
                indexB[0] = b2FindSupport(f.proxyB, axisB);

                b2Vec2 pointB = b2TransformPoint(xfB, f.proxyB.points[indexB[0]]);
                return b2Dot(b2Sub(pointB, pointA), normal);
            }

            case b2SeparationFunction.FACE_B: {
                b2Vec2 normal = b2RotateVector(xfB.q, f.axis);
                b2Vec2 pointB = b2TransformPoint(xfB, f.localPoint);
                b2Vec2 axisA = b2InvRotateVector(xfA.q, b2Neg(normal));

                indexB[0] = -1;
                indexA[0] = b2FindSupport(f.proxyA, axisA);

                b2Vec2 pointA = b2TransformPoint(xfA, f.proxyA.points[indexA[0]]);
                return b2Dot(b2Sub(pointA, pointB), normal);
            }

            default:
                b2Assert(false, "unknown separation type");
                indexA[0] = -1;
                indexB[0] = -1;
                return 0.0f;
        }
    }

    private static float b2EvaluateSeparation(b2SeparationFunction f, int indexA, int indexB, float t) {
        b2Transform xfA = b2GetSweepTransform(f.sweepA, t);
        b2Transform xfB = b2GetSweepTransform(f.sweepB, t);

        switch (f.type) {
            case b2SeparationFunction.POINTS: {
                b2Vec2 pointA = b2TransformPoint(xfA, f.proxyA.points[indexA]);
                b2Vec2 pointB = b2TransformPoint(xfB, f.proxyB.points[indexB]);
                return b2Dot(b2Sub(pointB, pointA), f.axis);
            }

            case b2SeparationFunction.FACE_A: {
                b2Vec2 normal = b2RotateVector(xfA.q, f.axis);
                b2Vec2 pointA = b2TransformPoint(xfA, f.localPoint);
                b2Vec2 pointB = b2TransformPoint(xfB, f.proxyB.points[indexB]);
                return b2Dot(b2Sub(pointB, pointA), normal);
            }

            case b2SeparationFunction.FACE_B: {
                b2Vec2 normal = b2RotateVector(xfB.q, f.axis);
                b2Vec2 pointB = b2TransformPoint(xfB, f.localPoint);
                b2Vec2 pointA = b2TransformPoint(xfA, f.proxyA.points[indexA]);
                return b2Dot(b2Sub(pointA, pointB), normal);
            }

            default:
                b2Assert(false, "unknown separation type");
                return 0.0f;
        }
    }

    public static b2TOIOutput b2TimeOfImpact(b2TOIInput input) {
        b2TOIOutput output = new b2TOIOutput();
        output.state = b2_toiStateUnknown;
        output.fraction = input.maxFraction;

        b2Sweep sweepA = copySweep(input.sweepA);
        b2Sweep sweepB = copySweep(input.sweepB);
        b2ShapeProxy proxyA = input.proxyA;
        b2ShapeProxy proxyB = input.proxyB;

        float tMax = input.maxFraction;
        float totalRadius = proxyA.radius + proxyB.radius;
        float target = b2MaxFloat(B2_LINEAR_SLOP(), totalRadius - B2_LINEAR_SLOP());
        float tolerance = 0.25f * B2_LINEAR_SLOP();

        float t1 = 0.0f;
        final int maxIterations = 20;
        int distanceIterations = 0;

        b2SimplexCache cache = new b2SimplexCache();
        b2DistanceInput distanceInput = new b2DistanceInput();
        distanceInput.proxyA = input.proxyA;
        distanceInput.proxyB = input.proxyB;
        distanceInput.useRadii = false;

        for (;;) {
            b2Transform xfA = b2GetSweepTransform(sweepA, t1);
            b2Transform xfB = b2GetSweepTransform(sweepB, t1);

            distanceInput.transformA = xfA;
            distanceInput.transformB = xfB;
            b2DistanceOutput distanceOutput = b2ShapeDistance(distanceInput, cache, null, 0);

            distanceIterations += 1;

            if (distanceOutput.distance <= 0.0f) {
                output.state = b2_toiStateOverlapped;
                output.fraction = 0.0f;
                break;
            }

            if (distanceOutput.distance <= target + tolerance) {
                output.state = b2_toiStateHit;
                output.fraction = t1;
                break;
            }

            b2SeparationFunction fcn = b2MakeSeparationFunction(cache, proxyA, sweepA, proxyB, sweepB, t1);

            boolean done = false;
            float t2 = tMax;
            int pushBackIterations = 0;
            for (;;) {
                int[] indexA = new int[1];
                int[] indexB = new int[1];
                float s2 = b2FindMinSeparation(fcn, indexA, indexB, t2);

                if (s2 > target + tolerance) {
                    output.state = b2_toiStateSeparated;
                    output.fraction = tMax;
                    done = true;
                    break;
                }

                if (s2 > target - tolerance) {
                    t1 = t2;
                    break;
                }

                float s1 = b2EvaluateSeparation(fcn, indexA[0], indexB[0], t1);

                if (s1 < target - tolerance) {
                    output.state = b2_toiStateFailed;
                    output.fraction = t1;
                    done = true;
                    break;
                }

                if (s1 <= target + tolerance) {
                    output.state = b2_toiStateHit;
                    output.fraction = t1;
                    done = true;
                    break;
                }

                int rootIterationCount = 0;
                float a1 = t1;
                float a2 = t2;
                for (;;) {
                    float t;
                    if ((rootIterationCount & 1) != 0) {
                        t = a1 + (target - s1) * (a2 - a1) / (s2 - s1);
                    } else {
                        t = 0.5f * (a1 + a2);
                    }

                    rootIterationCount += 1;
                    float s = b2EvaluateSeparation(fcn, indexA[0], indexB[0], t);

                    if (b2AbsFloat(s - target) < tolerance) {
                        t2 = t;
                        break;
                    }

                    if (s > target) {
                        a1 = t;
                        s1 = s;
                    } else {
                        a2 = t;
                        s2 = s;
                    }

                    if (rootIterationCount == 50) {
                        break;
                    }
                }

                pushBackIterations += 1;
                if (pushBackIterations == B2_MAX_POLYGON_VERTICES) {
                    break;
                }
            }

            if (done) {
                break;
            }

            if (distanceIterations == maxIterations) {
                output.state = b2_toiStateFailed;
                output.fraction = t1;
                break;
            }
        }

        return output;
    }

    private static b2Vec2[] transformed(b2ShapeProxy proxy, b2Transform transform) {
        b2Vec2[] values = b2Vec2.array(proxy.count);
        for (int i = 0; i < proxy.count; ++i) {
            values[i].set(b2TransformPoint(transform, proxy.points[i]));
        }
        return values;
    }

    private static b2Vec2 closestPointOnSegment(b2Vec2 p, b2Vec2 a, b2Vec2 b) {
        b2Vec2 ab = b2Sub(b, a);
        float den = b2Dot(ab, ab);
        if (den == 0.0f) {
            return a.copy();
        }
        float t = b2ClampFloat(b2Dot(b2Sub(p, a), ab) / den, 0.0f, 1.0f);
        return b2MulAdd(a, t, ab);
    }

    private static boolean overlapAt(b2ShapeCastPairInput input, float fraction) {
        b2Transform tb = input.transformB.copy();
        tb.p = b2MulAdd(tb.p, fraction, input.translationB);
        return convexOverlap(transformed(input.proxyA, input.transformA), input.proxyA.count, transformed(input.proxyB, tb), input.proxyB.count);
    }

    private static boolean convexOverlap(b2Vec2[] a, int countA, b2Vec2[] b, int countB) {
        return axesOverlap(a, countA, b, countB) && axesOverlap(b, countB, a, countA);
    }

    private static boolean axesOverlap(b2Vec2[] a, int countA, b2Vec2[] b, int countB) {
        if (countA == 1) {
            return true;
        }
        int edgeCount = countA == 2 ? 1 : countA;
        for (int i = 0; i < edgeCount; ++i) {
            b2Vec2 p1 = a[i];
            b2Vec2 p2 = a[(i + 1) % countA];
            b2Vec2 axis = b2Normalize(b2RightPerp(b2Sub(p2, p1)));
            float minA = Float.MAX_VALUE;
            float maxA = -Float.MAX_VALUE;
            float minB = Float.MAX_VALUE;
            float maxB = -Float.MAX_VALUE;
            for (int j = 0; j < countA; ++j) {
                float v = b2Dot(axis, a[j]);
                minA = b2MinFloat(minA, v);
                maxA = b2MaxFloat(maxA, v);
            }
            for (int j = 0; j < countB; ++j) {
                float v = b2Dot(axis, b[j]);
                minB = b2MinFloat(minB, v);
                maxB = b2MaxFloat(maxB, v);
            }
            if (maxA < minB || maxB < minA) {
                return false;
            }
        }
        return true;
    }

    public static b2PlaneSolverResult b2SolvePlanes(b2Vec2 targetDelta, b2CollisionPlane[] planes, int count) {
        for (int i = 0; i < count; ++i) {
            planes[i].push = 0.0f;
        }

        b2Vec2 delta = targetDelta.copy();
        float tolerance = B2_LINEAR_SLOP();
        int iteration;
        for (iteration = 0; iteration < 20; ++iteration) {
            float totalPush = 0.0f;
            for (int planeIndex = 0; planeIndex < count; ++planeIndex) {
                b2CollisionPlane plane = planes[planeIndex];
                float separation = b2PlaneSeparation(plane.plane, delta) + B2_LINEAR_SLOP();
                float push = -separation;
                float accumulatedPush = plane.push;
                plane.push = b2ClampFloat(plane.push + push, 0.0f, plane.pushLimit);
                push = plane.push - accumulatedPush;
                delta = b2MulAdd(delta, push, plane.plane.normal);
                totalPush += b2AbsFloat(push);
            }
            if (totalPush < tolerance) {
                break;
            }
        }

        b2PlaneSolverResult result = new b2PlaneSolverResult();
        result.translation = delta;
        result.iterationCount = iteration;
        return result;
    }

    public static b2Vec2 b2ClipVector(b2Vec2 vector, b2CollisionPlane[] planes, int count) {
        b2Vec2 v = vector.copy();
        for (int planeIndex = 0; planeIndex < count; ++planeIndex) {
            b2CollisionPlane plane = planes[planeIndex];
            if (plane.push == 0.0f || !plane.clipVelocity) {
                continue;
            }
            v = b2MulSub(v, b2MinFloat(0.0f, b2Dot(v, plane.plane.normal)), plane.plane.normal);
        }
        return v;
    }

    public static b2WorldId b2CreateWorld(b2WorldDef def) {
        B2DebugHooks.beforeWorldCreated(def);
        for (int i = 0; i < WORLDS.length; ++i) {
            if (WORLDS[i] == null) {
                int generation = WORLD_GENERATIONS[i];
                WORLDS[i] = new WorldSlot(i, generation, copyWorldDef(def));
                b2WorldId worldId = new b2WorldId(i + 1, generation);
                B2DebugHooks.worldCreated(worldId);
                return worldId;
            }
        }
        throw new IllegalStateException("Maximum Box2D world count reached");
    }

    private static void b2ParallelFor(WorldSlot world, int itemCount, int minRange,
                                      b2TaskCallback task, Object taskContext) {
        if (itemCount <= 0) {
            return;
        }
        int range = b2MaxInt(1, minRange);
        if (world.workerCount == 1 || itemCount < 2 * range) {
            task.invoke(0, itemCount, 0, taskContext);
            return;
        }
        Object taskHandle = world.enqueueTask.invoke(task, itemCount, minRange, taskContext,
            world.userTaskContext);
        if (taskHandle != null) {
            world.finishTask.invoke(taskHandle, world.userTaskContext);
        }
    }

    private static <T> void b2ParallelList(WorldSlot world, java.util.List<T> items, int minRange,
                                           java.util.function.Consumer<java.util.List<T>> rangeTask) {
        b2ParallelFor(world, items.size(), minRange,
            (startIndex, endIndex, workerIndex, taskContext) ->
                rangeTask.accept(items.subList(startIndex, endIndex)), null);
    }

    public static void b2DestroyWorld(b2WorldId worldId) {
        WorldSlot world = getWorld(worldId);
        if (world == null) {
            return;
        }
        if (B2DebugHooks.beforeWorldDestroyed(worldId)) {
            return;
        }
        for (BodySlot body : world.bodies) {
            if (body != null) {
                body.alive = false;
                body.generation += 1;
            }
        }
        for (ShapeSlot shape : world.shapes) {
            if (shape != null) {
                shape.alive = false;
                shape.generation += 1;
            }
        }
        for (ChainSlot chain : world.chains) {
            if (chain != null) {
                chain.alive = false;
                chain.generation += 1;
            }
        }
        for (JointSlot joint : world.joints) {
            if (joint != null) {
                joint.alive = false;
                joint.generation += 1;
            }
        }
        world.contacts.clear();
        world.freeContactIndices.clear();
        world.nextContactIndex = 0;
        world.contactCount = 0;
        b2DestroyBroadPhase(world.broadPhase);
        WORLDS[world.index] = null;
        WORLD_GENERATIONS[world.index] = (WORLD_GENERATIONS[world.index] + 1) & 0xFFFF;
    }

    public static boolean b2World_IsValid(b2WorldId worldId) {
        return getWorld(worldId) != null;
    }

    public static b2BodyId b2CreateBody(b2WorldId worldId, b2BodyDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot body = new BodySlot();
        body.index = b2AllocSlot(world.freeBodyIndices, world.bodies.size());
        if (body.index < world.bodies.size()) {
            body.generation = world.bodies.get(body.index).generation;
        }
        body.worldIndex = world.index;
        body.type = def.type;
        body.position = def.position.copy();
        body.rotation = def.rotation.copy();
        body.linearVelocity = def.linearVelocity.copy();
        body.angularVelocity = def.angularVelocity;
        body.linearDamping = def.linearDamping;
        body.angularDamping = def.angularDamping;
        body.gravityScale = def.gravityScale;
        body.enabled = def.isEnabled;
        body.awake = body.type != b2_staticBody && (def.isAwake || !def.enableSleep) && def.isEnabled;
        body.fixedRotation = def.fixedRotation;
        body.bullet = def.isBullet;
        body.allowFastRotation = def.allowFastRotation;
        body.enableSleep = def.enableSleep;
        body.sleepThreshold = def.sleepThreshold;
        body.name = b2CopyBodyName(def.name);
        body.userData = def.userData;
        body.center = def.position.copy();
        b2StoreSlot(world.bodies, body.index, body);
        if (body.enabled && body.type == b2_dynamicBody) {
            world.solverBodyOrder.add(body);
        }
        if (body.enabled && body.type != b2_staticBody) {
            body.sleepIslandId = b2CreateSleepIsland(world);
        }
        return bodyId(world, body);
    }

    public static void b2DestroyBody(b2BodyId bodyId) {
        BodySlot body = getBody(bodyId);
        if (body == null) {
            return;
        }
        body.alive = false;
        body.generation += 1;
        WorldSlot world = WORLDS[body.worldIndex];
        b2RemoveSwap(world.solverBodyOrder, body);
        for (int i = body.chains.size() - 1; i >= 0; --i) {
            ChainSlot chain = body.chains.get(i);
            chain.alive = false;
            chain.generation += 1;
            world.freeChainIndices.add(chain.index);
        }
        for (int i = body.shapes.size() - 1; i >= 0; --i) {
            ShapeSlot shape = body.shapes.get(i);
            b2DestroyShapeProxy(shape, world.broadPhase);
            shape.alive = false;
            shape.generation += 1;
            world.freeShapeIndices.add(shape.index);
        }
        for (JointSlot joint : new java.util.ArrayList<>(body.joints)) {
            b2DestroyJointInternal(world, joint);
        }
        b2DestroyContactsForBody(world, body, true);
        body.chains.clear();
        body.contacts.clear();
        body.joints.clear();
        body.shapes.clear();
        world.freeBodyIndices.add(body.index);
    }

    public static boolean b2Body_IsValid(b2BodyId bodyId) {
        return getBody(bodyId) != null;
    }

    public static int b2Body_GetType(b2BodyId bodyId) {
        return requireBody(bodyId).type;
    }

    public static void b2Body_SetType(b2BodyId bodyId, int type) {
        BodySlot body = requireBody(bodyId);
        if (body.type == type) {
            return;
        }
        WorldSlot world = WORLDS[body.worldIndex];
        if (body.enabled && body.type == b2_dynamicBody) {
            b2RemoveSwap(world.solverBodyOrder, body);
        }
        body.type = type;
        if (body.enabled && body.type == b2_dynamicBody) {
            world.solverBodyOrder.add(body);
        }
        if (type == b2_staticBody) {
            body.linearVelocity = b2Vec2_zero.copy();
            body.angularVelocity = 0.0f;
            body.force = b2Vec2_zero.copy();
            body.torque = 0.0f;
        }
        b2DestroyContactsForBody(world, body);
        b2WakeBody(body);
        for (JointSlot joint : body.joints) {
            b2WakeBody(joint.bodyA);
            b2WakeBody(joint.bodyB);
        }
        if (body.enabled) {
            for (ShapeSlot shape : body.shapes) {
                b2DestroyShapeProxy(shape, world.broadPhase);
                boolean forcePairCreation = shape.def.invokeContactCreation || shape.def.isSensor;
                b2CreateShapeProxy(shape, world.broadPhase, body.type, forcePairCreation);
            }
        }
        b2UpdateBodyMassData(body);
        b2RebuildContactPairSet(world);
    }

    public static void b2Body_SetName(b2BodyId bodyId, String name) {
        requireBody(bodyId).name = b2CopyBodyName(name);
    }

    public static String b2Body_GetName(b2BodyId bodyId) {
        return requireBody(bodyId).name;
    }

    public static void b2Body_SetUserData(b2BodyId bodyId, Object userData) {
        requireBody(bodyId).userData = userData;
    }

    public static Object b2Body_GetUserData(b2BodyId bodyId) {
        return requireBody(bodyId).userData;
    }

    public static void b2Body_SetTransform(b2BodyId bodyId, b2Vec2 position, b2Rot rotation) {
        BodySlot body = requireBody(bodyId);
        body.position = position.copy();
        body.rotation = rotation.copy();
        body.center = b2TransformPoint(new b2Transform(body.position, body.rotation), body.localCenter);
        b2SynchronizeBodyProxies(WORLDS[body.worldIndex], body);
    }

    public static b2ShapeId b2CreatePolygonShape(b2BodyId bodyId, b2ShapeDef def, b2Polygon polygon) {
        ShapeSlot shape = createShape(bodyId, def, 1);
        shape.polygon = b2TransformPolygon(b2Transform_identity, polygon);
        finishShapeCreate(shape);
        return shapeId(requireWorld(new b2WorldId(shape.body.worldIndex + 1, WORLD_GENERATIONS[shape.body.worldIndex])), shape);
    }

    public static b2ShapeId b2CreateCircleShape(b2BodyId bodyId, b2ShapeDef def, b2Circle circle) {
        ShapeSlot shape = createShape(bodyId, def, 2);
        shape.circle = new b2Circle(circle.center, circle.radius);
        finishShapeCreate(shape);
        return shapeId(requireWorld(new b2WorldId(shape.body.worldIndex + 1, WORLD_GENERATIONS[shape.body.worldIndex])), shape);
    }

    public static b2ShapeId b2CreateCapsuleShape(b2BodyId bodyId, b2ShapeDef def, b2Capsule capsule) {
        ShapeSlot shape = createShape(bodyId, def, 4);
        shape.capsule = new b2Capsule(capsule.center1, capsule.center2, capsule.radius);
        finishShapeCreate(shape);
        return shapeId(requireWorld(new b2WorldId(shape.body.worldIndex + 1, WORLD_GENERATIONS[shape.body.worldIndex])), shape);
    }

    public static b2ShapeId b2CreateSegmentShape(b2BodyId bodyId, b2ShapeDef def, b2Segment segment) {
        ShapeSlot shape = createShape(bodyId, def, 3);
        shape.segment = new b2Segment(segment.point1, segment.point2);
        finishShapeCreate(shape);
        return shapeId(requireWorld(new b2WorldId(shape.body.worldIndex + 1, WORLD_GENERATIONS[shape.body.worldIndex])), shape);
    }

    public static void b2DestroyShape(b2ShapeId shapeId, boolean updateBodyMass) {
        ShapeSlot shape = getShape(shapeId);
        if (shape == null) {
            return;
        }
        b2DestroyShapeInternal(WORLDS[shape.body.worldIndex], shape, updateBodyMass);
    }

    public static b2ChainId b2CreateChain(b2BodyId bodyId, b2ChainDef def) {
        BodySlot body = requireBody(bodyId);
        WorldSlot world = WORLDS[body.worldIndex];
        b2Assert(def.count >= 4, "chain point count is at least 4");
        b2Assert(def.materialCount == 1 || def.materialCount == def.count, "chain material count is 1 or point count");

        ChainSlot chain = new ChainSlot();
        chain.index = b2AllocSlot(world.freeChainIndices, world.chains.size());
        if (chain.index < world.chains.size()) {
            chain.generation = world.chains.get(chain.index).generation;
        }
        chain.body = body;
        chain.count = def.isLoop ? def.count : def.count - 3;
        chain.shapeIndices = new int[chain.count];
        chain.materials = new b2SurfaceMaterial[def.materialCount];
        for (int i = 0; i < def.materialCount; ++i) {
            chain.materials[i] = copySurfaceMaterial(def.materials[i]);
        }
        b2StoreSlot(world.chains, chain.index, chain);
        body.chains.add(chain);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.userData = def.userData;
        shapeDef.filter = copyFilter(def.filter);
        shapeDef.enableSensorEvents = def.enableSensorEvents;
        shapeDef.enableContactEvents = false;
        shapeDef.enableHitEvents = false;
        shapeDef.invokeContactCreation = true;
        shapeDef.updateBodyMass = false;

        b2Vec2[] points = def.points;
        int n = def.count;
        if (def.isLoop) {
            int prevIndex = n - 1;
            for (int i = 0; i < n - 2; ++i) {
                b2ChainSegment segment = new b2ChainSegment();
                segment.ghost1.set(points[prevIndex]);
                segment.segment = new b2Segment(points[i], points[i + 1]);
                segment.ghost2.set(points[i + 2]);
                segment.chainId = chain.index;
                prevIndex = i;
                chain.shapeIndices[i] = b2CreateChainSegmentShape(body, chain, shapeDef, segment, def.materials[def.materialCount == 1 ? 0 : i]);
            }
            b2ChainSegment segment = new b2ChainSegment();
            segment.ghost1.set(points[n - 3]);
            segment.segment = new b2Segment(points[n - 2], points[n - 1]);
            segment.ghost2.set(points[0]);
            segment.chainId = chain.index;
            chain.shapeIndices[n - 2] = b2CreateChainSegmentShape(body, chain, shapeDef, segment, def.materials[def.materialCount == 1 ? 0 : n - 2]);

            segment = new b2ChainSegment();
            segment.ghost1.set(points[n - 2]);
            segment.segment = new b2Segment(points[n - 1], points[0]);
            segment.ghost2.set(points[1]);
            segment.chainId = chain.index;
            chain.shapeIndices[n - 1] = b2CreateChainSegmentShape(body, chain, shapeDef, segment, def.materials[def.materialCount == 1 ? 0 : n - 1]);
        } else {
            for (int i = 0; i < n - 3; ++i) {
                b2ChainSegment segment = new b2ChainSegment();
                segment.ghost1.set(points[i]);
                segment.segment = new b2Segment(points[i + 1], points[i + 2]);
                segment.ghost2.set(points[i + 3]);
                segment.chainId = chain.index;
                chain.shapeIndices[i] = b2CreateChainSegmentShape(body, chain, shapeDef, segment, def.materials[def.materialCount == 1 ? 0 : i + 1]);
            }
        }

        return chainId(world, chain);
    }

    public static void b2DestroyChain(b2ChainId chainId) {
        ChainSlot chain = getChain(chainId);
        if (chain == null) {
            return;
        }
        WorldSlot world = WORLDS[chain.body.worldIndex];
        chain.alive = false;
        chain.generation += 1;
        world.freeChainIndices.add(chain.index);
        chain.body.chains.remove(chain);
        for (int shapeIndex : chain.shapeIndices) {
            ShapeSlot shape = world.shapes.get(shapeIndex);
            b2DestroyShapeProxy(shape, world.broadPhase);
            b2DestroyContactsForShape(world, shape);
            shape.alive = false;
            shape.generation += 1;
            world.freeShapeIndices.add(shape.index);
            chain.body.shapes.remove(shape);
        }
        b2RebuildContactPairSet(world);
    }

    public static b2WorldId b2Chain_GetWorld(b2ChainId chainId) {
        ChainSlot chain = requireChain(chainId);
        WorldSlot world = WORLDS[chain.body.worldIndex];
        return new b2WorldId(chainId.world0 + 1, world.generation);
    }

    public static int b2Chain_GetSegmentCount(b2ChainId chainId) {
        return requireChain(chainId).count;
    }

    public static int b2Chain_GetSegments(b2ChainId chainId, b2ShapeId[] segmentArray, int capacity) {
        ChainSlot chain = requireChain(chainId);
        WorldSlot world = WORLDS[chain.body.worldIndex];
        int count = b2MinInt(chain.count, b2MinInt(capacity, segmentArray.length));
        for (int i = 0; i < count; ++i) {
            ShapeSlot shape = world.shapes.get(chain.shapeIndices[i]);
            segmentArray[i] = shapeId(world, shape);
        }
        return count;
    }

    public static void b2Chain_SetFriction(b2ChainId chainId, float friction) {
        ChainSlot chain = requireChain(chainId);
        for (b2SurfaceMaterial material : chain.materials) {
            material.friction = friction;
        }
        forEachChainShape(chain, shape -> shape.def.material.friction = friction);
    }

    public static float b2Chain_GetFriction(b2ChainId chainId) {
        return requireChain(chainId).materials[0].friction;
    }

    public static void b2Chain_SetRestitution(b2ChainId chainId, float restitution) {
        ChainSlot chain = requireChain(chainId);
        for (b2SurfaceMaterial material : chain.materials) {
            material.restitution = restitution;
        }
        forEachChainShape(chain, shape -> shape.def.material.restitution = restitution);
    }

    public static float b2Chain_GetRestitution(b2ChainId chainId) {
        return requireChain(chainId).materials[0].restitution;
    }

    public static void b2Chain_SetMaterial(b2ChainId chainId, int material) {
        ChainSlot chain = requireChain(chainId);
        for (b2SurfaceMaterial surfaceMaterial : chain.materials) {
            surfaceMaterial.userMaterialId = material;
        }
        forEachChainShape(chain, shape -> shape.def.material.userMaterialId = material);
    }

    public static int b2Chain_GetMaterial(b2ChainId chainId) {
        return requireChain(chainId).materials[0].userMaterialId;
    }

    public static boolean b2Chain_IsValid(b2ChainId chainId) {
        return getChain(chainId) != null;
    }

    public static b2JointId b2CreateDistanceJoint(b2WorldId worldId, b2DistanceJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_distanceJoint, def.collideConnected);
        joint.localAnchorA = def.localAnchorA.copy();
        joint.localAnchorB = def.localAnchorB.copy();
        joint.distanceLength = b2MaxFloat(def.length, B2_LINEAR_SLOP());
        joint.distanceHertz = def.hertz;
        joint.distanceDampingRatio = def.dampingRatio;
        joint.distanceMinLength = b2MaxFloat(def.minLength, B2_LINEAR_SLOP());
        joint.distanceMaxLength = b2MaxFloat(def.minLength, def.maxLength);
        joint.distanceMaxMotorForce = def.maxMotorForce;
        joint.distanceMotorSpeed = def.motorSpeed;
        joint.distanceEnableSpring = def.enableSpring;
        joint.distanceEnableLimit = def.enableLimit;
        joint.distanceEnableMotor = def.enableMotor;
        if (!def.collideConnected) {
            b2DestroyContactsBetweenBodies(world, bodyA, bodyB);
        }
        return jointId(world, joint);
    }

    public static void b2DistanceJoint_SetLength(b2JointId jointId, float length) {
        JointSlot joint = requireDistanceJoint(jointId);
        joint.distanceLength = b2ClampFloat(length, B2_LINEAR_SLOP(), B2_HUGE());
        joint.distanceImpulse = 0.0f;
        joint.distanceLowerImpulse = 0.0f;
        joint.distanceUpperImpulse = 0.0f;
    }

    public static float b2DistanceJoint_GetLength(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceLength;
    }

    public static void b2DistanceJoint_EnableSpring(b2JointId jointId, boolean enableSpring) {
        requireDistanceJoint(jointId).distanceEnableSpring = enableSpring;
    }

    public static boolean b2DistanceJoint_IsSpringEnabled(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceEnableSpring;
    }

    public static void b2DistanceJoint_SetSpringHertz(b2JointId jointId, float hertz) {
        requireDistanceJoint(jointId).distanceHertz = hertz;
    }

    public static void b2DistanceJoint_SetSpringDampingRatio(b2JointId jointId, float dampingRatio) {
        requireDistanceJoint(jointId).distanceDampingRatio = dampingRatio;
    }

    public static float b2DistanceJoint_GetSpringHertz(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceHertz;
    }

    public static float b2DistanceJoint_GetSpringDampingRatio(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceDampingRatio;
    }

    public static void b2DistanceJoint_EnableLimit(b2JointId jointId, boolean enableLimit) {
        requireDistanceJoint(jointId).distanceEnableLimit = enableLimit;
    }

    public static boolean b2DistanceJoint_IsLimitEnabled(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceEnableLimit;
    }

    public static void b2DistanceJoint_SetLengthRange(b2JointId jointId, float minLength, float maxLength) {
        JointSlot joint = requireDistanceJoint(jointId);
        minLength = b2ClampFloat(minLength, B2_LINEAR_SLOP(), B2_HUGE());
        maxLength = b2ClampFloat(maxLength, B2_LINEAR_SLOP(), B2_HUGE());
        joint.distanceMinLength = b2MinFloat(minLength, maxLength);
        joint.distanceMaxLength = b2MaxFloat(minLength, maxLength);
        joint.distanceImpulse = 0.0f;
        joint.distanceLowerImpulse = 0.0f;
        joint.distanceUpperImpulse = 0.0f;
    }

    public static float b2DistanceJoint_GetMinLength(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceMinLength;
    }

    public static float b2DistanceJoint_GetMaxLength(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceMaxLength;
    }

    public static float b2DistanceJoint_GetCurrentLength(b2JointId jointId) {
        JointSlot joint = requireDistanceJoint(jointId);
        b2Vec2 pA = b2TransformPoint(new b2Transform(joint.bodyA.position, joint.bodyA.rotation), joint.localAnchorA);
        b2Vec2 pB = b2TransformPoint(new b2Transform(joint.bodyB.position, joint.bodyB.rotation), joint.localAnchorB);
        return b2Length(b2Sub(pB, pA));
    }

    public static void b2DistanceJoint_EnableMotor(b2JointId jointId, boolean enableMotor) {
        JointSlot joint = requireDistanceJoint(jointId);
        if (enableMotor != joint.distanceEnableMotor) {
            joint.distanceEnableMotor = enableMotor;
            joint.distanceMotorImpulse = 0.0f;
        }
    }

    public static boolean b2DistanceJoint_IsMotorEnabled(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceEnableMotor;
    }

    public static void b2DistanceJoint_SetMotorSpeed(b2JointId jointId, float motorSpeed) {
        requireDistanceJoint(jointId).distanceMotorSpeed = motorSpeed;
    }

    public static float b2DistanceJoint_GetMotorSpeed(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceMotorSpeed;
    }

    public static void b2DistanceJoint_SetMaxMotorForce(b2JointId jointId, float force) {
        requireDistanceJoint(jointId).distanceMaxMotorForce = force;
    }

    public static float b2DistanceJoint_GetMaxMotorForce(b2JointId jointId) {
        return requireDistanceJoint(jointId).distanceMaxMotorForce;
    }

    public static float b2DistanceJoint_GetMotorForce(b2JointId jointId) {
        JointSlot joint = requireDistanceJoint(jointId);
        return WORLDS[joint.bodyA.worldIndex].invH * joint.distanceMotorImpulse;
    }

    public static b2JointId b2CreateMotorJoint(b2WorldId worldId, b2MotorJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_motorJoint, def.collideConnected);
        joint.motorLinearOffset = def.linearOffset.copy();
        joint.motorAngularOffset = def.angularOffset;
        joint.motorMaxForce = def.maxForce;
        joint.motorMaxTorque = def.maxTorque;
        joint.motorCorrectionFactor = b2ClampFloat(def.correctionFactor, 0.0f, 1.0f);
        if (!def.collideConnected) {
            b2DestroyContactsBetweenBodies(world, bodyA, bodyB);
        }
        return jointId(world, joint);
    }

    public static void b2MotorJoint_SetLinearOffset(b2JointId jointId, b2Vec2 linearOffset) {
        requireMotorJoint(jointId).motorLinearOffset = linearOffset.copy();
    }

    public static b2Vec2 b2MotorJoint_GetLinearOffset(b2JointId jointId) {
        return requireMotorJoint(jointId).motorLinearOffset.copy();
    }

    public static void b2MotorJoint_SetAngularOffset(b2JointId jointId, float angularOffset) {
        requireMotorJoint(jointId).motorAngularOffset = angularOffset;
    }

    public static float b2MotorJoint_GetAngularOffset(b2JointId jointId) {
        return requireMotorJoint(jointId).motorAngularOffset;
    }

    public static void b2MotorJoint_SetMaxForce(b2JointId jointId, float maxForce) {
        requireMotorJoint(jointId).motorMaxForce = b2MaxFloat(0.0f, maxForce);
    }

    public static float b2MotorJoint_GetMaxForce(b2JointId jointId) {
        return requireMotorJoint(jointId).motorMaxForce;
    }

    public static void b2MotorJoint_SetMaxTorque(b2JointId jointId, float maxTorque) {
        requireMotorJoint(jointId).motorMaxTorque = b2MaxFloat(0.0f, maxTorque);
    }

    public static float b2MotorJoint_GetMaxTorque(b2JointId jointId) {
        return requireMotorJoint(jointId).motorMaxTorque;
    }

    public static void b2MotorJoint_SetCorrectionFactor(b2JointId jointId, float correctionFactor) {
        requireMotorJoint(jointId).motorCorrectionFactor = b2ClampFloat(correctionFactor, 0.0f, 1.0f);
    }

    public static float b2MotorJoint_GetCorrectionFactor(b2JointId jointId) {
        return requireMotorJoint(jointId).motorCorrectionFactor;
    }

    public static b2JointId b2CreateMouseJoint(b2WorldId worldId, b2MouseJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_mouseJoint, def.collideConnected);
        joint.localAnchorA = b2InvTransformPoint(new b2Transform(bodyA.position, bodyA.rotation), def.target);
        joint.localAnchorB = b2InvTransformPoint(new b2Transform(bodyB.position, bodyB.rotation), def.target);
        joint.mouseTarget = def.target.copy();
        joint.mouseHertz = def.hertz;
        joint.mouseDampingRatio = def.dampingRatio;
        joint.mouseMaxForce = def.maxForce;
        return jointId(world, joint);
    }

    public static void b2MouseJoint_SetTarget(b2JointId jointId, b2Vec2 target) {
        requireMouseJoint(jointId).mouseTarget = target.copy();
    }

    public static b2Vec2 b2MouseJoint_GetTarget(b2JointId jointId) {
        return requireMouseJoint(jointId).mouseTarget.copy();
    }

    public static void b2MouseJoint_SetSpringHertz(b2JointId jointId, float hertz) {
        requireMouseJoint(jointId).mouseHertz = hertz;
    }

    public static float b2MouseJoint_GetSpringHertz(b2JointId jointId) {
        return requireMouseJoint(jointId).mouseHertz;
    }

    public static void b2MouseJoint_SetSpringDampingRatio(b2JointId jointId, float dampingRatio) {
        requireMouseJoint(jointId).mouseDampingRatio = dampingRatio;
    }

    public static float b2MouseJoint_GetSpringDampingRatio(b2JointId jointId) {
        return requireMouseJoint(jointId).mouseDampingRatio;
    }

    public static void b2MouseJoint_SetMaxForce(b2JointId jointId, float maxForce) {
        requireMouseJoint(jointId).mouseMaxForce = maxForce;
    }

    public static float b2MouseJoint_GetMaxForce(b2JointId jointId) {
        return requireMouseJoint(jointId).mouseMaxForce;
    }

    public static b2JointId b2CreateFilterJoint(b2WorldId worldId, b2FilterJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_filterJoint, false);
        return jointId(world, joint);
    }

    public static b2JointId b2CreatePrismaticJoint(b2WorldId worldId, b2PrismaticJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_prismaticJoint, def.collideConnected);
        joint.localAnchorA = def.localAnchorA.copy();
        joint.localAnchorB = def.localAnchorB.copy();
        joint.localAxisA = b2Normalize(def.localAxisA);
        joint.referenceAngle = def.referenceAngle;
        joint.prismaticTargetTranslation = def.targetTranslation;
        joint.prismaticHertz = def.hertz;
        joint.prismaticDampingRatio = def.dampingRatio;
        joint.prismaticLowerTranslation = def.lowerTranslation;
        joint.prismaticUpperTranslation = def.upperTranslation;
        joint.prismaticMaxMotorForce = def.maxMotorForce;
        joint.prismaticMotorSpeed = def.motorSpeed;
        joint.prismaticEnableSpring = def.enableSpring;
        joint.prismaticEnableLimit = def.enableLimit;
        joint.prismaticEnableMotor = def.enableMotor;
        if (!def.collideConnected) {
            b2DestroyContactsBetweenBodies(world, bodyA, bodyB);
        }
        return jointId(world, joint);
    }

    public static void b2PrismaticJoint_EnableSpring(b2JointId jointId, boolean enableSpring) {
        JointSlot joint = requirePrismaticJoint(jointId);
        if (enableSpring != joint.prismaticEnableSpring) {
            joint.prismaticEnableSpring = enableSpring;
            joint.prismaticSpringImpulse = 0.0f;
        }
    }

    public static boolean b2PrismaticJoint_IsSpringEnabled(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticEnableSpring;
    }

    public static void b2PrismaticJoint_SetSpringHertz(b2JointId jointId, float hertz) {
        requirePrismaticJoint(jointId).prismaticHertz = hertz;
    }

    public static float b2PrismaticJoint_GetSpringHertz(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticHertz;
    }

    public static void b2PrismaticJoint_SetSpringDampingRatio(b2JointId jointId, float dampingRatio) {
        requirePrismaticJoint(jointId).prismaticDampingRatio = dampingRatio;
    }

    public static float b2PrismaticJoint_GetSpringDampingRatio(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticDampingRatio;
    }

    public static void b2PrismaticJoint_SetTargetTranslation(b2JointId jointId, float translation) {
        requirePrismaticJoint(jointId).prismaticTargetTranslation = translation;
    }

    public static float b2PrismaticJoint_GetTargetTranslation(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticTargetTranslation;
    }

    public static void b2PrismaticJoint_EnableLimit(b2JointId jointId, boolean enableLimit) {
        JointSlot joint = requirePrismaticJoint(jointId);
        if (enableLimit != joint.prismaticEnableLimit) {
            joint.prismaticEnableLimit = enableLimit;
            joint.prismaticLowerImpulse = 0.0f;
            joint.prismaticUpperImpulse = 0.0f;
        }
    }

    public static boolean b2PrismaticJoint_IsLimitEnabled(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticEnableLimit;
    }

    public static float b2PrismaticJoint_GetLowerLimit(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticLowerTranslation;
    }

    public static float b2PrismaticJoint_GetUpperLimit(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticUpperTranslation;
    }

    public static void b2PrismaticJoint_SetLimits(b2JointId jointId, float lower, float upper) {
        JointSlot joint = requirePrismaticJoint(jointId);
        if (lower != joint.prismaticLowerTranslation || upper != joint.prismaticUpperTranslation) {
            joint.prismaticLowerTranslation = b2MinFloat(lower, upper);
            joint.prismaticUpperTranslation = b2MaxFloat(lower, upper);
            joint.prismaticLowerImpulse = 0.0f;
            joint.prismaticUpperImpulse = 0.0f;
        }
    }

    public static void b2PrismaticJoint_EnableMotor(b2JointId jointId, boolean enableMotor) {
        JointSlot joint = requirePrismaticJoint(jointId);
        if (enableMotor != joint.prismaticEnableMotor) {
            joint.prismaticEnableMotor = enableMotor;
            joint.prismaticMotorImpulse = 0.0f;
        }
    }

    public static boolean b2PrismaticJoint_IsMotorEnabled(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticEnableMotor;
    }

    public static void b2PrismaticJoint_SetMotorSpeed(b2JointId jointId, float motorSpeed) {
        requirePrismaticJoint(jointId).prismaticMotorSpeed = motorSpeed;
    }

    public static float b2PrismaticJoint_GetMotorSpeed(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticMotorSpeed;
    }

    public static float b2PrismaticJoint_GetMotorForce(b2JointId jointId) {
        JointSlot joint = requirePrismaticJoint(jointId);
        return WORLDS[joint.bodyA.worldIndex].invH * joint.prismaticMotorImpulse;
    }

    public static void b2PrismaticJoint_SetMaxMotorForce(b2JointId jointId, float force) {
        requirePrismaticJoint(jointId).prismaticMaxMotorForce = force;
    }

    public static float b2PrismaticJoint_GetMaxMotorForce(b2JointId jointId) {
        return requirePrismaticJoint(jointId).prismaticMaxMotorForce;
    }

    public static float b2PrismaticJoint_GetTranslation(b2JointId jointId) {
        JointSlot joint = requirePrismaticJoint(jointId);
        b2Transform transformA = new b2Transform(joint.bodyA.position, joint.bodyA.rotation);
        b2Transform transformB = new b2Transform(joint.bodyB.position, joint.bodyB.rotation);
        b2Vec2 axisA = b2RotateVector(transformA.q, joint.localAxisA);
        b2Vec2 pA = b2TransformPoint(transformA, joint.localAnchorA);
        b2Vec2 pB = b2TransformPoint(transformB, joint.localAnchorB);
        return b2Dot(b2Sub(pB, pA), axisA);
    }

    public static float b2PrismaticJoint_GetSpeed(b2JointId jointId) {
        JointSlot joint = requirePrismaticJoint(jointId);
        b2Transform transformA = new b2Transform(joint.bodyA.position, joint.bodyA.rotation);
        b2Transform transformB = new b2Transform(joint.bodyB.position, joint.bodyB.rotation);
        b2Vec2 axisA = b2RotateVector(transformA.q, joint.localAxisA);
        b2Vec2 rA = b2RotateVector(transformA.q, b2Sub(joint.localAnchorA, joint.bodyA.localCenter));
        b2Vec2 rB = b2RotateVector(transformB.q, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        b2Vec2 d = b2Add(b2Sub(joint.bodyB.center, joint.bodyA.center), b2Sub(rB, rA));
        b2Vec2 vRel = b2Sub(b2Add(joint.bodyB.linearVelocity, b2CrossSV(joint.bodyB.angularVelocity, rB)),
            b2Add(joint.bodyA.linearVelocity, b2CrossSV(joint.bodyA.angularVelocity, rA)));
        return b2Dot(d, b2CrossSV(joint.bodyA.angularVelocity, axisA)) + b2Dot(axisA, vRel);
    }

    public static b2JointId b2CreateRevoluteJoint(b2WorldId worldId, b2RevoluteJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_revoluteJoint, def.collideConnected);
        joint.localAnchorA = def.localAnchorA.copy();
        joint.localAnchorB = def.localAnchorB.copy();
        joint.referenceAngle = b2ClampFloat(def.referenceAngle, -B2_PI, B2_PI);
        joint.revoluteTargetAngle = b2ClampFloat(def.targetAngle, -B2_PI, B2_PI);
        joint.revoluteHertz = def.hertz;
        joint.revoluteDampingRatio = def.dampingRatio;
        joint.revoluteLowerAngle = def.lowerAngle;
        joint.revoluteUpperAngle = def.upperAngle;
        joint.revoluteMaxMotorTorque = def.maxMotorTorque;
        joint.revoluteMotorSpeed = def.motorSpeed;
        joint.revoluteEnableSpring = def.enableSpring;
        joint.revoluteEnableLimit = def.enableLimit;
        joint.revoluteEnableMotor = def.enableMotor;
        if (!def.collideConnected) {
            b2DestroyContactsBetweenBodies(world, bodyA, bodyB);
        }
        return jointId(world, joint);
    }

    public static void b2RevoluteJoint_EnableSpring(b2JointId jointId, boolean enableSpring) {
        JointSlot joint = requireRevoluteJoint(jointId);
        if (enableSpring != joint.revoluteEnableSpring) {
            joint.revoluteEnableSpring = enableSpring;
            joint.revoluteSpringImpulse = 0.0f;
        }
    }

    public static boolean b2RevoluteJoint_IsSpringEnabled(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteEnableSpring;
    }

    public static void b2RevoluteJoint_SetSpringHertz(b2JointId jointId, float hertz) {
        requireRevoluteJoint(jointId).revoluteHertz = hertz;
    }

    public static float b2RevoluteJoint_GetSpringHertz(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteHertz;
    }

    public static void b2RevoluteJoint_SetSpringDampingRatio(b2JointId jointId, float dampingRatio) {
        requireRevoluteJoint(jointId).revoluteDampingRatio = dampingRatio;
    }

    public static float b2RevoluteJoint_GetSpringDampingRatio(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteDampingRatio;
    }

    public static void b2RevoluteJoint_SetTargetAngle(b2JointId jointId, float angle) {
        requireRevoluteJoint(jointId).revoluteTargetAngle = angle;
    }

    public static float b2RevoluteJoint_GetTargetAngle(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteTargetAngle;
    }

    public static float b2RevoluteJoint_GetAngle(b2JointId jointId) {
        JointSlot joint = requireRevoluteJoint(jointId);
        return b2UnwindAngle(b2RelativeAngle(joint.bodyB.rotation, joint.bodyA.rotation) - joint.referenceAngle);
    }

    public static void b2RevoluteJoint_EnableLimit(b2JointId jointId, boolean enableLimit) {
        JointSlot joint = requireRevoluteJoint(jointId);
        if (enableLimit != joint.revoluteEnableLimit) {
            joint.revoluteEnableLimit = enableLimit;
            joint.revoluteLowerImpulse = 0.0f;
            joint.revoluteUpperImpulse = 0.0f;
        }
    }

    public static boolean b2RevoluteJoint_IsLimitEnabled(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteEnableLimit;
    }

    public static float b2RevoluteJoint_GetLowerLimit(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteLowerAngle;
    }

    public static float b2RevoluteJoint_GetUpperLimit(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteUpperAngle;
    }

    public static void b2RevoluteJoint_SetLimits(b2JointId jointId, float lower, float upper) {
        JointSlot joint = requireRevoluteJoint(jointId);
        if (lower != joint.revoluteLowerAngle || upper != joint.revoluteUpperAngle) {
            joint.revoluteLowerAngle = b2MinFloat(lower, upper);
            joint.revoluteUpperAngle = b2MaxFloat(lower, upper);
            joint.revoluteLowerImpulse = 0.0f;
            joint.revoluteUpperImpulse = 0.0f;
        }
    }

    public static void b2RevoluteJoint_EnableMotor(b2JointId jointId, boolean enableMotor) {
        JointSlot joint = requireRevoluteJoint(jointId);
        if (enableMotor != joint.revoluteEnableMotor) {
            joint.revoluteEnableMotor = enableMotor;
            joint.revoluteMotorImpulse = 0.0f;
        }
    }

    public static boolean b2RevoluteJoint_IsMotorEnabled(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteEnableMotor;
    }

    public static void b2RevoluteJoint_SetMotorSpeed(b2JointId jointId, float motorSpeed) {
        requireRevoluteJoint(jointId).revoluteMotorSpeed = motorSpeed;
    }

    public static float b2RevoluteJoint_GetMotorSpeed(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteMotorSpeed;
    }

    public static float b2RevoluteJoint_GetMotorTorque(b2JointId jointId) {
        JointSlot joint = requireRevoluteJoint(jointId);
        return WORLDS[joint.bodyA.worldIndex].invH * joint.revoluteMotorImpulse;
    }

    public static void b2RevoluteJoint_SetMaxMotorTorque(b2JointId jointId, float torque) {
        requireRevoluteJoint(jointId).revoluteMaxMotorTorque = torque;
    }

    public static float b2RevoluteJoint_GetMaxMotorTorque(b2JointId jointId) {
        return requireRevoluteJoint(jointId).revoluteMaxMotorTorque;
    }

    public static b2JointId b2CreateWeldJoint(b2WorldId worldId, b2WeldJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_weldJoint, def.collideConnected);
        joint.localAnchorA = def.localAnchorA.copy();
        joint.localAnchorB = def.localAnchorB.copy();
        joint.referenceAngle = def.referenceAngle;
        joint.weldLinearHertz = def.linearHertz;
        joint.weldLinearDampingRatio = def.linearDampingRatio;
        joint.weldAngularHertz = def.angularHertz;
        joint.weldAngularDampingRatio = def.angularDampingRatio;
        if (!def.collideConnected) {
            b2DestroyContactsBetweenBodies(world, bodyA, bodyB);
        }
        return jointId(world, joint);
    }

    public static void b2WeldJoint_SetLinearHertz(b2JointId jointId, float hertz) {
        requireWeldJoint(jointId).weldLinearHertz = hertz;
    }

    public static float b2WeldJoint_GetLinearHertz(b2JointId jointId) {
        return requireWeldJoint(jointId).weldLinearHertz;
    }

    public static void b2WeldJoint_SetLinearDampingRatio(b2JointId jointId, float dampingRatio) {
        requireWeldJoint(jointId).weldLinearDampingRatio = dampingRatio;
    }

    public static float b2WeldJoint_GetLinearDampingRatio(b2JointId jointId) {
        return requireWeldJoint(jointId).weldLinearDampingRatio;
    }

    public static void b2WeldJoint_SetAngularHertz(b2JointId jointId, float hertz) {
        requireWeldJoint(jointId).weldAngularHertz = hertz;
    }

    public static float b2WeldJoint_GetAngularHertz(b2JointId jointId) {
        return requireWeldJoint(jointId).weldAngularHertz;
    }

    public static void b2WeldJoint_SetAngularDampingRatio(b2JointId jointId, float dampingRatio) {
        requireWeldJoint(jointId).weldAngularDampingRatio = dampingRatio;
    }

    public static float b2WeldJoint_GetAngularDampingRatio(b2JointId jointId) {
        return requireWeldJoint(jointId).weldAngularDampingRatio;
    }

    public static b2JointId b2CreateWheelJoint(b2WorldId worldId, b2WheelJointDef def) {
        WorldSlot world = requireWorld(worldId);
        BodySlot bodyA = requireBody(def.bodyIdA);
        BodySlot bodyB = requireBody(def.bodyIdB);
        JointSlot joint = b2CreateJointBase(world, bodyA, bodyB, def.userData, b2_wheelJoint, def.collideConnected);
        joint.localAnchorA = def.localAnchorA.copy();
        joint.localAnchorB = def.localAnchorB.copy();
        joint.localAxisA = b2Normalize(def.localAxisA);
        joint.wheelLowerTranslation = def.lowerTranslation;
        joint.wheelUpperTranslation = def.upperTranslation;
        joint.wheelMaxMotorTorque = def.maxMotorTorque;
        joint.wheelMotorSpeed = def.motorSpeed;
        joint.wheelHertz = def.hertz;
        joint.wheelDampingRatio = def.dampingRatio;
        joint.wheelEnableSpring = def.enableSpring;
        joint.wheelEnableLimit = def.enableLimit;
        joint.wheelEnableMotor = def.enableMotor;
        if (!def.collideConnected) {
            b2DestroyContactsBetweenBodies(world, bodyA, bodyB);
        }
        return jointId(world, joint);
    }

    public static void b2WheelJoint_EnableSpring(b2JointId jointId, boolean enableSpring) {
        JointSlot joint = requireWheelJoint(jointId);
        if (enableSpring != joint.wheelEnableSpring) {
            joint.wheelEnableSpring = enableSpring;
            joint.wheelSpringImpulse = 0.0f;
        }
    }

    public static boolean b2WheelJoint_IsSpringEnabled(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelEnableSpring;
    }

    public static void b2WheelJoint_SetSpringHertz(b2JointId jointId, float hertz) {
        requireWheelJoint(jointId).wheelHertz = hertz;
    }

    public static float b2WheelJoint_GetSpringHertz(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelHertz;
    }

    public static void b2WheelJoint_SetSpringDampingRatio(b2JointId jointId, float dampingRatio) {
        requireWheelJoint(jointId).wheelDampingRatio = dampingRatio;
    }

    public static float b2WheelJoint_GetSpringDampingRatio(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelDampingRatio;
    }

    public static void b2WheelJoint_EnableLimit(b2JointId jointId, boolean enableLimit) {
        JointSlot joint = requireWheelJoint(jointId);
        if (enableLimit != joint.wheelEnableLimit) {
            joint.wheelLowerImpulse = 0.0f;
            joint.wheelUpperImpulse = 0.0f;
            joint.wheelEnableLimit = enableLimit;
        }
    }

    public static boolean b2WheelJoint_IsLimitEnabled(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelEnableLimit;
    }

    public static float b2WheelJoint_GetLowerLimit(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelLowerTranslation;
    }

    public static float b2WheelJoint_GetUpperLimit(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelUpperTranslation;
    }

    public static void b2WheelJoint_SetLimits(b2JointId jointId, float lower, float upper) {
        JointSlot joint = requireWheelJoint(jointId);
        if (lower != joint.wheelLowerTranslation || upper != joint.wheelUpperTranslation) {
            joint.wheelLowerTranslation = b2MinFloat(lower, upper);
            joint.wheelUpperTranslation = b2MaxFloat(lower, upper);
            joint.wheelLowerImpulse = 0.0f;
            joint.wheelUpperImpulse = 0.0f;
        }
    }

    public static void b2WheelJoint_EnableMotor(b2JointId jointId, boolean enableMotor) {
        JointSlot joint = requireWheelJoint(jointId);
        if (enableMotor != joint.wheelEnableMotor) {
            joint.wheelMotorImpulse = 0.0f;
            joint.wheelEnableMotor = enableMotor;
        }
    }

    public static boolean b2WheelJoint_IsMotorEnabled(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelEnableMotor;
    }

    public static void b2WheelJoint_SetMotorSpeed(b2JointId jointId, float motorSpeed) {
        requireWheelJoint(jointId).wheelMotorSpeed = motorSpeed;
    }

    public static float b2WheelJoint_GetMotorSpeed(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelMotorSpeed;
    }

    public static void b2WheelJoint_SetMaxMotorTorque(b2JointId jointId, float torque) {
        requireWheelJoint(jointId).wheelMaxMotorTorque = torque;
    }

    public static float b2WheelJoint_GetMaxMotorTorque(b2JointId jointId) {
        return requireWheelJoint(jointId).wheelMaxMotorTorque;
    }

    public static float b2WheelJoint_GetMotorTorque(b2JointId jointId) {
        JointSlot joint = requireWheelJoint(jointId);
        return WORLDS[joint.bodyA.worldIndex].invH * joint.wheelMotorImpulse;
    }

    public static void b2DestroyJoint(b2JointId jointId) {
        JointSlot joint = getJoint(jointId);
        if (joint == null) {
            return;
        }
        b2DestroyJointInternal(WORLDS[joint.bodyA.worldIndex], joint);
    }

    public static boolean b2Joint_IsValid(b2JointId jointId) {
        return getJoint(jointId) != null;
    }

    public static int b2Joint_GetType(b2JointId jointId) {
        return requireJoint(jointId).type;
    }

    public static b2BodyId b2Joint_GetBodyA(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        return bodyId(WORLDS[joint.bodyA.worldIndex], joint.bodyA);
    }

    public static b2BodyId b2Joint_GetBodyB(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        return bodyId(WORLDS[joint.bodyB.worldIndex], joint.bodyB);
    }

    public static b2WorldId b2Joint_GetWorld(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        WorldSlot world = WORLDS[joint.bodyA.worldIndex];
        return new b2WorldId(world.index + 1, world.generation);
    }

    public static void b2Joint_SetLocalAnchorA(b2JointId jointId, b2Vec2 localAnchor) {
        requireJoint(jointId).localAnchorA = localAnchor.copy();
    }

    public static b2Vec2 b2Joint_GetLocalAnchorA(b2JointId jointId) {
        return requireJoint(jointId).localAnchorA.copy();
    }

    public static void b2Joint_SetLocalAnchorB(b2JointId jointId, b2Vec2 localAnchor) {
        requireJoint(jointId).localAnchorB = localAnchor.copy();
    }

    public static b2Vec2 b2Joint_GetLocalAnchorB(b2JointId jointId) {
        return requireJoint(jointId).localAnchorB.copy();
    }

    public static void b2Joint_SetReferenceAngle(b2JointId jointId, float angleInRadians) {
        JointSlot joint = requireJoint(jointId);
        if (joint.type == b2_prismaticJoint || joint.type == b2_revoluteJoint || joint.type == b2_weldJoint) {
            joint.referenceAngle = angleInRadians;
        }
    }

    public static float b2Joint_GetReferenceAngle(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        if (joint.type == b2_prismaticJoint || joint.type == b2_revoluteJoint || joint.type == b2_weldJoint) {
            return joint.referenceAngle;
        }
        return 0.0f;
    }

    public static void b2Joint_SetLocalAxisA(b2JointId jointId, b2Vec2 localAxis) {
        JointSlot joint = requireJoint(jointId);
        if (joint.type == b2_prismaticJoint || joint.type == b2_wheelJoint) {
            joint.localAxisA = localAxis.copy();
        }
    }

    public static b2Vec2 b2Joint_GetLocalAxisA(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        if (joint.type == b2_prismaticJoint || joint.type == b2_wheelJoint) {
            return joint.localAxisA.copy();
        }
        return b2Vec2_zero.copy();
    }

    public static void b2Joint_SetCollideConnected(b2JointId jointId, boolean shouldCollide) {
        JointSlot joint = requireJoint(jointId);
        if (joint.collideConnected == shouldCollide) {
            return;
        }
        joint.collideConnected = shouldCollide;
        WorldSlot world = WORLDS[joint.bodyA.worldIndex];
        if (shouldCollide) {
            for (ShapeSlot shape : joint.bodyA.shapes.size() < joint.bodyB.shapes.size() ? joint.bodyA.shapes : joint.bodyB.shapes) {
                if (shape.proxyKey != B2_NULL_INDEX) {
                    b2BufferMove(world.broadPhase, shape.proxyKey);
                }
            }
        } else {
            b2DestroyContactsBetweenBodies(world, joint.bodyA, joint.bodyB);
        }
    }

    public static boolean b2Joint_GetCollideConnected(b2JointId jointId) {
        return requireJoint(jointId).collideConnected;
    }

    public static void b2Joint_SetUserData(b2JointId jointId, Object userData) {
        requireJoint(jointId).userData = userData;
    }

    public static Object b2Joint_GetUserData(b2JointId jointId) {
        return requireJoint(jointId).userData;
    }

    public static void b2Joint_WakeBodies(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        b2WakeBody(joint.bodyA);
        b2WakeBody(joint.bodyB);
    }

    public static b2Vec2 b2Joint_GetConstraintForce(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        if (joint.type == b2_motorJoint) {
            return b2MulSV(WORLDS[joint.bodyA.worldIndex].invH, joint.motorLinearImpulse);
        }
        if (joint.type == b2_mouseJoint) {
            return b2MulSV(WORLDS[joint.bodyA.worldIndex].invH, joint.mouseLinearImpulse);
        }
        if (joint.type == b2_weldJoint) {
            return b2MulSV(WORLDS[joint.bodyA.worldIndex].invH, joint.weldLinearImpulse);
        }
        if (joint.type == b2_distanceJoint) {
            WorldSlot world = WORLDS[joint.bodyA.worldIndex];
            b2Transform transformA = new b2Transform(joint.bodyA.position, joint.bodyA.rotation);
            b2Transform transformB = new b2Transform(joint.bodyB.position, joint.bodyB.rotation);
            b2Vec2 pA = b2TransformPoint(transformA, joint.localAnchorA);
            b2Vec2 pB = b2TransformPoint(transformB, joint.localAnchorB);
            b2Vec2 axis = b2Normalize(b2Sub(pB, pA));
            float force = (joint.distanceImpulse + joint.distanceLowerImpulse - joint.distanceUpperImpulse
                + joint.distanceMotorImpulse) * world.invH;
            return b2MulSV(force, axis);
        }
        if (joint.type == b2_revoluteJoint) {
            return b2MulSV(WORLDS[joint.bodyA.worldIndex].invH, joint.revoluteLinearImpulse);
        }
        if (joint.type == b2_prismaticJoint) {
            b2Vec2 axisA = b2RotateVector(joint.bodyA.rotation, joint.localAxisA);
            b2Vec2 perpA = b2LeftPerp(axisA);
            float invH = WORLDS[joint.bodyA.worldIndex].invH;
            float perpForce = invH * joint.prismaticImpulse.x;
            float axialForce = invH * (joint.prismaticMotorImpulse + joint.prismaticLowerImpulse - joint.prismaticUpperImpulse);
            return b2Add(b2MulSV(perpForce, perpA), b2MulSV(axialForce, axisA));
        }
        if (joint.type == b2_wheelJoint) {
            b2Vec2 axisA = b2RotateVector(joint.bodyA.rotation, joint.localAxisA);
            b2Vec2 perpA = b2LeftPerp(axisA);
            float invH = WORLDS[joint.bodyA.worldIndex].invH;
            float perpForce = invH * joint.wheelPerpImpulse;
            float axialForce = invH * (joint.wheelSpringImpulse + joint.wheelLowerImpulse - joint.wheelUpperImpulse);
            return b2Add(b2MulSV(perpForce, perpA), b2MulSV(axialForce, axisA));
        }
        return b2Vec2_zero.copy();
    }

    public static float b2Joint_GetConstraintTorque(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        if (joint.type == b2_motorJoint) {
            return WORLDS[joint.bodyA.worldIndex].invH * joint.motorAngularImpulse;
        }
        if (joint.type == b2_mouseJoint) {
            return WORLDS[joint.bodyA.worldIndex].invH * joint.mouseAngularImpulse;
        }
        if (joint.type == b2_weldJoint) {
            return WORLDS[joint.bodyA.worldIndex].invH * joint.weldAngularImpulse;
        }
        if (joint.type == b2_revoluteJoint) {
            return WORLDS[joint.bodyA.worldIndex].invH *
                (joint.revoluteMotorImpulse + joint.revoluteLowerImpulse - joint.revoluteUpperImpulse);
        }
        if (joint.type == b2_prismaticJoint) {
            return WORLDS[joint.bodyA.worldIndex].invH * joint.prismaticImpulse.y;
        }
        if (joint.type == b2_wheelJoint) {
            return WORLDS[joint.bodyA.worldIndex].invH * joint.wheelMotorImpulse;
        }
        return 0.0f;
    }

    public static float b2Joint_GetLinearSeparation(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        b2Transform xfA = new b2Transform(joint.bodyA.position, joint.bodyA.rotation);
        b2Transform xfB = new b2Transform(joint.bodyB.position, joint.bodyB.rotation);
        b2Vec2 pA = b2TransformPoint(xfA, joint.localAnchorA);
        b2Vec2 pB = b2TransformPoint(xfB, joint.localAnchorB);
        b2Vec2 dp = b2Sub(pB, pA);

        if (joint.type == b2_distanceJoint) {
            float length = b2Length(dp);
            if (joint.distanceEnableSpring) {
                if (joint.distanceEnableLimit) {
                    if (length < joint.distanceMinLength) {
                        return joint.distanceMinLength - length;
                    }
                    if (length > joint.distanceMaxLength) {
                        return length - joint.distanceMaxLength;
                    }
                }
                return 0.0f;
            }
            return b2AbsFloat(length - joint.distanceLength);
        }
        if (joint.type == b2_prismaticJoint) {
            return b2GetAxisJointLinearSeparation(joint, dp, joint.prismaticEnableLimit,
                joint.prismaticLowerTranslation, joint.prismaticUpperTranslation);
        }
        if (joint.type == b2_revoluteJoint) {
            return b2Length(dp);
        }
        if (joint.type == b2_weldJoint) {
            return joint.weldLinearHertz == 0.0f ? b2Length(dp) : 0.0f;
        }
        if (joint.type == b2_wheelJoint) {
            return b2GetAxisJointLinearSeparation(joint, dp, joint.wheelEnableLimit,
                joint.wheelLowerTranslation, joint.wheelUpperTranslation);
        }
        return 0.0f;
    }

    public static float b2Joint_GetAngularSeparation(b2JointId jointId) {
        JointSlot joint = requireJoint(jointId);
        float relativeAngle = b2RelativeAngle(joint.bodyB.rotation, joint.bodyA.rotation);
        if (joint.type == b2_prismaticJoint) {
            return b2UnwindAngle(relativeAngle - joint.referenceAngle);
        }
        if (joint.type == b2_revoluteJoint) {
            if (joint.revoluteEnableLimit) {
                float angle = b2UnwindAngle(relativeAngle - joint.referenceAngle);
                if (angle < joint.revoluteLowerAngle) {
                    return joint.revoluteLowerAngle - angle;
                }
                if (joint.revoluteUpperAngle < angle) {
                    return angle - joint.revoluteUpperAngle;
                }
            }
            return 0.0f;
        }
        if (joint.type == b2_weldJoint) {
            return joint.weldAngularHertz == 0.0f ? b2UnwindAngle(relativeAngle - joint.referenceAngle) : 0.0f;
        }
        return 0.0f;
    }

    public static void b2Joint_GetConstraintTuning(b2JointId jointId, float[] hertz, float[] dampingRatio) {
        JointSlot joint = requireJoint(jointId);
        hertz[0] = joint.constraintHertz;
        dampingRatio[0] = joint.constraintDampingRatio;
    }

    public static void b2Joint_SetConstraintTuning(b2JointId jointId, float hertz, float dampingRatio) {
        JointSlot joint = requireJoint(jointId);
        b2Assert(b2IsValidFloat(hertz) && hertz >= 0.0f, "hertz is valid and non-negative");
        b2Assert(b2IsValidFloat(dampingRatio) && dampingRatio >= 0.0f, "damping ratio is valid and non-negative");
        joint.constraintHertz = hertz;
        joint.constraintDampingRatio = dampingRatio;
    }

    public static b2Vec2 b2Body_GetPosition(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        return body.position.copy();
    }

    public static b2Rot b2Body_GetRotation(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        return body.rotation.copy();
    }

    public static b2Transform b2Body_GetTransform(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        return new b2Transform(body.position, body.rotation);
    }

    public static b2Vec2 b2Body_GetLocalPoint(b2BodyId bodyId, b2Vec2 worldPoint) {
        return b2InvTransformPoint(b2Body_GetTransform(bodyId), worldPoint);
    }

    public static b2Vec2 b2Body_GetWorldPoint(b2BodyId bodyId, b2Vec2 localPoint) {
        return b2TransformPoint(b2Body_GetTransform(bodyId), localPoint);
    }

    public static b2Vec2 b2Body_GetLocalVector(b2BodyId bodyId, b2Vec2 worldVector) {
        return b2InvRotateVector(b2Body_GetRotation(bodyId), worldVector);
    }

    public static b2Vec2 b2Body_GetWorldVector(b2BodyId bodyId, b2Vec2 localVector) {
        return b2RotateVector(b2Body_GetRotation(bodyId), localVector);
    }

    public static b2Vec2 b2Body_GetLinearVelocity(b2BodyId bodyId) {
        return requireBody(bodyId).linearVelocity.copy();
    }

    public static float b2Body_GetAngularVelocity(b2BodyId bodyId) {
        return requireBody(bodyId).angularVelocity;
    }

    public static void b2Body_SetLinearVelocity(b2BodyId bodyId, b2Vec2 linearVelocity) {
        BodySlot body = requireBody(bodyId);
        if (body.type == b2_staticBody) {
            return;
        }
        if (b2LengthSquared(linearVelocity) > 0.0f) {
            b2WakeBody(body);
        }
        body.linearVelocity = linearVelocity.copy();
    }

    public static void b2Body_SetAngularVelocity(b2BodyId bodyId, float angularVelocity) {
        BodySlot body = requireBody(bodyId);
        if (body.type == b2_staticBody || body.fixedRotation) {
            return;
        }
        if (angularVelocity != 0.0f) {
            b2WakeBody(body);
        }
        body.angularVelocity = angularVelocity;
    }

    public static void b2Body_SetTargetTransform(b2BodyId bodyId, b2Transform target, float timeStep) {
        BodySlot body = requireBody(bodyId);
        if (body.type == b2_staticBody || timeStep <= 0.0f) {
            return;
        }
        float invTimeStep = 1.0f / timeStep;
        b2Vec2 center2 = b2TransformPoint(target, body.localCenter);
        b2Vec2 linearVelocity = b2MulSV(invTimeStep, b2Sub(center2, body.center));
        float angularVelocity = 0.0f;
        if (!body.fixedRotation) {
            angularVelocity = invTimeStep * b2RelativeAngle(target.q, body.rotation);
        }
        float maxVelocity = b2Length(linearVelocity) + b2AbsFloat(angularVelocity) * body.maxExtent;
        if (maxVelocity < body.sleepThreshold) {
            return;
        }
        b2WakeBody(body);
        body.linearVelocity = linearVelocity;
        body.angularVelocity = angularVelocity;
    }

    public static b2Vec2 b2Body_GetLocalPointVelocity(b2BodyId bodyId, b2Vec2 localPoint) {
        BodySlot body = requireBody(bodyId);
        if (body.type == b2_staticBody) {
            return b2Vec2_zero.copy();
        }
        b2Vec2 r = b2RotateVector(body.rotation, b2Sub(localPoint, body.localCenter));
        return b2Add(body.linearVelocity, b2CrossSV(body.angularVelocity, r));
    }

    public static b2Vec2 b2Body_GetWorldPointVelocity(b2BodyId bodyId, b2Vec2 worldPoint) {
        BodySlot body = requireBody(bodyId);
        if (body.type == b2_staticBody) {
            return b2Vec2_zero.copy();
        }
        b2Vec2 r = b2Sub(worldPoint, body.center);
        return b2Add(body.linearVelocity, b2CrossSV(body.angularVelocity, r));
    }

    public static boolean b2Body_IsAwake(b2BodyId bodyId) {
        return requireBody(bodyId).awake;
    }

    public static void b2Body_SetAwake(b2BodyId bodyId, boolean awake) {
        BodySlot body = requireBody(bodyId);
        WorldSlot world = WORLDS[body.worldIndex];
        if (awake) {
            b2WakeBody(body);
        } else if (body.awake && body.type != b2_staticBody) {
            b2ForceSleepIsland(world, body);
        }
    }

    public static void b2Body_EnableSleep(b2BodyId bodyId, boolean enableSleep) {
        BodySlot body = requireBody(bodyId);
        body.enableSleep = enableSleep;
        if (!enableSleep) {
            b2WakeBody(body);
        }
    }

    public static boolean b2Body_IsSleepEnabled(b2BodyId bodyId) {
        return requireBody(bodyId).enableSleep;
    }

    public static void b2Body_SetSleepThreshold(b2BodyId bodyId, float sleepThreshold) {
        requireBody(bodyId).sleepThreshold = sleepThreshold;
    }

    public static float b2Body_GetSleepThreshold(b2BodyId bodyId) {
        return requireBody(bodyId).sleepThreshold;
    }

    public static boolean b2Body_IsEnabled(b2BodyId bodyId) {
        return requireBody(bodyId).enabled;
    }

    public static void b2Body_Disable(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        if (!body.enabled) {
            return;
        }
        WorldSlot world = WORLDS[body.worldIndex];
        body.enabled = false;
        if (body.type == b2_dynamicBody) {
            b2RemoveSwap(world.solverBodyOrder, body);
        }
        for (ShapeSlot shape : body.shapes) {
            b2DestroyShapeProxy(shape, world.broadPhase);
            b2DestroyContactsForShape(world, shape);
        }
        b2RebuildContactPairSet(world);
    }

    public static void b2Body_Enable(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        if (body.enabled) {
            return;
        }
        WorldSlot world = WORLDS[body.worldIndex];
        body.enabled = true;
        body.awake = body.type != b2_staticBody;
        if (body.type == b2_dynamicBody) {
            world.solverBodyOrder.add(body);
        }
        for (ShapeSlot shape : body.shapes) {
            if (shape.proxyKey == B2_NULL_INDEX) {
                boolean forcePairCreation = shape.def.invokeContactCreation || shape.def.isSensor;
                b2CreateShapeProxy(shape, world.broadPhase, body.type, forcePairCreation);
            }
        }
    }

    public static void b2Body_SetFixedRotation(b2BodyId bodyId, boolean flag) {
        BodySlot body = requireBody(bodyId);
        if (body.fixedRotation != flag) {
            body.fixedRotation = flag;
            body.angularVelocity = 0.0f;
            b2UpdateBodyMassData(body);
        }
    }

    public static boolean b2Body_IsFixedRotation(b2BodyId bodyId) {
        return requireBody(bodyId).fixedRotation;
    }

    public static void b2Body_SetBullet(b2BodyId bodyId, boolean flag) {
        requireBody(bodyId).bullet = flag;
    }

    public static boolean b2Body_IsBullet(b2BodyId bodyId) {
        return requireBody(bodyId).bullet;
    }

    public static float b2Body_GetMass(b2BodyId bodyId) {
        return requireBody(bodyId).mass;
    }

    public static float b2Body_GetRotationalInertia(b2BodyId bodyId) {
        return requireBody(bodyId).inertia;
    }

    public static b2Vec2 b2Body_GetLocalCenterOfMass(b2BodyId bodyId) {
        return requireBody(bodyId).localCenter.copy();
    }

    public static b2Vec2 b2Body_GetWorldCenterOfMass(b2BodyId bodyId) {
        return requireBody(bodyId).center.copy();
    }

    public static void b2Body_SetMassData(b2BodyId bodyId, b2MassData massData) {
        BodySlot body = requireBody(bodyId);
        body.mass = massData.mass;
        body.inertia = massData.rotationalInertia;
        body.localCenter = massData.center.copy();
        body.center = b2TransformPoint(new b2Transform(body.position, body.rotation), body.localCenter);
        body.invMass = body.mass > 0.0f ? 1.0f / body.mass : 0.0f;
        body.invInertia = body.inertia > 0.0f ? 1.0f / body.inertia : 0.0f;
    }

    public static b2MassData b2Body_GetMassData(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        b2MassData massData = new b2MassData();
        massData.mass = body.mass;
        massData.center = body.localCenter.copy();
        massData.rotationalInertia = body.inertia;
        return massData;
    }

    public static void b2Body_ApplyMassFromShapes(b2BodyId bodyId) {
        b2UpdateBodyMassData(requireBody(bodyId));
    }

    public static void b2Body_ApplyForce(b2BodyId bodyId, b2Vec2 force, b2Vec2 point, boolean wake) {
        BodySlot body = requireBody(bodyId);
        if (wake) {
            b2WakeBody(body);
        }
        if (body.type == b2_dynamicBody && body.enabled && body.awake) {
            body.force = b2Add(body.force, force);
            body.torque += b2Cross(b2Sub(point, body.center), force);
        }
    }

    public static void b2Body_ApplyForceToCenter(b2BodyId bodyId, b2Vec2 force, boolean wake) {
        BodySlot body = requireBody(bodyId);
        if (wake) {
            b2WakeBody(body);
        }
        if (body.type == b2_dynamicBody && body.enabled && body.awake) {
            body.force = b2Add(body.force, force);
        }
    }

    public static void b2Body_ApplyTorque(b2BodyId bodyId, float torque, boolean wake) {
        BodySlot body = requireBody(bodyId);
        if (wake) {
            b2WakeBody(body);
        }
        if (body.type == b2_dynamicBody && body.enabled && body.awake) {
            body.torque += torque;
        }
    }

    public static void b2Body_ApplyLinearImpulse(b2BodyId bodyId, b2Vec2 impulse, b2Vec2 point, boolean wake) {
        BodySlot body = requireBody(bodyId);
        if (wake) {
            b2WakeBody(body);
        }
        if (body.type == b2_dynamicBody && body.enabled && body.awake) {
            body.linearVelocity = b2MulAdd(body.linearVelocity, body.invMass, impulse);
            body.angularVelocity += body.invInertia * b2Cross(b2Sub(point, body.center), impulse);
            b2LimitBodyLinearVelocity(body);
        }
    }

    public static void b2Body_ApplyLinearImpulseToCenter(b2BodyId bodyId, b2Vec2 impulse, boolean wake) {
        BodySlot body = requireBody(bodyId);
        if (wake) {
            b2WakeBody(body);
        }
        if (body.type == b2_dynamicBody && body.enabled && body.awake) {
            body.linearVelocity = b2MulAdd(body.linearVelocity, body.invMass, impulse);
            b2LimitBodyLinearVelocity(body);
        }
    }

    public static void b2Body_ApplyAngularImpulse(b2BodyId bodyId, float impulse, boolean wake) {
        BodySlot body = requireBody(bodyId);
        if (wake) {
            b2WakeBody(body);
        }
        if (body.type == b2_dynamicBody && body.enabled && body.awake) {
            body.angularVelocity += body.invInertia * impulse;
        }
    }

    public static void b2Body_SetLinearDamping(b2BodyId bodyId, float linearDamping) {
        requireBody(bodyId).linearDamping = linearDamping;
    }

    public static float b2Body_GetLinearDamping(b2BodyId bodyId) {
        return requireBody(bodyId).linearDamping;
    }

    public static void b2Body_SetAngularDamping(b2BodyId bodyId, float angularDamping) {
        requireBody(bodyId).angularDamping = angularDamping;
    }

    public static float b2Body_GetAngularDamping(b2BodyId bodyId) {
        return requireBody(bodyId).angularDamping;
    }

    public static void b2Body_SetGravityScale(b2BodyId bodyId, float gravityScale) {
        requireBody(bodyId).gravityScale = gravityScale;
    }

    public static float b2Body_GetGravityScale(b2BodyId bodyId) {
        return requireBody(bodyId).gravityScale;
    }

    public static void b2Body_EnableContactEvents(b2BodyId bodyId, boolean flag) {
        BodySlot body = requireBody(bodyId);
        for (ShapeSlot shape : body.shapes) {
            shape.def.enableContactEvents = flag;
        }
    }

    public static void b2Body_EnableHitEvents(b2BodyId bodyId, boolean flag) {
        BodySlot body = requireBody(bodyId);
        for (ShapeSlot shape : body.shapes) {
            shape.def.enableHitEvents = flag;
        }
    }

    public static b2WorldId b2Body_GetWorld(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        WorldSlot world = WORLDS[body.worldIndex];
        return new b2WorldId(body.worldIndex + 1, world.generation);
    }

    public static int b2Body_GetShapeCount(b2BodyId bodyId) {
        return requireBody(bodyId).shapes.size();
    }

    public static int b2Body_GetShapes(b2BodyId bodyId, b2ShapeId[] shapeArray, int capacity) {
        BodySlot body = requireBody(bodyId);
        WorldSlot world = WORLDS[body.worldIndex];
        int count = 0;
        int limit = b2MinInt(capacity, shapeArray.length);
        for (int i = body.shapes.size() - 1; i >= 0 && count < limit; --i) {
            shapeArray[count++] = shapeId(world, body.shapes.get(i));
        }
        return count;
    }

    public static int b2Body_GetJointCount(b2BodyId bodyId) {
        return requireBody(bodyId).joints.size();
    }

    public static int b2Body_GetJoints(b2BodyId bodyId, b2JointId[] jointArray, int capacity) {
        BodySlot body = requireBody(bodyId);
        WorldSlot world = WORLDS[body.worldIndex];
        int count = 0;
        int limit = b2MinInt(capacity, jointArray.length);
        for (JointSlot joint : body.joints) {
            if (count >= limit) {
                break;
            }
            jointArray[count++] = jointId(world, joint);
        }
        return count;
    }

    public static b2AABB b2Body_ComputeAABB(b2BodyId bodyId) {
        BodySlot body = requireBody(bodyId);
        if (body.shapes.isEmpty()) {
            return new b2AABB(body.position, body.position);
        }
        b2AABB aabb = body.shapes.get(0).aabb.copy();
        for (int i = 1; i < body.shapes.size(); ++i) {
            aabb = b2AABB_Union(aabb, body.shapes.get(i).aabb);
        }
        return aabb;
    }

    public static b2Counters b2World_GetCounters(b2WorldId worldId) {
        WorldSlot world = requireWorld(worldId);
        b2Counters counters = new b2Counters();
        java.util.HashSet<Integer> islandRoots = new java.util.HashSet<>();
        for (BodySlot body : world.bodies) {
            if (body != null && body.alive) {
                counters.bodyCount += 1;
                if (body.enabled && body.type != b2_staticBody) {
                    islandRoots.add(b2GetBodySleepIsland(world, body));
                }
            }
        }
        for (ShapeSlot shape : world.shapes) {
            if (shape != null && shape.alive) {
                counters.shapeCount += 1;
            }
        }
        for (JointSlot joint : world.joints) {
            if (joint != null && joint.alive) {
                counters.jointCount += 1;
            }
        }
        counters.contactCount = world.contactCount;
        counters.islandCount = islandRoots.size();
        counters.byteCount = b2GetByteCount();
        for (int i = 0; i < B2_GRAPH_COLOR_COUNT; ++i) {
            counters.colorCounts[i] = world.contactGraphColors[i].size() + world.jointGraphColors[i].size();
        }
        return counters;
    }

    public static void b2World_Step(b2WorldId worldId, float timeStep, int subStepCount) {
        WorldSlot world = requireWorld(worldId);
        B2DebugHooks.beforeWorldStep(worldId, timeStep, subStepCount);
        world.stepCount += 1;
        world.contactEvents = new b2ContactEvents();
        b2UpdateBroadPhasePairs(world);
        Object treeTask = world.enqueueTask.invoke(
            (startIndex, endIndex, workerIndex, taskContext) -> b2BroadPhase_RebuildTrees(world.broadPhase),
            1, 1, null, world.userTaskContext);
        try {
            b2UpdateContacts(world);
        } finally {
            if (treeTask != null) {
                world.finishTask.invoke(treeTask, world.userTaskContext);
            }
        }
        b2SplitSleepIsland(world);
        int actualSubStepCount = b2MaxInt(1, subStepCount);
        if (timeStep == 0.0f) {
            world.invH = 0.0f;
            updateSensorEvents(world);
            B2DebugHooks.stepComplete(worldId);
            return;
        }
        world.invH = actualSubStepCount / timeStep;
        if (b2SolveWorldContacts(world, timeStep, actualSubStepCount)) {
            b2IntegrateKinematicBodies(world, timeStep, actualSubStepCount);
            updateSensorEvents(world);
            B2DebugHooks.stepComplete(worldId);
            return;
        }
        float h = timeStep / actualSubStepCount;
        float maxLinearSpeed = world.def.maximumLinearSpeed;
        float maxLinearSpeedSquared = maxLinearSpeed * maxLinearSpeed;
        float maxAngularSpeed = B2_MAX_ROTATION() / timeStep;
        float maxAngularSpeedSquared = maxAngularSpeed * maxAngularSpeed;
        java.util.ArrayList<b2BodyMoveEvent> moves = new java.util.ArrayList<>();
        for (BodySlot body : world.bodies) {
            if (body == null || !body.alive || !body.enabled || body.type == b2_staticBody
                || (body.type == b2_dynamicBody && !body.awake)) {
                continue;
            }
            b2Vec2 deltaPosition = b2Vec2_zero.copy();
            b2Rot deltaRotation = b2Rot_identity.copy();
            for (int i = 0; i < actualSubStepCount; ++i) {
                float linearDamping = 1.0f / (1.0f + h * body.linearDamping);
                float angularDamping = 1.0f / (1.0f + h * body.angularDamping);

                float gravityScale = body.invMass > 0.0f ? body.gravityScale : 0.0f;
                b2Vec2 linearVelocityDelta = b2Add(b2MulSV(h * body.invMass, body.force),
                    b2MulSV(h * gravityScale, world.def.gravity));
                float angularVelocityDelta = h * body.invInertia * body.torque;
                body.linearVelocity = b2MulAdd(linearVelocityDelta, linearDamping, body.linearVelocity);
                body.angularVelocity = angularVelocityDelta + angularDamping * body.angularVelocity;

                if (b2Dot(body.linearVelocity, body.linearVelocity) > maxLinearSpeedSquared) {
                    float ratio = maxLinearSpeed / b2Length(body.linearVelocity);
                    body.linearVelocity = b2MulSV(ratio, body.linearVelocity);
                }

                if (body.angularVelocity * body.angularVelocity > maxAngularSpeedSquared && !body.allowFastRotation) {
                    float ratio = maxAngularSpeed / b2AbsFloat(body.angularVelocity);
                    body.angularVelocity *= ratio;
                }

                deltaRotation = b2IntegrateRotation(deltaRotation, h * body.angularVelocity);
                deltaPosition = b2MulAdd(deltaPosition, h, body.linearVelocity);
            }
            body.center = b2Add(body.center, deltaPosition);
            body.rotation = b2NormalizeRot(b2MulRot(deltaRotation, body.rotation));
            body.position = b2Sub(body.center, b2RotateVector(body.rotation, body.localCenter));
            b2SynchronizeBodyProxies(world, body);
            b2BodyMoveEvent event = new b2BodyMoveEvent();
            event.bodyId = bodyId(world, body);
            event.transform = new b2Transform(body.position, body.rotation);
            event.userData = body.userData;
            moves.add(event);
            body.force = b2Vec2_zero.copy();
            body.torque = 0.0f;
        }
        world.bodyEvents = new b2BodyEvents();
        world.bodyEvents.moveEvents = moves.toArray(new b2BodyMoveEvent[0]);
        world.bodyEvents.moveCount = world.bodyEvents.moveEvents.length;
        updateSensorEvents(world);
        B2DebugHooks.stepComplete(worldId);
    }

    private static void b2IntegrateKinematicBodies(WorldSlot world, float timeStep, int subStepCount) {
        float h = timeStep / b2MaxInt(1, subStepCount);
        java.util.ArrayList<b2BodyMoveEvent> moves = new java.util.ArrayList<>();
        if (world.bodyEvents.moveEvents != null) {
            java.util.Collections.addAll(moves, world.bodyEvents.moveEvents);
        }
        for (BodySlot body : world.bodies) {
            if (body == null || !body.alive || !body.enabled || body.type != b2_kinematicBody) {
                continue;
            }
            b2Vec2 deltaPosition = b2Vec2_zero.copy();
            b2Rot deltaRotation = b2Rot_identity.copy();
            for (int i = 0; i < subStepCount; ++i) {
                deltaRotation = b2IntegrateRotation(deltaRotation, h * body.angularVelocity);
                deltaPosition = b2MulAdd(deltaPosition, h, body.linearVelocity);
            }
            body.center = b2Add(body.center, deltaPosition);
            body.rotation = b2NormalizeRot(b2MulRot(deltaRotation, body.rotation));
            body.position = b2Sub(body.center, b2RotateVector(body.rotation, body.localCenter));
            b2SynchronizeBodyProxies(world, body);
            b2BodyMoveEvent event = new b2BodyMoveEvent();
            event.bodyId = bodyId(world, body);
            event.transform = new b2Transform(body.position, body.rotation);
            event.userData = body.userData;
            moves.add(event);
        }
        world.bodyEvents.moveEvents = moves.toArray(new b2BodyMoveEvent[0]);
        world.bodyEvents.moveCount = world.bodyEvents.moveEvents.length;
    }

    public static b2BodyEvents b2World_GetBodyEvents(b2WorldId worldId) {
        return requireWorld(worldId).bodyEvents;
    }

    public static b2SensorEvents b2World_GetSensorEvents(b2WorldId worldId) {
        return requireWorld(worldId).sensorEvents;
    }

    public static b2ContactEvents b2World_GetContactEvents(b2WorldId worldId) {
        return requireWorld(worldId).contactEvents;
    }

    public static void b2World_Draw(b2WorldId worldId, b2DebugDraw draw) {
        WorldSlot world = requireWorld(worldId);
        if (draw.useDrawingBounds) {
            for (ShapeSlot shape : world.shapes) {
                if (shape == null || !shape.alive || shape.proxyKey == B2_NULL_INDEX || !b2AABB_Overlaps(shape.fatAABB, draw.drawingBounds)) {
                    continue;
                }
                if (draw.drawShapes) {
                    b2DrawShape(draw, shape, new b2Transform(shape.body.position, shape.body.rotation), b2DebugDrawColor(shape));
                }
                if (draw.drawBounds) {
                    b2DrawAABB(draw, shape.fatAABB, b2_colorGold);
                }
            }
            return;
        }

        if (draw.drawShapes) {
            for (BodySlot body : world.bodies) {
                if (body == null || !body.alive || !body.enabled) {
                    continue;
                }
                b2Transform transform = new b2Transform(body.position, body.rotation);
                for (int i = body.shapes.size() - 1; i >= 0; --i) {
                    ShapeSlot shape = body.shapes.get(i);
                    if (shape.alive) {
                        b2DrawShape(draw, shape, transform, b2DebugDrawColor(shape));
                    }
                }
            }
        }

        if (draw.drawJoints) {
            for (JointSlot joint : world.joints) {
                if (joint != null && joint.alive) {
                    b2DrawJoint(draw, joint);
                }
            }
        }

        if (draw.drawBounds) {
            for (BodySlot body : world.bodies) {
                if (body == null || !body.alive || !body.enabled) {
                    continue;
                }
                draw.DrawStringFcn.invoke(body.center.copy(), Integer.toString(body.index), b2_colorWhite);
                for (int i = body.shapes.size() - 1; i >= 0; --i) {
                    ShapeSlot shape = body.shapes.get(i);
                    if (shape.alive) {
                        b2DrawAABB(draw, shape.fatAABB, b2_colorGold);
                    }
                }
            }
        }

        if (draw.drawBodyNames) {
            b2Vec2 offset = new b2Vec2(0.05f, 0.05f);
            for (BodySlot body : world.bodies) {
                if (body != null && body.alive && body.enabled && body.name != null && !body.name.isEmpty()) {
                    b2Transform transform = new b2Transform(body.center, body.rotation);
                    draw.DrawStringFcn.invoke(b2TransformPoint(transform, offset), body.name, b2_colorBlueViolet);
                }
            }
        }

        if (draw.drawMass) {
            b2Vec2 offset = new b2Vec2(0.1f, 0.1f);
            for (BodySlot body : world.bodies) {
                if (body != null && body.alive && body.enabled) {
                    b2Transform transform = new b2Transform(body.center, body.rotation);
                    draw.DrawTransformFcn.invoke(transform);
                    draw.DrawStringFcn.invoke(b2TransformPoint(transform, offset), String.format(java.util.Locale.ROOT, "  %.2f", body.mass),
                        b2_colorWhite);
                }
            }
        }

        if (draw.drawContacts) {
            b2DrawContacts(draw, world);
        }
    }

    public static b2TreeStats b2World_OverlapAABB(b2WorldId worldId, b2AABB aabb, b2QueryFilter filter, b2OverlapResultFcn callback) {
        WorldSlot world = requireWorld(worldId);
        b2TreeStats stats = new b2TreeStats();
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            b2TreeStats treeStats = b2DynamicTree_Query(world.broadPhase.trees[i], aabb, filter.maskBits, (proxyId, userData) -> {
                ShapeSlot shape = getWorldQueryShape(world, userData);
                if (shape == null || !b2ShouldQueryCollide(shape.def.filter, filter)) {
                    return true;
                }
                return callback.invoke(shapeId(world, shape));
            });
            stats.nodeVisits += treeStats.nodeVisits;
            stats.leafVisits += treeStats.leafVisits;
        }
        return stats;
    }

    public static b2TreeStats b2World_OverlapShape(b2WorldId worldId, b2ShapeProxy proxy, b2QueryFilter filter, b2OverlapResultFcn callback) {
        WorldSlot world = requireWorld(worldId);
        b2TreeStats stats = new b2TreeStats();
        b2AABB aabb = b2MakeAABB(proxy.points, proxy.count, proxy.radius);
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            b2TreeStats treeStats = b2DynamicTree_Query(world.broadPhase.trees[i], aabb, filter.maskBits, (proxyId, userData) -> {
                ShapeSlot shape = getWorldQueryShape(world, userData);
                if (shape == null || !b2ShouldQueryCollide(shape.def.filter, filter)) {
                    return true;
                }

                b2DistanceInput input = new b2DistanceInput();
                input.proxyA = proxy;
                input.proxyB = b2MakeShapeDistanceProxy(shape);
                input.transformA = b2Transform_identity.copy();
                input.transformB = new b2Transform(shape.body.position, shape.body.rotation);
                input.useRadii = true;

                b2SimplexCache cache = new b2SimplexCache();
                b2DistanceOutput output = b2ShapeDistance(input, cache, null, 0);
                if (output.distance > 0.1f * B2_LINEAR_SLOP()) {
                    return true;
                }
                return callback.invoke(shapeId(world, shape));
            });
            stats.nodeVisits += treeStats.nodeVisits;
            stats.leafVisits += treeStats.leafVisits;
        }
        return stats;
    }

    public static b2TreeStats b2World_CastRay(b2WorldId worldId, b2Vec2 origin, b2Vec2 translation, b2QueryFilter filter,
                                              b2CastResultFcn callback) {
        WorldSlot world = requireWorld(worldId);
        b2TreeStats stats = new b2TreeStats();
        b2RayCastInput input = new b2RayCastInput(origin, translation, 1.0f);
        float[] fraction = {1.0f};
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            b2TreeStats treeStats = b2DynamicTree_RayCast(world.broadPhase.trees[i], input, filter.maskBits, (subInput, proxyId, userData) -> {
                ShapeSlot shape = getWorldQueryShape(world, userData);
                if (shape == null || !b2ShouldQueryCollide(shape.def.filter, filter)) {
                    return subInput.maxFraction;
                }
                b2CastOutput output = b2RayCastShape(subInput, shape);
                if (!output.hit) {
                    return subInput.maxFraction;
                }
                float value = callback.invoke(shapeId(world, shape), output.point, output.normal, output.fraction);
                if (0.0f <= value && value <= 1.0f) {
                    fraction[0] = value;
                }
                return value;
            });
            stats.nodeVisits += treeStats.nodeVisits;
            stats.leafVisits += treeStats.leafVisits;
            if (fraction[0] == 0.0f) {
                return stats;
            }
            input.maxFraction = fraction[0];
        }
        return stats;
    }

    public static b2RayResult b2World_CastRayClosest(b2WorldId worldId, b2Vec2 origin, b2Vec2 translation, b2QueryFilter filter) {
        b2RayResult result = new b2RayResult();
        b2TreeStats stats = b2World_CastRay(worldId, origin, translation, filter, (shapeId, point, normal, fraction) -> {
            if (fraction == 0.0f) {
                return -1.0f;
            }
            result.shapeId = new b2ShapeId(shapeId.index1, shapeId.world0, shapeId.generation);
            result.point = point.copy();
            result.normal = normal.copy();
            result.fraction = fraction;
            result.hit = true;
            return fraction;
        });
        result.nodeVisits = stats.nodeVisits;
        result.leafVisits = stats.leafVisits;
        return result;
    }

    public static b2TreeStats b2World_CastShape(b2WorldId worldId, b2ShapeProxy proxy, b2Vec2 translation, b2QueryFilter filter,
                                                b2CastResultFcn callback) {
        WorldSlot world = requireWorld(worldId);
        b2TreeStats stats = new b2TreeStats();
        b2ShapeCastInput input = new b2ShapeCastInput();
        input.proxy = proxy;
        input.translation = translation.copy();
        input.maxFraction = 1.0f;
        float[] fraction = {1.0f};
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            b2TreeStats treeStats = b2DynamicTree_ShapeCast(world.broadPhase.trees[i], input, filter.maskBits, (subInput, proxyId, userData) -> {
                ShapeSlot shape = getWorldQueryShape(world, userData);
                if (shape == null || !b2ShouldQueryCollide(shape.def.filter, filter)) {
                    return subInput.maxFraction;
                }
                b2CastOutput output = b2ShapeCastShape(subInput, shape);
                if (!output.hit) {
                    return subInput.maxFraction;
                }
                float value = callback.invoke(shapeId(world, shape), output.point, output.normal, output.fraction);
                if (0.0f <= value && value <= 1.0f) {
                    fraction[0] = value;
                }
                return value;
            });
            stats.nodeVisits += treeStats.nodeVisits;
            stats.leafVisits += treeStats.leafVisits;
            if (fraction[0] == 0.0f) {
                return stats;
            }
            input.maxFraction = fraction[0];
        }
        return stats;
    }

    public static float b2World_CastMover(b2WorldId worldId, b2Capsule mover, b2Vec2 translation, b2QueryFilter filter) {
        WorldSlot world = requireWorld(worldId);
        b2Assert(b2IsValidVec2(translation), "translation is valid");
        b2Assert(mover.radius > 2.0f * B2_LINEAR_SLOP(), "mover radius is greater than twice the linear slop");

        b2ShapeCastInput input = new b2ShapeCastInput();
        input.proxy = b2MakeProxy(new b2Vec2[] {mover.center1, mover.center2}, 2, mover.radius);
        input.translation = translation.copy();
        input.maxFraction = 1.0f;
        input.canEncroach = true;
        float[] fraction = {1.0f};
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            b2DynamicTree_ShapeCast(world.broadPhase.trees[i], input, filter.maskBits, (subInput, proxyId, userData) -> {
                ShapeSlot shape = getWorldQueryShape(world, userData);
                if (shape == null || !b2ShouldQueryCollide(shape.def.filter, filter)) {
                    return fraction[0];
                }
                b2CastOutput output = b2ShapeCastShape(subInput, shape);
                if (output.fraction == 0.0f) {
                    return fraction[0];
                }
                fraction[0] = output.fraction;
                return output.fraction;
            });
            if (fraction[0] == 0.0f) {
                return 0.0f;
            }
            input.maxFraction = fraction[0];
        }
        return fraction[0];
    }

    public static void b2World_CollideMover(b2WorldId worldId, b2Capsule mover, b2QueryFilter filter, b2PlaneResultFcn callback) {
        WorldSlot world = requireWorld(worldId);
        b2Vec2 r = new b2Vec2(mover.radius, mover.radius);
        b2AABB aabb = new b2AABB(b2Sub(b2Min(mover.center1, mover.center2), r), b2Add(b2Max(mover.center1, mover.center2), r));
        for (int i = 0; i < b2_bodyTypeCount(); ++i) {
            final boolean[] keepGoing = {true};
            b2DynamicTree_Query(world.broadPhase.trees[i], aabb, filter.maskBits, (proxyId, userData) -> {
                if (!keepGoing[0]) {
                    return false;
                }
                ShapeSlot shape = getWorldQueryShape(world, userData);
                if (shape == null || !b2ShouldQueryCollide(shape.def.filter, filter)) {
                    return true;
                }
                b2PlaneResult result = b2CollideMover(shape, new b2Transform(shape.body.position, shape.body.rotation), mover);
                if (result.hit && b2IsNormalized(result.plane.normal)) {
                    keepGoing[0] = callback.invoke(shapeId(world, shape), result);
                    return keepGoing[0];
                }
                return true;
            });
            if (!keepGoing[0]) {
                return;
            }
        }
    }

    public static void b2World_EnableSleeping(b2WorldId worldId, boolean flag) {
        requireWorld(worldId).sleepEnabled = flag;
    }

    public static boolean b2World_IsSleepingEnabled(b2WorldId worldId) {
        return requireWorld(worldId).sleepEnabled;
    }

    public static void b2World_EnableContinuous(b2WorldId worldId, boolean flag) {
        requireWorld(worldId).continuousEnabled = flag;
    }

    public static boolean b2World_IsContinuousEnabled(b2WorldId worldId) {
        return requireWorld(worldId).continuousEnabled;
    }

    public static void b2World_EnableSpeculative(b2WorldId worldId, boolean flag) {
        requireWorld(worldId).speculativeEnabled = flag;
    }

    public static void b2World_SetRestitutionThreshold(b2WorldId worldId, float value) {
        requireWorld(worldId).def.restitutionThreshold = b2ClampFloat(value, 0.0f, Float.MAX_VALUE);
    }

    public static float b2World_GetRestitutionThreshold(b2WorldId worldId) {
        return requireWorld(worldId).def.restitutionThreshold;
    }

    public static void b2World_SetHitEventThreshold(b2WorldId worldId, float value) {
        requireWorld(worldId).def.hitEventThreshold = b2ClampFloat(value, 0.0f, Float.MAX_VALUE);
    }

    public static float b2World_GetHitEventThreshold(b2WorldId worldId) {
        return requireWorld(worldId).def.hitEventThreshold;
    }

    public static void b2World_SetGravity(b2WorldId worldId, b2Vec2 gravity) {
        requireWorld(worldId).def.gravity = gravity.copy();
    }

    public static b2Vec2 b2World_GetGravity(b2WorldId worldId) {
        return requireWorld(worldId).def.gravity.copy();
    }

    public static void b2World_Explode(b2WorldId worldId, b2ExplosionDef explosionDef) {
        WorldSlot world = requireWorld(worldId);
        b2Vec2 position = explosionDef.position.copy();
        float radius = explosionDef.radius;
        float falloff = explosionDef.falloff;
        float impulsePerLength = explosionDef.impulsePerLength;
        b2Assert(b2IsValidVec2(position), "explosion position is valid");
        b2Assert(b2IsValidFloat(radius) && radius >= 0.0f, "explosion radius is non-negative");
        b2Assert(b2IsValidFloat(falloff) && falloff >= 0.0f, "explosion falloff is non-negative");
        b2Assert(b2IsValidFloat(impulsePerLength), "explosion impulse is valid");

        float range = radius + falloff;
        b2AABB aabb = new b2AABB(new b2Vec2(position.x - range, position.y - range),
            new b2Vec2(position.x + range, position.y + range));
        b2ShapeProxy pointProxy = b2MakeProxy(new b2Vec2[] {position}, 1, 0.0f);
        b2DynamicTree_Query(world.broadPhase.trees[b2_dynamicBody], aabb, explosionDef.maskBits, (proxyId, userData) -> {
            ShapeSlot shape = getWorldQueryShape(world, userData);
            if (shape == null || shape.body.type != b2_dynamicBody) {
                return true;
            }

            BodySlot body = shape.body;
            b2Transform transform = new b2Transform(body.position, body.rotation);
            b2DistanceInput input = new b2DistanceInput();
            input.proxyA = b2MakeShapeDistanceProxy(shape);
            input.proxyB = pointProxy;
            input.transformA = transform;
            input.transformB = b2Transform_identity.copy();
            input.useRadii = true;
            b2SimplexCache cache = new b2SimplexCache();
            b2DistanceOutput output = b2ShapeDistance(input, cache, null, 0);
            if (output.distance > range) {
                return true;
            }

            b2WakeBody(body);
            if (!body.awake) {
                return true;
            }

            b2Vec2 closestPoint = output.pointA;
            if (output.distance == 0.0f) {
                closestPoint = b2TransformPoint(transform, b2GetShapeCentroid(shape));
            }

            b2Vec2 direction = b2Sub(closestPoint, position);
            float epsilon = Math.ulp(1.0f);
            if (b2LengthSquared(direction) > 100.0f * epsilon * epsilon) {
                direction = b2Normalize(direction);
            } else {
                direction = new b2Vec2(1.0f, 0.0f);
            }

            b2Vec2 localLine = b2InvRotateVector(transform.q, b2LeftPerp(direction));
            float perimeter = b2GetShapeProjectedPerimeter(shape, localLine);
            float scale = 1.0f;
            if (output.distance > radius && falloff > 0.0f) {
                scale = b2ClampFloat((range - output.distance) / falloff, 0.0f, 1.0f);
            }

            b2Vec2 impulse = b2MulSV(impulsePerLength * perimeter * scale, direction);
            body.linearVelocity = b2MulAdd(body.linearVelocity, body.invMass, impulse);
            body.angularVelocity += body.invInertia * b2Cross(b2Sub(closestPoint, body.center), impulse);
            return true;
        });
    }

    public static void b2World_SetContactTuning(b2WorldId worldId, float hertz, float dampingRatio, float pushSpeed) {
        WorldSlot world = requireWorld(worldId);
        world.def.contactHertz = b2ClampFloat(hertz, 0.0f, Float.MAX_VALUE);
        world.def.contactDampingRatio = b2ClampFloat(dampingRatio, 0.0f, Float.MAX_VALUE);
        world.def.maxContactPushSpeed = b2ClampFloat(pushSpeed, 0.0f, Float.MAX_VALUE);
    }

    public static void b2World_SetMaximumLinearSpeed(b2WorldId worldId, float value) {
        requireWorld(worldId).def.maximumLinearSpeed = value;
    }

    public static float b2World_GetMaximumLinearSpeed(b2WorldId worldId) {
        return requireWorld(worldId).def.maximumLinearSpeed;
    }

    public static void b2World_EnableWarmStarting(b2WorldId worldId, boolean flag) {
        requireWorld(worldId).warmStartingEnabled = flag;
    }

    public static boolean b2World_IsWarmStartingEnabled(b2WorldId worldId) {
        return requireWorld(worldId).warmStartingEnabled;
    }

    public static int b2World_GetAwakeBodyCount(b2WorldId worldId) {
        int count = 0;
        for (BodySlot body : requireWorld(worldId).bodies) {
            if (body != null && body.alive && body.enabled && body.type != b2_staticBody && body.awake) {
                count += 1;
            }
        }
        return count;
    }

    public static b2Profile b2World_GetProfile(b2WorldId worldId) {
        return copyProfile(requireWorld(worldId).profile);
    }

    public static void b2World_SetUserData(b2WorldId worldId, Object userData) {
        requireWorld(worldId).userData = userData;
    }

    public static Object b2World_GetUserData(b2WorldId worldId) {
        return requireWorld(worldId).userData;
    }

    public static void b2World_SetFrictionCallback(b2WorldId worldId, b2FrictionCallback callback) {
        requireWorld(worldId).def.frictionCallback = callback;
    }

    public static void b2World_SetRestitutionCallback(b2WorldId worldId, b2RestitutionCallback callback) {
        requireWorld(worldId).def.restitutionCallback = callback;
    }

    public static void b2World_DumpMemoryStats(b2WorldId worldId) {
        b2Counters counters = b2World_GetCounters(worldId);
        try (java.io.PrintWriter writer = new java.io.PrintWriter("box2d_memory.txt")) {
            writer.println("bodyCount = " + counters.bodyCount);
            writer.println("shapeCount = " + counters.shapeCount);
            writer.println("contactCount = " + counters.contactCount);
            writer.println("jointCount = " + counters.jointCount);
            writer.println("byteCount = " + counters.byteCount);
        } catch (java.io.FileNotFoundException ignored) {
        }
    }

    public static void b2World_RebuildStaticTree(b2WorldId worldId) {
        b2DynamicTree_Rebuild(requireWorld(worldId).broadPhase.trees[b2_staticBody], true);
    }

    public static void b2World_SetCustomFilterCallback(b2WorldId worldId, b2CustomFilterFcn callback, Object context) {
        WorldSlot world = requireWorld(worldId);
        world.customFilterFcn = callback;
        world.customFilterContext = context;
    }

    public static void b2World_SetCustomFilterCallback(b2WorldId worldId, Object callback, Object context) {
        b2World_SetCustomFilterCallback(worldId, callback instanceof b2CustomFilterFcn ? (b2CustomFilterFcn) callback : null, context);
    }

    public static void b2World_SetPreSolveCallback(b2WorldId worldId, b2PreSolveFcn callback, Object context) {
        WorldSlot world = requireWorld(worldId);
        world.preSolveFcn = callback;
        world.preSolveContext = context;
    }

    public static void b2World_SetPreSolveCallback(b2WorldId worldId, Object callback, Object context) {
        b2World_SetPreSolveCallback(worldId, callback instanceof b2PreSolveFcn ? (b2PreSolveFcn) callback : null, context);
    }

    public static int b2Body_GetContactCapacity(b2BodyId bodyId) {
        return requireBody(bodyId).contacts.size();
    }

    public static int b2Body_GetContactData(b2BodyId bodyId, b2ContactData[] contactData, int capacity) {
        BodySlot body = requireBody(bodyId);
        WorldSlot world = WORLDS[body.worldIndex];
        int count = 0;
        for (ContactSlot contact : body.contacts) {
            if (count == capacity) {
                break;
            }
            if (contact.alive && contact.touching) {
                contactData[count++] = b2MakeContactData(world, contact);
            }
        }
        return count;
    }

    public static int b2Shape_GetContactCapacity(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        if (shape.def.isSensor) {
            return 0;
        }
        return shape.body.contacts.size();
    }

    public static int b2Shape_GetContactData(b2ShapeId shapeId, b2ContactData[] contactData, int capacity) {
        ShapeSlot shape = requireShape(shapeId);
        WorldSlot world = WORLDS[shape.body.worldIndex];
        int count = 0;
        for (ContactSlot contact : shape.body.contacts) {
            if (count == capacity) {
                break;
            }
            if (contact.alive && contact.touching && (contact.shapeA == shape || contact.shapeB == shape)) {
                contactData[count++] = b2MakeContactData(world, contact);
            }
        }
        return count;
    }

    public static int b2Shape_GetSensorCapacity(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        if (!shape.def.isSensor) {
            return 0;
        }
        SensorOverlapState state = WORLDS[shape.body.worldIndex].sensorOverlaps.get(shape.index);
        return state != null ? state.refs.size() : 0;
    }

    public static int b2Shape_GetSensorOverlaps(b2ShapeId shapeId, b2ShapeId[] overlaps, int capacity) {
        ShapeSlot shape = requireShape(shapeId);
        if (!shape.def.isSensor) {
            return 0;
        }
        WorldSlot world = WORLDS[shape.body.worldIndex];
        SensorOverlapState state = world.sensorOverlaps.get(shape.index);
        if (state == null) {
            return 0;
        }
        int count = b2MinInt(state.refs.size(), capacity);
        for (int i = 0; i < count; ++i) {
            ShapeRef ref = state.refs.get(i);
            overlaps[i] = new b2ShapeId(ref.shapeIndex + 1, world.index, ref.generation);
        }
        return count;
    }

    public static boolean b2Shape_IsValid(b2ShapeId shapeId) {
        return getShape(shapeId) != null;
    }

    public static b2AABB b2Shape_GetAABB(b2ShapeId shapeId) {
        return requireShape(shapeId).aabb.copy();
    }

    public static int b2Shape_GetType(b2ShapeId shapeId) {
        return b2ShapeTypeFromKind(requireShape(shapeId).kind);
    }

    public static b2BodyId b2Shape_GetBody(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        WorldSlot world = WORLDS[shape.body.worldIndex];
        return bodyId(world, shape.body);
    }

    public static b2WorldId b2Shape_GetWorld(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        WorldSlot world = WORLDS[shape.body.worldIndex];
        return new b2WorldId(shape.body.worldIndex + 1, world.generation);
    }

    public static boolean b2Shape_IsSensor(b2ShapeId shapeId) {
        return requireShape(shapeId).def.isSensor;
    }

    public static void b2Shape_SetUserData(b2ShapeId shapeId, Object userData) {
        requireShape(shapeId).def.userData = userData;
    }

    public static Object b2Shape_GetUserData(b2ShapeId shapeId) {
        return requireShape(shapeId).def.userData;
    }

    public static void b2Shape_SetDensity(b2ShapeId shapeId, float density, boolean updateBodyMass) {
        ShapeSlot shape = requireShape(shapeId);
        if (density == shape.def.density) {
            return;
        }
        shape.def.density = density;
        if (updateBodyMass) {
            b2UpdateBodyMassData(shape.body);
        }
    }

    public static float b2Shape_GetDensity(b2ShapeId shapeId) {
        return requireShape(shapeId).def.density;
    }

    public static b2MassData b2Shape_GetMassData(b2ShapeId shapeId) {
        return b2ComputeShapeMass(requireShape(shapeId));
    }

    public static boolean b2Shape_TestPoint(b2ShapeId shapeId, b2Vec2 point) {
        ShapeSlot shape = requireShape(shapeId);
        b2Transform transform = new b2Transform(shape.body.position, shape.body.rotation);
        b2Vec2 localPoint = b2InvTransformPoint(transform, point);
        if (shape.kind == 1) {
            return b2PointInPolygon(localPoint, shape.polygon);
        }
        if (shape.kind == 2) {
            return b2PointInCircle(localPoint, shape.circle);
        }
        if (shape.kind == 4) {
            return b2PointInCapsule(localPoint, shape.capsule);
        }
        return false;
    }

    public static b2CastOutput b2Shape_RayCast(b2ShapeId shapeId, b2RayCastInput input) {
        return b2RayCastShape(input, requireShape(shapeId));
    }

    public static b2Vec2 b2Shape_GetClosestPoint(b2ShapeId shapeId, b2Vec2 target) {
        ShapeSlot shape = requireShape(shapeId);
        b2DistanceInput input = new b2DistanceInput();
        input.proxyA = b2MakeShapeDistanceProxy(shape);
        input.proxyB = b2MakeProxy(new b2Vec2[] {target}, 1, 0.0f);
        input.transformA = new b2Transform(shape.body.position, shape.body.rotation);
        input.transformB = b2Transform_identity.copy();
        input.useRadii = true;

        b2SimplexCache cache = new b2SimplexCache();
        b2DistanceOutput output = b2ShapeDistance(input, cache, null, 0);
        return output.pointA;
    }

    public static void b2Shape_SetFriction(b2ShapeId shapeId, float friction) {
        requireShape(shapeId).def.material.friction = friction;
    }

    public static float b2Shape_GetFriction(b2ShapeId shapeId) {
        return requireShape(shapeId).def.material.friction;
    }

    public static void b2Shape_SetRestitution(b2ShapeId shapeId, float restitution) {
        requireShape(shapeId).def.material.restitution = restitution;
    }

    public static float b2Shape_GetRestitution(b2ShapeId shapeId) {
        return requireShape(shapeId).def.material.restitution;
    }

    public static void b2Shape_SetMaterial(b2ShapeId shapeId, int material) {
        requireShape(shapeId).def.material.userMaterialId = material;
    }

    public static int b2Shape_GetMaterial(b2ShapeId shapeId) {
        return requireShape(shapeId).def.material.userMaterialId;
    }

    public static b2SurfaceMaterial b2Shape_GetSurfaceMaterial(b2ShapeId shapeId) {
        return copySurfaceMaterial(requireShape(shapeId).def.material);
    }

    public static void b2Shape_SetSurfaceMaterial(b2ShapeId shapeId, b2SurfaceMaterial surfaceMaterial) {
        requireShape(shapeId).def.material = copySurfaceMaterial(surfaceMaterial);
    }

    public static b2Filter b2Shape_GetFilter(b2ShapeId shapeId) {
        return copyFilter(requireShape(shapeId).def.filter);
    }

    public static void b2Shape_SetFilter(b2ShapeId shapeId, b2Filter filter) {
        ShapeSlot shape = requireShape(shapeId);
        b2Filter oldFilter = shape.def.filter;
        if (oldFilter.categoryBits == filter.categoryBits && oldFilter.maskBits == filter.maskBits
            && oldFilter.groupIndex == filter.groupIndex) {
            return;
        }
        boolean destroyProxy = oldFilter.categoryBits != filter.categoryBits;
        shape.def.filter = copyFilter(filter);
        WorldSlot world = WORLDS[shape.body.worldIndex];
        b2DestroyContactsForShape(world, shape, true);
        if (shape.proxyKey != B2_NULL_INDEX) {
            int proxyType = B2_PROXY_TYPE(shape.proxyKey);
            if (destroyProxy) {
                b2DestroyShapeProxy(shape, world.broadPhase);
                b2CreateShapeProxy(shape, world.broadPhase, proxyType, true);
            } else {
                b2UpdateShapeAABBs(shape, new b2Transform(shape.body.position, shape.body.rotation));
                b2BroadPhase_MoveProxy(world.broadPhase, shape.proxyKey, shape.fatAABB);
            }
        }
    }

    public static void b2Shape_EnableSensorEvents(b2ShapeId shapeId, boolean flag) {
        requireShape(shapeId).def.enableSensorEvents = flag;
    }

    public static boolean b2Shape_AreSensorEventsEnabled(b2ShapeId shapeId) {
        return requireShape(shapeId).def.enableSensorEvents;
    }

    public static void b2Shape_EnableContactEvents(b2ShapeId shapeId, boolean flag) {
        requireShape(shapeId).def.enableContactEvents = flag;
    }

    public static boolean b2Shape_AreContactEventsEnabled(b2ShapeId shapeId) {
        return requireShape(shapeId).def.enableContactEvents;
    }

    public static void b2Shape_EnablePreSolveEvents(b2ShapeId shapeId, boolean flag) {
        requireShape(shapeId).def.enablePreSolveEvents = flag;
    }

    public static boolean b2Shape_ArePreSolveEventsEnabled(b2ShapeId shapeId) {
        return requireShape(shapeId).def.enablePreSolveEvents;
    }

    public static void b2Shape_EnableHitEvents(b2ShapeId shapeId, boolean flag) {
        requireShape(shapeId).def.enableHitEvents = flag;
    }

    public static boolean b2Shape_AreHitEventsEnabled(b2ShapeId shapeId) {
        return requireShape(shapeId).def.enableHitEvents;
    }

    public static b2Circle b2Shape_GetCircle(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        b2Assert(shape.kind == 2, "shape is a circle");
        return new b2Circle(shape.circle.center, shape.circle.radius);
    }

    public static b2Segment b2Shape_GetSegment(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        b2Assert(shape.kind == 3, "shape is a segment");
        return new b2Segment(shape.segment.point1, shape.segment.point2);
    }

    public static b2ChainSegment b2Shape_GetChainSegment(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        b2Assert(shape.kind == 5, "shape is a chain segment");
        return copyChainSegment(shape.chainSegment);
    }

    public static b2Capsule b2Shape_GetCapsule(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        b2Assert(shape.kind == 4, "shape is a capsule");
        return new b2Capsule(shape.capsule.center1, shape.capsule.center2, shape.capsule.radius);
    }

    public static b2Polygon b2Shape_GetPolygon(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        b2Assert(shape.kind == 1, "shape is a polygon");
        return b2TransformPolygon(b2Transform_identity, shape.polygon);
    }

    public static void b2Shape_SetCircle(b2ShapeId shapeId, b2Circle circle) {
        ShapeSlot shape = requireShape(shapeId);
        shape.circle = new b2Circle(circle.center, circle.radius);
        shape.polygon = null;
        shape.segment = null;
        shape.capsule = null;
        shape.kind = 2;
        b2ResetShapeProxy(WORLDS[shape.body.worldIndex], shape, true, true);
    }

    public static void b2Shape_SetCapsule(b2ShapeId shapeId, b2Capsule capsule) {
        ShapeSlot shape = requireShape(shapeId);
        shape.capsule = new b2Capsule(capsule.center1, capsule.center2, capsule.radius);
        shape.polygon = null;
        shape.circle = null;
        shape.segment = null;
        shape.kind = 4;
        b2ResetShapeProxy(WORLDS[shape.body.worldIndex], shape, true, true);
    }

    public static void b2Shape_SetSegment(b2ShapeId shapeId, b2Segment segment) {
        ShapeSlot shape = requireShape(shapeId);
        shape.segment = new b2Segment(segment.point1, segment.point2);
        shape.polygon = null;
        shape.circle = null;
        shape.capsule = null;
        shape.kind = 3;
        b2ResetShapeProxy(WORLDS[shape.body.worldIndex], shape, true, true);
    }

    public static void b2Shape_SetPolygon(b2ShapeId shapeId, b2Polygon polygon) {
        ShapeSlot shape = requireShape(shapeId);
        shape.polygon = b2TransformPolygon(b2Transform_identity, polygon);
        shape.circle = null;
        shape.segment = null;
        shape.capsule = null;
        shape.kind = 1;
        b2ResetShapeProxy(WORLDS[shape.body.worldIndex], shape, true, true);
    }

    public static b2ChainId b2Shape_GetParentChain(b2ShapeId shapeId) {
        ShapeSlot shape = requireShape(shapeId);
        if (shape.kind != 5 || shape.chainSegment.chainId == B2_NULL_INDEX) {
            return b2_nullChainId;
        }
        WorldSlot world = WORLDS[shape.body.worldIndex];
        ChainSlot chain = world.chains.get(shape.chainSegment.chainId);
        return chain != null && chain.alive ? chainId(world, chain) : b2_nullChainId;
    }

    private static ShapeSlot createShape(b2BodyId bodyId, b2ShapeDef def, int kind) {
        BodySlot body = requireBody(bodyId);
        WorldSlot world = WORLDS[body.worldIndex];
        ShapeSlot shape = new ShapeSlot();
        shape.index = b2AllocSlot(world.freeShapeIndices, world.shapes.size());
        if (shape.index < world.shapes.size()) {
            shape.generation = world.shapes.get(shape.index).generation;
        }
        shape.body = body;
        shape.def = copyShapeDef(def);
        shape.kind = kind;
        b2StoreSlot(world.shapes, shape.index, shape);
        body.shapes.add(shape);
        return shape;
    }

    private static int b2CreateChainSegmentShape(BodySlot body, ChainSlot chain, b2ShapeDef baseDef,
                                                 b2ChainSegment segment, b2SurfaceMaterial material) {
        WorldSlot world = WORLDS[body.worldIndex];
        ShapeSlot shape = new ShapeSlot();
        shape.index = b2AllocSlot(world.freeShapeIndices, world.shapes.size());
        if (shape.index < world.shapes.size()) {
            shape.generation = world.shapes.get(shape.index).generation;
        }
        shape.body = body;
        shape.def = copyShapeDef(baseDef);
        shape.def.material = copySurfaceMaterial(material);
        shape.kind = 5;
        shape.chainSegment = copyChainSegment(segment);
        b2StoreSlot(world.shapes, shape.index, shape);
        body.shapes.add(shape);
        finishShapeCreate(shape);
        return shape.index;
    }

    private static JointSlot b2CreateJointBase(WorldSlot world, BodySlot bodyA, BodySlot bodyB, Object userData,
                                               int type, boolean collideConnected) {
        if (bodyA.worldIndex != world.index || bodyB.worldIndex != world.index) {
            throw new IllegalArgumentException("Joint bodies must belong to the target world");
        }
        JointSlot joint = new JointSlot();
        joint.index = b2AllocSlot(world.freeJointIndices, world.joints.size());
        if (joint.index < world.joints.size()) {
            joint.generation = world.joints.get(joint.index).generation;
        }
        joint.type = type;
        joint.bodyA = bodyA;
        joint.bodyB = bodyB;
        joint.userData = userData;
        joint.collideConnected = collideConnected;
        b2StoreSlot(world.joints, joint.index, joint);
        bodyA.joints.add(0, joint);
        if (bodyB != bodyA) {
            bodyB.joints.add(0, joint);
        }
        if (bodyA.type != b2_staticBody || bodyB.type != b2_staticBody) {
            b2AddJointToGraph(world, joint);
            b2LinkSleepJoint(world, joint);
        }
        return joint;
    }

    private static void b2DestroyJointInternal(WorldSlot world, JointSlot joint) {
        if (joint == null || !joint.alive) {
            return;
        }
        b2UnlinkSleepJoint(world, joint);
        b2RemoveJointFromGraph(world, joint);
        joint.alive = false;
        joint.generation += 1;
        world.freeJointIndices.add(joint.index);
        joint.bodyA.joints.remove(joint);
        joint.bodyB.joints.remove(joint);
        if (joint.collideConnected) {
            for (ShapeSlot shape : joint.bodyA.shapes.size() < joint.bodyB.shapes.size() ? joint.bodyA.shapes : joint.bodyB.shapes) {
                if (shape.proxyKey != B2_NULL_INDEX) {
                    b2BufferMove(world.broadPhase, shape.proxyKey);
                }
            }
        }
        b2RebuildContactPairSet(world);
    }

    private static void forEachChainShape(ChainSlot chain, java.util.function.Consumer<ShapeSlot> consumer) {
        WorldSlot world = WORLDS[chain.body.worldIndex];
        for (int shapeIndex : chain.shapeIndices) {
            ShapeSlot shape = world.shapes.get(shapeIndex);
            if (shape != null && shape.alive) {
                consumer.accept(shape);
            }
        }
    }

    private static void b2ResetShapeProxy(WorldSlot world, ShapeSlot shape, boolean wakeBodies, boolean destroyProxy) {
        b2DestroyContactsForShape(world, shape, wakeBodies);

        b2Transform transform = new b2Transform(shape.body.position, shape.body.rotation);
        shape.localCentroid = b2GetShapeCentroid(shape);
        b2UpdateShapeAABBs(shape, transform);
        if (shape.proxyKey != B2_NULL_INDEX) {
            int proxyType = B2_PROXY_TYPE(shape.proxyKey);
            if (destroyProxy) {
                b2DestroyShapeProxy(shape, world.broadPhase);
                b2CreateShapeProxy(shape, world.broadPhase, proxyType, true);
            } else {
                b2BroadPhase_MoveProxy(world.broadPhase, shape.proxyKey, shape.fatAABB);
            }
        }
    }

    private static void finishShapeCreate(ShapeSlot shape) {
        shape.localCentroid = b2GetShapeCentroid(shape);
        b2UpdateShapeAABBs(shape, new b2Transform(shape.body.position, shape.body.rotation));
        if (shape.body.enabled) {
            WorldSlot world = WORLDS[shape.body.worldIndex];
            boolean forcePairCreation = shape.def.invokeContactCreation || shape.def.isSensor;
            b2CreateShapeProxy(shape, world.broadPhase, shape.body.type, forcePairCreation);
        }
        if (shape.def.updateBodyMass) {
            b2UpdateBodyMassData(shape.body);
        }
    }

    private static b2Vec2 b2GetShapeCentroid(ShapeSlot shape) {
        if (shape.kind == 1) {
            return shape.polygon.centroid.copy();
        }
        if (shape.kind == 2) {
            return shape.circle.center.copy();
        }
        if (shape.kind == 4) {
            return b2Lerp(shape.capsule.center1, shape.capsule.center2, 0.5f);
        }
        if (shape.kind == 5) {
            return b2Lerp(shape.chainSegment.segment.point1, shape.chainSegment.segment.point2, 0.5f);
        }
        return b2Lerp(shape.segment.point1, shape.segment.point2, 0.5f);
    }

    private static float b2GetShapeProjectedPerimeter(ShapeSlot shape, b2Vec2 line) {
        if (shape.kind == 1) {
            b2Vec2[] points = shape.polygon.vertices;
            int count = shape.polygon.count;
            float value = b2Dot(points[0], line);
            float lower = value;
            float upper = value;
            for (int i = 1; i < count; ++i) {
                value = b2Dot(points[i], line);
                lower = b2MinFloat(lower, value);
                upper = b2MaxFloat(upper, value);
            }
            return upper - lower + 2.0f * shape.polygon.radius;
        }
        if (shape.kind == 2) {
            return 2.0f * shape.circle.radius;
        }
        if (shape.kind == 4) {
            b2Vec2 axis = b2Sub(shape.capsule.center2, shape.capsule.center1);
            return b2AbsFloat(b2Dot(axis, line)) + 2.0f * shape.capsule.radius;
        }
        if (shape.kind == 5) {
            float value1 = b2Dot(shape.chainSegment.segment.point1, line);
            float value2 = b2Dot(shape.chainSegment.segment.point2, line);
            return b2AbsFloat(value2 - value1);
        }
        float value1 = b2Dot(shape.segment.point1, line);
        float value2 = b2Dot(shape.segment.point2, line);
        return b2AbsFloat(value2 - value1);
    }

    private static b2AABB b2ComputeShapeAABB(ShapeSlot shape, b2Transform transform) {
        if (shape.kind == 1) {
            return b2ComputePolygonAABB(shape.polygon, transform);
        }
        if (shape.kind == 2) {
            return b2ComputeCircleAABB(shape.circle, transform);
        }
        if (shape.kind == 4) {
            return b2ComputeCapsuleAABB(shape.capsule, transform);
        }
        if (shape.kind == 5) {
            return b2ComputeSegmentAABB(shape.chainSegment.segment, transform);
        }
        return b2ComputeSegmentAABB(shape.segment, transform);
    }

    private static b2MassData b2ComputeShapeMass(ShapeSlot shape) {
        if (shape.kind == 1) {
            return b2ComputePolygonMass(shape.polygon, shape.def.density);
        }
        if (shape.kind == 2) {
            return b2ComputeCircleMass(shape.circle, shape.def.density);
        }
        if (shape.kind == 4) {
            return b2ComputeCapsuleMass(shape.capsule, shape.def.density);
        }
        return new b2MassData();
    }

    private static b2ShapeProxy b2MakeShapeDistanceProxy(ShapeSlot shape) {
        if (shape.kind == 1) {
            return b2MakeProxy(shape.polygon.vertices, shape.polygon.count, shape.polygon.radius);
        }
        if (shape.kind == 2) {
            return b2MakeProxy(new b2Vec2[] {shape.circle.center}, 1, shape.circle.radius);
        }
        if (shape.kind == 4) {
            return b2MakeProxy(new b2Vec2[] {shape.capsule.center1, shape.capsule.center2}, 2, shape.capsule.radius);
        }
        if (shape.kind == 5) {
            return b2MakeProxy(new b2Vec2[] {shape.chainSegment.segment.point1, shape.chainSegment.segment.point2}, 2, 0.0f);
        }
        return b2MakeProxy(new b2Vec2[] {shape.segment.point1, shape.segment.point2}, 2, 0.0f);
    }

    private static ShapeSlot getWorldQueryShape(WorldSlot world, long userData) {
        int shapeIndex = (int) userData;
        if (shapeIndex < 0 || shapeIndex >= world.shapes.size()) {
            return null;
        }
        ShapeSlot shape = world.shapes.get(shapeIndex);
        return shape != null && shape.alive ? shape : null;
    }

    private static boolean b2ShouldQueryCollide(b2Filter shapeFilter, b2QueryFilter queryFilter) {
        return (shapeFilter.categoryBits & queryFilter.maskBits) != 0L
            && (shapeFilter.maskBits & queryFilter.categoryBits) != 0L;
    }

    private static b2CastOutput b2RayCastShape(b2RayCastInput input, ShapeSlot shape) {
        b2Transform transform = new b2Transform(shape.body.position, shape.body.rotation);
        b2RayCastInput localInput = new b2RayCastInput();
        localInput.origin = b2InvTransformPoint(transform, input.origin);
        localInput.translation = b2InvRotateVector(transform.q, input.translation);
        localInput.maxFraction = input.maxFraction;

        b2CastOutput output;
        if (shape.kind == 1) {
            output = b2RayCastPolygon(localInput, shape.polygon);
        } else if (shape.kind == 2) {
            output = b2RayCastCircle(localInput, shape.circle);
        } else if (shape.kind == 3) {
            output = b2RayCastSegment(localInput, shape.segment, false);
        } else if (shape.kind == 4) {
            output = b2RayCastCapsule(localInput, shape.capsule);
        } else if (shape.kind == 5) {
            output = b2RayCastSegment(localInput, shape.chainSegment.segment, true);
        } else {
            output = new b2CastOutput();
        }

        if (output.hit) {
            output.normal = b2RotateVector(transform.q, output.normal);
            output.point = b2TransformPoint(transform, output.point);
        }
        return output;
    }

    private static b2CastOutput b2ShapeCastShape(b2ShapeCastInput input, ShapeSlot shape) {
        b2Transform transform = new b2Transform(shape.body.position, shape.body.rotation);
        b2ShapeCastInput localInput = new b2ShapeCastInput();
        localInput.proxy.count = input.proxy.count;
        localInput.proxy.radius = input.proxy.radius;
        for (int i = 0; i < input.proxy.count; ++i) {
            localInput.proxy.points[i].set(b2InvTransformPoint(transform, input.proxy.points[i]));
        }
        localInput.translation = b2InvRotateVector(transform.q, input.translation);
        localInput.maxFraction = input.maxFraction;
        localInput.canEncroach = input.canEncroach;

        b2CastOutput output;
        if (shape.kind == 4) {
            output = b2ShapeCastCapsule(localInput, shape.capsule);
        } else if (shape.kind == 2) {
            output = b2ShapeCastCircle(localInput, shape.circle);
        } else if (shape.kind == 1) {
            output = b2ShapeCastPolygon(localInput, shape.polygon);
        } else if (shape.kind == 3) {
            output = b2ShapeCastSegment(localInput, shape.segment);
        } else if (shape.kind == 5) {
            output = b2ShapeCastSegment(localInput, shape.chainSegment.segment);
        } else {
            return new b2CastOutput();
        }

        output.point = b2TransformPoint(transform, output.point);
        output.normal = b2RotateVector(transform.q, output.normal);
        return output;
    }

    private static void b2DrawShape(b2DebugDraw draw, ShapeSlot shape, b2Transform transform, int color) {
        if (shape.kind == 1) {
            draw.DrawSolidPolygonFcn.invoke(transform, b2CopyVec2Array(shape.polygon.vertices, shape.polygon.count), shape.polygon.count,
                shape.polygon.radius, color);
        } else if (shape.kind == 2) {
            b2Transform circleTransform = new b2Transform(b2TransformPoint(transform, shape.circle.center), transform.q);
            draw.DrawSolidCircleFcn.invoke(circleTransform, shape.circle.radius, color);
        } else if (shape.kind == 4) {
            draw.DrawSolidCapsuleFcn.invoke(b2TransformPoint(transform, shape.capsule.center1),
                b2TransformPoint(transform, shape.capsule.center2), shape.capsule.radius, color);
        } else if (shape.kind == 5) {
            b2Vec2 p1 = b2TransformPoint(transform, shape.chainSegment.segment.point1);
            b2Vec2 p2 = b2TransformPoint(transform, shape.chainSegment.segment.point2);
            draw.DrawSegmentFcn.invoke(p1, p2, color);
            draw.DrawPointFcn.invoke(p2, 4.0f, color);
            draw.DrawSegmentFcn.invoke(p1, b2Lerp(p1, p2, 0.1f), b2_colorPaleGreen);
        } else if (shape.kind == 3) {
            draw.DrawSegmentFcn.invoke(b2TransformPoint(transform, shape.segment.point1),
                b2TransformPoint(transform, shape.segment.point2), color);
        }
    }

    private static void b2DrawAABB(b2DebugDraw draw, b2AABB aabb, int color) {
        b2Vec2[] vertices = new b2Vec2[] {
            new b2Vec2(aabb.lowerBound.x, aabb.lowerBound.y),
            new b2Vec2(aabb.upperBound.x, aabb.lowerBound.y),
            new b2Vec2(aabb.upperBound.x, aabb.upperBound.y),
            new b2Vec2(aabb.lowerBound.x, aabb.upperBound.y)
        };
        draw.DrawPolygonFcn.invoke(vertices, 4, color);
    }

    private static void b2DrawContacts(b2DebugDraw draw, WorldSlot world) {
        final float impulseScale = 1.0f;
        final float axisScale = 0.3f;
        final float linearSlop = B2_LINEAR_SLOP();
        final int speculativeColor = b2_colorLightGray;
        final int addColor = b2_colorGreen;
        final int persistColor = b2_colorBlue;
        final int normalColor = b2_colorDimGray;
        final int impulseColor = b2_colorMagenta;
        final int frictionColor = b2_colorYellow;
        final int[] graphColors = {
            b2_colorRed, b2_colorOrange, b2_colorYellow, b2_colorGreen,
            b2_colorCyan, b2_colorBlue, b2_colorViolet, b2_colorPink,
            b2_colorChocolate, b2_colorGoldenRod, b2_colorCoral, b2_colorBlack
        };

        for (int colorIndex = 0; colorIndex < B2_GRAPH_COLOR_COUNT; ++colorIndex) {
            for (ContactSlot contact : world.contactGraphColors[colorIndex]) {
                if (contact == null || !contact.alive || !contact.touching
                    || contact.shapeA.def.isSensor || contact.shapeB.def.isSensor) {
                    continue;
                }
                b2Manifold manifold = contact.manifold;
                b2Vec2 normal = manifold.normal;
                for (int i = 0; i < manifold.pointCount; ++i) {
                    b2ManifoldPoint point = manifold.points[i];
                    if (draw.drawGraphColors && 0 <= colorIndex && colorIndex < B2_GRAPH_COLOR_COUNT) {
                        float pointSize = colorIndex == B2_OVERFLOW_INDEX ? 7.5f : 5.0f;
                        draw.DrawPointFcn.invoke(point.point, pointSize, graphColors[colorIndex]);
                    } else if (point.separation > linearSlop) {
                        draw.DrawPointFcn.invoke(point.point, 5.0f, speculativeColor);
                    } else if (!point.persisted) {
                        draw.DrawPointFcn.invoke(point.point, 10.0f, addColor);
                    } else {
                        draw.DrawPointFcn.invoke(point.point, 5.0f, persistColor);
                    }

                    if (draw.drawContactNormals) {
                        b2Vec2 p1 = point.point;
                        b2Vec2 p2 = b2MulAdd(p1, axisScale, normal);
                        draw.DrawSegmentFcn.invoke(p1, p2, normalColor);
                    } else if (draw.drawContactImpulses) {
                        b2Vec2 p1 = point.point;
                        b2Vec2 p2 = b2MulAdd(p1, impulseScale * point.totalNormalImpulse, normal);
                        draw.DrawSegmentFcn.invoke(p1, p2, impulseColor);
                        draw.DrawStringFcn.invoke(p1, String.format(java.util.Locale.ROOT, "%.2f", 1000.0f * point.totalNormalImpulse),
                            b2_colorWhite);
                    }

                    if (draw.drawContactFeatures) {
                        draw.DrawStringFcn.invoke(point.point, Integer.toString(point.id), b2_colorOrange);
                    }

                    if (draw.drawFrictionImpulses) {
                        b2Vec2 tangent = b2RightPerp(normal);
                        b2Vec2 p1 = point.point;
                        b2Vec2 p2 = b2MulAdd(p1, impulseScale * point.tangentImpulse, tangent);
                        draw.DrawSegmentFcn.invoke(p1, p2, frictionColor);
                        draw.DrawStringFcn.invoke(p1, String.format(java.util.Locale.ROOT, "%.2f", point.tangentImpulse),
                            b2_colorWhite);
                    }
                }
            }
        }
    }

    private static b2Vec2[] b2CopyVec2Array(b2Vec2[] src, int count) {
        b2Vec2[] dst = new b2Vec2[count];
        for (int i = 0; i < count; ++i) {
            dst[i] = src[i].copy();
        }
        return dst;
    }

    private static int b2DebugDrawColor(ShapeSlot shape) {
        BodySlot body = shape.body;
        if (shape.def.material.customColor != 0) {
            return shape.def.material.customColor;
        }
        if (body.type == b2_dynamicBody && body.mass == 0.0f) {
            return b2_colorRed;
        }
        if (!body.enabled) {
            return b2_colorSlateGray;
        }
        if (shape.def.isSensor) {
            return b2_colorWheat;
        }
        if (body.bullet && body.awake) {
            return b2_colorTurquoise;
        }
        if (body.type == b2_staticBody) {
            return b2_colorPaleGreen;
        }
        if (body.type == b2_kinematicBody) {
            return b2_colorRoyalBlue;
        }
        return body.awake ? b2_colorPink : b2_colorGray;
    }

    private static void b2DrawJoint(b2DebugDraw draw, JointSlot joint) {
        b2Transform transformA = new b2Transform(joint.bodyA.position, joint.bodyA.rotation);
        b2Transform transformB = new b2Transform(joint.bodyB.position, joint.bodyB.rotation);
        b2Vec2 pA = b2TransformPoint(transformA, joint.localAnchorA);
        b2Vec2 pB = b2TransformPoint(transformB, joint.localAnchorB);
        if (joint.type == b2_mouseJoint) {
            draw.DrawPointFcn.invoke(joint.mouseTarget.copy(), 4.0f, b2_colorGreen);
            draw.DrawPointFcn.invoke(pB, 4.0f, b2_colorGreen);
            draw.DrawSegmentFcn.invoke(joint.mouseTarget.copy(), pB, b2_colorGray);
            return;
        }
        draw.DrawSegmentFcn.invoke(transformA.p.copy(), pA, b2_colorWhite);
        draw.DrawSegmentFcn.invoke(pA, pB, b2_colorWhite);
        draw.DrawSegmentFcn.invoke(transformB.p.copy(), pB, b2_colorWhite);
    }

    private static b2PlaneResult b2CollideMover(ShapeSlot shape, b2Transform transform, b2Capsule mover) {
        b2Capsule localMover = new b2Capsule(b2InvTransformPoint(transform, mover.center1),
            b2InvTransformPoint(transform, mover.center2), mover.radius);
        b2PlaneResult result;
        if (shape.kind == 1) {
            result = b2CollideMoverWithProxy(b2MakeProxy(shape.polygon.vertices, shape.polygon.count, shape.polygon.radius),
                localMover, shape.polygon.radius + localMover.radius);
        } else if (shape.kind == 2) {
            result = b2CollideMoverWithProxy(b2MakeProxy(new b2Vec2[] {shape.circle.center}, 1, 0.0f),
                localMover, shape.circle.radius + localMover.radius);
        } else if (shape.kind == 4) {
            result = b2CollideMoverWithProxy(b2MakeProxy(new b2Vec2[] {shape.capsule.center1, shape.capsule.center2}, 2, 0.0f),
                localMover, shape.capsule.radius + localMover.radius);
        } else if (shape.kind == 5) {
            result = b2CollideMoverWithProxy(b2MakeProxy(new b2Vec2[] {shape.chainSegment.segment.point1,
                shape.chainSegment.segment.point2}, 2, 0.0f), localMover, localMover.radius);
        } else if (shape.kind == 3) {
            result = b2CollideMoverWithProxy(b2MakeProxy(new b2Vec2[] {shape.segment.point1, shape.segment.point2}, 2, 0.0f),
                localMover, localMover.radius);
        } else {
            return new b2PlaneResult();
        }
        if (!result.hit) {
            return result;
        }
        result.plane.normal = b2RotateVector(transform.q, result.plane.normal);
        return result;
    }

    private static b2PlaneResult b2CollideMoverWithProxy(b2ShapeProxy shapeProxy, b2Capsule mover, float totalRadius) {
        b2DistanceInput input = new b2DistanceInput();
        input.proxyA = shapeProxy;
        input.proxyB = b2MakeProxy(new b2Vec2[] {mover.center1, mover.center2}, 2, mover.radius);
        input.transformA = b2Transform_identity.copy();
        input.transformB = b2Transform_identity.copy();
        input.useRadii = false;

        b2SimplexCache cache = new b2SimplexCache();
        b2DistanceOutput output = b2ShapeDistance(input, cache, null, 0);
        b2PlaneResult result = new b2PlaneResult();
        if (output.distance <= totalRadius) {
            result.plane = new b2Plane(output.normal, totalRadius - output.distance);
            result.point = output.pointA.copy();
            result.hit = true;
        }
        return result;
    }

    private static b2ShapeProxy b2MakeTransformedShapeProxy(ShapeSlot shape) {
        b2Transform transform = new b2Transform(shape.body.position, shape.body.rotation);
        if (shape.kind == 1) {
            b2Vec2[] points = b2Vec2.array(shape.polygon.count);
            for (int i = 0; i < shape.polygon.count; ++i) {
                points[i].set(b2TransformPoint(transform, shape.polygon.vertices[i]));
            }
            return b2MakeProxy(points, shape.polygon.count, shape.polygon.radius);
        }
        if (shape.kind == 2) {
            return b2MakeProxy(new b2Vec2[] {b2TransformPoint(transform, shape.circle.center)}, 1, shape.circle.radius);
        }
        if (shape.kind == 4) {
            return b2MakeProxy(new b2Vec2[] {b2TransformPoint(transform, shape.capsule.center1),
                b2TransformPoint(transform, shape.capsule.center2)}, 2, shape.capsule.radius);
        }
        if (shape.kind == 5) {
            return b2MakeProxy(new b2Vec2[] {b2TransformPoint(transform, shape.chainSegment.segment.point1),
                b2TransformPoint(transform, shape.chainSegment.segment.point2)}, 2, 0.0f);
        }
        return b2MakeProxy(new b2Vec2[] {b2TransformPoint(transform, shape.segment.point1),
            b2TransformPoint(transform, shape.segment.point2)}, 2, 0.0f);
    }

    private static void b2UpdateShapeAABBs(ShapeSlot shape, b2Transform transform) {
        b2AABB aabb = b2ComputeShapeAABB(shape, transform);
        float speculativeDistance = B2_SPECULATIVE_DISTANCE();
        aabb.lowerBound.x -= speculativeDistance;
        aabb.lowerBound.y -= speculativeDistance;
        aabb.upperBound.x += speculativeDistance;
        aabb.upperBound.y += speculativeDistance;
        shape.aabb = aabb;

        float margin = shape.body.type == b2_staticBody ? speculativeDistance : B2_AABB_MARGIN();
        shape.fatAABB = new b2AABB(
            new b2Vec2(aabb.lowerBound.x - margin, aabb.lowerBound.y - margin),
            new b2Vec2(aabb.upperBound.x + margin, aabb.upperBound.y + margin));
    }

    private static void b2CreateShapeProxy(ShapeSlot shape, b2BroadPhase bp, int type, boolean forcePairCreation) {
        b2Assert(shape.proxyKey == B2_NULL_INDEX, "shape proxy is null");
        b2UpdateShapeAABBs(shape, new b2Transform(shape.body.position, shape.body.rotation));
        shape.proxyKey = b2BroadPhase_CreateProxy(bp, type, shape.fatAABB, shape.def.filter.categoryBits, shape.index, forcePairCreation);
    }

    private static void b2DestroyShapeProxy(ShapeSlot shape, b2BroadPhase bp) {
        if (shape.proxyKey != B2_NULL_INDEX) {
            b2BroadPhase_DestroyProxy(bp, shape.proxyKey);
            shape.proxyKey = B2_NULL_INDEX;
        }
    }

    private static void b2SynchronizeBodyProxies(WorldSlot world, BodySlot body) {
        b2SynchronizeBodyProxies(world, body, false);
    }

    private static void b2SynchronizeBodyProxies(WorldSlot world, BodySlot body, boolean deferEnlarge) {
        b2Transform transform = new b2Transform(body.position, body.rotation);
        for (ShapeSlot shape : body.shapes) {
            if (shape.proxyKey == B2_NULL_INDEX) {
                continue;
            }
            b2AABB aabb = b2ComputeShapeAABB(shape, transform);
            float speculativeDistance = B2_SPECULATIVE_DISTANCE();
            aabb.lowerBound.x -= speculativeDistance;
            aabb.lowerBound.y -= speculativeDistance;
            aabb.upperBound.x += speculativeDistance;
            aabb.upperBound.y += speculativeDistance;
            shape.aabb = aabb;

            if (!b2AABB_Contains(shape.fatAABB, aabb)) {
                float margin = B2_AABB_MARGIN();
                shape.fatAABB = new b2AABB(
                    new b2Vec2(aabb.lowerBound.x - margin, aabb.lowerBound.y - margin),
                    new b2Vec2(aabb.upperBound.x + margin, aabb.upperBound.y + margin));
                if (deferEnlarge) {
                    shape.enlargedAABB = true;
                } else {
                    b2BroadPhase_EnlargeProxy(world.broadPhase, shape.proxyKey, shape.fatAABB);
                }
            }
        }
    }

    private static void b2SynchronizeFastBodyProxies(WorldSlot world, BodySlot body, boolean didHit, boolean deferEnlarge) {
        b2Transform transform = new b2Transform(body.position, body.rotation);
        for (ShapeSlot shape : body.shapes) {
            if (shape.proxyKey == B2_NULL_INDEX) {
                continue;
            }

            b2AABB aabb = b2ComputeShapeAABB(shape, transform);
            if (didHit) {
                float speculativeDistance = B2_SPECULATIVE_DISTANCE();
                aabb.lowerBound.x -= speculativeDistance;
                aabb.lowerBound.y -= speculativeDistance;
                aabb.upperBound.x += speculativeDistance;
                aabb.upperBound.y += speculativeDistance;
            }
            shape.aabb = aabb;

            if (!b2AABB_Contains(shape.fatAABB, aabb)) {
                float margin = B2_AABB_MARGIN();
                shape.fatAABB = new b2AABB(
                    new b2Vec2(aabb.lowerBound.x - margin, aabb.lowerBound.y - margin),
                    new b2Vec2(aabb.upperBound.x + margin, aabb.upperBound.y + margin));
                if (deferEnlarge) {
                    shape.enlargedAABB = true;
                } else {
                    b2BroadPhase_EnlargeProxy(world.broadPhase, shape.proxyKey, shape.fatAABB);
                }
            }
        }
    }

    private static void b2ApplyDeferredProxyEnlargements(WorldSlot world, BodySlot body) {
        for (ShapeSlot shape : body.shapes) {
            if (shape.proxyKey != B2_NULL_INDEX && shape.enlargedAABB) {
                b2BroadPhase_EnlargeProxy(world.broadPhase, shape.proxyKey, shape.fatAABB);
                shape.enlargedAABB = false;
            }
        }
    }

    private static void b2UpdateBroadPhasePairs(WorldSlot world) {
        b2BroadPhase bp = world.broadPhase;
        if (bp.moveArray.isEmpty()) {
            return;
        }

        java.util.ArrayList<Integer> moveArray = new java.util.ArrayList<>(bp.moveArray);
        final class MovePair {
            int shapeIndexA;
            int shapeIndexB;
            MovePair next;
        }
        MovePair[] moveResults = new MovePair[moveArray.size()];

        b2ParallelFor(world, moveArray.size(), 64, (startIndex, endIndex, workerIndex, taskContext) -> {
            for (int moveIndex = startIndex; moveIndex < endIndex; ++moveIndex) {
                int proxyKey = moveArray.get(moveIndex);
                int proxyType = B2_PROXY_TYPE(proxyKey);
                int proxyId = B2_PROXY_ID(proxyKey);
                if (bp.trees[proxyType].nodes == null || proxyId < 0 || proxyId >= bp.trees[proxyType].nodeCapacity) {
                    continue;
                }
                b2TreeNode proxyNode = bp.trees[proxyType].nodes[proxyId];
                if ((proxyNode.flags & B2_ALLOCATED_NODE) == 0) {
                    continue;
                }

                int shapeIndex = b2BroadPhase_GetShapeIndex(bp, proxyKey);
                ShapeSlot shape = shapeIndex >= 0 && shapeIndex < world.shapes.size() ? world.shapes.get(shapeIndex) : null;
                if (shape == null || !shape.alive) {
                    continue;
                }

                b2AABB fatAABB = b2DynamicTree_GetAABB(bp.trees[proxyType], proxyId);
                int[] treeTypes = proxyType == b2_dynamicBody
                    ? new int[] {b2_kinematicBody, b2_staticBody, b2_dynamicBody}
                    : new int[] {b2_dynamicBody};
                final int resultIndex = moveIndex;
                for (int treeType : treeTypes) {
                    final int queryTreeType = treeType;
                    b2DynamicTree_Query(bp.trees[treeType], fatAABB, B2_DEFAULT_MASK_BITS, (candidateProxyId, userData) -> {
                    int candidateProxyKey = B2_PROXY_KEY(candidateProxyId, queryTreeType);
                    if (proxyType == b2_dynamicBody) {
                        if (queryTreeType == b2_dynamicBody && candidateProxyKey < proxyKey
                            && b2ContainsKey(bp.moveSet, candidateProxyKey + 1L)) {
                            return true;
                        }
                    } else if (b2ContainsKey(bp.moveSet, candidateProxyKey + 1L)) {
                        return true;
                    }

                    int candidateShapeIndex = (int) userData;
                    if (candidateShapeIndex == shape.index) {
                        return true;
                    }
                    ShapeSlot other = candidateShapeIndex >= 0 && candidateShapeIndex < world.shapes.size()
                        ? world.shapes.get(candidateShapeIndex)
                        : null;
                    if (other == null || !other.alive) {
                        return true;
                    }
                    int shapeIndexA;
                    int shapeIndexB;
                    if (candidateProxyKey < proxyKey) {
                        shapeIndexA = other.index;
                        shapeIndexB = shape.index;
                    } else {
                        shapeIndexA = shape.index;
                        shapeIndexB = other.index;
                    }
                    long pairKey = B2_SHAPE_PAIR_KEY(shapeIndexA + 1, shapeIndexB + 1);
                    if (b2ContainsKey(bp.pairSet, pairKey)) {
                        return true;
                    }
                    if (other.body == shape.body) {
                        return true;
                    }
                    if (shape.body.type == b2_staticBody && other.body.type == b2_staticBody) {
                        return true;
                    }
                    if (!b2ShouldShapesCollide(shape, other)) {
                        return true;
                    }
                    if (!b2ShouldCustomFilterCollide(world, shape, other)) {
                        return true;
                    }
                    MovePair pair = new MovePair();
                    pair.shapeIndexA = shapeIndexA;
                    pair.shapeIndexB = shapeIndexB;
                    pair.next = moveResults[resultIndex];
                    moveResults[resultIndex] = pair;
                    return true;
                    });
                }
            }
        }, null);

        for (MovePair pairList : moveResults) {
            MovePair pair = pairList;
            while (pair != null) {
                b2CreateContact(world, world.shapes.get(pair.shapeIndexA), world.shapes.get(pair.shapeIndexB));
                pair = pair.next;
            }
        }

        bp.moveArray.clear();
        b2ClearSet(bp.moveSet);
    }

    private static boolean b2ShouldShapesCollide(ShapeSlot shapeA, ShapeSlot shapeB) {
        if (!b2ShouldBodiesCollide(shapeA.body, shapeB.body)) {
            return false;
        }
        b2Filter filterA = shapeA.def.filter;
        b2Filter filterB = shapeB.def.filter;
        if (filterA.groupIndex == filterB.groupIndex && filterA.groupIndex != 0) {
            return filterA.groupIndex > 0;
        }
        return (filterA.maskBits & filterB.categoryBits) != 0L && (filterA.categoryBits & filterB.maskBits) != 0L;
    }

    private static boolean b2ShouldBodiesCollide(BodySlot bodyA, BodySlot bodyB) {
        java.util.List<JointSlot> joints = bodyA.joints.size() < bodyB.joints.size() ? bodyA.joints : bodyB.joints;
        BodySlot other = joints == bodyA.joints ? bodyB : bodyA;
        for (JointSlot joint : joints) {
            if (joint.alive && !joint.collideConnected && (joint.bodyA == other || joint.bodyB == other)) {
                return false;
            }
        }
        return true;
    }

    private static boolean b2ShouldCustomFilterCollide(WorldSlot world, ShapeSlot shapeA, ShapeSlot shapeB) {
        return world.customFilterFcn == null
            || world.customFilterFcn.invoke(shapeId(world, shapeA), shapeId(world, shapeB), world.customFilterContext);
    }

    private static float b2GetAxisJointLinearSeparation(JointSlot joint, b2Vec2 dp, boolean enableLimit,
                                                        float lowerTranslation, float upperTranslation) {
        b2Vec2 axisA = b2RotateVector(joint.bodyA.rotation, joint.localAxisA);
        b2Vec2 perpA = b2LeftPerp(axisA);
        float perpendicularSeparation = b2AbsFloat(b2Dot(perpA, dp));
        float limitSeparation = 0.0f;
        if (enableLimit) {
            float translation = b2Dot(axisA, dp);
            if (translation < lowerTranslation) {
                limitSeparation = lowerTranslation - translation;
            }
            if (upperTranslation < translation) {
                limitSeparation = translation - upperTranslation;
            }
        }
        return (float) Math.sqrt(perpendicularSeparation * perpendicularSeparation + limitSeparation * limitSeparation);
    }

    private static void b2DestroyContactsBetweenBodies(WorldSlot world, BodySlot bodyA, BodySlot bodyB) {
        BodySlot body = bodyA.contacts.size() < bodyB.contacts.size() ? bodyA : bodyB;
        for (ContactSlot contact : new java.util.ArrayList<>(body.contacts)) {
            if (contact.alive
                && ((contact.shapeA.body == bodyA && contact.shapeB.body == bodyB)
                    || (contact.shapeA.body == bodyB && contact.shapeB.body == bodyA))) {
                b2DestroyContact(world, contact);
            }
        }
    }

    private static void b2DestroyContactsForBody(WorldSlot world, BodySlot body) {
        b2DestroyContactsForBody(world, body, false);
    }

    private static void b2DestroyContactsForBody(WorldSlot world, BodySlot body, boolean wakeBodies) {
        while (!body.contacts.isEmpty()) {
            ContactSlot contact = body.contacts.get(0);
            if (contact.alive) {
                b2DestroyContact(world, contact, wakeBodies);
            } else {
                body.contacts.remove(0);
            }
        }
    }

    private static void b2DestroyShapeInternal(WorldSlot world, ShapeSlot shape, boolean updateBodyMass) {
        if (shape == null || !shape.alive) {
            return;
        }
        b2DestroyShapeProxy(shape, world.broadPhase);
        b2DestroyContactsForShape(world, shape);
        shape.alive = false;
        shape.generation += 1;
        world.freeShapeIndices.add(shape.index);
        shape.body.shapes.remove(shape);
        if (updateBodyMass) {
            b2UpdateBodyMassData(shape.body);
        }
        b2RebuildContactPairSet(world);
    }

    private static void b2CreateContact(WorldSlot world, ShapeSlot shapeA, ShapeSlot shapeB) {
        if (shapeA.def.isSensor || shapeB.def.isSensor) {
            return;
        }
        if (!b2CanCreateContact(shapeA.kind, shapeB.kind)) {
            return;
        }
        if (!b2IsPrimaryContactOrder(shapeA.kind, shapeB.kind)) {
            b2CreateContact(world, shapeB, shapeA);
            return;
        }
        long pairKey = B2_SHAPE_PAIR_KEY(shapeA.index + 1, shapeB.index + 1);
        if (b2AddKey(world.broadPhase.pairSet, pairKey)) {
            return;
        }
        ContactSlot contact = b2AllocContact(world);
        contact.shapeA = shapeA;
        contact.shapeB = shapeB;
        shapeA.body.contacts.add(0, contact);
        shapeB.body.contacts.add(0, contact);
    }

    private static boolean b2CanCreateContact(int kindA, int kindB) {
        if (kindA == kindB) {
            return kindA == 1 || kindA == 2 || kindA == 4;
        }
        return b2IsPrimaryContactOrder(kindA, kindB) || b2IsPrimaryContactOrder(kindB, kindA);
    }

    private static boolean b2IsPrimaryContactOrder(int kindA, int kindB) {
        if (kindA == kindB) {
            return kindA == 1 || kindA == 2 || kindA == 4;
        }
        if (kindA == 1) {
            return kindB == 2 || kindB == 4;
        }
        if (kindA == 3) {
            return kindB == 1 || kindB == 2 || kindB == 4;
        }
        if (kindA == 4) {
            return kindB == 2;
        }
        if (kindA == 5) {
            return kindB == 1 || kindB == 2 || kindB == 4;
        }
        return false;
    }

    private static void b2DestroyContactsForShape(WorldSlot world, ShapeSlot shape) {
        b2DestroyContactsForShape(world, shape, false);
    }

    private static void b2DestroyContactsForShape(WorldSlot world, ShapeSlot shape, boolean wakeBodies) {
        for (ContactSlot contact : new java.util.ArrayList<>(shape.body.contacts)) {
            if (contact.alive && (contact.shapeA == shape || contact.shapeB == shape)) {
                b2DestroyContact(world, contact, wakeBodies);
            }
        }
    }

    private static ContactSlot b2AllocContact(WorldSlot world) {
        ContactSlot contact;
        if (!world.freeContactIndices.isEmpty()) {
            int index = world.freeContactIndices.remove(world.freeContactIndices.size() - 1);
            contact = world.contacts.get(index);
            if (contact == null) {
                contact = new ContactSlot();
                contact.index = index;
                world.contacts.set(index, contact);
            }
        } else {
            int index = world.nextContactIndex;
            world.nextContactIndex += 1;
            contact = new ContactSlot();
            contact.index = index;
            if (index == world.contacts.size()) {
                world.contacts.add(contact);
            } else {
                world.contacts.set(index, contact);
            }
        }
        contact.alive = true;
        contact.manifold = new b2Manifold();
        contact.touching = false;
        contact.sleepIslandId = B2_NULL_INDEX;
        world.contactCount += 1;
        world.contactUpdateOrder.add(contact);
        return contact;
    }

    private static void b2DestroyContact(WorldSlot world, ContactSlot contact) {
        b2DestroyContact(world, contact, false);
    }

    private static void b2DestroyContact(WorldSlot world, ContactSlot contact, boolean wakeBodies) {
        if (contact == null || !contact.alive) {
            return;
        }
        boolean touching = contact.touching;
        BodySlot bodyA = contact.shapeA.body;
        BodySlot bodyB = contact.shapeB.body;
        b2RemoveKey(world.broadPhase.pairSet, B2_SHAPE_PAIR_KEY(contact.shapeA.index + 1, contact.shapeB.index + 1));
        b2UnlinkSleepContact(world, contact);
        b2RemoveContactFromGraph(world, contact);
        bodyA.contacts.remove(contact);
        bodyB.contacts.remove(contact);
        contact.alive = false;
        contact.touching = false;
        contact.manifold = new b2Manifold();
        world.contactCount -= 1;
        b2RemoveSwap(world.contactUpdateOrder, contact);
        world.freeContactIndices.add(contact.index);
        if (wakeBodies && touching) {
            b2WakeBody(bodyA);
            b2WakeBody(bodyB);
        }
    }

    private static void b2UpdateContacts(WorldSlot world) {
        java.util.ArrayList<b2ContactBeginTouchEvent> beginEvents = new java.util.ArrayList<>();
        java.util.ArrayList<b2ContactEndTouchEvent> endEvents = new java.util.ArrayList<>();
        byte[] contactState = new byte[world.contacts.size()];
        java.util.ArrayList<ContactSlot> updateOrder = new java.util.ArrayList<>(world.contactUpdateOrder);
        b2ParallelFor(world, updateOrder.size(), 64, (startIndex, endIndex, workerIndex, taskContext) -> {
            for (int updateIndex = startIndex; updateIndex < endIndex; ++updateIndex) {
                ContactSlot contact = updateOrder.get(updateIndex);
                if (contact == null || !contact.alive) {
                    continue;
                }
                if (!b2ContactHasAwakeDynamicBody(contact)) {
                    continue;
                }
                ShapeSlot shapeA = contact.shapeA;
                ShapeSlot shapeB = contact.shapeB;
                if (!shapeA.alive || !shapeB.alive || !b2AABB_Overlaps(shapeA.fatAABB, shapeB.fatAABB)) {
                    contactState[contact.index] = 1;
                    continue;
                }

                boolean wasTouching = contact.touching;
                b2Manifold oldManifold = contact.manifold;
                contact.manifold = b2ComputeContactManifold(shapeA, shapeB, oldManifold);
                contact.touching = contact.manifold.pointCount > 0;
                if (contact.touching && world.preSolveFcn != null
                    && (shapeA.def.enablePreSolveEvents || shapeB.def.enablePreSolveEvents)) {
                    contact.touching = world.preSolveFcn.invoke(shapeId(world, shapeA), shapeId(world, shapeB),
                        contact.manifold, world.preSolveContext);
                    if (!contact.touching) {
                        contact.manifold.pointCount = 0;
                    }
                }
                if (contact.touching && !wasTouching) {
                    contactState[contact.index] = 2;
                } else if (!contact.touching && wasTouching) {
                    contactState[contact.index] = 3;
                }
            }
        }, null);

        for (int contactIndex = 0; contactIndex < contactState.length; ++contactIndex) {
            byte state = contactState[contactIndex];
            if (state == 0) {
                continue;
            }
            ContactSlot contact = world.contacts.get(contactIndex);
            if (contact == null || !contact.alive) {
                continue;
            }
            if (state == 1) {
                if (contact.touching) {
                    endEvents.add(b2MakeContactEndTouchEvent(world, contact));
                }
                b2DestroyContact(world, contact);
            } else if (state == 2) {
                if (contact.touching) {
                    b2WakeTouchingContactBodies(world, contact);
                    b2AddContactToGraph(world, contact);
                    b2LinkSleepContact(world, contact);
                    beginEvents.add(b2MakeContactBeginTouchEvent(world, contact));
                }
            } else if (state == 3) {
                if (!contact.touching) {
                    b2UnlinkSleepContact(world, contact);
                    b2RemoveContactFromGraph(world, contact);
                    endEvents.add(b2MakeContactEndTouchEvent(world, contact));
                }
            }
        }

        b2ContactEvents events = new b2ContactEvents();
        events.beginEvents = beginEvents.toArray(new b2ContactBeginTouchEvent[0]);
        events.endEvents = endEvents.toArray(new b2ContactEndTouchEvent[0]);
        events.hitEvents = new b2ContactHitEvent[0];
        events.beginCount = beginEvents.size();
        events.endCount = endEvents.size();
        events.hitCount = 0;
        world.contactEvents = events;
    }

    private static boolean b2ContactHasAwakeDynamicBody(ContactSlot contact) {
        BodySlot bodyA = contact.shapeA.body;
        BodySlot bodyB = contact.shapeB.body;
        return (bodyA.type == b2_dynamicBody && bodyA.awake) || (bodyB.type == b2_dynamicBody && bodyB.awake);
    }

    private static void b2WakeTouchingContactBodies(WorldSlot world, ContactSlot contact) {
        BodySlot bodyA = contact.shapeA.body;
        BodySlot bodyB = contact.shapeB.body;
        if (bodyA.type == b2_dynamicBody && bodyA.awake && bodyA.enableSleep && !bodyB.awake) {
            b2WakeBodySleepIsland(world, bodyB);
        }
        if (bodyB.type == b2_dynamicBody && bodyB.awake && bodyB.enableSleep && !bodyA.awake) {
            b2WakeBodySleepIsland(world, bodyA);
        }
    }

    private static b2ContactData b2MakeContactData(WorldSlot world, ContactSlot contact) {
        b2ContactData data = new b2ContactData();
        data.shapeIdA = shapeId(world, contact.shapeA);
        data.shapeIdB = shapeId(world, contact.shapeB);
        data.manifold = b2CopyManifold(contact.manifold);
        return data;
    }

    @SuppressWarnings("unchecked")
    private static java.util.ArrayList<ContactSlot>[] b2CreateContactGraphColors() {
        java.util.ArrayList<ContactSlot>[] colors = new java.util.ArrayList[B2_GRAPH_COLOR_COUNT];
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = new java.util.ArrayList<>();
        }
        return colors;
    }

    @SuppressWarnings("unchecked")
    private static java.util.ArrayList<JointSlot>[] b2CreateJointGraphColors() {
        java.util.ArrayList<JointSlot>[] colors = new java.util.ArrayList[B2_GRAPH_COLOR_COUNT];
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = new java.util.ArrayList<>();
        }
        return colors;
    }

    @SuppressWarnings("unchecked")
    private static java.util.HashSet<Integer>[] b2CreateGraphBodySets() {
        java.util.HashSet<Integer>[] bodySets = new java.util.HashSet[B2_OVERFLOW_INDEX];
        for (int i = 0; i < bodySets.length; ++i) {
            bodySets[i] = new java.util.HashSet<>();
        }
        return bodySets;
    }

    private static int b2AssignGraphColor(WorldSlot world, BodySlot bodyA, BodySlot bodyB, boolean skipColorZeroForStatic) {
        boolean staticA = bodyA.type == b2_staticBody;
        boolean staticB = bodyB.type == b2_staticBody;
        int firstColor = skipColorZeroForStatic && (staticA || staticB) ? 1 : 0;
        for (int i = firstColor; i < B2_OVERFLOW_INDEX; ++i) {
            java.util.HashSet<Integer> bodySet = world.graphBodySets[i];
            if ((!staticA && bodySet.contains(bodyA.index)) || (!staticB && bodySet.contains(bodyB.index))) {
                continue;
            }
            if (!staticA) {
                bodySet.add(bodyA.index);
            }
            if (!staticB) {
                bodySet.add(bodyB.index);
            }
            return i;
        }
        return B2_OVERFLOW_INDEX;
    }

    private static void b2ClearGraphColorBodies(WorldSlot world, int colorIndex, BodySlot bodyA, BodySlot bodyB) {
        if (colorIndex < 0 || colorIndex >= B2_OVERFLOW_INDEX) {
            return;
        }
        if (bodyA.type != b2_staticBody) {
            world.graphBodySets[colorIndex].remove(bodyA.index);
        }
        if (bodyB.type != b2_staticBody) {
            world.graphBodySets[colorIndex].remove(bodyB.index);
        }
    }

    private static void b2AddContactToGraph(WorldSlot world, ContactSlot contact) {
        int colorIndex = b2AssignGraphColor(world, contact.shapeA.body, contact.shapeB.body, true);
        java.util.ArrayList<ContactSlot> color = world.contactGraphColors[colorIndex];
        contact.colorIndex = colorIndex;
        contact.localIndex = color.size();
        color.add(contact);
    }

    private static void b2RemoveContactFromGraph(WorldSlot world, ContactSlot contact) {
        if (contact.colorIndex == B2_NULL_INDEX) {
            return;
        }
        int colorIndex = contact.colorIndex;
        int localIndex = contact.localIndex;
        java.util.ArrayList<ContactSlot> color = world.contactGraphColors[colorIndex];
        ContactSlot moved = color.remove(color.size() - 1);
        if (localIndex < color.size()) {
            color.set(localIndex, moved);
            moved.localIndex = localIndex;
        }
        b2ClearGraphColorBodies(world, colorIndex, contact.shapeA.body, contact.shapeB.body);
        contact.colorIndex = B2_NULL_INDEX;
        contact.localIndex = B2_NULL_INDEX;
    }

    private static void b2AddJointToGraph(WorldSlot world, JointSlot joint) {
        int colorIndex = b2AssignGraphColor(world, joint.bodyA, joint.bodyB, false);
        java.util.ArrayList<JointSlot> color = world.jointGraphColors[colorIndex];
        joint.colorIndex = colorIndex;
        joint.localIndex = color.size();
        color.add(joint);
    }

    private static void b2RemoveJointFromGraph(WorldSlot world, JointSlot joint) {
        if (joint.colorIndex == B2_NULL_INDEX) {
            return;
        }
        int colorIndex = joint.colorIndex;
        int localIndex = joint.localIndex;
        java.util.ArrayList<JointSlot> color = world.jointGraphColors[colorIndex];
        JointSlot moved = color.remove(color.size() - 1);
        if (localIndex < color.size()) {
            color.set(localIndex, moved);
            moved.localIndex = localIndex;
        }
        b2ClearGraphColorBodies(world, colorIndex, joint.bodyA, joint.bodyB);
        joint.colorIndex = B2_NULL_INDEX;
        joint.localIndex = B2_NULL_INDEX;
    }

    private static b2ContactBeginTouchEvent b2MakeContactBeginTouchEvent(WorldSlot world, ContactSlot contact) {
        b2ContactBeginTouchEvent event = new b2ContactBeginTouchEvent();
        event.shapeIdA = shapeId(world, contact.shapeA);
        event.shapeIdB = shapeId(world, contact.shapeB);
        event.manifold = b2CopyManifold(contact.manifold);
        return event;
    }

    private static b2ContactEndTouchEvent b2MakeContactEndTouchEvent(WorldSlot world, ContactSlot contact) {
        b2ContactEndTouchEvent event = new b2ContactEndTouchEvent();
        event.shapeIdA = shapeId(world, contact.shapeA);
        event.shapeIdB = shapeId(world, contact.shapeB);
        return event;
    }

    private static b2Manifold b2CopyManifold(b2Manifold src) {
        b2Manifold dst = new b2Manifold();
        dst.normal = src.normal.copy();
        dst.pointCount = src.pointCount;
        for (int i = 0; i < src.points.length; ++i) {
            dst.points[i].anchorA = src.points[i].anchorA.copy();
            dst.points[i].anchorB = src.points[i].anchorB.copy();
            dst.points[i].point = src.points[i].point.copy();
            dst.points[i].separation = src.points[i].separation;
            dst.points[i].normalImpulse = src.points[i].normalImpulse;
            dst.points[i].tangentImpulse = src.points[i].tangentImpulse;
            dst.points[i].totalNormalImpulse = src.points[i].totalNormalImpulse;
            dst.points[i].normalVelocity = src.points[i].normalVelocity;
            dst.points[i].id = src.points[i].id;
            dst.points[i].persisted = src.points[i].persisted;
        }
        return dst;
    }

    private static b2Manifold b2ComputeContactManifold(ShapeSlot shapeA, ShapeSlot shapeB, b2Manifold oldManifold) {
        b2Manifold manifold = b2ComputeManifold(shapeA, shapeB);
        if (manifold.pointCount > 0) {
            manifold.rollingImpulse = oldManifold.rollingImpulse;
        }

        b2Vec2 centerOffsetA = b2RotateVector(shapeA.body.rotation, shapeA.body.localCenter);
        b2Vec2 centerOffsetB = b2RotateVector(shapeB.body.rotation, shapeB.body.localCenter);
        for (int i = 0; i < manifold.pointCount; ++i) {
            b2ManifoldPoint mp2 = manifold.points[i];
            mp2.anchorA = b2Sub(mp2.anchorA, centerOffsetA);
            mp2.anchorB = b2Sub(mp2.anchorB, centerOffsetB);
            mp2.normalImpulse = 0.0f;
            mp2.tangentImpulse = 0.0f;
            mp2.totalNormalImpulse = 0.0f;
            mp2.normalVelocity = 0.0f;
            mp2.persisted = false;
            int id2 = mp2.id;
            for (int j = 0; j < oldManifold.pointCount; ++j) {
                b2ManifoldPoint mp1 = oldManifold.points[j];
                if (mp1.id == id2) {
                    mp2.normalImpulse = mp1.normalImpulse;
                    mp2.tangentImpulse = mp1.tangentImpulse;
                    mp2.persisted = true;
                    break;
                }
            }
        }
        return manifold;
    }

    private static b2Manifold b2ComputeManifold(ShapeSlot shapeA, ShapeSlot shapeB) {
        b2Transform xfA = new b2Transform(shapeA.body.position, shapeA.body.rotation);
        b2Transform xfB = new b2Transform(shapeB.body.position, shapeB.body.rotation);
        if (shapeA.kind == 1 && shapeB.kind == 1) {
            return b2CollidePolygons(shapeA.polygon, xfA, shapeB.polygon, xfB);
        }
        if (shapeA.kind == 1 && shapeB.kind == 2) {
            return b2CollidePolygonAndCircle(shapeA.polygon, xfA, shapeB.circle, xfB);
        }
        if (shapeA.kind == 2 && shapeB.kind == 1) {
            return b2FlipManifold(b2CollidePolygonAndCircle(shapeB.polygon, xfB, shapeA.circle, xfA));
        }
        if (shapeA.kind == 2 && shapeB.kind == 2) {
            return b2CollideCircles(shapeA.circle, xfA, shapeB.circle, xfB);
        }
        if (shapeA.kind == 4 && shapeB.kind == 2) {
            return b2CollideCapsuleAndCircle(shapeA.capsule, xfA, shapeB.circle, xfB);
        }
        if (shapeA.kind == 2 && shapeB.kind == 4) {
            return b2FlipManifold(b2CollideCapsuleAndCircle(shapeB.capsule, xfB, shapeA.circle, xfA));
        }
        if (shapeA.kind == 4 && shapeB.kind == 4) {
            return b2CollideCapsules(shapeA.capsule, xfA, shapeB.capsule, xfB);
        }
        if (shapeA.kind == 1 && shapeB.kind == 4) {
            return b2CollidePolygonAndCapsule(shapeA.polygon, xfA, shapeB.capsule, xfB);
        }
        if (shapeA.kind == 4 && shapeB.kind == 1) {
            return b2FlipManifold(b2CollidePolygonAndCapsule(shapeB.polygon, xfB, shapeA.capsule, xfA));
        }
        if (shapeA.kind == 3 && shapeB.kind == 1) {
            return b2CollideSegmentAndPolygon(shapeA.segment, xfA, shapeB.polygon, xfB);
        }
        if (shapeA.kind == 1 && shapeB.kind == 3) {
            return b2FlipManifold(b2CollideSegmentAndPolygon(shapeB.segment, xfB, shapeA.polygon, xfA));
        }
        if (shapeA.kind == 3 && shapeB.kind == 2) {
            return b2CollideSegmentAndCircle(shapeA.segment, xfA, shapeB.circle, xfB);
        }
        if (shapeA.kind == 2 && shapeB.kind == 3) {
            return b2FlipManifold(b2CollideSegmentAndCircle(shapeB.segment, xfB, shapeA.circle, xfA));
        }
        if (shapeA.kind == 3 && shapeB.kind == 4) {
            return b2CollideSegmentAndCapsule(shapeA.segment, xfA, shapeB.capsule, xfB);
        }
        if (shapeA.kind == 4 && shapeB.kind == 3) {
            return b2FlipManifold(b2CollideSegmentAndCapsule(shapeB.segment, xfB, shapeA.capsule, xfA));
        }
        if (shapeA.kind == 5 && shapeB.kind == 2) {
            return b2CollideChainSegmentAndCircle(shapeA.chainSegment, xfA, shapeB.circle, xfB);
        }
        if (shapeA.kind == 2 && shapeB.kind == 5) {
            return b2FlipManifold(b2CollideChainSegmentAndCircle(shapeB.chainSegment, xfB, shapeA.circle, xfA));
        }
        if (shapeA.kind == 5 && shapeB.kind == 4) {
            return b2CollideChainSegmentAndCapsule(shapeA.chainSegment, xfA, shapeB.capsule, xfB, new b2SimplexCache());
        }
        if (shapeA.kind == 4 && shapeB.kind == 5) {
            return b2FlipManifold(b2CollideChainSegmentAndCapsule(shapeB.chainSegment, xfB, shapeA.capsule, xfA, new b2SimplexCache()));
        }
        if (shapeA.kind == 5 && shapeB.kind == 1) {
            return b2CollideChainSegmentAndPolygon(shapeA.chainSegment, xfA, shapeB.polygon, xfB, new b2SimplexCache());
        }
        if (shapeA.kind == 1 && shapeB.kind == 5) {
            return b2FlipManifold(b2CollideChainSegmentAndPolygon(shapeB.chainSegment, xfB, shapeA.polygon, xfA, new b2SimplexCache()));
        }
        return new b2Manifold();
    }

    private static b2Manifold b2FlipManifold(b2Manifold manifold) {
        manifold.normal = b2Neg(manifold.normal);
        for (int i = 0; i < manifold.pointCount; ++i) {
            b2ManifoldPoint mp = manifold.points[i];
            b2Vec2 anchorA = mp.anchorA;
            mp.anchorA = mp.anchorB;
            mp.anchorB = anchorA;
            mp.id = ((mp.id & 0xFF) << 8) | ((mp.id >>> 8) & 0xFF);
        }
        return manifold;
    }

    private static void b2RebuildContactPairSet(WorldSlot world) {
        b2DestroySet(world.broadPhase.pairSet);
        world.broadPhase.pairSet = b2CreateSet(32);
        for (ContactSlot contact : world.contacts) {
            if (contact != null && contact.alive) {
                b2AddKey(world.broadPhase.pairSet, B2_SHAPE_PAIR_KEY(contact.shapeA.index + 1, contact.shapeB.index + 1));
            }
        }
    }

    private static int b2CreateSleepIsland(WorldSlot world) {
        int islandId = world.nextSleepIslandId++;
        world.sleepIslandParents.put(islandId, islandId);
        world.sleepIslandContacts.put(islandId, new java.util.ArrayList<>());
        world.sleepIslandJoints.put(islandId, new java.util.ArrayList<>());
        return islandId;
    }

    private static int b2FindSleepIsland(WorldSlot world, int islandId) {
        if (islandId == B2_NULL_INDEX) {
            return B2_NULL_INDEX;
        }
        Integer parent = world.sleepIslandParents.get(islandId);
        if (parent == null) {
            world.sleepIslandParents.put(islandId, islandId);
            return islandId;
        }
        int root = islandId;
        while (true) {
            Integer next = world.sleepIslandParents.get(root);
            if (next == null) {
                world.sleepIslandParents.put(root, root);
                break;
            }
            if (next == root) {
                break;
            }
            root = next;
        }
        int index = islandId;
        while (index != root) {
            int next = world.sleepIslandParents.get(index);
            world.sleepIslandParents.put(index, root);
            index = next;
        }
        return root;
    }

    private static void b2EnsureBodySleepIsland(WorldSlot world, BodySlot body) {
        if (body == null || !body.alive || !body.enabled || body.type == b2_staticBody) {
            return;
        }
        if (body.sleepIslandId == B2_NULL_INDEX || !world.sleepIslandParents.containsKey(body.sleepIslandId)) {
            body.sleepIslandId = b2CreateSleepIsland(world);
        }
    }

    private static int b2GetBodySleepIsland(WorldSlot world, BodySlot body) {
        b2EnsureBodySleepIsland(world, body);
        return body == null ? B2_NULL_INDEX : b2FindSleepIsland(world, body.sleepIslandId);
    }

    private static int b2UnionSleepIslands(WorldSlot world, int islandIdA, int islandIdB) {
        int rootA = b2FindSleepIsland(world, islandIdA);
        int rootB = b2FindSleepIsland(world, islandIdB);
        if (rootA == B2_NULL_INDEX) {
            return rootB;
        }
        if (rootB == B2_NULL_INDEX || rootA == rootB) {
            return rootA;
        }
        world.sleepIslandParents.put(rootB, rootA);
        world.sleepIslandContacts.computeIfAbsent(rootA, ignored -> new java.util.ArrayList<>())
            .addAll(world.sleepIslandContacts.getOrDefault(rootB, new java.util.ArrayList<>()));
        world.sleepIslandJoints.computeIfAbsent(rootA, ignored -> new java.util.ArrayList<>())
            .addAll(world.sleepIslandJoints.getOrDefault(rootB, new java.util.ArrayList<>()));
        world.sleepIslandContacts.remove(rootB);
        world.sleepIslandJoints.remove(rootB);
        if (world.sleepingIslandRoots.remove(rootB)) {
            world.sleepingIslandRoots.add(rootA);
        }
        if (world.sleepIslandConstraintRemoved.remove(rootB)) {
            world.sleepIslandConstraintRemoved.add(rootA);
        }
        if (world.splitSleepIslandId == rootB) {
            world.splitSleepIslandId = rootA;
        }
        return rootA;
    }

    private static int b2UnionBodySleepIslands(WorldSlot world, BodySlot bodyA, BodySlot bodyB) {
        int islandA = b2GetBodySleepIsland(world, bodyA);
        int islandB = b2GetBodySleepIsland(world, bodyB);
        return b2UnionSleepIslands(world, islandA, islandB);
    }

    private static void b2WakeBodySleepIsland(WorldSlot world, BodySlot body) {
        if (body == null || body.awake) {
            return;
        }
        int root = b2GetBodySleepIsland(world, body);
        for (BodySlot candidate : world.bodies) {
            if (candidate == null || !candidate.alive || !candidate.enabled || candidate.type == b2_staticBody) {
                continue;
            }
            if (b2FindSleepIsland(world, candidate.sleepIslandId) == root) {
                candidate.awake = true;
                candidate.sleepTime = 0.0f;
            }
        }
        if (world.sleepingIslandRoots.remove(root)) {
            java.util.ArrayList<ContactSlot> contacts = world.sleepIslandContacts.get(root);
            if (contacts != null) {
                for (ContactSlot contact : contacts) {
                    if (contact.alive && contact.touching && contact.colorIndex == B2_NULL_INDEX) {
                        b2AddContactToGraph(world, contact);
                    }
                }
            }
            java.util.ArrayList<JointSlot> joints = world.sleepIslandJoints.get(root);
            if (joints != null) {
                for (JointSlot joint : joints) {
                    if (joint.alive && joint.colorIndex == B2_NULL_INDEX) {
                        b2AddJointToGraph(world, joint);
                    }
                }
            }
        }
    }

    private static void b2SleepIslandGraph(WorldSlot world, int islandId) {
        int root = b2FindSleepIsland(world, islandId);
        if (root == B2_NULL_INDEX || !world.sleepingIslandRoots.add(root)) {
            return;
        }
        java.util.ArrayList<ContactSlot> contacts = world.sleepIslandContacts.get(root);
        if (contacts != null) {
            for (ContactSlot contact : contacts) {
                if (contact.alive && contact.touching && contact.colorIndex != B2_NULL_INDEX) {
                    b2RemoveContactFromGraph(world, contact);
                }
            }
        }
        java.util.ArrayList<JointSlot> joints = world.sleepIslandJoints.get(root);
        if (joints != null) {
            for (JointSlot joint : joints) {
                if (joint.alive && joint.colorIndex != B2_NULL_INDEX) {
                    b2RemoveJointFromGraph(world, joint);
                }
            }
        }
    }

    private static void b2ForceSleepIsland(WorldSlot world, BodySlot body) {
        int root = b2GetBodySleepIsland(world, body);
        for (BodySlot candidate : world.bodies) {
            if (candidate == null || !candidate.alive || !candidate.enabled || candidate.type == b2_staticBody) {
                continue;
            }
            if (b2FindSleepIsland(world, candidate.sleepIslandId) == root) {
                candidate.awake = false;
                candidate.sleepTime = B2_TIME_TO_SLEEP();
                candidate.linearVelocity = b2Vec2_zero.copy();
                candidate.angularVelocity = 0.0f;
            }
        }
        b2SleepIslandGraph(world, root);
    }

    private static void b2MarkSleepIslandConstraintRemoved(WorldSlot world, int islandId) {
        int root = b2FindSleepIsland(world, islandId);
        if (root != B2_NULL_INDEX) {
            world.sleepIslandConstraintRemoved.add(root);
        }
    }

    private static void b2LinkSleepContact(WorldSlot world, ContactSlot contact) {
        if (contact == null || !contact.alive || !contact.touching
            || contact.shapeA.def.isSensor || contact.shapeB.def.isSensor) {
            return;
        }
        int root = b2UnionBodySleepIslands(world, contact.shapeA.body, contact.shapeB.body);
        contact.sleepIslandId = root;
        world.sleepIslandContacts.computeIfAbsent(root, ignored -> new java.util.ArrayList<>()).add(0, contact);
    }

    private static void b2UnlinkSleepContact(WorldSlot world, ContactSlot contact) {
        if (contact == null || contact.sleepIslandId == B2_NULL_INDEX) {
            return;
        }
        b2MarkSleepIslandConstraintRemoved(world, contact.sleepIslandId);
        int root = b2FindSleepIsland(world, contact.sleepIslandId);
        java.util.ArrayList<ContactSlot> contacts = world.sleepIslandContacts.get(root);
        if (contacts != null) {
            contacts.remove(contact);
        }
        contact.sleepIslandId = B2_NULL_INDEX;
    }

    private static void b2LinkSleepJoint(WorldSlot world, JointSlot joint) {
        if (joint == null || !joint.alive) {
            return;
        }
        int root = b2UnionBodySleepIslands(world, joint.bodyA, joint.bodyB);
        joint.sleepIslandId = root;
        world.sleepIslandJoints.computeIfAbsent(root, ignored -> new java.util.ArrayList<>()).add(0, joint);
    }

    private static void b2UnlinkSleepJoint(WorldSlot world, JointSlot joint) {
        if (joint == null || joint.sleepIslandId == B2_NULL_INDEX) {
            return;
        }
        b2MarkSleepIslandConstraintRemoved(world, joint.sleepIslandId);
        int root = b2FindSleepIsland(world, joint.sleepIslandId);
        java.util.ArrayList<JointSlot> joints = world.sleepIslandJoints.get(root);
        if (joints != null) {
            joints.remove(joint);
        }
        joint.sleepIslandId = B2_NULL_INDEX;
    }

    private static void b2SplitSleepIsland(WorldSlot world) {
        int splitIslandId = world.splitSleepIslandId;
        world.splitSleepIslandId = B2_NULL_INDEX;
        int splitRoot = b2FindSleepIsland(world, splitIslandId);
        if (splitRoot == B2_NULL_INDEX || !world.sleepIslandConstraintRemoved.remove(splitRoot)) {
            return;
        }

        java.util.HashSet<BodySlot> splitBodies = new java.util.HashSet<>();
        for (BodySlot body : world.bodies) {
            if (body == null || !body.alive || !body.enabled || body.type == b2_staticBody) {
                continue;
            }
            if (b2FindSleepIsland(world, body.sleepIslandId) == splitRoot) {
                splitBodies.add(body);
                body.sleepIslandId = b2CreateSleepIsland(world);
            }
        }
        if (splitBodies.isEmpty()) {
            world.sleepIslandParents.remove(splitRoot);
            world.sleepIslandContacts.remove(splitRoot);
            world.sleepIslandJoints.remove(splitRoot);
            world.sleepingIslandRoots.remove(splitRoot);
            return;
        }

        world.sleepIslandContacts.remove(splitRoot);
        world.sleepIslandJoints.remove(splitRoot);
        world.sleepingIslandRoots.remove(splitRoot);

        for (ContactSlot contact : world.contacts) {
            if (contact != null && b2FindSleepIsland(world, contact.sleepIslandId) == splitRoot) {
                contact.sleepIslandId = B2_NULL_INDEX;
            }
        }
        for (JointSlot joint : world.joints) {
            if (joint != null && b2FindSleepIsland(world, joint.sleepIslandId) == splitRoot) {
                joint.sleepIslandId = B2_NULL_INDEX;
            }
        }

        for (ContactSlot contact : world.contacts) {
            if (contact == null || !contact.alive || !contact.touching
                || contact.shapeA.def.isSensor || contact.shapeB.def.isSensor) {
                continue;
            }
            if (splitBodies.contains(contact.shapeA.body) || splitBodies.contains(contact.shapeB.body)) {
                b2LinkSleepContact(world, contact);
            }
        }
        for (JointSlot joint : world.joints) {
            if (joint == null || !joint.alive) {
                continue;
            }
            if (splitBodies.contains(joint.bodyA) || splitBodies.contains(joint.bodyB)) {
                b2LinkSleepJoint(world, joint);
            }
        }
        world.sleepIslandParents.remove(splitRoot);
    }

    private static boolean b2SolveWorldContacts(WorldSlot world, float timeStep, int subStepCount) {
        java.util.ArrayList<SolverBodyState> states = new java.util.ArrayList<>();
        java.util.IdentityHashMap<BodySlot, Integer> stateIndices = new java.util.IdentityHashMap<>();
        for (BodySlot body : world.solverBodyOrder) {
            if (body == null || !body.alive || !body.enabled || !body.awake || body.type != b2_dynamicBody) {
                continue;
            }
            SolverBodyState state = new SolverBodyState();
            state.body = body;
            state.linearVelocity = body.linearVelocity.copy();
            state.angularVelocity = body.angularVelocity;
            stateIndices.put(body, states.size());
            states.add(state);
        }
        if (states.isEmpty()) {
            return false;
        }

        java.util.ArrayList<ContactConstraint> constraints = new java.util.ArrayList<>();
        ConstraintColor[] colors = b2CreateConstraintColors();
        Softness contactSoftness = b2MakeSoftness(b2MinFloat(world.def.contactHertz, 0.125f * subStepCount / timeStep),
            world.def.contactDampingRatio, timeStep / subStepCount);
        Softness staticSoftness = b2MakeSoftness(2.0f * b2MinFloat(world.def.contactHertz, 0.125f * subStepCount / timeStep),
            world.def.contactDampingRatio, timeStep / subStepCount);
        float contactSpeed = world.def.maxContactPushSpeed / staticSoftness.massScale;
        float h = timeStep / subStepCount;
        float invH = subStepCount / timeStep;
        for (int colorIndex = 0; colorIndex < B2_GRAPH_COLOR_COUNT; ++colorIndex) {
            java.util.ArrayList<JointSlot> colorJoints = world.jointGraphColors[colorIndex];
            Object[] preparedJoints = new Object[colorJoints.size()];
            b2ParallelFor(world, colorJoints.size(), 4, (startIndex, endIndex, workerIndex, taskContext) -> {
                for (int jointIndex = startIndex; jointIndex < endIndex; ++jointIndex) {
                    preparedJoints[jointIndex] = b2PrepareJointConstraint(colorJoints.get(jointIndex), stateIndices,
                        h, invH, world.warmStartingEnabled);
                }
            }, null);
            for (Object preparedJoint : preparedJoints) {
                b2AddPreparedJointConstraint(colors[colorIndex], preparedJoint);
            }

            java.util.ArrayList<ContactSlot> colorContacts = world.contactGraphColors[colorIndex];
            ContactConstraint[] preparedContacts = new ContactConstraint[colorContacts.size()];
            b2ParallelFor(world, colorContacts.size(), 4, (startIndex, endIndex, workerIndex, taskContext) -> {
                for (int contactIndex = startIndex; contactIndex < endIndex; ++contactIndex) {
                    ContactSlot contact = colorContacts.get(contactIndex);
                    if (contact != null && contact.alive && contact.touching
                        && !contact.shapeA.def.isSensor && !contact.shapeB.def.isSensor) {
                        preparedContacts[contactIndex] = b2PrepareContactConstraint(contact, stateIndices,
                            contactSoftness, staticSoftness);
                    }
                }
            }, null);
            for (ContactConstraint constraint : preparedContacts) {
                if (constraint != null) {
                    colors[colorIndex].contacts.add(constraint);
                    constraints.add(constraint);
                }
            }
        }
        float maxLinearSpeed = world.def.maximumLinearSpeed;
        float maxLinearSpeedSquared = maxLinearSpeed * maxLinearSpeed;
        float maxAngularSpeed = B2_MAX_ROTATION() / timeStep;
        float maxAngularSpeedSquared = maxAngularSpeed * maxAngularSpeed;

        for (int i = 0; i < subStepCount; ++i) {
            b2IntegrateVelocities(world, states, h, maxLinearSpeed, maxLinearSpeedSquared,
                maxAngularSpeed, maxAngularSpeedSquared);

            b2WarmStartConstraintColor(colors[B2_OVERFLOW_INDEX], states);
            for (int colorIndex = 0; colorIndex < B2_OVERFLOW_INDEX; ++colorIndex) {
                b2WarmStartConstraintColorParallel(world, colors[colorIndex], states);
            }
            b2SolveConstraintColor(colors[B2_OVERFLOW_INDEX], states, h, invH, world.def.maxContactPushSpeed, true, true);
            for (int colorIndex = 0; colorIndex < B2_OVERFLOW_INDEX; ++colorIndex) {
                b2SolveConstraintColorParallel(world, colors[colorIndex], states, h, invH, contactSpeed, true);
            }

            b2IntegratePositions(world, states, h);

            b2SolveConstraintColor(colors[B2_OVERFLOW_INDEX], states, h, invH, world.def.maxContactPushSpeed, false, true);
            for (int colorIndex = 0; colorIndex < B2_OVERFLOW_INDEX; ++colorIndex) {
                b2SolveConstraintColorParallel(world, colors[colorIndex], states, h, invH, contactSpeed, false);
            }
        }

        for (int colorIndex = 0; colorIndex < B2_OVERFLOW_INDEX; ++colorIndex) {
            b2ParallelList(world, colors[colorIndex].contacts, 4,
                range -> b2ApplyRestitution(range, states, world.def.restitutionThreshold));
        }
        if (!colors[B2_OVERFLOW_INDEX].contacts.isEmpty()) {
            b2ApplyRestitution(colors[B2_OVERFLOW_INDEX].contacts, states, world.def.restitutionThreshold);
        }
        b2ParallelList(world, constraints, 4, B2::b2StoreContactImpulses);
        b2UpdateContactHitEvents(world, constraints);

        boolean[] sleepReady = new boolean[states.size()];
        BulletSweep[] bulletSweepByState = new BulletSweep[states.size()];
        java.util.ArrayList<BulletSweep> bulletSweeps = new java.util.ArrayList<>();
        b2ParallelFor(world, states.size(), 64, (startIndex, endIndex, workerIndex, taskContext) -> {
            for (int stateIndex = startIndex; stateIndex < endIndex; ++stateIndex) {
                SolverBodyState state = states.get(stateIndex);
                BodySlot body = state.body;
                body.linearVelocity = state.linearVelocity;
                body.angularVelocity = state.angularVelocity;
                b2Vec2 center1 = body.center;
                b2Rot rotation1 = body.rotation;
                b2Vec2 center2 = b2Add(body.center, state.deltaPosition);
                b2Rot rotation2 = b2NormalizeRot(b2MulRot(state.deltaRotation, body.rotation));
                float maxVelocity = b2Length(state.linearVelocity) + b2AbsFloat(state.angularVelocity) * body.maxExtent;
                boolean fastNonBullet = world.continuousEnabled && !body.bullet
                    && maxVelocity * timeStep > 0.5f * body.minExtent;
                boolean fastBullet = world.continuousEnabled && body.bullet
                    && maxVelocity * timeStep > 0.5f * body.minExtent;
                boolean didHit = false;
                if (fastNonBullet) {
                    float fraction = b2SolveContinuous(world, body, center1, rotation1, center2, rotation2);
                    if (fraction < 1.0f) {
                        center2 = b2Lerp(center1, center2, fraction);
                        rotation2 = b2NLerp(rotation1, rotation2, fraction);
                        didHit = true;
                    }
                }
                body.center = center2;
                body.rotation = rotation2;
                body.position = b2Sub(body.center, b2RotateVector(body.rotation, body.localCenter));
                if (fastBullet) {
                    BulletSweep sweep = new BulletSweep();
                    sweep.body = body;
                    sweep.center1 = center1;
                    sweep.rotation1 = rotation1;
                    sweep.center2 = center2;
                    sweep.rotation2 = rotation2;
                    bulletSweepByState[stateIndex] = sweep;
                }
                sleepReady[stateIndex] = b2UpdateSleepTimer(world, body, state, timeStep);
                if (fastNonBullet) {
                    b2SynchronizeFastBodyProxies(world, body, didHit, true);
                } else if (!fastBullet) {
                    b2SynchronizeBodyProxies(world, body, true);
                }
                body.force = b2Vec2_zero.copy();
                body.torque = 0.0f;
            }
        }, null);
        for (BulletSweep sweep : bulletSweepByState) {
            if (sweep != null) {
                bulletSweeps.add(sweep);
            }
        }

        float[] bulletFractions = new float[bulletSweeps.size()];
        java.util.Arrays.fill(bulletFractions, 1.0f);
        b2ParallelFor(world, bulletSweeps.size(), 8, (startIndex, endIndex, workerIndex, taskContext) -> {
            for (int bulletIndex = startIndex; bulletIndex < endIndex; ++bulletIndex) {
                BulletSweep sweep = bulletSweeps.get(bulletIndex);
                bulletFractions[bulletIndex] = b2SolveContinuous(world, sweep.body, sweep.center1, sweep.rotation1,
                    sweep.center2, sweep.rotation2);
            }
        }, null);
        for (int bulletIndex = 0; bulletIndex < bulletSweeps.size(); ++bulletIndex) {
            BulletSweep sweep = bulletSweeps.get(bulletIndex);
            float fraction = bulletFractions[bulletIndex];
            boolean didHit = fraction < 1.0f;
            if (didHit) {
                sweep.body.center = b2Lerp(sweep.center1, sweep.center2, fraction);
                sweep.body.rotation = b2NLerp(sweep.rotation1, sweep.rotation2, fraction);
                sweep.body.position = b2Sub(sweep.body.center,
                    b2RotateVector(sweep.body.rotation, sweep.body.localCenter));
            }
            b2SynchronizeFastBodyProxies(world, sweep.body, didHit, true);
        }

        for (SolverBodyState state : states) {
            b2ApplyDeferredProxyEnlargements(world, state.body);
        }

        final int[] sleepParents = new int[states.size()];
        for (int i = 0; i < sleepParents.length; ++i) {
            sleepParents[i] = i;
        }
        class SleepGroups {
            int find(int index) {
                int root = index;
                while (sleepParents[root] != root) {
                    root = sleepParents[root];
                }
                while (sleepParents[index] != index) {
                    int parent = sleepParents[index];
                    sleepParents[index] = root;
                    index = parent;
                }
                return root;
            }

            void union(int indexA, int indexB) {
                int rootA = find(indexA);
                int rootB = find(indexB);
                if (rootA != rootB) {
                    sleepParents[rootB] = rootA;
                }
            }
        }
        SleepGroups sleepGroups = new SleepGroups();
        int[] stateSleepIslandRoots = new int[states.size()];
        java.util.HashMap<Integer, Integer> firstStateForSleepIsland = new java.util.HashMap<>();
        for (int i = 0; i < states.size(); ++i) {
            int root = b2GetBodySleepIsland(world, states.get(i).body);
            stateSleepIslandRoots[i] = root;
            Integer first = firstStateForSleepIsland.putIfAbsent(root, i);
            if (first != null) {
                sleepGroups.union(first, i);
            }
        }
        boolean[] componentSleepReady = new boolean[states.size()];
        boolean[] componentConstraintRemoved = new boolean[states.size()];
        int[] componentSleepIslandRoot = new int[states.size()];
        float[] componentMaxSleepTime = new float[states.size()];
        java.util.Arrays.fill(componentSleepReady, true);
        java.util.Arrays.fill(componentSleepIslandRoot, B2_NULL_INDEX);
        for (int i = 0; i < states.size(); ++i) {
            int root = sleepGroups.find(i);
            if (!sleepReady[i]) {
                componentSleepReady[root] = false;
            }
            int sleepIslandRoot = b2FindSleepIsland(world, stateSleepIslandRoots[i]);
            componentSleepIslandRoot[root] = sleepIslandRoot;
            componentMaxSleepTime[root] = b2MaxFloat(componentMaxSleepTime[root], states.get(i).body.sleepTime);
            if (world.sleepIslandConstraintRemoved.contains(sleepIslandRoot)) {
                componentConstraintRemoved[root] = true;
            }
        }

        int splitCandidate = B2_NULL_INDEX;
        float splitSleepTime = 0.0f;
        for (int i = 0; i < states.size(); ++i) {
            if (!sleepReady[i]) {
                continue;
            }
            int sleepIslandRoot = b2FindSleepIsland(world, stateSleepIslandRoots[i]);
            if (!world.sleepIslandConstraintRemoved.contains(sleepIslandRoot)) {
                continue;
            }
            float bodySleepTime = states.get(i).body.sleepTime;
            if (bodySleepTime > splitSleepTime
                || (bodySleepTime == splitSleepTime && sleepIslandRoot > splitCandidate)) {
                splitCandidate = sleepIslandRoot;
                splitSleepTime = bodySleepTime;
            }
        }
        if (splitCandidate != B2_NULL_INDEX) {
            world.splitSleepIslandId = splitCandidate;
        }

        java.util.ArrayList<b2BodyMoveEvent> moves = new java.util.ArrayList<>();
        java.util.TreeSet<Integer> rootsFallingAsleep = new java.util.TreeSet<>(java.util.Collections.reverseOrder());
        for (int stateIndex = 0; stateIndex < states.size(); ++stateIndex) {
            SolverBodyState state = states.get(stateIndex);
            BodySlot body = state.body;
            int sleepRoot = sleepGroups.find(stateIndex);
            boolean fellAsleep = !componentConstraintRemoved[sleepRoot] && componentSleepReady[sleepRoot];
            if (fellAsleep) {
                body.awake = false;
                body.linearVelocity = b2Vec2_zero.copy();
                body.angularVelocity = 0.0f;
                rootsFallingAsleep.add(componentSleepIslandRoot[sleepRoot]);
            }
            b2BodyMoveEvent event = new b2BodyMoveEvent();
            event.bodyId = bodyId(world, body);
            event.transform = new b2Transform(body.position, body.rotation);
            event.userData = body.userData;
            event.fellAsleep = fellAsleep;
            moves.add(event);
        }
        for (int root : rootsFallingAsleep) {
            b2SleepIslandGraph(world, root);
        }
        world.bodyEvents = new b2BodyEvents();
        world.bodyEvents.moveEvents = moves.toArray(new b2BodyMoveEvent[0]);
        world.bodyEvents.moveCount = world.bodyEvents.moveEvents.length;
        return true;
    }

    private static Object b2PrepareJointConstraint(JointSlot joint,
                                                   java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                   float h, float invH, boolean enableWarmStarting) {
        if (joint == null || !joint.alive) {
            return null;
        }
        if (joint.type == b2_distanceJoint) {
            return b2PrepareDistanceConstraint(joint, stateIndices, h, invH, enableWarmStarting);
        }
        if (joint.type == b2_motorJoint) {
            return b2PrepareMotorConstraint(joint, stateIndices, enableWarmStarting);
        }
        if (joint.type == b2_mouseJoint) {
            return b2PrepareMouseConstraint(joint, stateIndices, h, enableWarmStarting);
        }
        if (joint.type == b2_prismaticJoint) {
            return b2PreparePrismaticConstraint(joint, stateIndices, h, invH, enableWarmStarting);
        }
        if (joint.type == b2_revoluteJoint) {
            return b2PrepareRevoluteConstraint(joint, stateIndices, h, invH, enableWarmStarting);
        }
        if (joint.type == b2_weldJoint) {
            return b2PrepareWeldConstraint(joint, stateIndices, h, invH, enableWarmStarting);
        }
        if (joint.type == b2_wheelJoint) {
            return b2PrepareWheelConstraint(joint, stateIndices, h, invH, enableWarmStarting);
        }
        return null;
    }

    private static void b2AddPreparedJointConstraint(ConstraintColor color, Object constraint) {
        if (constraint instanceof DistanceConstraint) {
            color.distances.add((DistanceConstraint) constraint);
        } else if (constraint instanceof MotorConstraint) {
            color.motors.add((MotorConstraint) constraint);
        } else if (constraint instanceof MouseConstraint) {
            color.mice.add((MouseConstraint) constraint);
        } else if (constraint instanceof PrismaticConstraint) {
            color.prismatics.add((PrismaticConstraint) constraint);
        } else if (constraint instanceof RevoluteConstraint) {
            color.revolutes.add((RevoluteConstraint) constraint);
        } else if (constraint instanceof WeldConstraint) {
            color.welds.add((WeldConstraint) constraint);
        } else if (constraint instanceof WheelConstraint) {
            color.wheels.add((WheelConstraint) constraint);
        }
    }

    private static void b2IntegrateVelocities(WorldSlot world, java.util.ArrayList<SolverBodyState> states,
                                              float h, float maxLinearSpeed, float maxLinearSpeedSquared,
                                              float maxAngularSpeed, float maxAngularSpeedSquared) {
        b2ParallelFor(world, states.size(), 32, (startIndex, endIndex, workerIndex, taskContext) -> {
            for (int stateIndex = startIndex; stateIndex < endIndex; ++stateIndex) {
                SolverBodyState state = states.get(stateIndex);
                BodySlot body = state.body;
                float linearDamping = 1.0f / (1.0f + h * body.linearDamping);
                float angularDamping = 1.0f / (1.0f + h * body.angularDamping);
                float gravityScale = body.invMass > 0.0f ? body.gravityScale : 0.0f;
                b2Vec2 linearVelocityDelta = b2Add(b2MulSV(h * body.invMass, body.force),
                    b2MulSV(h * gravityScale, world.def.gravity));
                float angularVelocityDelta = h * body.invInertia * body.torque;
                state.linearVelocity = b2MulAdd(linearVelocityDelta, linearDamping, state.linearVelocity);
                state.angularVelocity = angularVelocityDelta + angularDamping * state.angularVelocity;
                if (b2Dot(state.linearVelocity, state.linearVelocity) > maxLinearSpeedSquared) {
                    float ratio = maxLinearSpeed / b2Length(state.linearVelocity);
                    state.linearVelocity = b2MulSV(ratio, state.linearVelocity);
                }
                if (state.angularVelocity * state.angularVelocity > maxAngularSpeedSquared && !body.allowFastRotation) {
                    float ratio = maxAngularSpeed / b2AbsFloat(state.angularVelocity);
                    state.angularVelocity *= ratio;
                }
            }
        }, null);
    }

    private static void b2IntegratePositions(WorldSlot world, java.util.ArrayList<SolverBodyState> states, float h) {
        b2ParallelFor(world, states.size(), 32, (startIndex, endIndex, workerIndex, taskContext) -> {
            for (int stateIndex = startIndex; stateIndex < endIndex; ++stateIndex) {
                SolverBodyState state = states.get(stateIndex);
                state.deltaRotation = b2IntegrateRotation(state.deltaRotation, h * state.angularVelocity);
                state.deltaPosition = b2MulAdd(state.deltaPosition, h, state.linearVelocity);
            }
        }, null);
    }

    private static void b2WarmStartConstraintColor(ConstraintColor color, java.util.ArrayList<SolverBodyState> states) {
        if (!color.active()) {
            return;
        }
        b2WarmStartDistanceJoints(color.distances, states);
        b2WarmStartMotorJoints(color.motors, states);
        b2WarmStartMouseJoints(color.mice, states);
        b2WarmStartPrismaticJoints(color.prismatics, states);
        b2WarmStartRevoluteJoints(color.revolutes, states);
        b2WarmStartWeldJoints(color.welds, states);
        b2WarmStartWheelJoints(color.wheels, states);
        b2WarmStartContacts(color.contacts, states);
    }

    private static void b2WarmStartConstraintColorParallel(WorldSlot world, ConstraintColor color,
                                                           java.util.ArrayList<SolverBodyState> states) {
        if (world.workerCount == 1) {
            b2WarmStartConstraintColor(color, states);
            return;
        }
        b2ParallelList(world, color.distances, 4, range -> b2WarmStartDistanceJoints(range, states));
        b2ParallelList(world, color.motors, 4, range -> b2WarmStartMotorJoints(range, states));
        b2ParallelList(world, color.mice, 4, range -> b2WarmStartMouseJoints(range, states));
        b2ParallelList(world, color.prismatics, 4, range -> b2WarmStartPrismaticJoints(range, states));
        b2ParallelList(world, color.revolutes, 4, range -> b2WarmStartRevoluteJoints(range, states));
        b2ParallelList(world, color.welds, 4, range -> b2WarmStartWeldJoints(range, states));
        b2ParallelList(world, color.wheels, 4, range -> b2WarmStartWheelJoints(range, states));
        b2ParallelList(world, color.contacts, 4, range -> b2WarmStartContacts(range, states));
    }

    private static void b2SolveConstraintColor(ConstraintColor color,
                                               java.util.ArrayList<SolverBodyState> states,
                                               float h,
                                               float invH,
                                               float contactSpeed,
                                               boolean useBias,
                                               boolean scalarOverflow) {
        if (!color.active()) {
            return;
        }
        b2SolveDistanceJoints(color.distances, states, h, invH, useBias);
        b2SolveMotorJoints(color.motors, states, h, invH);
        b2SolveMouseJoints(color.mice, states, h);
        b2SolvePrismaticJoints(color.prismatics, states, h, invH, useBias);
        b2SolveRevoluteJoints(color.revolutes, states, h, invH, useBias);
        b2SolveWeldJoints(color.welds, states, useBias);
        b2SolveWheelJoints(color.wheels, states, h, invH, useBias);
        b2SolveContacts(color.contacts, states, invH, contactSpeed, useBias, scalarOverflow);
    }

    private static void b2SolveConstraintColorParallel(WorldSlot world, ConstraintColor color,
                                                       java.util.ArrayList<SolverBodyState> states,
                                                       float h, float invH, float contactSpeed,
                                                       boolean useBias) {
        if (world.workerCount == 1) {
            b2SolveConstraintColor(color, states, h, invH, contactSpeed, useBias, false);
            return;
        }
        b2ParallelList(world, color.distances, 4,
            range -> b2SolveDistanceJoints(range, states, h, invH, useBias));
        b2ParallelList(world, color.motors, 4,
            range -> b2SolveMotorJoints(range, states, h, invH));
        b2ParallelList(world, color.mice, 4,
            range -> b2SolveMouseJoints(range, states, h));
        b2ParallelList(world, color.prismatics, 4,
            range -> b2SolvePrismaticJoints(range, states, h, invH, useBias));
        b2ParallelList(world, color.revolutes, 4,
            range -> b2SolveRevoluteJoints(range, states, h, invH, useBias));
        b2ParallelList(world, color.welds, 4,
            range -> b2SolveWeldJoints(range, states, useBias));
        b2ParallelList(world, color.wheels, 4,
            range -> b2SolveWheelJoints(range, states, h, invH, useBias));
        b2ParallelList(world, color.contacts, 4,
            range -> b2SolveContacts(range, states, invH, contactSpeed, useBias, false));
    }

    private static ConstraintColor[] b2CreateConstraintColors() {
        ConstraintColor[] colors = new ConstraintColor[B2_GRAPH_COLOR_COUNT];
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = new ConstraintColor();
        }
        return colors;
    }

    private static float b2SolveContinuous(WorldSlot world, BodySlot fastBody, b2Vec2 center1, b2Rot rotation1,
                                           b2Vec2 center2, b2Rot rotation2) {
        b2Sweep sweepB = new b2Sweep();
        sweepB.localCenter = fastBody.localCenter.copy();
        sweepB.c1 = center1.copy();
        sweepB.c2 = center2.copy();
        sweepB.q1 = rotation1.copy();
        sweepB.q2 = rotation2.copy();
        b2Transform xf1 = b2GetSweepTransform(sweepB, 0.0f);
        b2Transform xf2 = b2GetSweepTransform(sweepB, 1.0f);
        float fraction = 1.0f;

        for (ShapeSlot fastShape : fastBody.shapes) {
            if (fastShape.def.isSensor) {
                continue;
            }
            b2Vec2 centroid1 = b2TransformPoint(xf1, fastShape.localCentroid);
            b2Vec2 centroid2 = b2TransformPoint(xf2, fastShape.localCentroid);
            b2AABB box2 = b2ComputeShapeAABB(fastShape, xf2);
            b2AABB box = b2AABB_Union(fastShape.aabb, box2);
            fastShape.aabb = box2;
            for (ShapeSlot shape : world.shapes) {
                BodySlot body = shape == null ? null : shape.body;
                if (shape == null || !shape.alive || body == fastBody
                    || (!fastBody.bullet && body.type != b2_staticBody)
                    || (fastBody.bullet && body.bullet)
                    || shape.def.isSensor || !b2AABB_Overlaps(box, shape.fatAABB)
                    || !b2ShouldShapesCollide(shape, fastShape)
                    || !b2ShouldBodiesCollide(body, fastBody)) {
                    continue;
                }
                if (shape.kind == 5 && b2SkipContinuousChainJunction(shape, centroid1, centroid2, fastBody.minExtent)) {
                    continue;
                }

                b2TOIInput input = new b2TOIInput();
                input.proxyA = b2MakeShapeDistanceProxy(shape);
                input.proxyB = b2MakeShapeDistanceProxy(fastShape);
                input.sweepA = b2MakeBodySweep(body, body.center, body.rotation, body.center, body.rotation);
                input.sweepB = sweepB;
                input.maxFraction = fraction;
                b2TOIOutput output = b2TimeOfImpact(input);
                if (0.0f < output.fraction && output.fraction < fraction) {
                    fraction = output.fraction;
                } else if (output.fraction == 0.0f) {
                    b2Vec2 centroid = b2GetShapeCentroid(fastShape);
                    ShapeExtent extent = b2ComputeShapeExtent(fastShape, centroid);
                    input.proxyB = b2MakeProxy(new b2Vec2[] {centroid}, 1, 0.25f * extent.minExtent);
                    output = b2TimeOfImpact(input);
                    if (0.0f < output.fraction && output.fraction < fraction) {
                        fraction = output.fraction;
                    }
                }
            }
        }
        return fraction;
    }

    private static boolean b2SkipContinuousChainJunction(ShapeSlot shape, b2Vec2 centroid1, b2Vec2 centroid2,
                                                         float fastBodyMinExtent) {
        b2Transform transform = new b2Transform(shape.body.position, shape.body.rotation);
        b2Vec2 p1 = b2TransformPoint(transform, shape.chainSegment.segment.point1);
        b2Vec2 p2 = b2TransformPoint(transform, shape.chainSegment.segment.point2);
        b2Vec2 e = b2Sub(p2, p1);
        float[] length = new float[1];
        e = b2GetLengthAndNormalize(length, e);
        if (length[0] > B2_LINEAR_SLOP()) {
            float offset1 = b2Cross(b2Sub(centroid1, p1), e);
            float offset2 = b2Cross(b2Sub(centroid2, p1), e);
            final float allowedFraction = 0.25f;
            return offset1 < 0.0f || offset1 - offset2 < allowedFraction * fastBodyMinExtent;
        }
        return false;
    }

    private static b2Sweep b2MakeBodySweep(BodySlot body, b2Vec2 center1, b2Rot rotation1, b2Vec2 center2, b2Rot rotation2) {
        b2Sweep sweep = new b2Sweep();
        sweep.localCenter = body.localCenter.copy();
        sweep.c1 = center1.copy();
        sweep.c2 = center2.copy();
        sweep.q1 = rotation1.copy();
        sweep.q2 = rotation2.copy();
        return sweep;
    }

    private static DistanceConstraint b2PrepareDistanceConstraint(JointSlot joint,
                                                                  java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                                  float h,
                                                                  float invH,
                                                                  boolean enableWarmStarting) {
        int indexA = stateIndices.getOrDefault(joint.bodyA, B2_NULL_INDEX);
        int indexB = stateIndices.getOrDefault(joint.bodyB, B2_NULL_INDEX);
        if (indexA == B2_NULL_INDEX && indexB == B2_NULL_INDEX) {
            return null;
        }

        DistanceConstraint constraint = new DistanceConstraint();
        constraint.joint = joint;
        constraint.indexA = indexA;
        constraint.indexB = indexB;
        constraint.invMassA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invMass : 0.0f;
        constraint.invMassB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invMass : 0.0f;
        constraint.invIA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invInertia : 0.0f;
        constraint.invIB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invInertia : 0.0f;
        constraint.anchorA = b2RotateVector(joint.bodyA.rotation, b2Sub(joint.localAnchorA, joint.bodyA.localCenter));
        constraint.anchorB = b2RotateVector(joint.bodyB.rotation, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        constraint.deltaCenter = b2Sub(joint.bodyB.center, joint.bodyA.center);

        b2Vec2 separation = b2Add(b2Sub(constraint.anchorB, constraint.anchorA), constraint.deltaCenter);
        b2Vec2 axis = b2Normalize(separation);
        float crA = b2Cross(constraint.anchorA, axis);
        float crB = b2Cross(constraint.anchorB, axis);
        float k = constraint.invMassA + constraint.invMassB + constraint.invIA * crA * crA + constraint.invIB * crB * crB;
        constraint.axialMass = k > 0.0f ? 1.0f / k : 0.0f;
        constraint.distanceSoftness = b2MakeSoftness(joint.distanceHertz, joint.distanceDampingRatio, h);
        float hertz = b2MinFloat(joint.constraintHertz, 0.25f * invH);
        constraint.constraintSoftness = b2MakeSoftness(hertz, joint.constraintDampingRatio, h);

        if (!enableWarmStarting) {
            joint.distanceImpulse = 0.0f;
            joint.distanceLowerImpulse = 0.0f;
            joint.distanceUpperImpulse = 0.0f;
            joint.distanceMotorImpulse = 0.0f;
        }
        return constraint;
    }

    private static void b2WarmStartDistanceJoints(java.util.List<DistanceConstraint> constraints,
                                                  java.util.ArrayList<SolverBodyState> states) {
        for (DistanceConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 ds = b2Add(b2Sub(dpB, dpA), b2Sub(rB, rA));
            b2Vec2 separation = b2Add(constraint.deltaCenter, ds);
            b2Vec2 axis = b2Normalize(separation);
            float axialImpulse = joint.distanceImpulse + joint.distanceLowerImpulse -
                joint.distanceUpperImpulse + joint.distanceMotorImpulse;
            b2Vec2 p = b2MulSV(axialImpulse, axis);

            vA = b2MulSub(vA, constraint.invMassA, p);
            wA -= constraint.invIA * b2Cross(rA, p);
            vB = b2MulAdd(vB, constraint.invMassB, p);
            wB += constraint.invIB * b2Cross(rB, p);

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2SolveDistanceJoints(java.util.List<DistanceConstraint> constraints,
                                              java.util.ArrayList<SolverBodyState> states,
                                              float h,
                                              float invH,
                                              boolean useBias) {
        for (DistanceConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 ds = b2Add(b2Sub(dpB, dpA), b2Sub(rB, rA));
            b2Vec2 separation = b2Add(constraint.deltaCenter, ds);
            float length = b2Length(separation);
            b2Vec2 axis = b2Normalize(separation);

            if (joint.distanceEnableSpring && (joint.distanceMinLength < joint.distanceMaxLength || !joint.distanceEnableLimit)) {
                if (joint.distanceHertz > 0.0f) {
                    b2Vec2 vr = b2Add(b2Sub(vB, vA), b2Sub(b2CrossSV(wB, rB), b2CrossSV(wA, rA)));
                    float cDot = b2Dot(axis, vr);
                    float c = length - joint.distanceLength;
                    float bias = constraint.distanceSoftness.biasRate * c;
                    float mass = constraint.distanceSoftness.massScale * constraint.axialMass;
                    float impulse = -mass * (cDot + bias) - constraint.distanceSoftness.impulseScale * joint.distanceImpulse;
                    joint.distanceImpulse += impulse;

                    b2Vec2 p = b2MulSV(impulse, axis);
                    vA = b2MulSub(vA, constraint.invMassA, p);
                    wA -= constraint.invIA * b2Cross(rA, p);
                    vB = b2MulAdd(vB, constraint.invMassB, p);
                    wB += constraint.invIB * b2Cross(rB, p);
                }

                if (joint.distanceEnableLimit) {
                    b2Vec2 vr = b2Add(b2Sub(vB, vA), b2Sub(b2CrossSV(wB, rB), b2CrossSV(wA, rA)));
                    float cDot = b2Dot(axis, vr);
                    float c = length - joint.distanceMinLength;
                    float bias = 0.0f;
                    float massScale = 1.0f;
                    float impulseScale = 0.0f;
                    if (c > 0.0f) {
                        bias = c * invH;
                    } else if (useBias) {
                        bias = constraint.constraintSoftness.biasRate * c;
                        massScale = constraint.constraintSoftness.massScale;
                        impulseScale = constraint.constraintSoftness.impulseScale;
                    }
                    float impulse = -massScale * constraint.axialMass * (cDot + bias) -
                        impulseScale * joint.distanceLowerImpulse;
                    float newImpulse = b2MaxFloat(0.0f, joint.distanceLowerImpulse + impulse);
                    impulse = newImpulse - joint.distanceLowerImpulse;
                    joint.distanceLowerImpulse = newImpulse;

                    b2Vec2 p = b2MulSV(impulse, axis);
                    vA = b2MulSub(vA, constraint.invMassA, p);
                    wA -= constraint.invIA * b2Cross(rA, p);
                    vB = b2MulAdd(vB, constraint.invMassB, p);
                    wB += constraint.invIB * b2Cross(rB, p);

                    vr = b2Add(b2Sub(vA, vB), b2Sub(b2CrossSV(wA, rA), b2CrossSV(wB, rB)));
                    cDot = b2Dot(axis, vr);
                    c = joint.distanceMaxLength - length;
                    bias = 0.0f;
                    massScale = 1.0f;
                    impulseScale = 0.0f;
                    if (c > 0.0f) {
                        bias = c * invH;
                    } else if (useBias) {
                        bias = constraint.constraintSoftness.biasRate * c;
                        massScale = constraint.constraintSoftness.massScale;
                        impulseScale = constraint.constraintSoftness.impulseScale;
                    }
                    impulse = -massScale * constraint.axialMass * (cDot + bias) -
                        impulseScale * joint.distanceUpperImpulse;
                    newImpulse = b2MaxFloat(0.0f, joint.distanceUpperImpulse + impulse);
                    impulse = newImpulse - joint.distanceUpperImpulse;
                    joint.distanceUpperImpulse = newImpulse;

                    p = b2MulSV(-impulse, axis);
                    vA = b2MulSub(vA, constraint.invMassA, p);
                    wA -= constraint.invIA * b2Cross(rA, p);
                    vB = b2MulAdd(vB, constraint.invMassB, p);
                    wB += constraint.invIB * b2Cross(rB, p);
                }

                if (joint.distanceEnableMotor) {
                    b2Vec2 vr = b2Add(b2Sub(vB, vA), b2Sub(b2CrossSV(wB, rB), b2CrossSV(wA, rA)));
                    float cDot = b2Dot(axis, vr);
                    float impulse = constraint.axialMass * (joint.distanceMotorSpeed - cDot);
                    float oldImpulse = joint.distanceMotorImpulse;
                    float maxImpulse = h * joint.distanceMaxMotorForce;
                    joint.distanceMotorImpulse = b2ClampFloat(joint.distanceMotorImpulse + impulse, -maxImpulse, maxImpulse);
                    impulse = joint.distanceMotorImpulse - oldImpulse;

                    b2Vec2 p = b2MulSV(impulse, axis);
                    vA = b2MulSub(vA, constraint.invMassA, p);
                    wA -= constraint.invIA * b2Cross(rA, p);
                    vB = b2MulAdd(vB, constraint.invMassB, p);
                    wB += constraint.invIB * b2Cross(rB, p);
                }
            } else {
                b2Vec2 vr = b2Add(b2Sub(vB, vA), b2Sub(b2CrossSV(wB, rB), b2CrossSV(wA, rA)));
                float cDot = b2Dot(axis, vr);
                float c = length - joint.distanceLength;
                float bias = 0.0f;
                float massScale = 1.0f;
                float impulseScale = 0.0f;
                if (useBias) {
                    bias = constraint.constraintSoftness.biasRate * c;
                    massScale = constraint.constraintSoftness.massScale;
                    impulseScale = constraint.constraintSoftness.impulseScale;
                }
                float impulse = -massScale * constraint.axialMass * (cDot + bias) -
                    impulseScale * joint.distanceImpulse;
                joint.distanceImpulse += impulse;

                b2Vec2 p = b2MulSV(impulse, axis);
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * b2Cross(rA, p);
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * b2Cross(rB, p);
            }

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static MotorConstraint b2PrepareMotorConstraint(JointSlot joint,
                                                            java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                            boolean enableWarmStarting) {
        int indexA = stateIndices.getOrDefault(joint.bodyA, B2_NULL_INDEX);
        int indexB = stateIndices.getOrDefault(joint.bodyB, B2_NULL_INDEX);
        if (indexA == B2_NULL_INDEX && indexB == B2_NULL_INDEX) {
            return null;
        }

        MotorConstraint constraint = new MotorConstraint();
        constraint.joint = joint;
        constraint.indexA = indexA;
        constraint.indexB = indexB;
        constraint.invMassA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invMass : 0.0f;
        constraint.invMassB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invMass : 0.0f;
        constraint.invIA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invInertia : 0.0f;
        constraint.invIB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invInertia : 0.0f;
        constraint.anchorA = b2RotateVector(joint.bodyA.rotation, b2Sub(joint.localAnchorA, joint.bodyA.localCenter));
        constraint.anchorB = b2RotateVector(joint.bodyB.rotation, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        constraint.deltaCenter = b2Sub(b2Sub(joint.bodyB.center, joint.bodyA.center), joint.motorLinearOffset);
        constraint.deltaAngle = b2RelativeAngle(joint.bodyB.rotation, joint.bodyA.rotation) - joint.motorAngularOffset;

        b2Vec2 rA = constraint.anchorA;
        b2Vec2 rB = constraint.anchorB;
        b2Mat22 k = new b2Mat22();
        k.cx.x = constraint.invMassA + constraint.invMassB + rA.y * rA.y * constraint.invIA + rB.y * rB.y * constraint.invIB;
        k.cx.y = -rA.y * rA.x * constraint.invIA - rB.y * rB.x * constraint.invIB;
        k.cy.x = k.cx.y;
        k.cy.y = constraint.invMassA + constraint.invMassB + rA.x * rA.x * constraint.invIA + rB.x * rB.x * constraint.invIB;
        constraint.linearMass = b2GetInverse22(k);

        float ka = constraint.invIA + constraint.invIB;
        constraint.angularMass = ka > 0.0f ? 1.0f / ka : 0.0f;

        if (!enableWarmStarting) {
            joint.motorLinearImpulse = b2Vec2_zero.copy();
            joint.motorAngularImpulse = 0.0f;
        }
        return constraint;
    }

    private static void b2WarmStartMotorJoints(java.util.List<MotorConstraint> constraints,
                                               java.util.ArrayList<SolverBodyState> states) {
        for (MotorConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);

            vA = b2MulSub(vA, constraint.invMassA, joint.motorLinearImpulse);
            wA -= constraint.invIA * (b2Cross(rA, joint.motorLinearImpulse) + joint.motorAngularImpulse);
            vB = b2MulAdd(vB, constraint.invMassB, joint.motorLinearImpulse);
            wB += constraint.invIB * (b2Cross(rB, joint.motorLinearImpulse) + joint.motorAngularImpulse);

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2SolveMotorJoints(java.util.List<MotorConstraint> constraints,
                                           java.util.ArrayList<SolverBodyState> states,
                                           float h,
                                           float invH) {
        for (MotorConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;

            float angularSeparation = b2UnwindAngle(b2RelativeAngle(dqB, dqA) + constraint.deltaAngle);
            float angularBias = invH * joint.motorCorrectionFactor * angularSeparation;
            float cDot = wB - wA;
            float impulse = -constraint.angularMass * (cDot + angularBias);
            float oldAngularImpulse = joint.motorAngularImpulse;
            float maxAngularImpulse = h * joint.motorMaxTorque;
            joint.motorAngularImpulse = b2ClampFloat(joint.motorAngularImpulse + impulse, -maxAngularImpulse, maxAngularImpulse);
            impulse = joint.motorAngularImpulse - oldAngularImpulse;

            wA -= constraint.invIA * impulse;
            wB += constraint.invIB * impulse;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 ds = b2Add(b2Sub(dpB, dpA), b2Sub(rB, rA));
            b2Vec2 linearSeparation = b2Add(constraint.deltaCenter, ds);
            b2Vec2 linearBias = b2MulSV(invH * joint.motorCorrectionFactor, linearSeparation);
            b2Vec2 linearCDot = b2Sub(b2Add(vB, b2CrossSV(wB, rB)), b2Add(vA, b2CrossSV(wA, rA)));
            b2Vec2 b = b2MulMV(constraint.linearMass, b2Add(linearCDot, linearBias));
            b2Vec2 linearImpulse = new b2Vec2(-b.x, -b.y);

            b2Vec2 oldLinearImpulse = joint.motorLinearImpulse.copy();
            float maxLinearImpulse = h * joint.motorMaxForce;
            joint.motorLinearImpulse = b2Add(joint.motorLinearImpulse, linearImpulse);
            if (b2LengthSquared(joint.motorLinearImpulse) > maxLinearImpulse * maxLinearImpulse) {
                joint.motorLinearImpulse = b2MulSV(maxLinearImpulse, b2Normalize(joint.motorLinearImpulse));
            }
            linearImpulse = b2Sub(joint.motorLinearImpulse, oldLinearImpulse);

            vA = b2MulSub(vA, constraint.invMassA, linearImpulse);
            wA -= constraint.invIA * b2Cross(rA, linearImpulse);
            vB = b2MulAdd(vB, constraint.invMassB, linearImpulse);
            wB += constraint.invIB * b2Cross(rB, linearImpulse);

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static MouseConstraint b2PrepareMouseConstraint(JointSlot joint,
                                                            java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                            float h,
                                                            boolean enableWarmStarting) {
        int indexB = stateIndices.getOrDefault(joint.bodyB, B2_NULL_INDEX);
        if (indexB == B2_NULL_INDEX) {
            return null;
        }

        MouseConstraint constraint = new MouseConstraint();
        constraint.joint = joint;
        constraint.indexB = indexB;
        constraint.invMassB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invMass : 0.0f;
        constraint.invIB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invInertia : 0.0f;
        constraint.anchorB = b2RotateVector(joint.bodyB.rotation, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        constraint.linearSoftness = b2MakeSoftness(joint.mouseHertz, joint.mouseDampingRatio, h);
        constraint.angularSoftness = b2MakeSoftness(0.5f, 0.1f, h);

        b2Vec2 rB = constraint.anchorB;
        b2Mat22 k = new b2Mat22();
        k.cx.x = constraint.invMassB + constraint.invIB * rB.y * rB.y;
        k.cx.y = -constraint.invIB * rB.x * rB.y;
        k.cy.x = k.cx.y;
        k.cy.y = constraint.invMassB + constraint.invIB * rB.x * rB.x;
        constraint.linearMass = b2GetInverse22(k);
        constraint.deltaCenter = b2Sub(joint.bodyB.center, joint.mouseTarget);

        if (!enableWarmStarting) {
            joint.mouseLinearImpulse = b2Vec2_zero.copy();
            joint.mouseAngularImpulse = 0.0f;
        }
        return constraint;
    }

    private static void b2WarmStartMouseJoints(java.util.List<MouseConstraint> constraints,
                                               java.util.ArrayList<SolverBodyState> states) {
        for (MouseConstraint constraint : constraints) {
            SolverBodyState stateB = states.get(constraint.indexB);
            b2Vec2 vB = stateB.linearVelocity;
            float wB = stateB.angularVelocity;
            b2Vec2 rB = b2RotateVector(stateB.deltaRotation, constraint.anchorB);
            JointSlot joint = constraint.joint;

            vB = b2MulAdd(vB, constraint.invMassB, joint.mouseLinearImpulse);
            wB += constraint.invIB * (b2Cross(rB, joint.mouseLinearImpulse) + joint.mouseAngularImpulse);

            stateB.linearVelocity = vB;
            stateB.angularVelocity = wB;
        }
    }

    private static void b2SolveMouseJoints(java.util.List<MouseConstraint> constraints,
                                           java.util.ArrayList<SolverBodyState> states,
                                           float h) {
        for (MouseConstraint constraint : constraints) {
            SolverBodyState stateB = states.get(constraint.indexB);
            b2Vec2 vB = stateB.linearVelocity;
            float wB = stateB.angularVelocity;
            JointSlot joint = constraint.joint;

            float angularImpulse = constraint.invIB > 0.0f ? -wB / constraint.invIB : 0.0f;
            angularImpulse = constraint.angularSoftness.massScale * angularImpulse -
                constraint.angularSoftness.impulseScale * joint.mouseAngularImpulse;
            joint.mouseAngularImpulse += angularImpulse;
            wB += constraint.invIB * angularImpulse;

            float maxImpulse = joint.mouseMaxForce * h;
            b2Vec2 rB = b2RotateVector(stateB.deltaRotation, constraint.anchorB);
            b2Vec2 cDot = b2Add(vB, b2CrossSV(wB, rB));
            b2Vec2 separation = b2Add(b2Add(stateB.deltaPosition, rB), constraint.deltaCenter);
            b2Vec2 bias = b2MulSV(constraint.linearSoftness.biasRate, separation);
            b2Vec2 b = b2MulMV(constraint.linearMass, b2Add(cDot, bias));
            b2Vec2 impulse = new b2Vec2(
                -constraint.linearSoftness.massScale * b.x - constraint.linearSoftness.impulseScale * joint.mouseLinearImpulse.x,
                -constraint.linearSoftness.massScale * b.y - constraint.linearSoftness.impulseScale * joint.mouseLinearImpulse.y);

            b2Vec2 oldImpulse = joint.mouseLinearImpulse.copy();
            joint.mouseLinearImpulse = b2Add(joint.mouseLinearImpulse, impulse);
            float mag = b2Length(joint.mouseLinearImpulse);
            if (mag > maxImpulse) {
                joint.mouseLinearImpulse = b2MulSV(maxImpulse, b2Normalize(joint.mouseLinearImpulse));
            }
            impulse = b2Sub(joint.mouseLinearImpulse, oldImpulse);

            vB = b2MulAdd(vB, constraint.invMassB, impulse);
            wB += constraint.invIB * b2Cross(rB, impulse);

            stateB.linearVelocity = vB;
            stateB.angularVelocity = wB;
        }
    }

    private static WeldConstraint b2PrepareWeldConstraint(JointSlot joint,
                                                          java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                          float h,
                                                          float invH,
                                                          boolean enableWarmStarting) {
        int indexA = stateIndices.getOrDefault(joint.bodyA, B2_NULL_INDEX);
        int indexB = stateIndices.getOrDefault(joint.bodyB, B2_NULL_INDEX);
        if (indexA == B2_NULL_INDEX && indexB == B2_NULL_INDEX) {
            return null;
        }

        WeldConstraint constraint = new WeldConstraint();
        constraint.joint = joint;
        constraint.indexA = indexA;
        constraint.indexB = indexB;
        constraint.invMassA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invMass : 0.0f;
        constraint.invMassB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invMass : 0.0f;
        constraint.invIA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invInertia : 0.0f;
        constraint.invIB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invInertia : 0.0f;
        constraint.anchorA = b2RotateVector(joint.bodyA.rotation, b2Sub(joint.localAnchorA, joint.bodyA.localCenter));
        constraint.anchorB = b2RotateVector(joint.bodyB.rotation, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        constraint.deltaCenter = b2Sub(joint.bodyB.center, joint.bodyA.center);
        constraint.deltaAngle = b2UnwindAngle(b2RelativeAngle(joint.bodyB.rotation, joint.bodyA.rotation) - joint.referenceAngle);
        float ka = constraint.invIA + constraint.invIB;
        constraint.axialMass = ka > 0.0f ? 1.0f / ka : 0.0f;
        float hertz = b2MinFloat(joint.constraintHertz, 0.25f * invH);
        Softness constraintSoftness = b2MakeSoftness(hertz, joint.constraintDampingRatio, h);
        constraint.linearSoftness = joint.weldLinearHertz == 0.0f
            ? constraintSoftness
            : b2MakeSoftness(joint.weldLinearHertz, joint.weldLinearDampingRatio, h);
        constraint.angularSoftness = joint.weldAngularHertz == 0.0f
            ? constraintSoftness
            : b2MakeSoftness(joint.weldAngularHertz, joint.weldAngularDampingRatio, h);

        if (!enableWarmStarting) {
            joint.weldLinearImpulse = b2Vec2_zero.copy();
            joint.weldAngularImpulse = 0.0f;
        }
        return constraint;
    }

    private static void b2WarmStartWeldJoints(java.util.List<WeldConstraint> constraints,
                                              java.util.ArrayList<SolverBodyState> states) {
        for (WeldConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);

            vA = b2MulSub(vA, constraint.invMassA, joint.weldLinearImpulse);
            wA -= constraint.invIA * (b2Cross(rA, joint.weldLinearImpulse) + joint.weldAngularImpulse);
            vB = b2MulAdd(vB, constraint.invMassB, joint.weldLinearImpulse);
            wB += constraint.invIB * (b2Cross(rB, joint.weldLinearImpulse) + joint.weldAngularImpulse);

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2SolveWeldJoints(java.util.List<WeldConstraint> constraints,
                                          java.util.ArrayList<SolverBodyState> states,
                                          boolean useBias) {
        for (WeldConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;

            float bias = 0.0f;
            float massScale = 1.0f;
            float impulseScale = 0.0f;
            if (useBias || joint.weldAngularHertz > 0.0f) {
                float c = b2RelativeAngle(dqB, dqA) + constraint.deltaAngle;
                bias = constraint.angularSoftness.biasRate * c;
                massScale = constraint.angularSoftness.massScale;
                impulseScale = constraint.angularSoftness.impulseScale;
            }

            float cDot = wB - wA;
            float angularImpulse = -massScale * constraint.axialMass * (cDot + bias) -
                impulseScale * joint.weldAngularImpulse;
            joint.weldAngularImpulse += angularImpulse;
            wA -= constraint.invIA * angularImpulse;
            wB += constraint.invIB * angularImpulse;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 linearBias = b2Vec2_zero.copy();
            massScale = 1.0f;
            impulseScale = 0.0f;
            if (useBias || joint.weldLinearHertz > 0.0f) {
                b2Vec2 c = b2Add(b2Add(b2Sub(dpB, dpA), b2Sub(rB, rA)), constraint.deltaCenter);
                linearBias = b2MulSV(constraint.linearSoftness.biasRate, c);
                massScale = constraint.linearSoftness.massScale;
                impulseScale = constraint.linearSoftness.impulseScale;
            }

            b2Vec2 linearCDot = b2Sub(b2Add(vB, b2CrossSV(wB, rB)), b2Add(vA, b2CrossSV(wA, rA)));
            b2Mat22 k = new b2Mat22();
            k.cx.x = constraint.invMassA + constraint.invMassB + rA.y * rA.y * constraint.invIA + rB.y * rB.y * constraint.invIB;
            k.cy.x = -rA.y * rA.x * constraint.invIA - rB.y * rB.x * constraint.invIB;
            k.cx.y = k.cy.x;
            k.cy.y = constraint.invMassA + constraint.invMassB + rA.x * rA.x * constraint.invIA + rB.x * rB.x * constraint.invIB;
            b2Vec2 b = b2Solve22(k, b2Add(linearCDot, linearBias));
            b2Vec2 linearImpulse = new b2Vec2(
                -massScale * b.x - impulseScale * joint.weldLinearImpulse.x,
                -massScale * b.y - impulseScale * joint.weldLinearImpulse.y);

            joint.weldLinearImpulse = b2Add(joint.weldLinearImpulse, linearImpulse);
            vA = b2MulSub(vA, constraint.invMassA, linearImpulse);
            wA -= constraint.invIA * b2Cross(rA, linearImpulse);
            vB = b2MulAdd(vB, constraint.invMassB, linearImpulse);
            wB += constraint.invIB * b2Cross(rB, linearImpulse);

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static PrismaticConstraint b2PreparePrismaticConstraint(JointSlot joint,
                                                                    java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                                    float h,
                                                                    float invH,
                                                                    boolean enableWarmStarting) {
        int indexA = stateIndices.getOrDefault(joint.bodyA, B2_NULL_INDEX);
        int indexB = stateIndices.getOrDefault(joint.bodyB, B2_NULL_INDEX);
        if (indexA == B2_NULL_INDEX && indexB == B2_NULL_INDEX) {
            return null;
        }

        PrismaticConstraint constraint = new PrismaticConstraint();
        constraint.joint = joint;
        constraint.indexA = indexA;
        constraint.indexB = indexB;
        constraint.invMassA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invMass : 0.0f;
        constraint.invMassB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invMass : 0.0f;
        constraint.invIA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invInertia : 0.0f;
        constraint.invIB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invInertia : 0.0f;
        constraint.anchorA = b2RotateVector(joint.bodyA.rotation, b2Sub(joint.localAnchorA, joint.bodyA.localCenter));
        constraint.anchorB = b2RotateVector(joint.bodyB.rotation, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        constraint.axisA = b2RotateVector(joint.bodyA.rotation, joint.localAxisA);
        constraint.deltaCenter = b2Sub(joint.bodyB.center, joint.bodyA.center);
        constraint.deltaAngle = b2UnwindAngle(b2RelativeAngle(joint.bodyB.rotation, joint.bodyA.rotation) - joint.referenceAngle);

        b2Vec2 d = b2Add(constraint.deltaCenter, b2Sub(constraint.anchorB, constraint.anchorA));
        float a1 = b2Cross(b2Add(d, constraint.anchorA), constraint.axisA);
        float a2 = b2Cross(constraint.anchorB, constraint.axisA);
        float k = constraint.invMassA + constraint.invMassB + constraint.invIA * a1 * a1 + constraint.invIB * a2 * a2;
        constraint.axialMass = k > 0.0f ? 1.0f / k : 0.0f;
        constraint.springSoftness = b2MakeSoftness(joint.prismaticHertz, joint.prismaticDampingRatio, h);
        float hertz = b2MinFloat(joint.constraintHertz, 0.25f * invH);
        constraint.constraintSoftness = b2MakeSoftness(hertz, joint.constraintDampingRatio, h);

        if (!enableWarmStarting) {
            joint.prismaticImpulse = b2Vec2_zero.copy();
            joint.prismaticSpringImpulse = 0.0f;
            joint.prismaticMotorImpulse = 0.0f;
            joint.prismaticLowerImpulse = 0.0f;
            joint.prismaticUpperImpulse = 0.0f;
        }
        return constraint;
    }

    private static void b2WarmStartPrismaticJoints(java.util.List<PrismaticConstraint> constraints,
                                                   java.util.ArrayList<SolverBodyState> states) {
        for (PrismaticConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 d = b2Add(b2Add(b2Sub(dpB, dpA), constraint.deltaCenter), b2Sub(rB, rA));
            b2Vec2 axisA = b2RotateVector(dqA, constraint.axisA);
            float a1 = b2Cross(b2Add(d, rA), axisA);
            float a2 = b2Cross(rB, axisA);
            float axialImpulse = joint.prismaticSpringImpulse + joint.prismaticMotorImpulse +
                joint.prismaticLowerImpulse - joint.prismaticUpperImpulse;

            b2Vec2 perpA = b2LeftPerp(axisA);
            float s1 = b2Cross(b2Add(d, rA), perpA);
            float s2 = b2Cross(rB, perpA);
            float perpImpulse = joint.prismaticImpulse.x;
            float angleImpulse = joint.prismaticImpulse.y;

            b2Vec2 p = b2Add(b2MulSV(axialImpulse, axisA), b2MulSV(perpImpulse, perpA));
            float la = axialImpulse * a1 + perpImpulse * s1 + angleImpulse;
            float lb = axialImpulse * a2 + perpImpulse * s2 + angleImpulse;

            vA = b2MulSub(vA, constraint.invMassA, p);
            wA -= constraint.invIA * la;
            vB = b2MulAdd(vB, constraint.invMassB, p);
            wB += constraint.invIB * lb;

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2SolvePrismaticJoints(java.util.List<PrismaticConstraint> constraints,
                                               java.util.ArrayList<SolverBodyState> states,
                                               float h,
                                               float invH,
                                               boolean useBias) {
        for (PrismaticConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 d = b2Add(b2Add(b2Sub(dpB, dpA), constraint.deltaCenter), b2Sub(rB, rA));
            b2Vec2 axisA = b2RotateVector(dqA, constraint.axisA);
            float translation = b2Dot(axisA, d);
            float a1 = b2Cross(b2Add(d, rA), axisA);
            float a2 = b2Cross(rB, axisA);

            if (joint.prismaticEnableSpring) {
                float c = translation - joint.prismaticTargetTranslation;
                float bias = constraint.springSoftness.biasRate * c;
                float cDot = b2Dot(axisA, b2Sub(vB, vA)) + a2 * wB - a1 * wA;
                float impulse = -constraint.springSoftness.massScale * constraint.axialMass * (cDot + bias) -
                    constraint.springSoftness.impulseScale * joint.prismaticSpringImpulse;
                joint.prismaticSpringImpulse += impulse;

                b2Vec2 p = b2MulSV(impulse, axisA);
                float la = impulse * a1;
                float lb = impulse * a2;
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * la;
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * lb;
            }

            if (joint.prismaticEnableMotor) {
                float cDot = b2Dot(axisA, b2Sub(vB, vA)) + a2 * wB - a1 * wA;
                float impulse = constraint.axialMass * (joint.prismaticMotorSpeed - cDot);
                float oldImpulse = joint.prismaticMotorImpulse;
                float maxImpulse = h * joint.prismaticMaxMotorForce;
                joint.prismaticMotorImpulse = b2ClampFloat(joint.prismaticMotorImpulse + impulse, -maxImpulse, maxImpulse);
                impulse = joint.prismaticMotorImpulse - oldImpulse;

                b2Vec2 p = b2MulSV(impulse, axisA);
                float la = impulse * a1;
                float lb = impulse * a2;
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * la;
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * lb;
            }

            if (joint.prismaticEnableLimit) {
                float c = translation - joint.prismaticLowerTranslation;
                float bias = 0.0f;
                float massScale = 1.0f;
                float impulseScale = 0.0f;
                if (c > 0.0f) {
                    bias = c * invH;
                } else if (useBias) {
                    bias = constraint.constraintSoftness.biasRate * c;
                    massScale = constraint.constraintSoftness.massScale;
                    impulseScale = constraint.constraintSoftness.impulseScale;
                }
                float oldImpulse = joint.prismaticLowerImpulse;
                float cDot = b2Dot(axisA, b2Sub(vB, vA)) + a2 * wB - a1 * wA;
                float impulse = -constraint.axialMass * massScale * (cDot + bias) - impulseScale * oldImpulse;
                joint.prismaticLowerImpulse = b2MaxFloat(oldImpulse + impulse, 0.0f);
                impulse = joint.prismaticLowerImpulse - oldImpulse;

                b2Vec2 p = b2MulSV(impulse, axisA);
                float la = impulse * a1;
                float lb = impulse * a2;
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * la;
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * lb;

                c = joint.prismaticUpperTranslation - translation;
                bias = 0.0f;
                massScale = 1.0f;
                impulseScale = 0.0f;
                if (c > 0.0f) {
                    bias = c * invH;
                } else if (useBias) {
                    bias = constraint.constraintSoftness.biasRate * c;
                    massScale = constraint.constraintSoftness.massScale;
                    impulseScale = constraint.constraintSoftness.impulseScale;
                }
                oldImpulse = joint.prismaticUpperImpulse;
                cDot = b2Dot(axisA, b2Sub(vA, vB)) + a1 * wA - a2 * wB;
                impulse = -constraint.axialMass * massScale * (cDot + bias) - impulseScale * oldImpulse;
                joint.prismaticUpperImpulse = b2MaxFloat(oldImpulse + impulse, 0.0f);
                impulse = joint.prismaticUpperImpulse - oldImpulse;

                p = b2MulSV(impulse, axisA);
                la = impulse * a1;
                lb = impulse * a2;
                vA = b2MulAdd(vA, constraint.invMassA, p);
                wA += constraint.invIA * la;
                vB = b2MulSub(vB, constraint.invMassB, p);
                wB -= constraint.invIB * lb;
            }

            b2Vec2 perpA = b2LeftPerp(axisA);
            float s1 = b2Cross(b2Add(d, rA), perpA);
            float s2 = b2Cross(rB, perpA);
            b2Vec2 cDot = new b2Vec2(
                b2Dot(perpA, b2Sub(vB, vA)) + s2 * wB - s1 * wA,
                wB - wA);
            b2Vec2 bias = b2Vec2_zero.copy();
            float massScale = 1.0f;
            float impulseScale = 0.0f;
            if (useBias) {
                b2Vec2 c = new b2Vec2(
                    b2Dot(perpA, d),
                    b2RelativeAngle(dqB, dqA) + constraint.deltaAngle);
                bias = b2MulSV(constraint.constraintSoftness.biasRate, c);
                massScale = constraint.constraintSoftness.massScale;
                impulseScale = constraint.constraintSoftness.impulseScale;
            }

            float k11 = constraint.invMassA + constraint.invMassB + constraint.invIA * s1 * s1 + constraint.invIB * s2 * s2;
            float k12 = constraint.invIA * s1 + constraint.invIB * s2;
            float k22 = constraint.invIA + constraint.invIB;
            if (k22 == 0.0f) {
                k22 = 1.0f;
            }
            b2Mat22 k = new b2Mat22();
            k.cx.x = k11;
            k.cx.y = k12;
            k.cy.x = k12;
            k.cy.y = k22;

            b2Vec2 b = b2Solve22(k, b2Add(cDot, bias));
            b2Vec2 impulse = new b2Vec2(
                -massScale * b.x - impulseScale * joint.prismaticImpulse.x,
                -massScale * b.y - impulseScale * joint.prismaticImpulse.y);
            joint.prismaticImpulse = b2Add(joint.prismaticImpulse, impulse);

            b2Vec2 p = b2MulSV(impulse.x, perpA);
            float la = impulse.x * s1 + impulse.y;
            float lb = impulse.x * s2 + impulse.y;
            vA = b2MulSub(vA, constraint.invMassA, p);
            wA -= constraint.invIA * la;
            vB = b2MulAdd(vB, constraint.invMassB, p);
            wB += constraint.invIB * lb;

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static RevoluteConstraint b2PrepareRevoluteConstraint(JointSlot joint,
                                                                  java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                                  float h,
                                                                  float invH,
                                                                  boolean enableWarmStarting) {
        int indexA = stateIndices.getOrDefault(joint.bodyA, B2_NULL_INDEX);
        int indexB = stateIndices.getOrDefault(joint.bodyB, B2_NULL_INDEX);
        if (indexA == B2_NULL_INDEX && indexB == B2_NULL_INDEX) {
            return null;
        }

        RevoluteConstraint constraint = new RevoluteConstraint();
        constraint.joint = joint;
        constraint.indexA = indexA;
        constraint.indexB = indexB;
        constraint.invMassA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invMass : 0.0f;
        constraint.invMassB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invMass : 0.0f;
        constraint.invIA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invInertia : 0.0f;
        constraint.invIB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invInertia : 0.0f;
        constraint.anchorA = b2RotateVector(joint.bodyA.rotation, b2Sub(joint.localAnchorA, joint.bodyA.localCenter));
        constraint.anchorB = b2RotateVector(joint.bodyB.rotation, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        constraint.deltaCenter = b2Sub(joint.bodyB.center, joint.bodyA.center);
        constraint.deltaAngle = b2RelativeAngle(joint.bodyB.rotation, joint.bodyA.rotation);
        float k = constraint.invIA + constraint.invIB;
        constraint.axialMass = k > 0.0f ? 1.0f / k : 0.0f;
        constraint.springSoftness = b2MakeSoftness(joint.revoluteHertz, joint.revoluteDampingRatio, h);
        float hertz = b2MinFloat(joint.constraintHertz, 0.25f * invH);
        constraint.constraintSoftness = b2MakeSoftness(hertz, joint.constraintDampingRatio, h);

        if (!enableWarmStarting) {
            joint.revoluteLinearImpulse = b2Vec2_zero.copy();
            joint.revoluteSpringImpulse = 0.0f;
            joint.revoluteMotorImpulse = 0.0f;
            joint.revoluteLowerImpulse = 0.0f;
            joint.revoluteUpperImpulse = 0.0f;
        }
        return constraint;
    }

    private static void b2WarmStartRevoluteJoints(java.util.List<RevoluteConstraint> constraints,
                                                  java.util.ArrayList<SolverBodyState> states) {
        for (RevoluteConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            float axialImpulse = joint.revoluteSpringImpulse + joint.revoluteMotorImpulse +
                joint.revoluteLowerImpulse - joint.revoluteUpperImpulse;

            vA = b2MulSub(vA, constraint.invMassA, joint.revoluteLinearImpulse);
            wA -= constraint.invIA * (b2Cross(rA, joint.revoluteLinearImpulse) + axialImpulse);
            vB = b2MulAdd(vB, constraint.invMassB, joint.revoluteLinearImpulse);
            wB += constraint.invIB * (b2Cross(rB, joint.revoluteLinearImpulse) + axialImpulse);

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2SolveRevoluteJoints(java.util.List<RevoluteConstraint> constraints,
                                              java.util.ArrayList<SolverBodyState> states,
                                              float h,
                                              float invH,
                                              boolean useBias) {
        for (RevoluteConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;
            boolean fixedRotation = constraint.invIA + constraint.invIB == 0.0f;

            if (joint.revoluteEnableSpring && !fixedRotation) {
                float jointAngle = b2RelativeAngle(dqB, dqA) + constraint.deltaAngle;
                float jointAngleDelta = b2UnwindAngle(jointAngle - joint.revoluteTargetAngle);
                float bias = constraint.springSoftness.biasRate * jointAngleDelta;
                float cDot = wB - wA;
                float impulse = -constraint.springSoftness.massScale * constraint.axialMass * (cDot + bias) -
                    constraint.springSoftness.impulseScale * joint.revoluteSpringImpulse;
                joint.revoluteSpringImpulse += impulse;
                wA -= constraint.invIA * impulse;
                wB += constraint.invIB * impulse;
            }

            if (joint.revoluteEnableMotor && !fixedRotation) {
                float cDot = wB - wA - joint.revoluteMotorSpeed;
                float impulse = -constraint.axialMass * cDot;
                float oldImpulse = joint.revoluteMotorImpulse;
                float maxImpulse = h * joint.revoluteMaxMotorTorque;
                joint.revoluteMotorImpulse = b2ClampFloat(joint.revoluteMotorImpulse + impulse, -maxImpulse, maxImpulse);
                impulse = joint.revoluteMotorImpulse - oldImpulse;
                wA -= constraint.invIA * impulse;
                wB += constraint.invIB * impulse;
            }

            if (joint.revoluteEnableLimit && !fixedRotation) {
                float jointAngle = b2UnwindAngle(b2RelativeAngle(dqB, dqA) + constraint.deltaAngle - joint.referenceAngle);

                float c = jointAngle - joint.revoluteLowerAngle;
                float bias = 0.0f;
                float massScale = 1.0f;
                float impulseScale = 0.0f;
                if (c > 0.0f) {
                    bias = c * invH;
                } else if (useBias) {
                    bias = constraint.constraintSoftness.biasRate * c;
                    massScale = constraint.constraintSoftness.massScale;
                    impulseScale = constraint.constraintSoftness.impulseScale;
                }
                float cDot = wB - wA;
                float oldImpulse = joint.revoluteLowerImpulse;
                float impulse = -massScale * constraint.axialMass * (cDot + bias) - impulseScale * oldImpulse;
                joint.revoluteLowerImpulse = b2MaxFloat(oldImpulse + impulse, 0.0f);
                impulse = joint.revoluteLowerImpulse - oldImpulse;
                wA -= constraint.invIA * impulse;
                wB += constraint.invIB * impulse;

                c = joint.revoluteUpperAngle - jointAngle;
                bias = 0.0f;
                massScale = 1.0f;
                impulseScale = 0.0f;
                if (c > 0.0f) {
                    bias = c * invH;
                } else if (useBias) {
                    bias = constraint.constraintSoftness.biasRate * c;
                    massScale = constraint.constraintSoftness.massScale;
                    impulseScale = constraint.constraintSoftness.impulseScale;
                }
                cDot = wA - wB;
                oldImpulse = joint.revoluteUpperImpulse;
                impulse = -massScale * constraint.axialMass * (cDot + bias) - impulseScale * oldImpulse;
                joint.revoluteUpperImpulse = b2MaxFloat(oldImpulse + impulse, 0.0f);
                impulse = joint.revoluteUpperImpulse - oldImpulse;
                wA += constraint.invIA * impulse;
                wB -= constraint.invIB * impulse;
            }

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 cDot = b2Sub(b2Add(vB, b2CrossSV(wB, rB)), b2Add(vA, b2CrossSV(wA, rA)));
            b2Vec2 bias = b2Vec2_zero.copy();
            float massScale = 1.0f;
            float impulseScale = 0.0f;
            if (useBias) {
                b2Vec2 separation = b2Add(b2Add(b2Sub(dpB, dpA), b2Sub(rB, rA)), constraint.deltaCenter);
                bias = b2MulSV(constraint.constraintSoftness.biasRate, separation);
                massScale = constraint.constraintSoftness.massScale;
                impulseScale = constraint.constraintSoftness.impulseScale;
            }

            b2Mat22 k = new b2Mat22();
            k.cx.x = constraint.invMassA + constraint.invMassB + rA.y * rA.y * constraint.invIA + rB.y * rB.y * constraint.invIB;
            k.cy.x = -rA.y * rA.x * constraint.invIA - rB.y * rB.x * constraint.invIB;
            k.cx.y = k.cy.x;
            k.cy.y = constraint.invMassA + constraint.invMassB + rA.x * rA.x * constraint.invIA + rB.x * rB.x * constraint.invIB;
            b2Vec2 b = b2Solve22(k, b2Add(cDot, bias));
            b2Vec2 impulse = new b2Vec2(
                -massScale * b.x - impulseScale * joint.revoluteLinearImpulse.x,
                -massScale * b.y - impulseScale * joint.revoluteLinearImpulse.y);
            joint.revoluteLinearImpulse = b2Add(joint.revoluteLinearImpulse, impulse);

            vA = b2MulSub(vA, constraint.invMassA, impulse);
            wA -= constraint.invIA * b2Cross(rA, impulse);
            vB = b2MulAdd(vB, constraint.invMassB, impulse);
            wB += constraint.invIB * b2Cross(rB, impulse);

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static WheelConstraint b2PrepareWheelConstraint(JointSlot joint,
                                                            java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                            float h,
                                                            float invH,
                                                            boolean enableWarmStarting) {
        int indexA = stateIndices.getOrDefault(joint.bodyA, B2_NULL_INDEX);
        int indexB = stateIndices.getOrDefault(joint.bodyB, B2_NULL_INDEX);
        if (indexA == B2_NULL_INDEX && indexB == B2_NULL_INDEX) {
            return null;
        }

        WheelConstraint constraint = new WheelConstraint();
        constraint.joint = joint;
        constraint.indexA = indexA;
        constraint.indexB = indexB;
        constraint.invMassA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invMass : 0.0f;
        constraint.invMassB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invMass : 0.0f;
        constraint.invIA = joint.bodyA.type == b2_dynamicBody ? joint.bodyA.invInertia : 0.0f;
        constraint.invIB = joint.bodyB.type == b2_dynamicBody ? joint.bodyB.invInertia : 0.0f;
        constraint.anchorA = b2RotateVector(joint.bodyA.rotation, b2Sub(joint.localAnchorA, joint.bodyA.localCenter));
        constraint.anchorB = b2RotateVector(joint.bodyB.rotation, b2Sub(joint.localAnchorB, joint.bodyB.localCenter));
        constraint.axisA = b2RotateVector(joint.bodyA.rotation, joint.localAxisA);
        constraint.deltaCenter = b2Sub(joint.bodyB.center, joint.bodyA.center);

        b2Vec2 d = b2Add(constraint.deltaCenter, b2Sub(constraint.anchorB, constraint.anchorA));
        b2Vec2 perpA = b2LeftPerp(constraint.axisA);
        float s1 = b2Cross(b2Add(d, constraint.anchorA), perpA);
        float s2 = b2Cross(constraint.anchorB, perpA);
        float kp = constraint.invMassA + constraint.invMassB + constraint.invIA * s1 * s1 + constraint.invIB * s2 * s2;
        constraint.perpMass = kp > 0.0f ? 1.0f / kp : 0.0f;

        float a1 = b2Cross(b2Add(d, constraint.anchorA), constraint.axisA);
        float a2 = b2Cross(constraint.anchorB, constraint.axisA);
        float ka = constraint.invMassA + constraint.invMassB + constraint.invIA * a1 * a1 + constraint.invIB * a2 * a2;
        constraint.axialMass = ka > 0.0f ? 1.0f / ka : 0.0f;
        constraint.springSoftness = b2MakeSoftness(joint.wheelHertz, joint.wheelDampingRatio, h);

        float km = constraint.invIA + constraint.invIB;
        constraint.motorMass = km > 0.0f ? 1.0f / km : 0.0f;
        float hertz = b2MinFloat(joint.constraintHertz, 0.25f * invH);
        constraint.constraintSoftness = b2MakeSoftness(hertz, joint.constraintDampingRatio, h);

        if (!enableWarmStarting) {
            joint.wheelPerpImpulse = 0.0f;
            joint.wheelSpringImpulse = 0.0f;
            joint.wheelMotorImpulse = 0.0f;
            joint.wheelLowerImpulse = 0.0f;
            joint.wheelUpperImpulse = 0.0f;
        }
        return constraint;
    }

    private static void b2WarmStartWheelJoints(java.util.List<WheelConstraint> constraints,
                                               java.util.ArrayList<SolverBodyState> states) {
        for (WheelConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 d = b2Add(b2Add(b2Sub(dpB, dpA), constraint.deltaCenter), b2Sub(rB, rA));
            b2Vec2 axisA = b2RotateVector(dqA, constraint.axisA);
            b2Vec2 perpA = b2LeftPerp(axisA);

            float a1 = b2Cross(b2Add(d, rA), axisA);
            float a2 = b2Cross(rB, axisA);
            float s1 = b2Cross(b2Add(d, rA), perpA);
            float s2 = b2Cross(rB, perpA);
            float axialImpulse = joint.wheelSpringImpulse + joint.wheelLowerImpulse - joint.wheelUpperImpulse;

            b2Vec2 p = b2Add(b2MulSV(axialImpulse, axisA), b2MulSV(joint.wheelPerpImpulse, perpA));
            float la = axialImpulse * a1 + joint.wheelPerpImpulse * s1 + joint.wheelMotorImpulse;
            float lb = axialImpulse * a2 + joint.wheelPerpImpulse * s2 + joint.wheelMotorImpulse;

            vA = b2MulSub(vA, constraint.invMassA, p);
            wA -= constraint.invIA * la;
            vB = b2MulAdd(vB, constraint.invMassB, p);
            wB += constraint.invIB * lb;

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2SolveWheelJoints(java.util.List<WheelConstraint> constraints,
                                           java.util.ArrayList<SolverBodyState> states,
                                           float h,
                                           float invH,
                                           boolean useBias) {
        for (WheelConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;
            JointSlot joint = constraint.joint;
            boolean fixedRotation = constraint.invIA + constraint.invIB == 0.0f;

            b2Vec2 rA = b2RotateVector(dqA, constraint.anchorA);
            b2Vec2 rB = b2RotateVector(dqB, constraint.anchorB);
            b2Vec2 d = b2Add(b2Add(b2Sub(dpB, dpA), constraint.deltaCenter), b2Sub(rB, rA));
            b2Vec2 axisA = b2RotateVector(dqA, constraint.axisA);
            float translation = b2Dot(axisA, d);
            float a1 = b2Cross(b2Add(d, rA), axisA);
            float a2 = b2Cross(rB, axisA);

            if (joint.wheelEnableMotor && !fixedRotation) {
                float cDot = wB - wA - joint.wheelMotorSpeed;
                float impulse = -constraint.motorMass * cDot;
                float oldImpulse = joint.wheelMotorImpulse;
                float maxImpulse = h * joint.wheelMaxMotorTorque;
                joint.wheelMotorImpulse = b2ClampFloat(joint.wheelMotorImpulse + impulse, -maxImpulse, maxImpulse);
                impulse = joint.wheelMotorImpulse - oldImpulse;
                wA -= constraint.invIA * impulse;
                wB += constraint.invIB * impulse;
            }

            if (joint.wheelEnableSpring) {
                float bias = constraint.springSoftness.biasRate * translation;
                float cDot = b2Dot(axisA, b2Sub(vB, vA)) + a2 * wB - a1 * wA;
                float impulse = -constraint.springSoftness.massScale * constraint.axialMass * (cDot + bias) -
                    constraint.springSoftness.impulseScale * joint.wheelSpringImpulse;
                joint.wheelSpringImpulse += impulse;

                b2Vec2 p = b2MulSV(impulse, axisA);
                float la = impulse * a1;
                float lb = impulse * a2;
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * la;
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * lb;
            }

            if (joint.wheelEnableLimit) {
                float c = translation - joint.wheelLowerTranslation;
                float bias = 0.0f;
                float massScale = 1.0f;
                float impulseScale = 0.0f;
                if (c > 0.0f) {
                    bias = c * invH;
                } else if (useBias) {
                    bias = constraint.constraintSoftness.biasRate * c;
                    massScale = constraint.constraintSoftness.massScale;
                    impulseScale = constraint.constraintSoftness.impulseScale;
                }
                float cDot = b2Dot(axisA, b2Sub(vB, vA)) + a2 * wB - a1 * wA;
                float impulse = -massScale * constraint.axialMass * (cDot + bias) - impulseScale * joint.wheelLowerImpulse;
                float oldImpulse = joint.wheelLowerImpulse;
                joint.wheelLowerImpulse = b2MaxFloat(oldImpulse + impulse, 0.0f);
                impulse = joint.wheelLowerImpulse - oldImpulse;

                b2Vec2 p = b2MulSV(impulse, axisA);
                float la = impulse * a1;
                float lb = impulse * a2;
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * la;
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * lb;

                c = joint.wheelUpperTranslation - translation;
                bias = 0.0f;
                massScale = 1.0f;
                impulseScale = 0.0f;
                if (c > 0.0f) {
                    bias = c * invH;
                } else if (useBias) {
                    bias = constraint.constraintSoftness.biasRate * c;
                    massScale = constraint.constraintSoftness.massScale;
                    impulseScale = constraint.constraintSoftness.impulseScale;
                }
                cDot = b2Dot(axisA, b2Sub(vA, vB)) + a1 * wA - a2 * wB;
                impulse = -massScale * constraint.axialMass * (cDot + bias) - impulseScale * joint.wheelUpperImpulse;
                oldImpulse = joint.wheelUpperImpulse;
                joint.wheelUpperImpulse = b2MaxFloat(oldImpulse + impulse, 0.0f);
                impulse = joint.wheelUpperImpulse - oldImpulse;

                p = b2MulSV(impulse, axisA);
                la = impulse * a1;
                lb = impulse * a2;
                vA = b2MulAdd(vA, constraint.invMassA, p);
                wA += constraint.invIA * la;
                vB = b2MulSub(vB, constraint.invMassB, p);
                wB -= constraint.invIB * lb;
            }

            b2Vec2 perpA = b2LeftPerp(axisA);
            float bias = 0.0f;
            float massScale = 1.0f;
            float impulseScale = 0.0f;
            if (useBias) {
                float c = b2Dot(perpA, d);
                bias = constraint.constraintSoftness.biasRate * c;
                massScale = constraint.constraintSoftness.massScale;
                impulseScale = constraint.constraintSoftness.impulseScale;
            }
            float s1 = b2Cross(b2Add(d, rA), perpA);
            float s2 = b2Cross(rB, perpA);
            float cDot = b2Dot(perpA, b2Sub(vB, vA)) + s2 * wB - s1 * wA;
            float impulse = -massScale * constraint.perpMass * (cDot + bias) -
                impulseScale * joint.wheelPerpImpulse;
            joint.wheelPerpImpulse += impulse;

            b2Vec2 p = b2MulSV(impulse, perpA);
            float la = impulse * s1;
            float lb = impulse * s2;
            vA = b2MulSub(vA, constraint.invMassA, p);
            wA -= constraint.invIA * la;
            vB = b2MulAdd(vB, constraint.invMassB, p);
            wB += constraint.invIB * lb;

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static boolean b2UpdateSleepTimer(WorldSlot world, BodySlot body, SolverBodyState state, float timeStep) {
        float maxVelocity = b2Length(body.linearVelocity) + b2AbsFloat(body.angularVelocity) * body.maxExtent;
        float maxDeltaPosition = b2Length(state.deltaPosition) + b2AbsFloat(state.deltaRotation.s) * body.maxExtent;
        float sleepVelocity = b2MaxFloat(maxVelocity, 0.5f * maxDeltaPosition / timeStep);
        if (!world.sleepEnabled || !body.enableSleep || sleepVelocity > body.sleepThreshold) {
            body.sleepTime = 0.0f;
            return false;
        }
        body.sleepTime += timeStep;
        return body.sleepTime >= B2_TIME_TO_SLEEP();
    }

    private static ContactConstraint b2PrepareContactConstraint(ContactSlot contact,
                                                               java.util.IdentityHashMap<BodySlot, Integer> stateIndices,
                                                               Softness contactSoftness,
                                                               Softness staticSoftness) {
        int pointCount = contact.manifold.pointCount;
        if (pointCount == 0) {
            return null;
        }
        ContactConstraint constraint = new ContactConstraint();
        constraint.contact = contact;
        constraint.indexA = stateIndices.getOrDefault(contact.shapeA.body, B2_NULL_INDEX);
        constraint.indexB = stateIndices.getOrDefault(contact.shapeB.body, B2_NULL_INDEX);
        if (constraint.indexA == B2_NULL_INDEX && constraint.indexB == B2_NULL_INDEX) {
            return null;
        }
        constraint.normal = contact.manifold.normal.copy();
        WorldSlot world = WORLDS[contact.shapeA.body.worldIndex];
        b2SurfaceMaterial materialA = contact.shapeA.def.material;
        b2SurfaceMaterial materialB = contact.shapeB.def.material;
        constraint.friction = world.def.frictionCallback != null
            ? world.def.frictionCallback.invoke(materialA.friction, materialA.userMaterialId, materialB.friction, materialB.userMaterialId)
            : (float) Math.sqrt(materialA.friction * materialB.friction);
        constraint.restitution = world.def.restitutionCallback != null
            ? world.def.restitutionCallback.invoke(materialA.restitution, materialA.userMaterialId, materialB.restitution, materialB.userMaterialId)
            : b2MaxFloat(materialA.restitution, materialB.restitution);
        constraint.rollingResistance = b2ComputeRollingResistance(contact.shapeA, contact.shapeB);
        float warmStartScale = worldWarmStarting(contact.shapeA.body) ? 1.0f : 0.0f;
        constraint.rollingImpulse = warmStartScale * contact.manifold.rollingImpulse;
        constraint.tangentSpeed = contact.shapeA.def.material.tangentSpeed + contact.shapeB.def.material.tangentSpeed;
        constraint.pointCount = pointCount;
        constraint.invMassA = contact.shapeA.body.type == b2_dynamicBody ? contact.shapeA.body.invMass : 0.0f;
        constraint.invIA = contact.shapeA.body.type == b2_dynamicBody ? contact.shapeA.body.invInertia : 0.0f;
        constraint.invMassB = contact.shapeB.body.type == b2_dynamicBody ? contact.shapeB.body.invMass : 0.0f;
        constraint.invIB = contact.shapeB.body.type == b2_dynamicBody ? contact.shapeB.body.invInertia : 0.0f;
        constraint.softness = constraint.indexA == B2_NULL_INDEX || constraint.indexB == B2_NULL_INDEX ? staticSoftness : contactSoftness;
        float kRolling = constraint.invIA + constraint.invIB;
        constraint.rollingMass = kRolling > 0.0f ? 1.0f / kRolling : 0.0f;

        b2Vec2 tangent = b2RightPerp(constraint.normal);
        b2Vec2 vA = contact.shapeA.body.linearVelocity;
        float wA = contact.shapeA.body.angularVelocity;
        b2Vec2 vB = contact.shapeB.body.linearVelocity;
        float wB = contact.shapeB.body.angularVelocity;
        for (int i = 0; i < pointCount; ++i) {
            b2ManifoldPoint mp = contact.manifold.points[i];
            ContactConstraintPoint cp = constraint.points[i];
            cp.normalImpulse = warmStartScale * mp.normalImpulse;
            cp.tangentImpulse = warmStartScale * mp.tangentImpulse;
            cp.totalNormalImpulse = 0.0f;
            cp.anchorA = mp.anchorA.copy();
            cp.anchorB = mp.anchorB.copy();
            cp.baseSeparation = mp.separation - b2Dot(b2Sub(cp.anchorB, cp.anchorA), constraint.normal);
            float rnA = b2Cross(cp.anchorA, constraint.normal);
            float rnB = b2Cross(cp.anchorB, constraint.normal);
            float kNormal = constraint.invMassA + constraint.invMassB + constraint.invIA * rnA * rnA + constraint.invIB * rnB * rnB;
            cp.normalMass = kNormal > 0.0f ? 1.0f / kNormal : 0.0f;
            float rtA = b2Cross(cp.anchorA, tangent);
            float rtB = b2Cross(cp.anchorB, tangent);
            float kTangent = constraint.invMassA + constraint.invMassB + constraint.invIA * rtA * rtA + constraint.invIB * rtB * rtB;
            cp.tangentMass = kTangent > 0.0f ? 1.0f / kTangent : 0.0f;
            b2Vec2 vrA = b2Add(vA, b2CrossSV(wA, cp.anchorA));
            b2Vec2 vrB = b2Add(vB, b2CrossSV(wB, cp.anchorB));
            cp.relativeVelocity = b2Dot(constraint.normal, b2Sub(vrB, vrA));
        }
        return constraint;
    }

    private static boolean worldWarmStarting(BodySlot body) {
        return WORLDS[body.worldIndex].warmStartingEnabled;
    }

    private static float b2ComputeRollingResistance(ShapeSlot shapeA, ShapeSlot shapeB) {
        float rollingA = shapeA.def.material.rollingResistance;
        float rollingB = shapeB.def.material.rollingResistance;
        if (rollingA == 0.0f && rollingB == 0.0f) {
            return 0.0f;
        }
        return b2MaxFloat(rollingA, rollingB) * b2MaxFloat(b2GetShapeRadius(shapeA), b2GetShapeRadius(shapeB));
    }

    private static float b2GetShapeRadius(ShapeSlot shape) {
        if (shape.kind == 1) {
            return shape.polygon.radius;
        }
        if (shape.kind == 2) {
            return shape.circle.radius;
        }
        if (shape.kind == 4) {
            return shape.capsule.radius;
        }
        return 0.0f;
    }

    private static Softness b2MakeSoftness(float hertz, float dampingRatio, float h) {
        Softness softness = new Softness();
        if (hertz == 0.0f) {
            return softness;
        }
        float omega = 2.0f * B2_PI * hertz;
        float a1 = 2.0f * dampingRatio + h * omega;
        float a2 = h * omega * a1;
        float a3 = 1.0f / (1.0f + a2);
        softness.biasRate = omega / a1;
        softness.massScale = a2 * a3;
        softness.impulseScale = a3;
        return softness;
    }

    private static void b2WarmStartContacts(java.util.List<ContactConstraint> constraints,
                                            java.util.ArrayList<SolverBodyState> states) {
        for (ContactConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;

            b2Vec2 normal = constraint.normal;
            b2Vec2 tangent = b2RightPerp(normal);
            for (int i = 0; i < constraint.pointCount; ++i) {
                ContactConstraintPoint cp = constraint.points[i];
                b2Vec2 p = b2Add(b2MulSV(cp.normalImpulse, normal), b2MulSV(cp.tangentImpulse, tangent));
                vA = b2MulAdd(vA, -constraint.invMassA, p);
                wA -= constraint.invIA * b2Cross(cp.anchorA, p);
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * b2Cross(cp.anchorB, p);
            }
            wA -= constraint.invIA * constraint.rollingImpulse;
            wB += constraint.invIB * constraint.rollingImpulse;

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2SolveContacts(java.util.List<ContactConstraint> constraints,
                                        java.util.ArrayList<SolverBodyState> states,
                                        float invH,
                                        float pushout,
                                        boolean useBias,
                                        boolean scalarOverflow) {
        for (ContactConstraint constraint : constraints) {
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Rot dqA = stateA != null ? stateA.deltaRotation : b2Rot_identity;
            b2Vec2 dpA = stateA != null ? stateA.deltaPosition : b2Vec2_zero;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Rot dqB = stateB != null ? stateB.deltaRotation : b2Rot_identity;
            b2Vec2 dpB = stateB != null ? stateB.deltaPosition : b2Vec2_zero;

            b2Vec2 dp = b2Sub(dpB, dpA);
            b2Vec2 normal = constraint.normal;
            b2Vec2 tangent = b2RightPerp(normal);
            Softness softness = constraint.softness;
            float totalNormalImpulse = 0.0f;

            for (int i = 0; i < constraint.pointCount; ++i) {
                ContactConstraintPoint cp = constraint.points[i];
                b2Vec2 ds = b2Add(dp, b2Sub(b2RotateVector(dqB, cp.anchorB), b2RotateVector(dqA, cp.anchorA)));
                float separation = cp.baseSeparation + b2Dot(ds, normal);
                float velocityBias = 0.0f;
                float massScale = 1.0f;
                float impulseScale = 0.0f;
                if (separation > 0.0f) {
                    velocityBias = separation * invH;
                } else if (useBias) {
                    velocityBias = b2MaxFloat(softness.biasRate * separation, -pushout);
                    massScale = softness.massScale;
                    impulseScale = softness.impulseScale;
                }

                b2Vec2 vrA = b2Add(vA, b2CrossSV(wA, cp.anchorA));
                b2Vec2 vrB = b2Add(vB, b2CrossSV(wB, cp.anchorB));
                float vn = b2Dot(b2Sub(vrB, vrA), normal);
                float impulseDelta;
                float newImpulse;
                if (scalarOverflow) {
                    impulseDelta = -cp.normalMass * massScale * (vn + velocityBias)
                        - impulseScale * cp.normalImpulse;
                    newImpulse = b2MaxFloat(cp.normalImpulse + impulseDelta, 0.0f);
                } else {
                    float negImpulse = cp.normalMass * (massScale * (vn + velocityBias))
                        + impulseScale * cp.normalImpulse;
                    newImpulse = b2MaxFloat(cp.normalImpulse - negImpulse, 0.0f);
                }
                float impulse = newImpulse - cp.normalImpulse;
                cp.normalImpulse = newImpulse;
                cp.totalNormalImpulse += newImpulse;
                totalNormalImpulse += newImpulse;

                b2Vec2 p = b2MulSV(impulse, normal);
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * b2Cross(cp.anchorA, p);
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * b2Cross(cp.anchorB, p);
            }

            for (int i = 0; i < constraint.pointCount; ++i) {
                ContactConstraintPoint cp = constraint.points[i];
                b2Vec2 vrB = b2Add(vB, b2CrossSV(wB, cp.anchorB));
                b2Vec2 vrA = b2Add(vA, b2CrossSV(wA, cp.anchorA));
                float vt = b2Dot(b2Sub(vrB, vrA), tangent) - constraint.tangentSpeed;
                float maxFriction = constraint.friction * cp.normalImpulse;
                float newImpulse = scalarOverflow
                    ? b2ClampFloat(cp.tangentImpulse + cp.tangentMass * (-vt), -maxFriction, maxFriction)
                    : b2ClampFloat(cp.tangentImpulse - cp.tangentMass * vt, -maxFriction, maxFriction);
                if (!scalarOverflow && newImpulse == 0.0f) {
                    newImpulse = 0.0f;
                }
                float impulse = newImpulse - cp.tangentImpulse;
                cp.tangentImpulse = newImpulse;
                b2Vec2 p = b2MulSV(impulse, tangent);
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * b2Cross(cp.anchorA, p);
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * b2Cross(cp.anchorB, p);
            }

            float deltaLambda = -constraint.rollingMass * (wB - wA);
            float lambda = constraint.rollingImpulse;
            float maxLambda = constraint.rollingResistance * totalNormalImpulse;
            constraint.rollingImpulse = b2ClampFloat(lambda + deltaLambda, -maxLambda, maxLambda);
            if (!scalarOverflow && constraint.rollingImpulse == 0.0f) {
                constraint.rollingImpulse = 0.0f;
            }
            deltaLambda = constraint.rollingImpulse - lambda;
            wA -= constraint.invIA * deltaLambda;
            wB += constraint.invIB * deltaLambda;

            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2ApplyRestitution(java.util.List<ContactConstraint> constraints,
                                           java.util.ArrayList<SolverBodyState> states,
                                           float threshold) {
        for (ContactConstraint constraint : constraints) {
            if (constraint.restitution == 0.0f) {
                continue;
            }
            SolverBodyState stateA = constraint.indexA == B2_NULL_INDEX ? null : states.get(constraint.indexA);
            SolverBodyState stateB = constraint.indexB == B2_NULL_INDEX ? null : states.get(constraint.indexB);
            b2Vec2 vA = stateA != null ? stateA.linearVelocity : b2Vec2_zero.copy();
            float wA = stateA != null ? stateA.angularVelocity : 0.0f;
            b2Vec2 vB = stateB != null ? stateB.linearVelocity : b2Vec2_zero.copy();
            float wB = stateB != null ? stateB.angularVelocity : 0.0f;
            b2Vec2 normal = constraint.normal;
            for (int i = 0; i < constraint.pointCount; ++i) {
                ContactConstraintPoint cp = constraint.points[i];
                if (cp.relativeVelocity > -threshold || cp.totalNormalImpulse == 0.0f) {
                    continue;
                }
                b2Vec2 vrB = b2Add(vB, b2CrossSV(wB, cp.anchorB));
                b2Vec2 vrA = b2Add(vA, b2CrossSV(wA, cp.anchorA));
                float vn = b2Dot(b2Sub(vrB, vrA), normal);
                float impulse = -cp.normalMass * (vn + constraint.restitution * cp.relativeVelocity);
                float newImpulse = b2MaxFloat(cp.normalImpulse + impulse, 0.0f);
                impulse = newImpulse - cp.normalImpulse;
                cp.normalImpulse = newImpulse;
                cp.totalNormalImpulse += impulse;
                b2Vec2 p = b2MulSV(impulse, normal);
                vA = b2MulSub(vA, constraint.invMassA, p);
                wA -= constraint.invIA * b2Cross(cp.anchorA, p);
                vB = b2MulAdd(vB, constraint.invMassB, p);
                wB += constraint.invIB * b2Cross(cp.anchorB, p);
            }
            if (stateA != null) {
                stateA.linearVelocity = vA;
                stateA.angularVelocity = wA;
            }
            if (stateB != null) {
                stateB.linearVelocity = vB;
                stateB.angularVelocity = wB;
            }
        }
    }

    private static void b2StoreContactImpulses(java.util.List<ContactConstraint> constraints) {
        for (ContactConstraint constraint : constraints) {
            b2Manifold manifold = constraint.contact.manifold;
            for (int i = 0; i < constraint.pointCount; ++i) {
                manifold.points[i].normalImpulse = constraint.points[i].normalImpulse;
                manifold.points[i].tangentImpulse = constraint.points[i].tangentImpulse;
                manifold.points[i].totalNormalImpulse = constraint.points[i].totalNormalImpulse;
                manifold.points[i].normalVelocity = constraint.points[i].relativeVelocity;
            }
            manifold.rollingImpulse = constraint.rollingImpulse;
        }
    }

    private static void b2UpdateContactHitEvents(WorldSlot world, java.util.ArrayList<ContactConstraint> constraints) {
        java.util.ArrayList<b2ContactHitEvent> hitEvents = new java.util.ArrayList<>();
        float threshold = world.def.hitEventThreshold;
        for (ContactConstraint constraint : constraints) {
            ContactSlot contact = constraint.contact;
            if (!(contact.shapeA.def.enableHitEvents || contact.shapeB.def.enableHitEvents)) {
                continue;
            }

            b2ContactHitEvent event = new b2ContactHitEvent();
            event.approachSpeed = threshold;
            boolean hit = false;
            b2Manifold manifold = contact.manifold;
            for (int i = 0; i < manifold.pointCount; ++i) {
                b2ManifoldPoint mp = manifold.points[i];
                float approachSpeed = -mp.normalVelocity;
                if (approachSpeed > event.approachSpeed && mp.totalNormalImpulse > 0.0f) {
                    event.approachSpeed = approachSpeed;
                    event.point = mp.point.copy();
                    hit = true;
                }
            }

            if (hit) {
                event.normal = manifold.normal.copy();
                event.shapeIdA = shapeId(world, contact.shapeA);
                event.shapeIdB = shapeId(world, contact.shapeB);
                hitEvents.add(event);
            }
        }

        world.contactEvents.hitEvents = hitEvents.toArray(new b2ContactHitEvent[0]);
        world.contactEvents.hitCount = hitEvents.size();
    }

    private static void b2UpdateBodyMassData(BodySlot body) {
        body.mass = 0.0f;
        body.inertia = 0.0f;
        body.invMass = 0.0f;
        body.invInertia = 0.0f;
        body.localCenter = b2Vec2_zero.copy();
        body.minExtent = B2_HUGE();
        body.maxExtent = 0.0f;

        b2Transform transform = new b2Transform(body.position, body.rotation);
        if (body.type != b2_dynamicBody) {
            body.center = transform.p.copy();
            return;
        }

        b2Vec2 localCenter = b2Vec2_zero.copy();
        for (int shapeIndex = body.shapes.size() - 1; shapeIndex >= 0; --shapeIndex) {
            ShapeSlot shape = body.shapes.get(shapeIndex);
            if (shape.def.density == 0.0f) {
                continue;
            }
            b2MassData massData = b2ComputeShapeMass(shape);
            body.mass += massData.mass;
            localCenter = b2MulAdd(localCenter, massData.mass, massData.center);
            body.inertia += massData.rotationalInertia;
        }

        if (body.mass > 0.0f) {
            body.invMass = 1.0f / body.mass;
            localCenter = b2MulSV(body.invMass, localCenter);
        }

        if (body.inertia > 0.0f && !body.fixedRotation) {
            body.inertia -= body.mass * b2Dot(localCenter, localCenter);
            body.invInertia = 1.0f / body.inertia;
        } else {
            body.inertia = 0.0f;
            body.invInertia = 0.0f;
        }

        b2Vec2 oldCenter = body.center;
        body.localCenter = localCenter;
        body.center = b2TransformPoint(transform, body.localCenter);
        body.linearVelocity = b2Add(body.linearVelocity, b2CrossSV(body.angularVelocity, b2Sub(body.center, oldCenter)));

        for (ShapeSlot shape : body.shapes) {
            ShapeExtent extent = b2ComputeShapeExtent(shape, localCenter);
            body.minExtent = b2MinFloat(body.minExtent, extent.minExtent);
            body.maxExtent = b2MaxFloat(body.maxExtent, extent.maxExtent);
        }
    }

    private static void b2WakeBody(BodySlot body) {
        if (body.enabled && body.type != b2_staticBody && !body.awake) {
            b2WakeBodySleepIsland(WORLDS[body.worldIndex], body);
        }
    }

    private static void b2LimitBodyLinearVelocity(BodySlot body) {
        WorldSlot world = WORLDS[body.worldIndex];
        float maxLinearSpeed = world.def.maximumLinearSpeed;
        float maxLinearSpeedSquared = maxLinearSpeed * maxLinearSpeed;
        if (b2Dot(body.linearVelocity, body.linearVelocity) > maxLinearSpeedSquared) {
            float ratio = maxLinearSpeed / b2Length(body.linearVelocity);
            body.linearVelocity = b2MulSV(ratio, body.linearVelocity);
        }
    }

    private static final class ShapeExtent {
        float minExtent;
        float maxExtent;
    }

    private static ShapeExtent b2ComputeShapeExtent(ShapeSlot shape, b2Vec2 localCenter) {
        ShapeExtent extent = new ShapeExtent();
        if (shape.kind == 1) {
            float minExtent = B2_HUGE();
            float maxExtentSquared = 0.0f;
            for (int i = 0; i < shape.polygon.count; ++i) {
                b2Vec2 v = shape.polygon.vertices[i];
                float planeOffset = b2Dot(shape.polygon.normals[i], b2Sub(v, shape.polygon.centroid));
                minExtent = b2MinFloat(minExtent, planeOffset);
                maxExtentSquared = b2MaxFloat(maxExtentSquared, b2LengthSquared(b2Sub(v, localCenter)));
            }
            extent.minExtent = minExtent + shape.polygon.radius;
            extent.maxExtent = (float) Math.sqrt(maxExtentSquared) + shape.polygon.radius;
        } else if (shape.kind == 2) {
            extent.minExtent = shape.circle.radius;
            extent.maxExtent = b2Length(b2Sub(shape.circle.center, localCenter)) + shape.circle.radius;
        } else if (shape.kind == 4) {
            extent.minExtent = shape.capsule.radius;
            float d1 = b2LengthSquared(b2Sub(shape.capsule.center1, localCenter));
            float d2 = b2LengthSquared(b2Sub(shape.capsule.center2, localCenter));
            extent.maxExtent = (float) Math.sqrt(b2MaxFloat(d1, d2)) + shape.capsule.radius;
        } else if (shape.kind == 5) {
            extent.minExtent = 0.0f;
            float d1 = b2LengthSquared(b2Sub(shape.chainSegment.segment.point1, localCenter));
            float d2 = b2LengthSquared(b2Sub(shape.chainSegment.segment.point2, localCenter));
            extent.maxExtent = (float) Math.sqrt(b2MaxFloat(d1, d2));
        } else {
            extent.minExtent = 0.0f;
            float d1 = b2LengthSquared(b2Sub(shape.segment.point1, localCenter));
            float d2 = b2LengthSquared(b2Sub(shape.segment.point2, localCenter));
            extent.maxExtent = (float) Math.sqrt(b2MaxFloat(d1, d2));
        }
        return extent;
    }

    private static boolean resolveGroundContacts(WorldSlot world, BodySlot body) {
        boolean changed = false;
        float halfHeight = 0.0f;
        for (ShapeSlot shape : body.shapes) {
            if (shape.kind == 1 && shape.polygon != null) {
                for (int i = 0; i < shape.polygon.count; ++i) {
                    halfHeight = b2MaxFloat(halfHeight, b2AbsFloat(shape.polygon.vertices[i].y));
                }
            } else if (shape.kind == 2 && shape.circle != null) {
                halfHeight = b2MaxFloat(halfHeight, shape.circle.radius);
            } else if (shape.kind == 4 && shape.capsule != null) {
                halfHeight = b2MaxFloat(halfHeight, b2MaxFloat(b2AbsFloat(shape.capsule.center1.y), b2AbsFloat(shape.capsule.center2.y)) + shape.capsule.radius);
            }
        }
        for (BodySlot other : world.bodies) {
            if (other == null || !other.alive || other.type != b2_staticBody) {
                continue;
            }
            for (ShapeSlot shape : other.shapes) {
                if (shape.kind == 1 && shape.polygon != null) {
                    float top = -Float.MAX_VALUE;
                    float left = Float.MAX_VALUE;
                    float right = -Float.MAX_VALUE;
                    for (int i = 0; i < shape.polygon.count; ++i) {
                        b2Vec2 v = b2Add(other.position, shape.polygon.vertices[i]);
                        top = b2MaxFloat(top, v.y);
                        left = b2MinFloat(left, v.x);
                        right = b2MaxFloat(right, v.x);
                    }
                    if (body.position.x >= left && body.position.x <= right && body.position.y - halfHeight < top) {
                        body.position.y = top + halfHeight;
                        changed = true;
                        if (body.linearVelocity.y < 0.0f) {
                            body.linearVelocity.y = 0.0f;
                        }
                    }
                }
            }
        }
        return changed;
    }

    private static void updateSensorEvents(WorldSlot world) {
        java.util.HashMap<Integer, SensorOverlapState> current = new java.util.HashMap<>();
        java.util.ArrayList<b2SensorBeginTouchEvent> begins = new java.util.ArrayList<>();
        java.util.ArrayList<b2SensorEndTouchEvent> ends = new java.util.ArrayList<>();

        java.util.TreeSet<Integer> sensorIndices = new java.util.TreeSet<>(world.sensorOverlaps.keySet());
        for (ShapeSlot shape : world.shapes) {
            if (shape != null && shape.alive && shape.def.isSensor) {
                sensorIndices.add(shape.index);
            }
        }

        java.util.ArrayList<Integer> orderedSensorIndices = new java.util.ArrayList<>(sensorIndices);
        ShapeSlot[] sensorShapes = new ShapeSlot[orderedSensorIndices.size()];
        boolean[] sensorActive = new boolean[orderedSensorIndices.size()];
        int[] sensorGenerations = new int[orderedSensorIndices.size()];
        @SuppressWarnings("unchecked")
        java.util.ArrayList<ShapeRef>[] previousRefs = new java.util.ArrayList[orderedSensorIndices.size()];
        @SuppressWarnings("unchecked")
        java.util.ArrayList<ShapeRef>[] currentRefs = new java.util.ArrayList[orderedSensorIndices.size()];
        for (int sensorOrdinal = 0; sensorOrdinal < orderedSensorIndices.size(); ++sensorOrdinal) {
            int sensorIndex = orderedSensorIndices.get(sensorOrdinal);
            ShapeSlot sensorShape = sensorIndex >= 0 && sensorIndex < world.shapes.size()
                ? world.shapes.get(sensorIndex) : null;
            SensorOverlapState previous = world.sensorOverlaps.get(sensorIndex);
            sensorShapes[sensorOrdinal] = sensorShape;
            sensorActive[sensorOrdinal] = sensorShape != null && sensorShape.alive && sensorShape.def.isSensor
                && sensorShape.def.enableSensorEvents && sensorShape.body.enabled;
            sensorGenerations[sensorOrdinal] = sensorActive[sensorOrdinal]
                ? sensorShape.generation : previous != null ? previous.sensorGeneration : 0;
            previousRefs[sensorOrdinal] = previous != null ? previous.refs : new java.util.ArrayList<>();
        }

        b2ParallelFor(world, orderedSensorIndices.size(), 16,
            (startIndex, endIndex, workerIndex, taskContext) -> {
                for (int sensorOrdinal = startIndex; sensorOrdinal < endIndex; ++sensorOrdinal) {
                    currentRefs[sensorOrdinal] = sensorActive[sensorOrdinal]
                        ? b2CollectSensorOverlapRefs(world, sensorShapes[sensorOrdinal])
                        : new java.util.ArrayList<>();
                }
            }, null);

        for (int sensorOrdinal = 0; sensorOrdinal < orderedSensorIndices.size(); ++sensorOrdinal) {
            int sensorIndex = orderedSensorIndices.get(sensorOrdinal);
            b2ShapeId sensorId = new b2ShapeId(sensorIndex + 1, world.index, sensorGenerations[sensorOrdinal]);
            b2PublishSensorEvents(world, sensorId, previousRefs[sensorOrdinal], currentRefs[sensorOrdinal], begins, ends);

            if (sensorActive[sensorOrdinal] && !currentRefs[sensorOrdinal].isEmpty()) {
                current.put(sensorIndex,
                    new SensorOverlapState(sensorShapes[sensorOrdinal].generation, currentRefs[sensorOrdinal]));
            }
        }

        world.sensorOverlaps.clear();
        world.sensorOverlaps.putAll(current);
        world.sensorEvents = new b2SensorEvents();
        world.sensorEvents.beginEvents = begins.toArray(new b2SensorBeginTouchEvent[0]);
        world.sensorEvents.endEvents = ends.toArray(new b2SensorEndTouchEvent[0]);
        world.sensorEvents.beginCount = begins.size();
        world.sensorEvents.endCount = ends.size();
    }

    private static void b2PublishSensorEvents(WorldSlot world, b2ShapeId sensorId, java.util.ArrayList<ShapeRef> previousRefs,
        java.util.ArrayList<ShapeRef> currentRefs, java.util.ArrayList<b2SensorBeginTouchEvent> begins,
        java.util.ArrayList<b2SensorEndTouchEvent> ends) {
        int index1 = 0;
        int index2 = 0;
        while (index1 < previousRefs.size() && index2 < currentRefs.size()) {
            ShapeRef r1 = previousRefs.get(index1);
            ShapeRef r2 = currentRefs.get(index2);
            if (r1.shapeIndex == r2.shapeIndex) {
                if (r1.generation < r2.generation) {
                    ends.add(b2MakeSensorEndTouchEvent(world, sensorId, r1));
                    index1 += 1;
                } else if (r1.generation > r2.generation) {
                    begins.add(b2MakeSensorBeginTouchEvent(world, sensorId, r2));
                    index2 += 1;
                } else {
                    index1 += 1;
                    index2 += 1;
                }
            } else if (r1.shapeIndex < r2.shapeIndex) {
                ends.add(b2MakeSensorEndTouchEvent(world, sensorId, r1));
                index1 += 1;
            } else {
                begins.add(b2MakeSensorBeginTouchEvent(world, sensorId, r2));
                index2 += 1;
            }
        }

        while (index1 < previousRefs.size()) {
            ends.add(b2MakeSensorEndTouchEvent(world, sensorId, previousRefs.get(index1++)));
        }

        while (index2 < currentRefs.size()) {
            begins.add(b2MakeSensorBeginTouchEvent(world, sensorId, currentRefs.get(index2++)));
        }
    }

    private static b2SensorBeginTouchEvent b2MakeSensorBeginTouchEvent(WorldSlot world, b2ShapeId sensorId, ShapeRef visitorRef) {
        b2SensorBeginTouchEvent event = new b2SensorBeginTouchEvent();
        event.sensorShapeId = sensorId;
        event.visitorShapeId = new b2ShapeId(visitorRef.shapeIndex + 1, world.index, visitorRef.generation);
        return event;
    }

    private static b2SensorEndTouchEvent b2MakeSensorEndTouchEvent(WorldSlot world, b2ShapeId sensorId, ShapeRef visitorRef) {
        b2SensorEndTouchEvent event = new b2SensorEndTouchEvent();
        event.sensorShapeId = sensorId;
        event.visitorShapeId = new b2ShapeId(visitorRef.shapeIndex + 1, world.index, visitorRef.generation);
        return event;
    }

    private static java.util.ArrayList<ShapeRef> b2CollectSensorOverlapRefs(WorldSlot world, ShapeSlot sensorShape) {
        java.util.ArrayList<ShapeRef> overlaps = new java.util.ArrayList<>();
        for (ShapeSlot other : world.shapes) {
            if (other == null || !other.alive || other == sensorShape) {
                continue;
            }
            if (!other.def.enableSensorEvents || other.body == sensorShape.body || !other.body.enabled) {
                continue;
            }
            if (!b2ShouldShapesCollide(sensorShape, other)) {
                continue;
            }
            if (!b2AABB_Overlaps(sensorShape.aabb, other.aabb)) {
                continue;
            }
            if (b2SensorShapesOverlap(sensorShape, other)) {
                overlaps.add(new ShapeRef(other.index, other.generation));
            }
        }
        overlaps.sort((a, b) -> a.shapeIndex != b.shapeIndex ? Integer.compare(a.shapeIndex, b.shapeIndex)
            : Integer.compare(a.generation, b.generation));
        return overlaps;
    }

    private static java.util.ArrayList<ShapeSlot> b2CollectSensorOverlaps(WorldSlot world, ShapeSlot sensorShape) {
        java.util.ArrayList<ShapeSlot> overlaps = new java.util.ArrayList<>();
        SensorOverlapState state = world.sensorOverlaps.get(sensorShape.index);
        if (state == null) {
            return overlaps;
        }
        for (ShapeRef ref : state.refs) {
            ShapeSlot other = ref.shapeIndex >= 0 && ref.shapeIndex < world.shapes.size() ? world.shapes.get(ref.shapeIndex) : null;
            if (other == null || !other.alive || other.generation != ref.generation) {
                continue;
            }
            overlaps.add(other);
        }
        return overlaps;
    }

    private static boolean b2SensorShapesOverlap(ShapeSlot sensorShape, ShapeSlot otherShape) {
        b2DistanceInput input = new b2DistanceInput();
        input.proxyA = b2MakeShapeDistanceProxy(sensorShape);
        input.proxyB = b2MakeShapeDistanceProxy(otherShape);
        input.transformA = new b2Transform(sensorShape.body.position, sensorShape.body.rotation);
        input.transformB = new b2Transform(otherShape.body.position, otherShape.body.rotation);
        input.useRadii = true;
        b2DistanceOutput output = b2ShapeDistance(input, new b2SimplexCache(), null, 0);
        return output.distance < 10.0f * Math.ulp(1.0f);
    }

    private static b2AABB shapeAabb(ShapeSlot shape) {
        b2Transform xf = new b2Transform(shape.body.position, shape.body.rotation);
        if (shape.kind == 1) {
            return b2ComputePolygonAABB(shape.polygon, xf);
        }
        if (shape.kind == 2) {
            return b2ComputeCircleAABB(shape.circle, xf);
        }
        if (shape.kind == 4) {
            return b2ComputeCapsuleAABB(shape.capsule, xf);
        }
        if (shape.kind == 5) {
            return b2ComputeSegmentAABB(shape.chainSegment.segment, xf);
        }
        return b2ComputeSegmentAABB(shape.segment, xf);
    }

    private static String b2CopyBodyName(String name) {
        if (name == null) {
            return "";
        }
        return name.length() > 31 ? name.substring(0, 31) : name;
    }

    private static b2WorldDef copyWorldDef(b2WorldDef src) {
        b2WorldDef dst = b2DefaultWorldDef();
        dst.gravity = src.gravity.copy();
        dst.restitutionThreshold = src.restitutionThreshold;
        dst.hitEventThreshold = src.hitEventThreshold;
        dst.contactHertz = src.contactHertz;
        dst.contactDampingRatio = src.contactDampingRatio;
        dst.maxContactPushSpeed = src.maxContactPushSpeed;
        dst.maximumLinearSpeed = src.maximumLinearSpeed;
        dst.frictionCallback = src.frictionCallback;
        dst.restitutionCallback = src.restitutionCallback;
        dst.enableSleep = src.enableSleep;
        dst.enableContinuous = src.enableContinuous;
        dst.workerCount = src.workerCount;
        dst.enqueueTask = src.enqueueTask;
        dst.finishTask = src.finishTask;
        dst.userTaskContext = src.userTaskContext;
        dst.userData = src.userData;
        dst.internalValue = src.internalValue;
        return dst;
    }

    private static b2Profile copyProfile(b2Profile src) {
        b2Profile dst = new b2Profile();
        dst.step = src.step;
        dst.pairs = src.pairs;
        dst.collide = src.collide;
        dst.solve = src.solve;
        dst.mergeIslands = src.mergeIslands;
        dst.prepareStages = src.prepareStages;
        dst.solveConstraints = src.solveConstraints;
        dst.prepareConstraints = src.prepareConstraints;
        dst.integrateVelocities = src.integrateVelocities;
        dst.warmStart = src.warmStart;
        dst.solveImpulses = src.solveImpulses;
        dst.integratePositions = src.integratePositions;
        dst.relaxImpulses = src.relaxImpulses;
        dst.applyRestitution = src.applyRestitution;
        dst.storeImpulses = src.storeImpulses;
        dst.splitIslands = src.splitIslands;
        dst.transforms = src.transforms;
        dst.hitEvents = src.hitEvents;
        dst.refit = src.refit;
        dst.bullets = src.bullets;
        dst.sleepIslands = src.sleepIslands;
        dst.sensors = src.sensors;
        return dst;
    }

    private static b2Sweep copySweep(b2Sweep src) {
        b2Sweep dst = new b2Sweep();
        dst.localCenter = src.localCenter.copy();
        dst.c1 = src.c1.copy();
        dst.c2 = src.c2.copy();
        dst.q1 = src.q1.copy();
        dst.q2 = src.q2.copy();
        return dst;
    }

    private static b2ShapeDef copyShapeDef(b2ShapeDef src) {
        b2ShapeDef dst = b2DefaultShapeDef();
        dst.userData = src.userData;
        dst.density = src.density;
        dst.filter = copyFilter(src.filter);
        dst.material = copySurfaceMaterial(src.material);
        dst.isSensor = src.isSensor;
        dst.enableSensorEvents = src.enableSensorEvents;
        dst.enableContactEvents = src.enableContactEvents;
        dst.enableHitEvents = src.enableHitEvents;
        dst.enablePreSolveEvents = src.enablePreSolveEvents;
        dst.invokeContactCreation = src.invokeContactCreation;
        dst.updateBodyMass = src.updateBodyMass;
        return dst;
    }

    private static b2ChainSegment copyChainSegment(b2ChainSegment src) {
        b2ChainSegment dst = new b2ChainSegment();
        dst.ghost1.set(src.ghost1);
        dst.segment = new b2Segment(src.segment.point1, src.segment.point2);
        dst.ghost2.set(src.ghost2);
        dst.chainId = src.chainId;
        return dst;
    }

    private static b2SurfaceMaterial copySurfaceMaterial(b2SurfaceMaterial src) {
        b2SurfaceMaterial dst = new b2SurfaceMaterial();
        dst.friction = src.friction;
        dst.restitution = src.restitution;
        dst.rollingResistance = src.rollingResistance;
        dst.tangentSpeed = src.tangentSpeed;
        dst.userMaterialId = src.userMaterialId;
        dst.customColor = src.customColor;
        return dst;
    }

    private static b2Filter copyFilter(b2Filter src) {
        b2Filter dst = new b2Filter();
        dst.categoryBits = src.categoryBits;
        dst.maskBits = src.maskBits;
        dst.groupIndex = src.groupIndex;
        return dst;
    }

    private static WorldSlot getWorld(b2WorldId id) {
        if (id == null || id.index1 <= 0 || id.index1 > WORLDS.length) {
            return null;
        }
        WorldSlot world = WORLDS[id.index1 - 1];
        return world != null && world.generation == id.generation ? world : null;
    }

    private static WorldSlot requireWorld(b2WorldId id) {
        WorldSlot world = getWorld(id);
        if (world == null) {
            throw new IllegalArgumentException("Invalid b2WorldId");
        }
        return world;
    }

    private static BodySlot getBody(b2BodyId id) {
        if (id == null || id.index1 <= 0 || id.world0 < 0 || id.world0 >= WORLDS.length) {
            return null;
        }
        WorldSlot world = WORLDS[id.world0];
        if (world == null) {
            return null;
        }
        int index = id.index1 - 1;
        if (index < 0 || index >= world.bodies.size()) {
            return null;
        }
        BodySlot body = world.bodies.get(index);
        return body != null && body.alive && body.generation == id.generation ? body : null;
    }

    private static BodySlot requireBody(b2BodyId id) {
        BodySlot body = getBody(id);
        if (body == null) {
            throw new IllegalArgumentException("Invalid b2BodyId");
        }
        return body;
    }

    private static ShapeSlot getShape(b2ShapeId id) {
        if (id == null || id.index1 <= 0 || id.world0 < 0 || id.world0 >= WORLDS.length) {
            return null;
        }
        WorldSlot world = WORLDS[id.world0];
        if (world == null) {
            return null;
        }
        int index = id.index1 - 1;
        if (index < 0 || index >= world.shapes.size()) {
            return null;
        }
        ShapeSlot shape = world.shapes.get(index);
        return shape != null && shape.alive && shape.generation == id.generation ? shape : null;
    }

    private static ChainSlot getChain(b2ChainId id) {
        if (id == null || id.index1 <= 0 || id.world0 < 0 || id.world0 >= WORLDS.length) {
            return null;
        }
        WorldSlot world = WORLDS[id.world0];
        if (world == null) {
            return null;
        }
        int index = id.index1 - 1;
        if (index < 0 || index >= world.chains.size()) {
            return null;
        }
        ChainSlot chain = world.chains.get(index);
        return chain != null && chain.alive && chain.generation == id.generation ? chain : null;
    }

    private static JointSlot getJoint(b2JointId id) {
        if (id == null || id.index1 <= 0 || id.world0 < 0 || id.world0 >= WORLDS.length) {
            return null;
        }
        WorldSlot world = WORLDS[id.world0];
        if (world == null) {
            return null;
        }
        int index = id.index1 - 1;
        if (index < 0 || index >= world.joints.size()) {
            return null;
        }
        JointSlot joint = world.joints.get(index);
        return joint != null && joint.alive && joint.generation == id.generation ? joint : null;
    }

    private static ShapeSlot requireShape(b2ShapeId id) {
        ShapeSlot shape = getShape(id);
        if (shape == null) {
            throw new IllegalArgumentException("Invalid b2ShapeId");
        }
        return shape;
    }

    private static ChainSlot requireChain(b2ChainId id) {
        ChainSlot chain = getChain(id);
        if (chain == null) {
            throw new IllegalArgumentException("Invalid b2ChainId");
        }
        return chain;
    }

    private static JointSlot requireJoint(b2JointId id) {
        JointSlot joint = getJoint(id);
        if (joint == null) {
            throw new IllegalArgumentException("Invalid b2JointId");
        }
        return joint;
    }

    private static JointSlot requireDistanceJoint(b2JointId id) {
        JointSlot joint = requireJoint(id);
        if (joint.type != b2_distanceJoint) {
            throw new IllegalArgumentException("Expected b2_distanceJoint");
        }
        return joint;
    }

    private static JointSlot requireMotorJoint(b2JointId id) {
        JointSlot joint = requireJoint(id);
        if (joint.type != b2_motorJoint) {
            throw new IllegalArgumentException("Expected b2_motorJoint");
        }
        return joint;
    }

    private static JointSlot requireMouseJoint(b2JointId id) {
        JointSlot joint = requireJoint(id);
        if (joint.type != b2_mouseJoint) {
            throw new IllegalArgumentException("Expected b2_mouseJoint");
        }
        return joint;
    }

    private static JointSlot requireWeldJoint(b2JointId id) {
        JointSlot joint = requireJoint(id);
        if (joint.type != b2_weldJoint) {
            throw new IllegalArgumentException("Expected b2_weldJoint");
        }
        return joint;
    }

    private static JointSlot requireRevoluteJoint(b2JointId id) {
        JointSlot joint = requireJoint(id);
        if (joint.type != b2_revoluteJoint) {
            throw new IllegalArgumentException("Expected b2_revoluteJoint");
        }
        return joint;
    }

    private static JointSlot requirePrismaticJoint(b2JointId id) {
        JointSlot joint = requireJoint(id);
        if (joint.type != b2_prismaticJoint) {
            throw new IllegalArgumentException("Expected b2_prismaticJoint");
        }
        return joint;
    }

    private static JointSlot requireWheelJoint(b2JointId id) {
        JointSlot joint = requireJoint(id);
        if (joint.type != b2_wheelJoint) {
            throw new IllegalArgumentException("Expected b2_wheelJoint");
        }
        return joint;
    }

    private static b2BodyId bodyId(WorldSlot world, BodySlot body) {
        return new b2BodyId(body.index + 1, world.index, body.generation);
    }

    private static b2ShapeId shapeId(WorldSlot world, ShapeSlot shape) {
        return new b2ShapeId(shape.index + 1, world.index, shape.generation);
    }

    private static b2ChainId chainId(WorldSlot world, ChainSlot chain) {
        return new b2ChainId(chain.index + 1, world.index, chain.generation);
    }

    private static b2JointId jointId(WorldSlot world, JointSlot joint) {
        return new b2JointId(joint.index + 1, world.index, joint.generation);
    }

    private static int b2ShapeTypeFromKind(int kind) {
        if (kind == 1) {
            return b2_polygonShape;
        }
        if (kind == 2) {
            return b2_circleShape;
        }
        if (kind == 3) {
            return b2_segmentShape;
        }
        if (kind == 4) {
            return b2_capsuleShape;
        }
        if (kind == 5) {
            return b2_chainSegmentShape;
        }
        return b2_shapeTypeCount;
    }
}
