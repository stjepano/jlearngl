# Platform - a native library wrapper

Access to all native libraries required to build games and cool demos. This wrapper wraps following:

1. GLFW - for window management and input handling
1. GLAD - OpenGL loader
1. OpenGL - core GL 4.6 profile via GLAD
1. ...

## Building

Please make sure you have following available on your system.

1. C compiler (I prefer GCC)
1. CMake: at least version 3.20 
1. glfw3 development package (version 3.x)
1. ...