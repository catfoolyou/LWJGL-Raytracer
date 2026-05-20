package io.github.catfoolyou;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.nio.FloatBuffer;

import static com.badlogic.gdx.graphics.GL20.GL_STATIC_DRAW;
import static com.badlogic.gdx.graphics.GL31.GL_SHADER_STORAGE_BUFFER;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch renderer;
    private FitViewport viewport;
    private final Vector2 screenSize = new Vector2(GlobalConstants.WIDTH, GlobalConstants.HEIGHT);

    private ShaderProgram shader;
    private TextureRegion fbo;
    private FrameBuffer readBuffer;
    private FrameBuffer writeBuffer;
    int sphereBufferID;
    int matBufferID;

    private int frameCount;

    private World world;

    @Override
    public void create() {
        this.renderer = new SpriteBatch();
        this.viewport = new FitViewport(GlobalConstants.WIDTH, GlobalConstants.HEIGHT);
        this.initWorld();

        this.fbo = new TextureRegion(new FrameBuffer(Pixmap.Format.RGB888, GlobalConstants.WIDTH, GlobalConstants.HEIGHT, false).getColorBufferTexture());
        this.readBuffer = new FrameBuffer(Pixmap.Format.RGB888, GlobalConstants.WIDTH, GlobalConstants.HEIGHT, false);
        this.writeBuffer = new FrameBuffer(Pixmap.Format.RGB888, GlobalConstants.WIDTH, GlobalConstants.HEIGHT, false);

        sphereBufferID = Gdx.gl.glGenBuffer();
        matBufferID = Gdx.gl.glGenBuffer();

        String fragmentShader = Gdx.files.internal("shaders/raytrace.fsh").readString();
        String vertexShader = Gdx.files.internal("shaders/raytrace.vsh").readString();
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertexShader, fragmentShader);

        if (!shader.isCompiled()) {
            System.err.println(shader.getLog());
            System.exit(0);
        }
    }

    private void initWorld(){
        this.world = new World();
        world.getSphereSSBO().add(new Sphere(0, -100.5f, -1, 100, new Material(new Vector3(0.8f, 0.8f, 0.0f), Material.materialType.LAMBERTIAN)));
        world.getSphereSSBO().add(new Sphere(0, 0, -1, 0.5f, new Material(new Vector3(0.1f, 0.2f, 0.5f), Material.materialType.LAMBERTIAN)));

        world.sortSSBOs();
    }

    private void passSSBOs(){
        shader.setUniformi("objectsInWorld", world.getSphereSSBO().size()); // pass amount of quads/triangles later, possibly separately

        Gdx.gl.glBindBuffer(GL_SHADER_STORAGE_BUFFER, sphereBufferID);

        FloatBuffer sphereBuffer = world.getSphereBuffer();

        Gdx.gl.glBufferData(GL_SHADER_STORAGE_BUFFER, sphereBuffer.capacity(), sphereBuffer, GL_STATIC_DRAW);
        Gdx.gl30.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, sphereBufferID);
//        MemoryUtil.memFree(sphereBuffer);

        Gdx.gl.glBindBuffer(GL_SHADER_STORAGE_BUFFER, matBufferID);

        FloatBuffer materialBuffer = world.getMaterialBuffer();

        Gdx.gl.glBufferData(GL_SHADER_STORAGE_BUFFER, materialBuffer.capacity(), materialBuffer, GL_STATIC_DRAW);
        Gdx.gl30.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, matBufferID);
//        MemoryUtil.memFree(materialBuffer);
    }

    FrameBuffer temp;
    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        world.handleInput();

        if(GlobalConstants.doDoubleBufferization) {
            renderer.begin();
            writeBuffer.begin();
            shader.bind();
            shader.setUniformf("u_resolution", screenSize);
            shader.setUniformf("cameraPos", world.getCameraPos());

            frameCount++; // must be rest when camera is moved
            shader.setUniformi("u_frameCount", frameCount);
            shader.setUniformi("u_previousFrame", 1);
            passSSBOs();

            readBuffer.getColorBufferTexture().bind(1);
            writeBuffer.end();

            renderer.setShader(shader);
            renderer.draw(fbo, 0, 0);

            temp = readBuffer;
            readBuffer = writeBuffer;
            writeBuffer = temp;

            renderer.end();
        }
        else {
            renderer.begin();

            shader.bind();
            shader.setUniformf("u_resolution", screenSize);
            shader.setUniformf("cameraPos", world.getCameraPos());

            shader.setUniformi("u_frameCount", 1);

            passSSBOs();

            renderer.setShader(shader);
            renderer.draw(fbo, 0, 0);
            renderer.setShader(null);

            renderer.end();
        }

        Gdx.graphics.setTitle("LWJGL GPU raytracer - FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        shader.dispose();
    }
}
