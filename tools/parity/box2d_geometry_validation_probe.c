// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <math.h>
#include <setjmp.h>
#include <stdio.h>

static jmp_buf assertionJump;

static int catch_assertion(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    longjmp(assertionJump, 1);
    return 0;
}

static void run_case(int index)
{
    b2Polygon empty = {0};
    b2Polygon degenerate = {0};
    degenerate.count = 3;
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2RayCastInput ray = {{0.0f, 0.0f}, {1.0f, 0.0f}, 1.0f};
    b2Circle circle = {{0.0f, 0.0f}, 1.0f};
    b2Capsule capsule = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 0.5f};
    b2Segment segment = {{-1.0f, 0.0f}, {1.0f, 0.0f}};
    switch (index)
    {
        case 0: (void)b2MakeBox(0.0f, 1.0f); break;
        case 1: (void)b2MakeBox(1.0f, NAN); break;
        case 2: (void)b2MakeRoundedBox(1.0f, 1.0f, -1.0f); break;
        case 3: (void)b2MakeRoundedBox(1.0f, 1.0f, INFINITY); break;
        case 4: (void)b2MakeOffsetRoundedBox(1.0f, 1.0f, b2Vec2_zero, b2Rot_identity, -1.0f); break;
        case 5: (void)b2MakeOffsetRoundedBox(1.0f, 1.0f, b2Vec2_zero, b2Rot_identity, NAN); break;
        case 6: (void)b2ComputePolygonMass(&empty, 1.0f); break;
        case 7: (void)b2ComputePolygonMass(&degenerate, 1.0f); break;
        case 8: (void)b2ComputePolygonAABB(&empty, b2Transform_identity); break;
        case 9: ray.maxFraction = -1.0f; (void)b2RayCastCircle(&ray, &circle); break;
        case 10: ray.origin.x = NAN; (void)b2RayCastCapsule(&ray, &capsule); break;
        case 11: ray.maxFraction = 100000.0f; (void)b2RayCastPolygon(&ray, &box); break;
        case 12: ray.maxFraction = -1.0f; (void)b2RayCastSegment(&ray, &segment, false); break;
        case 13: (void)b2MakeOffsetBox(0.0f, -1.0f, b2Vec2_zero, b2Rot_identity); break;
        case 14: (void)b2RayCastPolygon(&ray, &box); break;
        case 15:
        {
            b2Capsule a = {{0.0f, 0.0f}, {0.0f, 0.0f}, 0.5f};
            b2Capsule b = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 0.5f};
            (void)b2CollideCapsules(&a, b2Transform_identity, &b, b2Transform_identity);
            break;
        }
        case 16:
        {
            b2Capsule a = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 0.5f};
            b2Capsule b = {{0.0f, 0.0f}, {0.0f, 0.0f}, 0.5f};
            (void)b2CollideCapsules(&a, b2Transform_identity, &b, b2Transform_identity);
            break;
        }
        case 17:
        {
            b2Capsule a = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 0.5f};
            b2Capsule b = {{-0.5f, 0.3f}, {1.5f, 0.3f}, 0.5f};
            (void)b2CollideCapsules(&a, b2Transform_identity, &b, b2Transform_identity);
            break;
        }
        default:
        {
            b2WorldDef worldDef = b2DefaultWorldDef();
            b2WorldId worldId = b2CreateWorld(&worldDef);
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Circle shapeCircle = {{0.0f, 0.0f}, 1.0f};
            b2ShapeId shapeId;
            if (index == 22)
            {
                shapeId = b2CreatePolygonShape(bodyId, &shapeDef, &box);
                (void)b2Shape_GetCircle(shapeId);
            }
            else
            {
                shapeId = b2CreateCircleShape(bodyId, &shapeDef, &shapeCircle);
                if (index == 18) (void)b2Shape_GetSegment(shapeId);
                if (index == 19) (void)b2Shape_GetChainSegment(shapeId);
                if (index == 20) (void)b2Shape_GetCapsule(shapeId);
                if (index == 21) (void)b2Shape_GetPolygon(shapeId);
            }
            b2DestroyWorld(worldId);
            break;
        }
    }
}

static int asserts(int index)
{
    if (setjmp(assertionJump) == 0)
    {
        run_case(index);
        return 0;
    }
    return 1;
}

int main(void)
{
    b2SetAssertFcn(catch_assertion);
    printf("geometryValidation");
    for (int i = 0; i < 23; ++i)
    {
        printf(" %d", asserts(i));
    }
    printf("\n");
    return 0;
}
