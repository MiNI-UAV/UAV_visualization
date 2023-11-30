#version 330 core
out vec4 fragColor;

in vec2 texCoord;
in vec3 normal;
in vec3 fragPos;
in vec4 fragPosDirLightSpace;

uniform sampler2D objectTexture;
uniform sampler2D shadowMap;
uniform vec3 viewPos;
uniform vec3 backgroundColor;
uniform float gammaCorrection;
uniform bool useGammaCorrection;

// Material
struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
    vec3 metallic;
    float roughness;
};
uniform Material material;

// Directional Light
uniform bool useDirectionalLight;
struct DirLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform DirLight dirLight;

// Point Light
struct PointLight {
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
#define NR_POINT_LIGHTS 51
uniform PointLight pointLights[NR_POINT_LIGHTS];

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
#define NR_SPOT_LIGHTS 3
uniform SpotLight spotLights[NR_SPOT_LIGHTS];

vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, vec4 objectColor);
vec3 calcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir);
vec3 calcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir);
float shadowCalculation(vec4 fragPosLightSpace, vec3 lightDir, vec3 normal);

void main()
{
    vec4 objectColor = texture(objectTexture, texCoord);
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 normNormal = normalize(normal);
    vec3 result = backgroundColor;

    // Directional Light
    result = (useDirectionalLight == true)? calcDirLight(dirLight, normNormal, viewDir, objectColor): vec3(0.f); // TODO return *=
    // Point Lights
    //for(int i = 0; i < NR_POINT_LIGHTS; i++)
    //result += calcPointLight(pointLights[i], normNormal, fragPos, viewDir);
    // Spot Lights
    //for(int i = 0; i < NR_SPOT_LIGHTS; i++)
    //result += calcSpotLight(spotLights[i], normNormal, fragPos, viewDir);

    result *= objectColor.xyz;
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

vec3 calcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
{
    vec3 lightDir = normalize(light.position - fragPos);
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
    ambient  *= attenuation;
    diffuse  *= attenuation;
    specular *= attenuation;
    return (ambient + diffuse + specular);
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