# Porting Status

Upstream reference: `vendor/box2d` at Box2D `v3.1.1`
(`8c661469c9507d3ad6fbd2fea3f1aa71669c2fe3`).

## Implemented

- Gradle Java 11 project with wrapper.
- C-style public names for structs and functions under `org.box2d4j`.
- Configurable allocator hook via `B2Allocator` or `IntFunction<ByteBuffer>`,
  defaulting to `ByteBuffer.allocateDirect`, with C parity for zero-size
  allocation, 32-byte size/alignment forwarding, and byte-count accounting.
- Box2D-compatible user task hooks on `b2WorldDef`, plus the Java-friendly
  `B2TaskScheduler` contract. The core intentionally ships no scheduler and
  has no `ExecutorService` dependency. Broad-phase
  pair finding, dynamic/kinematic tree rebuild, narrow phase, sensor overlap,
  constraint preparation, velocity/position integration, graph-colored
  warm-start/solve/relax/restitution, impulse storage, body finalization, and
  bullet CCD use parallel task stages. Overflow constraints and broad-phase
  mutation remain deterministic serial barriers as in upstream.
- The graphical debugger owns an `ExecutorService` adapter outside the core.
  It honors `minRange`, keeps worker indices exclusive in `[0, workerCount)`,
  waits for every submitted range before propagating task failures, and shuts
  down its pool with the sample session.
- Foundation/math/id/bitset/hash-set utilities, with selected deterministic
  math outputs, id packing/unpacking, bitset behavior, and hash-set capacity/
  byte accounting compared against upstream C.
- Core geometry primitives, AABB, mass, point tests, ray casts, direct
  QuickHull helper,
  with shape mass/AABB/point/raycast and AABB validity/overlap/containment/
  raycast parity against upstream C.
- Shape surface material API with C parity for friction, restitution, material
  id, rolling resistance, tangent speed, custom color, and defensive material
  copying.
- Shape filter and event flag API with C parity for filter copying, filter
  reset/contact destruction, and sensor/contact/pre-solve/hit event toggles.
- Sensor overlap state and begin/end event generation with C parity for
  per-step begin, persisted overlap without duplicate events, end events after
  separation, overlap capacity, visitor ids, and generation-preserving ids.
- Shape geometry getter/setter API with C parity for circle, capsule, segment,
  and polygon copying, type changes, contact reset, and proxy recreation.
- Shape accessor/property API with C parity for `b2Shape_GetBody`,
  `b2Shape_GetWorld`, `b2Shape_SetUserData`, `b2Shape_GetUserData`,
  `b2Shape_IsSensor`, `b2Shape_SetDensity`, `b2Shape_GetDensity`, and
  `b2Shape_GetType`, including Box2D-compatible world generation recycling.
- World-level capsule shape creation with C parity for shape copying, body mass
  integration, contact creation, contact manifold point counts, and explicit
  mass refresh after geometry mutation.
- Shape query API with C parity for `b2Shape_GetAABB`,
  `b2Shape_GetMassData`, `b2Shape_TestPoint`, `b2Shape_RayCast`, and
  `b2Shape_GetClosestPoint` across polygon, capsule, circle, and segment
  shapes.
- Direct Java port of Box2D's GJK `b2ShapeDistance` and conservative
  advancement `b2ShapeCast`, upstream `b2SegmentDistance`, and
  separation-function `b2TimeOfImpact`, including the public circle/capsule/
  segment/polygon shape-cast wrappers.
- Manifold/contact generation routines with exact C parity for circle-circle,
  capsule-circle, polygon-circle, segment-circle, capsule-capsule, and
  polygon-polygon, segment-polygon, chain-circle, chain-capsule, and
  chain-polygon probe cases.
- Initial dynamic tree port with C parity for query, ray-cast, and shape-cast
  hits and visit statistics, proxy counts, category bits, user data, root
  bounds, insertion/removal, movement, enlargement, balancing rotations, and
  partial/full median rebuild.
- Dynamic-tree byte accounting now uses the upstream 72-byte
  `sizeof(b2DynamicTree)` value on the target ARM64 ABI, matching C accounting
  for both plain trees and rebuild scratch capacity.
- World counter island accounting now reports the number of live non-static
  sleep-island roots, matching the upstream island id pool across awake and
  sleeping bodies.
- World counters now expose all 12 upstream constraint-graph color counts,
  including the overflow color, as the combined contact and joint totals.
- Body, shape, chain, and joint ids now use upstream-style LIFO free lists and
  preserve generations across reuse. Body contact lists use head insertion and
  linked-list destruction order, while solver body/contact arrays preserve
  append and remove-swap ordering. This gives exact C parity across mass
  create/destroy/recreate cycles instead of only monotonically allocated ids.
- The dynamic tree reuse probe now destroys and recreates 820 proxies and
  compares proxy ids, root/free-list state, visit counts, and raw traversal
  hash against C.
- Initial broad-phase proxy layer with C parity for proxy key encoding,
  create/destroy/move/enlarge, move buffering, overlap testing, shape index
  lookup, and dynamic/kinematic tree rebuild calls.
- Initial world-level broad-phase pair update and contact creation with C parity
  for HelloWorld contact counts.
- World-level custom filter and pre-solve callback hooks with C parity for
  callback registration, broad-phase contact suppression, pre-solve manifold
  visibility, per-step contact disabling, and conservative contact capacity.
- Contact hit-event generation with C parity for world hit threshold,
  shape/body hit-event flags, post-solver normal velocity, total normal
  impulse gating, hit point, normal, shape ids, and approach speed.
- World-level query/cast API with C parity for `b2World_OverlapAABB`,
  `b2World_OverlapShape`, `b2World_CastRay`, `b2World_CastRayClosest`, and
  `b2World_CastShape`, including filter semantics, callback clipping, closest
  ray hit collection, and dynamic-tree traversal stats.
- World-level mover cast/collision API with C parity for `b2World_CastMover`
  and `b2World_CollideMover`, including capsule mover shape-casts and plane
  result callbacks.
- World shape casts now mirror upstream `shape.c` frame handling exactly by
  transforming the moving proxy into each target shape's local frame and
  transforming the result back to world space. This removes the few-ulp drift
  previously exposed by rotated circle/capsule/polygon casts.
- World-level explosion API with C parity for `b2World_Explode`, including
  dynamic-tree mask filtering, GJK shape-to-point distance, radius/falloff
  scaling, projected shape perimeter impulses, and linear/angular velocity
  application for circle, capsule, and polygon bodies.
- Character mover plane solver helpers with C parity for `b2SolvePlanes` and
  `b2ClipVector`, including accumulated push limits, iteration count, and
  velocity clipping rules.
- Debug draw API surface with C parity for `b2DefaultDebugDraw` and
  `b2World_Draw` shape/bounds/name/mass callbacks, including default no-op
  callback behavior, debug colors, static-proxy fat AABB margins, and default
  surface material custom-color parity. Contact debug draw callbacks now cover
  contact points, graph colors, contact normals, normal impulses, feature ids,
  and friction impulses.
