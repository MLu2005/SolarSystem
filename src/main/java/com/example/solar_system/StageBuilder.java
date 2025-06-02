package com.example.solar_system;

import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

import java.util.List;

/**
 * Builds and configures the 3D scene for the solar system visualization.
 * This includes setting up the root node, lighting, orbit rendering, and background.
 */
public class StageBuilder {
    private final int SCALE;
    private final Group root;
    private final OrbitRendering orbitRenderer;

    /**
     * Constructs a SceneBuilder with the specified scale.
     *
     * @param scale the scale factor to convert real-world coordinates into scene units
     */
    public StageBuilder(int scale) {
        this.SCALE = scale;
        this.root = new Group();
        this.orbitRenderer = new OrbitRendering(scale);

        Node backdrop = createSkyBoxBackdrop(30000);
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


    /**
     * Creates a skybox cube using 6 thin boxes textured on each face to avoid texture stretching.
     *
     * @param size length of each cube edge
     * @return a Group node representing the skybox cube
     */
    private Node createSkyBoxBackdrop(double size) {
        Group skyboxGroup = new Group();
        double half = size / 2;
        double thickness = 0.1;

        String texturePath = getClass().getResource("/styles/solarSystemStyling/voidMesh.jpg").toExternalForm();

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image(texturePath));


        Box rightFace = createFaceBox(size, size, thickness, material);
        rightFace.setTranslateX(half);
        rightFace.setRotationAxis(Rotate.Y_AXIS);
        rightFace.setRotate(90);


        Box leftFace = createFaceBox(size, size, thickness, material);
        leftFace.setTranslateX(-half);
        leftFace.setRotationAxis(Rotate.Y_AXIS);
        leftFace.setRotate(-90);


        Box topFace = createFaceBox(size, size, thickness, material);
        topFace.setTranslateY(-half);
        topFace.setRotationAxis(Rotate.X_AXIS);
        topFace.setRotate(-90);


        Box bottomFace = createFaceBox(size, size, thickness, material);
        bottomFace.setTranslateY(half);
        bottomFace.setRotationAxis(Rotate.X_AXIS);
        bottomFace.setRotate(90);


        Box frontFace = createFaceBox(size, size, thickness, material);
        frontFace.setTranslateZ(half);

        Box backFace = createFaceBox(size, size, thickness, material);
        backFace.setTranslateZ(-half);
        backFace.setRotationAxis(Rotate.Y_AXIS);
        backFace.setRotate(180);

        skyboxGroup.getChildren().addAll(
                rightFace, leftFace, topFace, bottomFace, frontFace, backFace
        );

        return skyboxGroup;
    }

    private Box createFaceBox(double width, double height, double depth, PhongMaterial material) {
        Box face = new Box(width, height, depth);
        face.setMaterial(material);
        return face;
    }

}
