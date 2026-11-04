package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.BitacoraController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.UsuarioController;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Bitacora;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/** Bitácora de entradas y salidas (Auditoría). Solo lectura y solo administrador. */
public class BitacoraPanel extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final BitacoraController bitacoraController = new BitacoraController();
    private final UsuarioController usuarioController = new UsuarioController();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Cédula", "Usuario", "Entrada", "Salida"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public BitacoraPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Bitácora de entradas y salidas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> recargar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnActualizar);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        botones.setAlignmentX(LEFT_ALIGNMENT);
        arriba.add(titulo);
        arriba.add(Box.createVerticalStrut(10));
        arriba.add(botones);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                recargar();
            }
        });
        recargar();
    }

    private void recargar() {
        try {
            // El registro solo trae la cédula; el nombre se busca en la lista de usuarios
            Map<Long, String> nombres = new HashMap<>();
            for (Usuario u : usuarioController.listar()) {
                nombres.put(u.getId(), u.getNombre() + " " + u.getApellido());
            }

            modelo.setRowCount(0);
            for (Bitacora b : bitacoraController.listar()) {
                modelo.addRow(new Object[]{
                        b.getId(),
                        b.getIdUsuario(),
                        nombres.getOrDefault(b.getIdUsuario(), "-"),
                        formatear(b.getFechaHoraEntrada(), "-"),
                        formatear(b.getFechaHoraSalida(), "Sesión abierta")});
            }
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String formatear(LocalDateTime fecha, String sinFecha) {
        return fecha == null ? sinFecha : fecha.format(FORMATO);
    }
}