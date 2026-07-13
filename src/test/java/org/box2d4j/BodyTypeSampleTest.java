package org.box2d4j;

import org.box2d4j.samples.BodyType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BodyTypeSampleTest {
    @Test
    void sampleAndScriptTransitionsMatchUpstreamCBodyType() throws Exception {
        Path probe = compileProbe();
        assertResult(runProbe(probe, 0, 5), BodyType.run());
        for (int phase = 0; phase <= 5; ++phase) {
            assertResult(runProbe(probe, 1, phase), BodyType.runScript(1, phase));
        }
        assertResult(runProbe(probe, 2400, 5), BodyType.runScript(2400, 5));
    }

    private static void assertResult(String upstream, BodyType.Result result) {
        String[] parts = upstream.split("\\s+");
        assertEquals("bodyType", parts[0]);

        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[6]), result.type);
        assertEquals(Integer.parseInt(parts[7]) != 0, result.enabled);
        int bodyCount = Integer.parseInt(parts[8]);
        assertEquals(bodyCount, result.states.length);
        for (int i = 0; i < bodyCount; ++i) {
            assertBody(parts, 9 + 10 * i, result.states[i]);
        }
        assertEquals(upstream, result.toLine());
    }

    private static void assertBody(String[] parts, int index, BodyType.BodyState body) {
        assertEquals(Integer.parseInt(parts[index]), body.type);
        assertEquals(Integer.parseInt(parts[index + 1]) != 0, body.enabled);
        assertEquals(Integer.parseInt(parts[index + 2]) != 0, body.awake);
        assertEquals(Integer.parseInt(parts[index + 3]), body.contactCapacity);
        assertEquals(Float.parseFloat(parts[index + 4]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), body.angle, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 7]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 8]), body.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 9]), body.angularVelocity, 0.0f);
    }

    private static Path compileProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_body_type_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_body_type_sample_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);
        return probe;
    }

    private static String runProbe(Path probe, int stepCount, int scriptPhaseCount) throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Process run = new ProcessBuilder(probe.toString(), Integer.toString(stepCount),
            Integer.toString(scriptPhaseCount)).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        return output;
    }
}
