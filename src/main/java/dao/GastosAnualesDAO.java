package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.GastosAnual;

/**
 * DAO para obtener reportes de gasto anual. Gestiona la creación de la vista
 * SQL y la lectura de sus datos.
 *
 * @author Milanes
 */
public class GastosAnualesDAO {

    // =====================================================
    // 🧱 CREAR O ACTUALIZAR VISTA
    // =====================================================
    public boolean crearVistaGastoAnual() {
        String sql
                = "CREATE OR REPLACE VIEW vista_gasto_anual AS "
                + "SELECT "
                + "u.nombre AS usuario, "
                + "YEAR(c.fecha_compra) AS anio, "
                + "SUM(c.total) AS gasto_total "
                + "FROM compras c "
                + "INNER JOIN usuarios u ON u.id_usuario = c.id_usuario "
                + "GROUP BY u.nombre, YEAR(c.fecha_compra) "
                + "ORDER BY anio ASC, usuario ASC;";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement()) {

            Logger.info("Creando o actualizando la vista SQL 'vista_gasto_anual'...");
            st.executeUpdate(sql);
            Logger.info("Vista 'vista_gasto_anual' creada o actualizada correctamente.");
            return true;

        } catch (SQLException e) {
            Logger.exception("Error al crear o actualizar la vista 'vista_gasto_anual'.", e);
            return false;
        }
    }

    // =====================================================
    // 📊 LISTAR DATOS DESDE LA VISTA
    // =====================================================
    /**
     * Lista los gastos anuales desde la vista SQL. Si anio = 0 → todos los
     * años. Si usuario = null → todos los usuarios.
     */
    public List<GastosAnual> listarPorAnioYUsuario(int anio, String usuario) {
        List<GastosAnual> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT usuario, anio, gasto_total ");
        sql.append("FROM vista_gasto_anual WHERE 1=1 ");

        if (anio > 0) {
            sql.append("AND anio = ? ");
        }
        if (usuario != null && !usuario.trim().isEmpty()) {
            sql.append("AND usuario = ? ");
        }

        sql.append("ORDER BY anio ASC, usuario ASC;");

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (anio > 0) {
                ps.setInt(index++, anio);
            }
            if (usuario != null && !usuario.trim().isEmpty()) {
                ps.setString(index++, usuario);
            }

            Logger.info("Ejecutando SQL: " + sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GastosAnual g = new GastosAnual();
                g.setUsuario(rs.getString("usuario"));
                g.setAnio(rs.getInt("anio"));
                g.setGastoTotal(rs.getDouble("gasto_total"));
                lista.add(g);
            }

            Logger.info("Consulta completada. Registros obtenidos: " + lista.size());
            if (lista.isEmpty()) {
                Logger.warning("No se encontraron registros con los filtros aplicados.");
            }

        } catch (SQLException e) {
            // ⚠️ Manejo especial si la vista no existe
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("doesn't exist") || msg.contains("no such table") || msg.contains("view")) {
                Logger.warning("La vista 'vista_gasto_anual' no existe. Crea la vista antes de cargar los datos.");
            } else {
                Logger.exception("Error al listar gastos anuales desde la vista.", e);
            }
        }

        return lista;
    }

    // =====================================================
    // 👥 OBTENER LISTA DE USUARIOS
    // =====================================================
    /**
     * Obtiene la lista de todos los nombres de usuarios para el ComboBox.
     *
     * @return lista de nombres de usuario
     */
    public List<String> obtenerUsuarios() {
        List<String> usuarios = new ArrayList<>();
        String sql = "SELECT nombre FROM usuarios ORDER BY nombre ASC;";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(rs.getString("nombre"));
            }

            Logger.info("Usuarios cargados correctamente: " + usuarios.size());

        } catch (SQLException e) {
            Logger.exception("Error al obtener la lista de usuarios.", e);
        }

        return usuarios;
    }
}
