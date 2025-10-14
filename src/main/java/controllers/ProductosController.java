package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import dao.CategoriaDAO;
import dao.ProductoDAO;
import dao.TiendaDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Categoria;
import models.Producto;
import models.Tienda;
import models.Usuario;

public class ProductosController implements Initializable {

    @FXML
    private Label labelUsuarioActivo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private TextField txtPrecio;
    @FXML
    private ComboBox<Tienda> comboTiendas;
    @FXML
    private ComboBox<Categoria> comboCategorias;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, Integer> colId;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colDescripcion;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, String> colTienda;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private Button btnVolver;
    @FXML
    private Label labelMensaje;
    @FXML
    private TextField txtbuscarProducto;
    @FXML
    private AnchorPane productosRoot;
    @FXML
    private Label labelTituloProductos;
    @FXML
    private Label labelNombre;
    @FXML
    private Label labelDescripcion;
    @FXML
    private Label labelPrecio;
    @FXML
    private Label labelTienda;
    @FXML
    private Label labelCategoria;
    @FXML
    private HBox hboxBotonesCRUD;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final TiendaDAO tiendaDAO = new TiendaDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ObservableList<Tienda> listaTiendas = FXCollections.observableArrayList();
    private ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();

    private Producto productoSeleccionado = null;
    @FXML
    private VBox headerProductos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarTiendas();
        cargarCategorias();
        cargarProductos();
        configurarEventosTabla();

        // Búsqueda en tiempo real
        txtbuscarProducto.textProperty().addListener((observable, oldValue, newValue) -> filtrarProductos(newValue));

        Usuario usuario = Session.getUsuarioActual();
        if (usuario != null) {
            labelUsuarioActivo.setText(usuario.getNombre());
            Logger.info("Sesión activa en vista Productos: " + usuario.getNombre());
        } else {
            labelUsuarioActivo.setText("Invitado");
            labelMensaje.setText("No hay sesión activa. Algunas funciones pueden estar limitadas.");
            labelMensaje.setStyle("-fx-text-fill: orange;");
            Logger.warning("Vista Productos abierta sin sesión activa.");
        }
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colTienda.setCellValueFactory(new PropertyValueFactory<>("nombreTienda"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));

