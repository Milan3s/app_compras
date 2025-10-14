package controllers;

import app_compras.App;
import config.ConexionDB;
import config.Logger;
import config.Session;
import dao.UsuarioDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import models.Usuario;

/**
 * Controlador de la vista de registro de usuario.
 * Gestiona la creación de nuevas cuentas en la base de datos.
 *
 * @author Milanes
 */
public class RegistroController implements Initializable {

    @FXML
    private AnchorPane registroRoot;
    @FXML
    private Label labelTituloRegistro;
    @FXML
    private Label labelSubtituloRegistro;
    @FXML
    private Separator registroSeparator;
    @FXML
    private Label labelConexionRegistro;
    @FXML
    private Label labelNombreRegistro;
    @FXML
    private TextField txtNombreRegistro;
    @FXML
    private Label labelCorreoRegistro;
    @FXML
    private TextField txtEmailRegistro;
    @FXML
    private Label labelPasswordRegistro;
    @FXML
    private PasswordField txtPasswordRegistro;
    @FXML
    private Label labelConfirmarRegistro;
    @FXML
    private PasswordField txtConfirmarRegistro;
    @FXML
    private Button btnRegistrarRegistro;
    @FXML
    private Button btnVolverRegistro;
    @FXML
    private Label labelMensajeRegistro;
    @FXML
    private Label labelTitulo1Registro;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ID del rol "usuario" por defecto en la base de datos
    private static final int ROL_POR_DEFECTO = 2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        verificarConexion();
    }

    /**
     * Verifica si la conexión con la base de datos está activa.
     */
    private void verificarConexion() {
        try (Connection conn = ConexionDB.getConexion()) {
            if (conn != null && !conn.isClosed()) {
                labelConexionRegistro.setText("Conexión OK");
                labelConexionRegistro.setStyle("-fx-text-fill: green;");
                Logger.info("Conexión establecida correctamente en la vista de registro.");
            } else {
                labelConexionRegistro.setText("Conexión KO");
                labelConexionRegistro.setStyle("-fx-text-fill: red;");
                Logger.warning("No se pudo establecer conexión con la base de datos (registro).");
            }
        } catch (Exception e) {
            labelConexionRegistro.setText("Conexión KO");
            labelConexionRegistro.setStyle("-fx-text-fill: red;");
            Logger.exception("Error comprobando conexión inicial (registro).", e);
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     */
    @FXML
    private void registrarUsuario(ActionEvent event) {
        String nombre = txtNombreRegistro.getText().trim();
        String email = txtEmailRegistro.getText().trim();
        String password = txtPasswordRegistro.getText().trim();
        String confirmar = txtConfirmarRegistro.getText().trim();

        // Validaciones básicas
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            mostrarMensaje("Por favor, completa todos los campos.", "red");
            return;
        }

        if (!password.equals(confirmar)) {
            mostrarMensaje("Las contraseñas no coinciden.", "red");
            return;
        }

        try {
            // Verificar si el correo ya está registrado
            if (usuarioDAO.existeEmail(email)) {
                mostrarMensaje("El correo ya está registrado.", "red");
                Logger.warning("Intento de registro duplicado para: " + email);
                return;
            }

            // Encriptar la contraseña antes de guardarla
            String passwordEncriptada = Session.generarMD5(password);

            // Crear nuevo usuario con rol por defecto y sin permisos
            Usuario nuevoUsuario = new Usuario(nombre, email, passwordEncriptada, ROL_POR_DEFECTO);

            boolean creado = usuarioDAO.crearUsuario(nuevoUsuario);

            if (creado) {
                mostrarMensaje("Usuario registrado correctamente.", "green");
                Logger.info("Usuario registrado: " + nombre + " (" + email + ")");
                limpiarCampos();
            } else {
                mostrarMensaje("No se pudo registrar el usuario.", "red");
                Logger.warning("Error al registrar usuario: " + email);
            }

        } catch (Exception e) {
            mostrarMensaje("Error al registrar el usuario.", "red");
            Logger.exception("Error durante el registro del usuario: " + email, e);
        }
    }

    /**
     * Limpia los campos del formulario.
     */
    private void limpiarCampos() {
        txtNombreRegistro.clear();
        txtEmailRegistro.clear();
        txtPasswordRegistro.clear();
        txtConfirmarRegistro.clear();
    }

    /**
     * Muestra un mensaje en pantalla con color.
     */
    private void mostrarMensaje(String mensaje, String color) {
        labelMensajeRegistro.setText(mensaje);
        labelMensajeRegistro.setStyle("-fx-text-fill: " + color + ";");
    }

    /**
     * Vuelve a la pantalla de login.
     */
    @FXML
    private void volverLogin(ActionEvent event) {
        try {
            App.setRoot("views/login");
            Logger.info("Regreso a pantalla de login desde registro.");
        } catch (IOException e) {
            mostrarMensaje("Error al volver al login.", "red");
            Logger.exception("Error al regresar al login desde registro.", e);
        }
    }
}
