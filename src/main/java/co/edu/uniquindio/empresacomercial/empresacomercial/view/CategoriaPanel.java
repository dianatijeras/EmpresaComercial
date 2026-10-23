package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.CategoriaController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Categoria;

import javax.swing.JComponent;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

public class CategoriaPanel extends CrudPanel {

    private final CategoriaController controller = new CategoriaController();
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtDescripcion = new JTextField(35);

    public CategoriaPanel() {
        super("Categorías", new String[]{"ID", "Nombre", "Descripción"});
        iniciar(crearFormulario(
                new String[]{"Nombre:", "Descripción:"},
                new JComponent[]{txtNombre, txtDescripcion}));
    }

    @Override
    protected List<Object[]> cargarFilas() throws NegocioException {
        List<Object[]> filas = new ArrayList<>();
        for (Categoria c : controller.listar()) {
            filas.add(new Object[]{c.getId(), c.getNombre(),
                    c.getDescripcion() == null ? "" : c.getDescripcion()});
        }
        return filas;
    }

    @Override
    protected void llenarFormulario(int fila) {
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtDescripcion.setText(String.valueOf(modelo.getValueAt(fila, 2)));
    }

    @Override
    protected void limpiarFormulario() {
        txtNombre.setText("");
        txtDescripcion.setText("");
    }

    @Override
    protected void guardar() throws NegocioException {
        if (haySeleccion()) {
            controller.actualizar(idSeleccionado(), txtNombre.getText(), txtDescripcion.getText());
        } else {
            controller.crear(txtNombre.getText(), txtDescripcion.getText());
        }
    }

    @Override
    protected void eliminar() throws NegocioException {
        controller.eliminar(idSeleccionado());
    }
}