package models;

/**
 * Modelo que representa un gasto anual, basado en la vista SQL "vista_gasto_anual".
 * Contiene los totales de gasto agrupados por usuario y año.
 *
 * @author Milanes
 */
public class GastosAnual {

    private String usuario;
    private int anio;
    private double gastoTotal;

    // ==============================
    // Constructores
    // ==============================
    public GastosAnual() {
    }

    public GastosAnual(String usuario, int anio, double gastoTotal) {
        this.usuario = usuario;
        this.anio = anio;
        this.gastoTotal = gastoTotal;
    }

    // ==============================
    // Getters y Setters
    // ==============================

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public double getGastoTotal() {
        return gastoTotal;
    }

    public void setGastoTotal(double gastoTotal) {
        this.gastoTotal = gastoTotal;
    }

    // ==============================
    // Métodos adicionales
    // ==============================

    /**
     * Devuelve una representación legible del gasto anual.
     */
    @Override
    public String toString() {
        return "GastosAnual{" +
                "usuario='" + usuario + '\'' +
                ", anio=" + anio +
                ", gastoTotal=" + gastoTotal +
                '}';
    }
}
