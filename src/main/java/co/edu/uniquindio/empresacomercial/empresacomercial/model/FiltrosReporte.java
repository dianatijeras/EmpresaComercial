package co.edu.uniquindio.empresacomercial.empresacomercial.model;

/**
 * Lo que el usuario eligió para filtrar un reporte. Un valor en null significa
 * "sin filtro" (todos). Los filtros que el reporte no usa se ignoran.
 */
public record FiltrosReporte(String nitProveedor, Integer idCiudad, Integer idProducto,
                             Integer idCategoria, Double precioMin, Double precioMax) {

    public static final FiltrosReporte SIN_FILTROS = new FiltrosReporte(null, null, null, null, null, null);
}