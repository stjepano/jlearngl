package dev.stjepano.learngl.tutorial;

import dev.stjepano.math.Projection;
import dev.stjepano.math.Transform;
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

    private final Projection projection = new Projection();
    private final Transform modelTransform = new Transform();

    private OpenGL gl;

    private VertexArray meshVertexArray;
    private Buffer meshVertexBuffer;
    private Buffer meshIndexBuffer;
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

            float aspect = (float) window.framebufferWidth() / (float) window.framebufferHeight();
            this.projection.setPerspective((float) Math.toRadians(45.0f), aspect, 0.1f, 100.0f);

            this.modelTransform.setPosition(0.0f, 0.0f, -3.0f)
                    .setRotationX((float) Math.toRadians(-55.0f));

            loadMesh();
            loadProgram();
            loadTexturesAndSampler();

            float[] projectionArray = new float[16];
            projection.toMatrixFloatArray(projectionArray);
            shaderProgram.setMat4Array(0, true, projectionArray);
            shaderProgram.setInt(3, 0);   // texture unit 0
            shaderProgram.setInt(4, 1);   // texture unit 1

            float[] worldArray = new float[16];
            modelTransform.toMatrixFloatArray(worldArray, 0);
            shaderProgram.setMat4Array(1, true, worldArray);

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
                brightness = 1.0f - (float)Math.abs(0.4f * Math.cos(2.0f * time.seconds()));
                shaderProgram.setFloat(2, brightness);


                gl.bindProgram(shaderProgram);
                gl.bindTextureUnit(0, containerTexture); // texture bound to texture unit 0
                gl.bindTextureUnit(1, smileyTexture);
                gl.bindSamplerUnit(0, textureSampler);
                gl.bindSamplerUnit(1, textureSampler);
                gl.bindVertexArray(meshVertexArray);
                gl.drawElements(GLPrimitive.TRIANGLES, 6, GLDataType.UNSIGNED_INT, 0);

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
                .minFilter(GLTextureMinFilter.NEAREST)
                .magFilter(GLTextureMagFilter.NEAREST)
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
            float[] vertices = new float[]{
                    // top left
                    -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                    // bottom left
                    -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                    // bottom right
                    0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                    // top right
                    0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f
            };
            int[] indices = new int[]{
                    0, 1, 2,
                    0, 2, 3
            };
            MemorySegment verticesPtr = arena.allocateFrom(ValueLayout.JAVA_FLOAT, vertices);
            MemorySegment indicesPtr = arena.allocateFrom(ValueLayout.JAVA_INT, indices);

            BufferStorageFlags storageFlags = BufferStorageFlags.builder().build();

            meshVertexBuffer = gl.createBuffer(verticesPtr.byteSize(), storageFlags, verticesPtr);
            meshIndexBuffer = gl.createBuffer(indicesPtr.byteSize(), storageFlags, indicesPtr);

            meshVertexArray = gl.createVertexArray();
            meshVertexArray.vertexBuffer(0, meshVertexBuffer, 0, 8 * (int) GLDataType.FLOAT.byteSize());
            meshVertexArray.indexBuffer(meshIndexBuffer);

            meshVertexArray.vertexAttrib(0, 0, 3, GLDataType.FLOAT, false, 0);
            meshVertexArray.vertexAttrib(1, 0, 3, GLDataType.FLOAT, false, 3 * (int) GLDataType.FLOAT.byteSize());
            meshVertexArray.vertexAttrib(2, 0, 2, GLDataType.FLOAT, false, 6 * (int) GLDataType.FLOAT.byteSize());
        }
    }

    private void deleteMesh() {
        meshVertexArray.delete();
        meshVertexBuffer.delete();
        meshIndexBuffer.delete();
    }
}
