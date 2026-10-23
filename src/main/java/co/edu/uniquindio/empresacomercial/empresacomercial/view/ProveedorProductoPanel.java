package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProductoController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProveedorController;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.HierarchyEvent;

/**
 * Transacción "Productos de un proveedor": se elige un proveedor, se ven los
 * productos que provee y se pueden agregar o quitar (relación N:M).
 */
public class ProveedorProductoPanel extends JPanel {

    private final ProveedorController proveedorController = new ProveedorController();
    private final ProductoController productoController = new ProductoController();

    private final JComboBox<Proveedor> cboProveedor = new JComboBox<>();
    private final JComboBox<Producto> cboProducto = new JComboBox<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Producto", "Precio"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    // Evita reaccionar a los eventos del combo mientras se está llenando
    private boolean cargando = false;

    public ProveedorProductoPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        JButton btnAgregar = new JButton("Agregar");
        JButton btnQuitar = new JButton("Quitar seleccionado");
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        abajo.add(new JLabel("Producto:"));
        abajo.add(cboProducto);
        abajo.add(btnAgregar);
        abajo.add(Box.createHorizontalStrut(20));
        abajo.add(btnQuitar);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarProducto());
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
        Proveedor p = proveedorElegido();
        if (p == null) {
            return;
        }
        for (Producto producto : p.getProductos()) {
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
            proveedorController.agregarProducto(proveedor.getNit(), producto.getId());
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
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Quitar \"" + modelo.getValueAt(fila, 1) + "\" de este proveedor?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        int idProducto = ((Number) modelo.getValueAt(fila, 0)).intValue();
        try {
            proveedorController.quitarProducto(proveedor.getNit(), idProducto);
            recargar();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // ---------- Ayudas ----------

    private Proveedor proveedorElegido() {
        return (Proveedor) cboProveedor.getSelectedItem();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}