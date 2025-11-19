package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.impl.NativePlatform;
import dev.stjepano.platform.opengl.Buffer;
import dev.stjepano.platform.opengl.BufferStorageFlags;
import dev.stjepano.platform.opengl.OpenGL;
import dev.stjepano.platform.memory.StackAllocator;
import dev.stjepano.platform.opengl.OpenGLException;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class OpenGLImpl implements OpenGL {

    private static final int GL_COLOR = 0x1800;
    private static final int GL_DEPTH = 0x1801;
    private static final int GL_STENCIL = 0x1802;
    private static final int GL_DEPTH_AND_STENCIL = 0x84f9;

    @Override
    public void viewport(int x, int y, int width, int height) {
        NativePlatform.jglViewport(x, y, width, height);
    }

    @Override
    public void clearColorBuffer(float r, float g, float b, float a) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment valuePtr = stack.allocateFrom(ValueLayout.JAVA_FLOAT, r, g, b, a);
            NativePlatform.jglClearNamedFramebufferfv(0, GL_COLOR, 0, valuePtr);
        }
    }

    @Override
    public void clearDepthStencil(float depthValue, int stencilValue) {
        NativePlatform.jglClearNamedFramebufferfi(0, GL_DEPTH_AND_STENCIL, 0, depthValue, stencilValue);
    }

    @Override
    public void clearDepth(float depthValue) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment valuePtr = stack.allocateFrom(ValueLayout.JAVA_FLOAT, depthValue);
            NativePlatform.jglClearNamedFramebufferfv(0, GL_DEPTH, 0, valuePtr);
        }
    }

    @Override
    public void clearStencil(int stencilValue) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment valuePtr = stack.allocateFrom(ValueLayout.JAVA_INT, stencilValue);
            NativePlatform.jglClearNamedFramebufferiv(0, GL_STENCIL, 0, valuePtr);
        }
    }

    @Override
    public Buffer createBuffer(long byteSize, BufferStorageFlags flags) {
        return createBuffer(byteSize, flags, MemorySegment.NULL);
    }

    @Override
    public Buffer createBuffer(long byteSize, BufferStorageFlags flags, MemorySegment data) {
        int bufferId = NativePlatform.jglCreateBufferWithStorage(byteSize, flags.glFlags(), data);
        if (bufferId == 0) {
            throw new OpenGLException("Failed to create buffer and set storage!");
        }
        return new BufferImpl(bufferId, byteSize, flags);
    }
}
