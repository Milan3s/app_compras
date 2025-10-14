package controllers;

import app_compras.App;
import config.Logger;
import config.Session;
import dao.CategoriaDAO;
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
import models.Categoria;
import models.Usuario;

/**
 * Controlador para la vista de Categorías (categorias.fxml). Gestiona el CRUD
 * completo de categorías y muestra el usuario activo.
 *
 * @author Milanes
 */
public class CategoriasController implements Initializable {

    @FXML
    private AnchorPane categoriasRoot;
    @FXML
    private Label labelTituloCategorias;
    @FXML
    private Label labelUsuarioActivo;
    @FXML
    private Label labelNombre;
    @FXML
    private TextField txtNombre;
    @FXML
    private Label labelDescripcion;
    @FXML
    private TextField txtDescripcion;
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
    private TextField txtBuscarCategoria;
    @FXML
    private TableView<Categoria> tablaCategorias;
    @FXML
    private TableColumn<Categoria, Integer> colId;
    @FXML
    private TableColumn<Categoria, String> colNombre;
    @FXML
    private TableColumn<Categoria, String> colDescripcion;
    @FXML
    private Label labelMensaje;
    @FXML
    private Button btnVolver;

    // --- DAO y datos ---
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private Categoria categoriaSeleccionada = null;
    @FXML
    private VBox headerCategorias;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarCategorias();
        configurarEventosTabla();

        // 🔍 Búsqueda en tiempo real
        txtBuscarCategoria.textProperty().addListener((obs, oldValue, newValue) -> {
            filtrarCategorias(newValue);
        });

        // 👤 Mostrar usuario activo
        Usuario usuario = Session.getUsuarioActual();
        if (usuario != null) {
            labelUsuarioActivo.setText(usuario.getNombre());
            Logger.info("Sesión activa en vista Categorías: " + usuario.getNombre());
        } else {
            labelUsuarioActivo.setText("Invitado");
            labelMensaje.setText("No hay sesión activa. Algunas funciones pueden estar limitadas.");
            labelMensaje.setStyle("-fx-text-fill: orange;");
            Logger.warning("Vista Categorías abierta sin sesión activa.");
        }
    }

    /**
     * Configura las columnas de la tabla.
     */
    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // ✅ Centrar todas las columnas correctamente
        colId.setCellFactory(tc -> {
            TableCell<Categoria, Integer> cell = new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

        colNombre.setCellFactory(tc -> {
            TableCell<Categoria, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

        colDescripcion.setCellFactory(tc -> {
            TableCell<Categoria, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });
    }

    /**
     * Carga todas las categorías desde la base de datos.
     */
    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.listarTodas();

        // Ordenar las categorías de menor a mayor por ID
        categorias.sort((c1, c2) -> Integer.compare(c1.getIdCategoria(), c2.getIdCategoria()));

        listaCategorias.setAll(categorias);
        tablaCategorias.setItems(listaCategorias);

        if (listaCategorias.isEmpty()) {
            labelMensaje.setText("No hay datos en la tabla.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
        } else {
            labelMensaje.setText("Datos cargados correctamente (" + categorias.size() + " categorí"
                    + (categorias.size() > 1 ? "as" : "a") + ").");
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }

    /**
     * Configura la selección de filas en la tabla.
     */
    private void configurarEventosTabla() {
        tablaCategorias.setOnMouseClicked((MouseEvent event) -> {
            if (tablaCategorias.getSelectionModel().getSelectedItem() != null) {
                categoriaSeleccionada = tablaCategorias.getSelectionModel().getSelectedItem();

                txtNombre.setText(categoriaSeleccionada.getNombre());
                txtDescripcion.setText(categoriaSeleccionada.getDescripcion());

                labelMensaje.setText("Categoría seleccionada: " + categoriaSeleccionada.getNombre());
                labelMensaje.setStyle("-fx-text-fill: #2c3e50;");
            }
        });
    }

    /**
     * Agregar una nueva categoría.
     */
    @FXML
    private void agregarCategoria(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos",
                    "Debe llenar todos los campos antes de agregar una categoría.");
            return;
        }

        Categoria nueva = new Categoria(nombre, descripcion);
        boolean insertado = categoriaDAO.insertar(nueva);

        if (insertado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría agregada correctamente.");
            limpiarFormulario(null);
            cargarCategorias();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agregar la categoría.");
        }
    }

    /**
     * Actualizar categoría seleccionada.
     */
    @FXML
    private void actualizarCategoria(ActionEvent event) {
        if (categoriaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Seleccione una categoría para actualizar.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos",
                    "Debe llenar todos los campos antes de actualizar.");
            return;
        }

        categoriaSeleccionada.setNombre(nombre);
        categoriaSeleccionada.setDescripcion(descripcion);

        boolean actualizado = categoriaDAO.actualizar(categoriaSeleccionada);
        if (actualizado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado", "Categoría actualizada correctamente.");
            limpiarFormulario(null);
            cargarCategorias();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la categoría.");
        }
    }

    /**
     * Eliminar categoría seleccionada.
     */
    @FXML
    private void eliminarCategoria(ActionEvent event) {
        if (categoriaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Seleccione una categoría para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Deseas eliminar esta categoría?");
        confirm.setContentText("Categoría: " + categoriaSeleccionada.getNombre());
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean eliminado = categoriaDAO.eliminar(categoriaSeleccionada.getIdCategoria());
                if (eliminado) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Categoría eliminada correctamente.");
                    limpiarFormulario(null);
                    cargarCategorias();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la categoría.");
                }
            }
        });
    }

    /**
     * Limpia los campos del formulario.
     */
    @FXML
    private void limpiarFormulario(ActionEvent event) {
        txtNombre.clear();
        txtDescripcion.clear();
        categoriaSeleccionada = null;
        tablaCategorias.getSelectionModel().clearSelection();
        labelMensaje.setText("Formulario limpio.");
        labelMensaje.setStyle("-fx-text-fill: gray;");
    }

    /**
     * Vuelve al dashboard principal.
     */
    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            App.setRoot("views/dashboard");
            Logger.info("Usuario volvió al dashboard desde vista Categorías.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo volver al Dashboard.");
            Logger.exception("Error al volver al dashboard desde CategoriasController", e);
        }
    }

    /**
     * Muestra alertas modales personalizadas.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Filtra las categorías según el texto de búsqueda.
     */
    private void filtrarCategorias(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            tablaCategorias.setItems(listaCategorias);
            labelMensaje.setText("Mostrando todas las categorías.");
            labelMensaje.setStyle("-fx-text-fill: gray;");
            return;
        }

        String texto = filtro.toLowerCase();

        ObservableList<Categoria> filtradas = listaCategorias.filtered(c
                -> (c.getNombre() != null && c.getNombre().toLowerCase().contains(texto))
                || (c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(texto))
                || String.valueOf(c.getIdCategoria()).contains(texto)
        );

        tablaCategorias.setItems(filtradas);

        if (filtradas.isEmpty()) {
            labelMensaje.setText("No se encontraron categorías para: \"" + filtro + "\"");
            labelMensaje.setStyle("-fx-text-fill: red;");
        } else {
            labelMensaje.setText("Coincidencias encontradas: " + filtradas.size());
            labelMensaje.setStyle("-fx-text-fill: green;");
        }
    }
}
