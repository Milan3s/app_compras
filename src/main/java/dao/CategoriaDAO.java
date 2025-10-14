package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Categoria;

/**
 * DAO para la gestión de categorías en la base de datos 'compras_del_hogar'.
 * Permite realizar operaciones CRUD sobre la tabla 'categorias'.
 *
 * @author Milanes
 */
public class CategoriaDAO {

    /**
     * Inserta una nueva categoría en la base de datos.
     *
     * @param categoria Objeto Categoria con los datos a guardar.
     * @return true si se insertó correctamente, false si ocurrió un error.
     */
    public boolean insertar(Categoria categoria) {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Categoría insertada correctamente: " + categoria.getNombre());
                return true;
            }
        } catch (SQLException e) {
            Logger.exception("Error al insertar categoría: " + categoria.getNombre(), e);
        }
        return false;
    }

    /**
     * Actualiza una categoría existente.
     *
     * @param categoria Objeto Categoria con los nuevos datos.
     * @return true si se actualizó correctamente, false si ocurrió un error.
     */
    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ? WHERE id_categoria = ?";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getIdCategoria());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Categoría actualizada correctamente: " + categoria.getNombre());
                return true;
            }
        } catch (SQLException e) {
            Logger.exception("Error al actualizar la categoría ID: " + categoria.getIdCategoria(), e);
        }
        return false;
    }

    /**
     * Elimina una categoría por su ID.
     *
     * @param id ID de la categoría a eliminar.
     * @return true si se eliminó correctamente, false si ocurrió un error.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM categorias WHERE id_categoria = ?";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                Logger.info("Categoría eliminada correctamente (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            Logger.exception("Error al eliminar la categoría con ID: " + id, e);
        }
        return false;
    }

    /**
     * Obtiene una categoría por su ID.
     *
     * @param id ID de la categoría.
     * @return Objeto Categoria o null si no se encuentra.
     */
    public Categoria obtenerPorId(int id) {
        String sql = "SELECT * FROM categorias WHERE id_categoria = ?";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                return c;
            }
        } catch (SQLException e) {
            Logger.exception("Error al obtener la categoría con ID: " + id, e);
        }
        return null;
    }

    /**
     * Obtiene una categoría por su nombre.
     *
     * @param nombre Nombre de la categoría.
     * @return Objeto Categoria o null si no se encuentra.
     */
    public Categoria obtenerPorNombre(String nombre) {
        String sql = "SELECT * FROM categorias WHERE nombre = ?";
        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                return c;
            }
        } catch (SQLException e) {
            Logger.exception("Error al obtener la categoría con nombre: " + nombre, e);
        }
        return null;
    }

    /**
     * Lista todas las categorías registradas.
     *
     * @return Lista de objetos Categoria.
     */
    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY nombre ASC";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                lista.add(c);
            }

            Logger.info("Listado de categorías obtenido correctamente. Total: " + lista.size());
        } catch (SQLException e) {
            Logger.exception("Error al listar las categorías.", e);
        }
        return lista;
    }

    /**
     * Obtiene solo los nombres de las categorías (para ComboBox).
     *
     * @return Lista de nombres de categorías.
     */
    public List<String> obtenerNombresCategorias() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre FROM categorias ORDER BY nombre ASC";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
            }

            Logger.info("Lista de nombres de categorías cargada correctamente (" + nombres.size() + " resultados).");
        } catch (SQLException e) {
            Logger.exception("Error al obtener los nombres de las categorías.", e);
        }
        return nombres;
    }
}
