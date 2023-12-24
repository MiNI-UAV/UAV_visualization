#version 330 core
out vec4 fragColor;

in vec2 texCoord;
in vec3 normal;
in vec3 fragPos;
in vec4 fragPosDirLightSpace;

uniform bool useAlbedoMap;
uniform sampler2D albedoMap;

uniform bool useNormalMap;
uniform sampler2D normalMap;

uniform bool useMetallicRoughnessMap;
uniform sampler2D metallicRoughnessMap;

uniform bool useAmbientOcclusionMap;
uniform sampler2D ambientOcclusionMap;

uniform sampler2D shadowMap;

uniform vec3 viewPos;
uniform vec3 backgroundColor;
uniform float gammaCorrection;
uniform bool useGammaCorrection;

// Material
struct Material {
    vec4 albedo;
    float normalScale;
    float metallic;
    float roughness;
    float aoStrength;
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
uniform PointLight cameraPointLight;

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

const float PI = 3.14159265359;

float getFogFactor(Fog fog, float fogCoordinate)
{
    float result = 0.0;
    result = exp(-pow(fog.density * fogCoordinate, 2.0));
    result = 1.0 - clamp(result, 0.0, 1.0);
    return result;
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

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

float DistributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;

    float num = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return num / denom;
}

float GeometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float num   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return num / denom;
}

float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2  = GeometrySchlickGGX(NdotV, roughness);
    float ggx1  = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

vec3 getNormalFromMap()
{
    if(useNormalMap == false) return normalize(normal);
    vec3 tangentNormal = texture(normalMap, texCoord).xyz * 2.0 - 1.0;

    vec3 Q1  = dFdx(fragPos);
    vec3 Q2  = dFdy(fragPos);
    vec2 st1 = dFdx(texCoord);
    vec2 st2 = dFdy(texCoord);

    vec3 N   = normalize(normal);
    vec3 T  = normalize(Q1*st2.t - Q2*st1.t);
    vec3 B  = -normalize(cross(N, T));
    mat3 TBN = mat3(T, B, N);

    vec3 result = TBN * tangentNormal;
    result = vec3(result.xy * material.normalScale, result.z);
    return normalize(result);
}

void main() {
    vec4 albedo = (useAlbedoMap == true) ? texture(albedoMap, texCoord) * material.albedo: material.albedo;
    float metallic  = (useMetallicRoughnessMap == true) ? texture(metallicRoughnessMap, texCoord).b * material.metallic: material.metallic;
    float roughness = (useMetallicRoughnessMap == true) ? texture(metallicRoughnessMap, texCoord).g * material.roughness: material.roughness;
    float ao        = (useAmbientOcclusionMap == true) ? texture(ambientOcclusionMap, texCoord).r: 1.0f;
    ao = 1.0 + material.aoStrength * (ao - 1.0);

    vec3 N = getNormalFromMap();
    vec3 V = normalize(viewPos - fragPos);

    const int DIRECTIONAL_LIGHTS = 1;
    vec3 Lo = vec3(0.0);
    for(int i = 0; i < DIRECTIONAL_LIGHTS; ++i)
    {
        vec3 L = normalize(dirLight.direction);
        vec3 H = normalize(V + L);
        vec3 radiance = dirLight.diffuse;
        // Fresnel
        vec3 F0 = vec3(0.04);
        F0 = mix(F0, albedo.rgb, metallic);
        vec3 F  = fresnelSchlick(max(dot(H, V), 0.0), F0);

        float NDF = DistributionGGX(N, H, roughness);
        float G   = GeometrySmith(N, V, L, roughness);

        vec3 numerator    = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0)  + 0.0001;
        vec3 specular     = numerator / denominator;

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metallic;

        float NdotL = max(dot(N, L), 0.0);

        float shadow = shadowCalculation(fragPosDirLightSpace, L, N);
        Lo += (kD * albedo.rgb / PI + specular) * radiance * NdotL * (1.0 - shadow);
    }

    vec3 ambient = vec3(0.1) * albedo.rgb * ao;
    vec3 color   = ambient + Lo;

    color = mix(color, fog.color, getFogFactor(fog, length(viewPos - fragPos))); // tODO Performance

    //    vec3 result = color;

    if(useGammaCorrection) {
        color = color / (color + vec3(1.0));
        color = pow(color, vec3(1.0/gammaCorrection));
    }
    fragColor = vec4(color, albedo.w);
}
