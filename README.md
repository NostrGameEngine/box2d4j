# box2d4j

> [!WARNING]
> **box2d4j is an experimental, AI-assisted port with limited human review.**
> Treat it as pre-production software and validate it against your own
> workloads before relying on it in an engine or application.

box2d4j is a pure Java 11 port of [Box2D `v3.1.1`](https://github.com/erincatto/box2d/tree/v3.1.1).
The core has no JNI dependency and aims to preserve the upstream C API,
behavior, data layout concepts, and simulation results as closely as Java
reasonably allows. It is an independent port and is not an official Box2D
project.

## Project Status

- [Porting status](PORTING_STATUS.md) inventories the implemented API, samples,
  verification coverage, adaptations, and known gaps.
- [Feature parity audit](FEATURE_PARITY.md) summarizes public-surface coverage
  and the evidence behind each parity claim.
- [Upstream porting guide](UPSTREAM_PORTING_GUIDE.md) records deliberate Java
  divergences and the workflow for updating the port to a newer Box2D release.

Parity is backed by Java unit tests and native C/C++ comparison probes at the
documented test horizons. It is not a proof that every possible callback
sequence, workload, platform, or floating-point environment behaves
identically.

## AI Provenance

Most of this port was produced with AI assistance and received only limited
human review. The initial porting effort was developed with GPT-5.5 and
GPT-5.6-Sol; subsequent porting, parity work, debugger development, and fixes
were developed with GPT-5.6-Sol. Human involvement primarily consisted of
requirements, review, testing, and project direction.

Git commits retain the human operator as the accountable Git author and use
`AI-Assisted-by` trailers to record the model involved. This avoids inventing
GitHub accounts or email identities for models while keeping provenance
machine-readable in the commit history.

The source in `vendor/box2d` is the upstream reference checkout used while
porting and for parity tests. Java code intentionally keeps C-style `b2...`
names and mutable data records where that preserves source parity.

All 110 active upstream sample creators are available as headless Java entry
points under `org.box2d4j.samples`. The test suite compiles the vendored C
sources with Clang and compares world counters, graph colors, raw float state,
events, queries, and simulation hashes against the Java port.

## Graphical Sample Debugger

Launch the jMonkeyEngine debugger with:

```bash
./gradlew runDebugger
```

The catalog contains the standalone HelloWorld tutorial plus all 110 active
upstream samples. Use the toolbar or `P` to play/pause, `N` to advance one
step, `R` to reset, and `F` to fit the world. Drag with the middle mouse button
to pan, use the wheel to zoom, and scroll over the catalog to browse samples.
Left-drag manipulates dynamic bodies or the active collision query. Debug draw
toggles cover shapes, joints, contacts, AABBs, and constraint graph colors.
Samples without a persistent `b2World` render a live geometric snapshot.

The debugger runs each sample and its Box2D world on a dedicated simulation
thread. That thread captures debug geometry and counters into a three-buffer
`latest frame wins` exchange; the jMonkeyEngine render loop only uploads the
newest completed batch and never calls `b2World_Step`, `b2World_Draw`, or live
world queries. Slow rendering can drop intermediate debug frames, but it does
not stall physics. Play, pause, single-step, speed changes, pointer input, and
draw-option refreshes are coordinated by the simulation clock without making
the render thread wait for a physics step.

World-based samples continue stepping indefinitely in the debugger after their
finite headless parity horizon. The 59 interactive v3.1.1 samples expose their
upstream actions, toggles, choices, numeric controls, and keyboard bindings in
the paginated `Sample Controls` panel. This includes gameplay controls for
Pinball, Gear Lift, Driving, Mover, and Drop; scene builders and launch/reset
actions; and draggable collision queries. The normal `run()` methods remain
finite for tests and C/Java parity probes.

## Allocator

Native-style allocations default to `ByteBuffer.allocateDirect`. Engines can
install their own allocator/free pair globally before creating Box2D objects:

```java
B2.b2SetAllocator(
    (size, alignment) -> engineAllocator.allocate(size, alignment),
    engineAllocator::free);
```

The exact `b2AllocFcn`/`b2FreeFcn` pair receives every allocation and free,
which allows an engine to keep ownership and accounting in its native memory
system. The allocation callback receives a 32-byte-rounded size and a 32-byte
alignment.

When explicit release is unnecessary, `B2Allocator` is the shorter form:

```java
B2.b2SetAllocator((size, alignment) -> engineAllocator.allocate(size, alignment));
```

An `IntFunction<ByteBuffer>` can be used when the engine allocator only needs
the requested size:

```java
B2.b2SetAllocator(engineAllocator::allocate);
```

The `IntFunction` adapter receives the rounded size. The one-callback forms use
a no-op free callback because their buffers are expected to be JVM-managed.

## Multithreading

The core intentionally provides **no task-system implementation** and has no
dependency on `ExecutorService`. It only exposes the Box2D callbacks on
`b2WorldDef` and the optional Java-friendly `B2TaskScheduler` contract. With no
hooks configured, a world runs serially on the calling thread.

On a regular JVM, an application can bridge an engine-owned `ExecutorService`
like this:

```java
final class ExecutorTaskScheduler implements B2TaskScheduler {
    private final ExecutorService executor;
    private final int workerCount;
    private final ArrayBlockingQueue<Integer> workerIndices;

    ExecutorTaskScheduler(ExecutorService executor, int workerCount) {
        if (workerCount < 1 || workerCount > 64) {
            throw new IllegalArgumentException("workerCount must be in [1, 64]");
        }
        this.executor = executor;
        this.workerCount = workerCount;
        this.workerIndices = new ArrayBlockingQueue<>(workerCount);
        for (int i = 0; i < workerCount; ++i) {
            workerIndices.add(i);
        }
    }

    public int workerCount() {
        return workerCount;
    }

    public Object enqueue(b2TaskCallback task, int itemCount, int minRange,
                          Object taskContext) {
        if (itemCount <= 0) {
            return null;
        }
        int range = Math.max(1, minRange);
        int taskCount = Math.min(workerCount, Math.max(1, itemCount / range));
        if (workerCount == 1 || taskCount == 1) {
            task.invoke(0, itemCount, 0, taskContext);
            return null;
        }

        List<Future<?>> futures = new ArrayList<>(taskCount);
        int base = itemCount / taskCount;
        int remainder = itemCount - base * taskCount;
        int start = 0;
        for (int taskIndex = 0; taskIndex < taskCount; ++taskIndex) {
            int count = base + (taskIndex < remainder ? 1 : 0);
            int rangeStart = start;
            int rangeEnd = start + count;
            futures.add(executor.submit(() -> {
                int workerIndex;
                try {
                    workerIndex = workerIndices.take();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                try {
                    task.invoke(rangeStart, rangeEnd, workerIndex, taskContext);
                } finally {
                    workerIndices.add(workerIndex);
                }
            }));
            start = rangeEnd;
        }
        return futures;
    }

    public void finish(Object taskHandle) {
        Throwable failure = null;
        boolean interrupted = false;
        for (Object item : (List<?>) taskHandle) {
            boolean complete = false;
            while (!complete) {
                try {
                    ((Future<?>) item).get();
                    complete = true;
                } catch (InterruptedException exception) {
                    interrupted = true;
                    if (failure == null) {
                        failure = exception;
                    }
                } catch (ExecutionException exception) {
                    if (failure == null) {
                        failure = exception.getCause();
                    }
                    complete = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        if (failure != null) {
            throw new IllegalStateException("Box2D task failed", failure);
        }
    }
}

ExecutorService physicsExecutor = Executors.newFixedThreadPool(4);
ExecutorTaskScheduler scheduler = new ExecutorTaskScheduler(physicsExecutor, 4);

b2WorldDef worldDef = B2.b2DefaultWorldDef();
worldDef.setTaskScheduler(scheduler);
b2WorldId worldId = B2.b2CreateWorld(worldDef);
```

`setTaskScheduler` only wires the three C-style fields; it does not retain or
own external resources. The application must shut down `physicsExecutor` at
the appropriate engine lifecycle point. Platforms without a complete
`ExecutorService`, including alternative runtimes such as TVM, can implement
the same small interface using their native job system or assign
`enqueueTask`, `finishTask`, and `userTaskContext` directly.

Worker indices must be exclusive slots in `[0, workerCount)`, even when tree
rebuild and narrow-phase tasks overlap. The callback should generally keep
each range at least `minRange` elements long. `finish` is a barrier and must
not return while any range in the handle is still running.

Call `b2World_Step` from one simulation thread per world. Custom filter,
pre-solve, friction, and restitution callbacks may run on task workers and
must be thread-safe when multithreading is enabled. The default world remains
single-threaded and requires no executor.

The graphical sample viewer contains its own `ExecutorService` bridge under
`src/debugger`; it is deliberately outside the core artifact. It defaults to
half the available processors, capped at eight workers. Override it with:

```bash
./gradlew runDebugger -PdebuggerWorkers=4
```

## Allocation profiling

The measured audit and remaining hotspot list are in
[`PERFORMANCE.md`](PERFORMANCE.md).

The core reuses per-world solver states, joint/contact constraints, graph
colors, dynamic-tree query/rebuild stacks, and thread-local narrow-phase and
solver scratch objects. Scratch values use an internal mark/release discipline
and are copied before they enter persistent or public state, so each Box2D task
worker receives independent temporary storage without adding an executor to
the core artifact.

The primary performance suite uses JMH 1.37 with isolated forks, a negative
control, and the JMH `gc` profiler. It covers all 16 upstream benchmark scenes,
Large World, public collision entry points, dynamic-tree traversal, debug
capture, and the viewer-owned scheduler at 1/2/4/8 workers:

```bash
./gradlew jmh
```

Results are written to `build/reports/jmh/results.txt` and `results.json`.
Use `-PjmhIncludes='.*DynamicTreeBenchmark.*'` to select a benchmark family or
`-PjmhProfiler=jfr` to create per-fork Flight Recorder captures. The lighter
`./gradlew profileAllocations` task remains available for long, fixed-step
Tumbler and Large Pyramid allocation checks.

On the optimization worktree, a 1,200-step Bridge run with a 256 MB G1 heap
dropped from 14 to 3 young collections. These are measured JVM allocation
figures, not a zero-allocation guarantee: collision manifold construction,
active event snapshots, and selected CCD paths remain the largest opportunities
for future work.

The final Java 11 JMH audit measures an empty world step at approximately
`0.004 B/op` (measurement floor), dynamic-tree query/ray/shape traversal at 24
B/op for their public result, and documents the remaining scene, collision,
debug-draw, event, and scheduler costs in `PERFORMANCE.md`.

## Build

```bash
./gradlew test
```

The CI workflow runs the same suite as two independent jobs:

```bash
./gradlew unitTest
./gradlew parityTest
```

`unitTest` contains the Java unit, API-surface, catalog, and runtime tests.
`parityTest` contains the C/C++ reference probes, multithreading parity checks,
and sample parity tests.

## Continuous integration and publishing

`.github/workflows/ci.yml` runs on every push, pull request, and published
GitHub release. It starts independent `build`, `unit-tests`, and
`parity-tests` jobs. Publishing starts only after all three jobs succeed.

Every non-tag push publishes `org.ngengine:box2d4j:3.1.1-SNAPSHOT` to the
Maven Central snapshots repository. Pull requests are verified but never
published. Publishing a release from the GitHub UI publishes its tag as the
Maven version and closes/releases the Sonatype staging repository. A leading
`v` is removed, so release tag `v3.1.1` publishes version `3.1.1`.

Configure these GitHub Actions repository secrets:

- `SONATYPE_USERNAME`: Maven Central/Sonatype token username
- `SONATYPE_PASSWORD`: Maven Central/Sonatype token password
- `GPG_PRIVATE_KEY`: ASCII-armored private signing key
- `GPG_PASSPHRASE`: signing-key passphrase

The publishing configuration uses the Gradle Nexus Publish Plugin and the
same environment variable names for local publishing. `GROUP` can override
the default `org.ngengine` group and `VERSION` can override the default
snapshot version. For example:

```bash
SONATYPE_USERNAME=... \
SONATYPE_PASSWORD=... \
GPG_PRIVATE_KEY="$(cat private-key.asc)" \
GPG_PASSPHRASE=... \
./gradlew publishToSonatype
```

Run every headless sample:

```bash
./gradlew runSamples
```

If the Gradle wrapper is not present yet, use the installed Gradle:

```bash
gradle test
```
