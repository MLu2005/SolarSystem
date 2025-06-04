package com.example.utilities.GA;

import com.example.solar_system.CelestialBody;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.PhysicsEngine;
import executables.Constants;
import executables.solvers.RK4Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.function.BiFunction;

public class Individual {

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

    private final Vector<Double> gene;     // x,y,z,vx,vy,vz,m (important to know, has to go in the readme.md)
    private double minDistanceTitanKm;
    private double fitness;

    public Individual() { this(randomGene()); }
    public Individual(Vector<Double> g) { gene = g; }
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
        // Lauching from the closest point of titan to Earth
        Vector3D earthPos = EARTH.getPosition();
        Vector3D titanPos = TITAN.getPosition();

        Vector3D dirToTitan = titanPos.subtract(earthPos).normalize();
        Vector3D surfaceOffset = dirToTitan.scale(EARTH_RADIUS);

        Vector3D pos = earthPos.add(surfaceOffset); // absolute launch position

        // random velocity <= 60 km/s relative to Earth
        double thetaV= 2 * Math.PI * Constants.RNG.nextDouble();
        double phiV = Math.acos(2 * Constants.RNG.nextDouble() - 1);
        double speed= MAX_DV * Constants.RNG.nextDouble();
        Vector3D dv = new Vector3D(speed * Math.sin(phiV) * Math.cos(thetaV),
                speed * Math.sin(phiV) * Math.sin(thetaV),
                speed * Math.cos(phiV));
        Vector3D vel = EARTH.getVelocity().add(dv);

        return new Vector<>(List.of(pos.getX(), pos.getY(), pos.getZ(),
                vel.getX(), vel.getY(), vel.getZ(),
                PROBE_MASS));
    }

    public void evaluate() {
        // Set up engine with all solar bodies + cloned Titan (no probe)
        PhysicsEngine engine = new PhysicsEngine();
        for (CelestialBody b : OBJECTS_IN_SPACE) {
            engine.addBody(cloneBody(b));
        }
        CelestialBody titanClone = cloneBody(TITAN);
        engine.addBody(titanClone);

        double[] yProbe = new double[6];
        {
            CelestialBody probe0 = new CelestialBody(
                    "Probe",
                    PROBE_MASS,
                    new Vector3D(gene.get(0), gene.get(1), gene.get(2)),
                    new Vector3D(gene.get(3), gene.get(4), gene.get(5))
            );
            yProbe[0] = probe0.getPosition().getX();
            yProbe[1] = probe0.getPosition().getY();
            yProbe[2] = probe0.getPosition().getZ();
            yProbe[3] = probe0.getVelocity().getX();
            yProbe[4] = probe0.getVelocity().getY();
            yProbe[5] = probe0.getVelocity().getZ();
        }

        final double G = Constants.G;
        final double dt = 3600;
        final double SIM_T  = Constants.SIM_LEN;
        double t = 0.0;

        minDistanceTitanKm = Double.MAX_VALUE;

        RK4Solver rk4 = new RK4Solver();

        while (t < SIM_T) {


            List<Vector3D> posOld = new ArrayList<>();
            for (CelestialBody b : engine.getBodies()) {
                posOld.add(b.getPosition());
            }

            engine.step(dt);

            List<Vector3D> posNew = new ArrayList<>();
            for (CelestialBody b : engine.getBodies()) {
                posNew.add(b.getPosition());
            }

            // making sure that we can use the rk4 with the engine step and we can still
            // do the solver step inside the same time interval as the world around it
            BiFunction<Double,double[],double[]> f = (tOffset, y) -> {
                double[] dy = new double[6];
                // position derivatives
                dy[0] = y[3];
                dy[1] = y[4];
                dy[2] = y[5];

                double ax = 0, ay = 0, az = 0;
                for (int i = 0; i < posOld.size(); i++) {
                    Vector3D P0 = posOld.get(i), P1 = posNew.get(i);
                    double alpha = tOffset / dt;
                    Vector3D sysPos = P0.add(P1.subtract(P0).scale(alpha));

                    double dx = sysPos.getX() - y[0];
                    double dy1 = sysPos.getY() - y[1];
                    double dz = sysPos.getZ() - y[2];
                    double r2 = dx*dx + dy1*dy1 + dz*dz;
                    double invR3 = 1.0 / (r2 * Math.sqrt(r2));
                    double m  = engine.getBodies().get(i).getMass();

                    ax += G * m * dx * invR3;
                    ay += G * m * dy1 * invR3;
                    az += G * m * dz * invR3;
                }
                dy[3] = ax;
                dy[4] = ay;
                dy[5] = az;
                return dy;
            };
            yProbe = rk4.solveStep(f, 0.0, yProbe, dt);

            Vector3D probePos = new Vector3D(yProbe[0], yProbe[1], yProbe[2]);
            double d = probePos.subtract(titanClone.getPosition()).magnitude();
            if (d < minDistanceTitanKm) {
                minDistanceTitanKm = d;
            }

            t += dt;
        }
        fitness = 1e6 / (minDistanceTitanKm + 1000);
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

    public double getMinDistanceKm() { return minDistanceTitanKm;}
    public double getFitness()       { return fitness;}
    public Vector<Double> genes()    { return gene;}


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

    @Override
    public String toString() {
        return String.format(
                "fitness %.6f  dTitan %.6f km%n" + "launch genes: %s%n",
                fitness,
                minDistanceTitanKm,
                gene.toString()
        );
    }
}