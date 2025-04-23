module com.example.project {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.compiler;
    requires java.desktop;
    requires exp4j;

    opens executables to javafx.fxml;
    exports executables;
}