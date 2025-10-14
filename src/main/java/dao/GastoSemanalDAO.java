package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.GastoSemanal;

/**
 * DAO para obtener reportes de gasto semanal.
 * Gestiona la creación manual de la vista SQL y la lectura de sus datos.
 *
 * @author Milanes
 */
public class GastoSemanalDAO {

    /**
     * Crea o actualiza la vista SQL "vista_gasto_semanal".
     * Incluye automáticamente solo los registros del mes y año actual.
     */
    public boolean crearVistaGastoSemanal() {
        int mesActual = LocalDate.now().getMonthValue();
        int anioActual = LocalDate.now().getYear();

        String sql =
                "CREATE OR REPLACE VIEW vista_gasto_semanal AS "
              + "SELECT "
              + "u.nombre AS usuario, "
              + "YEAR(c.fecha_compra) AS anio, "
              + "MONTH(c.fecha_compra) AS mes_num, "
              + "MONTHNAME(c.fecha_compra) AS mes_texto, "
              + "(WEEK(c.fecha_compra, 1) - WEEK(DATE_SUB(c.fecha_compra, INTERVAL DAY(c.fecha_compra)-1 DAY), 1) + 1) AS semana_del_mes, "
              + "MIN(DATE(c.fecha_compra)) AS inicio_semana, "
              + "MAX(DATE(c.fecha_compra)) AS fin_semana, "
              + "SUM(c.total) AS gasto_semana "
              + "FROM compras c "
              + "INNER JOIN usuarios u ON u.id_usuario = c.id_usuario "
              + "WHERE MONTH(c.fecha_compra) = " + mesActual + " "
              + "AND YEAR(c.fecha_compra) = " + anioActual + " "
              + "GROUP BY u.nombre, anio, mes_num, semana_del_mes "
              + "ORDER BY anio ASC, mes_num, semana_del_mes;";

        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement()) {

            Logger.info("Creando o actualizando la vista SQL 'vista_gasto_semanal'...");
            Logger.info("Mes actual: " + mesActual + ", Año actual: " + anioActual);

            st.executeUpdate(sql);

            Logger.info("Vista 'vista_gasto_semanal' creada correctamente con datos del mes actual.");
            return true;

        } catch (SQLException e) {
            Logger.exception("Error al crear o actualizar la vista 'vista_gasto_semanal'.", e);
            return false;
        }
    }

    /**
     * Lista los gastos semanales desde la vista SQL según semana y usuario.
     * Si numeroSemana = 0 → todas las semanas.
     * Si usuario = null → todos los usuarios.
     *
     * Controla que si la vista no existe, se captura el error SQL y se devuelve
     * una lista vacía sin romper el flujo del programa.
     */
    public List<GastoSemanal> listarPorSemanaYUsuario(int numeroSemana, String usuario) {
        List<GastoSemanal> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT usuario, anio, mes_texto AS mes, semana_del_mes, inicio_semana, fin_semana, gasto_semana ");
        sql.append("FROM vista_gasto_semanal WHERE 1=1 ");

        if (numeroSemana > 0) {
            sql.append("AND semana_del_mes = ? ");
        }
        if (usuario != null && !usuario.trim().isEmpty()) {
            sql.append("AND usuario = ? ");
        }

        sql.append("ORDER BY usuario, semana_del_mes;");

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (numeroSemana > 0) {
                ps.setInt(index++, numeroSemana);
            }
            if (usuario != null && !usuario.trim().isEmpty()) {
                ps.setString(index++, usuario);
            }

            Logger.info("Ejecutando SQL: " + sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GastoSemanal g = new GastoSemanal();
                g.setUsuario(rs.getString("usuario"));
                g.setAnio(rs.getInt("anio"));
                g.setMes(rs.getString("mes"));
                g.setSemanaDelMes(rs.getInt("semana_del_mes"));
                g.setInicioSemana(rs.getString("inicio_semana"));
                g.setFinSemana(rs.getString("fin_semana"));
                g.setGastoSemana(rs.getDouble("gasto_semana"));
                lista.add(g);
            }

            Logger.info("Consulta completada. Registros obtenidos: " + lista.size());
            if (lista.isEmpty()) {
                Logger.warning("No se encontraron registros con los filtros aplicados.");
            }

        } catch (SQLException e) {
            // ⚠️ Control específico: la vista no existe
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                Logger.warning("La vista 'vista_gasto_semanal' no existe en la base de datos.");
                Logger.warning("Debes crearla manualmente desde el programa antes de cargar los datos.");
            } else {
                Logger.exception("Error al listar gastos semanales desde la vista.", e);
            }
        }

        return lista;
    }

    /**
     * Obtiene la lista de todos los nombres de usuarios para el ComboBox.
     *
     * @return lista de nombres de usuario
     */
    public List<String> obtenerUsuarios() {
        List<String> usuarios = new ArrayList<>();
        String sql = "SELECT nombre FROM usuarios ORDER BY nombre ASC;";

        try (Connection conn = ConexionDB.getConexion();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

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
