package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.ProductoDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class ProductoController {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;        // VARCHAR(100)
    private static final double PRECIO_MAXIMO = 9_999_999_999.99; // DECIMAL(12,2)
    private static final int ERROR_LLAVE_FORANEA = 547;           // código de SQL Server

    private final ProductoDAO dao = new ProductoDAO();

    // Consultar lo puede hacer cualquier usuario.
    public List<Producto> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public Producto buscarPorId(int id) throws NegocioException {
        try {
            return dao.buscarPorId(id);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    // Crear, modificar y eliminar solo los usuarios que pueden modificar datos.
    public Producto crear(String nombre, double precio, int idCategoria) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);
        validarPrecio(precio);
        validarCategoria(idCategoria);

        Producto producto = new Producto(0, nombre, precio, idCategoria);
        try {
            dao.insertar(producto);
            return producto;
        } catch (SQLException e) {
            throw errorAlGuardar(e);
        }
    }

    public void actualizar(int id, String nombre, double precio, int idCategoria) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);
        validarPrecio(precio);
        validarCategoria(idCategoria);

        try {
            if (!dao.actualizar(new Producto(id, nombre, precio, idCategoria))) {
                throw new NegocioException("El producto no existe.");
            }
        } catch (SQLException e) {
            throw errorAlGuardar(e);
        }
    }

    // Al eliminar un producto también se borran sus vínculos con proveedores.
    public void eliminar(int id) throws NegocioException {
        verificarPermisoModificar();

        try {
            if (!dao.eliminar(id)) {
                throw new NegocioException("El producto no existe.");
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    private void verificarPermisoModificar() throws NegocioException {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null || !usuario.puedeModificarDatos()) {
            throw new NegocioException("No tiene permisos para modificar datos.");
        }
    }

    private String validarNombre(String nombre) throws NegocioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NegocioException("El nombre del producto es obligatorio.");
        }
        nombre = nombre.trim();
        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new NegocioException("El nombre no puede tener más de " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }
        return nombre;
    }

    private void validarPrecio(double precio) throws NegocioException {
        if (Double.isNaN(precio) || precio < 0) {
            throw new NegocioException("El precio no puede ser negativo.");
        }
        if (precio > PRECIO_MAXIMO) {
            throw new NegocioException("El precio es demasiado alto.");
        }
    }

    private void validarCategoria(int idCategoria) throws NegocioException {
        if (idCategoria <= 0) {
            throw new NegocioException("Seleccione una categoría.");
        }
    }

    // Al guardar, un error de llave foránea significa que la categoría no existe.
    private NegocioException errorAlGuardar(SQLException e) {
        if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
            return new NegocioException("La categoría seleccionada no existe.");
        }
        return errorBD(e);
    }

    private NegocioException errorBD(SQLException e) {
        return new NegocioException("Error en la base de datos: " + e.getMessage());
    }
}
