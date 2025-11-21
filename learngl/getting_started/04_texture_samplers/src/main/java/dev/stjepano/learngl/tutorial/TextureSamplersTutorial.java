package dev.stjepano.learngl.tutorial;

import dev.stjepano.platform.*;
import dev.stjepano.platform.util.JavaResources;
import dev.stjepano.platform.util.PixelImage;
import dev.stjepano.platform.opengl.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class TextureSamplersTutorial {

    static void main() {
        WindowSettings windowSettings = new WindowSettings(1920, 1080, "LearnOpenGL - Texture samplers", true);
        try (Platform platform = Platform.defaultInstance();
             Window window = platform.createWindow(windowSettings);
             Arena arena = Arena.ofConfined()
        ) {

            Keyboard keyboard = window.keyboard();
            OpenGL gl = platform.openGl();

            gl.viewport(0, 0, window.framebufferWidth(), window.framebufferHeight());

            float[] vertices = new float[] {
                    // top left
                    -0.5f, 0.5f, 0.0f,  1.0f, 1.0f, 1.0f,   0.0f, 1.0f,
                    // bottom left
                    -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 1.0f,   0.0f, 0.0f,
                    // bottom right
                    0.5f, -0.5f, 0.0f,  1.0f, 1.0f, 1.0f,   1.0f, 0.0f,
                    // top right
                    0.5f, 0.5f, 0.0f,   1.0f, 1.0f, 1.0f,   1.0f, 1.0f
            };
            int[] indices = new int[] {
                    0, 1, 2,
                    0, 2, 3
            };
            MemorySegment verticesPtr = arena.allocateFrom(ValueLayout.JAVA_FLOAT, vertices);
            MemorySegment indicesPtr = arena.allocateFrom(ValueLayout.JAVA_INT, indices);

            BufferStorageFlags storageFlags = BufferStorageFlags.builder().build();

            Buffer vertexBuffer = gl.createBuffer(verticesPtr.byteSize(), storageFlags, verticesPtr);
            Buffer indexBuffer = gl.createBuffer(indicesPtr.byteSize(), storageFlags, indicesPtr);

            VertexArray vertexArray = gl.createVertexArray();
            vertexArray.vertexBuffer(0,  vertexBuffer, 0, 8 * (int) GLDataType.FLOAT.byteSize());
            vertexArray.indexBuffer(indexBuffer);

            vertexArray.vertexAttrib(0,  0, 3, GLDataType.FLOAT, false, 0);
            vertexArray.vertexAttrib(1,  0, 3, GLDataType.FLOAT, false, 3 * (int) GLDataType.FLOAT.byteSize());
            vertexArray.vertexAttrib(2, 0, 2, GLDataType.FLOAT, false, 6 * (int) GLDataType.FLOAT.byteSize());

            Program program = gl.createProgram(JavaResources.loadIntoString("/shaders/vertex.glsl"),
                    JavaResources.loadIntoString("/shaders/fragment.glsl"));
            program.setInt(0, 0);   // texture unit 0
            program.setInt(1, 1);   // texture unit 1

            final Texture2DParameters defaultTextureParameters = Texture2DParameters.builder()
                    .build();

            Texture2D containerTexture, faceTexture;
            try (PixelImage image = PixelImage.loadFromResource("/textures/container.png", true)) {
                containerTexture = gl.createTexture2D(OpenGL.ALL_MIPS, GLTexture2DFormat.RGBA8,
                        image.width(), image.height(), defaultTextureParameters);
                containerTexture.update(0,
                        0, 0, image.width(), image.height(),
                        image.dataFormat(), image.dataType(), image.data());
                containerTexture.generateMipmaps();
            }
            try (PixelImage image = PixelImage.loadFromResource("/textures/awesomeface.png", true)) {
                faceTexture = gl.createTexture2D(OpenGL.ALL_MIPS, GLTexture2DFormat.RGBA8, image.width(), image.height(), defaultTextureParameters);
                faceTexture.update(image.dataFormat(), image.dataType(), image.data());
                faceTexture.generateMipmaps();
            }

            final SamplerParameters samplerParameters = SamplerParameters.builder()
                    .minFilter(GLTextureMinFilter.LINEAR_MIPMAP_LINEAR)
                    .magFilter(GLTextureMagFilter.LINEAR)
                    .lod(3.0f, 10.0f, 0.0f)
                    .build();
            final Sampler sampler = gl.createSampler(samplerParameters);

            float brightness;

            Time time = platform.time();

            while (!window.shouldClose()) {
                platform.pollEvents();
                if (keyboard.isPressed(Key.ESCAPE)) {
                    window.setShouldClose(true);
                    continue;
                }
                gl.clearColorBuffer(0.11f, 0.12f, 0.13f, 1.0f);
                brightness = 1.0f - (float)Math.abs(0.2f * Math.cos(2.0f * time.seconds()));
                program.setFloat(0, brightness);

                gl.bindProgram(program);
                gl.bindTextureUnit(0, containerTexture); // texture bound to texture unit 0
                gl.bindTextureUnit(1, faceTexture);
                gl.bindSamplerUnit(0, sampler);
                gl.bindSamplerUnit(1, sampler);
                gl.bindVertexArray(vertexArray);
                gl.drawElements(GLPrimitive.TRIANGLES, 6, GLDataType.UNSIGNED_INT, 0);

                window.swapBuffers();
            }

            sampler.delete();
            faceTexture.delete();
            containerTexture.delete();
            program.delete();
            vertexArray.delete();
            indexBuffer.delete();
            vertexBuffer.delete();
        }

    }

}
