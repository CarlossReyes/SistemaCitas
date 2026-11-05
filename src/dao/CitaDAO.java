package dao;

import conexion.Conexion;
import modelo.Cita;
import modelo.Psicologos;
import java.sql.*;
import javax.swing.JOptionPane;

public class CitaDAO {

    // Cuenta cuántas citas activas se enciman con la nueva.
    // columna = "id_psicologo" o "id_estudiante"
    private boolean hayTraslape(Connection con, String columna, int id, Cita c) throws SQLException {
        String sql = "SELECT COUNT(*) FROM citas "
                   + "WHERE " + columna + " = ? "
                   + "AND fecha = ? "
                   + "AND estado <> 'CANCELADA_ANTICIPADA' "
                   + "AND hora_inicio < ? "
                   + "AND hora_fin > ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setDate(2, c.getFecha());
            ps.setTime(3, c.getHoraFin());     // existente.inicio < nueva.fin
            ps.setTime(4, c.getHoraInicio());  // existente.fin > nueva.inicio
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    // Devuelve null si todo está bien, o un mensaje con el problema
    public String validar(Cita c) {
        if (!c.getHoraInicio().before(c.getHoraFin())) {
            return "La hora de inicio debe ser antes que la hora de fin.";
        }

        Psicologos p = new PsicologoDAO().buscarPorId(c.getIdPsicologo());
        if (c.getHoraInicio().before(p.getHoraEntrada()) || c.getHoraFin().after(p.getHoraSalida())) {
            return "La cita está fuera del horario del psicólogo ("
                   + p.getHoraEntrada() + " a " + p.getHoraSalida() + ").";
        }

        try (Connection con = Conexion.conectar()) {
            if (hayTraslape(con, "id_psicologo", c.getIdPsicologo(), c)) {
                return "El psicólogo ya tiene una cita en ese horario.";
            }
            if (hayTraslape(con, "id_estudiante", c.getIdEstudiante(), c)) {
                return "El estudiante ya tiene una cita en ese horario.";
            }
        } catch (SQLException ex) {
            return "Error al validar: " + ex.getMessage();
        }
        return null;
    }

    public boolean insertar(Cita c) {
        String error = validar(c);
        if (error != null) {
            JOptionPane.showMessageDialog(null, error, "No se puede agendar", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "INSERT INTO citas (id_estudiante, id_psicologo, fecha, turno, hora_inicio, hora_fin, "
                   + "tipo_sesion, modalidad, estado) VALUES (?,?,?,?,?,?,?,?, 'PROGRAMADA')";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getIdEstudiante());
            ps.setInt(2, c.getIdPsicologo());
            ps.setDate(3, c.getFecha());
            ps.setString(4, c.getTurno());
            ps.setTime(5, c.getHoraInicio());
            ps.setTime(6, c.getHoraFin());
            ps.setString(7, c.getTipoSesion());
            ps.setString(8, c.getModalidad());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage());
            return false;
        }
    }

    // Para cancelar, marcar falta o marcar atendida
    public boolean cambiarEstado(int idCita, String nuevoEstado) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("UPDATE citas SET estado = ? WHERE id_cita = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            return false;
        }
    }
}