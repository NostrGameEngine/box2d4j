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

final class DynamicTreeValidationParityTest {
    @Test
    void publicProxyPreconditionsMatchUpstream() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        List<Runnable> cases = new ArrayList<>();
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_CreateProxy(tree,
            aabb(-100000.0f, 0.0f, 0.0f, 1.0f), 1L, 0L)));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_CreateProxy(tree,
            aabb(0.0f, 0.0f, 1.0f, 100000.0f), 1L, 0L)));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_DestroyProxy(tree, -1)));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_DestroyProxy(tree, tree.nodeCapacity)));
        cases.add(() -> withTree((tree, proxyId) -> {
            b2DynamicTree_DestroyProxy(tree, proxyId);
            b2DynamicTree_DestroyProxy(tree, proxyId);
        }));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_MoveProxy(tree, proxyId,
            aabb(1.0f, 1.0f, -1.0f, -1.0f))));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_MoveProxy(tree, proxyId,
            aabb(0.0f, 0.0f, 100000.0f, 1.0f))));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_MoveProxy(tree, -1,
            aabb(0.0f, 0.0f, 1.0f, 1.0f))));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_EnlargeProxy(tree, proxyId,
            aabb(1.0f, 1.0f, -1.0f, -1.0f))));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_EnlargeProxy(tree, proxyId,
            aabb(0.0f, 0.0f, 1.0f, 100000.0f))));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_EnlargeProxy(tree, proxyId,
            aabb(0.2f, 0.2f, 0.8f, 0.8f))));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_GetCategoryBits(tree, -1)));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_GetUserData(tree, tree.nodeCapacity)));
        cases.add(() -> withTree((tree, proxyId) -> b2DynamicTree_GetAABB(tree, -1)));

        StringBuilder output = new StringBuilder("dynamicTreeValidation");
        for (Runnable testCase : cases) {
            output.append(' ').append(asserts(testCase));
        }
        return output.toString();
    }

    private static void withTree(TreeAction action) {
        b2DynamicTree tree = b2DynamicTree_Create();
        int proxyId = b2DynamicTree_CreateProxy(tree, aabb(0.0f, 0.0f, 1.0f, 1.0f), 1L, 7L);
        action.run(tree, proxyId);
        b2DynamicTree_Destroy(tree);
    }

    private static b2AABB aabb(float lx, float ly, float ux, float uy) {
        return new b2AABB(new b2Vec2(lx, ly), new b2Vec2(ux, uy));
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
        Path probe = outputDir.resolve("box2d_dynamic_tree_validation_probe");

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
        command.add(root.resolve("tools/parity/box2d_dynamic_tree_validation_probe.c").toString());
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

    @FunctionalInterface
    private interface TreeAction {
        void run(b2DynamicTree tree, int proxyId);
    }
}
