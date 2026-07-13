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

final class DistanceParityTest {
    @Test
    void distanceAlgorithmsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_distance_probe");

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
        command.add(root.resolve("tools/parity/box2d_distance_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2SegmentDistanceResult segment = b2SegmentDistance(
            new b2Vec2(-1.0f, -1.0f), new b2Vec2(-1.0f, 1.0f),
            new b2Vec2(2.0f, 0.0f), new b2Vec2(1.0f, 0.0f));
        String[] parts = lines[0].split("\\s+");
        assertEquals("segment", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), segment.fraction1, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), segment.fraction2, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), segment.closest1.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), segment.closest1.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), segment.closest2.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), segment.closest2.y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), segment.distanceSquared, 0.0f);

        b2Vec2[] vas = {new b2Vec2(-1.0f, -1.0f), new b2Vec2(1.0f, -1.0f), new b2Vec2(1.0f, 1.0f), new b2Vec2(-1.0f, 1.0f)};
        b2Vec2[] vbs = {new b2Vec2(2.0f, -1.0f), new b2Vec2(2.0f, 1.0f)};

        b2DistanceInput distanceInput = new b2DistanceInput();
        distanceInput.proxyA = b2MakeProxy(vas, vas.length, 0.0f);
        distanceInput.proxyB = b2MakeProxy(vbs, vbs.length, 0.0f);
        distanceInput.transformA = b2Transform_identity;
        distanceInput.transformB = b2Transform_identity;
        b2DistanceOutput distance = b2ShapeDistance(distanceInput, new b2SimplexCache(), null, 0);

        parts = lines[1].split("\\s+");
        assertEquals("distance", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), distance.distance, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), distance.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), distance.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), distance.pointA.x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), distance.pointA.y, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), distance.pointB.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), distance.pointB.y, 0.0f);
        assertEquals(Integer.parseInt(parts[8]), distance.iterations);

        b2ShapeCastPairInput castInput = new b2ShapeCastPairInput();
        castInput.proxyA = b2MakeProxy(vas, vas.length, 0.0f);
        castInput.proxyB = b2MakeProxy(vbs, vbs.length, 0.0f);
        castInput.transformA = b2Transform_identity;
        castInput.transformB = b2Transform_identity;
        castInput.translationB = new b2Vec2(-2.0f, 0.0f);
        castInput.maxFraction = 1.0f;
        b2CastOutput cast = b2ShapeCast(castInput);

        parts = lines[2].split("\\s+");
        assertEquals("cast", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) == 1, cast.hit);
        assertEquals(Float.parseFloat(parts[2]), cast.fraction, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), cast.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), cast.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), cast.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), cast.point.y, 0.0f);
        assertEquals(Integer.parseInt(parts[8]), cast.iterations);

        b2TOIInput toiInput = new b2TOIInput();
        toiInput.proxyA = b2MakeProxy(vas, vas.length, 0.0f);
        toiInput.proxyB = b2MakeProxy(vbs, vbs.length, 0.0f);
        toiInput.sweepA.localCenter = b2Vec2_zero.copy();
        toiInput.sweepA.c1 = b2Vec2_zero.copy();
        toiInput.sweepA.c2 = b2Vec2_zero.copy();
        toiInput.sweepA.q1 = b2Rot_identity.copy();
        toiInput.sweepA.q2 = b2Rot_identity.copy();
        toiInput.sweepB.localCenter = b2Vec2_zero.copy();
        toiInput.sweepB.c1 = b2Vec2_zero.copy();
        toiInput.sweepB.c2 = new b2Vec2(-2.0f, 0.0f);
        toiInput.sweepB.q1 = b2Rot_identity.copy();
        toiInput.sweepB.q2 = b2Rot_identity.copy();
        toiInput.maxFraction = 1.0f;
        b2TOIOutput toi = b2TimeOfImpact(toiInput);

        parts = lines[3].split("\\s+");
        assertEquals("toi", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), toi.state);
        assertEquals(Float.parseFloat(parts[2]), toi.fraction, 0.0f);

        b2Vec2[] castProxyPoints = {new b2Vec2(2.0f, -0.4f), new b2Vec2(2.0f, 0.4f)};
        b2ShapeCastInput shapeCastInput = new b2ShapeCastInput();
        shapeCastInput.proxy = b2MakeProxy(castProxyPoints, castProxyPoints.length, 0.1f);
        shapeCastInput.translation = new b2Vec2(-2.0f, 0.0f);
        shapeCastInput.maxFraction = 1.0f;

        assertCast(lines[4], "castCircle", b2ShapeCastCircle(shapeCastInput, new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f)));
        assertCast(lines[5], "castCapsule",
            b2ShapeCastCapsule(shapeCastInput, new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f)));
        assertCast(lines[6], "castSegment",
            b2ShapeCastSegment(shapeCastInput, new b2Segment(new b2Vec2(-0.5f, -0.5f), new b2Vec2(-0.5f, 0.5f))));
        assertCast(lines[7], "castPolygon", b2ShapeCastPolygon(shapeCastInput, b2MakeBox(0.5f, 0.5f)));

        assertEquals(2056, lines.length);
        for (int i = 8; i < lines.length; ++i) {
            assertRandomizedCase(lines[i], i - 8);
        }
    }

    private static void assertCast(String line, String label, b2CastOutput cast) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]) == 1, cast.hit);
        assertEquals(Float.parseFloat(parts[2]), cast.fraction, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), cast.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), cast.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), cast.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), cast.point.y, 0.0f);
        assertEquals(Integer.parseInt(parts[8]), cast.iterations);
    }

    private static void assertRandomizedCase(String line, int caseIndex) {
        String[] parts = line.split("\\s+");
        int[] cursor = {1};
        if (parts[0].equals("fuzzSegment")) {
            assertEquals(caseIndex < 512, true);
            b2Vec2 p1 = readVec2(parts, cursor);
            b2Vec2 q1 = readVec2(parts, cursor);
            b2Vec2 p2 = readVec2(parts, cursor);
            b2Vec2 q2 = readVec2(parts, cursor);
            b2SegmentDistanceResult output = b2SegmentDistance(p1, q1, p2, q2);
            assertBits(parts[cursor[0]++], output.fraction1, caseIndex, "fraction1");
            assertBits(parts[cursor[0]++], output.fraction2, caseIndex, "fraction2");
            assertVec2(parts, cursor, output.closest1, caseIndex, "closest1");
            assertVec2(parts, cursor, output.closest2, caseIndex, "closest2");
            assertBits(parts[cursor[0]++], output.distanceSquared, caseIndex, "distanceSquared");
        } else if (parts[0].equals("fuzzDistance")) {
            b2DistanceInput input = new b2DistanceInput();
            input.proxyA = readProxy(parts, cursor);
            input.transformA = readTransform(parts, cursor);
            input.proxyB = readProxy(parts, cursor);
            input.transformB = readTransform(parts, cursor);
            input.useRadii = Integer.parseInt(parts[cursor[0]++]) != 0;
            b2DistanceOutput output = b2ShapeDistance(input, new b2SimplexCache(), null, 0);
            assertVec2(parts, cursor, output.pointA, caseIndex, "pointA");
            assertVec2(parts, cursor, output.pointB, caseIndex, "pointB");
            assertVec2(parts, cursor, output.normal, caseIndex, "normal");
            assertBits(parts[cursor[0]++], output.distance, caseIndex, "distance");
            assertEquals(Integer.parseInt(parts[cursor[0]++]), output.iterations, label(caseIndex, "iterations"));
            assertEquals(Integer.parseInt(parts[cursor[0]++]), output.simplexCount, label(caseIndex, "simplexCount"));
        } else if (parts[0].equals("fuzzWarmDistance")) {
            b2DistanceInput input = new b2DistanceInput();
            input.proxyA = readProxy(parts, cursor);
            input.transformA = readTransform(parts, cursor);
            input.proxyB = readProxy(parts, cursor);
            input.transformB = readTransform(parts, cursor);
            input.useRadii = Integer.parseInt(parts[cursor[0]++]) != 0;
            b2SimplexCache cache = new b2SimplexCache();
            b2ShapeDistance(input, cache, null, 0);
            b2DistanceOutput output = b2ShapeDistance(input, cache, null, 0);
            assertVec2(parts, cursor, output.pointA, caseIndex, "pointA");
            assertVec2(parts, cursor, output.pointB, caseIndex, "pointB");
            assertVec2(parts, cursor, output.normal, caseIndex, "normal");
            assertBits(parts[cursor[0]++], output.distance, caseIndex, "distance");
            assertEquals(Integer.parseInt(parts[cursor[0]++]), output.iterations, label(caseIndex, "iterations"));
            assertEquals(Integer.parseInt(parts[cursor[0]++]), output.simplexCount, label(caseIndex, "simplexCount"));
            assertEquals(Integer.parseInt(parts[cursor[0]++]), cache.count, label(caseIndex, "cache.count"));
            for (int i = 0; i < 3; ++i) {
                assertEquals(Integer.parseInt(parts[cursor[0]++]), Byte.toUnsignedInt(cache.indexA[i]),
                    label(caseIndex, "cache.indexA[" + i + "]"));
                assertEquals(Integer.parseInt(parts[cursor[0]++]), Byte.toUnsignedInt(cache.indexB[i]),
                    label(caseIndex, "cache.indexB[" + i + "]"));
            }
        } else if (parts[0].equals("fuzzCast")) {
            b2ShapeCastPairInput input = new b2ShapeCastPairInput();
            input.proxyA = readProxy(parts, cursor);
            input.transformA = readTransform(parts, cursor);
            input.proxyB = readProxy(parts, cursor);
            input.transformB = readTransform(parts, cursor);
            input.translationB = readVec2(parts, cursor);
            input.maxFraction = readFloat(parts[cursor[0]++]);
            input.canEncroach = Integer.parseInt(parts[cursor[0]++]) != 0;
            b2CastOutput output = b2ShapeCast(input);
            assertEquals(Integer.parseInt(parts[cursor[0]++]) != 0, output.hit, label(caseIndex, "hit"));
            assertVec2(parts, cursor, output.point, caseIndex, "point");
            assertVec2(parts, cursor, output.normal, caseIndex, "normal");
            assertBits(parts[cursor[0]++], output.fraction, caseIndex, "fraction");
            assertEquals(Integer.parseInt(parts[cursor[0]++]), output.iterations, label(caseIndex, "iterations"));
        } else {
            assertEquals("fuzzToi", parts[0]);
            b2TOIInput input = new b2TOIInput();
            input.proxyA = readProxy(parts, cursor);
            input.sweepA = readSweep(parts, cursor);
            input.proxyB = readProxy(parts, cursor);
            input.sweepB = readSweep(parts, cursor);
            input.maxFraction = readFloat(parts[cursor[0]++]);
            b2TOIOutput output = b2TimeOfImpact(input);
            assertEquals(Integer.parseInt(parts[cursor[0]++]), output.state, label(caseIndex, "state"));
            assertBits(parts[cursor[0]++], output.fraction, caseIndex, "fraction");
        }
        assertEquals(parts.length, cursor[0], label(caseIndex, "field count"));
    }

    private static b2ShapeProxy readProxy(String[] parts, int[] cursor) {
        int type = Integer.parseInt(parts[cursor[0]++]);
        float x = readFloat(parts[cursor[0]++]);
        float y = readFloat(parts[cursor[0]++]);
        float radius = readFloat(parts[cursor[0]++]);
        if (type == 0) {
            return b2MakeProxy(new b2Vec2[] {new b2Vec2()}, 1, radius);
        }
        if (type == 1) {
            return b2MakeProxy(new b2Vec2[] {new b2Vec2(-x, 0.0f), new b2Vec2(x, 0.0f)}, 2, radius);
        }
        b2Polygon box = b2MakeBox(x, y);
        return b2MakeProxy(box.vertices, box.count, radius);
    }

    private static b2Transform readTransform(String[] parts, int[] cursor) {
        return new b2Transform(readVec2(parts, cursor),
            new b2Rot(readFloat(parts[cursor[0]++]), readFloat(parts[cursor[0]++])));
    }

    private static b2Sweep readSweep(String[] parts, int[] cursor) {
        b2Sweep sweep = new b2Sweep();
        sweep.localCenter = new b2Vec2();
        sweep.c1 = readVec2(parts, cursor);
        sweep.c2 = readVec2(parts, cursor);
        sweep.q1 = new b2Rot(readFloat(parts[cursor[0]++]), readFloat(parts[cursor[0]++]));
        sweep.q2 = new b2Rot(readFloat(parts[cursor[0]++]), readFloat(parts[cursor[0]++]));
        return sweep;
    }

    private static b2Vec2 readVec2(String[] parts, int[] cursor) {
        return new b2Vec2(readFloat(parts[cursor[0]++]), readFloat(parts[cursor[0]++]));
    }

    private static float readFloat(String value) {
        return Float.intBitsToFloat(Integer.parseUnsignedInt(value, 16));
    }

    private static void assertVec2(String[] parts, int[] cursor, b2Vec2 actual, int caseIndex, String field) {
        assertBits(parts[cursor[0]++], actual.x, caseIndex, field + ".x");
        assertBits(parts[cursor[0]++], actual.y, caseIndex, field + ".y");
    }

    private static void assertBits(String expected, float actual, int caseIndex, String field) {
        assertEquals(Integer.parseUnsignedInt(expected, 16), Float.floatToRawIntBits(actual), label(caseIndex, field));
    }

    private static String label(int caseIndex, String field) {
        return "randomized case " + caseIndex + " " + field;
    }
}
