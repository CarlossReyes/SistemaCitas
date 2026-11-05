package modelo;

import java.sql.Date;
import java.sql.Time;

public class Cita {
    private int idCita;
    private int idEstudiante;
    private int idPsicologo;
    private Date fecha;
    private String turno;
    private Time horaInicio;
    private Time horaFin;
    private String tipoSesion;
    private String modalidad;
    private String estado;
}