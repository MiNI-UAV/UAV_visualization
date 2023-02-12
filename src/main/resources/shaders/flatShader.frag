#version 330 core

flat in vec4 fragColor;

out vec4 outFragColor;

void main() {
    outFragColor = fragColor;
}