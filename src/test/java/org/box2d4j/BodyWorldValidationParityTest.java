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

final class BodyWorldValidationParityTest {
    @Test
    void bodyAndWorldInputAssertionsMatchUpstream() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2QueryFilter filter = b2DefaultQueryFilter();
        b2ShapeProxy proxy = b2MakeProxy(new b2Vec2[] {new b2Vec2()}, 1, 0.0f);

        List<Runnable> cases = new ArrayList<>();
        cases.add(() -> createBody(worldId, def -> def.position.x = Float.NaN));
        cases.add(() -> createBody(worldId, def -> def.rotation = new b2Rot(0.0f, 0.0f)));
        cases.add(() -> createBody(worldId, def -> def.linearVelocity.y = Float.POSITIVE_INFINITY));
        cases.add(() -> createBody(worldId, def -> def.angularVelocity = Float.NaN));
        cases.add(() -> createBody(worldId, def -> def.linearDamping = -1.0f));
        cases.add(() -> createBody(worldId, def -> def.angularDamping = Float.NaN));
        cases.add(() -> createBody(worldId, def -> def.sleepThreshold = -1.0f));
        cases.add(() -> createBody(worldId, def -> def.gravityScale = Float.POSITIVE_INFINITY));
        cases.add(() -> b2Body_SetTransform(bodyId, new b2Vec2(Float.NaN, 0.0f), b2Rot_identity));
        cases.add(() -> b2Body_SetTransform(bodyId, b2Vec2_zero, new b2Rot(0.0f, 0.0f)));
        cases.add(() -> {
            b2DebugDraw draw = b2DefaultDebugDraw();
            draw.drawingBounds = invalidAabb();
            b2World_Draw(worldId, draw);
        });
        cases.add(() -> {
            b2DebugDraw draw = b2DefaultDebugDraw();
            draw.useDrawingBounds = true;
            draw.drawingBounds = invalidAabb();
            b2World_Draw(worldId, draw);
        });
        cases.add(() -> b2World_OverlapAABB(worldId, invalidAabb(), filter, shapeId -> true));
        cases.add(() -> b2World_CastRay(worldId, new b2Vec2(Float.NaN, 0.0f), b2Vec2_zero, filter,
            (shapeId, point, normal, fraction) -> fraction));
        cases.add(() -> b2World_CastRay(worldId, b2Vec2_zero,
            new b2Vec2(Float.POSITIVE_INFINITY, 0.0f), filter,
            (shapeId, point, normal, fraction) -> fraction));
        cases.add(() -> b2World_CastRayClosest(worldId, new b2Vec2(Float.NaN, 0.0f), b2Vec2_zero, filter));
        cases.add(() -> b2World_CastRayClosest(worldId, b2Vec2_zero,
            new b2Vec2(Float.POSITIVE_INFINITY, 0.0f), filter));
        cases.add(() -> b2World_CastShape(worldId, proxy, new b2Vec2(Float.NaN, 0.0f), filter,
            (shapeId, point, normal, fraction) -> fraction));
        cases.add(() -> b2World_CastMover(worldId,
            new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f),
            new b2Vec2(Float.NaN, 0.0f), filter));
        cases.add(() -> b2World_CastMover(worldId,
            new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.0f),
            b2Vec2_zero, filter));
        cases.add(() -> explode(worldId, def -> def.position.x = Float.NaN));
        cases.add(() -> explode(worldId, def -> def.radius = -1.0f));
        cases.add(() -> explode(worldId, def -> def.falloff = Float.NaN));
        cases.add(() -> explode(worldId, def -> def.impulsePerLength = Float.POSITIVE_INFINITY));

        StringBuilder output = new StringBuilder("bodyWorldValidation");
        for (Runnable testCase : cases) {
            output.append(' ').append(asserts(testCase));
        }
        b2DestroyWorld(worldId);
        return output.toString();
    }

    private static void createBody(b2WorldId worldId, java.util.function.Consumer<b2BodyDef> mutation) {
        b2BodyDef def = b2DefaultBodyDef();
        mutation.accept(def);
        b2CreateBody(worldId, def);
    }

    private static void explode(b2WorldId worldId, java.util.function.Consumer<b2ExplosionDef> mutation) {
        b2ExplosionDef def = b2DefaultExplosionDef();
        mutation.accept(def);
        b2World_Explode(worldId, def);
    }

    private static b2AABB invalidAabb() {
        return new b2AABB(new b2Vec2(1.0f, 1.0f), new b2Vec2(-1.0f, -1.0f));
    }

    private static int asserts(Runnable testCase) {
        try {
            testCase.run();
            return 0;
        } catch (AssertionError expected) {
            return 1;
        }
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_body_world_validation_probe");

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
        command.add(root.resolve("tools/parity/box2d_body_world_validation_probe.c").toString());
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
