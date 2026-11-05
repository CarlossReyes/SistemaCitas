package dao;

import conexion.Conexion;
import modelo. Psicologos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class PsicologoDAO {

    public boolean insertar(Psicologos p) {
        String sql = "INSERT INTO psicologos (nombre, telefono, direccion, turno, hora_entrada, hora_salida) "
                   + "VALUES (?,?,?,?,?,?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTelefono());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getTurno());
            ps.setTime(5, p.getHoraEntrada());
            ps.setTime(6, p.getHoraSalida());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage());
            return false;
        }
    }

    public List<Psicologos> listar() {
        List<Psicologos> lista = new ArrayList<>();
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM psicologos ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al listar: " + ex.getMessage());
        }
        return lista;
    }

    public Psicologos buscarPorId(int id) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM psicologos WHERE id_psicologo = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + ex.getMessage());
        }
        return null;
    }

    private Psicologos mapear(ResultSet rs) throws SQLException {
        Psicologos p = new Psicologos();
        p.setIdPsicologo(rs.getInt("id_psicologo"));
        p.setNombre(rs.getString("nombre"));
        p.setTelefono(rs.getString("telefono"));
        p.setDireccion(rs.getString("direccion"));
        p.setTurno(rs.getString("turno"));
        p.setHoraEntrada(rs.getTime("hora_entrada"));
        p.setHoraSalida(rs.getTime("hora_salida"));
        return p;
    }
}