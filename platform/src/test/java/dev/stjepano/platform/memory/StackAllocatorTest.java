package dev.stjepano.platform.memory;

import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.junit.jupiter.api.Assertions.*;

class StackAllocatorTest {

    @Test
    public void nestedAllocations() {
        try (StackAllocator stack1 = StackAllocator.push()) {
            //noinspection unused
            MemorySegment intPtr = stack1.allocate(ValueLayout.JAVA_INT);
            long pointer1 = stack1.pointer();

            try (StackAllocator stack2 = StackAllocator.push()) {
                //noinspection unused
                MemorySegment longPtr = stack2.allocate(ValueLayout.JAVA_LONG);
                long pointer2 = stack2.pointer();

                try (StackAllocator stack3 = StackAllocator.push()) {
                    //noinspection unused
                    MemorySegment floatPtr = stack3.allocate(ValueLayout.JAVA_FLOAT);
                    long pointer3 = stack3.pointer();
                    assertEquals(20, pointer3); // 4 (int) + 4pad + 8 (long) + 4 (int)
                }

                assertEquals(pointer2, stack2.pointer());
            }

            assertEquals(pointer1, stack1.pointer());
        }
    }

}