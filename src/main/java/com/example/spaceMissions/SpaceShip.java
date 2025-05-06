package com.example.spaceMissions;

import com.example.solarSystem.Vector3D;

public class SpaceShip{

    private String name;
    private double thrust;
    private Vector3D velocity;
    private double mass;
    private double fuel;
    private Vector3D position;

    public SpaceShip (String Name, double Thrust, Vector3D Velocity, double Mass, double Fuel, Vector3D Position) {
        name = Name;
        thrust = Thrust;
        velocity = Velocity;
        mass = Mass;
        fuel = Fuel;
        position = Position;
    }
    //getters
    public String getName () {
        return name;
    }
    public double getThrust () {
        return thrust;
    }
    public Vector3D getVelocity () {
        return velocity;
    }
    public double getMass () {
        return mass;
    }
    public double getFuel () {
        return fuel;
    }
    public Vector3D getPosition () {
        return position;
    }
    //setter
    public void setName (String newName) {
        name = newName;
    }
    public void setThrust (double newThrust) {
        thrust = newThrust;
    }
    public void setVelocity (Vector3D newVelocity) {
        velocity = newVelocity;
    }
    public void setFuel (double newFuel) {
        fuel = newFuel;
    }
    public void setPosition (Vector3D newPosition) {
        position = newPosition;
    }
}