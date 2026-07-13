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

final class ManifoldParityTest {
    @Test
    void singlePointManifoldsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_manifold_probe");

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
        command.add(root.resolve("tools/parity/box2d_manifold_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        assertManifold(lines[0], "circles",
            b2CollideCircles(new b2Circle(new b2Vec2(0.0f, 0.0f), 1.0f), b2Transform_identity,
                new b2Circle(new b2Vec2(1.5f, 0.0f), 1.0f), b2Transform_identity));

        assertManifold(lines[1], "capsuleCircle",
            b2CollideCapsuleAndCircle(new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.5f),
                b2Transform_identity, new b2Circle(new b2Vec2(1.2f, 0.4f), 0.25f), b2Transform_identity));

        assertManifold(lines[2], "polygonCircle",
            b2CollidePolygonAndCircle(b2MakeBox(1.0f, 1.0f), b2Transform_identity,
                new b2Circle(new b2Vec2(1.2f, 0.3f), 0.5f), b2Transform_identity));

        assertManifold(lines[3], "segmentCircle",
            b2CollideSegmentAndCircle(new b2Segment(new b2Vec2(0.0f, -1.0f), new b2Vec2(0.0f, 1.0f)),
                b2Transform_identity, new b2Circle(new b2Vec2(0.2f, 0.0f), 0.5f), b2Transform_identity));

