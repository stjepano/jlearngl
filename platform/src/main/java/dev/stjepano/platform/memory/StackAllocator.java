package dev.stjepano.platform.memory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

/// Per-thread stack allocator.
///
/// This is per thread arena very similar to LWJGL MemoryStack but this one implements {@link SegmentAllocator}
/// interface and allocates {@link MemorySegment} objects.
///
/// Here is how to use it:
///
/// ```java
/// void someShortLivedMethod() {
///   try (var arena = StackAllocator.push()) {
///     MemorySegment someMemory = arena.allocate(16);
///   }
/// }
/// ```
///
/// NOTE: Arena size is 128KiB per thread.
/// **Warning**: you must close it otherwise the arena will get filled up!
public final class StackAllocator implements SegmentAllocator, AutoCloseable {
    private static final int STACK_CAPACITY = 128 * 1024;
    private static final int MAX_NESTED_PUSHES = 32;

    private static final ThreadLocal<StackAllocator> STACK = ThreadLocal.withInitial(() -> new StackAllocator(STACK_CAPACITY));

    private final MemorySegment buffer;
    private final long[] pointerStack = new long[MAX_NESTED_PUSHES];
    private int pointerStackIndex = 0;
    private long pointer;

    private StackAllocator(long sizeBytes) {
        this.buffer = Arena.global().allocate(sizeBytes);
    }

    public static StackAllocator push() {
        var threadsStack = STACK.get();
        if (threadsStack.pointerStackIndex == MAX_NESTED_PUSHES) {
            throw new IllegalStateException("To many nested pushes!");
        }
        threadsStack.pointerStack[threadsStack.pointerStackIndex++] = threadsStack.pointer;
        return threadsStack;
    }

    @Override
    public void close() {
        if (pointerStackIndex == 0) {
            throw new IllegalStateException("Close without push!");
        }
        pointer = pointerStack[--pointerStackIndex];
    }

    private boolean isPowerOfTwo(long num) {
        return (num & (num - 1)) == 0;
    }

    private long alignToPowerOfTwo(long num, long alignment) {
        return (num + alignment - 1) & -alignment;
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        if (byteSize <= 0) {
            throw new IllegalArgumentException("byteSize is lesser or equal to 0: " + byteSize);
        }
        if (byteAlignment <= 0) {
            throw new IllegalArgumentException("byteAlignment is lesser or equal to 0: " + byteAlignment);
        }
        if (!isPowerOfTwo(byteAlignment)) {
            throw new IllegalArgumentException("byteAlignment is not power of two: " + byteAlignment);
        }

        long aligned = alignToPowerOfTwo(pointer, byteAlignment);
        if (aligned + byteSize > buffer.byteSize()) {
            throw new OutOfMemoryError("MemStack overflow!");
        }

        MemorySegment slice = buffer.asSlice(aligned, byteSize);
        pointer = aligned + byteSize;
        return slice;
    }

    // For test only
    long pointer() {
        return pointer;
    }
}
