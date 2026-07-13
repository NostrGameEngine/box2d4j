package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class RandomWorldParityTest {
    @Test
    void deterministicRandomWorldsMatchScalarUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int lineIndex = 0;

        for (int expectedScene = 0; expectedScene < 8; ++expectedScene) {
            String[] scene = lines[lineIndex++].split("\\s+");
            assertEquals("scene", scene[0]);
            assertEquals(expectedScene, integer(scene[1]));
            int stepCount = integer(scene[5]);
            int bodyCount = integer(scene[6]);

            b2WorldDef worldDef = b2DefaultWorldDef();
            worldDef.gravity = new b2Vec2(decimal(scene[2]), decimal(scene[3]));
            worldDef.enableSleep = integer(scene[4]) != 0;
            b2WorldId worldId = b2CreateWorld(worldDef);

            int[] callbackCounts = new int[4];
            int[] callbackHashes = new int[4];
            b2World_SetCustomFilterCallback(worldId, (shapeA, shapeB, context) -> {
                callbackCounts[0] += 1;
                int materialA = b2Shape_GetMaterial(shapeA);
                int materialB = b2Shape_GetMaterial(shapeB);
                callbackHashes[0] = updateHash(callbackHashes[0], materialA, materialB);
                return (materialA + materialB) % 7 != 0;
            }, null);
            b2World_SetPreSolveCallback(worldId, (shapeA, shapeB, manifold, context) -> {
                callbackCounts[1] += 1;
                callbackHashes[1] = updateHash(callbackHashes[1], shapeA.index1, shapeB.index1);
                return (shapeA.index1 + shapeB.index1) % 11 != 0;
            }, null);
            b2World_SetFrictionCallback(worldId, (frictionA, materialA, frictionB, materialB) -> {
                callbackCounts[2] += 1;
                callbackHashes[2] = updateHash(callbackHashes[2], materialA, materialB);
                return 0.1f + 0.02f * (materialA + 2 * materialB);
            });
            b2World_SetRestitutionCallback(worldId, (restitutionA, materialA, restitutionB, materialB) -> {
                callbackCounts[3] += 1;
                callbackHashes[3] = updateHash(callbackHashes[3], materialA, materialB);
                return 0.05f * ((materialA + 3 * materialB) % 8);
            });

            b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
            b2ShapeDef groundShapeDef = b2DefaultShapeDef();
            groundShapeDef.material.userMaterialId = 90;
            groundShapeDef.enablePreSolveEvents = true;
            b2CreateSegmentShape(groundId, groundShapeDef,
                new b2Segment(new b2Vec2(-15.0f, 0.0f), new b2Vec2(15.0f, 0.0f)));

            b2BodyId sensorBodyId = b2CreateBody(worldId, b2DefaultBodyDef());
            b2ShapeDef sensorShapeDef = b2DefaultShapeDef();
            sensorShapeDef.isSensor = true;
            sensorShapeDef.enableSensorEvents = true;
            sensorShapeDef.material.userMaterialId = 91;
            b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, sensorShapeDef,
                new b2Circle(new b2Vec2(0.0f, 2.5f), 1.75f));

            b2BodyId[] bodies = new b2BodyId[bodyCount];
            b2ShapeId[] shapes = new b2ShapeId[bodyCount];
            for (int bodyIndex = 0; bodyIndex < bodyCount; ++bodyIndex) {
                String[] body = lines[lineIndex++].split("\\s+");
                assertEquals("body", body[0]);

                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = integer(body[1]);
                bodyDef.position = new b2Vec2(decimal(body[2]), decimal(body[3]));
                bodyDef.rotation = new b2Rot(decimal(body[4]), decimal(body[5]));
                bodyDef.linearVelocity = new b2Vec2(decimal(body[6]), decimal(body[7]));
                bodyDef.angularVelocity = decimal(body[8]);
                bodyDef.enableSleep = integer(body[9]) != 0;
                bodyDef.isBullet = integer(body[10]) != 0;
                bodies[bodyIndex] = b2CreateBody(worldId, bodyDef);

                int shapeKind = integer(body[11]);
                float p0 = decimal(body[12]);
                float p1 = decimal(body[13]);
                float p2 = decimal(body[14]);
                float p3 = decimal(body[15]);
                float p4 = decimal(body[16]);
                b2ShapeDef shapeDef = b2DefaultShapeDef();
                shapeDef.density = decimal(body[17]);
                shapeDef.material.friction = decimal(body[18]);
                shapeDef.material.restitution = decimal(body[19]);
                shapeDef.material.userMaterialId = bodyIndex + 1;
                shapeDef.enablePreSolveEvents = true;

                if (shapeKind == 0) {
                    shapes[bodyIndex] = b2CreateCircleShape(bodies[bodyIndex], shapeDef,
                        new b2Circle(new b2Vec2(p0, p1), p2));
                } else if (shapeKind == 1) {
                    shapes[bodyIndex] = b2CreateCapsuleShape(bodies[bodyIndex], shapeDef,
                        new b2Capsule(new b2Vec2(p0, p1), new b2Vec2(p2, p3), p4));
                } else {
                    shapes[bodyIndex] = b2CreatePolygonShape(bodies[bodyIndex], shapeDef, b2MakeBox(p0, p1));
                }
            }

            b2JointId[] joints = new b2JointId[3];
            b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
            distanceDef.bodyIdA = bodies[1];
            distanceDef.bodyIdB = bodies[2];
            distanceDef.length = 1.75f;
            distanceDef.enableSpring = true;
            distanceDef.hertz = 2.0f;
            distanceDef.dampingRatio = 0.5f;
            joints[0] = b2CreateDistanceJoint(worldId, distanceDef);

            b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
            revoluteDef.bodyIdA = bodies[3];
            revoluteDef.bodyIdB = bodies[4];
            revoluteDef.enableMotor = true;
            revoluteDef.maxMotorTorque = 12.0f;
            revoluteDef.motorSpeed = 0.5f;
            joints[1] = b2CreateRevoluteJoint(worldId, revoluteDef);

            b2WeldJointDef weldDef = b2DefaultWeldJointDef();
            weldDef.bodyIdA = bodies[9];
            weldDef.bodyIdB = bodies[10];
            weldDef.linearHertz = 2.5f;
            weldDef.angularHertz = 1.5f;
            weldDef.linearDampingRatio = 0.6f;
            weldDef.angularDampingRatio = 0.4f;
            joints[2] = b2CreateWeldJoint(worldId, weldDef);

            for (int step = 0; step < stepCount; ++step) {
                applyRuntimeAction(expectedScene, step, worldId, bodies, shapes, joints, sensorShapeId);
                b2World_Step(worldId, 1.0f / 60.0f, 4);
                String label = "scene " + expectedScene + " checkpoint " + step;
                String[] checkpoint = lines[lineIndex++].split("\\s+");
                assertEquals("checkpoint", checkpoint[0]);
                assertEquals(step, integer(checkpoint[1]), label + " step");
                for (int callbackIndex = 1; callbackIndex < callbackCounts.length; ++callbackIndex) {
                    assertEquals(integer(checkpoint[callbackIndex + 22]), callbackCounts[callbackIndex],
                        label + " callback " + callbackIndex + " count");
                    assertEquals(integer(checkpoint[callbackIndex + 34]), callbackHashes[callbackIndex],
                        label + " callback " + callbackIndex + " hash");
                }
                String[] capacities = lines[lineIndex++].split("\\s+");
                assertEquals("capacities", capacities[0]);
                int[] expectedCapacities = new int[bodyCount];
                int[] actualCapacities = new int[bodyCount];
                for (int bodyIndex = 0; bodyIndex < bodyCount; ++bodyIndex) {
                    expectedCapacities[bodyIndex] = integer(capacities[bodyIndex + 1]);
                    actualCapacities[bodyIndex] = b2Body_GetContactCapacity(bodies[bodyIndex]);
                }
                assertArrayEquals(expectedCapacities, actualCapacities, label + " contact capacities expected="
                    + java.util.Arrays.toString(expectedCapacities) + " actual=" + java.util.Arrays.toString(actualCapacities));
                assertEquals(integer(checkpoint[2]), b2World_GetCounters(worldId).contactCount,
                    label + " contact count");
                String[] touching = lines[lineIndex++].split("\\s+");
                assertEquals("touching", touching[0]);
                int[] expectedTouching = new int[bodyCount];
                int[] actualTouching = new int[bodyCount];
                for (int bodyIndex = 0; bodyIndex < bodyCount; ++bodyIndex) {
                    expectedTouching[bodyIndex] = integer(touching[bodyIndex + 1]);
                    actualTouching[bodyIndex] = b2Body_GetContactData(bodies[bodyIndex], new b2ContactData[32], 32);
                }
                assertArrayEquals(expectedTouching, actualTouching, label + " touching contacts expected="
                    + java.util.Arrays.toString(expectedTouching) + " actual=" + java.util.Arrays.toString(actualTouching));
                b2Transform reenabledTransform = b2Body_GetTransform(bodies[5]);
                b2Vec2 reenabledVelocity = b2Body_GetLinearVelocity(bodies[5]);
                assertEquals(decimal(checkpoint[27]), reenabledTransform.p.x, 0.0f, label + " reenabled x");
                assertEquals(decimal(checkpoint[28]), reenabledTransform.p.y, 0.0f, label + " reenabled y");
                assertEquals(decimal(checkpoint[29]), reenabledTransform.q.c, 0.0f, label + " reenabled rotation.c");
                assertEquals(decimal(checkpoint[30]), reenabledTransform.q.s, 0.0f, label + " reenabled rotation.s");
                assertEquals(decimal(checkpoint[31]), reenabledVelocity.x, 0.0f, label + " reenabled velocity.x");
                assertEquals(decimal(checkpoint[32]), reenabledVelocity.y, 0.0f, label + " reenabled velocity.y");
                assertEquals(decimal(checkpoint[33]), b2Body_GetAngularVelocity(bodies[5]), 0.0f,
                    label + " reenabled angular velocity");
                assertEquals(integer(checkpoint[26]), awakeMask(bodies), label + " awake mask");
                assertEquals(integer(checkpoint[3]), countAwake(bodies), label + " awake count");
                b2Transform trackedTransform = b2Body_GetTransform(bodies[6]);
                b2Vec2 trackedVelocity = b2Body_GetLinearVelocity(bodies[6]);
                assertEquals(decimal(checkpoint[4]), trackedTransform.p.x, 0.0f, label + " tracked x");
                assertEquals(decimal(checkpoint[5]), trackedTransform.p.y, 0.0f, label + " tracked y");
                assertEquals(decimal(checkpoint[6]), trackedTransform.q.c, 0.0f, label + " tracked rotation.c");
                assertEquals(decimal(checkpoint[7]), trackedTransform.q.s, 0.0f, label + " tracked rotation.s");
                assertEquals(decimal(checkpoint[8]), trackedVelocity.x, 0.0f, label + " tracked velocity.x");
                assertEquals(decimal(checkpoint[9]), trackedVelocity.y, 0.0f, label + " tracked velocity.y");
                assertEquals(decimal(checkpoint[10]), b2Body_GetAngularVelocity(bodies[6]), 0.0f,
                    label + " tracked angular velocity");
                assertEquals(integer(checkpoint[11]), b2Body_GetType(bodies[6]), label + " tracked type");
                b2Transform peerTransform = b2Body_GetTransform(bodies[7]);
                b2Vec2 peerVelocity = b2Body_GetLinearVelocity(bodies[7]);
                assertEquals(decimal(checkpoint[12]), peerTransform.p.x, 0.0f, label + " peer x");
                assertEquals(decimal(checkpoint[13]), peerTransform.p.y, 0.0f, label + " peer y");
                assertEquals(decimal(checkpoint[14]), peerTransform.q.c, 0.0f, label + " peer rotation.c");
                assertEquals(decimal(checkpoint[15]), peerTransform.q.s, 0.0f, label + " peer rotation.s");
                assertEquals(decimal(checkpoint[16]), peerVelocity.x, 0.0f, label + " peer velocity.x");
                assertEquals(decimal(checkpoint[17]), peerVelocity.y, 0.0f, label + " peer velocity.y");
                assertEquals(decimal(checkpoint[18]), b2Body_GetAngularVelocity(bodies[7]), 0.0f,
                    label + " peer angular velocity");
                assertEquals(integer(checkpoint[19]) != 0, b2Joint_IsValid(joints[0]), label + " distance valid");
                assertEquals(integer(checkpoint[20]) != 0, b2Joint_IsValid(joints[1]), label + " revolute valid");
                assertEquals(integer(checkpoint[21]) != 0, b2Joint_IsValid(joints[2]), label + " weld valid");
                String[] sensor = lines[lineIndex++].split("\\s+");
                assertEquals("sensor", sensor[0]);
                int sensorCapacity = b2Shape_GetSensorCapacity(sensorShapeId);
                b2ShapeId[] overlaps = new b2ShapeId[bodyCount + 2];
                int sensorCount = b2Shape_GetSensorOverlaps(sensorShapeId, overlaps, overlaps.length);
                int sensorChecksum = 0;
                for (int overlapIndex = 0; overlapIndex < sensorCount; ++overlapIndex) {
                    sensorChecksum += overlaps[overlapIndex].index1;
                }
                b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
                assertEquals(integer(sensor[1]), sensorCapacity, label + " sensor capacity");
                assertEquals(integer(sensor[2]), sensorCount, label + " sensor count");
                assertEquals(integer(sensor[3]), sensorChecksum, label + " sensor checksum");
                assertEquals(integer(sensor[4]), sensorEvents.beginCount, label + " sensor begins");
                assertEquals(integer(sensor[5]), sensorEvents.endCount, label + " sensor ends");
            }

            String[] result = lines[lineIndex++].split("\\s+");
            assertEquals("result", result[0]);
            b2Counters counters = b2World_GetCounters(worldId);
            assertEquals(integer(result[1]), counters.bodyCount, "scene " + expectedScene + " body count");
            assertEquals(integer(result[2]), counters.shapeCount, "scene " + expectedScene + " shape count");
            assertEquals(integer(result[3]), counters.contactCount, "scene " + expectedScene + " contact count");
            assertEquals(integer(result[4]), countAwake(bodies), "scene " + expectedScene + " awake count");

            for (int bodyIndex = 0; bodyIndex < bodyCount; ++bodyIndex) {
                String label = "scene " + expectedScene + " body " + bodyIndex;
                String[] state = lines[lineIndex++].split("\\s+");
                assertEquals("state", state[0]);
                b2Transform transform = b2Body_GetTransform(bodies[bodyIndex]);
                b2Vec2 velocity = b2Body_GetLinearVelocity(bodies[bodyIndex]);
                assertEquals(decimal(state[1]), transform.p.x, 0.0f, label + " x");
                assertEquals(decimal(state[2]), transform.p.y, 0.0f, label + " y");
                assertEquals(decimal(state[3]), transform.q.c, 0.0f, label + " rotation.c");
                assertEquals(decimal(state[4]), transform.q.s, 0.0f, label + " rotation.s");
                assertEquals(decimal(state[5]), velocity.x, 0.0f, label + " velocity.x");
                assertEquals(decimal(state[6]), velocity.y, 0.0f, label + " velocity.y");
                assertEquals(decimal(state[7]), b2Body_GetAngularVelocity(bodies[bodyIndex]), 0.0f,
                    label + " angular velocity");
                assertEquals(integer(state[8]) != 0, b2Body_IsAwake(bodies[bodyIndex]), label + " awake");
                assertEquals(integer(state[9]), b2Body_GetContactCapacity(bodies[bodyIndex]), label + " contacts");
            }

            b2DestroyWorld(worldId);
        }
        assertEquals(lines.length, lineIndex);
    }

    private static void applyRuntimeAction(int sceneIndex, int step, b2WorldId worldId, b2BodyId[] bodies,
                                           b2ShapeId[] shapes, b2JointId[] joints, b2ShapeId sensorShapeId) {
        if (step == 60) {
            b2Body_SetTransform(bodies[2], new b2Vec2(sceneIndex - 3.5f, 6.0f),
                b2MakeRot(0.125f * (sceneIndex - 3)));
        } else if (step == 90) {
            b2DistanceJoint_SetLength(joints[0], 2.25f);
        } else if (step == 120) {
            b2Body_Disable(bodies[5]);
        } else if (step == 150) {
            b2Body_Enable(bodies[5]);
        } else if (step == 180) {
            b2RevoluteJoint_SetMotorSpeed(joints[1], -0.75f);
        } else if (step == 210) {
            b2Body_SetType(bodies[6], b2_kinematicBody);
            b2Body_SetLinearVelocity(bodies[6], new b2Vec2(-0.75f, 0.25f));
        } else if (step == 240) {
            b2Shape_EnableSensorEvents(sensorShapeId, false);
        } else if (step == 255) {
            b2Shape_EnableSensorEvents(sensorShapeId, true);
        } else if (step == 270) {
            b2Body_SetType(bodies[6], b2_dynamicBody);
        } else if (step == 285) {
            b2WeldJoint_SetLinearHertz(joints[2], 3.5f);
        } else if (step == 300) {
            b2World_EnableSleeping(worldId, false);
        } else if (step == 315) {
            b2World_EnableSleeping(worldId, (sceneIndex & 1) == 0);
        } else if (step == 330) {
            b2Shape_SetFriction(shapes[7], 0.95f);
        } else if (step == 345) {
            b2Filter filter = b2Shape_GetFilter(shapes[7]);
            filter.maskBits = 0L;
            b2Shape_SetFilter(shapes[7], filter);
        } else if (step == 360) {
            b2Filter filter = b2Shape_GetFilter(sensorShapeId);
            filter.maskBits = 0L;
            b2Shape_SetFilter(sensorShapeId, filter);
        } else if (step == 375) {
            b2Shape_SetFilter(shapes[7], b2DefaultFilter());
        } else if (step == 380) {
            b2Shape_SetFilter(sensorShapeId, b2DefaultFilter());
        } else if (step == 390) {
            b2Body_ApplyLinearImpulseToCenter(bodies[8], new b2Vec2(1.5f, 2.25f), true);
        } else if (step == 405) {
            b2Shape_SetCapsule(shapes[7], new b2Capsule(new b2Vec2(-0.35f, 0.0f),
                new b2Vec2(0.35f, 0.0f), 0.22f));
        } else if (step == 420) {
            b2DestroyJoint(joints[2]);
        } else if (step == 435) {
            b2Body_SetFixedRotation(bodies[8], true);
        } else if (step == 450) {
            b2World_SetGravity(worldId, new b2Vec2(-0.25f, -6.5f));
        } else if (step == 465) {
            b2Body_SetFixedRotation(bodies[8], false);
        } else if (step == 495) {
            b2World_EnableContinuous(worldId, false);
        } else if (step == 510) {
            b2Body_SetLinearVelocity(bodies[9], new b2Vec2(0.5f, 1.75f));
        } else if (step == 525) {
            b2World_EnableContinuous(worldId, true);
        } else if (step == 540) {
            b2Body_SetGravityScale(bodies[11], 0.35f);
        } else if (step == 555) {
            b2DestroyJoint(joints[1]);
        } else if (step == 570) {
            b2Body_SetAngularVelocity(bodies[10], -1.25f);
        }
    }

    private static int countAwake(b2BodyId[] bodies) {
        int count = 0;
        for (b2BodyId bodyId : bodies) {
            count += b2Body_IsAwake(bodyId) ? 1 : 0;
        }
        return count;
    }

    private static int awakeMask(b2BodyId[] bodies) {
        int mask = 0;
        for (int bodyIndex = 0; bodyIndex < bodies.length; ++bodyIndex) {
            if (b2Body_IsAwake(bodies[bodyIndex])) {
                mask |= 1 << bodyIndex;
            }
        }
        return mask;
    }

    private static int updateHash(int hash, int valueA, int valueB) {
        long contribution = (long) valueA * 1_000_003L + (long) valueB * 97_409L
            + (long) valueA * valueB * 389L;
        return (int) ((hash + contribution) % 1_000_000_007L);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_random_world_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-DBOX2D_DISABLE_SIMD");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_random_world_probe.c").toString());
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

    private static int integer(String value) {
        return Integer.parseInt(value);
    }

    private static float decimal(String value) {
        return Float.parseFloat(value);
    }
}
