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

final class WorldQueryParityTest {
    @Test
    void worldQueriesMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_world_query_probe");

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
        command.add(root.resolve("tools/parity/box2d_world_query_probe.c").toString());
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
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = 0x0002L;
        shapeDef.filter.maskBits = 0xFFFFL;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(-2.0f, 0.0f);
        b2BodyId circleBody = b2CreateBody(worldId, bodyDef);
        b2ShapeId circleId = b2CreateCircleShape(circleBody, shapeDef, new b2Circle(new b2Vec2(), 0.5f));

        bodyDef.position = new b2Vec2(1.0f, 0.0f);
        b2BodyId boxBody = b2CreateBody(worldId, bodyDef);
        b2ShapeId boxId = b2CreatePolygonShape(boxBody, shapeDef, b2MakeBox(0.5f, 0.5f));

        shapeDef.filter.categoryBits = 0x0004L;
        bodyDef.position = new b2Vec2(3.0f, 0.0f);
        b2BodyId capsuleBody = b2CreateBody(worldId, bodyDef);
        b2ShapeId capsuleId = b2CreateCapsuleShape(capsuleBody, shapeDef,
            new b2Capsule(new b2Vec2(-0.25f, 0.0f), new b2Vec2(0.25f, 0.0f), 0.25f));
        assertEquals("ids " + circleId.index1 + " " + boxId.index1 + " " + capsuleId.index1, lines[0]);

        b2QueryFilter filter = b2DefaultQueryFilter();
        filter.categoryBits = 0x0002L;
        filter.maskBits = 0x0002L;

        List<Integer> hits = new ArrayList<>();
        b2TreeStats stats = b2World_OverlapAABB(worldId, new b2AABB(new b2Vec2(-3.0f, -1.0f), new b2Vec2(4.0f, 1.0f)),
            filter, shapeId -> {
                hits.add(shapeId.index1);
                return true;
            });
        assertQueryLine(lines[1], "overlapAABB", hits, stats);

        b2ShapeProxy proxy = b2MakeProxy(new b2Vec2[] {new b2Vec2(-0.25f, -0.25f), new b2Vec2(1.25f, -0.25f),
            new b2Vec2(1.25f, 0.25f), new b2Vec2(-0.25f, 0.25f)}, 4, 0.0f);
        hits.clear();
        stats = b2World_OverlapShape(worldId, proxy, filter, shapeId -> {
            hits.add(shapeId.index1);
            return true;
        });
        assertQueryLine(lines[2], "overlapShape", hits, stats);

        b2RayResult ray = b2World_CastRayClosest(worldId, new b2Vec2(-4.0f, 0.0f), new b2Vec2(8.0f, 0.0f), filter);
        assertClosestLine(lines[3], ray);

        hits.clear();
        List<CastHit> castHits = new ArrayList<>();
        stats = b2World_CastRay(worldId, new b2Vec2(-4.0f, 0.0f), new b2Vec2(8.0f, 0.0f), filter,
            (shapeId, point, normal, fraction) -> {
                hits.add(shapeId.index1);
                castHits.add(new CastHit(shapeId.index1, point, normal, fraction));
                return fraction;
            });
        assertCastHitLine(lines[4], castHits.get(0));
        assertQueryLine(lines[5], "castRay", hits, stats);

        b2ShapeProxy castProxy = b2MakeProxy(new b2Vec2[] {new b2Vec2(-4.0f, 0.0f)}, 1, 0.1f);
        hits.clear();
        castHits.clear();
        stats = b2World_CastShape(worldId, castProxy, new b2Vec2(8.0f, 0.0f), filter,
            (shapeId, point, normal, fraction) -> {
                hits.add(shapeId.index1);
                castHits.add(new CastHit(shapeId.index1, point, normal, fraction));
                return fraction;
            });
        assertCastHitLine(lines[6], castHits.get(0));
        assertQueryLine(lines[7], "castShape", hits, stats);

        b2DestroyWorld(worldId);
    }

    private static void assertQueryLine(String line, String label, List<Integer> hits, b2TreeStats stats) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(hits.size(), Integer.parseInt(parts[1]));
        for (int i = 0; i < hits.size(); ++i) {
            assertEquals(hits.get(i), Integer.parseInt(parts[2 + i]));
        }
        assertEquals("stats", parts[2 + hits.size()]);
        assertEquals(stats.nodeVisits, Integer.parseInt(parts[3 + hits.size()]));
        assertEquals(stats.leafVisits, Integer.parseInt(parts[4 + hits.size()]));
    }

    private static void assertClosestLine(String line, b2RayResult ray) {
        String[] parts = line.split("\\s+");
        assertEquals("closest", parts[0]);
        assertEquals(ray.hit, Integer.parseInt(parts[1]) != 0);
        assertEquals(ray.shapeId.index1, Integer.parseInt(parts[2]));
        assertEquals(Float.parseFloat(parts[3]), ray.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), ray.point.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), ray.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), ray.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), ray.fraction, 0.0f);
        assertEquals("stats", parts[8]);
        assertEquals(ray.nodeVisits, Integer.parseInt(parts[9]));
        assertEquals(ray.leafVisits, Integer.parseInt(parts[10]));
    }

    private static void assertCastHitLine(String line, CastHit hit) {
        String[] parts = line.split("\\s+");
        assertEquals("castHit", parts[0]);
        assertEquals(hit.shapeIndex, Integer.parseInt(parts[1]));
        assertEquals(Float.parseFloat(parts[2]), hit.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), hit.point.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), hit.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), hit.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), hit.fraction, 0.0f);
    }

    private static final class CastHit {
        final int shapeIndex;
        final b2Vec2 point;
        final b2Vec2 normal;
        final float fraction;

        CastHit(int shapeIndex, b2Vec2 point, b2Vec2 normal, float fraction) {
            this.shapeIndex = shapeIndex;
            this.point = point.copy();
            this.normal = normal.copy();
            this.fraction = fraction;
        }
    }
}
