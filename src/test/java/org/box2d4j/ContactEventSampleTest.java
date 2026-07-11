package org.box2d4j;

import org.box2d4j.samples.ContactEventSample;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ContactEventSampleTest {
    @Test
    void sampleMatchesUpstreamCContactEvent() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("contactEvent", parts[0]);

        ContactEventSample.Result result = ContactEventSample.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[6]), result.spawnTotal);
        assertEquals(Integer.parseInt(parts[7]), result.attachTotal);
        assertEquals(Integer.parseInt(parts[8]), result.destroyShapeTotal);
        assertEquals(Integer.parseInt(parts[9]), result.beginTotal);
        assertEquals(Integer.parseInt(parts[10]), result.endTotal);
        assertEquals(Integer.parseInt(parts[11]), result.hitTotal);
        assertEquals(Integer.parseInt(parts[12]), result.contactPointTotal);
        assertEquals(Integer.parseInt(parts[13]), result.normalChecksum);
        assertEquals(Integer.parseInt(parts[14]), result.impulseChecksum);
        assertEquals(Integer.parseInt(parts[15]), result.activeDebrisCount);
        assertEquals(Long.parseLong(parts[16]), result.activeMask);
        assertEquals(Float.parseFloat(parts[17]), result.wait, 0.0f);
        assertBody(parts, 18, result.player);
        int debrisCount = Integer.parseInt(parts[28]);
        assertEquals(debrisCount, result.debris.length);

        int index = 29;
        for (int i = 0; i < debrisCount; ++i) {
            assertBody(parts, index, result.debris[i]);
            index += 10;
        }
        assertEquals(parts.length, index);
    }

    private static void assertBody(String[] parts, int index, ContactEventSample.BodyState body) {
        assertEquals(Integer.parseInt(parts[index]), body.index);
        assertEquals(Float.parseFloat(parts[index + 1]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), body.cos, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), body.sin, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), body.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 7]), body.angularVelocity, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 8]), body.shapeCount);
        assertEquals(Integer.parseInt(parts[index + 9]), body.contactCapacity);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_contact_event_sample_probe");

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
        command.add("-I" + root.resolve("vendor/box2d/shared"));
        command.addAll(sources);
        command.add(root.resolve("vendor/box2d/shared/random.c").toString());
        command.add(root.resolve("tools/parity/box2d_contact_event_sample_probe.c").toString());
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
