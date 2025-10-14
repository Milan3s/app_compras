package controllers;

import app_compras.App;
import config.Logger;
import dao.GastoSemanalDAO;
import config.ConexionDB;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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
import models.GastoSemanal;

public class GastosPorSemanaController implements Initializable {

    // =====================================================
    // ELEMENTOS FXML
    // =====================================================
    @FXML
    private TableView<GastoSemanal> tablaGastosSemana;
    @FXML
    private TableColumn<GastoSemanal, String> colGastosUsuario;
    @FXML
    private TableColumn<GastoSemanal, Integer> colGastosAnio;
    @FXML
    private TableColumn<GastoSemanal, String> colGastosMes;
    @FXML
    private TableColumn<GastoSemanal, Integer> colGastosSemana;
    @FXML
    private TableColumn<GastoSemanal, String> colGastosInicio;
    @FXML
    private TableColumn<GastoSemanal, String> colGastosFin;
    @FXML
    private TableColumn<GastoSemanal, Double> colGastosTotal;

    @FXML
    private TextField txtSemanaGastos;
    @FXML
    private ComboBox<String> comboUsuariosGastos;
    @FXML
    private Button btnCrearVistaGastos;
    @FXML
    private Button btnCargarVistaGastos;
    @FXML
    private Button btnImprimirGastosSemana1;
    @FXML
    private Button btnRefrescarDatosGastosSemana;
    @FXML
    private Button btnVolverDashboardGastosSemana;
    @FXML
    private Label labelRegistrosGastosSemana;
    @FXML
    private Label labelEstadoGastosSemana;

    private final GastoSemanalDAO gastoSemanalDAO = new GastoSemanalDAO();

    @FXML
    private AnchorPane gastosSemanaRoot;
    @FXML
    private HBox headerGastosSemana;
    @FXML
    private ImageView iconoGastosSemana;
    @FXML
    private Label labelTituloGastosSemana;
    @FXML
    private Separator separatorTopGastosSemana;
    @FXML
    private Text textoDescripcionGastosSemana;
    @FXML
    private HBox filtrosGastosSemana;
    @FXML
    private Text labelFiltrarSemana;
    @FXML
    private Text labelFiltrarUsuario;
    @FXML
    private Separator separatorMiddleGastosSemana;
    @FXML
    private HBox accionesGastosSemana;
    @FXML
    private Separator separatorMiddleGastosSemana1;
    @FXML
    private HBox volverDashboardGastosSemana;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarUsuarios();

        comboUsuariosGastos.setDisable(true);
        txtSemanaGastos.setDisable(true);
        mostrarEstado("Verificando si existe la vista SQL...", false);

        if (existeVistaEnBaseDeDatos("vista_gasto_semanal")) {
            mostrarEstado("Vista detectada en base de datos. Cargando datos...", true);
            cargarDatosVista();
        } else {
            mostrarEstado("No se encontró la vista. Debes crearla antes de consultar.", false);
        }

