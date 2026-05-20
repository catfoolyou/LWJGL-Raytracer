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

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch renderer;
    private FitViewport viewport;
    private final Vector2 screenSize = new Vector2(GlobalConstants.WIDTH, GlobalConstants.HEIGHT);

    private ShaderProgram shader;
    private TextureRegion fbo;
    private FrameBuffer readBuffer;
    private FrameBuffer writeBuffer;

    private int frameCount;

    private Vector3 cameraPos = new Vector3(0, 0, 0);

    @Override
    public void create() {
        this.renderer = new SpriteBatch();
        this.viewport = new FitViewport(GlobalConstants.WIDTH, GlobalConstants.HEIGHT);

        this.fbo = new TextureRegion(new FrameBuffer(Pixmap.Format.RGB888, GlobalConstants.WIDTH, GlobalConstants.HEIGHT, false).getColorBufferTexture());
        this.readBuffer = new FrameBuffer(Pixmap.Format.RGB888, GlobalConstants.WIDTH, GlobalConstants.HEIGHT, false);
        this.writeBuffer = new FrameBuffer(Pixmap.Format.RGB888, GlobalConstants.WIDTH, GlobalConstants.HEIGHT, false);

        String fragmentShader = Gdx.files.internal("shaders/raytrace.fsh").readString();
        String vertexShader = Gdx.files.internal("shaders/raytrace.vsh").readString();
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertexShader, fragmentShader);

        if (!shader.isCompiled()) {
            System.err.println(shader.getLog());
            System.exit(0);
        }
    }

    private void passSSBOs(){
        // replace with actual SSBOs
        float[] balls = new float[]{
            0, -100.5f, -1, 100,
            0, 0, -1, 0.5f
        };

        shader.setUniformi("objectsInWorld", balls.length /4);
        shader.setUniform4fv("balls", balls, 0, balls.length);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if(GlobalConstants.doDoubleBufferization) {
            renderer.begin();
            writeBuffer.begin();
            shader.bind();
            shader.setUniformf("u_resolution", screenSize);
            shader.setUniformf("cameraPos", this.cameraPos);

            frameCount++; // must be rest when camera is moved
            shader.setUniformi("u_frameCount", frameCount);
            shader.setUniformi("u_previousFrame", 1);
            passSSBOs();

            readBuffer.getColorBufferTexture().bind(1);
            writeBuffer.end();

            renderer.setShader(shader);
            renderer.draw(fbo, 0, 0);

            FrameBuffer temp = readBuffer;
            readBuffer = writeBuffer;
            writeBuffer = temp;

            renderer.end();
        }
        else {
            renderer.begin();

            shader.bind();
            shader.setUniformf("u_resolution", screenSize);
            shader.setUniformf("cameraPos", this.cameraPos);

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
