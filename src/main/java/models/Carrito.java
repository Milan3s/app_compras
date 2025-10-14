package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo que representa un registro de la tabla 'carrito' en la base de datos
 * compras_del_hogar.
 *
 * @author Milanes
 */
public class Carrito {

    private int idCarrito;
    private int idUsuario;
    private int idProducto;
    private int cantidad;
    private LocalDateTime fechaAgregado;

    // --- Relaciones opcionales (para mostrar info más legible en la app)
    private String nombreUsuario;
    private String nombreProducto;
    private String nombreCompra; // 🔹 Nuevo campo: nombre de la compra asociada
    private double precioProducto;
    private double subtotal;

    // ─────────────────────────────────────────────
    // Constructores
    // ─────────────────────────────────────────────
    public Carrito() {
    }

    public Carrito(int idUsuario, int idProducto, int cantidad) {
        this.idUsuario = idUsuario;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    public Carrito(int idCarrito, int idUsuario, int idProducto, int cantidad, LocalDateTime fechaAgregado) {
        this.idCarrito = idCarrito;
        this.idUsuario = idUsuario;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.fechaAgregado = fechaAgregado;
    }

    // ─────────────────────────────────────────────
    // Getters y Setters
    // ─────────────────────────────────────────────
    public int getIdCarrito() {
        return idCarrito;
    }

    public void setIdCarrito(int idCarrito) {
        this.idCarrito = idCarrito;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getNombreCompra() {
        return nombreCompra;
    }

    public void setNombreCompra(String nombreCompra) {
        this.nombreCompra = nombreCompra;
    }

    public double getPrecioProducto() {
        return precioProducto;
    }

    public void setPrecioProducto(double precioProducto) {
        this.precioProducto = precioProducto;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // ─────────────────────────────────────────────
    // Métodos auxiliares
    // ─────────────────────────────────────────────

    /**
     * Calcula dinámicamente el subtotal basado en la cantidad y precio actual.
     */
    public double calcularSubtotal() {
        return this.precioProducto * this.cantidad;
    }

    /**
     * Devuelve la fecha formateada (dd/MM/yyyy HH:mm) para mostrar en tablas.
     */
    public String getFechaAgregadoFormatted() {
        if (fechaAgregado != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaAgregado.format(formatter);
        }
        return "";
    }

    @Override
    public String toString() {
        return "Carrito{" +
                "idCarrito=" + idCarrito +
                ", idUsuario=" + idUsuario +
                ", idProducto=" + idProducto +
                ", cantidad=" + cantidad +
                ", fechaAgregado=" + getFechaAgregadoFormatted() +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", nombreCompra='" + (nombreCompra != null ? nombreCompra : "Sin asignar") + '\'' +
                ", subtotal=" + calcularSubtotal() +
                '}';
    }
}
