package com.multimedia.my.d3;


import com.multimedia.data.VertexArray;
import com.multimedia.programs.ColorShaderProgram;

import java.util.List;

public class Puck {

    private final static int POSITION_COMPONENT_COUNT = 3;

    public float radius,height;
    private VertexArray vertexArray;
    private List<ObjectBuilder.DrawCommand> commandList;

    public Puck(float radius,float height,int numPointAroundPuck){
        ObjectBuilder.GenerateData generateData = ObjectBuilder.createBuck(
                new Geometry.Cylinder(new Geometry.Point(0f,0f,0f),radius,height),numPointAroundPuck);
        this.radius = radius;
        this.height = height;
        vertexArray = new VertexArray(generateData.vertexData);
        commandList = generateData.commandList;
    }

    public void bindData(ColorShaderProgram colorShaderProgram){
        vertexArray.setVertexAttribPointer(0,
                colorShaderProgram.getaPositionLocation(),
                POSITION_COMPONENT_COUNT,
                0);
    }

    public void draw(){
        for (ObjectBuilder.DrawCommand command:commandList){
            command.draw();
        }
    }
}
