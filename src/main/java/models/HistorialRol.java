package models;

import java.time.LocalDateTime;

public class HistorialRol {

    private int idHistorial;
    private int idRol;
    private String accion;
    private String usuarioResponsable;
    private LocalDateTime fechaAccion;

    public HistorialRol() {
    }

    public HistorialRol(int idRol, String accion, String usuarioResponsable) {
        this.idRol = idRol;
        this.accion = accion;
        this.usuarioResponsable = usuarioResponsable;
    }

    public int getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(int idHistorial) {
        this.idHistorial = idHistorial;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(String usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
    }

    public LocalDateTime getFechaAccion() {
        return fechaAccion;
    }

    public void setFechaAccion(LocalDateTime fechaAccion) {
        this.fechaAccion = fechaAccion;
    }
}
