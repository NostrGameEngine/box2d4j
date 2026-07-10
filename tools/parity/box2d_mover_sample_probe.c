// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <assert.h>
#include <ctype.h>
#include <float.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    PLANE_CAPACITY = 8,
    BRIDGE_COUNT = 50,
    STATIC_BIT = 0x0001,
    MOVER_BIT = 0x0002,
    DYNAMIC_BIT = 0x0004,
    DEBRIS_BIT = 0x0008,
    ALL_BITS = ~0u,
};

typedef struct ShapeUserData
{
    float maxPush;
    bool clipVelocity;
} ShapeUserData;

typedef struct CastResult
{
    b2Vec2 point;
    b2Vec2 normal;
    b2BodyId bodyId;
    float fraction;
    bool hit;
} CastResult;

typedef struct Scene
{
    b2WorldId worldId;
    b2Transform transform;
    b2Vec2 velocity;
    b2Capsule capsule;
    b2BodyId elevatorId;
    b2BodyId ballBodyId;
    b2ShapeId ballId;
    b2BodyId bridgeBodies[BRIDGE_COUNT];
    ShapeUserData friendlyShape;
    ShapeUserData elevatorShape;
    b2CollisionPlane planes[PLANE_CAPACITY];
    CastResult castResult;
    int planeCount;
    int totalIterations;
    float maxSpeed;
    float minSpeed;
    float stopSpeed;
    float accelerate;
    float airSteer;
    float friction;
    float gravity;
    float pogoHertz;
    float pogoDampingRatio;
    float pogoVelocity;
    float time;
    bool onGround;
} Scene;

static const b2Vec2 elevatorBase = {112.0f, 10.0f};
static const float elevatorAmplitude = 4.0f;

static const char* path1 =
    "M 2.6458333,201.08333 H 293.68751 v -47.625 h -2.64584 l -10.58333,7.9375 -13.22916,7.9375 -13.24648,5.29167 "
    "-31.73269,7.9375 -21.16667,2.64583 -23.8125,10.58333 H 142.875 v -5.29167 h -5.29166 v 5.29167 H 119.0625 v "
    "-2.64583 h -2.64583 v -2.64584 h -2.64584 v -2.64583 H 111.125 v -2.64583 H 84.666668 v -2.64583 h -5.291666 v "
    "-2.64584 h -5.291667 v -2.64583 H 68.791668 V 174.625 h -5.291666 v -2.64584 H 52.916669 L 39.6875,177.27083 H "
    "34.395833 L 23.8125,185.20833 H 15.875 L 5.2916669,187.85416 V 153.45833 H 2.6458333 v 47.625";

static const char* path2 =
    "M 2.6458333,201.08333 H 293.68751 l 0,-23.8125 h -23.8125 l 21.16667,21.16667 h -23.8125 l -39.68751,-13.22917 "
    "-26.45833,7.9375 -23.8125,2.64583 h -13.22917 l -0.0575,2.64584 h -5.29166 v -2.64583 l -7.86855,-1e-5 "
    "-0.0114,-2.64583 h -2.64583 l -2.64583,2.64584 h -7.9375 l -2.64584,2.64583 -2.58891,-2.64584 h -13.28609 v "
    "-2.64583 h -2.64583 v -2.64584 l -5.29167,1e-5 v -2.64583 h -2.64583 v -2.64583 l -5.29167,-1e-5 v -2.64583 h "
    "-2.64583 v -2.64584 h -5.291667 v -2.64583 H 92.60417 V 174.625 h -5.291667 v -2.64584 l -34.395835,1e-5 "
    "-7.9375,-2.64584 -7.9375,-2.64583 -5.291667,-5.29167 H 21.166667 L 13.229167,158.75 5.2916668,153.45833 H "
    "2.6458334 l -10e-8,47.625";

