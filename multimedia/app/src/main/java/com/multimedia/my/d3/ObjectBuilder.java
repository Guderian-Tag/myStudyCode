package com.multimedia.my.d3;


import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;

public class ObjectBuilder {

    public interface DrawCommand{
        void draw();
    }


    public static class GenerateData{
        float[] vertexData;
        List<DrawCommand> commandList;

        public GenerateData(float[] vertexData,List<DrawCommand> commandList){
            this.vertexData = vertexData;
            this.commandList = commandList;
        }
    }
    private static final int FLOAT_PER_VERTEX = 3;
    private float[] vertexData;
    private int offSet = 0;
    private List<DrawCommand> commandList = new ArrayList<>();


    public static GenerateData createBuck(Geometry.Cylinder cylinder,int numberPoints){
        int size = sizeOfCircleInVertices(numberPoints)+sizeOfOpenCylinderInVertices(numberPoints);
        ObjectBuilder builder = new ObjectBuilder(size);
        Geometry.Circle circle = new Geometry.Circle(cylinder.center.translateY(cylinder.height/2f),
                cylinder.radius);
        builder.appendCircle(circle,numberPoints);
        builder.appendCylinder(cylinder,numberPoints);
        return builder.build();
    }

   public static GenerateData createMallet(Geometry.Point center,float radius,float height,int numPoints){
        int size = sizeOfCircleInVertices(numPoints)*2+sizeOfOpenCylinderInVertices(numPoints)*2;
        ObjectBuilder builder = new ObjectBuilder(size);
        float baseHeight = height*0.25f;
        Geometry.Circle baseCircle = new Geometry.Circle(center.translateY(-baseHeight),radius);
        Geometry.Cylinder baseLinder = new Geometry.Cylinder(baseCircle.center.translateY(-baseHeight/2f)
                ,radius,baseHeight);
        builder.appendCircle(baseCircle,numPoints);
        builder.appendCylinder(baseLinder,numPoints);
        float handlerHeight = height*0.75f;
        float handleRadius = radius/3f;
        Geometry.Circle handleCircle = new Geometry.Circle(center.translateY(height*0.5f),handleRadius);
        Geometry.Cylinder handleCylinder = new Geometry.Cylinder(handleCircle.center.translateY(-handlerHeight/2f),
                handleRadius,handlerHeight);
        builder.appendCircle(handleCircle,numPoints);
        builder.appendCylinder(handleCylinder,numPoints);
        return builder.build();
    }

    private void appendCircle(Geometry.Circle circle, final int numberPoints){
        final int startVertex = offSet/FLOAT_PER_VERTEX;
        final int numVertex = sizeOfCircleInVertices(numberPoints);
        vertexData[offSet++] = circle.center.x;
        vertexData[offSet++] = circle.center.y;
        vertexData[offSet++] = circle.center.z;
        for (int i=0;i<=numberPoints;i++) {
            float angleInRadius = ((float)i/(float)numberPoints)*((float) Math.PI*2f);
            vertexData[offSet++] = circle.center.x +(float)(circle.radius* Math.cos(angleInRadius));
            vertexData[offSet++] = circle.center.y;
            vertexData[offSet++] = circle.center.z + (float)(circle.radius*Math.sin(angleInRadius));
        }
        commandList.add(new DrawCommand() {
            @Override
            public void draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,startVertex,numVertex);
            }
        });
    }

    private void appendCylinder(Geometry.Cylinder cylinder, final int numPoints){
        final int startVertex = offSet/FLOAT_PER_VERTEX;
        final int numVertex = sizeOfOpenCylinderInVertices(numPoints);
        float yStart = cylinder.center.y-(cylinder.height/2f);
        float yEnd = cylinder.center.y+(cylinder.height/2f);
        for (int i = 0; i <=numPoints ; i++) {
            float angleInRadius = ((float)i/(float)numPoints)/((float)Math.PI*2f);
            float xPosition = cylinder.center.x+(float)(cylinder.radius*Math.cos(angleInRadius));
            float zPosition = cylinder.center.z+(float)(cylinder.radius*Math.sin(angleInRadius));
            vertexData[offSet++]=xPosition;
            vertexData[offSet++]=yStart;
            vertexData[offSet++]=zPosition;
            vertexData[offSet++]=xPosition;
            vertexData[offSet++]=yEnd;
            vertexData[offSet++]=zPosition;
        }
        commandList.add(new DrawCommand() {
            @Override
            public void draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,startVertex,numVertex);
            }
        });
    }

    private GenerateData build(){
        return new GenerateData(vertexData,commandList);
    }


    public ObjectBuilder(int vertexSize){
        vertexData = new float[vertexSize*FLOAT_PER_VERTEX];
    }

    private static int sizeOfCircleInVertices(int numberPoints){
        return 1+(numberPoints+1);
    }

    private static int sizeOfOpenCylinderInVertices(int numberPoints){
        return (numberPoints+1)*2;
    }

}
