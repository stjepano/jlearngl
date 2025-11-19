#version 460 core
in vec3 vColor;

out vec4 oFragColor;

void main() {
    oFragColor = vec4(vColor.r, vColor.g, vColor.b, 1.0);
}