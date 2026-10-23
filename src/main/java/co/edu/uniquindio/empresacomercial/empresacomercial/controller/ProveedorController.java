package co.edu.uniquindio.empresacomercial.empresacomercial.controller;

import co.edu.uniquindio.empresacomercial.empresacomercial.DAO.ProveedorDAO;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Producto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Proveedor;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.ProveedorProducto;
import co.edu.uniquindio.empresacomercial.empresacomercial.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class ProveedorController {

    private static final int LONGITUD_MAXIMA_NIT = 20;         // VARCHAR(20)
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;     // VARCHAR(100)
    private static final int LONGITUD_MAXIMA_TELEFONO = 20;    // VARCHAR(20)
    private static final int LONGITUD_MAXIMA_DIRECCION = 120;  // VARCHAR(120)
    private static final double PRECIO_MAXIMO = 9_999_999_999.99; // DECIMAL(12,2)
    private static final int ERROR_LLAVE_FORANEA = 547;        // código de SQL Server
    private static final int ERROR_LLAVE_DUPLICADA = 2627;     // código de SQL Server

    private final ProveedorDAO dao = new ProveedorDAO();

    // Consultar lo puede hacer cualquier usuario. Los proveedores traen su lista de productos con precio.
    /**
     * Lista todos los proveedores.
     * @return
     * @throws NegocioException
     */
    public List<Proveedor> listar() throws NegocioException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /**
     * Busca un proveedor por su NIT.
     * @param nit
     * @return
     * @throws NegocioException
     */
    public Proveedor buscarPorNit(String nit) throws NegocioException {
        try {
            return dao.buscarPorNit(nit);
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    // Crea el proveedor junto con sus productos y precios (la lista puede ser null o estar vacía).
    /**
     * Crea un nuevo proveedor con los datos proporcionados y sus productos con precios.
     * @param nit
     * @param nombre
     * @param telefono
     * @param direccion
     * @param idCiudad
     * @param productos
     * @return
     * @throws NegocioException
     */
    public Proveedor crear(String nit, String nombre, String telefono, String direccion,
                           int idCiudad, List<ProveedorProducto> productos) throws NegocioException {
        verificarPermisoModificar();
        nit = validarNit(nit);
        nombre = validarNombre(nombre);
        telefono = validarOpcional(telefono, LONGITUD_MAXIMA_TELEFONO, "El teléfono");
        direccion = validarOpcional(direccion, LONGITUD_MAXIMA_DIRECCION, "La dirección");
        validarCiudad(idCiudad);

        Proveedor proveedor = new Proveedor(nit, nombre, telefono, direccion, idCiudad);
        if (productos != null) {
            for (ProveedorProducto pp : productos) {
                if (pp == null || pp.getProducto() == null) {
                    throw new NegocioException("Hay un producto sin seleccionar.");
                }
                validarPrecio(pp.getPrecio());
                proveedor.agregarProducto(pp.getProducto(), pp.getPrecio()); // evita repetidos
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


    /**
     * Actualiza un proveedor existente con los datos proporcionados.
     * El NIT no se puede cambiar. Los productos se manejan con agregarProducto, actualizarPrecio y quitarProducto.
     * @param nit
     * @param nombre
     * @param telefono
     * @param direccion
     * @param idCiudad
     * @throws NegocioException
     */
    public void actualizar(String nit, String nombre, String telefono, String direccion, int idCiudad) throws NegocioException {
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
    /**
     * Elimina un proveedor por su NIT.
     * @param nit
     * @throws NegocioException
     */
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

    // Vincula un producto con el proveedor al precio indicado.
    /**
     * Agrega un producto a un proveedor con el precio indicado.
     * @param nit
     * @param idProducto
     * @param precio
     * @throws NegocioException
     */
    public void agregarProducto(String nit, int idProducto, double precio) throws NegocioException {
        verificarPermisoModificar();
        validarPrecio(precio);

        try {
            dao.agregarProducto(nit, idProducto, precio);
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

    /**
     * Actualiza el precio de un producto que un proveedor ofrece.
     * Cambia el precio al que el proveedor ofrece un producto.
     * @param nit
     * @param idProducto
     * @param precio
     * @throws NegocioException
     */
    public void actualizarPrecio(String nit, int idProducto, double precio) throws NegocioException {
        verificarPermisoModificar();
        validarPrecio(precio);

        try {
            if (!dao.actualizarPrecio(nit, idProducto, precio)) {
                throw new NegocioException("El proveedor no tiene ese producto.");
            }
        } catch (SQLException e) {
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


    /**
     * Verifica si el usuario actual tiene permisos para modificar datos.
     * Si no tiene permisos, lanza una NegocioException.
     * @throws NegocioException
     */
    private void verificarPermisoModificar() throws NegocioException {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null || !usuario.puedeModificarDatos()) {
            throw new NegocioException("No tiene permisos para modificar datos.");
        }
    }

    /**
     * Valida el NIT del proveedor. Si es nulo o vacío, lanza una NegocioException.
     * @param nit
     * @return
     * @throws NegocioException
     */
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

    /**
     * Valida el nombre del proveedor. Si es nulo o vacío, lanza una NegocioException.
     * @param nombre
     * @return
     * @throws NegocioException
     */
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


    /**
     * Valida un campo opcional. Si el valor es nulo o vacío, devuelve null.
     * Para los datos opcionales: si vienen vacíos se guardan como null.
     * @param valor
     * @param maximo
     * @param nombreCampo
     * @return
     * @throws NegocioException
     */
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

    /**
     * Valida que la ciudad seleccionada sea válida.
     * Si el ID de la ciudad es menor o igual a cero, lanza una NegocioException.
     * @param idCiudad
     * @throws NegocioException
     */
    private void validarCiudad(int idCiudad) throws NegocioException {
        if (idCiudad <= 0) {
            throw new NegocioException("Seleccione una ciudad.");
        }
    }

    /**
     * Valida que el precio sea un número válido y no negativo.
     * Si el precio es NaN o negativo, lanza una NegocioException.
     * @param precio
     * @throws NegocioException
     */
    private void validarPrecio(double precio) throws NegocioException {
        if (Double.isNaN(precio) || precio < 0) {
            throw new NegocioException("El precio no puede ser negativo.");
        }
        if (precio > PRECIO_MAXIMO) {
            throw new NegocioException("El precio es demasiado alto.");
        }
    }

    private NegocioException errorBD(SQLException e) {
        return new NegocioException("Error en la base de datos: " + e.getMessage());
    }
}
