package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import dao.RolDAO;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import models.Permiso;
import models.Rol;
import models.Usuario;

public class DashboardController implements Initializable {

    @FXML
    private BorderPane dashboardRoot;
    @FXML
    private VBox contentArea;
    @FXML
    private Label lblBienvenida;
    @FXML
    private Label dashboardTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private Button btnLogout;

    // === Botones ===
    @FXML
    private Button btnHacerCompra, btnListadoDeCompras;
    @FXML
    private Button btnGestionTiendas, btnGestionProductos, btnGestionCategorias;
    @FXML
    private Button btnGestionUsuarios, btnGestionRoles, btnGestionRepartidores;
    @FXML
    private Button btnGastosPorDia, btnGastosPorSemana, btnGastosPorMes, btnGastosAnual;

    // === Secciones ===
    @FXML
    private Label labelCompras, labelInventario, labelAdministracion, labelGastos;
    @FXML
    private Separator sepCompras, sepInventario, sepAdministracion, sepGastos, sepHeader, sepFooter;
    @FXML
    private GridPane panelIzquierdo, panelDerecho;
    @FXML
    private HBox mainContent;

    private final RolDAO rolDAO = new RolDAO();
    private final Map<Button, ButtonState> estadoOriginal = new HashMap<>();
    private double posicionOriginalLogoutY = -1;

    private static class ButtonState {

        String texto;
        ImageView icono;

