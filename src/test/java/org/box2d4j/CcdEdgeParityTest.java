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

final class CcdEdgeParityTest {
    @Test
    void movingKinematicAndToiPreSolveMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        assertEquals(2, lines.length);
        assertMovingKinematic(lines[0]);
        assertToiPreSolve(lines[1]);
    }

    private static void assertMovingKinematic(String expected) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef targetDef = b2DefaultBodyDef();
        targetDef.type = b2_kinematicBody;
        targetDef.linearVelocity = new b2Vec2(-5.0f, 0.0f);
        b2BodyId targetId = b2CreateBody(worldId, targetDef);
        b2CreatePolygonShape(targetId, b2DefaultShapeDef(), b2MakeBox(0.25f, 2.0f));

        b2BodyDef bulletDef = b2DefaultBodyDef();
        bulletDef.type = b2_dynamicBody;
        bulletDef.isBullet = true;
        bulletDef.position = new b2Vec2(-5.0f, 0.0f);
        bulletDef.linearVelocity = new b2Vec2(20.0f, 0.0f);
        b2BodyId bulletId = b2CreateBody(worldId, bulletDef);
        b2ShapeDef bulletShapeDef = b2DefaultShapeDef();
        bulletShapeDef.density = 1.0f;
        b2CreateCircleShape(bulletId, bulletShapeDef, new b2Circle(new b2Vec2(), 0.25f));

        b2World_Step(worldId, 0.25f, 1);
        String[] parts = expected.split("\\s+");
        assertEquals("kinematic", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Body_GetPosition(bulletId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2Body_GetPosition(targetId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2Body_GetLinearVelocity(bulletId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2Body_GetLinearVelocity(targetId).x, 0.0f);
        assertEquals(Integer.parseInt(parts[5]), b2World_GetCounters(worldId).contactCount);
        b2DestroyWorld(worldId);
    }

    private static void assertToiPreSolve(String expected) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);
        b2BodyId wallId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef wallShapeDef = b2DefaultShapeDef();
        wallShapeDef.enablePreSolveEvents = true;
        b2CreatePolygonShape(wallId, wallShapeDef, b2MakeBox(0.1f, 2.0f));

        b2BodyDef bulletDef = b2DefaultBodyDef();
        bulletDef.type = b2_dynamicBody;
        bulletDef.isBullet = true;
        bulletDef.position = new b2Vec2(-5.0f, 0.0f);
        bulletDef.linearVelocity = new b2Vec2(40.0f, 0.0f);
        b2BodyId bulletId = b2CreateBody(worldId, bulletDef);
        b2ShapeDef bulletShapeDef = b2DefaultShapeDef();
        bulletShapeDef.density = 1.0f;
        b2CreateCircleShape(bulletId, bulletShapeDef, new b2Circle(new b2Vec2(), 0.25f));

        int[] callbackCount = {0};
        int[] pointCount = {0};
        int[] shapeA = {0};
        int[] shapeB = {0};
        b2World_SetPreSolveCallback(worldId, (shapeIdA, shapeIdB, manifold, context) -> {
            callbackCount[0] += 1;
            pointCount[0] = manifold.pointCount;
            shapeA[0] = shapeIdA.index1;
            shapeB[0] = shapeIdB.index1;
            return false;
        }, null);

        b2World_Step(worldId, 0.2f, 1);
        String[] parts = expected.split("\\s+");
        assertEquals("preSolve", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), callbackCount[0]);
        assertEquals(Integer.parseInt(parts[2]), pointCount[0]);
        assertEquals(Integer.parseInt(parts[3]), shapeA[0]);
        assertEquals(Integer.parseInt(parts[4]), shapeB[0]);
        assertEquals(Float.parseFloat(parts[5]), b2Body_GetPosition(bulletId).x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), b2Body_GetLinearVelocity(bulletId).x, 0.0f);
        assertEquals(Integer.parseInt(parts[7]), b2World_GetCounters(worldId).contactCount);
        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_ccd_edge_probe");
        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c")).sorted()
                .forEach(path -> command.add(path.toString()));
        }
        command.add(root.resolve("tools/parity/box2d_ccd_edge_probe.c").toString());
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
}