- Chain shape API with C parity for open/loop chain creation, segment shape
  ids, ghost vertices, parent-chain lookup, per-segment material cloning, chain
  material propagation, overlap/raycast visibility, destroy invalidation, and
  chain validity.
- Initial joint definition/create/access/destroy API with C parity for all
  public joint types, default definitions, id/type/body/world accessors, body
  joint enumeration, local anchors, local axes, reference angles,
  collide-connected contact behavior, user data, wake hooks, zeroed constraint
  force/torque accessors, destroy invalidation, and joint counters.
- Distance joint API with C parity for rest length clamping, length-range
  clamping/reordering, spring and limit controls, current transformed length,
  and motor enable/speed/max-force controls.
- Initial distance joint solver path with C parity for a dynamic spring/limit/
  motor simulation, including body transforms, velocities, current length,
  motor force, and constraint force.
- Motor joint API with C parity for linear/angular offsets, creation-time
  correction-factor clamping, setter max-force/max-torque clamping, correction
  factor controls, and force/torque accessors.
- Initial motor joint solver path with C parity for a dynamic linear/angular
  correction simulation, including body transforms, velocities, constraint
  force, and constraint torque.
- Mouse joint API with C parity for target storage, local anchor computation
  from the creation target, spring hertz/damping controls, max-force controls,
  and force/torque accessors.
- Initial mouse joint solver path with C parity for a dynamic target-spring
  simulation, including body transforms, velocities, constraint force, and
  constraint torque.
- Weld joint API with C parity for local anchors, reference angle controls,
  linear/angular hertz and damping-ratio controls, and force/torque accessors.
- Initial weld joint solver path with C parity for a dynamic linear/angular
  weld simulation, including body transforms, velocities, constraint force,
  and constraint torque.
- Revolute joint API with C parity for local anchors, reference angle clamping,
  target-angle clamping at creation, current angle unwinding, spring controls,
  limit controls, motor controls, and zeroed pre-step constraint force/torque
  access.
- Initial revolute joint solver path with C parity for isolated,
  single-body ground-contact, mini FallingHinges-style free-fall, and
  mini FallingHinges-style ground-contact dynamic simulations, including warm
  starting, angular spring, motor torque clamping and reporting, angular limits,
  point-to-point constraint impulses, rounded-box geometry, multi-body
  joint/contact coupling, and force/torque access scaled by the world sub-step
  inverse time.
- Body proxy synchronization now mirrors upstream `body.c`: speculative shape
  AABBs update every step, while fat AABBs and broad-phase move buffering only
  update when the speculative AABB escapes the registered fat proxy.
- Small FallingHinges-style determinism slices with C parity for four, eight,
  sixteen, twenty-four, thirty-two, forty-eight, sixty-four, and 120 dynamic
  rounded boxes, two, four, eight, twelve, sixteen, twenty-four, thirty-two,
  and sixty revolute hinge pairs, shared ground contact, 90 simulation steps,
  body transforms/velocities, contact count, and move-event count. The
  determinism helper's four-column 120-body layout also has exact C parity
  with sleeping disabled through 165 simulation steps. The 120-body slices are
  covered by sparse contact ids, persistent constraint-graph color/local
  ordering, dynamic-vs-static continuous collision for fast non-bullet bodies,
  upstream-style fast-body AABB/proxy synchronization including deferred
  broad-phase proxy enlargement during solver finalization, staged broad-phase
  pair creation in upstream move-result order, upstream-style dynamic/kinematic
  broad-phase tree rebuilds after contact update, and upstream timestep-based
  angular velocity capping.
- Continuous collision finalization now mirrors the upstream two-stage fast
  body flow: non-bullets sweep against static geometry during finalization,
  while bullet bodies are deferred until all target transforms are final and
  then sweep against static, kinematic, and dynamic non-bullet shapes. This
  provides exact bullet-versus-dynamic TOI clipping for the Pinball sample.
- Prismatic joint API with C parity for local-axis normalization, local anchors,
  reference angle, current translation and speed, spring controls, limit
  controls, motor controls, and force/torque accessors.
- Initial prismatic joint solver path with C parity for a dynamic spring/limit/
  motor simulation, including body transforms, velocities, translation/speed,
  motor force, constraint force, and constraint torque.
- Wheel joint API with C parity for local-axis normalization, local anchors,
  spring controls, limit controls, motor controls, and force/torque accessors.
- Initial wheel joint solver path with C parity for a dynamic spring/limit/
  motor simulation, including body transforms, velocities, linear separation,
  motor torque, constraint force, and constraint torque.
- Initial contact update/data/event surface with C parity for HelloWorld contact
  capacity, contact-data count, begin/end event totals, and manifold point
  count. Contact state changes are staged and applied in contact-id order,
  matching upstream's bitset-driven transition pass rather than mutating the
  constraint graph during narrow-phase iteration.
- Initial scalar contact solver path for world stepping, including contact
  constraint preparation, warm starting, solve/relax, restitution, impulse
  storage, body sleeping, body move events, and graph/SIMD-equivalent impulse
  arithmetic ordering for bit-exact HelloWorld parity.
- Sleep handling now tracks persistent contact/joint sleep islands and delayed
  island splitting, matching the upstream FallingHinges determinism helper
  sleep step and final transform hash. Contacts that start touching now wake the
  contacted sleep island before graph insertion. When an island sleeps, its
  touching contacts and joints leave the awake constraint graph in upstream
  island-list order and return in sleeping-set order when the island wakes.
  Touching and non-touching contacts whose dynamic endpoints are all asleep are
  left out of narrow-phase recomputation, mirroring upstream sleeping and
  disabled solver sets while preserving warm-start manifolds and broad-phase
  contact capacity until an island wakes.
- Initial body mass/center-of-mass integration with C parity for shape-derived
  mass data, local/world point and vector transforms, linear velocity at points,
  and angular velocity.
- Body accessor/property API with C parity for type/name/user data, transform
  mutation, velocity setters, point velocities, damping/gravity controls,
  enabled state, fixed rotation, bullet flag, body-wide contact/hit event
  toggles, owner world lookup, shape enumeration, body AABB computation, and
  shape validity checks.
- Additional non-debug public API coverage with C parity for body type changes,
  target-transform velocity setup, shape destruction, sensor-overlap retrieval,
  world profile/rebuild/speculative/memory-stat entry points, restitution and
  hit-event threshold clamping, friction/restitution callback storage, world
  lifecycle/id validity/recycling behavior, and common joint
  separation/constraint-tuning accessors.
- World contact tuning setter mirrors upstream non-negative float clamping for
  contact hertz, damping ratio, and max push speed.
- Body type changes now clear velocity/force state when entering static mode,
  matching the upstream observable state after static-to-dynamic toggles.
- Body force/impulse API with C parity for awake dynamic bodies, including
  force/torque accumulation, linear/angular impulse application, integration
  before damping, maximum linear speed clamping, and force/torque reset after
  stepping.
