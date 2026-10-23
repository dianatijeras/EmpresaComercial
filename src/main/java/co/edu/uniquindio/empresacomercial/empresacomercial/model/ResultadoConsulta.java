
package co.edu.uniquindio.empresacomercial.empresacomercial.model;

import java.util.List;

/** Resultado genérico de una consulta: nombres de columnas y una lista de filas. */
public record ResultadoConsulta(List<String> columnas, List<Object[]> filas) {
}