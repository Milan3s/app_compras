package dao;

import config.ConexionDB;
import config.Logger;
import models.Rol;
import models.Permiso;
import models.HistorialRol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de roles en la base de datos.
 * Compatible con la tabla 'roles' que usa el campo 'descripcion_rol'.
 *
 * @author Milanes
 */
public class RolDAO {

    private final PermisoDAO permisoDAO = new PermisoDAO();
    private final HistorialRolDAO historialDAO = new HistorialRolDAO();

    // ============================================================
    // CREAR ROL
    // ============================================================
    public boolean crearRol(Rol rol, String usuarioResponsable) {
        String sql = "INSERT INTO roles (nombre, descripcion_rol) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getDescripcion());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    rol.setIdRol(rs.getInt(1));
                }

                Logger.info("Rol creado correctamente: " + rol.getNombre());
                historialDAO.registrarAccion(
                        new HistorialRol(rol.getIdRol(), "CREAR", usuarioResponsable)
                );
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al crear rol: " + rol.getNombre(), e);
        }
        return false;
    }

    // ============================================================
    // OBTENER ROL POR ID
    // ============================================================
    public Rol obtenerRolPorId(int idRol) {
        String sql = "SELECT * FROM roles WHERE id_rol = ?";
        Rol rol = null;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rol = mapearRol(rs);
                rol.setPermisos(permisoDAO.listarPorRol(idRol));
                Logger.info("Rol encontrado con ID: " + idRol);
            } else {
                Logger.warning("No se encontró rol con ID: " + idRol);
            }

        } catch (SQLException e) {
            Logger.exception("Error al obtener rol por ID.", e);
        }

        return rol;
    }

    // ============================================================
    // LISTAR TODOS LOS ROLES (corregido)
    // ============================================================
    public List<Rol> listarTodos() {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT id_rol, nombre, descripcion_rol FROM roles ORDER BY id_rol ASC";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Guardamos primero todos los roles
            List<Rol> rolesTemporales = new ArrayList<>();
            while (rs.next()) {
                Rol rol = mapearRol(rs);
                rolesTemporales.add(rol);
            }

            // Luego cargamos sus permisos con otra conexión
            try (Connection connPermisos = ConexionDB.getConexion()) {
                for (Rol rol : rolesTemporales) {
                    rol.setPermisos(permisoDAO.listarPorRol(rol.getIdRol(), connPermisos));
                    lista.add(rol);
                }
            }

            Logger.info("Se obtuvieron todos los roles correctamente. Total: " + lista.size());

        } catch (SQLException e) {
            Logger.exception("Error al listar todos los roles.", e);
        }

        return lista;
    }

    public List<Rol> listarRoles() {
        return listarTodos();
    }

    // ============================================================
    // ACTUALIZAR ROL
    // ============================================================
    public boolean actualizarRol(Rol rol, String usuarioResponsable) {
        String sql = "UPDATE roles SET nombre = ?, descripcion_rol = ? WHERE id_rol = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getDescripcion());
            ps.setInt(3, rol.getIdRol());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Rol actualizado correctamente: " + rol.getNombre());
                historialDAO.registrarAccion(
                        new HistorialRol(rol.getIdRol(), "MODIFICAR", usuarioResponsable)
                );
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al actualizar rol: " + rol.getNombre(), e);
        }

        return false;
    }

    // ============================================================
    // ELIMINAR ROL
    // ============================================================
    public boolean eliminarRol(int idRol, String usuarioResponsable) {
        String sql = "DELETE FROM roles WHERE id_rol = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                Logger.info("Rol eliminado correctamente con ID: " + idRol);
                historialDAO.registrarAccion(
                        new HistorialRol(idRol, "ELIMINAR", usuarioResponsable)
                );
                return true;
            } else {
                Logger.warning("No se encontró el rol con ID: " + idRol);
            }

        } catch (SQLException e) {
            Logger.exception("Error al eliminar rol con ID: " + idRol, e);
        }

        return false;
    }

    // ============================================================
    // OBTENER ROL POR NOMBRE
    // ============================================================
    public Rol obtenerRolPorNombre(String nombre) {
        String sql = "SELECT * FROM roles WHERE nombre = ?";
        Rol rol = null;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                rol = mapearRol(rs);
                rol.setPermisos(permisoDAO.listarPorRol(rol.getIdRol()));
                Logger.info("Rol encontrado con nombre: " + nombre);
            } else {
                Logger.warning("No se encontró rol con nombre: " + nombre);
            }

        } catch (SQLException e) {
            Logger.exception("Error al buscar rol por nombre: " + nombre, e);
        }

        return rol;
    }

    // ============================================================
    // PERMISOS DEL ROL
    // ============================================================
    public List<Permiso> obtenerPermisosDelRol(int idRol) {
        return permisoDAO.listarPorRol(idRol);
    }

    // ============================================================
    // MAPEADOR
    // ============================================================
    private Rol mapearRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion_rol"));
        return rol;
    }
}
