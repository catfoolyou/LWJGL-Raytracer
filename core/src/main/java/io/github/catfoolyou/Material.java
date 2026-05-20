package io.github.catfoolyou;

import com.badlogic.gdx.math.Vector3;

public class Material {

    public enum materialType{
        LAMBERTIAN, METAL, DIELECTRIC
    }

    private final Vector3 color;
    private final materialType type;
    private final float bits; // reflection/refraction index, ignored for lambertian materials

    public Material(Vector3 color, materialType type, float dataBits){
        this.color = color;
        this.type = type;
        this.bits = dataBits;
    }

    public Vector3 getColor(){
        return color;
    }

    public float getBits(){
        return bits;
    }

    public int getType(){
        return switch (type) {
            case LAMBERTIAN -> 0;
            case METAL -> 1;
            case DIELECTRIC -> 2;
        };
    }
}
