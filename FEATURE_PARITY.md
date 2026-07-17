# Feature Parity Audit

Reference: Box2D `v3.1.1`, commit
`8c661469c9507d3ad6fbd2fea3f1aa71669c2fe3` under `vendor/box2d`.

Audit date: 2026-07-13.

Current verdict: Box2D v3.1.1 has no known missing public API, active sample,
core task-hook, or tested physics behavior in the Java port. No divergence or
non-zero tolerance remains in the registered C/C++ parity scenarios. Finite
scenario coverage is not by itself mathematical proof of strict 1:1 behavior
for every possible simulation and callback sequence.

## Status Terms

- **Covered:** implemented and backed by direct C/C++ comparison or focused
  Java tests.
- **Covered at tested horizons:** the registered scenarios pass for the exact
  setup, actions, duration, and tolerances stated in `PORTING_STATUS.md`.
- **Partial:** substantial behavior exists, but an upstream path or observable
  output is missing or has incomplete evidence.
- **Missing:** known upstream behavior is not implemented.

## Public Surface

- **Functions:** 422/422 exported `B2_API` names and all 73 public `B2_INLINE`
  names have public static Java entry points. `PublicApiSurfaceTest` derives
  both inventories from the vendored headers on every unit-test run. A
  separate audit also confirms that every exported name is referenced by at
  least one Java test or native parity probe; this is direct nominal coverage,
  not a claim of exhaustive inputs.
- **Types and values:** all 71 public struct definitions and all 16 public
  callback typedefs have matching Java classes or functional interfaces. All
  six public `static const` values are represented. `PublicApiSurfaceTest`
  derives these inventories from the same headers and verifies every public
  struct field name. The only non-public Java fields are the five
  `b2DynamicTree` storage arrays that upstream explicitly labels private data.
  All 168 enumerators across the five public
  enums (`b2BodyType`, `b2ShapeType`, `b2JointType`, `b2TOIState`, and
  `b2HexColor`) have exact-value `int` constants in `B2`.
- **Callbacks:** all 16 C callback typedef names are functional interfaces.
  Drawing callback context pointers are normally captured by Java lambdas; the
  public `b2DebugDraw.context` field remains available for source parity.
- **Java adaptations:** pointer output parameters become return values or Java
  arrays where appropriate. API-name coverage does not by itself claim that
  every upstream precondition, callback restriction, or side effect is equal.

## Verification Snapshot

- `unitTest`: 21 suites, 39 tests, zero failures/errors/skips.
- `parityTest`: 154 suites, 168 tests, zero failures/errors/skips. The native
  reference inventory is 153 C probes plus one C++ multithreading probe.
- The two Gradle test tasks cover all 175 test classes without overlap. On the
  current worktree, `parityTest --rerun-tasks` completed in 8m26s and
  `unitTest assemble --rerun-tasks` completed in 11s. The
  preceding 125-suite baseline also passed on the Ubuntu GitHub Actions runner
  after making the C17 probes portable to glibc with
  `_POSIX_C_SOURCE=200809L` and explicit `libm` linkage.
- The sample catalog contains all 110 active upstream `RegisterSample`
  creators plus the standalone HelloWorld tutorial. The disabled `Mover2`
  experiment under upstream `#if 0` is intentionally excluded.
- `SampleBindingCoverageTest` inventories 59 interactive sample entries and
  requires each to register debugger controls through `SampleRuntime`.

## Feature Matrix

