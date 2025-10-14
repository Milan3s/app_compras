package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import java.util.*;
import models.ListadoCompra;

/**
 * DAO para la vista Listado de Compras. Une información de compras, usuarios,
 * productos y tiendas.
 *
 * @author Milanes
 */
public class ListadoDeCompraDAO {

    /**
     * Obtiene todos los registros de la tabla listado_de_compras con
     * información completa (modo administrador).
     */
    public List<ListadoCompra> listarDetallesCompras() {
        List<ListadoCompra> lista = new ArrayList<>();

        String sql = "SELECT "
                + " ld.id_detalle, "
                + " ld.id_compra, "
                + " c.id_usuario, "
                + " c.nombre AS nombre_compra, "
                + " u.nombre AS usuario, "
                + " p.nombre AS producto, "
                + " t.nombre AS tienda, "
                + " ld.cantidad, "
                + " ld.precio_unitario, "
                + " ld.subtotal, "
                + " c.fecha_compra "
                + "FROM listado_de_compras ld "
                + "INNER JOIN compras c ON ld.id_compra = c.id_compra "
                + "INNER JOIN usuarios u ON c.id_usuario = u.id_usuario "
                + "INNER JOIN productos p ON ld.id_producto = p.id_producto "
                + "INNER JOIN tiendas t ON c.id_tienda = t.id_tienda "
                + "ORDER BY ld.id_detalle ASC;"; // 🔥 Orden numérico puro por ID

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ListadoCompra lc = new ListadoCompra(
                        rs.getInt("id_detalle"),
                        rs.getInt("id_compra"),
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_compra"),
                        rs.getString("usuario"),
                        rs.getString("producto"),
                        rs.getString("tienda"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal"),
                        rs.getString("fecha_compra")
                );
                lista.add(lc);
            }

            Logger.info("Listado de TODAS las compras cargado correctamente. Total registros: " + lista.size());
        } catch (SQLException e) {
            Logger.exception("Error al listar TODAS las compras", e);
        }

