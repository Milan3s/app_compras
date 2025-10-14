package dao;

import config.ConexionDB;
import config.Logger;
import config.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Carrito;
import models.Compra;

public class CompraDAO {

    /**
     * Registra una compra y sus productos en la base de datos.
     *
     * @param idTienda ID de la tienda (puede ser null)
     * @param nombreCompra Nombre de la compra (nuevo campo)
     * @param carrito Lista de productos del carrito
     * @return true si la compra fue registrada correctamente
     */
    public boolean registrarCompra(Integer idTienda, String nombreCompra, List<Carrito> carrito) {
        int idUsuario = Session.getUsuarioId();
        if (idUsuario <= 0) {
            Logger.warning("Intento de registrar compra sin usuario logueado.");
            return false;
        }

        if (carrito == null || carrito.isEmpty()) {
            Logger.warning("Intento de registrar compra con carrito vacío (Usuario ID: " + idUsuario + ")");
            return false;
        }

        double total = carrito.stream().mapToDouble(Carrito::getSubtotal).sum();

        String sqlCompra = "INSERT INTO compras (id_usuario, id_tienda, nombre, fecha_compra, total) VALUES (?, ?, ?, NOW(), ?)";
        String sqlListadoCompra = "INSERT INTO listado_de_compras (id_compra, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psCompra = null;
        PreparedStatement psListadoCompra = null;
        ResultSet rs = null;

        try {
            conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            // Insertar la compra principal
            psCompra = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
            psCompra.setInt(1, idUsuario);

            if (idTienda != null) {
                psCompra.setInt(2, idTienda);
            } else {
                psCompra.setNull(2, Types.INTEGER);
            }

            psCompra.setString(3, nombreCompra != null ? nombreCompra : "Compra sin nombre");
            psCompra.setDouble(4, total);
            psCompra.executeUpdate();

            // Obtener el id_compra generado
            rs = psCompra.getGeneratedKeys();
            int idCompra = 0;
            if (rs.next()) {
                idCompra = rs.getInt(1);
            }

            if (idCompra == 0) {
                Logger.error("No se pudo obtener el ID de la compra generada.");
                conn.rollback();
                return false;
            }

            // Insertar los registros en listado_de_compras
            psListadoCompra = conn.prepareStatement(sqlListadoCompra);
            for (Carrito c : carrito) {
                psListadoCompra.setInt(1, idCompra);
                psListadoCompra.setInt(2, c.getIdProducto());
                psListadoCompra.setInt(3, c.getCantidad());
                psListadoCompra.setDouble(4, c.getPrecioProducto());
                psListadoCompra.addBatch();
            }
            psListadoCompra.executeBatch();

            // Confirmar la transacción
            conn.commit();
            Logger.info("Compra registrada correctamente (ID: " + idCompra
                    + ", Usuario ID: " + idUsuario
                    + ", Tienda ID: " + (idTienda != null ? idTienda : "NULL")
                    + ", Nombre: " + nombreCompra
                    + ", Total: " + total + " €)");
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
                Logger.warning("Rollback ejecutado en registrarCompra()");
            } catch (SQLException ex) {
                Logger.exception("Error al hacer rollback en registrarCompra()", ex);
            }
            Logger.exception("Error SQL al registrar compra", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (psCompra != null) psCompra.close();
                if (psListadoCompra != null) psListadoCompra.close();
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException e) {
                Logger.exception("Error cerrando recursos en registrarCompra()", e);
            }
        }
        return false;
    }

    /**
     * Devuelve todas las compras de un usuario determinado.
     */
    public List<Compra> listarComprasPorUsuario(int idUsuario) {
        List<Compra> lista = new ArrayList<>();

        String sql = "SELECT c.id_compra, c.nombre, c.fecha_compra, c.total, c.id_tienda, t.nombre AS tienda "
                   + "FROM compras c "
                   + "LEFT JOIN tiendas t ON c.id_tienda = t.id_tienda "
                   + "WHERE c.id_usuario = ? "
                   + "ORDER BY c.fecha_compra DESC";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Compra compra = new Compra();
                compra.setIdCompra(rs.getInt("id_compra"));
                compra.setIdUsuario(idUsuario);
                compra.setIdTienda(rs.getInt("id_tienda"));
                compra.setNombre(rs.getString("nombre"));
                compra.setNombreTienda(rs.getString("tienda"));
                compra.setFechaCompra(rs.getTimestamp("fecha_compra").toLocalDateTime());
                compra.setTotal(rs.getDouble("total"));
                lista.add(compra);
            }

            Logger.info("Compras cargadas correctamente para usuario ID: " + idUsuario);

        } catch (SQLException e) {
            Logger.exception("Error al listar compras por usuario", e);
        }

        return lista;
    }

    /**
     * Calcula el total global de todas las compras registradas.
     */
    public double calcularTotalGeneral() {
        String sql = "SELECT SUM(total) AS total_general FROM compras";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total_general");
            }

        } catch (SQLException e) {
            Logger.exception("Error al calcular el total general de compras", e);
        }
        return 0.0;
    }

    /**
     * Devuelve el nombre del usuario con mayor gasto total en compras.
     */
    public String obtenerUsuarioMayorGasto() {
        String sql = "SELECT u.nombre, SUM(c.total) AS gasto_total "
                   + "FROM compras c "
                   + "INNER JOIN usuarios u ON c.id_usuario = u.id_usuario "
                   + "GROUP BY u.id_usuario "
                   + "ORDER BY gasto_total DESC LIMIT 1";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                String nombre = rs.getString("nombre");
                double gasto = rs.getDouble("gasto_total");
                Logger.info("Usuario con mayor gasto: " + nombre + " (" + gasto + " €)");
                return nombre + " (" + gasto + " €)";
            }

        } catch (SQLException e) {
            Logger.exception("Error al obtener el usuario con mayor gasto", e);
        }

        return "Sin datos";
    }

    /**
     * Devuelve el total gastado por un usuario específico.
     */
    public double calcularTotalPorUsuario(int idUsuario) {
        String sql = "SELECT SUM(total) AS total_usuario FROM compras WHERE id_usuario = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_usuario");
            }

        } catch (SQLException e) {
            Logger.exception("Error al calcular total por usuario", e);
        }
        return 0.0;
    }

    /**
     * Realiza una compra completa desde el carrito filtrando solo la lista activa.
     */
    public boolean realizarCompraDesdeCarrito(int idUsuario, int idTienda, String nombreCompra) {
        if (idUsuario <= 0) {
            idUsuario = Session.getUsuarioId();
        }

        if (idUsuario <= 0) {
            Logger.warning("Intento de realizar compra sin usuario logueado.");
            return false;
        }

        CarritoDAO carritoDAO = new CarritoDAO();
        // 🔹 Ahora solo obtiene los productos del carrito con ese nombre de compra
        List<Carrito> carrito = carritoDAO.listarPorUsuarioYNombre(idUsuario, nombreCompra);

        if (carrito == null || carrito.isEmpty()) {
            Logger.warning("Intento de realizar compra con carrito vacío o sin nombre válido (Usuario ID: " + idUsuario + ", Nombre: " + nombreCompra + ")");
            return false;
        }

        try {
            boolean compraRegistrada = registrarCompra(idTienda, nombreCompra, carrito);

            if (compraRegistrada) {
                // 🔹 Solo elimina del carrito los productos de esa compra específica
                String sqlEliminar = "DELETE FROM carrito WHERE id_usuario = ? AND nombre_compra = ?";
                try (Connection conn = ConexionDB.getConexion();
                     PreparedStatement stmt = conn.prepareStatement(sqlEliminar)) {
                    stmt.setInt(1, idUsuario);
                    stmt.setString(2, nombreCompra);
                    stmt.executeUpdate();
                }

                Logger.info("Compra '" + nombreCompra + "' confirmada y productos eliminados del carrito "
                        + "(Usuario ID: " + idUsuario + ", Tienda ID: " + idTienda + ")");
                return true;
            } else {
                Logger.warning("Error al registrar la compra desde carrito "
                        + "(Usuario ID: " + idUsuario + ", Tienda ID: " + idTienda + ")");
            }

        } catch (Exception e) {
            Logger.exception("Error al realizar compra desde carrito "
                    + "(Usuario ID: " + idUsuario + ", Tienda ID: " + idTienda + ")", e);
        }

        return false;
    }
}