static int parse_path(const char* svgPath, b2Vec2 offset, b2Vec2* points, int capacity, float scale)
{
    int pointCount = 0;
    b2Vec2 currentPoint = {0};
    const char* ptr = svgPath;
    char command = *ptr;

    while (*ptr != '\0')
    {
        if (isdigit(*ptr) == 0 && *ptr != '-')
        {
            command = *ptr;
            if (command == 'M' || command == 'L' || command == 'H' || command == 'V' ||
                command == 'm' || command == 'l' || command == 'h' || command == 'v')
            {
                ptr += 2;
            }
            if (command == 'z')
            {
                break;
            }
        }

        float x = 0.0f;
        float y = 0.0f;
        switch (command)
        {
            case 'M':
            case 'L':
                assert(sscanf(ptr, "%f,%f", &x, &y) == 2);
                currentPoint = (b2Vec2){x, y};
                break;
            case 'H':
                assert(sscanf(ptr, "%f", &x) == 1);
                currentPoint.x = x;
                break;
            case 'V':
                assert(sscanf(ptr, "%f", &y) == 1);
                currentPoint.y = y;
                break;
            case 'm':
            case 'l':
                assert(sscanf(ptr, "%f,%f", &x, &y) == 2);
                currentPoint.x += x;
                currentPoint.y += y;
                break;
            case 'h':
                assert(sscanf(ptr, "%f", &x) == 1);
                currentPoint.x += x;
                break;
            case 'v':
                assert(sscanf(ptr, "%f", &y) == 1);
                currentPoint.y += y;
                break;
            default:
                assert(false);
        }

        points[pointCount++] = (b2Vec2){scale * (currentPoint.x + offset.x),
                                       -scale * (currentPoint.y + offset.y)};
        if (pointCount == capacity)
        {
            break;
        }
        while (*ptr != '\0' && isspace(*ptr) == 0)
        {
            ptr += 1;
        }
        while (isspace(*ptr))
        {
            ptr += 1;
        }
    }
    return pointCount;
}

static float cast_callback(b2ShapeId shapeId, b2Vec2 point, b2Vec2 normal, float fraction, void* context)
{
    CastResult* result = context;
    result->point = point;
    result->normal = normal;
    result->bodyId = b2Shape_GetBody(shapeId);
    result->fraction = fraction;
    result->hit = true;
    return fraction;
}

static bool plane_result_callback(b2ShapeId shapeId, const b2PlaneResult* planeResult, void* context)
{
    Scene* scene = context;
    float pushLimit = FLT_MAX;
    bool clipVelocity = true;
    ShapeUserData* userData = b2Shape_GetUserData(shapeId);
    if (userData != NULL)
    {
        pushLimit = userData->maxPush;
        clipVelocity = userData->clipVelocity;
    }

    if (scene->planeCount < PLANE_CAPACITY)
    {
        scene->planes[scene->planeCount++] =
            (b2CollisionPlane){planeResult->plane, pushLimit, 0.0f, clipVelocity};
    }
    return true;
}

static b2QueryFilter query_filter(uint64_t categoryBits, uint64_t maskBits)
{
    return (b2QueryFilter){categoryBits, maskBits};
}

static void create_scene(Scene* scene, b2WorldId worldId)
{
    *scene = (Scene){0};
    scene->worldId = worldId;
    scene->transform = (b2Transform){{2.0f, 8.0f}, b2Rot_identity};
    scene->capsule = (b2Capsule){{0.0f, -0.5f}, {0.0f, 0.5f}, 0.3f};
    scene->maxSpeed = 6.0f;
    scene->minSpeed = 0.1f;
    scene->stopSpeed = 3.0f;
    scene->accelerate = 20.0f;
    scene->airSteer = 0.2f;
    scene->friction = 8.0f;
    scene->gravity = 30.0f;
    scene->pogoHertz = 5.0f;
    scene->pogoDampingRatio = 0.8f;

    b2Vec2 points[64];
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId1 = b2CreateBody(worldId, &bodyDef);
    int pointCount = parse_path(path1, (b2Vec2){-50.0f, -200.0f}, points, 64, 0.2f);
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = pointCount;
    chainDef.isLoop = true;
    b2CreateChain(groundId1, &chainDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){98.0f, 0.0f};
    b2BodyId groundId2 = b2CreateBody(worldId, &bodyDef);
    pointCount = parse_path(path2, (b2Vec2){0.0f, -200.0f}, points, 64, 0.2f);
    chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = pointCount;
    chainDef.isLoop = true;
    b2CreateChain(groundId2, &chainDef);

    b2Polygon box = b2MakeBox(0.5f, 0.125f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.maxMotorTorque = 10.0f;
    jointDef.enableMotor = true;
    jointDef.hertz = 3.0f;
    jointDef.dampingRatio = 0.8f;
    jointDef.enableSpring = true;

    float xBase = 48.7f;
    float yBase = 9.2f;
    b2BodyId previousBodyId = groundId1;
    for (int i = 0; i < BRIDGE_COUNT; ++i)
    {
        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){xBase + 0.5f + i, yBase};
        bodyDef.angularDamping = 0.2f;
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        scene->bridgeBodies[i] = bodyId;
        b2CreatePolygonShape(bodyId, &shapeDef, &box);

        b2Vec2 pivot = {xBase + i, yBase};
        jointDef.bodyIdA = previousBodyId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        b2CreateRevoluteJoint(worldId, &jointDef);
        previousBodyId = bodyId;
    }

    b2Vec2 pivot = {xBase + BRIDGE_COUNT, yBase};
    jointDef.bodyIdA = previousBodyId;
    jointDef.bodyIdB = groundId2;
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
    b2CreateRevoluteJoint(worldId, &jointDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){32.0f, 4.5f};
    shapeDef = b2DefaultShapeDef();
    scene->friendlyShape = (ShapeUserData){0.025f, false};
    shapeDef.filter = (b2Filter){MOVER_BIT, ALL_BITS, 0};
    shapeDef.userData = &scene->friendlyShape;
    b2BodyId friendlyBodyId = b2CreateBody(worldId, &bodyDef);
    b2CreateCapsuleShape(friendlyBodyId, &shapeDef, &scene->capsule);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){7.0f, 7.0f};
    scene->ballBodyId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.filter = (b2Filter){DEBRIS_BIT, ALL_BITS, 0};
    shapeDef.material.restitution = 0.7f;
    shapeDef.material.rollingResistance = 0.2f;
    b2Circle circle = {b2Vec2_zero, 0.3f};
    scene->ballId = b2CreateCircleShape(scene->ballBodyId, &shapeDef, &circle);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_kinematicBody;
    bodyDef.position = (b2Vec2){elevatorBase.x, elevatorBase.y - elevatorAmplitude};
    scene->elevatorId = b2CreateBody(worldId, &bodyDef);
    scene->elevatorShape = (ShapeUserData){0.1f, true};
    shapeDef = b2DefaultShapeDef();
    shapeDef.filter = (b2Filter){DYNAMIC_BIT, ALL_BITS, 0};
    shapeDef.userData = &scene->elevatorShape;
    box = b2MakeBox(2.0f, 0.1f);
    b2CreatePolygonShape(scene->elevatorId, &shapeDef, &box);
}

