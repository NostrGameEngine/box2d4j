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

final class RevoluteJointParityTest {
    @Test
    void revoluteJointApiMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(1.0f, -2.0f);
        bodyDef.rotation = b2MakeRot(1.5f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2RevoluteJointDef def = b2DefaultRevoluteJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.localAnchorA = new b2Vec2(0.2f, -0.3f);
        def.localAnchorB = new b2Vec2(-0.4f, 0.5f);
        def.referenceAngle = 4.0f;
        def.targetAngle = -4.0f;
        def.enableSpring = true;
        def.hertz = 3.5f;
        def.dampingRatio = 0.45f;
        def.enableLimit = true;
        def.lowerAngle = -0.75f;
        def.upperAngle = 0.9f;
        def.enableMotor = true;
        def.maxMotorTorque = 11.0f;
        def.motorSpeed = -2.25f;
        def.collideConnected = true;
        b2JointId jointId = b2CreateRevoluteJoint(worldId, def);

        assertVecLine(lines[line++], "anchorA", b2Joint_GetLocalAnchorA(jointId));
        assertVecLine(lines[line++], "anchorB", b2Joint_GetLocalAnchorB(jointId));
        assertInitialLine(lines[line++], jointId);

        b2RevoluteJoint_EnableSpring(jointId, false);
        b2RevoluteJoint_SetSpringHertz(jointId, 8.0f);
        b2RevoluteJoint_SetSpringDampingRatio(jointId, 0.8f);
        b2RevoluteJoint_SetTargetAngle(jointId, 1.25f);
        assertSpringLine(lines[line++], jointId);

        b2RevoluteJoint_EnableLimit(jointId, false);
        b2RevoluteJoint_SetLimits(jointId, 0.6f, 1.1f);
        assertLimitLine(lines[line++], jointId);

        b2RevoluteJoint_EnableMotor(jointId, false);
        b2RevoluteJoint_SetMotorSpeed(jointId, 4.25f);
        b2RevoluteJoint_SetMaxMotorTorque(jointId, -13.0f);
        assertMotorLine(lines[line++], jointId);

        b2Body_SetTransform(bodyB, new b2Vec2(2.0f, 3.0f), b2MakeRot(-2.75f));
        assertFloatLine(lines[line], "angle1", b2RevoluteJoint_GetAngle(jointId));

        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_revolute_joint_probe");

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
        command.add(root.resolve("tools/parity/box2d_revolute_joint_probe.c").toString());
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
        assertEquals(Float.parseFloat(parts[2]), b2RevoluteJoint_GetTargetAngle(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[3]) != 0, b2RevoluteJoint_IsSpringEnabled(jointId));
        assertEquals(Float.parseFloat(parts[4]), b2RevoluteJoint_GetSpringHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2RevoluteJoint_GetSpringDampingRatio(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[6]), b2RevoluteJoint_GetAngle(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[7]) != 0, b2RevoluteJoint_IsLimitEnabled(jointId));
        assertEquals(Float.parseFloat(parts[8]), b2RevoluteJoint_GetLowerLimit(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[9]), b2RevoluteJoint_GetUpperLimit(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[10]), b2RevoluteJoint_GetMotorSpeed(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[11]) != 0, b2RevoluteJoint_IsMotorEnabled(jointId));
        assertEquals(Float.parseFloat(parts[12]), b2RevoluteJoint_GetMaxMotorTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[13]), b2RevoluteJoint_GetMotorTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[14]), b2Joint_GetConstraintForce(jointId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[15]), b2Joint_GetConstraintForce(jointId).y, 0.0f);
        assertEquals(Float.parseFloat(parts[16]), b2Joint_GetConstraintTorque(jointId), 0.0f);
    }

    private static void assertSpringLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("spring", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2RevoluteJoint_GetTargetAngle(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[2]) != 0, b2RevoluteJoint_IsSpringEnabled(jointId));
        assertEquals(Float.parseFloat(parts[3]), b2RevoluteJoint_GetSpringHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2RevoluteJoint_GetSpringDampingRatio(jointId), 0.0f);
    }

    private static void assertLimitLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("limit", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2RevoluteJoint_IsLimitEnabled(jointId));
        assertEquals(Float.parseFloat(parts[2]), b2RevoluteJoint_GetLowerLimit(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2RevoluteJoint_GetUpperLimit(jointId), 0.0f);
    }

    private static void assertMotorLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("motor", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2RevoluteJoint_IsMotorEnabled(jointId));
        assertEquals(Float.parseFloat(parts[2]), b2RevoluteJoint_GetMotorSpeed(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2RevoluteJoint_GetMaxMotorTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2RevoluteJoint_GetMotorTorque(jointId), 0.0f);
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
}
