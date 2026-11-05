package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.CategoriaController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.CiudadController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProductoController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProveedorController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ReporteController;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Categoria;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Ciudad;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.FiltrosReporte;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Proveedor;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Reporte;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Reporte.Filtro;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reportes en PDF. El usuario elige el reporte, los filtros que ese reporte
 * permite y dónde guardar el archivo.
 */
public class ReportesPanel extends JPanel {

    // Opción "sin filtro" de los combos
    private static final Object TODOS = new Object() {
        @Override
        public String toString() {
            return "(Todos)";
        }
    };

    private final ReporteController controller = new ReporteController();
    private final ProveedorController proveedorController = new ProveedorController();
    private final CiudadController ciudadController = new CiudadController();
    private final ProductoController productoController = new ProductoController();
    private final CategoriaController categoriaController = new CategoriaController();

    private final JComboBox<Reporte> cboReporte = new JComboBox<>(Reporte.values());
    private final JLabel lblDescripcion = new JLabel(" ");

    private final JComboBox<Object> cboProveedor = new JComboBox<>();
    private final JComboBox<Object> cboCiudad = new JComboBox<>();
    private final JComboBox<Object> cboProducto = new JComboBox<>();
    private final JComboBox<Object> cboCategoria = new JComboBox<>();
    private final JTextField txtPrecioMin = new JTextField(12);
    private final JTextField txtPrecioMax = new JTextField(12);

    // Cada filtro con su etiqueta y su campo, para mostrar solo los que usa el reporte elegido
    private final Map<Filtro, JLabel> etiquetas = new EnumMap<>(Filtro.class);
    private final Map<Filtro, Component> campos = new EnumMap<>(Filtro.class);

    private File ultimaCarpeta;

