package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.facturacionapp.dao.CategoriaDao;
import ni.edu.uam.facturacionapp.model.Categoria;
import java.sql.SQLException;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActivo;

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final CategoriaDao categoriaDAO = new CategoriaDao();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));


        colActivo.setCellValueFactory(new PropertyValueFactory<>("activa"));

        tblCategorias.setItems(categorias);
        chkActivo.setSelected(true);

        cargarCategorias();
    }

    private void cargarCategorias() {
        try {
            categorias.clear();
            categorias.addAll(categoriaDAO.listar());
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar categorías desde PostgreSQL: " + e.getMessage());
        }
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Ingrese el nombre de la categoría.");
            return;
        }

        try {
            Categoria nuevaCategoria = new Categoria(
                    null,
                    txtNombre.getText().trim(),
                    chkActivo.isSelected()
            );


            if (categoriaDAO.insertar(nuevaCategoria)) {
                mensaje(Alert.AlertType.INFORMATION, "Categoría guardada con éxito.");
                cargarCategorias();
                limpiar();
            }
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos: " + e.getMessage());
        }
    }


    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActivo.setSelected(true);
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}