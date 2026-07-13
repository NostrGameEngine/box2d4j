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

final class ShapeDestroyWakeParityTest {
    @Test
    void destroyingShapesAndChainsWakesTouchingBodiesLikeUpstream() throws Exception {
        String[] expected = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2();

        b2WorldId worldId = b2CreateWorld(worldDef);
        b2ShapeId[] staticShape = new b2ShapeId[1];
        b2ShapeId[] dynamicShape = new b2ShapeId[1];
        makeCircleBody(worldId, b2_staticBody, new b2Vec2(), staticShape);
        b2BodyId dynamicBody = makeCircleBody(worldId, b2_dynamicBody, new b2Vec2(1.0f, 0.0f), dynamicShape);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2Body_SetAwake(dynamicBody, false);
        assertEquals(expected[0], String.format("shapeBefore %d %d", bool(b2Body_IsAwake(dynamicBody)),
            b2World_GetCounters(worldId).contactCount));
        b2DestroyShape(staticShape[0], false);
        assertEquals(expected[1], String.format("shapeAfter %d %d %d", bool(b2Body_IsAwake(dynamicBody)),
            bool(b2Shape_IsValid(staticShape[0])), b2World_GetCounters(worldId).contactCount));
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = new b2Vec2[]{new b2Vec2(-2.0f, -1.0f), new b2Vec2(2.0f, -1.0f),
            new b2Vec2(2.0f, 1.0f), new b2Vec2(-2.0f, 1.0f)};
        chainDef.count = chainDef.points.length;
        chainDef.isLoop = true;
        b2ChainId chainId = b2CreateChain(groundId, chainDef);
        dynamicBody = makeCircleBody(worldId, b2_dynamicBody, new b2Vec2(0.0f, -1.5f), dynamicShape);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2Body_SetAwake(dynamicBody, false);
        assertEquals(expected[2], String.format("chainBefore %d %d", bool(b2Body_IsAwake(dynamicBody)),
            b2World_GetCounters(worldId).contactCount));
        b2DestroyChain(chainId);
        assertEquals(expected[3], String.format("chainAfter %d %d %d", bool(b2Body_IsAwake(dynamicBody)),
            bool(b2Chain_IsValid(chainId)), b2World_GetCounters(worldId).contactCount));
        b2DestroyWorld(worldId);
    }

    private static b2BodyId makeCircleBody(b2WorldId worldId, int type, b2Vec2 position, b2ShapeId[] shapeId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = type;
        bodyDef.position = position;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = type == b2_dynamicBody ? 1.0f : 0.0f;
        shapeId[0] = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(), 1.0f));
        return bodyId;
    }

    private static int bool(boolean value) {
        return value ? 1 : 0;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_destroy_wake_probe");

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-Wall");
        command.add("-Wextra");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .map(Path::toString)
                .forEach(command::add);
        }
        command.add(root.resolve("tools/parity/box2d_shape_destroy_wake_probe.c").toString());
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
