package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.CiudadDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Ciudad;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class CiudadController {
    private static final int LONGITUD_MAXIMA_NOMBRE = 60;
    private static final int ERROR_LLAVE_FORANEA = 547;

    private final CiudadDAO dao = new CiudadDAO();

    /**
     * Lista todas las ciudades.
     * @return
     * @throws NegocioException
     */
    public List<Ciudad> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /**
     * Busca una ciudad por su ID.
     * @param id
     * @return
     * @throws NegocioException
     */
    public Ciudad buscarPorId(int id) throws NegocioException {
        try {
            return dao.buscarPorId(id);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    // Crear, modificar y eliminar solo los usuarios que pueden modificar datos.
    /**
     * Crea una nueva ciudad con el nombre proporcionado.
     * @param nombre
     * @return
     * @throws NegocioException
     */
    public Ciudad crear(String nombre) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);

        Ciudad ciudad = new Ciudad();
        ciudad.setNombre(nombre);
        try {
            dao.insertar(ciudad);
            return ciudad;
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /**
     * Actualiza una ciudad existente con el ID y nombre proporcionados.
     * @param id
     * @param nombre
     * @throws NegocioException
     */
    public void actualizar(int id, String nombre) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);

        try {
            if (!dao.actualizar(new Ciudad(id, nombre))) {
                throw new NegocioException("La ciudad no existe.");
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /**
     * Elimina una ciudad por su ID. No se puede eliminar si tiene proveedores asociados.
     * @param id
     * @throws NegocioException
     */
    public void eliminar(int id) throws NegocioException {
        verificarPermisoModificar();

        try {
            if (!dao.eliminar(id)) {
                throw new NegocioException("La ciudad no existe.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                throw new NegocioException("No se puede eliminar la ciudad porque tiene proveedores asociados.");
            }
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
     * Valida el nombre de la ciudad. No puede estar vacío ni exceder la longitud máxima.
     * @param nombre
     * @return
     * @throws NegocioException
     */
    private String validarNombre(String nombre) throws NegocioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NegocioException("El nombre de la ciudad es obligatorio.");
        }
        nombre = nombre.trim();
        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new NegocioException("El nombre no puede tener más de " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }
        return nombre;
    }

    private NegocioException errorBD(SQLException e) {
        return new NegocioException("Error en la base de datos: " + e.getMessage());
    }
}
