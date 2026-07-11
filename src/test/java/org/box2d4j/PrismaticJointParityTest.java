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

final class PrismaticJointParityTest {
    @Test
    void prismaticJointApiMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(1.0f, 2.0f);
        bodyDef.rotation = b2MakeRot(0.25f);
        bodyDef.linearVelocity = new b2Vec2(0.4f, -0.2f);
        bodyDef.angularVelocity = 0.7f;
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(3.0f, -1.0f);
        bodyDef.rotation = b2MakeRot(-0.5f);
        bodyDef.linearVelocity = new b2Vec2(-1.2f, 0.9f);
        bodyDef.angularVelocity = -0.35f;
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2PrismaticJointDef def = b2DefaultPrismaticJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.localAnchorA = new b2Vec2(0.2f, -0.3f);
        def.localAnchorB = new b2Vec2(-0.4f, 0.5f);
        def.localAxisA = new b2Vec2(2.0f, 0.0f);
        def.referenceAngle = 0.75f;
        def.targetTranslation = 1.5f;
        def.enableSpring = true;
        def.hertz = 3.5f;
        def.dampingRatio = 0.45f;
        def.enableLimit = true;
        def.lowerTranslation = -0.75f;
        def.upperTranslation = 1.2f;
        def.enableMotor = true;
        def.maxMotorForce = 11.0f;
        def.motorSpeed = -2.25f;
        def.collideConnected = true;
        b2JointId jointId = b2CreatePrismaticJoint(worldId, def);

        assertVecLine(lines[line++], "axis0", b2Joint_GetLocalAxisA(jointId));
        assertVecLine(lines[line++], "anchorA", b2Joint_GetLocalAnchorA(jointId));
        assertVecLine(lines[line++], "anchorB", b2Joint_GetLocalAnchorB(jointId));
        assertInitialLine(lines[line++], jointId);

        b2PrismaticJoint_EnableSpring(jointId, false);
        b2PrismaticJoint_SetSpringHertz(jointId, 8.0f);
        b2PrismaticJoint_SetSpringDampingRatio(jointId, 0.8f);
        b2PrismaticJoint_SetTargetTranslation(jointId, -1.25f);
        assertSpringLine(lines[line++], jointId);

        b2PrismaticJoint_EnableLimit(jointId, false);
        b2PrismaticJoint_SetLimits(jointId, 0.6f, 1.1f);
        assertLimitLine(lines[line++], jointId);

        b2PrismaticJoint_EnableMotor(jointId, false);
        b2PrismaticJoint_SetMotorSpeed(jointId, 4.25f);
        b2PrismaticJoint_SetMaxMotorForce(jointId, -13.0f);
        assertMotorLine(lines[line++], jointId);

