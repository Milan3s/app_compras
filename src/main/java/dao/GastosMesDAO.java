package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.GastosMes;

/**
 * DAO para generar y consultar el reporte de gastos mensuales. Gestiona la
 * creación de la vista SQL "vista_gasto_mensual" y la lectura de sus datos por
 * usuario y mes.
 */
public class GastosMesDAO {

    /**
     * Crea o actualiza la vista SQL "vista_gasto_mensual". Incluye
     * automáticamente los registros del año actual agrupados por usuario, mes y
     * categoría. Fuerza los nombres de los meses en español (lc_time_names =
     * 'es_ES').
     */
    public boolean crearVistaGastoMensual() {
        int anioActual = LocalDate.now().getYear();

        String sqlConfig = "SET lc_time_names = 'es_ES';";

        String sqlVista
                = "CREATE OR REPLACE VIEW vista_gasto_mensual AS "
                + "SELECT "
                + "u.nombre AS usuario, "
                + "YEAR(c.fecha_compra) AS anio, "
                + "MONTH(c.fecha_compra) AS mes_num, "
                + "LPAD(MONTH(c.fecha_compra), 2, '0') AS mes_numero, "
                + "CONCAT(UCASE(LEFT(MONTHNAME(c.fecha_compra), 1)), "
                + "       LCASE(SUBSTRING(MONTHNAME(c.fecha_compra), 2))) AS mes_texto, "
                + "cat.nombre AS categoria, "
                + "SUM(c.total) AS gasto_total "
                + "FROM compras c "
                + "INNER JOIN usuarios u ON u.id_usuario = c.id_usuario "
                + "LEFT JOIN listado_de_compras ldc ON ldc.id_compra = c.id_compra "
                + "LEFT JOIN productos p ON p.id_producto = ldc.id_producto "
                + "LEFT JOIN categorias cat ON cat.id_categoria = p.id_categoria "
                + "WHERE YEAR(c.fecha_compra) = " + anioActual + " "
                + "GROUP BY u.nombre, anio, mes_num, categoria "
                + "ORDER BY anio ASC, mes_num, u.nombre, categoria;";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement()) {

            Logger.info("Creando o actualizando la vista SQL 'vista_gasto_mensual'...");
            Logger.info("Año actual: " + anioActual);

            // Forzamos los nombres de mes en español
            st.execute(sqlConfig);

            // Creamos la vista
            st.executeUpdate(sqlVista);

            Logger.info("Vista 'vista_gasto_mensual' creada correctamente con meses en español.");
            return true;

        } catch (SQLException e) {
            Logger.exception("Error al crear o actualizar la vista 'vista_gasto_mensual'.", e);
            return false;
        }
    }

    /**
     * Lista los gastos mensuales desde la vista SQL. Si mes = 0 → muestra todos
     * los meses del año. Si usuario = null → muestra todos los usuarios.
     *
     * @param mes número del mes (1–12 o 0 para todos)
     * @param usuario nombre del usuario o null
     * @return lista de objetos GastosMes
     */
    public List<GastosMes> listarPorMesYUsuario(int mes, String usuario) {
        List<GastosMes> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT usuario, anio, mes_texto AS mes, categoria, gasto_total ");
        sql.append("FROM vista_gasto_mensual WHERE 1=1 ");

        if (mes > 0) {
            sql.append("AND mes_num = ? ");
        }
        if (usuario != null && !usuario.trim().isEmpty()) {
            sql.append("AND usuario = ? ");
        }

        sql.append("ORDER BY anio ASC, mes_num, usuario, categoria;");

        try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (mes > 0) {
                ps.setInt(index++, mes);
            }
            if (usuario != null && !usuario.trim().isEmpty()) {
                ps.setString(index++, usuario);
            }

            Logger.info("Ejecutando consulta SQL de gastos mensuales: " + sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                GastosMes gm = new GastosMes();
                gm.setUsuario(rs.getString("usuario"));
                gm.setAnio(rs.getInt("anio"));
                gm.setMes(rs.getString("mes"));
                gm.setCategoria(rs.getString("categoria"));
                gm.setGastoTotal(rs.getDouble("gasto_total"));
                lista.add(gm);
            }

            Logger.info("Consulta completada. Registros obtenidos: " + lista.size());
            if (lista.isEmpty()) {
                Logger.warning("No se encontraron registros de gastos mensuales con los filtros aplicados.");
            }

        } catch (SQLException e) {
            Logger.exception("Error al listar los gastos mensuales desde la vista.", e);
        }

        return lista;
    }

    /**
     * Devuelve todos los nombres de usuarios (para los filtros del ComboBox).
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
