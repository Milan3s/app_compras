package controllers;

import app_compras.App;
import config.Logger;
import dao.GastosAnualesDAO;
import config.ConexionDB;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import models.GastosAnual;

public class GastosAnualesController implements Initializable {

    // ===================================================
    // ELEMENTOS FXML
    // ===================================================
    @FXML
    private TableView<GastosAnual> tablaGastosAnuales;
    @FXML
    private TableColumn<GastosAnual, String> colGastosUsuarioAnual;
    @FXML
    private TableColumn<GastosAnual, Integer> colGastosAnio;
    @FXML
    private TableColumn<GastosAnual, Double> colGastosTotalAnual;
    @FXML
    private TextField txtAnioGastosAnuales;
    @FXML
    private ComboBox<String> comboUsuariosGastosAnuales;
    @FXML
    private Button btnCrearVistaGastosAnuales;
    @FXML
    private Button btnCargarVistaGastosAnuales;
    @FXML
    private Button btnImprimirGastosAnuales1;
    @FXML
    private Button btnVolverDashboardGastosAnuales;
    @FXML
    private Button btnRefrescarGastosAnuales;
    @FXML
    private Label labelRegistrosGastosAnuales;
    @FXML
    private Label labelEstadoGastosAnuales;

    private final GastosAnualesDAO gastosAnualesDAO = new GastosAnualesDAO();

    @FXML
    private AnchorPane gastosAnualesRoot;
    @FXML
    private HBox headerGastosAnuales;
    @FXML
    private ImageView iconoGastosAnuales;
    @FXML
    private Label labelTituloGastosAnuales;
    @FXML
    private Separator separatorTopGastosAnuales;
    @FXML
    private Text textoDescripcionGastosAnuales;
    @FXML
    private HBox filtrosGastosAnuales;
    @FXML
    private Text labelFiltroAnioGastosAnuales;
    @FXML
    private Text labelFiltroUsuarioGastosAnuales;
    @FXML
    private Separator separatorMiddleGastosAnuales;
    @FXML
    private HBox accionesGastosAnuales;
    @FXML
    private Separator separatorMiddleGastosAnuales1;
    @FXML
    private HBox volverDashboardGastosAnuales;

    // ===================================================
    // INICIALIZACIÓN
    // ===================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarUsuarios();

        comboUsuariosGastosAnuales.setDisable(true);
        txtAnioGastosAnuales.setDisable(true);
        mostrarMensajeUsuario("Verificando si existe la vista SQL...", false);

        // 🔹 Detectar vista en la base de datos
        if (existeVistaEnBaseDeDatos("vista_gasto_anual")) {
            mostrarMensajeUsuario("Vista detectada en base de datos. Cargando datos...", true);
            cargarDatosVista();
        } else {
            mostrarMensajeUsuario("No se encontró la vista. Debes crearla antes de consultar.", false);
        }

