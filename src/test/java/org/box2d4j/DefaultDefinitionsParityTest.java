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

final class DefaultDefinitionsParityTest {
    @Test
    void publicDefaultDefinitionsMatchEveryTrackedUpstreamField() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef world = b2DefaultWorldDef();
        assertValues(lines[0], "world", world.gravity.x, world.gravity.y, world.restitutionThreshold,
            world.hitEventThreshold, world.contactHertz, world.contactDampingRatio, world.maxContactPushSpeed,
            world.maximumLinearSpeed, world.frictionCallback == null, world.restitutionCallback == null,
            world.enableSleep, world.enableContinuous, world.workerCount, world.enqueueTask == null,
            world.finishTask == null, world.userTaskContext == null, world.userData == null, world.internalValue);

        b2BodyDef body = b2DefaultBodyDef();
        assertValues(lines[1], "body", body.type, body.position.x, body.position.y, body.rotation.c, body.rotation.s,
            body.linearVelocity.x, body.linearVelocity.y, body.angularVelocity, body.linearDamping,
            body.angularDamping, body.gravityScale, body.sleepThreshold, body.name == null, body.userData == null,
            body.enableSleep, body.isAwake, body.fixedRotation, body.isBullet, body.isEnabled,
            body.allowFastRotation, body.internalValue);

        b2Filter filter = b2DefaultFilter();
        b2QueryFilter query = b2DefaultQueryFilter();
        b2SurfaceMaterial material = b2DefaultSurfaceMaterial();
        assertValues(lines[2], "base", filter.categoryBits, filter.maskBits, filter.groupIndex, query.categoryBits,
            query.maskBits, material.friction, material.restitution, material.rollingResistance,
            material.tangentSpeed, material.userMaterialId, material.customColor);

        b2ShapeDef shape = b2DefaultShapeDef();
        assertValues(lines[3], "shape", shape.userData == null, shape.material.friction, shape.material.restitution,
            shape.material.rollingResistance, shape.material.tangentSpeed, shape.material.userMaterialId,
            shape.material.customColor, shape.density, shape.filter.categoryBits, shape.filter.maskBits,
            shape.filter.groupIndex, shape.isSensor, shape.enableSensorEvents, shape.enableContactEvents,
            shape.enableHitEvents, shape.enablePreSolveEvents, shape.invokeContactCreation, shape.updateBodyMass,
            shape.internalValue);

        b2ChainDef chain = b2DefaultChainDef();
        assertValues(lines[4], "chain", chain.userData == null, chain.points == null, chain.count,
            chain.materials != null, chain.materialCount, chain.materials[0].friction,
            chain.materials[0].restitution, chain.materials[0].rollingResistance,
            chain.materials[0].tangentSpeed, chain.materials[0].userMaterialId, chain.materials[0].customColor,
            chain.filter.categoryBits, chain.filter.maskBits, chain.filter.groupIndex, chain.isLoop,
            chain.enableSensorEvents, chain.internalValue);

        b2ExplosionDef explosion = b2DefaultExplosionDef();
        assertValues(lines[5], "explosion", explosion.maskBits, explosion.position.x, explosion.position.y,
            explosion.radius, explosion.falloff, explosion.impulsePerLength);

        b2DebugDraw draw = b2DefaultDebugDraw();
        assertValues(lines[6], "draw", draw.DrawPolygonFcn != null, draw.DrawSolidPolygonFcn != null,
            draw.DrawCircleFcn != null, draw.DrawSolidCircleFcn != null, draw.DrawSolidCapsuleFcn != null,
            draw.DrawSegmentFcn != null, draw.DrawTransformFcn != null, draw.DrawPointFcn != null,
            draw.DrawStringFcn != null, draw.drawingBounds.lowerBound.x, draw.drawingBounds.lowerBound.y,
            draw.drawingBounds.upperBound.x, draw.drawingBounds.upperBound.y, draw.useDrawingBounds,
            draw.drawShapes, draw.drawJoints, draw.drawJointExtras, draw.drawBounds, draw.drawMass,
            draw.drawBodyNames, draw.drawContacts, draw.drawGraphColors, draw.drawContactNormals,
            draw.drawContactImpulses, draw.drawContactFeatures, draw.drawFrictionImpulses, draw.drawIslands,
            draw.context == null);

        b2DistanceJointDef distance = b2DefaultDistanceJointDef();
        b2MotorJointDef motor = b2DefaultMotorJointDef();
        b2MouseJointDef mouse = b2DefaultMouseJointDef();
        b2FilterJointDef filterJoint = b2DefaultFilterJointDef();
        b2PrismaticJointDef prismatic = b2DefaultPrismaticJointDef();
        b2RevoluteJointDef revolute = b2DefaultRevoluteJointDef();
        b2WeldJointDef weld = b2DefaultWeldJointDef();
        b2WheelJointDef wheel = b2DefaultWheelJointDef();
        assertValues(lines[7], "joints", distance.length, distance.maxLength, distance.internalValue,
            motor.maxForce, motor.maxTorque, motor.correctionFactor, motor.internalValue, mouse.hertz,
            mouse.dampingRatio, mouse.maxForce, mouse.internalValue, filterJoint.internalValue,
            prismatic.localAxisA.x, prismatic.localAxisA.y, prismatic.internalValue, revolute.drawSize,
            revolute.internalValue, weld.internalValue, wheel.localAxisA.x, wheel.localAxisA.y,
            wheel.enableSpring, wheel.hertz, wheel.dampingRatio, wheel.internalValue);
        assertEquals(8, lines.length);
    }

    private static void assertValues(String line, String label, Object... expected) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(expected.length + 1, parts.length, label + " field count");
        for (int i = 0; i < expected.length; ++i) {
            Object value = expected[i];
            String token = parts[i + 1];
            if (value instanceof Float) {
                assertEquals(Float.floatToRawIntBits((Float) value),
                    Float.floatToRawIntBits(Float.parseFloat(token)), label + " field " + i);
            } else if (value instanceof Long) {
                assertEquals(((Long) value).longValue(), Long.parseLong(token), label + " field " + i);
            } else if (value instanceof Boolean) {
                assertEquals((Boolean) value, Integer.parseInt(token) != 0, label + " field " + i);
            } else {
                assertEquals(((Number) value).intValue(), Integer.parseInt(token), label + " field " + i);
            }
        }
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_default_definitions_probe");

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
        command.add(root.resolve("tools/parity/box2d_default_definitions_probe.c").toString());
        command.add("-lm");
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
}