        assertManifold(lines[4], "capsules",
            b2CollideCapsules(new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.25f),
                b2Transform_identity,
                new b2Capsule(new b2Vec2(-0.5f, 0.3f), new b2Vec2(1.5f, 0.3f), 0.25f),
                b2Transform_identity));

        assertManifold(lines[5], "polygons",
            b2CollidePolygons(b2MakeBox(1.0f, 1.0f), b2Transform_identity,
                b2MakeOffsetBox(1.0f, 1.0f, new b2Vec2(0.5f, 0.25f), b2MakeRot(0.15f)),
                b2Transform_identity));

        assertManifold(lines[6], "segmentPolygon",
            b2CollideSegmentAndPolygon(new b2Segment(new b2Vec2(0.0f, 0.0f), new b2Vec2(2.0f, 0.0f)),
                b2Transform_identity, b2MakeOffsetBox(0.35f, 0.2f, new b2Vec2(1.0f, 0.2f), b2MakeRot(0.1f)),
                b2Transform_identity));

        b2ChainSegment chain = new b2ChainSegment();
        chain.ghost1.set(-1.0f, -1.0f);
        chain.segment.point1.set(0.0f, 0.0f);
        chain.segment.point2.set(2.0f, 0.0f);
        chain.ghost2.set(3.0f, -1.0f);
        chain.chainId = 7;

        assertManifold(lines[7], "chainCircle",
            b2CollideChainSegmentAndCircle(chain, b2Transform_identity, new b2Circle(new b2Vec2(1.0f, 0.2f), 0.35f),
                b2Transform_identity));

        assertManifold(lines[8], "chainCapsule",
            b2CollideChainSegmentAndCapsule(chain, b2Transform_identity,
                new b2Capsule(new b2Vec2(0.5f, 0.25f), new b2Vec2(1.6f, 0.25f), 0.2f),
                b2Transform_identity, new b2SimplexCache()));

        assertManifold(lines[9], "chainPolygon",
            b2CollideChainSegmentAndPolygon(chain, b2Transform_identity,
                b2MakeOffsetBox(0.45f, 0.2f, new b2Vec2(1.0f, 0.25f), b2MakeRot(0.05f)),
                b2Transform_identity, new b2SimplexCache()));

        assertEquals(1034, lines.length);
        for (int i = 10; i < lines.length; ++i) {
            assertRandomizedManifold(lines[i], i - 10);
        }
    }

    private static void assertManifold(String line, String label, b2Manifold actual) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), actual.pointCount);
        assertEquals(Float.parseFloat(parts[2]), actual.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), actual.normal.y, 0.0f);

        b2ManifoldPoint point = actual.points[0];
        assertEquals(Float.parseFloat(parts[4]), point.anchorA.x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), point.anchorA.y, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), point.anchorB.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), point.anchorB.y, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), point.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), point.point.y, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), point.separation, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), point.normalImpulse, 0.0f);
        assertEquals(Float.parseFloat(parts[12]), point.tangentImpulse, 0.0f);
        assertEquals(Integer.parseInt(parts[13]), point.id);

        b2ManifoldPoint point2 = actual.points[1];
        assertEquals(Float.parseFloat(parts[14]), point2.anchorA.x, 0.0f);
        assertEquals(Float.parseFloat(parts[15]), point2.anchorA.y, 0.0f);
        assertEquals(Float.parseFloat(parts[16]), point2.anchorB.x, 0.0f);
        assertEquals(Float.parseFloat(parts[17]), point2.anchorB.y, 0.0f);
        assertEquals(Float.parseFloat(parts[18]), point2.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[19]), point2.point.y, 0.0f);
        assertEquals(Float.parseFloat(parts[20]), point2.separation, 0.0f);
        assertEquals(Integer.parseInt(parts[21]), point2.id);
    }

    private static void assertRandomizedManifold(String line, int caseIndex) {
        String[] parts = line.split("\\s+");
        assertEquals("fuzz", parts[0]);
        int type = Integer.parseInt(parts[1]);
        assertEquals(caseIndex & 7, type);
        int inputCount = Integer.parseInt(parts[2]);
        float[] input = new float[inputCount];
        int cursor = 3;
        for (int i = 0; i < inputCount; ++i) {
            input[i] = Float.intBitsToFloat(Integer.parseUnsignedInt(parts[cursor++], 16));
        }

        b2Manifold actual;
        if (type == 0) {
            b2Circle a = new b2Circle(new b2Vec2(input[0], input[1]), input[2]);
            b2Transform transformA = transform(input, 3);
            b2Circle b = new b2Circle(new b2Vec2(input[7], input[8]), input[9]);
            actual = b2CollideCircles(a, transformA, b, transform(input, 10));
        } else if (type == 1) {
            b2Capsule a = new b2Capsule(new b2Vec2(input[0], input[1]), new b2Vec2(input[2], input[3]), input[4]);
            b2Transform transformA = transform(input, 5);
            b2Circle b = new b2Circle(new b2Vec2(input[9], input[10]), input[11]);
            actual = b2CollideCapsuleAndCircle(a, transformA, b, transform(input, 12));
        } else if (type == 2) {
            b2Capsule a = new b2Capsule(new b2Vec2(input[0], input[1]), new b2Vec2(input[2], input[3]), input[4]);
            b2Transform transformA = transform(input, 5);
            b2Capsule b = new b2Capsule(new b2Vec2(input[9], input[10]), new b2Vec2(input[11], input[12]), input[13]);
            actual = b2CollideCapsules(a, transformA, b, transform(input, 14));
        } else if (type == 3) {
            b2Polygon a = b2MakeRoundedBox(input[0], input[1], input[2]);
            b2Transform transformA = transform(input, 3);
            b2Polygon b = b2MakeRoundedBox(input[7], input[8], input[9]);
            actual = b2CollidePolygons(a, transformA, b, transform(input, 10));
        } else if (type == 4) {
            b2Segment a = new b2Segment(new b2Vec2(input[0], input[1]), new b2Vec2(input[2], input[3]));
            b2Transform transformA = transform(input, 4);
            b2Circle b = new b2Circle(new b2Vec2(input[8], input[9]), input[10]);
            actual = b2CollideSegmentAndCircle(a, transformA, b, transform(input, 11));
        } else if (type == 5) {
            b2Segment a = new b2Segment(new b2Vec2(input[0], input[1]), new b2Vec2(input[2], input[3]));
            b2Transform transformA = transform(input, 4);
            b2Capsule b = new b2Capsule(new b2Vec2(input[8], input[9]), new b2Vec2(input[10], input[11]), input[12]);
            actual = b2CollideSegmentAndCapsule(a, transformA, b, transform(input, 13));
        } else if (type == 6) {
            b2Segment a = new b2Segment(new b2Vec2(input[0], input[1]), new b2Vec2(input[2], input[3]));
            b2Transform transformA = transform(input, 4);
            b2Polygon b = b2MakeRoundedBox(input[8], input[9], input[10]);
            actual = b2CollideSegmentAndPolygon(a, transformA, b, transform(input, 11));
        } else {
            b2Polygon a = b2MakeRoundedBox(input[0], input[1], input[2]);
            b2Transform transformA = transform(input, 3);
            b2Capsule b = new b2Capsule(new b2Vec2(input[7], input[8]), new b2Vec2(input[9], input[10]), input[11]);
            actual = b2CollidePolygonAndCapsule(a, transformA, b, transform(input, 12));
        }

        String label = "case " + caseIndex + " type " + type;
        assertEquals(Integer.parseInt(parts[cursor++]), actual.pointCount, label + " pointCount");
        assertFloatBits(parts[cursor++], actual.normal.x, label + " normal.x");
        assertFloatBits(parts[cursor++], actual.normal.y, label + " normal.y");
        assertFloatBits(parts[cursor++], actual.rollingImpulse, label + " rollingImpulse");
        for (int i = 0; i < 2; ++i) {
            b2ManifoldPoint point = actual.points[i];
            String[] expectedFloats = new String[11];
            for (int field = 0; field < expectedFloats.length; ++field) {
                expectedFloats[field] = parts[cursor++];
            }
            int expectedId = Integer.parseInt(parts[cursor++]);
            boolean expectedPersisted = Integer.parseInt(parts[cursor++]) != 0;
            if (i < actual.pointCount) {
                float[] actualFloats = {
                    point.point.x, point.point.y,
                    point.anchorA.x, point.anchorA.y,
                    point.anchorB.x, point.anchorB.y,
                    point.separation, point.normalImpulse, point.tangentImpulse,
                    point.totalNormalImpulse, point.normalVelocity
                };
                String[] names = {
                    "point.x", "point.y", "anchorA.x", "anchorA.y", "anchorB.x", "anchorB.y",
                    "separation", "normalImpulse", "tangentImpulse", "totalNormalImpulse", "normalVelocity"
                };
                for (int field = 0; field < actualFloats.length; ++field) {
                    assertFloatBits(expectedFloats[field], actualFloats[field],
                        label + " point[" + i + "]." + names[field]);
                }
                assertEquals(expectedId, point.id, label + " point[" + i + "].id");
                assertEquals(expectedPersisted, point.persisted, label + " point[" + i + "].persisted");
            }
        }
        assertEquals(parts.length, cursor, label + " field count");
    }

    private static b2Transform transform(float[] input, int offset) {
        return new b2Transform(new b2Vec2(input[offset], input[offset + 1]),
            new b2Rot(input[offset + 2], input[offset + 3]));
    }

    private static void assertFloatBits(String expected, float actual, String label) {
        assertEquals(Integer.parseUnsignedInt(expected, 16), Float.floatToRawIntBits(actual), label);
    }
}
