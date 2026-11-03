package co.edu.uniquindio.empresacomercial.empresacomercial.reporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Escritor de PDF mínimo, sin librerías externas. Solo sabe escribir texto,
 * líneas y rectángulos con las fuentes Helvetica y Helvetica Negrita (que todo
 * visor de PDF trae), lo suficiente para armar reportes en forma de tabla.
 *
 * Las coordenadas están en puntos (1 punto = 1/72 de pulgada) y el origen (0, 0)
 * es la esquina inferior izquierda de la página.
 * Los caracteres que no existan en la fuente (por ejemplo emojis) salen como "?".
 */
public class PdfBasico {

    // Tamaño de la hoja A4 en vertical
    public static final float ANCHO_A4 = 595f;
    public static final float ALTO_A4 = 842f;

    public enum Fuente {
        NORMAL("F1", "Helvetica"),
        NEGRITA("F2", "Helvetica-Bold");

        private final String nombre;
        private final String nombrePdf;

        Fuente(String nombre, String nombrePdf) {
            this.nombre = nombre;
            this.nombrePdf = nombrePdf;
        }
    }

    // Codificación de los textos: así se escriben las tildes y la ñ
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");

    // Ancho de cada carácter en milésimas del tamaño de la fuente, para los códigos 32 a 255
    private static final int[] ANCHOS_NORMAL = {
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584, 761,
            556, 0, 222, 556, 333, 1000, 556, 556, 333, 1000, 667, 333, 1000, 0, 611, 0,
            0, 222, 222, 333, 333, 350, 556, 1000, 333, 1000, 500, 333, 944, 0, 500, 667,
            278, 333, 556, 556, 556, 556, 260, 556, 333, 737, 370, 556, 584, 333, 737, 333,
            400, 584, 333, 333, 333, 556, 537, 278, 333, 333, 365, 556, 834, 834, 834, 611,
            667, 667, 667, 667, 667, 667, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
            722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
            556, 556, 556, 556, 556, 556, 889, 500, 556, 556, 556, 556, 278, 278, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 584, 611, 556, 556, 556, 556, 500, 556, 500
    };
    private static final int[] ANCHOS_NEGRITA = {
            278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
            975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
            333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
            611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584, 761,
            556, 0, 278, 556, 500, 1000, 556, 556, 333, 1000, 667, 333, 1000, 0, 611, 0,
            0, 278, 278, 500, 500, 350, 556, 1000, 333, 1000, 556, 333, 944, 0, 500, 667,
            278, 333, 556, 556, 556, 556, 280, 556, 333, 737, 370, 556, 584, 333, 737, 333,
            400, 584, 333, 333, 333, 611, 556, 278, 333, 333, 365, 556, 834, 834, 834, 611,
            722, 722, 722, 722, 722, 722, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
            722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
            556, 556, 556, 556, 556, 556, 889, 556, 556, 556, 556, 556, 278, 278, 278, 278,
            611, 611, 611, 611, 611, 611, 611, 584, 611, 611, 611, 611, 611, 556, 611, 556
    };

    private final float ancho;
    private final float alto;
    private final String titulo;
    private final List<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder pagina;

    public PdfBasico(float ancho, float alto, String titulo) {
        this.ancho = ancho;
        this.alto = alto;
        this.titulo = titulo;
    }

    // ---------- Dibujo ----------

    /** Empieza una página nueva; todo lo que se dibuje después va en ella. */
    public void nuevaPagina() {
        pagina = new StringBuilder();
        paginas.add(pagina);
    }

    /** Escribe el texto con su esquina inferior izquierda (la línea base) en (x, y). */
    public void texto(String texto, float x, float y, Fuente fuente, float tamano, float[] color) {
        comprobarPagina();
        pagina.append("q ").append(color(color, "rg"))
                .append(" BT /").append(fuente.nombre).append(' ').append(num(tamano)).append(" Tf ")
                .append(num(x)).append(' ').append(num(y)).append(" Td ")
                .append(literal(codificar(texto))).append(" Tj ET Q\n");
    }

    /** Rectángulo relleno; (x, y) es su esquina inferior izquierda. */
    public void rectangulo(float x, float y, float w, float h, float[] color) {
        comprobarPagina();
        pagina.append("q ").append(color(color, "rg")).append(' ')
                .append(num(x)).append(' ').append(num(y)).append(' ')
                .append(num(w)).append(' ').append(num(h)).append(" re f Q\n");
    }

    public void linea(float x1, float y1, float x2, float y2, float grosor, float[] color) {
        comprobarPagina();
        pagina.append("q ").append(color(color, "RG")).append(' ').append(num(grosor)).append(" w ")
                .append(num(x1)).append(' ').append(num(y1)).append(" m ")
                .append(num(x2)).append(' ').append(num(y2)).append(" l S Q\n");
    }

    /** Ancho en puntos que ocupa el texto con esa fuente y tamaño. */
    public static float anchoTexto(String texto, Fuente fuente, float tamano) {
        int[] anchos = fuente == Fuente.NEGRITA ? ANCHOS_NEGRITA : ANCHOS_NORMAL;
        float suma = 0;
        for (byte b : codificar(texto)) {
            int codigo = b & 0xFF;
            if (codigo >= 32) {
                suma += anchos[codigo - 32];
            }
        }
        return suma * tamano / 1000f;
    }

