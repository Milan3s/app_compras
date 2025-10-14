package models;

/**
 * Modelo que representa un gasto diario,
 * basado en la vista SQL "vista_gasto_diario".
 *
 * Agrupa los gastos por usuario, año, mes y día de la semana.
 *
 * @author Milanes
 */
public class GastosDia {

    private String usuario;
    private int anio;
    private String mes;
    private String diaSemana;
    private double gastoTotal;

    public GastosDia() {
    }

    public GastosDia(String usuario, int anio, String mes, String diaSemana, double gastoTotal) {
        this.usuario = usuario;
        this.anio = anio;
        this.mes = mes;
        this.diaSemana = diaSemana;
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
        if (mes != null && !mes.isEmpty()) {
            this.mes = mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase();
        } else {
            this.mes = mes;
        }
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public double getGastoTotal() {
        return gastoTotal;
    }

    public void setGastoTotal(double gastoTotal) {
        this.gastoTotal = gastoTotal;
    }

    // ==============================
    // Métodos útiles
    // ==============================

    @Override
    public String toString() {
        return "GastosDia{" +
                "usuario='" + usuario + '\'' +
                ", anio=" + anio +
                ", mes='" + mes + '\'' +
                ", diaSemana='" + diaSemana + '\'' +
                ", gastoTotal=" + gastoTotal +
                '}';
    }
}