        ButtonState(String texto, ImageView icono) {
            this.texto = texto;
            this.icono = icono;
        }
    }

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.haySesionActiva()) {
            lblBienvenida.setText("No hay sesión activa.");
            bloquearTodo();
            btnLogout.setVisible(true);
            return;
        }

        Platform.runLater(() -> {
            if (posicionOriginalLogoutY == -1) {
                posicionOriginalLogoutY = btnLogout.getLayoutY();
            }
        });

        try {
            Usuario usuario = Session.getUsuarioActual();
            lblBienvenida.setText("Bienvenido, " + usuario.getNombre());

            Rol rolUsuario = rolDAO.obtenerRolPorId(usuario.getIdRol());
            if (rolUsuario == null) {
                Logger.warning("Usuario sin rol asignado. Bloqueando todo.");
                bloquearTodo();
                btnLogout.setVisible(true);
                return;
            }

            Logger.info("Rol activo: " + rolUsuario.getNombre());

            Set<Permiso> permisosEfectivos = new HashSet<>();
            if (usuario.getPermisos() != null && !usuario.getPermisos().isEmpty()) {
                permisosEfectivos.addAll(usuario.getPermisos());
                Logger.info("Permisos personalizados aplicados para el usuario: " + usuario.getNombre());
            } else if (rolUsuario.getPermisos() != null && !rolUsuario.getPermisos().isEmpty()) {
                permisosEfectivos.addAll(rolUsuario.getPermisos());
                Logger.info("Permisos de rol aplicados para el usuario: " + usuario.getNombre());
            } else {
                Logger.warning("Usuario sin permisos ni por rol ni individuales: " + usuario.getNombre());
            }

            aplicarPermisosDesdeBD(permisosEfectivos);
            mostrarTodasLasSecciones(); // 🔹 se asegura de que los títulos y separadores SIEMPRE se vean
            restaurarPosicionLogout();
            btnLogout.setVisible(true);

        } catch (Exception e) {
            Logger.exception("Error inicializando Dashboard dinámico.", e);
            bloquearTodo();
            btnLogout.setVisible(true);
        }
    }

    // ============================================================
    // APLICAR PERMISOS DESDE BD
    // ============================================================
    private void aplicarPermisosDesdeBD(Set<Permiso> permisosEfectivos) {
        bloquearTodo();

        if (permisosEfectivos == null || permisosEfectivos.isEmpty()) {
            Logger.warning("El usuario o su rol no tienen permisos asignados.");
            return;
        }

        Set<String> nombresPermitidos = new HashSet<>();
        for (Permiso p : permisosEfectivos) {
            if (p.getNombre() != null) {
                nombresPermitidos.add(p.getNombre().trim());
            }
        }

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType() == Button.class) {
                field.setAccessible(true);
                try {
                    Button btn = (Button) field.get(this);
                    if (btn == null) {
                        continue;
                    }

                    if (btn == btnLogout) {
                        restaurarBoton(btn);
                        continue;
                    }

                    if (nombresPermitidos.contains(field.getName())) {
                        restaurarBoton(btn);
                        Logger.info("Permiso activo -> " + field.getName());
                    } else {
                        bloquearBoton(btn);
                    }

                } catch (Exception e) {
                    Logger.exception("Error procesando botón: " + field.getName(), e);
                }
            }
        }
    }

    // ============================================================
    // BLOQUEAR / RESTAURAR BOTONES
    // ============================================================
    private void bloquearTodo() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getType() == Button.class) {
                    field.setAccessible(true);
                    Button btn = (Button) field.get(this);
                    if (btn != null && btn != btnLogout) {
                        bloquearBoton(btn);
                    }
                }
            }
        } catch (Exception e) {
            Logger.exception("Error bloqueando elementos del dashboard.", e);
        }
    }

    private void bloquearBoton(Button btn) {
        try {
            if (!estadoOriginal.containsKey(btn)) {
                ImageView originalIcon = btn.getGraphic() instanceof ImageView ? (ImageView) btn.getGraphic() : null;
                estadoOriginal.put(btn, new ButtonState(btn.getText(), originalIcon));
            }

            btn.setDisable(true);
            btn.setOpacity(0.6);
            btn.setTextFill(Color.DARKRED);
            btn.setText("Sin permiso");

            ImageView icon = new ImageView(
                    new Image(getClass().getResourceAsStream("/iconos/area-restringida.png"))
            );
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            btn.setGraphic(icon);

            Tooltip tooltip = new Tooltip("No tienes permiso para acceder a esta función.");
            tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #333; -fx-text-fill: white;");
            btn.setTooltip(tooltip);

        } catch (Exception e) {
            Logger.warning("No se pudo bloquear correctamente el botón: " + btn.getText());
        }
    }

    private void restaurarBoton(Button btn) {
        btn.setDisable(false);
        btn.setOpacity(1.0);
        btn.setTextFill(Color.BLACK);
        btn.setTooltip(null);

        ButtonState original = estadoOriginal.get(btn);
        if (original != null) {
            btn.setText(original.texto);
            btn.setGraphic(original.icono);
        }
    }

    // ============================================================
    // VISIBILIDAD DE SECCIONES
    // ============================================================
    private void mostrarTodasLasSecciones() {
        toggleSeccion(labelCompras, sepCompras, true);
        toggleSeccion(labelInventario, sepInventario, true);
        toggleSeccion(labelAdministracion, sepAdministracion, true);
        toggleSeccion(labelGastos, sepGastos, true);
    }

    private void toggleSeccion(Label label, Separator sep, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        sep.setVisible(visible);
        sep.setManaged(visible);
    }

    private void restaurarPosicionLogout() {
        Platform.runLater(() -> {
            if (posicionOriginalLogoutY >= 0) {
                btnLogout.setLayoutY(posicionOriginalLogoutY);
            }
        });
    }

    // ============================================================
    // NAVEGACIÓN ENTRE VISTAS
    // ============================================================
    private void abrirVista(String vista, String nombre) {
        try {
            if (Session.haySesionActiva()) {
                App.setRoot(vista);
                Logger.info("Usuario abrió la vista de " + nombre);
            } else {
                Logger.warning("Intento de acceder a " + nombre + " sin sesión activa.");
            }
        } catch (IOException e) {
            Logger.exception("Error abriendo vista " + nombre, e);
        }
    }

    // ============================================================
    // ACCIONES DE BOTONES
    // ============================================================
    @FXML
    private void accion_hacer_compra(ActionEvent e) {
        abrirVista("views/carrito", "compras");
    }

    @FXML
    private void accion_listado_de_compras(ActionEvent e) {
        abrirVista("views/listado_de_compras", "listado de compras");
    }

    @FXML
    private void accion_gestion_de_tiendas(ActionEvent e) {
        abrirVista("views/tienda", "tiendas");
    }

    @FXML
    private void accion_gestion_de_productos(ActionEvent e) {
        abrirVista("views/productos", "productos");
    }

    @FXML
    private void accion_gestion_de_categorias(ActionEvent e) {
        abrirVista("views/categorias", "categorías");
    }

    @FXML
    private void accion_gestion_de_repartidores(ActionEvent e) {
        abrirVista("views/repartidores", "repartidores");
    }

    @FXML
    private void accion_gestion_de_usuarios(ActionEvent e) {
        abrirVista("views/usuarios", "usuarios");
    }

    @FXML
    private void accion_gestion_de_roles(ActionEvent e) {
        abrirVista("views/roles", "roles");
    }

    @FXML
    private void accion_gastos_por_dia(ActionEvent e) {
        abrirVista("views/gastos_por_dia", "gastos diarios");
    }

    @FXML
    private void accion_gastos_por_semana(ActionEvent e) {
        abrirVista("views/gastos_por_semana", "gastos semanales");
    }

    private void accion_gastos_por_mes(ActionEvent e) {
        abrirVista("views/gastos_por_mes", "resumen mensual");
    }

    private void accion_gastos_anual(ActionEvent e) {
        abrirVista("views/gastos_anuales", "gastos anuales");
    }

    @FXML
    private void accion_resumen_mensual(ActionEvent e) {
        abrirVista("views/gastos_por_mes", "resumen mensual");
    }

    @FXML
    private void accion_gasto_anual(ActionEvent e) {
        abrirVista("views/gastos_anuales", "gastos anuales");
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    @FXML
    private void accion_logout(ActionEvent event) {
        try {
            if (Session.haySesionActiva()) {
                Logger.info("Cierre de sesión de " + Session.getUsuarioActual().getNombre());
            }
            Session.cerrarSesion();
            App.setRoot("views/login");
            Logger.info("Sesión cerrada. Volviendo al inicio de sesión.");
        } catch (IOException e) {
            Logger.exception("Error al volver a login.", e);
        }
    }
}
