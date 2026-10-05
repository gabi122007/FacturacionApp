package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.facturacionapp.dao.CategoriaDao;
import ni.edu.uam.facturacionapp.dao.ProductoDao;
import ni.edu.uam.facturacionapp.model.Categoria;
import ni.edu.uam.facturacionapp.model.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class ProductoController {
    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final ProductoDao productoDAO = new ProductoDao();
    private String rutaImagen;
    private final CategoriaDao categoriaDAO = new CategoriaDao();

    private Producto productoEdicion = null;
    @FXML private TextField txtBuscar;

    @FXML
    private void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colCategoria.setCellValueFactory(cellData -> {
            Categoria cat = cellData.getValue().getCategoria();
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "");
        });

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        try {
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listar()));
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage());
        }

        tblProductos.setItems(productos);
        chkActivo.setSelected(true);
        cargarProductos();
        Buscador();
    }

    private void cargarProductos() {
        try {
            productos.clear();
            productos.addAll(productoDAO.listar());
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar productos de PostgreSQL: " + e.getMessage());
        }
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
        }
    }

    @FXML
    private void editar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Por favor, selecciona un producto de la tabla para editar.");
            return;
        }

        this.productoEdicion = seleccionado;

        txtCodigo.setText(seleccionado.getCodigo());
        txtNombre.setText(seleccionado.getNombre());
        txtPrecio.setText(seleccionado.getPrecioVenta() != null ? seleccionado.getPrecioVenta().toString() : "");
        txtExistencia.setText(String.valueOf(seleccionado.getExistencia()));
        chkActivo.setSelected(seleccionado.getActivo() != null && seleccionado.getActivo());

        if (seleccionado.getCategoria() != null) {
            for (Categoria cat : cmbCategoria.getItems()) {
                if (cat.getId() == seleccionado.getCategoria().getId()) {
                    cmbCategoria.setValue(cat);
                    break;
                }
            }
        }

        if (seleccionado.getRutaImagen() != null && !seleccionado.getRutaImagen().isBlank()) {
            this.rutaImagen = seleccionado.getRutaImagen();
            try {
                imgProducto.setImage(new Image(this.rutaImagen));
            } catch (Exception e) {
                imgProducto.setImage(null);
            }
        } else {
            imgProducto.setImage(null);
            this.rutaImagen = null;
        }
    }

    @FXML
    private void guardar() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || txtPrecio.getText().isBlank() || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete los campos obligatorios.");
            return;
        }
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "Precio mayor que cero y existencia no negativa.");
                return;
            }

            if (productoEdicion == null) {
                Producto nuevoProducto = new Producto(
                        null,
                        txtCodigo.getText().trim(),
                        txtNombre.getText().trim(),
                        cmbCategoria.getValue(),
                        precio,
                        existencia,
                        rutaImagen,
                        chkActivo.isSelected()
                );

                if (productoDAO.insertar(nuevoProducto)) {
                    mensaje(Alert.AlertType.INFORMATION, "Producto guardado con éxito en PostgreSQL.");
                    cargarProductos();
                    limpiar();
                }
            } else {
                productoEdicion.setCodigo(txtCodigo.getText().trim());
                productoEdicion.setNombre(txtNombre.getText().trim());
                productoEdicion.setCategoria(cmbCategoria.getValue());
                productoEdicion.setPrecioVenta(precio);
                productoEdicion.setExistencia(existencia);
                productoEdicion.setRutaImagen(rutaImagen);
                productoEdicion.setActivo(chkActivo.isSelected());

                if (productoDAO.actualizar(productoEdicion)) {
                    mensaje(Alert.AlertType.INFORMATION, "Producto actualizado con éxito.");
                    cargarProductos();
                    limpiar();
                } else {
                    mensaje(Alert.AlertType.ERROR, "No se pudo actualizar el producto.");
                }
            }

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Por favor, selecciona un producto de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Estás seguro de que deseas eliminar el producto '" + productoSeleccionado.getNombre() + "'?",
                ButtonType.YES,
                ButtonType.NO
        );

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
            try {
                if (productoDAO.eliminar(productoSeleccionado.getId())) {
                    mensaje(Alert.AlertType.INFORMATION, "Producto eliminado con éxito.");
                    cargarProductos();
                    limpiar();
                } else {
                    mensaje(Alert.AlertType.ERROR, "No se pudo eliminar el producto.");
                }
            } catch (SQLException e) {
                mensaje(Alert.AlertType.ERROR, "Error al eliminar en PostgreSQL: " + e.getMessage());
            }
        }
    }

    private void Buscador() {
        FilteredList<Producto> productosFiltrados = new FilteredList<>(productos, p -> true);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            productosFiltrados.setPredicate(p -> {
                if (newValue == null || newValue.isBlank()) {
                    return true;
                }

                String busqueda = newValue.toLowerCase().trim();

                if (p.getNombre() != null && p.getNombre().toLowerCase().contains(busqueda)) {
                    return true;
                } else if (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(busqueda)) {
                    return true;
                } else if (p.getCategoria() != null && p.getCategoria().getNombre() != null
                        && p.getCategoria().getNombre().toLowerCase().contains(busqueda)) {
                    return true;
                }

                return false;
            });
        });


        SortedList<Producto> productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tblProductos.comparatorProperty());

        tblProductos.setItems(productosOrdenados);
    }


    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    @FXML
    private void limpiar() {
        txtCodigo.clear(); txtNombre.clear(); txtPrecio.clear(); txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true); imgProducto.setImage(null); rutaImagen = null;
        productoEdicion = null;
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}