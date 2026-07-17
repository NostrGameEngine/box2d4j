# Performance and allocation audit

This document records the July 2026 allocation pass. Measurements are intended
for regression tracking on the same JVM and machine, not as cross-platform
throughput guarantees.

## Method

- JMH 1.37 is the primary repeatable harness. Each case runs in an isolated
  Java 11 fork; `gc.alloc.rate.norm` reports bytes allocated per benchmark
  operation after JIT warmup.
- The JMH matrix covers all 16 upstream Benchmark scenes plus an empty world,
  Large World, all public primitive collision entry points, dynamic-tree
  query/ray/shape/rebuild paths, debug-draw capture, and the viewer scheduler
  with 1/2/4/8 workers.
- CPU and allocation call sites are sampled by selecting the JMH `jfr`
  profiler, which keeps scene setup outside the measured benchmark method.
- GC pressure used a 1,200-step `Bridge` run with G1, `-Xms256m`, and
  `-Xmx256m`.
- The lightweight `profileAllocations` task remains a supplementary long-run
  check using `com.sun.management.ThreadMXBean`.
- Every numerical change is checked against C parity. Public math methods keep
  their allocating, C-shaped return-value API; scratch storage is private to
  internal hot paths.

## Results

The Bridge GC run progressed as follows:

| Stage | Young collections |
| --- | ---: |
| Original implementation | 14 |
| Persistent solver state and constraints | 12 |
| Contact/revolute scratch and in-place integration | 7 |
| Reusable tree stacks and index-based joint filtering | 4 |
| In-place transform commit and force reset | 3 |

During the initial 200/400-step diagnostic, pooling polygon-collision and
tree-rebuild workspaces changed allocation from about 4.40 MB/step to 2.56
MB/step in Tumbler and from 1.49 MB/step to 0.86 MB/step in Large Pyramid. The
JMH matrix below is the current baseline; absolute values must only be compared
with the same scene horizon, JVM implementation, and JIT state.

JMH isolated dynamic-tree traversal allocations that were obscured in whole
world profiles. Reentrant per-thread ray/shape-cast workspaces changed ray cast
from about 7,424 B/op to 24 B/op and shape cast from about 4,688 B/op to 24
B/op. The remaining 24 bytes are the public `b2TreeStats` result. Query was
already at 24 B/op.

The full Java 11 JMH run (3 one-second warmups, 5 one-second measurements, one
fork) produced these representative results on the audit machine:

| Operation | Time | Allocation |
| --- | ---: | ---: |
| Empty world step | 0.191 us | 0.004 B/op |
| Large Pyramid frame | 0.880 ms | 0.852 MB/op |
| Tumbler frame | 1.917 ms | 2.425 MB/op |
| Large World frame | 1.994 ms | 2.360 MB/op |
| Dynamic-tree query | 2.841 us | 24 B/op |
| Dynamic-tree ray cast | 3.171 us | 24 B/op |
| Dynamic-tree shape cast | 3.442 us | 24 B/op |
| Debug capture, shapes | 23.927 us | 54.8 KB/op |
| Debug capture, all diagnostics | 0.828 ms | 1.149 MB/op |

The empty-world negative control initially exposed 984 B/step from fresh
profile/event wrappers, empty arrays, temporary contact/sensor collections, and
a captured tree-task lambda. Reusing the per-world outputs and skipping empty
pipelines reduced this to the measurement floor. A separate empty JMH method
measures approximately `0.000002 B/op` of harness overhead.

The scheduler benchmark uses the viewer's real `ExecutorService` adapter on a
sleep-disabled 210-body pyramid. One worker measured 0.888 ms and 0.852 MB/op;
2/4/8 workers measured 2.998/3.258/3.461 ms and
0.956/0.992/1.046 MB/op. This scene is below the point where its parallel work
repays executor scheduling. Applications should select worker count from their
own JMH/JFR workload instead of assuming more workers are faster.

## Implemented changes

- `WorldSlot` retains solver-state lists, contact constraints, graph-color
  lists, and softness records.
- Each body owns one reusable solver state; contacts and joints retain their
  typed solver constraints.
- A `ThreadLocal` solver workspace uses mark/release indices for vectors,
  rotations, and matrices. Parallel Box2D workers therefore never share
  mutable scratch values.
- Dynamic-tree query, ray-cast, shape-cast, and rebuild workspaces are retained
  per thread and nesting depth. Broad-phase body/joint filtering uses indexed
  traversal and creates no iterator.
- Polygon-polygon collision reuses two fully initialized local polygons per
  active thread and nesting depth.
- Integration, transform commit, and force reset mutate retained records.
- Profile and event wrapper objects are retained per world. Empty contact and
  sensor pipelines return without creating collections or arrays; body/contact
  event scratch lists retain capacity until the next step.
- The debugger reuses three world-draw batches and two jMonkeyEngine mesh
  upload buffers. Capacity grows only when a larger scene requires it.

Scratch values must never be assigned to persistent or API-visible fields.
Hot paths copy their final values into the owning body, manifold, joint, or
frame before releasing a mark.

## Remaining work

The core is not allocation-free. Current JFR traces identify these primary
remaining sites:

1. Collision routines still construct transient `b2Manifold` and
   `b2ManifoldPoint` graphs. A safe next step is an internal output-parameter
   collision API backed by the contact's alternate manifold buffer.
2. Event wrappers and empty outputs are reused, but individual begin/end/hit
   events, body-move records, and non-empty result arrays are still rebuilt.
   Grow-only event object pools could remove this remaining active-event cost.
3. CCD creates sweeps, transforms, contexts, and temporary AABBs for fast
   bodies. This needs a separate reentrant workspace because callbacks can
   execute during tree traversal.
4. Broad-phase move-pair results and sleep-component arrays are sized from the
   current world on each step. Per-world grow-only buffers would remove these
   steady-state arrays.
5. Public value-returning helpers intentionally allocate. Internal code should
   continue moving to private output/scratch variants only where profiling
   proves a frame-loop benefit.

## Commands

```bash
./gradlew jmh
./gradlew jmh -PjmhIncludes='.*DynamicTreeBenchmark.*'
./gradlew jmh -PjmhIncludes='.*DebugDrawBenchmark.*' -PjmhProfiler=jfr

./gradlew profileAllocations
./gradlew profileAllocations -PprofileWarmupSteps=200 -PprofileMeasuredSteps=400

java -Xms256m -Xmx256m \
  -Xlog:gc:file=build/profile-bridge.log:time,level,tags \
  -cp build/classes/java/main:build/classes/java/samples \
  org.box2d4j.samples.Bridge 1200
```

JMH reports are generated under `build/reports/jmh`. Its JFR profiler creates a
`profile.jfr` directory per selected benchmark and parameter combination.
