package dev.stjepano.learngl.tutorial;

import dev.stjepano.platform.*;
import dev.stjepano.platform.opengl.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public class TriangleTutorial {

    static void main() {
        WindowSettings windowSettings = new WindowSettings(1920, 1080, "LearnOpenGL - Triangle", true);
        try (Platform platform = Platform.defaultInstance();
             Window window = platform.createWindow(windowSettings);
             Arena arena = Arena.ofConfined()) {

            final Keyboard keyboard = window.keyboard();
            final OpenGL gl = platform.openGl();

            gl.viewport(0, 0, window.framebufferWidth(), window.framebufferHeight());
            GLPolygonRasterMode currentMode = GLPolygonRasterMode.FILL;
            gl.polygonMode(currentMode);

            float[] vertices = new float[] {
                    -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, // top left
                    -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, // bottom left
                    0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom right
                    0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 1.0f // top right
            };
            int[] indices = new int[] {
                    0, 1, 2,
                    0, 2, 3
            };

            MemorySegment verticesPtr = arena.allocateFrom(ValueLayout.JAVA_FLOAT, vertices);
            MemorySegment indicesPtr = arena.allocateFrom(ValueLayout.JAVA_INT, indices);
            BufferStorageFlags storageFlags = BufferStorageFlags.builder()
                    .build();
            Buffer vertexBuffer = gl.createBuffer(verticesPtr.byteSize(), storageFlags, verticesPtr);
            Buffer indexBuffer = gl.createBuffer(indicesPtr.byteSize(), storageFlags, indicesPtr);

            String vertexShaderSrc = loadText("/shaders/vertex.glsl");
            String fragmentShaderSrc = loadText("/shaders/fragment.glsl");
            Program program = gl.createProgram(vertexShaderSrc, fragmentShaderSrc);

            VertexArray vao = gl.createVertexArray();
            vao.vertexBuffer(0, vertexBuffer, 0,  6 * (int)GLDataType.FLOAT.byteSize());
            vao.vertexAttrib(0, 0, 3, GLDataType.FLOAT, false,  0);
            vao.vertexAttrib(1, 0, 3, GLDataType.FLOAT, false, 3 * (int)GLDataType.FLOAT.byteSize());
            vao.indexBuffer(indexBuffer);

            while (!window.shouldClose()) {
                platform.pollEvents();
                gl.clearColorBuffer(0.2f, 0.3f, 0.4f, 1.0f);
                if (keyboard.isPressed(Key.ESCAPE)) {
                    IO.println("ESCAPE pressed");
                    window.setShouldClose(true);
                    continue;
                }
                if (keyboard.wasJustPressed(Key.SPACE)) {
                    currentMode = (currentMode == GLPolygonRasterMode.FILL) ? GLPolygonRasterMode.LINE : GLPolygonRasterMode.FILL;
                    gl.polygonMode(currentMode);
                }

                gl.bindVertexArray(vao);
                gl.bindProgram(program);
                //gl.drawArrays(GLPrimitive.TRIANGLES, 0, 6);
                gl.drawElements(GLPrimitive.TRIANGLES, 6, GLDataType.UNSIGNED_INT, 0L);

                window.swapBuffers();
            }

            vao.delete();
            program.delete();
            indexBuffer.delete();
            vertexBuffer.delete();
        }
    }

    private static String loadText(String resourcePath) throws RuntimeException {
        try (InputStream inputStream = TriangleTutorial.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource " + resourcePath + " not found!");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
