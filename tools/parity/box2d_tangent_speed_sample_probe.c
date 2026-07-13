// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>

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

            if (command == 'M' || command == 'L' || command == 'H' || command == 'V' || command == 'm' || command == 'l' ||
                command == 'h' || command == 'v')
            {
                ptr += 2;
            }

            if (command == 'z')
            {
                break;
            }
        }

        float x = 0.0f, y = 0.0f;
        switch (command)
        {
            case 'M':
            case 'L':
                sscanf(ptr, "%f,%f", &x, &y);
                currentPoint.x = x;
                currentPoint.y = y;
                break;
            case 'H':
                sscanf(ptr, "%f", &x);
                currentPoint.x = x;
                break;
            case 'V':
                sscanf(ptr, "%f", &y);
                currentPoint.y = y;
                break;
            case 'm':
            case 'l':
                sscanf(ptr, "%f,%f", &x, &y);
                currentPoint.x += x;
                currentPoint.y += y;
                break;
            case 'h':
                sscanf(ptr, "%f", &x);
                currentPoint.x += x;
                break;
            case 'v':
                sscanf(ptr, "%f", &y);
                currentPoint.y += y;
                break;
            default:
                break;
        }

        points[pointCount] = (b2Vec2){scale * (currentPoint.x + offset.x), -scale * (currentPoint.y + offset.y)};
        pointCount += 1;
        if (pointCount == capacity)
        {
            break;
        }

        while (*ptr != '\0' && isspace(*ptr) == 0)
        {
            ptr++;
        }

        while (isspace(*ptr))
        {
            ptr++;
        }
    }

    return pointCount;
}

static b2BodyId drop_ball(b2WorldId worldId)
{
    b2Circle circle = {b2Vec2_zero, 0.5f};

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){110.0f, -30.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.6f;
    shapeDef.material.rollingResistance = 0.3f;
    b2CreateCircleShape(bodyId, &shapeDef, &circle);
    return bodyId;
}

static void print_vec(b2Vec2 v)
{
    printf(" %.9g %.9g", v.x, v.y);
}

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 240;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    const char* path = "m 613.8334,185.20833 -42.33338,0 h -37.04166 l -34.39581,0 -29.10417,-2.64583 -26.45834,-7.9375 "
                       "-26.45833,-13.22917 -23.81251,-21.16666 h -13.22916 v 44.97916 H 68.791712 V 0 h -21.16671 v "
                       "206.375 l 566.208398,-1e-5 z";

    b2Vec2 points[20] = {0};
    int pointCount = 0;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2Vec2 offset = {-47.375002f, 0.25f};
        pointCount = parse_path(path, offset, points, 20, 0.2f);

        b2SurfaceMaterial materials[20] = {0};
        for (int i = 0; i < 20; ++i)
        {
            materials[i].friction = 0.6f;
        }

        materials[0].tangentSpeed = -10.0f;
        materials[0].customColor = b2_colorDarkBlue;
        materials[1].tangentSpeed = -20.0f;
        materials[1].customColor = b2_colorDarkCyan;
        materials[2].tangentSpeed = -30.0f;
        materials[2].customColor = b2_colorDarkGoldenRod;
        materials[3].tangentSpeed = -40.0f;
        materials[3].customColor = b2_colorDarkGray;
        materials[4].tangentSpeed = -50.0f;
        materials[4].customColor = b2_colorDarkGreen;
        materials[5].tangentSpeed = -60.0f;
        materials[5].customColor = b2_colorDarkKhaki;
        materials[6].tangentSpeed = -70.0f;
        materials[6].customColor = b2_colorDarkMagenta;

        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = pointCount;
        chainDef.isLoop = true;
        chainDef.materials = materials;
        chainDef.materialCount = pointCount;

        b2CreateChain(groundId, &chainDef);
    }

    b2BodyId bodies[200];
    int bodyCount = 0;
    for (int step = 0; step < stepCount; ++step)
    {
        if (step % 25 == 0 && bodyCount < 200)
        {
            bodies[bodyCount++] = drop_ball(worldId);
        }
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("tangentSpeed %d %d %d %d %d %d %d", pointCount, counters.bodyCount, counters.shapeCount, counters.contactCount,
           b2World_GetAwakeBodyCount(worldId), bodyCount, 10);
    for (int i = 0; i < pointCount; ++i)
    {
        print_vec(points[i]);
    }
    int sampleIndices[10] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    for (int i = 0; i < 10; ++i)
    {
        print_body(bodies[sampleIndices[i]]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