        btnCrearVistaGastos.setOnAction(e -> crearVistaManual());
        btnCargarVistaGastos.setOnAction(e -> cargarDatosVista());
        btnImprimirGastosSemana1.setOnAction(e -> imprimirListado());
        btnRefrescarDatosGastosSemana.setOnAction(this::accion_RefrescarDatosGastosSemana);
        comboUsuariosGastos.setOnAction(e -> aplicarFiltros());
        txtSemanaGastos.setOnAction(e -> aplicarFiltros());
        btnVolverDashboardGastosSemana.setOnAction(this::accion_volver_al_dashboard);
    }

    // =====================================================
    // VERIFICAR SI EXISTE LA VISTA EN LA BASE DE DATOS
    // =====================================================
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

    // =====================================================
    // CONFIGURAR TABLA
    // =====================================================
    private void configurarTabla() {
        colGastosUsuario.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsuario()));
        colGastosAnio.setCellValueFactory(c
                -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnio()).asObject());
        colGastosMes.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMes()));
        colGastosSemana.setCellValueFactory(c
                -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSemanaDelMes()).asObject());
        colGastosInicio.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getInicioSemana()));
        colGastosFin.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFinSemana()));
        colGastosTotal.setCellValueFactory(c
                -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getGastoSemana()).asObject());
        tablaGastosSemana.setPlaceholder(new Label("No hay registros disponibles."));
    }

    // =====================================================
    // CARGAR USUARIOS
    // =====================================================
    private void cargarUsuarios() {
        try {
            List<String> usuarios = gastoSemanalDAO.obtenerUsuarios();
            comboUsuariosGastos.setItems(FXCollections.observableArrayList(usuarios));
            comboUsuariosGastos.getItems().add(0, "Todos los usuarios");
            comboUsuariosGastos.getSelectionModel().selectFirst();
        } catch (Exception e) {
            Logger.exception("Error al cargar los usuarios.", e);
            mostrarEstado("Error al cargar los usuarios: " + e.getMessage(), false);
        }
    }

    // =====================================================
    // CREAR VISTA MANUAL
    // =====================================================
    private void crearVistaManual() {
        boolean creada = gastoSemanalDAO.crearVistaGastoSemanal();
        if (creada) {
            mostrarEstado("Vista creada correctamente. Cargando datos...", true);
            comboUsuariosGastos.setDisable(false);
            txtSemanaGastos.setDisable(false);
            cargarDatosVista();
        } else {
            mostrarEstado("Error al crear la vista SQL. Verifica permisos o conexión.", false);
        }
    }

    // =====================================================
    // CARGAR DATOS
    // =====================================================
    private void cargarDatosVista() {
        List<GastoSemanal> lista = gastoSemanalDAO.listarPorSemanaYUsuario(0, null);
        tablaGastosSemana.setItems(FXCollections.observableArrayList(lista));
        tablaGastosSemana.refresh();
        actualizarContador(lista.size());

        if (lista.isEmpty()) {
            mostrarEstado("No hay registros disponibles o la vista está vacía.", false);
            comboUsuariosGastos.setDisable(true);
            txtSemanaGastos.setDisable(true);
        } else {
            mostrarEstado("Datos cargados correctamente desde la vista semanal.", true);
            btnCrearVistaGastos.setDisable(true);
            btnCargarVistaGastos.setDisable(true);
            comboUsuariosGastos.setDisable(false);
            txtSemanaGastos.setDisable(false);
        }
    }

    // =====================================================
    // APLICAR FILTROS
    // =====================================================
    private void aplicarFiltros() {
        if (comboUsuariosGastos.isDisabled() || txtSemanaGastos.isDisabled()) {
            mostrarEstado("Primero debes crear o cargar la vista antes de filtrar.", false);
            return;
        }

        String textoSemana = txtSemanaGastos.getText().trim();
        String usuario = comboUsuariosGastos.getSelectionModel().getSelectedItem();
        int numeroSemana = 0;

        if (!textoSemana.isEmpty()) {
            try {
                numeroSemana = Integer.parseInt(textoSemana);
                if (numeroSemana < 1 || numeroSemana > 5) {
                    mostrarEstado("Introduce un número de semana válido (1-5).", false);
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarEstado("Introduce un número de semana válido (1-5).", false);
                return;
            }
        }

        if ("Todos los usuarios".equals(usuario)) {
            usuario = null;
        }

        List<GastoSemanal> lista = gastoSemanalDAO.listarPorSemanaYUsuario(numeroSemana, usuario);
        tablaGastosSemana.setItems(FXCollections.observableArrayList(lista));
        tablaGastosSemana.refresh();
        actualizarContador(lista.size());

        mostrarEstado(lista.isEmpty()
                ? "No se encontraron registros con los filtros aplicados."
                : "Filtros aplicados correctamente.", !lista.isEmpty());
    }

    // =====================================================
    // IMPRESIÓN (fecha dd/MM/yyyy)
    // =====================================================
    private void imprimirListado() {
        List<GastoSemanal> registros = tablaGastosSemana.getItems();
        if (registros.isEmpty()) {
            mostrarEstado("No hay registros para imprimir.", false);
            return;
        }

        String fechaFormateada = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Map<String, List<GastoSemanal>> agrupado = registros.stream()
                .collect(Collectors.groupingBy(GastoSemanal::getUsuario));

        StringBuilder informe = new StringBuilder();
        informe.append("REPORTE DE GASTOS SEMANALES\n");
        informe.append("Fecha de generación: ").append(fechaFormateada).append("\n\n");

        for (String usuario : agrupado.keySet()) {
            informe.append("Usuario: ").append(usuario).append("\n");
            informe.append("-------------------------------------------------------------\n");
            informe.append(String.format("%-10s %-15s %-15s %-15s\n", "Semana", "Inicio", "Fin", "Gasto (€)"));
            informe.append("-------------------------------------------------------------\n");

            for (GastoSemanal g : agrupado.get(usuario)) {
                informe.append(String.format("%-10d %-15s %-15s %-15.2f\n",
                        g.getSemanaDelMes(), g.getInicioSemana(), g.getFinSemana(), g.getGastoSemana()));
            }
            informe.append("\n");
        }

        Label lblInforme = new Label(informe.toString());
        lblInforme.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        lblInforme.setWrapText(false);

        VBox contenedor = new VBox(lblInforme);
        contenedor.setPrefWidth(700);
        contenedor.setPrefHeight(Region.USE_COMPUTED_SIZE);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tablaGastosSemana.getScene().getWindow())) {
            if (job.printPage(contenedor)) {
                job.endJob();
                mostrarEstado("Informe impreso/exportado correctamente.", true);
            } else {
                mostrarEstado("Error al imprimir/exportar el informe.", false);
            }
        }
    }

    // =====================================================
    // REFRESCAR DATOS
    // =====================================================
    @FXML
    private void accion_RefrescarDatosGastosSemana(ActionEvent event) {
        mostrarEstado("Actualizando datos desde la vista SQL...", false);
        try {
            String textoSemana = txtSemanaGastos.getText().trim();
            String usuario = comboUsuariosGastos.getSelectionModel().getSelectedItem();
            int numeroSemana = 0;

            if (!textoSemana.isEmpty()) {
                try {
                    numeroSemana = Integer.parseInt(textoSemana);
                } catch (NumberFormatException e) {
                    mostrarEstado("Introduce un número de semana válido (1–5).", false);
                    return;
                }
            }

            if ("Todos los usuarios".equals(usuario)) {
                usuario = null;
            }

            List<GastoSemanal> lista = gastoSemanalDAO.listarPorSemanaYUsuario(numeroSemana, usuario);
            tablaGastosSemana.setItems(FXCollections.observableArrayList(lista));
            tablaGastosSemana.refresh();
            actualizarContador(lista.size());
            mostrarEstado("Datos actualizados correctamente.", true);

        } catch (Exception e) {
            mostrarEstado("Error al refrescar los datos.", false);
            Logger.exception("Error al refrescar datos desde vista_gasto_semanal", e);
        }
    }

    // =====================================================
    // UTILIDADES
    // =====================================================
    private void actualizarContador(int total) {
        labelRegistrosGastosSemana.setText("Registros cargados: " + total);
    }

    private void mostrarEstado(String mensaje, boolean exito) {
        labelEstadoGastosSemana.setText(mensaje);
        labelEstadoGastosSemana.setTextFill(exito ? Color.web("#28a745") : Color.web("#dc3545"));
        Logger.info(mensaje);
    }

    // =====================================================
    // NAVEGACIÓN
    // =====================================================
    @FXML
    private void accion_volver_al_dashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista GastosPorSemana.");
        } catch (IOException e) {
            mostrarEstado("No se pudo volver al Dashboard.", false);
            Logger.exception("Error al volver al Dashboard desde GastosPorSemanaController", e);
        }
    }
}
