// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "random.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    COUNT = 20
};

typedef struct BodyUserData
{
    int index;
} BodyUserData;

typedef struct Scene
{
    b2WorldId worldId;
    b2BodyId playerId;
    b2ShapeId coreShapeId;
    b2BodyId debrisIds[COUNT];
    BodyUserData userData[COUNT];
    float force;
    float wait;
    int spawnTotal;
    int attachTotal;
    int destroyShapeTotal;
    int beginTotal;
    int endTotal;
    int hitTotal;
    int contactPointTotal;
    int normalChecksum;
    int impulseChecksum;
} Scene;

static int round1000(float value)
{
    return (int)(1000.0f * value + (value >= 0.0f ? 0.5f : -0.5f));
}

static bool contains_shape(b2ShapeId* shapeIds, int count, b2ShapeId shapeId)
{
    for (int i = 0; i < count; ++i)
    {
        if (B2_ID_EQUALS(shapeIds[i], shapeId))
        {
            return true;
        }
    }
    return false;
}

static void spawn_debris(Scene* scene)
{
    int index = -1;
    for (int i = 0; i < COUNT; ++i)
    {
        if (B2_IS_NULL(scene->debrisIds[i]))
        {
            index = i;
            break;
        }
    }

    if (index == -1)
    {
        return;
    }

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){RandomFloatRange(-38.0f, 38.0f), RandomFloatRange(-38.0f, 38.0f)};
    bodyDef.rotation = b2MakeRot(RandomFloatRange(-B2_PI, B2_PI));
    bodyDef.linearVelocity = (b2Vec2){RandomFloatRange(-5.0f, 5.0f), RandomFloatRange(-5.0f, 5.0f)};
    bodyDef.angularVelocity = RandomFloatRange(-1.0f, 1.0f);
    bodyDef.gravityScale = 0.0f;
    bodyDef.userData = scene->userData + index;
    scene->debrisIds[index] = b2CreateBody(scene->worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.restitution = 0.8f;
    shapeDef.enableContactEvents = false;
    if ((index + 1) % 3 == 0)
    {
        b2Circle circle = {{0.0f, 0.0f}, 0.5f};
        b2CreateCircleShape(scene->debrisIds[index], &shapeDef, &circle);
    }
    else if ((index + 1) % 2 == 0)
    {
        b2Capsule capsule = {{0.0f, -0.25f}, {0.0f, 0.25f}, 0.25f};
        b2CreateCapsuleShape(scene->debrisIds[index], &shapeDef, &capsule);
    }
    else
    {
        b2Polygon box = b2MakeBox(0.4f, 0.6f);
        b2CreatePolygonShape(scene->debrisIds[index], &shapeDef, &box);
    }
    scene->spawnTotal += 1;
}

