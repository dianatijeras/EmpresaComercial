package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.CiudadController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProveedorController;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Ciudad;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Proveedor;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD de proveedores. Los productos de cada proveedor se manejan en la
 * pantalla de Transacciones (ProveedorProductoPanel), no aquí.
 */
public class ProveedorPanel extends CrudPanel {

    private final ProveedorController controller = new ProveedorController();
    private final CiudadController ciudadController = new CiudadController();

    private final JTextField txtNit = new JTextField(20);
    private final JTextField txtNombre = new JTextField(30);
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtDireccion = new JTextField(35);
    private final JComboBox<Ciudad> cboCiudad = new JComboBox<>();

    public ProveedorPanel() {
        super("Proveedores", new String[]{"NIT", "Nombre", "Teléfono", "Dirección", "Ciudad"});
        iniciar(crearFormulario(
                new String[]{"NIT:", "Nombre:", "Teléfono:", "Dirección:", "Ciudad:"},
                new JComponent[]{txtNit, txtNombre, txtTelefono, txtDireccion, cboCiudad}));
    }

    @Override
    protected List<Object[]> cargarFilas() throws NegocioException {
        // La misma instancia de Ciudad va en el combo y en la tabla
        Map<Integer, Ciudad> ciudades = new HashMap<>();
        cboCiudad.removeAllItems();
        for (Ciudad c : ciudadController.listar()) {
            ciudades.put(c.getId(), c);
            cboCiudad.addItem(c);
        }
        cboCiudad.setSelectedIndex(-1);

        List<Object[]> filas = new ArrayList<>();
        for (Proveedor p : controller.listar()) {
            filas.add(new Object[]{p.getNit(), p.getNombre(),
                    p.getTelefono() == null ? "" : p.getTelefono(),
                    p.getDireccion() == null ? "" : p.getDireccion(),
                    ciudades.get(p.getIdCiudad())});
        }
        return filas;
    }

    @Override
    protected void llenarFormulario(int fila) {
        txtNit.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        txtNit.setEditable(false); // el NIT no se puede cambiar
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtTelefono.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtDireccion.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        cboCiudad.setSelectedItem(modelo.getValueAt(fila, 4));
    }

    @Override
    protected void limpiarFormulario() {
        txtNit.setText("");
        txtNit.setEditable(true);
        txtNombre.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        cboCiudad.setSelectedIndex(-1);
    }

    @Override
    protected void guardar() throws NegocioException {
        int idCiudad = idCiudadElegida();
        if (haySeleccion()) {
            controller.actualizar(nitSeleccionado(), txtNombre.getText(), txtTelefono.getText(),
                    txtDireccion.getText(), idCiudad);
        } else {
            controller.crear(txtNit.getText(), txtNombre.getText(), txtTelefono.getText(),
                    txtDireccion.getText(), idCiudad, null);
        }
    }

    @Override
    protected void eliminar() throws NegocioException {
        controller.eliminar(nitSeleccionado());
    }

    // ---------- Ayudas ----------

    // La llave de Proveedor es el NIT (texto), no un id numérico
    private String nitSeleccionado() {
        return String.valueOf(valorSeleccionado(0));
    }

    // Sin ciudad elegida se envía 0 y el controller responde "Seleccione una ciudad."
    private int idCiudadElegida() {
        Ciudad c = (Ciudad) cboCiudad.getSelectedItem();
        return c == null ? 0 : c.getId();
    }
}