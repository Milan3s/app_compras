package dao;

import config.ConexionDB;
import config.Logger;
import models.Usuario;
import models.Rol;
import models.Permiso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la gestión de usuarios con soporte para roles y múltiples permisos.
 * Relación N:M entre usuarios y permisos mediante la tabla intermedia
 * usuarios_permisos.
 *
 * Compatible con la tabla "roles" que usa el campo "descripcion_rol".
 *
 * @author Milanes
 */
public class UsuarioDAO {

    // ============================================================
    // CONSTANTES
    // ============================================================
    // ID del rol "Pendiente" por defecto
    private static final int ROL_PENDIENTE = 6;

    // ============================================================
    // CREAR USUARIO (SIMPLE)
    // ============================================================
    public boolean crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, email, password, id_rol) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPassword());
            ps.setInt(4, ROL_PENDIENTE); // Asigna siempre el rol Pendiente (id=6)

            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Usuario creado correctamente con rol Pendiente (id_rol=6): " + usuario.getEmail());
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al crear usuario: " + usuario.getEmail(), e);
        }

        return false;
    }

    // ============================================================
    // CREAR USUARIO CON MÚLTIPLES PERMISOS
    // ============================================================
    public boolean crearUsuarioConPermisos(Usuario usuario) {
        String sqlInsertUsuario = "INSERT INTO usuarios (nombre, email, password, id_rol) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement psUsuario = conn.prepareStatement(sqlInsertUsuario, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            psUsuario.setString(1, usuario.getNombre());
            psUsuario.setString(2, usuario.getEmail());
            psUsuario.setString(3, usuario.getPassword());
            psUsuario.setInt(4, ROL_PENDIENTE); // También se registra como Pendiente
            psUsuario.executeUpdate();

            int idUsuario = 0;
            ResultSet rs = psUsuario.getGeneratedKeys();
            if (rs.next()) {
                idUsuario = rs.getInt(1);
            }

            if (usuario.getPermisos() != null && !usuario.getPermisos().isEmpty()) {
                String sqlPermisos = "INSERT INTO usuarios_permisos (id_usuario, id_permiso) VALUES (?, ?)";
                try (PreparedStatement psPermisos = conn.prepareStatement(sqlPermisos)) {
                    for (Permiso permiso : usuario.getPermisos()) {
                        psPermisos.setInt(1, idUsuario);
                        psPermisos.setInt(2, permiso.getIdPermiso());
                        psPermisos.addBatch();
                    }
                    psPermisos.executeBatch();
                }
            }

            conn.commit();
            Logger.info("Usuario creado con múltiples permisos (rol Pendiente): " + usuario.getEmail());
            return true;

        } catch (SQLException e) {
            Logger.exception("Error al crear usuario con permisos: " + usuario.getEmail(), e);
        }

        return false;
    }

    // ============================================================
    // ACTUALIZAR USUARIO Y PERMISOS
    // ============================================================
    public boolean actualizarUsuarioConPermisos(Usuario usuario) {
        String sqlUpdateUsuario = "UPDATE usuarios SET nombre = ?, email = ?, password = ?, id_rol = ? WHERE id_usuario = ?";

        try (Connection conn = ConexionDB.getConexion()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateUsuario)) {
                psUpdate.setString(1, usuario.getNombre());
                psUpdate.setString(2, usuario.getEmail());
                psUpdate.setString(3, usuario.getPassword());
                psUpdate.setInt(4, usuario.getIdRol());
                psUpdate.setInt(5, usuario.getIdUsuario());
                psUpdate.executeUpdate();
            }

            // 🔹 Eliminar permisos previos
            try (PreparedStatement psDelete = conn.prepareStatement("DELETE FROM usuarios_permisos WHERE id_usuario = ?")) {
                psDelete.setInt(1, usuario.getIdUsuario());
                psDelete.executeUpdate();
            }

            // 🔹 Insertar nuevos permisos
            if (usuario.getPermisos() != null && !usuario.getPermisos().isEmpty()) {
                try (PreparedStatement psInsert = conn.prepareStatement("INSERT INTO usuarios_permisos (id_usuario, id_permiso) VALUES (?, ?)")) {
                    for (Permiso permiso : usuario.getPermisos()) {
                        psInsert.setInt(1, usuario.getIdUsuario());
                        psInsert.setInt(2, permiso.getIdPermiso());
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch();
                }
            }

            conn.commit();
            Logger.info("Usuario actualizado correctamente con permisos: " + usuario.getEmail());
            return true;

        } catch (SQLException e) {
            Logger.exception("Error al actualizar usuario con permisos: " + usuario.getEmail(), e);
            return false;
        }
    }

    // ============================================================
    // LISTAR USUARIOS CON ROLES Y PERMISOS
    // ============================================================
    public List<Usuario> listarUsuariosConPermisos() {
        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT u.id_usuario, u.nombre, u.email, u.password, u.id_rol, "
                + "r.nombre AS nombre_rol, r.descripcion_rol AS descripcion_rol "
                + "FROM usuarios u "
                + "LEFT JOIN roles r ON u.id_rol = r.id_rol "
                + "ORDER BY u.id_usuario ASC";

        try (Connection conn = ConexionDB.getConexion(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPassword(rs.getString("password"));
                usuario.setIdRol(rs.getInt("id_rol"));

                Rol rol = new Rol(
                        rs.getInt("id_rol"),
                        rs.getString("nombre_rol"),
                        rs.getString("descripcion_rol")
                );
                usuario.setRol(rol);

                usuario.setPermisos(obtenerPermisosPorUsuario(usuario.getIdUsuario(), conn));
                lista.add(usuario);
            }

            Logger.info("Usuarios cargados correctamente: " + lista.size());

        } catch (SQLException e) {
            Logger.exception("Error al listar usuarios con permisos.", e);
        }

        return lista;
    }

    // ============================================================
    // OBTENER USUARIO POR ID
    // ============================================================
    public Usuario obtenerUsuarioPorId(int idUsuario) {
        Usuario usuario = null;

        String sql = "SELECT u.id_usuario, u.nombre, u.email, u.password, u.id_rol, "
                + "r.nombre AS nombre_rol, r.descripcion_rol AS descripcion_rol "
                + "FROM usuarios u "
                + "LEFT JOIN roles r ON u.id_rol = r.id_rol "
                + "WHERE u.id_usuario = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPassword(rs.getString("password"));
                usuario.setIdRol(rs.getInt("id_rol"));

                Rol rol = new Rol(
                        rs.getInt("id_rol"),
                        rs.getString("nombre_rol"),
                        rs.getString("descripcion_rol")
                );
                usuario.setRol(rol);

                usuario.setPermisos(obtenerPermisosPorUsuario(idUsuario, conn));
            }

        } catch (SQLException e) {
            Logger.exception("Error al obtener usuario por ID: " + idUsuario, e);
        }

        return usuario;
    }

    // ============================================================
    // OBTENER PERMISOS POR USUARIO
    // ============================================================
    private List<Permiso> obtenerPermisosPorUsuario(int idUsuario, Connection conn) throws SQLException {
        List<Permiso> permisos = new ArrayList<>();

        String sql = "SELECT p.id_permiso, p.nombre, p.descripcion, p.area "
                + "FROM usuarios_permisos up "
                + "JOIN permisos p ON up.id_permiso = p.id_permiso "
                + "WHERE up.id_usuario = ? "
                + "ORDER BY p.area, p.nombre";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Permiso permiso = new Permiso(
                        rs.getInt("id_permiso"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("area")
                );
                permisos.add(permiso);
            }
        }

        return permisos;
    }

    // ============================================================
    // ELIMINAR USUARIO Y SUS PERMISOS
    // ============================================================
    public boolean eliminarUsuario(int idUsuario) {
        String sqlPermisos = "DELETE FROM usuarios_permisos WHERE id_usuario = ?";
        String sqlUsuario = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = ConexionDB.getConexion()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlPermisos); PreparedStatement ps2 = conn.prepareStatement(sqlUsuario)) {

                ps1.setInt(1, idUsuario);
                ps1.executeUpdate();

                ps2.setInt(1, idUsuario);
                int filas = ps2.executeUpdate();

                conn.commit();
                return filas > 0;
            }

        } catch (SQLException e) {
            Logger.exception("Error al eliminar usuario con ID: " + idUsuario, e);
        }

        return false;
    }

    // ============================================================
    // AUTENTICAR USUARIO (POR NOMBRE)
    // ============================================================
    public Usuario autenticarUsuario(String nombre, String password) {
        Usuario usuario = null;

        String sql = "SELECT u.id_usuario, u.nombre, u.email, u.password, u.id_rol, "
                + "r.nombre AS nombre_rol, r.descripcion_rol AS descripcion_rol "
                + "FROM usuarios u "
                + "LEFT JOIN roles r ON u.id_rol = r.id_rol "
                + "WHERE u.nombre = ? AND u.password = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setEmail(rs.getString("email"));
                usuario.setPassword(rs.getString("password"));
                usuario.setIdRol(rs.getInt("id_rol"));

                Rol rol = new Rol(
                        rs.getInt("id_rol"),
                        rs.getString("nombre_rol"),
                        rs.getString("descripcion_rol")
                );
                usuario.setRol(rol);

                usuario.setPermisos(obtenerPermisosPorUsuario(usuario.getIdUsuario(), conn));

                Logger.info("Usuario autenticado correctamente: " + nombre);
            } else {
                Logger.warning("Credenciales incorrectas para usuario: " + nombre);
            }

        } catch (SQLException e) {
            Logger.exception("Error al autenticar usuario: " + nombre, e);
        }

        return usuario;
    }

    // ============================================================
    // VERIFICAR EXISTENCIA DE EMAIL
    // ============================================================
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) AS total FROM usuarios WHERE email = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            return rs.next() && rs.getInt("total") > 0;

        } catch (SQLException e) {
            Logger.exception("Error al verificar existencia del email: " + email, e);
        }

        return false;
    }
}
