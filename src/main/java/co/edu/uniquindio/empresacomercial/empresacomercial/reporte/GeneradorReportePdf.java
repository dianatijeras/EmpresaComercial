package co.edu.uniquindio.empresacomercial.empresacomercial.reporte;

import co.edu.uniquindio.empresacomercial.empresacomercial.reporte.PdfBasico.Fuente;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Arma un reporte en forma de tabla: título, filtros usados, quién lo generó,
 * la tabla con su encabezado repetido en cada página, el total de registros
 * y "Página x de y" al pie.
 */
public class GeneradorReportePdf {

    private static final float MARGEN = 36f;
    private static final float ALTO_FILA = 16f;
    private static final float ALTO_ENCABEZADO = 18f;
    private static final float RELLENO = 4f;       // espacio a cada lado del texto de una celda
    private static final float TAMANO = 9f;        // tamaño de la letra de la tabla
    private static final float ESPACIO_PIE = 44f;  // espacio libre abajo para el pie de página
    private static final int MAX_LINEAS_FILTROS = 3;

    private static final float[] OSCURO = {0.13f, 0.18f, 0.24f};
    private static final float[] GRIS = {0.35f, 0.35f, 0.35f};
    private static final float[] BLANCO = {1f, 1f, 1f};
    private static final float[] FRANJA = {0.95f, 0.96f, 0.97f};
    private static final float[] BORDE = {0.80f, 0.82f, 0.85f};

    private GeneradorReportePdf() {
    }

    /**
     * @param filtros     texto que describe los filtros usados (ya armado)
     * @param generadoPor quién generó el reporte
     * @param encabezados nombres de las columnas
     * @param filas       una fila por registro; los números salen alineados a la derecha
     *                    y los decimales (BigDecimal, Double) como dinero
     * @param horizontal  true para hoja A4 horizontal (reportes con muchas columnas)
     */
    public static void generar(String titulo, String filtros, String generadoPor,
                               String[] encabezados, List<Object[]> filas, boolean horizontal,
                               OutputStream destino) throws IOException {
        float ancho = horizontal ? PdfBasico.ALTO_A4 : PdfBasico.ANCHO_A4;
        float alto = horizontal ? PdfBasico.ANCHO_A4 : PdfBasico.ALTO_A4;
        PdfBasico pdf = new PdfBasico(ancho, alto, titulo);

        // ---------- Datos de la tabla ----------
        int columnas = encabezados.length;
        String[][] texto = new String[filas.size()][columnas];
        boolean[] numerica = new boolean[columnas];
        for (int i = 0; i < filas.size(); i++) {
            for (int c = 0; c < columnas; c++) {
                Object valor = c < filas.get(i).length ? filas.get(i)[c] : null;
                texto[i][c] = formatear(valor);
                if (valor instanceof Number) {
                    numerica[c] = true;
                }
            }
        }
        float[] anchos = calcularAnchos(encabezados, texto, ancho - 2 * MARGEN);

        // ---------- Posiciones verticales ----------
        float arriba = alto - MARGEN;
        List<String> lineasFiltros = partir("Filtros: " + filtros, ancho - 2 * MARGEN, MAX_LINEAS_FILTROS);
        String generado = "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + "   |   Por: " + generadoPor;

        float lineaTitulo = arriba - 14;
        float primeraLineaFiltros = lineaTitulo - 18;
        float lineaGenerado = primeraLineaFiltros - 12 * lineasFiltros.size();
        float tablaPrimera = lineaGenerado - 12;     // donde empieza la tabla en la primera página
        float tablaOtras = arriba - 11 - 14;         // y en las demás

        // ---------- Reparto de filas por página ----------
        // La última línea de la tabla es "Total de registros", por eso hay una más que filas
        int lineas = filas.size() + 1;
        List<int[]> paginas = new ArrayList<>();
        int desde = 0;
        while (desde < lineas) {
            float inicio = paginas.isEmpty() ? tablaPrimera : tablaOtras;
            int caben = Math.max(1, (int) ((inicio - ALTO_ENCABEZADO - ESPACIO_PIE) / ALTO_FILA));
            int hasta = Math.min(lineas, desde + caben);
            paginas.add(new int[]{desde, hasta});
            desde = hasta;
        }

        // ---------- Dibujo ----------
        for (int p = 0; p < paginas.size(); p++) {
            pdf.nuevaPagina();
            float tablaArriba;
            if (p == 0) {
                pdf.texto(ajustar(titulo, ancho - 2 * MARGEN, Fuente.NEGRITA, 16), MARGEN, lineaTitulo,
                        Fuente.NEGRITA, 16, OSCURO);
                float y = primeraLineaFiltros;
                for (String l : lineasFiltros) {
                    pdf.texto(l, MARGEN, y, Fuente.NORMAL, 9, GRIS);
                    y -= 12;
                }
                pdf.texto(ajustar(generado, ancho - 2 * MARGEN, Fuente.NORMAL, 9), MARGEN, lineaGenerado,
                        Fuente.NORMAL, 9, GRIS);
                tablaArriba = tablaPrimera;
            } else {
                pdf.texto(ajustar(titulo + " (continuación)", ancho - 2 * MARGEN, Fuente.NEGRITA, 11),
                        MARGEN, arriba - 11, Fuente.NEGRITA, 11, OSCURO);
                tablaArriba = tablaOtras;
            }

            dibujarEncabezado(pdf, encabezados, anchos, numerica, tablaArriba);

            int[] rango = paginas.get(p);
            for (int i = rango[0]; i < rango[1]; i++) {
                float techo = tablaArriba - ALTO_ENCABEZADO - (i - rango[0]) * ALTO_FILA;
                if (i == filas.size()) {
                    dibujarTotal(pdf, filas.size(), techo, ancho);
                } else {
                    dibujarFila(pdf, texto[i], anchos, numerica, techo, (i - rango[0]) % 2 == 1);
                }
            }

            // Pie de página
            float lineaPie = 24;
            pdf.linea(MARGEN, lineaPie + 12, ancho - MARGEN, lineaPie + 12, 0.5f, BORDE);
            pdf.texto("Sistema de Proveedores y Productos", MARGEN, lineaPie, Fuente.NORMAL, 8, GRIS);
            String numero = "Página " + (p + 1) + " de " + paginas.size();
            pdf.texto(numero, ancho - MARGEN - PdfBasico.anchoTexto(numero, Fuente.NORMAL, 8), lineaPie,
                    Fuente.NORMAL, 8, GRIS);
        }

        pdf.guardar(destino);
    }

