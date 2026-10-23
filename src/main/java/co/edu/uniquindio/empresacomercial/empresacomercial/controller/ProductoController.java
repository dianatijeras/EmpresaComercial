package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.ProductoDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class ProductoController {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;        // VARCHAR(100)
    private static final int ERROR_LLAVE_FORANEA = 547;           // código de SQL Server

    private final ProductoDAO dao = new ProductoDAO();

    // Consultar lo puede hacer cualquier usuario.
    /**
     * Lista todos los productos.
     * @return
     * @throws NegocioException
     */
    public List<Producto> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /**
     * Busca un producto por su ID.
     * @param id
     * @return
     * @throws NegocioException
     */
    public Producto buscarPorId(int id) throws NegocioException {
        try {
            return dao.buscarPorId(id);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    // Crear, modificar y eliminar solo los usuarios que pueden modificar datos.
    /**
     * Crea un nuevo producto con el nombre y la categoría proporcionados.
     * @param nombre
     * @param idCategoria
     * @return
     * @throws NegocioException
     */
    public Producto crear(String nombre, int idCategoria) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);
        validarCategoria(idCategoria);

        Producto producto = new Producto(0, nombre, idCategoria);
        try {
            dao.insertar(producto);
            return producto;
        } catch (SQLException e) {
            throw errorAlGuardar(e);
        }
    }

    /**
     * Actualiza un producto existente con el ID, nombre y categoría proporcionados.
     * @param id
     * @param nombre
     * @param idCategoria
     * @throws NegocioException
     */
    public void actualizar(int id, String nombre, int idCategoria) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);
        validarCategoria(idCategoria);

        try {
            if (!dao.actualizar(new Producto(id, nombre, idCategoria))) {
                throw new NegocioException("El producto no existe.");
            }
        } catch (SQLException e) {
            throw errorAlGuardar(e);
        }
    }

    // Al eliminar un producto también se borran sus vínculos con proveedores.
    /**
     * Elimina un producto por su ID.
     * @param id
     * @throws NegocioException
     */
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

    /**
     * Verifica si el usuario actual tiene permisos para modificar datos.
     * @throws NegocioException
     */
    private void verificarPermisoModificar() throws NegocioException {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null || !usuario.puedeModificarDatos()) {
            throw new NegocioException("No tiene permisos para modificar datos.");
        }
    }

    /**
     * Valida el nombre del producto, asegurándose de que no sea nulo, vacío o exceda la longitud máxima permitida.
     * @param nombre
     * @return
     * @throws NegocioException
     */
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

    /**
     * Valida la categoría del producto, asegurándose de que sea un ID válido (mayor que cero).
     * @param idCategoria
     * @throws NegocioException
     */
    private void validarCategoria(int idCategoria) throws NegocioException {
        if (idCategoria <= 0) {
            throw new NegocioException("Seleccione una categoría.");
        }
    }

    // Al guardar, un error de llave foránea significa que la categoría no existe.
    /**
     * Maneja los errores al guardar un producto, verificando si el error es debido a una llave foránea (categoría inexistente)
     * o un error general de base de datos.
     * @param e
     * @return
     */
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