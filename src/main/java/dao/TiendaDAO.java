package dao;

import config.ConexionDB;
import config.Logger;
import config.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Tienda;

/**
 * DAO para la gestión de tiendas en la base de datos 'compras_del_hogar'.
 * Incluye relación con la tabla 'repartidores' (JOIN).
 * 
 * @author Milanes
 */
public class TiendaDAO {

    /** Inserta una nueva tienda en la base de datos (con repartidor opcional). */
    public boolean insertar(Tienda tienda) {
        String checkSql = "SELECT COUNT(*) FROM tiendas WHERE nombre = ?";
        String insertSql = "INSERT INTO tiendas (nombre, direccion, telefono, sitio_web, id_repartidor) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion()) {
            // Comprobar duplicado
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, tienda.getNombre());
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    Logger.warning("Intento de insertar tienda duplicada: " + tienda.getNombre());
                    return false;
                }
            }

            // Insertar si no existe
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, tienda.getNombre());
                ps.setString(2, tienda.getDireccion());
                ps.setString(3, tienda.getTelefono());
                ps.setString(4, tienda.getSitioWeb());

                if (tienda.getIdRepartidor() != null) {
                    ps.setInt(5, tienda.getIdRepartidor());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                int filas = ps.executeUpdate();
                if (filas > 0) {
                    Logger.info("Tienda insertada correctamente: " + tienda.getNombre()
                            + " | Usuario: " + Session.getUsuarioId());
                    return true;
                }
            }
        } catch (SQLException e) {
            Logger.exception("Error al insertar tienda: " + tienda.getNombre(), e);
        }
        return false;
    }

    /** Actualiza los datos de una tienda existente (incluyendo el repartidor). */
    public boolean actualizar(Tienda tienda) {
        String sql = "UPDATE tiendas SET nombre = ?, direccion = ?, telefono = ?, sitio_web = ?, id_repartidor = ? WHERE id_tienda = ?";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tienda.getNombre());
            ps.setString(2, tienda.getDireccion());
            ps.setString(3, tienda.getTelefono());
            ps.setString(4, tienda.getSitioWeb());

            if (tienda.getIdRepartidor() != null) {
                ps.setInt(5, tienda.getIdRepartidor());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(6, tienda.getIdTienda());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Tienda actualizada correctamente: " + tienda.getNombre()
                        + " | Usuario: " + Session.getUsuarioId());
                return true;
            }
        } catch (SQLException e) {
            Logger.exception("Error al actualizar la tienda con ID: " + tienda.getIdTienda(), e);
        }
        return false;
    }

    /** Elimina una tienda por su ID. */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM tiendas WHERE id_tienda = ?";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Tienda eliminada correctamente (ID: " + id
                        + ") | Usuario: " + Session.getUsuarioId());
                return true;
            }
        } catch (SQLException e) {
            Logger.exception("Error al eliminar la tienda con ID: " + id, e);
        }
        return false;
    }

    /** Busca una tienda por su ID (incluye nombre del repartidor si aplica). */
    public Tienda obtenerPorId(int id) {
        String sql = "SELECT t.*, r.nombre AS repartidor_nombre "
                   + "FROM tiendas t "
                   + "LEFT JOIN repartidores r ON t.id_repartidor = r.id_repartidor "
                   + "WHERE t.id_tienda = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Tienda tienda = new Tienda();
                tienda.setIdTienda(rs.getInt("id_tienda"));
                tienda.setNombre(rs.getString("nombre"));
                tienda.setDireccion(rs.getString("direccion"));
                tienda.setTelefono(rs.getString("telefono"));
                tienda.setSitioWeb(rs.getString("sitio_web"));
                tienda.setIdRepartidor((Integer) rs.getObject("id_repartidor"));
                tienda.setNombreRepartidor(rs.getString("repartidor_nombre"));
                return tienda;
            }
        } catch (SQLException e) {
            Logger.exception("Error al obtener la tienda con ID: " + id, e);
        }
        return null;
    }

    /** Obtiene todas las tiendas registradas, con nombre del repartidor (JOIN). */
    public List<Tienda> listarTodas() {
        List<Tienda> lista = new ArrayList<>();
        String sql = "SELECT t.*, r.nombre AS repartidor_nombre "
                   + "FROM tiendas t "
                   + "LEFT JOIN repartidores r ON t.id_repartidor = r.id_repartidor "
                   + "ORDER BY t.nombre ASC";

        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Tienda t = new Tienda();
                t.setIdTienda(rs.getInt("id_tienda"));
                t.setNombre(rs.getString("nombre"));
                t.setDireccion(rs.getString("direccion"));
                t.setTelefono(rs.getString("telefono"));
                t.setSitioWeb(rs.getString("sitio_web"));
                t.setIdRepartidor((Integer) rs.getObject("id_repartidor"));
                t.setNombreRepartidor(rs.getString("repartidor_nombre"));
                lista.add(t);
            }

            Logger.info("Listado de tiendas cargado correctamente (orden ASC). Total: " + lista.size());
        } catch (SQLException e) {
            Logger.exception("Error al listar las tiendas.", e);
        }
        return lista;
    }

    /** Busca una tienda por su nombre (exacto). */
    public Tienda obtenerPorNombre(String nombre) {
        String sql = "SELECT t.*, r.nombre AS repartidor_nombre "
                   + "FROM tiendas t "
                   + "LEFT JOIN repartidores r ON t.id_repartidor = r.id_repartidor "
                   + "WHERE t.nombre = ?";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Tienda tienda = new Tienda();
                tienda.setIdTienda(rs.getInt("id_tienda"));
                tienda.setNombre(rs.getString("nombre"));
                tienda.setDireccion(rs.getString("direccion"));
                tienda.setTelefono(rs.getString("telefono"));
                tienda.setSitioWeb(rs.getString("sitio_web"));
                tienda.setIdRepartidor((Integer) rs.getObject("id_repartidor"));
                tienda.setNombreRepartidor(rs.getString("repartidor_nombre"));
                return tienda;
            }
        } catch (SQLException e) {
            Logger.exception("Error al obtener la tienda con nombre: " + nombre, e);
        }
        return null;
    }
}