        return lista;
    }

    /**
     * Obtiene los registros de compras asociados a un usuario específico.
     */
    public List<ListadoCompra> listarDetallesPorUsuario(int idUsuario) {
        List<ListadoCompra> lista = new ArrayList<>();

        String sql = "SELECT "
                + " ld.id_detalle, "
                + " ld.id_compra, "
                + " c.id_usuario, "
                + " c.nombre AS nombre_compra, "
                + " u.nombre AS usuario, "
                + " p.nombre AS producto, "
                + " t.nombre AS tienda, "
                + " ld.cantidad, "
                + " ld.precio_unitario, "
                + " ld.subtotal, "
                + " c.fecha_compra "
                + "FROM listado_de_compras ld "
                + "INNER JOIN compras c ON ld.id_compra = c.id_compra "
                + "INNER JOIN usuarios u ON c.id_usuario = u.id_usuario "
                + "INNER JOIN productos p ON ld.id_producto = p.id_producto "
                + "INNER JOIN tiendas t ON c.id_tienda = t.id_tienda "
                + "WHERE c.id_usuario = ? "
                + "ORDER BY ld.id_detalle ASC;"; // 🔥 También orden numérico puro por ID

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, idUsuario);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ListadoCompra lc = new ListadoCompra(
                            rs.getInt("id_detalle"),
                            rs.getInt("id_compra"),
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_compra"),
                            rs.getString("usuario"),
                            rs.getString("producto"),
                            rs.getString("tienda"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("subtotal"),
                            rs.getString("fecha_compra")
                    );
                    lista.add(lc);
                }
            }

            Logger.info("Listado de compras cargado para usuario ID " + idUsuario + ". Total: " + lista.size());
        } catch (SQLException e) {
            Logger.exception("Error al listar compras por usuario", e);
        }

        return lista;
    }

    /**
     * Calcula el total gastado por un usuario específico.
     */
    public double obtenerTotalGastadoPorUsuario(int idUsuario) {
        double total = 0.0;

        String sql = "SELECT SUM(ld.subtotal) AS total "
                + "FROM listado_de_compras ld "
                + "JOIN compras c ON ld.id_compra = c.id_compra "
                + "WHERE c.id_usuario = ?;";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, idUsuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }

            Logger.info("Total gastado por usuario ID " + idUsuario + ": " + total + " €");
        } catch (SQLException e) {
            Logger.exception("Error al calcular total gastado por usuario", e);
        }

        return total;
    }

    /**
     * Calcula el total general de todas las compras (modo administrador).
     */
    public double obtenerTotalGeneral() {
        double total = 0.0;
        String sql = "SELECT SUM(ld.subtotal) AS total FROM listado_de_compras ld;";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                total = rs.getDouble("total");
            }
            Logger.info("Total general de compras (admin): " + total + " €");
        } catch (SQLException e) {
            Logger.exception("Error al calcular total general de compras", e);
        }

        return total;
    }

    /**
     * Devuelve una lista con el nombre de cada usuario y su total gastado.
     */
    public List<Map<String, Object>> obtenerTotalesPorUsuario() {
        List<Map<String, Object>> lista = new ArrayList<>();

        String sql = "SELECT u.id_usuario, u.nombre AS usuario, SUM(ld.subtotal) AS total "
                + "FROM listado_de_compras ld "
                + "JOIN compras c ON ld.id_compra = c.id_compra "
                + "JOIN usuarios u ON c.id_usuario = u.id_usuario "
                + "GROUP BY u.id_usuario, u.nombre "
                + "ORDER BY total ASC;";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("id_usuario", rs.getInt("id_usuario"));
                fila.put("usuario", rs.getString("usuario"));
                fila.put("total", rs.getDouble("total"));
                lista.add(fila);
            }

            Logger.info("Totales por usuario cargados correctamente (" + lista.size() + " usuarios).");
        } catch (SQLException e) {
            Logger.exception("Error al obtener totales por usuario", e);
        }

        return lista;
    }

    /**
     * Corrige error con columna generada 'subtotal'.
     */
    public boolean actualizarDetalleCompra(ListadoCompra compra) {
        String sql = "UPDATE listado_de_compras "
                + "SET cantidad = ?, precio_unitario = ? "
                + "WHERE id_detalle = ?;";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, compra.getCantidad());
            pst.setDouble(2, compra.getPrecioUnitario());
            pst.setInt(3, compra.getIdDetalle());

            int filas = pst.executeUpdate();
            if (filas > 0) {
                Logger.info("Detalle de compra actualizado correctamente. ID: " + compra.getIdDetalle());
                return true;
            }
        } catch (SQLException e) {
            Logger.exception("Error al actualizar detalle de compra ID " + compra.getIdDetalle(), e);
        }

        return false;
    }

    /**
     * Obtiene el ID del usuario a partir de su nombre.
     */
    public int obtenerIdUsuarioPorNombre(String nombreUsuario) {
        String sql = "SELECT id_usuario FROM usuarios WHERE nombre = ?;";
        int id = -1;

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, nombreUsuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id_usuario");
                }
            }

            Logger.info("ID obtenido para usuario '" + nombreUsuario + "': " + id);
        } catch (SQLException e) {
            Logger.exception("Error al obtener ID del usuario: " + nombreUsuario, e);
        }

        return id;
    }

    /**
     * Devuelve todas las compras agrupadas por usuario. Si el parámetro
     * idUsuario es -1 ⇒ devuelve todos los usuarios (modo admin). Si el
     * parámetro idUsuario > 0 ⇒ devuelve solo ese usuario.
     */
    public Map<String, List<ListadoCompra>> listarComprasAgrupadasPorUsuario(int idUsuario) {
        Map<String, List<ListadoCompra>> mapaUsuarios = new LinkedHashMap<>();

        String sql = "SELECT "
                + " u.nombre AS usuario, "
                + " ld.id_detalle, "
                + " ld.id_compra, "
                + " c.id_usuario, "
                + " c.nombre AS nombre_compra, "
                + " p.nombre AS producto, "
                + " t.nombre AS tienda, "
                + " ld.cantidad, "
                + " ld.precio_unitario, "
                + " ld.subtotal, "
                + " c.fecha_compra "
                + "FROM listado_de_compras ld "
                + "INNER JOIN compras c ON ld.id_compra = c.id_compra "
                + "INNER JOIN usuarios u ON c.id_usuario = u.id_usuario "
                + "INNER JOIN productos p ON ld.id_producto = p.id_producto "
                + "INNER JOIN tiendas t ON c.id_tienda = t.id_tienda "
                + (idUsuario > 0 ? "WHERE c.id_usuario = ? " : "")
                + "ORDER BY u.nombre ASC, c.id_compra ASC, ld.id_detalle ASC;";

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement pst = conn.prepareStatement(sql)) {

            if (idUsuario > 0) {
                pst.setInt(1, idUsuario);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String usuario = rs.getString("usuario");

                    ListadoCompra lc = new ListadoCompra(
                            rs.getInt("id_detalle"),
                            rs.getInt("id_compra"),
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_compra"),
                            usuario,
                            rs.getString("producto"),
                            rs.getString("tienda"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("subtotal"),
                            rs.getString("fecha_compra")
                    );

                    mapaUsuarios.computeIfAbsent(usuario, k -> new ArrayList<>()).add(lc);
                }
            }

            Logger.info("Compras agrupadas cargadas correctamente. Filtro: "
                    + (idUsuario > 0 ? "usuario ID " + idUsuario : "TODOS los usuarios")
                    + ". Total usuarios: " + mapaUsuarios.size());
        } catch (SQLException e) {
            Logger.exception("Error al listar compras agrupadas por usuario", e);
        }

        return mapaUsuarios;
    }

}
