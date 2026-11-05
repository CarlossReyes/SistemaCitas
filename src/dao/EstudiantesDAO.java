package dao;

import conexion.Conexion;
import modelo.Estudiantes;
import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class EstudiantesDAO {

    // INSERTAR y generar expediente
    public boolean insertar(Estudiantes e) {
        String sql = "INSERT INTO estudiantes (matricula, nombre, carrera, semestre, edad, "
                   + "telefono_emergencia, motivo_ingreso) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getMatricula());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getCarrera());
            ps.setInt(4, e.getSemestre());
            ps.setInt(5, e.getEdad());
            ps.setString(6, e.getTelefonoEmergencia());
            ps.setString(7, e.getMotivoIngreso());
            ps.executeUpdate();

            // Recuperar el id que MySQL le asignó y generar el expediente
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                String exp = String.format("EXP-%d-%04d", Year.now().getValue(), id);

                PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE estudiantes SET expediente = ? WHERE id_estudiante = ?");
                ps2.setString(1, exp);
                ps2.setInt(2, id);
                ps2.executeUpdate();

                e.setIdEstudiante(id);
                e.setExpediente(exp);
            }
            return true;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage());
            return false;
        }
    }

    // LISTAR todos (para el JTable)
    public List<Estudiantes> listar() {
        List<Estudiantes> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY nombre";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al listar: " + ex.getMessage());
        }
        return lista;
    }

    // BUSCAR por matrícula
    public Estudiantes buscarPorMatricula(String matricula) {
        String sql = "SELECT * FROM estudiantes WHERE matricula = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al buscar: " + ex.getMessage());
        }
        return null;
    }

    // Convierte una fila del ResultSet en un objeto Estudiante
    private Estudiantes mapear(ResultSet rs) throws SQLException {
        Estudiantes e = new Estudiantes();
        e.setIdEstudiante(rs.getInt("id_estudiante"));
        e.setMatricula(rs.getString("matricula"));
        e.setNombre(rs.getString("nombre"));
        e.setCarrera(rs.getString("carrera"));
        e.setSemestre(rs.getInt("semestre"));
        e.setEdad(rs.getInt("edad"));
        e.setTelefonoEmergencia(rs.getString("telefono_emergencia"));
        e.setMotivoIngreso(rs.getString("motivo_ingreso"));
        e.setExpediente(rs.getString("expediente"));
        return e;
    }
}