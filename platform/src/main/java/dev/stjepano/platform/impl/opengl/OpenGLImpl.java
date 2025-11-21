package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.opengl.*;
import dev.stjepano.platform.memory.StackAllocator;

import java.lang.foreign.*;
import java.util.ArrayList;
import java.util.List;

public class OpenGLImpl implements OpenGL {

    private static final int GL_COLOR = 0x1800;
    private static final int GL_DEPTH = 0x1801;
    private static final int GL_STENCIL = 0x1802;
    private static final int GL_DEPTH_AND_STENCIL = 0x84f9;

    private static final Texture2DParameters BASELINE_TEXTURE_PARAMETERS = Texture2DParameters.builder().build();
    private static final SamplerParameters BASELINE_SAMPLER_PARAMETERS = SamplerParameters.builder().build();

    public sealed interface GLParameter { }
    public record IntParameter(int name, int value) implements GLParameter { }
    public record FloatParameter(int name, float value) implements GLParameter { }
    public record BorderColorParameter(int name, Float4 value) implements GLParameter { }

    public static final int GL_TEXTURE_MIN_FILTER = 0x2801;
    public static final int GL_TEXTURE_MAG_FILTER = 0x2800;
    public static final int GL_TEXTURE_WRAP_S = 0x2802;
    public static final int GL_TEXTURE_WRAP_T = 0x2803;
    public static final int GL_TEXTURE_WRAP_R = 0x8072;
    public static final int GL_TEXTURE_BORDER_COLOR = 0x1004;
    public static final int GL_TEXTURE_MIN_LOD = 0x813A;
    public static final int GL_TEXTURE_MAX_LOD = 0x813B;
    public static final int GL_TEXTURE_LOD_BIAS = 0x8501;
    public static final int GL_TEXTURE_BASE_LEVEL = 0x813C;
    public static final int GL_TEXTURE_MAX_LEVEL = 0x813D;
    public static final int GL_TEXTURE_MAX_ANISOTROPY = 0x84FE;
    public static final int GL_TEXTURE_COMPARE_MODE = 0x884C;
    public static final int GL_TEXTURE_COMPARE_FUNC = 0x884D;

    @Override
    public void viewport(int x, int y, int width, int height) {
        JGL.jglViewport(x, y, width, height);
    }

