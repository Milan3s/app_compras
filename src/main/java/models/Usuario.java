package models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo que representa un usuario del sistema. Tabla: usuarios
 *
 * @author Milanes
 */
public class Usuario {

    private int idUsuario;
    private String nombre;
    private String email;
    private String password;
    private int idRol;      // FK hacia la tabla roles

    private Rol rol;        // Asociación al objeto Rol
    private List<Permiso> permisos; // Asociación múltiple a permisos

    // ============================================================
    // CONSTRUCTORES
    // ============================================================
    public Usuario() {
        this.permisos = new ArrayList<>();
    }

    // Constructor para nuevos registros
    public Usuario(String nombre, String email, String password, int idRol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.idRol = idRol;
        this.permisos = new ArrayList<>();
    }

    // Constructor completo sin permisos
    public Usuario(int idUsuario, String nombre, String email, String password, int idRol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.idRol = idRol;
        this.permisos = new ArrayList<>();
    }

    // Constructor completo con permisos
    public Usuario(int idUsuario, String nombre, String email, String password, int idRol, List<Permiso> permisos) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.idRol = idRol;
        this.permisos = permisos != null ? permisos : new ArrayList<>();
    }

    // ============================================================
    // GETTERS Y SETTERS
    // ============================================================
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public List<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos;
    }

    // ============================================================
    // UTILIDADES
    // ============================================================
    @Override
    public String toString() {
        String rolStr = (rol != null) ? rol.getNombre() : "Sin rol";
        String permisosStr = (permisos != null && !permisos.isEmpty())
                ? String.join(", ", permisos.stream()
                        .map(Permiso::getNombre)
                        .collect(Collectors.toList()))
                : "Sin permisos";
        return nombre + " (" + rolStr + " - " + permisosStr + ")";
    }
}
