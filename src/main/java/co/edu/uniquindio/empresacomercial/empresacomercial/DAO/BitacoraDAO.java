package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class BitacoraDAO {

    // Guarda un ingreso nuevo (sin salida todavía) y copia el id generado al objeto.
    public boolean registrarEntrada(Bitacora bitacora) throws SQLException {
        // En la tabla la fecha y la hora van en columnas separadas
        String sql = "INSERT INTO Bitacora (idUsuario, fechaEntrada, horaEntrada) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, bitacora.getIdUsuario());
            ps.setDate(2, Date.valueOf(bitacora.getFechaEntrada()));
            ps.setTime(3, Time.valueOf(bitacora.getHoraEntrada()));
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

            ps.setDate(1, Date.valueOf(bitacora.getFechaSalida()));
            ps.setTime(2, Time.valueOf(bitacora.getHoraSalida()));
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
                // La salida queda vacía (null) mientras la sesión esté abierta
                Date fechaSalida = rs.getDate("fechaSalida");
                Time horaSalida = rs.getTime("horaSalida");

                registros.add(new Bitacora(
                        rs.getInt("id"),
                        rs.getLong("idUsuario"),
                        rs.getDate("fechaEntrada").toLocalDate(),
                        rs.getTime("horaEntrada").toLocalTime(),
                        fechaSalida != null ? fechaSalida.toLocalDate() : null,
                        horaSalida != null ? horaSalida.toLocalTime() : null));
            }
        }
        return registros;
    }
}