        b2Joint_SetLocalAxisA(jointId, new b2Vec2(0.0f, 1.0f));
        assertVecLine(lines[line++], "axis1", b2Joint_GetLocalAxisA(jointId));
        assertFloatLine(lines[line++], "translation1", b2PrismaticJoint_GetTranslation(jointId), b2PrismaticJoint_GetSpeed(jointId));

        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(b2DefaultWorldDef());
        PrismaticSimData sim = createPrismaticSim(worldId);
        for (int step = 1; step <= 24; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step == 1 || step == 6 || step == 24) {
                assertSimLine(lines[line++], step, sim.bodyId, sim.jointId);
            }
        }

        assertEquals(lines.length, line);
        b2DestroyWorld(worldId);
    }

    private static PrismaticSimData createPrismaticSim(b2WorldId worldId) {
        b2BodyDef anchorDef = b2DefaultBodyDef();
        anchorDef.position = new b2Vec2(-0.5f, 1.5f);
        anchorDef.rotation = b2MakeRot(0.35f);
        b2BodyId anchorId = b2CreateBody(worldId, anchorDef);

        b2BodyDef dynamicDef = b2DefaultBodyDef();
        dynamicDef.type = b2_dynamicBody;
        dynamicDef.position = new b2Vec2(1.2f, 0.4f);
        dynamicDef.rotation = b2MakeRot(-0.2f);
        dynamicDef.linearVelocity = new b2Vec2(-0.1f, 0.3f);
        dynamicDef.angularVelocity = 0.4f;
        b2BodyId dynamicId = b2CreateBody(worldId, dynamicDef);
        b2ShapeDef dynamicShapeDef = b2DefaultShapeDef();
        dynamicShapeDef.density = 1.7f;
        b2CreatePolygonShape(dynamicId, dynamicShapeDef, b2MakeBox(0.45f, 0.35f));

        b2PrismaticJointDef simDef = b2DefaultPrismaticJointDef();
        simDef.bodyIdA = anchorId;
        simDef.bodyIdB = dynamicId;
        simDef.localAnchorA = new b2Vec2(0.1f, -0.2f);
        simDef.localAnchorB = new b2Vec2(-0.15f, 0.05f);
        simDef.localAxisA = new b2Vec2(0.6f, 0.8f);
        simDef.referenceAngle = -0.45f;
        simDef.targetTranslation = 0.2f;
        simDef.enableSpring = true;
        simDef.hertz = 2.7f;
        simDef.dampingRatio = 0.45f;
        simDef.enableLimit = true;
        simDef.lowerTranslation = -0.3f;
        simDef.upperTranslation = 0.8f;
        simDef.enableMotor = true;
        simDef.motorSpeed = 0.6f;
        simDef.maxMotorForce = 5.0f;
        return new PrismaticSimData(dynamicId, b2CreatePrismaticJoint(worldId, simDef));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_prismatic_joint_probe");

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
        command.add(root.resolve("tools/parity/box2d_prismatic_joint_probe.c").toString());
        command.add("-lm");
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

    private static void assertInitialLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("initial", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Joint_GetReferenceAngle(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2PrismaticJoint_GetTargetTranslation(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[3]) != 0, b2PrismaticJoint_IsSpringEnabled(jointId));
        assertEquals(Float.parseFloat(parts[4]), b2PrismaticJoint_GetSpringHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2PrismaticJoint_GetSpringDampingRatio(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[6]), b2PrismaticJoint_GetTranslation(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[7]), b2PrismaticJoint_GetSpeed(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[8]) != 0, b2PrismaticJoint_IsLimitEnabled(jointId));
        assertEquals(Float.parseFloat(parts[9]), b2PrismaticJoint_GetLowerLimit(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[10]), b2PrismaticJoint_GetUpperLimit(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[11]), b2PrismaticJoint_GetMotorSpeed(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[12]) != 0, b2PrismaticJoint_IsMotorEnabled(jointId));
        assertEquals(Float.parseFloat(parts[13]), b2PrismaticJoint_GetMaxMotorForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[14]), b2PrismaticJoint_GetMotorForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[15]), b2Joint_GetConstraintForce(jointId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[16]), b2Joint_GetConstraintForce(jointId).y, 0.0f);
        assertEquals(Float.parseFloat(parts[17]), b2Joint_GetConstraintTorque(jointId), 0.0f);
    }

    private static void assertSpringLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("spring", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2PrismaticJoint_GetTargetTranslation(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[2]) != 0, b2PrismaticJoint_IsSpringEnabled(jointId));
        assertEquals(Float.parseFloat(parts[3]), b2PrismaticJoint_GetSpringHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2PrismaticJoint_GetSpringDampingRatio(jointId), 0.0f);
    }

    private static void assertLimitLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("limit", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2PrismaticJoint_IsLimitEnabled(jointId));
        assertEquals(Float.parseFloat(parts[2]), b2PrismaticJoint_GetLowerLimit(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2PrismaticJoint_GetUpperLimit(jointId), 0.0f);
    }

    private static void assertMotorLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("motor", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2PrismaticJoint_IsMotorEnabled(jointId));
        assertEquals(Float.parseFloat(parts[2]), b2PrismaticJoint_GetMotorSpeed(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2PrismaticJoint_GetMaxMotorForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2PrismaticJoint_GetMotorForce(jointId), 0.0f);
    }

    private static void assertVecLine(String line, String label, b2Vec2 value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), value.y, 0.0f);
    }

    private static void assertFloatLine(String line, String label, float... values) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(values.length + 1, parts.length);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(Float.parseFloat(parts[i + 1]), values[i], 0.0f);
        }
    }

    private static void assertSimLine(String line, int step, b2BodyId bodyId, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("sim", parts[0]);
        assertEquals(step, Integer.parseInt(parts[1]));
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        assertEquals(Float.parseFloat(parts[2]), transform.p.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), transform.p.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), transform.q.c, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), transform.q.s, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), linearVelocity.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), linearVelocity.y, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), b2Body_GetAngularVelocity(bodyId), 0.0f);
        assertEquals(Float.parseFloat(parts[9]), b2PrismaticJoint_GetTranslation(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[10]), b2PrismaticJoint_GetSpeed(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[11]), b2PrismaticJoint_GetMotorForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[12]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[13]), force.y, 0.0f);
        assertEquals(Float.parseFloat(parts[14]), b2Joint_GetConstraintTorque(jointId), 0.0f);
    }

    private static final class PrismaticSimData {
        final b2BodyId bodyId;
        final b2JointId jointId;

        PrismaticSimData(b2BodyId bodyId, b2JointId jointId) {
            this.bodyId = bodyId;
            this.jointId = jointId;
        }
    }
}
