package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    // Componentes mapeados exactamente con los fx:id del FXML
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecioVenta; // Coincide con fx:id="txtPrecioVenta"
    @FXML private TextField txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;

    // Componentes de Búsqueda y Filtro en el FXML
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;

    // Imagen (Opcional, protegido con nulos por si no está en el FXML)
    @FXML private ImageView imgProducto;

    // Tabla y Columnas (Alineadas con la lista de columnas del FXML)
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final ProductoDao productoDAO = new ProductoDao();
    private final CategoriaDao categoriaDAO = new CategoriaDao();
    private String rutaImagen;

    // Variable para controlar si se está creando o editando un producto
    private Producto productoEdicion = null;

    @FXML
    private void initialize() {

        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        if (colCategoria != null) {
            colCategoria.setCellValueFactory(cellData -> {
                Categoria cat = cellData.getValue().getCategoria();
                return new SimpleStringProperty(cat != null ? cat.getNombre() : "");
            });
        }

        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        if (colExistencia != null) colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Carga de categorías en el ComboBox
        if (cmbCategoria != null) {
            try {
                cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listar()));
            } catch (SQLException e) {
                mensaje(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage());
            }
        }

        if (tblProductos != null) {
            tblProductos.setItems(productos);
        }

        if (chkActivo != null) {
            chkActivo.setSelected(true);
        }

        cargarProductos();
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
        if (txtCodigo == null || txtCodigo.getScene() == null) return;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            if (imgProducto != null) {
                imgProducto.setImage(new Image(rutaImagen));
            }
        }
    }

    @FXML
    private void editar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Selecciona un producto de la tabla.");
            return;
        }
        this.productoEdicion = seleccionado; // Guarda la referencia del producto a actualizar
        txtCodigo.setText(seleccionado.getCodigo());
        txtNombre.setText(seleccionado.getNombre());
        txtPrecioVenta.setText(seleccionado.getPrecioVenta() != null ? seleccionado.getPrecioVenta().toString() : "");
        txtExistencia.setText(String.valueOf(seleccionado.getExistencia()));
        chkActivo.setSelected(seleccionado.getActivo() != null && seleccionado.getActivo());

        if (seleccionado.getCategoria() != null) {
            cmbCategoria.setValue(seleccionado.getCategoria());
        }
    }

    @FXML
    private void guardar() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || txtPrecioVenta.getText().isBlank() || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete los campos obligatorios.");
            return;
        }
        try {
            BigDecimal precio = new BigDecimal(txtPrecioVenta.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "Precio mayor que cero y existencia no negativa.");
                return;
            }

            if (productoEdicion == null) {
                // MODO: INSERTAR NUEVO
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
                // MODO: ACTUALIZAR EXISTENTE
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
        if (tblProductos == null) return;
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

    @FXML
    private void cerrar() {
        if (txtCodigo != null && txtCodigo.getScene() != null) {
            ((Stage) txtCodigo.getScene().getWindow()).close();
        }
    }

    @FXML
    private void limpiar() {
        if (txtCodigo != null) txtCodigo.clear();
        if (txtNombre != null) txtNombre.clear();
        if (txtPrecioVenta != null) txtPrecioVenta.clear();
        if (txtExistencia != null) txtExistencia.clear();
        if (cmbCategoria != null) cmbCategoria.getSelectionModel().clearSelection();
        if (chkActivo != null) chkActivo.setSelected(true);
        if (imgProducto != null) imgProducto.setImage(null);
        rutaImagen = null;
        productoEdicion = null; // Reinicia el estado de edición
        if (tblProductos != null) tblProductos.getSelectionModel().clearSelection();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}