package models;

/**
 * Modelo que representa un permiso del sistema. Tabla: permisos
 *
 * Cada permiso pertenece a un área (por ejemplo: "Compras", "Productos",
 * "Reportes").
 *
 * @author Milanes
 */
public class Permiso {

    private int idPermiso;
    private String nombre;
    private String descripcion;
    private String area;  // Agrupación del permiso (más legible)

    // ============================================================
    // CONSTRUCTORES
    // ============================================================
    public Permiso() {
    }

    public Permiso(int idPermiso, String nombre, String descripcion, String area) {
        this.idPermiso = idPermiso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.area = area;
    }

    public Permiso(int idPermiso, String nombre) {
        this.idPermiso = idPermiso;
        this.nombre = nombre;
    }

    // ============================================================
    // GETTERS Y SETTERS
    // ============================================================
    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================
    @Override
    public String toString() {
        // ✅ Muestra solo el área (más legible en la interfaz)
        return (area != null && !area.isBlank()) ? area : nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Permiso)) return false;
        Permiso other = (Permiso) obj;
        return this.idPermiso == other.idPermiso;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idPermiso);
    }
}
