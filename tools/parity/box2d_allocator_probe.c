// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/base.h"
#include "core.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

static int g_allocCount;
static int g_freeCount;
static int g_lastSize;
static int g_lastAlignment;

static void* ProbeAlloc( unsigned int size, int alignment )
{
    g_allocCount += 1;
    g_lastSize = (int)size;
    g_lastAlignment = alignment;
    return aligned_alloc( alignment, size );
}

static void ProbeFree( void* mem )
{
    g_freeCount += 1;
    free( mem );
}

int main( void )
{
    b2SetAllocator( ProbeAlloc, ProbeFree );

    void* zero = b2Alloc( 0 );
    printf( "zero %d %d %d\n", zero == NULL, g_allocCount, b2GetByteCount() );

    void* mem = b2Alloc( 65 );
    printf( "alloc65 %d %d %d %d %d\n", mem != NULL, g_lastSize, g_lastAlignment, g_allocCount, b2GetByteCount() );
    b2Free( mem, 65 );
    printf( "free65 %d %d\n", g_freeCount, b2GetByteCount() );

    mem = b2Alloc( 32 );
    printf( "alloc32 %d %d %d %d %d\n", mem != NULL, g_lastSize, g_lastAlignment, g_allocCount, b2GetByteCount() );
    b2Free( mem, 32 );
    printf( "free32 %d %d\n", g_freeCount, b2GetByteCount() );

    b2SetAllocator( NULL, NULL );
    return 0;
}