| Area | Status | Evidence or boundary |
| --- | --- | --- |
| Math, geometry, GJK, shape cast, TOI, manifolds | Covered | Direct C probes and exact/raw-float parity tests, including transformed polygon construction, oversized proxy clamping, 512 deterministic QuickHull clouds, 512 world-space shape ray casts across all five shape kinds, 2,048 segment-distance/GJK/cache/shape-cast/TOI cases, and 1,024 manifold cases across eight primitive/polygon pair families |
| Dynamic tree and broad phase | Covered | Tree traversal/rebuild, proxy reuse, AABB/category access, pair/contact order, query, cast, and invariant probes |
| Worlds, bodies, shapes, chains, and joint APIs | Covered at tested horizons | Public surface, lifecycle, property, solver, and sample parity suites, including eight deterministic generated worlds with 96 mixed bodies, 24 mixed joints, 4,800 aggregate steps, per-step counters, callback argument multisets, and scripted runtime mutations; world-pool exhaustion/recycling, stale-destroy assertions, independent Java null-handle values, global sleeping-disable wake propagation, awake-body remove-swap order, kinematic rotational extents, solver-set velocity lifetime, disable/destroy wake propagation, exact broad-phase proxy reinsertion after direct transforms, disabled target-transform state, degenerate-capsule creation fallback, and the runtime speculative-contact toggle |
| Public input preconditions | Covered for audited inputs | Definition cookies plus dynamic-tree, geometry/manifold, GJK/TOI, factory, joint setter, body creation/transform, mover/explosion, world query/cast, conditional debug-bounds, shape/material, mass/damping/gravity, and maximum-speed cases have 110 directly observed C/Java outcomes: 104 assertions and six intentional non-assert controls; this remains focused evidence rather than an exhaustive invalid-input matrix |
| Public default definitions | Covered | All 17 `b2Default...` constructors compare world/body/shape/chain/filter/material/explosion/debug-draw fields plus joint non-zero defaults and definition cookies directly with C |
| Contact/joint solver, graph colors, and sleep islands | Covered at tested horizons | HelloWorld, Falling Hinges, joint suites, registered sample horizons, generated distance/revolute/weld worlds with runtime setters and destruction, awake-set non-touching contact remove-swap order, sleeping-set wake order, and awake-kinematic contact pruning when the dynamic peer is sleeping |
| Queries, casts, movers, explosions, callbacks, and events | Covered | Dedicated C/Java parity suites, including 4,800 generated-world sensor checkpoints with overlap ID checksums and transient begin/end counts, directional pre-solve/friction/restitution callback argument multisets, controlled custom-filter A/B order and sensor exclusion, miss results and front/back one-sided chain-segment ray casts, the locked `b2World_CastMover` neutral fraction of `1.0f`, and exact friction/restitution callback creation/update/reset lifecycle with raw material arguments |
| Continuous collision and bullets | Covered at tested horizons | All 15 active Continuous samples plus exact Pinball bullet-versus-dynamic clipping |
| CCD moving-target and pre-solve edges | Covered | Bullet versus moving kinematic target and TOI-time temporary-manifold pre-solve acceptance/rejection have exact C parity in addition to the registered Continuous matrix |
| Multithreading task system | Covered | Hook-only core API, debugger-owned executor bridge, parallel task stages, and C++/Java parity at 1/2/4/8 workers |
| Native-style allocation hook | Covered | Exact `b2AllocFcn`/`b2FreeFcn` pair plus `B2Allocator` and `IntFunction<ByteBuffer>` conveniences, with allocation/free invocation, 32-byte forwarding, accounting parity, and direct-buffer default |
| Active sample catalog | Covered at tested horizons | 110 active upstream creators plus HelloWorld, with per-sample evidence in `PORTING_STATUS.md` |
| Interactive sample controls | Covered for inventoried entries | 59 sample entries register actions, holds, toggles, choices, and numeric controls; not every arbitrary GUI action sequence has parity coverage |
| Graphical debugger | Covered | jMonkeyEngine catalog, indefinite stepping, navigation, controls, counters, diagnostic toggles, labels, debugger-owned multithreading, tested mouse-joint pick/drag/release lifecycle, and a non-blocking three-buffer frame exchange that keeps all world access on the simulation thread |
| Debug draw diagnostics | Covered | Shapes, type-specific joints, revolute extras, island AABBs, contacts, bounds, mass, names, and graph colors have callback parity; debugger toggles expose joint extras and islands |
| Profiling and counters | Covered with runtime adaptation | All 22 `b2Profile` fields are populated at their corresponding pipeline boundaries, including merge, preparation, integration, warm-start, solve/relax, restitution, and impulse-storage sub-phases; measured phases retain a positive minimum even when the JVM timer returns the same tick, while conditional phases not executed remain zero. A native activity mask verifies every solver phase observed in C is observed in Java. Static/dynamic tree heights have exact C parity; `taskCount` counts Java task stages and `stackUsed` is zero because the Java port has no C arena |
| Memory statistics dump | Covered with runtime adaptation | Preserves upstream id-pool, world-array, broad-phase, solver-set, graph, and stack sections; Java-managed storage is marked `n/a` with live counts, tree/hash bytes remain concrete, and native-hook bytes have a dedicated section |
| Managed allocation pressure | Optimized, not allocation-free | Per-world solver, constraint, profile, and event-output reuse plus reentrant thread-local solver, polygon-collision, tree-query/cast, and rebuild scratch reduce a measured 1,200-step Bridge run from 14 to 3 G1 young collections and the JMH empty-world baseline from 984 B/step to the approximately 0.004 B/step measurement floor; the JMH `gc` matrix tracks all benchmark scenes, collision APIs, tree operations, debug capture, and 1/2/4/8-worker stepping |
| World locking and callback mutation policy | Covered | All upstream lock-guarded public families reject access while stepping; reentrant step, null-id creation, no-op mutation, empty event/query returns, and exception-safe unlock have focused Java and C parity coverage |
| Step argument and zero-time contract | Covered | Valid-float and positive-sub-step assertions match upstream; zero-time steps clear transient events/profile and skip pair, contact, solver, and sensor processing, with direct C parity |
| Long-horizon numeric parity | Covered at tested horizons | Sixty registered scenes run bit-exact through 2400 steps in CI, including the earlier stack/contact/sensor/ragdoll/joint set plus Weeble, Capsule Stack, Friction, Restitution, Rolling Resistance, Conveyor Belt, Custom Filter, Chain Shape, Chain Link, Rounded Shapes, Ellipse, Single Box, Kinematic Body, Circle Stack, Tangent Speed, Shape Filter, Recreate Static, HelloWorld, Confined, Gear Lift, and deterministic-target Motor Joint. Shape Filter runs 2400 steps in each of its two filter phases. Driving, Scissor Lift, Card House, and Gear Lift additionally compare native and scalar-C paths with zero tolerance. Continuous and Robustness preserve transient checkpoint matrices through frame 120. No divergence remains in the registered paths |
| CI and Maven delivery | Covered | Java 11 build/unit/parity jobs on pushes and PRs; gated snapshot/release publishing with sources, Javadoc, POM metadata, and signing hooks |