- Initial body sleep/awake API with C parity for awake state, sleep enablement,
  sleep-disabled body creation as awake, started-touching contact wake guards
  for static and sleep-disabled sources, and sleep threshold controls.
- Initial dynamic-body kinematics in `b2World_Step` with C parity for free-body
  sub-step velocity integration, damping, angular integration, center/transform
  finalization, and maximum speed clamps.
- Kinematic body target-transform stepping mirrors upstream awake-set
  integration, including zero-gravity/inv-mass handling and awake body counts
  that include enabled kinematic bodies.
- Minimal world/body/shape API sufficient for the ported world API tests.
- Headless sample entry points under `src/samples/java`, including the
  HelloWorld physics sample wired to the same 90-step scenario used by the
  upstream C parity probe and the Determinism/Falling Hinges sample wired to
  the upstream shared determinism helper, plus the Stacking/Single Box sample,
  Stacking/Tilted Stack sample, Stacking/Vertical Stack sample,
  Stacking/Double Domino sample,
  Stacking/Circle Stack sample,
  Stacking/Capsule Stack sample, Stacking/Cliff sample, Stacking/Arch sample,
  Stacking/Confined sample, Stacking/Card House sample, and
  Shapes/Friction sample, Shapes/Restitution sample, Shapes/Rolling Resistance
  sample, Shapes/Conveyor Belt sample, Shapes/Tangent Speed sample,
  Shapes/Filter sample, Shapes/Custom Filter sample, Shapes/Modify Geometry sample,
  Shapes/Compound Shapes sample, Shapes/Chain Shape sample,
  Shapes/Chain Link sample, Shapes/Rounded sample,
  Shapes/Ellipse sample, Shapes/Explosion sample, Collision/Shape Distance sample,
  Bodies/Pivot sample, Bodies/Weeble sample, Bodies/Body Type sample,
  Bodies/Sleep sample, Bodies/Bad sample, Bodies/Kinematic sample,
  Joints/Filter Joint sample, Joints/Revolute sample, Joints/Motor Joint
  sample, Joints/Distance Joint sample, Joints/Prismatic sample,
  Joints/Wheel sample, Joints/Bridge sample, Joints/Ball & Chain sample,
  Joints/Cantilever sample, Joints/Fixed Rotation sample, Joints/Breakable
  sample, Joints/Separation sample, and Joints/User Constraint sample wired
  to their upstream sample setups, plus Joints/Driving sample wired to its
  upstream sample setup, and Joints/Ragdoll sample wired to its upstream
  `shared/human.c` setup, and Joints/Soft Body sample wired to its upstream
  donut setup, Joints/Doohickey sample wired to its upstream helper setup,
  Joints/Scissor Lift sample wired to its upstream setup, Joints/Gear Lift
  sample wired to its upstream setup, Joints/Door sample wired to its upstream
  setup with a deterministic impulse-button action, Joints/Scale Ragdoll
  sample wired to its upstream setup with a deterministic scale-slider action,
  Events/Sensor Bookend sample wired to its upstream setup, and Events/Foot
  Sensor sample wired to its upstream setup, and Events/Sensor Funnel sample
  wired to its upstream human-funnel setup, Events/Contact sample wired to its
  upstream debris-attachment setup, and Events/Sensor Types sample wired to its
  upstream setup, and Events/Platformer sample wired to its upstream setup, and
  Events/Body Move sample wired to its upstream setup, and Character/Mover
  sample wired to its upstream terrain, bridge, pogo cast, collision-plane,
  elevator, and soft-mover setup, plus Collision/Ray Cast wired to its upstream
  circle, capsule, box, triangle, segment, and closest-hit clipping sequence,
  Collision/Time of Impact wired to its upstream polygon/capsule sweep, and
  Collision/Shape Cast wired to its upstream box/point sweep and terminal GJK
  distance check, plus Collision/Smooth Manifold wired to its upstream 36-edge
  ghost-vertex chain with default and deterministic box/circle interaction
  states, and Collision/Manifold wired to all 20 upstream primitive/polygon/
  chain collision calls across cold and warm simplex-cache passes, plus
  Collision/Overlap World wired to the seeded ten-polygon setup, ignored-shape
  callback, all query-shape modes, and deferred destruction path, and
  Collision/Cast World wired to all ray/shape cast modes, sorted and unsorted
  callbacks, simple closest query, ignored-shape path, and tree statistics,
  plus Collision/Dynamic Tree wired to the upstream 100x100 debug grid,
  incremental/full/partial update modes, deterministic movement, query/ray
  drag paths, raw proxy-state hashing, and BVH memory/statistics reporting,
  plus all 15 active Continuous samples: Bounce House, Bounce Humans, Chain
  Drop, Chain Slide, Segment Slide, Skinny Box, Ghost Bumps, Speculative
  Fallback, Speculative Sliver, Speculative Ghost, Pixel Imperfect,
  Restitution Threshold, Drop, Pinball, and Wedge, plus all six active
  Robustness samples: High Mass Ratio 1, High Mass Ratio 2, High Mass Ratio 3,
  Overlap Recovery, Tiny Pyramid, and Cart, all 16 active Benchmark samples:
  Barrel, Tumbler, Many Tumblers, Large Pyramid, Many Pyramids, CreateDestroy,
  Sleep, Joint Grid, Smash, Compound, Kinematic, Cast, Spinner, Rain, Shape
  Distance, and Sensor, plus World/Large World.
- Interactive jMonkeyEngine 3.9 sample debugger under `src/debugger/java`,
  with a complete 111-entry catalog, play/pause and single-step execution,
  reset, speed control, pan/zoom/fit navigation, live world counters, and
  batched debug rendering for shapes, joints, contacts, AABBs, graph colors,
  transforms, points, circles, capsules, polygons, and segments. Algorithmic
  samples without a persistent world render dedicated result snapshots for
  Convex Hull, Shape Distance, Ray Cast, Time of Impact, Shape Cast, Smooth
  Manifold, Manifold, and Dynamic Tree.
- The graphical session retains the final sample world and continues stepping
  it indefinitely after the finite headless parity horizon. `SampleRuntime`
  bindings are registered thread-locally and applied on the simulation thread;
  the debugger currently exposes Pinball's upstream hold-to-flip `Space`
  control, a motor-speed stepper for Benchmark/Spinner, and Gear Lift's Motor,
  Max Torque, and Speed controls with upstream `A`/`D` key holds. Headless
  `run()` behavior and output remain unchanged.
