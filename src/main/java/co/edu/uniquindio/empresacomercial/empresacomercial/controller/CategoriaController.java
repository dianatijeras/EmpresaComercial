package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.CategoriaDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Categoria;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class CategoriaController {

    private static final int LONGITUD_MAXIMA_NOMBRE = 60;        // VARCHAR(60)
    private static final int LONGITUD_MAXIMA_DESCRIPCION = 200;  // VARCHAR(200)
    private static final int ERROR_LLAVE_FORANEA = 547;          // código de SQL Server

    private final CategoriaDAO dao = new CategoriaDAO();

    // Consultar lo puede hacer cualquier usuario.
    public List<Categoria> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public Categoria buscarPorId(int id) throws NegocioException {
        try {
            return dao.buscarPorId(id);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    // Crear, modificar y eliminar solo los usuarios que pueden modificar datos.
    public Categoria crear(String nombre, String descripcion) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);
        descripcion = validarDescripcion(descripcion);

        Categoria categoria = new Categoria(0, nombre, descripcion);
        try {
            dao.insertar(categoria);
            return categoria;
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void actualizar(int id, String nombre, String descripcion) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);
        descripcion = validarDescripcion(descripcion);

        try {
            if (!dao.actualizar(new Categoria(id, nombre, descripcion))) {
                throw new NegocioException("La categoría no existe.");
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void eliminar(int id) throws NegocioException {
        verificarPermisoModificar();

        try {
            if (!dao.eliminar(id)) {
                throw new NegocioException("La categoría no existe.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                throw new NegocioException("No se puede eliminar la categoría porque tiene productos asociados.");
            }
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
            throw new NegocioException("El nombre de la categoría es obligatorio.");
        }
        nombre = nombre.trim();
        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new NegocioException("El nombre no puede tener más de " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }
        return nombre;
    }

    // La descripción es opcional: si viene vacía se guarda como null.
    private String validarDescripcion(String descripcion) throws NegocioException {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        descripcion = descripcion.trim();
        if (descripcion.length() > LONGITUD_MAXIMA_DESCRIPCION) {
            throw new NegocioException("La descripción no puede tener más de " + LONGITUD_MAXIMA_DESCRIPCION + " caracteres.");
        }
        return descripcion;
    }

    private NegocioException errorBD(SQLException e) {
        return new NegocioException("Error en la base de datos: " + e.getMessage());
    }
}
