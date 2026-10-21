package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    // CREATE: guarda el proveedor y sus productos en una sola transacción
    public boolean insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO Proveedor (nit, nombre, telefono, direccion, idCiudad) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, proveedor.getNit());
                    ps.setString(2, proveedor.getNombre());
                    ps.setString(3, proveedor.getTelefono());
                    ps.setString(4, proveedor.getDireccion());
                    ps.setInt(5, proveedor.getIdCiudad());
                    ps.executeUpdate();
                }
                guardarProductos(con, proveedor);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // READ: todos los proveedores (con sus productos) ordenados por nombre.
    public List<Proveedor> listar() throws SQLException {
        String sql = "SELECT nit, nombre, telefono, direccion, idCiudad FROM Proveedor ORDER BY nombre";
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(mapear(rs));
                }
            }
            // Primero se lee toda la lista y después se cargan los productos de cada uno.
            for (Proveedor proveedor : proveedores) {
                cargarProductos(con, proveedor);
            }
        }
        return proveedores;
    }

    // READ: devuelve null si no existe.
    public Proveedor buscarPorNit(String nit) throws SQLException {
        String sql = "SELECT nit, nombre, telefono, direccion, idCiudad FROM Proveedor WHERE nit = ?";

        try (Connection con = ConexionBD.getConexion()) {
            Proveedor proveedor = null;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nit);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        proveedor = mapear(rs);
                    }
                }
            }
            if (proveedor != null) {
                cargarProductos(con, proveedor);
            }
            return proveedor;
        }
    }

    // UPDATE: el NIT es la llave primaria y no se cambia.
    public boolean actualizar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE Proveedor SET nombre = ?, telefono = ?, direccion = ?, idCiudad = ? WHERE nit = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getTelefono());
            ps.setString(3, proveedor.getDireccion());
            ps.setInt(4, proveedor.getIdCiudad());
            ps.setString(5, proveedor.getNit());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: también se borran sus vínculos con productos
    public boolean eliminar(String nit) throws SQLException {
        String sql = "DELETE FROM Proveedor WHERE nit = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nit);
            return ps.executeUpdate() > 0;
        }
    }

    // Vincula un producto con un proveedor. Si ya estaba vinculado, SQL Server lo rechaza (llave primaria repetida).
    public boolean agregarProducto(String nit, int idProducto) throws SQLException {
        String sql = "INSERT INTO Proveedor_Producto (nitProveedor, idProducto) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nit);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    // Quita el vínculo entre un proveedor y un producto (no borra el producto).
    public boolean quitarProducto(String nit, int idProducto) throws SQLException {
        String sql = "DELETE FROM Proveedor_Producto WHERE nitProveedor = ? AND idProducto = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nit);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        }
    }


    // Guarda en Proveedor_Producto los productos de la lista del proveedor.
    private void guardarProductos(Connection con, Proveedor proveedor) throws SQLException {
        if (proveedor.getProductos().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO Proveedor_Producto (nitProveedor, idProducto) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Producto producto : proveedor.getProductos()) {
                ps.setString(1, proveedor.getNit());
                ps.setInt(2, producto.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Llena la lista de productos del proveedor con lo que hay en la base.
    private void cargarProductos(Connection con, Proveedor proveedor) throws SQLException {
        String sql = "SELECT p.id, p.nombre, p.precio, p.idCategoria "
                + "FROM Producto p "
                + "JOIN Proveedor_Producto pp ON pp.idProducto = p.id "
                + "WHERE pp.nitProveedor = ? "
                + "ORDER BY p.nombre";
        List<Producto> productos = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, proveedor.getNit());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new Producto(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("idCategoria")));
                }
            }
        }
        proveedor.setProductos(productos);
    }

    // Convierte la fila actual del resultado en un objeto Proveedor (sin productos).
    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getString("nit"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getInt("idCiudad"));
    }

}
