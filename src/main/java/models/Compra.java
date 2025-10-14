package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo que representa una compra registrada en el sistema. Corresponde a la
 * tabla 'compras' en la base de datos.
 *
 * @author Milanes
 */
public class Compra {

    private int idCompra;
    private int idUsuario;
    private Integer idTienda;
    private String nombre; // 🔹 NUEVO: nombre de la compra
    private LocalDateTime fechaCompra;
    private double total;

    // Información extra (opcional para mostrar en vistas)
    private String nombreTienda;
    private String nombreUsuario;

    // ───────────────────────────────
    // Constructores
    // ───────────────────────────────
    public Compra() {
    }

    public Compra(int idCompra, int idUsuario, Integer idTienda, String nombre, LocalDateTime fechaCompra, double total) {
        this.idCompra = idCompra;
        this.idUsuario = idUsuario;
        this.idTienda = idTienda;
        this.nombre = nombre;
        this.fechaCompra = fechaCompra;
        this.total = total;
    }

    public Compra(int idUsuario, Integer idTienda, String nombre, double total) {
        this.idUsuario = idUsuario;
        this.idTienda = idTienda;
        this.nombre = nombre;
        this.total = total;
        this.fechaCompra = LocalDateTime.now();
    }

    // ───────────────────────────────
    // Getters y Setters
    // ───────────────────────────────
    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(Integer idTienda) {
        this.idTienda = idTienda;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    // ───────────────────────────────
    // Métodos útiles
    // ───────────────────────────────
    /**
     * Devuelve la fecha de compra formateada (dd/MM/yyyy HH:mm).
     */
    public String getFechaCompraFormatted() {
        if (fechaCompra == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fechaCompra.format(formatter);
    }

    @Override
    public String toString() {
        return "Compra #" + idCompra
                + " - " + (nombre != null ? nombre : "Sin nombre")
                + " - Total: " + total + "€ - Fecha: " + getFechaCompraFormatted();
    }
}
