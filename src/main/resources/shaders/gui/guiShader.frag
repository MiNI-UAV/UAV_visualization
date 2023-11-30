#version 330 core
out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D ourTexture;
uniform float gammaCorrection;
uniform bool useGammaCorrection;

void main()
{
    vec4 color = texture(ourTexture, texCoord);
    vec3 result = color.xyz;
    if(useGammaCorrection)
        result = pow(result, vec3(1.0/gammaCorrection));
    fragColor = vec4(result, color.w);
}