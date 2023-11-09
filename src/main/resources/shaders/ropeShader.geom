#version 330 core
layout (points) in;
layout (triangle_strip, max_vertices = 16) out;

uniform vec3 pointA;
uniform vec3 pointB;
uniform vec3 color1;
uniform vec3 color2;
uniform float thickness;
uniform float a;
uniform float xOffset;
uniform float yOffset;
uniform bool useLinear;
uniform mat4 view;
uniform mat4 projection;

in VS_OUT {
    float t;
    float tNext;
} gs_in[];

out vec3 out_color;
out vec3 out_normal;
out vec3 fragPos;

vec3 getTangent(vec3 ropeVector, float x, float l);
vec3 getNormal(vec3 ropeVector, float x, float l);
mat3 getRotationMatrix(vec3 axis, float angle);
vec3 getGlobalCoordinates(vec3 pivotPoint, vec3 ropeVector, vec3 normal, float x, float l, float radius);

void main() {
    if(gs_in[0].tNext == -1) return;

    vec3 ropeVector = pointB - pointA;
    vec3 pivotPoint = pointA;
    float l = sqrt(ropeVector.x*ropeVector.x + ropeVector.y*ropeVector.y);

    float x = gs_in[0].t * l;
    vec3 tangent = getTangent(ropeVector, x, l);
    vec3 normal = getNormal(ropeVector, x, l);

    float xNext = gs_in[0].tNext * l;
    vec3 tangentNext = getTangent(ropeVector, xNext, l);
    vec3 normalNext = getNormal(ropeVector, xNext, l);

    int n = 7;
    float radius = thickness / 2;
    float angle = 3.14 * 2 / n;
    mat3 rotM = getRotationMatrix(tangent, angle);
    mat3 rotMNext = getRotationMatrix(tangentNext, angle);

    for(int i=0; i< n+1; i++)
    {
        out_color = i%2==0? color1: color2;

        out_normal = normal;
        fragPos = getGlobalCoordinates(pivotPoint, ropeVector, normal, x, l, radius);
        gl_Position = projection * view * vec4(getGlobalCoordinates(pivotPoint, ropeVector, normal, x, l, radius), 1);
        EmitVertex();

        out_normal = normalNext;
        fragPos = getGlobalCoordinates(pivotPoint, ropeVector, normal, x, l, radius);
        gl_Position = projection * view * vec4(getGlobalCoordinates(pivotPoint, ropeVector, normalNext, xNext, l, radius), 1);
        EmitVertex();

        normal = rotM * normal;
        normalNext = rotMNext * normalNext;
    }
    EndPrimitive();
}

mat3 getRotationMatrix(vec3 axis, float angle) {
    mat3 W = mat3(0, axis.z, -axis.y, -axis.z, 0, axis.x, axis.y, -axis.x, 0);
    return mat3(1.0) + sin(angle) * W + (2*sin(angle/2) * sin(angle/2)) * (W * W);
}

vec3 getGlobalCoordinates(vec3 pivotPoint, vec3 ropeVector, vec3 normal, float x, float l, float radius) {
    return useLinear?
        pivotPoint + vec3(x/l * ropeVector.x, x/l * ropeVector.y, -(a * (x + xOffset) + yOffset)) + radius*normal:
        pivotPoint + vec3(x/l * ropeVector.x, x/l * ropeVector.y, -(a * cosh((x + xOffset) / a) + yOffset)) + radius*normal;
}

vec3 getTangent(vec3 ropeVector, float x, float l) {
    return normalize(useLinear?
        vec3(1/l * ropeVector.x, 1/l * ropeVector.y, -a):
        vec3(1/l * ropeVector.x, 1/l * ropeVector.y, -sinh((x+xOffset)/a)));
}

vec3 getNormal(vec3 ropeVector, float x, float l) {
    return normalize(useLinear?
    vec3(a/l * ropeVector.x, a/l * ropeVector.y, 1):
    vec3(sinh((x+xOffset)/a)/l * ropeVector.x, sinh((x+xOffset)/a)/l * ropeVector.y, 1));
}