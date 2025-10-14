package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
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
import models.Repartidor;
import models.Usuario;

/**
 * Controlador para la gestión de repartidores. Permite realizar operaciones
 * CRUD sobre la tabla "repartidores".
 *
 * @author Milanes
 */
public class RepartidorController implements Initializable {

    @FXML
    private AnchorPane repartidoresRoot;
    @FXML
    private Label labelTituloRepartidores;
    @FXML
    private Label labelUsuarioActivo;

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtContacto;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtSitioWeb;
    @FXML
    private TextField txtBuscarRepartidor;

    @FXML
    private HBox hboxBotonesCRUD;
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
    private TableView<Repartidor> tablaRepartidores;
    @FXML
    private TableColumn<Repartidor, Integer> colId;
    @FXML
    private TableColumn<Repartidor, String> colNombre;
    @FXML
    private TableColumn<Repartidor, String> colContacto;
    @FXML
    private TableColumn<Repartidor, String> colTelefono;
    @FXML
    private TableColumn<Repartidor, String> colEmail;
    @FXML
    private TableColumn<Repartidor, String> colDireccion;
    @FXML
    private TableColumn<Repartidor, String> colWeb;

    @FXML
    private Label labelMensaje;

    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final ObservableList<Repartidor> listaRepartidores = FXCollections.observableArrayList();
    private Repartidor repartidorSeleccionado = null;
    @FXML
    private Label labelNombre;
    @FXML
    private Label labelContacto;
    @FXML
    private Label labelTelefono;
    @FXML
    private Label labelEmail;
    @FXML
    private Label labelDireccion;
    @FXML
    private Label labelWeb;
    @FXML
    private VBox headerRepartidores;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarRepartidores();
        configurarEventosTabla();

        txtBuscarRepartidor.textProperty().addListener((observable, oldValue, newValue) -> filtrarRepartidores(newValue));