    @Override
    public void polygonMode(GLPolygonRasterMode mode) {
        if (!JGL.jglPolygonMode(mode.glEnumValue())) {
            throw new OpenGLException("Failed to set polygon raster mode " + mode);
        }
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
    public void bindTextureUnit(int unit, Texture2D texture) {
        if (!(texture instanceof Texture2DImpl textureImpl)) {
            throw new IllegalStateException("texture not instance of TextureImpl");
        }
        JGL.jglBindTextureUnit(unit, textureImpl.glId);
    }

    @Override
    public void bindSamplerUnit(int unit, Sampler sampler) {
        if (!(sampler instanceof SamplerImpl samplerImpl)) {
            throw new IllegalStateException("sampler not instance of SamplerImpl");
        }
        JGL.jglBindSamplerUnit(unit, samplerImpl.glId);
    }

    public static List<GLParameter> textureParametersDelta(Texture2DParameters parameters, Texture2DParameters referenceParameters) {
        final List<GLParameter> delta = new ArrayList<>(16);
        if (parameters.minFilter() != referenceParameters.minFilter()) {
            delta.add(new IntParameter(GL_TEXTURE_MIN_FILTER, parameters.minFilter().glEnumValue()));
        }
        if (parameters.magFilter() != referenceParameters.magFilter()) {
            delta.add(new IntParameter(GL_TEXTURE_MAG_FILTER, parameters.magFilter().glEnumValue()));
        }
        if (parameters.wrapS() != referenceParameters.wrapS()) {
            delta.add(new IntParameter(GL_TEXTURE_WRAP_S, parameters.wrapS().glEnumValue()));
        }
        if (parameters.wrapT() != referenceParameters.wrapT()) {
            delta.add(new IntParameter(GL_TEXTURE_WRAP_T, parameters.wrapT().glEnumValue()));
        }
        if (!parameters.borderColor().equals(referenceParameters.borderColor())) {
            delta.add(new BorderColorParameter(GL_TEXTURE_BORDER_COLOR, parameters.borderColor()));
        }
        if (parameters.minLod() != referenceParameters.minLod()) {
            delta.add(new FloatParameter(GL_TEXTURE_MIN_LOD, parameters.minLod()));
        }
        if (parameters.maxLod() != referenceParameters.maxLod()) {
            delta.add(new FloatParameter(GL_TEXTURE_MAX_LOD, parameters.maxLod()));
        }
        if (parameters.lodBias() != referenceParameters.lodBias()) {
            delta.add(new FloatParameter(GL_TEXTURE_LOD_BIAS, parameters.lodBias()));
        }
        if (parameters.baseLevel() != referenceParameters.baseLevel()) {
            delta.add(new IntParameter(GL_TEXTURE_BASE_LEVEL, parameters.baseLevel()));
        }
        if (parameters.maxLevel() != referenceParameters.maxLevel()) {
            delta.add(new IntParameter(GL_TEXTURE_MAX_LEVEL, parameters.maxLevel()));
        }
        if (parameters.maxAnisotropy() != referenceParameters.maxAnisotropy()) {
            delta.add(new FloatParameter(GL_TEXTURE_MAX_ANISOTROPY, parameters.maxAnisotropy()));
        }
        if (parameters.textureCompareMode() != referenceParameters.textureCompareMode()) {
            delta.add(new IntParameter(GL_TEXTURE_COMPARE_MODE, parameters.textureCompareMode().glEnumValue()));
        }
        if (parameters.textureCompareFunc() != referenceParameters.textureCompareFunc()) {
            delta.add(new IntParameter(GL_TEXTURE_COMPARE_FUNC, parameters.textureCompareFunc().glEnumValue()));
        }
        return delta;
    }

    public static long calculateParametersStreamSize(List<GLParameter> delta) {
        long byteSize = 0;
        if (!delta.isEmpty()) {
            byteSize = ValueLayout.JAVA_INT.byteSize(); // for one extra int (terminator)
            for (var texParameter : delta) {
                //noinspection IfCanBeSwitch
                if (texParameter instanceof IntParameter) {
                    byteSize += 2 * ValueLayout.JAVA_INT.byteSize();
                } else if (texParameter instanceof FloatParameter) {
                    byteSize += ValueLayout.JAVA_INT.byteSize() + ValueLayout.JAVA_FLOAT.byteSize();
                } else if (texParameter instanceof BorderColorParameter) {
                    byteSize += ValueLayout.JAVA_INT.byteSize() + 4 * ValueLayout.JAVA_FLOAT.byteSize();
                } else {
                    throw new RuntimeException("Unsupported texture parameter type: " + texParameter.getClass().getName());
                }
            }
        }
        return byteSize;
    }

    public static void encodeParametersStream(List<GLParameter> delta, MemorySegment stream) {
        long offset = 0;
        for (var texParameter : delta) {
            //noinspection IfCanBeSwitch
            if (texParameter instanceof IntParameter(int name, int value)) {
                stream.set(ValueLayout.JAVA_INT, offset, name);
                stream.set(ValueLayout.JAVA_INT, offset + ValueLayout.JAVA_INT.byteSize(), value);
                offset += 2 * ValueLayout.JAVA_INT.byteSize();
            } else if (texParameter instanceof FloatParameter(int name, float value)) {
                stream.set(ValueLayout.JAVA_INT, offset, name);
                stream.set(ValueLayout.JAVA_FLOAT, offset + ValueLayout.JAVA_INT.byteSize(), value);
                offset += ValueLayout.JAVA_INT.byteSize() + ValueLayout.JAVA_FLOAT.byteSize();
            } else if (texParameter instanceof BorderColorParameter(int name, Float4 color)) {
                stream.set(ValueLayout.JAVA_INT, offset, name);
                stream.set(ValueLayout.JAVA_FLOAT, offset + ValueLayout.JAVA_INT.byteSize(), color.x());
                stream.set(ValueLayout.JAVA_FLOAT, offset + ValueLayout.JAVA_INT.byteSize() + ValueLayout.JAVA_FLOAT.byteSize(), color.y());
                stream.set(ValueLayout.JAVA_FLOAT, offset + ValueLayout.JAVA_INT.byteSize() + 2 * ValueLayout.JAVA_FLOAT.byteSize(), color.z());
                stream.set(ValueLayout.JAVA_FLOAT, offset + ValueLayout.JAVA_INT.byteSize() + 3 * ValueLayout.JAVA_FLOAT.byteSize(), color.w());
                offset += ValueLayout.JAVA_INT.byteSize() + 4 * ValueLayout.JAVA_FLOAT.byteSize();
            } else {
                throw new RuntimeException("Unsupported texture parameter type: " + texParameter.getClass().getName());
            }
        }
        stream.set(ValueLayout.JAVA_INT, offset, 0);
    }

    public static List<GLParameter> samplerParametersDelta(SamplerParameters parameters, SamplerParameters referenceParameters) {
        final List<GLParameter> delta = new ArrayList<>(16);
        if (parameters.minFilter() != referenceParameters.minFilter()) {
            delta.add(new IntParameter(GL_TEXTURE_MIN_FILTER, parameters.minFilter().glEnumValue()));
        }
        if (parameters.magFilter() != referenceParameters.magFilter()) {
            delta.add(new IntParameter(GL_TEXTURE_MAG_FILTER, parameters.magFilter().glEnumValue()));
        }
        if (parameters.wrapS() != referenceParameters.wrapS()) {
            delta.add(new IntParameter(GL_TEXTURE_WRAP_S, parameters.wrapS().glEnumValue()));
        }
        if (parameters.wrapT() != referenceParameters.wrapT()) {
            delta.add(new IntParameter(GL_TEXTURE_WRAP_T, parameters.wrapT().glEnumValue()));
        }
        if (parameters.wrapR() != referenceParameters.wrapR()) {
            delta.add(new IntParameter(GL_TEXTURE_WRAP_R, parameters.wrapR().glEnumValue()));
        }
        if (!parameters.borderColor().equals(referenceParameters.borderColor())) {
            delta.add(new BorderColorParameter(GL_TEXTURE_BORDER_COLOR, parameters.borderColor()));
        }
        if (parameters.minLod() != referenceParameters.minLod()) {
            delta.add(new FloatParameter(GL_TEXTURE_MIN_LOD, parameters.minLod()));
        }
        if (parameters.maxLod() != referenceParameters.maxLod()) {
            delta.add(new FloatParameter(GL_TEXTURE_MAX_LOD, parameters.maxLod()));
        }
        if (parameters.lodBias() != referenceParameters.lodBias()) {
            delta.add(new FloatParameter(GL_TEXTURE_LOD_BIAS, parameters.lodBias()));
        }
        if (parameters.maxAnisotropy() != referenceParameters.maxAnisotropy()) {
            delta.add(new FloatParameter(GL_TEXTURE_MAX_ANISOTROPY, parameters.maxAnisotropy()));
        }
        if (parameters.textureCompareMode() != referenceParameters.textureCompareMode()) {
            delta.add(new IntParameter(GL_TEXTURE_COMPARE_MODE, parameters.textureCompareMode().glEnumValue()));
        }
        if (parameters.textureCompareFunc() != referenceParameters.textureCompareFunc()) {
            delta.add(new IntParameter(GL_TEXTURE_COMPARE_FUNC, parameters.textureCompareFunc().glEnumValue()));
        }
        return delta;
    }

    @Override
    public Texture2D createTexture2D(int mipLevels, GLTexture2DFormat internalFormat, int width, int height, Texture2DParameters parameters) {
        if (mipLevels < 0) {
            throw new IllegalArgumentException("Invalid mipLevels: " + mipLevels);
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid texture size. Width = " + width +  " height = " + height);
        }
        if (mipLevels == ALL_MIPS) {
            mipLevels = Texture2D.calculateMipLevels(width, height);
        }
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment textureParameterStream = MemorySegment.NULL;
            List<GLParameter> delta = textureParametersDelta(parameters, BASELINE_TEXTURE_PARAMETERS);
            if (!delta.isEmpty()) {
                long deltaStreamSize = calculateParametersStreamSize(delta);
                textureParameterStream = stack.allocate(deltaStreamSize, 8);
                encodeParametersStream(delta, textureParameterStream);
            }

            int glId = JGL.jglCreateTexture2D(mipLevels, internalFormat.glEnumValue(), width, height, textureParameterStream, textureParameterStream.byteSize());
            if (glId == 0) {
                throw new OpenGLException("Failed to create 2D texture!");
            }

            return new Texture2DImpl(glId, width, height, mipLevels, internalFormat, parameters);
        }
    }

