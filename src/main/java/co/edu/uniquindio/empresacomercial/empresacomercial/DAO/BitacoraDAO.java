package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BitacoraDAO {

    // Guarda un ingreso nuevo (sin salida todavía) y copia el id generado al objeto.
    public boolean registrarEntrada(Bitacora bitacora) throws SQLException {
        String sql = "INSERT INTO Bitacora (idUsuario, fechaHoraEntrada) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, bitacora.getIdUsuario());
            ps.setTimestamp(2, Timestamp.valueOf(bitacora.getFechaHoraEntrada()));
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

    // Completa el registro con la fecha y hora de salida.
    public boolean registrarSalida(Bitacora bitacora) throws SQLException {
        String sql = "UPDATE Bitacora SET fechaHoraSalida = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(bitacora.getFechaHoraSalida()));
            ps.setInt(2, bitacora.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // READ: todos los registros, del más reciente al más antiguo.
    public List<Bitacora> listar() throws SQLException {
        String sql = "SELECT id, idUsuario, fechaHoraEntrada, fechaHoraSalida "
                + "FROM Bitacora ORDER BY fechaHoraEntrada DESC";
        List<Bitacora> registros = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp salida = rs.getTimestamp("fechaHoraSalida");
                LocalDateTime fechaSalida = (salida != null) ? salida.toLocalDateTime() : null;

                registros.add(new Bitacora(
                        rs.getInt("id"),
                        rs.getLong("idUsuario"),
                        rs.getTimestamp("fechaHoraEntrada").toLocalDateTime(),
                        fechaSalida));
            }
        }
        return registros;
    }
}
