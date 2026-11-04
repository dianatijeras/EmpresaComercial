package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.CategoriaController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.ProductoController;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Categoria;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductoPanel extends CrudPanel {

    private final ProductoController controller = new ProductoController();
    private final CategoriaController categoriaController = new CategoriaController();

    private final JTextField txtNombre = new JTextField(30);
    private final JComboBox<Categoria> cboCategoria = new JComboBox<>();

    public ProductoPanel() {
        super("Productos", new String[]{"ID", "Nombre", "Categoría"});
        cboCategoria.setPreferredSize(new Dimension(250, 26));
        iniciar(crearFormulario(
                new String[]{"Nombre:", "Categoría:"},
                new JComponent[]{txtNombre, cboCategoria}));
    }

    @Override
    protected List<Object[]> cargarFilas() throws NegocioException {
        // Se recarga el combo cada vez, así aparecen las categorías nuevas.
        // La misma instancia de Categoria va en el combo y en la tabla.
        Map<Integer, Categoria> categorias = new HashMap<>();
        cboCategoria.removeAllItems();
        for (Categoria c : categoriaController.listar()) {
            categorias.put(c.getId(), c);
            cboCategoria.addItem(c);
        }
        cboCategoria.setSelectedIndex(-1);

        List<Object[]> filas = new ArrayList<>();
        for (Producto p : controller.listar()) {
            filas.add(new Object[]{p.getId(), p.getNombre(), categorias.get(p.getIdCategoria())});
        }
        return filas;
    }

    @Override
    protected void llenarFormulario(int fila) {
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        cboCategoria.setSelectedItem(modelo.getValueAt(fila, 2));
    }

    @Override
    protected void limpiarFormulario() {
        txtNombre.setText("");
        cboCategoria.setSelectedIndex(-1);
    }

    @Override
    protected void guardar() throws NegocioException {
        int idCategoria = idCategoriaElegida();
        if (haySeleccion()) {
            controller.actualizar(idSeleccionado(), txtNombre.getText(), idCategoria);
        } else {
            controller.crear(txtNombre.getText(), idCategoria);
        }
    }

    @Override
    protected void eliminar() throws NegocioException {
        controller.eliminar(idSeleccionado());
    }

    // ---------- Lectura del formulario ----------

    // Sin categoría elegida se envía 0 y el controller responde "Seleccione una categoría."
    private int idCategoriaElegida() {
        Categoria c = (Categoria) cboCategoria.getSelectedItem();
        return c == null ? 0 : c.getId();
    }
}