        colPrecio.setStyle("-fx-alignment: CENTER-RIGHT;");
        colId.setStyle("-fx-alignment: CENTER;");
        colCategoria.setStyle("-fx-alignment: CENTER;");
    }

    private void cargarTiendas() {
        List<Tienda> tiendas = tiendaDAO.listarTodas();
        listaTiendas.setAll(tiendas);
        comboTiendas.setItems(listaTiendas);

        comboTiendas.setPromptText("Selecciona una tienda...");

        comboTiendas.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Tienda t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.getNombre());
            }
        });

        comboTiendas.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tienda t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? comboTiendas.getPromptText() : t.getNombre());
            }
        });
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.listarTodas();
        listaCategorias.setAll(categorias);
        comboCategorias.setItems(listaCategorias);

        comboCategorias.setPromptText("Selecciona una categoría...");

        comboCategorias.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Categoria c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNombre());
            }
        });

        comboCategorias.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categoria c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? comboCategorias.getPromptText() : c.getNombre());
            }
        });
    }

    private void cargarProductos() {
        List<Producto> productos = productoDAO.listarTodos();
        listaProductos.setAll(productos);
        tablaProductos.setItems(listaProductos);

        if (listaProductos.isEmpty()) {
            labelMensaje.setText("No hay datos en la tabla.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
        } else {
            labelMensaje.setText("Datos cargados correctamente (" + productos.size() + " producto"
                    + (productos.size() > 1 ? "s" : "") + ").");
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }

    private void configurarEventosTabla() {
        tablaProductos.setOnMouseClicked((MouseEvent event) -> {
            if (tablaProductos.getSelectionModel().getSelectedItem() != null) {
                productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

                txtNombre.setText(productoSeleccionado.getNombre());
                txtDescripcion.setText(productoSeleccionado.getDescripcion());
                txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecio()));

                listaTiendas.stream()
                        .filter(t -> t.getIdTienda() == productoSeleccionado.getIdTienda())
                        .findFirst()
                        .ifPresent(comboTiendas::setValue);

                listaCategorias.stream()
                        .filter(c -> c.getIdCategoria() == productoSeleccionado.getIdCategoria())
                        .findFirst()
                        .ifPresent(comboCategorias::setValue);

                labelMensaje.setText("Producto seleccionado: " + productoSeleccionado.getNombre());
                labelMensaje.setStyle("-fx-text-fill: #2c3e50;");
            }
        });
    }

    @FXML
    private void agregarProducto(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String precioTxt = txtPrecio.getText().trim();
        Tienda tienda = comboTiendas.getValue();
        Categoria categoria = comboCategorias.getValue();

        if (nombre.isEmpty() || descripcion.isEmpty() || precioTxt.isEmpty() || tienda == null || categoria == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos",
                    "Debe llenar todos los campos, y seleccionar una tienda y categoría.");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioTxt);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato inválido", "El precio debe ser un número válido.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar creación");
        confirm.setHeaderText("¿Deseas agregar este producto?");
        confirm.setContentText("Nombre: " + nombre + "\nPrecio: " + precio);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                Producto nuevo = new Producto();
                nuevo.setNombre(nombre);
                nuevo.setDescripcion(descripcion);
                nuevo.setPrecio(precio);
                nuevo.setIdTienda(tienda.getIdTienda());
                nuevo.setIdCategoria(categoria.getIdCategoria());

                boolean insertado = productoDAO.insertar(nuevo);
                if (insertado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Producto agregado", "Producto agregado correctamente.");
                    limpiarFormulario(null);
                    cargarProductos();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agregar el producto.");
                }
            }
        });
    }

    @FXML
    private void actualizarProducto(ActionEvent event) {
        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Seleccione un producto para actualizar.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String precioTxt = txtPrecio.getText().trim();
        Tienda tienda = comboTiendas.getValue();
        Categoria categoria = comboCategorias.getValue();

        if (nombre.isEmpty() || descripcion.isEmpty() || precioTxt.isEmpty() || tienda == null || categoria == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos",
                    "Debe llenar todos los campos, y seleccionar una tienda y categoría.");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioTxt);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato inválido", "El precio debe ser un número válido.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar actualización");
        confirm.setHeaderText("¿Deseas actualizar el producto seleccionado?");
        confirm.setContentText("Nuevo nombre: " + nombre + "\nNuevo precio: " + precio);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                productoSeleccionado.setNombre(nombre);
                productoSeleccionado.setDescripcion(descripcion);
                productoSeleccionado.setPrecio(precio);
                productoSeleccionado.setIdTienda(tienda.getIdTienda());
                productoSeleccionado.setIdCategoria(categoria.getIdCategoria());

                boolean actualizado = productoDAO.actualizar(productoSeleccionado);
                if (actualizado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado", "Producto actualizado correctamente.");
                    limpiarFormulario(null);
                    cargarProductos();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el producto.");
                }
            }
        });
    }

    @FXML
    private void eliminarProducto(ActionEvent event) {
        if (productoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Seleccione un producto para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Deseas eliminar este producto?");
        confirm.setContentText("Producto: " + productoSeleccionado.getNombre());
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean eliminado = productoDAO.eliminar(productoSeleccionado.getIdProducto());
                if (eliminado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Producto eliminado correctamente.");
                    limpiarFormulario(null);
                    cargarProductos();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el producto.");
                }
            }
        });
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        comboTiendas.getSelectionModel().clearSelection(); // mantiene prompt
        comboCategorias.getSelectionModel().clearSelection(); // mantiene prompt
        productoSeleccionado = null;
        tablaProductos.getSelectionModel().clearSelection();
        labelMensaje.setText("Formulario limpio.");
        labelMensaje.setStyle("-fx-text-fill: gray;");
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista Productos.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo volver al Dashboard.");
            Logger.exception("Error al volver al dashboard desde ProductosController", e);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            tablaProductos.setItems(listaProductos);
            labelMensaje.setText("Mostrando todos los productos.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
            return;
        }

        String texto = filtro.toLowerCase();

        ObservableList<Producto> filtrados = listaProductos.filtered(p
                -> (p.getNombre() != null && p.getNombre().toLowerCase().contains(texto))
                || (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(texto))
                || (p.getNombreCategoria() != null && p.getNombreCategoria().toLowerCase().contains(texto))
                || (p.getNombreTienda() != null && p.getNombreTienda().toLowerCase().contains(texto))
                || String.valueOf(p.getPrecio()).contains(texto)
        );

        tablaProductos.setItems(filtrados);

        if (filtrados.isEmpty()) {
            labelMensaje.setText("No se encontraron productos para: \"" + filtro + "\"");
            labelMensaje.setStyle("-fx-text-fill: red;");
        } else {
            labelMensaje.setText("Coincidencias encontradas: " + filtrados.size());
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }
}
