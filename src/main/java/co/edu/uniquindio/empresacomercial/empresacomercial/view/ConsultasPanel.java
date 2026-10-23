package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.CiudadController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ConsultaController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Ciudad;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Consulta;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.ResultadoConsulta;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Consultas del enunciado (JOIN y subconsultas). Es un solo panel: la consulta
 * elegida decide las columnas de la tabla y si se pide una ciudad.
 */
public class ConsultasPanel extends JPanel {

    private final ConsultaController controller = new ConsultaController();
    private final CiudadController ciudadController = new CiudadController();

    private final JComboBox<Consulta> cboConsulta = new JComboBox<>(Consulta.values());
    private final JLabel lblDescripcion = new JLabel(" ");
    private final JLabel lblCiudad = new JLabel("Ciudad:");
    private final JComboBox<Ciudad> cboCiudad = new JComboBox<>();
    private final JLabel lblFilas = new JLabel(" ");

    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        // Para que los números queden alineados a la derecha y los precios con formato
        @Override
        public Class<?> getColumnClass(int columna) {
            for (int i = 0; i < getRowCount(); i++) {
                Object valor = getValueAt(i, columna);
                if (valor != null) {
                    return valor.getClass();
                }
            }
            return Object.class;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public ConsultasPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        cboConsulta.setPreferredSize(new Dimension(560, 26));
        cboCiudad.setPreferredSize(new Dimension(220, 26));
        tabla.setAutoCreateRowSorter(true); // clic en el encabezado ordena

        DecimalFormat dinero = new DecimalFormat("#,##0.00");
        tabla.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            protected void setValue(Object valor) {
                setText(valor == null ? "" : dinero.format(valor));
            }
        });

        construirPantalla();
        actualizarParametro();

        cboConsulta.addActionListener(e -> actualizarParametro());

        // Las ciudades se cargan cada vez que se abre la pantalla
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                cargarCiudades();
            }
        });
    }

    private void construirPantalla() {
        JLabel titulo = new JLabel("Consultas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JButton btnEjecutar = new JButton("Ejecutar");
        JPanel filaConsulta = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        filaConsulta.add(new JLabel("Consulta:"));
        filaConsulta.add(cboConsulta);
        filaConsulta.add(btnEjecutar);

        JPanel filaCiudad = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        filaCiudad.add(lblCiudad);
        filaCiudad.add(cboCiudad);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        for (JPanel fila : new JPanel[]{filaConsulta, filaCiudad}) {
            fila.setAlignmentX(LEFT_ALIGNMENT);
        }
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        lblDescripcion.setAlignmentX(LEFT_ALIGNMENT);
        arriba.add(titulo);
        arriba.add(Box.createVerticalStrut(10));
        arriba.add(filaConsulta);
        arriba.add(lblDescripcion);
        arriba.add(filaCiudad);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblFilas, BorderLayout.SOUTH);

        btnEjecutar.addActionListener(e -> ejecutar());
    }

    // Muestra la descripción y el combo de ciudad solo cuando la consulta lo necesita
    private void actualizarParametro() {
        Consulta consulta = (Consulta) cboConsulta.getSelectedItem();
        boolean pideCiudad = consulta != null && consulta.getParametro() == Consulta.Parametro.CIUDAD;
        lblDescripcion.setText(consulta == null ? " " : consulta.getDescripcion());
        lblCiudad.setVisible(pideCiudad);
        cboCiudad.setVisible(pideCiudad);
    }

    private void cargarCiudades() {
        try {
            cboCiudad.removeAllItems();
            for (Ciudad c : ciudadController.listar()) {
                cboCiudad.addItem(c);
            }
            cboCiudad.setSelectedIndex(-1);
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void ejecutar() {
        Consulta consulta = (Consulta) cboConsulta.getSelectedItem();
        Ciudad ciudad = (Ciudad) cboCiudad.getSelectedItem();
        Integer idParametro = null;
        if (consulta != null && consulta.getParametro() == Consulta.Parametro.CIUDAD && ciudad != null) {
            idParametro = ciudad.getId();
        }

        try {
            ResultadoConsulta resultado = controller.ejecutar(consulta, idParametro);
            modelo.setDataVector(
                    resultado.filas().toArray(new Object[0][]),
                    resultado.columnas().toArray());
            int filas = resultado.filas().size();
            lblFilas.setText(filas == 0 ? "La consulta no devolvió resultados."
                    : filas + (filas == 1 ? " fila" : " filas"));
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}