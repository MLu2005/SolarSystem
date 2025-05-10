package com.example.solarSystem.GA;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Physics.SolarSystemFactory;
import com.example.solarSystem.Vector3D;
import com.example.solarSystem.Physics.PhysicsEngine;
import executables.solvers.Constants;

import java.util.List;
import java.util.Random;
import java.util.Vector;

class Individual {

    private static final double EARTH_RADIUS = Constants.EARTH_RADIUS_KM;
    private static final double MAX_DV = 60.0;  // max rel‑speed km/s
    private static final double PROBE_MASS = Constants.PROBE_MASS; // kg


    private static final List<CelestialBody> OBJECTS_IN_SPACE =
            SolarSystemFactory.loadFromTable();
    private static final CelestialBody EARTH = find("Earth");
    private static final CelestialBody TITAN = createTitanStub();

    private static CelestialBody find(String name) {
        return OBJECTS_IN_SPACE.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Body " + name + " not found"));
    }
    /**
     * Adds a simple circular‑orbit approximation for Titan around Saturn.
     * Replace this stub with real ephemeris data for production use.
     */
    private static CelestialBody createTitanStub() {
        CelestialBody saturn = find("Saturn");
        Vector3D rSatTitan = new Vector3D(1_221_870, 0, 0);
        Vector3D vSatTitan = new Vector3D(0, 5.57, 0);
        return new CelestialBody("Titan", 1.3452E23,
                saturn.getPosition().add(rSatTitan),
                saturn.getVelocity().add(vSatTitan));
    }

    private final Vector<Double> gene;     // x,y,z,vx,vy,vz,m
    private double minDistanceTitanKm;
    private double fitness;

    public Individual() { this(randomGene()); }
    private Individual(Vector<Double> g) { gene = g; }
    private static Individual of(Vector<Double> g){ return new Individual(g); }

    /**
     * Generates a random gene for a new individual.
     *
     * The gene is a vector of 7 elements:
     * - position on Earth surface (x,y,z)
     * - velocity relative to Earth (vx,vy,vz)
     * - mass of the probe (kg)
    */
    private static Vector<Double> randomGene() {
        // constant launch site: sub-Titan point on Earth
        Vector3D earthPos = EARTH.getPosition();
        Vector3D titanPos = TITAN.getPosition();

        Vector3D dirToTitan = titanPos.subtract(earthPos).normalize();
        Vector3D surfaceOffset = dirToTitan.scale(EARTH_RADIUS);

        Vector3D pos = earthPos.add(surfaceOffset);     // absolute launch position

        // random velocity <= 60 km/s relative to Earth
        double thetaV = 2 * Math.PI * Constants.RNG.nextDouble();
        double phiV   = Math.acos(2 * Constants.RNG.nextDouble() - 1);
        double speed  = MAX_DV * Constants.RNG.nextDouble();
        Vector3D dv = new Vector3D(speed * Math.sin(phiV) * Math.cos(thetaV),
                speed * Math.sin(phiV) * Math.sin(thetaV),
                speed * Math.cos(phiV));
        Vector3D vel = EARTH.getVelocity().add(dv);

        return new Vector<>(List.of(pos.getX(), pos.getY(), pos.getZ(),
                vel.getX(), vel.getY(), vel.getZ(),
                PROBE_MASS));
    }

    void evaluate() {
        PhysicsEngine engine = new PhysicsEngine();
        for (CelestialBody b : OBJECTS_IN_SPACE)
            engine.addBody(cloneBody(b));

        // cloned titan, so we dont have to look for it again and again and we never get a null pointer
        CelestialBody titanClone = cloneBody(TITAN);
        engine.addBody(titanClone);

        CelestialBody probe = new CelestialBody(
                "Probe", PROBE_MASS,
                new Vector3D(gene.get(0), gene.get(1), gene.get(2)),
                new Vector3D(gene.get(3), gene.get(4), gene.get(5)));
        engine.addBody(probe);

        double dt       = 3600;
        double SIM_LEN  = 365 * 86400;
        double t        = 0.0;

        minDistanceTitanKm = Double.MAX_VALUE;

        while (t <= SIM_LEN) {
            double d = probe.getPosition()
                    .subtract(titanClone.getPosition())
                    .magnitude();
            if (d < minDistanceTitanKm) minDistanceTitanKm = d;

            engine.step(dt);
            t += dt;
        }
        fitness = 1e6 / (minDistanceTitanKm + 1_000);
    }

