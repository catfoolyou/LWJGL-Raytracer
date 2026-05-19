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

#define MAX_OBJECTS 128

uniform int objectsInWorld;
uniform vec4 balls[MAX_OBJECTS];

const float pi = 3.1415926535897932385;
const float infinity = uintBitsToFloat(0x7F800000);

struct Ray{
    vec3 origin;
    vec3 dir;
};

struct hitRecord{
    vec3 p;
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

float randomDouble() {
    return fract(sin(dot(vec2(0.5), vec2(12.9898, 78.233))) * 43758.5453);
}

float randomDouble(float min, float max) {
    return min + (max-min) * randomDouble();
}

vec3 sampleSquare(){
    return vec3(randomDouble() - 0.5, randomDouble() - 0.5, 0);
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
    rec.p = at(r, rec.t);
    vec3 outwardNormal = (rec.p - center) / radius;

    rec.frontFace = dot(r.dir, outwardNormal) < 0;
    rec.normal = rec.frontFace ? outwardNormal : -outwardNormal;

    return true;
}

vec3 rayColor(Ray r){
    hitRecord rec;
    for (int i = 0; i < MAX_OBJECTS; i++) {
       if(i <= objectsInWorld+1){
           if (hitSphere(r, 0.0, infinity, balls[i].xyz, balls[i].w, rec)) {
               return 0.5 * (rec.normal + vec3(1.0));
           }
       }
    }

    float a = 0.5 * (r.dir.y + 1.0);
    return (1.0 - a) * vec3(1.0) + a * vec3(0.5, 0.7, 1.0);
}

void main() {
    vec2 uv = v_texCoords * 2.0 - 1.0;
    uv.x *= u_resolution.x/u_resolution.y;

    vec3 rayDirection = normalize(vec3(uv.x, -uv.y, -1.0));
    Ray r = Ray(cameraPos, rayDirection);

    vec3 col = rayColor(r);

    fragColor = vec4(col, 1.0);
}
