package com.multimedia.my.d3;



public class Geometry {

    public static class Point{

        public float x,y,z;

        public Point(float x,float y,float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point translateY(float offSet){
            return new Point(x,y+offSet,z);
        }

        public Point translate(Vector vector){
            return new Point(x+vector.x,y+vector.y,z+vector.z);
        }

    }

    public static class Circle{

        public Point center;
        public float radius;

        public Circle(Point center,float radius){
            this.center = center;
            this.radius = radius;
        }

        public Circle scale(float scale){
            return new Circle(center,radius*scale);
        }

    }

    public static class Cylinder{

        public float radius;
        public Point center;
        public float height;

        public Cylinder(Point center,float radius,float height){
            this.center = center;
            this.radius = radius;
            this.height = height;
        }

    }

    public static class Vector{
        public final float x,y,z;

        public Vector(float x,float y,float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length(){
            return (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
        }

        public Vector crossProduct(Vector other){
            return new Vector(
                    y*other.z-z*other.y,
                    z*other.x-x*other.z,
                    x*other.y-y*other.x
            );
        }

        public float dotProduct(Vector other){
            return  x*other.x + y*other.y + z*other.z;
        }

        public Vector scale(float f){
            return new Vector(x*f,y*f,z*f);
        }

    }

    public static Vector vectorBetween(Point from,Point to){
        return new Vector(to.x-from.x
                ,to.y-from.y
                ,to.z-from.z);
    }

    public static class Sphere{
        public final Point center;
        public final float radius;

        public Sphere(Point center,float radius){
            this.center = center;
            this.radius = radius;
        }
    }

    public static class Ray{
        public final Point point;
        public final Vector vector;

        public Ray(Point point,Vector vector){
            this.point = point;
            this.vector = vector;
        }
    }

    public static float distanceBetween(Point point,Ray ray){
        Vector v1ToPoint = vectorBetween(ray.point,point);
        Vector v2ToPoint = vectorBetween(ray.point.translate(ray.vector),point);
        float areaOfTriangleTwoTimes = v1ToPoint.crossProduct(ray.vector).length();
        float lengthOfBase = ray.vector.length();
        float fromPointToRay = areaOfTriangleTwoTimes/lengthOfBase;
        return fromPointToRay;
    }

    public static class Plane{
        public final Point point;
        public final Vector normal;

        public Plane(Point point,Vector normal){
            this.point = point;
            this.normal = normal;
        }
    }

    public static Point insertSectionPoint(Ray ray,Plane plane){
        Vector rayToPlaneVector = vectorBetween(ray.point,plane.point);
        float scaleFactor = rayToPlaneVector.dotProduct(plane.normal)/ray.vector.dotProduct(plane.normal);
        Point point = ray.point.translate(ray.vector.scale(scaleFactor));
        return point;
    }

    public static boolean intersects(Sphere sphere,Ray ray){
        return distanceBetween(sphere.center,ray)<sphere.radius;
    }
}
