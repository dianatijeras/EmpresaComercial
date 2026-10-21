package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Ciudad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CiudadDAO {

    // CREATE: inserta una ciudad y devuelve true si se insertó correctamente. El id generado se asigna al objeto ciudad.
    public boolean insertar(Ciudad ciudad) throws SQLException {
        String sql = "INSERT INTO Ciudad (nombre) VALUES (?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, ciudad.getNombre());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        ciudad.setId(claves.getInt(1));
                    }
                }
            }
            return filas > 0;
        }
    }

    // READ: devuelve todas las ciudades ordenadas por nombre.
    public List<Ciudad> listar() throws SQLException {
        String sql = "SELECT id, nombre FROM Ciudad ORDER BY nombre";
        List<Ciudad> ciudades = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ciudades.add(new Ciudad(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return ciudades;
    }

    // READ: busca una ciudad por su id. Devuelve null si no existe.
    public Ciudad buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM Ciudad WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ciudad(rs.getInt("id"), rs.getString("nombre"));
                }
            }
        }
        return null;
    }

    // UPDATE: cambia el nombre de una ciudad existente.
    public boolean actualizar(Ciudad ciudad) throws SQLException {
        String sql = "UPDATE Ciudad SET nombre = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ciudad.getNombre());
            ps.setInt(2, ciudad.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: elimina una ciudad. Si tiene proveedores, SQL Server lo rechaza y lanza SQLException
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Ciudad WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
