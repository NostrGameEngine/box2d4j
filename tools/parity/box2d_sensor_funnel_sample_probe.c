// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "human.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

enum
{
    HUMAN = 2,
    DEFAULT_STEP_COUNT = 88,
    MAX_COUNT = 32,
    BONE_COUNT = 11
};

typedef struct Scene
{
    b2WorldId worldId;
    Human humans[MAX_COUNT];
    bool isSpawned[MAX_COUNT];
    int createdTotal;
    int destroyedTotal;
    int beginTotal;
    int endTotal;
    int lastBeginCount;
    float wait;
    float side;
} Scene;

static void create_element(Scene* scene)
{
    int index = -1;
    for (int i = 0; i < MAX_COUNT; ++i)
    {
        if (scene->isSpawned[i] == false)
        {
            index = i;
            break;
        }
    }

    if (index == -1)
    {
        return;
    }

    Human* human = scene->humans + index;
    b2Vec2 center = {scene->side, 29.5f};
    CreateHuman(human, scene->worldId, center, 2.0f, 0.05f, 6.0f, 0.5f, index + 1, human, true);
    Human_EnableSensorEvents(human, true);

    scene->isSpawned[index] = true;
    scene->createdTotal += 1;
    scene->side = -scene->side;
}

static void destroy_element(Scene* scene, int index)
{
    if (scene->isSpawned[index] == false)
    {
        return;
    }

    DestroyHuman(scene->humans + index);
    memset(scene->humans + index, 0, sizeof(Human));
    scene->isSpawned[index] = false;
    scene->destroyedTotal += 1;
}

static Scene create_scene(b2WorldId worldId)
{
    Scene scene = {0};
    scene.worldId = worldId;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2Vec2 points[] = {
        {-16.8672504f, 31.088623f},
        {16.8672485f, 31.088623f},
        {16.8672485f, 17.1978741f},
        {8.26824951f, 11.906374f},
        {16.8672485f, 11.906374f},
        {16.8672485f, -0.661376953f},
        {8.26824951f, -5.953125f},
        {16.8672485f, -5.953125f},
        {16.8672485f, -13.229126f},
        {3.63799858f, -23.151123f},
        {3.63799858f, -31.088623f},
        {-3.63800049f, -31.088623f},
        {-3.63800049f, -23.151123f},
        {-16.8672504f, -13.229126f},
        {-16.8672504f, -5.953125f},
        {-8.26825142f, -5.953125f},
        {-16.8672504f, -0.661376953f},
        {-16.8672504f, 11.906374f},
        {-8.26825142f, 11.906374f},
        {-16.8672504f, 17.1978741f},
    };

    b2SurfaceMaterial material = {0};
    material.friction = 0.2f;
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = sizeof(points) / sizeof(points[0]);
    chainDef.isLoop = true;
    chainDef.materials = &material;
    chainDef.materialCount = 1;
    b2CreateChain(groundId, &chainDef);

    float sign = 1.0f;
    float y = 14.0f;
    for (int i = 0; i < 3; ++i)
    {
        bodyDef.position = (b2Vec2){0.0f, y};
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(6.0f, 0.5f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.1f;
        shapeDef.material.restitution = 1.0f;
        shapeDef.density = 1.0f;
        b2CreatePolygonShape(bodyId, &shapeDef, &box);

        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = groundId;
        revoluteDef.bodyIdB = bodyId;
        revoluteDef.localAnchorA = bodyDef.position;
        revoluteDef.localAnchorB = b2Vec2_zero;
        revoluteDef.maxMotorTorque = 200.0f;
        revoluteDef.motorSpeed = 2.0f * sign;
        revoluteDef.enableMotor = true;
        b2CreateRevoluteJoint(worldId, &revoluteDef);

        y -= 14.0f;
        sign = -sign;
    }

    b2Polygon box = b2MakeOffsetBox(4.0f, 1.0f, (b2Vec2){0.0f, -30.5f}, b2Rot_identity);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    scene.wait = 0.5f;
    scene.side = -15.0f;
    return scene;
}

static void print_body(int elementIndex, int boneIndex, b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
           elementIndex,
           boneIndex,
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

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    Scene scene = create_scene(worldId);
    create_element(&scene);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);

        bool deferredDestruction[MAX_COUNT] = {0};
        b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
        scene.lastBeginCount = sensorEvents.beginCount;
        scene.beginTotal += sensorEvents.beginCount;
        scene.endTotal += sensorEvents.endCount;
        for (int i = 0; i < sensorEvents.beginCount; ++i)
        {
            b2SensorBeginTouchEvent event = sensorEvents.beginEvents[i];
            b2BodyId bodyId = b2Shape_GetBody(event.visitorShapeId);
            Human* human = b2Body_GetUserData(bodyId);
            if (human != NULL)
            {
                int index = (int)(human - scene.humans);
                if (0 <= index && index < MAX_COUNT)
                {
                    deferredDestruction[index] = true;
                }
            }
        }

        for (int i = 0; i < MAX_COUNT; ++i)
        {
            if (deferredDestruction[i])
            {
                destroy_element(&scene, i);
            }
        }

        scene.wait -= 1.0f / 60.0f;
        if (scene.wait < 0.0f)
        {
            create_element(&scene);
            scene.wait += 0.5f;
        }
    }

    uint32_t activeMask = 0;
    int activeCount = 0;
    int bodyStateCount = 0;
    for (int i = 0; i < MAX_COUNT; ++i)
    {
        if (scene.isSpawned[i])
        {
            activeCount += 1;
            activeMask |= 1u << i;
            bodyStateCount += BONE_COUNT;
        }
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("sensorFunnel %d %d %d %d %d %d %d %d %d %u %d %d %d %.9g %.9g %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           HUMAN,
           scene.createdTotal,
           scene.destroyedTotal,
           activeCount,
           activeMask,
           scene.beginTotal,
           scene.endTotal,
           scene.lastBeginCount,
           scene.wait,
           scene.side,
           bodyStateCount);
    for (int i = 0; i < MAX_COUNT; ++i)
    {
        if (scene.isSpawned[i])
        {
            Human* human = scene.humans + i;
            for (int boneIndex = 0; boneIndex < BONE_COUNT; ++boneIndex)
            {
                print_body(i, boneIndex, human->bones[boneIndex].bodyId);
            }
        }
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
