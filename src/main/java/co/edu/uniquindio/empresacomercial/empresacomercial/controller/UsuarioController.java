package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.UsuarioDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.TipoUsuario;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class UsuarioController {

    private static final int LONGITUD_MAXIMA_CEDULA = 15;
    private static final int LONGITUD_MAXIMA_NOMBRE = 60;        // VARCHAR(60)
    private static final int ERROR_LLAVE_FORANEA = 547;          // código de SQL Server
    private static final int ERROR_INDICE_DUPLICADO = 2601;      // segundo administrador
    private static final int ERROR_LLAVE_DUPLICADA = 2627;       // cédula repetida

    private final UsuarioDAO dao = new UsuarioDAO();

    public List<Usuario> listar() throws NegocioException {
        verificarAdministrador();
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public Usuario buscarPorId(long id) throws NegocioException {
        verificarAdministrador();
        try {
            return dao.buscarPorId(id);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public Usuario crear(String cedula, String nombre, String apellido,
                         String password, TipoUsuario tipo) throws NegocioException {
        verificarAdministrador();
        long id = validarCedula(cedula);
        nombre = validarTexto(nombre, "El nombre");
        apellido = validarTexto(apellido, "El apellido");
        if (password == null || password.isEmpty()) {
            throw new NegocioException("La contraseña es obligatoria.");
        }
        if (tipo == null) {
            throw new NegocioException("Seleccione el tipo de usuario.");
        }

        Usuario usuario = new Usuario(id, nombre, apellido, password, tipo);
        try {
            dao.insertar(usuario);
            return usuario;
        } catch (SQLException e) {
            throw errorAlGuardar(e);
        }
    }

    // La cédula no se cambia. Si la contraseña viene vacía se conserva la actual.
    public void actualizar(long id, String nombre, String apellido,
                           String password, TipoUsuario tipo) throws NegocioException {
        verificarAdministrador();
        nombre = validarTexto(nombre, "El nombre");
        apellido = validarTexto(apellido, "El apellido");
        if (tipo == null) {
            throw new NegocioException("Seleccione el tipo de usuario.");
        }

        try {
            Usuario actual = dao.buscarPorId(id);
            if (actual == null) {
                throw new NegocioException("El usuario no existe.");
            }
            // Si el administrador cambiara de tipo, nadie más podría crear usuarios.
            if (actual.esAdministrador() && tipo != TipoUsuario.ADMINISTRADOR) {
                throw new NegocioException("El administrador no puede cambiar de tipo.");
            }
            if (password == null || password.isEmpty()) {
                password = actual.getPassword();
            }
            dao.actualizar(new Usuario(id, nombre, apellido, password, tipo));
        } catch (SQLException e) {
            throw errorAlGuardar(e);
        }
    }

    public void eliminar(long id) throws NegocioException {
        verificarAdministrador();

        try {
            Usuario usuario = dao.buscarPorId(id);
            if (usuario == null) {
                throw new NegocioException("El usuario no existe.");
            }
            if (usuario.esAdministrador()) {
                throw new NegocioException("El administrador no se puede eliminar.");
            }
            dao.eliminar(id);
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                throw new NegocioException("No se puede eliminar el usuario porque tiene registros en la bitácora.");
            }
            throw errorBD(e);
        }
    }


    private void verificarAdministrador() throws NegocioException {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null || !usuario.puedeCrearUsuario()) {
            throw new NegocioException("Solo el administrador puede gestionar usuarios.");
        }
    }

    private long validarCedula(String cedula) throws NegocioException {
        if (cedula == null || cedula.trim().isEmpty()) {
            throw new NegocioException("La cédula es obligatoria.");
        }
        cedula = cedula.trim();
        if (!cedula.matches("\\d+")) {
            throw new NegocioException("La cédula debe contener solo números.");
        }
        if (cedula.length() > LONGITUD_MAXIMA_CEDULA) {
            throw new NegocioException("La cédula no puede tener más de " + LONGITUD_MAXIMA_CEDULA + " dígitos.");
        }
        long id = Long.parseLong(cedula);
        if (id <= 0) {
            throw new NegocioException("La cédula debe ser mayor que cero.");
        }
        return id;
    }

    private String validarTexto(String valor, String nombreCampo) throws NegocioException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new NegocioException(nombreCampo + " es obligatorio.");
        }
        valor = valor.trim();
        if (valor.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new NegocioException(nombreCampo + " no puede tener más de " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }
        return valor;
    }

    private NegocioException errorAlGuardar(SQLException e) {
        if (e.getErrorCode() == ERROR_LLAVE_DUPLICADA) {
            return new NegocioException("Ya existe un usuario con esa cédula.");
        }
        if (e.getErrorCode() == ERROR_INDICE_DUPLICADO) {
            return new NegocioException("Ya existe un administrador. Solo puede haber uno.");
        }
        return errorBD(e);
    }

    private NegocioException errorBD(SQLException e) {
        return new NegocioException("Error en la base de datos: " + e.getMessage());
    }
}
