package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class DebugDrawParityTest {
    @Test
    void debugDrawCallbacksMatchUpstreamC() throws Exception {
        String upstream = runProbe();

        List<String> lines = new ArrayList<>();
        int[] counts = new int[8];
        b2DebugDraw draw = b2DefaultDebugDraw();
        draw.DrawPolygonFcn = (vertices, vertexCount, color) -> {
            counts[0] += 1;
            lines.add(format("polygon %d %.9g %.9g %.9g %.9g %06x", vertexCount, vertices[0].x, vertices[0].y,
                vertices[2].x, vertices[2].y, color));
        };
        draw.DrawSolidPolygonFcn = (transform, vertices, vertexCount, radius, color) -> {
            counts[1] += 1;
            lines.add(format("solidPolygon %d %.9g %.9g %.9g %.9g %.9g %.9g %06x", vertexCount, transform.p.x,
                transform.p.y, transform.q.c, transform.q.s, radius, vertices[0].x, color));
        };
        draw.DrawSolidCircleFcn = (transform, radius, color) -> {
            counts[2] += 1;
            lines.add(format("solidCircle %.9g %.9g %.9g %.9g %.9g %06x", transform.p.x, transform.p.y,
                transform.q.c, transform.q.s, radius, color));
        };
        draw.DrawSolidCapsuleFcn = (p1, p2, radius, color) -> {
            counts[3] += 1;
            lines.add(format("solidCapsule %.9g %.9g %.9g %.9g %.9g %06x", p1.x, p1.y, p2.x, p2.y, radius, color));
        };
        draw.DrawSegmentFcn = (p1, p2, color) -> {
            counts[4] += 1;
            lines.add(format("segment %.9g %.9g %.9g %.9g %06x", p1.x, p1.y, p2.x, p2.y, color));
        };
        draw.DrawTransformFcn = transform -> {
            counts[5] += 1;
            lines.add(format("transform %.9g %.9g %.9g %.9g", transform.p.x, transform.p.y, transform.q.c, transform.q.s));
        };
        draw.DrawPointFcn = (p, size, color) -> {
            counts[6] += 1;
            lines.add(format("point %.9g %.9g %.9g %06x", p.x, p.y, size, color));
        };
        draw.DrawStringFcn = (p, text, color) -> {
            counts[7] += 1;
            lines.add(format("string %.9g %.9g %s %06x", p.x, p.y, text, color));
        };
        draw.drawShapes = true;
        draw.drawBounds = true;
        draw.drawBodyNames = true;
        draw.drawMass = true;

        b2WorldId worldId = createDrawWorld();
        b2World_Draw(worldId, draw);
        lines.add(format("counts %d %d %d %d %d %d %d %d", counts[0], counts[1], counts[2], counts[3],
            counts[4], counts[5], counts[6], counts[7]));
        b2DestroyWorld(worldId);

        assertEquals(normalizeNumericTokens(upstream), normalizeNumericTokens(String.join("\n", lines)));
    }

    private static b2WorldId createDrawWorld() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, 0.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.name = "ground";
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(groundId, shapeDef, b2MakeBox(1.0f, 0.5f));
        b2CreateSegmentShape(groundId, shapeDef, new b2Segment(new b2Vec2(-2.0f, 1.0f), new b2Vec2(-1.0f, 1.5f)));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.name = "ball";
        bodyDef.position = new b2Vec2(2.0f, 3.0f);
        bodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId dynamicId = b2CreateBody(worldId, bodyDef);
        shapeDef.density = 1.0f;
        b2CreateCircleShape(dynamicId, shapeDef, new b2Circle(new b2Vec2(0.25f, -0.5f), 0.75f));
        b2CreateCapsuleShape(dynamicId, shapeDef, new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.2f));
        return worldId;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_debug_draw_probe");

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
        command.add(root.resolve("tools/parity/box2d_debug_draw_probe.c").toString());
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

    private static String format(String pattern, Object... args) {
        return String.format(Locale.ROOT, pattern, args);
    }

    private static String normalizeNumericTokens(String value) {
        String[] lines = value.split("\\R");
        List<String> normalizedLines = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            for (int i = 0; i < parts.length; ++i) {
                if (parts[i].matches("[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][-+]?\\d+)?")) {
                    parts[i] = Float.toString(Float.parseFloat(parts[i]));
                }
            }
            normalizedLines.add(String.join(" ", parts));
        }
        return String.join("\n", normalizedLines);
    }
}
