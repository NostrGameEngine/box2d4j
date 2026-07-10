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

final class ContactDataParityTest {
    @Test
    void helloWorldContactDataCountsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_contact_data_probe");

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
        command.add(root.resolve("tools/parity/box2d_contact_data_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundBodyDef = b2DefaultBodyDef();
        groundBodyDef.position = new b2Vec2(0.0f, -10.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundBodyDef);
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        groundShapeDef.enableContactEvents = true;
        b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, groundShapeDef, b2MakeBox(50.0f, 10.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 4.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.3f;
        shapeDef.enableContactEvents = true;
        b2ShapeId dynamicShapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

        int beginTotal = 0;
        int endTotal = 0;
        for (int i = 0; i < 90; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            b2ContactEvents events = b2World_GetContactEvents(worldId);
            beginTotal += events.beginCount;
            endTotal += events.endCount;
        }

        b2ContactData[] contactData = new b2ContactData[4];
        int bodyCount = b2Body_GetContactData(bodyId, contactData, contactData.length);

        String[] parts = lines[0].split("\\s+");
        assertEquals("contacts", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), b2Body_GetContactCapacity(bodyId));
        assertEquals(Integer.parseInt(parts[2]), b2Shape_GetContactCapacity(dynamicShapeId));
        assertEquals(Integer.parseInt(parts[3]), b2Shape_GetContactCapacity(groundShapeId));
        assertEquals(Integer.parseInt(parts[4]), bodyCount);
        assertEquals(Integer.parseInt(parts[5]), beginTotal);
        assertEquals(Integer.parseInt(parts[6]), endTotal);

        parts = lines[1].split("\\s+");
        assertEquals("manifold", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), bodyCount > 0 ? contactData[0].manifold.pointCount : 0);

        b2DestroyWorld(worldId);
    }
}
