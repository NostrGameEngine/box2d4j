package org.box2d4j;

@FunctionalInterface
public interface b2RestitutionCallback {
    float invoke(float restitutionA, int userMaterialIdA, float restitutionB, int userMaterialIdB);
}
