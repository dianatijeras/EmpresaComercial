package co.edu.uniquindio.empresacomercial.empresacomercial;

import co.edu.uniquindio.empresacomercial.empresacomercial.view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla se usa el aspecto por defecto de Swing
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}