package com.example.solar_system;

import com.almasb.fxgl.scene3d.Cylinder;
import com.example.utilities.Vector3D;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class SpaceShipBuilder {
    public static Group build(Vector3D position, int scale) {
        Group spaceshipGroup = new Group();

        // Body box
        Box bodyBox = new Box(7.5, 11, 6); // width - height - depth

        Image body = new Image(SpaceShipBuilder.class.getResource("/styles/solarSystemStyling/rocketMesh.png").toExternalForm(), false);
        Image fins = new Image(SpaceShipBuilder.class.getResource("/styles/solarSystemStyling/finsMesh.png").toExternalForm(), false);
        Image man = new Image(SpaceShipBuilder.class.getResource("/styles/solarSystemStyling/spaceMan.png").toExternalForm(), false);

        PhongMaterial rocketBodyMaterial = BodyDecorations.createMaterialFromImages(Color.WHITE, body);
        PhongMaterial finsMaterial = BodyDecorations.createMaterialFromImages(Color.DARKRED, fins);
        bodyBox.setMaterial(rocketBodyMaterial);

        // Nose as a rotated cylinder (cone-like shape)
        Cylinder nose = new Cylinder(3.5, 4.5); // radius, height
        nose.setMaterial(finsMaterial);
        nose.setRotationAxis(Rotate.X_AXIS);
        nose.setRotate(180);   // Lay it forward along X axis
        nose.setTranslateY(-6.5);  // Place on top of the body (half height)

        // Fins
        Box leftFin = new Box(2, 8, 3); // width - height - depth
        leftFin.setTranslateX(-5);
        leftFin.setTranslateY(3);
        leftFin.setMaterial(finsMaterial);
        Box rightFin = new Box(2, 8, 3); // width - height - depth
        rightFin.setTranslateX(5);
        rightFin.setTranslateY(3);
        rightFin.setMaterial(finsMaterial);

        // Glass
        Box glass = new Box(4, 2.3, 0.01); // width - height - depth
        glass.setRotationAxis(Rotate.X_AXIS);
        glass.setRotate(-90);
        glass.setTranslateY(-7.7);
        glass.setTranslateZ(0.6);
        PhongMaterial manMat = BodyDecorations.createMaterialFromImages(Color.LIGHTCYAN, man);
        glass.setMaterial(manMat);


        // Thrust nozzle at the back
        Cylinder nozzle = new Cylinder(3, 2); // radius - height (a short, wide cylinder)
        nozzle.setRotationAxis(Rotate.X_AXIS);
        nozzle.setTranslateY(6.5);
        nozzle.setMaterial(new PhongMaterial(Color.DARKGRAY));
        nozzle.setMaterial(rocketBodyMaterial);


        // Inner core flame (sharp center)
        Cylinder innerCore = new Cylinder(1.2, 2);
        innerCore.setRotationAxis(Rotate.X_AXIS);
        innerCore.setTranslateY(9);
        innerCore.setRotate(180);
        innerCore.setScaleX(0.8);
        innerCore.setScaleZ(0.8);
        PhongMaterial innerMaterial = new PhongMaterial(Color.rgb(255, 100, 0, 1.0));
        innerCore.setMaterial(innerMaterial);

        // Core flame (middle orange)
        Cylinder coreFlame = new Cylinder(1.5, 2);
        coreFlame.setRotationAxis(Rotate.X_AXIS);
        coreFlame.setTranslateY(9);
        coreFlame.setRotate(180);
        coreFlame.setScaleX(0.85);
        coreFlame.setScaleZ(0.85);
        PhongMaterial coreMaterial = new PhongMaterial(Color.rgb(255, 120, 0, 0.85));
        coreFlame.setMaterial(coreMaterial);

        // Outer flame (soft transparent glow)
        Cylinder outerFlame = new Cylinder(2.5, 4);
        outerFlame.setRotationAxis(Rotate.X_AXIS);
        outerFlame.setTranslateY(9);
        outerFlame.setRotate(180);
        outerFlame.setScaleX(1.0);
        outerFlame.setScaleZ(1.0);
        PhongMaterial outerMaterial = new PhongMaterial(Color.rgb(255, 180, 0, 0.25));
        outerFlame.setMaterial(outerMaterial);

        // Glow aura (duplicate outer flame with max blur)
        Cylinder aura = new Cylinder(3.2, 6);
        aura.setRotationAxis(Rotate.X_AXIS);
        aura.setTranslateY(9);
        aura.setRotate(180);
        aura.setScaleX(1.3);
        aura.setScaleZ(1.3);
        PhongMaterial auraMaterial = new PhongMaterial(Color.rgb(255, 200, 50, 0.15));
        aura.setMaterial(auraMaterial);

    // Combining Glow and DropShadow for radiant glow
        Glow glow = new Glow(0.1);
        DropShadow radiatingShadow = new DropShadow();
        radiatingShadow.setColor(Color.ORANGE);
        radiatingShadow.setOffsetX(0);
        radiatingShadow.setOffsetY(0);
        radiatingShadow.setRadius(100);
        radiatingShadow.setSpread(1.0);
        radiatingShadow.setBlurType(BlurType.GAUSSIAN);
        glow.setInput(radiatingShadow);
        outerFlame.setEffect(glow);
        aura.setEffect(glow);

        // * Flickering animation handling.
        ScaleTransition flickerInner = new ScaleTransition(Duration.millis(20), innerCore);  // ~50 / 3
        flickerInner.setFromY(0.7);
        flickerInner.setToY(1.4);
        flickerInner.setCycleCount(Animation.INDEFINITE);
        flickerInner.setAutoReverse(true);
        flickerInner.play();

        ScaleTransition flickerCore = new ScaleTransition(Duration.millis(35), coreFlame);   // ~70 / 3
        flickerCore.setFromY(0.7);
        flickerCore.setToY(1.4);
        flickerCore.setCycleCount(Animation.INDEFINITE);
        flickerCore.setAutoReverse(true);
        flickerCore.play();

        ScaleTransition flickerOuter = new ScaleTransition(Duration.millis(50), outerFlame);  // ~100 / 3
        flickerOuter.setFromY(0.75);
        flickerOuter.setToY(1.5);
        flickerOuter.setCycleCount(Animation.INDEFINITE);
        flickerOuter.setAutoReverse(true);
        flickerOuter.play();


        // * Core pulsing.
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, e -> coreMaterial.setDiffuseColor(Color.rgb(255, 120, 0, 0.85))),
                new KeyFrame(Duration.millis(159), e -> coreMaterial.setDiffuseColor(Color.rgb(255, 160, 0, 0.85))),
                new KeyFrame(Duration.millis(250), e -> coreMaterial.setDiffuseColor(Color.rgb(255, 120, 0, 0.85)))
        );
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();


        spaceshipGroup.getChildren().addAll(
                bodyBox, leftFin, rightFin, nose, glass,
                nozzle, innerCore, coreFlame, outerFlame, aura
        );


        // * Spaceship rotations.
        Rotate rotateX = new Rotate(90, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        Rotate rotateZ = new Rotate(90, Rotate.Z_AXIS);

        spaceshipGroup.getTransforms().addAll(rotateX, rotateY, rotateZ);



        spaceshipGroup.setTranslateX(position.x / scale);
        spaceshipGroup.setTranslateY(position.z / scale);
        spaceshipGroup.setTranslateZ(position.y / scale);

        return spaceshipGroup;
    }
}
