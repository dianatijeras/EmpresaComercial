package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProductoController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProveedorController;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.ProductoProveedor;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Proveedor;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Transacción "Productos de un proveedor": se elige un proveedor, se ven los
 * productos que provee con el precio de ese proveedor y se pueden agregar,
 * cambiar de precio o quitar (relación N:M con precio en el vínculo).
 */
public class ProveedorProductoPanel extends JPanel {

    private final ProveedorController proveedorController = new ProveedorController();
    private final ProductoController productoController = new ProductoController();

    private final JComboBox<Proveedor> cboProveedor = new JComboBox<>();
    private final JComboBox<Producto> cboProducto = new JComboBox<>();
    private final JTextField txtPrecioNuevo = new JTextField(10);
    private final JTextField txtPrecioFila = new JTextField(10);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Producto", "Precio"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 2 ? Double.class : Object.class;
        }
    };
    private final JTable tabla = new JTable(modelo);

    // Los productos del proveedor elegido, en el mismo orden que las filas de la tabla
    private List<ProductoProveedor> actuales = new ArrayList<>();

    // Evita reaccionar a los eventos del combo mientras se está llenando
    private boolean cargando = false;

    public ProveedorProductoPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DecimalFormat dinero = new DecimalFormat("#,##0.00");
        tabla.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            protected void setValue(Object valor) {
                setText(valor == null ? "" : dinero.format(valor));
            }
        });

        // Proveedor y Producto no tienen toString(): se muestra con un renderer
        cboProveedor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Proveedor p) {
                    setText(p.getNit() + " - " + p.getNombre());
                }
                return this;
            }
        });
        cboProducto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Producto p) {
                    setText(p.getNombre());
                }
                return this;
            }
        });

        cboProveedor.setPreferredSize(new Dimension(400, 26));
        cboProducto.setPreferredSize(new Dimension(300, 26));

        construirPantalla();

        cboProveedor.addActionListener(e -> {
            if (!cargando) {
                mostrarProductos();
            }
        });
        // Al elegir una fila, su precio aparece en el campo para poder cambiarlo
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tabla.getSelectedRow();
                txtPrecioFila.setText(fila >= 0 ? formatoPrecio(actuales.get(fila).getPrecio()) : "");
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                recargar();
            }
        });

        recargar();
    }

    private void construirPantalla() {
        JLabel titulo = new JLabel("Productos de un proveedor");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel filaProveedor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaProveedor.add(new JLabel("Proveedor:"));
        filaProveedor.add(cboProveedor);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        filaProveedor.setAlignmentX(LEFT_ALIGNMENT);
        arriba.add(titulo);
        arriba.add(Box.createVerticalStrut(10));
        arriba.add(filaProveedor);

        // Fila 1: agregar un producto nuevo al proveedor
        JButton btnAgregar = new JButton("Agregar");
        JPanel filaAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaAgregar.add(new JLabel("Agregar producto:"));
        filaAgregar.add(cboProducto);
        filaAgregar.add(new JLabel("Precio:"));
        filaAgregar.add(txtPrecioNuevo);
        filaAgregar.add(btnAgregar);

        // Fila 2: cambiar el precio o quitar la fila elegida en la tabla
        JButton btnCambiar = new JButton("Cambiar precio");
        JButton btnQuitar = new JButton("Quitar");
        JPanel filaSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaSeleccion.add(new JLabel("Fila seleccionada:"));
        filaSeleccion.add(new JLabel("Precio:"));
        filaSeleccion.add(txtPrecioFila);
        filaSeleccion.add(btnCambiar);
        filaSeleccion.add(Box.createHorizontalStrut(20));
        filaSeleccion.add(btnQuitar);

        JPanel abajo = new JPanel();
        abajo.setLayout(new BoxLayout(abajo, BoxLayout.Y_AXIS));
        filaAgregar.setAlignmentX(LEFT_ALIGNMENT);
        filaSeleccion.setAlignmentX(LEFT_ALIGNMENT);
        abajo.add(filaAgregar);
        abajo.add(filaSeleccion);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarProducto());
        btnCambiar.addActionListener(e -> cambiarPrecio());
        btnQuitar.addActionListener(e -> quitarProducto());
    }

    // ---------- Datos ----------

    // Recarga los dos combos y conserva el proveedor que estaba elegido
    private void recargar() {
        Proveedor elegido = proveedorElegido();
        String nitElegido = elegido == null ? null : elegido.getNit();
        try {
            cargando = true;
            cboProveedor.removeAllItems();
            Proveedor reseleccionar = null;
            for (Proveedor p : proveedorController.listar()) {
                cboProveedor.addItem(p);
                if (p.getNit().equals(nitElegido)) {
                    reseleccionar = p;
                }
            }
            cboProveedor.setSelectedItem(reseleccionar); // null = sin selección

            cboProducto.removeAllItems();
            for (Producto p : productoController.listar()) {
                cboProducto.addItem(p);
            }
            cboProducto.setSelectedIndex(-1);
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } finally {
            cargando = false;
        }
        mostrarProductos();
    }

    private void mostrarProductos() {
        modelo.setRowCount(0);
        txtPrecioFila.setText("");
        Proveedor p = proveedorElegido();
        actuales = p == null ? new ArrayList<>() : new ArrayList<>(p.getProductos());
        for (ProductoProveedor producto : actuales) {
            modelo.addRow(new Object[]{producto.getId(), producto.getNombre(), producto.getPrecio()});
        }
    }

    // ---------- Acciones ----------

    private void agregarProducto() {
        Proveedor proveedor = proveedorElegido();
        Producto producto = (Producto) cboProducto.getSelectedItem();
        if (proveedor == null) {
            mostrarError("Seleccione un proveedor.");
            return;
        }
        if (producto == null) {
            mostrarError("Seleccione el producto que desea agregar.");
            return;
        }
        try {
            double precio = leerPrecio(txtPrecioNuevo);
            proveedorController.agregarProducto(proveedor.getNit(), producto.getId(), precio);
            txtPrecioNuevo.setText("");
            recargar();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cambiarPrecio() {
        Proveedor proveedor = proveedorElegido();
        int fila = tabla.getSelectedRow();
        if (proveedor == null) {
            mostrarError("Seleccione un proveedor.");
            return;
        }
        if (fila < 0) {
            mostrarError("Seleccione en la tabla el producto al que desea cambiarle el precio.");
            return;
        }
        try {
            double precio = leerPrecio(txtPrecioFila);
            proveedorController.actualizarPrecio(proveedor.getNit(), actuales.get(fila).getId(), precio);
            recargar();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void quitarProducto() {
        Proveedor proveedor = proveedorElegido();
        int fila = tabla.getSelectedRow();
        if (proveedor == null) {
            mostrarError("Seleccione un proveedor.");
            return;
        }
        if (fila < 0) {
            mostrarError("Seleccione en la tabla el producto que desea quitar.");
            return;
        }
        ProductoProveedor producto = actuales.get(fila);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Quitar \"" + producto.getNombre() + "\" de este proveedor?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            proveedorController.quitarProducto(proveedor.getNit(), producto.getId());
            recargar();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // ---------- Ayudas ----------

    private Proveedor proveedorElegido() {
        return (Proveedor) cboProveedor.getSelectedItem();
    }

    // Acepta coma o punto decimal
    private double leerPrecio(JTextField campo) throws NegocioException {
        String texto = campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            throw new NegocioException("El precio es obligatorio.");
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new NegocioException("El precio debe ser un número (por ejemplo 2500.50).");
        }
    }

    private String formatoPrecio(double precio) {
        return BigDecimal.valueOf(precio).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}