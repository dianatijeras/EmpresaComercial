package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.UsuarioController;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.TipoUsuario;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de usuarios (Auditoría). Solo el administrador puede entrar:
 * el controller lo valida y MainFrame solo crea este panel para él.
 */
public class UsuarioPanel extends CrudPanel {

    private final UsuarioController controller = new UsuarioController();

    private final JTextField txtCedula = new JTextField(15);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtApellido = new JTextField(25);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JComboBox<TipoUsuario> cboTipo = new JComboBox<>(TipoUsuario.values());

    public UsuarioPanel() {
        super("Usuarios", new String[]{"Cédula", "Nombre", "Apellido", "Tipo"});
        cboTipo.setPreferredSize(new Dimension(200, 26));
        txtPassword.setToolTipText("Al modificar, déjela vacía para conservar la contraseña actual");
        iniciar(crearFormulario(
                new String[]{"Cédula:", "Nombre:", "Apellido:", "Contraseña:", "Tipo:"},
                new JComponent[]{txtCedula, txtNombre, txtApellido, txtPassword, cboTipo}));
    }

    @Override
    protected List<Object[]> cargarFilas() throws NegocioException {
        List<Object[]> filas = new ArrayList<>();
        for (Usuario u : controller.listar()) {
            filas.add(new Object[]{u.getId(), u.getNombre(), u.getApellido(), u.getTipoUsuario()});
        }
        return filas;
    }

    @Override
    protected void llenarFormulario(int fila) {
        txtCedula.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        txtCedula.setEditable(false); // la cédula no se puede cambiar
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtApellido.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtPassword.setText(""); // vacía = conservar la actual

        TipoUsuario tipo = (TipoUsuario) modelo.getValueAt(fila, 3);
        cboTipo.setSelectedItem(tipo);
        cboTipo.setEnabled(tipo != TipoUsuario.ADMINISTRADOR); // el administrador no cambia de tipo
    }

    @Override
    protected void limpiarFormulario() {
        txtCedula.setText("");
        txtCedula.setEditable(true);
        txtNombre.setText("");
        txtApellido.setText("");
        txtPassword.setText("");
        cboTipo.setSelectedIndex(-1);
        cboTipo.setEnabled(true);
    }

    @Override
    protected void guardar() throws NegocioException {
        String password = new String(txtPassword.getPassword());
        TipoUsuario tipo = (TipoUsuario) cboTipo.getSelectedItem();
        if (haySeleccion()) {
            controller.actualizar(cedulaSeleccionada(), txtNombre.getText(), txtApellido.getText(),
                    password, tipo);
        } else {
            controller.crear(txtCedula.getText(), txtNombre.getText(), txtApellido.getText(),
                    password, tipo);
        }
    }

    @Override
    protected void eliminar() throws NegocioException {
        controller.eliminar(cedulaSeleccionada());
    }

    // La cédula es un long: idSeleccionado() de CrudPanel la recortaría a int
    private long cedulaSeleccionada() {
        return ((Number) valorSeleccionado(0)).longValue();
    }
}