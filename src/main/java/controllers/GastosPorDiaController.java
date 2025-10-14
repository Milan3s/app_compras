package controllers;

import app_compras.App;
import config.Logger;
import dao.GastosDIADAO;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
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
import models.GastosDia;
import config.ConexionDB;

public class GastosPorDiaController implements Initializable {

    @FXML
    private TableView<GastosDia> tablaGastosDiarios;
    @FXML
    private TableColumn<GastosDia, String> colGastosUsuarioDia;
    @FXML
    private TableColumn<GastosDia, Integer> colGastosAnioDia;
    @FXML
    private TableColumn<GastosDia, String> colGastosMesDia;
    @FXML
    private TableColumn<GastosDia, String> colGastosDiaSemana;
    @FXML
    private TableColumn<GastosDia, Double> colGastosMontoDia;

    @FXML
    private ComboBox<String> comboUsuariosGastosDia;
    @FXML
    private ComboBox<String> comboDiasGastosDia;
    @FXML
    private Button btnCrearVistaGastosDia;
    @FXML
    private Button btnCargarVistaGastosDia;
    @FXML
    private Button btnImprimirGastosDia1;
    @FXML
    private Button btnVolverDashboardGastosDia;
    @FXML
    private Button btnRefescarGastosDia;
    @FXML
    private Label labelRegistrosGastosDia;
    @FXML
    private Label labelEstadoGastosDia;

    private final GastosDIADAO gastosDiaDAO = new GastosDIADAO();

    @FXML
    private AnchorPane gastosDiaRoot;
    @FXML
    private HBox headerGastosDia;
    @FXML
    private ImageView iconoGastosDia;
    @FXML
    private Label labelTituloGastosDia;
    @FXML
    private Separator separatorTopGastosDia;
    @FXML
    private Text textoDescripcionGastosDia;
    @FXML
    private HBox filtrosGastosDia;
    @FXML
    private Text labelFiltrarDia;
    @FXML
    private Text labelFiltrarUsuarioDia;
    @FXML
    private Separator separatorMiddleGastosDia;
    @FXML
    private HBox accionesGastosDia;
    @FXML
    private Separator separatorMiddleGastosDia1;
    @FXML
    private HBox volverDashboardGastosDia;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarUsuarios();
        cargarDiasSemana();

        comboUsuariosGastosDia.setDisable(true);
        comboDiasGastosDia.setDisable(true);
        mostrarMensajeUsuario("Verificando si existe la vista SQL...", false);

        // Detectar vista
        if (existeVistaEnBaseDeDatos("vista_gasto_diario")) {
            mostrarMensajeUsuario("Vista detectada en base de datos. Cargando datos...", true);
            cargarDatosVista();
        } else {
            mostrarMensajeUsuario("No se encontró la vista. Debes crearla antes de consultar.", false);
        }

