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

    exports com.example.solar_system to javafx.graphics;
    opens com.example.solar_system to javafx.fxml;

    exports com.example.main_gui;
    opens com.example.main_gui to javafx.fxml;
}
