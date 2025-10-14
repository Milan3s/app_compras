package dao;

import config.ConexionDB;
import config.Logger;
import models.Permiso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de permisos con soporte para el campo 'area'. Incluye
 * operaciones CRUD y consultas asociadas a roles.
 *
 * @author Milanes
 */
public class PermisoDAO {

    // ============================================================
    // LISTAR TODOS LOS PERMISOS
    // ============================================================
    public List<Permiso> listarTodos() {
        List<Permiso> permisos = new ArrayList<>();

        String sql
                = "SELECT p.id_permiso, p.nombre, p.descripcion, p.area "
                + "FROM permisos p "
                + "ORDER BY p.area ASC, p.nombre ASC";

        try (Connection conn = ConexionDB.getConexion(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                permisos.add(mapear(rs));
            }

            Logger.info("Permisos cargados correctamente. Total: " + permisos.size());

        } catch (SQLException e) {
            Logger.exception("Error al listar todos los permisos.", e);
        }

        return permisos;
    }

    // ============================================================
    // LISTAR PERMISOS POR ROL (usa nueva conexión interna)
    // ============================================================
    public List<Permiso> listarPorRol(int idRol) {
        List<Permiso> permisos = new ArrayList<>();

        String sql
                = "SELECT p.id_permiso, p.nombre, p.descripcion, p.area "
                + "FROM permisos p "
                + "INNER JOIN roles_permisos rp ON rp.id_permiso = p.id_permiso "
                + "WHERE rp.id_rol = ? "
                + "ORDER BY p.area ASC, p.nombre ASC";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapear(rs));
                }
            }

            Logger.info("Permisos cargados para el rol ID " + idRol + ": " + permisos.size());
            for (Permiso p : permisos) {
                Logger.info("  - " + p.getNombre() + " (" + p.getArea() + ")");
            }

        } catch (SQLException e) {
            Logger.exception("Error al listar permisos por rol ID " + idRol, e);
        }

        return permisos;
    }

    // ============================================================
    // LISTAR PERMISOS POR ROL (usando conexión externa)
    // ============================================================
    public List<Permiso> listarPorRol(int idRol, Connection conn) {
        List<Permiso> permisos = new ArrayList<>();

        String sql
                = "SELECT p.id_permiso, p.nombre, p.descripcion, p.area "
                + "FROM permisos p "
                + "INNER JOIN roles_permisos rp ON p.id_permiso = rp.id_permiso "
                + "WHERE rp.id_rol = ? "
                + "ORDER BY p.area ASC, p.nombre ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            Logger.exception("Error al listar permisos para rol ID " + idRol + " (usando conexión externa)", e);
        }

        return permisos;
    }

    // ============================================================
    // OBTENER PERMISO POR ID
    // ============================================================
    public Permiso obtenerPorId(int idPermiso) {
        Permiso permiso = null;

        String sql
                = "SELECT p.id_permiso, p.nombre, p.descripcion, p.area "
                + "FROM permisos p "
                + "WHERE p.id_permiso = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPermiso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    permiso = mapear(rs);
                    Logger.info("Permiso encontrado con ID: " + idPermiso);
                } else {
                    Logger.warning("No se encontró permiso con ID: " + idPermiso);
                }
            }

        } catch (SQLException e) {
            Logger.exception("Error al obtener permiso por ID " + idPermiso, e);
        }

        return permiso;
    }

    // ============================================================
    // CREAR PERMISO
    // ============================================================
    public boolean crearPermiso(Permiso permiso) {
        if (permiso == null) {
            Logger.warning("Intento de crear permiso nulo.");
            return false;
        }

        String sql
                = "INSERT INTO permisos (nombre, descripcion, area) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, permiso.getNombre());
            ps.setString(2, permiso.getDescripcion());
            ps.setString(3, permiso.getArea());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        permiso.setIdPermiso(rs.getInt(1));
                    }
                }
                Logger.info("Permiso creado correctamente: " + permiso.getNombre());
                return true;
            } else {
                Logger.warning("No se insertó ningún registro al crear el permiso: " + permiso.getNombre());
            }

        } catch (SQLException e) {
            Logger.exception("Error al crear permiso: " + permiso.getNombre(), e);
        }

        return false;
    }

    // ============================================================
    // ACTUALIZAR PERMISO
    // ============================================================
    public boolean actualizarPermiso(Permiso permiso) {
        if (permiso == null) {
            Logger.warning("Se intentó actualizar un permiso nulo.");
            return false;
        }

        if (permiso.getIdPermiso() <= 0) {
            Logger.warning("El ID del permiso no es válido. No se ejecutará el UPDATE.");
            return false;
        }

        String sql
                = "UPDATE permisos "
                + "SET nombre = ?, descripcion = ?, area = ? "
                + "WHERE id_permiso = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, permiso.getNombre());
            ps.setString(2, permiso.getDescripcion());
            ps.setString(3, permiso.getArea());
            ps.setInt(4, permiso.getIdPermiso());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                Logger.info("Permiso actualizado correctamente (ID: " + permiso.getIdPermiso()
                        + ") -> " + permiso.getNombre());
                return true;
            } else {
                Logger.warning("No se actualizó ningún permiso. Verifica que exista el ID: " + permiso.getIdPermiso());
            }

        } catch (SQLException e) {
            Logger.exception("Error al actualizar permiso: " + permiso.getNombre(), e);
        }

        return false;
    }

    // ============================================================
    // ELIMINAR PERMISO
    // ============================================================
    public boolean eliminarPermiso(int idPermiso) {
        String sql = "DELETE FROM permisos WHERE id_permiso = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPermiso);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                Logger.info("Permiso eliminado con ID: " + idPermiso);
                return true;
            } else {
                Logger.warning("No se encontró permiso con ID: " + idPermiso);
            }

        } catch (SQLException e) {
            Logger.exception("Error al eliminar permiso con ID: " + idPermiso, e);
        }

        return false;
    }

    // ============================================================
    // VERIFICAR SI UN ROL TIENE UN PERMISO
    // ============================================================
    public boolean tienePermiso(int idRol, String nombrePermiso) {
        String sql
                = "SELECT COUNT(*) "
                + "FROM roles_permisos rp "
                + "INNER JOIN permisos p ON rp.id_permiso = p.id_permiso "
                + "WHERE rp.id_rol = ? AND p.nombre = ?";  // 👈 sin rp.activo

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.setString(2, nombrePermiso);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    Logger.info("Permiso '" + nombrePermiso + "' encontrado para rol " + idRol + " = " + (count > 0));
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            Logger.exception("Error al verificar si el rol tiene el permiso '" + nombrePermiso + "'", e);
        }

        return false;
    }

    // ============================================================
    // MAPEADOR DE RESULTSET A OBJETO PERMISO
    // ============================================================
    private Permiso mapear(ResultSet rs) throws SQLException {
        Permiso p = new Permiso();
        p.setIdPermiso(rs.getInt("id_permiso"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setArea(rs.getString("area"));
        return p;
    }
}
