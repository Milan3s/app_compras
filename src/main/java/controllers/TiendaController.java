package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import dao.TiendaDAO;
import dao.RepartidorDAO;
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
import models.Tienda;
import models.Repartidor;
import models.Usuario;

public class TiendaController implements Initializable {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtSitioWeb;
    @FXML
    private TextField txtBuscarTienda;

    @FXML
    private ComboBox<Repartidor> comboRepartidor;

    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnVolver;

    @FXML
    private TableView<Tienda> tablaTiendas;
    @FXML
    private TableColumn<Tienda, Integer> colId;
    @FXML
    private TableColumn<Tienda, String> colNombre;
    @FXML
    private TableColumn<Tienda, String> colDireccion;
    @FXML
    private TableColumn<Tienda, String> colTelefono;
    @FXML
    private TableColumn<Tienda, String> colSitioWeb;
    @FXML
    private TableColumn<Tienda, String> colRepartidor;

    @FXML
    private Label labelMensaje;
    @FXML
    private Label labelUsuarioActivo;
    @FXML
    private Label labelRepartidor;
    @FXML
    private HBox hboxBotonesTienda;
    @FXML
    private AnchorPane tiendaRoot;

    private final TiendaDAO tiendaDAO = new TiendaDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final ObservableList<Tienda> listaTiendas = FXCollections.observableArrayList();
    private final ObservableList<Repartidor> listaRepartidores = FXCollections.observableArrayList();
    private Tienda tiendaSeleccionada = null;
    @FXML
    private VBox headerTienda;
    @FXML
    private Label labelTituloTienda;
    @FXML
    private Label labelNombre;
    @FXML
    private Label labelDireccion;
    @FXML
    private Label labelTelefono;
    @FXML
    private Label labelSitioWeb;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarRepartidores();
        cargarTiendas();
        configurarEventosTabla();

        txtBuscarTienda.textProperty().addListener((observable, oldValue, newValue) -> filtrarTiendas(newValue));

