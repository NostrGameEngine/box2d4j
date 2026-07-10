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
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_manifold_probe.c").toString());
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
}
