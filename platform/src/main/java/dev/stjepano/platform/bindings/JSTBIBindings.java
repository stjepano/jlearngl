package dev.stjepano.platform.bindings;

import dev.stjepano.platform.processor.anno.GenerateNativeBindings;
import dev.stjepano.platform.processor.anno.NativeBinding;

import java.lang.foreign.MemorySegment;

@GenerateNativeBindings(className = "JSTBI", classAccess = "", targetPackage = "dev.stjepano.platform.util")
interface JSTBIBindings {
    @NativeBinding
    MemorySegment jstbiLoadFromFile(MemorySegment filePathPtr, boolean flipVertically, MemorySegment outImageInfoPtr, MemorySegment errorBuf, int errorBufSize);

    @NativeBinding
    MemorySegment jstbiLoadFromMemory(MemorySegment memory, long memorySize, boolean flipVertically, MemorySegment outImageInfoPtr, MemorySegment errorBuf, int errorBufSize);

    @NativeBinding
    void jstbiFree(MemorySegment imageDataPtr);
}
