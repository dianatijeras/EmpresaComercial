package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * Panel base para los CRUD: título, formulario, botones (Nuevo, Guardar,
 * Eliminar) y una tabla con los registros.
 *
 * Cada entidad solo define sus columnas, su formulario y cómo cargar, guardar
 * y eliminar. Los mensajes de error y la confirmación de borrado se manejan aquí.
 *
 * Uso en la subclase: llamar a super(titulo, columnas), crear los campos y,
 * al final del constructor, llamar a iniciar(formulario).
 */
public abstract class CrudPanel extends JPanel {

    private final String titulo;
    protected final DefaultTableModel modelo;
    protected final JTable tabla;

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEliminar = new JButton("Eliminar");

    protected CrudPanel(String titulo, String[] columnas) {
        super(new BorderLayout(10, 10));
        this.titulo = titulo;
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Tabla de solo lectura: se edita desde el formulario
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                alSeleccionar();
            }
        });
    }

    // ---------- Lo que define cada entidad ----------

    /** Una fila por registro, en el mismo orden que las columnas. */
    protected abstract List<Object[]> cargarFilas() throws NegocioException;

    /** Pasa los datos de la fila seleccionada a los campos del formulario. */
    protected abstract void llenarFormulario(int fila);

    /** Deja los campos del formulario vacíos. */
    protected abstract void limpiarFormulario();

    /** Crea el registro si no hay fila seleccionada; si la hay, lo actualiza. */
    protected abstract void guardar() throws NegocioException;

    /** Elimina el registro de la fila seleccionada. */
    protected abstract void eliminar() throws NegocioException;

    // ---------- Armado ----------

    /** Se llama al final del constructor de la subclase. */
    protected void iniciar(JComponent formulario) {
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(LEFT_ALIGNMENT);
        formulario.setAlignmentX(LEFT_ALIGNMENT);
        botones.setAlignmentX(LEFT_ALIGNMENT);
        arriba.add(lblTitulo);
        arriba.add(Box.createVerticalStrut(10));
        arriba.add(formulario);
        arriba.add(botones);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> accionGuardar());
        btnEliminar.addActionListener(e -> accionEliminar());

        btnEliminar.setEnabled(false);
        recargar();
    }

    // ---------- Acciones ----------

    protected void recargar() {
        try {
            List<Object[]> filas = cargarFilas();
            modelo.setRowCount(0);
            for (Object[] fila : filas) {
                modelo.addRow(fila);
            }
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    protected void nuevo() {
        tabla.clearSelection();
        limpiarFormulario();
    }

    private void alSeleccionar() {
        int fila = tabla.getSelectedRow();
        btnEliminar.setEnabled(fila >= 0);
        if (fila >= 0) {
            llenarFormulario(fila);
        }
    }

    private void accionGuardar() {
        try {
            guardar();
            recargar();
            nuevo();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void accionEliminar() {
        if (!haySeleccion()) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar el registro seleccionado?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            eliminar();
            recargar();
            nuevo();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    // ---------- Ayudas para las subclases ----------

    protected boolean haySeleccion() {
        return tabla.getSelectedRow() >= 0;
    }

    /** Valor de una columna de la fila seleccionada (la columna 0 suele ser el id). */
    protected Object valorSeleccionado(int columna) {
        return modelo.getValueAt(tabla.getSelectedRow(), columna);
    }

    protected int idSeleccionado() {
        return ((Number) valorSeleccionado(0)).intValue();
    }

    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}