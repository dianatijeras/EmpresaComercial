package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class BitacoraDAO {

    // Guarda un ingreso nuevo (sin salida todavía) y copia el id generado al objeto.
    public boolean registrarEntrada(Bitacora bitacora) throws SQLException {
        String sql = "INSERT INTO Bitacora (idUsuario, fechaEntrada, horaEntrada) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, bitacora.getIdUsuario());
            ps.setObject(2, bitacora.getFechaEntrada());
            ps.setObject(3, bitacora.getHoraEntrada());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        bitacora.setId(claves.getInt(1));
                    }
                }
            }
            return filas > 0;
        }
    }

    // Completa el registro con la fecha y la hora de salida.
    public boolean registrarSalida(Bitacora bitacora) throws SQLException {
        String sql = "UPDATE Bitacora SET fechaSalida = ?, horaSalida = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, bitacora.getFechaSalida());
            ps.setObject(2, bitacora.getHoraSalida());
            ps.setInt(3, bitacora.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // READ: todos los registros, del más reciente al más antiguo.
    public List<Bitacora> listar() throws SQLException {
        String sql = "SELECT id, idUsuario, fechaEntrada, horaEntrada, fechaSalida, horaSalida "
                + "FROM Bitacora ORDER BY fechaEntrada DESC, horaEntrada DESC";
        List<Bitacora> registros = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                registros.add(new Bitacora(
                        rs.getInt("id"),
                        rs.getLong("idUsuario"),
                        rs.getObject("fechaEntrada", LocalDate.class),
                        rs.getObject("horaEntrada", LocalTime.class),
                        rs.getObject("fechaSalida", LocalDate.class),   // null si la sesión sigue abierta
                        rs.getObject("horaSalida", LocalTime.class)));  // null si la sesión sigue abierta
            }
        }
        return registros;
    }
}
