package models;

/**
 * Modelo que representa un gasto mensual, basado en la vista SQL
 * "vista_gasto_mensual". Cada registro corresponde al gasto total de un usuario
 * en una categoría específica durante un mes y año.
 */
public class GastosMes {

    private String usuario;
    private int anio;
    private String mes;
    private String categoria;
    private double gastoTotal;

    // ==============================
    // Constructores
    // ==============================
    public GastosMes() {
    }

    public GastosMes(String usuario, int anio, String mes, String categoria, double gastoTotal) {
        this.usuario = usuario;
        this.anio = anio;
        setMes(mes);
        this.categoria = categoria;
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

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        // Uniformar formato (Primera letra mayúscula, resto minúscula)
        if (mes != null && !mes.isEmpty()) {
            this.mes = mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase();
        } else {
            this.mes = mes;
        }
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getGastoTotal() {
        return gastoTotal;
    }

    public void setGastoTotal(double gastoTotal) {
        this.gastoTotal = gastoTotal;
    }

    // ==============================
    // Métodos adicionales útiles
    // ==============================
    /**
     * Devuelve un formato legible para mostrar en la tabla o en reportes.
     */
    @Override
    public String toString() {
        return "GastosMes{"
                + "usuario='" + usuario + '\''
                + ", anio=" + anio
                + ", mes='" + mes + '\''
                + ", categoria='" + categoria + '\''
                + ", gastoTotal=" + gastoTotal
                + '}';
    }

    /**
     * Devuelve un texto de resumen del gasto mensual. Ejemplo: "Octubre 2025 -
     * Lácteos: 35.50 €"
     */
    public String getResumen() {
        return String.format("%s %d - %s: %.2f €", mes, anio, categoria, gastoTotal);
    }
}