    // ---------- Partes de la tabla ----------

    private static void dibujarEncabezado(PdfBasico pdf, String[] encabezados, float[] anchos,
                                          boolean[] numerica, float arriba) {
        float ancho = suma(anchos);
        pdf.rectangulo(MARGEN, arriba - ALTO_ENCABEZADO, ancho, ALTO_ENCABEZADO, OSCURO);
        float x = MARGEN;
        for (int c = 0; c < encabezados.length; c++) {
            celda(pdf, encabezados[c], x, anchos[c], arriba - 12.5f, Fuente.NEGRITA, numerica[c], BLANCO);
            x += anchos[c];
        }
    }

    private static void dibujarFila(PdfBasico pdf, String[] fila, float[] anchos, boolean[] numerica,
                                    float techo, boolean franja) {
        if (franja) {
            pdf.rectangulo(MARGEN, techo - ALTO_FILA, suma(anchos), ALTO_FILA, FRANJA);
        }
        float x = MARGEN;
        for (int c = 0; c < fila.length; c++) {
            celda(pdf, fila[c], x, anchos[c], techo - 11.5f, Fuente.NORMAL, numerica[c], OSCURO);
            x += anchos[c];
        }
        pdf.linea(MARGEN, techo - ALTO_FILA, MARGEN + suma(anchos), techo - ALTO_FILA, 0.4f, BORDE);
    }

    private static void dibujarTotal(PdfBasico pdf, int registros, float techo, float ancho) {
        pdf.linea(MARGEN, techo, ancho - MARGEN, techo, 1f, OSCURO);
        pdf.texto("Total de registros: " + registros, MARGEN + RELLENO, techo - 12, Fuente.NEGRITA, TAMANO, OSCURO);
    }

