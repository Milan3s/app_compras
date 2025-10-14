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
 * Controlador del Login de la aplicación. Valida el inicio de sesión usando el
 * nombre de usuario y contraseña (MD5).
 *
 * @author Milanes
 */
public class LoginController implements Initializable {

    @FXML
    private AnchorPane loginRoot;
    @FXML
    private Label labelTitulo;
    @FXML
    private Label labelSubtitulo;
    @FXML
    private Separator loginSeparator;
    @FXML
    private Label labelConexion;
    @FXML
    private Label labelUsuario;
    @FXML
    private TextField txtNombre; // ✅ Cambiado: login con nombre de usuario
    @FXML
    private Label labelPassword;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRegistro;
    @FXML
    private Label labelMensaje;
    @FXML
    private Label labelTitulo1;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        verificarConexion();

        // Evita redimensionar o maximizar la ventana
        loginRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                    if (newWin != null) {
                        javafx.stage.Stage stage = (javafx.stage.Stage) newWin;
                        stage.setResizable(false);
                        stage.setMaximized(false);
                    }
                });
            }
        });
    }

    // ============================================================
    // CONEXIÓN
    // ============================================================
    private void verificarConexion() {
        try (Connection conn = ConexionDB.getConexion()) {
            if (conn != null && !conn.isClosed()) {
                labelConexion.setText("Conexión OK");
                labelConexion.setStyle("-fx-text-fill: green;");
                labelConexion.setAlignment(javafx.geometry.Pos.CENTER);
                Logger.info("Conexión establecida correctamente al iniciar la vista de login.");
            } else {
                labelConexion.setText("Conexión KO");
                labelConexion.setStyle("-fx-text-fill: red;");
                labelConexion.setAlignment(javafx.geometry.Pos.CENTER);
                labelMensaje.setText("Error al conectar con la base de datos.");
                labelMensaje.setStyle("-fx-text-fill: red;");
                Logger.warning("Conexión KO al iniciar la vista de login.");
            }
        } catch (Exception e) {
            labelConexion.setText("Conexión KO");
            labelConexion.setStyle("-fx-text-fill: red;");
            labelConexion.setAlignment(javafx.geometry.Pos.CENTER);
            labelMensaje.setText("Error al intentar verificar la conexión.");
            labelMensaje.setStyle("-fx-text-fill: red;");
            Logger.exception("Error comprobando la conexión inicial (LoginController).", e);
        }
    }

    // ============================================================
    // LOGIN (AUTENTICACIÓN POR NOMBRE)
    // ============================================================
    @FXML
    private void loginUsuario(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String password = txtPassword.getText().trim();

        if (nombre.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor, completa todos los campos.", "red");
            return;
        }

        try {
            // Encriptar la contraseña con MD5 antes de compararla
            String passwordEncriptada = Session.generarMD5(password);

            // Buscar el usuario por nombre (NO email)
            Usuario usuario = usuarioDAO.autenticarUsuario(nombre, passwordEncriptada);

            if (usuario != null) {
                Session.iniciarSesion(usuario);
                mostrarMensaje("Bienvenido, " + usuario.getNombre() + "!", "green");
                Logger.info("Inicio de sesión exitoso del usuario: " + usuario.getNombre());

                App.setRoot("views/dashboard");
            } else {
                mostrarMensaje("Usuario o contraseña incorrectos.", "red");
                Logger.warning("Intento de inicio de sesión fallido para: " + nombre);
            }

        } catch (Exception e) {
            mostrarMensaje("Error durante el inicio de sesión.", "red");
            Logger.exception("Error al intentar iniciar sesión para el usuario: " + nombre, e);
        }
    }

    // ============================================================
    // CAMBIO A VISTA DE REGISTRO
    // ============================================================
    @FXML
    private void irARegistro(ActionEvent event) {
        try {
            App.setRoot("views/registro");
            Logger.info("Cambio a vista de registro realizado correctamente.");
        } catch (IOException e) {
            mostrarMensaje("Error al abrir la ventana de registro.", "red");
            Logger.exception("Error al cambiar a la vista de registro.", e);
        }
    }

    // ============================================================
    // UTILIDAD: MOSTRAR MENSAJES
    // ============================================================
    private void mostrarMensaje(String mensaje, String color) {
        labelMensaje.setText(mensaje);
        labelMensaje.setStyle("-fx-text-fill: " + color + ";");
    }
}
