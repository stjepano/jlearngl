# LearnGL in Java

Following tutorials from [https://learnopengl.com/](https://learnopengl.com/) in Java but with my
own native library (not LWJGL or something).

## OpenGL version

Using CORE OpenGL 4.6 instead of OpenGL 3.3 (as in tutorial).

## Building

```shell
./gradlew build
```
This will build everything (including native library).

## Run demos

You can run any demo with command like this (an example):

```shell
./gradlew :learngl:getting_started:01_window:run
```

This will start the demo 01_window in learngl/getting_started directory.

## My own native library

See [native library docs](platform/README.md)