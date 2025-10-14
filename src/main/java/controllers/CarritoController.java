package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import config.ConexionDB;
import dao.CarritoDAO;
import dao.CompraDAO;
import dao.ProductoDAO;
import dao.TiendaDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Carrito;
import models.Producto;
import models.Tienda;
import models.Usuario;

/**
 * Controlador del carrito de compras. Desactiva todos los elementos (incluyendo
 * tienda y cantidad) hasta que se guarde el nombre de la compra.
 */
public class CarritoController implements Initializable {

    // --- Componentes UI ---
    @FXML
    private TableView<Carrito> tablaCarrito;
    @FXML
    private TableColumn<Carrito, String> colNombreCompra;
    @FXML
    private TableColumn<Carrito, String> colProducto;
    @FXML
    private TableColumn<Carrito, Double> colPrecio;
    @FXML
    private TableColumn<Carrito, Integer> colCantidad;
    @FXML
    private TableColumn<Carrito, Double> colSubtotal;
    @FXML
    private TableColumn<Carrito, String> colFecha;

    @FXML
    private Label labelTotal, labelMensaje, labelUsuarioActivo;
    @FXML
    private ComboBox<Tienda> comboTiendas;
    @FXML
    private ComboBox<Producto> comboProductos;
    @FXML
    private Spinner<Integer> spinnerCantidad;
    @FXML
    private TextField txtBuscarProducto, txtNombreCompra;
    @FXML
    private Button btnAgregar, btnQuitar, btnVaciar, btnConfirmar1,
            btnGuardarCompraTemporalmente, btnImprimirTicketDeCompra,
            btnVolver, btnGuardarNombreCompra;

    // --- DAOs ---
    private final CarritoDAO carritoDAO = new CarritoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CompraDAO compraDAO = new CompraDAO();
    private final TiendaDAO tiendaDAO = new TiendaDAO();

    // --- Datos dinámicos ---
    private ObservableList<Carrito> listaCarrito;
    private ObservableList<Producto> listaProductos;
    private ObservableList<Tienda> listaTiendas;
    private Tienda tiendaSeleccionada;
    private String nombreCompraActual = null;
    @FXML
    private AnchorPane carritoRoot;
    @FXML
    private VBox headerCarrito;
    @FXML
    private Label labelTituloCarrito;
    @FXML
    private HBox hboxNombreCompra;
    @FXML
    private Label labelNombreCompra;
    @FXML
    private HBox hboxTienda;
    @FXML
    private Label labelTienda;
    @FXML
    private HBox hboxProducto;
    @FXML
    private Label labelProducto;
    @FXML
    private Label labelCantidad;
    @FXML
    private HBox hboxTotal;
    @FXML
    private Label labelTotalTexto;
    @FXML
    private VBox vboxBotones;
    @FXML
    private HBox hboxBotonesFila1;
    @FXML
    private HBox hboxBotonesFila2;

    // ----------------------------------------------------------
    // INICIALIZACIÓN
    // ----------------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        inicializarCombos();
        configurarSpinnerCantidad();
        cargarTiendas();
        mostrarUsuarioActivo();
        cargarCarritoTemporal();
        txtBuscarProducto.textProperty().addListener((obs, o, n) -> filtrarProductos(n));

