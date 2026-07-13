package org.box2d4j.debugger;

import org.box2d4j.b2AABB;
import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2JointId;
import org.box2d4j.b2MouseJointDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

final class MouseDragController {
    static final int DOWN = 0;
    static final int UP = 1;
    static final int MOVE = 2;

    private b2JointId mouseJointId = b2_nullJointId;
    private b2BodyId mouseGroundId = b2_nullBodyId;

    void handle(b2WorldId worldId, int type, float worldX, float worldY, int button) {
        if (type == DOWN && button == 0 && B2_IS_NULL(mouseJointId)) {
            begin(worldId, new b2Vec2(worldX, worldY));
        } else if (type == MOVE && b2Joint_IsValid(mouseJointId)) {
            b2MouseJoint_SetTarget(mouseJointId, new b2Vec2(worldX, worldY));
            b2Body_SetAwake(b2Joint_GetBodyB(mouseJointId), true);
        } else if (type == UP && button == 0) {
            end();
        }
    }

    b2JointId jointId() {
        return new b2JointId(mouseJointId.index1, mouseJointId.world0, mouseJointId.generation);
    }

    private void begin(b2WorldId worldId, b2Vec2 point) {
        b2Vec2 extent = new b2Vec2(0.001f, 0.001f);
        b2AABB aabb = new b2AABB(b2Sub(point, extent), b2Add(point, extent));
        b2BodyId[] pickedBody = {b2_nullBodyId};
        b2World_OverlapAABB(worldId, aabb, b2DefaultQueryFilter(), shapeId -> {
            b2BodyId bodyId = b2Shape_GetBody(shapeId);
            if (b2Body_GetType(bodyId) != b2_dynamicBody || !b2Shape_TestPoint(shapeId, point)) {
                return true;
            }
            pickedBody[0] = bodyId;
            return false;
        });
        if (B2_IS_NULL(pickedBody[0])) {
            return;
        }

        b2BodyDef groundDef = b2DefaultBodyDef();
        mouseGroundId = b2CreateBody(worldId, groundDef);
        try {
            b2MouseJointDef jointDef = b2DefaultMouseJointDef();
            jointDef.bodyIdA = mouseGroundId;
            jointDef.bodyIdB = pickedBody[0];
            jointDef.target = point;
            jointDef.hertz = 10.0f;
            jointDef.dampingRatio = 0.7f;
            jointDef.maxForce = 1000.0f * b2Body_GetMass(pickedBody[0]) * b2Length(b2World_GetGravity(worldId));
            mouseJointId = b2CreateMouseJoint(worldId, jointDef);
            b2Body_SetAwake(pickedBody[0], true);
        } catch (RuntimeException | Error failure) {
            if (b2Body_IsValid(mouseGroundId)) {
                b2DestroyBody(mouseGroundId);
            }
            mouseGroundId = b2_nullBodyId;
            throw failure;
        }
    }

    private void end() {
        if (b2Joint_IsValid(mouseJointId)) {
            b2DestroyJoint(mouseJointId);
        }
        if (b2Body_IsValid(mouseGroundId)) {
            b2DestroyBody(mouseGroundId);
        }
        mouseJointId = b2_nullJointId;
        mouseGroundId = b2_nullBodyId;
    }
}
