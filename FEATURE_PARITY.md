# Feature Parity Audit

Reference: Box2D `v3.1.1`, commit
`8c661469c9507d3ad6fbd2fea3f1aa71669c2fe3` under `vendor/box2d`.

Audit date: 2026-07-10.

## Public Surface

- **Functions:** 422/422 exported `B2_API` names have public static Java entry
  points. `PublicApiSurfaceTest` derives this inventory from the vendored
  headers on every test run.
- **Records:** all public C structs have Java record-style classes. C pointer
  output parameters become return values or Java arrays where appropriate.
- **Enums:** `b2BodyType`, `b2ShapeType`, `b2JointType`, `b2TOIState`, and
  `b2HexColor` are represented by the matching `int` constants in `B2`.
- **Callbacks:** C function pointers are functional interfaces. Drawing
  callback context pointers are normally captured by Java lambdas; the public
  `b2DebugDraw.context` field remains available for source parity.

## Feature Matrix

| Area | Status | Evidence or boundary |
| --- | --- | --- |
| Math, geometry, GJK, shape cast, TOI, manifolds | Covered | Direct C probes and exact/raw-float parity tests |
| Dynamic tree and broad phase | Covered | Tree traversal/rebuild, proxy reuse, pair/contact order probes |
| Worlds, bodies, shapes, chains, all joint APIs | Covered | Public API and lifecycle parity suites |
| Contact/joint solver, graph colors, sleep islands | Covered | HelloWorld and Falling Hinges exact C state/hash parity |
| Continuous collision and bullets | Covered for registered samples | All 15 active Continuous samples plus bullet/dynamic Pinball parity |
| Queries, casts, movers, explosions, callbacks, events | Covered | Dedicated C/Java parity suites |
| Multithreading task system | Covered | Hook-only core API, debugger-owned executor bridge, parallel solver/collision/broad-phase/sensor/CCD stages, and direct upstream C++/Java parity at 1/2/4/8 workers |
| Samples | Covered | 110 active upstream creators plus HelloWorld; 59 interactive bindings |
| Graphical debugger | Covered | jMonkeyEngine catalog, controls, indefinite graphical stepping |
| Debug draw diagnostics | Partial | Shapes, joints, contacts, bounds, mass, names, graph-colored contacts work; upstream joint extras and island AABBs are not yet drawn |
| Profiling and counters | Partial | Public records exist, but detailed `b2Profile` timings and `stackUsed`, tree-height, and task-count telemetry are not populated |
| Memory statistics dump | Partial | Produces `box2d_memory.txt`, but with fewer allocator/arena sections than upstream |
| World locking during callbacks | Missing | The C world rejects mutation and reentrant stepping while locked; Java does not yet enforce the full per-entry-point lock policy |
| Zero-time step event semantics | Gap | Upstream clears current events and returns before pair/contact/sensor work; Java currently performs collision and sensor updates |
| Remaining CCD edge matrix | Partial evidence | Moving-kinematic targets and custom pre-solve behavior around TOI need dedicated C probes beyond registered samples |

## Implementation Adaptations

These are not public feature gaps:

- Java heap objects and collections replace C arena arrays and solver-set
  storage. `B2Allocator` remains available for explicit native-style/direct
  allocations and defaults to `ByteBuffer.allocateDirect`.
- The Java contact solver is scalar rather than SIMD. Graph coloring and task
  stages preserve deterministic observable results; this is currently a
  performance difference.
- The core deliberately ships no scheduler implementation and does not refer
  to `ExecutorService`. The graphical debugger owns a JVM-specific bridge;
  engines and alternative runtimes implement `B2TaskScheduler` themselves.

## Next Closure Order

1. Enforce upstream world locking and zero-time-step event behavior.
2. Populate profile and counter telemetry.
3. Port joint-extra and island debug drawing.
4. Expand dedicated CCD and long-horizon C parity probes.
