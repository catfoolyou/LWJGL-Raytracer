#version 430

#ifdef GL_ES
precision mediump float;
#endif

in vec4 v_color;
in vec2 v_texCoords;

out vec4 fragColor;

uniform sampler2D u_texture;

uniform vec2 u_resolution;
uniform vec3 cameraPos;
uniform vec3 rayOrigin;
uniform vec3 rayDir;

uniform sampler2D u_previousFrame;
uniform int u_frameCount;

#define MAX_OBJECTS 128
#define MAX_BOUNCES 4

uniform int objectsInWorld;
uniform vec4 balls[MAX_OBJECTS];

const float pi = 3.1415926535897932385;
const float infinity = uintBitsToFloat(0x7F800000);

struct Ray{
    vec3 origin;
    vec3 dir;
};

struct hitRecord{
    vec3 point;
    vec3 normal;
    float t;
    bool frontFace;
};

vec3 at(Ray r, float t){
    return r.origin + t * r.dir;
}

bool inRange(float x, float min, float max){
    float inRange = step(min, x) * step(x, max);
    return inRange > 0.0;
}

bool surrounds(float x, float min, float max){
    return min < x && x < max; // branching?
}

float seed = v_texCoords.x * v_texCoords.y + float(u_frameCount) * 0.01;

float random() {
    seed = fract(sin(dot(vec2(seed), vec2(12.9898, 78.233))) * 43758.5453);
    return seed;
}

vec2 hash2(vec2 p) {
    return fract(sin(vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)))) * 43758.5453);
}

float randomInRange(float min, float max) {
    return min + (max-min) * random();
}

vec3 randomUnitVector3D() {
    float z = random() * 2.0 - 1.0;          // Range [-1, 1]
    float a = random() * 6.28318530718; // Range [0, 2PI]
    float r = sqrt(1.0 - z * z);
    return vec3(r * cos(a), r * sin(a), z);
}

vec3 randomVectorInRange(float min, float max) {
    return vec3(randomInRange(min,max), randomInRange(min,max), randomInRange(min,max));
}

vec3 randomOnHemisphere(vec3 normal) {
    vec3 onUnitSphere = randomVectorInRange(-1.0, 1.0);
    if (dot(onUnitSphere, normal) > 0.0){
        return onUnitSphere;
    }
    else {
        return -onUnitSphere;
    }
}

bool hitSphere(Ray r, float rayMin, float rayMax, vec3 center, float radius, inout hitRecord rec) {
    vec3 oc = center - r.origin;
    float a = dot(r.dir, r.dir);
    float halfB = dot(r.dir, oc);
    float c = dot(oc, oc) - radius * radius;
    float discriminant = halfB * halfB - a * c;

    if (discriminant < 0){
        return false;
    }

    float sqrtd = sqrt(discriminant);

    float root = (halfB - sqrtd) / a;
    if (root <= rayMin || rayMax <= root) {
        root = (halfB + sqrtd) / a;
        if (root <= rayMin || rayMax <= root)
        return false;
    }

    rec.t = root;
    rec.point = at(r, rec.t);
    vec3 outwardNormal = (rec.point - center) / radius;

    rec.frontFace = dot(r.dir, outwardNormal) < 0;
    rec.normal = rec.frontFace ? outwardNormal : -outwardNormal;

    return true;
}

vec3 rayColor(Ray r){
    hitRecord rec;
    vec3 col = vec3(1.0);

    bool hit = false;

    for(int i = 0; i < MAX_BOUNCES; i++){
        hit = false;
        for (int j = 0; j < MAX_OBJECTS; j++) {
            if (j <= objectsInWorld+1){
                if (hitSphere(r, 0.01, infinity, balls[j].xyz, balls[j].w, rec)) {
                    hit = true;
                }
            }
        }
        if(hit){
            vec3 direction = randomOnHemisphere(rec.normal);
            r.origin = rec.point;
            r.dir = rec.normal + randomUnitVector3D();
            col *= 0.5;
        }
        else{
            float a = 0.5 * (r.dir.y + 1.0);
            vec3 skyColor = (1.0 - a) * vec3(1.0) + a * vec3(0.5, 0.7, 1.0);
            return col * skyColor;
        }
    }

    return col;
}

void main() {
    vec2 uv = v_texCoords * 2.0 - 1.0;
    uv.x *= u_resolution.x/u_resolution.y;
    uv.y = -uv.y;

    vec3 col = vec3(0.0);

    vec2 samples[4] = vec2[](
        vec2(-0.25, -0.75),
        vec2(0.75, -0.25),
        vec2(-0.75, 0.25),
        vec2(0.25, 0.75)
    );

    for (int i = 0; i < 4; i++) {
        vec2 pixelOffset = (samples[i] + hash2(gl_FragCoord.xy + float(i))) / u_resolution.xy;
        vec2 currentUV = uv + pixelOffset;

        vec3 rayDirection = normalize(vec3(currentUV, -1.0));
        Ray r = Ray(cameraPos, rayDirection);

        col += rayColor(r);
    }

    fragColor = vec4(col / 4.0, 1.0);
}
