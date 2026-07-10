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

final class MouseJointParityTest {
    @Test
    void mouseJointApiMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(2.0f, -1.0f);
        bodyDef.rotation = b2MakeRot(0.5f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2MouseJointDef def = b2DefaultMouseJointDef();
        def.bodyIdA = groundId;
        def.bodyIdB = bodyId;
        def.target = new b2Vec2(3.0f, 1.0f);
        def.hertz = 6.25f;
        def.dampingRatio = 0.45f;
        def.maxForce = 123.0f;
        def.collideConnected = true;
        b2JointId jointId = b2CreateMouseJoint(worldId, def);

        assertVecLine(lines[line++], "target0", b2MouseJoint_GetTarget(jointId));
        assertVecLine(lines[line++], "anchorA", b2Joint_GetLocalAnchorA(jointId));
        assertVecLine(lines[line++], "anchorB", b2Joint_GetLocalAnchorB(jointId));
        assertInitialLine(lines[line++], jointId);

        b2MouseJoint_SetTarget(jointId, new b2Vec2(-4.0f, 5.5f));
        b2MouseJoint_SetSpringHertz(jointId, 9.0f);
        b2MouseJoint_SetSpringDampingRatio(jointId, 0.8f);
        b2MouseJoint_SetMaxForce(jointId, 321.0f);
        assertVecLine(lines[line++], "target1", b2MouseJoint_GetTarget(jointId));
        assertFloatLine(lines[line++], "updated", b2MouseJoint_GetSpringHertz(jointId),
            b2MouseJoint_GetSpringDampingRatio(jointId), b2MouseJoint_GetMaxForce(jointId));

        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(b2DefaultWorldDef());
        MouseSimData sim = createMouseSim(worldId);
        for (int step = 1; step <= 24; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step == 1 || step == 6 || step == 24) {
                assertSimLine(lines[line++], step, sim.bodyId, sim.jointId);
            }
        }

        assertEquals(lines.length, line);
        b2DestroyWorld(worldId);
    }

    private static MouseSimData createMouseSim(b2WorldId worldId) {
        b2BodyDef groundDef = b2DefaultBodyDef();
        b2BodyId anchorId = b2CreateBody(worldId, groundDef);

        b2BodyDef dynamicDef = b2DefaultBodyDef();
        dynamicDef.type = b2_dynamicBody;
        dynamicDef.position = new b2Vec2(2.2f, -0.8f);
        dynamicDef.rotation = b2MakeRot(-0.35f);
        dynamicDef.linearVelocity = new b2Vec2(0.3f, -0.2f);
        dynamicDef.angularVelocity = 0.4f;
        b2BodyId dynamicId = b2CreateBody(worldId, dynamicDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.4f;
        b2CreatePolygonShape(dynamicId, shapeDef, b2MakeBox(0.5f, 0.3f));

        b2MouseJointDef simDef = b2DefaultMouseJointDef();
        simDef.bodyIdA = anchorId;
        simDef.bodyIdB = dynamicId;
        simDef.target = new b2Vec2(-1.25f, 1.5f);
        simDef.hertz = 4.0f;
        simDef.dampingRatio = 0.65f;
        simDef.maxForce = 9.0f;
        return new MouseSimData(dynamicId, b2CreateMouseJoint(worldId, simDef));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_mouse_joint_probe");

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
        command.add(root.resolve("tools/parity/box2d_mouse_joint_probe.c").toString());
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
        assertEquals(Float.parseFloat(parts[1]), b2MouseJoint_GetSpringHertz(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2MouseJoint_GetSpringDampingRatio(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2MouseJoint_GetMaxForce(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2Joint_GetConstraintForce(jointId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2Joint_GetConstraintForce(jointId).y, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), b2Joint_GetConstraintTorque(jointId), 0.0f);
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

    private static final class MouseSimData {
        final b2BodyId bodyId;
        final b2JointId jointId;

        MouseSimData(b2BodyId bodyId, b2JointId jointId) {
            this.bodyId = bodyId;
            this.jointId = jointId;
        }
    }
}
