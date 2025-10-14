package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import dao.RolDAO;
import dao.UsuarioDAO;
import dao.PermisoDAO;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Rol;
import models.Usuario;
import models.Permiso;

public class UsuariosController implements Initializable {

    @FXML
    private AnchorPane usuariosRoot;
    @FXML
    private Label labelTituloUsuarios;
    @FXML
    private Label labelUsuarioActivo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private ComboBox<Rol> comboRol;
    @FXML
    private ListView<Permiso> listPermisos;
    @FXML
    private TextField txtBuscarUsuario;
    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, Integer> colId;
    @FXML
    private TableColumn<Usuario, String> colNombre;
    @FXML
    private TableColumn<Usuario, String> colEmail;
    @FXML
    private TableColumn<Usuario, String> colRol;
    @FXML
    private TableColumn<Usuario, String> colPermiso;
    @FXML
    private Label labelMensaje;
    @FXML
    private VBox headerUsuarios;
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

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final PermisoDAO permisoDAO = new PermisoDAO();

    private final ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();
    private final ObservableList<Rol> listaRoles = FXCollections.observableArrayList();
    private final ObservableList<Permiso> listaPermisos = FXCollections.observableArrayList();

    private Usuario usuarioSeleccionado = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarListView();
        cargarRoles();
        cargarPermisos();
        cargarUsuarios();
        configurarBusqueda();
        mostrarUsuarioActivo();
    }

    private void mostrarUsuarioActivo() {
        if (Session.getUsuarioActual() != null) {
            labelUsuarioActivo.setText(Session.getUsuarioActual().getNombre());
        } else {
            labelUsuarioActivo.setText("Invitado");
            labelMensaje.setText("No hay sesión activa.");
            labelMensaje.setStyle("-fx-text-fill: orange;");
        }
    }

    // ============================================================
    // CONFIGURAR TABLA Y LISTVIEW
    // ============================================================
    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colRol.setCellValueFactory(cellData
                -> new SimpleStringProperty(
                        cellData.getValue().getRol() != null
                        ? cellData.getValue().getRol().getNombre()
                        : "Sin rol"
                )
        );

        colPermiso.setCellValueFactory(cellData -> {
            List<Permiso> permisos = cellData.getValue().getPermisos();
            if (permisos == null || permisos.isEmpty()) {
                return new SimpleStringProperty("Sin permisos");
            }
            String permisosStr = permisos.stream()
                    .map(p -> (p.getArea() != null && !p.getArea().isBlank())
                    ? p.getArea()
                    : p.getNombre())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(permisosStr);
        });

        colPermiso.setCellFactory(column -> new TableCell<Usuario, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    tooltip.setText(item);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(350);
                    setTooltip(tooltip);
                }
            }
        });
    }

    private void configurarListView() {
        listPermisos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listPermisos.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Permiso permiso, boolean empty) {
                super.updateItem(permiso, empty);
                if (empty || permiso == null) {
                    setText(null);
                } else {
                    setText(permiso.getArea() + " - " + permiso.getNombre());
                }
            }
        });
    }

    // ============================================================
    // CARGA DE DATOS
    // ============================================================
    private void cargarRoles() {
        try {
            listaRoles.setAll(rolDAO.listarTodos());
            comboRol.setItems(listaRoles);
        } catch (Exception e) {
            Logger.exception("Error al cargar roles en UsuariosController", e);
        }
    }

    private void cargarPermisos() {
        try {
            listaPermisos.setAll(permisoDAO.listarTodos());
            listPermisos.setItems(listaPermisos);
        } catch (Exception e) {
            Logger.exception("Error al cargar permisos en UsuariosController", e);
        }
    }

    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioDAO.listarUsuariosConPermisos();
            usuarios.sort(Comparator.comparingInt(Usuario::getIdUsuario));
            listaUsuarios.setAll(usuarios);
            tablaUsuarios.setItems(listaUsuarios);

            if (usuarios.isEmpty()) {
                labelMensaje.setText("No hay usuarios registrados.");
                labelMensaje.setStyle("-fx-text-fill: gray;");
            } else {
                labelMensaje.setText("Usuarios cargados correctamente.");
                labelMensaje.setStyle("-fx-text-fill: green;");
            }

        } catch (Exception e) {
            labelMensaje.setText("Error al cargar usuarios.");
            labelMensaje.setStyle("-fx-text-fill: red;");
            Logger.exception("Error al cargar usuarios.", e);
        }

        tablaUsuarios.setOnMouseClicked(event -> {
            usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
            if (usuarioSeleccionado != null) {
                txtNombre.setText(usuarioSeleccionado.getNombre());
                txtEmail.setText(usuarioSeleccionado.getEmail());
                txtPassword.setText(usuarioSeleccionado.getPassword());
                comboRol.setValue(usuarioSeleccionado.getRol());

                listPermisos.getSelectionModel().clearSelection();
                if (usuarioSeleccionado.getPermisos() != null) {
                    for (Permiso p : usuarioSeleccionado.getPermisos()) {
                        listPermisos.getSelectionModel().select(p);
                    }
                }

                labelMensaje.setText("Usuario seleccionado: " + usuarioSeleccionado.getNombre());
                labelMensaje.setStyle("-fx-text-fill: #2c3e50;");
            }
        });
    }

    // ============================================================
    // BÚSQUEDA
    // ============================================================
    private void configurarBusqueda() {
        txtBuscarUsuario.textProperty().addListener((obs, oldValue, newValue) -> filtrarUsuarios(newValue));
    }

    private void filtrarUsuarios(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            tablaUsuarios.setItems(listaUsuarios);
            labelMensaje.setText("Mostrando todos los usuarios.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
            return;
        }

        String texto = filtro.toLowerCase();
        ObservableList<Usuario> filtrados = listaUsuarios.filtered(u
                -> (u.getNombre() != null && u.getNombre().toLowerCase().contains(texto))
                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(texto))
                || (u.getRol() != null && u.getRol().getNombre().toLowerCase().contains(texto))
                || (u.getPermisos() != null && u.getPermisos().stream()
                .anyMatch(p -> p.getNombre().toLowerCase().contains(texto)))
        );

        tablaUsuarios.setItems(filtrados);

        if (filtrados.isEmpty()) {
            labelMensaje.setText("No se encontraron usuarios para: \"" + filtro + "\"");
            labelMensaje.setStyle("-fx-text-fill: red;");
        } else {
            labelMensaje.setText("Coincidencias encontradas: " + filtrados.size());
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }

    // ============================================================
    // CRUD DE USUARIOS
    // ============================================================
    @FXML
    private void agregarUsuario(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        Rol rol = comboRol.getValue();
        List<Permiso> permisosSeleccionados = listPermisos.getSelectionModel().getSelectedItems();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || rol == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos",
                    "Debes llenar todos los campos y seleccionar un rol.");
            return;
        }

        if (usuarioDAO.existeEmail(email)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Duplicado",
                    "Ya existe un usuario con este email.");
            return;
        }

        Usuario nuevo = new Usuario(nombre, email, password, rol.getIdRol());
        nuevo.setPermisos(permisosSeleccionados);

        boolean creado = usuarioDAO.crearUsuarioConPermisos(nuevo);

        if (creado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Usuario agregado correctamente.");
            limpiarFormulario(null);
            cargarUsuarios();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agregar el usuario.");
        }
    }

    @FXML
    private void actualizarUsuario(ActionEvent event) {
        if (usuarioSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona un usuario para actualizar.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        Rol rol = comboRol.getValue();
        List<Permiso> permisosSeleccionados = listPermisos.getSelectionModel().getSelectedItems();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || rol == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Debes llenar todos los campos y seleccionar un rol.");
            return;
        }

        usuarioSeleccionado.setNombre(nombre);
        usuarioSeleccionado.setEmail(email);
        usuarioSeleccionado.setPassword(password);
        usuarioSeleccionado.setIdRol(rol.getIdRol());
        usuarioSeleccionado.setPermisos(permisosSeleccionados);

        boolean actualizado = usuarioDAO.actualizarUsuarioConPermisos(usuarioSeleccionado);

        if (actualizado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado", "Usuario actualizado correctamente.");
            limpiarFormulario(null);
            cargarUsuarios();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el usuario.");
        }
    }

    @FXML
    private void eliminarUsuario(ActionEvent event) {
        if (usuarioSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona un usuario para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Estás seguro de eliminar este usuario?");
        confirm.setContentText("Usuario: " + usuarioSeleccionado.getNombre());
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean eliminado = usuarioDAO.eliminarUsuario(usuarioSeleccionado.getIdUsuario());
                if (eliminado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Usuario eliminado correctamente.");
                    limpiarFormulario(null);
                    cargarUsuarios();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el usuario.");
                }
            }
        });
    }

    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtNombre.clear();
        txtEmail.clear();
        txtPassword.clear();
        comboRol.getSelectionModel().clearSelection();
        listPermisos.getSelectionModel().clearSelection();
        usuarioSeleccionado = null;
        tablaUsuarios.getSelectionModel().clearSelection();
        labelMensaje.setText("Formulario limpio.");
        labelMensaje.setStyle("-fx-text-fill: gray;");
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
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
            Logger.info("Usuario volvió al dashboard desde Usuarios.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo volver al Dashboard.");
            Logger.exception("Error al volver al dashboard desde UsuariosController", e);
        }
    }
}
