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

final class ShapeMaterialParityTest {
    @Test
    void shapeMaterialApiMatchesUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_material_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_material_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.25f;
        shapeDef.material.restitution = 0.125f;
        shapeDef.material.rollingResistance = 0.05f;
        shapeDef.material.tangentSpeed = 0.75f;
        shapeDef.material.userMaterialId = 7;
        shapeDef.material.customColor = 1234;
        b2ShapeId shapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));
        shapeDef.material.friction = 0.9f;

        assertMaterialLine(lines[0], "initial", b2Shape_GetFriction(shapeId), b2Shape_GetRestitution(shapeId),
            b2Shape_GetMaterial(shapeId));

        b2Shape_SetFriction(shapeId, 0.4f);
        b2Shape_SetRestitution(shapeId, 0.6f);
        b2Shape_SetMaterial(shapeId, 11);
        assertMaterialLine(lines[1], "set", b2Shape_GetFriction(shapeId), b2Shape_GetRestitution(shapeId),
            b2Shape_GetMaterial(shapeId));

        assertSurfaceLine(lines[2], "surface", b2Shape_GetSurfaceMaterial(shapeId));

        b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(shapeId);
        material.friction = 0.8f;
        material.restitution = 0.2f;
        material.rollingResistance = 0.3f;
        material.tangentSpeed = -0.5f;
        material.userMaterialId = 13;
        material.customColor = 5678;
        b2Shape_SetSurfaceMaterial(shapeId, material);
        assertSurfaceLine(lines[3], "surface2", b2Shape_GetSurfaceMaterial(shapeId));

        b2DestroyWorld(worldId);
    }

    private static void assertMaterialLine(String line, String label, float friction, float restitution, int material) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), friction, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), restitution, 0.0f);
        assertEquals(Integer.parseInt(parts[3]), material);
    }

    private static void assertSurfaceLine(String line, String label, b2SurfaceMaterial material) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), material.friction, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), material.restitution, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), material.rollingResistance, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), material.tangentSpeed, 0.0f);
        assertEquals(Integer.parseInt(parts[5]), material.userMaterialId);
        assertEquals(Integer.parseInt(parts[6]), material.customColor);
    }
}
