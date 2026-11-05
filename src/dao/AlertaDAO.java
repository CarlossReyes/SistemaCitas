package dao;

import conexion.Conexion;
import modelo.Alerta;
import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;

public class AlertaDAO {

    public List<Alerta> generar() {
        Map<Integer, Alerta> mapa = new LinkedHashMap<>();

        try (Connection con = Conexion.conectar()) {

            // 1. Todos los estudiantes
            ResultSet rs = con.prepareStatement(
                "SELECT id_estudiante, matricula, nombre, telefono_emergencia FROM estudiantes").executeQuery();
            while (rs.next()) {
                Alerta a = new Alerta();
                a.setIdEstudiante(rs.getInt("id_estudiante"));
                a.setMatricula(rs.getString("matricula"));
                a.setNombre(rs.getString("nombre"));
                a.setTelefonoEmergencia(rs.getString("telefono_emergencia"));
                a.setUltimoEstadoAnimico("Sin sesiones");
                mapa.put(a.getIdEstudiante(), a);
            }

            // 2. Faltas consecutivas: se cuentan desde la cita más reciente
            //    hasta encontrar una ATENDIDA
            rs = con.prepareStatement(
                "SELECT id_estudiante, estado FROM citas "
              + "WHERE estado IN ('ATENDIDA', 'NO_ASISTIO') "
              + "ORDER BY id_estudiante, fecha DESC, hora_inicio DESC").executeQuery();
            Set<Integer> rachaCortada = new HashSet<>();
            while (rs.next()) {
                int id = rs.getInt("id_estudiante");
                if (rachaCortada.contains(id)) continue;
                if (rs.getString("estado").equals("NO_ASISTIO")) {
                    Alerta a = mapa.get(id);
                    a.setFaltasConsecutivas(a.getFaltasConsecutivas() + 1);
                } else {
                    rachaCortada.add(id);   // llegó a una ATENDIDA: ya no cuenta más
                }
            }

            // 3. Último estado anímico registrado
            rs = con.prepareStatement(
                "SELECT c.id_estudiante, s.estado_animico FROM sesiones s "
              + "JOIN citas c ON s.id_cita = c.id_cita "
              + "ORDER BY s.fecha_hora_real DESC").executeQuery();
            Set<Integer> yaTiene = new HashSet<>();
            while (rs.next()) {
                int id = rs.getInt("id_estudiante");
                if (yaTiene.add(id)) {   // solo el primero = el más reciente
                    mapa.get(id).setUltimoEstadoAnimico(rs.getString("estado_animico"));
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al generar alertas: " + ex.getMessage());
        }

        // 4. Prioridad
        List<Alerta> lista = new ArrayList<>(mapa.values());
        for (Alerta a : lista) {
            a.setPrioridad(calcularPrioridad(a));
        }

        // Ordenar: primero las críticas
        lista.sort(Comparator.comparingInt(a -> orden(a.getPrioridad())));
        return lista;
    }

    private String calcularPrioridad(Alerta a) {
        if (a.getFaltasConsecutivas() > 2 || "En riesgo".equals(a.getUltimoEstadoAnimico())) {
            return "Alta/Crítica";
        }
        if (a.getFaltasConsecutivas() >= 1 || "Estable".equals(a.getUltimoEstadoAnimico())) {
            return "Media";
        }
        return "Baja";
    }

    private int orden(String prioridad) {
        switch (prioridad) {
            case "Alta/Crítica": return 0;
            case "Media": return 1;
            default: return 2;
        }
    }
}