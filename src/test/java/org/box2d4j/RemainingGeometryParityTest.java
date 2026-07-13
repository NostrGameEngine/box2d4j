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

final class RemainingGeometryParityTest {
    @Test
    void uncoveredGeometryFunctionsMatchC() throws Exception {
        String[] lines = runProbe().split("\\R");
        assertEquals(513, lines.length);
        assertEquals(lines[0], runJava());
        for (int i = 1; i < lines.length; ++i) {
            assertRandomHull(lines[i], i - 1);
        }
    }

    private static void assertRandomHull(String line, int caseIndex) {
        String[] parts = line.split("\\s+");
        assertEquals("fuzzHull", parts[0]);
        int count = Integer.parseInt(parts[1]);
        b2Vec2[] points = new b2Vec2[count];
        int cursor = 2;
        for (int i = 0; i < count; ++i) {
            points[i] = new b2Vec2(readFloat(parts[cursor++]), readFloat(parts[cursor++]));
        }
        b2Hull hull = b2ComputeHull(points, count);
        assertEquals(Integer.parseInt(parts[cursor++]), hull.count, "hull count case " + caseIndex);
        assertEquals(Integer.parseInt(parts[cursor++]) != 0, hull.count > 0 && b2ValidateHull(hull),
            "hull validity case " + caseIndex);
        for (int i = 0; i < hull.count; ++i) {
            assertEquals(Integer.parseUnsignedInt(parts[cursor++], 16), Float.floatToRawIntBits(hull.points[i].x),
                "hull point " + i + ".x case " + caseIndex);
            assertEquals(Integer.parseUnsignedInt(parts[cursor++], 16), Float.floatToRawIntBits(hull.points[i].y),
                "hull point " + i + ".y case " + caseIndex);
        }
        assertEquals(parts.length, cursor, "hull field count case " + caseIndex);
    }

    private static float readFloat(String bits) {
        return Float.intBitsToFloat(Integer.parseUnsignedInt(bits, 16));
    }

    private static String runJava() {
        b2Vec2[] points = {
            new b2Vec2(-1.25f, -0.75f), new b2Vec2(2.0f, -0.4f),
            new b2Vec2(1.4f, 1.8f), new b2Vec2(-0.8f, 1.2f)
        };
        b2Hull hull = b2ComputeHull(points, points.length);
        b2Vec2 position = new b2Vec2(3.25f, -2.75f);
        b2Rot rotation = b2MakeRot(0.37f);
        b2Polygon plain = b2MakeOffsetPolygon(hull, position, rotation);
        b2Polygon rounded = b2MakeOffsetRoundedPolygon(hull, position, rotation, 0.23f);

        b2Capsule capsule = new b2Capsule(new b2Vec2(-1.1f, 0.35f), new b2Vec2(2.2f, -0.45f), 0.6f);
        b2Transform transform = new b2Transform(new b2Vec2(-0.7f, 1.9f), b2MakeRot(-0.28f));
        b2AABB aabb = b2ComputeCapsuleAABB(capsule, transform);

        b2Hull validHull = new b2Hull();
        validHull.count = 4;
        validHull.points[0].set(-1.0f, -1.0f);
        validHull.points[1].set(1.0f, -1.0f);
        validHull.points[2].set(1.0f, 1.0f);
        validHull.points[3].set(-1.0f, 1.0f);
        b2Hull nearlyCollinearHull = new b2Hull();
        nearlyCollinearHull.count = 5;
        nearlyCollinearHull.points[0].set(-1.0f, -1.0f);
        nearlyCollinearHull.points[1].set(0.0f, -1.001f);
        nearlyCollinearHull.points[2].set(1.0f, -1.0f);
        nearlyCollinearHull.points[3].set(1.0f, 1.0f);
        nearlyCollinearHull.points[4].set(-1.0f, 1.0f);

        StringBuilder output = new StringBuilder("remainingGeometry");
        appendPolygon(output, plain);
        appendPolygon(output, rounded);
        output.append(String.format(" %08x %08x %08x %08x %d %d %d %d %d",
            Float.floatToRawIntBits(aabb.lowerBound.x), Float.floatToRawIntBits(aabb.lowerBound.y),
            Float.floatToRawIntBits(aabb.upperBound.x), Float.floatToRawIntBits(aabb.upperBound.y),
            b2PointInCapsule(new b2Vec2(0.3f, 0.1f), capsule) ? 1 : 0,
            b2PointInCapsule(new b2Vec2(2.6f, -0.45f), capsule) ? 1 : 0,
            b2PointInCapsule(new b2Vec2(3.0f, 0.5f), capsule) ? 1 : 0,
            b2ValidateHull(validHull) ? 1 : 0,
            b2ValidateHull(nearlyCollinearHull) ? 1 : 0));
        return output.toString();
    }

    private static void appendPolygon(StringBuilder output, b2Polygon polygon) {
        output.append(String.format(" %d %08x", polygon.count, Float.floatToRawIntBits(polygon.radius)));
        for (int i = 0; i < polygon.count; ++i) {
            output.append(String.format(" %08x %08x %08x %08x",
                Float.floatToRawIntBits(polygon.vertices[i].x), Float.floatToRawIntBits(polygon.vertices[i].y),
                Float.floatToRawIntBits(polygon.normals[i].x), Float.floatToRawIntBits(polygon.normals[i].y)));
        }
        output.append(String.format(" %08x %08x", Float.floatToRawIntBits(polygon.centroid.x),
            Float.floatToRawIntBits(polygon.centroid.y)));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_remaining_geometry_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted().forEach(path -> sources.add(path.toString()));
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
        command.add(root.resolve("tools/parity/box2d_remaining_geometry_probe.c").toString());
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
