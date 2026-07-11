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

final class WeldJointParityTest {
    @Test
    void weldJointApiMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(1.0f, 2.0f);
        bodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2WeldJointDef def = b2DefaultWeldJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.localAnchorA = new b2Vec2(0.2f, -0.1f);
        def.localAnchorB = new b2Vec2(-0.3f, 0.4f);
        def.referenceAngle = 0.75f;
        def.linearHertz = 2.5f;
        def.linearDampingRatio = 0.35f;
        def.angularHertz = 4.5f;
        def.angularDampingRatio = 0.65f;
        def.collideConnected = true;
        b2JointId jointId = b2CreateWeldJoint(worldId, def);

        assertVecLine(lines[line++], "anchorA", b2Joint_GetLocalAnchorA(jointId));
        assertVecLine(lines[line++], "anchorB", b2Joint_GetLocalAnchorB(jointId));
        assertInitialLine(lines[line++], jointId);

        b2Joint_SetReferenceAngle(jointId, -1.25f);
        b2WeldJoint_SetLinearHertz(jointId, 3.25f);
        b2WeldJoint_SetLinearDampingRatio(jointId, 0.55f);
        b2WeldJoint_SetAngularHertz(jointId, 7.5f);
        b2WeldJoint_SetAngularDampingRatio(jointId, 0.85f);
        assertFloatLine(lines[line++], "updated", b2Joint_GetReferenceAngle(jointId),
            b2WeldJoint_GetLinearHertz(jointId), b2WeldJoint_GetLinearDampingRatio(jointId),
            b2WeldJoint_GetAngularHertz(jointId), b2WeldJoint_GetAngularDampingRatio(jointId));

        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(b2DefaultWorldDef());
        WeldSimData sim = createWeldSim(worldId);
        for (int step = 1; step <= 24; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step == 1 || step == 6 || step == 24) {
                assertSimLine(lines[line++], step, sim.bodyA, sim.bodyB, sim.jointId);
            }
        }

        assertEquals(lines.length, line);
        b2DestroyWorld(worldId);
    }

    private static WeldSimData createWeldSim(b2WorldId worldId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(-0.75f, 1.2f);
        bodyDef.rotation = b2MakeRot(-0.25f);
        bodyDef.linearVelocity = new b2Vec2(0.25f, -0.1f);
        bodyDef.angularVelocity = 0.15f;
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(1.1f, -0.4f);
        bodyDef.rotation = b2MakeRot(0.45f);
        bodyDef.linearVelocity = new b2Vec2(-0.35f, 0.3f);
        bodyDef.angularVelocity = -0.25f;
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.5f;
        b2CreatePolygonShape(bodyA, shapeDef, b2MakeBox(0.4f, 0.3f));
        b2CreatePolygonShape(bodyB, shapeDef, b2MakeBox(0.35f, 0.45f));

        b2WeldJointDef def = b2DefaultWeldJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.localAnchorA = new b2Vec2(0.15f, -0.05f);
        def.localAnchorB = new b2Vec2(-0.2f, 0.1f);
        def.referenceAngle = 0.35f;
        def.linearHertz = 3.0f;
        def.linearDampingRatio = 0.4f;
        def.angularHertz = 5.0f;
        def.angularDampingRatio = 0.7f;
        return new WeldSimData(bodyA, bodyB, b2CreateWeldJoint(worldId, def));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_weld_joint_probe");

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
        command.add(root.resolve("tools/parity/box2d_weld_joint_probe.c").toString());
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
        assertEquals(Float.parseFloat(parts[2]), b2WeldJoint_GetLinearHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2WeldJoint_GetLinearDampingRatio(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2WeldJoint_GetAngularHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2WeldJoint_GetAngularDampingRatio(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[6]), b2Joint_GetConstraintForce(jointId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), b2Joint_GetConstraintForce(jointId).y, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), b2Joint_GetConstraintTorque(jointId), 0.0f);
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

    private static void assertSimLine(String line, int step, b2BodyId bodyA, b2BodyId bodyB, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("sim", parts[0]);
        assertEquals(step, Integer.parseInt(parts[1]));
        b2Transform transformA = b2Body_GetTransform(bodyA);
        b2Transform transformB = b2Body_GetTransform(bodyB);
        b2Vec2 linearVelocityB = b2Body_GetLinearVelocity(bodyB);
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        assertEquals(Float.parseFloat(parts[2]), transformA.p.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), transformA.p.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), transformA.q.c, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), transformA.q.s, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), transformB.p.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), transformB.p.y, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), transformB.q.c, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), transformB.q.s, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), linearVelocityB.x, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), linearVelocityB.y, 0.0f);
        assertEquals(Float.parseFloat(parts[12]), b2Body_GetAngularVelocity(bodyB), 0.0f);
        assertEquals(Float.parseFloat(parts[13]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[14]), force.y, 0.0f);
        assertEquals(Float.parseFloat(parts[15]), b2Joint_GetConstraintTorque(jointId), 0.0f);
    }

    private static final class WeldSimData {
        final b2BodyId bodyA;
        final b2BodyId bodyB;
        final b2JointId jointId;

        WeldSimData(b2BodyId bodyA, b2BodyId bodyB, b2JointId jointId) {
            this.bodyA = bodyA;
            this.bodyB = bodyB;
            this.jointId = jointId;
        }
    }
}
