package dev.stjepano.learngl.tutorial;

import dev.stjepano.platform.*;
import dev.stjepano.platform.opengl.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public class ShadersTutorial {

    static void main() {
        WindowSettings windowSettings = new WindowSettings(1920, 1080, "LearnOpenGL - Shaders", true);
        try (Platform platform = Platform.defaultInstance();
             Window window = platform.createWindow(windowSettings);
             Arena arena = Arena.ofConfined()) {

            Keyboard keyboard = window.keyboard();
            OpenGL gl = platform.openGl();

            gl.viewport(0, 0, window.framebufferWidth(), window.framebufferHeight());

            float[] vertices = new float[] {
                    // top left
                    -0.5f, 0.5f, 0.0f,  1.0f, 0.0f, 0.0f,
                    // bottom left
                    -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f,
                    // bottom right
                    0.5f, -0.5f, 0.0f,  0.0f, 0.0f, 1.0f,
                    // top right
                    0.5f, 0.5f, 0.0f,   0.0f, 0.0f, 0.0f
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
            vertexArray.vertexBuffer(0,  vertexBuffer, 0, 6 * (int) GLDataType.FLOAT.byteSize());
            vertexArray.indexBuffer(indexBuffer);

            vertexArray.vertexAttrib(0,  0, 3, GLDataType.FLOAT, false, 0);
            vertexArray.vertexAttrib(1,  0, 3, GLDataType.FLOAT, false, 3 * (int) GLDataType.FLOAT.byteSize());

            Program program = gl.createProgram(loadText("/shaders/vertex.glsl"), loadText("/shaders/fragment.glsl"));

            float brightness;

            Time time = platform.time();

            while (!window.shouldClose()) {
                platform.pollEvents();
                if (keyboard.isPressed(Key.ESCAPE)) {
                    window.setShouldClose(true);
                    continue;
                }
                gl.clearColorBuffer(0.11f, 0.12f, 0.13f, 1.0f);
                brightness = (float)Math.abs(Math.cos(2.0f * time.seconds()));
                program.setFloat(0, brightness);

                gl.bindProgram(program);
                gl.bindVertexArray(vertexArray);
                gl.drawElements(GLPrimitive.TRIANGLES, 6, GLDataType.UNSIGNED_INT, 0);

                window.swapBuffers();
            }

            program.delete();
            vertexArray.delete();
            indexBuffer.delete();
            vertexBuffer.delete();


        }


    }

    private static String loadText(String resourcePath) throws RuntimeException {
        try (InputStream inputStream = ShadersTutorial.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource " + resourcePath + " not found!");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
