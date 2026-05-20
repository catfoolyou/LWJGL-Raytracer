package io.github.catfoolyou;

import com.badlogic.gdx.math.Vector3;

public class Sphere {
    private final Vector3 center;
    private final float radius;
    private final Material material;

    public Sphere(Vector3 center, float radius, Material material){
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    public Sphere(float x, float y, float z, float radius, Material material){
        this.center = new Vector3(x, y, z);
        this.radius = radius;
        this.material = material;
    }

    public Vector3 center(){
        return center;
    }

    public float radius(){
        return radius;
    }

    public Material getMaterial(){
        return material;
    }

}
