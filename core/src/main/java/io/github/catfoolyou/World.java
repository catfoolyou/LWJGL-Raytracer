package io.github.catfoolyou;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class World {
    private final Camera simulatedCamera = new PerspectiveCamera(67f, GlobalConstants.WIDTH, GlobalConstants.HEIGHT);
    private final Vector3 velocity = new Vector3();

    private final List<Sphere> balls = new ArrayList<>();
//    private final List<Quaternion> quads = new ArrayList<>();
    private final List<Material> materials = new ArrayList<>();

    public World(){
        simulatedCamera.position.set(new Vector3(0, 0, 0));
    }

    public World(Vector3 initialPos){
        simulatedCamera.position.set(initialPos);
    }

    float yaw = -90f;
    float pitch = 0f;

    public void handleInput(float delta){
        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            velocity.z -= delta;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            velocity.z += delta;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            velocity.x -= delta;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            velocity.x += delta;
        }

        simulatedCamera.position.add(velocity);
        velocity.scl(0.5f);

        Gdx.input.setCursorCatched(!Gdx.input.isKeyPressed(Input.Keys.ESCAPE));

        float sensitivity = 0.2f;

        yaw += Gdx.input.getDeltaX() * sensitivity;
        pitch -= Gdx.input.getDeltaY() * sensitivity;
        pitch = MathUtils.clamp(pitch, -89.9f, 89.9f);

        float yawRad = MathUtils.degreesToRadians * yaw;
        float pitchRad = MathUtils.degreesToRadians * pitch;

        simulatedCamera.direction.set(new Vector3(MathUtils.cos(pitchRad) * MathUtils.cos(yawRad), MathUtils.sin(pitchRad), MathUtils.cos(pitchRad) * MathUtils.sin(yawRad)).nor());
    }

    public Vector3 getCameraPos() {
        return simulatedCamera.position;
    }

    public Vector3 lookAt(){
        return simulatedCamera.position.cpy().add(simulatedCamera.direction);
    }

    public Vector3 up(){
        return simulatedCamera.up;
    }

    public List<Sphere> getSphereSSBO() {
        return balls;
    }

    public void sortSSBOs(){
        balls.sort((s1, s2) -> {
            double dist1 = Math.sqrt(Math.pow(s1.center().x - simulatedCamera.position.x, 2) + Math.pow(s1.center().y - simulatedCamera.position.y, 2) + Math.pow(s1.center().z - simulatedCamera.position.z, 2));
            double dist2 = Math.sqrt(Math.pow(s2.center().x - simulatedCamera.position.x, 2) + Math.pow(s2.center().y - simulatedCamera.position.y, 2) + Math.pow(s2.center().z - simulatedCamera.position.z, 2));

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
