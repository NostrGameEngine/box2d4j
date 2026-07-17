# Upstream Porting and Upgrade Guide

This document is the maintenance contract for updating box2d4j to a newer
Box2D release. It explains which differences from upstream are deliberate,
why they exist, and which invariants an automated or human porting pass must
preserve.

Read this guide before changing the vendored Box2D revision. Also read
[`FEATURE_PARITY.md`](FEATURE_PARITY.md), [`PORTING_STATUS.md`](PORTING_STATUS.md),
and [`PERFORMANCE.md`](PERFORMANCE.md). Those files describe the current
evidence and performance baseline; this file describes how to evolve the port
without accidentally changing its design.

## Current Baseline

- Upstream repository: <https://github.com/erincatto/box2d>
- Upstream release: `v3.1.1`
- Upstream commit: `8c661469c9507d3ad6fbd2fea3f1aa71669c2fe3`
- Local reference: the `vendor/box2d` Git submodule
- Language target: pure Java 11
- Published coordinates: `org.ngengine:box2d4j`
- Native bridge policy: no JNI in the core artifact

The submodule is not merely an attribution copy. Unit tests inspect its public
headers and parity tests compile its C and C++ sources. Updating it changes the
reference implementation against which box2d4j is judged.

## Porting Priorities

When goals conflict, use this order:

1. Preserve Box2D behavior, lifecycle, ordering, validation, and observable
   floating-point results.
2. Preserve the upstream public names and API concepts so code can be compared
   mechanically with the C implementation.
3. Keep the core pure Java 11 and usable on restricted runtimes such as TVM.
4. Avoid steady-state allocations in simulation hot paths without allowing
   pooled or scratch values to escape.
5. Keep Java-only conveniences additive. They must not replace or obscure the
   upstream-shaped API.

"Java-friendly" in this project means adapting mechanisms Java cannot express
directly. It does not mean redesigning the Box2D API into an unrelated
object-oriented facade.

## Repository Map

| Path | Role during an upgrade |
| --- | --- |
| `vendor/box2d` | Exact upstream source and native parity reference |
| `src/main/java/org/box2d4j` | Published Java 11 core API and implementation |
| `src/samples/java/org/box2d4j/samples` | Headless ports of upstream samples and interactive binding metadata |
| `src/debugger/java/org/box2d4j/debugger` | jMonkeyEngine viewer and JVM-specific scheduler adapter |
| `src/test/java/org/box2d4j` | Java regressions, API inventories, and C/C++ parity drivers |
| `tools/parity` | Native probe programs used by parity tests |
| `src/jmh/java` | JMH performance and allocation benchmarks |
| `.github/workflows/ci.yml` | Build, unit, parity, and publishing gates |

Most upstream exported functions and internal algorithms are consolidated in
`B2.java`. Public C structs and callback typedefs are separate Java classes or
functional interfaces. This asymmetry is intentional: it keeps public type
names recognizable while allowing closely related C translation units to
share private Java implementation records.

## Deliberate Divergences

The following differences are adaptations, not missing features. Preserve
them unless a later design decision explicitly supersedes this document.

### Public API shape

| Upstream Box2D | box2d4j | Reason and upgrade rule |
| --- | --- | --- |
| Exported and inline C functions | Public static methods, primarily on `B2` | Keep every `b2...` name. Add the new upstream name before considering convenience aliases. |
| C structs | Mutable Java classes with matching field names | Preserve mutability, names, defaults, and copy semantics. Do not turn them into records or immutable builders. |
| C enums | Exact `public static final int` values on `B2` | Integers preserve C call shapes and bitwise behavior. Do not replace the core API with Java enums. |
| Function-pointer typedefs | `@FunctionalInterface` types with `invoke(...)` | Keep the upstream typedef name and argument order. Context pointers become `Object` where required. |
| Pointer output parameters | Return objects, result wrappers, or Java arrays | Choose the smallest adaptation that preserves all outputs. Document any overload that cannot match C one-to-one. |
| Null or zero-value IDs | Mutable Java ID objects whose zero fields represent null | Return fresh or defensively copied IDs where mutation could corrupt engine state. Never rely on Java reference identity for Box2D identity. |
| Private pointer fields in `b2DynamicTree` | Package-private Java storage arrays | The five upstream-private storage fields are intentionally absent from the public Java field surface. Public counters remain public. |