    public ReportesPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        cboReporte.setPreferredSize(new Dimension(400, 26));
        for (JComboBox<Object> combo : List.of(cboProveedor, cboCiudad, cboProducto, cboCategoria)) {
            combo.setPreferredSize(new Dimension(400, 26));
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                              boolean seleccionado, boolean foco) {
                    super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                    if (valor instanceof Proveedor p) {
                        setText(p.getNit() + " - " + p.getNombre());
                    } else if (valor instanceof Producto p) {
                        setText(p.getNombre());
                    }
                    return this;
                }
            });
        }

        construirPantalla();
        actualizarFiltros();

        cboReporte.addActionListener(e -> actualizarFiltros());

        // Los combos se llenan cada vez que se abre la pantalla
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                cargarOpciones();
            }
        });
    }

    private void construirPantalla() {
        JLabel titulo = new JLabel("Reportes en PDF");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel filaReporte = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        filaReporte.add(new JLabel("Reporte:"));
        filaReporte.add(cboReporte);

        JPanel filtros = new JPanel(new GridBagLayout());
        agregarFiltro(filtros, 0, Filtro.PROVEEDOR, "Proveedor:", cboProveedor);
        agregarFiltro(filtros, 1, Filtro.CIUDAD, "Ciudad:", cboCiudad);
        agregarFiltro(filtros, 2, Filtro.PRODUCTO, "Producto:", cboProducto);
        agregarFiltro(filtros, 3, Filtro.CATEGORIA, "Categoría:", cboCategoria);
        agregarFiltro(filtros, 4, Filtro.PRECIO_MIN, "Precio desde:", txtPrecioMin);
        agregarFiltro(filtros, 5, Filtro.PRECIO_MAX, "Precio hasta:", txtPrecioMax);
        txtPrecioMin.setToolTipText("Opcional. Por ejemplo 5000 o 5000.50");
        txtPrecioMax.setToolTipText("Opcional. Por ejemplo 20000");

        JButton btnGenerar = new JButton("Generar PDF");
        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 8));
        filaBoton.add(btnGenerar);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        // Se envuelve en un FlowLayout para que los filtros queden pegados a la izquierda
        JPanel filtrosIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filtrosIzquierda.add(filtros);

        for (JComponent c : new JComponent[]{titulo, filaReporte, lblDescripcion, filtrosIzquierda, filaBoton}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
        }
        centro.add(titulo);
        centro.add(Box.createVerticalStrut(10));
        centro.add(filaReporte);
        centro.add(lblDescripcion);
        centro.add(Box.createVerticalStrut(10));
        centro.add(filtrosIzquierda);
        centro.add(filaBoton);

        add(centro, BorderLayout.NORTH);
        btnGenerar.addActionListener(e -> generar());
    }

    private void agregarFiltro(JPanel panel, int fila, Filtro filtro, String texto, Component campo) {
        JLabel etiqueta = new JLabel(texto);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = fila;
        c.gridx = 0;
        panel.add(etiqueta, c);
        c.gridx = 1;
        panel.add(campo, c);
        etiquetas.put(filtro, etiqueta);
        campos.put(filtro, campo);
    }

    // Muestra la descripción y solo los filtros que usa el reporte elegido
    private void actualizarFiltros() {
        Reporte reporte = (Reporte) cboReporte.getSelectedItem();
        lblDescripcion.setText(reporte == null ? " " : "<html><body style='width:700px'>"
                + reporte.getDescripcion() + "</body></html>");
        for (Filtro filtro : Filtro.values()) {
            boolean visible = reporte != null && reporte.getFiltros().contains(filtro);
            etiquetas.get(filtro).setVisible(visible);
            campos.get(filtro).setVisible(visible);
        }
        revalidate();
        repaint();
    }

    private void cargarOpciones() {
        try {
            llenar(cboProveedor, proveedorController.listar());
            llenar(cboCiudad, ciudadController.listar());
            llenar(cboProducto, productoController.listar());
            llenar(cboCategoria, categoriaController.listar());
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void llenar(JComboBox<Object> combo, List<?> elementos) {
        combo.removeAllItems();
        combo.addItem(TODOS);
        for (Object e : elementos) {
            combo.addItem(e);
        }
        combo.setSelectedIndex(0);
    }

    //Generar

    private void generar() {
        Reporte reporte = (Reporte) cboReporte.getSelectedItem();
        if (reporte == null) {
            mostrarError("Seleccione un reporte.");
            return;
        }

        FiltrosReporte filtros;
        String descripcion;
        try {
            Double precioMin = leerPrecio(txtPrecioMin, "desde");
            Double precioMax = leerPrecio(txtPrecioMax, "hasta");

            Object proveedor = elegido(cboProveedor);
            Object ciudad = elegido(cboCiudad);
            Object producto = elegido(cboProducto);
            Object categoria = elegido(cboCategoria);

            filtros = new FiltrosReporte(
                    proveedor instanceof Proveedor p ? p.getNit() : null,
                    ciudad instanceof Ciudad c ? c.getId() : null,
                    producto instanceof Producto p ? p.getId() : null,
                    categoria instanceof Categoria c ? c.getId() : null,
                    precioMin, precioMax);
            descripcion = describir(reporte, proveedor, ciudad, producto, categoria, precioMin, precioMax);
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
            return;
        }

        File archivo = pedirArchivo(reporte);
        if (archivo == null) {
            return; // el usuario canceló
        }

        try {
            int registros = controller.generarPdf(reporte, filtros, descripcion, archivo);
            int abrir = JOptionPane.showConfirmDialog(this,
                    "Se generó el reporte con " + registros + (registros == 1 ? " registro" : " registros")
                            + ".\n" + archivo.getAbsolutePath() + "\n\n¿Desea abrirlo ahora?",
                    "Reporte generado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (abrir == JOptionPane.YES_OPTION) {
                abrirArchivo(archivo);
            }
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // Pregunta dónde guardar el PDF; devuelve null si el usuario cancela
    private File pedirArchivo(Reporte reporte) {
        JFileChooser selector = new JFileChooser(ultimaCarpeta);
        selector.setDialogTitle("Guardar reporte");
        selector.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf"));
        selector.setSelectedFile(new File(nombreSugerido(reporte)));

        while (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = selector.getSelectedFile();
            if (!archivo.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".pdf");
            }
            if (archivo.exists()) {
                int sobrescribir = JOptionPane.showConfirmDialog(this,
                        "Ya existe el archivo \"" + archivo.getName() + "\". ¿Desea reemplazarlo?",
                        "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (sobrescribir != JOptionPane.YES_OPTION) {
                    continue; // vuelve a mostrar el selector
                }
            }
            ultimaCarpeta = archivo.getParentFile();
            return archivo;
        }
        return null;
    }

    // Por ejemplo: productos_por_proveedor_20261031.pdf
    private String nombreSugerido(Reporte reporte) {
        String base = Normalizer.normalize(reporte.getTitulo(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        return base + "_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
    }

    private void abrirArchivo(File archivo) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
                return;
            }
        } catch (Exception e) {
            // se avisa abajo
        }
        mostrarError("No se pudo abrir el archivo automáticamente. Búsquelo en:\n" + archivo.getAbsolutePath());
    }

    //Ayudas

    // Lo que el usuario eligió en el combo; TODOS (o nada) significa sin filtro
    private Object elegido(JComboBox<Object> combo) {
        Object valor = combo.getSelectedItem();
        return valor == TODOS ? null : valor;
    }

    // Vacío = sin filtro. Acepta coma o punto decimal.
    private Double leerPrecio(JTextField campo, String nombre) throws NegocioException {
        String texto = campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(texto);
        } catch (NumberFormatException e) {
            throw new NegocioException("El precio " + nombre + " debe ser un número (por ejemplo 5000 o 5000.50).");
        }
    }

    // Texto que se imprime en el PDF con los filtros elegidos (solo los que usa ese reporte)
    private String describir(Reporte reporte, Object proveedor, Object ciudad, Object producto,
                             Object categoria, Double precioMin, Double precioMax) {
        List<String> partes = new ArrayList<>();
        for (Filtro filtro : reporte.getFiltros()) {
            switch (filtro) {
                case PROVEEDOR -> {
                    if (proveedor instanceof Proveedor p) {
                        partes.add("Proveedor: " + p.getNit() + " - " + p.getNombre());
                    }
                }
                case CIUDAD -> {
                    if (ciudad instanceof Ciudad c) {
                        partes.add("Ciudad: " + c.getNombre());
                    }
                }
                case PRODUCTO -> {
                    if (producto instanceof Producto p) {
                        partes.add("Producto: " + p.getNombre());
                    }
                }
                case CATEGORIA -> {
                    if (categoria instanceof Categoria c) {
                        partes.add("Categoría: " + c.getNombre());
                    }
                }
                case PRECIO_MIN -> {
                    if (precioMin != null) {
                        partes.add("Precio desde: " + dinero(precioMin));
                    }
                }
                case PRECIO_MAX -> {
                    if (precioMax != null) {
                        partes.add("Precio hasta: " + dinero(precioMax));
                    }
                }
            }
        }
        return partes.isEmpty() ? "ninguno (todos los registros)" : String.join("  |  ", partes);
    }

    private String dinero(double valor) {
        return "$ " + new DecimalFormat("#,##0.00",
                new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"))).format(valor);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}