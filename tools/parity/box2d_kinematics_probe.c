// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, -10.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){1.25f, 7.0f};
    bodyDef.rotation = b2MakeRot(0.3f);
    bodyDef.linearVelocity = (b2Vec2){2.0f, -1.0f};
    bodyDef.angularVelocity = 0.4f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 2.0f;
    b2Polygon box = b2MakeOffsetBox(0.75f, 0.5f, (b2Vec2){0.2f, -0.1f}, b2MakeRot(0.15f));
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    for (int i = 0; i < 12; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 c = b2Body_GetWorldCenterOfMass(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    b2Rot q = b2Body_GetRotation(bodyId);
    printf("position %.9g %.9g\n", p.x, p.y);
    printf("center %.9g %.9g\n", c.x, c.y);
    printf("velocity %.9g %.9g\n", v.x, v.y);
    printf("angle %.9g\n", b2Rot_GetAngle(q));
    printf("angular %.9g\n", b2Body_GetAngularVelocity(bodyId));

    b2DestroyWorld(worldId);
    return 0;
}