- C parity probes that compile the vendored Box2D C sources and compare
  allocator behavior, selected math outputs, id packing/unpacking, bitset and
  hash-set behavior, shape unit helpers, AABB collision helpers, distance
  algorithms, selected manifold generation, body mass helpers,
  shape material controls, shape filter/event controls, capsule shape behavior,
  shape query behavior, shape geometry mutation controls, shape accessor and
  density controls, body accessor/property controls, body sleep controls,
  force/impulse integration, world query/cast traversal, chain shape behavior,
  common joint behavior, distance joint API and solver behavior, motor joint
  API and solver behavior, mouse joint API and solver behavior, weld joint
  API and solver behavior, revolute joint behavior, revolute joint solver
  behavior, small
  FallingHinges behavior,
  FallingHinges determinism sleep-step behavior,
  prismatic joint API and solver behavior, wheel joint API and solver behavior,
  free-body kinematics, explosion impulses, custom filter and pre-solve callbacks,
  contact hit-event generation, sensor begin/end event generation,
  remaining non-debug public API behavior, world lifecycle/id recycling behavior,
  mover cast/collision behavior,
  mover plane solving/clipping behavior, debug draw callback behavior,
  debug contact draw callback behavior,
  broad-phase contact creation counts, contact data/event counts, contact
  solver settling/sleeping, per-step
  HelloWorld traces, a simulated
  HelloWorld result against Java, the Java HelloWorld sample output, and the
  Java FallingHinges, SingleBox, TiltedStack, VerticalStack, DoubleDomino,
  KinematicBody, CircleStack, CapsuleStack, Cliff, Arch, Confined, CardHouse, and Friction, Restitution, RollingResistance,
  ConveyorBelt,
  TangentSpeed, ShapeFilter, CustomFilter, SensorFunnel, SensorBookend, FootSensor, ContactEvent, SensorTypes, Platformer, BodyMove, BodyType, FilterJoint,
  RevoluteJoint, MotorJoint, DistanceJoint, PrismaticJoint, WheelJoint, Bridge,
  BallAndChain, Cantilever, FixedRotation, BreakableJoint, JointSeparation, UserConstraint, Driving, Ragdoll, SoftBody, Doohickey, ScissorLift, GearLift, Door, ScaleRagdoll, ModifyGeometry, Pivot, Weeble, Sleep, BadBody,
  CompoundShapes, ChainShape, ChainLink, RoundedShapes, EllipseShape,
  ConvexHull, OffsetShapes, Explosion, ShapeDistance, Mover, RayCast,
  TimeOfImpact, ShapeCast, SmoothManifold, Manifold, OverlapWorld, CastWorld,
  DynamicTree, all 15 active Continuous sample outputs, and all six active
  Robustness sample outputs. The grouped Benchmark probe additionally compares
  exact body-state hashes, all 12 graph colors, Cast traversal/result hashes,
  Shape Distance output hashes, and Sensor event hashes for all 16 Benchmark
  creators. A dedicated Large World probe compares its complete debug setup and
  four simulation steps with two explosions.
- Raw-bit C/Java DoubleDomino trace probes under `tools/parity` capture body
  states and contact manifold/impulse data around steps 53-55 for solver
  ordering diagnostics.

## Verification

- `./gradlew test --rerun-tasks` currently runs 154 tests in 138 suites; all
  154 pass with zero skips, failures, or errors (6m33s on the audit machine).
- `./gradlew runDebugger` has been visually verified on macOS through the
  compositor with HelloWorld, Joints/Ragdoll, and Benchmark/Spinner; the
  rendered scene, controls, catalog, and live counters remain visible under
  both minimal and dense workloads. Infinite-session verification reached
  step 1,191 in Pinball and step 985 in Spinner; Pinball's `Space` press/release
  changed both the UI binding state and flipper pose. Gear Lift reached step
  3,715 while a held `D` advanced Speed from `0.0` to `0.3` and rotated the
  lift gears. A four-worker HelloWorld session was also rendered through the
  debugger-owned executor bridge and displayed `Workers 4` in the status bar.
- The headless suite contains 111 entry points: all 110 active upstream
  `RegisterSample` creators plus the standalone HelloWorld tutorial. The
  disabled experimental `Mover2` block under `#if 0` is excluded from this
  inventory.
- `PublicApiSurfaceTest` derives the public inventory from the vendored headers
  and finds all 422 `B2_API` function names represented in Java, including
  `b2InternalAssertFcn`. Allocator callback typedefs are represented by
  `B2Allocator` and the `IntFunction<ByteBuffer>` adapter.
- `MultithreadingTest` compiles a C++17 thread-pool probe against the upstream
  C17 Box2D sources and matches the official Falling Hinges sleep step (`288`)
  and transform hash (`0x35467e1e`) in both C++ and Java using 1, 2, 4, and 8
  workers. It also passes with bit-identical transform and velocity state
  for the 120-body Falling Hinges workload after 165 steps using 1, 2, 4, and
  8 workers. It also proves that task ranges reach multiple executor threads,
  overlap in time, preserve exclusive worker indices, and fall back to direct
  execution when both C-style callbacks are not supplied.
- `BenchmarkCoreSampleTest` passes exact C parity for all 16 Benchmark samples,
  including create/destroy id reuse, scalar-overflow versus SIMD contact solver
  arithmetic, static-body awake semantics, static-static joint graph behavior,
  query workloads, and sensor events.
- `LargeWorldSampleTest` passes exact C parity for all 513 bodies, 5,303 shapes,
  257 joints, graph counters, raw body-state hash, and four steps containing two
  explosions.
- `AllocatorParityTest` passes for custom `B2Allocator` behavior,
  `IntFunction<ByteBuffer>` adapter behavior, 32-byte allocation rounding,
  alignment forwarding, zero-size allocation, and byte-count accounting against
  upstream C.
- `MathParityTest` passes for selected `b2Atan2`, `b2MakeRot`,
  `b2UnwindAngle`, transform composition/inversion,
  `b2ComputeRotationBetweenUnitVectors`, and `b2NLerp` outputs against
  upstream C.
- `IdCollisionParityTest` passes for upstream id load/store round trips and
  AABB validity, overlap, containment, and ray-cast output against upstream C.
- `BitSetTableParityTest` passes for upstream Fibonacci bit-set behavior,
  bit-set bytes, hash-set growth/removal/lookup counts, and C-compatible
  hash-set byte accounting.
- `ShapeUnitParityTest` passes for upstream shape mass, capsule mass bounds,
  shape AABB, point-in-shape, and ray-cast unit slices against upstream C.
- `SingleBoxSampleTest` passes for the upstream Stacking/Single Box sample's
  120-step body transform, velocity, contact count, and awake count against
  upstream C.
- `TiltedStackSampleTest` passes for the upstream Stacking/Tilted Stack
  sample's final contact and awake counts plus representative box transforms
  and velocities after 180 steps against upstream C.
- `VerticalStackSampleTest` passes for the upstream Stacking/Vertical Stack
  sample's default one-column rounded-box setup, final body/shape/contact/awake
  counts, and all twelve box transforms and velocities after 240 steps against
  upstream C.
- `DoubleDominoSampleTest` passes for the upstream Stacking/Double Domino
  sample's impulse-driven domino setup, final body/shape/contact/awake counts,
  and all fifteen domino transforms and velocities after both 54 and 240 steps
  against upstream C.
- `KinematicBodySampleTest` passes for the upstream Bodies/Kinematic sample's
  target-transform stepping, final transform, velocity, elapsed time, and awake
  count against upstream C.
- `PivotSampleTest` passes for the upstream Bodies/Pivot sample's body
  transform, velocity, pivot-point velocity, local/world point velocity, contact
  count, and awake count after 120 steps against upstream C.
