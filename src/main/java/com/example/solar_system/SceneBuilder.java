package com.example.solar_system;


import javafx.scene.*;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Builds and configures the 3D scene for the solar system visualization.
 * This includes setting up the root node, lighting, orbit rendering, and background.
 */
public class SceneBuilder {
    private final int SCALE;
    private final Group root;
    private final OrbitRendering orbitRenderer;

    /**
     * Constructs a SceneBuilder with the specified scale.
     *
     * @param scale the scale factor to convert real-world coordinates into scene units
     */
    public SceneBuilder(int scale) {
        this.SCALE = scale;
        this.root = new Group();
        this.orbitRenderer = new OrbitRendering(scale);
        Node backdrop = SpaceFiller.createBackdrop(50000);
        root.getChildren().add(backdrop);
    }

    /**
     * Sets up the lighting for the 3D scene.
     * Includes a directional sunlight, ambient light, fill light, and rim light
     * with reduced intensities for a balanced visual aesthetic.
     */
    public void setupLighting() {
        PointLight sunLight = new PointLight(Color.rgb(128, 122, 100)); // ~half of (255, 244, 200)
        sunLight.setTranslateX(600);
        sunLight.setTranslateY(-400);
        sunLight.setTranslateZ(-900);

        // * Ambient light — reduced brightness
        AmbientLight ambientLight = new AmbientLight(Color.color(0.075, 0.075, 0.09)); // halved from (0.15, 0.15, 0.18)

        // * Fill light — reduced cool tone
        PointLight fillLight = new PointLight(Color.color(0.06, 0.09, 0.15)); // halved
        fillLight.setTranslateX(-250);
        fillLight.setTranslateY(300);
        fillLight.setTranslateZ(350);

        // * Rim light — dimmed to subtle highlight
        PointLight rimLight = new PointLight(Color.color(0.1, 0.125, 0.15)); // halved
        rimLight.setTranslateX(-400);
        rimLight.setTranslateY(-100);
        rimLight.setTranslateZ(500);

        root.getChildren().addAll(sunLight, ambientLight, fillLight, rimLight);
    }


    public SubScene createSubScene() {
        SubScene subScene = new SubScene(root, 1000, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        return subScene;
    }

    /**
     * Adds orbit rings to the scene for the given list of celestial bodies.
     *
     * @param bodies the celestial bodies for which orbits should be rendered
     */
    public void prepareOrbits(List<CelestialBody> bodies) {
        orbitRenderer.addOrbits(bodies);
        root.getChildren().add(orbitRenderer.getOrbitGroup());
    }

    /**
     * Returns the root group node of the scene graph.
     *
     * @return the root Group node
     */
    public Group getRoot() {
        return root;
    }

    public OrbitRendering getOrbitRenderer() {
        return orbitRenderer;
    }
}