    // ---------- Escritura del archivo ----------

    public void guardar(OutputStream destino) throws IOException {
        if (paginas.isEmpty()) {
            throw new IllegalStateException("El documento no tiene páginas.");
        }
        int cantidad = paginas.size();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        List<Integer> posiciones = new ArrayList<>();

        // Cabecera; los 4 bytes altos del comentario le indican a los programas que el archivo es binario
        salida.write(new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '4', '\n',
                '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});

        // Objetos: 1 catálogo, 2 lista de páginas, 3 y 4 fuentes, 5 información,
        // y luego, por cada página, el objeto de la página (6 + 2i) y su contenido (7 + 2i)
        StringBuilder hijos = new StringBuilder();
        for (int i = 0; i < cantidad; i++) {
            hijos.append(6 + 2 * i).append(" 0 R ");
        }
        objeto(salida, posiciones, "<< /Type /Catalog /Pages 2 0 R >>");
        objeto(salida, posiciones, "<< /Type /Pages /Kids [" + hijos + "] /Count " + cantidad + " >>");
        for (Fuente f : Fuente.values()) {
            objeto(salida, posiciones, "<< /Type /Font /Subtype /Type1 /BaseFont /" + f.nombrePdf
                    + " /Encoding /WinAnsiEncoding >>");
        }
        objeto(salida, posiciones, "<< /Title " + hexadecimal(titulo)
                + " /Creator (Sistema de Proveedores y Productos) >>");

        for (int i = 0; i < cantidad; i++) {
            objeto(salida, posiciones, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + num(ancho) + " " + num(alto) + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >>"
                    + " /Contents " + (7 + 2 * i) + " 0 R >>");
            String contenido = paginas.get(i).toString(); // solo tiene caracteres ASCII
            objeto(salida, posiciones, "<< /Length " + contenido.length() + " >>\nstream\n"
                    + contenido + "endstream");
        }

        // Tabla de referencias: dónde empieza cada objeto dentro del archivo
        int inicioTabla = salida.size();
        StringBuilder tabla = new StringBuilder("xref\n0 " + (posiciones.size() + 1)
                + "\n0000000000 65535 f \n");
        for (int posicion : posiciones) {
            tabla.append(String.format(Locale.ROOT, "%010d 00000 n \n", posicion));
        }
        tabla.append("trailer\n<< /Size ").append(posiciones.size() + 1)
                .append(" /Root 1 0 R /Info 5 0 R >>\nstartxref\n")
                .append(inicioTabla).append("\n%%EOF\n");
        salida.write(tabla.toString().getBytes(StandardCharsets.ISO_8859_1));

        destino.write(salida.toByteArray());
        destino.flush();
    }

    // ---------- Ayudas ----------

    private void comprobarPagina() {
        if (pagina == null) {
            throw new IllegalStateException("Llame primero a nuevaPagina().");
        }
    }

    private static void objeto(ByteArrayOutputStream salida, List<Integer> posiciones, String cuerpo)
            throws IOException {
        posiciones.add(salida.size());
        String texto = posiciones.size() + " 0 obj\n" + cuerpo + "\nendobj\n";
        salida.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }

    // Convierte el texto a los bytes de la fuente. Lo que no exista se cambia por "?"
    private static byte[] codificar(String texto) {
        StringBuilder limpio = new StringBuilder();
        for (char c : texto.toCharArray()) {
            limpio.append(c < 32 ? ' ' : c); // saltos de línea y tabuladores salen como espacio
        }
        CharsetEncoder codificador = WIN_ANSI.newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .replaceWith(new byte[]{'?'});
        try {
            ByteBuffer bytes = codificador.encode(CharBuffer.wrap(limpio));
            byte[] resultado = new byte[bytes.remaining()];
            bytes.get(resultado);
            return resultado;
        } catch (CharacterCodingException e) {
            return limpio.toString().getBytes(StandardCharsets.US_ASCII);
        }
    }

    // Texto entre paréntesis como lo pide PDF: los símbolos especiales y los caracteres no ASCII van con "\"
    private static String literal(byte[] bytes) {
        StringBuilder sb = new StringBuilder("(");
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v == '(' || v == ')' || v == '\\') {
                sb.append('\\').append((char) v);
            } else if (v < 32 || v > 126) {
                sb.append(String.format(Locale.ROOT, "\\%03o", v));
            } else {
                sb.append((char) v);
            }
        }
        return sb.append(')').toString();
    }

    // El título del documento va en UTF-16 escrito en hexadecimal, para que admita cualquier carácter
    private static String hexadecimal(String texto) {
        StringBuilder sb = new StringBuilder("<FEFF");
        for (char c : texto.toCharArray()) {
            sb.append(String.format(Locale.ROOT, "%04X", (int) c));
        }
        return sb.append('>').toString();
    }

    private static String color(float[] c, String operador) {
        return num(c[0]) + " " + num(c[1]) + " " + num(c[2]) + " " + operador;
    }

    private static String num(float valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }
}