package dev.stjepano.learngl.tutorial;

import dev.stjepano.math.Projection;
import dev.stjepano.math.Transform;
import dev.stjepano.math.geometry.mesh.IndexedMesh;
import dev.stjepano.math.geometry.mesh.TriangleMeshGen;
import dev.stjepano.platform.*;
import dev.stjepano.platform.util.JavaResources;
import dev.stjepano.platform.util.PixelImage;
import dev.stjepano.platform.opengl.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class CoordinateSystemsTutorial {

    static void main() {
        new CoordinateSystemsTutorial().runDemo();
    }

    record RenderableMesh(IndexedMesh mesh, VertexArray vertexArray, Buffer vertexBuffer, Buffer indexBuffer) implements AutoCloseable {

        @Override
        public void close() {
            vertexArray.delete();
            vertexBuffer.delete();
            indexBuffer.delete();
        }
    }

    private final Projection projection = new Projection();
    private final Transform capsuleTransform = new Transform();
    private final Transform sphereTransform = new Transform();
    private final Transform cylinderTransform = new Transform();

    private OpenGL gl;

    RenderableMesh capsuleMesh;
    RenderableMesh sphereMesh;
    RenderableMesh cylinderMesh;
    private Program shaderProgram;
    private Texture2D containerTexture;
    private Texture2D smileyTexture;
    private Sampler textureSampler;

    private void runDemo() {
        WindowSettings windowSettings = new WindowSettings(1920, 1080, "LearnOpenGL - Coordinate systems", true);
        try (Platform platform = Platform.defaultInstance();
             Window window = platform.createWindow(windowSettings)) {

            Keyboard keyboard = window.keyboard();
            this.gl = platform.openGl();

            gl.viewport(0, 0, window.framebufferWidth(), window.framebufferHeight());

            final DepthState depthState = DepthState.builder()
                    .enable(true)
                    .build();
            gl.depthState(depthState);

            final CullState cullState = CullState.builder()
                    .enable(true)
                    .cullMode(GLFaceSide.BACK)
                    .frontFaceWinding(GLFaceWinding.CCW)
                    .build();
            gl.cullState(cullState);

            float aspect = (float) window.framebufferWidth() / (float) window.framebufferHeight();
            this.projection.setPerspective((float) Math.toRadians(45.0f), aspect, 0.1f, 100.0f);

            this.capsuleTransform.setPosition(-1, 0.0f, -3.0f).rotateLocalX((float)Math.toRadians(20.0f));
            this.sphereTransform.setPosition(1, 0.0f, -3.0f).rotateLocalX((float)Math.toRadians(20.0f));
            this.cylinderTransform.setPosition(0, 0.0f, -3.0f).rotateLocalX((float)Math.toRadians(20.0f));

            loadMesh();
            loadProgram();
            loadTexturesAndSampler();

            float[] projectionArray = new float[16];
            projection.toMatrixFloatArray(projectionArray);
            shaderProgram.setMat4Array(0, true, projectionArray);
            shaderProgram.setInt(3, 0);   // texture unit 0
            shaderProgram.setInt(4, 1);   // texture unit 1

            final float[] modelMatrixArray = new float[16];
            shaderProgram.setMat4Array(1, true, modelMatrixArray);

            Time time = platform.time();
            double t0 = time.seconds();

            float brightness;
            while (!window.shouldClose()) {
                platform.pollEvents();
                if (keyboard.isPressed(Key.ESCAPE)) {
                    window.setShouldClose(true);
                    continue;
                }
                gl.clearColorBuffer(0.11f, 0.12f, 0.13f, 1.0f);
                gl.clearDepth(1.0f);
                brightness = 1.0f - (float)Math.abs(0.4f * Math.cos(2.0f * time.seconds()));
                shaderProgram.setFloat(2, brightness);


                gl.bindProgram(shaderProgram);
                gl.bindTextureUnit(0, containerTexture); // texture bound to texture unit 0
                gl.bindTextureUnit(1, smileyTexture);
                gl.bindSamplerUnit(0, textureSampler);
                gl.bindSamplerUnit(1, textureSampler);
                // Draw capsule
                capsuleTransform.toMatrixFloatArray(modelMatrixArray, 0);
                shaderProgram.setMat4Array(1, true, modelMatrixArray);
                gl.bindVertexArray(capsuleMesh.vertexArray);
                gl.drawElements(GLPrimitive.TRIANGLES, capsuleMesh.mesh.indices().length, GLDataType.UNSIGNED_INT, 0);
                // Draw sphere
                sphereTransform.toMatrixFloatArray(modelMatrixArray, 0);
                shaderProgram.setMat4Array(1, true, modelMatrixArray);
                gl.bindVertexArray(sphereMesh.vertexArray);
                gl.drawElements(GLPrimitive.TRIANGLES, sphereMesh.mesh.indices().length, GLDataType.UNSIGNED_INT, 0);
                // Draw cylinder
                cylinderTransform.toMatrixFloatArray(modelMatrixArray, 0);
                shaderProgram.setMat4Array(1, true, modelMatrixArray);
                gl.bindVertexArray(cylinderMesh.vertexArray);
                gl.drawElements(GLPrimitive.TRIANGLES, cylinderMesh.mesh.indices().length, GLDataType.UNSIGNED_INT, 0);

                // Update capsule
                float dt = (float) time.frameDeltaSeconds();
                capsuleTransform.rotateLocalY((float)Math.toRadians(20.0f) * dt);
                sphereTransform.rotateLocalY((float)Math.toRadians(20.0f) * dt);
                cylinderTransform.rotateLocalY((float)Math.toRadians(20.0f) * dt);

                window.swapBuffers();
            }

            double t1 = time.seconds();
            double deltaTime = t1 - t0;
            double framesPerSecond = ((double) time.frameIndex()) / deltaTime;
            IO.println("FPS: " + framesPerSecond);

            deleteTexturesAndSampler();
            deleteProgram();
            deleteMesh();
        }
    }

    private void loadTexturesAndSampler() {
        final Texture2DParameters defaultTextureParameters = Texture2DParameters.builder()
                .build();

        final SamplerParameters samplerParameters = SamplerParameters.builder()
                .minFilter(GLTextureMinFilter.LINEAR_MIPMAP_LINEAR)
                .magFilter(GLTextureMagFilter.LINEAR)
                .maxAnisotropy(16.0f)
                .build();
        this.textureSampler = gl.createSampler(samplerParameters);

        try (PixelImage image = PixelImage.loadFromResource("/textures/container.png", true)) {
            containerTexture = gl.createTexture2D(OpenGL.ALL_MIPS, GLTexture2DFormat.RGBA8,
                    image.width(), image.height(), defaultTextureParameters);
            containerTexture.update(0,
                    0, 0, image.width(), image.height(),
                    image.dataFormat(), image.dataType(), image.data());
            containerTexture.generateMipmaps();
        }
        try (PixelImage image = PixelImage.loadFromResource("/textures/awesomeface.png", true)) {
            smileyTexture = gl.createTexture2D(OpenGL.ALL_MIPS, GLTexture2DFormat.RGBA8, image.width(), image.height(), defaultTextureParameters);
            smileyTexture.update(image.dataFormat(), image.dataType(), image.data());
            smileyTexture.generateMipmaps();
        }
    }

    void deleteTexturesAndSampler() {
        smileyTexture.delete();
        containerTexture.delete();
        textureSampler.delete();
    }

    private void loadProgram() {
        this.shaderProgram = gl.createProgram(JavaResources.loadIntoString("/shaders/vertex.glsl"),
                JavaResources.loadIntoString("/shaders/fragment.glsl"));
    }

    private void deleteProgram() {
        this.shaderProgram.delete();
    }


    private void loadMesh() {
        try (Arena arena = Arena.ofConfined()) {
            TriangleMeshGen meshGen = new TriangleMeshGen(IndexedMesh.VertexFormat.AttributeType.COLOR, IndexedMesh.VertexFormat.AttributeType.TEX_COORD);
            meshGen.addCapsule(0.5f, 0.5f, 16, 6);
            IndexedMesh capsuleMesh = meshGen.build();

            MemorySegment verticesPtr = arena.allocateFrom(ValueLayout.JAVA_FLOAT, capsuleMesh.vertexData());
            MemorySegment indicesPtr = arena.allocateFrom(ValueLayout.JAVA_INT, capsuleMesh.indices());

            BufferStorageFlags storageFlags = BufferStorageFlags.builder().build();

            Buffer capsuleVertexBuffer = gl.createBuffer(verticesPtr.byteSize(), storageFlags, verticesPtr);
            Buffer capsuleIndexBuffer = gl.createBuffer(indicesPtr.byteSize(), storageFlags, indicesPtr);

            VertexArray capsuleVertexArray = gl.createVertexArray();
            capsuleVertexArray.vertexBuffer(0, capsuleVertexBuffer, 0, capsuleMesh.vertexFormat().stride() * (int) GLDataType.FLOAT.byteSize());
            capsuleVertexArray.indexBuffer(capsuleIndexBuffer);

            capsuleVertexArray.vertexAttrib(0, 0, 3, GLDataType.FLOAT, false, 0);
            capsuleVertexArray.vertexAttrib(1, 0, 3, GLDataType.FLOAT, false, capsuleMesh.vertexFormat().colorOffset() * (int) GLDataType.FLOAT.byteSize());
            capsuleVertexArray.vertexAttrib(2, 0, 2, GLDataType.FLOAT, false, capsuleMesh.vertexFormat().textureCoordinateOffset() * (int) GLDataType.FLOAT.byteSize());

            this.capsuleMesh = new RenderableMesh(capsuleMesh, capsuleVertexArray, capsuleVertexBuffer, capsuleIndexBuffer);

            meshGen.addSphere(0.5f, 16, 16);
            IndexedMesh sphereMesh = meshGen.build();

            MemorySegment sphereVerticesPtr = arena.allocateFrom(ValueLayout.JAVA_FLOAT, sphereMesh.vertexData());
            MemorySegment sphereIndicesPtr = arena.allocateFrom(ValueLayout.JAVA_INT, sphereMesh.indices());

            Buffer sphereVertexBuffer = gl.createBuffer(sphereVerticesPtr.byteSize(), storageFlags, sphereVerticesPtr);
            Buffer sphereIndexBuffer = gl.createBuffer(sphereIndicesPtr.byteSize(), storageFlags, sphereIndicesPtr);

            VertexArray sphereVertexArray = gl.createVertexArray();
            sphereVertexArray.vertexBuffer(0, sphereVertexBuffer, 0, sphereMesh.vertexFormat().stride() * (int) GLDataType.FLOAT.byteSize());
            sphereVertexArray.indexBuffer(sphereIndexBuffer);

            sphereVertexArray.vertexAttrib(0, 0, 3, GLDataType.FLOAT, false, 0);
            sphereVertexArray.vertexAttrib(1, 0, 3, GLDataType.FLOAT, false, capsuleMesh.vertexFormat().colorOffset() * (int) GLDataType.FLOAT.byteSize());
            sphereVertexArray.vertexAttrib(2, 0, 2, GLDataType.FLOAT, false, capsuleMesh.vertexFormat().textureCoordinateOffset() * (int) GLDataType.FLOAT.byteSize());

            this.sphereMesh = new RenderableMesh(sphereMesh, sphereVertexArray, sphereVertexBuffer, sphereIndexBuffer);

            //meshGen.addPyramid(1.0f, 1.0f, 1.0f);
            //meshGen.addCone(0.5f, 1.0f, 16, 4);
            meshGen.addCylinder(0.5f, 1.0f, 16, 1, true);
            IndexedMesh cylinderMesh = meshGen.build();

            MemorySegment cylinderVerticesPtr = arena.allocateFrom(ValueLayout.JAVA_FLOAT, cylinderMesh.vertexData());
            MemorySegment cylinderIndicesPtr = arena.allocateFrom(ValueLayout.JAVA_INT, cylinderMesh.indices());

            Buffer cylinderVertexBuffer = gl.createBuffer(cylinderVerticesPtr.byteSize(), storageFlags, cylinderVerticesPtr);
            Buffer cylinderIndexBuffer = gl.createBuffer(cylinderIndicesPtr.byteSize(), storageFlags, cylinderIndicesPtr);

            VertexArray cylinderVertexArray = gl.createVertexArray();
            cylinderVertexArray.vertexBuffer(0, cylinderVertexBuffer, 0, cylinderMesh.vertexFormat().stride() * (int) GLDataType.FLOAT.byteSize());
            cylinderVertexArray.indexBuffer(cylinderIndexBuffer);

            cylinderVertexArray.vertexAttrib(0, 0, 3, GLDataType.FLOAT, false, 0);
            cylinderVertexArray.vertexAttrib(1, 0, 3, GLDataType.FLOAT, false, cylinderMesh.vertexFormat().colorOffset() * (int) GLDataType.FLOAT.byteSize());
            cylinderVertexArray.vertexAttrib(2, 0, 2, GLDataType.FLOAT, false, cylinderMesh.vertexFormat().textureCoordinateOffset() * (int) GLDataType.FLOAT.byteSize());

            this.cylinderMesh = new RenderableMesh(cylinderMesh, cylinderVertexArray, cylinderVertexBuffer, cylinderIndexBuffer);
        }
    }

    private void deleteMesh() {
        capsuleMesh.close();
        sphereMesh.close();
    }
}
