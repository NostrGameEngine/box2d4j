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

final class RevoluteSolverParityTest {
    @Test
    void revoluteSolverMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2();
        worldDef.enableSleep = false;
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.enableSleep = false;
        bodyDef.linearDamping = 0.0f;
        bodyDef.angularDamping = 0.0f;
        bodyDef.position = new b2Vec2(-0.6f, 0.0f);
        bodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        b2Body_SetLinearVelocity(bodyA, new b2Vec2(0.7f, -0.2f));
        b2Body_SetAngularVelocity(bodyA, 0.4f);

        bodyDef.position = new b2Vec2(0.8f, 0.2f);
        bodyDef.rotation = b2MakeRot(-0.35f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);
        b2Body_SetLinearVelocity(bodyB, new b2Vec2(-0.3f, 0.5f));
        b2Body_SetAngularVelocity(bodyB, -0.7f);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.3f;
        b2CreatePolygonShape(bodyA, shapeDef, b2MakeBox(0.5f, 0.3f));
        b2CreatePolygonShape(bodyB, shapeDef, b2MakeBox(0.4f, 0.6f));

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        jointDef.localAnchorA = new b2Vec2(0.25f, 0.1f);
        jointDef.localAnchorB = new b2Vec2(-0.2f, -0.15f);
        jointDef.referenceAngle = -0.6f;
        jointDef.targetAngle = 0.2f;
        jointDef.enableSpring = true;
        jointDef.hertz = 2.0f;
        jointDef.dampingRatio = 0.3f;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.4f;
        jointDef.upperAngle = 0.25f;
        jointDef.enableMotor = true;
        jointDef.motorSpeed = 1.1f;
        jointDef.maxMotorTorque = 3.5f;
        b2JointId jointId = b2CreateRevoluteJoint(worldId, jointDef);

        for (int i = 0; i < 6; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        assertBodyLine(lines[0], "bodyA", bodyA);
        assertBodyLine(lines[1], "bodyB", bodyB);
        assertJointLine(lines[2], jointId);

        b2DestroyWorld(worldId);
    }

    @Test
    void revoluteSolverWithGroundContactMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        worldDef.enableSleep = false;
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -0.5f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        groundShapeDef.material.friction = 0.4f;
        b2CreatePolygonShape(groundId, groundShapeDef, b2MakeBox(4.0f, 0.5f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.enableSleep = false;
        bodyDef.position = new b2Vec2(0.0f, 1.2f);
        bodyDef.rotation = b2MakeRot(0.35f);
        bodyDef.linearDamping = 0.0f;
        bodyDef.angularDamping = 0.0f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2Body_SetLinearVelocity(bodyId, new b2Vec2(0.2f, 0.0f));
        b2Body_SetAngularVelocity(bodyId, 0.1f);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.35f;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.25f, 0.75f));

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = new b2Vec2(0.0f, 1.7f);
        jointDef.localAnchorB = new b2Vec2(0.0f, 0.75f);
        jointDef.referenceAngle = 0.0f;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.55f;
        jointDef.upperAngle = 0.45f;
        jointDef.enableMotor = true;
        jointDef.motorSpeed = -0.6f;
        jointDef.maxMotorTorque = 2.0f;
        b2JointId jointId = b2CreateRevoluteJoint(worldId, jointDef);

        for (int i = 0; i < 80; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        assertBodyLine(lines[3], "groundedBody", bodyId);
        assertGroundedJointLine(lines[4], jointId, b2World_GetCounters(worldId).contactCount);

        b2DestroyWorld(worldId);
    }

    @Test
    void miniFallingHingeFreeFallMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.enableSleep = false;
        b2WorldId worldId = b2CreateWorld(worldDef);

        float h = 0.25f;
        float r = 0.1f * h;
        float offset = 0.4f * h;
        b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.3f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.enableSleep = false;
        bodyDef.position = new b2Vec2(0.0f, h);
        bodyDef.rotation = b2MakeRot(-1.0f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyA, shapeDef, box);

        bodyDef.position = new b2Vec2(offset, h + 2.0f * h);
        bodyDef.rotation = b2MakeRot(-0.9f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyB, shapeDef, box);

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.1f * B2_PI;
        jointDef.upperAngle = 0.2f * B2_PI;
        jointDef.enableSpring = true;
        jointDef.hertz = 0.5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.localAnchorA = new b2Vec2(h, h);
        jointDef.localAnchorB = new b2Vec2(offset, -h);
        b2JointId jointId = b2CreateRevoluteJoint(worldId, jointDef);

        for (int i = 0; i < 60; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        assertBodyLine(lines[5], "miniFreeBodyA", bodyA);
        assertBodyLine(lines[6], "miniFreeBodyB", bodyB);
        assertMiniFreeJointLine(lines[7], jointId, b2World_GetCounters(worldId).contactCount);

        b2DestroyWorld(worldId);
    }

