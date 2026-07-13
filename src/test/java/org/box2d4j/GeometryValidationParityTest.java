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

final class GeometryValidationParityTest {
    @Test
    void geometryFactoryMassAabbAndRayAssertionsMatchC() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        List<Runnable> cases = new ArrayList<>();
        cases.add(() -> b2MakeBox(0.0f, 1.0f));
        cases.add(() -> b2MakeBox(1.0f, Float.NaN));
        cases.add(() -> b2MakeRoundedBox(1.0f, 1.0f, -1.0f));
        cases.add(() -> b2MakeRoundedBox(1.0f, 1.0f, Float.POSITIVE_INFINITY));
        cases.add(() -> b2MakeOffsetRoundedBox(1.0f, 1.0f, b2Vec2_zero, b2Rot_identity, -1.0f));
        cases.add(() -> b2MakeOffsetRoundedBox(1.0f, 1.0f, b2Vec2_zero, b2Rot_identity, Float.NaN));
        cases.add(() -> b2ComputePolygonMass(new b2Polygon(), 1.0f));
        cases.add(() -> {
            b2Polygon polygon = new b2Polygon();
            polygon.count = 3;
            b2ComputePolygonMass(polygon, 1.0f);
        });
        cases.add(() -> b2ComputePolygonAABB(new b2Polygon(), b2Transform_identity));
        cases.add(() -> b2RayCastCircle(ray(-1.0f), new b2Circle(new b2Vec2(), 1.0f)));
        cases.add(() -> {
            b2RayCastInput ray = ray(1.0f);
            ray.origin.x = Float.NaN;
            b2RayCastCapsule(ray, new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.5f));
        });
        cases.add(() -> b2RayCastPolygon(ray(100000.0f), b2MakeBox(1.0f, 1.0f)));
        cases.add(() -> b2RayCastSegment(ray(-1.0f),
            new b2Segment(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f)), false));
        cases.add(() -> b2MakeOffsetBox(0.0f, -1.0f, b2Vec2_zero, b2Rot_identity));
        cases.add(() -> b2RayCastPolygon(ray(1.0f), b2MakeBox(1.0f, 1.0f)));
        cases.add(() -> b2CollideCapsules(
            new b2Capsule(new b2Vec2(), new b2Vec2(), 0.5f), b2Transform_identity,
            new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.5f), b2Transform_identity));
        cases.add(() -> b2CollideCapsules(
            new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.5f), b2Transform_identity,
            new b2Capsule(new b2Vec2(), new b2Vec2(), 0.5f), b2Transform_identity));
        cases.add(() -> b2CollideCapsules(
            new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.5f), b2Transform_identity,
            new b2Capsule(new b2Vec2(-0.5f, 0.3f), new b2Vec2(1.5f, 0.3f), 0.5f), b2Transform_identity));
        cases.add(() -> withShape(false, B2::b2Shape_GetSegment));
        cases.add(() -> withShape(false, B2::b2Shape_GetChainSegment));
        cases.add(() -> withShape(false, B2::b2Shape_GetCapsule));
        cases.add(() -> withShape(false, B2::b2Shape_GetPolygon));
        cases.add(() -> withShape(true, B2::b2Shape_GetCircle));

        StringBuilder output = new StringBuilder("geometryValidation");
        for (Runnable testCase : cases) {
            output.append(' ').append(asserts(testCase));
        }
        return output.toString();
    }

    private static b2RayCastInput ray(float maxFraction) {
        return new b2RayCastInput(new b2Vec2(), new b2Vec2(1.0f, 0.0f), maxFraction);
    }

    private static void withShape(boolean polygon, java.util.function.Consumer<b2ShapeId> getter) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeId shapeId = polygon
            ? b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), b2MakeBox(1.0f, 1.0f))
            : b2CreateCircleShape(bodyId, b2DefaultShapeDef(), new b2Circle(new b2Vec2(), 1.0f));
        try {
            getter.accept(shapeId);
        } finally {
            b2DestroyWorld(worldId);
        }
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
        Path probe = outputDir.resolve("box2d_geometry_validation_probe");

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted().map(Path::toString).forEach(command::add);
        }
        command.add(root.resolve("tools/parity/box2d_geometry_validation_probe.c").toString());
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