static void solve_move(Scene* scene, float timeStep, float throttle)
{
    float speed = b2Length(scene->velocity);
    if (speed < scene->minSpeed)
    {
        scene->velocity = b2Vec2_zero;
    }
    else if (scene->onGround)
    {
        float control = speed < scene->stopSpeed ? scene->stopSpeed : speed;
        float drop = control * scene->friction * timeStep;
        float newSpeed = b2MaxFloat(0.0f, speed - drop);
        scene->velocity = b2MulSV(newSpeed / speed, scene->velocity);
    }

    b2Vec2 desiredVelocity = {scene->maxSpeed * throttle, 0.0f};
    float desiredSpeed;
    b2Vec2 desiredDirection = b2GetLengthAndNormalize(&desiredSpeed, desiredVelocity);
    if (desiredSpeed > scene->maxSpeed)
    {
        desiredSpeed = scene->maxSpeed;
    }
    if (scene->onGround)
    {
        scene->velocity.y = 0.0f;
    }

    float currentSpeed = b2Dot(scene->velocity, desiredDirection);
    float addSpeed = desiredSpeed - currentSpeed;
    if (addSpeed > 0.0f)
    {
        float steer = scene->onGround ? 1.0f : scene->airSteer;
        float accelSpeed = steer * scene->accelerate * scene->maxSpeed * timeStep;
        if (accelSpeed > addSpeed)
        {
            accelSpeed = addSpeed;
        }
        scene->velocity = b2MulAdd(scene->velocity, accelSpeed, desiredDirection);
    }
    scene->velocity.y -= scene->gravity * timeStep;

    float pogoRestLength = 3.0f * scene->capsule.radius;
    float rayLength = pogoRestLength + scene->capsule.radius;
    b2Vec2 origin = b2TransformPoint(scene->transform, scene->capsule.center1);
    b2Vec2 segmentOffset = {0.75f * scene->capsule.radius, 0.0f};
    b2Vec2 segmentPoints[2] = {b2Sub(origin, segmentOffset), b2Add(origin, segmentOffset)};
    b2ShapeProxy proxy = b2MakeProxy(segmentPoints, 2, 0.0f);
    b2Vec2 translation = {0.0f, -rayLength};
    b2QueryFilter pogoFilter = query_filter(MOVER_BIT, STATIC_BIT | DYNAMIC_BIT);
    scene->castResult = (CastResult){0};
    b2World_CastShape(scene->worldId, &proxy, translation, pogoFilter, cast_callback, &scene->castResult);

    if (scene->onGround == false)
    {
        scene->onGround = scene->castResult.hit && scene->velocity.y <= 0.01f;
    }
    else
    {
        scene->onGround = scene->castResult.hit;
    }

    if (scene->castResult.hit == false)
    {
        scene->pogoVelocity = 0.0f;
    }
    else
    {
        float pogoCurrentLength = scene->castResult.fraction * rayLength;
        float offset = pogoCurrentLength - pogoRestLength;
        scene->pogoVelocity = b2SpringDamper(scene->pogoHertz, scene->pogoDampingRatio,
                                             offset, scene->pogoVelocity, timeStep);
        b2Body_ApplyForce(scene->castResult.bodyId, (b2Vec2){0.0f, -50.0f},
                          scene->castResult.point, true);
    }

    b2Vec2 target = b2MulAdd(b2MulAdd(scene->transform.p, timeStep, scene->velocity),
                             timeStep * scene->pogoVelocity, (b2Vec2){0.0f, 1.0f});
    b2QueryFilter collideFilter = query_filter(MOVER_BIT, STATIC_BIT | DYNAMIC_BIT | MOVER_BIT);
    b2QueryFilter castFilter = query_filter(MOVER_BIT, STATIC_BIT | DYNAMIC_BIT);
    scene->totalIterations = 0;
    float tolerance = 0.01f;

    for (int iteration = 0; iteration < 5; ++iteration)
    {
        scene->planeCount = 0;
        b2Capsule mover = {
            b2TransformPoint(scene->transform, scene->capsule.center1),
            b2TransformPoint(scene->transform, scene->capsule.center2),
            scene->capsule.radius,
        };
        b2World_CollideMover(scene->worldId, &mover, collideFilter, plane_result_callback, scene);
        b2PlaneSolverResult result = b2SolvePlanes(b2Sub(target, scene->transform.p),
                                                   scene->planes, scene->planeCount);
        scene->totalIterations += result.iterationCount;
        float fraction = b2World_CastMover(scene->worldId, &mover, result.translation, castFilter);
        b2Vec2 delta = b2MulSV(fraction, result.translation);
        scene->transform.p = b2Add(scene->transform.p, delta);
        if (b2LengthSquared(delta) < tolerance * tolerance)
        {
            break;
        }
    }
    scene->velocity = b2ClipVector(scene->velocity, scene->planes, scene->planeCount);
}

