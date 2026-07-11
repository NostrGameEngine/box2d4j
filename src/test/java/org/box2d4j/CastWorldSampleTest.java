package org.box2d4j;

import org.box2d4j.samples.CastWorld;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CastWorldSampleTest {
    @Test
    void sampleMatchesUpstreamCCastWorld() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("castWorld", parts[0]);
        CastWorld.Result result = CastWorld.run();
        int index = 1;
        assertEquals(Integer.parseInt(parts[index++]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[index++]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[index++]), result.contactCount);
        assertEquals(Integer.parseInt(parts[index++]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[index++]), result.ignoreIndex);
        int queryCount = Integer.parseInt(parts[index++]);
        assertEquals(queryCount, result.queries.length);
        for (CastWorld.QueryResult query : result.queries) {
            assertEquals(Integer.parseInt(parts[index++]), query.castType);
            assertEquals(Integer.parseInt(parts[index++]), query.mode);
            assertEquals(Float.parseFloat(parts[index++]), query.startX, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), query.startY, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), query.endX, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), query.endY, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), query.angle, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), query.radius, 0.0f);
            assertEquals(Integer.parseInt(parts[index++]), query.nodeVisits);
            assertEquals(Integer.parseInt(parts[index++]), query.leafVisits);
            int hitCount = Integer.parseInt(parts[index++]);
            assertEquals(hitCount, query.hits.length);
            for (CastWorld.HitState hit : query.hits) {
                assertEquals(Float.parseFloat(parts[index++]), hit.pointX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), hit.pointY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), hit.normalX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), hit.normalY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), hit.fraction, 0.0f);
                assertEquals(Integer.parseInt(parts[index++]), hit.shapeIndex);
            }
        }
        int bodyCount = Integer.parseInt(parts[index++]);
        assertEquals(bodyCount, result.bodies.length);
        for (CastWorld.BodyState body : result.bodies) {
            assertEquals(Integer.parseInt(parts[index++]), body.type);
            assertEquals(Float.parseFloat(parts[index++]), body.x, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), body.y, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), body.cos, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), body.sin, 0.0f);
        }
        assertEquals(parts.length, index);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_cast_world_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_cast_world_sample_probe.c").toString());
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