        // Al inicio, todo desactivado hasta guardar nombre
        desactivarBotonesIniciales();
    }

    private void mostrarUsuarioActivo() {
        Usuario usuario = Session.getUsuarioActual();
        if (usuario != null) {
            labelUsuarioActivo.setText(usuario.getNombre());
        } else {
            labelUsuarioActivo.setText("Invitado");
            labelMensaje.setText("No hay sesión activa.");
            labelMensaje.setStyle("-fx-text-fill: orange;");
        }
    }

    private void configurarColumnas() {
        colNombreCompra.setCellValueFactory(new PropertyValueFactory<>("nombreCompra"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaAgregadoFormatted"));
    }

    private void inicializarCombos() {
        comboTiendas.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tienda t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "Selecciona una tienda..." : t.getNombre());
            }
        });
        comboTiendas.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Tienda t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.getNombre());
            }
        });
        comboProductos.setDisable(true);
        comboTiendas.setDisable(true);
        btnAgregar.setDisable(true);
    }

    private void configurarSpinnerCantidad() {
        SpinnerValueFactory<Integer> vf
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0, 1);
        spinnerCantidad.setValueFactory(vf);
        spinnerCantidad.setDisable(true);
    }

    // ----------------------------------------------------------
    // MÉTODOS DE CARGA
    // ----------------------------------------------------------
    private void cargarTiendas() {
        try {
            listaTiendas = FXCollections.observableArrayList(tiendaDAO.listarTodas());
            comboTiendas.setItems(listaTiendas);
        } catch (Exception e) {
            Logger.exception("Error al cargar tiendas", e);
        }
    }

    private void cargarCarrito() {
        int idUsuario = Session.getUsuarioId();
        if (idUsuario <= 0) {
            return;
        }

        if (nombreCompraActual == null || nombreCompraActual.isBlank()) {
            tablaCarrito.getItems().clear();
            labelTotal.setText("0,00 €");
            return;
        }

        List<Carrito> lista = carritoDAO.listarPorUsuarioYNombre(idUsuario, nombreCompraActual);
        listaCarrito = FXCollections.observableArrayList(lista);
        tablaCarrito.setItems(listaCarrito);

        double total = listaCarrito.stream().mapToDouble(Carrito::getSubtotal).sum();
        labelTotal.setText(String.format("%.2f €", total));
        actualizarEstadoBotones();
    }

    @FXML
    private void tiendaSeleccionada(ActionEvent event) {
        tiendaSeleccionada = comboTiendas.getValue();
        if (tiendaSeleccionada == null) {
            comboProductos.getItems().clear();
            comboProductos.setDisable(true);
            btnAgregar.setDisable(true);
            return;
        }

        List<Producto> productos = productoDAO.listarPorTienda(tiendaSeleccionada.getIdTienda());
        if (productos.isEmpty()) {
            comboProductos.getItems().clear();
            comboProductos.setDisable(true);
            btnAgregar.setDisable(true);
        } else {
            listaProductos = FXCollections.observableArrayList(productos);
            comboProductos.setItems(listaProductos);
            comboProductos.setDisable(false);
            btnAgregar.setDisable(false);
        }
    }

    // ----------------------------------------------------------
    // MÉTODOS DE ACCIÓN
    // ----------------------------------------------------------
    @FXML
    private void guardarNombreCompra(ActionEvent event) {
        String nombre = txtNombreCompra.getText().trim();
        if (nombre.isEmpty()) {
            labelMensaje.setText("Debes ingresar un nombre para la compra.");
            labelMensaje.setStyle("-fx-text-fill:red;");
            return;
        }

        this.nombreCompraActual = nombre;
        labelMensaje.setText("Nombre de compra guardado: " + nombre);
        labelMensaje.setStyle("-fx-text-fill:green;");
        activarBotones();
        cargarCarrito();
    }

    @FXML
    private void agregarProducto(ActionEvent event) {
        if (nombreCompraActual == null) {
            labelMensaje.setText("Primero debes guardar el nombre de la compra.");
            labelMensaje.setStyle("-fx-text-fill:red;");
            return;
        }

        Producto p = comboProductos.getValue();
        Integer cant = spinnerCantidad.getValue();
        int idUsuario = Session.getUsuarioId();

        if (idUsuario <= 0 || p == null || cant == null || cant <= 0) {
            labelMensaje.setText("Datos inválidos para agregar.");
            labelMensaje.setStyle("-fx-text-fill:red;");
            return;
        }

        Carrito nuevo = new Carrito(idUsuario, p.getIdProducto(), cant);
        nuevo.setNombreCompra(nombreCompraActual);

        if (carritoDAO.agregarAlCarrito(nuevo)) {
            labelMensaje.setText("Producto añadido: " + p.getNombre());
            labelMensaje.setStyle("-fx-text-fill:green;");
            cargarCarrito();
            spinnerCantidad.getValueFactory().setValue(0);
        }
    }

    @FXML
    private void quitarProductoSeleccionado(ActionEvent event) {
        Carrito c = tablaCarrito.getSelectionModel().getSelectedItem();
        if (c == null) {
            labelMensaje.setText("Selecciona un producto para quitar.");
            labelMensaje.setStyle("-fx-text-fill:orange;");
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar producto del carrito?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText("Producto: " + c.getNombreProducto());
        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (carritoDAO.eliminarProducto(c.getIdCarrito())) {
                cargarCarrito();
                labelMensaje.setText("Producto eliminado.");
                labelMensaje.setStyle("-fx-text-fill:green;");
            }
        }
    }

    @FXML
    private void vaciarCarrito(ActionEvent event) {
        int idUsuario = Session.getUsuarioId();
        if (idUsuario <= 0) {
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Vaciar carrito actual?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK && nombreCompraActual != null) {
            try (Connection conn = ConexionDB.getConexion(); PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM carrito WHERE id_usuario = ? AND nombre_compra = ?")) {
                stmt.setInt(1, idUsuario);
                stmt.setString(2, nombreCompraActual);
                stmt.executeUpdate();
            } catch (Exception e) {
                Logger.exception("Error al vaciar carrito actual", e);
            }
            cargarCarrito();
            labelMensaje.setText("Carrito '" + nombreCompraActual + "' vaciado.");
            labelMensaje.setStyle("-fx-text-fill:green;");
        }
    }

    @FXML
    private void confirmarCompra(ActionEvent event) {
        int idUsuario = Session.getUsuarioId();
        if (idUsuario <= 0 || listaCarrito == null || listaCarrito.isEmpty()) {
            return;
        }

        if (nombreCompraActual == null) {
            labelMensaje.setText("Primero debes guardar el nombre de la compra.");
            labelMensaje.setStyle("-fx-text-fill:red;");
            return;
        }

        if (tiendaSeleccionada == null) {
            labelMensaje.setText("Debes seleccionar una tienda antes de confirmar.");
            labelMensaje.setStyle("-fx-text-fill:red;");
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Confirmar compra '" + nombreCompraActual + "'?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            boolean exito = compraDAO.realizarCompraDesdeCarrito(
                    idUsuario,
                    tiendaSeleccionada.getIdTienda(),
                    nombreCompraActual
            );

            if (exito) {
                cargarCarrito();
                labelMensaje.setText("Compra confirmada: " + nombreCompraActual);
                labelMensaje.setStyle("-fx-text-fill:green;");
                nombreCompraActual = null;
                txtNombreCompra.clear();
                desactivarBotonesIniciales();
            } else {
                labelMensaje.setText("Error al confirmar la compra.");
                labelMensaje.setStyle("-fx-text-fill:red;");
            }
        }
    }

    // ----------------------------------------------------------
    // FUNCIONALIDADES ADICIONALES
    // ----------------------------------------------------------
    @FXML
    private void accion_guardar_compra_temporalmente(ActionEvent event) {
        try {
            int idUsuario = Session.getUsuarioId();
            if (idUsuario <= 0) {
                labelMensaje.setText("Debes iniciar sesión para guardar.");
                labelMensaje.setStyle("-fx-text-fill:red;");
                return;
            }
            if (listaCarrito == null || listaCarrito.isEmpty()) {
                labelMensaje.setText("No hay productos en el carrito.");
                labelMensaje.setStyle("-fx-text-fill:orange;");
                return;
            }

            var carpeta = java.nio.file.Paths.get("data", "compras_temporales");
            if (!java.nio.file.Files.exists(carpeta)) {
                java.nio.file.Files.createDirectories(carpeta);
            }
            var archivo = carpeta.resolve("usuario_" + idUsuario + ".tmp");

            var sb = new StringBuilder();
            sb.append("USUARIO: ").append(idUsuario).append("\n");
            sb.append("FECHA: ").append(LocalDate.now()).append("\n");
            sb.append("NOMBRE COMPRA: ").append(nombreCompraActual != null ? nombreCompraActual : "Sin nombre").append("\n");
            sb.append("TIENDA: ").append(tiendaSeleccionada != null
                    ? tiendaSeleccionada.getNombre() : "Sin tienda").append("\n");
            sb.append("----------------------------------------------------\n");
            for (Carrito c : listaCarrito) {
                sb.append(c.getNombreProducto()).append("|")
                        .append(c.getPrecioProducto()).append("|")
                        .append(c.getCantidad()).append("|")
                        .append(c.getSubtotal()).append("|")
                        .append(c.getFechaAgregadoFormatted() != null
                                ? c.getFechaAgregadoFormatted() : LocalDate.now())
                        .append("\n");
            }
            java.nio.file.Files.writeString(archivo, sb.toString());

            labelMensaje.setText("Compra guardada temporalmente.");
            labelMensaje.setStyle("-fx-text-fill:green;");
            Logger.info("Compra temporal guardada: " + archivo);

        } catch (Exception e) {
            Logger.exception("Error al guardar compra temporal.", e);
            labelMensaje.setText("Error al guardar.");
            labelMensaje.setStyle("-fx-text-fill:red;");
        }
    }

    @FXML
    private void accion_imprimir_ticket_de_compra(ActionEvent event) {
        if (listaCarrito == null || listaCarrito.isEmpty()) {
            return;
        }

        double total = listaCarrito.stream().mapToDouble(Carrito::getSubtotal).sum();
        StringBuilder t = new StringBuilder();
        t.append("*********** TICKET DE COMPRA ***********\n")
                .append("Compra: ").append(nombreCompraActual != null ? nombreCompraActual : "Sin nombre").append("\n")
                .append("Fecha: ").append(LocalDate.now()).append("\n")
                .append("Usuario: ").append(labelUsuarioActivo.getText()).append("\n")
                .append("---------------------------------------\n");
        for (Carrito c : listaCarrito) {
            t.append(String.format("%-20s %6.2f x%d = %.2f\n",
                    c.getNombreProducto(), c.getPrecioProducto(), c.getCantidad(), c.getSubtotal()));
        }
        t.append("---------------------------------------\nTOTAL: ")
                .append(String.format("%.2f €", total))
                .append("\n****************************************\n");

        Label lbl = new Label(t.toString());
        lbl.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tablaCarrito.getScene().getWindow())) {
            if (job.printPage(lbl)) {
                job.endJob();
            }
        }
    }

    // ----------------------------------------------------------
    // MÉTODOS UTILITARIOS
    // ----------------------------------------------------------
    private void desactivarBotonesIniciales() {
        btnAgregar.setDisable(true);
        btnQuitar.setDisable(true);
        btnVaciar.setDisable(true);
        btnConfirmar1.setDisable(true);
        btnGuardarCompraTemporalmente.setDisable(true);
        btnImprimirTicketDeCompra.setDisable(true);
        comboProductos.setDisable(true);
        comboTiendas.setDisable(true);
        spinnerCantidad.setDisable(true);
    }

    private void activarBotones() {
        btnAgregar.setDisable(false);
        btnQuitar.setDisable(false);
        btnVaciar.setDisable(false);
        btnConfirmar1.setDisable(false);
        btnGuardarCompraTemporalmente.setDisable(false);
        btnImprimirTicketDeCompra.setDisable(false);
        comboProductos.setDisable(false);
        comboTiendas.setDisable(false);
        spinnerCantidad.setDisable(false);
    }

    private void filtrarProductos(String f) {
        if (f == null || f.isBlank()) {
            tablaCarrito.setItems(listaCarrito);
            return;
        }
        String filtro = f.toLowerCase();
        ObservableList<Carrito> filtrados = listaCarrito.filtered(c
                -> c.getNombreProducto().toLowerCase().contains(filtro)
                || c.getFechaAgregadoFormatted().toLowerCase().contains(filtro)
                || (c.getNombreCompra() != null && c.getNombreCompra().toLowerCase().contains(filtro)));
        tablaCarrito.setItems(filtrados);
    }

    private void cargarCarritoTemporal() {
        try {
            int idUsuario = Session.getUsuarioId();
            var archivo = java.nio.file.Paths.get("data", "compras_temporales",
                    "usuario_" + idUsuario + ".tmp");
            if (!java.nio.file.Files.exists(archivo)) {
                return;
            }

            var lineas = java.nio.file.Files.readAllLines(archivo);
            ObservableList<Carrito> temp = FXCollections.observableArrayList();
            for (String l : lineas) {
                if (l.startsWith("USUARIO") || l.startsWith("FECHA")
                        || l.startsWith("TIENDA") || l.startsWith("-") || l.isBlank()) {
                    continue;
                }
                String[] p = l.split("\\|");
                if (p.length >= 5) {
                    Carrito c = new Carrito();
                    c.setNombreProducto(p[0]);
                    c.setPrecioProducto(Double.parseDouble(p[1]));
                    c.setCantidad(Integer.parseInt(p[2]));
                    c.setSubtotal(Double.parseDouble(p[3]));
                    temp.add(c);
                }
            }
            if (!temp.isEmpty()) {
                listaCarrito = temp;
                tablaCarrito.setItems(listaCarrito);
                labelTotal.setText(String.format("%.2f €",
                        listaCarrito.stream().mapToDouble(Carrito::getSubtotal).sum()));
                labelMensaje.setText("Compra temporal cargada correctamente.");
                labelMensaje.setStyle("-fx-text-fill:green;");
            }
            actualizarEstadoBotones();
        } catch (Exception e) {
            Logger.exception("Error al cargar compra temporal.", e);
        }
    }

    private void actualizarEstadoBotones() {
        boolean tieneProductos = listaCarrito != null && !listaCarrito.isEmpty();
        btnGuardarCompraTemporalmente.setDisable(!tieneProductos);
        btnImprimirTicketDeCompra.setDisable(!tieneProductos);
        btnVaciar.setDisable(!tieneProductos);
        btnQuitar.setDisable(!tieneProductos);
        btnConfirmar1.setDisable(!tieneProductos);
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
        } catch (IOException e) {
            Logger.exception("Error al volver al Dashboard", e);
        }
    }
}
