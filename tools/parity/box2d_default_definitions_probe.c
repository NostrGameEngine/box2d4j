// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>

#define F(value) (double)(value)
#define N(value) ((value) == NULL ? 1 : 0)

int main(void)
{
    b2WorldDef world = b2DefaultWorldDef();
    printf("world %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d %d %d %d %d %d %d %d %d\n",
           F(world.gravity.x), F(world.gravity.y), F(world.restitutionThreshold), F(world.hitEventThreshold),
           F(world.contactHertz), F(world.contactDampingRatio), F(world.maxContactPushSpeed),
           F(world.maximumLinearSpeed), N(world.frictionCallback), N(world.restitutionCallback),
           world.enableSleep, world.enableContinuous, world.workerCount, N(world.enqueueTask), N(world.finishTask),
           N(world.userTaskContext), N(world.userData), world.internalValue);

    b2BodyDef body = b2DefaultBodyDef();
    printf("body %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d %d %d %d %d %d %d %d\n",
           body.type, F(body.position.x), F(body.position.y), F(body.rotation.c), F(body.rotation.s),
           F(body.linearVelocity.x), F(body.linearVelocity.y), F(body.angularVelocity), F(body.linearDamping),
           F(body.angularDamping), F(body.gravityScale), F(body.sleepThreshold), N(body.name), N(body.userData),
           body.enableSleep, body.isAwake, body.fixedRotation, body.isBullet, body.isEnabled, body.allowFastRotation,
           body.internalValue);

    b2Filter filter = b2DefaultFilter();
    b2QueryFilter query = b2DefaultQueryFilter();
    b2SurfaceMaterial material = b2DefaultSurfaceMaterial();
    printf("base %lld %lld %d %lld %lld %.9g %.9g %.9g %.9g %d %u\n",
           (long long)filter.categoryBits, (long long)filter.maskBits, filter.groupIndex,
           (long long)query.categoryBits, (long long)query.maskBits, F(material.friction), F(material.restitution),
           F(material.rollingResistance), F(material.tangentSpeed), material.userMaterialId, material.customColor);

    b2ShapeDef shape = b2DefaultShapeDef();
    printf("shape %d %.9g %.9g %.9g %.9g %d %u %.9g %lld %lld %d %d %d %d %d %d %d %d %d\n",
           N(shape.userData), F(shape.material.friction), F(shape.material.restitution),
           F(shape.material.rollingResistance), F(shape.material.tangentSpeed), shape.material.userMaterialId,
           shape.material.customColor, F(shape.density), (long long)shape.filter.categoryBits,
           (long long)shape.filter.maskBits, shape.filter.groupIndex, shape.isSensor, shape.enableSensorEvents,
           shape.enableContactEvents, shape.enableHitEvents, shape.enablePreSolveEvents, shape.invokeContactCreation,
           shape.updateBodyMass, shape.internalValue);

    b2ChainDef chain = b2DefaultChainDef();
    printf("chain %d %d %d %d %d %.9g %.9g %.9g %.9g %d %u %lld %lld %d %d %d %d\n",
           N(chain.userData), N(chain.points), chain.count, chain.materials != NULL ? 1 : 0, chain.materialCount,
           F(chain.materials[0].friction), F(chain.materials[0].restitution),
           F(chain.materials[0].rollingResistance), F(chain.materials[0].tangentSpeed),
           chain.materials[0].userMaterialId, chain.materials[0].customColor,
           (long long)chain.filter.categoryBits, (long long)chain.filter.maskBits, chain.filter.groupIndex,
           chain.isLoop, chain.enableSensorEvents, chain.internalValue);

    b2ExplosionDef explosion = b2DefaultExplosionDef();
    printf("explosion %lld %.9g %.9g %.9g %.9g %.9g\n", (long long)explosion.maskBits,
           F(explosion.position.x), F(explosion.position.y), F(explosion.radius), F(explosion.falloff),
           F(explosion.impulsePerLength));

    b2DebugDraw draw = b2DefaultDebugDraw();
    printf("draw %d %d %d %d %d %d %d %d %d %.9g %.9g %.9g %.9g %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d\n",
           !N(draw.DrawPolygonFcn), !N(draw.DrawSolidPolygonFcn), !N(draw.DrawCircleFcn),
           !N(draw.DrawSolidCircleFcn), !N(draw.DrawSolidCapsuleFcn), !N(draw.DrawSegmentFcn),
           !N(draw.DrawTransformFcn), !N(draw.DrawPointFcn), !N(draw.DrawStringFcn),
           F(draw.drawingBounds.lowerBound.x), F(draw.drawingBounds.lowerBound.y),
           F(draw.drawingBounds.upperBound.x), F(draw.drawingBounds.upperBound.y), draw.useDrawingBounds,
           draw.drawShapes, draw.drawJoints, draw.drawJointExtras, draw.drawBounds, draw.drawMass,
           draw.drawBodyNames, draw.drawContacts, draw.drawGraphColors, draw.drawContactNormals,
           draw.drawContactImpulses, draw.drawContactFeatures, draw.drawFrictionImpulses, draw.drawIslands,
           N(draw.context));

    b2DistanceJointDef distance = b2DefaultDistanceJointDef();
    b2MotorJointDef motor = b2DefaultMotorJointDef();
    b2MouseJointDef mouse = b2DefaultMouseJointDef();
    b2FilterJointDef filterJoint = b2DefaultFilterJointDef();
    b2PrismaticJointDef prismatic = b2DefaultPrismaticJointDef();
    b2RevoluteJointDef revolute = b2DefaultRevoluteJointDef();
    b2WeldJointDef weld = b2DefaultWeldJointDef();
    b2WheelJointDef wheel = b2DefaultWheelJointDef();
    printf("joints %.9g %.9g %d %.9g %.9g %.9g %d %.9g %.9g %.9g %d %d %.9g %.9g %d %.9g %d %d %.9g %.9g %d %.9g %.9g %d\n",
           F(distance.length), F(distance.maxLength), distance.internalValue, F(motor.maxForce), F(motor.maxTorque),
           F(motor.correctionFactor), motor.internalValue, F(mouse.hertz), F(mouse.dampingRatio), F(mouse.maxForce),
           mouse.internalValue, filterJoint.internalValue, F(prismatic.localAxisA.x), F(prismatic.localAxisA.y),
           prismatic.internalValue, F(revolute.drawSize), revolute.internalValue, weld.internalValue,
           F(wheel.localAxisA.x), F(wheel.localAxisA.y), wheel.enableSpring, F(wheel.hertz),
           F(wheel.dampingRatio), wheel.internalValue);
    return 0;
}
