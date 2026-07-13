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

final class CallbackParityTest {
    @Test
    void customFilterAndPreSolveCallbacksMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        assertEquals(8, lines.length);

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, 0.0f);

        b2WorldId worldId = b2CreateWorld(worldDef);
        int[] filterCalls = {0};
        int[] filterShapes = new int[2];
        b2World_SetCustomFilterCallback(worldId, (shapeIdA, shapeIdB, context) -> {
            filterCalls[0] += 1;
            filterShapes[0] = shapeIdA.index1;
            filterShapes[1] = shapeIdB.index1;
            return false;
        }, null);
        createBoxBody(worldId, new b2Vec2(0.0f, 0.0f), false);
        createBoxBody(worldId, new b2Vec2(0.25f, 0.0f), false);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2Counters counters = b2World_GetCounters(worldId);
        b2ContactEvents events = b2World_GetContactEvents(worldId);
        assertEquals(lines[0], "filter " + filterCalls[0] + " " + filterShapes[0] + " " + filterShapes[1] + " "
            + counters.contactCount + " " +
            events.beginCount + " " + events.endCount);
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        filterCalls[0] = 0;
        b2World_SetCustomFilterCallback(worldId, (shapeIdA, shapeIdB, context) -> {
            filterCalls[0] += 1;
            return true;
        }, null);
        b2BodyId sensorBodyId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef sensorDef = b2DefaultShapeDef();
        sensorDef.isSensor = true;
        b2CreateCircleShape(sensorBodyId, sensorDef, new b2Circle(new b2Vec2(), 1.0f));
        createBoxBody(worldId, new b2Vec2(), false);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertEquals(lines[1], "sensor_filter " + filterCalls[0] + " "
            + b2World_GetCounters(worldId).contactCount);
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        int[] preSolveCalls = {0};
        int[] preSolvePointCount = {0};
        b2World_SetPreSolveCallback(worldId, (shapeIdA, shapeIdB, manifold, context) -> {
            preSolveCalls[0] += 1;
            preSolvePointCount[0] = manifold.pointCount;
            return false;
        }, null);
        b2BodyId bodyA = createBoxBody(worldId, new b2Vec2(0.0f, 0.0f), true);
        b2BodyId bodyB = createBoxBody(worldId, new b2Vec2(0.25f, 0.0f), false);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        counters = b2World_GetCounters(worldId);
        events = b2World_GetContactEvents(worldId);
        b2ContactData[] data = new b2ContactData[2];
        int contactDataCount = b2Body_GetContactData(bodyA, data, 2);
        assertEquals(lines[2], "presolve " + preSolveCalls[0] + " " + preSolvePointCount[0] + " " +
            counters.contactCount + " " + events.beginCount + " " + events.endCount + " " +
            b2Body_GetContactCapacity(bodyA) + " " + b2Body_GetContactCapacity(bodyB) + " " + contactDataCount);
        b2DestroyWorld(worldId);

        assertEquals(lines[3], runPreSolveCapture(worldDef, false));
        assertEquals(lines[4], runPreSolveCapture(worldDef, true));

        String[] materialMixing = runMaterialMixingCallbacks(worldDef);
        assertEquals(lines[5], materialMixing[0]);
        assertEquals(lines[6], materialMixing[1]);
        assertEquals(lines[7], materialMixing[2]);
    }

    private static String[] runMaterialMixingCallbacks(b2WorldDef worldDef) {
        b2WorldId worldId = b2CreateWorld(worldDef);
        int[] frictionCalls = {0};
        int[] restitutionCalls = {0};
        int[] frictionArgs = new int[4];
        int[] restitutionArgs = new int[4];
        b2World_SetFrictionCallback(worldId, (frictionA, materialA, frictionB, materialB) -> {
            frictionCalls[0] += 1;
            frictionArgs[0] = Float.floatToRawIntBits(frictionA);
            frictionArgs[1] = materialA;
            frictionArgs[2] = Float.floatToRawIntBits(frictionB);
            frictionArgs[3] = materialB;
            return 0.25f;
        });
        b2World_SetRestitutionCallback(worldId, (restitutionA, materialA, restitutionB, materialB) -> {
            restitutionCalls[0] += 1;
            restitutionArgs[0] = Float.floatToRawIntBits(restitutionA);
            restitutionArgs[1] = materialA;
            restitutionArgs[2] = Float.floatToRawIntBits(restitutionB);
            restitutionArgs[3] = materialB;
            return 0.0f;
        });

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        groundShapeDef.material.friction = 0.2f;
        groundShapeDef.material.restitution = 0.1f;
        groundShapeDef.material.userMaterialId = 11;
        b2CreatePolygonShape(groundId, groundShapeDef, b2MakeBox(1.0f, 1.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.5f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.8f;
        shapeDef.material.restitution = 0.6f;
        shapeDef.material.userMaterialId = 22;
        b2ShapeId shapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

        try {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            String first = formatMixLine("mix1", frictionCalls[0], restitutionCalls[0], frictionArgs, restitutionArgs);

            b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(shapeId);
            material.friction = 0.45f;
            material.restitution = 0.75f;
            material.userMaterialId = 33;
            b2Shape_SetSurfaceMaterial(shapeId, material);
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            String second = formatMixLine("mix2", frictionCalls[0], restitutionCalls[0], frictionArgs, restitutionArgs);

            b2World_SetFrictionCallback(worldId, null);
            b2World_SetRestitutionCallback(worldId, null);
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            String reset = String.format(Locale.ROOT, "mix3 %d %d", frictionCalls[0], restitutionCalls[0]);
            return new String[] {first, second, reset};
        } finally {
            b2DestroyWorld(worldId);
        }
    }

    private static String formatMixLine(String label, int frictionCalls, int restitutionCalls,
                                        int[] frictionArgs, int[] restitutionArgs) {
        return String.format(Locale.ROOT,
            "%s %d %d %08x %d %08x %d %08x %d %08x %d",
            label, frictionCalls, restitutionCalls,
            frictionArgs[0], frictionArgs[1], frictionArgs[2], frictionArgs[3],
            restitutionArgs[0], restitutionArgs[1], restitutionArgs[2], restitutionArgs[3]);
    }

    private static String runPreSolveCapture(b2WorldDef worldDef, boolean initialFlag) {
        b2WorldId worldId = b2CreateWorld(worldDef);
        int[] calls = {0};
        int[] pointCount = {0};
        b2World_SetPreSolveCallback(worldId, (shapeIdA, shapeIdB, manifold, context) -> {
            calls[0] += 1;
            pointCount[0] = manifold.pointCount;
            return false;
        }, null);

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(2.0f, 0.5f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.08f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enablePreSolveEvents = initialFlag;
        b2ShapeId shapeId = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(), 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2Shape_EnablePreSolveEvents(shapeId, !initialFlag);
        b2Body_SetTransform(bodyId, new b2Vec2(0.0f, 0.9f), b2Rot_identity);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        String result = "capture " + (initialFlag ? 1 : 0) + " " + calls[0] + " " + pointCount[0];
        b2DestroyWorld(worldId);
        return result;
    }

    private static b2BodyId createBoxBody(b2WorldId worldId, b2Vec2 position, boolean enablePreSolve) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = position;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.enablePreSolveEvents = enablePreSolve;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.5f, 0.5f));
        return bodyId;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_callback_probe");

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
        command.add(root.resolve("tools/parity/box2d_callback_probe.c").toString());
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