`PublicApiSurfaceTest` derives exported functions, inline functions, callback
types, structs, fields, constants, and enum values from the vendored headers.
Its expected totals and enum parser currently describe v3.1.1. A new upstream
release may require updating both the implementation and the inventory parser;
never update an expected number merely to make the test green without
reviewing the added or removed declarations.

### Data ownership and memory

Upstream arenas, typed arrays, pointer arithmetic, and explicit allocation are
represented with Java objects, primitive arrays, and collections. Internal
world state is retained in private slots such as `WorldSlot`, `BodySlot`,
`ShapeSlot`, `ContactSlot`, and `JointSlot`.

Important consequences:

- Java field arrangement is not a C ABI promise. Preserve logical layout and
  behavior, not native byte offsets.
- ID pools use upstream-style generations and LIFO free lists.
- Solver body and contact collections preserve upstream append and remove-swap
  ordering. Body contact lists preserve upstream head insertion and destruction
  order. Replacing these with unordered or insertion-preserving structures can
  change simulation bits even when the set of objects is the same.
- Mutable definitions, shapes, materials, filters, transforms, events, and IDs
  are copied at the same ownership boundaries as upstream. Do not retain a
  caller-owned mutable object where C copied the struct.
- Native `sizeof` values reported by counters or memory dumps are compatibility
  measurements, not `Instrumentation` estimates of Java objects. The dynamic
  tree currently uses the upstream 72-byte ARM64 C ABI size where native
  accounting requires it.

box2d4j exposes the upstream-shaped `b2AllocFcn`/`b2FreeFcn` pair and two
conveniences: `B2Allocator` and `IntFunction<ByteBuffer>`. The default allocator
is `ByteBuffer::allocateDirect`. `b2Alloc` rounds the requested native size to
32 bytes and forwards alignment 32 while accounting the unrounded requested
size, matching the tested v3.1.1 contract.

The allocator controls explicit Box2D native-style buffers. It cannot and must
not pretend to control Java heap objects or ordinary collection growth. An
engine allocator can be connected to the direct-buffer path, but Java-managed
world records remain managed by the JVM.

### Allocation reduction and scratch memory

Upstream stack and arena temporaries are replaced by retained per-world state
and reentrant thread-local scratch storage:

- solver vectors, rotations, and matrices use a mark/release stack;
- polygon collision workspaces are retained per thread and nesting depth;
- dynamic-tree query, ray-cast, shape-cast, and rebuild workspaces are retained
  per thread and nesting depth;
- solver states and typed constraints are retained by their owning world,
  body, contact, or joint;
- profile/event wrappers and debugger draw buffers are reused.

The non-escape rule is absolute: a scratch value must never be assigned to a
persistent field, returned through the public API, stored in an event, or
retained by a callback. Copy its final value into the owner or result before
releasing the scratch mark. Scratch implementations must support nested calls
because tree and collision callbacks can re-enter query code.

Public value-returning math and collision methods may allocate to preserve the
C-shaped API. Optimize internal call sites with private output-parameter or
scratch variants only after profiling identifies a hot allocation. Do not
silently change public result ownership to save an allocation.

### Task system and multithreading

Box2D delegates scheduling to the embedding application. box2d4j preserves
that model:

- `b2WorldDef` exposes `workerCount`, `enqueueTask`, `finishTask`, and
  `userTaskContext`.
- `B2TaskScheduler` is an additive Java adapter for those hooks.
- The core provides no scheduler implementation and has no dependency on
  `ExecutorService`.
- With no hooks configured, the world follows the serial path.
- A scheduler must honor `minRange`, finish every submitted range before
  returning from `finish`, and provide an exclusive `workerIndex` in
  `[0, workerCount)` while a callback executes.
- Parallel stages and their barriers must follow upstream. Broad-phase
  mutation and overflow constraints remain serial where ordering requires it.

The debugger's `DebuggerTaskScheduler` is the example `ExecutorService`
implementation. It belongs in the debugger source set, not in the published
core. Future JVM conveniences may be documented or provided in optional
modules, but a default executor must not be added to `src/main`.

