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

final class ShapeAccessParityTest {
    @Test
    void shapeAccessorsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_access_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_access_probe.c").toString());
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
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        Object userData = new Object();
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        shapeDef.isSensor = true;
        shapeDef.userData = userData;
        b2ShapeId shapeId = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(0.2f, -0.1f), 0.75f));

        b2BodyId ownerBodyId = b2Shape_GetBody(shapeId);
        b2WorldId ownerWorldId = b2Shape_GetWorld(shapeId);
        assertEquals(worldId.index1, ownerWorldId.index1);
        assertEquals(worldId.generation, ownerWorldId.generation);
        assertIdsLine(lines[0], shapeId, ownerBodyId, ownerWorldId, b2Shape_GetType(shapeId));
        assertFlagsLine(lines[1], b2Shape_IsSensor(shapeId), b2Shape_GetUserData(shapeId) == userData, b2Shape_GetDensity(shapeId));
        assertSame(userData, b2Shape_GetUserData(shapeId));
        assertMassLine(lines[2], "mass0", b2Body_GetMassData(bodyId));

        b2Shape_SetDensity(shapeId, 4.0f, false);
        assertDensityLine(lines[3], "density1", b2Shape_GetDensity(shapeId));
        assertMassLine(lines[4], "mass1", b2Body_GetMassData(bodyId));

        b2Shape_SetDensity(shapeId, 4.0f, true);
        assertMassLine(lines[5], "mass2", b2Body_GetMassData(bodyId));

        b2Shape_SetDensity(shapeId, 1.5f, true);
        assertDensityLine(lines[6], "density2", b2Shape_GetDensity(shapeId));
        assertMassLine(lines[7], "mass3", b2Body_GetMassData(bodyId));

        Object userData2 = new Object();
        b2Shape_SetUserData(shapeId, userData2);
        assertEquals("userdata2", lines[8].split("\\s+")[0]);
        assertSame(userData2, b2Shape_GetUserData(shapeId));

        b2DestroyWorld(worldId);
    }

    private static void assertIdsLine(String line, b2ShapeId shapeId, b2BodyId bodyId, b2WorldId worldId, int type) {
        String[] parts = line.split("\\s+");
        assertEquals("ids", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), shapeId.index1);
        assertEquals(Integer.parseInt(parts[2]), bodyId.index1);
        assertEquals(Integer.parseInt(parts[3]), bodyId.world0);
        assertEquals(Integer.parseInt(parts[4]), bodyId.generation);
        assertEquals(Integer.parseInt(parts[5]), worldId.index1);
        assertEquals(0, Integer.parseInt(parts[6]));
        assertEquals(Integer.parseInt(parts[7]), type);
    }

    private static void assertFlagsLine(String line, boolean sensor, boolean sameUserData, float density) {
        String[] parts = line.split("\\s+");
        assertEquals("flags", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, sensor);
        assertEquals(Integer.parseInt(parts[2]) != 0, sameUserData);
        assertEquals(Float.parseFloat(parts[3]), density, 0.0f);
    }

    private static void assertDensityLine(String line, String label, float density) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), density, 0.0f);
    }

    private static void assertMassLine(String line, String label, b2MassData mass) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), mass.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), mass.center.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), mass.center.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), mass.rotationalInertia, 0.0f);
    }
}
