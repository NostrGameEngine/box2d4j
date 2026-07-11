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

final class MotorJointParityTest {
    @Test
    void motorJointApiMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(2.0f, -1.0f);
        bodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2MotorJointDef def = b2DefaultMotorJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.linearOffset = new b2Vec2(1.25f, -0.75f);
        def.angularOffset = 2.5f;
        def.maxForce = -4.0f;
        def.maxTorque = -5.0f;
        def.correctionFactor = 1.75f;
        def.collideConnected = true;
        b2JointId jointId = b2CreateMotorJoint(worldId, def);

        assertVecLine(lines[line++], "linear0", b2MotorJoint_GetLinearOffset(jointId));
        assertInitialLine(lines[line++], jointId);

        b2MotorJoint_SetLinearOffset(jointId, new b2Vec2(-3.0f, 4.5f));
        b2MotorJoint_SetAngularOffset(jointId, -7.0f);
        assertVecLine(lines[line++], "linear1", b2MotorJoint_GetLinearOffset(jointId));
        assertFloatLine(lines[line++], "angular1", b2MotorJoint_GetAngularOffset(jointId));

        b2MotorJoint_SetMaxForce(jointId, -8.0f);
        b2MotorJoint_SetMaxTorque(jointId, -9.0f);
        assertFloatLine(lines[line++], "maxClamp0", b2MotorJoint_GetMaxForce(jointId), b2MotorJoint_GetMaxTorque(jointId));

        b2MotorJoint_SetMaxForce(jointId, 8.25f);
        b2MotorJoint_SetMaxTorque(jointId, 9.5f);
        assertFloatLine(lines[line++], "maxClamp1", b2MotorJoint_GetMaxForce(jointId), b2MotorJoint_GetMaxTorque(jointId));

        b2MotorJoint_SetCorrectionFactor(jointId, -0.25f);
        assertFloatLine(lines[line++], "correction0", b2MotorJoint_GetCorrectionFactor(jointId));
        b2MotorJoint_SetCorrectionFactor(jointId, 0.42f);
        assertFloatLine(lines[line++], "correction1", b2MotorJoint_GetCorrectionFactor(jointId));
        b2MotorJoint_SetCorrectionFactor(jointId, 2.0f);
        assertFloatLine(lines[line++], "correction2", b2MotorJoint_GetCorrectionFactor(jointId));

        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(b2DefaultWorldDef());
        MotorSimData sim = createMotorSim(worldId);
        for (int step = 1; step <= 24; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step == 1 || step == 6 || step == 24) {
                assertSimLine(lines[line++], step, sim.bodyId, sim.jointId);
            }
        }

        assertEquals(lines.length, line);
        b2DestroyWorld(worldId);
    }

    private static MotorSimData createMotorSim(b2WorldId worldId) {
        b2BodyDef anchorDef = b2DefaultBodyDef();
        anchorDef.position = new b2Vec2(-0.5f, 1.25f);
        anchorDef.rotation = b2MakeRot(-0.2f);
        b2BodyId anchorId = b2CreateBody(worldId, anchorDef);

        b2BodyDef dynamicDef = b2DefaultBodyDef();
        dynamicDef.type = b2_dynamicBody;
        dynamicDef.position = new b2Vec2(2.5f, -0.75f);
        dynamicDef.rotation = b2MakeRot(0.6f);
        dynamicDef.linearVelocity = new b2Vec2(-0.35f, 0.65f);
        dynamicDef.angularVelocity = -0.45f;
        b2BodyId dynamicId = b2CreateBody(worldId, dynamicDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.75f;
        b2CreatePolygonShape(dynamicId, shapeDef, b2MakeBox(0.45f, 0.35f));

        b2MotorJointDef simDef = b2DefaultMotorJointDef();
        simDef.bodyIdA = anchorId;
        simDef.bodyIdB = dynamicId;
        simDef.linearOffset = new b2Vec2(1.0f, -0.25f);
        simDef.angularOffset = 0.4f;
        simDef.maxForce = 6.25f;
        simDef.maxTorque = 3.5f;
        simDef.correctionFactor = 0.65f;
        return new MotorSimData(dynamicId, b2CreateMotorJoint(worldId, simDef));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_motor_joint_probe");

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
        command.add(root.resolve("tools/parity/box2d_motor_joint_probe.c").toString());
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
        assertEquals(Float.parseFloat(parts[1]), b2MotorJoint_GetAngularOffset(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2MotorJoint_GetMaxForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2MotorJoint_GetMaxTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2MotorJoint_GetCorrectionFactor(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2Joint_GetConstraintForce(jointId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), b2Joint_GetConstraintForce(jointId).y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), b2Joint_GetConstraintTorque(jointId), 0.0f);
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
        assertEquals(Float.parseFloat(parts[9]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), force.y, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), b2Joint_GetConstraintTorque(jointId), 0.0f);
    }

    private static final class MotorSimData {
        final b2BodyId bodyId;
        final b2JointId jointId;

        MotorSimData(b2BodyId bodyId, b2JointId jointId) {
            this.bodyId = bodyId;
            this.jointId = jointId;
        }
    }
}
