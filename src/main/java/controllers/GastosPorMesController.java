package controllers;

import app_compras.App;
import config.Logger;
import dao.GastosMesDAO;
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
import models.GastosMes;

public class GastosPorMesController implements Initializable {

    // =====================================================
    // ELEMENTOS FXML
    // =====================================================
    @FXML
    private TableView<GastosMes> tablaGastosMensuales;
    @FXML
    private TableColumn<GastosMes, String> colGastosUsuarioMes;
    @FXML
    private TableColumn<GastosMes, Integer> colGastosAnioMes;
    @FXML
    private TableColumn<GastosMes, String> colGastosMesNombre;
    @FXML
    private TableColumn<GastosMes, String> colGastosCategoria;
    @FXML
    private TableColumn<GastosMes, Double> colGastosTotal;
    @FXML
    private ComboBox<String> comboMesesGastosMes;
    @FXML
    private ComboBox<String> comboUsuariosGastosMes;
    @FXML
    private Button btnCrearVistaGastosMes;
    @FXML
    private Button btnCargarVistaGastosMes;
    @FXML
    private Button btnImprimirGastosMes1;
    @FXML
    private Button btnRefrescarDatosGastosMes;
    @FXML
    private Button btnVolverDashboardGastosMes;
    @FXML
    private Label labelRegistrosGastosMes;
    @FXML
    private Label labelEstadoGastosMes;

    private final GastosMesDAO gastosMesDAO = new GastosMesDAO();

    @FXML
    private AnchorPane gastosMesRoot;
    @FXML
    private HBox headerGastosMes;
    @FXML
    private ImageView iconoGastosMes;
    @FXML
    private Label labelTituloGastosMes;
    @FXML
    private Separator separatorTopGastosMes;
    @FXML
    private Text textoDescripcionGastosMes;
    @FXML
    private HBox filtrosGastosMes;
    @FXML
    private Text labelFiltrarMes;
    @FXML
    private Text labelFiltrarUsuarioMes;
    @FXML
    private Separator separatorMiddleGastosMes;
    @FXML
    private HBox accionesGastosMes;
    @FXML
    private Separator separatorMiddleGastosMes1;
    @FXML
    private HBox volverDashboardGastosMes;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarMeses();
        cargarUsuarios();

        comboMesesGastosMes.setDisable(true);
        comboUsuariosGastosMes.setDisable(true);
        mostrarEstado("Verificando si existe la vista SQL...", false);

        // 🔹 Detectar vista existente
        if (existeVistaEnBaseDeDatos("vista_gasto_mensual")) {
            mostrarEstado("Vista detectada en base de datos. Cargando datos...", true);
            cargarDatosVista();
        } else {
            mostrarEstado("No se encontró la vista. Debes crearla antes de consultar.", false);
        }

