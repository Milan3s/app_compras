package models;

/**
 * Modelo que representa una tienda dentro del sistema 'compras_del_hogar'.
 *
 * Corresponde a la tabla 'tiendas' en la base de datos.
 *
 * Campos:
 *  - id_tienda (PK)
 *  - nombre
 *  - direccion
 *  - telefono
 *  - sitio_web
 *  - id_repartidor (FK hacia repartidores)
 *
 * @author Milanes
 */
public class Tienda {

    private int idTienda;
    private String nombre;
    private String direccion;
    private String telefono;
    private String sitioWeb;

    // 🔹 Nuevo: relación con repartidor
    private Integer idRepartidor;         // puede ser null si no hay repartidor asignado
    private String nombreRepartidor;      // nombre del repartidor (obtenido por JOIN)

    // --- Constructores ---
    public Tienda() {
    }

    public Tienda(int idTienda, String nombre, String direccion, String telefono, String sitioWeb) {
        this.idTienda = idTienda;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.sitioWeb = sitioWeb;
    }

    public Tienda(int idTienda, String nombre, String direccion, String telefono, String sitioWeb, Integer idRepartidor, String nombreRepartidor) {
        this.idTienda = idTienda;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.sitioWeb = sitioWeb;
        this.idRepartidor = idRepartidor;
        this.nombreRepartidor = nombreRepartidor;
    }

    public Tienda(String nombre, String direccion, String telefono, String sitioWeb) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.sitioWeb = sitioWeb;
    }

    // --- Getters y Setters ---
    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public Integer getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(Integer idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    // --- Métodos auxiliares ---
    @Override
    public String toString() {
        String rep = (nombreRepartidor != null) ? " (" + nombreRepartidor + ")" : "";
        return nombre + rep + (direccion != null ? " - " + direccion : "");
    }

    /**
     * Devuelve una representación detallada de la tienda para logs o depuración.
     */
    public String toDebugString() {
        return "Tienda{"
                + "idTienda=" + idTienda
                + ", nombre='" + nombre + '\''
                + ", direccion='" + direccion + '\''
                + ", telefono='" + telefono + '\''
                + ", sitioWeb='" + sitioWeb + '\''
                + ", idRepartidor=" + idRepartidor
                + ", nombreRepartidor='" + nombreRepartidor + '\''
                + '}';
    }
}
