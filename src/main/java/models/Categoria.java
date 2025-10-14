package models;

/**
 * Modelo que representa una categoría de productos dentro del sistema.
 * Cada categoría puede agrupar varios productos.
 *
 * @author Milanes
 */
public class Categoria {

    private int idCategoria;
    private String nombre;
    private String descripcion;

    /** Constructor vacío (requerido por JavaFX y DAO) */
    public Categoria() {
    }

    /** Constructor con todos los campos */
    public Categoria(int idCategoria, String nombre, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Constructor sin ID (para insertar nuevas categorías) */
    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // --- Getters y Setters ---
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Sobrescritura del método toString para mostrar nombres en ComboBox */
    @Override
    public String toString() {
        return nombre;
    }
}
