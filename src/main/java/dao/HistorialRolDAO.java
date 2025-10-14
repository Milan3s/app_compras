package dao;

import config.ConexionDB;
import config.Logger;
import java.sql.*;
import models.HistorialRol;

public class HistorialRolDAO {

    public boolean registrarAccion(HistorialRol historial) {
        String sql = "INSERT INTO historial_roles (id_rol, accion, usuario_responsable) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, historial.getIdRol());
            ps.setString(2, historial.getAccion());
            ps.setString(3, historial.getUsuarioResponsable());

            ps.executeUpdate();
            Logger.info("Historial registrado: " + historial.getAccion());
            return true;

        } catch (SQLException e) {
            Logger.exception("Error al registrar historial de rol.", e);
            return false;
        }
    }
}