## Implementation Adaptations

These are deliberate Java or project-architecture differences, not missing
Box2D features:

- Java heap objects and collections replace C arena arrays and solver-set
  storage. `B2Allocator` remains available for explicit native-style/direct
  allocations and defaults to `ByteBuffer.allocateDirect`.
- `b2Counters.stackUsed` is always zero because no arena allocator backs the
  Java solver. `taskCount` reports Java task-stage dispatches and therefore is
  meaningful for scheduler diagnostics without claiming the same count as the
  differently partitioned SIMD solver stages in C.
- The Java contact solver is scalar rather than SIMD. Card House is bit-exact
  through 2400 registered steps against both native ARM64 NEON C and C compiled
  with `BOX2D_DISABLE_SIMD`.
  This is scenario evidence rather than a blanket claim that every architecture's
  SIMD instruction rounding can be reproduced portably by Java.
- Upstream samples call platform `sinf`/`cosf`; Java narrows double-precision
  `Math` results. Motor Joint proves the core solver bit-exact through 2400
  steps when both sides use `b2ComputeCosSin`, while the unmodified sample first
  sees a platform-libm target difference at step 111 on the audit machine.
- The core deliberately ships no scheduler implementation and does not refer
  to `ExecutorService`. Engines and alternative runtimes implement
  `B2TaskScheduler`; the debugger owns the JVM-specific executor bridge.
- Samples and the jMonkeyEngine debugger use separate Gradle source sets and
  are not included in the published core `components.java` artifact.

## Verification Boundary

There are no known v3.1.1 implementation gaps. Additional generated worlds,
longer horizons, and callback sequences can strengthen regression evidence but
are ongoing verification rather than missing ported functionality.
