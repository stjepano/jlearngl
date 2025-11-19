package dev.stjepano.platform.bindings;

import dev.stjepano.platform.processor.anno.GenerateNativeBindings;
import dev.stjepano.platform.processor.anno.NativeBinding;

import java.lang.foreign.MemorySegment;

@GenerateNativeBindings(className = "JGL", classAccess = "", targetPackage = "dev.stjepano.platform.impl.opengl")
interface JGLBindings {
    @NativeBinding
    void jglViewport(int x, int y, int width, int height);

    @NativeBinding
    void jglClearNamedFramebufferiv(int framebuffer, int buffer, int drawbuffer, MemorySegment valuePtr);

    @NativeBinding
    void jglClearNamedFramebufferuiv(int framebuffer, int buffer, int drawbuffer, MemorySegment valuePtr);

    @NativeBinding
    void jglClearNamedFramebufferfv(int framebuffer, int buffer, int drawbuffer, MemorySegment valuePtr);

    @NativeBinding
    void jglClearNamedFramebufferfi(int framebuffer, int buffer, int drawbuffer, float depthValue, int stencilValue);

    @NativeBinding
    boolean jglNamedBufferSubData(int buffer, long offset, long size, MemorySegment data);

    @NativeBinding
    MemorySegment jglMapNamedBufferRange(int buffer, long offset, long length, int accessflags);

    @NativeBinding
    boolean jglUnmapNamedBuffer(int buffer);

    @NativeBinding
    boolean jglFlushMappedNamedBufferRange(int buffer, long offset, long length);

    @NativeBinding
    void jglDeleteBuffers(int buffer);

    @NativeBinding
    int jglCreateBufferWithStorage(long byteSize, int storageflags, MemorySegment data);

    @NativeBinding
    int jglCreateProgramVF(MemorySegment vertexShaderSrcBuf,
                         MemorySegment fragmentShaderSrcBuf,
                         MemorySegment errorBuf, long errorBufSize);

    @NativeBinding
    void jglBindProgram(int programId);

    @NativeBinding
    void jglDeleteProgram(int programId);

    @NativeBinding
    int jglCreateVertexArray();

    @NativeBinding
    void jglBindVertexArray(int vaoId);

    @NativeBinding
    boolean jglVertexArrayVertexBuffer(int vaoId, int bindingIndex, int bufferId, long offset, int stride);

    @NativeBinding
    boolean jglVertexArrayVertexAttrib(int vaoId, int attribIndex, int bufferBindingIndex, int numComponent,
                                       int componentType, boolean normalized, int offset);

    @NativeBinding
    boolean jglVertexArrayToggleAttrib(int vaoId, int attribIndex, boolean enabled);

    @NativeBinding
    void jglDeleteVertexArray(int vaoId);

    @NativeBinding
    void jglDrawArrays(int mode, int first, int count);
}

