package com.example.solar_system;

import javafx.scene.paint.PhongMaterial;

public interface MaterialProvider {

    /**
     * Creates and returns a new PhongMaterial.
     *
     * can apply glassy material - Rocky material.
     * There exists other type that I don't know :(
     *
     * @return a new instance of PhongMaterial
     */
    PhongMaterial createMaterial();
}