    @Test
    void highAngleFallingHingePairMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.enableSleep = false;
        b2WorldId worldId = b2CreateWorld(worldDef);

        float h = 0.25f;
        float r = 0.1f * h;
        float offset = 0.4f * h;
        b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.3f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.enableSleep = false;
        bodyDef.position = new b2Vec2(offset * 42.0f, h + 2.0f * h * 42.0f);
        bodyDef.rotation = b2MakeRot(0.1f * 42.0f - 1.0f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyA, shapeDef, box);

        bodyDef.position = new b2Vec2(offset * 43.0f, h + 2.0f * h * 43.0f);
        bodyDef.rotation = b2MakeRot(0.1f * 43.0f - 1.0f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyB, shapeDef, box);

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.1f * B2_PI;
        jointDef.upperAngle = 0.2f * B2_PI;
        jointDef.enableSpring = true;
        jointDef.hertz = 0.5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.localAnchorA = new b2Vec2(h, h);
        jointDef.localAnchorB = new b2Vec2(offset, -h);
        b2JointId jointId = b2CreateRevoluteJoint(worldId, jointDef);

        b2World_Step(worldId, 1.0f / 60.0f, 4);

        assertBodyLine(lines[8], "highAngleBodyA", bodyA);
        assertBodyLine(lines[9], "highAngleBodyB", bodyB);
        assertJointLine(lines[10], "highAngleJoint", jointId, b2World_GetCounters(worldId).contactCount);

        b2DestroyWorld(worldId);
    }

    @Test
    void miniFallingHingeWithGroundContactMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.enableSleep = false;
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(20.0f, 1.0f));

        float h = 0.25f;
        float r = 0.1f * h;
        float offset = 0.4f * h;
        b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.3f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.enableSleep = false;
        bodyDef.position = new b2Vec2(0.0f, h);
        bodyDef.rotation = b2MakeRot(-1.0f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyA, shapeDef, box);

        bodyDef.position = new b2Vec2(offset, h + 2.0f * h);
        bodyDef.rotation = b2MakeRot(-0.9f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyB, shapeDef, box);

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.1f * B2_PI;
        jointDef.upperAngle = 0.2f * B2_PI;
        jointDef.enableSpring = true;
        jointDef.hertz = 0.5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.localAnchorA = new b2Vec2(h, h);
        jointDef.localAnchorB = new b2Vec2(offset, -h);
        b2JointId jointId = b2CreateRevoluteJoint(worldId, jointDef);

        for (int i = 0; i < 60; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        assertBodyLine(lines[11], "miniGroundBodyA", bodyA);
        assertBodyLine(lines[12], "miniGroundBodyB", bodyB);
        assertMiniGroundJointLine(lines[13], jointId, b2World_GetCounters(worldId).contactCount);

        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_revolute_solver_probe");

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
        command.add(root.resolve("tools/parity/box2d_revolute_solver_probe.c").toString());
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

    private static void assertBodyLine(String line, String label, b2BodyId bodyId) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        assertEquals(Float.parseFloat(parts[1]), transform.p.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), transform.p.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), transform.q.c, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), transform.q.s, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), velocity.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), velocity.y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), b2Body_GetAngularVelocity(bodyId), 0.0f);
    }

    private static void assertJointLine(String line, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        assertEquals("joint", parts[0]);
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        assertEquals(Float.parseFloat(parts[1]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), force.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2Joint_GetConstraintTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2RevoluteJoint_GetAngle(jointId), 0.0f);
    }

    private static void assertGroundedJointLine(String line, b2JointId jointId, int contactCount) {
        String[] parts = line.split("\\s+");
        assertEquals("groundedJoint", parts[0]);
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        assertEquals(Float.parseFloat(parts[1]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), force.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2Joint_GetConstraintTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2RevoluteJoint_GetAngle(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[5]), contactCount);
    }

    private static void assertMiniFreeJointLine(String line, b2JointId jointId, int contactCount) {
        String[] parts = line.split("\\s+");
        assertEquals("miniFreeJoint", parts[0]);
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        assertEquals(Float.parseFloat(parts[1]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), force.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2Joint_GetConstraintTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2RevoluteJoint_GetAngle(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[5]), contactCount);
    }

    private static void assertMiniGroundJointLine(String line, b2JointId jointId, int contactCount) {
        assertJointLine(line, "miniGroundJoint", jointId, contactCount);
    }

    private static void assertJointLine(String line, String label, b2JointId jointId, int contactCount) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        assertEquals(Float.parseFloat(parts[1]), force.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), force.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2Joint_GetConstraintTorque(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2RevoluteJoint_GetAngle(jointId), 0.0f);
        assertEquals(Integer.parseInt(parts[5]), contactCount);
    }
}
