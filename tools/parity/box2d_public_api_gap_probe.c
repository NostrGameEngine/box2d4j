// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

static void print_joint(const char* label, b2JointId jointId)
{
    float hertz = 0.0f;
    float dampingRatio = 0.0f;
    b2Joint_GetConstraintTuning(jointId, &hertz, &dampingRatio);
    printf("%s %.9g %.9g %.9g %.9g\n", label, b2Joint_GetLinearSeparation(jointId),
           b2Joint_GetAngularSeparation(jointId), hertz, dampingRatio);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2Profile profile = b2World_GetProfile(worldId);
    printf("profile %.9g %.9g %.9g\n", profile.step, profile.solve, profile.sensors);
    b2World_SetRestitutionThreshold(worldId, -2.5f);
    b2World_SetHitEventThreshold(worldId, -3.5f);
    printf("thresholds %.9g %.9g\n", b2World_GetRestitutionThreshold(worldId), b2World_GetHitEventThreshold(worldId));
    b2World_EnableSpeculative(worldId, false);
    b2World_RebuildStaticTree(worldId);
    b2World_DumpMemoryStats(worldId);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){1.0f, -2.0f};
    bodyDef.rotation = b2MakeRot(0.2f);
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 2.0f;
    b2ShapeId circleId = b2CreateCircleShape(bodyId, &shapeDef, &(b2Circle){{0.25f, -0.1f}, 0.5f});
    b2MassData mass0 = b2Body_GetMassData(bodyId);
    printf("mass0 %.9g %.9g %.9g %.9g %d\n", mass0.mass, mass0.center.x, mass0.center.y,
           mass0.rotationalInertia, b2Body_GetShapeCount(bodyId));

    b2Body_SetTargetTransform(bodyId, (b2Transform){{3.0f, 1.0f}, b2MakeRot(0.8f)}, 0.5f);
    print_vec("targetLinear", b2Body_GetLinearVelocity(bodyId));
    printf("targetAngular %.9g %d\n", b2Body_GetAngularVelocity(bodyId), b2Body_IsAwake(bodyId) ? 1 : 0);

    b2Body_SetType(bodyId, b2_staticBody);
    printf("type0 %d %.9g %d\n", b2Body_GetType(bodyId), b2Body_GetMass(bodyId), b2World_GetAwakeBodyCount(worldId));
    b2Body_SetType(bodyId, b2_dynamicBody);
    printf("type1 %d %.9g %d\n", b2Body_GetType(bodyId), b2Body_GetMass(bodyId), b2World_GetAwakeBodyCount(worldId));

    b2Polygon box = b2MakeBox(0.25f, 0.2f);
    b2ShapeId boxId = b2CreatePolygonShape(bodyId, &shapeDef, &box);
    b2MassData mass1 = b2Body_GetMassData(bodyId);
    b2DestroyShape(boxId, false);
    b2MassData mass2 = b2Body_GetMassData(bodyId);
    b2Body_ApplyMassFromShapes(bodyId);
    b2MassData mass3 = b2Body_GetMassData(bodyId);
    printf("destroy %d %d %d %.9g %.9g %.9g\n", b2Shape_IsValid(boxId) ? 1 : 0,
           b2Shape_IsValid(circleId) ? 1 : 0, b2Body_GetShapeCount(bodyId), mass1.mass, mass2.mass, mass3.mass);

    b2BodyDef sensorBodyDef = b2DefaultBodyDef();
    b2BodyId sensorBody = b2CreateBody(worldId, &sensorBodyDef);
    b2ShapeDef sensorDef = b2DefaultShapeDef();
    sensorDef.isSensor = true;
    sensorDef.enableSensorEvents = true;
    b2ShapeId sensorId = b2CreateCircleShape(sensorBody, &sensorDef, &(b2Circle){{10.0f, 0.0f}, 2.0f});

    b2BodyDef visitorDef = b2DefaultBodyDef();
    visitorDef.type = b2_dynamicBody;
    visitorDef.position = (b2Vec2){10.5f, 0.0f};
    b2BodyId visitorBody = b2CreateBody(worldId, &visitorDef);
    shapeDef.enableSensorEvents = true;
    b2ShapeId visitorId = b2CreateCircleShape(visitorBody, &shapeDef, &(b2Circle){{0.0f, 0.0f}, 0.25f});
    b2World_Step(worldId, 1.0f / 60.0f, 1);
    b2ShapeId overlaps[4] = {0};
    int overlapCount = b2Shape_GetSensorOverlaps(sensorId, overlaps, 4);
    printf("sensor %d %d %d %d %d\n", b2Shape_GetSensorCapacity(sensorId), overlapCount,
           overlaps[0].index1, overlaps[0].generation, overlaps[0].index1 == visitorId.index1 ? 1 : 0);

    b2BodyDef jointBodyDef = b2DefaultBodyDef();
    jointBodyDef.type = b2_dynamicBody;
    jointBodyDef.position = (b2Vec2){0.0f, 0.0f};
    jointBodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId jointA = b2CreateBody(worldId, &jointBodyDef);
    jointBodyDef.position = (b2Vec2){2.0f, 3.0f};
    jointBodyDef.rotation = b2MakeRot(0.75f);
    b2BodyId jointB = b2CreateBody(worldId, &jointBodyDef);

    b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
    distanceDef.bodyIdA = jointA;
    distanceDef.bodyIdB = jointB;
    distanceDef.length = 2.0f;
    distanceDef.enableSpring = false;
    b2JointId distanceJoint = b2CreateDistanceJoint(worldId, &distanceDef);
    print_joint("distanceSep0", distanceJoint);
    b2Joint_SetConstraintTuning(distanceJoint, 12.0f, 0.7f);
    print_joint("distanceSep1", distanceJoint);

    b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
    prismaticDef.bodyIdA = jointA;
    prismaticDef.bodyIdB = jointB;
    prismaticDef.localAxisA = (b2Vec2){1.0f, 0.0f};
    prismaticDef.referenceAngle = 0.25f;
    prismaticDef.enableLimit = true;
    prismaticDef.lowerTranslation = -1.0f;
    prismaticDef.upperTranslation = 1.0f;
    b2JointId prismaticJoint = b2CreatePrismaticJoint(worldId, &prismaticDef);
    print_joint("prismaticSep", prismaticJoint);

    b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = jointA;
    revoluteDef.bodyIdB = jointB;
    revoluteDef.referenceAngle = 0.25f;
    revoluteDef.enableLimit = true;
    revoluteDef.lowerAngle = -0.2f;
    revoluteDef.upperAngle = 0.2f;
    b2JointId revoluteJoint = b2CreateRevoluteJoint(worldId, &revoluteDef);
    print_joint("revoluteSep", revoluteJoint);

    b2WeldJointDef weldDef = b2DefaultWeldJointDef();
    weldDef.bodyIdA = jointA;
    weldDef.bodyIdB = jointB;
    weldDef.referenceAngle = 0.25f;
    weldDef.linearHertz = 0.0f;
    weldDef.angularHertz = 0.0f;
    b2JointId weldJoint = b2CreateWeldJoint(worldId, &weldDef);
    print_joint("weldSep", weldJoint);

    b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
    wheelDef.bodyIdA = jointA;
    wheelDef.bodyIdB = jointB;
    wheelDef.localAxisA = (b2Vec2){1.0f, 0.0f};
    wheelDef.enableLimit = true;
    wheelDef.lowerTranslation = -1.0f;
    wheelDef.upperTranslation = 1.0f;
    b2JointId wheelJoint = b2CreateWheelJoint(worldId, &wheelDef);
    print_joint("wheelSep", wheelJoint);

    b2DestroyWorld(worldId);
    return 0;
}
