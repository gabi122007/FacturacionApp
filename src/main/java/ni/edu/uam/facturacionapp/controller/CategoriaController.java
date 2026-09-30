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
import java.util.Optional;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActivo;

    // Referencias a botones según fx:id en el FXML
    @FXML private Button btnEliminar;
    @FXML private Button btnActualizar;

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final CategoriaDao categoriaDAO = new CategoriaDao();

    // Variable para rastrear la categoría que se está editando
    private Categoria categoriaEdicion = null;

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

    // Método asociado al botón "Editar" o selección
    @FXML
    private void editar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Por favor, selecciona una categoría de la tabla para editar.");
            return;
        }

        this.categoriaEdicion = seleccionada;
        txtNombre.setText(seleccionada.getNombre());
        chkActivo.setSelected(seleccionada.isActiva());
    }

    // Método invocado por el botón con onAction="#actualizar" en tu FXML
    @FXML
    private void actualizar() {
        // Carga los datos seleccionados al formulario o ejecuta la edición
        editar();
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Ingrese el nombre de la categoría.");
            return;
        }

        try {
            if (categoriaEdicion == null) {
                // Modo: INSERTAR
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
            } else {
                // Modo: ACTUALIZAR
                categoriaEdicion.setNombre(txtNombre.getText().trim());
                categoriaEdicion.setActiva(chkActivo.isSelected());

                if (categoriaDAO.actualizar(categoriaEdicion)) {
                    mensaje(Alert.AlertType.INFORMATION, "Categoría actualizada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mensaje(Alert.AlertType.ERROR, "No se pudo actualizar la categoría.");
                }
            }
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos: " + e.getMessage());
        }
    }

    // Método invocado por el botón con onAction="#eliminar" en tu FXML
    @FXML
    private void eliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Por favor, selecciona una categoría de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Estás seguro de que deseas eliminar la categoría '" + seleccionada.getNombre() + "'?",
                ButtonType.YES,
                ButtonType.NO
        );

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            try {
                if (categoriaDAO.eliminar(seleccionada.getId())) {
                    mensaje(Alert.AlertType.INFORMATION, "Categoría eliminada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mensaje(Alert.AlertType.ERROR, "No se pudo eliminar la categoría.");
                }
            } catch (SQLException e) {
                mensaje(Alert.AlertType.ERROR, "Error al eliminar en PostgreSQL: " + e.getMessage());
            }
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActivo.setSelected(true);
        categoriaEdicion = null; // Reiniciar estado de edición
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}