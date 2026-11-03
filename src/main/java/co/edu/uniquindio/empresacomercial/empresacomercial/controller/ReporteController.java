package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.ReporteDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.FiltrosReporte;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Reporte;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.ResultadoConsulta;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;
import co.edu.uniquindio.empresacomercial.empresacomercial.reporte.GeneradorReportePdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class ReporteController {

    private static final double PRECIO_MAXIMO = 9_999_999_999.99; // DECIMAL(12,2)

    private final ReporteDAO dao = new ReporteDAO();

    /**
     * Consulta los datos, arma el PDF y lo guarda en el archivo indicado.
     * Devuelve cuántos registros tiene el reporte.
     *
     * @param descripcionFiltros texto que se imprime en el PDF con los filtros elegidos
     */
    public int generarPdf(Reporte reporte, FiltrosReporte filtros, String descripcionFiltros, File destino)
            throws NegocioException {
        // Reportar lo puede hacer cualquier usuario con sesión iniciada (también el esporádico).
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null) {
            throw new NegocioException("Debe iniciar sesión para generar reportes.");
        }
        if (reporte == null) {
            throw new NegocioException("Seleccione un reporte.");
        }
        if (destino == null) {
            throw new NegocioException("Indique dónde guardar el archivo.");
        }
        validarPrecios(filtros);

        ResultadoConsulta datos;
        try {
            datos = dao.consultar(reporte, filtros);
        } catch (SQLException e) {
            throw new NegocioException("Error en la base de datos: " + e.getMessage());
        }
        if (datos.filas().isEmpty()) {
            throw new NegocioException("No hay datos con los filtros elegidos. Pruebe con otros filtros.");
        }

        // El PDF se arma completo en memoria: si algo falla no queda un archivo a medias
        try {
            ByteArrayOutputStream pdf = new ByteArrayOutputStream();
            GeneradorReportePdf.generar(reporte.getTitulo(), descripcionFiltros,
                    usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getTipoUsuario() + ")",
                    reporte.getEncabezados(), datos.filas(), reporte.esHorizontal(), pdf);
            try (FileOutputStream salida = new FileOutputStream(destino)) {
                pdf.writeTo(salida);
            }
        } catch (IOException e) {
            throw new NegocioException("No se pudo guardar el archivo: " + e.getMessage());
        }
        return datos.filas().size();
    }

    private void validarPrecios(FiltrosReporte filtros) throws NegocioException {
        Double min = filtros.precioMin();
        Double max = filtros.precioMax();
        if (min != null && (Double.isNaN(min) || min < 0)) {
            throw new NegocioException("El precio mínimo no puede ser negativo.");
        }
        if (max != null && (Double.isNaN(max) || max < 0)) {
            throw new NegocioException("El precio máximo no puede ser negativo.");
        }
        if ((min != null && min > PRECIO_MAXIMO) || (max != null && max > PRECIO_MAXIMO)) {
            throw new NegocioException("El precio es demasiado alto.");
        }
        if (min != null && max != null && min > max) {
            throw new NegocioException("El precio mínimo no puede ser mayor que el máximo.");
        }
    }
}