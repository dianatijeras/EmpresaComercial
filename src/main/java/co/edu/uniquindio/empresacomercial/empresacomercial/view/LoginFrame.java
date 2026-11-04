package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.LoginController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {

    private final LoginController controller = new LoginController();

    private final JTextField txtCedula = new JTextField(15);
    private final JPasswordField txtPassword = new JPasswordField(15);
    private final JButton btnIngresar = new JButton("Ingresar");

    public LoginFrame() {
        super("Ingreso al sistema");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirFormulario();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        JLabel titulo = new JLabel("Proveedores y Productos");
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(titulo, c);

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Cédula:"), c);
        c.gridx = 1;
        panel.add(txtCedula, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("Contraseña:"), c);
        c.gridx = 1;
        panel.add(txtPassword, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(btnIngresar, c);

        setContentPane(panel);
        getRootPane().setDefaultButton(btnIngresar); // Enter = Ingresar

        btnIngresar.addActionListener(e -> ingresar());
    }

    private void ingresar() {
        try {
            controller.iniciarSesion(txtCedula.getText(), new String(txtPassword.getPassword()));
            new MainFrame().setVisible(true);
            dispose();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo ingresar",
                    JOptionPane.WARNING_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
        }
    }
}