- `WeebleSampleTest` passes for the upstream Bodies/Weeble sample's custom
  friction/restitution callbacks, shifted mass data, body transform, local/world
  point velocities, contact count, and awake count after 240 steps against
  upstream C.
- `BodyTypeSampleTest` passes for the upstream Bodies/Body Type sample setup
  plus deterministic GUI-style type and enable toggles, including body
  type/enabled/awake state, proxy/contact counters, joint count, and all tracked
  body transforms and velocities against upstream C. Advancing the
  kinematic/joint simulation beyond the scripted toggles currently enters joint
  solver drift and remains solver follow-up work.
- `FilterJointSampleTest` passes for the upstream Joints/Filter Joint sample's
  static ground, two dynamic square bodies, non-colliding filter joint, final
  world counters, joint type/collide flag, contact capacities, and body
  transforms and velocities after 120 steps against upstream C.
- `RevoluteJointSampleTest` passes for the upstream Joints/Revolute sample's
  default capsule, ball, and lever setup, including final world counters, all
  dynamic body transforms and velocities, contact capacities, revolute angles,
  motor torque, and constraint force/torque after 120 steps against upstream C.
- `MotorJointSampleTest` passes for the upstream Joints/Motor Joint sample's
  animated motor target, final world counters, target offsets, constraint
  force/torque, body transform, velocity, and contact capacity after 110 steps
  against upstream C. Step 111 currently diverges by a few ulps through the
  sinusoidal target and accumulated motor impulse, so longer horizons remain
  solver/trig follow-up work.
- `DistanceJointSampleTest` passes for the upstream Joints/Distance Joint
  sample's default one-body distance chain, including final world counters, body
  transform and velocity, contact capacity, current joint length, motor force,
  and constraint force/torque after 120 steps against upstream C.
- `PrismaticJointSampleTest` passes for the upstream Joints/Prismatic sample's
  default diagonal-axis prismatic joint, including final world counters, body
  transform and velocity, contact capacity, joint translation/speed, motor force,
  and constraint force/torque after 120 steps against upstream C.
- `WheelJointSampleTest` passes for the upstream Joints/Wheel sample's default
  capsule suspension setup, including final world counters, body transform and
  velocity, contact capacity, linear separation, motor torque, and constraint
  force/torque after 120 steps against upstream C.
- `BridgeSampleTest` passes for the upstream Joints/Bridge sample's 160-plank
  spring/motor revolute bridge plus triangle and circle payloads, including
  final world counters, all 165 dynamic body transforms and velocities, contact
  capacities, and all 161 revolute joint angles, motor torques, and constraint
  force/torque values after 120 steps against upstream C. The assertion is
  field-by-field numeric parity because Java and C may choose different
  shortest decimal spellings for the same float with `%.9g`-style formatting.
- `BallAndChainSampleTest` passes for the upstream Joints/Ball & Chain sample's
  30-capsule filtered chain plus heavy ball setup, including final world
  counters, all 31 dynamic body transforms and velocities, contact capacities,
  and all 31 revolute joint angles, motor torques, and constraint force/torque
  values after 120 steps against upstream C.
- `CantileverSampleTest` passes for the upstream Joints/Cantilever sample's
  eight sleeping capsule bodies joined by weld joints, including final world
  counters, all body transforms and velocities, contact capacities, weld
  hertz/damping settings, and constraint force/torque values after 120 steps
  against upstream C.
- `FixedRotationSampleTest` passes for the upstream Joints/Fixed Rotation
  sample's six fixed-rotation dynamic boxes using distance, motor, prismatic,
  revolute, weld, and wheel joints, including final world counters, all body
  transforms and velocities, fixed-rotation flags, joint types, type-specific
  joint metrics, linear separation, and constraint force/torque values after
  120 steps against upstream C.
- `BreakableJointSampleTest` passes for the upstream Joints/Breakable sample's
  six non-sleeping dynamic boxes using distance, motor, prismatic, revolute,
  weld, and wheel joints, including break-force joint destruction decisions,
  final world counters, all body transforms and velocities, remaining joint
  types, type-specific joint metrics, linear separation, and constraint
  force/torque values after 120 steps against upstream C.
- `JointSeparationSampleTest` passes for the upstream Joints/Separation
  sample's five non-sleeping dynamic boxes using distance, prismatic, revolute,
  weld, and wheel joints, including final world counters, all body transforms
  and velocities, joint types, type-specific joint metrics, linear and angular
  separation, and constraint force/torque values after 120 steps against
  upstream C.
- `UserConstraintSampleTest` passes for the upstream Joints/User Constraint
  sample's custom post-step two-anchor constraint on a damped dynamic box,
  including final world counters, body transform and velocities, mass and
  rotational inertia, world center of mass, accumulated impulses, and reported
  forces after 120 steps against upstream C.
- `DrivingSampleTest` passes for the upstream Joints/Driving sample's default
  terrain chain, teeter, bridge, box stack, and two-wheel car setup, including
  final world counters, representative body transforms and velocities for the
  car, bridge, teeter, and boxes, wheel motor/spring settings, motor torque,
  linear separation, and constraint torque after 120 steps against upstream C.
  Wheel axle force-vector diagnostics are compared within `2e-5f`; all other
  tracked values are exact float parity.
- `RagdollSampleTest` passes for the upstream Joints/Ragdoll sample's default
  `CreateHuman` setup, including final world counters, all eleven bone
  transforms and velocities, per-bone shape/contact capacities, and all ten
  revolute joint angles, motor/spring settings, constraint force, and
  constraint torque values after 120 steps against upstream C compiled with
  `shared/human.c`.
- `SoftBodySampleTest` passes for the upstream Joints/Soft Body sample's
  seven-body donut setup, including final world counters, all capsule body
  transforms and velocities, contact capacities, weld joint linear/angular
  separation, hertz settings, constraint force, and constraint torque values
  after 120 steps against upstream C.
- `DoohickeySampleTest` passes for the upstream Joints/Doohickey sample's four
  helper instances, including final world counters, all sixteen wheel/bar body
  transforms and velocities, contact capacities, all revolute/prismatic joint
  types, type-specific joint metrics, linear separation, constraint force, and
  constraint torque values after 120 steps against upstream C.
- `ScissorLiftSampleTest` passes for the upstream Joints/Scissor Lift sample's
  three-stage lift plus car payload using 8 sub-steps, including final world
  counters, all tracked link/platform/car body transforms and velocities,
  contact capacities, all tracked revolute/wheel/distance joint types,
  type-specific joint metrics, linear separation, and constraint torque values
  after 120 steps against upstream C. Joint force-vector diagnostics are
  compared within `5e-4f`; all other tracked values are exact float parity.
