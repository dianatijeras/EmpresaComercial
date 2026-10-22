package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.ProveedorDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Proveedor;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class ProveedorController {

    private static final int LONGITUD_MAXIMA_NIT = 20;         // VARCHAR(20)
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;     // VARCHAR(100)
    private static final int LONGITUD_MAXIMA_TELEFONO = 20;    // VARCHAR(20)
    private static final int LONGITUD_MAXIMA_DIRECCION = 120;  // VARCHAR(120)
    private static final int ERROR_LLAVE_FORANEA = 547;        // código de SQL Server
    private static final int ERROR_LLAVE_DUPLICADA = 2627;     // código de SQL Server

    private final ProveedorDAO dao = new ProveedorDAO();

    // Consultar lo puede hacer cualquier usuario. Los proveedores traen su lista de productos.
    public List<Proveedor> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public Proveedor buscarPorNit(String nit) throws NegocioException {
        try {
            return dao.buscarPorNit(nit);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    // Crea el proveedor junto con sus productos (la lista puede ser null o estar vacía).
    public Proveedor crear(String nit, String nombre, String telefono, String direccion,
                           int idCiudad, List<Producto> productos) throws NegocioException {
        verificarPermisoModificar();
        nit = validarNit(nit);
        nombre = validarNombre(nombre);
        telefono = validarOpcional(telefono, LONGITUD_MAXIMA_TELEFONO, "El teléfono");
        direccion = validarOpcional(direccion, LONGITUD_MAXIMA_DIRECCION, "La dirección");
        validarCiudad(idCiudad);

        Proveedor proveedor = new Proveedor(nit, nombre, telefono, direccion, idCiudad);
        if (productos != null) {
            for (Producto producto : productos) {
                proveedor.agregarProducto(producto); // evita repetidos
            }
        }

        try {
            dao.insertar(proveedor);
            return proveedor;
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_LLAVE_DUPLICADA) {
                throw new NegocioException("Ya existe un proveedor con ese NIT.");
            }
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                throw new NegocioException("La ciudad o alguno de los productos seleccionados no existe.");
            }
            throw errorBD(e);
        }
    }

    // El NIT no se puede cambiar. Los productos se manejan con agregarProducto y quitarProducto.
    public void actualizar(String nit, String nombre, String telefono, String direccion,
                           int idCiudad) throws NegocioException {
        verificarPermisoModificar();
        nombre = validarNombre(nombre);
        telefono = validarOpcional(telefono, LONGITUD_MAXIMA_TELEFONO, "El teléfono");
        direccion = validarOpcional(direccion, LONGITUD_MAXIMA_DIRECCION, "La dirección");
        validarCiudad(idCiudad);

        try {
            if (!dao.actualizar(new Proveedor(nit, nombre, telefono, direccion, idCiudad))) {
                throw new NegocioException("El proveedor no existe.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                throw new NegocioException("La ciudad seleccionada no existe.");
            }
            throw errorBD(e);
        }
    }

    // Al eliminar un proveedor también se borran sus vínculos con productos.
    public void eliminar(String nit) throws NegocioException {
        verificarPermisoModificar();

        try {
            if (!dao.eliminar(nit)) {
                throw new NegocioException("El proveedor no existe.");
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public void agregarProducto(String nit, int idProducto) throws NegocioException {
        verificarPermisoModificar();

        try {
            dao.agregarProducto(nit, idProducto);
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_LLAVE_DUPLICADA) {
                throw new NegocioException("El proveedor ya tiene ese producto.");
            }
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                throw new NegocioException("El proveedor o el producto no existe.");
            }
            throw errorBD(e);
        }
    }

    public void quitarProducto(String nit, int idProducto) throws NegocioException {
        verificarPermisoModificar();

        try {
            if (!dao.quitarProducto(nit, idProducto)) {
                throw new NegocioException("El proveedor no tiene ese producto.");
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    private void verificarPermisoModificar() throws NegocioException {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null || !usuario.puedeModificarDatos()) {
            throw new NegocioException("No tiene permisos para modificar datos.");
        }
    }

    private String validarNit(String nit) throws NegocioException {
        if (nit == null || nit.trim().isEmpty()) {
            throw new NegocioException("El NIT es obligatorio.");
        }
        nit = nit.trim();
        if (nit.length() > LONGITUD_MAXIMA_NIT) {
            throw new NegocioException("El NIT no puede tener más de " + LONGITUD_MAXIMA_NIT + " caracteres.");
        }
        return nit;
    }

    private String validarNombre(String nombre) throws NegocioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NegocioException("El nombre del proveedor es obligatorio.");
        }
        nombre = nombre.trim();
        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new NegocioException("El nombre no puede tener más de " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }
        return nombre;
    }

    // Para los datos opcionales: si vienen vacíos se guardan como null.
    private String validarOpcional(String valor, int maximo, String nombreCampo) throws NegocioException {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        valor = valor.trim();
        if (valor.length() > maximo) {
            throw new NegocioException(nombreCampo + " no puede tener más de " + maximo + " caracteres.");
        }
        return valor;
    }

    private void validarCiudad(int idCiudad) throws NegocioException {
        if (idCiudad <= 0) {
            throw new NegocioException("Seleccione una ciudad.");
        }
    }

    private NegocioException errorBD(SQLException e) {
        return new NegocioException("Error en la base de datos: " + e.getMessage());
    }
}
