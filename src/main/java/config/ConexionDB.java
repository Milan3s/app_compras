package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    // Configuración de conexión (ajusta según tu entorno local)
    private static final String URL = "jdbc:mysql://localhost:3306/app_compras";
    private static final String USUARIO = "root";       // Usuario de phpMyAdmin
    private static final String PASSWORD = "";          // Contraseña (vacía si no tienes)

    private static Connection conexion = null;

    /**
     * Retorna una conexión activa a la base de datos.
     */
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                // Registrar el driver JDBC
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Crear la conexión
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                Logger.info("Conexión exitosa a la base de datos 'compras_del_hogar'.");
            }
        } catch (ClassNotFoundException e) {
            Logger.error("No se encontró el driver JDBC de MySQL.");
            Logger.exception("Error al intentar cargar el driver MySQL.", e);
        } catch (SQLException e) {
            Logger.error("Error al conectar con la base de datos 'compras_del_hogar'.");
            Logger.exception("Error de SQL al establecer la conexión.", e);
        }

        return conexion;
    }

    /**
     * Cierra la conexión si está abierta.
     */
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                Logger.info("Conexión cerrada correctamente.");
            }
        } catch (SQLException e) {
            Logger.error("Error al cerrar la conexión a la base de datos.");
            Logger.exception("Error de SQL al cerrar la conexión.", e);
        }
    }
}