Multithreading parity means more than obtaining a plausible final picture.
Test the same scene at 1, 2, 4, and 8 workers against the upstream C++ task
probe, including callback completion, worker-index exclusivity, failure
propagation, stage counts where meaningful, and final simulation state.

### Floating point and SIMD

The Java solver is scalar. It does not reproduce upstream architecture-specific
SIMD code structurally. Preserve the upstream scalar operation order and avoid
algebraic simplification, reassociation, fused expressions, or unnecessary
double intermediates. A mathematically equivalent rewrite can still break
raw-float parity.

Java `Math.sin` and `Math.cos` calculate in double and are then narrowed,
whereas upstream samples may call platform `sinf` and `cosf`. Prefer Box2D's
deterministic helpers such as `b2ComputeCosSin` in core behavior when upstream
does. A sample-only platform-libm difference must be identified as such; do not
distort the core solver to match one host's C library.

For sensitive changes, compare Java with both the native default build and a C
build using `BOX2D_DISABLE_SIMD`. Exact agreement in registered scenarios is
evidence for those scenarios, not proof that Java can reproduce every CPU's
SIMD rounding.

### Assertions, invalid inputs, and world locking

`b2SetAssertFcn` is represented by a Java functional interface. A failed
Box2D assertion invokes the handler and then raises `AssertionError` when the
handler requests a break. Definition cookies (`internalValue`) and upstream
preconditions remain part of observable compatibility.

Every upstream API guarded by `world->locked` must reject the same reentrant
operation during a step. Java methods return the corresponding neutral value,
empty result, null ID, or no-op result after the assertion, as appropriate for
their return type. `b2World_Step` must unlock in a `finally` boundary when a
callback or scheduler throws.

Do not replace these rules with broad Java exceptions based on taste. First
observe the exact C assertion, mutation, and return behavior with a native
probe, then implement the closest Java result.

### Counters, profiles, and diagnostic memory reports

All upstream profile fields are populated at corresponding pipeline
boundaries, but timing uses `System.nanoTime()`. An executed phase receives the
smallest positive float if both reads use the same timer tick. Conditional
phases that did not execute remain zero, and profile getters return defensive
copies.

Two counters intentionally differ in interpretation:

- `stackUsed` is zero because Java heap and thread-local scratch replace the C
  arena allocator.
- `taskCount` counts Java task-stage dispatches and is useful for scheduler
  diagnostics; it need not equal a differently partitioned SIMD build.

Memory reports retain recognizable upstream sections. Java-managed sections
may report live counts and `n/a` rather than inventing byte precision. Tree,
hash, and explicit allocator bytes remain concrete where the native contract
can be reproduced.

### Samples and interactive controls

Headless sample classes are close ports with a public static `run()` entry
point and a finite horizon suitable for deterministic tests. The graphical
debugger intentionally continues a retained world after that horizon so users
can watch it indefinitely. Do not make headless parity tests infinite.

Interactive upstream UI actions are registered through the thread-local
`SampleRuntime` adapter. It supports actions, holds, toggles, choices, integer
and float controls, pointer handlers, camera targets, step hooks, target rate,
and snapshot suppliers. Whenever a new or changed upstream sample has controls:

1. port the default values and action timing;
2. register a stable binding ID and user-facing label;
3. apply world mutations on the simulation thread, normally before a step;
4. add or update `SampleBindingCoverageTest` and a focused behavior test;
5. include the same scripted action in the native sample parity probe when it
   affects simulation behavior.

The catalog mirrors active upstream `RegisterSample` entries. Code hidden by
upstream `#if 0`, such as the v3.1.1 `Mover2` experiment, is not an active
sample and is intentionally excluded. The standalone HelloWorld tutorial is
included as an additional entry.

### Graphical debugger

The jMonkeyEngine viewer is a project tool, not part of Box2D's public core
and not part of the published JAR. Its threading model is deliberate:

- each sample owns a dedicated simulation thread;
- all live world operations, controls, pointer actions, debug capture, and
  destruction occur on that thread;
