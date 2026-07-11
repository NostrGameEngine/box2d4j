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

final class PublicApiGapParityTest {
    @Test
    void remainingNonDebugPublicApisMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldDef worldDef = b2DefaultWorldDef();
        b2WorldId worldId = b2CreateWorld(worldDef);

        assertProfileLine(lines[line++], b2World_GetProfile(worldId));
        b2World_SetRestitutionThreshold(worldId, -2.5f);
        b2World_SetHitEventThreshold(worldId, -3.5f);
        assertThresholdsLine(lines[line++], b2World_GetRestitutionThreshold(worldId), b2World_GetHitEventThreshold(worldId));
        b2World_EnableSpeculative(worldId, false);
        b2World_RebuildStaticTree(worldId);
        b2World_DumpMemoryStats(worldId);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(1.0f, -2.0f);
        bodyDef.rotation = b2MakeRot(0.2f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2ShapeId circleId = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(0.25f, -0.1f), 0.5f));
        assertMassLine(lines[line++], "mass0", b2Body_GetMassData(bodyId), b2Body_GetShapeCount(bodyId));

        b2Body_SetTargetTransform(bodyId, new b2Transform(new b2Vec2(3.0f, 1.0f), b2MakeRot(0.8f)), 0.5f);
        assertVecLine(lines[line++], "targetLinear", b2Body_GetLinearVelocity(bodyId));
        assertTargetAngularLine(lines[line++], b2Body_GetAngularVelocity(bodyId), b2Body_IsAwake(bodyId));

        b2Body_SetType(bodyId, b2_staticBody);
        assertTypeLine(lines[line++], "type0", b2Body_GetType(bodyId), b2Body_GetMass(bodyId), b2World_GetAwakeBodyCount(worldId));
        b2Body_SetType(bodyId, b2_dynamicBody);
        assertTypeLine(lines[line++], "type1", b2Body_GetType(bodyId), b2Body_GetMass(bodyId), b2World_GetAwakeBodyCount(worldId));

        b2Polygon box = b2MakeBox(0.25f, 0.2f);
        b2ShapeId boxId = b2CreatePolygonShape(bodyId, shapeDef, box);
        b2MassData mass1 = b2Body_GetMassData(bodyId);
        b2DestroyShape(boxId, false);
        b2MassData mass2 = b2Body_GetMassData(bodyId);
        b2Body_ApplyMassFromShapes(bodyId);
        b2MassData mass3 = b2Body_GetMassData(bodyId);
        assertDestroyLine(lines[line++], b2Shape_IsValid(boxId), b2Shape_IsValid(circleId), b2Body_GetShapeCount(bodyId),
            mass1, mass2, mass3);

        b2BodyDef sensorBodyDef = b2DefaultBodyDef();
        b2BodyId sensorBody = b2CreateBody(worldId, sensorBodyDef);
        b2ShapeDef sensorDef = b2DefaultShapeDef();
        sensorDef.isSensor = true;
        sensorDef.enableSensorEvents = true;
        b2ShapeId sensorId = b2CreateCircleShape(sensorBody, sensorDef, new b2Circle(new b2Vec2(10.0f, 0.0f), 2.0f));

        b2BodyDef visitorDef = b2DefaultBodyDef();
        visitorDef.type = b2_dynamicBody;
        visitorDef.position = new b2Vec2(10.5f, 0.0f);
        b2BodyId visitorBody = b2CreateBody(worldId, visitorDef);
        shapeDef.enableSensorEvents = true;
        b2ShapeId visitorId = b2CreateCircleShape(visitorBody, shapeDef, new b2Circle(new b2Vec2(0.0f, 0.0f), 0.25f));
        b2World_Step(worldId, 1.0f / 60.0f, 1);
        b2ShapeId[] overlaps = new b2ShapeId[4];
        int overlapCount = b2Shape_GetSensorOverlaps(sensorId, overlaps, 4);
        assertSensorLine(lines[line++], b2Shape_GetSensorCapacity(sensorId), overlapCount, overlaps[0], visitorId);

        b2BodyDef jointBodyDef = b2DefaultBodyDef();
        jointBodyDef.type = b2_dynamicBody;
        jointBodyDef.position = new b2Vec2(0.0f, 0.0f);
        jointBodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId jointA = b2CreateBody(worldId, jointBodyDef);
        jointBodyDef.position = new b2Vec2(2.0f, 3.0f);
        jointBodyDef.rotation = b2MakeRot(0.75f);
        b2BodyId jointB = b2CreateBody(worldId, jointBodyDef);

        b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
        distanceDef.bodyIdA = jointA;
        distanceDef.bodyIdB = jointB;
        distanceDef.length = 2.0f;
        distanceDef.enableSpring = false;
        b2JointId distanceJoint = b2CreateDistanceJoint(worldId, distanceDef);
        assertJointLine(lines[line++], "distanceSep0", distanceJoint);
        b2Joint_SetConstraintTuning(distanceJoint, 12.0f, 0.7f);
        assertJointLine(lines[line++], "distanceSep1", distanceJoint);

        b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
        prismaticDef.bodyIdA = jointA;
        prismaticDef.bodyIdB = jointB;
        prismaticDef.localAxisA = new b2Vec2(1.0f, 0.0f);
        prismaticDef.referenceAngle = 0.25f;
        prismaticDef.enableLimit = true;
        prismaticDef.lowerTranslation = -1.0f;
        prismaticDef.upperTranslation = 1.0f;
        b2JointId prismaticJoint = b2CreatePrismaticJoint(worldId, prismaticDef);
        assertJointLine(lines[line++], "prismaticSep", prismaticJoint);

        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = jointA;
        revoluteDef.bodyIdB = jointB;
        revoluteDef.referenceAngle = 0.25f;
        revoluteDef.enableLimit = true;
        revoluteDef.lowerAngle = -0.2f;
        revoluteDef.upperAngle = 0.2f;
        b2JointId revoluteJoint = b2CreateRevoluteJoint(worldId, revoluteDef);
        assertJointLine(lines[line++], "revoluteSep", revoluteJoint);

        b2WeldJointDef weldDef = b2DefaultWeldJointDef();
        weldDef.bodyIdA = jointA;
        weldDef.bodyIdB = jointB;
        weldDef.referenceAngle = 0.25f;
        weldDef.linearHertz = 0.0f;
        weldDef.angularHertz = 0.0f;
        b2JointId weldJoint = b2CreateWeldJoint(worldId, weldDef);
        assertJointLine(lines[line++], "weldSep", weldJoint);

        b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
        wheelDef.bodyIdA = jointA;
        wheelDef.bodyIdB = jointB;
        wheelDef.localAxisA = new b2Vec2(1.0f, 0.0f);
        wheelDef.enableLimit = true;
        wheelDef.lowerTranslation = -1.0f;
        wheelDef.upperTranslation = 1.0f;
        b2JointId wheelJoint = b2CreateWheelJoint(worldId, wheelDef);
        assertJointLine(lines[line++], "wheelSep", wheelJoint);

        assertEquals(lines.length, line);
        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_public_api_gap_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_public_api_gap_probe.c").toString());
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

    private static void assertProfileLine(String line, b2Profile profile) {
        String[] parts = line.split("\\s+");
        assertEquals("profile", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), profile.step, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), profile.solve, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), profile.sensors, 0.0f);
    }

    private static void assertThresholdsLine(String line, float restitutionThreshold, float hitEventThreshold) {
        String[] parts = line.split("\\s+");
        assertEquals("thresholds", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), restitutionThreshold, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), hitEventThreshold, 0.0f);
    }

    private static void assertMassLine(String line, String label, b2MassData mass, int shapeCount) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), mass.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), mass.center.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), mass.center.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), mass.rotationalInertia, 0.0f);
        assertEquals(Integer.parseInt(parts[5]), shapeCount);
    }

    private static void assertVecLine(String line, String label, b2Vec2 value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), value.y, 0.0f);
    }

    private static void assertTargetAngularLine(String line, float angularVelocity, boolean awake) {
        String[] parts = line.split("\\s+");
        assertEquals("targetAngular", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), angularVelocity, 0.0f);
        assertEquals(Integer.parseInt(parts[2]) != 0, awake);
    }

    private static void assertTypeLine(String line, String label, int type, float mass, int awakeCount) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), type);
        assertEquals(Float.parseFloat(parts[2]), mass, 0.0f);
        assertEquals(Integer.parseInt(parts[3]), awakeCount);
    }

    private static void assertDestroyLine(String line, boolean boxValid, boolean circleValid, int shapeCount,
                                          b2MassData mass1, b2MassData mass2, b2MassData mass3) {
        String[] parts = line.split("\\s+");
        assertEquals("destroy", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, boxValid);
        assertEquals(Integer.parseInt(parts[2]) != 0, circleValid);
        assertEquals(Integer.parseInt(parts[3]), shapeCount);
        assertEquals(Float.parseFloat(parts[4]), mass1.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), mass2.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), mass3.mass, 0.0f);
    }

    private static void assertSensorLine(String line, int capacity, int count, b2ShapeId overlap, b2ShapeId visitorId) {
        String[] parts = line.split("\\s+");
        assertEquals("sensor", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), capacity);
        assertEquals(Integer.parseInt(parts[2]), count);
        assertEquals(Integer.parseInt(parts[5]) != 0, overlap.index1 == visitorId.index1);
    }

    private static void assertJointLine(String line, String label, b2JointId jointId) {
        String[] parts = line.split("\\s+");
        float[] hertz = new float[1];
        float[] dampingRatio = new float[1];
        b2Joint_GetConstraintTuning(jointId, hertz, dampingRatio);
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Joint_GetLinearSeparation(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2Joint_GetAngularSeparation(jointId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), hertz[0], 0.0f);
        assertEquals(Float.parseFloat(parts[4]), dampingRatio[0], 0.0f);
    }
}
