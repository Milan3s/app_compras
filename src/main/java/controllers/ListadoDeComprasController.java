package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import dao.ListadoDeCompraDAO;
import dao.PermisoDAO;
import dao.RolDAO;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import models.ListadoCompra;
import models.Rol;
import models.Usuario;

public class ListadoDeComprasController implements Initializable {

    @FXML
    private AnchorPane listadoCompraRoot;
    @FXML
    private Label labelTituloListadoCompra;
    @FXML
    private Label labelUsuarioActivoListadoCompra;
    @FXML
    private TextField txtBuscarListadoCompra;
    @FXML
    private TableView<ListadoCompra> tablaListadoCompra;
    @FXML
    private TableColumn<ListadoCompra, Integer> colListadoCompraIdDetalle;
    @FXML
    private TableColumn<ListadoCompra, String> colListadoCompraNombreCompra;
    @FXML
    private TableColumn<ListadoCompra, String> colListadoCompraUsuario;
    @FXML
    private TableColumn<ListadoCompra, String> colListadoCompraProducto;
    @FXML
    private TableColumn<ListadoCompra, String> colListadoCompraTienda;
    @FXML
    private TableColumn<ListadoCompra, Integer> colListadoCompraCantidad;
    @FXML
    private TableColumn<ListadoCompra, Double> colListadoCompraPrecioUnitario;
    @FXML
    private TableColumn<ListadoCompra, Double> colListadoCompraSubtotal;
    @FXML
    private TableColumn<ListadoCompra, String> colListadoCompraFecha;
    @FXML
    private Label labelMensajeListadoCompra;
    @FXML
    private Button btnVolverListadoCompra;
    @FXML
    private Label labelSeleccionUsuarioListadoCompra;
    @FXML
    private ComboBox<String> comboUsuariosListadoCompra;
    @FXML
    private Label labelTotalGastadoListadoCompra;
    @FXML
    private Button btnImprimirListadoCompra;

    private final ListadoDeCompraDAO listadoCompraDAO = new ListadoDeCompraDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final PermisoDAO permisoDAO = new PermisoDAO();
    private final ObservableList<ListadoCompra> listaCompras = FXCollections.observableArrayList();
    private final ObservableList<String> listaUsuarios = FXCollections.observableArrayList();

