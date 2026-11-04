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
        // En la tabla la fecha y la hora van en columnas separadas
        String sql = "INSERT INTO Bitacora (idUsuario, fechaEntrada, horaEntrada) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime entrada = bitacora.getFechaHoraEntrada();
            ps.setLong(1, bitacora.getIdUsuario());
            ps.setDate(2, Date.valueOf(entrada.toLocalDate()));
            ps.setTime(3, Time.valueOf(entrada.toLocalTime()));
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

            LocalDateTime salida = bitacora.getFechaHoraSalida();
            ps.setDate(1, Date.valueOf(salida.toLocalDate()));
            ps.setTime(2, Time.valueOf(salida.toLocalTime()));
            ps.setInt(3, bitacora.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // READ: todos los registros, del más reciente al más antiguo.
    public List<Bitacora> listar() throws SQLException {
        String sql = "SELECT id, idUsuario, fechaEntrada, horaEntrada, fechaSalida, horaSalida "
                + "FROM Bitacora ORDER BY fechaEntrada DESC, horaEntrada DESC, id DESC";
        List<Bitacora> registros = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Se junta la fecha y la hora en un solo LocalDateTime
                LocalDateTime entrada = LocalDateTime.of(
                        rs.getDate("fechaEntrada").toLocalDate(),
                        rs.getTime("horaEntrada").toLocalTime());

                // La salida queda vacía (null) mientras la sesión esté abierta
                Date fechaSalida = rs.getDate("fechaSalida");
                Time horaSalida = rs.getTime("horaSalida");
                LocalDateTime salida = (fechaSalida != null && horaSalida != null)
                        ? LocalDateTime.of(fechaSalida.toLocalDate(), horaSalida.toLocalTime())
                        : null;

                registros.add(new Bitacora(
                        rs.getInt("id"),
                        rs.getLong("idUsuario"),
                        entrada,
                        salida));
            }
        }
        return registros;
    }
}