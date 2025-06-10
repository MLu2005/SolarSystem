module com.example.project {

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.compiler;
    requires exp4j;
    requires annotations;
    requires com.almasb.fxgl.all;
    requires javafx.media;
    requires com.jfoenix;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;

    opens com.example.coreGui to javafx.fxml;
    exports com.example.coreGui;

    opens com.example.utilities to javafx.graphics;
    opens com.example.utilities.GA to javafx.graphics;

    // Export packages needed for testing
    exports com.example.utilities;
    exports com.example.utilities.physics_utilities;
    exports com.example.utilities.GA;
    exports com.example.utilities.HillClimb;
    exports com.example.spaceMissions;
    exports com.example.lander;

    exports com.example.solar_system;
    opens com.example.solar_system to javafx.fxml;

    exports com.example.ode_gui;
    opens com.example.ode_gui to javafx.fxml;
    exports com.example.utilities.Ship;
    exports com.example;
    opens com.example to javafx.fxml;
}
