package io.github.catfoolyou;

import com.badlogic.gdx.math.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class World {

    private final Vector3 cameraPos;
    private final List<Sphere> balls = new ArrayList<>();
//    private final List<Quaternion> quads = new ArrayList<>();
    private final List<Material> materials = new ArrayList<>();

    public World(){
        cameraPos = new Vector3(0, 0, 0);
    }

    public World(Vector3 initialPos){
        cameraPos = initialPos;
    }

    public void handleInput(){
        // update cameraPos position/rotation here
    }

    public Vector3 getCameraPos() {
        return cameraPos;
    }

    public List<Sphere> getSphereSSBO() {
        return balls;
    }

    public void sortSSBOs(){
        balls.sort((s1, s2) -> {
            double dist1 = Math.sqrt(Math.pow(s1.center().x - cameraPos.x, 2) + Math.pow(s1.center().y - cameraPos.y, 2) + Math.pow(s1.center().z - cameraPos.z, 2));
            double dist2 = Math.sqrt(Math.pow(s2.center().x - cameraPos.x, 2) + Math.pow(s2.center().y - cameraPos.y, 2) + Math.pow(s2.center().z - cameraPos.z, 2));

            return Double.compare(dist2, dist1);
        });

        for(Sphere s : balls){
            materials.add(s.getMaterial());
        }
        // also sort quads/triangles
    }

    public FloatBuffer getSphereBuffer() {
        FloatBuffer buf = ByteBuffer.allocateDirect((balls.size() * 8) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (Sphere s : balls) {
            buf.put(s.center().x).put(s.center().y).put(s.center().z).put(s.radius());
            buf.put((float) balls.indexOf(s)).put(0).put(0).put(0);
        }
        buf.flip();
        return buf;
    }

    public FloatBuffer getMaterialBuffer() {
        FloatBuffer buf = ByteBuffer.allocateDirect((materials.size() * 8) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (Material mat : materials) {
            buf.put(mat.getColor().x).put(mat.getColor().y).put(mat.getColor().z).put(mat.getType());
            buf.put(mat.getBits()).put(0).put(0).put(0);
        }
        buf.flip();
        return buf;
    }

}
