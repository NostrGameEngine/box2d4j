// SPDX-License-Identifier: MIT

#include "bitset.h"
#include "ctz.h"
#include "table.h"

#include "box2d/base.h"

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

#define COUNT 169
#define SET_SPAN 317
#define ITEM_COUNT ( ( SET_SPAN * SET_SPAN - SET_SPAN ) / 2 )

static void RunBitSet(void)
{
    b2BitSet bitSet = b2CreateBitSet(COUNT);
    b2SetBitCountAndClear(&bitSet, COUNT);
    bool values[COUNT] = {false};

    int32_t i1 = 0, i2 = 1;
    b2SetBit(&bitSet, i1);
    values[i1] = true;

    while (i2 < COUNT)
    {
        b2SetBit(&bitSet, i2);
        values[i2] = true;
        int32_t next = i1 + i2;
        i1 = i2;
        i2 = next;
    }

    int mismatchCount = 0;
    int setCount = 0;
    uint32_t hash = B2_HASH_INIT;
    for (int32_t i = 0; i < COUNT; ++i)
    {
        bool value = b2GetBit(&bitSet, i);
        if (value)
        {
            setCount += 1;
        }
        if (value != values[i])
        {
            mismatchCount += 1;
        }
        uint8_t byte = value ? 1 : 0;
        hash = b2Hash(hash, &byte, 1);
    }

    printf("bitset %u %u %d %d %u %d\n", bitSet.blockCapacity, bitSet.blockCount, b2GetBitSetBytes(&bitSet),
           setCount, hash, mismatchCount);
    b2DestroyBitSet(&bitSet);
}

static void RunTable(void)
{
    int power = b2BoundingPowerOf2(3008);
    int nextPowerOf2 = b2RoundUpPowerOf2(3008);

    const int32_t N = SET_SPAN;
    const uint32_t itemCount = ITEM_COUNT;
    bool removed[ITEM_COUNT] = {0};

    b2HashSet set = b2CreateSet(16);

    for (int32_t i = 0; i < N; ++i)
    {
        for (int32_t j = i + 1; j < N; ++j)
        {
            uint64_t key = B2_SHAPE_PAIR_KEY(i, j);
            b2AddKey(&set, key);
        }
    }

    uint32_t filledCount = set.count;
    uint32_t filledCapacity = set.capacity;
    int filledBytes = b2GetHashSetBytes(&set);

    int32_t k = 0;
    uint32_t removeCount = 0;
    for (int32_t i = 0; i < N; ++i)
    {
        for (int32_t j = i + 1; j < N; ++j)
        {
            if (j == i + 1)
            {
                uint64_t key = B2_SHAPE_PAIR_KEY(i, j);
                b2RemoveKey(&set, key);
                removed[k++] = true;
                removeCount += 1;
            }
            else
            {
                removed[k++] = false;
            }
        }
    }

    uint32_t afterRemoveCount = set.count;
    uint32_t containsCount = 0;
    uint32_t removedObservedCount = 0;
    uint32_t missCount = 0;
    k = 0;
    for (int32_t i = 0; i < N; ++i)
    {
        for (int32_t j = i + 1; j < N; ++j)
        {
            uint64_t key = B2_SHAPE_PAIR_KEY(j, i);
            bool contains = b2ContainsKey(&set, key);
            if (contains)
            {
                containsCount += 1;
            }
            if (removed[k])
            {
                removedObservedCount += 1;
            }
            if (contains == false && removed[k] == false)
            {
                missCount += 1;
            }
            k += 1;
        }
    }

    for (int32_t i = 0; i < N; ++i)
    {
        for (int32_t j = i + 1; j < N; ++j)
        {
            uint64_t key = B2_SHAPE_PAIR_KEY(i, j);
            b2RemoveKey(&set, key);
        }
    }

    printf("table %d %d %u %u %d %u %u %u %u %u %u %u\n", power, nextPowerOf2, itemCount, filledCount,
           filledCapacity, filledBytes, removeCount, afterRemoveCount, containsCount, removedObservedCount, missCount,
           set.count);
    b2DestroySet(&set);
}

int main(void)
{
    RunBitSet();
    RunTable();
    return 0;
}
