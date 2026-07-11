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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BodyAccessParityTest {
    @Test
    void bodyAccessorsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_body_access_probe");

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
        command.add(root.resolve("tools/parity/box2d_body_access_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        String[] lines = output.split("\\R");

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Object userData = new Object();
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.name = "abcdefghijklmnopqrstuvwxyzABCDEZZZ";
        bodyDef.userData = userData;
        bodyDef.linearDamping = 0.2f;
        bodyDef.angularDamping = 0.3f;
        bodyDef.gravityScale = 0.75f;
        bodyDef.isBullet = true;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2ShapeId circleId = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(0.2f, -0.1f), 0.75f));
        b2ShapeId boxId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.5f, 0.25f));
        b2ShapeId segmentId = b2CreateSegmentShape(bodyId, shapeDef, new b2Segment(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f)));

        b2WorldId ownerWorldId = b2Body_GetWorld(bodyId);
        assertEquals(worldId.index1, ownerWorldId.index1);
        assertEquals(worldId.generation, ownerWorldId.generation);
        assertIdentityLine(lines[0], bodyId, ownerWorldId, b2Body_GetType(bodyId), b2Body_GetName(bodyId),
            b2Body_GetUserData(bodyId) == userData, b2Body_IsEnabled(bodyId), b2Body_IsBullet(bodyId),
            b2Body_IsFixedRotation(bodyId), b2Body_GetLinearDamping(bodyId), b2Body_GetAngularDamping(bodyId),
            b2Body_GetGravityScale(bodyId), b2Body_GetShapeCount(bodyId), b2Body_IsValid(bodyId),
            b2Shape_IsValid(circleId));
        assertSame(userData, b2Body_GetUserData(bodyId));

        b2ShapeId[] shapes = new b2ShapeId[2];
        int shapeCount = b2Body_GetShapes(bodyId, shapes, 2);
        assertShapesLine(lines[1], shapeCount, shapes, segmentId, boxId);
        assertAabbLine(lines[2], "aabb0", b2Body_ComputeAABB(bodyId));

        b2Rot rotation = new b2Rot(0.87758255f, 0.47942555f);
        b2Body_SetTransform(bodyId, new b2Vec2(1.5f, -0.75f), rotation);
        assertTransformLine(lines[3], b2Body_GetTransform(bodyId));
        assertVecLine(lines[4], "center", b2Body_GetWorldCenterOfMass(bodyId));
        assertAabbLine(lines[5], "aabb1", b2Body_ComputeAABB(bodyId));

        b2Body_SetLinearVelocity(bodyId, new b2Vec2(3.0f, -2.0f));
        b2Body_SetAngularVelocity(bodyId, 4.0f);
        assertVecLine(lines[6], "linear", b2Body_GetLinearVelocity(bodyId));
        assertScalarLine(lines[7], "angular", b2Body_GetAngularVelocity(bodyId));
        assertVecLine(lines[8], "localVelocity", b2Body_GetLocalPointVelocity(bodyId, new b2Vec2(0.3f, -0.4f)));
        assertVecLine(lines[9], "worldVelocity", b2Body_GetWorldPointVelocity(bodyId, new b2Vec2(1.2f, 0.7f)));

        b2Body_SetLinearDamping(bodyId, 0.8f);
        b2Body_SetAngularDamping(bodyId, 0.9f);
        b2Body_SetGravityScale(bodyId, -0.5f);
        assertTuningLine(lines[10], b2Body_GetLinearDamping(bodyId), b2Body_GetAngularDamping(bodyId), b2Body_GetGravityScale(bodyId));

        assertMassLine(lines[11], "mass0", b2Body_GetMassData(bodyId));
        b2Body_SetFixedRotation(bodyId, true);
        b2Body_SetAngularVelocity(bodyId, 9.0f);
        assertMassLine(lines[12], "mass1", b2Body_GetMassData(bodyId));
        assertFixedLine(lines[13], b2Body_IsFixedRotation(bodyId), b2Body_GetAngularVelocity(bodyId));

        b2Body_EnableContactEvents(bodyId, true);
        b2Body_EnableHitEvents(bodyId, true);
        assertEventsLine(lines[14], circleId, boxId, segmentId);

        Object userData2 = new Object();
        b2Body_SetName(bodyId, null);
        b2Body_SetUserData(bodyId, userData2);
        assertRenameLine(lines[15], b2Body_GetName(bodyId).isEmpty());
        assertSame(userData2, b2Body_GetUserData(bodyId));

        b2Body_Disable(bodyId);
        assertEnabledLine(lines[16], "enabled0", b2Body_IsEnabled(bodyId));
        b2Body_Enable(bodyId);
        assertEnabledLine(lines[17], "enabled1", b2Body_IsEnabled(bodyId));

        b2DestroyWorld(worldId);
    }

    private static void assertIdentityLine(String line, b2BodyId bodyId, b2WorldId worldId, int type, String name,
                                           boolean sameUserData, boolean enabled, boolean bullet, boolean fixed,
                                           float linearDamping, float angularDamping, float gravityScale,
                                           int shapeCount, boolean bodyValid, boolean shapeValid) {
        String[] parts = line.split("\\s+");
        assertEquals("identity", parts[0]);
        assertEquals(type, Integer.parseInt(parts[1]));
        assertEquals(name, parts[2]);
        assertEquals(sameUserData, Integer.parseInt(parts[3]) != 0);
        assertEquals(enabled, Integer.parseInt(parts[4]) != 0);
        assertEquals(bullet, Integer.parseInt(parts[5]) != 0);
        assertEquals(fixed, Integer.parseInt(parts[6]) != 0);
        assertEquals(Float.parseFloat(parts[7]), linearDamping, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), angularDamping, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), gravityScale, 0.0f);
        assertEquals(worldId.index1, Integer.parseInt(parts[10]));
        assertEquals(0, Integer.parseInt(parts[11]));
        assertEquals(shapeCount, Integer.parseInt(parts[12]));
        assertEquals(bodyValid, Integer.parseInt(parts[13]) != 0);
        assertEquals(shapeValid, Integer.parseInt(parts[14]) != 0);
    }

    private static void assertShapesLine(String line, int shapeCount, b2ShapeId[] shapes, b2ShapeId segmentId, b2ShapeId boxId) {
        String[] parts = line.split("\\s+");
        assertEquals("shapes", parts[0]);
        assertEquals(shapeCount, Integer.parseInt(parts[1]));
        assertEquals(shapes[0].index1, Integer.parseInt(parts[2]));
        assertEquals(shapes[0].generation, Integer.parseInt(parts[3]));
        assertEquals(shapes[1].index1, Integer.parseInt(parts[4]));
        assertEquals(shapes[1].generation, Integer.parseInt(parts[5]));
        assertEquals(segmentId.index1 == shapes[0].index1 && boxId.index1 == shapes[1].index1, Integer.parseInt(parts[6]) != 0);
    }

    private static void assertTransformLine(String line, b2Transform transform) {
        String[] parts = line.split("\\s+");
        assertEquals("transform", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), transform.p.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), transform.p.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), transform.q.c, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), transform.q.s, 0.0f);
    }

    private static void assertVecLine(String line, String label, b2Vec2 value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), value.y, 0.0f);
    }

    private static void assertAabbLine(String line, String label, b2AABB value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value.lowerBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), value.lowerBound.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), value.upperBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), value.upperBound.y, 0.0f);
    }

    private static void assertScalarLine(String line, String label, float value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value, 0.0f);
    }

    private static void assertTuningLine(String line, float linearDamping, float angularDamping, float gravityScale) {
        String[] parts = line.split("\\s+");
        assertEquals("tuning", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), linearDamping, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), angularDamping, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), gravityScale, 0.0f);
    }

    private static void assertMassLine(String line, String label, b2MassData mass) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), mass.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), mass.center.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), mass.center.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), mass.rotationalInertia, 0.0f);
    }

    private static void assertFixedLine(String line, boolean fixed, float angularVelocity) {
        String[] parts = line.split("\\s+");
        assertEquals("fixed", parts[0]);
        assertEquals(fixed, Integer.parseInt(parts[1]) != 0);
        assertEquals(Float.parseFloat(parts[2]), angularVelocity, 0.0f);
    }

    private static void assertEventsLine(String line, b2ShapeId circleId, b2ShapeId boxId, b2ShapeId segmentId) {
        String[] parts = line.split("\\s+");
        assertEquals("events", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2Shape_AreContactEventsEnabled(circleId));
        assertEquals(Integer.parseInt(parts[2]) != 0, b2Shape_AreHitEventsEnabled(circleId));
        assertEquals(Integer.parseInt(parts[3]) != 0, b2Shape_AreContactEventsEnabled(boxId));
        assertEquals(Integer.parseInt(parts[4]) != 0, b2Shape_AreHitEventsEnabled(segmentId));
    }

    private static void assertRenameLine(String line, boolean nameEmpty) {
        String[] parts = line.split("\\s+");
        assertEquals("rename", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, nameEmpty);
    }

    private static void assertEnabledLine(String line, String label, boolean enabled) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, enabled);
    }
}
