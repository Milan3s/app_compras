package models;

/**
 * Modelo que representa un producto en la base de datos 'compras_del_hogar'.
 * Relacionado con la tabla 'productos'.
 *
 * Incluye referencias a la tienda y categoría asociadas.
 *
 * @author Milanes
 */
public class Producto {

    private int idProducto;
    private String nombre;
    private String descripcion;
    private double precio;
    private int idTienda;
    private int idCategoria;

    // Campos opcionales para mostrar nombres descriptivos
    private String nombreTienda;
    private String nombreCategoria;

    // --- Constructores ---
    public Producto() {
    }

    public Producto(int idProducto, String nombre, String descripcion, double precio, int idTienda, int idCategoria) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.idTienda = idTienda;
        this.idCategoria = idCategoria;
    }

    public Producto(String nombre, String descripcion, double precio, int idTienda, int idCategoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.idTienda = idTienda;
        this.idCategoria = idCategoria;
    }

    // --- Getters y Setters ---
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {
        return nombre + " (" + precio + " €)";
    }
}