    private Usuario usuarioActual;
    private Rol rolUsuario;
    private boolean puedeVerTodas = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        tablaListadoCompra.setEditable(true);
        tablaListadoCompra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        inicializarSegunRol();
        configurarBusqueda();
    }

    private void configurarColumnas() {
        colListadoCompraIdDetalle.setCellValueFactory(new PropertyValueFactory<>("idDetalle"));
        colListadoCompraNombreCompra.setCellValueFactory(new PropertyValueFactory<>("nombreCompra"));
        colListadoCompraUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colListadoCompraProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colListadoCompraTienda.setCellValueFactory(new PropertyValueFactory<>("tiendaNombre"));
        colListadoCompraCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colListadoCompraPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colListadoCompraSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colListadoCompraFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCompra"));

        colListadoCompraFecha.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    try {
                        Date fecha = new Date(java.sql.Timestamp.valueOf(item).getTime());
                        setText(formato.format(fecha));
                    } catch (Exception e) {
                        setText(item);
                    }
                }
            }
        });

        colListadoCompraCantidad.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colListadoCompraPrecioUnitario.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        colListadoCompraCantidad.setOnEditCommit(this::editarCantidad);
        colListadoCompraPrecioUnitario.setOnEditCommit(this::editarPrecio);
    }

    private void editarCantidad(CellEditEvent<ListadoCompra, Integer> event) {
        ListadoCompra compra = event.getRowValue();
        int nuevaCantidad = event.getNewValue();
        if (nuevaCantidad <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Valor inválido", "La cantidad debe ser mayor que 0.");
            tablaListadoCompra.refresh();
            return;
        }
        compra.setCantidad(nuevaCantidad);
        compra.setSubtotal(compra.getPrecioUnitario() * nuevaCantidad);
        listadoCompraDAO.actualizarDetalleCompra(compra);
        tablaListadoCompra.refresh();
    }

    private void editarPrecio(CellEditEvent<ListadoCompra, Double> event) {
        ListadoCompra compra = event.getRowValue();
        double nuevoPrecio = event.getNewValue();
        if (nuevoPrecio <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Valor inválido", "El precio debe ser mayor que 0.");
            tablaListadoCompra.refresh();
            return;
        }
        compra.setPrecioUnitario(nuevoPrecio);
        compra.setSubtotal(compra.getCantidad() * nuevoPrecio);
        listadoCompraDAO.actualizarDetalleCompra(compra);
        tablaListadoCompra.refresh();
    }

    private void inicializarSegunRol() {
        usuarioActual = Session.getUsuarioActual();
        if (usuarioActual == null) {
            labelUsuarioActivoListadoCompra.setText("Invitado");
            labelMensajeListadoCompra.setText("No hay sesión activa.");
            tablaListadoCompra.setDisable(true);
            txtBuscarListadoCompra.setDisable(true);
            comboUsuariosListadoCompra.setVisible(false);
            labelSeleccionUsuarioListadoCompra.setVisible(false);
            return;
        }

        rolUsuario = rolDAO.obtenerRolPorId(usuarioActual.getIdRol());
        labelUsuarioActivoListadoCompra.setText(usuarioActual.getNombre());
        puedeVerTodas = permisoDAO.tienePermiso(rolUsuario.getIdRol(), "permisoVerTodasLasCompras");

        // 🔹 Siempre cargamos todos los usuarios para el combo
        cargarUsuariosEnComboBox();

        // 🔹 Seleccionamos el usuario actual por defecto
        comboUsuariosListadoCompra.getSelectionModel().select(usuarioActual.getNombre());

        // 🔹 Mostramos solo sus compras inicialmente
        cargarComprasUsuario(usuarioActual.getIdUsuario());
        labelMensajeListadoCompra.setText("Mostrando tus compras personales.");

        // 🔹 Pero si tiene el permiso, añadimos también la opción 'Todos'
        if (puedeVerTodas) {
            if (!comboUsuariosListadoCompra.getItems().contains("Todos")) {
                comboUsuariosListadoCompra.getItems().add(0, "Todos");
            }
        }

        // 🔹 Dejamos el combo habilitado SIEMPRE (puede cambiar el filtro cuando quiera)
        comboUsuariosListadoCompra.setDisable(false);

        btnImprimirListadoCompra.setDisable(false);
    }

    private void cargarTodasLasCompras() {
        List<ListadoCompra> compras = listadoCompraDAO.listarDetallesCompras();
        compras.sort(Comparator.comparingInt(ListadoCompra::getIdDetalle));
        listaCompras.setAll(compras);
        tablaListadoCompra.setItems(listaCompras);
        actualizarTotal(listadoCompraDAO.obtenerTotalGeneral());
    }

    private void cargarComprasUsuario(int idUsuario) {
        List<ListadoCompra> compras = listadoCompraDAO.listarDetallesPorUsuario(idUsuario);
        compras.sort(Comparator.comparingInt(ListadoCompra::getIdDetalle));
        listaCompras.setAll(compras);
        tablaListadoCompra.setItems(listaCompras);
        actualizarTotal(listadoCompraDAO.obtenerTotalGastadoPorUsuario(idUsuario));
    }

    private void cargarUsuariosEnComboBox() {
        listaUsuarios.clear();
        listaUsuarios.add("Todos");
        List<Map<String, Object>> usuariosTotales = listadoCompraDAO.obtenerTotalesPorUsuario();
        for (Map<String, Object> fila : usuariosTotales) {
            listaUsuarios.add((String) fila.get("usuario"));
        }
        comboUsuariosListadoCompra.setItems(listaUsuarios);
    }

    @FXML
    private void filtrarPorUsuarioSeleccionado(ActionEvent event) {
        String seleccion = comboUsuariosListadoCompra.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            return;
        }

        if (seleccion.equals("Todos")) {
            cargarTodasLasCompras();
            labelMensajeListadoCompra.setText("Mostrando todas las compras del sistema.");
        } else {
            int idUsuario = listadoCompraDAO.obtenerIdUsuarioPorNombre(seleccion);
            if (idUsuario != -1) {
                cargarComprasUsuario(idUsuario);
                labelMensajeListadoCompra.setText("Compras de " + seleccion + ".");
            }
        }
    }

    private void configurarBusqueda() {
        txtBuscarListadoCompra.textProperty().addListener((obs, oldValue, newValue) -> filtrarDetalles(newValue));
    }

    private void filtrarDetalles(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            tablaListadoCompra.setItems(listaCompras);
            return;
        }
        String texto = filtro.toLowerCase();
        ObservableList<ListadoCompra> filtrados = listaCompras.filtered(d
                -> (d.getNombreCompra() != null && d.getNombreCompra().toLowerCase().contains(texto))
                || (d.getUsuarioNombre() != null && d.getUsuarioNombre().toLowerCase().contains(texto))
                || (d.getProductoNombre() != null && d.getProductoNombre().toLowerCase().contains(texto))
                || (d.getTiendaNombre() != null && d.getTiendaNombre().toLowerCase().contains(texto))
                || (d.getFechaCompra() != null && d.getFechaCompra().toLowerCase().contains(texto))
        );
        tablaListadoCompra.setItems(filtrados);
    }

    private void actualizarTotal(double total) {
        labelTotalGastadoListadoCompra.setText(String.format("Total: %.2f €", total));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al Dashboard desde Listado de Compras.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo volver al Dashboard.");
        }
    }

    // ✅ Exportar TODAS las compras completas a PDF (sin recortes)
    @FXML
    private void imprimirListado(ActionEvent event) {
        String seleccion = comboUsuariosListadoCompra.getSelectionModel().getSelectedItem();
        int idFiltro = (seleccion == null || seleccion.equals("Todos"))
                ? -1
                : listadoCompraDAO.obtenerIdUsuarioPorNombre(seleccion);

        Map<String, List<ListadoCompra>> comprasPorUsuario = listadoCompraDAO.listarComprasAgrupadasPorUsuario(idFiltro);

        if (comprasPorUsuario.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", "No hay registros para imprimir.");
            return;
        }

        String usuarioGenerador = Session.getUsuarioActual() != null
                ? Session.getUsuarioActual().getNombre()
                : "Invitado";

        // 🔹 Formato de fecha legible (dd/MM/yyyy)
        String fechaActual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null || !job.showPrintDialog(listadoCompraRoot.getScene().getWindow())) {
            Logger.info("Impresión cancelada por el usuario.");
            return;
        }

        int totalUsuarios = comprasPorUsuario.size();
        int numPagina = 1;

        Map<String, Double> totalesUsuarios = new LinkedHashMap<>();
        double totalGeneral = 0.0;

        for (Map.Entry<String, List<ListadoCompra>> entry : comprasPorUsuario.entrySet()) {
            String usuario = entry.getKey();
            List<ListadoCompra> compras = entry.getValue();

            VBox pagina = new VBox(5);
            pagina.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11px; -fx-padding: 30;");

            Label encabezado = new Label("*********** LISTADO DE COMPRAS ***********\n"
                    + "Generado por: " + usuarioGenerador + "\n"
                    + "Usuario actual: " + usuario + "\n"
                    + "Fecha: " + fechaActual + "\n"
                    + "==========================================");
            encabezado.setStyle("-fx-font-weight: bold;");
            encabezado.setWrapText(true);
            encabezado.setMaxWidth(580);
            pagina.getChildren().add(encabezado);

            double totalUsuario = 0.0;

            // 🔹 Agrupar productos por nombre de compra (para no repetir títulos)
            Map<String, List<ListadoCompra>> agrupadasPorCompra = new LinkedHashMap<>();
            for (ListadoCompra lc : compras) {
                String clave = lc.getNombreCompra() + " (" + lc.getTiendaNombre() + ")";
                agrupadasPorCompra.computeIfAbsent(clave, k -> new ArrayList<>()).add(lc);
            }

            // 🔹 Imprimir cada bloque agrupado
            for (Map.Entry<String, List<ListadoCompra>> compraEntry : agrupadasPorCompra.entrySet()) {
                String nombreCompra = compraEntry.getKey();
                List<ListadoCompra> productos = compraEntry.getValue();

                Label lblTitulo = new Label("Nombre de la compra: " + nombreCompra);
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #000;");
                pagina.getChildren().add(lblTitulo);

                for (ListadoCompra p : productos) {
                    Label lblDetalle = new Label(String.format("   - %-25s %.2f€ x%d = %.2f€",
                            p.getProductoNombre(),
                            p.getPrecioUnitario(),
                            p.getCantidad(),
                            p.getSubtotal()));
                    pagina.getChildren().add(lblDetalle);
                    totalUsuario += p.getSubtotal();
                }
                pagina.getChildren().add(new Label(""));
            }

            totalesUsuarios.put(usuario, totalUsuario);
            totalGeneral += totalUsuario;

            Label totalLbl = new Label(String.format("\nTOTAL USUARIO %s: %.2f €", usuario, totalUsuario));
            totalLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            pagina.getChildren().addAll(new Label("------------------------------------------"), totalLbl);

            Label piePagina = new Label(String.format("\n--- Página %d de %d ---", numPagina, totalUsuarios + 1));
            piePagina.setStyle("-fx-font-size: 10px; -fx-text-alignment: center;");
            pagina.getChildren().add(piePagina);

            boolean ok = job.printPage(pagina);
            if (!ok) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de impresión", "Hubo un problema al imprimir la página de " + usuario);
                return;
            }

            numPagina++;
        }

        // 🔹 Página final de resumen general
        VBox resumen = new VBox(5);
        resumen.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11px; -fx-padding: 40;");

        Label tituloResumen = new Label("*********** RESUMEN GENERAL ***********");
        tituloResumen.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        resumen.getChildren().add(tituloResumen);

        resumen.getChildren().add(new Label("Fecha: " + fechaActual));
        resumen.getChildren().add(new Label("Generado por: " + usuarioGenerador));
        resumen.getChildren().add(new Label("=========================================="));

        for (Map.Entry<String, Double> t : totalesUsuarios.entrySet()) {
            resumen.getChildren().add(new Label(String.format("Total %-10s : %.2f €", t.getKey(), t.getValue())));
        }

        resumen.getChildren().add(new Label("------------------------------------------"));
        Label totalLblFinal = new Label(String.format("TOTAL GENERAL DEL SISTEMA: %.2f €", totalGeneral));
        totalLblFinal.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        resumen.getChildren().add(totalLblFinal);

        resumen.getChildren().add(new Label("******************************************"));
        resumen.getChildren().add(new Label(String.format("\n--- Página %d de %d ---", numPagina, totalUsuarios + 1)));

        job.printPage(resumen);
        job.endJob();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Impresión completada",
                "Se imprimieron todas las páginas correctamente, incluido el resumen final.");
        Logger.info("Impresión finalizada. Total páginas: " + (totalUsuarios + 1));
    }

    /**
     * Divide un texto largo en páginas, calculando líneas aproximadas por hoja.
     */
    private List<String> dividirTextoPorAltura(String texto, int lineasPorPagina) {
        List<String> paginas = new ArrayList<>();
        String[] lineas = texto.split("\n");
        StringBuilder pagina = new StringBuilder();
        int contador = 0;

        for (String linea : lineas) {
            pagina.append(linea).append("\n");
            contador++;
            if (contador >= lineasPorPagina) {
                paginas.add(pagina.toString());
                pagina.setLength(0);
                contador = 0;
            }
        }
        if (pagina.length() > 0) {
            paginas.add(pagina.toString());
        }

        return paginas;
    }

}
