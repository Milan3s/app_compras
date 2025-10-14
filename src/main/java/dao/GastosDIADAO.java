package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.GastosDia;

/**
 * DAO para gestionar los reportes de gasto diario (por día de la semana).
 * Gestiona la creación de la vista SQL y la lectura de sus datos.
 *
 * @author Milanes
 */
public class GastosDIADAO {

    /**
     * Crea o actualiza la vista SQL "vista_gasto_diario". Agrupa los gastos por
     * usuario y día de la semana (Lunes, Martes, etc.).
     */
    public boolean crearVistaGastoDiario() {
        String sql
                = "CREATE OR REPLACE VIEW vista_gasto_diario AS "
                + "SELECT "
                + "u.nombre AS usuario, "
                + "YEAR(c.fecha_compra) AS anio, "
                + "MONTHNAME(c.fecha_compra) AS mes, "
                + "ELT(DAYOFWEEK(c.fecha_compra), "
                + "   'Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') AS dia_semana, "
                + "SUM(c.total) AS gasto_total "
                + "FROM compras c "
                + "INNER JOIN usuarios u ON u.id_usuario = c.id_usuario "
                + "GROUP BY u.nombre, anio, mes, dia_semana "
                + "ORDER BY anio ASC, mes ASC, FIELD(dia_semana,'Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo'), usuario ASC;";

        try (Connection conn = ConexionDB.getConexion(); Statement st = conn.createStatement()) {

            Logger.info("Creando o actualizando la vista SQL 'vista_gasto_diario'...");
            st.executeUpdate(sql);
            Logger.info("Vista 'vista_gasto_diario' creada correctamente.");
            return true;

        } catch (SQLException e) {
            Logger.exception("Error al crear o actualizar la vista 'vista_gasto_diario'.", e);
            return false;
        }
    }

    /**
     * Lista los gastos diarios desde la vista SQL según día de la semana y
     * usuario. Si diaSemana = null → todos los días. Si usuario = null → todos
     * los usuarios.
     *
     * @param diaSemana nombre del día (Lunes, Martes...) o null
     * @param usuario nombre del usuario o null
     * @return lista de registros de gastos diarios
     */
    public List<GastosDia> listarPorDiaYUsuario(String diaSemana, String usuario) {
        List<GastosDia> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT usuario, anio, mes, dia_semana, gasto_total ");
        sql.append("FROM vista_gasto_diario WHERE 1=1 ");

        if (diaSemana != null && !diaSemana.trim().isEmpty()) {
            sql.append("AND dia_semana = ? ");
        }
        if (usuario != null && !usuario.trim().isEmpty()) {
            sql.append("AND usuario = ? ");
        }

        // ✅ Orden corregido: primero usuario, luego día de la semana
        sql.append("ORDER BY anio ASC, mes ASC, usuario ASC, ");
        sql.append("FIELD(dia_semana,'Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo');");

        boolean reintentoHecho = false;

        while (true) {
            try (Connection conn = ConexionDB.getConexion(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                int index = 1;
                if (diaSemana != null && !diaSemana.trim().isEmpty()) {
                    ps.setString(index++, diaSemana);
                }
                if (usuario != null && !usuario.trim().isEmpty()) {
                    ps.setString(index++, usuario);
                }

                Logger.info("Ejecutando SQL: " + sql);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    GastosDia g = new GastosDia();
                    g.setUsuario(rs.getString("usuario"));
                    g.setAnio(rs.getInt("anio"));
                    g.setMes(rs.getString("mes"));
                    g.setDiaSemana(rs.getString("dia_semana"));
                    g.setGastoTotal(rs.getDouble("gasto_total"));
                    lista.add(g);
                }

                Logger.info("Consulta completada. Registros obtenidos: " + lista.size());
                if (lista.isEmpty()) {
                    Logger.info("No hay registros disponibles en la vista.");
                }
                break;

            } catch (SQLException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

                // Si la vista no existe, crearla y reintentar una sola vez
                if (msg.contains("doesn't exist") && !reintentoHecho) {
                    Logger.warning("La vista 'vista_gasto_diario' no existe. Se procederá a crearla...");
                    boolean creada = crearVistaGastoDiario();
                    if (creada) {
                        Logger.info("Vista 'vista_gasto_diario' creada correctamente. Reintentando consulta...");
                        reintentoHecho = true;
                        continue;
                    } else {
                        Logger.warning("No se pudo crear la vista 'vista_gasto_diario'. Se aborta el proceso.");
                        break;
                    }
                }

                Logger.exception("Error al listar los gastos diarios desde la vista.", e);
                break;
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
