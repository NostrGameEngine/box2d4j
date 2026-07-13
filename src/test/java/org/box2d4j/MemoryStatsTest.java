package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MemoryStatsTest {
    @Test
    void dumpContainsUpstreamSectionsAndJavaOwnershipBoundaries() throws Exception {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId staticBody = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(staticBody, b2DefaultShapeDef(), b2MakeSquare(1.0f));
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId dynamicBody = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(dynamicBody, b2DefaultShapeDef(), new b2Circle(new b2Vec2(), 0.5f));

        b2World_DumpMemoryStats(worldId);
        String dump = Files.readString(Path.of("box2d_memory.txt"));
        b2DestroyWorld(worldId);

        assertTrue(dump.contains("id pools\n"));
        assertTrue(dump.contains("world arrays\n"));
        assertTrue(dump.contains("broad-phase\n"));
        assertTrue(dump.contains("solver sets\n"));
        assertTrue(dump.contains("constraint graph\n"));
        assertTrue(dump.contains("stack allocator: n/a (Java heap; stackUsed=0)"));
        assertTrue(dump.contains("native allocation hook\ntracked bytes:"));
        assertTrue(dump.contains("body ids: n/a (Java managed; live=2)"));
        assertTrue(dump.contains("static tree: 712"));
        assertTrue(dump.contains("dynamic tree: 712"));
        assertTrue(dump.contains("moveSet:"));
        assertTrue(dump.contains("pairSet:"));
    }
}
