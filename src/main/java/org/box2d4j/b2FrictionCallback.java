package org.box2d4j;

@FunctionalInterface
public interface b2FrictionCallback {
    float invoke(float frictionA, int userMaterialIdA, float frictionB, int userMaterialIdB);
}
