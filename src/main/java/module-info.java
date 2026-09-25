module ni.edu.uam.facturacionapp {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires static lombok;

    opens ni.edu.uam.facturacionapp to javafx.fxml;
    opens ni.edu.uam.facturacionapp.controller to javafx.fxml;

    exports ni.edu.uam.facturacionapp;
    opens ni.edu.uam.facturacionapp.controller to javafx.fxml;
    exports ni.edu.uam.facturacionapp.controller;
    opens ni.edu.uam.facturacionapp.model to javafx.fxml;
    exports ni.edu.uam.facturacionapp.model;
}