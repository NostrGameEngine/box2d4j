// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>
#include <string.h>

static uint32_t bits(float value)
{
    uint32_t output;
    memcpy(&output, &value, sizeof(output));
    return output;
}

int main(void)
{
    b2Vec2 points[10];
    for (int i = 0; i < 10; ++i)
    {
        points[i] = (b2Vec2){i + 0.25f, -2.0f * i};
    }
    b2ShapeProxy plain = b2MakeProxy(points, 10, 0.3f);
    b2ShapeProxy offset = b2MakeOffsetProxy(points, 10, 0.4f, (b2Vec2){2.0f, -1.0f}, b2MakeRot(0.25f));
    printf("shapeProxyClamp %d %08x %08x %08x %d %08x %08x %08x\n", plain.count,
           bits(plain.points[7].x), bits(plain.points[7].y), bits(plain.radius), offset.count,
           bits(offset.points[7].x), bits(offset.points[7].y), bits(offset.radius));
    return 0;
}
