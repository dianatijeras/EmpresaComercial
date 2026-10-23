package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    // CREATE: inserta una nueva categoría y devuelve true si se insertó correctamente.
    public boolean insertar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO Categoria (nombre, descripcion) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        categoria.setId(claves.getInt(1));
                    }
                }
            }
            return filas > 0;
        }
    }

    // READ: todas las categorías ordenadas por nombre.
    public List<Categoria> listar() throws SQLException {
        String sql = "SELECT id, nombre, descripcion FROM Categoria ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapear(rs));
            }
        }
        return categorias;
    }

    // READ: devuelve null si no existe.
    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, descripcion FROM Categoria WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // UPDATE
    public boolean actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE Categoria SET nombre = ?, descripcion = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: si la categoría tiene productos, SQL Server lo rechaza
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Categoria WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Convierte la fila actual del resultado en un objeto Categoria.
    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion"));
    }
}
