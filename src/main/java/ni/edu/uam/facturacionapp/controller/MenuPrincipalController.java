package ni.edu.uam.facturacionapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.facturacionapp.util.SceneManager;
import java.io.IOException;

import static ni.edu.uam.facturacionapp.util.SceneManager.abrirVentana;

public class MenuPrincipalController {

    @FXML
    private void abrirProductos() {
        try {
            abrirVentana(
                    "/ni/edu/uam/facturacionapp/fxml/producto-view.fxml",
                    "Gestión de productos");
        } catch (IOException e) {
            // IMPRIMIR EL ERROR EN LA CONSOLA
            e.printStackTrace();

            // MOSTRAR EL ERROR TÉCNICO EN LA ALERTA
            new Alert(Alert.AlertType.ERROR,
                    "No fue posible abrir Productos: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void abrirCategorias() {
        try {
            abrirVentana(
                    "/ni/edu/uam/facturacionapp/fxml/categoria-view.fxml",
                    "Gestión de categorías");
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "No fue posible abrir Categorías: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void salir() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea cerrar la aplicación?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
            Platform.exit();
    }
}