        // 🔹 Eventos
        btnCrearVistaGastosMes.setOnAction(e -> crearVistaManual());
        btnCargarVistaGastosMes.setOnAction(e -> cargarDatosVista());
        btnImprimirGastosMes1.setOnAction(e -> imprimirListado());
        btnRefrescarDatosGastosMes.setOnAction(this::accion_RefrescarDatosGastosMes);
        comboMesesGastosMes.setOnAction(e -> aplicarFiltros());
        comboUsuariosGastosMes.setOnAction(e -> aplicarFiltros());
        btnVolverDashboardGastosMes.setOnAction(this::accion_volver_al_dashboard);
    }

    // =====================================================
    // VERIFICAR SI EXISTE LA VISTA
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
        colGastosUsuarioMes.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsuario()));
        colGastosAnioMes.setCellValueFactory(c
                -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnio()).asObject());
        colGastosMesNombre.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(traducirMes(c.getValue().getMes())));
        colGastosCategoria.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategoria()));
        colGastosTotal.setCellValueFactory(c
                -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getGastoTotal()).asObject());
        tablaGastosMensuales.setPlaceholder(new Label("No hay registros disponibles."));
    }

    // =====================================================
    // CARGAR COMBOS
    // =====================================================
    private void cargarMeses() {
        comboMesesGastosMes.setItems(FXCollections.observableArrayList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));
        comboMesesGastosMes.getItems().add(0, "Todos los meses");
        comboMesesGastosMes.getSelectionModel().selectFirst();
    }

    private void cargarUsuarios() {
        try {
            List<String> usuarios = gastosMesDAO.obtenerUsuarios();
            comboUsuariosGastosMes.setItems(FXCollections.observableArrayList(usuarios));
            comboUsuariosGastosMes.getItems().add(0, "Todos los usuarios");
            comboUsuariosGastosMes.getSelectionModel().selectFirst();
        } catch (Exception e) {
            Logger.exception("Error al cargar los usuarios.", e);
            mostrarEstado("Error al cargar los usuarios: " + e.getMessage(), false);
        }
    }

    // =====================================================
    // CREAR VISTA MANUAL
    // =====================================================
    private void crearVistaManual() {
        try {
            boolean creada = gastosMesDAO.crearVistaGastoMensual();
            if (creada) {
                mostrarEstado("Vista creada correctamente. Cargando datos...", true);
                comboMesesGastosMes.setDisable(false);
                comboUsuariosGastosMes.setDisable(false);
                cargarDatosVista();
            } else {
                mostrarEstado("Error al crear la vista SQL. Verifica permisos o conexión.", false);
            }
        } catch (Exception e) {
            Logger.exception("Error al crear la vista de gastos mensuales.", e);
            mostrarEstado("Error al crear la vista: " + e.getMessage(), false);
        }
    }

    // =====================================================
    // CARGAR DATOS DE LA VISTA
    // =====================================================
    private void cargarDatosVista() {
        try {
            List<GastosMes> lista = gastosMesDAO.listarPorMesYUsuario(0, null);
            tablaGastosMensuales.setItems(FXCollections.observableArrayList(lista));
            actualizarContador(lista.size());

            if (lista.isEmpty()) {
                mostrarEstado("No hay registros disponibles o la vista está vacía.", false);
                comboMesesGastosMes.setDisable(true);
                comboUsuariosGastosMes.setDisable(true);
            } else {
                mostrarEstado("Datos cargados correctamente desde la vista mensual.", true);
                btnCrearVistaGastosMes.setDisable(true);
                btnCargarVistaGastosMes.setDisable(true);
                comboMesesGastosMes.setDisable(false);
                comboUsuariosGastosMes.setDisable(false);
            }
        } catch (Exception e) {
            Logger.exception("Error al cargar datos desde la vista mensual.", e);
            mostrarEstado("Error al cargar los datos: " + e.getMessage(), false);
        }
    }

    // =====================================================
    // APLICAR FILTROS
    // =====================================================
    private void aplicarFiltros() {
        if (comboMesesGastosMes.isDisabled() || comboUsuariosGastosMes.isDisabled()) {
            mostrarEstado("Primero debes crear o cargar la vista antes de filtrar.", false);
            return;
        }

        String mesSeleccionado = comboMesesGastosMes.getValue();
        String usuarioSeleccionado = comboUsuariosGastosMes.getSelectionModel().getSelectedItem();
        int numeroMes = convertirMesANumero(mesSeleccionado);

        if ("Todos los meses".equals(mesSeleccionado)) {
            numeroMes = 0;
        }
        if ("Todos los usuarios".equals(usuarioSeleccionado)) {
            usuarioSeleccionado = null;
        }

        try {
            List<GastosMes> lista = gastosMesDAO.listarPorMesYUsuario(numeroMes, usuarioSeleccionado);
            tablaGastosMensuales.setItems(FXCollections.observableArrayList(lista));
            actualizarContador(lista.size());
            mostrarEstado(lista.isEmpty()
                    ? "No se encontraron registros con los filtros aplicados."
                    : "Filtros aplicados correctamente.", !lista.isEmpty());
        } catch (Exception e) {
            Logger.exception("Error al aplicar filtros.", e);
            mostrarEstado("Error al aplicar filtros: " + e.getMessage(), false);
        }
    }

    // =====================================================
    // TRADUCCIÓN Y CONVERSIÓN DE MESES
    // =====================================================
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

    private int convertirMesANumero(String mes) {
        if (mes == null) {
            return 0;
        }
        switch (mes.toLowerCase()) {
            case "enero":
                return 1;
            case "febrero":
                return 2;
            case "marzo":
                return 3;
            case "abril":
                return 4;
            case "mayo":
                return 5;
            case "junio":
                return 6;
            case "julio":
                return 7;
            case "agosto":
                return 8;
            case "septiembre":
                return 9;
            case "octubre":
                return 10;
            case "noviembre":
                return 11;
            case "diciembre":
                return 12;
            default:
                return 0;
        }
    }

    // =====================================================
    // IMPRESIÓN (formato dd/MM/yyyy)
    // =====================================================
    private void imprimirListado() {
        List<GastosMes> registros = tablaGastosMensuales.getItems();
        if (registros.isEmpty()) {
            mostrarEstado("No hay registros para imprimir.", false);
            return;
        }

        String fechaFormateada = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Map<String, List<GastosMes>> agrupado = registros.stream()
                .collect(Collectors.groupingBy(GastosMes::getUsuario));

        StringBuilder informe = new StringBuilder("REPORTE DE GASTOS MENSUALES\n")
                .append("Fecha de generación: ").append(fechaFormateada).append("\n\n");

        for (String usuario : agrupado.keySet()) {
            informe.append("Usuario: ").append(usuario).append("\n")
                    .append("-------------------------------------------------------------\n")
                    .append(String.format("%-10s %-20s %-15s\n", "Mes", "Categoría", "Gasto (€)"))
                    .append("-------------------------------------------------------------\n");

            for (GastosMes g : agrupado.get(usuario)) {
                informe.append(String.format("%-10s %-20s %-15.2f\n",
                        traducirMes(g.getMes()), g.getCategoria(), g.getGastoTotal()));
            }
            informe.append("\n");
        }

        Label lblInforme = new Label(informe.toString());
        lblInforme.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        VBox contenedor = new VBox(lblInforme);
        contenedor.setPrefWidth(700);
        contenedor.setPrefHeight(Region.USE_COMPUTED_SIZE);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tablaGastosMensuales.getScene().getWindow())) {
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
    private void accion_RefrescarDatosGastosMes(ActionEvent event) {
        mostrarEstado("Actualizando datos desde la vista SQL...", false);
        try {
            String mesSeleccionado = comboMesesGastosMes.getValue();
            String usuarioSeleccionado = comboUsuariosGastosMes.getSelectionModel().getSelectedItem();
            int numeroMes = convertirMesANumero(mesSeleccionado);

            if ("Todos los meses".equals(mesSeleccionado)) {
                numeroMes = 0;
            }
            if ("Todos los usuarios".equals(usuarioSeleccionado)) {
                usuarioSeleccionado = null;
            }

            List<GastosMes> lista = gastosMesDAO.listarPorMesYUsuario(numeroMes, usuarioSeleccionado);
            tablaGastosMensuales.setItems(FXCollections.observableArrayList(lista));
            actualizarContador(lista.size());
            mostrarEstado("Datos actualizados correctamente.", true);

        } catch (Exception e) {
            mostrarEstado("Error al refrescar los datos.", false);
            Logger.exception("Error al refrescar datos desde vista_gasto_mensual", e);
        }
    }

    // =====================================================
    // UTILIDADES
    // =====================================================
    private void actualizarContador(int total) {
        labelRegistrosGastosMes.setText("Registros cargados: " + total);
    }

    private void mostrarEstado(String mensaje, boolean exito) {
        labelEstadoGastosMes.setText(mensaje);
        labelEstadoGastosMes.setTextFill(exito ? Color.web("#28a745") : Color.web("#dc3545"));
        Logger.info(mensaje);
    }

    // =====================================================
    // NAVEGACIÓN
    // =====================================================
    @FXML
    private void accion_volver_al_dashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista GastosPorMes.");
        } catch (IOException e) {
            mostrarEstado("No se pudo volver al Dashboard.", false);
            Logger.exception("Error al volver al Dashboard desde GastosPorMesController", e);
        }
    }
}
