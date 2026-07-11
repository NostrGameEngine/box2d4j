package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class JointParityTest {
    @Test
    void commonJointApiMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int lineIndex = 0;

        b2DistanceJointDef distanceDef0 = b2DefaultDistanceJointDef();
        b2MotorJointDef motorDef0 = b2DefaultMotorJointDef();
        b2MouseJointDef mouseDef0 = b2DefaultMouseJointDef();
        b2PrismaticJointDef prismaticDef0 = b2DefaultPrismaticJointDef();
        b2RevoluteJointDef revoluteDef0 = b2DefaultRevoluteJointDef();
        b2WheelJointDef wheelDef0 = b2DefaultWheelJointDef();
        assertDefaultsLine(lines[lineIndex++], distanceDef0, motorDef0, mouseDef0, wheelDef0);
        assertVecLine(lines[lineIndex++], "prismaticDefaultAxis", prismaticDef0.localAxisA);
        assertFloatLine(lines[lineIndex++], "revoluteDefault", revoluteDef0.drawSize);
        assertVecLine(lines[lineIndex++], "wheelDefaultAxis", wheelDef0.localAxisA);

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(0.5f, 0.0f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeBox(1.0f, 1.0f);
        b2CreatePolygonShape(bodyA, shapeDef, box);
        b2CreatePolygonShape(bodyB, shapeDef, box);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertIntsLine(lines[lineIndex++], "contacts0", b2World_GetCounters(worldId).contactCount, b2Body_GetContactCapacity(bodyA));

        Object userData = new Object();
        b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
        distanceDef.bodyIdA = bodyA;
        distanceDef.bodyIdB = bodyB;
        distanceDef.localAnchorA = new b2Vec2(0.1f, 0.2f);
        distanceDef.localAnchorB = new b2Vec2(0.3f, 0.4f);
        distanceDef.length = 2.0f;
        distanceDef.collideConnected = false;
        distanceDef.userData = userData;
        b2JointId distanceId = b2CreateDistanceJoint(worldId, distanceDef);
        b2JointId[] bodyAJoints = new b2JointId[8];
        int bodyAJointCount = b2Body_GetJoints(bodyA, bodyAJoints, 8);
        assertDistanceLine(lines[lineIndex++], distanceId, bodyA, bodyB, worldId, bodyAJointCount);
        assertEquals("distanceBodyJoint " + bodyAJoints[0].index1 + " " + bodyAJoints[0].generation, lines[lineIndex++]);
        assertVecLine(lines[lineIndex++], "anchorA0", b2Joint_GetLocalAnchorA(distanceId));
        assertVecLine(lines[lineIndex++], "anchorB0", b2Joint_GetLocalAnchorB(distanceId));
        assertDistancePropsLine(lines[lineIndex++], distanceId, b2Joint_GetUserData(distanceId) == userData,
            b2World_GetCounters(worldId).contactCount);
        assertSame(userData, b2Joint_GetUserData(distanceId));

        b2Joint_SetLocalAnchorA(distanceId, new b2Vec2(-0.5f, 0.75f));
        b2Joint_SetLocalAnchorB(distanceId, new b2Vec2(1.25f, -1.5f));
        Object userData2 = new Object();
        b2Joint_SetUserData(distanceId, userData2);
        assertVecLine(lines[lineIndex++], "anchorA1", b2Joint_GetLocalAnchorA(distanceId));
        assertVecLine(lines[lineIndex++], "anchorB1", b2Joint_GetLocalAnchorB(distanceId));
        assertEquals("userdata1 " + boolInt(b2Joint_GetUserData(distanceId) == userData2), lines[lineIndex++]);
        assertSame(userData2, b2Joint_GetUserData(distanceId));

        b2Joint_SetCollideConnected(distanceId, true);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertIntsLine(lines[lineIndex++], "contacts1", boolInt(b2Joint_GetCollideConnected(distanceId)),
            b2World_GetCounters(worldId).contactCount);

        b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
        prismaticDef.bodyIdA = bodyA;
        prismaticDef.bodyIdB = bodyB;
        prismaticDef.localAnchorA = new b2Vec2(0.2f, -0.1f);
        prismaticDef.localAnchorB = new b2Vec2(-0.3f, 0.6f);
        prismaticDef.localAxisA = new b2Vec2(2.0f, 0.0f);
        prismaticDef.referenceAngle = 0.4f;
        prismaticDef.collideConnected = true;
        b2JointId prismaticId = b2CreatePrismaticJoint(worldId, prismaticDef);
        assertVecLine(lines[lineIndex++], "prismaticAxis0", b2Joint_GetLocalAxisA(prismaticId));
        assertFloatLine(lines[lineIndex++], "prismaticRef0", b2Joint_GetReferenceAngle(prismaticId));
        b2Joint_SetLocalAxisA(prismaticId, new b2Vec2(0.0f, 1.0f));
        b2Joint_SetReferenceAngle(prismaticId, -0.7f);
        assertVecLine(lines[lineIndex++], "prismaticAxis1", b2Joint_GetLocalAxisA(prismaticId));
        assertFloatLine(lines[lineIndex++], "prismaticRef1", b2Joint_GetReferenceAngle(prismaticId));

        b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
        wheelDef.bodyIdA = bodyA;
        wheelDef.bodyIdB = bodyB;
        wheelDef.localAxisA = new b2Vec2(0.0f, 2.0f);
        wheelDef.collideConnected = true;
        b2JointId wheelId = b2CreateWheelJoint(worldId, wheelDef);
        assertVecLine(lines[lineIndex++], "wheelAxis0", b2Joint_GetLocalAxisA(wheelId));

        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = bodyA;
        revoluteDef.bodyIdB = bodyB;
        revoluteDef.referenceAngle = 4.0f;
        revoluteDef.collideConnected = true;
        b2JointId revoluteId = b2CreateRevoluteJoint(worldId, revoluteDef);
        assertFloatLine(lines[lineIndex++], "revolute", b2Joint_GetReferenceAngle(revoluteId));

        b2WeldJointDef weldDef = b2DefaultWeldJointDef();
        weldDef.bodyIdA = bodyA;
        weldDef.bodyIdB = bodyB;
        weldDef.referenceAngle = 1.25f;
        weldDef.collideConnected = true;
        b2JointId weldId = b2CreateWeldJoint(worldId, weldDef);
        assertFloatLine(lines[lineIndex++], "weld", b2Joint_GetReferenceAngle(weldId));

        b2MotorJointDef motorDef = b2DefaultMotorJointDef();
        motorDef.bodyIdA = bodyA;
        motorDef.bodyIdB = bodyB;
        motorDef.collideConnected = true;
        b2JointId motorId = b2CreateMotorJoint(worldId, motorDef);

        b2Body_SetTransform(bodyA, new b2Vec2(0.0f, 0.0f), b2MakeRot(0.0f));
        b2Body_SetTransform(bodyB, new b2Vec2(0.5f, 0.0f), b2MakeRot(0.0f));

        b2MouseJointDef mouseDef = b2DefaultMouseJointDef();
        mouseDef.bodyIdA = bodyA;
        mouseDef.bodyIdB = bodyB;
        mouseDef.target = new b2Vec2(0.25f, 0.75f);
        mouseDef.collideConnected = true;
        b2JointId mouseId = b2CreateMouseJoint(worldId, mouseDef);
        assertVecLine(lines[lineIndex++], "mouseAnchorA", b2Joint_GetLocalAnchorA(mouseId));
        assertVecLine(lines[lineIndex++], "mouseAnchorB", b2Joint_GetLocalAnchorB(mouseId));

        b2FilterJointDef filterDef = b2DefaultFilterJointDef();
        filterDef.bodyIdA = bodyA;
        filterDef.bodyIdB = bodyB;
        b2JointId filterId = b2CreateFilterJoint(worldId, filterDef);
        assertTypesLine(lines[lineIndex++], filterId, motorId, mouseId, prismaticId, revoluteId, weldId, wheelId);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertIntsLine(lines[lineIndex++], "contacts2", boolInt(b2Joint_GetCollideConnected(filterId)),
            b2World_GetCounters(worldId).contactCount);

        b2DestroyJoint(distanceId);
        assertIntsLine(lines[lineIndex++], "destroyDistance", boolInt(b2Joint_IsValid(distanceId)),
            b2Body_GetJointCount(bodyA), b2World_GetCounters(worldId).jointCount);
        b2DestroyBody(bodyB);
        assertIntsLine(lines[lineIndex++], "destroyBody", boolInt(b2Joint_IsValid(filterId)),
            b2Body_GetJointCount(bodyA), b2World_GetCounters(worldId).jointCount);

        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_joint_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_joint_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        return output;
    }

    private static void assertDefaultsLine(String line, b2DistanceJointDef distance, b2MotorJointDef motor,
                                           b2MouseJointDef mouse, b2WheelJointDef wheel) {
        String[] parts = line.split("\\s+");
        assertEquals("defaults", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), distance.length, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), distance.maxLength, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), motor.maxForce, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), motor.maxTorque, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), motor.correctionFactor, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), mouse.hertz, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), mouse.dampingRatio, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), mouse.maxForce, 0.0f);
        assertEquals(Integer.parseInt(parts[9]) != 0, wheel.enableSpring);
        assertEquals(Float.parseFloat(parts[10]), wheel.hertz, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), wheel.dampingRatio, 0.0f);
    }

    private static void assertDistanceLine(String line, b2JointId jointId, b2BodyId bodyA, b2BodyId bodyB,
                                           b2WorldId worldId, int bodyAJointCount) {
        String[] parts = line.split("\\s+");
        assertEquals("distance", parts[0]);
        assertEquals(jointId.index1, Integer.parseInt(parts[1]));
        assertEquals(jointId.world0, Integer.parseInt(parts[2]));
        assertEquals(jointId.generation, Integer.parseInt(parts[3]));
        assertEquals(b2Joint_IsValid(jointId), Integer.parseInt(parts[4]) != 0);
        assertEquals(b2_distanceJoint, Integer.parseInt(parts[5]));
        assertEquals(bodyA.index1, Integer.parseInt(parts[6]));
        assertEquals(bodyB.index1, Integer.parseInt(parts[7]));
        assertEquals(worldId.index1, Integer.parseInt(parts[8]));
        assertEquals(b2Body_GetJointCount(bodyA), Integer.parseInt(parts[9]));
        assertEquals(bodyAJointCount, Integer.parseInt(parts[10]));
    }

    private static void assertDistancePropsLine(String line, b2JointId jointId, boolean sameUserData, int contactCount) {
        String[] parts = line.split("\\s+");
        assertEquals("distanceProps", parts[0]);
        assertEquals(b2Joint_GetCollideConnected(jointId), Integer.parseInt(parts[1]) != 0);
        assertEquals(sameUserData, Integer.parseInt(parts[2]) != 0);
        assertEquals(Float.parseFloat(parts[3]), b2Joint_GetReferenceAngle(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2Joint_GetConstraintForce(jointId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2Joint_GetConstraintTorque(jointId), 0.0f);
        assertEquals(contactCount, Integer.parseInt(parts[6]));
    }

    private static void assertTypesLine(String line, b2JointId filter, b2JointId motor, b2JointId mouse,
                                        b2JointId prismatic, b2JointId revolute, b2JointId weld,
                                        b2JointId wheel) {
        String[] parts = line.split("\\s+");
        assertEquals("types", parts[0]);
        assertEquals(b2Joint_GetType(filter), Integer.parseInt(parts[1]));
        assertEquals(b2Joint_GetType(motor), Integer.parseInt(parts[2]));
        assertEquals(b2Joint_GetType(mouse), Integer.parseInt(parts[3]));
        assertEquals(b2Joint_GetType(prismatic), Integer.parseInt(parts[4]));
        assertEquals(b2Joint_GetType(revolute), Integer.parseInt(parts[5]));
        assertEquals(b2Joint_GetType(weld), Integer.parseInt(parts[6]));
        assertEquals(b2Joint_GetType(wheel), Integer.parseInt(parts[7]));
    }

    private static void assertVecLine(String line, String label, b2Vec2 value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), value.y, 0.0f);
    }

    private static void assertFloatLine(String line, String label, float value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value, 0.0f);
    }

    private static void assertIntsLine(String line, String label, int... values) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(values.length + 1, parts.length);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(values[i], Integer.parseInt(parts[i + 1]));
        }
    }

    private static int boolInt(boolean value) {
        return value ? 1 : 0;
    }
}
