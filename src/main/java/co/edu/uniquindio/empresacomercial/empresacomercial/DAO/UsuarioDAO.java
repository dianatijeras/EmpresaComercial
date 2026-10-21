package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.TipoUsuario;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // CREATE
    public boolean insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO Usuario (id, nombre, apellido, password, tipo) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, usuario.getId());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getApellido());
            ps.setString(4, usuario.getPassword());
            ps.setString(5, usuario.getTipoUsuario().name());
            return ps.executeUpdate() > 0;
        }
    }

    // READ: todos los usuarios ordenados por apellido y nombre.
    public List<Usuario> listar() throws SQLException {
        String sql = "SELECT id, nombre, apellido, password, tipo FROM Usuario ORDER BY apellido, nombre";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
        }
        return usuarios;
    }

    // READ: busca por cédula. Devuelve null si no existe.
    public Usuario buscarPorId(long id) throws SQLException {
        String sql = "SELECT id, nombre, apellido, password, tipo FROM Usuario WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // UPDATE: la cédula (id) no se cambia.
    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE Usuario SET nombre = ?, apellido = ?, password = ?, tipo = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getTipoUsuario().name());
            ps.setLong(5, usuario.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: si el usuario tiene registros en la bitácora, SQL Server lo rechaza (código 547).
    public boolean eliminar(long id) throws SQLException {
        String sql = "DELETE FROM Usuario WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Convierte la fila actual del resultado en un objeto Usuario.
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("password"),
                TipoUsuario.valueOf(rs.getString("tipo")));
    }
}
