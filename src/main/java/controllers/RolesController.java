package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import dao.RolDAO;
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
import javafx.scene.layout.AnchorPane;
import models.Rol;
import models.Usuario;

public class RolesController implements Initializable {

    @FXML
    private AnchorPane rolesRoot;
    @FXML
    private Label labelTituloRoles;
    @FXML
    private Label labelUsuarioActivo;
    @FXML
    private TextField txtNombreRol;
    @FXML
    private TextField txtDescripcionRol;
    @FXML
    private TextField txtBuscarRol;
    @FXML
    private TableView<Rol> tablaRoles;
    @FXML
    private TableColumn<Rol, Integer> colIdRol;
    @FXML
    private TableColumn<Rol, String> colNombreRol;
    @FXML
    private TableColumn<Rol, String> colDescripcionRol;
    @FXML
    private Label labelMensajeRol;
    @FXML
    private Button btnAgregarRol;
    @FXML
    private Button btnActualizarRol;
    @FXML
    private Button btnEliminarRol;
    @FXML
    private Button btnLimpiarRol;
    @FXML
    private Button btnVolverRol;

    private final RolDAO rolDAO = new RolDAO();
    private ObservableList<Rol> listaRoles = FXCollections.observableArrayList();
    private Rol rolSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarRoles();
        mostrarUsuarioActivo();
        configurarEventosTabla();
        configurarBusqueda();
    }

    // ============================================================
    // CONFIGURACIÓN DE COLUMNAS Y TABLA
    // ============================================================
    private void configurarColumnas() {
        colIdRol.setCellValueFactory(new PropertyValueFactory<>("idRol"));
        colNombreRol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcionRol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
    }

    private void configurarEventosTabla() {
        tablaRoles.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                rolSeleccionado = newSel;
                txtNombreRol.setText(newSel.getNombre());
                txtDescripcionRol.setText(newSel.getDescripcion());
            }
        });
    }

    // ============================================================
    // SESIÓN ACTIVA
    // ============================================================
    private void mostrarUsuarioActivo() {
        Usuario usuario = Session.getUsuarioActual();
        if (usuario != null) {
            labelUsuarioActivo.setText(usuario.getNombre());
            Logger.info("RolesController -> Sesión activa: " + usuario.getNombre());
        } else {
            labelUsuarioActivo.setText("Invitado");
            labelMensajeRol.setText("No hay sesión activa. Algunas funciones estarán limitadas.");
            labelMensajeRol.setStyle("-fx-text-fill: orange;");
        }
    }

    // ============================================================
    // CARGA DE DATOS
    // ============================================================
    private void cargarRoles() {
        List<Rol> roles = rolDAO.listarRoles();
        listaRoles.setAll(roles);
        tablaRoles.setItems(listaRoles);

        if (roles.isEmpty()) {
            labelMensajeRol.setText("No hay roles registrados.");
            labelMensajeRol.setStyle("-fx-text-fill: gray;");
        } else {
            labelMensajeRol.setText("Roles cargados correctamente (" + roles.size() + ").");
            labelMensajeRol.setStyle("-fx-text-fill: green;");
        }
    }

    // ============================================================
    // CRUD DE ROLES
    // ============================================================
    @FXML
    private void agregarRol(ActionEvent event) {
        String nombre = txtNombreRol.getText().trim();
        String descripcion = txtDescripcionRol.getText().trim();
        Usuario usuario = Session.getUsuarioActual();

        if (nombre.isEmpty()) {
            mostrarMensaje("El nombre del rol es obligatorio.", "orange");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar creación");
        confirm.setHeaderText("¿Deseas agregar este rol?");
        confirm.setContentText("Nombre: " + nombre + "\nDescripción: " + descripcion);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                Rol nuevo = new Rol(0, nombre, descripcion);
                String responsable = (usuario != null) ? usuario.getNombre() : "Sistema";
                boolean exito = rolDAO.crearRol(nuevo, responsable);

                if (exito) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Rol agregado", "El rol se agregó correctamente.");
                    cargarRoles();
                    limpiarFormulario(null);
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agregar el rol.");
                }
            }
        });
    }

    @FXML
    private void actualizarRol(ActionEvent event) {
        if (rolSeleccionado == null) {
            mostrarMensaje("Selecciona un rol para actualizar.", "orange");
            return;
        }

        String nombre = txtNombreRol.getText().trim();
        String descripcion = txtDescripcionRol.getText().trim();
        Usuario usuario = Session.getUsuarioActual();

        if (nombre.isEmpty()) {
            mostrarMensaje("El nombre del rol no puede estar vacío.", "orange");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar actualización");
        confirm.setHeaderText("¿Deseas actualizar el rol seleccionado?");
        confirm.setContentText("Nombre nuevo: " + nombre + "\nDescripción nueva: " + descripcion);

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                rolSeleccionado.setNombre(nombre);
                rolSeleccionado.setDescripcion(descripcion);

                String responsable = (usuario != null) ? usuario.getNombre() : "Sistema";
                boolean actualizado = rolDAO.actualizarRol(rolSeleccionado, responsable);

                if (actualizado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Rol actualizado", "El rol se actualizó correctamente.");
                    cargarRoles();
                    limpiarFormulario(null);
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el rol.");
                }
            }
        });
    }

    @FXML
    private void eliminarRol(ActionEvent event) {
        Rol seleccionado = tablaRoles.getSelectionModel().getSelectedItem();
        Usuario usuario = Session.getUsuarioActual();

        if (seleccionado == null) {
            mostrarMensaje("Selecciona un rol para eliminar.", "orange");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Deseas eliminar el rol seleccionado?");
        confirm.setContentText("Rol: " + seleccionado.getNombre() + "\nEsta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String responsable = (usuario != null) ? usuario.getNombre() : "Sistema";
                boolean eliminado = rolDAO.eliminarRol(seleccionado.getIdRol(), responsable);

                if (eliminado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Rol eliminado", "El rol se eliminó correctamente.");
                    cargarRoles();
                    limpiarFormulario(null);
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el rol.");
                }
            }
        });
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtNombreRol.clear();
        txtDescripcionRol.clear();
        txtBuscarRol.clear();
        tablaRoles.getSelectionModel().clearSelection();
        rolSeleccionado = null;
        mostrarMensaje("Formulario limpio.", "gray");
    }

    // ============================================================
    // BÚSQUEDA
    // ============================================================
    private void configurarBusqueda() {
        txtBuscarRol.textProperty().addListener((obs, oldVal, newVal) -> filtrarRoles(newVal));
    }

    private void filtrarRoles(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            tablaRoles.setItems(listaRoles);
            mostrarMensaje("Mostrando todos los roles.", "gray");
            return;
        }

        String texto = filtro.toLowerCase();
        ObservableList<Rol> filtrados = listaRoles.filtered(r
                -> (r.getNombre() != null && r.getNombre().toLowerCase().contains(texto))
                || (r.getDescripcion() != null && r.getDescripcion().toLowerCase().contains(texto))
        );
        tablaRoles.setItems(filtrados);

        if (filtrados.isEmpty()) {
            mostrarMensaje("No se encontraron coincidencias para \"" + filtro + "\"", "red");
        } else {
            mostrarMensaje("Coincidencias encontradas: " + filtrados.size(), "green");
        }
    }

    // ============================================================
    // NAVEGACIÓN
    // ============================================================
    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al Dashboard desde Roles.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo volver al Dashboard.");
            Logger.exception("Error al volver al Dashboard desde RolesController", e);
        }
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    private void mostrarMensaje(String mensaje, String color) {
        labelMensajeRol.setText(mensaje);
        labelMensajeRol.setStyle("-fx-text-fill: " + color + ";");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