        // Eventos
        btnCrearVistaGastosAnuales.setOnAction(e -> crearVistaManual());
        btnCargarVistaGastosAnuales.setOnAction(e -> cargarDatosVista());
        btnImprimirGastosAnuales1.setOnAction(e -> imprimirListado());
        comboUsuariosGastosAnuales.setOnAction(e -> aplicarFiltros());
        txtAnioGastosAnuales.setOnAction(e -> aplicarFiltros());
        btnRefrescarGastosAnuales.setOnAction(this::accion_RefrescarGastosAnuales);
        btnVolverDashboardGastosAnuales.setOnAction(this::accion_volver_al_dashboard);
    }

    // ===================================================
    // VERIFICAR EXISTENCIA DE VISTA
    // ===================================================
    private boolean existeVistaEnBaseDeDatos(String vistaNombre) {
        try (Connection conn = ConexionDB.getConexion()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, vistaNombre, new String[]{"VIEW"})) {
                return rs.next();
            }
        } catch (Exception e) {
            Logger.exception("Error verificando la existencia de la vista " + vistaNombre, e);
            return false;
        }
    }

    // ===================================================
    // CONFIGURAR TABLA
    // ===================================================
    private void configurarTabla() {
        colGastosUsuarioAnual.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsuario()));
        colGastosAnio.setCellValueFactory(c
                -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnio()).asObject());
        colGastosTotalAnual.setCellValueFactory(c
                -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getGastoTotal()).asObject());

        tablaGastosAnuales.setPlaceholder(new Label("No hay registros para mostrar."));
    }

    // ===================================================
    // CARGAR USUARIOS
    // ===================================================
    private void cargarUsuarios() {
        try {
            List<String> usuarios = gastosAnualesDAO.obtenerUsuarios();
            comboUsuariosGastosAnuales.setItems(FXCollections.observableArrayList(usuarios));
            comboUsuariosGastosAnuales.getItems().add(0, "Todos los usuarios");
            comboUsuariosGastosAnuales.getSelectionModel().selectFirst();
        } catch (Exception e) {
            Logger.exception("Error al cargar los usuarios en GastosAnuales.", e);
            mostrarMensajeUsuario("Error al cargar los usuarios: " + e.getMessage(), false);
        }
    }

    // ===================================================
    // CREAR VISTA MANUAL
    // ===================================================
    private void crearVistaManual() {
        boolean creada = gastosAnualesDAO.crearVistaGastoAnual();
        if (creada) {
            mostrarMensajeUsuario("Vista creada correctamente. Cargando datos...", true);
            comboUsuariosGastosAnuales.setDisable(false);
            txtAnioGastosAnuales.setDisable(false);
            cargarDatosVista();
        } else {
            mostrarMensajeUsuario("Error al crear la vista SQL. Verifica permisos o conexión.", false);
        }
    }

    // ===================================================
    // CARGAR DATOS DE LA VISTA
    // ===================================================
    private void cargarDatosVista() {
        List<GastosAnual> lista = gastosAnualesDAO.listarPorAnioYUsuario(0, null);
        tablaGastosAnuales.setItems(FXCollections.observableArrayList(lista));
        actualizarContador(lista.size());

        if (lista.isEmpty()) {
            mostrarMensajeUsuario("No hay registros disponibles o la vista está vacía.", false);
            comboUsuariosGastosAnuales.setDisable(true);
            txtAnioGastosAnuales.setDisable(true);
        } else {
            mostrarMensajeUsuario("Datos cargados correctamente desde la vista anual.", true);
            btnCrearVistaGastosAnuales.setDisable(true);
            btnCargarVistaGastosAnuales.setDisable(true);
            comboUsuariosGastosAnuales.setDisable(false);
            txtAnioGastosAnuales.setDisable(false);
        }
    }

    // ===================================================
    // FILTROS
    // ===================================================
    private void aplicarFiltros() {
        if (comboUsuariosGastosAnuales.isDisabled() || txtAnioGastosAnuales.isDisabled()) {
            mostrarMensajeUsuario("Primero debes crear o cargar la vista antes de filtrar.", false);
            return;
        }

        String textoAnio = txtAnioGastosAnuales.getText().trim();
        String usuario = comboUsuariosGastosAnuales.getSelectionModel().getSelectedItem();
        int anio = 0;

        if (!textoAnio.isEmpty()) {
            try {
                anio = Integer.parseInt(textoAnio);
                if (anio < 2000 || anio > LocalDate.now().getYear() + 1) {
                    mostrarMensajeUsuario("Introduce un año válido (2000-" + (LocalDate.now().getYear() + 1) + ")", false);
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarMensajeUsuario("Introduce un número de año válido (por ejemplo: 2025)", false);
                return;
            }
        }

        if ("Todos los usuarios".equals(usuario)) {
            usuario = null;
        }

        List<GastosAnual> lista = gastosAnualesDAO.listarPorAnioYUsuario(anio, usuario);
        tablaGastosAnuales.setItems(FXCollections.observableArrayList(lista));
        actualizarContador(lista.size());

        if (lista.isEmpty()) {
            mostrarMensajeUsuario("No se encontraron registros para los filtros aplicados.", false);
        } else {
            mostrarMensajeUsuario("Filtros aplicados correctamente.", true);
        }
    }

    // ===================================================
    // IMPRESIÓN (formato dd/MM/yyyy)
    // ===================================================
    private void imprimirListado() {
        List<GastosAnual> registros = tablaGastosAnuales.getItems();
        if (registros.isEmpty()) {
            mostrarMensajeUsuario("No hay registros para imprimir.", false);
            return;
        }

        String fechaFormateada = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        StringBuilder informe = new StringBuilder();
        informe.append("REPORTE DE GASTOS ANUALES\n");
        informe.append("Fecha de generación: ").append(fechaFormateada).append("\n\n");
        informe.append(String.format("%-25s %-10s %-15s\n", "Usuario", "Año", "Gasto Total (€)"));
        informe.append("--------------------------------------------------------\n");

        for (GastosAnual g : registros) {
            informe.append(String.format("%-25s %-10d %-15.2f\n",
                    g.getUsuario(), g.getAnio(), g.getGastoTotal()));
        }

        Label lblInforme = new Label(informe.toString());
        lblInforme.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        lblInforme.setWrapText(false);

        VBox contenedor = new VBox(lblInforme);
        contenedor.setPrefWidth(700);
        contenedor.setPrefHeight(Region.USE_COMPUTED_SIZE);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tablaGastosAnuales.getScene().getWindow())) {
            if (job.printPage(contenedor)) {
                job.endJob();
                mostrarMensajeUsuario("Informe impreso/exportado correctamente.", true);
            } else {
                mostrarMensajeUsuario("Error al imprimir/exportar el informe.", false);
            }
        }
    }

    // ===================================================
    // REFRESCAR DATOS
    // ===================================================
    @FXML
    private void accion_RefrescarGastosAnuales(ActionEvent event) {
        mostrarMensajeUsuario("Actualizando datos desde la vista SQL...", false);

        try {
            String textoAnio = txtAnioGastosAnuales.getText().trim();
            String usuario = comboUsuariosGastosAnuales.getSelectionModel().getSelectedItem();
            int anio = 0;

            if (!textoAnio.isEmpty()) {
                try {
                    anio = Integer.parseInt(textoAnio);
                } catch (NumberFormatException e) {
                    mostrarMensajeUsuario("Introduce un año válido para refrescar los datos.", false);
                    return;
                }
            }

            if ("Todos los usuarios".equals(usuario)) {
                usuario = null;
            }

            List<GastosAnual> lista = gastosAnualesDAO.listarPorAnioYUsuario(anio, usuario);
            tablaGastosAnuales.setItems(FXCollections.observableArrayList(lista));
            actualizarContador(lista.size());

            mostrarMensajeUsuario("Datos actualizados correctamente.", true);

        } catch (Exception e) {
            mostrarMensajeUsuario("Error al refrescar los datos.", false);
            Logger.exception("Error al refrescar datos desde vista_gasto_anual", e);
        }
    }

    // ===================================================
    // UTILIDADES
    // ===================================================
    private void actualizarContador(int total) {
        labelRegistrosGastosAnuales.setText("Registros cargados: " + total);
    }

    private void mostrarMensajeUsuario(String mensaje, boolean exito) {
        labelEstadoGastosAnuales.setText(mensaje);
        labelEstadoGastosAnuales.setTextFill(exito ? Color.web("#28a745") : Color.web("#dc3545"));
        Logger.info(mensaje);
    }

    // ===================================================
    // NAVEGACIÓN
    // ===================================================
    @FXML
    private void accion_volver_al_dashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista GastosAnuales.");
        } catch (IOException e) {
            mostrarMensajeUsuario("No se pudo volver al Dashboard.", false);
            Logger.exception("Error al volver al dashboard desde GastosAnualesController", e);
        }
    }
}