static void print_body(b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d",
           transform.p.x, transform.p.y, transform.q.c, transform.q.s,
           velocity.x, velocity.y, b2Body_GetAngularVelocity(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    Scene scene;
    create_scene(&scene, worldId);
    float timeStep = 1.0f / 60.0f;

    for (int step = 0; step < stepCount; ++step)
    {
        b2Vec2 point = {
            elevatorBase.x,
            elevatorAmplitude * cosf(scene.time + B2_PI) + elevatorBase.y,
        };
        b2Transform target = {point, b2Rot_identity};
        b2Body_SetTargetTransform(scene.elevatorId, target, timeStep);
        scene.time += timeStep;
        b2World_Step(worldId, timeStep, 4);
        solve_move(&scene, timeStep, 0.0f);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("mover %d %d %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g %d %.9g %d %.9g",
           counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
           b2World_GetAwakeBodyCount(worldId), scene.transform.p.x, scene.transform.p.y,
           scene.transform.q.c, scene.transform.q.s, scene.velocity.x, scene.velocity.y,
           scene.onGround ? 1 : 0, scene.pogoVelocity, scene.totalIterations, scene.time);
    printf(" %d %.9g %.9g %.9g %.9g %.9g %d", scene.castResult.hit ? 1 : 0,
           scene.castResult.fraction, scene.castResult.point.x, scene.castResult.point.y,
           scene.castResult.normal.x, scene.castResult.normal.y,
           scene.castResult.hit && b2Body_IsValid(scene.castResult.bodyId) ? 1 : 0);
    printf(" %d", scene.planeCount);
    for (int i = 0; i < scene.planeCount; ++i)
    {
        b2CollisionPlane* plane = scene.planes + i;
        printf(" %.9g %.9g %.9g %.9g %.9g %d", plane->plane.normal.x, plane->plane.normal.y,
               plane->plane.offset, plane->pushLimit, plane->push, plane->clipVelocity ? 1 : 0);
    }
    print_body(scene.ballBodyId);
    print_body(scene.elevatorId);
    print_body(scene.bridgeBodies[0]);
    print_body(scene.bridgeBodies[24]);
    print_body(scene.bridgeBodies[49]);
    printf(" %d\n", b2Shape_IsValid(scene.ballId) ? 1 : 0);

    b2DestroyWorld(worldId);
    return 0;
}
