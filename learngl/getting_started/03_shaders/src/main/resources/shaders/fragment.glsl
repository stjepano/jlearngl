#version 460 core
in vec3 vColor;

out vec4 oFragColor;

layout(location = 0) uniform float uBrightness;

void main() {
    oFragColor = vec4(vColor.r * uBrightness, vColor.g * uBrightness, vColor.b * uBrightness, 1.0);
}