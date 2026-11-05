package modelo;

import java.sql.Time;

public class Psicologos {
    private int idPsicologo;
    private String nombre;
    private String telefono;
    private String direccion;
    private String turno;
    private Time horaEntrada;
    private Time horaSalida;

    @Override
    public String toString() {
        return nombre;
    }
}