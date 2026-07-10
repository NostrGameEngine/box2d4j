package org.box2d4j;

import org.box2d4j.samples.SampleCatalog;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SampleBindingCoverageTest {
    private static final Set<String> INTERACTIVE_SAMPLES = Set.of(
        "BallAndChain", "BenchmarkBarrel", "BenchmarkCast", "BenchmarkManyTumblers", "BenchmarkShapeDistance",
        "BodyMove", "BodyType", "BounceHouse", "BreakableJoint", "Bridge", "Cantilever", "Cart", "CastWorld",
        "ChainDrop", "ChainShape", "Cliff", "CompoundShapes", "ContactEventSample", "ConvexHull", "DistanceJoint",
        "Door", "Driving", "Drop", "DynamicTreeSample", "Explosion", "FixedRotation", "FootSensor", "GearLift",
        "GhostBumps", "JointSeparation", "LargeWorld", "ManifoldSample", "ModifyGeometry", "MotorJoint", "Mover",
        "OverlapRecovery", "OverlapWorld", "Pinball", "Platformer", "PrismaticJoint", "Ragdoll", "RayCast",
        "Restitution", "RevoluteJoint", "RollingResistance", "ScaleRagdoll", "ScissorLift", "SensorBookend",
        "SensorFunnel", "ShapeCast", "ShapeDistance", "ShapeFilter", "SkinnyBox", "Sleep", "SmoothManifold",
        "TangentSpeed", "VerticalStack", "Weeble", "WheelJoint"
    );

    @Test
    void everyUpstreamInteractiveSampleRegistersJavaRuntimeControls() throws Exception {
        Set<String> catalogTypes = SampleCatalog.entries().stream()
            .map(entry -> entry.type.getSimpleName())
            .collect(Collectors.toSet());
        assertTrue(catalogTypes.containsAll(INTERACTIVE_SAMPLES));
        assertEquals(59, INTERACTIVE_SAMPLES.size());

        Path sourceRoot = Path.of("src/samples/java/org/box2d4j/samples");
        for (String sample : INTERACTIVE_SAMPLES) {
            String source = Files.readString(sourceRoot.resolve(sample + ".java"), StandardCharsets.UTF_8);
            assertTrue(source.contains("SampleRuntime."), sample + " has no debugger binding");
        }
    }
}
