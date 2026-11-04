package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.CiudadController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Ciudad;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class CiudadPanel extends CrudPanel {

    private final CiudadController controller = new CiudadController();
    private final JTextField txtNombre = new JTextField(25);

    public CiudadPanel() {
        super("Ciudades", new String[]{"ID", "Nombre"});

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);

        iniciar(formulario);
    }

    @Override
    protected List<Object[]> cargarFilas() throws NegocioException {
        List<Object[]> filas = new ArrayList<>();
        for (Ciudad c : controller.listar()) {
            filas.add(new Object[]{c.getId(), c.getNombre()});
        }
        return filas;
    }

    @Override
    protected void llenarFormulario(int fila) {
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
    }

    @Override
    protected void limpiarFormulario() {
        txtNombre.setText("");
    }

    @Override
    protected void guardar() throws NegocioException {
        if (haySeleccion()) {
            controller.actualizar(idSeleccionado(), txtNombre.getText());
        } else {
            controller.crear(txtNombre.getText());
        }
    }

    @Override
    protected void eliminar() throws NegocioException {
        controller.eliminar(idSeleccionado());
    }
}