static Scene create_scene(b2WorldId worldId)
{
    Scene scene = {0};
    scene.worldId = worldId;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2Vec2 points[] = {{40.0f, -40.0f}, {-40.0f, -40.0f}, {-40.0f, 40.0f}, {40.0f, 40.0f}};
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.count = 4;
    chainDef.points = points;
    chainDef.isLoop = true;
    b2CreateChain(groundId, &chainDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.gravityScale = 0.0f;
    bodyDef.linearDamping = 0.5f;
    bodyDef.angularDamping = 0.5f;
    bodyDef.isBullet = true;
    scene.playerId = b2CreateBody(worldId, &bodyDef);

    b2Circle circle = {{0.0f, 0.0f}, 1.0f};
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableContactEvents = true;
    scene.coreShapeId = b2CreateCircleShape(scene.playerId, &shapeDef, &circle);

    for (int i = 0; i < COUNT; ++i)
    {
        scene.debrisIds[i] = b2_nullBodyId;
        scene.userData[i].index = i;
    }

    scene.wait = 0.5f;
    scene.force = 200.0f;
    return scene;
}

static void accumulate_contact_data(Scene* scene, b2ContactBeginTouchEvent event)
{
    int capacityA = b2Shape_GetContactCapacity(event.shapeIdA);
    int capacityB = b2Shape_GetContactCapacity(event.shapeIdB);
    b2ShapeId queryShape = capacityA < capacityB ? event.shapeIdA : event.shapeIdB;
    b2ShapeId otherShape = capacityA < capacityB ? event.shapeIdB : event.shapeIdA;
    int capacity = capacityA < capacityB ? capacityA : capacityB;
    b2ContactData contactData[64];
    int count = b2Shape_GetContactData(queryShape, contactData, capacity);
    for (int i = 0; i < count; ++i)
    {
        if (B2_ID_EQUALS(contactData[i].shapeIdA, otherShape) || B2_ID_EQUALS(contactData[i].shapeIdB, otherShape))
        {
            b2Manifold manifold = contactData[i].manifold;
            scene->contactPointTotal += manifold.pointCount;
            scene->normalChecksum += round1000(manifold.normal.x) + round1000(manifold.normal.y);
            for (int k = 0; k < manifold.pointCount; ++k)
            {
                scene->impulseChecksum += round1000(manifold.points[k].totalNormalImpulse);
            }
        }
    }
}

static void attach_debris(Scene* scene, int index)
{
    b2BodyId debrisId = scene->debrisIds[index];
    if (B2_IS_NULL(debrisId))
    {
        return;
    }

    b2Transform playerTransform = b2Body_GetTransform(scene->playerId);
    b2Transform debrisTransform = b2Body_GetTransform(debrisId);
    b2Transform relativeTransform = b2InvMulTransforms(playerTransform, debrisTransform);

    int shapeCount = b2Body_GetShapeCount(debrisId);
    if (shapeCount == 0)
    {
        return;
    }

    b2ShapeId shapeId;
    b2Body_GetShapes(debrisId, &shapeId, 1);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableContactEvents = true;
    b2ShapeType type = b2Shape_GetType(shapeId);
    if (type == b2_circleShape)
    {
        b2Circle circle = b2Shape_GetCircle(shapeId);
        circle.center = b2TransformPoint(relativeTransform, circle.center);
        b2CreateCircleShape(scene->playerId, &shapeDef, &circle);
    }
    else if (type == b2_capsuleShape)
    {
        b2Capsule capsule = b2Shape_GetCapsule(shapeId);
        capsule.center1 = b2TransformPoint(relativeTransform, capsule.center1);
        capsule.center2 = b2TransformPoint(relativeTransform, capsule.center2);
        b2CreateCapsuleShape(scene->playerId, &shapeDef, &capsule);
    }
    else if (type == b2_polygonShape)
    {
        b2Polygon originalPolygon = b2Shape_GetPolygon(shapeId);
        b2Polygon polygon = b2TransformPolygon(relativeTransform, &originalPolygon);
        b2CreatePolygonShape(scene->playerId, &shapeDef, &polygon);
    }

    b2DestroyBody(debrisId);
    scene->debrisIds[index] = b2_nullBodyId;
    scene->attachTotal += 1;
}

static void process_contact_events(Scene* scene)
{
    int debrisToAttach[COUNT] = {0};
    b2ShapeId shapesToDestroy[COUNT] = {b2_nullShapeId};
    int attachCount = 0;
    int destroyCount = 0;

    b2ContactEvents events = b2World_GetContactEvents(scene->worldId);
    scene->beginTotal += events.beginCount;
    scene->endTotal += events.endCount;
    scene->hitTotal += events.hitCount;

    for (int i = 0; i < events.beginCount; ++i)
    {
        b2ContactBeginTouchEvent event = events.beginEvents[i];
        b2BodyId bodyIdA = b2Shape_GetBody(event.shapeIdA);
        b2BodyId bodyIdB = b2Shape_GetBody(event.shapeIdB);
        accumulate_contact_data(scene, event);

        if (B2_ID_EQUALS(bodyIdA, scene->playerId))
        {
            BodyUserData* userDataB = b2Body_GetUserData(bodyIdB);
            if (userDataB == NULL)
            {
                if (B2_ID_EQUALS(event.shapeIdA, scene->coreShapeId) == false && destroyCount < COUNT
                    && contains_shape(shapesToDestroy, destroyCount, event.shapeIdA) == false)
                {
                    shapesToDestroy[destroyCount++] = event.shapeIdA;
                }
            }
            else if (attachCount < COUNT)
            {
                debrisToAttach[attachCount++] = userDataB->index;
            }
        }
        else
        {
            BodyUserData* userDataA = b2Body_GetUserData(bodyIdA);
            if (userDataA == NULL)
            {
                if (B2_ID_EQUALS(event.shapeIdB, scene->coreShapeId) == false && destroyCount < COUNT
                    && contains_shape(shapesToDestroy, destroyCount, event.shapeIdB) == false)
                {
                    shapesToDestroy[destroyCount++] = event.shapeIdB;
                }
            }
            else if (attachCount < COUNT)
            {
                debrisToAttach[attachCount++] = userDataA->index;
            }
        }
    }

    for (int i = 0; i < attachCount; ++i)
    {
        attach_debris(scene, debrisToAttach[i]);
    }

    for (int i = 0; i < destroyCount; ++i)
    {
        b2DestroyShape(shapesToDestroy[i], false);
        scene->destroyShapeTotal += 1;
    }
    if (destroyCount > 0)
    {
        b2Body_ApplyMassFromShapes(scene->playerId);
    }
}

static void print_body(int index, b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
           index,
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetShapeCount(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    g_randomSeed = RAND_SEED;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    Scene scene = create_scene(worldId);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        process_contact_events(&scene);

        scene.wait -= 1.0f / 60.0f;
        if (scene.wait < 0.0f)
        {
            spawn_debris(&scene);
            scene.wait += 0.5f;
        }
    }

    int activeDebrisCount = 0;
    uint32_t activeMask = 0;
    for (int i = 0; i < COUNT; ++i)
    {
        if (B2_IS_NON_NULL(scene.debrisIds[i]))
        {
            activeDebrisCount += 1;
            activeMask |= 1u << i;
        }
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("contactEvent %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %u %.9g",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           scene.spawnTotal,
           scene.attachTotal,
           scene.destroyShapeTotal,
           scene.beginTotal,
           scene.endTotal,
           scene.hitTotal,
           scene.contactPointTotal,
           scene.normalChecksum,
           scene.impulseChecksum,
           activeDebrisCount,
           activeMask,
           scene.wait);
    print_body(-1, scene.playerId);
    printf(" %d", activeDebrisCount);
    for (int i = 0; i < COUNT; ++i)
    {
        if (B2_IS_NON_NULL(scene.debrisIds[i]))
        {
            print_body(i, scene.debrisIds[i]);
        }
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
