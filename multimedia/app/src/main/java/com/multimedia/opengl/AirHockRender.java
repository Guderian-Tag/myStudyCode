package com.multimedia.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.multimedia.R;
import com.multimedia.my.d3.Geometry;
import com.multimedia.my.d3.Puck;
import com.multimedia.my.d3.book.Mallet;
import com.multimedia.object.Table;
import com.multimedia.programs.ColorShaderProgram;
import com.multimedia.programs.TextureShaderProgram;
import com.multimedia.utils.MatrixHelper;
import com.multimedia.utils.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class AirHockRender implements GLSurfaceView.Renderer {

    private final Context context;
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private Table table;
   // private Mallet mallet;

    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private Puck puck;
    private Mallet mallet3d;
    private final float[] modelViewProjectionMatrix = new float[16];

    private TextureShaderProgram textureShaderProgram;
    private ColorShaderProgram colorShaderProgram;

    private int texture;

    private boolean malletPressed = false;
    private Geometry.Point blueMalletPosition;
    private float[] invertedViewProjectionMatrix = new float[16];

    private float leftBound = -0.5f;
    private float rightBound = 0.5f;
    private float farBound = -0.8f;
    private float nearBound = 0.8f;


    public AirHockRender(Context context){
        this.context = context;
    }

    public void handleTouchPress(float normalizeX,float normalizedY){
        Log.d("wang","handleTouchPress");
        Geometry.Ray ray = convertNormalize2DPointToRay(normalizeX,normalizedY);
        Geometry.Sphere malletBoundingPoint = new Geometry.Sphere(new Geometry.Point(blueMalletPosition.x
                ,blueMalletPosition.y
                ,blueMalletPosition.z)
                ,mallet3d.height/2f);
        malletPressed = Geometry.intersects(malletBoundingPoint,ray);
    }

    public void handleTouchDrag(float normalizeX,float normalizedY){
        Log.d("wang","handleTouchDrag");
        if (malletPressed){
            Geometry.Ray ray = convertNormalize2DPointToRay(normalizeX,normalizedY);
            Geometry.Plane plane = new Geometry.Plane(new Geometry.Point(0,0,0),new Geometry.Vector(0,1,0));
            Geometry.Point touchPoint = Geometry.insertSectionPoint(ray,plane);
            blueMalletPosition = new Geometry.Point(
                    clamp(touchPoint.x,mallet3d.radius+leftBound,rightBound-mallet3d.radius),
                    mallet3d.height/2f,
                    clamp(touchPoint.z,mallet3d.radius,nearBound-mallet3d.radius)

            );
        }
    }

    private float clamp(float value,float min,float max){
        return Math.max(max,Math.max(value,min));
    }

    private void devidedByW(float[] vector){
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }

    private Geometry.Ray convertNormalize2DPointToRay(float normalizeX,float normalizedY){
        float[] nearPointNdc = {normalizeX,normalizedY,-1,1};
        float[] farPointNdc = {normalizeX,normalizedY,1,1};
        float[] nearPointWorld = new float[4];
        float[] farPointWorld = new float[4];

        Matrix.multiplyMV(nearPointWorld,0,invertedViewProjectionMatrix,0,nearPointNdc,0);
        Matrix.multiplyMV(farPointWorld,0,invertedViewProjectionMatrix,0,farPointNdc,0);
        devidedByW(nearPointWorld);
        devidedByW(farPointWorld);
        Geometry.Point nearPointRay = new Geometry.Point(nearPointWorld[0],nearPointWorld[1],nearPointWorld[2]);
        Geometry.Point farPointRay = new Geometry.Point(farPointWorld[0],farPointWorld[1],farPointWorld[2]);
        return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay,farPointRay));
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f,0.0f,0.0f,0.0f);
        table = new Table();
        //mallet = new Mallet();
        mallet3d = new Mallet(0.08f,0.15f,32);
        puck = new Puck(0.06f,0.02f,32);
        textureShaderProgram = new TextureShaderProgram(context);
        colorShaderProgram = new ColorShaderProgram(context);
        texture = TextureHelper.loadTexture(context, R.mipmap.air_hockey_surface);

        blueMalletPosition = new Geometry.Point(0f,mallet3d.height/2f,0.4f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 10f);

        /*Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -2.5f);
        Matrix.rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

        final float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);*/
        Matrix.setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        /*textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(projectionMatrix,texture);
        table.bindData(textureShaderProgram);
        table.draw();

        colorShaderProgram.useProgram();
        colorShaderProgram.setUniforms(projectionMatrix);
        mallet.bindData(colorShaderProgram);
        mallet.draw();*/

        // Multiply the view and projection matrices together.
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.invertM(invertedViewProjectionMatrix,0,viewProjectionMatrix,0);
        // Draw the table.
        positionTableInScene();
        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureShaderProgram);
        table.draw();

        // Draw the mallets.
        positionObjectInScene(0f, mallet3d.height / 2f, -0.4f);
        colorShaderProgram.useProgram();
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
        mallet3d.bindData(colorShaderProgram);
        mallet3d.draw();

        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z);
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
        // Note that we don't have to define the object data twice -- we just
        // draw the same mallet again but in a different position and with a
        // different color.
        mallet3d.draw();

        // Draw the puck.
        positionObjectInScene(0f, puck.height / 2f, 0f);
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f);
        puck.bindData(colorShaderProgram);
        puck.draw();
    }

    private void positionTableInScene() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    // The mallets and the puck are positioned on the same plane as the table.
    private void positionObjectInScene(float x, float y, float z) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, z);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

}
