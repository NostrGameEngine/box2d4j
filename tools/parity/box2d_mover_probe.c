// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static bool plane_callback(b2ShapeId shapeId, const b2PlaneResult* plane, void* context)
{
    int* count = context;
    *count += 1;
    printf("plane %d %d %.9g %.9g %.9g %.9g %.9g %d\n", shapeId.index1, shapeId.generation,
           plane->plane.normal.x, plane->plane.normal.y, plane->plane.offset, plane->point.x, plane->point.y,
           plane->hit ? 1 : 0);
    return true;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2ShapeId boxId = b2CreatePolygonShape(groundId, &shapeDef, &box);

    b2Capsule castMover = {{-3.0f, -0.5f}, {-3.0f, 0.5f}, 0.35f};
    float fraction = b2World_CastMover(worldId, &castMover, (b2Vec2){5.0f, 0.0f}, b2DefaultQueryFilter());
    printf("cast %.9g\n", fraction);

    b2Capsule collideMover = {{1.2f, -0.5f}, {1.2f, 0.5f}, 0.35f};
    int count = 0;
    b2World_CollideMover(worldId, &collideMover, b2DefaultQueryFilter(), plane_callback, &count);
    printf("count %d %d\n", count, boxId.index1);

    b2DestroyWorld(worldId);
    return 0;
}
