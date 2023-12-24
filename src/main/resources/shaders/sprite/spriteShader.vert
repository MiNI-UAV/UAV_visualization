#version 420 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

out vec2 texCoord;
uniform mat3x2 transform;

void main() {
    gl_Position = vec4(aPos, 1.0f, 1.0f);
    texCoord = transform * vec3(aTexCoords, 1.f);
}
