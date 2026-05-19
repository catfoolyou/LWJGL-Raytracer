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
import com.badlogic.gdx.math.collision.Sphere;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch renderer;
    private FitViewport viewport;
    private final Vector2 screenSize = new Vector2(GlobalConstants.WIDTH, GlobalConstants.HEIGHT);

    private ShaderProgram shader;
    private TextureRegion fbo;

    private Vector3 cameraPos = new Vector3(0, 0, 0);

    @Override
    public void create() {
        this.renderer = new SpriteBatch();
        this.viewport = new FitViewport(GlobalConstants.WIDTH, GlobalConstants.HEIGHT);

        this.fbo = new TextureRegion(new FrameBuffer(Pixmap.Format.RGB888, GlobalConstants.WIDTH, GlobalConstants.HEIGHT, false).getColorBufferTexture());

        String fragmentShader = Gdx.files.internal("shaders/raytrace.fsh").readString();
        String vertexShader = Gdx.files.internal("shaders/raytrace.vsh").readString();
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertexShader, fragmentShader);

        if (!shader.isCompiled()) {
            System.err.println(shader.getLog());
            System.exit(0);
        }

        this.initWorld();
    }

    private void initWorld(){
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        renderer.begin();

        shader.bind();
        shader.setUniformf("u_resolution", screenSize);
        shader.setUniformf("cameraPos", this.cameraPos);

        float[] balls = new float[]{
            0, 0, -1, 0.5f,
            0, -100.5f, -1, 100
        };

        shader.setUniformf("objectsInWorld", balls.length/4);
        shader.setUniform4fv("balls", balls, 0, balls.length);

        renderer.setShader(shader);
        renderer.draw(fbo, 0, 0);
        renderer.setShader(null);

        renderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
//        model.dispose();
        shader.dispose();
    }
}