- `GearLiftSampleTest` passes for the upstream Joints/Gear Lift sample's
  parsed chain terrain, two toothed gears, forty-link chain, sliding door, and
  200 random polygon payload bodies, including world counters, all 243 dynamic
  body states, contact capacities, all 44 revolute/prismatic joint types,
  type-specific joint metrics, linear separation, and constraint force/torque
  diagnostics after 10 steps against upstream C. Body floats are compared
  within `1e-6f`, joint scalar metrics within `5e-6f`, and joint force/torque
  diagnostics within `2e-5f`; counts and capacities are exact.
- `DoorSampleTest` passes for the upstream Joints/Door sample's top-down
  revolute door setup plus deterministic impulse-button action, including
  final world counters, door transform and velocities, contact capacity,
  maximum translation error, revolute spring/limit/constraint tuning settings,
  angle/separation metrics, and constraint force/torque values after 120 steps
  against upstream C.
- `ScaleRagdollSampleTest` passes for the upstream Joints/Scale Ragdoll sample's
  ground box, `CreateHuman` setup, deterministic random torso angular impulse,
  and scripted `Human_SetScale(1.75f)` slider action, including world counters,
  all eleven bone transforms, velocities, shape/contact capacities, scaled
  masses/inertia, and all ten revolute joint settings/separation/force
  diagnostics after 60 pre-contact steps against upstream C compiled with
  `shared/human.c`.
- `SleepSampleTest` passes for the upstream Bodies/Sleep sample setup plus
  deterministic invoker create/destroy actions, including sleep-disabled body
  creation, sensor touch event accumulation, body awake/sleep flags, contact
  capacities, counters, and all tracked body states after 20 steps against
  upstream C. Longer horizons currently enter box contact solver drift and
  remain solver follow-up work.
- `BadBodySampleTest` passes for the upstream Bodies/Bad sample's zero-density
  dynamic body behavior, including force application, transforms, velocities,
  mass data, contact count, and awake count after 120 steps against upstream C.
- `CircleStackSampleTest` passes for the upstream Stacking/Circle Stack
  sample's accumulated hit events, shape user-data pair checksum, final contact
  and awake counts, and first/last circle transforms and velocities against
  upstream C.
- `CapsuleStackSampleTest` passes for the upstream Stacking/Capsule Stack
  sample's final contact and awake counts plus bottom/middle/top capsule
  transforms and velocities after 180 steps against upstream C.
- `CliffSampleTest` passes for the upstream Stacking/Cliff sample's multi-shape
  static cliff/platform ground plus scripted Flip action, including final world
  counters and all nine dynamic capsule/box/circle body transforms, velocities,
  shape counts, and contact capacities after 120 steps against upstream C.
- `ArchSampleTest` passes for the upstream Stacking/Arch sample's ground
  segment, 17 arch blocks, and four top boxes, including final world counters
  and all 21 dynamic body transforms, velocities, shape counts, and contact
  capacities after 120 steps against upstream C.
- `ConfinedSampleTest` passes for the upstream Stacking/Confined sample's
  four-capsule boundary and 25-by-25 zero-gravity circle grid, including final
  world counters and nine distributed dynamic body transforms, velocities,
  shape counts, and contact capacities after 120 steps against upstream C.
- `CardHouseSampleTest` passes for the upstream Stacking/Card House sample's
  five-level card construction, including final world counters and all 40
  dynamic body transforms, velocities, shape counts, and contact capacities
  against upstream C. The setup and first two steps are bit-exact; the
  default 120-step final state is within `2e-6f` after tiny solver drift.
- `FrictionSampleTest` passes for the upstream Shapes/Friction sample's final
  contact and awake counts plus all five dynamic box transforms and velocities
  after 240 steps against upstream C.
- `RestitutionSampleTest` passes for the upstream Shapes/Restitution sample's
  circle variant, including final contact and awake counts plus five
  representative circle transforms and velocities after 240 steps against
  upstream C.
- `RollingResistanceSampleTest` passes for the upstream Shapes/Rolling
  Resistance sample's default flat-lift scene, including final contact and
  awake counts plus representative circle transforms, linear velocities, and
  angular velocities after 240 steps against upstream C.
- `ConveyorBeltSampleTest` passes for the upstream Shapes/Conveyor Belt sample,
  including final contact and awake counts plus all five dynamic box
  transforms and velocities after 240 steps against upstream C.
- `TangentSpeedSampleTest` passes for the upstream Shapes/Tangent Speed sample's
  SVG chain parsing, loop-chain setup, world counters, drop cadence, and all ten
  dropped body trajectories against upstream C.
- `ShapeFilterSampleTest` passes for the upstream Shapes/Filter sample setup
  plus deterministic filter-mask toggles corresponding to the sample UI,
  including final filters, contact capacities, world counters, and all three
  body transforms and velocities against upstream C.
- `CustomFilterSampleTest` passes for the upstream Shapes/Custom Filter sample
  setup, including shape user-data, exact custom-filter callback counts,
  broad-phase existing-contact filtering order, world counters, and all ten body
  transforms and velocities after 240 steps against upstream C.
- `SensorBookendSampleTest` passes for the upstream Events/Sensor Bookend
  sample setup, including cumulative begin/end sensor events, final visiting
  flags, sensor overlap counts/capacities, shape validity, and dynamic sensor
  plus visitor transforms, velocities, shape counts, and contact capacities
  after 120 steps against upstream C.
- `FootSensorSampleTest` passes for the upstream Events/Foot Sensor sample's
  chain ground filters and dynamic player capsule plus foot sensor, including
  cumulative begin/end events, tracked overlap count, sensor capacity/overlap
  count, final world counters, and player transform/velocity/shape/contact
  state after 120 steps against upstream C.
- `ContactEventSampleTest` passes for the upstream Events/Contact sample's
  looped wall chain, zero-gravity bullet player, deterministic debris spawning,
  contact-event processing, debris attachment to the player, non-core player
  shape destruction path, final world counters, event/contact-data checksums,
  and player/debris transforms and velocities after 120 steps against upstream
  C.
- `SensorTypesSampleTest` passes for the upstream Events/Sensor Types sample's
  static, kinematic, and dynamic sensors, filtered ground segments, falling
  ball, and ray cast, including cumulative sensor event counts, final
  per-sensor overlap capacities/checksums, body transforms/velocities, contact
  capacities, and ray result after 120 steps against upstream C.
- `SensorFunnelSampleTest` passes for the upstream Events/Sensor Funnel sample's
  looped chain funnel, three motorized paddles, human spawning cadence, sensor
  event setup, final world counters, active-spawn mask, and all active human
  bone transforms/velocities/shape/contact states after 88 bit-exact steps
  against upstream C. Longer runs enter multi-ragdoll contact drift starting at
  step 89; counters remain aligned through 240 pre-destruction steps, while the
  later sensor-destruction phase diverges and needs separate solver-order
  investigation.
- `PlatformerSampleTest` passes for the upstream Events/Platformer sample's
  static ground, one-sided static and kinematic platforms, dynamic capsule
  player, pre-solve callback decisions, jump gating, final contact summaries,
  world counters, and player/platform transforms and velocities after 120 steps
  against upstream C.
