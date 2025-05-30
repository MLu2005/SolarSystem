package com.example.solar_system;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.Node;

/**
 * Utility class for generating a 3D spherical space backdrop for the solar system scene.
 * Renders a skybox-style environment using an inward-facing textured sphere.
 */
public class SpaceFiller {

/**
 * Creates a non-interactive, inward-facing textured sphere that simulates the space background.
 */
    public static Node createBackdrop(double radius) {
        Sphere spaceSphere = new Sphere(radius);
        spaceSphere.setCullFace(CullFace.FRONT);
        spaceSphere.setDrawMode(DrawMode.FILL);

        try {
            Image starTexture = new Image(
                    SpaceFiller.class.getResource("/styles/voidMesh7.png").toExternalForm()
            );
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseMap(starTexture);
            material.setSpecularColor(Color.BLACK);
            spaceSphere.setMaterial(material);
        } catch (Exception e) {
            System.err.println("Failed to load background texture: " + e.getMessage());
            spaceSphere.setMaterial(new PhongMaterial(Color.BLACK));
        }

        spaceSphere.setMouseTransparent(true);
        return spaceSphere;
    }
}
