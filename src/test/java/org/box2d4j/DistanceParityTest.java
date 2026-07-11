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
}