- `BodyMoveSampleTest` passes for the upstream Events/Body Move sample's
  five-shape boundary, 50 spawned dynamic bodies with deterministic random
  polygons, body move events, sleep tracking, final world counters, and all
  tracked dynamic body transforms/velocities/shape/contact/sleep states after
  108 bit-exact steps against upstream C. The longer 120-step horizon enters
  body-state solver drift while preserving event and counter parity.
- `MoverSampleTest` passes for the upstream Character/Mover sample's two SVG
  terrain chains, 50-link bridge, soft mover, falling ball, kinematic elevator,
  pogo shape cast, collision-plane solve/cast loop, final world counters, and
  representative body states after 120 steps against upstream C with zero
  tolerance.
- `RayCastSampleTest` passes for the upstream Collision/Ray Cast sample's
  circle, capsule, box, triangle, and segment sequence, including progressive
  closest-fraction clipping and the final world-space hit, against upstream C
  with zero tolerance.
- `TimeOfImpactSampleTest` passes for the upstream Collision/Time of Impact
  sample's polygon/capsule sweeps, TOI state/fraction, and capsule endpoints at
  the start, reported fraction, and end of the sweep against upstream C with
  zero tolerance.
- `ShapeCastSampleTest` passes for the upstream Collision/Shape Cast sample's
  default box/point-radius sweep, cast output, terminal transform, GJK distance
  output, iteration/simplex counts, and simplex cache count against upstream C
  with zero tolerance.
- `SmoothManifoldSampleTest` passes for all 36 ghost-vertex chain segments in
  the upstream Collision/Smooth Manifold sample across its default state plus
  deterministic rounded-box and circle interactions, including every contact
  normal, point, anchor, separation, and feature id with zero tolerance.
- `ManifoldSampleTest` passes for all 20 primitive, polygon, segment, and chain
  collision calls in the upstream Collision/Manifold sample across cold and
  warm cache passes, including all four simplex caches with zero tolerance.
- `OverlapWorldSampleTest` passes for the seeded upstream Collision/Overlap
  World setup across default, circle, capsule, and box query states, including
  ignored-shape filtering, traversal statistics, hit order, and deferred body
  destruction with zero tolerance.
- `CastWorldSampleTest` passes for the upstream Collision/Cast World setup
  across all 16 ray/shape-cast mode combinations, simple closest ray cast, and
  ignored-shape ray path, including hit order, points, normals, fractions,
  user-data indices, and tree statistics with zero tolerance.
- `DynamicTreeSampleTest` passes for the upstream Collision/Dynamic Tree
  100x100 debug grid across incremental, full-rebuild, and partial-rebuild
  modes, including deterministic movement counts, full raw proxy-state hashes,
  BVH height/area/byte accounting, query/ray visits and hit sets, and
  representative proxy states with zero tolerance.
- `ContinuousCasesSampleTest` passes for all 15 active upstream Continuous
  samples across 31 checkpoints through frame 120, including transient
  contacts, transforms, velocities, awake/island/world counters, chain and
  segment CCD, speculative contacts, high-speed drops, moving gravity, and
  Pinball bullet-versus-dynamic TOI clipping with zero tolerance.
- `RobustnessCasesSampleTest` passes for all six active upstream Robustness
  samples across 12 checkpoints through frame 120, including raw-bit hashes of
  every dynamic body state, representative transforms/velocities/contact data,
  awake/island/world counters, all 12 constraint-graph colors, high-mass-ratio
  sleep/wake transitions, overlap recovery, Tiny Pyramid sleeping contacts,
  and Cart joint/contact coupling with zero tolerance.
- `ModifyGeometrySampleTest` passes for the upstream Shapes/Modify Geometry
  sample setup plus deterministic geometry/type changes, including shape/body
  types, body mass data, AABBs, contact counts, and awake counts against
  upstream C.
- `RecreateStaticSampleTest` passes for the upstream Shapes/Recreate Static
  sample's per-step static body destruction/recreation with immediate contact
  creation, final dynamic transform/velocity, counters, contact data count, and
  awake count against upstream C.
- `CompoundShapesSampleTest` passes for the upstream Shapes/Compound Shapes
  sample setup plus deterministic Intrude action, including compound body mass
  data, body AABBs, final counters, and all eight relevant body transforms and
  velocities after 10 steps against upstream C. Longer horizons currently
  diverge after early contact solving and remain solver follow-up work.
- `ChainShapeSampleTest` passes for the upstream Shapes/Chain Shape sample's
  loop-chain scene, material setup, and circle/capsule/box launch variants,
  including final counters and body transforms and velocities after 240 steps
  against upstream C.
- `ChainLinkSampleTest` passes for the upstream Shapes/Chain Link sample's
  linked open-chain ground setup, circle/capsule/polygon dynamic bodies, final
  counters, and all three body transforms and velocities after 240 steps
  against upstream C.
- `RoundedShapesSampleTest` passes for the upstream Shapes/Rounded sample's
  random rounded polygon stack, generated shape descriptors, final counters,
  and representative body transforms and velocities after 240 steps against
  upstream C.
- `EllipseShapeSampleTest` passes for the upstream Shapes/Ellipse sample's
  rounded diamond hull stack, final counters, and representative body
  transforms and velocities after 240 steps against upstream C.
- `ExplosionSampleTest` passes for the upstream Shapes/Explosion sample's
  welded ring setup, deterministic explode action, rotating reference angles,
  final counters, and all twelve body transforms and velocities after 120 steps
  against upstream C.
- `ConvexHullSampleTest` passes for the upstream Geometry/Convex Hull sample's
  deterministic XorShift32 point generation, rotated/clamped input points,
  hull count, hull vertices, and validation result against upstream C.
- `OffsetShapesSampleTest` passes for the upstream Shapes/Offset sample's
  offset/rotated polygon and capsule setup, including body transforms, shape
  counts/types, body and shape mass data, AABBs, and world counters against
  upstream C.
- `ShapeDistanceSampleTest` passes for the upstream Collision/Shape Distance
  sample's default and GUI-style mixed proxy cases, including proxy counts,
  simplex cache indices, distance output, iteration count, and debug
  simplex-count behavior against upstream C.
- `DistanceParityTest` passes with exact C/Java parity for
  `b2SegmentDistance`, `b2ShapeDistance`, `b2ShapeCast`, and
  `b2TimeOfImpact`.
- `ManifoldParityTest` passes with exact C/Java parity for selected
  single-point and two-point manifold generation, including chain segment
  contacts.
- `DynamicTreeParityTest` passes for observable broad-phase query behavior,
  traversal visit statistics, and rebuild metrics.
- `BroadPhaseParityTest` passes for the broad-phase proxy and move-buffer layer.
- `WorldQueryParityTest` passes for world overlap AABB/shape queries, ray casts,
  closest ray casts, shape casts, query filtering, callback hit data, clipping,
  and traversal stats.
- `MoverParityTest` passes for capsule mover shape-cast fractions and collision
  plane callbacks against upstream C.
- `PlaneSolverParityTest` passes for mover plane solving, accumulated push, and
  velocity clipping against upstream C.