    private CelestialBody cloneBody(CelestialBody b) {
        Vector3D p = b.getPosition();
        Vector3D v = b.getVelocity();
        return new CelestialBody(
                b.getName(),
                b.getMass(),
                new Vector3D(p.getX(), p.getY(), p.getZ()),
                new Vector3D(v.getX(), v.getY(), v.getZ()));
    }

    private CelestialBody find(PhysicsEngine e, String name) {
        return e.getBodies().stream().filter(b -> b.getName().equals(name)).findFirst().orElseThrow();
    }

    public double getMinDistanceKm() { return minDistanceTitanKm; }
    public double getFitness()       { return fitness;          }
    public Vector<Double> genes()    { return gene;            }


    /**
     * Mutates the individual by small surface wiggle and velocity tweak +- 0.5 km/s.
     *
     * @return a new individual with mutated genes
     */
    public Individual mutate() {
        Vector<Double> g = (Vector<Double>) gene.clone();
        Random rand = Constants.RNG;

        // small surface wiggle (+- 0.05 °)
        Vector3D relPos = new Vector3D(g.get(0), g.get(1), g.get(2)).subtract(EARTH.getPosition());
        double r = relPos.magnitude();
        double theta = Math.atan2(relPos.getY(), relPos.getX()) + (rand.nextDouble()*2 - 1)*Math.toRadians(0.05);
        double phi   = Math.acos(relPos.getZ()/r)              + (rand.nextDouble()*2 - 1)*Math.toRadians(0.05);
        Vector3D pos = EARTH.getPosition().add(new Vector3D(
                EARTH_RADIUS * Math.sin(phi) * Math.cos(theta),
                EARTH_RADIUS * Math.sin(phi) * Math.sin(theta),
                EARTH_RADIUS * Math.cos(phi)));

        // velocity tweak +-0.5 km/s
        Vector3D dv = new Vector3D((rand.nextDouble()*2 - 1)*0.5,
                (rand.nextDouble()*2 - 1)*0.5,
                (rand.nextDouble()*2 - 1)*0.5);
        Vector3D relV = new Vector3D(g.get(3), g.get(4), g.get(5)).subtract(EARTH.getVelocity()).add(dv);
        double speed = relV.magnitude();

        if (speed > MAX_DV) relV = relV.scale(MAX_DV/speed);
        Vector3D vel = EARTH.getVelocity().add(relV);
        g.set(0, pos.getX()); g.set(1, pos.getY()); g.set(2, pos.getZ());
        g.set(3, vel.getX()); g.set(4, vel.getY()); g.set(5, vel.getZ());
        return Individual.of(g);
    }

    public static Individual crossover(Individual p1, Individual p2) {
        Random rand = Constants.RNG;
        double t = rand.nextDouble();

        Vector3D pos1 = new Vector3D(p1.genes().get(0), p1.genes().get(1), p1.genes().get(2));
        Vector3D pos2 = new Vector3D(p2.genes().get(0), p2.genes().get(1), p2.genes().get(2));
        Vector3D vel1 = new Vector3D(p1.genes().get(3), p1.genes().get(4), p1.genes().get(5)).subtract(EARTH.getVelocity());
        Vector3D vel2 = new Vector3D(p2.genes().get(3), p2.genes().get(4), p2.genes().get(5)).subtract(EARTH.getVelocity());

        Vector3D pos = pos1.scale(1 - t).add(pos2.scale(t)); // blend positions and re‑project on sphere
        Vector3D dir = pos.subtract(EARTH.getPosition()).normalize();
        pos = EARTH.getPosition().add(dir.scale(EARTH_RADIUS));

        Vector3D dv = vel1.scale(1 - t).add(vel2.scale(t));
        double speed = dv.magnitude();
        if (speed > MAX_DV) dv = dv.scale(MAX_DV / speed);
        Vector3D vel = EARTH.getVelocity().add(dv);

        Vector<Double> g = new Vector<>(List.of(
                pos.getX(), pos.getY(), pos.getZ(),
                vel.getX(), vel.getY(), vel.getZ(),
                PROBE_MASS));
        return Individual.of(g);
    }

    @Override public String toString() {
        return String.format("fitness %.6f  dTitan %.1f km\nlaunch genes: %s",
                fitness, minDistanceTitanKm, gene);
    }
}