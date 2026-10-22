package co.edu.uniquindio.empresacomercial.empresacomercial.view;

import co.edu.uniquindio.empresacomercial.empresacomercial.controller.LoginController;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.NegocioException;
import co.edu.uniquindio.empresacomercial.empresacomercial.controller.Sesion;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Ventana principal. Muestra un menú con Entidades, Transacciones,
 * Reportes/Consultas y Auditoría, y una sola pantalla a la vez (CardLayout).
 */
public class MainFrame extends JFrame {

    // Nombres de las "tarjetas" del CardLayout
    public static final String INICIO = "inicio";
    public static final String CIUDADES = "ciudades";
    public static final String CATEGORIAS = "categorias";
    public static final String PRODUCTOS = "productos";
    public static final String PROVEEDORES = "proveedores";
    public static final String PROVEEDOR_PRODUCTO = "proveedorProducto";
    public static final String CONSULTAS = "consultas";
    public static final String REPORTES = "reportes";
    public static final String USUARIOS = "usuarios";
    public static final String BITACORA = "bitacora";

    private final LoginController loginController = new LoginController();
    private final Usuario usuario = Sesion.getUsuarioActual();

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);

    public MainFrame() {
        super("Proveedores y Productos");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));

        construirContenido();
        setJMenuBar(construirMenu());
        add(contenido, BorderLayout.CENTER);
        add(new JLabel("  Usuario: " + usuario.getNombre() + " " + usuario.getApellido()
                + "  |  Tipo: " + usuario.getTipoUsuario()), BorderLayout.SOUTH);

        // Cerrar con la X también registra la salida en la bitácora
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir(false);
            }
        });

        pack();
        setLocationRelativeTo(null);
        mostrar(INICIO);
    }

    // ---------- Pantallas ----------

    private void construirContenido() {
        agregar(INICIO, new JLabel("Bienvenido, " + usuario.getNombre(), SwingConstants.CENTER));

        // TODO: reemplazar cada placeholder por su panel real
        agregar(CIUDADES, pendiente("Ciudades"));
        agregar(CATEGORIAS, pendiente("Categorías"));
        agregar(PRODUCTOS, pendiente("Productos"));
        agregar(PROVEEDORES, pendiente("Proveedores"));
        agregar(PROVEEDOR_PRODUCTO, pendiente("Proveedor - Producto"));
        agregar(CONSULTAS, pendiente("Consultas"));
        agregar(REPORTES, pendiente("Reportes"));
        agregar(USUARIOS, pendiente("Usuarios"));
        agregar(BITACORA, pendiente("Bitácora"));
    }

    private void agregar(String nombre, JComponent panel) {
        contenido.add(panel, nombre);
    }

    private JComponent pendiente(String titulo) {
        return new JLabel(titulo + " (en construcción)", SwingConstants.CENTER);
    }

    public void mostrar(String nombre) {
        cartas.show(contenido, nombre);
    }

    // ---------- Menú (según el tipo de usuario) ----------

    private JMenuBar construirMenu() {
        JMenuBar barra = new JMenuBar();

        // Solo administrador y tradicional pueden modificar datos
        if (usuario.getTipoUsuario().puedeModificarDatos()) {
            JMenu entidades = new JMenu("Entidades");
            entidades.add(item("Ciudades", CIUDADES));
            entidades.add(item("Categorías", CATEGORIAS));
            entidades.add(item("Productos", PRODUCTOS));
            entidades.add(item("Proveedores", PROVEEDORES));
            barra.add(entidades);

            JMenu transacciones = new JMenu("Transacciones");
            transacciones.add(item("Productos de un proveedor", PROVEEDOR_PRODUCTO));
            barra.add(transacciones);
        }

        // Todos pueden consultar
        JMenu consultas = new JMenu("Reportes/Consultas");
        consultas.add(item("Consultas", CONSULTAS));
        consultas.add(item("Reportes", REPORTES));
        barra.add(consultas);

        // Solo el administrador gestiona usuarios y ve la bitácora
        if (usuario.esAdministrador()) {
            JMenu auditoria = new JMenu("Auditoría");
            auditoria.add(item("Usuarios", USUARIOS));
            auditoria.add(item("Bitácora", BITACORA));
            barra.add(auditoria);
        }

        JMenu sesion = new JMenu("Sesión");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        cerrar.addActionListener(e -> salir(true));
        sesion.add(cerrar);
        barra.add(sesion);

        return barra;
    }

    private JMenuItem item(String texto, String carta) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> mostrar(carta));
        return item;
    }

    // ---------- Salida ----------

    /**
     * Registra la salida en la bitácora y cierra la ventana.
     * @param volverAlLogin true: abre el login de nuevo; false: termina la aplicación
     */
    private void salir(boolean volverAlLogin) {
        try {
            loginController.cerrarSesion();
        } catch (NegocioException ex) {
            // La sesión ya se cerró; solo se avisa que no quedó la salida en la bitácora
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
        if (volverAlLogin) {
            new LoginFrame().setVisible(true);
        } else {
            System.exit(0);
        }
    }
}