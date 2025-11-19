package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.opengl.*;
import dev.stjepano.platform.memory.StackAllocator;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class OpenGLImpl implements OpenGL {

    private static final int GL_COLOR = 0x1800;
    private static final int GL_DEPTH = 0x1801;
    private static final int GL_STENCIL = 0x1802;
    private static final int GL_DEPTH_AND_STENCIL = 0x84f9;

    @Override
    public void viewport(int x, int y, int width, int height) {
        JGL.jglViewport(x, y, width, height);
    }

    @Override
    public void clearColorBuffer(float r, float g, float b, float a) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment valuePtr = stack.allocateFrom(ValueLayout.JAVA_FLOAT, r, g, b, a);
            JGL.jglClearNamedFramebufferfv(0, GL_COLOR, 0, valuePtr);
        }
    }

    @Override
    public void clearDepthStencil(float depthValue, int stencilValue) {
        JGL.jglClearNamedFramebufferfi(0, GL_DEPTH_AND_STENCIL, 0, depthValue, stencilValue);
    }

    @Override
    public void clearDepth(float depthValue) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment valuePtr = stack.allocateFrom(ValueLayout.JAVA_FLOAT, depthValue);
            JGL.jglClearNamedFramebufferfv(0, GL_DEPTH, 0, valuePtr);
        }
    }

    @Override
    public void clearStencil(int stencilValue) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment valuePtr = stack.allocateFrom(ValueLayout.JAVA_INT, stencilValue);
            JGL.jglClearNamedFramebufferiv(0, GL_STENCIL, 0, valuePtr);
        }
    }

    @Override
    public Buffer createBuffer(long byteSize, BufferStorageFlags flags) {
        return createBuffer(byteSize, flags, MemorySegment.NULL);
    }

    @Override
    public Buffer createBuffer(long byteSize, BufferStorageFlags flags, MemorySegment data) {
        int bufferId = JGL.jglCreateBufferWithStorage(byteSize, flags.glFlags(), data);
        if (bufferId == 0) {
            throw new OpenGLException("Failed to create buffer and set storage!");
        }
        return new BufferImpl(bufferId, byteSize, flags);
    }

    @Override
    public Program createProgram(String vertexShaderSource, String fragmentShaderSource) {
        try (StackAllocator stackAllocator = StackAllocator.push()) {
            MemorySegment vertexShaderSrcPtr = stackAllocator.allocateFrom(vertexShaderSource);
            MemorySegment fragmentShaderSrcPtr = stackAllocator.allocateFrom(fragmentShaderSource);
            MemorySegment errorBufPtr = stackAllocator.allocate(1024);
            int programId = JGL.jglCreateProgramVF(vertexShaderSrcPtr, fragmentShaderSrcPtr, errorBufPtr, 1024L);
            if (programId == 0) {
                throw new OpenGLException("Failed to create program: " + errorBufPtr.getString(0));
            }
            return new ProgramImpl(programId);
        }
    }

    @Override
    public VertexArray createVertexArray() {
        int vaoId = JGL.jglCreateVertexArray();
        if (vaoId == 0) {
            throw new OpenGLException("Failed to create vertex array.");
        }
        return new VertexArrayImpl(vaoId);
    }

    @Override
    public void bindVertexArray(VertexArray vao) {
        if (vao == null) {
            JGL.jglBindVertexArray(0);
        } else {
            if (vao instanceof VertexArrayImpl vaoImpl) {
                JGL.jglBindVertexArray(vaoImpl.glId);
            } else {
                throw new IllegalStateException("vao not instance of VertexArrayImpl");
            }
        }
    }

    @Override
    public void bindProgram(Program program) {
        if (program == null) {
            JGL.jglBindProgram(0);
        } else {
            if (program instanceof ProgramImpl programImpl) {
                JGL.jglBindProgram(programImpl.glId);
            } else {
                throw new IllegalStateException("program not instance of ProgramImpl");
            }
        }
    }

    @Override
    public void drawArrays(GLPrimitive primitiveType, int first, int count) {
        JGL.jglDrawArrays(primitiveType.glEnumValue(), first, count);
    }
}
