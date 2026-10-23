package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.BitacoraDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class BitacoraController {

    private final BitacoraDAO dao = new BitacoraDAO();

    /**
     * Lista todas las entradas de la bitácora.
     * Solo el administrador puede consultar la bitácora.
     * @return
     * @throws NegocioException
     */
    public List<Bitacora> listar() throws NegocioException {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null || !usuario.esAdministrador()) {
            throw new NegocioException("Solo el administrador puede consultar la bitácora.");
        }

        try {
            return dao.listar();
        } catch (SQLException e) {
            throw new NegocioException("Error en la base de datos: " + e.getMessage());
        }
    }
}
