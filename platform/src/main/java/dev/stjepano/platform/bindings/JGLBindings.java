package dev.stjepano.platform.bindings;

import dev.stjepano.platform.processor.anno.GenerateNativeBindings;
import dev.stjepano.platform.processor.anno.NativeBinding;

import java.lang.foreign.MemorySegment;

@GenerateNativeBindings(className = "JGL", classAccess = "", targetPackage = "dev.stjepano.platform.impl.opengl")
interface JGLBindings {
    @NativeBinding
    void jglViewport(int x, int y, int width, int height);

    @NativeBinding
    boolean jglPolygonMode(int mode);

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
    void jglProgramUniform1i(int programId, int location, int i);

    @NativeBinding
    void jglProgramUniform1iv(int programId, int location, int count, MemorySegment valuePtr);

    @NativeBinding
    void jglProgramUniform1f(int programId, int location, float x);

    @NativeBinding
    void jglProgramUniform1fv(int programId, int location, int count, MemorySegment valuePtr);

    @NativeBinding
    void jglProgramUniform2f(int programId, int location, float x, float y);

    @NativeBinding
    void jglProgramUniform2fv(int programId, int location, int count, MemorySegment valuePtr);

    @NativeBinding
    void jglProgramUniform3f(int programId, int location, float x, float y, float z);

    @NativeBinding
    void jglProgramUniform3fv(int programId, int location, int count, MemorySegment valuePtr);

    @NativeBinding
    void jglProgramUniform4f(int programId, int location, float x, float y, float z, float w);

    @NativeBinding
    void jglProgramUniform4fv(int programId, int location, int count, MemorySegment valuePtr);

    @NativeBinding
    void jglProgramUniformMatrix2fv(int programId, int location, int count, boolean transpose, MemorySegment valuePtr);

    @NativeBinding
    void jglProgramUniformMatrix3fv(int programId, int location, int count, boolean transpose, MemorySegment valuePtr);

    @NativeBinding
    void jglProgramUniformMatrix4fv(int programId, int location, int count, boolean transpose, MemorySegment valuePtr);

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
    boolean jglVertexArrayIndexBuffer(int vaoId, int indexBuffer);

    @NativeBinding
    boolean jglVertexArrayVertexAttrib(int vaoId, int attribIndex, int bufferBindingIndex, int numComponent,
                                       int componentType, boolean normalized, int offset);

    @NativeBinding
    boolean jglVertexArrayToggleAttrib(int vaoId, int attribIndex, boolean enabled);

    @NativeBinding
    void jglDeleteVertexArray(int vaoId);

    @NativeBinding
    int jglCreateTexture2D(int mipLevels, int internalFormat, int width, int height, MemorySegment parameterStream, long parameterStreamSize);

    @NativeBinding
    boolean jglTextureUpdateSubImage2D(int textureId, int mipLevel,
                                       int x, int y, int width, int height,
                                       int format, int type,
                                       int rowAlignment, int rowLength,
                                       MemorySegment pixels);

    @NativeBinding
    boolean jglTextureUpdateSubImage2DBuffer(int textureId, int mipLevel,
                                       int x, int y, int width, int height,
                                       int format, int type,
                                       int rowAlignment, int rowLength,
                                       int bufferId, long offset);

    @NativeBinding
    void jglTextureGenerateMipmaps(int textureId);

    @NativeBinding
    boolean jglTextureConfigure(int textureId, MemorySegment parameterStream, long parameterStreamSize);

    @NativeBinding
    int jglCreateSampler(MemorySegment parameterStream, long parameterStreamSize);

    @NativeBinding
    boolean jglSamplerConfigure(int samplerId, MemorySegment parameterStream, long parameterStreamSize);

    @NativeBinding
    void jglDeleteSampler(int samplerId);

    @NativeBinding
    void jglDeleteTexture(int textureId);

    @NativeBinding
    void jglBindTextureUnit(int unit, int textureId);

    @NativeBinding
    void jglBindSamplerUnit(int unit, int samplerId);

    @NativeBinding
    void jglDrawArrays(int mode, int first, int count);

    @NativeBinding
    void jglDrawElements(int mode, int count, int type, long offset);

    @NativeBinding
    boolean jglDepthStateConfigure(MemorySegment depthStatePtr);
}

