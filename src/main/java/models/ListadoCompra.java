package models;

/**
 * Modelo que representa una fila del listado de compras, incluyendo datos del
 * usuario, producto, tienda y compra.
 *
 * @author Milanes
 */
public class ListadoCompra {

    private int idDetalle;
    private int idCompra;
    private int idUsuario; // Necesario para filtros o cálculos por usuario
    private String nombreCompra;
    private String usuarioNombre;
    private String productoNombre;
    private String tiendaNombre;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private String fechaCompra;

    // --- Campo adicional opcional ---
    // Se usa solo para representar totales por usuario (ComboBox admin)
    private double totalUsuario;

    // --- Constructores ---
    public ListadoCompra() {
    }

    /**
     * Constructor principal sin idUsuario. Usado cuando no se requiere
     * información directa del usuario.
     */
    public ListadoCompra(int idDetalle, int idCompra, String nombreCompra,
            String usuarioNombre, String productoNombre,
            String tiendaNombre, int cantidad,
            double precioUnitario, double subtotal,
            String fechaCompra) {
        this.idDetalle = idDetalle;
        this.idCompra = idCompra;
        this.nombreCompra = nombreCompra;
        this.usuarioNombre = usuarioNombre;
        this.productoNombre = productoNombre;
        this.tiendaNombre = tiendaNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.fechaCompra = fechaCompra;
    }

    /**
     * Constructor extendido con idUsuario (para cálculos por usuario).
     */
    public ListadoCompra(int idDetalle, int idCompra, int idUsuario,
            String nombreCompra, String usuarioNombre,
            String productoNombre, String tiendaNombre,
            int cantidad, double precioUnitario,
            double subtotal, String fechaCompra) {
        this.idDetalle = idDetalle;
        this.idCompra = idCompra;
        this.idUsuario = idUsuario;
        this.nombreCompra = nombreCompra;
        this.usuarioNombre = usuarioNombre;
        this.productoNombre = productoNombre;
        this.tiendaNombre = tiendaNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.fechaCompra = fechaCompra;
    }

    /**
     * Constructor adicional usado para mostrar totales por usuario (por
     * ejemplo, en el ComboBox del administrador).
     */
    public ListadoCompra(int idUsuario, String usuarioNombre, double totalUsuario) {
        this.idUsuario = idUsuario;
        this.usuarioNombre = usuarioNombre;
        this.totalUsuario = totalUsuario;
    }

    // --- Getters y Setters ---
    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

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

    public String getNombreCompra() {
        return nombreCompra;
    }

    public void setNombreCompra(String nombreCompra) {
        this.nombreCompra = nombreCompra;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getTiendaNombre() {
        return tiendaNombre;
    }

    public void setTiendaNombre(String tiendaNombre) {
        this.tiendaNombre = tiendaNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(String fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getTotalUsuario() {
        return totalUsuario;
    }

    public void setTotalUsuario(double totalUsuario) {
        this.totalUsuario = totalUsuario;
    }

    @Override
    public String toString() {
        if (usuarioNombre != null && totalUsuario > 0) {
            return usuarioNombre + " — " + String.format("%.2f €", totalUsuario);
        }

        return "ListadoCompra{"
                + "idDetalle=" + idDetalle
                + ", idCompra=" + idCompra
                + ", idUsuario=" + idUsuario
                + ", nombreCompra='" + nombreCompra + '\''
                + ", usuarioNombre='" + usuarioNombre + '\''
                + ", productoNombre='" + productoNombre + '\''
                + ", tiendaNombre='" + tiendaNombre + '\''
                + ", cantidad=" + cantidad
                + ", precioUnitario=" + precioUnitario
                + ", subtotal=" + subtotal
                + ", fechaCompra='" + fechaCompra + '\''
                + '}';
    }
}
