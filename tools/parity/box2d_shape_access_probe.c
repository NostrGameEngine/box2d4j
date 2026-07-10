// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>

static void print_mass(const char* label, b2BodyId bodyId)
{
    b2MassData mass = b2Body_GetMassData(bodyId);
    printf("%s %.9g %.9g %.9g %.9g\n", label, mass.mass, mass.center.x, mass.center.y, mass.rotationalInertia);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    uintptr_t userDataValue = 0x1234u;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 2.0f;
    shapeDef.isSensor = true;
    shapeDef.userData = (void*)userDataValue;
    b2Circle circle = { { 0.2f, -0.1f }, 0.75f };
    b2ShapeId shapeId = b2CreateCircleShape(bodyId, &shapeDef, &circle);

    b2BodyId ownerBodyId = b2Shape_GetBody(shapeId);
    b2WorldId ownerWorldId = b2Shape_GetWorld(shapeId);
    printf("ids %d %d %d %d %d %d %d\n", shapeId.index1, ownerBodyId.index1, ownerBodyId.world0,
           ownerBodyId.generation, ownerWorldId.index1, ownerWorldId.generation, b2Shape_GetType(shapeId));
    printf("flags %d %d %.9g %llu\n", b2Shape_IsSensor(shapeId) ? 1 : 0, b2Shape_GetUserData(shapeId) == (void*)userDataValue,
           b2Shape_GetDensity(shapeId), (unsigned long long)(uintptr_t)b2Shape_GetUserData(shapeId));
    print_mass("mass0", bodyId);

    b2Shape_SetDensity(shapeId, 4.0f, false);
    printf("density1 %.9g\n", b2Shape_GetDensity(shapeId));
    print_mass("mass1", bodyId);

    b2Shape_SetDensity(shapeId, 4.0f, true);
    print_mass("mass2", bodyId);

    b2Shape_SetDensity(shapeId, 1.5f, true);
    printf("density2 %.9g\n", b2Shape_GetDensity(shapeId));
    print_mass("mass3", bodyId);

    uintptr_t userDataValue2 = 0x5678u;
    b2Shape_SetUserData(shapeId, (void*)userDataValue2);
    printf("userdata2 %llu\n", (unsigned long long)(uintptr_t)b2Shape_GetUserData(shapeId));

    b2DestroyWorld(worldId);
    return 0;
}
