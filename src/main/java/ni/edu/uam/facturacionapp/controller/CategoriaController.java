package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    @FXML private TextField txtBuscar;

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final CategoriaDao categoriaDAO = new CategoriaDao();

    private Categoria categoriaEdicion = null;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activa"));

        tblCategorias.setItems(categorias);
        chkActivo.setSelected(true);

        cargarCategorias();
        Buscador();
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


    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "Ingrese el nombre de la categoría.");
            return;
        }

        try {
            if (categoriaEdicion == null) {
                if (categoriaDAO.existeNombre(txtNombre.getText().trim(), null)) {
                    mensaje(Alert.AlertType.WARNING, "Ya existe una categoría registrada con el nombre: " + txtNombre.getText().trim());
                    return;
                }

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
                if (categoriaDAO.existeNombre(txtNombre.getText().trim(), categoriaEdicion.getId())) {
                    mensaje(Alert.AlertType.WARNING, "Ya existe otra categoría con el nombre: " + txtNombre.getText().trim());
                    return;
                }
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

    private void Buscador() {

        FilteredList<Categoria> categoriasFiltradas = new FilteredList<>(categorias, c -> true);


        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            categoriasFiltradas.setPredicate(categoria -> {
                if (newValue == null || newValue.isBlank()) {
                    return true;
                }

                String busqueda = newValue.toLowerCase().trim();


                if (categoria.getNombre().toLowerCase().contains(busqueda)) {
                    return true;
                }

                else if (String.valueOf(categoria.getId()).contains(busqueda)) {
                    return true;
                }

                return false;
            });
        });


        SortedList<Categoria> categoriasOrdenadas = new SortedList<>(categoriasFiltradas);
        categoriasOrdenadas.comparatorProperty().bind(tblCategorias.comparatorProperty());

        tblCategorias.setItems(categoriasOrdenadas);
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActivo.setSelected(true);
        categoriaEdicion = null;
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}