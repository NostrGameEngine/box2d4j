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

final class ContactEventEnableParityTest {
    @Test
    void contactEventFlagIsCapturedWhenTheContactIsCreated() throws Exception {
        assertEquals(runProbe(), "contactEventEnable" + runCase(false) + runCase(true)
            + runDestroyCase(false) + runDestroyCase(true));
    }

    private static String runCase(boolean initialFlag) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(2.0f, 0.5f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.08f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enableContactEvents = initialFlag;
        b2ShapeId shapeId = b2CreateCircleShape(bodyId, shapeDef,
            new b2Circle(new b2Vec2(), 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        int capacityBeforeTouch = b2Body_GetContactCapacity(bodyId);

        b2Shape_EnableContactEvents(shapeId, !initialFlag);
        b2Body_SetTransform(bodyId, new b2Vec2(0.0f, 0.9f), b2Rot_identity);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        int beginCount = b2World_GetContactEvents(worldId).beginCount;

        b2Body_SetTransform(bodyId, new b2Vec2(0.0f, 2.0f), b2Rot_identity);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        int endCount = b2World_GetContactEvents(worldId).endCount;
        b2DestroyWorld(worldId);
        return String.format(" %d %d %d %d", initialFlag ? 1 : 0, capacityBeforeTouch, beginCount, endCount);
    }

    private static String runDestroyCase(boolean destroyBody) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(2.0f, 0.5f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 0.9f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enableContactEvents = true;
        b2ShapeId shapeId = b2CreateCircleShape(bodyId, shapeDef,
            new b2Circle(new b2Vec2(), 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        int beginCount = b2World_GetContactEvents(worldId).beginCount;
        if (destroyBody) {
            b2DestroyBody(bodyId);
        } else {
            b2DestroyShape(shapeId, false);
        }
        int immediateEndCount = b2World_GetContactEvents(worldId).endCount;
        b2World_Step(worldId, 0.0f, 1);
        int deferredEndCount = b2World_GetContactEvents(worldId).endCount;
        b2DestroyWorld(worldId);
        return String.format(" %d %d %d %d", destroyBody ? 1 : 0, beginCount, immediateEndCount,
            deferredEndCount);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_contact_event_enable_probe");

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
        command.add(root.resolve("tools/parity/box2d_contact_event_enable_probe.c").toString());
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