- `ExplosionParityTest` passes for world explosion impulses across circle,
  capsule, polygon, and mask-filtered dynamic bodies against upstream C.
- `CallbackParityTest` passes for custom contact filtering and pre-solve
  callback contact disabling against upstream C.
- `HitEventParityTest` passes for post-solver contact hit event count, shape
  ids, hit point, normal, and approach speed against upstream C.
- `SensorEventParityTest` passes for sensor begin/end events, persisted
  overlaps, overlap capacity, visitor ids, and shape generations against
  upstream C.
- `DebugDrawParityTest` passes for debug draw shape, bounds, body-name, and
  mass callbacks against upstream C.
- `DebugContactDrawParityTest` passes for debug draw contact points, normal
  vectors, graph-color points, normal impulse segments/labels, contact feature
  labels, and friction impulse segments/labels against upstream C.
- `ChainParityTest` passes for chain creation/access/material/query/destroy
  behavior against upstream C.
- `JointParityTest` passes for common joint default/create/access/destroy and
  collide-connected behavior against upstream C.
- `DistanceJointParityTest` passes for distance joint length, spring, limit,
  current-length, and motor accessors, plus dynamic spring/limit/motor solver
  simulation state and force output against upstream C.
- `MotorJointParityTest` passes for motor joint offset, max force/torque,
  correction factor, and dynamic solver simulation state and force/torque
  output against upstream C.
- `MouseJointParityTest` passes for mouse joint target, local anchors, spring
  tuning, max force, and dynamic solver simulation state and force/torque
  output against upstream C.
- `WeldJointParityTest` passes for weld joint local anchors, reference angle,
  linear/angular tuning, and dynamic solver simulation state and force/torque
  output against upstream C.
- `RevoluteJointParityTest` passes for revolute joint local anchors, reference
  angle, spring, limit, motor, current angle, and zeroed constraint
  force/torque accessors against upstream C.
- `RevoluteSolverParityTest` passes for isolated, single-body ground-contact,
  mini FallingHinges-style free-fall, and mini FallingHinges-style
  ground-contact dynamic revolute joint simulations, including body transforms,
  velocities, joint force/torque, joint angle, and contact count against
  upstream C.
- `SmallFallingHingesParityTest` passes for four-body/two-hinge-pair,
  eight-body/four-hinge-pair, sixteen-body/eight-hinge-pair,
  twenty-four-body/twelve-hinge-pair, thirty-two-body/sixteen-hinge-pair,
  forty-eight-body/twenty-four-hinge-pair, sixty-four-body/thirty-two-hinge-pair,
  and 120-body/sixty-hinge-pair slices of the upstream FallingHinges helper,
  plus the upstream determinism helper's four-column 120-body layout at 90 and
  165 steps with sleeping disabled, including shared ground contact,
  transforms, velocities, contact count, and move-event count against upstream
  C.
- `DeterminismParityTest` passes for the upstream FallingHinges determinism
  helper sleep step (`288`) and final transform hash (`0x35467e1e`) against
  upstream C.
- `PrismaticJointParityTest` passes for prismatic joint local axis/anchors,
  reference angle, translation/speed, spring, limit, motor, and dynamic solver
  simulation state and force/torque output against upstream C.
- `WheelJointParityTest` passes for wheel joint local axis/anchors, spring,
  limit, motor, and dynamic solver simulation state and force/torque output
  against upstream C.
- `PublicApiGapParityTest` passes for body type mutation, target-transform
  velocity setup, destroy-shape behavior, sensor overlap semantics, world
  profile/rebuild/speculative/memory-stat entry points, restitution and
  hit-event threshold clamping, and common joint separation/constraint-tuning
  accessors against upstream C.
- `WorldLifecycleParityTest` passes for upstream empty-world stepping,
  destroy-all-bodies counters, body/world id invalidation, and world recycling
  across `B2_MAX_WORLDS / 2` worlds for 100 iterations against upstream C.
- `ShapeMaterialParityTest` passes for shape friction/restitution/material and
  full surface material APIs.
- `ShapeFilterParityTest` passes for shape filters, contact reset after filter
  changes, and event flag toggles.
- `ShapeGeometryParityTest` passes for circle/segment/polygon geometry getters
  and setters, defensive copies, contact destruction, and proxy recreation.
- `ShapeAccessParityTest` passes for shape body/world accessors, sensor/type
  reporting, user data, density mutation, body mass refresh behavior, and world
  generation parity.
- `CapsuleShapeParityTest` passes for capsule shape creation, getter/setter
  defensive copies, body mass data, contact capacity, and manifold point count.
- `ShapeQueryParityTest` passes for shape AABB, mass data, point tests,
  ray-casts, and closest-point queries across polygon, capsule, circle, and
  segment shapes.
- `BodyAccessParityTest` passes for body type/name/user data, owner world,
  shape count/enumeration order, AABB, transform mutation, velocity setters,
  point velocities, damping/gravity, fixed rotation, event toggles, and
  enable/disable state.
- `BodyForceParityTest` passes for body force/torque accumulation, linear and
  angular impulses, force integration across sub-steps, and force reset against
  upstream C.
- `BodyMassParityTest` passes for shape-derived body mass data, center of mass,
  body-space/world-space transforms, point velocity, and angular velocity.
- `BodySleepParityTest` passes for body awake/sleep enablement and sleep
  threshold APIs.
- `KinematicsParityTest` passes for contact-free dynamic body stepping with
  exact C parity across sub-stepped linear/angular integration.
- `ContactCreationParityTest` passes for broad-phase pair update/contact
  creation counts in the HelloWorld scenario.
- `ContactDataParityTest` passes for contact capacity, contact data count,
  begin/end event totals, and manifold point count in the HelloWorld scenario.
- `ContactSolverParityTest` passes for HelloWorld contact-solver settling and
  sleeping against upstream C with a tight tolerance.
- `ParityTest.helloWorldMatchesUpstreamC` passes with zero tolerance for the
  simulated HelloWorld body transform after 90 steps.
- `HelloWorldSampleTest` passes with zero tolerance for the headless
  `org.box2d4j.samples.HelloWorld` sample result and output line against the
  upstream C probe.
- `FallingHingesSampleTest` passes for the headless
  `org.box2d4j.samples.FallingHinges` sample sleep step (`288`) and transform
  hash (`0x35467e1e`) against the upstream C determinism helper.

Current C HelloWorld result:

```text
hello -8.30699282e-05 0.999911129 -5.57434032e-06
```

Current Java solver result:

```text
hello -8.30699282e-05 0.999911129 -5.57434032e-06
```

## Remaining Work

The current evidence and known gaps are maintained in `FEATURE_PARITY.md`.
The concrete closure order is:

1. Enforce upstream world locking during callbacks/reentrant stepping and fix
   zero-time-step event semantics.
2. Populate detailed profile timings and stack/tree/task counter telemetry.
3. Add upstream joint-extra and island-AABB debug drawing.
4. Expand exact long-horizon and CCD parity probes, especially moving
   kinematic targets and custom pre-solve behavior around TOI.
