module app_compras {
    // Dependencias necesarias
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;

    // Apertura de paquetes a JavaFX (para carga FXML y reflexión)
    opens app_compras to javafx.fxml;
    opens controllers to javafx.fxml;
    opens config to javafx.fxml;
    opens dao to javafx.fxml;
    opens models to javafx.fxml;

    // Exportación de paquetes (para ser accesibles desde otros módulos/clases)
    exports app_compras;
    exports controllers;
    exports config;
    exports dao;
    exports models;
}
