package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.Consulta;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.ResultadoConsulta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas de solo lectura del enunciado. El esquema es el del script
 * script_proveedores.sql: el precio está en Proveedor_Producto (cada
 * proveedor tiene su precio por producto).
 */
public class ConsultaDAO {

    // JOIN de 4 tablas
    static final String SQL_CATALOGO =
            "SELECT p.nombre AS Producto, c.nombre AS Categoria, "
                    + "pr.nombre AS Proveedor, pp.precio AS Precio "
                    + "FROM Producto p "
                    + "INNER JOIN Categoria c ON c.id = p.idCategoria "
                    + "INNER JOIN Proveedor_Producto pp ON pp.idProducto = p.id "
                    + "INNER JOIN Proveedor pr ON pr.nit = pp.nitProveedor "
                    + "ORDER BY c.nombre, p.nombre, pp.precio";

    // JOIN de 3 tablas (LEFT JOIN: los proveedores sin productos salen con 0)
    static final String SQL_PROVEEDORES_POR_CIUDAD =
            "SELECT ci.nombre AS Ciudad, pr.nombre AS Proveedor, "
                    + "COUNT(pp.idProducto) AS Productos, AVG(pp.precio) AS PrecioPromedio "
                    + "FROM Proveedor pr "
                    + "INNER JOIN Ciudad ci ON ci.id = pr.idCiudad "
                    + "LEFT JOIN Proveedor_Producto pp ON pp.nitProveedor = pr.nit "
                    + "GROUP BY ci.nombre, pr.nit, pr.nombre "
                    + "ORDER BY ci.nombre, pr.nombre";

    // JOIN de 5 tablas, con la ciudad como parámetro
    static final String SQL_PRODUCTOS_EN_CIUDAD =
            "SELECT pr.nombre AS Proveedor, p.nombre AS Producto, "
                    + "c.nombre AS Categoria, pp.precio AS Precio "
                    + "FROM Proveedor pr "
                    + "INNER JOIN Ciudad ci ON ci.id = pr.idCiudad "
                    + "INNER JOIN Proveedor_Producto pp ON pp.nitProveedor = pr.nit "
                    + "INNER JOIN Producto p ON p.id = pp.idProducto "
                    + "INNER JOIN Categoria c ON c.id = p.idCategoria "
                    + "WHERE ci.id = ? "
                    + "ORDER BY pr.nombre, p.nombre";

    // Subconsulta correlacionada: Producto contra Proveedor_Producto
    static final String SQL_PRODUCTOS_CON_VARIOS_PROVEEDORES =
            "SELECT p.nombre AS Producto, "
                    + "(SELECT COUNT(*) FROM Proveedor_Producto pp WHERE pp.idProducto = p.id) AS Proveedores, "
                    + "(SELECT MIN(pp.precio) FROM Proveedor_Producto pp WHERE pp.idProducto = p.id) AS PrecioMinimo, "
                    + "(SELECT MAX(pp.precio) FROM Proveedor_Producto pp WHERE pp.idProducto = p.id) AS PrecioMaximo "
                    + "FROM Producto p "
                    + "WHERE (SELECT COUNT(*) FROM Proveedor_Producto pp WHERE pp.idProducto = p.id) > 1 "
                    + "ORDER BY Proveedores DESC, p.nombre";

    // Subconsulta: cada oferta contra el promedio de su producto
    static final String SQL_OFERTAS_SOBRE_EL_PROMEDIO =
            "SELECT pr.nombre AS Proveedor, pr.nit AS NIT "
                    + "FROM Proveedor pr "
                    + "WHERE pr.nit IN (SELECT pp.nitProveedor FROM Proveedor_Producto pp "
                    + "WHERE pp.precio > (SELECT AVG(precio) FROM Proveedor_Producto)) "
                    + "ORDER BY pr.nombre";

    /** idParametro solo se usa en las consultas que lo piden (la ciudad). */
    public ResultadoConsulta ejecutar(Consulta consulta, Integer idParametro) throws SQLException {
        return switch (consulta) {
            case CATALOGO -> ejecutar(SQL_CATALOGO);
            case PROVEEDORES_POR_CIUDAD -> ejecutar(SQL_PROVEEDORES_POR_CIUDAD);
            case PRODUCTOS_EN_CIUDAD -> ejecutar(SQL_PRODUCTOS_EN_CIUDAD, idParametro);
            case PRODUCTOS_CON_VARIOS_PROVEEDORES -> ejecutar(SQL_PRODUCTOS_CON_VARIOS_PROVEEDORES);
            case OFERTAS_SOBRE_EL_PROMEDIO -> ejecutar(SQL_OFERTAS_SOBRE_EL_PROMEDIO);
        };
    }

    // Ejecuta cualquier SELECT y arma las columnas y filas a partir del resultado
    private ResultadoConsulta ejecutar(String sql, Object... parametros) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cantidad = meta.getColumnCount();

                List<String> columnas = new ArrayList<>();
                for (int i = 1; i <= cantidad; i++) {
                    columnas.add(meta.getColumnLabel(i));
                }

                List<Object[]> filas = new ArrayList<>();
                while (rs.next()) {
                    Object[] fila = new Object[cantidad];
                    for (int i = 0; i < cantidad; i++) {
                        fila[i] = rs.getObject(i + 1);
                    }
                    filas.add(fila);
                }
                return new ResultadoConsulta(columnas, filas);
            }
        }
    }
}