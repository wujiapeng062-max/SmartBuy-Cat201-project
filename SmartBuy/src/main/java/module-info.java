module com.example.smartbuy {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;

    opens com.example.smartbuy to javafx.fxml;
    opens com.example.smartbuy.controller to javafx.fxml;
    exports com.example.smartbuy;
    exports com.example.smartbuy.controller;
    exports com.example.smartbuy.model;
}
