#version 330 core
#define VERTEX_COUNT 20
layout (points) in;
layout (line_strip, max_vertices = VERTEX_COUNT) out;

uniform mat4 view;
uniform mat4 projection;
uniform vec3 trailPoints[VERTEX_COUNT];
uniform int pointCount;
uniform float startingOpacity;

out float opacity;

void main() {
    for(int i=0; i<pointCount; i++)
    {
        opacity = startingOpacity * float(i + VERTEX_COUNT - pointCount) / VERTEX_COUNT;
        gl_Position = projection * view * vec4(trailPoints[i], 1);
        EmitVertex();
    }
    EndPrimitive();
}