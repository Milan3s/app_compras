package models;

/**
 * Modelo que representa un repartidor dentro del sistema 'compras_del_hogar'.
 *
 * Corresponde a la tabla 'repartidores' en la base de datos.
 *
 * Campos:
 *  - id_repartidor (PK)
 *  - nombre
 *  - contacto
 *  - telefono
 *  - email
 *  - direccion
 *  - sitio_web
 *
 * @author Milanes
 */
public class Repartidor {

    private int idRepartidor;
    private String nombre;
    private String contacto;
    private String telefono;
    private String email;
    private String direccion;
    private String sitioWeb;

    // --- Constructores ---
    public Repartidor() {}

    public Repartidor(int idRepartidor, String nombre, String contacto, String telefono, String email, String direccion, String sitioWeb) {
        this.idRepartidor = idRepartidor;
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.sitioWeb = sitioWeb;
    }

    public Repartidor(String nombre, String contacto, String telefono, String email, String direccion, String sitioWeb) {
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.sitioWeb = sitioWeb;
    }

    // --- Getters y Setters ---
    public int getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    // --- Métodos auxiliares ---
    @Override
    public String toString() {
        return nombre + (contacto != null && !contacto.isEmpty() ? " - " + contacto : "");
    }

    public String toDebugString() {
        return "Repartidor{"
                + "idRepartidor=" + idRepartidor
                + ", nombre='" + nombre + '\''
                + ", contacto='" + contacto + '\''
                + ", telefono='" + telefono + '\''
                + ", email='" + email + '\''
                + ", direccion='" + direccion + '\''
                + ", sitioWeb='" + sitioWeb + '\''
                + '}';
    }
}