- a reusable three-frame exchange publishes the newest complete snapshot;
- the jMonkeyEngine render thread only consumes snapshots and never calls into
  the live world;
- when rendering falls behind, stale frames are dropped instead of blocking
  physics;
- `B2DebugHooks` and `SampleRuntime` are thread-local tooling bridges used to
  observe sample lifecycle without rewriting every sample around the viewer.

This separation prevents `b2World_Draw` or mouse-joint operations from racing
with a locked world. An upgrade must not move physics stepping or world capture
back onto the jMonkeyEngine render loop.

### Build and publication boundaries

`components.java` publishes only `src/main`. Samples, debugger, jMonkeyEngine,
native probes, and JMH are development inputs and must not leak into the core
POM or runtime dependency graph. Keep `options.release = 11` and verify the
result with a Java 11 toolchain even when development occurs on a newer JDK.

The CI workflow has independent build, unit-test, and parity-test jobs. Snapshot
or release publication is gated on all three. A release tag sets the Maven
version; normal pushes publish the configured snapshot.

## Upgrade Workflow

Use a dedicated branch and keep algorithmic changes reviewable. Do not mix a
new upstream version with unrelated cleanup.

### 1. Establish the old baseline

Before changing the submodule:

```bash
git status --short --branch
git submodule update --init --recursive
git -C vendor/box2d describe --tags --exact-match
git -C vendor/box2d rev-parse HEAD
./gradlew --no-daemon clean unitTest assemble
./gradlew --no-daemon parityTest
```

Record the old release, commit, test results, parity horizon, and relevant JMH
numbers. If the old baseline fails, diagnose that first; otherwise old defects
become indistinguishable from upstream changes.

### 2. Pin the new upstream revision

Fetch tags in the submodule, check out the exact target tag or commit, and
record both SHAs:

```bash
git -C vendor/box2d fetch --tags origin
git -C vendor/box2d checkout <target-tag>
git -C vendor/box2d rev-parse HEAD
```

Do not track an unpinned moving branch. Update the version and description in
`build.gradle`, the README baseline, status documents, and CI snapshot version
only after the target commit is known.

### 3. Inventory the upstream delta

Start from files and public declarations, not release-note prose alone:

```bash
git -C vendor/box2d diff --stat <old-sha>..<new-sha>
git -C vendor/box2d diff --name-status <old-sha>..<new-sha> -- \
  include src samples test
git -C vendor/box2d diff <old-sha>..<new-sha> -- include/box2d
rg -n 'B2_API|B2_INLINE|typedef (struct|enum)|RegisterSample\(' \
  vendor/box2d/include vendor/box2d/samples
```

Classify every change as one or more of:

- public function, type, field, constant, enum, callback, or default change;
- validation, locking, lifetime, event, or callback-order change;
- algorithm or floating-point operation-order change;
- ID pool, array ordering, broad-phase, island, or solver-set change;
- task stage, partition, barrier, worker scratch, or SIMD change;
- allocator, counter, profile, or diagnostic change;
- sample registration, default, control, camera, or debug-draw change;
- build-only, documentation-only, or unsupported platform change.

Maintain a checklist linked to upstream commits or diff hunks. A changed
internal function can affect behavior even when no header changed.

### 4. Update the public surface first

Port new or changed public classes, fields, constants, callbacks, defaults, and
method signatures before deep implementation work. This makes the header
inventory fail in a useful way and exposes the complete work queue.

For each declaration:

- preserve its upstream name and field order conceptually;
- preserve default values and definition cookies;
- decide and document pointer, ownership, null, and output-parameter mappings;
- add a Java-friendly overload only when it is additive;
- extend `PublicApiSurfaceTest` if upstream syntax or enum families changed;
- add a default-definition native probe where applicable.

### 5. Port internal behavior in dependency order

A practical order is:

1. constants, math, IDs, arrays, bitsets, tables, and allocators;
2. geometry, hulls, distance, casts, manifolds, and dynamic trees;
3. world slots, bodies, shapes, broad phase, contacts, and events;
4. joints, islands, solver sets, constraint graph, and sleeping;
5. solver stages, continuous collision, sensors, profiles, and debug draw;
6. task partitioning and multithreaded barriers.