    @Override
    public Sampler createSampler(SamplerParameters parameters) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment samplerParametersStream = MemorySegment.NULL;
            List<GLParameter> delta = samplerParametersDelta(parameters, BASELINE_SAMPLER_PARAMETERS);
            if (!delta.isEmpty()) {
                long deltaStreamSize = calculateParametersStreamSize(delta);
                samplerParametersStream = stack.allocate(deltaStreamSize, 8);
                encodeParametersStream(delta, samplerParametersStream);
            }

            int glId = JGL.jglCreateSampler(samplerParametersStream, samplerParametersStream.byteSize());
            if (glId == 0) {
                throw new OpenGLException("Failed to create sampler.");
            }

            return new SamplerImpl(glId, parameters);
        }
    }

    @Override
    public void drawArrays(GLPrimitive primitiveType, int first, int count) {
        JGL.jglDrawArrays(primitiveType.glEnumValue(), first, count);
    }

    @Override
    public void drawElements(GLPrimitive primitiveType, int count, GLDataType indexType, long offset) {
        if (!(indexType == GLDataType.UNSIGNED_BYTE || indexType == GLDataType.UNSIGNED_SHORT || indexType == GLDataType.UNSIGNED_INT)) {
            throw new OpenGLException("indexType not supported");
        }
        JGL.jglDrawElements(primitiveType.glEnumValue(), count, indexType.glEnumValue(), offset);
    }
}