        Usuario usuario = Session.getUsuarioActual();
        if (usuario != null) {
            labelUsuarioActivo.setText(usuario.getNombre());
            Logger.info("Sesión activa: " + usuario.getNombre());
        } else {
            labelUsuarioActivo.setText("Invitado");
            labelMensaje.setText("No hay sesión activa. Algunas funciones pueden estar limitadas.");
            labelMensaje.setStyle("-fx-text-fill: orange;");
            Logger.warning("Vista Repartidores abierta sin sesión activa.");
        }
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idRepartidor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colWeb.setCellValueFactory(new PropertyValueFactory<>("sitioWeb"));
    }

    private void cargarRepartidores() {
        List<Repartidor> repartidores = repartidorDAO.listarTodos();

        //Ordenar los repartidores de menor a mayor según su ID
        repartidores.sort((r1, r2) -> Integer.compare(r1.getIdRepartidor(), r2.getIdRepartidor()));

        listaRepartidores.setAll(repartidores);
        tablaRepartidores.setItems(listaRepartidores);

        if (repartidores.isEmpty()) {
            labelMensaje.setText("No hay datos en la tabla.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
        } else {
            labelMensaje.setText("Datos cargados correctamente (" + repartidores.size() + " repartidor"
                    + (repartidores.size() > 1 ? "es" : "") + ").");
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }

    private void configurarEventosTabla() {
        tablaRepartidores.setOnMouseClicked((MouseEvent event) -> {
            if (tablaRepartidores.getSelectionModel().getSelectedItem() != null) {
                repartidorSeleccionado = tablaRepartidores.getSelectionModel().getSelectedItem();
                txtNombre.setText(repartidorSeleccionado.getNombre());
                txtContacto.setText(repartidorSeleccionado.getContacto());
                txtTelefono.setText(repartidorSeleccionado.getTelefono());
                txtEmail.setText(repartidorSeleccionado.getEmail());
                txtDireccion.setText(repartidorSeleccionado.getDireccion());
                txtSitioWeb.setText(repartidorSeleccionado.getSitioWeb());

                labelMensaje.setText("Repartidor seleccionado: " + repartidorSeleccionado.getNombre());
                labelMensaje.setStyle("-fx-text-fill: #2c3e50;");
            }
        });
    }

    @FXML
    private void agregarRepartidor(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String contacto = txtContacto.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String sitioWeb = txtSitioWeb.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre del repartidor es obligatorio.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar creación");
        confirm.setHeaderText("¿Deseas agregar este repartidor?");
        confirm.setContentText("Nombre: " + nombre + "\nTeléfono: " + telefono);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                Repartidor nuevo = new Repartidor();
                nuevo.setNombre(nombre);
                nuevo.setContacto(contacto);
                nuevo.setTelefono(telefono);
                nuevo.setEmail(email);
                nuevo.setDireccion(direccion);
                nuevo.setSitioWeb(sitioWeb);

                boolean insertado = repartidorDAO.insertar(nuevo);
                if (insertado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Repartidor agregado", "Repartidor registrado correctamente.");
                    limpiarFormulario(null);
                    cargarRepartidores();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agregar el repartidor.");
                }
            }
        });
    }

    @FXML
    private void actualizarRepartidor(ActionEvent event) {
        if (repartidorSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona un repartidor para actualizar.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String contacto = txtContacto.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String sitioWeb = txtSitioWeb.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre no puede estar vacío.");
            return;
        }

        repartidorSeleccionado.setNombre(nombre);
        repartidorSeleccionado.setContacto(contacto);
        repartidorSeleccionado.setTelefono(telefono);
        repartidorSeleccionado.setEmail(email);
        repartidorSeleccionado.setDireccion(direccion);
        repartidorSeleccionado.setSitioWeb(sitioWeb);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar actualización");
        confirm.setHeaderText("¿Deseas actualizar este repartidor?");
        confirm.setContentText("Nuevo nombre: " + nombre);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean actualizado = repartidorDAO.actualizar(repartidorSeleccionado);
                if (actualizado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Repartidor actualizado", "El repartidor se actualizó correctamente.");
                    limpiarFormulario(null);
                    cargarRepartidores();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el repartidor.");
                }
            }
        });
    }

    @FXML
    private void eliminarRepartidor(ActionEvent event) {
        if (repartidorSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona un repartidor para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Deseas eliminar este repartidor?");
        confirm.setContentText("Repartidor: " + repartidorSeleccionado.getNombre());
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean eliminado = repartidorDAO.eliminar(repartidorSeleccionado.getIdRepartidor());
                if (eliminado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Repartidor eliminado", "El repartidor fue eliminado correctamente.");
                    limpiarFormulario(null);
                    cargarRepartidores();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el repartidor.");
                }
            }
        });
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtNombre.clear();
        txtContacto.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtSitioWeb.clear();
        repartidorSeleccionado = null;
        tablaRepartidores.getSelectionModel().clearSelection();
        labelMensaje.setText("Formulario limpio.");
        labelMensaje.setStyle("-fx-text-fill: gray;");
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista Repartidores.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo volver al Dashboard.");
            Logger.exception("Error al volver al dashboard desde RepartidorController", e);
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

    private void filtrarRepartidores(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            tablaRepartidores.setItems(listaRepartidores);
            labelMensaje.setText("Mostrando todos los repartidores.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
            return;
        }

        String texto = filtro.toLowerCase();

        ObservableList<Repartidor> filtradas = listaRepartidores.filtered(r
                -> (r.getNombre() != null && r.getNombre().toLowerCase().contains(texto))
                || (r.getContacto() != null && r.getContacto().toLowerCase().contains(texto))
                || (r.getTelefono() != null && r.getTelefono().toLowerCase().contains(texto))
                || (r.getEmail() != null && r.getEmail().toLowerCase().contains(texto))
                || (r.getDireccion() != null && r.getDireccion().toLowerCase().contains(texto))
                || (r.getSitioWeb() != null && r.getSitioWeb().toLowerCase().contains(texto))
        );

        tablaRepartidores.setItems(filtradas);

        if (filtradas.isEmpty()) {
            labelMensaje.setText("No se encontraron repartidores para: \"" + filtro + "\"");
            labelMensaje.setStyle("-fx-text-fill: red;");
        } else {
            labelMensaje.setText("Coincidencias encontradas: " + filtradas.size());
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }
}
