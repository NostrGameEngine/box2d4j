package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MultithreadingTest {
    private static final Pattern CPP_RESULT = Pattern.compile(
        "workers (\\d+) sleep (\\d+) hash ([0-9a-fA-F]+)");

    @Test
    void coreContainsOnlyTaskHooks() throws Exception {
        List<Path> offenders = new ArrayList<>();
        try (java.util.stream.Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".java"))::iterator) {
                if (Files.readString(file).contains("ExecutorService")) {
                    offenders.add(file);
                }
            }
        }
        assertTrue(offenders.isEmpty(), "ExecutorService leaked into the core: " + offenders);
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.box2d4j.B2TaskSchedulers"));
    }

    @Test
    void taskCallbacksFallBackToDirectExecutionUnlessBothArePresent() {
        AtomicInteger enqueueCount = new AtomicInteger();
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.workerCount = 8;
        worldDef.enqueueTask = (task, itemCount, minRange, taskContext, userContext) -> {
            enqueueCount.incrementAndGet();
            task.invoke(0, itemCount, 0, taskContext);
            return null;
        };
        worldDef.finishTask = null;

        b2WorldId worldId = b2CreateWorld(worldDef);
        try {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(bodyId, b2DefaultShapeDef(), new b2Circle(b2Vec2_zero, 0.5f));
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            assertEquals(0, enqueueCount.get());
        } finally {
            b2DestroyWorld(worldId);
        }
    }

    @Test
    void fallingHingesAreBitwiseDeterministicAcrossWorkerCounts() throws Exception {
        SimulationResult baseline = simulate(1);
        for (int workerCount : new int[] {2, 4, 8}) {
            SimulationResult parallel = simulate(workerCount);
            assertArrayEquals(baseline.stateBits, parallel.stateBits,
                "simulation diverged with " + workerCount + " workers");
            assertTrue(parallel.scheduler.workerIndices.size() > 1,
                "task ranges were not split with " + workerCount + " workers");
            assertTrue(parallel.scheduler.threadNames.size() > 1,
                "tasks did not reach multiple executor threads with " + workerCount + " workers");
            assertTrue(parallel.scheduler.maxInFlight.get() > 1,
                "tasks never overlapped with " + workerCount + " workers");
            assertFalse(parallel.scheduler.workerIndexCollision,
                "a worker index was used concurrently with " + workerCount + " workers");
        }
    }

    @Test
    void fallingHingesMatchUpstreamCppAcrossWorkerCounts() throws Exception {
        Map<Integer, SleepResult> cppResults = runCppProbe();
        assertEquals(Set.of(1, 2, 4, 8), cppResults.keySet());
        for (int workerCount : cppResults.keySet()) {
            SleepResult javaResult = simulateUntilSleep(workerCount);
            SleepResult cppResult = cppResults.get(workerCount);
            assertEquals(cppResult.sleepStep, javaResult.sleepStep,
                "sleep step diverged with " + workerCount + " workers");
            assertEquals(cppResult.hash, javaResult.hash,
                "transform hash diverged with " + workerCount + " workers");
        }
    }

    @Test
    void testSchedulerHonorsTheHookContractWithoutCoreSupport() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            B2TaskScheduler scheduler = new TestExecutorTaskScheduler(executor, 2);
            ConcurrentLinkedQueue<Integer> rangeSizes = new ConcurrentLinkedQueue<>();
            Object handle = scheduler.enqueue((startIndex, endIndex, workerIndex, taskContext) ->
                rangeSizes.add(endIndex - startIndex), 17, 8, null);
            scheduler.finish(handle);
            assertEquals(Set.of(8, 9), Set.copyOf(rangeSizes));
            assertEquals(2, rangeSizes.size());
            assertFalse(executor.isShutdown());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static SimulationResult simulate(int workerCount) throws Exception {
        AtomicInteger threadNumber = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable,
                "box2d4j-test-" + workerCount + "-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        ExecutorService executor = Executors.newFixedThreadPool(workerCount, threadFactory);
        TrackingScheduler scheduler = new TrackingScheduler(new TestExecutorTaskScheduler(executor, workerCount));
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.enableSleep = false;
        worldDef.setTaskScheduler(scheduler);
        b2WorldId worldId = b2CreateWorld(worldDef);
        try {
            b2BodyId[] bodyIds = createFallingHinges(worldId);
            for (int stepIndex = 0; stepIndex < 165; ++stepIndex) {
                b2World_Step(worldId, 1.0f / 60.0f, 4);
            }
            return new SimulationResult(captureState(worldId, bodyIds), scheduler);
        } finally {
            b2DestroyWorld(worldId);
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static SleepResult simulateUntilSleep(int workerCount) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.setTaskScheduler(new TestExecutorTaskScheduler(executor, workerCount));
        b2WorldId worldId = b2CreateWorld(worldDef);
        try {
            b2BodyId[] bodyIds = createFallingHinges(worldId);
            int stepCount = 0;
            while (stepCount < 1000) {
                b2World_Step(worldId, 1.0f / 60.0f, 4);
                if (b2World_GetBodyEvents(worldId).moveCount == 0) {
                    assertEquals(0, b2World_GetAwakeBodyCount(worldId));
                    return new SleepResult(stepCount, hashTransforms(bodyIds));
                }
                stepCount += 1;
            }
            throw new AssertionError("falling hinges did not sleep");
        } finally {
            b2DestroyWorld(worldId);
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static int hashTransforms(b2BodyId[] bodyIds) {
        int hash = B2_HASH_INIT;
        ByteBuffer bytes = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        for (b2BodyId bodyId : bodyIds) {
            b2Transform transform = b2Body_GetTransform(bodyId);
            bytes.clear();
            bytes.putFloat(transform.p.x);
            bytes.putFloat(transform.p.y);
            bytes.putFloat(transform.q.c);
            bytes.putFloat(transform.q.s);
            hash = b2Hash(hash, bytes.array(), bytes.capacity());
        }
        return hash;
    }

    private static Map<Integer, SleepResult> runCppProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDirectory = root.resolve("build/parity/cpp-multithreading-test");
        Files.createDirectories(outputDirectory);
        try (java.util.stream.Stream<Path> files = Files.list(outputDirectory)) {
            for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".o"))::iterator) {
                Files.delete(file);
            }
        }

        List<Path> cSources = new ArrayList<>();
        try (java.util.stream.Stream<Path> files = Files.list(root.resolve("vendor/box2d/src"))) {
            files.filter(path -> path.toString().endsWith(".c")).sorted().forEach(cSources::add);
        }
        cSources.add(root.resolve("vendor/box2d/shared/determinism.c"));

        List<String> compileC = new ArrayList<>();
        compileC.add("clang");
        compileC.add("-std=c17");
        compileC.add("-O2");
        compileC.add("-ffp-contract=off");
        compileC.add("-I" + root.resolve("vendor/box2d/include"));
        compileC.add("-I" + root.resolve("vendor/box2d/src"));
        compileC.add("-I" + root.resolve("vendor/box2d/shared"));
        compileC.add("-c");
        for (Path source : cSources) {
            compileC.add(source.toString());
        }
        runCommand(compileC, outputDirectory);

        List<String> linkCpp = new ArrayList<>();
        linkCpp.add("clang++");
        linkCpp.add("-std=c++17");
        linkCpp.add("-O2");
        linkCpp.add("-ffp-contract=off");
        linkCpp.add("-I" + root.resolve("vendor/box2d/include"));
        linkCpp.add("-I" + root.resolve("vendor/box2d/shared"));
        linkCpp.add(root.resolve("tools/parity/box2d_multithreading_probe.cpp").toString());
        for (Path source : cSources) {
            String objectName = source.getFileName().toString().replaceFirst("\\.c$", ".o");
            linkCpp.add(outputDirectory.resolve(objectName).toString());
        }
        linkCpp.add("-pthread");
        Path executable = outputDirectory.resolve("box2d_multithreading_probe");
        linkCpp.add("-o");
        linkCpp.add(executable.toString());
        runCommand(linkCpp, outputDirectory);

        String output = runCommand(List.of(executable.toString()), outputDirectory);
        Map<Integer, SleepResult> results = new TreeMap<>();
        for (String line : output.split("\\R")) {
            Matcher matcher = CPP_RESULT.matcher(line.trim());
            if (matcher.matches()) {
                int workerCount = Integer.parseInt(matcher.group(1));
                int sleepStep = Integer.parseInt(matcher.group(2));
                int hash = (int) Long.parseLong(matcher.group(3), 16);
                results.put(workerCount, new SleepResult(sleepStep, hash));
            }
        }
        return results;
    }

    private static String runCommand(List<String> command, Path directory) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), output);
        return output.trim();
    }

    private static b2BodyId[] createFallingHinges(b2WorldId worldId) {
        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(20.0f, 1.0f));

        int columnCount = 4;
        int rowCount = 30;
        b2BodyId[] bodyIds = new b2BodyId[columnCount * rowCount];
        float h = 0.25f;
        float radius = 0.1f * h;
        float offset = 0.4f * h;
        float dx = 10.0f * h;
        float xRoot = -0.5f * dx * (columnCount - 1.0f);
        b2Polygon box = b2MakeRoundedBox(h - radius, h - radius, radius);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.3f;
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.1f * B2_PI;
        jointDef.upperAngle = 0.2f * B2_PI;
        jointDef.enableSpring = true;
        jointDef.hertz = 0.5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.localAnchorA = new b2Vec2(h, h);
        jointDef.localAnchorB = new b2Vec2(offset, -h);
        jointDef.drawSize = 0.1f;

        int bodyIndex = 0;
        for (int column = 0; column < columnCount; ++column) {
            float x = xRoot + column * dx;
            b2BodyId previousBodyId = b2_nullBodyId;
            for (int row = 0; row < rowCount; ++row) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;
                bodyDef.position = new b2Vec2(x + offset * row, h + 2.0f * h * row);
                bodyDef.rotation = b2MakeRot(0.1f * row - 1.0f);
                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                if ((row & 1) == 0) {
                    previousBodyId = bodyId;
                } else {
                    jointDef.bodyIdA = previousBodyId;
                    jointDef.bodyIdB = bodyId;
                    b2CreateRevoluteJoint(worldId, jointDef);
                    previousBodyId = b2_nullBodyId;
                }
                b2CreatePolygonShape(bodyId, shapeDef, box);
                bodyIds[bodyIndex++] = bodyId;
            }
        }
        return bodyIds;
    }

    private static int[] captureState(b2WorldId worldId, b2BodyId[] bodyIds) {
        int valuesPerBody = 7;
        int[] bits = new int[bodyIds.length * valuesPerBody + 3];
        int offset = 0;
        for (b2BodyId bodyId : bodyIds) {
            b2Transform transform = b2Body_GetTransform(bodyId);
            b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
            bits[offset++] = Float.floatToRawIntBits(transform.p.x);
            bits[offset++] = Float.floatToRawIntBits(transform.p.y);
            bits[offset++] = Float.floatToRawIntBits(transform.q.c);
            bits[offset++] = Float.floatToRawIntBits(transform.q.s);
            bits[offset++] = Float.floatToRawIntBits(linearVelocity.x);
            bits[offset++] = Float.floatToRawIntBits(linearVelocity.y);
            bits[offset++] = Float.floatToRawIntBits(b2Body_GetAngularVelocity(bodyId));
        }
        b2Counters counters = b2World_GetCounters(worldId);
        bits[offset++] = counters.bodyCount;
        bits[offset++] = counters.contactCount;
        bits[offset] = counters.jointCount;
        return bits;
    }

    private static final class TestExecutorTaskScheduler implements B2TaskScheduler {
        final ExecutorService executor;
        final int workerCount;
        final ArrayBlockingQueue<Integer> availableWorkerIndices;

        TestExecutorTaskScheduler(ExecutorService executor, int workerCount) {
            this.executor = executor;
            this.workerCount = workerCount;
            this.availableWorkerIndices = new ArrayBlockingQueue<>(workerCount);
            for (int workerIndex = 0; workerIndex < workerCount; ++workerIndex) {
                availableWorkerIndices.add(workerIndex);
            }
        }

        @Override
        public int workerCount() {
            return workerCount;
        }

        @Override
        public Object enqueue(b2TaskCallback task, int itemCount, int minRange, Object taskContext) {
            int range = Math.max(1, minRange);
            int taskCount = Math.min(workerCount, Math.max(1, itemCount / range));
            if (workerCount == 1) {
                task.invoke(0, itemCount, 0, taskContext);
                return null;
            }
            List<Future<?>> futures = new ArrayList<>(taskCount);
            int baseCount = itemCount / taskCount;
            int remainder = itemCount - baseCount * taskCount;
            int startIndex = 0;
            for (int taskIndex = 0; taskIndex < taskCount; ++taskIndex) {
                int count = baseCount + (taskIndex < remainder ? 1 : 0);
                int rangeStart = startIndex;
                int rangeEnd = rangeStart + count;
                futures.add(executor.submit(() -> {
                    int workerIndex;
                    try {
                        workerIndex = availableWorkerIndices.take();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                    try {
                        task.invoke(rangeStart, rangeEnd, workerIndex, taskContext);
                    } finally {
                        availableWorkerIndices.add(workerIndex);
                    }
                }));
                startIndex = rangeEnd;
            }
            return futures;
        }

        @Override
        public void finish(Object taskHandle) {
            for (Object item : (List<?>) taskHandle) {
                try {
                    ((Future<?>) item).get();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                } catch (ExecutionException exception) {
                    throw new IllegalStateException(exception.getCause());
                }
            }
        }
    }

    private static final class TrackingScheduler implements B2TaskScheduler {
        final B2TaskScheduler delegate;
        final Set<String> threadNames = ConcurrentHashMap.newKeySet();
        final Set<Integer> workerIndices = ConcurrentHashMap.newKeySet();
        final AtomicInteger inFlight = new AtomicInteger();
        final AtomicInteger maxInFlight = new AtomicInteger();
        final AtomicIntegerArray workerInFlight;
        volatile boolean workerIndexCollision;

        TrackingScheduler(B2TaskScheduler delegate) {
            this.delegate = delegate;
            this.workerInFlight = new AtomicIntegerArray(delegate.workerCount());
        }

        @Override
        public int workerCount() {
            return delegate.workerCount();
        }

        @Override
        public Object enqueue(b2TaskCallback task, int itemCount, int minRange, Object taskContext) {
            return delegate.enqueue((startIndex, endIndex, workerIndex, context) -> {
                threadNames.add(Thread.currentThread().getName());
                workerIndices.add(workerIndex);
                if (workerInFlight.incrementAndGet(workerIndex) != 1) {
                    workerIndexCollision = true;
                }
                int active = inFlight.incrementAndGet();
                maxInFlight.accumulateAndGet(active, Math::max);
                try {
                    task.invoke(startIndex, endIndex, workerIndex, context);
                } finally {
                    inFlight.decrementAndGet();
                    workerInFlight.decrementAndGet(workerIndex);
                }
            }, itemCount, minRange, taskContext);
        }

        @Override
        public void finish(Object taskHandle) {
            delegate.finish(taskHandle);
        }
    }

    private static final class SimulationResult {
        final int[] stateBits;
        final TrackingScheduler scheduler;

        SimulationResult(int[] stateBits, TrackingScheduler scheduler) {
            this.stateBits = stateBits;
            this.scheduler = scheduler;
        }
    }

    private static final class SleepResult {
        final int sleepStep;
        final int hash;

        SleepResult(int sleepStep, int hash) {
            this.sleepStep = sleepStep;
            this.hash = hash;
        }
    }
}
