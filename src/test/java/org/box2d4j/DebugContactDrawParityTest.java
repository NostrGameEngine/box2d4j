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

final class DebugContactDrawParityTest {
    @Test
    void debugContactCallbacksMatchUpstreamC() throws Exception {
        String upstream = runProbe();

        List<String> lines = new ArrayList<>();
        int[] counts = new int[3];
        b2DebugDraw draw = b2DefaultDebugDraw();
        draw.DrawSegmentFcn = (p1, p2, color) -> {
            counts[0] += 1;
            lines.add(format("segment %.9g %.9g %.9g %.9g %06x", p1.x, p1.y, p2.x, p2.y, color));
        };
        draw.DrawPointFcn = (p, size, color) -> {
            counts[1] += 1;
            lines.add(format("point %.9g %.9g %.9g %06x", p.x, p.y, size, color));
        };
        draw.DrawStringFcn = (p, text, color) -> {
            counts[2] += 1;
            lines.add(format("string %.9g %.9g %s %06x", p.x, p.y, text, color));
        };
        draw.drawContacts = true;
        draw.drawContactImpulses = true;
        draw.drawContactFeatures = true;
        draw.drawFrictionImpulses = true;

        b2WorldId worldId = createContactWorld();
        b2World_Draw(worldId, draw);
        lines.add(format("counts %d %d %d", counts[0], counts[1], counts[2]));

        counts[0] = 0;
        counts[1] = 0;
        counts[2] = 0;
        draw.drawContactImpulses = false;
        draw.drawContactFeatures = false;
        draw.drawFrictionImpulses = false;
        draw.drawContactNormals = true;
        draw.drawGraphColors = true;
        b2World_Draw(worldId, draw);
        lines.add(format("countsGraphNormals %d %d %d", counts[0], counts[1], counts[2]));

        b2DestroyWorld(worldId);

        assertEquals(normalizeNumericTokens(upstream), normalizeNumericTokens(String.join("\n", lines)));
    }

    private static b2WorldId createContactWorld() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -0.5f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        groundShapeDef.material.friction = 0.7f;
        b2CreatePolygonShape(groundId, groundShapeDef, b2MakeBox(3.0f, 0.5f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 0.45f);
        bodyDef.linearVelocity = new b2Vec2(1.5f, -1.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.5f;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.4f, 0.4f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        return worldId;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_debug_contact_draw_probe");

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
        command.add(root.resolve("tools/parity/box2d_debug_contact_draw_probe.c").toString());
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
