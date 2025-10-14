package dao;

import config.ConexionDB;
import config.Logger;
import config.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Carrito;

/**
 * DAO para la gestión del carrito de compras.
 * Gestiona las operaciones CRUD sobre la tabla 'carrito'.
 *
 * @author Milanes
 */
public class CarritoDAO {

    /**
     * Agrega un producto al carrito del usuario logueado.
     * Si ya existe, incrementa la cantidad automáticamente.
     */
    public boolean agregarAlCarrito(Carrito carrito) {
        int idUsuario = carrito.getIdUsuario() > 0 ? carrito.getIdUsuario() : Session.getUsuarioId();
        if (idUsuario <= 0) {
            Logger.warning("Intento de agregar producto sin sesión activa.");
            return false;
        }

        String sql = "INSERT INTO carrito (id_usuario, id_producto, cantidad, nombre_compra) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE cantidad = cantidad + VALUES(cantidad)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setInt(2, carrito.getIdProducto());
            stmt.setInt(3, carrito.getCantidad());
            stmt.setString(4, carrito.getNombreCompra());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                Logger.info("Producto agregado o actualizado en carrito (Usuario ID: "
                        + idUsuario + ", Producto ID: " + carrito.getIdProducto()
                        + ", Cantidad: " + carrito.getCantidad()
                        + ", Compra: " + carrito.getNombreCompra() + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error SQL al agregar producto al carrito (Usuario ID " + idUsuario + ")", e);
        } catch (Exception ex) {
            Logger.exception("Error general al agregar producto al carrito", ex);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // CRUD BÁSICO
    // -------------------------------------------------------------------------

    public boolean actualizarCantidad(int idCarrito, int cantidad) {
        String sql = "UPDATE carrito SET cantidad = ? WHERE id_carrito = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cantidad);
            stmt.setInt(2, idCarrito);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                Logger.info("Cantidad actualizada correctamente (id_carrito = " + idCarrito + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al actualizar cantidad (id_carrito)", e);
        }
        return false;
    }

    public boolean actualizarCantidad(int idUsuario, int idProducto, int nuevaCantidad) {
        if (idUsuario <= 0) idUsuario = Session.getUsuarioId();

        if (idUsuario <= 0 || idProducto <= 0) {
            Logger.warning("Intento de actualizar cantidad sin IDs válidos (usuario o producto).");
            return false;
        }

        if (nuevaCantidad <= 0) {
            Logger.info("Cantidad <= 0 detectada. Eliminando producto del carrito (Usuario " + idUsuario + ", Producto " + idProducto + ")");
            return eliminarDelCarrito(idUsuario, idProducto);
        }

        String sql = "UPDATE carrito SET cantidad = ? WHERE id_usuario = ? AND id_producto = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nuevaCantidad);
            stmt.setInt(2, idUsuario);
            stmt.setInt(3, idProducto);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                Logger.info("Cantidad actualizada correctamente (Usuario: " + idUsuario
                        + ", Producto: " + idProducto + ", Nueva cantidad: " + nuevaCantidad + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error SQL al actualizar cantidad (Usuario ID: " + idUsuario + ", Producto ID: " + idProducto + ")", e);
        }
        return false;
    }

    public boolean eliminarProducto(int idCarrito) {
        String sql = "DELETE FROM carrito WHERE id_carrito = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCarrito);
            int filas = stmt.executeUpdate();

            if (filas > 0) {
                Logger.info("Producto eliminado del carrito (id_carrito: " + idCarrito + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al eliminar producto del carrito (id_carrito)", e);
        }
        return false;
    }

    public boolean eliminarDelCarrito(int idUsuario, int idProducto) {
        if (idUsuario <= 0) idUsuario = Session.getUsuarioId();

        if (idUsuario <= 0 || idProducto <= 0) {
            Logger.warning("Intento de eliminar producto sin IDs válidos.");
            return false;
        }

        String sql = "DELETE FROM carrito WHERE id_usuario = ? AND id_producto = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idProducto);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                Logger.info("Producto eliminado del carrito (Usuario ID: " + idUsuario + ", Producto ID: " + idProducto + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error SQL al eliminar producto del carrito (Usuario ID: " + idUsuario + ", Producto ID: " + idProducto + ")", e);
        }
        return false;
    }

    public boolean vaciarCarrito(int idUsuario) {
        if (idUsuario <= 0) idUsuario = Session.getUsuarioId();

        String sql = "DELETE FROM carrito WHERE id_usuario = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            int filas = stmt.executeUpdate();

            if (filas > 0) {
                Logger.info("Carrito vaciado correctamente (Usuario ID: " + idUsuario + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al vaciar carrito (Usuario ID: " + idUsuario + ")", e);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // CONSULTAS EXTENDIDAS
    // -------------------------------------------------------------------------

    /**
     * Obtiene los productos del carrito de un usuario y nombre de compra específicos.
     */
    public List<Carrito> listarPorUsuarioYNombre(int idUsuario, String nombreCompra) {
        if (idUsuario <= 0) idUsuario = Session.getUsuarioId();

        List<Carrito> lista = new ArrayList<>();
        String sql = "SELECT c.id_carrito, c.id_usuario, c.id_producto, c.cantidad, c.fecha_agregado, "
                   + "p.nombre AS producto, p.precio "
                   + "FROM carrito c "
                   + "INNER JOIN productos p ON c.id_producto = p.id_producto "
                   + "WHERE c.id_usuario = ? AND c.nombre_compra = ? "
                   + "ORDER BY c.fecha_agregado ASC";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, nombreCompra);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Carrito c = new Carrito();
                c.setIdCarrito(rs.getInt("id_carrito"));
                c.setIdUsuario(rs.getInt("id_usuario"));
                c.setIdProducto(rs.getInt("id_producto"));
                c.setCantidad(rs.getInt("cantidad"));
                c.setNombreProducto(rs.getString("producto"));
                c.setPrecioProducto(rs.getDouble("precio"));
                c.setSubtotal(c.getCantidad() * c.getPrecioProducto());

                Timestamp ts = rs.getTimestamp("fecha_agregado");
                if (ts != null) {
                    c.setFechaAgregado(ts.toLocalDateTime());
                }

                c.setNombreCompra(nombreCompra);
                lista.add(c);
            }

            Logger.info("Carrito cargado correctamente para usuario "
                    + idUsuario + " y compra '" + nombreCompra + "'");

        } catch (SQLException e) {
            Logger.exception("Error al listar productos del carrito filtrando por nombre de compra", e);
        }
        return lista;
    }

    /**
     * Calcula el total del carrito del usuario actual (sin filtrar).
     */
    public double calcularTotal(int idUsuario) {
        if (idUsuario <= 0) idUsuario = Session.getUsuarioId();

        String sql = "SELECT SUM(c.cantidad * p.precio) AS total "
                   + "FROM carrito c "
                   + "INNER JOIN productos p ON c.id_producto = p.id_producto "
                   + "WHERE c.id_usuario = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            Logger.exception("Error al calcular total del carrito", e);
        }
        return 0.0;
    }
}
