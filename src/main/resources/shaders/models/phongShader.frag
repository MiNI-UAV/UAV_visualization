#version 330 core
out vec4 fragColor;

in vec2 texCoord;
in vec3 normal;
in vec3 fragPos;
in vec4 fragPosDirLightSpace;

uniform bool useTexture;
uniform sampler2D objectTexture;
uniform sampler2D shadowMap;
uniform vec3 viewPos;
uniform vec3 backgroundColor;
uniform float gammaCorrection;
uniform bool useGammaCorrection;

// Material
struct Material {
    vec4 baseColor;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
    vec3 metallic;
    float roughness;
};
uniform Material material;

// Directional Light
struct DirLight {
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform DirLight dirLight;

// Spot Light
struct SpotLight {
    vec3 position;
    vec3  direction;
    float cutOff;
    float outerCutOff;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform SpotLight spotLight;
uniform bool spotLightOn;

// Fog
struct Fog {
    vec3 color;
    float density;
};
uniform Fog fog;

vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, vec4 objectColor);
vec3 calcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir);
float shadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal);
float getFogFactor(Fog fog, float fogCoordinate);

void main()
{
    vec4 objectColor = (useTexture == true) ? texture(objectTexture, texCoord) : material.baseColor;
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 normNormal = normalize(normal);
    vec3 result = backgroundColor;

    result = calcDirLight(dirLight, normNormal, viewDir, objectColor);
    result += (spotLightOn == true)? calcSpotLight(spotLight, normNormal, fragPos, viewDir): vec3(0);
    result *= objectColor.xyz;
    result = mix(result, fog.color, getFogFactor(fog, length(viewPos - fragPos)));

    if(useGammaCorrection)
        result = pow(result, vec3(1.0/gammaCorrection));
    fragColor = vec4(result, objectColor.w);
}

vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, vec4 objectColor)
{
    vec3 lightDir = normalize(-light.direction);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    float kPi = 3.14159265;
    float kEnergyConservation = ( 8.0 + material.shininess ) / ( 8.0 * kPi );
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = kEnergyConservation * pow(max(dot(normal, halfwayDir), 0.0), material.shininess);
    //vec3 reflectDir = reflect(-lightDir, normal);
    //float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // PBR
    float gloss = 1.0-material.roughness;
    vec3 diffuseColor = mix(objectColor.xyz, vec3(0), material.metallic);
    vec3 specularColor = mix(vec3(1), objectColor.xyz, material.metallic);

    // combine results
    vec3 ambient  = light.ambient  * material.ambient;
    vec3 diffuse  = light.diffuse  * diff * material.diffuse;
    vec3 specular = light.specular * spec * material.specular;
    //vec3 ambient = light.ambient;
    //vec3 diffuse = light.diffuse * diff * diffuseColor;
    //vec3 specular = light.specular * spec * specularColor; // TODO PBR model

    float shadow = shadowCalculation(fragPosDirLightSpace, lightDir, normal);
    return (ambient + (1.0 - shadow) * (diffuse + specular));
}

float shadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal)
{
    // perform perspective divide
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    if(projCoords.z > 1.0)
        return 0.0;
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;
    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);
    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    for(int x = -1; x <= 1; ++x)
    {
        for(int y = -1; y <= 1; ++y)
        {
            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }
    shadow /= 9.0;
    return shadow;
}

vec3 calcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
{
    vec3 lightDir = normalize(light.position - fragPos);

    float theta     = dot(lightDir, normalize(-light.direction));
    float epsilon   = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);

    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance    = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance +
    light.quadratic * (distance * distance));
    // combine results
    vec3 ambient  = light.ambient  * material.ambient;
    vec3 diffuse  = light.diffuse  * diff * material.diffuse;
    vec3 specular = light.specular * spec * material.specular;
    ambient  *= attenuation * intensity;
    diffuse  *= attenuation * intensity;
    specular *= attenuation * intensity;
    return (ambient + diffuse + specular);
}

float getFogFactor(Fog fog, float fogCoordinate)
{
    float result = 0.0;
    result = exp(-pow(fog.density * fogCoordinate, 2.0));
    result = 1.0 - clamp(result, 0.0, 1.0);
    return result;
}
