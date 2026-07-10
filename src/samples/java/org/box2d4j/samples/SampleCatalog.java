package org.box2d4j.samples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SampleCatalog {
    private static final List<Entry> ENTRIES = createEntries();

    private SampleCatalog() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    private static List<Entry> createEntries() {
        ArrayList<Entry> entries = new ArrayList<>();
        add(entries, "Tutorial", HelloWorld.class);
        add(entries, "Determinism", FallingHinges.class);
        add(entries, "Stacking", SingleBox.class, TiltedStack.class, VerticalStack.class, DoubleDomino.class,
            CircleStack.class, CapsuleStack.class, Cliff.class, Arch.class, Confined.class, CardHouse.class);
        add(entries, "Bodies", KinematicBody.class, BodyType.class, Sleep.class, Pivot.class, Weeble.class,
            BadBody.class, RecreateStatic.class);
        add(entries, "Shapes", Friction.class, Restitution.class, RollingResistance.class, ConveyorBelt.class,
            TangentSpeed.class, ShapeFilter.class, CustomFilter.class, ModifyGeometry.class, ChainShape.class,
            CompoundShapes.class, ChainLink.class, RoundedShapes.class, EllipseShape.class, ConvexHull.class,
            OffsetShapes.class, Explosion.class);
        add(entries, "Events", SensorFunnel.class, SensorBookend.class, FootSensor.class, ContactEventSample.class,
            SensorTypes.class, Platformer.class, BodyMove.class);
        add(entries, "Joints", FilterJoint.class, RevoluteJoint.class, MotorJoint.class, DistanceJoint.class,
            PrismaticJoint.class, WheelJoint.class, Bridge.class, BallAndChain.class, Cantilever.class,
            FixedRotation.class, BreakableJoint.class, JointSeparation.class, UserConstraint.class, Driving.class,
            Ragdoll.class, SoftBody.class, Doohickey.class, ScissorLift.class, GearLift.class, Door.class,
            ScaleRagdoll.class);
        add(entries, "Character", Mover.class);
        add(entries, "Collision", ShapeDistance.class, RayCast.class, TimeOfImpact.class, ShapeCast.class,
            SmoothManifold.class, ManifoldSample.class, OverlapWorld.class, CastWorld.class, DynamicTreeSample.class);
        add(entries, "Continuous", SegmentSlide.class, SkinnyBox.class, SpeculativeFallback.class,
            SpeculativeSliver.class, SpeculativeGhost.class, ChainDrop.class, ChainSlide.class,
            PixelImperfect.class, RestitutionThreshold.class, Wedge.class, BounceHouse.class, GhostBumps.class,
            Drop.class, Pinball.class, BounceHumans.class);
        add(entries, "Robustness", HighMassRatio1.class, HighMassRatio2.class, HighMassRatio3.class,
            OverlapRecovery.class, TinyPyramid.class, Cart.class);
        add(entries, "Benchmark", BenchmarkBarrel.class, BenchmarkTumbler.class, BenchmarkLargePyramid.class,
            BenchmarkManyPyramids.class, BenchmarkJointGrid.class, BenchmarkSmash.class, BenchmarkSpinner.class,
            BenchmarkManyTumblers.class, BenchmarkCreateDestroy.class, BenchmarkSleep.class,
            BenchmarkCompound.class, BenchmarkKinematic.class, BenchmarkCast.class, BenchmarkRain.class,
            BenchmarkShapeDistance.class, BenchmarkSensor.class);
        add(entries, "World", LargeWorld.class);
        return Collections.unmodifiableList(entries);
    }

    @SafeVarargs
    private static void add(List<Entry> entries, String category, Class<?>... types) {
        for (Class<?> type : types) {
            entries.add(new Entry(category, displayName(type.getSimpleName()), type));
        }
    }

    private static String displayName(String simpleName) {
        String name = simpleName.startsWith("Benchmark") ? simpleName.substring("Benchmark".length()) : simpleName;
        if (name.endsWith("Sample")) {
            name = name.substring(0, name.length() - "Sample".length());
        }
        return name.replaceAll("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Za-z])(?=[0-9])", " ");
    }

    public static final class Entry {
        public final String category;
        public final String name;
        public final Class<?> type;

        private Entry(String category, String name, Class<?> type) {
            this.category = category;
            this.name = name;
            this.type = type;
        }

        public String qualifiedName() {
            return category + " / " + name;
        }
    }
}
