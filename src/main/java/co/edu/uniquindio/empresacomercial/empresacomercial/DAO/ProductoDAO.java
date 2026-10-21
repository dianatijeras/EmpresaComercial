package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // CREATE: el id lo asigna SQL Server y se copia al objeto.
    public boolean insertar(Producto producto) throws SQLException {
        String sql = "INSERT INTO Producto (nombre, precio, idCategoria) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, producto.getNombre());
            ps.setBigDecimal(2, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(3, producto.getIdCategoria());
            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        producto.setId(claves.getInt(1));
                    }
                }
            }
            return filas > 0;
        }
    }

    // READ: todos los productos ordenados por nombre.
    public List<Producto> listar() throws SQLException {
        String sql = "SELECT id, nombre, precio, idCategoria FROM Producto ORDER BY nombre";
        List<Producto> productos = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }
        return productos;
    }

    // READ: devuelve null si no existe.
    public Producto buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, precio, idCategoria FROM Producto WHERE id = ?";

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
    public boolean actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE Producto SET nombre = ?, precio = ?, idCategoria = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setBigDecimal(2, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(3, producto.getIdCategoria());
            ps.setInt(4, producto.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: también se borran sus vínculos con proveedores
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Producto WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Convierte la fila actual del resultado en un objeto Producto.
    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getInt("idCategoria"));
    }
}
