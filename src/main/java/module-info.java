module roman {
    requires javafx.controls;
    requires javafx.fxml;

    opens controller to javafx.fxml;

    exports app;
    exports controller;
    exports model;
    exports algorithm;
    exports service;
    exports util;
}
