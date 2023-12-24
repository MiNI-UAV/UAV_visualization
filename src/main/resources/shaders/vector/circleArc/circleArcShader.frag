#version 420 core
out vec4 fragColor;

in vec2 fragPos;
in vec2 transformedCenter;

uniform vec4 color;
uniform float gammaCorrection;
uniform vec4 cropRectangle;

uniform float startAngle;
uniform float arcAngle;

const float PI = 3.14159265359;

void main()
{
    vec3 arm = normalize(vec3(fragPos - transformedCenter, 0));
    float fragAngle = atan(arm.y, arm.x) + PI;
    float arc = arcAngle;
    float start = startAngle;
    if(arc < 0) {
        arc = -arc;
        fragAngle = 2*PI - fragAngle;
        start = 2*PI - start;
    }
    arc = arc + start;
    if(arc > 2 * PI) {
        arc = arc - 2 * PI;
    }
    if(fragAngle < start) {
        if (arc < start) {
            if (fragAngle >= arc) discard;
        } else {
            discard;
        }
    } else {
        if(arc >= start && fragAngle >= arc) discard;
    }


    if(fragPos.x < cropRectangle.x || fragPos.y < cropRectangle.y || fragPos.x > cropRectangle.x + cropRectangle.z || fragPos.y > cropRectangle.y + cropRectangle.w)
        discard;
    vec3 result = color.xyz;
    result = pow(result, vec3(1.0/gammaCorrection));
    fragColor = vec4(result, color.w);
}