package executables.solar_system;

import javafx.scene.shape.Sphere;

public class SpaceObject extends Sphere {
    private String name;
    private double radius;
    private double mass;
    private double gravity;
    private double radiusOfRevolution;
    private double xVelocity;
    private double yVelocity;
    private double zVelocity;
    private double Xcoordinates;
    private double Ycoordinates;
    private double Zcoordinates;

    public SpaceObject() {
        this.name = name;
        this.radius = radius;
        this.mass = mass;
        this.gravity = gravity;
        this.radiusOfRevolution = radiusOfRevolution;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        this.zVelocity = zVelocity;
        this.Xcoordinates = Xcoordinates;
        this.Ycoordinates = Ycoordinates;
        this.Zcoordinates = Zcoordinates;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getMass(){
        return mass;
    }
    public void setMass(double mass){
        this.mass = mass;
    }

    public double getGravity() {
        return gravity;
    }
    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getRadiusOfRevolution(){
        return radiusOfRevolution;
    }
    public void setRadiusOfRevolution(double radiusOfRevolution){
        this.radiusOfRevolution = radiusOfRevolution;
    }

    public double getXVelocity(){
        return xVelocity;
    }
    public void setXVelocity(double xVelocity){
        this.xVelocity = xVelocity;
    }

    public double getYVelocity(){
        return yVelocity;
    }
    public void setYVelocity(double yVelocity){
        this.yVelocity = yVelocity;
    }

    public double getZVelocity(){
        return zVelocity;
    }
    public void setZVelocity(double zVelocity){
        this.zVelocity = zVelocity;
    }

    public double getXCoordinates(){
        return Xcoordinates;
    }
    public void setXCoordinates(double Xcoordinates){
        this.Xcoordinates = Xcoordinates;
    }

    public double getYCoordinates(){
        return Ycoordinates;
    }
    public void setYCoordinates(double Ycoordinates){
        this.Ycoordinates = Ycoordinates;
    }

    public double getZCoordinates(){
        return Zcoordinates;
    }
    public void setZCoordinates(double Zcoordinates){
        this.Zcoordinates = Zcoordinates;
    }
}