Translate the upstream control flow and arithmetic order closely. Retain Java
storage and scratch conventions from this guide. Avoid opportunistic
refactors while parity is red; first make the new reference behavior visible,
then optimize with tests and measurements.

### 6. Update samples and tooling

Re-inventory all active `RegisterSample` calls, not only newly added source
files. Port changed defaults, setup order, scripted actions, cameras, and UI
controls. Update `SampleCatalog`, `SampleRuntime` bindings, focused sample
tests, native probes, and debugger result rendering.

If upstream changes debug-draw callbacks or world lifecycle, audit
`WorldDrawBatch`, `JmeDebugRenderer`, `MouseDragController`, `SampleSession`,
and `B2DebugHooks` together. The viewer must keep world ownership on the
simulation thread.

### 7. Build native parity evidence

For each behaviorally meaningful upstream change, create or extend a small
program in `tools/parity` that uses the new upstream checkout directly. The
corresponding JUnit test should compile it with Clang, run it, execute the same
Java scenario, and compare structured output.

Prefer these comparisons, in descending strength:

1. raw `float` bit patterns and exact integers/IDs/order hashes;
2. complete event and callback sequences with arguments;
3. per-step state hashes plus selected full records at checkpoints;
4. a documented tolerance only when exact equality is impossible for a proven
   platform reason.

Do not compare only the last transform. Check intermediate steps, transient
events, counters, contact/joint order, wake/sleep state, IDs, and callback
timing. Short focused probes diagnose failures; generated worlds and long
horizons catch interactions.

Native C probes must remain portable to both Apple Clang and Ubuntu Clang. The
current C17 setup defines `_POSIX_C_SOURCE=200809L` and links `libm`. The task
probe is C++ because its reference scheduler uses C++ threading facilities.

### 8. Revalidate allocation and concurrency

Run focused JMH/JFR measurements after parity is restored. Check empty-world,
representative benchmark scenes, dynamic-tree operations, debug capture, and
1/2/4/8-worker stepping. Compare on the same machine, JVM, heap, warmup, and
scene horizon.

An upstream port is not complete if it introduces an avoidable allocation per
body/contact/constraint per frame. It is also not complete if pooling creates
aliasing, callback reentrancy bugs, cross-world sharing, or races. Add a focused
regression whenever new scratch storage is introduced.

### 9. Update evidence and release metadata

Before declaring parity, update:

- `README.md` version and high-level capabilities;
- `FEATURE_PARITY.md` inventory, status matrix, and verification boundary;
- `PORTING_STATUS.md` detailed evidence and any known gaps;
- `PERFORMANCE.md` only when measurements or allocation design changed;
- this guide when a new deliberate divergence or maintenance rule was added;
- Gradle/CI version strings and Maven metadata.

Status documents must distinguish `Covered`, `Covered at tested horizons`,
`Partial`, and `Missing`. Never turn finite test coverage into an unqualified
claim of mathematical equivalence.

## Verification Commands

The minimum local gate is:

```bash
./gradlew --no-daemon clean unitTest assemble
./gradlew --no-daemon parityTest
```

For changes involving performance, threading, samples, or the viewer, also
run the relevant subset of:

```bash
./gradlew --no-daemon runSamples
./gradlew --no-daemon runDebugger
./gradlew --no-daemon profileAllocations
./gradlew --no-daemon jmh
./gradlew --no-daemon jmh -PjmhProfiler=jfr
```

`parityTest` requires a functioning C/C++ compiler and is intentionally slower
than `unitTest`. Do not substitute a Java-only test run for native parity.

## Common Porting Traps

- Using `double` literals or `Math` operations where upstream rounded to float
  between operations.
- Reordering commutative expressions, loops, contact pairs, graph colors, or
  remove-swap collections.
- Treating mutable ID objects as identity-bearing references or exposing an
  internal ID instance to callers.
- Forgetting generation increments and LIFO reuse after destruction.
- Retaining caller-owned definitions, materials, filters, arrays, or vectors
  that upstream copied.
- Returning a thread-local scratch object or storing it in a manifold/event.
- Using one scratch object per thread without supporting nested callbacks.
- Sharing a worker index concurrently across scheduler ranges.
- Completing `finish` before every range and exception has been observed.
- Adding `ExecutorService`, jMonkeyEngine, JMH, or desktop classes to the core
  artifact.
