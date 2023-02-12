#version 330 core
out vec4 fragColor;

in vec2 texCoord;
in vec3 fragPos;

uniform sampler2D ourTexture;
uniform vec3 lightColor;
uniform vec3 viewPos;
uniform vec3 backgroundColor;

// Fog
struct Fog {
    vec3 color;
    float density;
    bool useFog;
};
uniform Fog fog;

float getFogFactor(Fog fog, float fogCoordinate);

void main()
{
    float brightness = 0.8;
    vec4 objectColor = texture(ourTexture, texCoord);
    vec3 result = lightColor * brightness + (1-brightness) * objectColor.xyz;

    if(fog.useFog == true)
        result = mix(result, fog.color, getFogFactor(fog, length(viewPos - fragPos)));

    fragColor = vec4(result, objectColor.w);
}

float getFogFactor(Fog fog, float fogCoordinate)
{
    float result = 0.0;
    result = exp(-pow(fog.density * fogCoordinate, 2.0));
    result = 1.0 - clamp(result, 0.0, 1.0);
    return result;
}