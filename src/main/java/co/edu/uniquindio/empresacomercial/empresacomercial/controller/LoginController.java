package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.BitacoraDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.UsuarioDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final BitacoraDAO bitacoraDAO = new BitacoraDAO();

    /**
     * Valida la cédula y la contraseña. Si son correctas, guarda la sesión
     * y registra la entrada en la bitácora. Devuelve el usuario que ingresó.
     */
    public Usuario iniciarSesion(String cedula, String password) throws NegocioException {
        if (cedula == null || cedula.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new NegocioException("Ingrese la cédula y la contraseña.");
        }

        long id;
        try {
            id = Long.parseLong(cedula.trim());
        } catch (NumberFormatException e) {
            throw new NegocioException("La cédula debe contener solo números.");
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorId(id);

            // La contraseña se compara aquí en Java (distingue mayúsculas y minúsculas)
            if (usuario == null || !usuario.getPassword().equals(password)) {
                throw new NegocioException("Cédula o contraseña incorrecta.");
            }

            Bitacora bitacora = new Bitacora();
            bitacora.setIdUsuario(usuario.getId());
            bitacora.setFechaHoraEntrada(LocalDateTime.now());
            bitacoraDAO.registrarEntrada(bitacora);

            Sesion.iniciar(usuario, bitacora);
            return usuario;

        } catch (SQLException e) {
            throw new NegocioException("Error en la base de datos: " + e.getMessage());
        }
    }

    /**
     * Registra la salida en la bitácora y cierra la sesión.
     * La sesión se cierra aunque falle el registro en la base de datos.
     */
    public void cerrarSesion() throws NegocioException {
        Bitacora bitacora = Sesion.getBitacoraActual();
        try {
            if (bitacora != null) {
                bitacora.cerrarSesion();
                bitacoraDAO.registrarSalida(bitacora);
            }
        } catch (SQLException e) {
            throw new NegocioException("No se pudo registrar la salida: " + e.getMessage());
        } finally {
            Sesion.cerrar();
        }
    }
}