- Calling live-world APIs from the debugger render thread.
- Making a sample's headless parity loop infinite instead of extending only
  the graphical session.
- Porting a sample but omitting its interactive controls or applying them on
  the render thread.
- Updating API-count assertions without examining the declarations that
  changed.
- Assuming a passing final-frame screenshot proves callback, event, or solver
  parity.
- Reporting Java object memory as though it were the upstream C arena.
- Optimizing before capturing the changed upstream behavior in a native probe.

## Definition of Done for a New Box2D Version

An upgrade is complete only when all applicable items below are true:

- [ ] `vendor/box2d` is pinned to the exact target tag and commit.
- [ ] The upstream diff has been inventoried across headers, core, samples,
      tests, task stages, and debug draw.
- [ ] Every active upstream public declaration is represented with reviewed
      Java ownership and output mappings.
- [ ] Defaults, constants, enum values, validation, locks, and callback order
      match the new reference.
- [ ] IDs, generations, free lists, collection order, and solver stage order
      preserve the new upstream behavior.
- [ ] No default scheduler or `ExecutorService` dependency was added to core.
- [ ] Serial and 1/2/4/8-worker scenarios have native parity where the task
      system changed.
- [ ] Every active upstream sample is cataloged; interactive controls and
      default camera behavior are represented.
- [ ] New or changed behavior has focused C/C++ probes and Java regressions.
- [ ] `unitTest`, `parityTest`, and `assemble` pass from a clean checkout.
- [ ] Relevant long-horizon and generated-world comparisons pass.
- [ ] JMH/JFR shows no unexplained hot-path allocation or major regression.
- [ ] The debugger runs without live-world access from the render thread.
- [ ] README, feature parity, porting status, performance notes, versions, and
      CI metadata describe the new baseline accurately.
- [ ] Remaining differences are explicitly classified as deliberate,
      partial, missing, or platform-dependent.

## Prompt Template for a Future AI Upgrade

The following prompt can be given to an AI coding agent together with access
to the repository:

```text
Update box2d4j from its current vendored Box2D baseline to <target tag or
commit> from https://github.com/erincatto/box2d.

Before editing, read UPSTREAM_PORTING_GUIDE.md, FEATURE_PARITY.md,
PORTING_STATUS.md, PERFORMANCE.md, README.md, and the live Gradle/source-set
configuration. Treat UPSTREAM_PORTING_GUIDE.md as the maintenance contract.

Fetch and pin vendor/box2d to the exact target commit, record the old/new SHAs,
and inventory every change in include/, src/, samples/, tests, task stages,
and debug draw. Preserve C-style b2 names, mutable public data classes, exact
int constants, Java 11, the pure-Java/no-JNI core, upstream ordering and
floating-point operation order, native allocator hooks, and the hook-only
task system. Do not add a default ExecutorService implementation to core.

Keep samples, jMonkeyEngine debugger, JMH, and native probes outside the
published core. Preserve the debugger's dedicated simulation thread and
latest-frame snapshot exchange. Add SampleRuntime bindings for every changed
interactive control while keeping headless parity horizons finite.

For each changed behavior, add or extend a small native C/C++ probe against the
vendored target and compare Java at intermediate checkpoints, preferably with
raw float bits, exact IDs/order, events, callbacks, and counters. Test serial
and 1/2/4/8 workers where task behavior changed. Do not accept a tolerance
without documenting and proving the platform-specific reason.

Run clean unitTest, parityTest, and assemble. Profile changed hot paths with
JMH/JFR, preserve reentrant thread-local scratch non-escape rules, and add
regressions for new pooling. Finally update README.md, FEATURE_PARITY.md,
PORTING_STATUS.md, PERFORMANCE.md when applicable, this guide when decisions
change, Gradle/CI version metadata, and report every remaining gap explicitly.
Do not claim complete parity solely because finite tests pass.
```

This template is intentionally strict. A future upstream release may require a
new adaptation, but that adaptation should be documented here together with
its rationale, test evidence, and rule for subsequent upgrades.
