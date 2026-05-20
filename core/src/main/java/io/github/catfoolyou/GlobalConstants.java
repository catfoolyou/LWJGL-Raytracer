package io.github.catfoolyou;

public class GlobalConstants {
    public static final float aspectRatio = 16.0f / 9.0f;

    public static final int WIDTH = 800;
    public static final int HEIGHT = Math.max((int) (WIDTH / aspectRatio), 1);

    public static final boolean doDoubleBufferization = false;
}