        // Eventos
        btnCrearVistaGastosDia.setOnAction(e -> crearVistaManual());
        btnCargarVistaGastosDia.setOnAction(e -> cargarDatosVista());
        btnImprimirGastosDia1.setOnAction(e -> imprimirListado());
        comboUsuariosGastosDia.setOnAction(e -> aplicarFiltro());
        comboDiasGastosDia.setOnAction(e -> aplicarFiltro());
    }

    // ===================================================
    // Verificar existencia de vista
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
    // Configuración de tabla
    // ===================================================
    private void configurarTabla() {
        colGastosUsuarioDia.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsuario()));
        colGastosAnioDia.setCellValueFactory(c
                -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnio()).asObject());
        colGastosMesDia.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(traducirMes(c.getValue().getMes())));
        colGastosDiaSemana.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDiaSemana()));
        colGastosMontoDia.setCellValueFactory(c
                -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getGastoTotal()).asObject());

        tablaGastosDiarios.setPlaceholder(new Label("No hay registros disponibles."));
    }

    // ===================================================
    // Cargar combos
    // ===================================================
    private void cargarUsuarios() {
        try {
            List<String> usuarios = gastosDiaDAO.obtenerUsuarios();
            comboUsuariosGastosDia.setItems(FXCollections.observableArrayList(usuarios));
            comboUsuariosGastosDia.getItems().add(0, "Todos los usuarios");
            comboUsuariosGastosDia.getSelectionModel().selectFirst();
        } catch (Exception e) {
            Logger.exception("Error al cargar usuarios.", e);
        }
    }

    private void cargarDiasSemana() {
        List<String> dias = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");
        comboDiasGastosDia.setItems(FXCollections.observableArrayList(dias));
        comboDiasGastosDia.getItems().add(0, "Todos los días");
        comboDiasGastosDia.getSelectionModel().selectFirst();
    }

    // ===================================================
    // Acciones de botones
    // ===================================================
    private void crearVistaManual() {
        boolean creada = gastosDiaDAO.crearVistaGastoDiario();
        if (creada) {
            mostrarMensajeUsuario("Vista creada correctamente. Cargando datos...", true);
            comboUsuariosGastosDia.setDisable(false);
            comboDiasGastosDia.setDisable(false);
            cargarDatosVista();
        } else {
            mostrarMensajeUsuario("Error al crear la vista SQL. Verifica permisos o conexión.", false);
        }
    }

    private void cargarDatosVista() {
        List<GastosDia> lista = gastosDiaDAO.listarPorDiaYUsuario(null, null);
        mostrarDatosOrdenados(lista);
    }

    private void aplicarFiltro() {
        if (comboUsuariosGastosDia.isDisabled()) {
            mostrarMensajeUsuario("Primero debes crear o cargar la vista antes de filtrar.", false);
            return;
        }

        String diaSemana = comboDiasGastosDia.getSelectionModel().getSelectedItem();
        if ("Todos los días".equals(diaSemana)) {
            diaSemana = null;
        }

        String usuario = comboUsuariosGastosDia.getSelectionModel().getSelectedItem();
        if ("Todos los usuarios".equals(usuario)) {
            usuario = null;
        }

        List<GastosDia> lista = gastosDiaDAO.listarPorDiaYUsuario(diaSemana, usuario);
        mostrarDatosOrdenados(lista);
    }

    // ===================================================
    // Mostrar y ordenar datos
    // ===================================================
    private void mostrarDatosOrdenados(List<GastosDia> lista) {
        if (lista == null || lista.isEmpty()) {
            tablaGastosDiarios.setItems(FXCollections.observableArrayList());
            actualizarContador(0);
            mostrarMensajeUsuario("No hay registros disponibles o la vista está vacía.", false);
            return;
        }

        ObservableList<GastosDia> datos = FXCollections.observableArrayList(lista);

        SortedList<GastosDia> sorted = new SortedList<>(datos);
        sorted.setComparator(Comparator
                .comparing(GastosDia::getAnio)
                .thenComparing(g -> mesANumero(g.getMes()))
                .thenComparing(GastosDia::getUsuario, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(this::ordenDiaSemana));

        tablaGastosDiarios.setItems(sorted);
        actualizarContador(sorted.size());
        mostrarMensajeUsuario("Datos cargados correctamente desde la vista diaria.", true);

        btnCrearVistaGastosDia.setDisable(true);
        btnCargarVistaGastosDia.setDisable(true);
        comboUsuariosGastosDia.setDisable(false);
        comboDiasGastosDia.setDisable(false);
    }

    // ===================================================
    // Conversión de mes y orden de día
    // ===================================================
    private int mesANumero(String mes) {
        if (mes == null) {
            return 0;
        }
        switch (mes.toLowerCase()) {
            case "january":
                return 1;
            case "february":
                return 2;
            case "march":
                return 3;
            case "april":
                return 4;
            case "may":
                return 5;
            case "june":
                return 6;
            case "july":
                return 7;
            case "august":
                return 8;
            case "september":
                return 9;
            case "october":
                return 10;
            case "november":
                return 11;
            case "december":
                return 12;
            default:
                return 0;
        }
    }

    private int ordenDiaSemana(GastosDia g) {
        switch (g.getDiaSemana()) {
            case "Lunes":
                return 1;
            case "Martes":
                return 2;
            case "Miércoles":
                return 3;
            case "Jueves":
                return 4;
            case "Viernes":
                return 5;
            case "Sábado":
                return 6;
            case "Domingo":
                return 7;
            default:
                return 8;
        }
    }

    private String traducirMes(String mes) {
        if (mes == null) {
            return "";
        }
        switch (mes.toLowerCase()) {
            case "january":
                return "Enero";
            case "february":
                return "Febrero";
            case "march":
                return "Marzo";
            case "april":
                return "Abril";
            case "may":
                return "Mayo";
            case "june":
                return "Junio";
            case "july":
                return "Julio";
            case "august":
                return "Agosto";
            case "september":
                return "Septiembre";
            case "october":
                return "Octubre";
            case "november":
                return "Noviembre";
            case "december":
                return "Diciembre";
            default:
                return mes;
        }
    }

    // ===================================================
// Impresión
// ===================================================
    private void imprimirListado() {
        List<GastosDia> registros = tablaGastosDiarios.getItems();
        if (registros.isEmpty()) {
            mostrarMensajeUsuario("No hay registros para imprimir.", false);
            return;
        }

        // 🔹 Formatear fecha como dd/MM/yyyy
        String fechaFormateada = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        StringBuilder informe = new StringBuilder();
        informe.append("REPORTE DE GASTOS POR DÍA DE LA SEMANA\n");
        informe.append("Fecha de generación: ").append(fechaFormateada).append("\n\n");
        informe.append(String.format("%-20s %-6s %-12s %-15s %-10s\n",
                "Usuario", "Año", "Mes", "Día Semana", "Gasto (€)"));
        informe.append("--------------------------------------------------------------------------\n");

        for (GastosDia g : registros) {
            informe.append(String.format("%-20s %-6d %-12s %-15s %-10.2f\n",
                    g.getUsuario(),
                    g.getAnio(),
                    traducirMes(g.getMes()),
                    g.getDiaSemana(),
                    g.getGastoTotal()));
        }

        Label lblInforme = new Label(informe.toString());
        lblInforme.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        lblInforme.setWrapText(false);

        VBox contenedor = new VBox(lblInforme);
        contenedor.setPrefWidth(700);
        contenedor.setPrefHeight(Region.USE_COMPUTED_SIZE);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tablaGastosDiarios.getScene().getWindow())) {
            if (job.printPage(contenedor)) {
                job.endJob();
                mostrarMensajeUsuario("Informe impreso/exportado correctamente.", true);
            } else {
                mostrarMensajeUsuario("Error al imprimir/exportar el informe.", false);
            }
        }
    }

    // ===================================================
    // Utilidades
    // ===================================================
    private void actualizarContador(int total) {
        labelRegistrosGastosDia.setText("Registros cargados: " + total);
    }

    private void mostrarMensajeUsuario(String mensaje, boolean exito) {
        labelEstadoGastosDia.setText(mensaje);
        labelEstadoGastosDia.setTextFill(exito ? Color.web("#28a745") : Color.web("#dc3545"));
        Logger.info(mensaje);
    }

    // ===================================================
    // Navegación
    // ===================================================
    @FXML
    private void accion_volver_al_dashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista GastosPorDia.");
        } catch (IOException e) {
            mostrarMensajeUsuario("No se pudo volver al Dashboard.", false);
            Logger.exception("Error al volver al dashboard desde GastosPorDiaController", e);
        }
    }

    @FXML
    private void refrescarDatos(ActionEvent event) {
        mostrarMensajeUsuario("Actualizando datos desde la vista SQL...", false);
        try {
            String diaSemana = comboDiasGastosDia.getSelectionModel().getSelectedItem();
            if ("Todos los días".equals(diaSemana)) {
                diaSemana = null;
            }

            String usuario = comboUsuariosGastosDia.getSelectionModel().getSelectedItem();
            if ("Todos los usuarios".equals(usuario)) {
                usuario = null;
            }

            List<GastosDia> lista = gastosDiaDAO.listarPorDiaYUsuario(diaSemana, usuario);
            mostrarDatosOrdenados(lista);

            mostrarMensajeUsuario("Datos actualizados correctamente.", true);
        } catch (Exception e) {
            mostrarMensajeUsuario("Error al actualizar los datos.", false);
            Logger.exception("Error al refrescar datos desde vista_gasto_diario", e);
        }
    }
}
