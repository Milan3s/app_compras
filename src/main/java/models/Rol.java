package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa un rol dentro del sistema. Tabla: roles
 *
 * Cada rol puede tener múltiples permisos asociados, gestionados desde la tabla
 * intermedia roles_permisos.
 *
 * @author Milanes
 */
public class Rol {

    private int idRol;
    private String nombre;
    private String descripcion;
    private List<Permiso> permisos; // 🔗 Permisos asociados a este rol

    // Constructor vacío
    public Rol() {
        this.permisos = new ArrayList<>();
    }

    // Constructor con parámetros
    public Rol(int idRol, String nombre, String descripcion) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = new ArrayList<>();
    }

    // Constructor extendido con permisos
    public Rol(int idRol, String nombre, String descripcion, List<Permiso> permisos) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = permisos != null ? permisos : new ArrayList<>();
    }

    // Getters y setters
    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
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

    public List<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos;
    }

    /**
     * Agrega un permiso a la lista de este rol si no está duplicado.
     */
    public void agregarPermiso(Permiso permiso) {
        if (permiso != null && !permisos.contains(permiso)) {
            permisos.add(permiso);
        }
    }

    /**
     * Elimina un permiso de la lista de este rol.
     */
    public void eliminarPermiso(Permiso permiso) {
        if (permiso != null) {
            permisos.remove(permiso);
        }
    }

    /**
     * Comprueba si el rol tiene un permiso específico (por nombre).
     */
    public boolean tienePermiso(String nombrePermiso) {
        if (nombrePermiso == null) {
            return false;
        }
        return permisos.stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombrePermiso));
    }

    @Override
    public String toString() {
        return nombre != null ? nombre : "Sin nombre";
    }

}
