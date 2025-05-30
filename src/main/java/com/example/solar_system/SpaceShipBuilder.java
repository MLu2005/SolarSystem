package com.example.solar_system;

import com.example.utilities.Vector3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

    public class SpaceShipBuilder {
        public static Group build(Vector3D position, int scale) {
            Group spaceshipGroup = new Group();

            Box bodyBox = new Box(15, 100, 15); // width, height, depth
            bodyBox.setMaterial(new PhongMaterial(Color.DARKGRAY));


            Sphere nose = new Sphere(5);
            nose.setMaterial(new PhongMaterial(Color.SILVER));
            nose.setTranslateY(-55);

            Box leftFin = new Box(4, 15, 10);
            leftFin.setTranslateX(-10);
            leftFin.setTranslateY(40);
            leftFin.setMaterial(new PhongMaterial(Color.RED));

            Box rightFin = new Box(4, 15, 10);
            rightFin.setTranslateX(10);
            rightFin.setTranslateY(40);
            rightFin.setMaterial(new PhongMaterial(Color.RED));

            spaceshipGroup.getChildren().addAll(bodyBox, nose, leftFin, rightFin);

            spaceshipGroup.setRotationAxis(Rotate.X_AXIS);
            spaceshipGroup.setRotate(90);

            spaceshipGroup.setTranslateX(position.x / scale);
            spaceshipGroup.setTranslateY(position.z / scale);
            spaceshipGroup.setTranslateZ(position.y / scale);

            return spaceshipGroup;
        }

}