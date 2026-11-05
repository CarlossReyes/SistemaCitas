package dao;

import conexion.Conexion;
import modelo.Sesion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class SesionDAO {

    public boolean registrar(Sesion s) {
        String sqlSesion = "INSERT INTO sesiones (id_cita, fecha_hora_real, objetivos, tecnicas, tareas, estado_animico) "
                         + "VALUES (?,?,?,?,?,?)";
        String sqlCita = "UPDATE citas SET estado = 'ATENDIDA' WHERE id_cita = ?";

        Connection con = Conexion.conectar();
        try {
            con.setAutoCommit(false);   // empieza la transacción

            PreparedStatement ps1 = con.prepareStatement(sqlSesion);
            ps1.setInt(1, s.getIdCita());
            ps1.setTimestamp(2, s.getFechaHoraReal());
            ps1.setString(3, s.getObjetivos());
            ps1.setString(4, s.getTecnicas());
            ps1.setString(5, s.getTareas());
            ps1.setString(6, s.getEstadoAnimico());
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(sqlCita);
            ps2.setInt(1, s.getIdCita());
            ps2.executeUpdate();

            con.commit();               // todo salió bien: se guardan las dos
            return true;

        } catch (SQLException ex) {
            try { con.rollback(); } catch (SQLException e) { }   // algo falló: se deshace todo
            JOptionPane.showMessageDialog(null, "Error al registrar sesión: " + ex.getMessage());
            return false;
        } finally {
            try { con.close(); } catch (SQLException e) { }
        }
    }

    // Historial de sesiones de un estudiante (notas de evolución)
    public List<Sesion> historialEstudiante(int idEstudiante) {
        List<Sesion> lista = new ArrayList<>();
        String sql = "SELECT s.* FROM sesiones s "
                   + "JOIN citas c ON s.id_cita = c.id_cita "
                   + "WHERE c.id_estudiante = ? "
                   + "ORDER BY s.fecha_hora_real DESC";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Sesion s = new Sesion();
                s.setIdSesion(rs.getInt("id_sesion"));
                s.setIdCita(rs.getInt("id_cita"));
                s.setFechaHoraReal(rs.getTimestamp("fecha_hora_real"));
                s.setObjetivos(rs.getString("objetivos"));
                s.setTecnicas(rs.getString("tecnicas"));
                s.setTareas(rs.getString("tareas"));
                s.setEstadoAnimico(rs.getString("estado_animico"));
                lista.add(s);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
        return lista;
    }
    public List<Cita> listarProgramadas() {
    List<Cita> lista = new ArrayList<>();
    String sql = "SELECT * FROM citas WHERE estado = 'PROGRAMADA' ORDER BY fecha, hora_inicio";
    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            lista.add(mapear(rs));
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
    }
    return lista;
}

public List<Cita> listar() {
    List<Cita> lista = new ArrayList<>();
    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM citas ORDER BY fecha DESC, hora_inicio");
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            lista.add(mapear(rs));
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
    }
    return lista;
}

private Cita mapear(ResultSet rs) throws SQLException {
    Cita c = new Cita();
    c.setIdCita(rs.getInt("id_cita"));
    c.setIdEstudiante(rs.getInt("id_estudiante"));
    c.setIdPsicologo(rs.getInt("id_psicologo"));
    c.setFecha(rs.getDate("fecha"));
    c.setTurno(rs.getString("turno"));
    c.setHoraInicio(rs.getTime("hora_inicio"));
    c.setHoraFin(rs.getTime("hora_fin"));
    c.setTipoSesion(rs.getString("tipo_sesion"));
    c.setModalidad(rs.getString("modalidad"));
    c.setEstado(rs.getString("estado"));
    return c;
}
}