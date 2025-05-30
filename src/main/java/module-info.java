module com.example.project {

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.compiler;
    requires exp4j;
    requires annotations;
    requires com.almasb.fxgl.all;
    requires java.xml;

    opens executables to javafx.fxml;
    exports executables;
    opens com.example.utilities to javafx.graphics;
    opens com.example.utilities.GA to javafx.graphics;
    opens com.example.solar_system to javafx.graphics;

}