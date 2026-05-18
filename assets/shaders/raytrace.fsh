#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

uniform vec2 u_resolution;
uniform vec3 cameraPos;
uniform vec3 rayOrigin;
uniform vec3 rayDir;

uniform vec4 balls[];

const float pi = 3.1415926535897932385;

struct Ray{
    vec3 origin;
    vec3 dir;
};

vec3 at(Ray r, float t){
    return r.origin + t * r.dir;
}

float hitSphere(vec3 center, float radius, Ray r) {
    vec3 oc = center - r.origin;
    float a = dot(r.dir, r.dir);
    float h = dot(r.dir, oc);
    float c = dot(oc, oc) - radius * radius;
    float discriminant = h * h - a * c;

    if (discriminant < 0.0) {
        return -1.0;
    } else {
        return(h - sqrt(discriminant)) / a;
    }
}

vec3 rayColor(Ray r){
    float t = hitSphere(balls[0].xyz, balls[0].w, r);
    if (t > 0.0) {
        vec3 N = normalize(at(r, t) - vec3(0, 0, -1));
        return 0.5 * vec3(N.x + 1.0, N.y + 1.0, N.z + 1.0);
    }

    float a = 0.5 * (r.dir.y + 1.0);
    return (1.0 - a) * vec3(1.0) + a * vec3(0.5, 0.7, 1.0);
}

void main() {
    vec2 uv = v_texCoords * 2.0 - 1.0;
    uv.x *= u_resolution.x/u_resolution.y;

    vec3 ray_direction = normalize(vec3(uv.x, -uv.y, -1.0));
    Ray r = Ray(cameraPos, ray_direction);

    vec3 col = rayColor(r);

    gl_FragColor = vec4(col, 1.0);
}
