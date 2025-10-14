package dao;

import config.ConexionDB;
import config.Logger;
import config.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Producto;

/**
 * DAO para gestionar los productos de la base de datos 'compras_del_hogar'.
 * Incluye métodos CRUD y filtros por tienda y categoría.
 *
 * @author Milanes
 */
public class ProductoDAO {

    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;

    /** Inserta un nuevo producto en la base de datos. */
    public boolean insertar(Producto producto) {
        String sql = "INSERT INTO productos (nombre, descripcion, precio, id_tienda, id_categoria) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getIdTienda());
            ps.setInt(5, producto.getIdCategoria());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Producto agregado: " + producto.getNombre()
                        + " | Tienda ID: " + producto.getIdTienda()
                        + " | Categoría ID: " + producto.getIdCategoria()
                        + " | Usuario: " + Session.getUsuarioId());
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al insertar producto: " + producto.getNombre(), e);
        }
        return false;
    }

    /** Actualiza un producto existente. */
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, id_tienda = ?, id_categoria = ? WHERE id_producto = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getIdTienda());
            ps.setInt(5, producto.getIdCategoria());
            ps.setInt(6, producto.getIdProducto());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Producto actualizado: " + producto.getNombre()
                        + " (ID: " + producto.getIdProducto() + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al actualizar producto ID: " + producto.getIdProducto(), e);
        }
        return false;
    }

    /** Elimina un producto por su ID. */
    public boolean eliminar(int idProducto) {
        String sql = "DELETE FROM productos WHERE id_producto = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                Logger.info("Producto eliminado correctamente (ID: " + idProducto + ")");
                return true;
            }

        } catch (SQLException e) {
            Logger.exception("Error al eliminar producto ID: " + idProducto, e);
        }
        return false;
    }

    /** Lista todos los productos con su tienda y categoría (ordenados por ID). */
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, "
                   + "p.id_tienda, t.nombre AS nombre_tienda, "
                   + "p.id_categoria, c.nombre AS nombre_categoria "
                   + "FROM productos p "
                   + "INNER JOIN tiendas t ON p.id_tienda = t.id_tienda "
                   + "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + "ORDER BY p.id_producto ASC";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getDouble("precio"));
                p.setIdTienda(rs.getInt("id_tienda"));
                p.setNombreTienda(rs.getString("nombre_tienda"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setNombreCategoria(rs.getString("nombre_categoria"));
                lista.add(p);
            }

            Logger.info("Productos listados (ordenados por ID): " + lista.size());

        } catch (SQLException e) {
            Logger.exception("Error al listar todos los productos", e);
        } finally {
            ConexionDB.cerrarConexion();
        }

        return lista;
    }

    /** Lista los productos por tienda específica (ordenados por ID). */
    public List<Producto> listarPorTienda(int idTienda) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, "
                   + "p.id_tienda, t.nombre AS nombre_tienda, "
                   + "p.id_categoria, c.nombre AS nombre_categoria "
                   + "FROM productos p "
                   + "INNER JOIN tiendas t ON p.id_tienda = t.id_tienda "
                   + "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + "WHERE p.id_tienda = ? "
                   + "ORDER BY p.id_producto ASC";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTienda);
            rs = ps.executeQuery();

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getDouble("precio"));
                p.setIdTienda(rs.getInt("id_tienda"));
                p.setNombreTienda(rs.getString("nombre_tienda"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setNombreCategoria(rs.getString("nombre_categoria"));
                lista.add(p);
            }

            Logger.info("Productos listados por tienda ID " + idTienda + " (ordenados por ID): " + lista.size());

        } catch (SQLException e) {
            Logger.exception("Error al listar productos por tienda ID " + idTienda, e);
        } finally {
            ConexionDB.cerrarConexion();
        }

        return lista;
    }

    /** Obtiene productos por nombre de tienda (ordenados por ID). */
    public List<Producto> obtenerPorTiendaNombre(String nombreTienda) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, "
                   + "p.id_tienda, t.nombre AS nombre_tienda, "
                   + "p.id_categoria, c.nombre AS nombre_categoria "
                   + "FROM productos p "
                   + "INNER JOIN tiendas t ON p.id_tienda = t.id_tienda "
                   + "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + "WHERE t.nombre = ? "
                   + "ORDER BY p.id_producto ASC";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreTienda);
            rs = ps.executeQuery();

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getDouble("precio"));
                p.setIdTienda(rs.getInt("id_tienda"));
                p.setNombreTienda(rs.getString("nombre_tienda"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setNombreCategoria(rs.getString("nombre_categoria"));
                lista.add(p);
            }

            Logger.info("Productos obtenidos para tienda '" + nombreTienda + "' (ordenados por ID): " + lista.size());

        } catch (SQLException e) {
            Logger.exception("Error al obtener productos por tienda '" + nombreTienda + "'", e);
        } finally {
            ConexionDB.cerrarConexion();
        }

        return lista;
    }
}
