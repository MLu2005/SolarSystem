module com.example.project {

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.compiler;
    requires exp4j;
    requires annotations;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.media;
    requires com.jfoenix;

    opens com.example.coreGui to javafx.fxml;
    exports com.example.coreGui;

    opens executables to javafx.fxml;
    exports executables;

    opens com.example.utilities to javafx.graphics;
    opens com.example.utilities.GA to javafx.graphics;

    // Export packages needed for testing
    exports com.example.utilities;
    exports com.example.utilities.physics_utilities;
    exports com.example.utilities.GA;
    exports com.example.spaceMissions;

    exports com.example.solar_system;
    opens com.example.solar_system to javafx.fxml;

    exports com.example.main_gui;
    opens com.example.main_gui to javafx.fxml;
    exports com.example.utilities.GD.Opitmizers;
    exports com.example.utilities.GD.Controllers;
    exports com.example.utilities.GD.Utility;
    exports com.example.utilities.GD;
    exports com.example.utilities.Ship;
}
