// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){2.0f, 3.0f};
    bodyDef.rotation = b2MakeRot(0.2f);
    bodyDef.linearVelocity = (b2Vec2){1.0f, -2.0f};
    bodyDef.angularVelocity = 0.75f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef boxDef = b2DefaultShapeDef();
    boxDef.density = 2.0f;
    b2Polygon box = b2MakeOffsetBox(0.5f, 1.0f, (b2Vec2){0.25f, 0.0f}, b2MakeRot(0.1f));
    b2CreatePolygonShape(bodyId, &boxDef, &box);

    b2ShapeDef circleDef = b2DefaultShapeDef();
    circleDef.density = 0.75f;
    b2Circle circle = {{-0.75f, 0.25f}, 0.3f};
    b2CreateCircleShape(bodyId, &circleDef, &circle);

    b2MassData massData = b2Body_GetMassData(bodyId);
    printf("mass %.9g %.9g %.9g %.9g\n", b2Body_GetMass(bodyId), b2Body_GetRotationalInertia(bodyId),
           massData.mass, massData.rotationalInertia);
    print_vec("local", b2Body_GetLocalCenterOfMass(bodyId));
    print_vec("world", b2Body_GetWorldCenterOfMass(bodyId));
    print_vec("massCenter", massData.center);
    print_vec("localPoint", b2Body_GetLocalPoint(bodyId, (b2Vec2){2.5f, 4.0f}));
    print_vec("worldPoint", b2Body_GetWorldPoint(bodyId, (b2Vec2){0.5f, -0.25f}));
    print_vec("localVector", b2Body_GetLocalVector(bodyId, (b2Vec2){1.0f, 0.5f}));
    print_vec("worldVector", b2Body_GetWorldVector(bodyId, (b2Vec2){1.0f, 0.5f}));
    print_vec("velocity", b2Body_GetLinearVelocity(bodyId));
    printf("angular %.9g\n", b2Body_GetAngularVelocity(bodyId));

    b2DestroyWorld(worldId);
    return 0;
}
