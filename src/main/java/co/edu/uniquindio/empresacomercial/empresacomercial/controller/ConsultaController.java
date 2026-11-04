package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.ConsultaDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Consulta;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.ResultadoConsulta;

import java.sql.SQLException;

public class ConsultaController {

    private final ConsultaDAO dao = new ConsultaDAO();

    // Consultar lo puede hacer cualquier usuario con sesión iniciada (también el esporádico).
    public ResultadoConsulta ejecutar(Consulta consulta, Integer idParametro) throws NegocioException {
        if (Sesion.getUsuarioActual() == null) {
            throw new NegocioException("Debe iniciar sesión para consultar.");
        }
        if (consulta == null) {
            throw new NegocioException("Seleccione una consulta.");
        }
        if (consulta.necesitaParametro() && (idParametro == null || idParametro <= 0)) {
            throw new NegocioException("Seleccione una " + nombreParametro(consulta) + ".");
        }

        try {
            return dao.ejecutar(consulta, idParametro);
        } catch (SQLException e) {
            throw new NegocioException("Error en la base de datos: " + e.getMessage());
        }
    }

    private String nombreParametro(Consulta consulta) {
        return switch (consulta.getParametro()) {
            case CIUDAD -> "ciudad";
            case NINGUNO -> "opción";
        };
    }
}