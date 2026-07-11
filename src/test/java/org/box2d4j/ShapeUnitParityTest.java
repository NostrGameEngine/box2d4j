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

final class ShapeUnitParityTest {
    @Test
    void upstreamShapeUnitSlicesMatchC() throws Exception {
        String upstream = runProbe();

        b2Capsule capsule = new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 1.0f);
        b2Circle circle = new b2Circle(new b2Vec2(1.0f, 0.0f), 1.0f);
        b2Polygon box = b2MakeBox(1.0f, 1.0f);
        b2Segment segment = new b2Segment(new b2Vec2(0.0f, 1.0f), new b2Vec2(0.0f, -1.0f));

        List<String> actual = new ArrayList<>();
        addMass(actual, "massCircle", b2ComputeCircleMass(circle, 1.0f));

        float radius = capsule.radius;
        float length = b2Distance(capsule.center1, capsule.center2);
        b2MassData capsuleMass = b2ComputeCapsuleMass(capsule, 1.0f);
        b2MassData containingBoxMass = b2ComputePolygonMass(b2MakeBox(radius, radius + 0.5f * length), 1.0f);

        b2Vec2[] points = b2Vec2.array(8);
        float d = B2_PI / 3.0f;
        float angle = -0.5f * B2_PI;
        for (int i = 0; i < 4; ++i) {
            points[i].x = 1.0f + radius * (float) Math.cos(angle);
            points[i].y = radius * (float) Math.sin(angle);
            angle += d;
        }

        angle = 0.5f * B2_PI;
        for (int i = 4; i < 8; ++i) {
            points[i].x = -1.0f + radius * (float) Math.cos(angle);
            points[i].y = radius * (float) Math.sin(angle);
            angle += d;
        }

        b2MassData approximateMass = b2ComputePolygonMass(b2MakePolygon(b2ComputeHull(points, 8), 0.0f), 1.0f);
        addMass(actual, "massCapsule", capsuleMass);
        addMass(actual, "massCapsuleContainingBox", containingBoxMass);
        actual.add(format("massCapsuleBounds %d %d",
            approximateMass.mass < capsuleMass.mass && capsuleMass.mass < containingBoxMass.mass ? 1 : 0,
            approximateMass.rotationalInertia < capsuleMass.rotationalInertia
                && capsuleMass.rotationalInertia < containingBoxMass.rotationalInertia ? 1 : 0));
        addMass(actual, "massBox", b2ComputePolygonMass(box, 1.0f));

        addAabb(actual, "aabbCircle", b2ComputeCircleAABB(circle, b2Transform_identity));
        addAabb(actual, "aabbBox", b2ComputePolygonAABB(box, b2Transform_identity));
        addAabb(actual, "aabbSegment", b2ComputeSegmentAABB(segment, b2Transform_identity));

        b2Vec2 p1 = new b2Vec2(0.5f, 0.5f);
        b2Vec2 p2 = new b2Vec2(4.0f, -4.0f);
        actual.add(format("pointCircle %d %d", b2PointInCircle(p1, circle) ? 1 : 0, b2PointInCircle(p2, circle) ? 1 : 0));
        actual.add(format("pointPolygon %d %d", b2PointInPolygon(p1, box) ? 1 : 0, b2PointInPolygon(p2, box) ? 1 : 0));

        b2RayCastInput input = new b2RayCastInput();
        input.origin = new b2Vec2(-4.0f, 0.0f);
        input.translation = new b2Vec2(8.0f, 0.0f);
        input.maxFraction = 1.0f;
        addCast(actual, "rayCircle", b2RayCastCircle(input, circle));
        addCast(actual, "rayPolygon", b2RayCastPolygon(input, box));
        addCast(actual, "raySegment", b2RayCastSegment(input, segment, true));

        assertEquals(normalizeNumericTokens(upstream), normalizeNumericTokens(String.join("\n", actual)));
    }

    private static void addMass(List<String> lines, String label, b2MassData md) {
        lines.add(format("%s %.9g %.9g %.9g %.9g", label, md.mass, md.center.x, md.center.y, md.rotationalInertia));
    }

    private static void addAabb(List<String> lines, String label, b2AABB aabb) {
        lines.add(format("%s %.9g %.9g %.9g %.9g", label, aabb.lowerBound.x, aabb.lowerBound.y, aabb.upperBound.x,
            aabb.upperBound.y));
    }

    private static void addCast(List<String> lines, String label, b2CastOutput output) {
        lines.add(format("%s %d %.9g %.9g %.9g", label, output.hit ? 1 : 0, output.fraction, output.normal.x,
            output.normal.y));
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

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_unit_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_unit_probe.c").toString());
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
