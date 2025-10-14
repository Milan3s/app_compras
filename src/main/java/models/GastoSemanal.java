package models;

/**
 * Modelo que representa un gasto semanal, basado en la vista SQL "vista_gasto_semanal".
 */
public class GastoSemanal {

    private String usuario;
    private int anio;
    private String mes;
    private int semanaDelMes;
    private String inicioSemana;
    private String finSemana;
    private double gastoSemana;

    public GastoSemanal() {
    }

    public GastoSemanal(String usuario, int anio, String mes, int semanaDelMes,
                        String inicioSemana, String finSemana, double gastoSemana) {
        this.usuario = usuario;
        this.anio = anio;
        this.mes = mes;
        this.semanaDelMes = semanaDelMes;
        this.inicioSemana = inicioSemana;
        this.finSemana = finSemana;
        this.gastoSemana = gastoSemana;
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
        // Evitar nulos y uniformar formato (primera letra mayúscula)
        if (mes != null && !mes.isEmpty()) {
            this.mes = mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase();
        } else {
            this.mes = mes;
        }
    }

    public int getSemanaDelMes() {
        return semanaDelMes;
    }

    public void setSemanaDelMes(int semanaDelMes) {
        this.semanaDelMes = semanaDelMes;
    }

    public String getInicioSemana() {
        return inicioSemana;
    }

    public void setInicioSemana(String inicioSemana) {
        this.inicioSemana = inicioSemana;
    }

    public String getFinSemana() {
        return finSemana;
    }

    public void setFinSemana(String finSemana) {
        this.finSemana = finSemana;
    }

    public double getGastoSemana() {
        return gastoSemana;
    }

    public void setGastoSemana(double gastoSemana) {
        this.gastoSemana = gastoSemana;
    }

    // ==============================
    // Métodos adicionales útiles
    // ==============================

    /**
     * Devuelve el rango de fechas de la semana (ej: "01/10/2025 - 07/10/2025").
     */
    public String getRangoSemana() {
        if (inicioSemana != null && finSemana != null) {
            return inicioSemana + " - " + finSemana;
        }
        return "";
    }

    /**
     * Devuelve un formato amigable para la vista de tabla.
     */
    @Override
    public String toString() {
        return "GastoSemanal{" +
                "usuario='" + usuario + '\'' +
                ", anio=" + anio +
                ", mes='" + mes + '\'' +
                ", semanaDelMes=" + semanaDelMes +
                ", inicioSemana='" + inicioSemana + '\'' +
                ", finSemana='" + finSemana + '\'' +
                ", gastoSemana=" + gastoSemana +
                '}';
    }
}
