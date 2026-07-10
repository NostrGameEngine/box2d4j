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

final class DistanceJointParityTest {
    @Test
    void distanceJointApiMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(1.0f, 2.0f);
        bodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(4.0f, -1.0f);
        bodyDef.rotation = b2MakeRot(-0.5f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2DistanceJointDef def = b2DefaultDistanceJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.localAnchorA = new b2Vec2(0.3f, -0.2f);
        def.localAnchorB = new b2Vec2(-0.4f, 0.6f);
        def.length = 2.25f;
        def.enableSpring = true;
        def.hertz = 5.5f;
        def.dampingRatio = 0.33f;
        def.enableLimit = true;
        def.minLength = 0.75f;
        def.maxLength = 3.5f;
        def.enableMotor = true;
        def.maxMotorForce = 12.0f;
        def.motorSpeed = -1.25f;
        def.collideConnected = true;
        b2JointId jointId = b2CreateDistanceJoint(worldId, def);

        assertInitialLine(lines[line++], jointId);

        b2DistanceJoint_SetLength(jointId, -10.0f);
        assertFloatLine(lines[line++], "lengthClamp", b2DistanceJoint_GetLength(jointId), b2DistanceJoint_GetMotorForce(jointId));
        b2DistanceJoint_SetLength(jointId, 200000.0f);
        assertFloatLine(lines[line++], "lengthHuge", b2DistanceJoint_GetLength(jointId));

        b2DistanceJoint_SetLengthRange(jointId, 5.0f, 1.0f);
        assertFloatLine(lines[line++], "rangeSwap", b2DistanceJoint_GetMinLength(jointId), b2DistanceJoint_GetMaxLength(jointId));
        b2DistanceJoint_SetLengthRange(jointId, -2.0f, 200000.0f);
        assertFloatLine(lines[line++], "rangeClamp", b2DistanceJoint_GetMinLength(jointId), b2DistanceJoint_GetMaxLength(jointId));

        b2DistanceJoint_EnableSpring(jointId, false);
        b2DistanceJoint_SetSpringHertz(jointId, 7.25f);
        b2DistanceJoint_SetSpringDampingRatio(jointId, 0.85f);
        b2DistanceJoint_EnableLimit(jointId, false);
        assertSpringLimitLine(lines[line++], jointId);

        b2DistanceJoint_EnableMotor(jointId, false);
        b2DistanceJoint_SetMotorSpeed(jointId, 3.75f);
        b2DistanceJoint_SetMaxMotorForce(jointId, 44.0f);
        assertMotorLine(lines[line++], "motor0", jointId);
        b2DistanceJoint_EnableMotor(jointId, true);
        assertEquals("motor1 " + boolInt(b2DistanceJoint_IsMotorEnabled(jointId)), lines[line++]);

        b2Body_SetTransform(bodyB, new b2Vec2(2.0f, 4.0f), b2MakeRot(0.75f));
        assertFloatLine(lines[line++], "current1", b2DistanceJoint_GetCurrentLength(jointId));

        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(b2DefaultWorldDef());
        DistanceSimData sim = createDistanceSim(worldId);
        for (int step = 1; step <= 30; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step == 1 || step == 8 || step == 30) {
                assertSimLine(lines[line++], step, sim.bodyId, sim.jointId);
            }
        }

        assertEquals(lines.length, line);
        b2DestroyWorld(worldId);
    }

    private static DistanceSimData createDistanceSim(b2WorldId worldId) {
        b2BodyDef anchorDef = b2DefaultBodyDef();
        anchorDef.position = new b2Vec2(0.0f, 3.25f);
        b2BodyId anchorId = b2CreateBody(worldId, anchorDef);

        b2BodyDef dynamicDef = b2DefaultBodyDef();
        dynamicDef.type = b2_dynamicBody;
        dynamicDef.position = new b2Vec2(2.0f, 1.0f);
        dynamicDef.rotation = b2MakeRot(0.2f);
        dynamicDef.linearVelocity = new b2Vec2(0.5f, -0.25f);
        dynamicDef.angularVelocity = 0.35f;
        b2BodyId dynamicId = b2CreateBody(worldId, dynamicDef);
        b2ShapeDef dynamicShapeDef = b2DefaultShapeDef();
        dynamicShapeDef.density = 2.0f;
        b2CreatePolygonShape(dynamicId, dynamicShapeDef, b2MakeBox(0.4f, 0.6f));

        b2DistanceJointDef simDef = b2DefaultDistanceJointDef();
        simDef.bodyIdA = anchorId;
        simDef.bodyIdB = dynamicId;
        simDef.localAnchorA = new b2Vec2(0.0f, 0.0f);
        simDef.localAnchorB = new b2Vec2(0.2f, -0.1f);
        simDef.length = 2.0f;
        simDef.enableSpring = true;
        simDef.hertz = 2.5f;
        simDef.dampingRatio = 0.35f;
        simDef.enableLimit = true;
        simDef.minLength = 1.25f;
        simDef.maxLength = 3.0f;
        simDef.enableMotor = true;
        simDef.motorSpeed = -0.4f;
        simDef.maxMotorForce = 7.5f;
        return new DistanceSimData(dynamicId, b2CreateDistanceJoint(worldId, simDef));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_distance_joint_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_distance_joint_probe.c").toString());
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
        assertEquals(Float.parseFloat(parts[1]), b2DistanceJoint_GetLength(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[2]) != 0, b2DistanceJoint_IsSpringEnabled(jointId));
        assertEquals(Float.parseFloat(parts[3]), b2DistanceJoint_GetSpringHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2DistanceJoint_GetSpringDampingRatio(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[5]) != 0, b2DistanceJoint_IsLimitEnabled(jointId));
        assertEquals(Float.parseFloat(parts[6]), b2DistanceJoint_GetMinLength(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[7]), b2DistanceJoint_GetMaxLength(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[8]), b2DistanceJoint_GetCurrentLength(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[9]) != 0, b2DistanceJoint_IsMotorEnabled(jointId));
        assertEquals(Float.parseFloat(parts[10]), b2DistanceJoint_GetMotorSpeed(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[11]), b2DistanceJoint_GetMaxMotorForce(jointId), 0.0f);
    }

    private static void assertSpringLimitLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("springLimit", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2DistanceJoint_IsSpringEnabled(jointId));
        assertEquals(Float.parseFloat(parts[2]), b2DistanceJoint_GetSpringHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2DistanceJoint_GetSpringDampingRatio(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[4]) != 0, b2DistanceJoint_IsLimitEnabled(jointId));
    }

    private static void assertMotorLine(String line, String label, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2DistanceJoint_IsMotorEnabled(jointId));
        assertEquals(Float.parseFloat(parts[2]), b2DistanceJoint_GetMotorSpeed(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2DistanceJoint_GetMaxMotorForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2DistanceJoint_GetMotorForce(jointId), 0.0f);
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
        assertEquals(Float.parseFloat(parts[9]), b2DistanceJoint_GetCurrentLength(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[10]), b2DistanceJoint_GetMotorForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[11]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[12]), force.y, 0.0f);
    }

    private static int boolInt(boolean value) {
        return value ? 1 : 0;
    }

    private static final class DistanceSimData {
        final b2BodyId bodyId;
        final b2JointId jointId;

        DistanceSimData(b2BodyId bodyId, b2JointId jointId) {
            this.bodyId = bodyId;
            this.jointId = jointId;
        }
    }
}