        Usuario usuario = Session.getUsuarioActual();
        if (usuario != null) {
            labelUsuarioActivo.setText(usuario.getNombre());
            Logger.info("Sesión activa: " + usuario.getNombre());
        } else {
            labelUsuarioActivo.setText("Invitado");
            labelMensaje.setText("No hay sesión activa. Algunas funciones pueden estar limitadas.");
            labelMensaje.setStyle("-fx-text-fill: orange;");
            Logger.warning("Vista Tienda abierta sin sesión activa.");
        }
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTienda"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colSitioWeb.setCellValueFactory(new PropertyValueFactory<>("sitioWeb"));
        colRepartidor.setCellValueFactory(new PropertyValueFactory<>("nombreRepartidor"));
    }

    private void cargarRepartidores() {
        listaRepartidores.setAll(repartidorDAO.listarTodos());
        comboRepartidor.setItems(listaRepartidores);
        comboRepartidor.setPromptText("Seleccionar repartidor (opcional)");
    }

    private void cargarTiendas() {
        List<Tienda> tiendas = tiendaDAO.listarTodas();

        //Ordenar las tiendas de menor a mayor según su ID
        tiendas.sort((t1, t2) -> Integer.compare(t1.getIdTienda(), t2.getIdTienda()));

        listaTiendas.setAll(tiendas);
        tablaTiendas.setItems(listaTiendas);

        if (tiendas.isEmpty()) {
            labelMensaje.setText("No hay datos en la tabla.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
        } else {
            labelMensaje.setText("Datos cargados correctamente (" + tiendas.size() + " registro"
                    + (tiendas.size() > 1 ? "s" : "") + ").");
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }

    private void configurarEventosTabla() {
        tablaTiendas.setOnMouseClicked((MouseEvent event) -> {
            if (tablaTiendas.getSelectionModel().getSelectedItem() != null) {
                tiendaSeleccionada = tablaTiendas.getSelectionModel().getSelectedItem();
                txtNombre.setText(tiendaSeleccionada.getNombre());
                txtDireccion.setText(tiendaSeleccionada.getDireccion());
                txtTelefono.setText(tiendaSeleccionada.getTelefono());
                txtSitioWeb.setText(tiendaSeleccionada.getSitioWeb());

                if (tiendaSeleccionada.getIdRepartidor() != null) {
                    for (Repartidor r : listaRepartidores) {
                        if (r.getIdRepartidor() == tiendaSeleccionada.getIdRepartidor()) {
                            comboRepartidor.getSelectionModel().select(r);
                            break;
                        }
                    }
                } else {
                    comboRepartidor.getSelectionModel().clearSelection();
                }

                labelMensaje.setText("Tienda seleccionada: " + tiendaSeleccionada.getNombre());
                labelMensaje.setStyle("-fx-text-fill: #2c3e50;");
            }
        });
    }

    @FXML
    private void agregarTienda(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String sitioWeb = txtSitioWeb.getText().trim();
        Repartidor repartidorSeleccionado = comboRepartidor.getValue();

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre de la tienda es obligatorio.");
            return;
        }

        Tienda existente = tiendaDAO.obtenerPorNombre(nombre);
        if (existente != null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Duplicado", "Ya existe una tienda con ese nombre.");
            return;
        }

        Tienda nueva = new Tienda();
        nueva.setNombre(nombre);
        nueva.setDireccion(direccion);
        nueva.setTelefono(telefono);
        nueva.setSitioWeb(sitioWeb);

        if (repartidorSeleccionado != null) {
            nueva.setIdRepartidor(repartidorSeleccionado.getIdRepartidor());
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar creación");
        confirm.setHeaderText("¿Deseas agregar esta tienda?");
        confirm.setContentText("Nombre: " + nombre + "\nDirección: " + direccion);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean insertado = tiendaDAO.insertar(nueva);
                if (insertado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Tienda agregada", "Tienda registrada correctamente.");
                    limpiarFormulario(null);
                    cargarTiendas();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agregar la tienda.");
                }
            }
        });
    }

    @FXML
    private void actualizarTienda(ActionEvent event) {
        if (tiendaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona una tienda para actualizar.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String sitioWeb = txtSitioWeb.getText().trim();
        Repartidor repartidorSeleccionado = comboRepartidor.getValue();

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre no puede estar vacío.");
            return;
        }

        Tienda otra = tiendaDAO.obtenerPorNombre(nombre);
        if (otra != null && otra.getIdTienda() != tiendaSeleccionada.getIdTienda()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Duplicado", "Ya existe otra tienda con ese nombre.");
            return;
        }

        tiendaSeleccionada.setNombre(nombre);
        tiendaSeleccionada.setDireccion(direccion);
        tiendaSeleccionada.setTelefono(telefono);
        tiendaSeleccionada.setSitioWeb(sitioWeb);

        if (repartidorSeleccionado != null) {
            tiendaSeleccionada.setIdRepartidor(repartidorSeleccionado.getIdRepartidor());
        } else {
            tiendaSeleccionada.setIdRepartidor(null);
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar actualización");
        confirm.setHeaderText("¿Deseas actualizar la tienda seleccionada?");
        confirm.setContentText("Nuevo nombre: " + nombre + "\nNueva dirección: " + direccion);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean actualizado = tiendaDAO.actualizar(tiendaSeleccionada);
                if (actualizado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Tienda actualizada", "La tienda se actualizó correctamente.");
                    limpiarFormulario(null);
                    cargarTiendas();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la tienda.");
                }
            }
        });
    }

    @FXML
    private void eliminarTienda(ActionEvent event) {
        if (tiendaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona una tienda para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Deseas eliminar esta tienda?");
        confirm.setContentText("Tienda: " + tiendaSeleccionada.getNombre());
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean eliminado = tiendaDAO.eliminar(tiendaSeleccionada.getIdTienda());
                if (eliminado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Tienda eliminada", "La tienda fue eliminada correctamente.");
                    limpiarFormulario(null);
                    cargarTiendas();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la tienda.");
                }
            }
        });
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtNombre.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtSitioWeb.clear();
        comboRepartidor.getSelectionModel().clearSelection();
        tiendaSeleccionada = null;
        tablaTiendas.getSelectionModel().clearSelection();
        labelMensaje.setText("Formulario limpio.");
        labelMensaje.setStyle("-fx-text-fill: gray;");
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista Tienda.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo volver al Dashboard.");
            Logger.exception("Error al volver al dashboard desde TiendaController", e);
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

    private void filtrarTiendas(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            tablaTiendas.setItems(listaTiendas);
            labelMensaje.setText("Mostrando todas las tiendas.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
            return;
        }

        String texto = filtro.toLowerCase();

        ObservableList<Tienda> filtradas = listaTiendas.filtered(t
                -> (t.getNombre() != null && t.getNombre().toLowerCase().contains(texto))
                || (t.getDireccion() != null && t.getDireccion().toLowerCase().contains(texto))
                || (t.getTelefono() != null && t.getTelefono().toLowerCase().contains(texto))
                || (t.getSitioWeb() != null && t.getSitioWeb().toLowerCase().contains(texto))
                || (t.getNombreRepartidor() != null && t.getNombreRepartidor().toLowerCase().contains(texto))
        );

        tablaTiendas.setItems(filtradas);

        if (filtradas.isEmpty()) {
            labelMensaje.setText("No se encontraron tiendas para: \"" + filtro + "\"");
            labelMensaje.setStyle("-fx-text-fill: red;");
        } else {
            labelMensaje.setText("Coincidencias encontradas: " + filtradas.size());
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }
}
