package co.edu.uniquindio.empresacomercial.empresacomercial.DAO;

import co.edu.uniquindio.empresacomercial.empresacomercial.model.FiltrosReporte;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Reporte;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Reporte.Filtro;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.ResultadoConsulta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de los reportes (solo lectura). Cada reporte tiene su SELECT; el WHERE
 * se arma con solo los filtros que el usuario eligió, y los valores siempre
 * van como parámetros (?), nunca pegados al texto del SQL.
 * El esquema es el de script_proveedores.sql.
 */
public class ReporteDAO {

    /** El SQL listo para ejecutar y sus parámetros, en orden. */
    record Sentencia(String sql, List<Object> parametros) {
    }

    public ResultadoConsulta consultar(Reporte reporte, FiltrosReporte filtros) throws SQLException {
        Sentencia sentencia = construir(reporte, filtros);

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sentencia.sql())) {
            for (int i = 0; i < sentencia.parametros().size(); i++) {
                ps.setObject(i + 1, sentencia.parametros().get(i));
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

    // Arma el SQL de un reporte con sus filtros. Está separado de consultar() para poder probarlo sin base de datos.
    static Sentencia construir(Reporte reporte, FiltrosReporte filtros) {
        String select;
        String cola;      // GROUP BY y ORDER BY, van después del WHERE

        switch (reporte) {
            case PRODUCTOS_POR_PROVEEDOR -> {
                select = "SELECT pr.nombre AS Proveedor, p.nombre AS Producto, pp.precio AS Precio "
                        + "FROM Proveedor_Producto pp "
                        + "INNER JOIN Proveedor pr ON pr.nit = pp.nitProveedor "
                        + "INNER JOIN Producto p ON p.id = pp.idProducto";
                cola = " ORDER BY pr.nombre, p.nombre";
            }
            case DIRECTORIO_DE_PROVEEDORES -> {
                select = "SELECT pr.nombre AS Proveedor, pr.nit AS NIT, pr.telefono AS Telefono, "
                        + "pr.direccion AS Direccion, ci.nombre AS Ciudad, COUNT(pp.idProducto) AS Productos "
                        + "FROM Proveedor pr "
                        + "INNER JOIN Ciudad ci ON ci.id = pr.idCiudad "
                        + "LEFT JOIN Proveedor_Producto pp ON pp.nitProveedor = pr.nit";
                cola = " GROUP BY pr.nombre, pr.nit, pr.telefono, pr.direccion, ci.nombre"
                        + " ORDER BY ci.nombre, pr.nombre";
            }
            case PROVEEDORES_DE_UN_PRODUCTO -> {
                select = "SELECT p.nombre AS Producto, pr.nombre AS Proveedor, ci.nombre AS Ciudad, pp.precio AS Precio "
                        + "FROM Proveedor_Producto pp "
                        + "INNER JOIN Producto p ON p.id = pp.idProducto "
                        + "INNER JOIN Proveedor pr ON pr.nit = pp.nitProveedor "
                        + "INNER JOIN Ciudad ci ON ci.id = pr.idCiudad";
                cola = " ORDER BY p.nombre, pp.precio, pr.nombre";
            }
            case PRODUCTOS_POR_CATEGORIA -> {
                select = "SELECT c.nombre AS Categoria, p.nombre AS Producto, COUNT(pp.nitProveedor) AS Proveedores, "
                        + "MIN(pp.precio) AS PrecioMinimo, MAX(pp.precio) AS PrecioMaximo "
                        + "FROM Categoria c "
                        + "INNER JOIN Producto p ON p.idCategoria = c.id "
                        + "LEFT JOIN Proveedor_Producto pp ON pp.idProducto = p.id";
                cola = " GROUP BY c.nombre, p.nombre ORDER BY c.nombre, p.nombre";
            }
            case OFERTAS_POR_CATEGORIA -> {
                select = "SELECT c.nombre AS Categoria, p.nombre AS Producto, pr.nombre AS Proveedor, pp.precio AS Precio "
                        + "FROM Categoria c "
                        + "INNER JOIN Producto p ON p.idCategoria = c.id "
                        + "INNER JOIN Proveedor_Producto pp ON pp.idProducto = p.id "
                        + "INNER JOIN Proveedor pr ON pr.nit = pp.nitProveedor";
                cola = " ORDER BY c.nombre, p.nombre, pp.precio";
            }
            default -> throw new IllegalArgumentException("Reporte desconocido: " + reporte);
        }

        // Solo se aplican los filtros que ese reporte usa y que el usuario eligió
        List<String> condiciones = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();
        for (Filtro filtro : reporte.getFiltros()) {
            Object valor = valorDe(filtro, filtros);
            if (valor != null) {
                condiciones.add(condicion(filtro));
                parametros.add(valor);
            }
        }

        String where = condiciones.isEmpty() ? "" : " WHERE " + String.join(" AND ", condiciones);
        return new Sentencia(select + where + cola, parametros);
    }

    // Condición de cada filtro. Los alias (pr, ci, p, c, pp) son los mismos en todos los reportes.
    private static String condicion(Filtro filtro) {
        return switch (filtro) {
            case PROVEEDOR -> "pr.nit = ?";
            case CIUDAD -> "ci.id = ?";
            case PRODUCTO -> "p.id = ?";
            case CATEGORIA -> "c.id = ?";
            case PRECIO_MIN -> "pp.precio >= ?";
            case PRECIO_MAX -> "pp.precio <= ?";
        };
    }

    private static Object valorDe(Filtro filtro, FiltrosReporte f) {
        return switch (filtro) {
            case PROVEEDOR -> f.nitProveedor();
            case CIUDAD -> f.idCiudad();
            case PRODUCTO -> f.idProducto();
            case CATEGORIA -> f.idCategoria();
            case PRECIO_MIN -> f.precioMin() == null ? null : BigDecimal.valueOf(f.precioMin());
            case PRECIO_MAX -> f.precioMax() == null ? null : BigDecimal.valueOf(f.precioMax());
        };
    }
}