cmake_minimum_required(VERSION 3.20)
project(platform C)
set(CMAKE_C_STANDARD 11)

find_package(glfw3 CONFIG REQUIRED)
find_package(OpenGL REQUIRED)

set(LIBNAME platform)
set(LIBSRC
        src/constants.h
        src/common.h
        src/glfw3_wrapper.h
        src/glfw3_wrapper.c
        src/platform.h
        src/platform.c
        external/glad/src/glad.c
        src/glwrap.h
        src/glwrap.c
        src/stbwrap.h
        src/stbwrap.c
)
add_library(${LIBNAME} SHARED ${LIBSRC})
target_link_libraries(${LIBNAME} PRIVATE glfw OpenGL::GL)
target_include_directories(${LIBNAME} PRIVATE external/glad/include external/stb)
target_compile_options(${LIBNAME} PRIVATE -fvisibility=hidden)