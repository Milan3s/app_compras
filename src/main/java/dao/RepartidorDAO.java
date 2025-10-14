package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Repartidor;

/**
 * DAO encargado de gestionar las operaciones CRUD de la tabla "repartidores".
 * Permite crear, leer, actualizar y eliminar registros de repartidores.
 *
 * @author Milanes
 */
public class RepartidorDAO {

    // ===========================================================
    // 🔹 CREAR REPARTIDOR
    // ===========================================================
    public boolean insertar(Repartidor repartidor) {
        String sql = "INSERT INTO repartidores (nombre, contacto, telefono, email, direccion, sitio_web) VALUES ("
                + "'" + repartidor.getNombre() + "', "
                + (repartidor.getContacto() != null ? "'" + repartidor.getContacto() + "'" : "NULL") + ", "
                + (repartidor.getTelefono() != null ? "'" + repartidor.getTelefono() + "'" : "NULL") + ", "
                + (repartidor.getEmail() != null ? "'" + repartidor.getEmail() + "'" : "NULL") + ", "
                + (repartidor.getDireccion() != null ? "'" + repartidor.getDireccion() + "'" : "NULL") + ", "
                + (repartidor.getSitioWeb() != null ? "'" + repartidor.getSitioWeb() + "'" : "NULL") + ");";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement()) {
            Logger.info("Ejecutando SQL INSERT repartidor: " + sql);
            int filas = st.executeUpdate(sql);
            Logger.info("Repartidor insertado correctamente. Filas afectadas: " + filas);
            return filas > 0;
        } catch (SQLException e) {
            Logger.exception("Error al insertar repartidor en la base de datos.", e);
            return false;
        }
    }

    // ===========================================================
    // 🔹 ACTUALIZAR REPARTIDOR
    // ===========================================================
    public boolean actualizar(Repartidor repartidor) {
        String sql = "UPDATE repartidores SET "
                + "nombre = '" + repartidor.getNombre() + "', "
                + "contacto = " + (repartidor.getContacto() != null ? "'" + repartidor.getContacto() + "'" : "NULL") + ", "
                + "telefono = " + (repartidor.getTelefono() != null ? "'" + repartidor.getTelefono() + "'" : "NULL") + ", "
                + "email = " + (repartidor.getEmail() != null ? "'" + repartidor.getEmail() + "'" : "NULL") + ", "
                + "direccion = " + (repartidor.getDireccion() != null ? "'" + repartidor.getDireccion() + "'" : "NULL") + ", "
                + "sitio_web = " + (repartidor.getSitioWeb() != null ? "'" + repartidor.getSitioWeb() + "'" : "NULL")
                + " WHERE id_repartidor = " + repartidor.getIdRepartidor() + ";";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement()) {
            Logger.info("Ejecutando SQL UPDATE repartidor: " + sql);
            int filas = st.executeUpdate(sql);
            Logger.info("Repartidor actualizado correctamente. Filas afectadas: " + filas);
            return filas > 0;
        } catch (SQLException e) {
            Logger.exception("Error al actualizar repartidor.", e);
            return false;
        }
    }

    // ===========================================================
    // 🔹 ELIMINAR REPARTIDOR
    // ===========================================================
    public boolean eliminar(int idRepartidor) {
        String sql = "DELETE FROM repartidores WHERE id_repartidor = " + idRepartidor + ";";
        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement()) {
            Logger.info("Ejecutando SQL DELETE repartidor: " + sql);
            int filas = st.executeUpdate(sql);
            Logger.info("Repartidor eliminado correctamente. Filas afectadas: " + filas);
            return filas > 0;
        } catch (SQLException e) {
            Logger.exception("Error al eliminar repartidor con ID: " + idRepartidor, e);
            return false;
        }
    }

    // ===========================================================
    // 🔹 LISTAR TODOS LOS REPARTIDORES
    // ===========================================================
    public List<Repartidor> listarTodos() {
        List<Repartidor> lista = new ArrayList<>();
        String sql = "SELECT id_repartidor, nombre, contacto, telefono, email, direccion, sitio_web "
                + "FROM repartidores ORDER BY nombre ASC;";

        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            Logger.info("Ejecutando SQL SELECT repartidores: " + sql);

            while (rs.next()) {
                Repartidor r = new Repartidor();
                r.setIdRepartidor(rs.getInt("id_repartidor"));
                r.setNombre(rs.getString("nombre"));
                r.setContacto(rs.getString("contacto"));
                r.setTelefono(rs.getString("telefono"));
                r.setEmail(rs.getString("email"));
                r.setDireccion(rs.getString("direccion"));
                r.setSitioWeb(rs.getString("sitio_web"));
                lista.add(r);
            }

            Logger.info("Consulta completada. Repartidores obtenidos: " + lista.size());

        } catch (SQLException e) {
            Logger.exception("Error al listar repartidores.", e);
        }

        return lista;
    }

    // ===========================================================
    // 🔹 OBTENER REPARTIDOR POR ID
    // ===========================================================
    public Repartidor obtenerPorId(int idRepartidor) {
        String sql = "SELECT id_repartidor, nombre, contacto, telefono, email, direccion, sitio_web "
                + "FROM repartidores WHERE id_repartidor = " + idRepartidor + ";";

        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            Logger.info("Ejecutando SQL SELECT repartidor por ID: " + sql);

            if (rs.next()) {
                Repartidor r = new Repartidor();
                r.setIdRepartidor(rs.getInt("id_repartidor"));
                r.setNombre(rs.getString("nombre"));
                r.setContacto(rs.getString("contacto"));
                r.setTelefono(rs.getString("telefono"));
                r.setEmail(rs.getString("email"));
                r.setDireccion(rs.getString("direccion"));
                r.setSitioWeb(rs.getString("sitio_web"));
                return r;
            }

        } catch (SQLException e) {
            Logger.exception("Error al obtener repartidor con ID: " + idRepartidor, e);
        }

        return null;
    }

    // ===========================================================
    // 🔹 BUSCAR REPARTIDORES POR NOMBRE (LIKE)
    // ===========================================================
    public List<Repartidor> buscarPorNombre(String nombre) {
        List<Repartidor> lista = new ArrayList<>();
        String sql = "SELECT id_repartidor, nombre, contacto, telefono, email, direccion, sitio_web "
                + "FROM repartidores WHERE nombre LIKE '%" + nombre + "%' ORDER BY nombre ASC;";

        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            Logger.info("Ejecutando SQL búsqueda de repartidores: " + sql);

            while (rs.next()) {
                Repartidor r = new Repartidor();
                r.setIdRepartidor(rs.getInt("id_repartidor"));
                r.setNombre(rs.getString("nombre"));
                r.setContacto(rs.getString("contacto"));
                r.setTelefono(rs.getString("telefono"));
                r.setEmail(rs.getString("email"));
                r.setDireccion(rs.getString("direccion"));
                r.setSitioWeb(rs.getString("sitio_web"));
                lista.add(r);
            }

            Logger.info("Repartidores encontrados: " + lista.size());

        } catch (SQLException e) {
            Logger.exception("Error al buscar repartidores por nombre.", e);
        }

        return lista;
    }
}
