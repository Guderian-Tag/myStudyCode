/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.multimedia.my.d3.book;

import com.multimedia.data.VertexArray;
import com.multimedia.my.d3.Geometry;
import com.multimedia.programs.ColorShaderProgram;

import java.util.List;

public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 3;

    public  float radius;
    public  float height;

    private  VertexArray vertexArray;
    private  List<ObjectBuilder.DrawCommand> drawList;

    public Mallet(float radius, float height, int numPointsAroundMallet) {
       ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createMallet(new Geometry.Point(0f,
            0f, 0f), radius, height, numPointsAroundMallet);

        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }
    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(0,
            colorProgram.getaPositionLocation(),
            POSITION_COMPONENT_COUNT, 0);
    }
    public void draw() {
        for (ObjectBuilder.DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