    // Escribe el texto dentro de la celda, cortado con "..." si no cabe
    private static void celda(PdfBasico pdf, String texto, float x, float ancho, float lineaBase,
                              Fuente fuente, boolean derecha, float[] color) {
        String visible = ajustar(texto, ancho - 2 * RELLENO, fuente, TAMANO);
        float posicion = derecha
                ? x + ancho - RELLENO - PdfBasico.anchoTexto(visible, fuente, TAMANO)
                : x + RELLENO;
        pdf.texto(visible, posicion, lineaBase, fuente, TAMANO, color);
    }

    // ---------- Anchos de columna ----------

    // Cada columna pide lo que ocupa su texto más largo. Si sobra espacio se reparte en proporción;
    // si falta, las columnas cortas conservan su ancho y las largas se reparten lo que queda.
    private static float[] calcularAnchos(String[] encabezados, String[][] texto, float disponible) {
        int n = encabezados.length;
        float[] natural = new float[n];
        for (int c = 0; c < n; c++) {
            float mayor = PdfBasico.anchoTexto(encabezados[c], Fuente.NEGRITA, TAMANO);
            for (String[] fila : texto) {
                mayor = Math.max(mayor, PdfBasico.anchoTexto(fila[c], Fuente.NORMAL, TAMANO));
            }
            natural[c] = mayor + 2 * RELLENO;
        }

        float[] resultado = new float[n];
        float total = suma(natural);
        if (total <= disponible) {
            float sobra = disponible - total;
            for (int c = 0; c < n; c++) {
                resultado[c] = natural[c] + sobra * natural[c] / total;
            }
            return resultado;
        }

        boolean[] fija = new boolean[n];
        float restante = disponible;
        int libres = n;
        boolean cambio = true;
        while (cambio && libres > 0) {
            cambio = false;
            float cuota = restante / libres;
            for (int c = 0; c < n; c++) {
                if (!fija[c] && natural[c] <= cuota) {
                    fija[c] = true;
                    resultado[c] = natural[c];
                    restante -= natural[c];
                    libres--;
                    cambio = true;
                }
            }
        }
        for (int c = 0; c < n; c++) {
            if (!fija[c]) {
                resultado[c] = restante / libres;
            }
        }
        return resultado;
    }

    // ---------- Texto ----------

    private static String formatear(Object valor) {
        if (valor == null) {
            return "-";
        }
        if (valor instanceof BigDecimal || valor instanceof Double || valor instanceof Float) {
            // Formato colombiano: punto para los miles y coma para los decimales
            DecimalFormat dinero = new DecimalFormat("#,##0.00",
                    new DecimalFormatSymbols(Locale.forLanguageTag("es-CO")));
            return "$ " + dinero.format(valor);
        }
        return String.valueOf(valor);
    }

    // Corta el texto y le pone "..." si no cabe en el ancho dado
    private static String ajustar(String texto, float maximo, Fuente fuente, float tamano) {
        if (PdfBasico.anchoTexto(texto, fuente, tamano) <= maximo) {
            return texto;
        }
        int fin = texto.length();
        while (fin > 0 && PdfBasico.anchoTexto(texto.substring(0, fin).stripTrailing() + "...", fuente, tamano) > maximo) {
            fin--;
        }
        return texto.substring(0, fin).stripTrailing() + "...";
    }

    // Reparte un texto largo en varias líneas, cortando por palabras (máximo de líneas dado)
    private static List<String> partir(String texto, float maximo, int maxLineas) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split("\\s+")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (actual.length() > 0 && PdfBasico.anchoTexto(prueba, Fuente.NORMAL, 9) > maximo) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) {
            lineas.add(actual.toString());
        }
        if (lineas.size() > maxLineas) {
            lineas = new ArrayList<>(lineas.subList(0, maxLineas));
            lineas.set(maxLineas - 1, lineas.get(maxLineas - 1) + " ...");
        }
        lineas.replaceAll(l -> ajustar(l, maximo, Fuente.NORMAL, 9));
        return lineas;
    }

    private static float suma(float[] valores) {
        float total = 0;
        for (float v : valores) {
            total += v;
        }
        return total;
    }
}