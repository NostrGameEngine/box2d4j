// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <math.h>
#include <setjmp.h>
#include <stdio.h>

static jmp_buf assertionJump;
static b2WorldId worldId;
static b2BodyId bodyId;

static int catch_assertion(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    longjmp(assertionJump, 1);
    return 0;
}

static int ignore_assertion(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    return 0;
}

static bool overlap_callback(b2ShapeId shapeId, void* context)
{
    (void)shapeId;
    (void)context;
    return true;
}

static float cast_callback(b2ShapeId shapeId, b2Vec2 point, b2Vec2 normal, float fraction, void* context)
{
    (void)shapeId;
    (void)point;
    (void)normal;
    (void)context;
    return fraction;
}

static b2AABB invalid_aabb(void)
{
    return (b2AABB){{1.0f, 1.0f}, {-1.0f, -1.0f}};
}

static void run_case(int index)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2QueryFilter filter = b2DefaultQueryFilter();
    b2Vec2 point = b2Vec2_zero;
    b2ShapeProxy proxy = b2MakeProxy(&point, 1, 0.0f);
    switch (index)
    {
        case 0: bodyDef.position.x = NAN; (void)b2CreateBody(worldId, &bodyDef); break;
        case 1: bodyDef.rotation = (b2Rot){0.0f, 0.0f}; (void)b2CreateBody(worldId, &bodyDef); break;
        case 2: bodyDef.linearVelocity.y = INFINITY; (void)b2CreateBody(worldId, &bodyDef); break;
        case 3: bodyDef.angularVelocity = NAN; (void)b2CreateBody(worldId, &bodyDef); break;
        case 4: bodyDef.linearDamping = -1.0f; (void)b2CreateBody(worldId, &bodyDef); break;
        case 5: bodyDef.angularDamping = NAN; (void)b2CreateBody(worldId, &bodyDef); break;
        case 6: bodyDef.sleepThreshold = -1.0f; (void)b2CreateBody(worldId, &bodyDef); break;
        case 7: bodyDef.gravityScale = INFINITY; (void)b2CreateBody(worldId, &bodyDef); break;
        case 8: b2Body_SetTransform(bodyId, (b2Vec2){NAN, 0.0f}, b2Rot_identity); break;
        case 9: b2Body_SetTransform(bodyId, b2Vec2_zero, (b2Rot){0.0f, 0.0f}); break;
        case 10:
        {
            b2DebugDraw draw = b2DefaultDebugDraw();
            draw.drawingBounds = invalid_aabb();
            b2World_Draw(worldId, &draw);
            break;
        }
        case 11:
        {
            b2DebugDraw draw = b2DefaultDebugDraw();
            draw.useDrawingBounds = true;
            draw.drawingBounds = invalid_aabb();
            b2World_Draw(worldId, &draw);
            break;
        }
        case 12: (void)b2World_OverlapAABB(worldId, invalid_aabb(), filter, overlap_callback, NULL); break;
        case 13: (void)b2World_CastRay(worldId, (b2Vec2){NAN, 0.0f}, b2Vec2_zero, filter, cast_callback, NULL); break;
        case 14: (void)b2World_CastRay(worldId, b2Vec2_zero, (b2Vec2){INFINITY, 0.0f}, filter, cast_callback, NULL); break;
        case 15: (void)b2World_CastRayClosest(worldId, (b2Vec2){NAN, 0.0f}, b2Vec2_zero, filter); break;
        case 16: (void)b2World_CastRayClosest(worldId, b2Vec2_zero, (b2Vec2){INFINITY, 0.0f}, filter); break;
        case 17: (void)b2World_CastShape(worldId, &proxy, (b2Vec2){NAN, 0.0f}, filter, cast_callback, NULL); break;
        case 18:
        {
            b2Capsule mover = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.25f};
            (void)b2World_CastMover(worldId, &mover, (b2Vec2){NAN, 0.0f}, filter);
            break;
        }
        case 19:
        {
            b2Capsule mover = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.0f};
            (void)b2World_CastMover(worldId, &mover, b2Vec2_zero, filter);
            break;
        }
        default:
        {
            b2ExplosionDef def = b2DefaultExplosionDef();
            if (index == 20) def.position.x = NAN;
            if (index == 21) def.radius = -1.0f;
            if (index == 22) def.falloff = NAN;
            if (index == 23) def.impulsePerLength = INFINITY;
            b2World_Explode(worldId, &def);
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
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyId = b2CreateBody(worldId, &bodyDef);

    b2SetAssertFcn(catch_assertion);
    printf("bodyWorldValidation");
    for (int i = 0; i < 24; ++i)
    {
        printf(" %d", asserts(i));
    }
    printf("\n");
    b2SetAssertFcn(ignore_assertion);
    b2DestroyWorld(worldId);
    return 0;
}
